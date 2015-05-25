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

import java.awt.Dimension;
import java.awt.Frame;
import java.util.List;

import javax.swing.JScrollPane;

import org.openbp.core.model.ModelObject;
import org.openbp.swing.components.JStandardDialog;

/**
 * Item selection dialog.
 *
 * @author Heiko Erhardt
 */
public class ItemSelectionDialog extends JStandardDialog
	implements ItemTreeListener
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** the default size of the editor */
	private static final Dimension SIZE = new Dimension(300, 600);

	/** Tree */
	protected ItemTree tree;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 * @param owner Owning frame. The dialog will be centered over the owner.
	 * @param modal
	 * true: Display as modal dialog.<br>
	 * false: Display as modeless dialog.
	 */
	public ItemSelectionDialog(Frame owner, boolean modal)
	{
		super(owner, modal);

		setSize(SIZE);

		// Set up the item tree
		createTree();

		tree.addItemTreeListener(this);

		getMainPane().add(new JScrollPane(tree));

		getOkBtn().setEnabled(false);
	}

	//////////////////////////////////////////////////
	// @@ Public methods
	//////////////////////////////////////////////////

	/**
	 * Rebuilds the model/item tree.
	 */
	public void rebuildTree()
	{
		tree.rebuildTree();
	}

	/**
	 * Saves the state of the item tree.
	 *
	 * @return The saved state
	 */
	public ItemTreeState saveState()
	{
		return tree.saveState();
	}

	/**
	 * Restores the state of the item tree.
	 *
	 * @param state State to restore
	 */
	public void restoreState(ItemTreeState state)
	{
		tree.restoreState(state);
	}

	/**
	 * Expands the tree up to the desired level.
	 *
	 * @param level Maximum level to display
	 */
	public void expand(int level)
	{
		tree.expand(level);
	}

	//////////////////////////////////////////////////
	// @@ Overridables
	//////////////////////////////////////////////////

	/**
	 * Creates the item tree.
	 * Called by the constructor.
	 * Creates a regular {@link ItemTree} by default.
	 */
	protected void createTree()
	{
		tree = new ItemTree();
	}

	//////////////////////////////////////////////////
	// @@ ItemTreeListener implementation
	//////////////////////////////////////////////////

	/**
	 * Called when an item tree event has happened.
	 *
	 * @param e Item tree event holding the event information
	 */
	public void handleItemTreeEvent(ItemTreeEvent e)
	{
		if (e.eventType == ItemTreeEvent.SELECTION_CHANGED)
		{
			// Update Ok button status according to selection
			List selection = getSelectedObjects();
			getOkBtn().setEnabled(selection != null);
		}

		if (e.eventType == ItemTreeEvent.OPEN)
		{
			// ENTER/double click means close the dialog if we have a valid selection
			List selection = getSelectedObjects();
			if (selection != null)
			{
				dispose();
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the currently selected objects.
	 *
	 * @return A list of {@link ModelObject} s.<br>
	 * The objects in the list are either items of one of the types specified in
	 * the list set by the {@link #setSelectableItemTypes} method or objects of
	 * the model object classes set by the {@link #setSelectableObjectClasses} method.<br>
	 * If no object matching this criteria is selected or the dialog has been cancelled,
	 * null will be returned.
	 */
	public List getSelectedObjects()
	{
		if (isCancelled())
			return null;
		return tree.getSelectedObjects();
	}

	/**
	 * Selects the given objects.
	 * The current selection will be cleared.
	 * Call this method after the tree has been initialized using the {@link #rebuildTree} method.
	 *
	 * @param objects A list of {@link ModelObject} s or null
	 */
	public void setSelectedObjects(List objects)
	{
		tree.setSelectedObjects(objects);
	}

	/**
	 * Sets the the root object of the item tree or null for the root of the model tree.
	 * @nowarn
	 */
	public void setRootObject(ModelObject rootObject)
	{
		tree.setRootObject(rootObject);
	}

	/**
	 * Determines if the root node will be visible.
	 * Default: false.
	 * @nowarn
	 */
	public void setRootVisible(boolean visible)
	{
		tree.setRootVisible(visible);
	}

	/**
	 * Sets the item types supported by this browser (null for the standard tem types).
	 * Call this method before the plugin is installed (best use the constructor of derived classes).
	 * @nowarn
	 */
	public void setSupportedItemTypes(String [] supportedItemTypes)
	{
		tree.setSupportedItemTypes(supportedItemTypes);
	}

	/**
	 * Sets the item types selectable by the user (null if no item types selectable).
	 * @nowarn
	 */
	public void setSelectableItemTypes(String [] selectableItemTypes)
	{
		tree.setSelectableItemTypes(selectableItemTypes);
	}

	/**
	 * Sets the model object classes supported by this item browser (null if no model objects displayed).
	 * @nowarn
	 */
	public void setSupportedObjectClasses(Class [] supportedObjectClasses)
	{
		tree.setSupportedObjectClasses(supportedObjectClasses);
	}

	/**
	 * Sets the model object classes selectable by the user (null if no model objects selectable).
	 * @nowarn
	 */
	public void setSelectableObjectClasses(Class [] selectableObjectClasses)
	{
		tree.setSelectableObjectClasses(selectableObjectClasses);
	}

	/**
	 * Sets the group display flag.
	 * @nowarn
	 */
	public void setShowGroups(boolean showGroups)
	{
		tree.setShowGroups(showGroups);
	}

	/**
	 * Sets the selection mode.
	 * @param selectionMode {@link ItemTree#SELECTION_NONE}/{@link ItemTree#SELECTION_SINGLE}/{@link ItemTree#SELECTION_MULTI}
	 */
	public void setSelectionMode(int selectionMode)
	{
		tree.setSelectionMode(selectionMode);
	}

	/**
	 * Gets the tree.
	 * @nowarn
	 */
	public ItemTree getTree()
	{
		return tree;
	}
}
