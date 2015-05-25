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
package org.openbp.core.model.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.common.CollectionUtil;
import org.openbp.common.ReflectException;
import org.openbp.common.ReflectUtil;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.core.model.Model;

/**
 * Factory class for regular items.
 * @seec ItemTypeDescriptor
 *
 * @author Heiko Erhardt
 */
public class StandardItemFactory
	implements ItemFactory
{
	//////////////////////////////////////////////////
	// @@ Data members used by the default implementation
	//////////////////////////////////////////////////

	/** Item type descriptor */
	private ItemTypeDescriptor itemTypeDescriptor;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public StandardItemFactory()
	{
	}

	/**
	 * Gets the item type descriptor.
	 * @nowarn
	 */
	public ItemTypeDescriptor getItemTypeDescr()
	{
		return itemTypeDescriptor;
	}

	/**
	 * Sets the item type descriptor.
	 * @nowarn
	 */
	public void setItemTypeDescr(ItemTypeDescriptor itemTypeDescriptor)
	{
		this.itemTypeDescriptor = itemTypeDescriptor;
	}

	//////////////////////////////////////////////////
	// @@ Factory methods
	//////////////////////////////////////////////////

	/**
	 * Creates a new item.
	 * The default implementation just performs Class.newInstance on the item class
	 * and assigns a name to the item that
	 *
	 * @param model Model the item shall belong to
	 * @param suggestedName Suggested name or null for a default item-type-specific name.
	 * The method guarantees that the generated name will be unique within the items of this type of this model.
	 * @param suggestedDisplayName Suggested display name or null
	 * @return The new item instance or null on error
	 */
	public Item createItem(Model model, String suggestedName, String suggestedDisplayName)
	{
		// Instantiate the item
		Item item;
		String type = itemTypeDescriptor.getItemType();
		String name = suggestedName;
		if (name == null)
		{
			name = type;
		}
		try
		{
			item = (Item) ReflectUtil.instantiate(itemTypeDescriptor.getItemClass(), Item.class, "component");
		}
		catch (ReflectException e)
		{
			LogUtil.error(getClass(), "Cannot instantiate component of type $0.", type, e);

			return null;
		}

		// Find a new name for the item; we generate the name by appending a running number to the name
		// Iterate the current item list and count all items start start with the type name
		if (model != null)
		{
			Iterator itemIterator = model.getItems(type);
			List items = new ArrayList();
			CollectionUtil.addAll(items, itemIterator);
			item.setName(NamedObjectCollectionUtil.createUniqueId(items, name));
			item.setModel(model);
		}
		else
		{
			item.setName(name);
		}
		item.setDisplayName(suggestedDisplayName);

		return item;
	}
}
