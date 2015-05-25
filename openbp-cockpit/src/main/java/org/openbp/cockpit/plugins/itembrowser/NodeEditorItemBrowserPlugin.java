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
package org.openbp.cockpit.plugins.itembrowser;

import org.openbp.core.model.Model;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.guiclient.model.item.itemtree.ItemTreeEvent;
import org.openbp.jaspira.gui.interaction.BasicTransferable;

/**
 * Item browser that displays data types and icons only.
 *
 * @author Jens Ferchland
 */
public class NodeEditorItemBrowserPlugin extends ItemBrowserPlugin
{
	/**
	 * Default constructor.
	 */
	public NodeEditorItemBrowserPlugin()
	{
		super();

		setSupportedItemTypes(new String [] { ItemTypes.TYPE, });
	}

	//////////////////////////////////////////////////
	// @@ ItemBrowserPlugin overrides
	//////////////////////////////////////////////////

	/**
	 * The item browser used in the node editor will not provide the ability to edit its items
	 * in the property browser.
	 * The o.e. is used for diagram elements only.
	 * So we simply do nothing here.
	 *
	 * @param item An item or a model
	 */
	protected void firePropertyBrowserEvent(Item item)
	{
	}

	/**
	 * Called when an item tree event has happened.
	 *
	 * @param e Item tree event holding the event information
	 */
	public void handleItemTreeEvent(ItemTreeEvent e)
	{
		if (e.eventType == ItemTreeEvent.OPEN)
		{
			// Double-click or ENTER ->
			// Simulate a DnD of the item to the currently selected model object
			Item item = getSelectedItem();

			if (item != null && !(item instanceof Model))
			{
				fireEvent("modeler.view.importtoselection", new BasicTransferable(item));
			}
		}
		else
		{
			super.handleItemTreeEvent(e);
		}
	}
}
