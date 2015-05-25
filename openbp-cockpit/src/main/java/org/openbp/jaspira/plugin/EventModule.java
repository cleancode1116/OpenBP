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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openbp.common.ExceptionUtil;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.event.JaspiraEventListener;

/**
 * A module is a group of event handlers for Jaspira events.
 * Each module has a name which is the base name of all events it registers itself with.
 *
 * In order to activate a module, install () has to be called. This is usually
 * done anonymously (new ModuleName ().install ()). A newly installed Module accepts
 * only one event $MODULENAME.activate, which installs the other eventreaction methods.
 *
 * The names of receivable events are generated via introspection i.e. every public
 * method that reveives a JaspiraEvent (or subclass) parameter and returns an
 * Integer object is converted into a method receiver for "$MODULENAME.$Methodname".
 *
 * If the method name contains any underscore characters, they are converted into dots
 * resulting in a noneNative event receiver WITHOUT prepending the Module-Name.<br>
 * E.g.<br>
 * public Integer model_model_selected (JaspiraEvent pe)<br>
 * would be converted to a receiver for "model.model.selected".
 *
 * Note that while a method can be in mixed case, the resulting event will
 * always be in lower case.
 *
 * Any event that is received but not dispatched to an eventHandler will be
 * passed to handleUnaccountedEvent ().
 *
 * @author Jens Ferchland
 */
public abstract class EventModule
	implements JaspiraEventListener
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Module types
	/////////////////////////////////////////////////////////////////////////

	/** Denotes a module that is to be executed inside of the plugin tree. */
	public static final int MODULE_TREE = 0;

	/** Denotes a module that is to be executed before actual event passing begins */
	public static final int MODULE_PRE_GLOBAL = 1;

	/** Denotes a module that is to be executed after actual event passing has ended */
	public static final int MODULE_POST_GLOBAL = 2;

	/** Event handler return code: The Event was ignored. */
	public static final JaspiraEventHandlerCode EVENT_IGNORED = new JaspiraEventHandlerCode("ignored");

	/** Event handler return code: The Event was noted. */
	public static final JaspiraEventHandlerCode EVENT_HANDLED = new JaspiraEventHandlerCode("handled");

	/** Event handler return code: The Event was consumed. */
	public static final JaspiraEventHandlerCode EVENT_CONSUMED = new JaspiraEventHandlerCode("consumed");

	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Table containing all event methods of this module.
	 * Maps event names to their corresponding methods of this module (Method objects).
	 */
	private Map eventMethods = new HashMap();

	/** List of names of event actions defined by this module */
	private List actionEventNames;

	/** List of all foreign event names for this module (strings). */
	private List foreignEventNames;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Create a new module.
	 */
	public EventModule()
	{
		super();

		readEvents();
	}

	///////////////////////////////////////////////////////////////////////////////////
	// @@ Info methods
	///////////////////////////////////////////////////////////////////////////////////

	/**
	 * The name of this module like "editor.file".
	 * Decides which events will be received.
	 * @nowarn
	 */
	public abstract String getName();

	/**
	 * Returns a list of the names of all foreign events that this module accepts.
	 * @return A list of strings or null if this module does not contain event handlers for foreign events
	 */
	public List getForeignEventNames()
	{
		return foreignEventNames;
	}

	/**
	 * Returns a list of names of event actions.
	 * @return A list of strings or null if this module does not contain action event handlers
	 */
	public List getEventActionNames()
	{
		return actionEventNames;
	}

	/**
	 * The module type determines the way the events of this module are handled.
	 * @return {@link #MODULE_TREE}/{@link #MODULE_PRE_GLOBAL}/{@link #MODULE_POST_GLOBAL}
	 */
	public int getModuleType()
	{
		return MODULE_TREE;
	}

	///////////////////////////////////////////////////////////////////////////////////
	// @@ Initialize
	///////////////////////////////////////////////////////////////////////////////////

	/**
	 * Reads all EventHandling methods of this Module (via introspection) into the method cache.
	 */
	private void readEvents()
	{
		Method [] methods = this.getClass().getMethods();

		// we search through all public methods of this module
		for (int i = 0; i < methods.length; i++)
		{
			Method method = methods [i];

			// Check for correct return type
			if (!(method.getReturnType() == JaspiraEventHandlerCode.class))
				continue;

			// Check for correct parameters
			Class [] paramTypes = method.getParameterTypes();
			if (paramTypes.length != 1)
				continue;

			Class paramType = paramTypes [0];
			if (!JaspiraEvent.class.isAssignableFrom(paramType))
				continue;

			// We convert the name to lowercase
			String name = method.getName().toLowerCase();

			if (name.indexOf('_') < 0)
			{
				// No underscores -> we are a native eventhandler; prepend module name
				name = getName() + "." + name;
			}
			else
			{
				// Foreign event handler, convert method name to a valid event name.
				name = name.replace('_', '.');

				// Check if there is a foreign event which should actually be a local event
				boolean addToForeignEventList = true;
				if (name.startsWith(getName() + "."))
				{
					String suffix = name.substring(getName().length() + 1);
					if (suffix.indexOf('.') < 0)
					{
						System.err.println("Foreign event should be local: " + name + " in module " + this.getClass().getName());
						addToForeignEventList = false;
					}
				}

				if (addToForeignEventList)
				{
					if (foreignEventNames == null)
						foreignEventNames = new ArrayList();
					foreignEventNames.add(name);
				}
			}

			// Name represents that event to react to
			eventMethods.put(name, method);

			if (paramType == JaspiraActionEvent.class)
			{
				// Method qualifies as receiver for a JaspiraAction.
				if (actionEventNames == null)
					actionEventNames = new ArrayList();
				actionEventNames.add(name);
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////////////
	// @@ Protected methods
	///////////////////////////////////////////////////////////////////////////////////

	/**
	 * This method is called when an event has been received that does not map to an event handler method of the module.
	 * Overwrite this if you want special functionality (i.e. react to events that are unknown at runtime).
	 *
	 * @param pe The client event
	 * @return The event status code (defaults to EVENT_IGNORED)
	 */
	protected JaspiraEventHandlerCode handleUnaccountedEvent(JaspiraEvent pe)
	{
		return EVENT_IGNORED;
	}

	///////////////////////////////////////////////////////////////////////////////////
	// @@ JaspiraEventListener implementation
	///////////////////////////////////////////////////////////////////////////////////

	/**
	 * This method checks the incoming client event and forwards it to the corrresponding
	 * event handler method if there exists one. Otherwise, the {@link #handleUnaccountedEvent}
	 * method will be called.
	 *
	 * @param je The client event
	 * @return
	 *		true	The event has been consumed by an event handler.<br>
	 *		false	The event has not been consumed.
	 */
	public boolean eventFired(JaspiraEvent je)
	{
		JaspiraEventHandlerCode result = null;

		// Find handler method for event
		String eventname = je.getEventName();
		Method method = (Method) eventMethods.get(eventname);
		if (method != null)
		{
			try
			{
				result = (JaspiraEventHandlerCode) method.invoke(this, new Object [] { je });
			}
			catch (IllegalAccessException iae)
			{
				System.err.println("Error invoking event method " + method.getDeclaringClass().getName() + "." + method.getName() + ":");
				ExceptionUtil.printTrace(iae);
				return false;
			}
			catch (InvocationTargetException ite)
			{
				System.err.println("Error invoking event method " + method.getDeclaringClass().getName() + "." + method.getName() + ":");
				ExceptionUtil.printTrace(ite);
				return false;
			}
		}
		else
		{
			// The event is not accounted for
			result = handleUnaccountedEvent(je);
		}

		return je.updateFlags(result);
	}

	/**
	 * Gets the module priority.
	 *
	 * @return The standard implementation returns 50
	 */
	public int getPriority()
	{
		return 50;
	}
}
