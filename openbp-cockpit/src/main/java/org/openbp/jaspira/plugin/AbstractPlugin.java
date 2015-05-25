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

import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.SwingUtilities;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.icon.MultiIcon;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.rc.ResourceCollectionMgr;
import org.openbp.jaspira.action.ActionMgr;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.action.keys.KeyMgr;
import org.openbp.jaspira.action.keys.KeySequence;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.event.JaspiraEventMgr;
import org.openbp.jaspira.event.KeySequenceEvent;
import org.openbp.jaspira.event.StackActionEvent;
import org.openbp.jaspira.event.VetoableEvent;
import org.openbp.jaspira.gui.interaction.InteractionClient;
import org.openbp.jaspira.gui.plugin.ApplicationBase;
import org.openbp.jaspira.plugins.statusbar.StatusBarTextEvent;

/**
 * Abstract superclass for plugins.
 *
 * @author Stephan Moritz
 */
public abstract class AbstractPlugin
	implements Plugin
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Constants
	/////////////////////////////////////////////////////////////////////////

	/** Description resource key */
	public static final String PROPERTY_DESCRIPTION = "description";

	/** Plugin name resource key */
	public static final String PROPERTY_NAME = "name";

	/** Title resource key */
	public static final String PROPERTY_TITLE = "title";

	/** Icon resource key */
	public static final String PROPERTY_ICON = "icon";

	/** Version resource key */
	public static final String PROPERTY_VERSION = "version";

	/** Vendor resource key */
	public static final String PROPERTY_VENDOR = "vendor";

	/** Key sequence to show the plugin */
	public static final String PROPERTY_SEQUENCE = "sequence";

	/** Conditional expression resource key */
	public static final String PROPERTY_CONDITION = "condition";

	/////////////////////////////////////////////////////////////////////////
	// @@ Properties
	/////////////////////////////////////////////////////////////////////////

	/** Plugin resource */
	private ResourceCollection resourceCollection;

	/** Plugin name (can be null) */
	private String name;

	/** Plugin title */
	private String title;

	/** Description */
	private String description;

	/** Plugin icon */
	private MultiIcon icon;

	/** Plugin vendor */
	private String vendor;

	/** Plugin version */
	private String version;

	/** Conditional expression that determines if the plugin should be active */
	private String condition;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Unique id of this plugin instance */
	private String uniqueId;

	/** Option modules of this plugin (internal and external, contains {@link OptionModule} objects) */
	private List optionModules;

	/** Event modules defined as inner classes of this plugin */
	private List eventModules;

	/** Contains eventgroups that this plugin reacts to as String - Set of {@link EventModule} pair. */
	private Map eventgroups;

	/**
	 * Maps names of foreign events that this plugin reacts to (in the event tree) as
	 * String - Set of {@link EventModule} pair.
	 */
	private Map foreignEvents;

	/**
	 * Table mapping event names to {@link JaspiraAction} objects that have been
	 * generated based upon the event methods provided by the event modules of this plugin.
	 */
	private List eventActionNames;

	/**
	 * This map contains all keysequence activatable actions of the
	 * plugin as {@link KeySequence} - {@link JaspiraAction} pair.
	 */
	private Map actionsBySequence;

	/**
	 * Map of peerGroup identifiers (String - Objects). Two plugins are considered to
	 * be in the same peer group, if both plugin's peer group list have at least one common
	 * String - Object pair. An entry with null as value is considered member of all peer groups
	 * of that specific type.
	 */
	private Map peerGroups;

	/** Contains a list of all children plugins of this plugin */
	private List childPlugins;

	/** The parent plugin of this plugin */
	private Plugin parent;

	/** Level of this plugin in the plugin tree (see {@link Plugin}) */
	private int level;

	/** Stack perfomer for event stack */
	private StackPerformer stackPerformer;

	/** Instance counter used to generate unique ids */
	private static int instanceCounter;

	/////////////////////////////////////////////////////////////////////////
	// @@ Install/Uninstall
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Standard constructor. Note that all plugins should provide an empty constructor and
	 * that they are usually instantiated via the plugin manager as opposed to this
	 * constructor.
	 */
	public AbstractPlugin()
	{
		// Provide a default for the plugin name
		name = getClass().getName();
		level = LEVEL_PLUGIN;
	}

	/**
	 * Initializes the plugin.
	 * Should only be called by the plugin manager.
	 */
	public void initializePlugin()
	{
		initializeResources();

		fireEvent(new StatusBarTextEvent(this, "Loading plugin '" + getTitle() + "'..."));

		installEventModules();
	}

	/**
	 * Installs the plugin and all its modules.
	 * Should only be called by the plugin manager.
	 */
	public void installPlugin()
	{
		KeyMgr.getInstance().suspendUpdate();
		try
		{
			installPluginContent();
		}
		finally
		{
			KeyMgr.getInstance().resumeUpdate();
		}

		pluginInstalled();
	}

	/**
	 * Installs the contents of the plugin.
	 */
	protected void installPluginContent()
	{
		installActions();
	}

	/**
	 * Uninstalls the plugin and all of its modules.
	 * Never call this method directly, use {@link PluginMgr#removeInstance(Plugin)} instead.
	 */
	public void uninstallPlugin()
	{
		KeyMgr.getInstance().suspendUpdate();
		try
		{
			pluginUninstalled();

			uninstallActions();
			uninstallEventModules();

			setParentPlugin(null);
		}
		finally
		{
			KeyMgr.getInstance().resumeUpdate();
		}
	}

	/**
	 * This template method is called before the first plugin instance of this kind is installed.
	 * Can be used to implement peer group behaviour.<br>
	 * Called by the plugin manager before the {@link #initializePlugin()} method has been called.<br>
	 * If you override this method, make sure to call super.installFirst().
	 */
	public void installFirstPlugin()
	{
		installOptionModules();
	}

	/**
	 * This is called after uninstall for the last instance has been uninstalled.
	 * Called by the plugin manager after the {@link #uninstallLastPlugin()} method has been called.<br>
	 * If you override this method, make sure to call super.uninstallLast().
	 */
	public void uninstallLastPlugin()
	{
		uninstallOptionModules();
	}

	/**
	 * Loads the plugin resources.
	 */
	protected void initializeResources()
	{
		// Get the default resource if none given yet
		ResourceCollection res = getPluginResourceCollection();
		if (res == null)
		{
			res = ResourceCollectionMgr.getDefaultInstance().getResource(getResourceCollectionContainerName(), getClass());
			if (res == null)
				throw new RuntimeException("Cannot find resource for class '" + getClass().getName() + "' in resource container '"
					+ getResourceCollectionContainerName() + "'.");
			setResourceCollection(res);
		}

		// Name and description are optional, title and icon not
		name = getNonNullResourceString(PROPERTY_NAME, name);
		title = getNonNullResourceString(PROPERTY_TITLE, title);
		description = getNonNullResourceString(PROPERTY_DESCRIPTION, description);

		icon = (MultiIcon) res.getRequiredObject(PROPERTY_ICON);

		vendor = getNonNullResourceString(PROPERTY_VENDOR, vendor);
		version = getNonNullResourceString(PROPERTY_VERSION, version);

		condition = getNonNullResourceString(PROPERTY_CONDITION, condition);
	}

	private String getNonNullResourceString(String resItemName, String dflt)
	{
		String s = getPluginResourceCollection().getOptionalString(resItemName);
		if (s != null)
			return s;
		return dflt;
	}

	/**
	 * Initializes some properties from the given plugin profile.
	 *
	 * @param profile Profile
	 */
	protected void initializeFromPluginProfile(PluginProfile profile)
	{
		if (profile.getName() != null)
		{
			name = profile.getName();
		}

		if (profile.getTitle() != null)
		{
			title = profile.getTitle();
		}

		if (profile.getDescription() != null)
		{
			description = profile.getDescription();
		}

		if (profile.getVendor() != null)
		{
			vendor = profile.getVendor();
		}

		if (profile.getVersion() != null)
		{
			version = profile.getVersion();
		}

		if (profile.getCondition() != null)
		{
			condition = profile.getCondition();
		}
	}

	/**
	 * Adds a key sequence that triggers the given action.
	 *
	 * @param sequence Sequence to add
	 * @param action Action to execute if the sequence is recognized
	 */
	protected void addActionKeySequence(KeySequence sequence, JaspiraAction action)
	{
		if (actionsBySequence == null)
			actionsBySequence = new HashMap();
		actionsBySequence.put(sequence, action);
	}

	/**
	 * Returns a string representation of this plugin
	 *
	 * @return tile (uniqueId) [child plugins, ...]
	 */
	public String toString()
	{
		String title = getTitle();
		StringBuffer result = new StringBuffer(title != null ? title : "(no title)");

		result.append(" (");
		result.append(getUniqueId());
		result.append(" )");

		if (childPlugins != null)
		{
			result.append(" [");
			int n = childPlugins.size();
			for (int i = 0; i < n; ++i)
			{
				result.append(childPlugins.get(i));
			}
			result.append("]");
		}

		return result.toString();
	}

	//////////////////////////////////////////////////
	// @@ Template methods
	//////////////////////////////////////////////////

	/**
	 * This template method is called after the plugin has been installed.
	 */
	protected void pluginInstalled()
	{
	}

	/**
	 * This template method is called before the plugin is uninstalled.
	 */
	protected void pluginUninstalled()
	{
	}

	//////////////////////////////////////////////////
	// @@ Overridables for plugin customization
	//////////////////////////////////////////////////

	/**
	 * Returns a list of external option module classes of this plugin.
	 * Override this method to customize the plugin.
	 *
	 * @return An array of classes or null (the default) if the plugin doesn't declare external option modules.
	 * The returned classes need to subclass {@link OptionModule} .
	 */
	protected Class[] getExternalOptionModuleClasses()
	{
		return null;
	}

	/**
	 * Returns the list of external event module classes.
	 * Override this method to customize the plugin.
	 *
	 * @return An array of classes or null (the default) if the plugin doesn't declare external event modules.
	 * The returned classes need to subclass {@link ExternalEventModule}
	 */
	protected Class[] getExternalEventModuleClasses()
	{
		return null;
	}

	/**
	 * Returns a collection of the external actions of this plugin.
	 * Override this method to customize the plugin.
	 *
	 * @return A collection of {@link JaspiraAction} objects or null
	 */
	protected Collection getExternalActions()
	{
		return null;
	}

	/**
	 * Gets the Dnd sub clients of this plugin.
	 *
	 * @return A list containing this object if it is a {@link InteractionClient} or null otherwise
	 */
	public List getSubClients()
	{
		if (this instanceof InteractionClient)
			return Collections.singletonList(this);
		return null;
	}

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
	public boolean fireEvent(JaspiraEvent je)
	{
		boolean ret = false;

		// Use EventManager to pre-handle the event
		if (JaspiraEventMgr.getInstance().preHandleEvent(je))
		{
			// This is like consuming the event
			ret = true;
		}
		else
		{
			// pass the event on
			if (je.getType() == JaspiraEvent.TYPE_GLOBAL)
			{
				ret = JaspiraEventMgr.getInstance().postHandleEvent(je);
			}
			else
			{
				ret = receiveEvent(je);
			}
		}

		return ret;
	}

	/**
	 * Creates a new client event using the given event name and fires it.
	 *
	 * @param eventName Name of the event
	 * @return
	 *		true	If the event was consumed by a plugin<br>
	 *		false	Otherwise
	 */
	public boolean fireEvent(String eventName)
	{
		return fireEvent(new JaspiraEvent(this, eventName));
	}

	/**
	 * Creates a new client event using the given event name and event object and fires it.
	 *
	 * @param eventName Name of the event
	 * @param data Event object
	 * @return
	 *		true	If the event was consumed by a plugin<br>
	 *		false	Otherwise
	 */
	public boolean fireEvent(String eventName, Object data)
	{
		return fireEvent(new JaspiraEvent(this, eventName, data));
	}

	/**
	 * Passes an event down to the child plugins.
	 * @param je Event to pass
	 * @return
	 *		true	If the event was consumed by a child plugin<br>
	 *		false	Otherwise
	 */
	protected boolean passDown(JaspiraEvent je)
	{
		Plugin ignore = je.getBrand();

		if (childPlugins != null)
		{
			for (int i = 0; i < childPlugins.size(); ++i)
			{
				Plugin child = (Plugin) childPlugins.get(i);
				if (ignore != child)
				{
					if (child.inheritEvent(je))
						return true;
				}
			}
		}

		return false;
	}

	/**
	 * Tries to handles the event for this plugin without passing it on.
	 * Stackable events will be stacked.
	 *
	 * @param je The event to handle
	 * @return
	 *		true	If the event was consumed by the plugin<br>
	 *		false	Otherwise
	 */
	public boolean handleEvent(JaspiraEvent je)
	{
		String target = je.getTargetPluginId();
		if (target != null)
		{
			// Target id specified, check if it maches
			if (! target.equals(getUniqueId()))
				// No, ignore this event for this plugin
				return false;
		}

		target = je.getTargetClassName();
		if (target != null)
		{
			// Target id specified, check if it maches
			if (! target.equals(getClass().getName()))
				// No, ignore this event for this plugin
				return false;
		}

		if (je.isStackable())
		{
			stackEvent(je);
			return false;
		}
		return doHandleEvent(je);
	}

	/**
	 * Tries to handles the event for this plugin without passing it on.
	 *
	 * @param je The event to handle
	 * @return
	 *		true	If the event was consumed by the plugin<br>
	 *		false	Otherwise
	 */
	boolean doHandleEvent(JaspiraEvent je)
	{
		// Check event groups
		if (eventgroups != null)
		{
			Set modules = (Set) eventgroups.get(je.getEventGroup());
			if (modules != null)
			{
				for (Iterator it = modules.iterator(); it.hasNext();)
				{
					if (((EventModule) it.next()).eventFired(je))
						return true;
				}
			}
		}

		// Check foreign events
		if (foreignEvents != null)
		{
			Set modules = (Set) foreignEvents.get(je.getEventName());
			if (modules != null)
			{
				for (Iterator it = modules.iterator(); it.hasNext();)
				{
					if (((EventModule) it.next()).eventFired(je))
						return true;
				}
			}
		}

		return false;
	}

	/**
	 * Handles an incoming event that is received from a child plugin.
	 * Passes this event to all other children if necessary (flood events).
	 *
	 * @param je The event to handle
	 * @return
	 *		true	If the event was consumed by the plugin<br>
	 *		false	Otherwise
	 */
	public boolean receiveEvent(JaspiraEvent je)
	{
		if (je.getType() == JaspiraEvent.TYPE_FLOOD)
		{
			if (passDown(je))
				return true;
		}

		if (handleEvent(je))
			return true;

		if (getLevel() > je.getLevel() && getParentPlugin() != null)
		{
			// Prevent re-entering this part of the plugin tree when the parent plugin passed the event down to its children.
			je.brand(this);

			return getParentPlugin().receiveEvent(je);
		}
		// Use the event manager to post-handle the event
		return JaspiraEventMgr.getInstance().postHandleEvent(je);
	}

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
	public boolean inheritEvent(JaspiraEvent je)
	{
		if (je.getType() == JaspiraEvent.TYPE_FLOOD)
		{
			if (passDown(je))
				return true;
		}

		if (handleEvent(je))
			return true;

		return false;
	}

	/**
	 * Adds a stackable event to the event stack.
	 * If the event is already part of the stack, moves it to the end.
	 *
	 * @param je The event to stack
	 */
	public synchronized void stackEvent(JaspiraEvent je)
	{
		if (stackPerformer == null)
		{
			stackPerformer = new StackPerformer();
		}

		stackPerformer.addEvent(je);
	}

	/**
	 * Checks if the event stack contains a particular event.
	 *
	 * @param eventName Event to look for
	 * @return
	 *		true	The event stack contains an event of this type.<br>
	 *		false	The event stack does not contain such an event.
	 */
	public synchronized boolean containsStackedEvent(String eventName)
	{
		if (stackPerformer != null)
			return stackPerformer.containsEvent(eventName);

		return false;
	}

	/**
	 * Removes any events with the given name from the event stack.
	 *
	 * @param eventName Event to remove
	 */
	public synchronized void removeStackedEvent(String eventName)
	{
		if (stackPerformer != null)
		{
			stackPerformer.removeEvent(eventName);
		}
	}

	/**
	 * Clears the stack performer.
	 * This is done during stack execution in order to make {@link #containsStackedEvent}
	 * return false (some methods, e. g. those invoked during container rebuild, check if the stack
	 * contains a GEU or GER and will return if so).
	 */
	synchronized void clearStackPerformer()
	{
		stackPerformer = null;
	}

	/**
	 * Restores the stack performer after calling {@link #clearStackPerformer}.
	 *
	 * @param oldStackPerformer Old stack performer
	 */
	synchronized void restoreStackPerformer(StackPerformer oldStackPerformer)
	{
		// Restore only if no events have been stacked during the call to clearStackPerformer and this method.
		// Otherwise, continue using the new stack performer
		if (stackPerformer == null)
		{
			stackPerformer = oldStackPerformer;
		}
	}

	/**
	 * Runnable that checks the event stack for stacked events. If found, processes them.
	 */
	private class StackPerformer
		implements Runnable
	{
		/** Contains stacked events as name - {@link JaspiraEvent} pair */
		private Map eventStack;

		/**
		 * Constructor.
		 */
		StackPerformer()
		{
		}

		/**
		 * Adds a stackable event to the event stack.
		 * If the event is already part of the stack, moves it to the end.
		 *
		 * @param je Event to add
		 */
		public synchronized void addEvent(JaspiraEvent je)
		{
			if (eventStack == null)
			{
				eventStack = new LinkedHashMap();
			}

			if (eventStack.isEmpty())
			{
				// When putting the first event on the event stack,
				// register the stack performer for later execution
				SwingUtilities.invokeLater(this);
			}

			// Put the event on the event stack, replacing an existing one if present
			eventStack.put(je.getEventName(), je);
		}

		/**
		 * Checks if the event stack contains a particular event.
		 *
		 * @param eventName Event to look for
		 * @return
		 *		true	The event stack contains an event of this type.<br>
		 *		false	The event stack does not contain such an event.
		 */
		public boolean containsEvent(String eventName)
		{
			if (eventStack != null)
			{
				if (eventStack.get(eventName) != null)
					return true;
			}

			return false;
		}

		/**
		 * Removes any events with the given name from the event stack.
		 *
		 * @param eventName Event to remove
		 */
		public synchronized void removeEvent(String eventName)
		{
			if (eventStack != null)
			{
				eventStack.remove(eventName);
			}
		}

		/**
		 * Handles the events on the event stack and clears the stack.
		 */
		public synchronized void run()
		{
			clearStackPerformer();

			for (Iterator it = eventStack.values().iterator(); it.hasNext();)
			{
				JaspiraEvent je = (JaspiraEvent) it.next();

				// Process the event
				doHandleEvent(je);
			}

			// Clear the event stack after execution
			eventStack.clear();

			restoreStackPerformer(this);
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Plugin tree
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns a list of all children of this plugin.
	 * @return The child plugin list or null if there are no child plugins
	 */
	public List getChildPlugins()
	{
		return childPlugins;
	}

	/**
	 * Returns a list of all children and further descendants of this plugin.
	 * @param result The children of this plugin will be added to this list, if given
	 * @return The list argument or a new linked list if null
	 */
	public List getDescendantPlugins(List result)
	{
		if (result == null)
		{
			result = new LinkedList();
		}

		if (childPlugins != null)
		{
			result.addAll(childPlugins);

			for (int i = 0; i < childPlugins.size(); ++i)
			{
				Plugin child = (Plugin) childPlugins.get(i);
				child.getDescendantPlugins(result);
			}
		}

		return result;
	}

	/**
	 * Returns the parent plugin of this plugin.
	 * Each plugin except {@link ApplicationBase} must have a parent.
	 *
	 * @return Parent of this plugin or null in case of ApplicationBase
	 */
	public Plugin getParentPlugin()
	{
		return parent;
	}

	/**
	 * Sets the parent plugin of this plugin.
	 * Each plugin except {@link ApplicationBase} must have a parent.
	 *
	 * @param newParent New parent of this plugin or null in case of ApplicationBase
	 */
	public void setParentPlugin(Plugin newParent)
	{
		if (parent != null)
		{
			parent.removePlugin(this);
		}
		parent = newParent;

		if (newParent != null)
		{
			newParent.addPlugin(this);
			level = newParent.getLevel() + 1;
		}
		else
		{
			// Top-level plugin
			level = 0;
		}
	}

	/**
	 * Adds a plugin as child plugin of this plugin.
	 *
	 * @param child Child plugin to add
	 */
	public void addPlugin(Plugin child)
	{
		if (childPlugins == null)
		{
			childPlugins = new ArrayList();
		}
		childPlugins.add(child);
	}

	/**
	 * Removes a child plugin from this plugin.
	 *
	 * @param child Child plugin to remove
	 */
	public void removePlugin(Plugin child)
	{
		if (childPlugins != null)
		{
			childPlugins.remove(child);
		}
	}

	/**
	 * Returns the level of this plugin in the plugin tree.
	 * The level corresponds to the hierarchy level in the plugin tree. The level of
	 * a child plugin is always 1 higher as the level of its parent.
	 *
	 * @return The level with {@link Plugin#LEVEL_APPLICATION} being the lowest.
	 * Defaults to {@link Plugin#LEVEL_PLUGIN}.
	 */
	public int getLevel()
	{
		return level;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Key sequences
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Invokes the action associated with the given key sequence if applicable.
	 *
	 * @param ks Key sequence to handle
	 * @return True if this sequence has been handeled, false otherwise
	 */
	protected boolean handleKeySequence(KeySequence ks)
	{
		if (actionsBySequence != null)
		{
			JaspiraAction action = (JaspiraAction) actionsBySequence.get(ks);
			if (action != null && action.isEnabled())
			{
				action.actionPerformed(new ActionEvent(this, 0, null));
				return true;
			}
		}

		return false;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the name of the resource component the default resource of the plugin belongs to.
	 * @return The container name
	 */
	public abstract String getResourceCollectionContainerName();

	/**
	 * Gets the plugin resource.
	 * @nowarn
	 */
	public ResourceCollection getPluginResourceCollection()
	{
		return resourceCollection;
	}

	/**
	 * Sets the plugin resource.
	 * @nowarn
	 */
	public void setResourceCollection(ResourceCollection resourceCollection)
	{
		this.resourceCollection = resourceCollection;
	}

	/**
	 * Gets the title.
	 * @nowarn
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * Sets the title.
	 * @nowarn
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * Returns the sub title of this plugin.
	 * This defaults to the title itself.
	 * @nowarn
	 */
	public String getSubTitle()
	{
		return getTitle();
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
	 * Gets the icon.
	 * @nowarn
	 */
	public MultiIcon getIcon()
	{
		return icon;
	}

	/**
	 * Sets the icon.
	 * @nowarn
	 */
	public void setIcon(MultiIcon icon)
	{
		this.icon = icon;
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
	 * Gets the plugin version.
	 * @nowarn
	 */
	public String getVersion()
	{
		return version;
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
	 * Returns the state of this plugin.
	 * @nowarn
	 */
	public PluginState getPluginState()
	{
		return new PluginState(this);
	}

	/**
	 * Rebuilds the state of the plugin using the given state object.
	 * @nowarn
	 */
	public void setPluginState(PluginState state)
	{
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Member access
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the name of the plugin.
	 * @return By default, this is the fully qualified class name (see {@link #getClassName})
	 * unless not otherwise set or specified in the resource file.
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * Sets the plugin name (can be null).
	 * @nowarn
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Returns the class name of the plugin.
	 * @return The name of the plugin class (the class that implements this interface)
	 */
	public String getClassName()
	{
		return getClass().getName();
	}

	/**
	 * Returns the unique id of this plugin.
	 * @nowarn
	 */
	public final String getUniqueId()
	{
		if (uniqueId == null)
		{
			uniqueId = name + ID_DELIMETER + instanceCounter++;
		}
		return uniqueId;
	}

	/**
	 * Returns a list of the event modules of this plugin.
	 * @return A list of {@link EventModule} objects or null if the plugin does not define event modules as inner classes
	 */
	protected final List getEventModules()
	{
		return eventModules;
	}

	/**
	 * Returns a list of all option modules of this plugin.
	 *
	 * @return A list of OptionModule objects or null
	 */
	protected final List getOptionModules()
	{
		return optionModules;
	}

	/**
	 * Gets a list of the action names of the event actions of this plugin.
	 * @return A list of strings or null if this module does not contain action event handlers
	 */
	public List getEventActionNames()
	{
		return eventActionNames;
	}

	/**
	 * This convenience method retrieves an action from the action manager.
	 *
	 * @param actionName Action name
	 * @return The action or null if no such action exists
	 */
	public JaspiraAction getAction(String actionName)
	{
		return ActionMgr.getInstance().getAction(actionName);
	}

	/**
	 * Convenience method for retrieving an icon from the resourcepackage.
	 *
	 * @param iconName Icon resource name
	 * @return The icon
	 */
	public MultiIcon getIcon(String iconName)
	{
		return (MultiIcon) getPluginResourceCollection().getRequiredObject(iconName);
	}

	//////////////////////////////////////////////////
	// @@ Peer groups
	//////////////////////////////////////////////////

	/**
	 * Returns the peer groups this plugin has been added to.
	 *
	 * @return A set of objects denoting the peer groups or null if this plugin does not
	 * belong to any peer group
	 */
	public Set getPeerGroups()
	{
		if (peerGroups == null || peerGroups.isEmpty())
			return null;
		return new HashSet(peerGroups.values());
	}

	/**
	 * Returns the names of all peergroups that this plugin has been added to.
	 *
	 * @return A set of strings (peer group names) or null if this plugin does not
	 * belong to any peer group
	 */
	public Set getPeerGroupNames()
	{
		if (peerGroups == null || peerGroups.isEmpty())
			return null;
		return peerGroups.keySet();
	}

	/**
	 * Adds the plugin to a peer group given by key and object.
	 *
	 * @param peerName Peer group name
	 * @param group Peer group object
	 */
	public void addToPeerGroup(String peerName, Object group)
	{
		if (peerGroups == null)
			peerGroups = new HashMap();
		peerGroups.put(peerName, group);
	}

	/**
	 * Gets the peer group for the given group name.
	 *
	 * @param peerName Name of the peer group
	 * @return The peer group or null if this group is not defined for the plugin
	 */
	public Object getPeerGroup(String peerName)
	{
		return peerGroups != null ? peerGroups.get(peerName) : null;
	}

	/**
	 * Removes the plugin from a peer group.
	 *
	 * @param peerName Peer group name
	 */
	public void removeFromPeerGroup(String peerName)
	{
		peerGroups.remove(peerName);
	}

	/**
	 * Check if the plugin is a member of the given peer group.
	 * Eithe the group or the group name must be given.
	 *
	 * @param peerName Name of the peer group to check
	 * @param group Peer group to check
	 * @param strict
	 *		true	The peergroups must be exactly met.<br>
	 *		false	null as group on one side is sufficient.
	 */
	public boolean matchesPeerGroup(String peerName, Object group, boolean strict)
	{
		// Null check
		if (peerGroups == null || peerGroups.isEmpty())
		{
			if (! strict)
				// Null on either side matches
				return true;

			// No exact match possible
			return false;
		}

		if (group == null)
		{
			if (! strict)
				// Null on either side matches
				return true;

			// Only return true, if group is NOT set
			return ! peerGroups.containsKey(peerName);
		}

		// peergroups contains name - check if value is equal or null.
		// null is allowed and returns true
		Object o = peerGroups.get(peerName);
		if (o == null && ! strict)
			// Null on either side matches
			return true;

		return group.equals(o);
	}

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
	public boolean matchesPeerGroups(Plugin plugin, boolean strict)
	{
		// TODO Refactor 6: This will probably not work due to changes in the peer group stuff; however, peer groups are not used currently; revise and adjust
		if (peerGroups == null || peerGroups.isEmpty())
			return true;

		Set those = plugin.getPeerGroups();
		if (those == null || those.isEmpty())
			return true;

		// We check for a single match.
		those.retainAll(peerGroups.entrySet());
		if (those.isEmpty())
			// No common elements
			return false;

		if (! strict)
			// not strict: we are finished!
			return true;

		// Determine relevant keys
		Set myKeys = getPeerGroupNames();

		myKeys.retainAll(plugin.getPeerGroupNames());

		// Check for other peer groups
		for (Iterator it = myKeys.iterator(); it.hasNext();)
		{
			String next = (String) it.next();

			if (! plugin.matchesPeerGroup(next, getPeerGroup(next), false))
				return false;
		}

		return false;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Module and action initialization
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Initialises the option modules of this plugin.
	 */
	private final void installOptionModules()
	{
		optionModules = new ArrayList(1);

		// Strategy: we muster search for any inner classes that are
		// descendants of OptionModule (equals to installEventModules)
		Class[] innerClasses = getClass().getClasses();

		for (int i = 0; i < innerClasses.length; i++)
		{
			if (OptionModule.class.isAssignableFrom(innerClasses[i]))
			{
				try
				{
					// We retrieve the Constructor of the module that uses one single parameter
					// of the type of our enclosing class (i.e. getClass ())
					// Note that this constructor is an implicit, private constructor of the
					// inner class mechanism.
					Constructor ctor = innerClasses[i].getDeclaredConstructors()[0];

					// Create a new instance of the module using us a reference parameter.
					OptionModule module = (OptionModule) ctor.newInstance(new Object[]
					{
						this
					});

					module.install();

					// add the module to the module list
					optionModules.add(module);
				}
				catch (InvocationTargetException e)
				{
					ExceptionUtil.printTrace(e);
				}
				catch (InstantiationException e)
				{
					ExceptionUtil.printTrace(e);
				}
				catch (IllegalAccessException e)
				{
					ExceptionUtil.printTrace(e);
				}
			}
		}

		// Initialize external modules
		Class[] externalModules = getExternalOptionModuleClasses();
		if (externalModules != null)
		{
			for (int i = 0; i < externalModules.length; ++i)
			{
				Class cls = externalModules[i];

				// Instantiate the event module class
				try
				{
					// Use the constructor that takes a plugin as parameter for instantiation
					Constructor ctor = cls.getConstructor(new Class[]
					{
						Plugin.class,
					});

					// Create a new instance of the module using us a reference parameter.
					ExternalOptionModule module = (ExternalOptionModule) ctor.newInstance(new Object[]
					{
						this,
					});

					module.install();

					// Add the module to the module list
					optionModules.add(module);
				}
				catch (InstantiationException e)
				{
					ExceptionUtil.printTrace(e);
				}
				catch (IllegalAccessException e)
				{
					ExceptionUtil.printTrace(e);
				}
				catch (InvocationTargetException e)
				{
					ExceptionUtil.printTrace(e);
				}
				catch (NoSuchMethodException e)
				{
					ExceptionUtil.printTrace(e);
				}
				catch (ClassCastException e)
				{
					ExceptionUtil.printTrace(e);
				}
			}
		}
	}

	/**
	 * Uninstalls the option modules of this plugin.
	 */
	private void uninstallOptionModules()
	{
		if (optionModules != null)
		{
			for (Iterator it = optionModules.iterator(); it.hasNext();)
			{
				((OptionModule) it.next()).uninstall();
			}
		}
	}

	/**
	 * Initializes the event modules of this plugin.
	 */
	private final void installEventModules()
	{
		eventModules = new ArrayList(2);

		// Strategy: we muster search for any inner classes that are
		// descendants of EventModule
		Class[] innerClasses = getClass().getClasses();

		for (int i = 0; i < innerClasses.length; i++)
		{
			Class cls = innerClasses[i];

			if (EventModule.class.isAssignableFrom(cls))
			{
				// Instantiate the event module class
				try
				{
					// We retrieve the Constructor of the module that uses one single parameter
					// of the type of our enclosing class (i.e. getClass ())
					// Note that this constructor is an implicit, private constructor of the
					// inner class mechanism.
					Constructor ctor = cls.getDeclaredConstructors()[0];

					// Create a new instance of the module using us a reference parameter.
					EventModule module = (EventModule) ctor.newInstance(new Object[]
					{
						this
					});

					// Add the module to the module list
					eventModules.add(module);
				}
				catch (InvocationTargetException e)
				{
					ExceptionUtil.printTrace(e);
				}
				catch (InstantiationException e)
				{
					ExceptionUtil.printTrace(e);
				}
				catch (IllegalAccessException e)
				{
					ExceptionUtil.printTrace(e);
				}
			}
		}

		// Initialize external modules
		Class[] externalModules = getExternalEventModuleClasses();
		if (externalModules != null)
		{
			for (int i = 0; i < externalModules.length; ++i)
			{
				Class cls = externalModules[i];

				// Instantiate the event module class
				try
				{
					// Use the constructor that takes a plugin as parameter for instantiation
					Constructor ctor = cls.getConstructor(new Class[]
					{
						Plugin.class,
					});

					// Create a new instance of the module using us a reference parameter.
					ExternalEventModule module = (ExternalEventModule) ctor.newInstance(new Object[]
					{
						this,
					});

					// Add the module to the module list
					eventModules.add(module);
				}
				catch (InstantiationException e)
				{
					ExceptionUtil.printTrace(e);
				}
				catch (IllegalAccessException e)
				{
					ExceptionUtil.printTrace(e);
				}
				catch (InvocationTargetException e)
				{
					ExceptionUtil.printTrace(e);
				}
				catch (NoSuchMethodException e)
				{
					ExceptionUtil.printTrace(e);
				}
				catch (ClassCastException e)
				{
					ExceptionUtil.printTrace(e);
				}
			}
		}

		// Register the modules as event listeners
		if (eventModules != null)
		{
			for (Iterator it = eventModules.iterator(); it.hasNext();)
			{
				EventModule module = (EventModule) it.next();

				if (module.getModuleType() != EventModule.MODULE_TREE)
				{
					// Global modules need to be registered with the event manager
					JaspiraEventMgr.getInstance().registerModule(module);
					continue;
				}

				// Local modules are registered with the plugin itself

				// Add to the module group list, so we have access to the local events
				// (i. e. event methods w/o '_')
				if (eventgroups == null)
				{
					eventgroups = new HashMap();
				}

				String moduleName = module.getName();
				Set modules = (Set) eventgroups.get(moduleName);
				if (modules == null)
				{
					modules = new TreeSet(JaspiraEventMgr.moduleComparator);
					eventgroups.put(moduleName, modules);
				}
				modules.add(module);

				// Add foreign events
				List foreignEventNames = module.getForeignEventNames();
				if (foreignEventNames != null)
				{
					if (foreignEvents == null)
					{
						foreignEvents = new HashMap();
					}

					int n = foreignEventNames.size();
					for (int i = 0; i < n; ++i)
					{
						String event = (String) foreignEventNames.get(i);

						Set foreignModules = (Set) foreignEvents.get(event);
						if (foreignModules == null)
						{
							foreignModules = new TreeSet(JaspiraEventMgr.moduleComparator);
							foreignEvents.put(event, foreignModules);
						}

						foreignModules.add(module);
					}
				}
			}
		}
	}

	/**
	 * Uninstalls the option modules of this plugin.
	 */
	private void uninstallEventModules()
	{
		for (Iterator iter = getEventModules().iterator(); iter.hasNext();)
		{
			EventModule module = (EventModule) iter.next();

			if (module.getModuleType() != EventModule.MODULE_TREE)
			{
				JaspiraEventMgr.getInstance().unregisterModule(module);
			}
		}
	}

	/**
	 * Initializes the actions of this plugin.
	 * Creates an action and adds it to the action manager for each method of the event modules
	 * of the plugin that returns an Integer and accepts a {@link JaspiraActionEvent} parameter.
	 */
	private final void installActions()
	{
		// Register the actions of the event modules
		if (eventModules != null)
		{
			for (Iterator it = eventModules.iterator(); it.hasNext();)
			{
				EventModule module = (EventModule) it.next();

				// An through all EventActions of the modules
				List actionNames = module.getEventActionNames();
				if (actionNames != null)
				{
					int n = actionNames.size();
					for (int i = 0; i < n; i++)
					{
						String actionName = (String) actionNames.get(i);

						JaspiraAction action = new JaspiraAction(this, actionName);
						if (installAction(action))
						{
							if (eventActionNames == null)
							{
								eventActionNames = new ArrayList();
							}
							eventActionNames.add(actionName);
						}
					}
				}
			}
		}

		// Register the user actions
		Collection externalActions = getExternalActions();
		if (externalActions != null)
		{
			for (Iterator it = externalActions.iterator(); it.hasNext();)
			{
				JaspiraAction action = (JaspiraAction) it.next();

				installAction(action);
			}
		}

		// Register the action key sequences with the key manager
		if (actionsBySequence != null)
		{
			KeyMgr.getInstance().addSequences(actionsBySequence.keySet().iterator());
		}
	}

	/**
	 * Registers an action of the plugin if the action condition evalueates to true.
	 *
	 * @param action Action
	 * @return
	 *		true	If the action was installed.<br>
	 *		false	If the condition evluated to false.
	 */
	private boolean installAction(JaspiraAction action)
	{
		if (! ConfigMgr.getInstance().evaluate(action.getCondition()))
			// Action condition evaluation failed, don't create
			return false;

		KeySequence[] sequence = action.getKeySequences();
		if (sequence != null && sequence.length > 0)
		{
			for (int j = 0; j < sequence.length; j++)
			{
				addActionKeySequence(sequence[j], action);
			}
		}

		ActionMgr.getInstance().addAction(action);
		return true;
	}

	/**
	 * Unregister the actions of the plugin from the keyboard/action manager.
	 */
	private void uninstallActions()
	{
		// Unregister the action key sequences from the key manager
		if (actionsBySequence != null)
		{
			KeyMgr.getInstance().removeSequences(actionsBySequence.keySet().iterator());
		}

		ActionMgr actionMgr = ActionMgr.getInstance();

		// Unregister the actions of the event modules
		if (eventActionNames != null)
		{
			for (Iterator it = eventActionNames.iterator(); it.hasNext();)
			{
				actionMgr.removeAction((String) it.next());
			}
		}

		// Unregister the user actions
		actionMgr.removeAllActions(getExternalActions());
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Close support
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Checks, whether this plugin can be closed. Note that this method
	 * does not necessarily mean that the plugin WILL be closed. Overwrite this
	 * method to handle unsafe states or user interaction. Calling of this method
	 * is always followed by either {@link #preClose} or {@link #closeCanceled}.
	 * @return True if this plugin can be closed right now
	 */
	public boolean canClose()
	{
		return true;
	}

	/**
	 * Request the plugin to be closed.
	 * The plugin should check whether this is currently possible an if so do it.
	 *
	 * The default implementation will issue a global.askclose veto event to all its
	 * child plugins and itself.<br>
	 * If this event has been vetoed, it will broadcast a global.closecanceled event
	 * to itself and the childs. Otherwise, it will bradcast a global.doclose event,
	 * which in turn will call {@link #preClose} and then remove the plugin instance.
	 * So if a plugin is being closed, all its children will be closed, too.
	 *
	 * @return
	 *		true	If the plugin has been closed<br>
	 *		false	Otherwise
	 */
	public boolean requestClose()
	{
		VetoableEvent ve = new VetoableEvent(this, "global.askclose");

		inheritEvent(ve);

		if (ve.isVetoed())
		{
			inheritEvent(new JaspiraEvent(this, "global.closecanceled"));
			return false;
		}

		// The event handlers of the global.doclose event will add Runnables that
		// perform the actual call to the {@link #doClose} method.
		// This delayed execution has been chosen to prevent problems when shutting down the application
		StackActionEvent sae = new StackActionEvent(this, "global.doclose");
		inheritEvent(sae);
		sae.performActions();
		return true;
	}

	/**
	 * Closes a plugin.
	 * The method calls the {@link #preClose} template method and then removes itself from the plugin manager.
	 * In order to implement custom behaviour, overwrite preClose ().
	 */
	void doClose()
	{
		preClose();

		PluginMgr.getInstance().removeInstance(this);
	}

	/**
	 * This method is called when actual closing is performed.
	 */
	protected void preClose()
	{
	}

	/**
	 * This method is called when the closing has been vetoed by a plugin.
	 */
	protected void closeCanceled()
	{
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Standard event module
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Event module.
	 */
	public class StandardPluginEvents extends EventModule
	{
		public String getName()
		{
			return "global.page";
		}

		/**
		 * Event handler: Checks if this plugin can be closed.
		 *
		 * @event global.askclose
		 * @param ve Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode global_askclose(VetoableEvent ve)
		{
			if (! canClose())
			{
				ve.veto();
				return EVENT_CONSUMED;
			}

			return EVENT_HANDLED;
		}

		/**
		 * Event handler: Closes the plugin.
		 *
		 * @event global.doclose
		 * @param ve Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode global_doclose(StackActionEvent ve)
		{
			ve.addAction(new Runnable()
			{
				public void run()
				{
					doClose();
				}
			});

			return EVENT_HANDLED;
		}

		/**
		 * Event handler: Called when the close operation has been vetoed by a plugin.
		 *
		 * @event global.closecanceled
		 * @param ve Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode global_closecanceled(JaspiraEvent ve)
		{
			closeCanceled();

			return EVENT_HANDLED;
		}

		/**
		 * Event handler: Receives key sequence events and passes them to the Plugin itself.
		 *
		 * @event global.keyevent
		 * @param kse Event
		 * @return The event status code<br>
		 * Consumes the key event if the sequence can be handled by this plugin.
		 */
		public JaspiraEventHandlerCode global_keyevent(KeySequenceEvent kse)
		{
			return handleKeySequence(kse.getKeySequence()) ? EventModule.EVENT_CONSUMED : EventModule.EVENT_IGNORED;
		}
	}
}
