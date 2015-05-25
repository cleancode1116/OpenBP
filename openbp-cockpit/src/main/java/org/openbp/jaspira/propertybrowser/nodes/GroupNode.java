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
package org.openbp.jaspira.propertybrowser.nodes;

import org.openbp.common.util.ToStringHelper;

/**
 * Property browser tree node that reflects a group node of a property group
 * as specified in the object descriptor.
 *
 * @author Erich Lauterbach
 */
public class GroupNode extends AbstractNode
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** The name of the group. */
	private String groupName;

	//////////////////////////////////////////////////
	// @@ Constructors
	//////////////////////////////////////////////////

	/**
	 * Default constructor
	 *
	 * @param groupName Name of the group (functional name)
	 */
	public GroupNode(String groupName)
	{
		super();
		this.groupName = groupName;
	}

	/**
	 * Returns a string representation of this object.
	 * @nowarn
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "groupName");
	}

	//////////////////////////////////////////////////
	// @@ AbstractNode overides
	//////////////////////////////////////////////////

	/**
	 * Returns the column value for specified column index.
	 *
	 * @param columnIndex The of the column object to be returned
	 * @return The column value as an object
	 */
	public Object getColumnValue(int columnIndex)
	{
		switch (columnIndex)
		{
		case 0:
			return groupName;

		default:
			return null;
		}
	}

	/**
	 * @copy TreeTableNode.getNodeText()
	 */
	public String getNodeText()
	{
		return groupName;
	}

	/**
	 * @copy AbstractNode.isLeaf
	 */
	public boolean isLeaf()
	{
		return false;
	}

	/**
	 * Checks if this node represents the given property.
	 *
	 * @param propertyName Name of the property to check
	 * @nowarn
	 */
	public boolean representsProperty(String propertyName)
	{
		return groupName.equals(propertyName);
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the the name of the group.
	 * @nowarn
	 */
	public String getGroupName()
	{
		return groupName;
	}
}
