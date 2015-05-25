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

import org.openbp.server.context.TokenContext;

/**
 * The engine exception handler event is raised when an exception occured during the execution of a process.
 *
 * The {@link #setHandlingOption} attribute can be used to indicate to the engine how to proceed with the exception.
 * By default, the engine will try to continue execution at the error socket of the current node
 * and throw an exception if there is no error socket (handling option {@link #HANDLING_OPTION_ERROR_SOCKET}).<br>
 * The handling option {@link #HANDLING_OPTION_CONTINUE} indicates that the handler has taken action and wants the process
 * to continue at the current socket. The handler might have changed the current socket also.<br>
 * The {@link #HANDLING_OPTION_RETHROW} option instructs the engine to propagate the error without any corrective action.
 *
 * @author Heiko Erhardt
 */
public class EngineExceptionHandlerEvent extends EngineEvent
{
	//////////////////////////////////////////////////
	// @@ Event type constants
	//////////////////////////////////////////////////

	/** Event type: Node Entry */
	public static final String HANDLE_EXCEPTION = "handleexception";

	/** Table of all possible values */
	public static final String[] SUPPORTED_EVENT_TYPES =
	{
		HANDLE_EXCEPTION,
	};

	/** Exception handling option: Continue execution at an error socket of the current socket (default) */
	public static final int HANDLING_OPTION_ERROR_SOCKET = 0;

	/** Exception handling option: Continue the process (exception handler might has set a new current position in the context) */
	public static final int HANDLING_OPTION_CONTINUE = 1;

	/** Exception handling option: Propagate the exception (i. e. rethrow it) */
	public static final int HANDLING_OPTION_RETHROW = 2;

	/**
	 * Returns a list of supported event types.
	 * @nowarn
	 */
	public static String[] getSupportedEventTypes()
	{
		return SUPPORTED_EVENT_TYPES;
	}

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Exception that has occurred (for {@link EngineExceptionHandlerEvent#HANDLE_EXCEPTION} event only) */
	private Throwable exception;

	/** Exception handling option */
	private int handlingOption = HANDLING_OPTION_ERROR_SOCKET;

	/** Engine */
	private Engine engine;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Value constructor.
	 *
	 * @param context Token context
	 * @param eventType Event type (see {@link EngineExceptionHandlerEvent} class)
	 * @param exception Exception that has occurred (for {@link EngineExceptionHandlerEvent#HANDLE_EXCEPTION} event only)
	 * @param engine Engine
	 */
	public EngineExceptionHandlerEvent(String eventType, TokenContext context, Throwable exception, Engine engine)
	{
		super(eventType, context, engine);
		this.exception = exception;
	}

	/**
	 * Returns a string representation of the object.
	 *
	 * @return The multi-line string includes the event type and all information that is
	 * present in respect to this type.
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		String eventType = getEventType();

		sb.append("Event = " + eventType + "\n");

		if (getException() != null)
		{
			sb.append("\tException = " + getException() + "\n");
		}
		return sb.toString();
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the exception that has occurred (for {@link EngineExceptionHandlerEvent#HANDLE_EXCEPTION} event only).
	 * @nowarn
	 */
	public Throwable getException()
	{
		return exception;
	}

	/**
	 * Gets the exception handling option.
	 * @return {@link #HANDLING_OPTION_ERROR_SOCKET} | {@link #HANDLING_OPTION_CONTINUE} | {@link #HANDLING_OPTION_RETHROW}
	 */
	public int getHandlingOption()
	{
		return handlingOption;
	}

	/**
	 * Sets the exception handling option.
	 * @param handlingOption {@link #HANDLING_OPTION_ERROR_SOCKET} | {@link #HANDLING_OPTION_CONTINUE} | {@link #HANDLING_OPTION_RETHROW}
	 */
	public void setHandlingOption(int handlingOption)
	{
		this.handlingOption = handlingOption;
	}

	/**
	 * Gets the engine.
	 * @nowarn
	 */
	public Engine getEngine()
	{
		return engine;
	}

	/**
	 * Sets the engine.
	 * @nowarn
	 */
	public void setEngine(Engine engine)
	{
		this.engine = engine;
	}
}
