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

/**
 * The item container is a helper class for deserialization of item xml files.
 * The problem is that an item can have different faces (for example an item
 * can actually be a Java activity, a process activity etc.).
 * When deserializing such an item from a file (that contains a single item only),
 * the particular type of the item cannot be specified in the top-level xml element of a Castor mapping.
 * Hence we wrap this top-level attribute in a container class that may
 * specify the type of its content. Not pretty, but it works.
 *
 * @author Heiko Erhardt
 */
public class ItemContainer
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** The contained item */
	private Item item;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ItemContainer()
	{
	}

	/**
	 * Value constructor.
	 *
	 * @param item The contained item
	 */
	public ItemContainer(Item item)
	{
		this.item = item;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the the contained item.
	 * @nowarn
	 */
	public Item getItem()
	{
		return item;
	}

	/**
	 * Sets the the contained item.
	 * @nowarn
	 */
	public void setItem(Item item)
	{
		this.item = item;
	}
}
