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
package org.openbp.core.engine;

import org.openbp.core.OpenBPException;

/**
 * Exception that indicates a process execution failure that is likely to be a programming error.
 * The range is wide, from parameter binding up to missing jump targets or process inconsistencies.
 * In general, engine exceptions are not recoverable, i. e. cannot be handled by process error mechanisms.
 *
 * @author Author: Heiko Erhardt
 */
public class EngineException extends OpenBPException
{
	/**
	 * Constructor.
	 *
	 * @param errorCode An error code describing the exception
	 * @param msg A message describing the exception
	 */
	public EngineException(String errorCode, String msg)
	{
		super(errorCode, msg);
		setUnrecoverable(true);
	}

	/**
	 * Constructor.
	 *
	 * @param errorCode An error code describing the exception
	 * @param exception An exception which is the cause for this exception
	 */
	public EngineException(String errorCode, Throwable exception)
	{
		super(errorCode, exception);
		setUnrecoverable(true);
	}

	/**
	 * Constructor.
	 *
	 * @param errorCode An error code describing the exception
	 * @param msg A message describing the exception
	 * @param exception An exception which is the cause for this exception
	 */
	public EngineException(String errorCode, String msg, Throwable exception)
	{
		super(errorCode, msg, exception);
		setUnrecoverable(true);
	}
}
