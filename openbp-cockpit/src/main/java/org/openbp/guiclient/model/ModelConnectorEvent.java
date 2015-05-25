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
package org.openbp.guiclient.model;

import java.io.Serializable;

import org.openbp.common.util.observer.ObserverEvent;
import org.openbp.core.model.ModelQualifier;

/**
 * A model connector event is triggered whenever there is a change to the model model.
 * model connector events can be subscribed by register an observer with the model model
 * using the {@link ModelConnector#registerObserver} method.
 *
 * @author Heiko Erhardt
 */
public class ModelConnectorEvent
	implements ObserverEvent, Serializable
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** The models have been initially loaded; This event is being called at startup and after a reload operation */
	public static final String MODELS_LOADED = "ModelLoaded";

	/** Some batch operation has been performed on several models */
	public static final String MODEL_BATCH = "ModelBatch";

	/** A model has been added */
	public static final String MODEL_ADDED = "ModelAdded";

	/** A model has been updated */
	public static final String MODEL_UPDATED = "ModelUpdated";

	/** A model has been removed */
	public static final String MODEL_REMOVED = "ModelRemoved";

	/** A model has been renamed */
	public static final String MODEL_RENAMED = "ModelRenamed";

	/** Some batch operation has been performed on several items */
	public static final String ITEM_BATCH = "ItemBatch";

	/** An item has been added */
	public static final String ITEM_ADDED = "ItemAdded";

	/** An item has been updated */
	public static final String ITEM_UPDATED = "ItemUpdated";

	/** An item has been removed */
	public static final String ITEM_REMOVED = "ItemRemoved";

	/** An item has been renamed */
	public static final String ITEM_RENAMED = "ItemRenamed";

	public static final String [] SUPPORTED_EVENT_TYPES = 
	{ MODELS_LOADED, MODEL_BATCH, MODEL_ADDED, MODEL_UPDATED, MODEL_REMOVED, MODEL_RENAMED, ITEM_BATCH, ITEM_ADDED, ITEM_UPDATED, ITEM_REMOVED, ITEM_RENAMED };

	/**
	 * Returns a list of supported event types.
	 * @nowarn
	 */
	public static final String [] getSupportedEventTypes()
	{
		return SUPPORTED_EVENT_TYPES;
	}

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Event type (see the constants of this class) */
	protected String eventType;

	/** Model qualifier of the model or item concerned by this event or null for the {@link #MODELS_LOADED} event */
	private ModelQualifier qualifier;

	/** Previous model qualifier of the model or item in case of a rename operation */
	private ModelQualifier previousQualifier;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ModelConnectorEvent()
	{
	}

	/**
	 * Value constructor.
	 *
	 * @param eventType Event type (see the constants of this class)
	 */
	public ModelConnectorEvent(String eventType)
	{
		this.eventType = eventType;
	}

	/**
	 * Value constructor.
	 *
	 * @param eventType Event type (see the constants of this class)
	 * @param qualifier Model qualifier of the model or item concerned by this event or null for the {@link #MODELS_LOADED} event
	 */
	public ModelConnectorEvent(String eventType, ModelQualifier qualifier)
	{
		this.eventType = eventType;
		this.qualifier = qualifier;
	}

	/**
	 * Value constructor.
	 *
	 * @param eventType Event type (see the constants of this class)
	 * @param qualifier Model qualifier of the model or item concerned by this event or null for the {@link #MODELS_LOADED} event
	 * @param previousQualifier Previous model qualifier of the model or item in case of a rename operation
	 */
	public ModelConnectorEvent(String eventType, ModelQualifier qualifier, ModelQualifier previousQualifier)
	{
		this.eventType = eventType;
		this.qualifier = qualifier;
		this.previousQualifier = previousQualifier;
	}

	/**
	 * Returns a string representation of the object.
	 *
	 * @return The multi-line string includes the event type and all information that is
	 * present in respect to this type.
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();

		sb.append("ModelConnector: Event = " + eventType + "\r\n");
		sb.append("\tQualifier = " + qualifier);
		if (previousQualifier != null)
		{
			sb.append("\tPrevious model qualifier = " + previousQualifier);
		}

		return sb.toString();
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the event type (see the constants of this class).
	 * @nowarn
	 */
	public String getEventType()
	{
		return eventType;
	}

	/**
	 * Template method that signalizes if subsequent observers shall be skipped.
	 * @return true to break the observer calling loop for this event
	 */
	public boolean shallSkipSubsequentObservers()
	{
		return false;
	}

	/**
	 * Gets the jOI of the model or item concerned by this event or null for the {@link #MODELS_LOADED} event.
	 * @return The qualifier will be null for {@link #MODELS_LOADED}, {@link #MODEL_BATCH} and {@link #ITEM_BATCH} events
	 */
	public ModelQualifier getQualifier()
	{
		return qualifier;
	}

	/**
	 * Gets the previous model qualifier of the model or item in case of a rename operation.
	 * @nowarn
	 */
	public ModelQualifier getPreviousQualifier()
	{
		return previousQualifier;
	}
}
