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
package org.openbp.core.model.item.activity;

import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.PlaceholderNode;
import org.openbp.core.model.item.process.PlaceholderNodeImpl;
import org.openbp.core.model.item.process.ProcessItem;

/**
 * Pseudo item that exists in order to edit placeholder nodes.
 * This item has a single property that will be copied to the placeholder node.
 * It is a reference path to some process element.
 *
 * @author Heiko Erhardt
 */
public class PlaceholderItemImpl extends ActivityItemImpl
	implements PlaceholderItem
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Reference path to some process element */
	private String referencePath;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public PlaceholderItemImpl()
	{
		setItemType(ItemTypes.PSEUDO_PLACEHOLDER);
	}

	/**
	 * Copies the values of the source object to this object.
	 *
	 * @param source The source object. Must be of the same type as this object.
	 * @param copyMode Determines if a deep copy, a first level copy or a shallow copy is to be
	 * performed. See the constants of the org.openbp.common.generic.description.Copyable class.
	 * @throws CloneNotSupportedException If the cloning of one of the contained objects failed
	 */
	public void copyFrom(Object source, int copyMode)
		throws CloneNotSupportedException
	{
		if (source == this)
			return;
		super.copyFrom(source, copyMode);

		PlaceholderItemImpl src = (PlaceholderItemImpl) source;

		referencePath = src.referencePath;
	}

	/**
	 * Creates a new placeholder node that references this placeholder.
	 *
	 * @return The new {@link PlaceholderNode}
	 * @copy NodeProvider.toNode
	 */
	public Node toNode(ProcessItem process, int syncFlags)
	{
		PlaceholderNode result = new PlaceholderNodeImpl();

		result.setProcess(process);
		result.copyFromItem(this, syncFlags);

		return result;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the reference path to some process element.
	 * @nowarn
	 */
	public String getReferencePath()
	{
		return referencePath;
	}

	/**
	 * Sets the reference path to some process element.
	 * @nowarn
	 */
	public void setReferencePath(String referencePath)
	{
		this.referencePath = referencePath;
	}
}
