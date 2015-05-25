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

import org.openbp.core.OpenBPException;

/**
 * This is an exception that is thrown by the ClientSessionService, if a check
 * for validity of a session  has failed. This can be caused by manipulation
 * of a session or by a timeout.
 *
 * @author Falk Hartmann
 */
public class InvalidSessionException extends OpenBPException
{
	/**
	 * Constructor.
	 */
	public InvalidSessionException()
	{
		super("InvalidClientSession", "Invalid client session or client session has expired.");
	}
}
