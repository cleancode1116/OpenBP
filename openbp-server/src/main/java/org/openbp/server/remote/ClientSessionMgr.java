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
package org.openbp.server.remote;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.openbp.common.setting.SettingUtil;
import org.openbp.common.util.ExpirationHashtable;
import org.openbp.core.remote.ClientLoginInfo;
import org.openbp.core.remote.ClientSession;
import org.openbp.core.remote.InvalidSessionException;
import org.openbp.server.ServerConstants;

/**
 * This class manages sessions ({@link ClientSession}),
 * i\.e\. connections between a client and an OpenBP server.
 *
 * @author Falk Hartmann
 */
public final class ClientSessionMgr
{
	//////////////////////////////////////////////////
	// @@  Data members.
	//////////////////////////////////////////////////

	/** This holds the {@link ClientSession} objects currently active (mapped to session event {@link java.util.Set}). */
	private ExpirationHashtable sessions;

	/** This holds the one and only instance of this class. */
	private static ClientSessionMgr singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	private ClientSessionMgr()
	{
		// Determine timeout from the settings
		long timeout = SettingUtil.getIntSetting(ServerConstants.SYSPROP_CLIENTSESSION_TIMEOUT, 24 * 60 * 60);

		// Initialize session table.
		sessions = new ExpirationHashtable(timeout * 1000);
	}

	/**
	 * This returns the one and only instance of this class.
	 *
	 * @return The session manager
	 */
	public static synchronized ClientSessionMgr getInstance()
	{
		if (singletonInstance == null)
		{
			singletonInstance = new ClientSessionMgr();
		}
		return singletonInstance;
	}

	//////////////////////////////////////////////////
	// @@ Session management
	//////////////////////////////////////////////////

	/**
	 * This creates a session using the given Login data.
	 *
	 * @param loginInfo The data for the login
	 * @return The created session
	 */
	public ClientSession createSession(ClientLoginInfo loginInfo)
	{
		// Create session id.
		String sessionID = Long.toString(((long) (Long.MAX_VALUE * Math.random())));

		// Create session.
		ClientSession session = new ClientSession(sessionID);

		// Hold session in the expiration hashtable.
		sessions.put(session, new HashSet());

		// Initialize and return the session.
		return session;
	}

	/**
	 * This method checks whether the given section is valid.
	 *
	 * @param session The session to be checked
	 * @throws InvalidSessionException If the session is invalid
	 */
	public void checkSession(ClientSession session)
		throws InvalidSessionException
	{
		// Lookup session.
		if (sessions.get(session) == null)
		{
			// ...not found - thus illegal.
			throw new InvalidSessionException();
		}
	}

	//////////////////////////////////////////////////
	// @@ Event management
	//////////////////////////////////////////////////

	/**
	 * This method puts the passed event into the event sets of all registered sessions.
	 *
	 * @param eventName The event name to be fired
	 */
	public void broadcastSessionEvent(String eventName)
	{
		// For each session event set...
		Enumeration eventSets = sessions.elements();
		while (eventSets.hasMoreElements())
		{
			// Get event set.
			Set eventSet = (Set) eventSets.nextElement();

			// Add the event.
			eventSet.add(eventName);
		}
	}

	/**
	 * This method puts the passed event into the event set of the passed session.
	 *
	 * @param session The session to which the event should be propagated
	 * @param eventName The name of the event to be fired
	 * @throws InvalidSessionException If the passed session is invalid
	 */
	public void postSessionEvent(ClientSession session, String eventName)
		throws InvalidSessionException
	{
		// Get session event set for the session.
		Set eventSet = (Set) sessions.get(session);

		// If session could not be found, throw the exception.
		if (session == null)
		{
			throw new InvalidSessionException();
		}

		// Ok, so add the event.
		eventSet.add(eventName);
	}

	/**
	 * This method gets the event set for the passed session, thereby clearing
	 * the event set for the session.
	 *
	 * @param session The session to retrieve the event set for
	 * @return The event set for the passed session or null if there are no events
	 * for the specified session
	 */
	public Set popSessionEvents(ClientSession session)
	{
		// Get the events
		Set eventSet = (Set) sessions.get(session);
		if (eventSet.isEmpty())
		{
			// No events, so return null.
			return null;
		}

		// Clear the session events.
		sessions.put(session, new HashSet());

		// Return the events so far.
		return eventSet;
	}
}
