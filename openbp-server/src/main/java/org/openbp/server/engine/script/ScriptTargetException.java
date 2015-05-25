/*
 *   Copyright 2009 skynamics AG
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
package org.openbp.server.engine.script;

import org.openbp.core.OpenBPException;

/**
 * Exception that wraps another exception that occured within a script.
 *
 * @author Heiko Erhardt
 */
public class ScriptTargetException extends OpenBPException
{
	/**
	 * Constructor.
	 *
	 * @param exception An exception which is the cause for this exception
	 */
	public ScriptTargetException(Throwable exception)
	{
		super("ScriptTargetException", exception);
	}

	/**
	 * Constructor.
	 *
	 * @param msg A message describing the exception
	 * @param exception An exception which is the cause for this exception
	 */
	public ScriptTargetException(String msg, Throwable exception)
	{
		super("ScriptTargetException", msg, exception);
	}
}
