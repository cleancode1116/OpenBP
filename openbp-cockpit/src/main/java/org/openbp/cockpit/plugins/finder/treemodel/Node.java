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
public class Node extends DefaultTreeNode
{
	//////////////////////////////////////////////////
	// @@ Member
	//////////////////////////////////////////////////

	/** Mapper of the node. */
	protected NodeMapper nodeMapper;

	/** Table with properties. */
	private Hashtable properties = new Hashtable();

	protected Object nodeData;

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Set the node mapper
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
	 * @see org.openbp.swing.components.tree.DefaultTreeNode#addChild(DefaultTreeNode)
	 */
	public void addChild(Node child, Strategy strategy)
	{
		super.addChild(child);
		Comparator comp = strategy.getTreeComparator();
		if (comp != null)
			Collections.sort(getChildList(), comp);
	}

	/**
	 * Mapped to node mapper.
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return nodeMapper.getDisplayString(nodeData);
	}
}
