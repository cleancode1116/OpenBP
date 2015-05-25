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
package org.openbp.guiclient.model.item.itemtree;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.openbp.core.model.ModelObject;

/**
 * Editing event.
 * Fired whenever an event during editing of a property editor occurs
 * that may require interaction with the component that hosts the editor.
 *
 * @author Heiko Erhardt
 */
public class ItemTreeEvent
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** The current selection has changed */
	public static final int SELECTION_CHANGED = 1;

	/** A tree node was doubleclicked or ENTER was pressed */
	public static final int OPEN = 2;

	/** A tree node was right-clicked */
	public static final int POPUP = 3;

	/**
	 * An object shall be added to the tree.
	 * The event handler may check if this particular object should appear in the tree.
	 * Set the {@link #cancel} flag to prevent adding the object.
	 */
	public static final int IS_SUPPORTED = 4;

	/**
	 * An object that has been selected by the user shall be added to the list of selected items.
	 * The event handler may check if this particular object should appear in the list.
	 * Set the {@link #cancel} flag to prevent adding the object to the list.
	 */
	public static final int IS_SELECTABLE = 5;

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Item tree that caused the event */
	public ItemTree itemTree;

	/** Event code (see the constants of this class) */
	public int eventType;

	/** Tree node that is associated with this event, if any */
	public ItemTree.ItemTreeNode treeNode;

	/** Object that is associated with this event, if any */
	public ModelObject object;

	/**
	 * Mouse event associated with the event.
	 * If the event was not caused by a mouse interaction, this member will be null.
	 */
	public MouseEvent mouseEvent;

	/**
	 * Key event associated with the event.
	 * If a key code is not applicable or the event was caused by e. g. a mouse interaction,
	 * this member will be null.
	 */
	public KeyEvent keyEvent;

	/** Cancel flag */
	public boolean cancel;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param itemTree Property editor
	 * @param eventType Event code (see the constants of this class)
	 */
	public ItemTreeEvent(ItemTree itemTree, int eventType)
	{
		this.eventType = eventType;
		this.itemTree = itemTree;
	}
}
