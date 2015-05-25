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
import org.openbp.jaspira.plugin.PluginState;

/**
 * Plugin state for the property browser plugin.
 *
 * @author Andreas Putz
 */
public class PropertyBrowserPluginState extends PluginState
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Title icon */
	protected transient MultiIcon icon;

	/** Title */
	protected transient String title;

	/** Title description */
	protected transient String description;

	/** The unmodified Object */
	protected transient Object unmodifiedObject;

	/** The modified object */
	protected transient Object modifiedObject;

	/** The original object */
	protected transient Object originalObject;

	/** Flag that determines if the object has just been created */
	protected boolean isObjectNew;

	/** Flag for read only usage */
	protected transient boolean readOnly;

	/** Save after modifying property */
	protected boolean saveImmediately;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param plugin The Property browser plugin
	 */
	public PropertyBrowserPluginState(PropertyBrowserPlugin plugin)
	{
		super(plugin);
	}
}
