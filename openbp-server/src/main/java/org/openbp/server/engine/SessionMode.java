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
package org.openbp.server.engine;

/**
 * Session mode.
 *
 * @author Heiko Erhardt
 */
public final class SessionMode
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private SessionMode()
	{
	}

	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	/**
	 * Manual session registry.
	 * The application is responsible for registering the session.
	 * The process engine will unregister the session only in case of an unrecoverable exception.
	 */
	public static final int MANUAL = 0;

	/**
	 * Automatic session registry.
	 * A session will be registered when the execution of a process starts.
	 * The session will be destroyed when the execution of the process ends (i. e. when the process is suspended).
	 */
	public static final int AUTO = 1;

}
