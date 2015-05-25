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

import org.openbp.core.model.item.Item;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.plugin.Plugin;

/**
 * Jaspira event containing all item relevant information
 *
 * @author Andreas Putz
 */
public class ItemEvent extends JaspiraEvent
{
	//////////////////////////////////////////////////
	// @@ Symbolic constants
	//////////////////////////////////////////////////

	/** None event flag */
	public static final int NONE = -1;

	/** Update event flag */
	public static final int UPDATE = 1;

	/** New item selected flag */
	public static final int NEW_ITEM = 2;

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Current event flag */
	public int eventFlag = NONE;

	/** Unmodified item */
	public Item originalItem;

	/** Current / Modified item */
	public Item item;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param source The events source object
	 * @param eventName The name of the event
	 */
	public ItemEvent(Plugin source, String eventName)
	{
		super(source, eventName);
	}

	//////////////////////////////////////////////////
	// @@ Initialize methods
	//////////////////////////////////////////////////

	/**
	 * Set the event to a set new item event.
	 *
	 * @param item Item which is selected
	 */
	public void setNewItemInfo(Item item)
	{
		eventFlag = NEW_ITEM;
		this.item = item;
	}

	/**
	 * Set the event to a update item event.
	 *
	 * @param original The unmodified item
	 * @param modified The modified item
	 */
	public void setUpdateInfo(Item original, Item modified)
	{
		eventFlag = UPDATE;
		this.originalItem = original;
		this.item = modified;
	}
}
