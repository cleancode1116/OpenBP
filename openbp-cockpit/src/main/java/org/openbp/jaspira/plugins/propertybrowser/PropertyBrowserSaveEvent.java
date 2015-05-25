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
package org.openbp.jaspira.plugins.propertybrowser;

import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.plugin.Plugin;

/**
 * This event is created by the property browser in order to save the currently edited object.
 * The listeners of this event should execute the save function.
 *
 * @author Andreas Putz
 */
public class PropertyBrowserSaveEvent extends JaspiraEvent
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Original object (unmodified) */
	public Object original;

	/** Saved flag. */
	public boolean saved;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param source Sender of the event
	 * @param eventName Name of the event
	 * @param modifiedObject Modified object to save
	 * @param originalObject Unmodified object
	 */
	public PropertyBrowserSaveEvent(Plugin source, String eventName, Object modifiedObject, Object originalObject)
	{
		super(source, eventName, modifiedObject, TYPE_FLOOD, Plugin.LEVEL_PAGE);
		this.original = originalObject;
	}
}
