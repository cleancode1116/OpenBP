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
package org.openbp.cockpit.plugins.finder.treemodel;

/**
 * Node for a group in the tree.
 *
 * @author Baumgartner Michael
 */
public class PropertyNode extends GenericNode
{
	//////////////////////////////////////////////////
	// @@ Member
	//////////////////////////////////////////////////

	/** The level of the group. */
	private int level = 0;

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Get the data of the group.
	 * @return the data
	 */
	public Object getPropertyData()
	{
		return nodeData;
	}

	/**
	 * Set the data of the group.
	 * @param propertyData The data
	 */
	public void setPropertyData(Object propertyData)
	{
		this.nodeData = propertyData;
	}

	/**
	 * Get the level of the group.
	 * @return the level
	 */
	public int getLevel()
	{
		return level;
	}

	/**
	 * Set the level of the group.
	 * @param level The level
	 */
	public void setLevel(int level)
	{
		this.level = level;
	}

	//////////////////////////////////////////////////
	// @@ Support
	//////////////////////////////////////////////////

	/**
	 * @copy org.openbp.cockpit.plugins.finder.treemodel.GenericNode.getPropertyNode
	 */
	public PropertyNode getPropertyNode(Object data)
	{
		for (int i = 0; i < getChildCount(); i++)
		{
			GenericNode node = (GenericNode) getChildAt(i);
			if (node instanceof PropertyNode)
			{
				PropertyNode prop = (PropertyNode) node;
				if (data.equals(prop.getPropertyData()))
					return prop;
			}
		}
		return null;
	}
}
