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
package org.openbp.common.util.observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openbp.common.CollectionUtil;

/**
 * The observer manager helps registering and calling observers.
 *
 * @author Heiko Erhardt
 */
public class EventObserverMgr
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Hash table mapping event types to clients registered for this event type */
	private Map eventTypesToObservers;

	/** Flag that disables broadcasting of model connector events (useful for batch operations) */
	private boolean eventsSuspended;

	/** List of supported event types */
	private String [] supportedEventTypes;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public EventObserverMgr()
	{
	}

	/**
	 * Gets the list of possible event types.
	 * @nowarn
	 */
	public String [] getPossibleEventTypes()
	{
		return supportedEventTypes;
	}

	/**
	 * Sets the list of supported event types.
	 * @nowarn
	 */
	public void setSupportedEventTypes(String [] supportedEventTypes)
	{
		this.supportedEventTypes = supportedEventTypes;
	}

	/**
	 * Registers an observer.
	 *
	 * @param observer The observer
	 * @param eventTypes Lit of event types the observer wants to be notified of
	 * or null for all event types
	 */
	public void registerObserver(EventObserver observer, String [] eventTypes)
	{
		if (eventTypesToObservers == null)
			eventTypesToObservers = new HashMap();

		if (eventTypes == null || eventTypes.length == 0)
		{
			// No types specified, get all available
			if (supportedEventTypes == null)
			{
				throw new IllegalArgumentException("Trying to register an observer for any event type, but no list of supported event types specified.");
			}
			eventTypes = supportedEventTypes;
		}

		for (int i = 0; i < eventTypes.length; ++i)
		{
			String eventType = eventTypes [i];

			List observers = (List) eventTypesToObservers.get(eventType);
			if (observers == null)
			{
				// This type hasn't been used so far by a observer
				observers = new ArrayList();
				eventTypesToObservers.put(eventType, observers);
			}

			// Add the observer to the list of observers for this type
			if (!observers.contains(observer))
			{
				observers.add(observer);
			}
		}
	}

	/**
	 * Unregisters an observer.
	 *
	 * @param observer The observer
	 */
	public void unregisterObserver(EventObserver observer)
	{
		if (eventTypesToObservers == null)
			return;

		// Iterate all event types
		for (Iterator it = eventTypesToObservers.values().iterator(); it.hasNext();)
		{
			List observers = (List) it.next();

			// Remove the observer from the list of observers for this event type
			CollectionUtil.removeReference(observers, observer);

			if (observers.isEmpty())
			{
				// If there is no observer left in this list remove the event type from the hash table
				it.remove();
			}
		}

		if (eventTypesToObservers.isEmpty())
		{
			// This indicates that the no observer is active any more
			eventTypesToObservers = null;
		}
	}

	/**
	 * Checks if there are active observers registered.
	 *
	 * @return true if there is at least one observer
	 */
	public boolean hasActiveObservers()
	{
		return eventTypesToObservers != null && !eventsSuspended;
	}

	/**
	 * Checks if there are observers registered that are interested in the given event type.
	 *
	 * @param eventType Type of event in question
	 * @return true if there is at least one observer registered for this event type
	 */
	public boolean hasActiveObservers(String eventType)
	{
		if (eventTypesToObservers != null && !eventsSuspended)
		{
			List observers = (List) eventTypesToObservers.get(eventType);
			if (observers != null)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Suspends broadcasting of model connector events.
	 *
	 * @return The previous suspend status
	 */
	public boolean suspendObserverEvents()
	{
		boolean ret = eventsSuspended;
		eventsSuspended = true;
		return ret;
	}

	/**
	 * Resumes broadcasting of model connector events.
	 */
	public void resumeObserverEvents()
	{
		eventsSuspended = false;
	}

	/**
	 * Notifies all registered observers about a model connector event.
	 *
	 * @param event Model connector event to dispatch
	 */
	public void fireEvent(ObserverEvent event)
	{
		if (eventTypesToObservers != null && !eventsSuspended)
		{
			String eventType = event.getEventType();

			List observers = (List) eventTypesToObservers.get(eventType);
			if (observers != null)
			{
				int nObservers = observers.size();
				for (int i = 0; i < nObservers; ++i)
				{
					EventObserver observer = (EventObserver) observers.get(i);
					observer.observeEvent(event);
					if (event.shallSkipSubsequentObservers())
						break;
				}
			}
		}
	}
}
