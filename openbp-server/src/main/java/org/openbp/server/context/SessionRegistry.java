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

import org.openbp.core.model.modelmgr.ModelNotificationObserver;

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
public interface SessionRegistry extends ModelNotificationObserver
{
	/**
	 * Gets a token context according to a session id.
	 * If a valid context is found for this session, the access timestamp of the
	 * context is updated, so it will remain in the store.
	 *
	 * @param sessionId Session id of the context
	 * @return The context or null if no context exists for this session or the
	 * context has timed out.
	 */
	public TokenContext lookupSession(Object sessionId);

	/**
	 * Adds a context to the store.
	 *
	 * @param sessionId Id of the context
	 * @param context Context to add
	 * @param expirationTime Time in seconds that the context may remain inactive.
	 * If this time has expired, the context will be removed from the session.
	 */
	public void registerSession(Object sessionId, TokenContext context, long expirationTime);

	/**
	 * Removes a session context from the session map.
	 *
	 * @param context Context to remove
	 */
	public void unregisterSession(TokenContext context);

	/**
	 * Removes a session context with the given session id from the session map.
	 *
	 * @param sessionId Id of the context to unregister
	 */
	public void unregisterSession(Object sessionId);

	/**
	 * Requests the termination of processes that are debugged by the specified debugger.
	 *
	 * @param debuggerId Debugger client id
	 */
	public void requestSessionAbort(String debuggerId);
}
