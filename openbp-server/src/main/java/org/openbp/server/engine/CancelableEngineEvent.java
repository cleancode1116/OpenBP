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
package org.openbp.server.engine;

import org.openbp.common.util.ToStringHelper;
import org.openbp.server.context.TokenContext;

/**
 * Engine event that supports cancellation of the action that caused the event by an event observer.
 *
 * @author Heiko Erhardt
 */
public class CancelableEngineEvent extends EngineEvent
{
	//////////////////////////////////////////////////
	// @@ Event type constants
	//////////////////////////////////////////////////

	/** Event type: Node Entry */
	public static final String SHALL_EXECUTE_TOKEN = "shallexecutetoken";

	/** Table of all possible values */
	public static final String[] SUPPORTED_EVENT_TYPES =
	{
		SHALL_EXECUTE_TOKEN,
	};

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

	/** Flag if the action that caused the event should be canceled */
	private boolean canceled;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Value constructor.
	 *
	 * @param context Token context
	 * @param eventType Event type (see {@link EngineTraceEvent} class)
	 * @param engine Engine
	 */
	public CancelableEngineEvent(String eventType, TokenContext context, Engine engine)
	{
		super(eventType, context, engine);
	}

	/**
	 * Returns a string representation of the object.
	 *
	 * @return The multi-line string includes the event type and all information that is
	 * present in respect to this type.
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "context", "canceled");
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the flag if the action that caused the event should be canceled.
	 * @nowarn
	 */
	public boolean isCanceled()
	{
		return canceled;
	}

	/**
	 * Sets the flag if the action that caused the event should be canceled.
	 * @nowarn
	 */
	public void setCanceled(boolean canceled)
	{
		this.canceled = canceled;
	}
}
