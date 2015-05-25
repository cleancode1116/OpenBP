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

import java.util.EventObject;

import org.openbp.jaspira.gui.plugin.ApplicationBase;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.jaspira.plugin.Plugin;

/**
 * A client event represents a high level event in the Jaspira framework.
 * It contains Informations about the source, kind of event and if possible
 * the original event created by a java component.
 *
 * @author Jens Ferchland
 */
public class JaspiraEvent extends EventObject
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Constants
	/////////////////////////////////////////////////////////////////////////

	/** Event type: Handle by target plugin only (does not get passed to other plugins) */
	public static final int TYPE_DIRECT = 0;

	/** Event type: Bottom up event (passed from child to parent) */
	public static final int TYPE_BOTTOM_UP = 1;

	/** Event type: Flood event (passed to all children, then to parent - the most common event type) */
	public static final int TYPE_FLOOD = 2;

	/** Event type: Global event, passed only to the {@link JaspiraEventMgr} */
	public static final int TYPE_GLOBAL = 3;

	/** Event flag: An unconsumable event that can't be consumed by a listener */
	public static final int UNCONSUMABLE = 1 << 0;

	/**
	 * Event flag: The event can be stacked with events of the same type.
	 * Only the most recent event of this type will be kept and will be executed by
	 * the Swing event queue. A typical example is the global.environment.update event.
	 */
	public static final int STACKABLE = 1 << 1;

	/** Event status flag: The event has been consumed by a listener */
	public static final int CONSUMED = 1 << 2;

	/** Event status flag: The event has been handled by at least one listener */
	public static final int HANDELED = 1 << 3;

	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** Name of the event. */
	private String eventName;

	/** Object to pass along with this event. */
	private Object object;

	/** Flags of this event. */
	private int flags;

	/**
	 * The type of this event.
	 * ({@link JaspiraEvent#TYPE_DIRECT}/{@link JaspiraEvent#TYPE_BOTTOM_UP}/{@link JaspiraEvent#TYPE_FLOOD}/{@link JaspiraEvent#TYPE_GLOBAL})
	 */
	private int type;

	/**
	 * Propagation level up to which this event is to be passed.
	 * ({@link Plugin#LEVEL_APPLICATION}/{@link Plugin#LEVEL_FRAME}/
	 *  {@link Plugin#LEVEL_PAGE}/{@link Plugin#LEVEL_PLUGIN})
	 */
	private int level;

	/** Used to mark passed paths during event processing. */
	private Plugin brand;

	/** Used to cache the group of this event (i\.e\. the event name minus the last element). */
	private String eventGroup;

	/** Classs name of possible receivers of this event */
	private String targetClassName;

	/** Plugin id of the receiver plugin of this event */
	private String targetPluginId;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new event with all possible parameters.
	 * Usually it will be sufficient to use one of the shorter constructors below.
	 *
	 * @param source The plugin that dispatches this event. Must not be null.
	 * Will be converted to lower case.
	 * @param eventName The name of the event
	 * @param object An additional data object. Can be null.
	 * @param type The type of the event (i\.e\. the mode of passing the event).
	 * @param level The top level for passing of this event
	 * @param flags Flags for this event
	 */
	public JaspiraEvent(Plugin source, String eventName, Object object, int type, int level, int flags)
	{
		// Use the application base as event source (for global events, usually) of no source plugin given
		super(source != null ? source : ApplicationBase.getInstance());

		if (eventName == null || eventName.length() == 0)
		{
			throw new IllegalArgumentException("EventName must not be null");
		}
		this.eventName = eventName.toLowerCase();
		this.flags = flags;
		this.object = object;

		this.level = level;
		this.type = type;

		int index = eventName.lastIndexOf('.');
		eventGroup = (index >= 0) ? eventName.substring(0, index) : eventName;
	}

	/**
	 * Creates a standard event with no data object, flood passing mode,
	 * application level and no flags.
	 * @nowarn
	 */
	public JaspiraEvent(Plugin source, String eventName)
	{
		this(source, eventName, null, TYPE_FLOOD, Plugin.LEVEL_APPLICATION, 0);
	}

	/**
	 * Creates an application wide flood event with the given data object.
	 * @nowarn
	 */
	public JaspiraEvent(Plugin source, String eventName, Object object)
	{
		this(source, eventName, object, TYPE_FLOOD, Plugin.LEVEL_APPLICATION, 0);
	}

	/**
	 * Creates an event with the given level and type and no data object.
	 * @nowarn
	 */
	public JaspiraEvent(Plugin source, String eventName, int type, int level)
	{
		this(source, eventName, null, type, level, 0);
	}

	/**
	 * Creates an event with the given level, type and data object.
	 * @nowarn
	 */
	public JaspiraEvent(Plugin source, String eventName, Object object, int type, int level)
	{
		this(source, eventName, object, type, level, 0);
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Access methods
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the original event.
	 *
	 * @return The original event object (e.g. SelectionEvent,
	 * ActionEvent, ListChangedEvent, ...) if there is one or null
	 */
	public EventObject getOriginalEvent()
	{
		return getObject() instanceof EventObject ? (EventObject) getObject() : null;
	}

	/**
	 * Gets source plug in.
	 * @nowarn
	 */
	public Plugin getSourcePlugin()
	{
		return (Plugin) getSource();
	}

	/**
	 * Gets event name.
	 * @nowarn
	 */
	public String getEventName()
	{
		return eventName;
	}

	/**
	 * Gets the event flags.
	 * @nowarn
	 */
	public int getFlags()
	{
		return flags;
	}

	/**
	 * Checks if the event is unconsumable.
	 * An unconsumable event that can't be consumed by a listener.
	 * @nowarn
	 */
	public boolean isUnconsumable()
	{
		return (flags & UNCONSUMABLE) != 0;
	}

	/**
	 * Checks if the event has been consumed by a listener.
	 * @nowarn
	 */
	public boolean isConsumed()
	{
		return (flags & CONSUMED) != 0;
	}

	/**
	 * Checks if the event can be stacked.
	 * A stackable event can be stacked with events of the same type.
	 * Only the most recent event of this type will be kept and will be executed by
	 * the Swing event queue. A typical example is the global.environment.update event.
	 * @nowarn
	 */
	public boolean isStackable()
	{
		return (flags & STACKABLE) != 0;
	}

	/**
	 * Updates the event flags.
	 *
	 * @param flag Flag bits to set
	 */
	public void updateFlags(int flag)
	{
		flags = flags | flag;
	}

	/**
	 * Updates the event flags using the given module event handler return code.
	 *
	 * @param returnCode Event handler return code
	 * ({@link EventModule#EVENT_HANDLED}/{@link EventModule#EVENT_IGNORED}/{@link EventModule#EVENT_CONSUMED})
	 * @return
	 *		true	The event has been consumed
	 *		false	The event is not consumed yet
	 */
	public boolean updateFlags(JaspiraEventHandlerCode returnCode)
	{
		if (returnCode == EventModule.EVENT_CONSUMED && !isUnconsumable())
		{
			flags |= CONSUMED;
			return true;
		}

		if (returnCode == EventModule.EVENT_HANDLED)
		{
			flags |= HANDELED;
		}

		return false;
	}

	/**
	 * Returns the event type.
	 * @return {@link JaspiraEvent#TYPE_DIRECT}/{@link JaspiraEvent#TYPE_BOTTOM_UP}/{@link JaspiraEvent#TYPE_FLOOD}/{@link JaspiraEvent#TYPE_GLOBAL}
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * Returns the propagation level up to which this event is to be passed.
	 * @return {@link Plugin#LEVEL_APPLICATION}/{@link Plugin#LEVEL_FRAME}/
	 * {@link Plugin#LEVEL_PAGE}/{@link Plugin#LEVEL_PLUGIN}
	 */
	public int getLevel()
	{
		return level;
	}

	/**
	 * Gets the event object.
	 * @return The object to pass along with this event or null
	 */
	public Object getObject()
	{
		return object;
	}

	/**
	 * Sets the event object.
	 * @param object The object to pass along with this event or null
	 */
	public void setObject(Object object)
	{
		this.object = object;
	}

	/**
	 * Gets the classs name of possible receivers of this event.
	 * @return The class name or null if the event is not directed to a particular type of plugin
	 */
	public String getTargetClassName()
	{
		return targetClassName;
	}

	/**
	 * Sets the classs name of possible receivers of this event.
	 * @param targetClassName The class name or null if the event is not directed to a particular type of plugin
	 */
	public void setTargetClassName(String targetClassName)
	{
		this.targetClassName = targetClassName;
	}

	/**
	 * Gets the plugin id of the receiver plugin of this event.
	 * @return The plugin id or null if the event is not directed to a particular plugin
	 */
	public String getTargetPluginId()
	{
		return targetPluginId;
	}

	/**
	 * Sets the plugin id of the receiver plugin of this event.
	 * @param targetPluginId The plugin id or null if the event is not directed to a particular plugin
	 */
	public void setTargetPluginId(String targetPluginId)
	{
		this.targetPluginId = targetPluginId;
	}

	/**
	 * Returns a String which describes the object.
	 * @nowarn
	 */
	public String toString()
	{
		return "[JaspiraEvent] " + getEventName() + " from: " + source;
	}

	/**
	 * Gets the brand.
	 * Used to mark passed paths during event processing.
	 * @nowarn
	 */
	public Plugin getBrand()
	{
		return brand;
	}

	/**
	 * Sets the brand.
	 * Used to mark passed paths during event processing.
	 * @nowarn
	 */
	public void brand(Plugin brand)
	{
		this.brand = brand;
	}

	/**
	 * Returns the event group.
	 * @return The event name minus the last element
	 */
	public String getEventGroup()
	{
		return eventGroup;
	}
}
