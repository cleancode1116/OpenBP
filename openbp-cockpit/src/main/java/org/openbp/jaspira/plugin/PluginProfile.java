/*
 *   Copyright 2008 skynamics AG
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

/**
 * Plugin profile.
 */
public class PluginProfile
{
	/** Plugin class name */
	private String className;

	/** Plugin name */
	private String name;

	/** Plugin title */
	private String title;

	/** Description */
	private String description;

	/** Plugin vendor */
	private String vendor;

	/** Plugin version */
	private String version;

	/** Conditional expression that determines if the plugin should be active */
	private String condition;

	/**
	 * Default constructor.
	 */
	public PluginProfile()
	{
	}

	/**
	 * Gets the plugin class name.
	 * @nowarn
	 */
	public String getClassName()
	{
		return className;
	}

	/**
	 * Sets the plugin class name.
	 * @nowarn
	 */
	public void setClassName(String className)
	{
		this.className = className;
	}

	/**
	 * Gets the plugin name.
	 * @nowarn
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the plugin name.
	 * @nowarn
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the plugin title.
	 * @nowarn
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * Sets the plugin title.
	 * @nowarn
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * Gets the description.
	 * @nowarn
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Sets the description.
	 * @nowarn
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * Gets the plugin vendor.
	 * @nowarn
	 */
	public String getVendor()
	{
		return vendor;
	}

	/**
	 * Sets the plugin vendor.
	 * @nowarn
	 */
	public void setVendor(String vendor)
	{
		this.vendor = vendor;
	}

	/**
	 * Gets the plugin version.
	 * @nowarn
	 */
	public String getVersion()
	{
		return version;
	}

	/**
	 * Sets the plugin version.
	 * @nowarn
	 */
	public void setVersion(String version)
	{
		this.version = version;
	}

	/**
	 * Gets the conditional expression that determines if the plugin should be active.
	 * @nowarn
	 */
	public String getCondition()
	{
		return condition;
	}

	/**
	 * Sets the conditional expression that determines if the plugin should be active.
	 * @nowarn
	 */
	public void setCondition(String condition)
	{
		this.condition = condition;
	}
}
