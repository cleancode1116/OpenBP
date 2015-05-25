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
package org.openbp.core.model.item.process;

import java.util.Iterator;
import java.util.List;

import org.openbp.common.generic.Copyable;
import org.openbp.common.util.CopyUtil;
import org.openbp.common.util.SortingArrayList;
import org.openbp.common.util.iterator.EmptyIterator;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;

/**
 * Process node group.
 * A node group consists of an arbitary number of nodes of a process.
 *
 * @author Stephan Moritz
 */
public class NodeGroupImpl extends ProcessObjectImpl
	implements NodeGroup
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Geometry information (required by the Modeler) */
	private String geometry;

	/** Names of nodes contained in this node group (contains String objects) */
	private List nodeNameList;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Process the node belongs to (may not be null) */
	private transient ProcessItem process;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public NodeGroupImpl()
	{
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

		NodeGroupImpl src = (NodeGroupImpl) source;

		geometry = src.geometry;

		process = src.process;

		if (copyMode == Copyable.COPY_FIRST_LEVEL || copyMode == Copyable.COPY_DEEP)
		{
			// Create copy of the string collection
			nodeNameList = (List) CopyUtil.copyCollection(src.nodeNameList, copyMode == Copyable.COPY_DEEP ? CopyUtil.CLONE_VALUES : CopyUtil.CLONE_NONE);
		}
		else
		{
			// Shallow clone
			nodeNameList = src.nodeNameList;
		}
	}

	/**
	 * Gets the reference to the object.
	 * @return The qualified name
	 */
	public ModelQualifier getQualifier()
	{
		return new ModelQualifier(getProcess(), getName());
	}

	//////////////////////////////////////////////////
	// @@ ProcessObject implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the process the object belongs to.
	 * @nowarn
	 */
	public ProcessItem getProcess()
	{
		return process;
	}

	/**
	 * Sets the process the object belongs to.
	 * @nowarn
	 */
	public void setProcess(ProcessItem process)
	{
		this.process = process;
	}

	/**
	 * Gets the partially qualified name of the object relative to the process.
	 * @nowarn
	 */
	public String getProcessRelativeName()
	{
		return getName();
	}

	/**
	 * Gets the container object (i. e. the parent) of this object.
	 *
	 * @return The container object or null if this object doesn't have a container.
	 * If the parent of this object references only a single object of this type,
	 * the method returns null.
	 */
	public ModelObject getContainer()
	{
		return process;
	}

	/**
	 * Gets an iterator of the children of the container this object belongs to.
	 * This can be used to check on name clashes between objects of this type.
	 * By default, the method returns null.
	 *
	 * @return The iterator if this object is part of a collection or a map.
	 * If the parent of this object references only a single object of this type,
	 * the method returns null.
	 */
	public Iterator getContainerIterator()
	{
		return process.getNodeGroups();
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the names of nodes contained in this node group.
	 * @return An iterator of String objects
	 */
	public Iterator getNodeNames()
	{
		if (nodeNameList == null)
			return EmptyIterator.getInstance();
		return nodeNameList.iterator();
	}

	/**
	 * Adds a node name.
	 * @param nodeName The node name to add
	 */
	public void addNodeName(String nodeName)
	{
		if (nodeNameList == null)
			nodeNameList = new SortingArrayList();
		nodeNameList.add(nodeName);
	}

	/**
	 * Clears the names of nodes contained in this node group.
	 */
	public void clearNodeNames()
	{
		nodeNameList = null;
	}

	/**
	 * Gets the names of nodes contained in this node group.
	 * @return A list of String objects
	 */
	public List getNodeNameList()
	{
		return nodeNameList;
	}

	/**
	 * Sets the names of nodes contained in this node group.
	 * @param nodeNameList A list of String objects
	 */
	public void setNodeNameList(List nodeNameList)
	{
		this.nodeNameList = nodeNameList;
	}

	/**
	 * Adds a node to the node group.
	 *
	 * @param node Node to add
	 */
	public void addNode(Node node)
	{
		addNodeName(node.getProcessRelativeName());
	}

	/**
	 * Gets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public String getGeometry()
	{
		return geometry;
	}

	/**
	 * Sets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public void setGeometry(String geometry)
	{
		this.geometry = geometry;
	}
}
