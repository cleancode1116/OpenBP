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

import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import org.openbp.swing.components.tree.DefaultTreeNode;

/**
 * Default node in the generic model.
 *
 * @author Baumgartner Michael
 */
public class GenericNode extends DefaultTreeNode
{
	//////////////////////////////////////////////////
	// @@ Member
	//////////////////////////////////////////////////

	/** Mapper of the node. */
	protected NodeMapper nodeMapper;

	/** Table with properties to store additional information. */
	private Hashtable properties = new Hashtable();

	/** Data object that is represented by the node. */
	protected Object nodeData;

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Set the node mapper. The mapper must be set in order
	 * to display a string for the node.
	 * @param nodeMapper The node mapper
	 */
	public void setNodeMapper(NodeMapper nodeMapper)
	{
		this.nodeMapper = nodeMapper;
	}

	/**
	 * Get the node mapper.
	 * @return the node mapper
	 */
	public NodeMapper getNodeMapper()
	{
		return nodeMapper;
	}

	//////////////////////////////////////////////////
	// @@ Property handling
	//////////////////////////////////////////////////

	/**
	 * Add a property to the node
	 * @param key The key of the property
	 * @param value The value
	 */
	public void addProperty(String key, Object value)
	{
		properties.put(key, value);
	}

	/**
	 * Get the property of the node
	 * @param key The key of the property
	 * @return The value or null if property does not exist
	 */
	public Object getProperty(String key)
	{
		return properties.get(key);
	}

	/**
	 * Get the group with a special data object
	 * @param propertyData The special data object
	 * @return The found group or null
	 */
	public PropertyNode getPropertyNode(Object propertyData)
	{
		return null;
	}

	/**
	 * Add a new child to the node. The children are order by
	 * the comparator of the strategy
	 * @param child The child to add
	 * @param strategy The strategy used by the model
	 */
	public void addChild(GenericNode child, Strategy strategy)
	{
		super.addChild(child);
		Comparator comp = strategy.getTreeComparator();
		if (comp != null)
			Collections.sort(getChildList(), comp);
	}

	/**
	 * Map the call to the node mapper.
	 * @return display test of the node
	 */
	public String toString()
	{
		if (nodeMapper == null)
			return "no node mapper";
		return nodeMapper.getDisplayString(nodeData);
	}
}
