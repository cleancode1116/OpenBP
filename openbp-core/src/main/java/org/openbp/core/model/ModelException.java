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
package org.openbp.core.model;

import org.openbp.core.OpenBPException;

/**
 * Exception that indicates an inconsistency in the process model.
 * In general, this exception is thrown when an object that is specified by some name in the process model
 * cannot be found (e. g. a data type definition or a sub process name or such).
 * In general, model exceptions are not recoverable, i. e. cannot be handled by process error mechanisms.
 *
 * @author Author: Heiko Erhardt
 */
public class ModelException extends OpenBPException
{
	/**
	 * Constructor.
	 *
	 * @param errorCode An error code describing the exception
	 * @param msg A message describing the exception
	 */
	public ModelException(String errorCode, String msg)
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
	public ModelException(String errorCode, Throwable exception)
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
	public ModelException(String errorCode, String msg, Throwable exception)
	{
		super(errorCode, msg, exception);
		setUnrecoverable(true);
	}
}
