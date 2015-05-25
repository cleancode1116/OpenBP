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
package org.openbp.core;

/**
 * Exception indicating a runtime error during the an OpenBP process.
 * This exception may wrap another exception that describes the failure in detail.
 * Note that it indirectly extends from RuntimeException and thus does not need to be declared at the method level.
 *
 * @author Heiko Erhardt
 */
public class OpenBPException extends RuntimeException
{
	/** This holds the error code used to construct this exception. */
	private final String errorCode;

	/** Unrecoverable flag (determines that this exception should not be handled via process mechanisms) */
	private boolean unrecoverable;

	/**
	 * Constructor.
	 *
	 * @param errorCode An error code describing the exception
	 * @param msg A message describing the exception
	 */
	public OpenBPException(String errorCode, String msg)
	{
		super(msg);
		this.errorCode = errorCode;
	}

	/**
	 * Constructor.
	 *
	 * @param errorCode An error code describing the exception
	 * @param exception An exception which is the cause for this exception
	 */
	public OpenBPException(String errorCode, Throwable exception)
	{
		super(exception);
		this.errorCode = errorCode;
	}

	/**
	 * Constructor.
	 *
	 * @param errorCode An error code describing the exception
	 * @param msg A message describing the exception
	 * @param exception An exception which is the cause for this exception
	 */
	public OpenBPException(String errorCode, String msg, Throwable exception)
	{
		super(msg, exception);
		this.errorCode = errorCode;
	}

	/**
	 * This method returns the error code that has been used to construct this exception.
	 *
	 * @return The error code
	 */
	public String getErrorCode()
	{
		return errorCode;
	}

	/**
	 * Gets the unrecoverable flag (determines that this exception should not be handled via process mechanisms).
	 * @nowarn
	 */
	public boolean isUnrecoverable()
	{
		return unrecoverable;
	}

	/**
	 * Sets the unrecoverable flag (determines that this exception should not be handled via process mechanisms).
	 * @nowarn
	 */
	public void setUnrecoverable(boolean unrecoverable)
	{
		this.unrecoverable = unrecoverable;
	}

	/**
	 * Wraps the given exception in an OpenBPException.
	 *
	 * @param t Exception to wrap
	 * @return The original exception if this is an OpenBPException or a new OpenBPException otherwise.
	 */
	public static OpenBPException wrap(Throwable t)
	{
		OpenBPException ret = null;
		if (t instanceof OpenBPException)
			ret = (OpenBPException) t;
		else
			ret = new OpenBPException("GenericException", "Unexpected exception.", t);
		return ret;
	}

	public static OpenBPException wrapUnrecoverable(Throwable t)
	{
		OpenBPException ret = wrap(t);
		ret.setUnrecoverable(true);
		return ret;
	}
}
