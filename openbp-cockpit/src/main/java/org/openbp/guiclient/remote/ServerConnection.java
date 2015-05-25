/*
 *   Copyright 2007 skynamics AG
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.openbp.guiclient.remote;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.openbp.client.remote.RemoteServiceFactory;
import org.openbp.core.OpenBPException;
import org.openbp.core.remote.ClientConnectionInfo;
import org.openbp.core.remote.ClientLoginInfo;
import org.openbp.core.remote.ClientSession;
import org.openbp.core.remote.ClientSessionService;
import org.openbp.core.remote.InvalidSessionException;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventMgr;
import org.openbp.swing.components.JMsgBox;

/**
 * The server manager manages the connection to the OpenBP server.
 * It is especially used to lookup services of the server.
 * The services are cached in a local table, so lookup of a retrieved service is fast.
 *
 * @author Heiko Erhardt
 */
public final class ServerConnection
{
	//////////////////////////////////////////////////
	// @@ Private data
	//////////////////////////////////////////////////

	/** Remote service factory */
	private RemoteServiceFactory remoteServiceFactory;

	/** Connection info to the OpenBP server */
	private ClientConnectionInfo connectionInfo;

	/** Login info for the client session */
	private ClientLoginInfo loginInfo;

	/** The session server on the server. */
	private ClientSessionService sessionService;

	/** Service cache */
	private Hashtable serviceCache;

	/** The session to the server. */
	private ClientSession session;

	/** Debug flag: Disable the continously running timer (useful for profiling and memory leak detection) */
	public static boolean disableTimersForDebug;

	/** Singleton instance */
	private static ServerConnection singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized ServerConnection getInstance()
	{
		if (singletonInstance == null)
		{
			singletonInstance = new ServerConnection();
		}
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private ServerConnection()
	{
		serviceCache = new Hashtable();
	}

	//////////////////////////////////////////////////
	// @@ ClientSession operations
	//////////////////////////////////////////////////

	/**
	 * This method returns the session this model is maintaining, or null,
	 * if no session has been created yet.
	 *
	 * @return The ClientSession object or null
	 */
	public ClientSession getSession()
	{
		return session;
	}

	/**
	 * Gets the connection info to the OpenBP server.
	 * @nowarn
	 */
	public ClientConnectionInfo getConnectionInfo()
	{
		return connectionInfo;
	}

	/**
	 * Sets the connection info to the OpenBP server.
	 * @nowarn
	 */
	public void setConnectionInfo(ClientConnectionInfo connectionInfo)
	{
		this.connectionInfo = connectionInfo;
	}

	/**
	 * Gets the login info for the client session.
	 * @nowarn
	 */
	public ClientLoginInfo getLoginInfo()
	{
		return loginInfo;
	}

	/**
	 * Sets the login info for the client session.
	 * @nowarn
	 */
	public void setLoginInfo(ClientLoginInfo loginInfo)
	{
		this.loginInfo = loginInfo;
	}

	//////////////////////////////////////////////////
	// @@ Server connection
	//////////////////////////////////////////////////

	/**
	 * Tries to connect to the server.
	 * Also establishes a session and starts the server event polling if login information is present.
	 *
	 * @param throwError
	 *		true	Throws an exception in case of error.<br>
	 *		false	Silently ignores if no server is present. However, throws exceptions if other error occur.
	 * @throws OpenBPException On error
	 */
	public void connect(boolean throwError)
	{
		try
		{
			disconnect();

			if (connectionInfo == null)
			{
				connectionInfo = new ClientConnectionInfo();
			}
			connectionInfo.loadFromProperties();

			if (connectionInfo.isEnabled())
			{
				if (remoteServiceFactory == null)
				{
					remoteServiceFactory = RemoteServiceFactory.getInstance();
				}
				remoteServiceFactory.setConnectionInfo(connectionInfo);

				// Locate the session service...
				sessionService = (ClientSessionService) remoteServiceFactory.getService(ClientSessionService.class);

				if (loginInfo != null)
				{
					// ...and create a new session
					session = sessionService.createSession(loginInfo);

					if (!disableTimersForDebug)
					{
						// When we have a session,
						(new ClientHeartBeat(3)).start();
					}
				}
			}
		}
		catch (Exception e)
		{
			throw OpenBPException.wrap(e);
		}
	}

	/**
	 * Disconnects from the server.
	 */
	public void disconnect()
	{
		remoteServiceFactory = null;
		sessionService = null;
		session = null;
		serviceCache.clear();
	}

	/**
	 * Checks if we are connected to the server.
	 */
	public boolean isConnected()
	{
		return session != null;
	}

	//////////////////////////////////////////////////
	// @@ Service lookup
	//////////////////////////////////////////////////

	/**
	 * Looks up a service of the server, throwing an exception on error.
	 *
	 * @param serviceCls Class of the service to lookup
	 * @return The service
	 * @throws OpenBPException If the service cannot be found on the server
	 */
	public Object lookupService(Class serviceCls)
	{
		return obtainService(serviceCls, true);
	}

	/**
	 * Looks up a service of the server, ignoring errors silently.
	 *
	 * @param serviceCls Class of the service to lookup
	 * @return The service or null if not found
	 */
	public Object lookupOptionalService(Class serviceCls)
	{
		return obtainService(serviceCls, false);
	}

	/**
	 * Looks up a service of the server.
	 *
	 * @param serviceCls Service class
	 * @param throwError
	 *		true	Throws an exception in case of error.<br>
	 *		false	Silently ignores errors.
	 * @return The service
	 * @throws OpenBPException If the service cannot be found on the server
	 */
	public Object obtainService(Class serviceCls, boolean throwError)
	{
		Object cacheContent = serviceCache.get(serviceCls.getName());

		if (cacheContent == Boolean.FALSE)
		{
			if (throwError)
			{
				throw new OpenBPException("ServerConnection.Unvavailable", "Service not available, try server reconnect.");
			}
			return null;
		}

		Object service = cacheContent;

		if (service == null)
		{
			// Service not in cache
			if (remoteServiceFactory == null)
			{
				// No service registry
				if (throwError)
				{
					throw new OpenBPException("ServerConnection.Unvavailable", "Service not available, try server reconnect.");
				}
				return null;
			}

			try
			{
				// Look up the remote service
				service = remoteServiceFactory.getService(serviceCls);

				// Save to cache
				serviceCache.put(serviceCls.getName(), service);
			}
			catch (OpenBPException e)
			{
				// Save error marker to cache
				serviceCache.put(serviceCls.getName(), Boolean.FALSE);

				if (throwError)
				{
					throw new OpenBPException("ServerConnection.Unvavailable", "Error obtaining service " + serviceCls.getName() + ".", e);
				}
			}
		}

		return service;
	}

	//////////////////////////////////////////////////
	// @@ Event handling
	//////////////////////////////////////////////////

	/**
	 * This method iterates over a given Set with event names as received from the
	 * server and re-fires each of them as {@link JaspiraEvent}
	 *
	 * @param eventSet The set of ClientSession Event names
	 */
	void fireEventSet(Set eventSet)
	{
		// Iterate over all events in the set.
		Iterator eventIter = eventSet.iterator();
		while (eventIter.hasNext())
		{
			// Get the event.
			String eventName = (String) eventIter.next();

			// Broadcast the event.
			JaspiraEventMgr.fireGlobalEvent(eventName);
		}
	}

	/**
	 * This method is called when an {@link InvalidSessionException} has been caught
	 * during pinging the server.
	 *
	 * @param exception The exception caught during the server request
	 */
	void handleInvalidSession(Exception exception)
	{
		disconnect();

		// Let the Swing event queue thread display a connection lost message.
		// TOLOCALIZE
		String msg = "The connection to the OpenBP Server has been lost. Cause:\n\n{0}\n\nEither the server was shut down or the session timed out.\nMake sure that the server is running and press the Reload button to reconnect.";
		JMsgBox.showFormat(null, msg, exception, JMsgBox.ICON_ERROR | JMsgBox.TYPE_OKLATER);
	}

	//////////////////////////////////////////////////
	// @@ Inner classes
	//////////////////////////////////////////////////

	/**
	 * This class pings the server at regular intervals to keep the client's session alive
	 * on the server side and to retrieve events sent from the server to the client.
	 */
	private class ClientHeartBeat extends Thread
	{
		/** This holds the time between two pings in seconds. */
		private long interval;

		/**
		 * The constructor.
		 *
		 * @param interval The time between two pings (seconds)
		 */
		public ClientHeartBeat(int interval)
		{
			super("Server event poller");

			// Keep the interval.
			this.interval = interval * 1000;

			// This thread must not prevent VM shutdown.
			setDaemon(true);
		}

		/**
		 * This method implements the process of pinging the server.
		 */
		public void run()
		{
			while (session != null)
			{
				try
				{
					// Get the event set.
					Set eventSet = sessionService.getSessionEvents(session);

					// Fire the events as Jaspira events if there are any.
					if (eventSet != null)
					{
						fireEventSet(eventSet);
					}
				}
				catch (InvalidSessionException e)
				{
					// Handle expiration of session (reconnect?).
					handleInvalidSession(e);

					// For now, we give up because we cannot re-establish the server connection
					break;
				}
				catch (OpenBPException e)
				{
					// Handle expiration of session (reconnect?).
					handleInvalidSession(e);

					// For now, we give up because we cannot re-establish the server connection
					break;
				}

				// Try to sleep...
				try
				{
					sleep(interval);
				}
				catch (InterruptedException ie)
				{
					// Because of the loop, there is no need to do something here.
				}
			}
		}
	}
}
