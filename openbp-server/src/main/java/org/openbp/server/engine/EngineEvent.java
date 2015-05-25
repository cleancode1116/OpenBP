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

import org.openbp.common.util.ToStringHelper;
import org.openbp.common.util.observer.ObserverEvent;
import org.openbp.server.context.TokenContext;

/**
 * Observer event class used to indicate all types of engine events.
 *
 * @author Heiko Erhardt
 */
public class EngineEvent
	implements ObserverEvent, EngineContext
{
	//////////////////////////////////////////////////
	// @@ Event type constants
	//////////////////////////////////////////////////

	/** Event type: A new token has been started. */
	public static final String BEGIN_TOKEN = "begintoken";

	/** Event type: A token is about to be ended. */
	public static final String BEFORE_END_TOKEN = "beforeendtoken";

	/** Event type: A token has been ended. */
	public static final String AFTER_END_TOKEN = "afterendtoken";

	/** Event type: Engine ended to execute a portion of the process. */
	public static final String TOKEN_STATE_CHANGE = "tokenstatechange";

	/** Event type: Engine begins to execute a portion of the process. */
	public static final String BEGIN_EXECUTION = "beginexecution";

	/** Event type: Engine ended to execute a portion of the process. */
	public static final String END_EXECUTION = "endexecution";

	/** Table of all possible values */
	public static final String [] SUPPORTED_EVENT_TYPES = 
	{ BEGIN_EXECUTION, END_EXECUTION, BEGIN_TOKEN, BEFORE_END_TOKEN, AFTER_END_TOKEN, TOKEN_STATE_CHANGE };

	/**
	 * Returns a list of supported event types.
	 * @nowarn
	 */
	public static String [] getSupportedEventTypes()
	{
		return SUPPORTED_EVENT_TYPES;
	}

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Event type (one of the constants of this class) */
	protected String eventType;

	/** Skip subsequent observers */
	private boolean skipSubsequentObservers;

	/** Token context */
	private TokenContext context;

	/** Engine */
	private Engine engine;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Value constructor.
	 *
	 * @param eventType Event type
	 * @param context Token context
	 * @param engine Engine
	 */
	public EngineEvent(String eventType, TokenContext context, Engine engine)
	{
		this.eventType = eventType;
		this.context = context;
		this.engine = engine;
	}

	/**
	 * Returns a string representation of the object.
	 *
	 * @return The multi-line string includes the event type and all information that is
	 * present in respect to this type.
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "context");
	}

	/**
	 * Gets the event type.
	 * @return The event type
	 */
	public String getEventType()
	{
		return eventType;
	}

	/**
	 * Template method that signalizes if subsequent observers shall be skipped.
	 * @return true to break the observer calling loop for this event
	 */
	public boolean shallSkipSubsequentObservers()
	{
		return skipSubsequentObservers;
	}

	/**
	 * Instructs the system to skip subsequent observers.
	 * @nowarn
	 */
	public void skipSubsequentObservers()
	{
		skipSubsequentObservers = true;
	}

	/**
	 * Gets the token context.
	 * @nowarn
	 * @deprecated Use {@link #getTokenContext} instead
	 */
	public TokenContext getContext()
	{
		return getTokenContext();
	}

	/**
	 * Sets the token context.
	 * @nowarn
	 * @deprecated Use {@link #setTokenContext} instead
	 */
	protected void setContext(TokenContext context)
	{
		setTokenContext(context);
	}

	/**
	 * Gets the token context.
	 * @nowarn
	 */
	public TokenContext getTokenContext()
	{
		return context;
	}

	/**
	 * Sets the token context.
	 * @nowarn
	 */
	public void setTokenContext(TokenContext context)
	{
		this.context = context;
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
