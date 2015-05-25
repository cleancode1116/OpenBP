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
package org.openbp.core.remote;

import java.util.Set;

import org.openbp.core.OpenBPException;

/**
 * This interface defines methods for session management, i.e.,
 * creating and checking a session.
 *
 * @author Falk Hartmann
 */
public interface ClientSessionService
{
	//////////////////////////////////////////////////
	// @@ Operations
	//////////////////////////////////////////////////

	/**
	 * This method uses the passed ClientLoginInfo to create a session.
	 *
	 * @param loginInfo The {@link ClientLoginInfo} needed to create a session
	 * @return The created session
	 * @throws OpenBPException in case of an RMI problem
	 */
	public ClientSession createSession(ClientLoginInfo loginInfo);

	/**
	 * The method returns a set of events that has been
	 * broadcasted on the server or has been sent specifically for the session.
	 * The method should be called on a regular basis from each client.
	 * In addition, the session gets touched, i.e., the sessions last
	 * accessed date is renewed.
	 *
	 * @param session The {@link ClientSession} to be pinged
	 * @return The set of events that are of interest to the client or null if
	 * there are currently no events.
	 * @throws OpenBPException in case of an RMI problem
	 */
	public Set getSessionEvents(ClientSession session);
}
