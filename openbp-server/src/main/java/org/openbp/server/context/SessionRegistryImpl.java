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
package org.openbp.server.context;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import org.openbp.common.CommonRegistry;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.util.DisposalListener;
import org.openbp.common.util.ExpirationHashtable;
import org.openbp.common.util.ToStringHelper;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.modelmgr.ModelMgr;
import org.openbp.core.model.modelmgr.ModelNotificationService;
import org.openbp.server.engine.EngineUtil;

/**
 * The context manager keeps a store of the session context ({@link TokenContext})
 * for each session that has been opened.
 * Each context is identified by the session id.<br>
 * When context is created, it is also passed an expiration time, after which the context
 * will be removed from the store. The expiration time should be identical to the expiration
 * time of the session that created the context.
 *
 * @author Heiko Erhardt
 */
public class SessionRegistryImpl
	implements SessionRegistry, DisposalListener
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Hashtable mapping session id's (Strings) to {@link TokenContext} objects */
	private ExpirationHashtable contextMap;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor.
	 */
	public SessionRegistryImpl()
	{
		// Initialize session context map with a timeout of 10 x one hour.
		contextMap = new ExpirationHashtable(0);

		// Add a disposal listener for the hashtable.
		contextMap.setDisposalListener(this);
	}

	/**
	 * Returns a string represenation of this object.
	 *
	 * @return Debug string containing the most important properties of this object
	 */
	public String toString()
	{
		return ToStringHelper.toString(this);
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Gets a token context according to a session id.
	 * If a valid context is found for this session, the access timestamp of the
	 * context is updated, so it will remain in the store.
	 *
	 * @param sessionId Session id of the context
	 * @return The context or null if no context exists for this session or the
	 * context has timed out.
	 */
	public TokenContext lookupSession(Object sessionId)
	{
		return (TokenContext) contextMap.get(sessionId);
	}

	/**
	 * Adds a context to the store.
	 *
	 * @param sessionId Id of the context
	 * @param context Context to add
	 * @param timeout Time in seconds that the context may remain inactive.
	 * If this time has expired, the context will be removed from the session.
	 */
	public void registerSession(Object sessionId, TokenContext context, long timeout)
	{
		LogUtil.trace(getClass(), "Registered session id $0 (timeout: {1} sec). [{2}]", sessionId, new Long(timeout), context);

		// Store session context.
		contextMap.put(sessionId, context, timeout * 1000);

		LogUtil.debug(getClass(), "SessionRegistryImpl.registerSession called for session $0. Now maintaining $1 session(s).", sessionId, Integer.valueOf(contextMap.size()));

		// TODO Feature 4 Trigger session creation event
	}

	/**
	 * Removes a session context with the given session id from the session map.
	 *
	 * @param sessionId Id of the context to unregister
	 */
	public void unregisterSession(Object sessionId)
	{
		// ...this should be told the session context, too.
		if (sessionId != null)
		{
			onDispose(sessionId);

			// ...remove the context from the map.
			contextMap.remove(sessionId);

			LogUtil.debug(getClass(), "SessionRegistryImpl.unregisterSession called. Now maintaining $0 session(s).", Integer.valueOf(contextMap.size()));
		}
	}

	/**
	 * Removes a session context from the session map by searching the map for the context.
	 * This method is much slower than the {@link #unregisterSession(Object)} method.
	 *
	 * @param context Context to remove
	 */
	public void unregisterSession(TokenContext context)
	{
		for (Iterator it = contextMap.entrySet().iterator(); it.hasNext();)
		{
			Map.Entry entry = (Map.Entry) it.next();

			TokenContext c = (TokenContext) entry.getValue();

			if (c == context)
			{
				Object key = entry.getKey();

				// ...this should be told the session context, too.
				onDispose(key);

				// ...remove the context from the map.
				it.remove();

				break;
			}
		}

		LogUtil.debug(getClass(), "SessionRegistryImpl.unregisterSession called. Now maintaining $0 session(s).", Integer.valueOf(contextMap.size()));
	}


	//////////////////////////////////////////////////
	// @@ ModelNotificationObserver implementation
	//////////////////////////////////////////////////

	/**
	 * Notification method for model updates.
	 *
	 * @param qualifier Qualifier of the object that has been updated
	 * @param mode Type of model update ({@link ModelNotificationService#ADDED}/{@link ModelNotificationService#UPDATED}/{@link ModelNotificationService#REMOVED})
	 */
	public void modelUpdated(ModelQualifier qualifier, int mode)
	{
		boolean ret = false;
		if (qualifier.getItem() != null && ItemTypes.PROCESS.equals(qualifier.getItemType()))
		{
			ProcessItem process = (ProcessItem) ((ModelMgr) CommonRegistry.lookup(ModelMgr.class)).getItemByQualifier(qualifier, true);
			if (process != null)
			{
				for (Enumeration en = contextMap.elements(); en.hasMoreElements();)
				{
					TokenContext context = (TokenContext) en.nextElement();
					ret &= performProcessUpdateOnContext(context, process);
				}
			}
		}
	}

	/**
	 * Resets all models.
	 * Re-initializes the model classloader and the model properties and reinitializes the components of the model.
	 */
	public void requestModelReset()
	{
	}

	//////////////////////////////////////////////////
	// @@ Process updates
	//////////////////////////////////////////////////

	/**
	 * Checks if we reference any sockets of the supplied process and refreshes
	 * the socket reference if appropriate.
	 * 
	 * @param context Token context
	 * @param process
	 *            Updated process
	 * @return true All updates have been performed successfully.\n false The
	 *         context references one or more sockets that do not exist any more
	 *         in the updated process.
	 */
	public boolean performProcessUpdateOnContext(TokenContext context, ProcessItem process)
	{
		boolean result = true;

		// First, give the call stack a chance to update itself.
		if (!context.getCallStack().performProcessUpdate(process))
			result = false;

		// Second, try to update current position.
		NodeSocket currentSocket = context.getCurrentSocket();
		if (currentSocket != null && currentSocket.getProcess() == process)
		{
			NodeSocket newSocket = EngineUtil.updateSocketReference(currentSocket, process);
			if (newSocket != null)
			{
				currentSocket = newSocket;
			}
			else
			{
				result = false;
			}
		}

		for (Iterator it = context.getChildContexts(); it.hasNext();)
		{
			TokenContext cc = (TokenContext) it.next();
			performProcessUpdateOnContext(cc, process);
		}

		return result;
	}

	/**
	 * Requests the termination of processes that are debugged by the specified debugger.
	 *
	 * @param debuggerId Debugger client id
	 */
	public void requestSessionAbort(String debuggerId)
	{
		for (Enumeration en = contextMap.keys(); en.hasMoreElements();)
		{
			// Get the key...
			Object key = en.nextElement();

			// ...and the token context.
			TokenContext context = (TokenContext) contextMap.get(key);

			// ...and compare the debugger id with the passed one.
			if (debuggerId.equals(context.getDebuggerId()))
			{
				// Remove the context from the map.
				contextMap.remove(key);

				// Request termination.
				TokenContextUtil.requestTermination(context);
			}
		}
	}

	/**
	 * For toString debugging only.
	 * @nowarn
	 */
	protected ExpirationHashtable getContextMap()
	{
		return contextMap;
	}

	//////////////////////////////////////////////////
	// @@ DisposalListener
	//////////////////////////////////////////////////

	/**
	 * This method is called when a session is removed from the TokenContextMgrImpl, either
	 * explicitly or by expiration.
	 *
	 * @param key The session id
	 */
	public void onDispose(Object key)
	{
		// Get the corresponding session.
		if (key != null)
		{
			// TokenContext context = (TokenContext) contextMap.get(key);

			// TODO Feature 4 Trigger session destroy event
		}
	}
}
