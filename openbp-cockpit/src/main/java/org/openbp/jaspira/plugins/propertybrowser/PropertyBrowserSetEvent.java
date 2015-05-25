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

import org.openbp.common.icon.MultiIcon;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.plugin.Plugin;

/**
 * This event is created by plugins that want the property browser to display an object.
 * The event name is always "plugin.propertybrowser.setobject".
 * The recipient of this event is usually an property browser.
 *
 * @author Andreas Putz
 */
public class PropertyBrowserSetEvent extends JaspiraEvent
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Original object for name uniqueness check or null */
	public Object originalObject;

	/** Flag that determines if the object has just been created */
	public boolean isObjectNew;

	/** The description for the set object */
	public String description;

	/** The title information for the property browser */
	public String title;

	/** The title icon */
	public MultiIcon icon;

	/** Read only flag of the object */
	public boolean readOnly;

	/** Save after modifying property */
	public boolean saveImmediately;

	/** Set the object also if currently editing this same object */
	public boolean reedit;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param source Sender of the event
	 * @param object Object to set to the property browser
	 * @param originalObject Reference object for name uniqueness check or null
	 * @param isObjectNew Flag that determines if the object has just been created
	 * @param description The description for the set object
	 * @param title The title information for the property browser
	 * @param icon The title icon
	 * @param readOnly Flag wheter the object is read only
	 * @param saveImmediately Save after modifying property
	 */
	public PropertyBrowserSetEvent(Plugin source, Object object, Object originalObject, boolean isObjectNew, String description, String title, MultiIcon icon, boolean readOnly, boolean saveImmediately)
	{
		super(source, "plugin.propertybrowser.setobject", object, TYPE_FLOOD, Plugin.LEVEL_PAGE);
		this.originalObject = originalObject;
		this.description = description;
		this.title = title;
		this.icon = icon;
		this.readOnly = readOnly;
		this.saveImmediately = saveImmediately;
		this.isObjectNew = isObjectNew;

		reedit = true;
	}

	/**
	 * Generates an event that clears the content of the propertyBrowser.
	 *
	 * @param source Sender of the event
	 */
	public PropertyBrowserSetEvent(Plugin source)
	{
		this(source, null, null, false, null, null, null, false, false);
	}
}
