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
package org.openbp.jaspira.gui.plugin;

import java.util.List;

import org.openbp.jaspira.gui.interaction.BasicDropRegionId;

/**
 * Container used to display a single visible plugin.
 * Interface implemented by all Container for plugins. Has methods to add and remove
 * plugins. Note that according to Composite pattern, a plugin is a plugin container by itself.
 *
 * @author Stephan Moritz
 */
public interface PluginContainer
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Constants
	/////////////////////////////////////////////////////////////////////////

	/** Constraint: Center segment (adds an object directly to this container) */
	public static final String CENTER = "center";

	/** Constraint: North segment (adds an object to the north of this container) */
	public static final String NORTH = "north";

	/** Constraint: South segment (adds an object to the south of this container) */
	public static final String SOUTH = "south";

	/** Constraint: East segment (adds an object to the east of this container) */
	public static final String EAST = "east";

	/** Constraint: West segment (adds an object to the west of this container) */
	public static final String WEST = "west";

	/////////////////////////////////////////////////////////////////////////
	// @@ Drag and drop region identifiers
	/////////////////////////////////////////////////////////////////////////

	/** Center region */
	public static final BasicDropRegionId REGION_CENTER = new BasicDropRegionId("center", CENTER);

	/** Northern region */
	public static final BasicDropRegionId REGION_NORTH = new BasicDropRegionId("north", NORTH);

	/** Southern region */
	public static final BasicDropRegionId REGION_SOUTH = new BasicDropRegionId("south", SOUTH);

	/** Western region */
	public static final BasicDropRegionId REGION_WEST = new BasicDropRegionId("west", WEST);

	/** Eastern region */
	public static final BasicDropRegionId REGION_EAST = new BasicDropRegionId("east", EAST);

	/////////////////////////////////////////////////////////////////////////
	// @@ Methods
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Adds a plugin to the container.
	 *
	 * @param p Plugin to add
	 */
	public void addPlugin(VisiblePlugin p);

	/**
	 * Removes a plugin from this container.
	 *
	 * @param p Plugin to remove
	 */
	public void removePlugin(VisiblePlugin p);

	/**
	 * Returns all plugins hosted by this container and its sub containers.
	 *
	 * @return A list of {@link VisiblePlugin} objects or null
	 */
	public List getPlugins();

	/**
	 * Returns all plugins that are currently visible, i\.e\. shown in their tabbed containers.
	 * This assumes that the container itself is visible.
	 *
	 * @return A list of {@link VisiblePlugin} objects or null
	 */
	public List getVisiblePlugins();

	/**
	 * Returns the currently active plugin inside this container.
	 *
	 * @return The plugin or null if the container does not contain a plugin
	 */
	public VisiblePlugin getActivePlugin();

	/**
	 * Returns the parent container of this container.
	 *
	 * @return The parent container or null
	 */
	public PluginContainer getParentContainer();

	/**
	 * Returns the page level parent plugin of this plugin.
	 *
	 * @return The page that holds this plugin or null
	 */
	public JaspiraPage getPage();

	//////////////////////////////////////////////////
	// @@ Internal methods
	//////////////////////////////////////////////////

	/**
	 * Slices the component at the given edge, i\.e\. replaces the entry with
	 * a new {@link PluginDivider} consisting of the old component and the new container.
	 *
	 * @param toInsert Containert to insert
	 * @param currentContainer Currrent container that shall be replaced
	 * @param constraint The constraint determines where to place the new container:<br>
	 * {@link PluginContainer#CENTER}/{@link PluginContainer#NORTH}/{@link PluginContainer#SOUTH}/{@link PluginContainer#EAST}/{@link PluginContainer#WEST}
	 */
	public void sliceContainer(PluginContainer toInsert, PluginContainer currentContainer, String constraint);
}
