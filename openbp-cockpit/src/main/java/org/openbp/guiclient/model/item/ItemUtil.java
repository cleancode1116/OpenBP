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
package org.openbp.guiclient.model.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.common.CollectionUtil;
import org.openbp.common.generic.propertybrowser.ObjectDescriptor;
import org.openbp.common.generic.propertybrowser.ObjectDescriptorMgr;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.core.model.Model;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypeDescriptor;

/**
 * Item utility methods.
 *
 * @author Heiko Erhardt
 */
public class ItemUtil
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private ItemUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	/**
	 * Ensures that the name of an item is unique in the specified model and
	 * adjusts the item name by appending a running number.
	 * The method also changes the display name of the item accordingly by appending
	 * the running number, e. g. "Name (2)".
	 *
	 * @param item Item to check
	 * @param model Reference model
	 */
	public static void ensureUniqueItemName(Item item, Model model)
	{
		// Find a new name for the item; we generate the name by appending a running
		// number to the item type

		String name = item.getName();

		// Iterate the current item list and count all items start start with the type name
		String itemType = item.getItemType();
		Iterator modelItemIterator = model.getItems(itemType);
		List allItems = new ArrayList();
		CollectionUtil.addAll(allItems, modelItemIterator);

		String newName = NamedObjectCollectionUtil.createUniqueId(allItems, name);
		item.setName(newName);

		if (!newName.equals(name))
		{
			String appendix = newName.substring(name.length());
			String displayName = item.getDisplayName();
			if (displayName == null)
				displayName = name;
			displayName += " (" + appendix + ")";
			item.setDisplayName(displayName);
		}
	}

	/**
	 * Gets an object descriptor for the item's interface class.
	 *
	 * @param itd Item type descriptor or null
	 * @return The object descriptor or null on error
	 */
	public static ObjectDescriptor obtainObjectDescriptor(ItemTypeDescriptor itd)
	{
		if (itd != null)
		{
			try
			{
				// Get the object descriptor, but skip errors
				return ObjectDescriptorMgr.getInstance().getDescriptor(itd.getItemInterface(), 0);
			}
			catch (XMLDriverException e)
			{
				// Silently ignored, this is caught elsewhere anyway...
			}
		}
		return null;
	}
}
