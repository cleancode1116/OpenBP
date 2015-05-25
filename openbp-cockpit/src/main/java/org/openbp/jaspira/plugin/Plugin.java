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

import java.util.List;
import java.util.Set;

import org.openbp.common.icon.MultiIcon;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.gui.plugin.ApplicationBase;

/**
 * Generic interface for plugins.
 * A plugin is an encapsulation for a group of actions, options, and event handling modules.
 *
 * @author Stephan Moritz
 */
public interface Plugin
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Constants
	/////////////////////////////////////////////////////////////////////////

	/** Plugin level: application */
	public static final int LEVEL_APPLICATION = 0;

	/** Plugin level: frame */
	public static final int LEVEL_FRAME = 1;

	/** Plugin level: page */
	public static final int LEVEL_PAGE = 2;

	/** Plugin level: regular plugin */
	public static final int LEVEL_PLUGIN = 3;

	/** Delimeter for the unique id of a plugin */
	public static final String ID_DELIMETER = "@";

	/////////////////////////////////////////////////////////////////////////
	// @@ Properties
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the name of the plugin.
	 * @return By default, this is the fully qualified class name (see {@link #getClassName})
	 */
	public String getName();

	/**
	 * Returns the class name of the plugin.
	 * @return The name of the plugin class (the class that implements this interface)
	 */
	public String getClassName();

	/**
	 * Returns the unique id of this plugin.
	 * @nowarn
	 */
	public String getUniqueId();

	/**
	 * Gets the plugin resource.
	 * @nowarn
	 */
	public ResourceCollection getPluginResourceCollection();

	/**
	 * Gets the title.
	 * @nowarn
	 */
	public String getTitle();

	/**
	 * Returns the sub title of this plugin.
	 * This defaults to the title itself.
	 * @nowarn
	 */
	public String getSubTitle();

	/**
	 * Gets the description.
	 * @nowarn
	 */
	public String getDescription();

	/**
	 * Gets the icon.
	 * @nowarn
	 */
	public MultiIcon getIcon();

	/**
	 * Gets the plugin vendor.
	 * @nowarn
	 */
	public String getVendor();

	/**
	 * Gets the plugin version.
	 * @nowarn
	 */
	public String getVersion();

	/**
	 * Gets the conditional expression that determines if the plugin should be active.
	 * @nowarn
	 */
	public String getCondition();

	/**
	 * Gets a list of the action names of the event actions of this plugin.
	 * @return A list of strings or null if this module does not contain action event handlers
	 */
	public List getEventActionNames();

	/**
	 * This convenience method retrieves an action from the action manager.
	 *
	 * @param actionName Action name
	 * @return The action or null if no such action exists
	 */
	public JaspiraAction getAction(String actionName);

	/////////////////////////////////////////////////////////////////////////
	// @@ Installation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Initializes the plugin.
	 * Should only be called by the plugin manager.
	 */
	public void initializePlugin();

	/**
	 * Installs the plugin and all its modules.
	 * Should only be called by the plugin manager.
	 */
	public void installPlugin();

	/**
	 * This template method is called before the first plugin instance of this kind is installed.
	 * Can be used to implement peer group behaviour.<br>
	 * Should only be called by the plugin manager.
	 */
	public void installFirstPlugin();

	/**
	 * Uninstalls the plugin and all of its modules.
	 * Should only be called by the plugin manager.
	 */
	public void uninstallPlugin();

	/**
	 * This is called after uninstall for the last instance has been uninstalled.
	 * Should only be called by the plugin manager.
	 */
	public void uninstallLastPlugin();

	/////////////////////////////////////////////////////////////////////////
	// @@ Event handling
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Fires the given event using the event queue.
	 * @param je Event to fire
	 * @return
	 *		true	If the event was consumed by a plugin<br>
	 *		false	Otherwise
	 */
	public boolean fireEvent(JaspiraEvent je);

	/**
	 * Creates a new client event using the given event name and fires it.
	 *
	 * @param eventName Name of the event
	 * @return
	 *		true	If the event was consumed by a plugin<br>
	 *		false	Otherwise
	 */
	public boolean fireEvent(String eventName);

	/**
	 * Creates a new client event using the given event name and event object and fires it.
	 *
	 * @param eventName Name of the event
	 * @param data Event object
	 * @return
	 *		true	If the event was consumed by a plugin<br>
	 *		false	Otherwise
	 */
	public boolean fireEvent(String eventName, Object data);

	/**
	 * Tries to handles the event for this plugin without passing it on.
	 * Stackable events will be stacked.
	 *
	 * @param je The event to handle
	 * @return
	 *		true	If the event was consumed by the plugin<br>
	 *		false	Otherwise
	 */
	public boolean handleEvent(JaspiraEvent je);

	/**
	 * Handles an incoming event that is received from a child plugin.
	 * Passes this event to all other children if necessary (flood events).
	 *
	 * @param je The event to handle
	 * @return
	 *		true	If the event was consumed by the plugin<br>
	 *		false	Otherwise
	 */
	public boolean receiveEvent(JaspiraEvent je);

	/**
	 * Receives an event incoming from a parent.
	 * This event is first passed down to the children, than handeled by the plugin itself.
	 * It is not passed upwards.
	 *
	 * @param je The event to handle
	 * @return
	 *		true	If the event was consumed by the plugin<br>
	 *		false	Otherwise
	 */
	public boolean inheritEvent(JaspiraEvent je);

	/**
	 * Adds a stackable event to the event stack.
	 * If the event is already part of the stack, moves it to the end.
	 * @param je The event to stack
	 */
	public void stackEvent(JaspiraEvent je);

	/**
	 * Checks if the event stack contains a particular event.
	 *
	 * @param eventName Event to look for
	 * @return
	 *		true	The event stack contains an event of this type.<br>
	 *		false	The event stack does not contain such an event.
	 */
	public boolean containsStackedEvent(String eventName);

	/////////////////////////////////////////////////////////////////////////
	// @@ Plugin Tree
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns a list of all children of this plugin.
	 * @return The child plugin list or null if there are no child plugins
	 */
	public List getChildPlugins();

	/**
	 * Returns a list of all children and further descendants of this plugin.
	 * @param plugins The children of this plugin will be added to this list, if given
	 * @return The list argument or a new linked list if null
	 */
	public List getDescendantPlugins(List plugins);

	/**
	 * Returns the parent plugin of this plugin.
	 * Each plugin except {@link ApplicationBase} must have a parent.
	 *
	 * @return Parent of this plugin or null in case of ApplicationBase
	 */
	public Plugin getParentPlugin();

	/**
	 * Sets the parent plugin of this plugin.
	 * Each plugin except {@link ApplicationBase} must have a parent.
	 *
	 * @param newParent New parent of this plugin or null in case of ApplicationBase
	 */
	public void setParentPlugin(Plugin newParent);

	/**
	 * Adds a plugin as child plugin of this plugin.
	 *
	 * @param child Child plugin to add
	 */
	public void addPlugin(Plugin child);

	/**
	 * Removes a child plugin from this plugin.
	 *
	 * @param child Child plugin to remove
	 */
	public void removePlugin(Plugin child);

	/**
	 * Returns the level of this plugin in the plugin tree.
	 * The level corresponds to the hierarchy level in the plugin tree. The level of
	 * a child plugin is always 1 higher as the level of its parent.
	 *
	 * @return The level with {@link Plugin#LEVEL_APPLICATION} being the lowest.
	 * Defaults to {@link Plugin#LEVEL_PLUGIN}.
	 */
	public int getLevel();

	//////////////////////////////////////////////////
	// @@ Peer groups
	//////////////////////////////////////////////////

	/**
	 * Returns the peer groups this plugin has been added to.
	 *
	 * @return A set of objects denoting the peer groups
	 */
	public Set getPeerGroups();

	/**
	 * Returns the names of all peergroups that this plugin has been added to.
	 *
	 * @return A set of strings (peer group names)
	 */
	public Set getPeerGroupNames();

	/**
	 * Adds the plugin to a peer group given by key and object.
	 *
	 * @param name Peer group name
	 * @param group Peer group object
	 */
	public void addToPeerGroup(String name, Object group);

	/**
	 * Gets the peer group for the given group name.
	 *
	 * @param name Name of the peer group
	 * @return The peer group or null if this group is not defined for the plugin
	 */
	public Object getPeerGroup(String name);

	/**
	 * Removes the plugin from a peer group.
	 *
	 * @param name Peer group name
	 */
	public void removeFromPeerGroup(String name);

	/**
	 * Check if the plugin is a member of the given peer group.
	 * Eithe the group or the group name must be given.
	 *
	 * @param name Name of the peer group to check
	 * @param group Peer group to check
	 * @param strict
	 *		true	The peergroups must be exactly met.<br>
	 *		false	null as group on one side is sufficient.
	 * @return
	 *		true	If the peer group matches.<br>
	 *		false	Otherwise
	 */
	public boolean matchesPeerGroup(String name, Object group, boolean strict);

	/**
	 * Matches all peer groups of one plugin against another.
	 *
	 * @param plugin Plugin to match agains this plugin
	 * @param strict
	 *		true	All peer groups must match.<br>
	 *		false	A single peer group must match.
	 * @return
	 *		true	If both plugins contain at least one common peergroup.<br>
	 *				If the strict parameter is true, there may not be any different groups.<br>
	 *				Plugins with empty peergroups ALWAYS match!
	 *		false	Otherwise
	 */
	public boolean matchesPeerGroups(Plugin plugin, boolean strict);

	/////////////////////////////////////////////////////////////////////////
	// @@ State serialization
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the state of this plugin.
	 * @nowarn
	 */
	public PluginState getPluginState();

	/**
	 * Rebuilds the state of the plugin using the given state object.
	 * @nowarn
	 */
	public void setPluginState(PluginState state);

	/////////////////////////////////////////////////////////////////////////
	// @@ Close support
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Checks, whether this plugin can be closed. Note that this method
	 * does not necessarily mean that the plugin WILL be closed. Overwrite this
	 * method to handle unsafe states or user interaction. Calling of this method
	 * is always followed by either preClose () or closeCanceled ().
	 * @return True if this plugin can be closed right now
	 */
	public boolean canClose();

	/**
	 * Request the plugin to be closed.
	 * The plugin should check whether this is currently possible an if so do it.
	 * @return
	 *		true	If the plugin has been closed<br>
	 *		false	Otherwise
	 */
	public boolean requestClose();
}
