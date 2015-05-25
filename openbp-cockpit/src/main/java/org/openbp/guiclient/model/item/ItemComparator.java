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

import java.util.Comparator;

import org.openbp.core.model.item.Item;
import org.openbp.guiclient.plugins.displayobject.DisplayObjectPlugin;

/**
 * Display object comparator.
 * This class is a singleton.
 *
 * @author Heiko Erhardt
 */
public class ItemComparator
	implements Comparator
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Item types supported by this comparator */
	private String [] itemTypes;

	/** Nuber of item types */
	private int nTypes;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor.
	 */
	public ItemComparator()
	{
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Sets the item types supported by this comparator.
	 * @param itemTypes The item types in the order they should appear or null if the item type order doesn't matter
	 */
	public void setItemTypes(String [] itemTypes)
	{
		this.itemTypes = itemTypes;
		nTypes = itemTypes != null ? itemTypes.length : 0;
	}

	//////////////////////////////////////////////////
	// @@ Comparator implementation
	//////////////////////////////////////////////////

	public int compare(Object o1, Object o2)
	{
		Item i1 = (Item) o1;
		Item i2 = (Item) o2;

		if (itemTypes != null)
		{
			String type1 = i1.getItemType();
			String type2 = i2.getItemType();
			if (!type1.equals(type2))
			{
				int n1 = getTypeIndex(type1);
				int n2 = getTypeIndex(type2);

				return n1 - n2;
			}
		}

		String s1;
		String s2;

		if (DisplayObjectPlugin.getInstance().isTitleModeText())
		{
			s1 = i1.getDisplayText();
			s2 = i2.getDisplayText();
		}
		else
		{
			s1 = i1.getName();
			s2 = i2.getName();
		}

		return s1.compareTo(s2);
	}

	/**
	 * Gets the index for the given item type.
	 *
	 * @param type Type
	 * @return The index or -1 if not found
	 */
	private int getTypeIndex(String type)
	{
		for (int i = 0; i < nTypes; ++i)
		{
			if (type.equals(itemTypes [i]))
				return i;
		}
		return -1;
	}
}
