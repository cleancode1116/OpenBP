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

/**
 * Process node group.
 * A node group consists of an arbitary number of nodes of a process.
 *
 * @author Stephan Moritz
 */
public interface NodeGroup
	extends ProcessObject
{
	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the names of nodes contained in this node group.
	 * @return An iterator of String objects
	 */
	public Iterator getNodeNames();

	/**
	 * Adds a node name.
	 * @param nodeName The node name to add
	 */
	public void addNodeName(String nodeName);

	/**
	 * Clears the names of nodes contained in this node group.
	 */
	public void clearNodeNames();

	/**
	 * Gets the names of nodes contained in this node group.
	 * @return A list of String objects
	 */
	public List getNodeNameList();

	/**
	 * Sets the names of nodes contained in this node group.
	 * @param nodeNameList A list of String objects
	 */
	public void setNodeNameList(List nodeNameList);

	/**
	 * Adds a node to the node group.
	 *
	 * @param node Node to add
	 */
	public void addNode(Node node);

	/**
	 * Gets the process the node group belongs to.
	 * @nowarn
	 */
	public ProcessItem getProcess();

	/**
	 * Sets the process the node group belongs to.
	 * @nowarn
	 */
	public void setProcess(ProcessItem process);

	/**
	 * Gets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public String getGeometry();

	/**
	 * Sets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public void setGeometry(String geometry);
}
