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

import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.activity.JavaActivityItem;
import org.openbp.core.model.item.activity.PlaceholderItem;
import org.openbp.core.model.item.visual.VisualItem;

/**
 * Process object.
 * A process contains a number of nodes and a number of links between the nodes.
 *
 * @author Heiko Erhardt
 */
public interface ProcessItem
	extends Item, ProcessObject, NodeProvider
{
	//////////////////////////////////////////////////
	// @@ Dependent object access
	//////////////////////////////////////////////////

	/**
	 * Gets the default initial node of this process.
	 *
	 * @return The node or null if there is no entry socket that has the default entry flag
	 * ({@link InitialNode#setDefaultEntry}) set
	 */
	public InitialNode getDefaultInitialNode();

	/**
	 * Gets the specified node socket.
	 *
	 * @param qualifiedSocketName Reference to the socket (starting with the node name)
	 * @return The socket or null if the node or the socket at the node could not be found
	 */
	public NodeSocket getSocketByName(String qualifiedSocketName);

	/**
	 * Gets the specified node parameter.
	 *
	 * @param qualifiedParamName Reference to the parameter (starting with the node name)
	 * @return The parameter or null if the node, the socket at the node or the parameter
	 * at the socket could not be found
	 */
	public Param getParamByName(String qualifiedParamName);

	/**
	 * Gets a process element by its process-relative name.
	 *
	 * @param qualifiedName Reference to a node, a socket or a parameter (starting with the node name)
	 * @return The processs object or null if the node, the socket at the node or the parameter
	 * at the socket could not be found
	 */
	public ProcessObject getProcessElementByName(String qualifiedName);

	//////////////////////////////////////////////////
	// @@ Node creation
	//////////////////////////////////////////////////

	/**
	 * Creates a new node according to the given node class.
	 * The method will assign a unique name to the node that is derived
	 * from the class name of the node class.<br>
	 * In order to create activity nodes, use the {@link #createItemNode} method.
	 *
	 * @param nodeClass The node class must be an instantiatable class that is a
	 * sub class of {@link Node}
	 * @return The new node or null if the node could not be created
	 */
	public Node createNode(Class nodeClass);

	/**
	 * Creates a new node that referes to an item, i\.e\. an activity or process item.
	 * The method will assign a unique name to the node that is derived
	 * from the class name of the node class. The name is based on the name of the supplied item.<br>
	 *
	 * @param item Item the node should refer to. The following items are supported:<br>
	 * {@link JavaActivityItem} : {@link ActivityNode}<br>
	 * {@link VisualItem} : {@link VisualNode}<br>
	 * {@link ProcessItem} : {@link SubprocessNode}<br>
	 * {@link PlaceholderItem} : {@link PlaceholderNode}
	 *
	 * @return The new node or null if the node could not be created
	 */
	public Node createItemNode(Item item);

	/**
	 * Creates a unique node name.
	 *
	 * @param newName Pattern for the new node name
	 * @return The given name if no such node exists, otherwise the name
	 * prepended with a running number (starting with 1)
	 */
	public String createUniqueNodeName(String newName);

	//////////////////////////////////////////////////
	// @@ Property access: Nodes
	//////////////////////////////////////////////////

	/**
	 * Gets a node by its name.
	 *
	 * @param name Name of the node
	 * @return The node or null if no such node exists
	 */
	public Node getNodeByName(String name);

	/**
	 * Gets the process nodes.
	 * @return An iterator of {@link Node} objects
	 */
	public Iterator getNodes();

	/**
	 * Adds a node to the process nodes.
	 * @param node The node to add
	 */
	public void addNode(Node node);

	/**
	 * Removes a node from the process nodes.
	 * @param node The node to remove
	 */
	public void removeNode(Node node);

	/**
	 * Clears the process nodes.
	 */
	public void clearNodes();

	/**
	 * Gets the process nodes.
	 * @return A list of {@link Node} objects
	 */
	public List getNodeList();

	//////////////////////////////////////////////////
	// @@ Property access: NodeGroups
	//////////////////////////////////////////////////

	/**
	 * Gets a node by its name.
	 *
	 * @param name Name of the node
	 * @return The node or null if no such node exists
	 */
	public NodeGroup getNodeGroupByName(String name);

	/**
	 * Gets the process nodes.
	 * @return An iterator of {@link NodeGroup} objects
	 */
	public Iterator getNodeGroups();

	/**
	 * Gets the number of nodes.
	 * @return The number of nodes in the collection
	 */
	public int getNumberOfNodeGroups();

	/**
	 * Adds a node to the process nodes.
	 * @param nodeGroup The node to add
	 */
	public void addNodeGroup(NodeGroup nodeGroup);

	/**
	 * Removes a node group from the process node groups.
	 *
	 * @param nodeGroup The node group to remove
	 */
	public void removeNodeGroup(NodeGroup nodeGroup);

	/**
	 * Clears the process node groups.
	 */
	public void clearNodeGroups();

	/**
	 * Gets the process node groups.
	 * @return A list of {@link NodeGroup} objects
	 */
	public List getNodeGroupList();

	//////////////////////////////////////////////////
	// @@ Property access: Control links
	//////////////////////////////////////////////////

	/**
	 * Gets a control link by its name.
	 *
	 * @param name Name of the control link
	 * @return The control link or null if no such control link exists
	 */
	public ControlLink getControlLinkByName(String name);

	/**
	 * Gets the control links.
	 * @return An iterator of {@link ControlLink} objects
	 */
	public Iterator getControlLinks();

	/**
	 * Checks if this item has outgoing control links.
	 * @nowarn
	 */
	public boolean hasControlLinks();

	/**
	 * Creates a new control and assigns a new name to it link.
	 * @return The new control
	 */
	public ControlLink createControlLink();

	/**
	 * Adds a control link.
	 * @param controlLink The control link to add
	 */
	public void addControlLink(ControlLink controlLink);

	/**
	 * Removes a control link.
	 * @param controlLink The control link to remove
	 */
	public void removeControlLink(ControlLink controlLink);

	/**
	 * Clears the control links.
	 */
	public void clearControlLinks();

	/**
	 * Gets the control links.
	 * @return A list of {@link ControlLink} objects
	 */
	public List getControlLinkList();

	//////////////////////////////////////////////////
	// @@ Property access: Data links
	//////////////////////////////////////////////////

	/**
	 * Gets a data link by its name.
	 *
	 * @param name Name of the data link
	 * @return The data link or null if no such data link exists
	 */
	public DataLink getDataLinkByName(String name);

	/**
	 * Gets the data links.
	 * @return An iterator of {@link DataLink} objects
	 */
	public Iterator getDataLinks();

	/**
	 * Creates a new data and assigns a new name to it link.
	 * @return The new data
	 */
	public DataLink createDataLink();

	/**
	 * Adds a data link.
	 * @param dataLink The data link to add
	 */
	public void addDataLink(DataLink dataLink);

	/**
	 * Removes a data link.
	 * @param dataLink The data link to remove
	 */
	public void removeDataLink(DataLink dataLink);

	/**
	 * Clears the data links.
	 */
	public void clearDataLinks();

	/**
	 * Gets the data links.
	 * @return A list of {@link DataLink} objects
	 */
	public List getDataLinkList();

	/**
	 * Gets the process variables of the process.
	 * @return An iterator of {@link ProcessVariable} objects
	 */
	public Iterator getProcessVariables();

	/**
	 * Gets a process variable by its name.
	 *
	 * @param name Name of the process variable
	 * @return The process variable or null if no such process variable exists
	 */
	public ProcessVariable getProcessVariableByName(String name);

	/**
	 * Adds a process variable.
	 * @param processVariable The process variable to add
	 */
	public void addProcessVariable(ProcessVariable processVariable);

	/**
	 * Removes a process variable.
	 *
	 * @param processVariable The process variable to remove
	 */
	public void removeProcessVariable(ProcessVariable processVariable);

	/**
	 * Clears the process variables of the process.
	 */
	public void clearProcessVariables();

	/**
	 * Gets the process variables of the process.
	 * @return A list of {@link ProcessVariable} objects
	 */
	public List getProcessVariableList();

	/**
	 * Sets the process variables of the process.
	 * @param processVariableList A list of {@link ProcessVariable} objects
	 */
	public void setProcessVariableList(List processVariableList);

	/**
	 * Creates a new text element and assigns a new name to it.
	 * @return The new control
	 */
	public TextElement createTextElement();

	/**
	 * Gets the text element list.
	 * @return An iterator of {@link TextElement} objects
	 */
	public Iterator getTextElements();

	/**
	 * Adds a text element.
	 * @param textElement The text element to add
	 */
	public void addTextElement(TextElement textElement);

	/**
	 * Removes a text element.
	 *
	 * @param textElement The text element to remove
	 */
	public void removeTextElement(TextElement textElement);

	/**
	 * Clears the text element list.
	 */
	public void clearTextElements();

	/**
	 * Gets the text element list.
	 * @return A list of {@link TextElement} objects
	 */
	public List getTextElementList();

	//////////////////////////////////////////////////
	// @@ Property access: Miscelleanous
	//////////////////////////////////////////////////

	/**
	 * Gets the process type.
	 * @return One of the constants defined in the {@link ProcessTypes} class
	 */
	public String getProcessType();

	/**
	 * Sets the process type.
	 * @param processType One of the constants defined in the {@link ProcessTypes} class
	 */
	public void setProcessType(String processType);

	/**
	 * Gets the name of the presentation skin.
	 * @nowarn
	 */
	public String getSkinName();

	/**
	 * Sets the name of the presentation skin.
	 * @nowarn
	 */
	public void setSkinName(String skinName);

	/**
	 * Gets the default process of the model.
	 * The default process will be chosen if the model is executed as a whole.
	 * @nowarn
	 */
	public boolean isDefaultProcess();

	/**
	 * Sets the default process of the model.
	 * The default process will be chosen if the model is executed as a whole.
	 * @nowarn
	 */
	public void setDefaultProcess(boolean defaultProcess);

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

	/**
	 * Gets the geometry information of the sub process node that calls this process (required by the Modeler).
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public String getNodeGeometry();

	/**
	 * Sets the geometry information of the sub process node that calls this process (required by the Modeler).
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public void setNodeGeometry(String nodeGeometry);
}
