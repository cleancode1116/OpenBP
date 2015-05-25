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
package org.openbp.jaspira.event;

import org.openbp.jaspira.plugin.EventModule;
import org.openbp.jaspira.plugin.Plugin;

/**
 * Event that precedes vetoable changes.
 * Allows plugins to veto, i.e. prevent the execution of a certain action.
 *
 * @author Stephan Moritz
 */
public class VetoableEvent extends JaspiraEvent
{
	/** Indicates if a veto has been received, never changes back once true. */
	public boolean vetoed;

	/** A second message object useful for oldState - newState messages. */
	protected Object newObject;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new VetoEvent with an old and a new state.
	 * @param source The plugin that dispatches this event. Must not be null.
	 * Will be converted to lower case.
	 * @param eventName The name of the event
	 * @param oldObject Original object. Can be null.
	 * @param newObject Modified object. Can be null.
	 */
	public VetoableEvent(Plugin source, String eventName, Object oldObject, Object newObject)
	{
		super(source, eventName, oldObject);

		this.newObject = newObject;
	}

	/**
	 * Creates a VetoEvent without additional information.
	 * @param source The plugin that dispatches this event. Must not be null.
	 * Will be converted to lower case.
	 * @param eventName The name of the event
	 */
	public VetoableEvent(Plugin source, String eventName)
	{
		super(source, eventName);
	}

	/**
	 * Creates a VetoEvent with a single message object.
	 * @param source The plugin that dispatches this event. Must not be null.
	 * Will be converted to lower case.
	 * @param eventName The name of the event
	 * @param object The argument object
	 */
	public VetoableEvent(Plugin source, String eventName, Object object)
	{
		super(source, eventName, object);
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Access
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the veto-status of this event.
	 * @return
	 *		true	If a veto has been placed.<br>
	 *		false	if it has benn accepted so far.
	 */
	public boolean isVetoed()
	{
		return vetoed;
	}

	/**
	 * Places a veto, i\.e\. sets vetoed to true.
	 */
	public void veto()
	{
		vetoed = true;

		updateFlags(EventModule.EVENT_CONSUMED);
	}

	/**
	 * Returns the old state. This is simply another naming for getObject ()
	 * (for name-consistancy with getNewObject ()).
	 * @nowarn
	 */
	public Object getOldObject()
	{
		return getObject();
	}

	/**
	 * Returns the new Object or null if none.
	 * @nowarn
	 */
	public Object getNewObject()
	{
		return newObject;
	}
}
