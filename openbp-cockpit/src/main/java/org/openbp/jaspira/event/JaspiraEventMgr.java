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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.openbp.jaspira.gui.plugin.ApplicationBase;
import org.openbp.jaspira.plugin.AbstractPlugin;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.jaspira.plugin.Plugin;

/**
 * The event manager manages the of global event processing.
 * An event module ({@link EventModule}) can designate itself as global event handler.
 * Global event handlers can handle events independently of the plugin tree-based
 * event handling before or after the actual tree-related event handling is performed.
 * The event manager maintains a list of events that should be handled globally and
 * provides methods that perform the event processing for these types of events.
 *
 * The manager is a singleton class that is intended to be used by plugins internally only.
 * In order to fire an event, use either the {@link Plugin#fireEvent(JaspiraEvent)} method or one of the
 * {@link #fireGlobalEvent(JaspiraEvent)} convenience methods provided by this class.
 *
 * @author Jens Ferchland
 */
public final class JaspiraEventMgr
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/*
	 * The following tables map event groups (i. e. the event name with the tail stripped off)
	 * to sets (java.util.Set) of modules that accept global events of this group.
	 *
	 * There are 2 x 2 types of module queues:
	 *
	 * Native queues contain modules, whichs names are equal to the event group, i. e. the event
	 * handlers handle events 'native' to the module.
	 *
	 * Foreign queues contain modules that implement event handlers with fully qualified
	 * names, e. g. global_environment_update.
	 *
	 * The handlers of an event module will be added to the 'pre' tables (which will be called
	 * before the regular event processing) if the module type is {@link EventModule#MODULE_PRE_GLOBAL},
	 * and will added to the 'post' tables (which will be called after the regular event processing)
	 * if the module type is {@link EventModule#MODULE_POST_GLOBAL}. Modules of type {@link EventModule#MODULE_TREE}
	 * (the default) will not be added to the tables of the event manager; the event tables for these events
	 * will be kept locally within the plugin.
	 */
	/** Singleton instance */
	private static JaspiraEventMgr singletonInstance;

	/** Map for the native pre event handlers. */
	private Map nativePreTable;

	/** Map for the foreign pre event handlers. */
	private Map foreignPreTable;

	/** Map for the native post event handlers. */
	private Map nativePostTable;

	/** Map for the foreign post event handlers. */
	private Map foreignPostTable;

	//////////////////////////////////////////////////
	// @@ Construction/Singleton access
	//////////////////////////////////////////////////

	/**
	 * Creating a new JaspiraEventMgr instance isn't allowed.
	 * JaspiraEventMgr is a Singleton class - use getInstance () instead!
	 */
	private JaspiraEventMgr()
	{
		nativePreTable = new HashMap();
		foreignPreTable = new HashMap();
		nativePostTable = new HashMap();
		foreignPostTable = new HashMap();
	}

	/**
	 * Returns the event manager instance of the system.
	 * All global Jaspira events will be managed by this instance.
	 *
	 * @return The singleton instance
	 */
	public static synchronized JaspiraEventMgr getInstance()
	{
		if (singletonInstance == null)
		{
			singletonInstance = new JaspiraEventMgr();
		}
		return singletonInstance;
	}

	//////////////////////////////////////////////////
	// @@ Convenience functions for firing global events
	//////////////////////////////////////////////////

	/**
	 * Fires the given event using the application itself as startingpoint.
	 * Merely a convenience method for ApplicationBase.getInstance ().fireEvent (JaspiraEvent).
	 *
	 * @param je The event to fire
	 * @return
	 *		true	If the event was consumed by a plugin<br>
	 *		false	Otherwise
	 */
	public static boolean fireGlobalEvent(JaspiraEvent je)
	{
		return ApplicationBase.getInstance().fireEvent(je);
	}

	/**
	 * Creates a new client event with the given name and fires it using the
	 * application itself as startingpoint.
	 * Merely a convenience method for ApplicationBase.getInstance().fireEvent (String).
	 *
	 * @param eventname The name for the event
	 * @return
	 *		true	If the event was consumed by a plugin<br>
	 *		false	Otherwise
	 */
	public static boolean fireGlobalEvent(String eventname)
	{
		return ApplicationBase.getInstance().fireEvent(eventname);
	}

	/**
	 * Creates a new client event with the given name and data and fires it using the
	 * application itself as startingpoint.
	 * Merely a convenience method for ApplicationBase.getInstance().fireEvent (String, Object).
	 *
	 * @param eventname The name for the event
	 * @param data The dataobject for the event
	 * @return
	 *		true	If the event was consumed by a plugin<br>
	 *		false	Otherwise
	 */
	public static boolean fireGlobalEvent(String eventname, Object data)
	{
		return ApplicationBase.getInstance().fireEvent(eventname, data);
	}

	//////////////////////////////////////////////////
	// @@ Event handling
	//////////////////////////////////////////////////

	/**
	 * Performs the event handling for global events that should be handled
	 * before the actual event handling of the plugin.
	 * To be called by the {@link AbstractPlugin} class only.
	 *
	 * @param je EVent to handle
	 * @return
	 *		true	The event was consumed by an event handler.<br>
	 *		false	The event is not a global pre-handled event or was not consumed by the handler.
	 */
	public boolean preHandleEvent(JaspiraEvent je)
	{
		return handleEvent(je, nativePreTable, foreignPreTable);
	}

	/**
	 * Performs the event handling for global events that should be handled
	 * after the actual event handling of the plugin.
	 * To be called by the {@link AbstractPlugin} class only.
	 *
	 * @param je EVent to handle
	 * @return
	 *		true	The event was consumed by an event handler.<br>
	 *		false	The event is not a global post-handled event or was not consumed by the handler.
	 */
	public boolean postHandleEvent(JaspiraEvent je)
	{
		return handleEvent(je, nativePostTable, foreignPostTable);
	}

	/**
	 * Does the actual handling of the event.
	 *
	 * @param je The event to handle
	 * @param nativeTable The table containing the native eventhandlers
	 * @param foreignTable The table containing foreign eventhandlers
	 * @return True, if the event has been consumed, false otherwise
	 */
	private boolean handleEvent(JaspiraEvent je, Map nativeTable, Map foreignTable)
	{
		// Native events - compare event group only
		Set modules = (Set) nativeTable.get(je.getEventGroup());
		if (modules != null)
		{
			for (Iterator it = modules.iterator(); it.hasNext();)
			{
				if (((EventModule) it.next()).eventFired(je))
				{
					return true;
				}
			}
		}

		// Foreign events - compare directly the event name
		modules = (Set) foreignTable.get(je.getEventName());
		if (modules != null)
		{
			for (Iterator it = modules.iterator(); it.hasNext();)
			{
				if (((EventModule) it.next()).eventFired(je))
				{
					return true;
				}
			}
		}

		return false;
	}

	//////////////////////////////////////////////////
	// @@ Module registration
	//////////////////////////////////////////////////

	/**
	 * Registers a global event module.
	 * @nowarn
	 */
	public void registerModule(EventModule module)
	{
		// Add native events
		Map table = module.getModuleType() == EventModule.MODULE_PRE_GLOBAL ? nativePreTable : nativePostTable;

		Set modules = (Set) table.get(module.getName());
		if (modules == null)
		{
			modules = new TreeSet(JaspiraEventMgr.moduleComparator);
			table.put(module.getName(), modules);
		}
		modules.add(module);

		// Add foreign events
		table = module.getModuleType() == EventModule.MODULE_PRE_GLOBAL ? foreignPreTable : foreignPostTable;

		List foreignEventNames = module.getForeignEventNames();
		if (foreignEventNames != null)
		{
			int n = foreignEventNames.size();
			for (int i = 0; i < n; ++i)
			{
				String eventName = (String) foreignEventNames.get(i);

				modules = (Set) table.get(eventName);
				if (modules == null)
				{
					modules = new TreeSet(JaspiraEventMgr.moduleComparator);
					table.put(eventName, modules);
				}
				modules.add(module);
			}
		}
	}

	/**
	 * Unregisters a global event module.
	 * @nowarn
	 */
	public void unregisterModule(EventModule module)
	{
		// Remove native events
		Map table = module.getModuleType() == EventModule.MODULE_PRE_GLOBAL ? nativePreTable : nativePostTable;

		Set modules = (Set) table.get(module.getName());

		modules.remove(module);
		if (modules.isEmpty())
		{
			table.remove(module.getName());
		}

		// Remove foreign events
		table = module.getModuleType() == EventModule.MODULE_PRE_GLOBAL ? foreignPreTable : foreignPostTable;

		List foreignEventNames = module.getForeignEventNames();
		if (foreignEventNames != null)
		{
			int n = foreignEventNames.size();
			for (int i = 0; i < n; ++i)
			{
				String event = (String) foreignEventNames.get(i);
				modules = (Set) table.get(event);

				modules.remove(module);
				if (modules.isEmpty())
				{
					table.remove(event);
				}
			}
		}
	}

	/**
	 * Event module comparator.
	 * Sorts modules according to their priority.
	 */
	public static Comparator moduleComparator = new Comparator()
	{
		public int compare(Object o1, Object o2)
		{
			int result = ((JaspiraEventListener) o1).getPriority() - ((JaspiraEventListener) o2).getPriority();

			// if the priority is the same, we use the toString methods of both obejcts.
			return result != 0 ? result : o1.toString().compareTo(o2.toString());
		}
	};
}
