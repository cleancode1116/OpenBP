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
package org.openbp.jaspira.plugin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract PluginState object, creates name and version out of the class
 *
 * @author Stephan Moritz
 */
public class PluginState
	implements Serializable
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** Class name of the plugin */
	private String className;

	/** Version of the plugin. */
	private String version;

	/** Map that contains arbitrary name - value pairs */
	private Map values;

	/**
	 * The unique id of the source plugin.
	 * Note that this is NOT used during deserialization.
	 */
	private String uniqueId;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param plugin Plugin for which to create the state object for
	 */
	public PluginState(Plugin plugin)
	{
		super();

		this.className = plugin.getClassName();
		this.version = plugin.getVersion();
		this.uniqueId = plugin.getUniqueId();
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the class name of the plugin.
	 * @nowarn
	 */
	public String getPluginClassName()
	{
		return className;
	}

	/**
	 * Gets the version of the plugin..
	 * @nowarn
	 */
	public String getPluginVersion()
	{
		return version;
	}

	/**
	 * Gets the the unique id of the source plugin.
	 * Note that this is NOT used during deserialization.
	 * @nowarn
	 */
	public String getUniqueId()
	{
		return uniqueId;
	}

	/**
	 * Stores a value with a given name in the properties table of the state object.
	 *
	 * @param name Property name
	 * @param value Property value
	 */
	public void setValue(String name, Object value)
	{
		if (values == null)
		{
			values = new HashMap();
		}

		/*
		 if (! (value instanceof Serializable))
		 {
		 throw new IllegalArgumentException ("Values must be Serializable: " + value.getClass() + " is not!");
		 }
		 */
		values.put(name, value);
	}

	/**
	 * Gets the property value with the given name.
	 *
	 * @param name Key of the value to retrieve
	 * @return The property value or null if not present
	 */
	public Object getValue(String name)
	{
		return values != null ? values.get(name) : null;
	}

	public String toString()
	{
		return "!" + uniqueId;
	}
}
