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
package org.openbp.guiclient.event;

import org.openbp.core.model.Model;
import org.openbp.core.model.item.ItemTypeDescriptor;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.plugin.Plugin;

/**
 * Jaspira event containing all model relevant information.
 *
 * @author Andreas Putz
 */
public class ModelEvent extends JaspiraEvent
{
	//////////////////////////////////////////////////
	// @@ Symbolic constants
	//////////////////////////////////////////////////

	/** None event flag */
	public static final int NONE = -1;

	/** Update event flag */
	public static final int UPDATE = 1;

	/** New model selected flag */
	public static final int NEW_MODEL = 2;

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Curren event flag */
	public int eventFlag = NONE;

	/** Unmodified model */
	public Model unmodifiedModel;

	/** Modified / Current model */
	public Model model;

	/** Supported item types by the model */
	public ItemTypeDescriptor [] supportedItemTypes;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param source The events source object
	 * @param eventName The name of the event
	 */
	public ModelEvent(Plugin source, String eventName)
	{
		super(source, eventName);
	}

	//////////////////////////////////////////////////
	// @@ Initialize methods
	//////////////////////////////////////////////////

	/**
	 * Set the event to a set new model to the browser event.
	 *
	 * @param model The model which is selected
	 * @param supportedItemTypes The supported item types by that model
	 */
	public void setNewModelInfo(Model model, ItemTypeDescriptor [] supportedItemTypes)
	{
		eventFlag = NEW_MODEL;
		this.model = model;
		this.supportedItemTypes = supportedItemTypes;
	}

	/**
	 * Set the event to a update item event.
	 *
	 * @param original The unmodified model
	 * @param modified The modified model
	 */
	public void setUpdateInfo(Model original, Model modified)
	{
		eventFlag = UPDATE;
		this.unmodifiedModel = original;
		this.model = modified;
	}
}
