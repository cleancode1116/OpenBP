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
import java.util.StringTokenizer;

import org.openbp.common.CollectionUtil;
import org.openbp.common.ReflectException;
import org.openbp.common.ReflectUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.util.CopyUtil;
import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.common.util.SortingArrayList;
import org.openbp.common.util.iterator.EmptyIterator;
import org.openbp.core.CoreConstants;
import org.openbp.core.MimeTypes;
import org.openbp.core.model.Association;
import org.openbp.core.model.AssociationUtil;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemImpl;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.activity.JavaActivityItem;
import org.openbp.core.model.item.activity.PlaceholderItem;
import org.openbp.core.model.item.visual.VisualItem;

/**
 * Standard implementation of a process.
 *
 * @author Heiko Erhardt
 */
public class ProcessItemImpl extends ItemImpl
	implements ProcessItem
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Process nodes (contains {@link Node} objects, may be null) */
	private List nodeList;

	/** Process node groups (contains {@link NodeGroup} objects, may be null) */
	private List nodeGroupList;

	/** Control links (contains {@link ControlLink} objects, may be null) */
	private List controlLinkList;

	/** Data links (contains {@link DataLink} objects, may be null) */
	private List dataLinkList;

	/** Process variables of the process (contains {@link ProcessVariable} objects) */
	private List processVariableList;

	/** Text element list (contains {@link TextElement} objects) */
	private List textElementList;

	/** Default process flag */
	private boolean defaultProcess;

	/** Process type (may be null) */
	private String processType;

	/** Name of the presentation skin */
	private String skinName;

	/** Geometry information (required by the Modeler) */
	private String geometry;

	/** Geometry information of the sub process node that calls this process (required by the Modeler) */
	private String nodeGeometry;

	/** Arbitary object that can be used by an editor to create a pointer to a graphical representation */
	private transient Object representation;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ProcessItemImpl()
	{
		setItemType(ItemTypes.PROCESS);
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

		ProcessItemImpl src = (ProcessItemImpl) source;

		super.copyFrom(source, copyMode);

		defaultProcess = src.defaultProcess;
		processType = src.processType;
		skinName = src.skinName;
		geometry = src.geometry;
		nodeGeometry = src.nodeGeometry;

		if (copyMode == Copyable.COPY_FIRST_LEVEL || copyMode == Copyable.COPY_DEEP)
		{
			nodeList = null;

			// Add clones of all source nodes to this process
			if (src.nodeList != null)
			{
				for (int i = 0; i < src.nodeList.size(); ++i)
				{
					Node node = (Node) src.nodeList.get(i);
					if (copyMode == Copyable.COPY_DEEP)
						node = (Node) node.clone();
					addNode(node);
				}
			}

			// Create deep clones of collection members
			controlLinkList = (List) CopyUtil.copyCollection(src.controlLinkList, copyMode == Copyable.COPY_DEEP ? CopyUtil.CLONE_VALUES : CopyUtil.CLONE_NONE);
			dataLinkList = (List) CopyUtil.copyCollection(src.dataLinkList, copyMode == Copyable.COPY_DEEP ? CopyUtil.CLONE_VALUES : CopyUtil.CLONE_NONE);
			processVariableList = (List) CopyUtil.copyCollection(src.processVariableList, copyMode == Copyable.COPY_DEEP ? CopyUtil.CLONE_VALUES : CopyUtil.CLONE_NONE);
			textElementList = (List) CopyUtil.copyCollection(src.textElementList, copyMode == Copyable.COPY_DEEP ? CopyUtil.CLONE_VALUES : CopyUtil.CLONE_NONE);
		}
		else
		{
			// Shallow clone
			nodeList = src.nodeList;
			controlLinkList = src.controlLinkList;
			dataLinkList = src.dataLinkList;
			processVariableList = src.processVariableList;
			textElementList = src.textElementList;
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ NodeProvider implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Generates a sub process node that calls this process.
	 *
	 * @return A {@link SubprocessNode}
	 * @copy NodeProvider.toNode
	 */
	public Node toNode(ProcessItem process, int syncFlags)
	{
		SubprocessNode result = new SubprocessNodeImpl();

		result.setProcess(process);
		result.copyFromItem(this, syncFlags);

		return result;
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
		// Return myself
		return this;
	}

	/**
	 * Gets the partially qualified name of the object relative to the process.
	 * @return Always null since the process is the top-level element of the process object hierarchy
	 * (just doing Stephan a favor)
	 */
	public String getProcessRelativeName()
	{
		return null;
	}

	/**
	 * Gets the children (direct subordinates) of this object.
	 * The method returns the parameters of the socket.
	 *
	 * @return The list of objects or null if the object does not have children
	 */
	public List getChildren()
	{
		return nodeList;
	}

	//////////////////////////////////////////////////
	// @@ Pre save/post load processing and validation
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.maintainReferences
	 */
	public void maintainReferences(int flag)
	{
		super.maintainReferences(flag);

		if ((flag & UNLINK_FROM_REPRESENTATION) != 0)
		{
			setRepresentation(null);
		}

		// Note that the order in which the references are resolved should not be changed!

		if (nodeList != null)
		{
			int n = nodeList.size();
			for (int i = 0; i < n; ++i)
			{
				Node node = (Node) nodeList.get(i);

				// Establish back reference
				node.setProcess(this);

				node.maintainReferences(flag);
			}
		}

		if (nodeGroupList != null)
		{
			int n = nodeGroupList.size();
			for (int i = 0; i < n; ++i)
			{
				NodeGroup nodeGroup = (NodeGroup) nodeGroupList.get(i);

				// Establish back reference
				nodeGroup.setProcess(this);

				nodeGroup.maintainReferences(flag);
			}
		}

		if (processVariableList != null)
		{
			int n = processVariableList.size();
			for (int i = 0; i < n; ++i)
			{
				ProcessVariable param = (ProcessVariable) processVariableList.get(i);

				// Establish back reference
				param.setProcess(this);

				param.maintainReferences(flag);
			}
		}

		if (textElementList != null)
		{
			int n = textElementList.size();
			for (int i = 0; i < n; ++i)
			{
				TextElement textElement = (TextElement) textElementList.get(i);

				// Establish back reference
				textElement.setProcess(this);

				textElement.maintainReferences(flag);
			}
		}

		if (controlLinkList != null)
		{
			// Do not optimize, array size may change if invalid links are removed by maintainReferences()
			for (int i = 0; i < controlLinkList.size(); ++i)
			{
				ControlLink link = (ControlLink) controlLinkList.get(i);

				if ((flag & SYNC_LOCAL_REFNAMES) != 0)
				{
					if (link.getSourceSocket() == null || link.getSourceSocket().getProcess() != this || link.getTargetSocket() == null || link.getTargetSocket().getProcess() != this)
					{
						// The source/target of this link doesn't exist any more or doesn't belong to this process.
						// This should normally not happen; However, as a precaution against modeler bugs, remove the invalid link.
						removeControlLink(link);
						--i;
						continue;
					}
				}

				// Establish back reference
				link.setProcess(this);

				link.maintainReferences(flag);
			}
		}

		if (dataLinkList != null)
		{
			// Do not optimize, array size may change if invalid links are removed by maintainReferences()
			for (int i = 0; i < dataLinkList.size(); ++i)
			{
				DataLink link = (DataLink) dataLinkList.get(i);

				if ((flag & SYNC_LOCAL_REFNAMES) != 0)
				{
					if (link.getSourceParam() == null || link.getSourceParam().getProcess() != this || link.getTargetParam() == null || link.getTargetParam().getProcess() != this)
					{
						// The source/target of this link doesn't exist any more or doesn't belong to this process.
						// This should normally not happen; However, as a precaution against modeler bugs, remove the invalid link.
						removeDataLink(link);
						--i;
						continue;
					}
				}

				// Establish back reference
				link.setProcess(this);

				link.maintainReferences(flag);
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Associations
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.getAssociations
	 */
	public List getAssociations()
	{
		List associations = null;

		// Add 'this' as process item association in order to start the Modeler
		associations = AssociationUtil.addAssociation(associations, -1, "Process", this, this, new String [] { MimeTypes.PROCESS_ITEM }, Association.PRIMARY, "");

		associations = AssociationUtil.addAssociations(associations, -1, super.getAssociations());

		return associations;
	}

	//////////////////////////////////////////////////
	// @@ Dependent object access
	//////////////////////////////////////////////////

	/**
	 * Gets the default initial node of this process.
	 *
	 * @return The node or null if there is no entry socket that has the default entry flag
	 * ({@link InitialNode#setDefaultEntry}) set
	 */
	public InitialNode getDefaultInitialNode()
	{
		if (nodeList != null)
		{
			int n = nodeList.size();
			for (int i = 0; i < n; ++i)
			{
				Node node = (Node) nodeList.get(i);

				if (node instanceof InitialNode)
				{
					InitialNode initialNode = (InitialNode) node;
					if (initialNode.isDefaultEntry())
						return initialNode;
				}
			}
		}

		return null;
	}

	/**
	 * Gets the specified node socket.
	 *
	 * @param qualifiedSocketName Reference to the socket (starting with the node name)
	 * @return The socket or null if the node or the socket at the node could not be found
	 */
	public NodeSocket getSocketByName(String qualifiedSocketName)
	{
		StringTokenizer st = new StringTokenizer(qualifiedSocketName, ModelQualifier.OBJECT_DELIMITER);

		String nodeName = st.nextToken();
		if (!st.hasMoreTokens())
			return null;
		String socketName = st.nextToken();

		Node node = getNodeByName(nodeName);
		if (node == null)
			return null;

		return node.getSocketByName(socketName);
	}

	/**
	 * Gets the specified node parameter.
	 *
	 * @param qualifiedParamName Reference to the parameter (starting with the node name)
	 * @return The parameter or null if the node, the socket at the node or the parameter
	 * at the socket could not be found
	 */
	public Param getParamByName(String qualifiedParamName)
	{
		if (qualifiedParamName == null)
		{
			return null;
		}

		if (qualifiedParamName.startsWith(CoreConstants.PROCESS_VARIABLE_INDICATOR))
		{
			String name = qualifiedParamName.substring(1);
			return getProcessVariableByName(name);
		}

		StringTokenizer st = new StringTokenizer(qualifiedParamName, ModelQualifier.OBJECT_DELIMITER);

		String nodeName = st.nextToken();
		if (!st.hasMoreTokens())
			return null;
		String socketName = st.nextToken();
		if (!st.hasMoreTokens())
			return null;
		String paramName = st.nextToken();

		Node node = getNodeByName(nodeName);
		if (node == null)
			return null;

		NodeSocket socket = node.getSocketByName(socketName);
		if (socket == null)
			return null;

		return socket.getParamByName(paramName);
	}

	/**
	 * Gets a process element by its process-relative name.
	 *
	 * @param qualifiedName Reference to a node, a socket or a parameter (starting with the node name)
	 * @return The processs object or null if the node, the socket at the node or the parameter
	 * at the socket could not be found
	 */
	public ProcessObject getProcessElementByName(String qualifiedName)
	{
		if (qualifiedName == null)
			return null;

		if (qualifiedName.startsWith(CoreConstants.PROCESS_VARIABLE_INDICATOR))
		{
			String name = qualifiedName.substring(1);
			return getProcessVariableByName(name);
		}

		StringTokenizer st = new StringTokenizer(qualifiedName, ModelQualifier.OBJECT_DELIMITER);

		String nodeName = st.nextToken();

		// Try nodes first
		Node node = getNodeByName(nodeName);
		if (node != null)
		{
			if (!st.hasMoreTokens())
				return node;

			String socketName = st.nextToken();
			NodeSocket socket = node.getSocketByName(socketName);
			if (socket == null)
				return null;

			if (!st.hasMoreTokens())
				return socket;

			String paramName = st.nextToken();
			return socket.getParamByName(paramName);
		}

		ControlLink controlLink = getControlLinkByName(nodeName);
		if (controlLink != null)
			return controlLink;

		DataLink dataLink = getDataLinkByName(nodeName);
		if (dataLink != null)
			return dataLink;

		return null;
	}

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
	public Node createNode(Class nodeClass)
	{
		Node node = createNodeObject(nodeClass);
		if (node == null)
			return null;

		// Determine the base name of the new node.

		// Cut package name
		String className = nodeClass.getName();
		int index = className.lastIndexOf('.');
		className = className.substring(index + 1);

		// Cut "Node..."
		index = className.indexOf("Node");
		if (index > 0)
			className = className.substring(0, index);

		// Get a unique node name based on the class name we have
		String newName = createUniqueNodeName(className);
		node.setName(newName);

		if (node instanceof InitialNode)
		{
			// For entry and final nodes, we create the only socket
			NodeSocket socket = ProcessUtil.createNodeSocket(CoreConstants.SOCKET_OUT, false);
			((InitialNode) node).setSocket(socket);
		}
		else if (node instanceof FinalNode)
		{
			// For entry and final nodes, we create the only socket
			NodeSocket socket = ProcessUtil.createNodeSocket(CoreConstants.SOCKET_IN, true);
			((FinalNode) node).setSocket(socket);
		}

		return node;
	}

	/**
	 * Creates a new node that refers to an item, i\.e\. an activity or process item.
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
	public Node createItemNode(Item item)
	{
		Class nodeClass = null;
		if (item instanceof JavaActivityItem)
		{
			nodeClass = ActivityNodeImpl.class;
		}
		else if (item instanceof VisualItem)
		{
			nodeClass = VisualNodeImpl.class;
		}
		else if (item instanceof ProcessItem)
		{
			nodeClass = SubprocessNodeImpl.class;
		}
		else if (item instanceof PlaceholderItem)
		{
			nodeClass = PlaceholderNodeImpl.class;
		}
		else
		{
			getModelMgr().getMsgContainer().addMsg(this, "ProcessImpl.createItemNode: Invalid component $0.", new Object [] { item });
			return null;
		}

		Node node = createNodeObject(nodeClass);
		if (node == null)
			return null;

		if (node instanceof ItemProvider)
		{
			((ItemProvider) node).copyFromItem(item, 0);
		}

		// Get a unique node name based on the class name we have
		String newName = NamedObjectCollectionUtil.createUniqueId(nodeList, item.getName());
		node.setName(newName);

		return node;
	}

	/**
	 * Creates a node object according to the specified node class
	 * @param nodeClass The node class must be an instantiatable class that is a
	 * sub class of {@link Node}
	 * @return The new node or null if the node could not be created
	 */
	protected Node createNodeObject(Class nodeClass)
	{
		try
		{
			return (Node) ReflectUtil.instantiate(nodeClass, Node.class, "node");
		}
		catch (ReflectException e)
		{
			getModelMgr().getMsgContainer().addMsg(this, "Error instantiating node class.", new Object [] { e });
		}
		return null;
	}

	/**
	 * Creates a unique node name.
	 *
	 * @param newName Pattern for the new node name
	 * @return The given name if no such node exists, otherwise the name
	 * prepended with a running number (starting with 1)
	 */
	public String createUniqueNodeName(String newName)
	{
		if (NamedObjectCollectionUtil.getByName(nodeList, newName) != null)
		{
			// Node with this name exists already
			newName = NamedObjectCollectionUtil.createUniqueId(nodeList, newName);
		}
		return newName;
	}

	//////////////////////////////////////////////////
	// @@ Property access: Nodes
	//////////////////////////////////////////////////

	/**
	 * Gets a node by its name.
	 *
	 * @param name Name of the node
	 * @return The node or null if no such node exists
	 */
	public Node getNodeByName(String name)
	{
		if (nodeList == null)
			return null;
		return (Node) NamedObjectCollectionUtil.getByName(nodeList, name);
	}

	/**
	 * Gets the process nodes.
	 * @return An iterator of {@link Node} objects
	 */
	public Iterator getNodes()
	{
		if (nodeList == null)
			return EmptyIterator.getInstance();
		return nodeList.iterator();
	}

	/**
	 * Adds a node to the process nodes.
	 * @param node The node to add
	 */
	public void addNode(Node node)
	{
		if (nodeList == null)
			nodeList = new SortingArrayList();
		nodeList.add(node);

		// Set the node process to this
		node.setProcess(this);
	}

	/**
	 * Removes a node from the process nodes.
	 * Also clears the control and data links from this node.
	 *
	 * @param node The node to remove
	 */
	public void removeNode(Node node)
	{
		CollectionUtil.removeReference(nodeList, node);

		// Remove all control links from/to this node
		for (Iterator it = getControlLinks(); it.hasNext();)
		{
			ControlLink controlLink = (ControlLink) it.next();
			NodeSocket sourceSocket = controlLink.getSourceSocket();
			NodeSocket targetSocket = controlLink.getTargetSocket();
			if ((sourceSocket != null && sourceSocket.getNode() == node) || (targetSocket != null && targetSocket.getNode() == node))
			{
				controlLink.unlink();
				it.remove();
			}
		}

		// Remove all data links from/to this node
		for (Iterator it = getDataLinks(); it.hasNext();)
		{
			DataLink dataLink = (DataLink) it.next();

			Param sourceParam = dataLink.getSourceParam();
			Param targetParam = dataLink.getTargetParam();
			if ((sourceParam instanceof NodeParam && ((NodeParam) sourceParam).getSocket().getNode() == node) || (targetParam instanceof NodeParam && ((NodeParam) targetParam).getSocket().getNode() == node))
			{
				dataLink.unlink();
				it.remove();
			}
		}
	}

	/**
	 * Clears the process nodes.
	 * Make sure to remove all control links and data links prior to calling this method.
	 */
	public void clearNodes()
	{
		nodeList = null;
	}

	/**
	 * Gets the process nodes.
	 * @return A list of {@link Node} objects
	 */
	public List getNodeList()
	{
		return nodeList;
	}

	//////////////////////////////////////////////////
	// @@ Property access: Node groups
	//////////////////////////////////////////////////

	/**
	 * Gets a node by its name.
	 *
	 * @param name Name of the node
	 * @return The node or null if no such node exists
	 */
	public NodeGroup getNodeGroupByName(String name)
	{
		if (nodeGroupList == null)
			return null;
		return (NodeGroup) NamedObjectCollectionUtil.getByName(nodeGroupList, name);
	}

	/**
	 * Gets the process nodes.
	 * @return An iterator of {@link NodeGroup} objects
	 */
	public Iterator getNodeGroups()
	{
		if (nodeGroupList == null)
			return EmptyIterator.getInstance();
		return nodeGroupList.iterator();
	}

	/**
	 * Gets the number of nodes.
	 * @return The number of nodes in the collection
	 */
	public int getNumberOfNodeGroups()
	{
		return nodeGroupList != null ? nodeGroupList.size() : 0;
	}

	/**
	 * Adds a node to the process nodes.
	 * @param nodeGroup The node to add
	 */
	public void addNodeGroup(NodeGroup nodeGroup)
	{
		if (nodeGroupList == null)
			nodeGroupList = new SortingArrayList();
		nodeGroupList.add(nodeGroup);

		// Set the node process to this
		nodeGroup.setProcess(this);
	}

	/**
	 * Removes a node group from the process node groups.
	 *
	 * @param nodeGroup The node group to remove
	 */
	public void removeNodeGroup(NodeGroup nodeGroup)
	{
		CollectionUtil.removeReference(nodeGroupList, nodeGroup);
	}

	/**
	 * Clears the process node groups.
	 */
	public void clearNodeGroups()
	{
		nodeGroupList = null;
	}

	/**
	 * Gets the process node groups.
	 * @return A list of {@link NodeGroup} objects
	 */
	public List getNodeGroupList()
	{
		return nodeGroupList;
	}

	//////////////////////////////////////////////////
	// @@ Property access: Control links
	//////////////////////////////////////////////////

	/**
	 * Gets a control link by its name.
	 *
	 * @param name Name of the control link
	 * @return The control link or null if no such control link exists
	 */
	public ControlLink getControlLinkByName(String name)
	{
		if (controlLinkList == null)
			return null;
		return (ControlLink) NamedObjectCollectionUtil.getByName(controlLinkList, name);
	}

	/**
	 * Gets the control links.
	 * @return An iterator of {@link ControlLink} objects
	 */
	public Iterator getControlLinks()
	{
		if (controlLinkList == null)
			return EmptyIterator.getInstance();
		return controlLinkList.iterator();
	}

	/**
	 * Checks if there are any control links attached to the socket.
	 * @nowarn
	 */
	public boolean hasControlLinks()
	{
		return controlLinkList != null && controlLinkList.size() > 0;
	}

	/**
	 * Creates a new control and assigns a new name to it link.
	 * @return The new control
	 */
	public ControlLink createControlLink()
	{
		ControlLink controlLink = new ControlLinkImpl();
		String name = NamedObjectCollectionUtil.createUniqueId(controlLinkList, "ControlLink");
		controlLink.setName(name);
		return controlLink;
	}

	/**
	 * Adds a control link.
	 * @param controlLink The control link to add
	 */
	public void addControlLink(ControlLink controlLink)
	{
		if (controlLinkList == null)
			controlLinkList = new SortingArrayList();
		controlLinkList.add(controlLink);

		controlLink.setProcess(this);
	}

	/**
	 * Removes a control link.
	 * The method will not unlink the control link from its nodes.
	 * Use the {@link ControlLink#unlink} method for this before removing the link.
	 *
	 * @param controlLink The control link to remove
	 */
	public void removeControlLink(ControlLink controlLink)
	{
		controlLink.unlink();
		CollectionUtil.removeReference(controlLinkList, controlLink);
	}

	/**
	 * Clears the control links.
	 *
	 * The method will not unlink the control links from their nodes.
	 * Iterate the control link list and
	 * use the {@link ControlLink#unlink} method for this before removing the link.
	 */
	public void clearControlLinks()
	{
		if (controlLinkList != null)
		{
			int n = controlLinkList.size();
			for (int i = 0; i < n; ++i)
			{
				ControlLink link = (ControlLink) controlLinkList.get(i);
				link.unlink();
			}
		}

		controlLinkList = null;
	}

	/**
	 * Gets the control links.
	 * @return A list of {@link ControlLink} objects
	 */
	public List getControlLinkList()
	{
		return controlLinkList;
	}

	//////////////////////////////////////////////////
	// @@ Property access: Data links
	//////////////////////////////////////////////////

	/**
	 * Gets a data link by its name.
	 *
	 * @param name Name of the data link
	 * @return The data link or null if no such data link exists
	 */
	public DataLink getDataLinkByName(String name)
	{
		if (dataLinkList == null)
			return null;
		return (DataLink) NamedObjectCollectionUtil.getByName(dataLinkList, name);
	}

	/**
	 * Gets the data links.
	 * @return An iterator of {@link DataLink} objects
	 */
	public Iterator getDataLinks()
	{
		if (dataLinkList == null)
			return EmptyIterator.getInstance();
		return dataLinkList.iterator();
	}

	/**
	 * Creates a new data and assigns a new name to it link.
	 * @return The new data
	 */
	public DataLink createDataLink()
	{
		DataLink dataLink = new DataLinkImpl();
		String name = NamedObjectCollectionUtil.createUniqueId(dataLinkList, "DataLink");
		dataLink.setName(name);
		return dataLink;
	}

	/**
	 * Adds a data link.
	 * @param dataLink The data link to add
	 */
	public void addDataLink(DataLink dataLink)
	{
		if (dataLinkList == null)
			dataLinkList = new SortingArrayList();
		dataLinkList.add(dataLink);

		dataLink.setProcess(this);
	}

	/**
	 * Removes a data link.
	 * The method will not unlink the data link from its nodes.
	 * Use the {@link DataLink#unlink} method for this before removing the link.
	 *
	 * @param dataLink The data link to remove
	 */
	public void removeDataLink(DataLink dataLink)
	{
		dataLink.unlink();
		CollectionUtil.removeReference(dataLinkList, dataLink);
	}

	/**
	 * Clears the data links.
	 *
	 * The method will not unlink the data links from their nodes.
	 * Iterate the data link list and
	 * use the {@link DataLink#unlink} method for this before removing the link.
	 */
	public void clearDataLinks()
	{
		if (dataLinkList != null)
		{
			int n = dataLinkList.size();
			for (int i = 0; i < n; ++i)
			{
				DataLink link = (DataLink) dataLinkList.get(i);
				link.unlink();
			}
		}

		dataLinkList = null;
	}

	/**
	 * Gets the data links.
	 * @return A list of {@link DataLink} objects
	 */
	public List getDataLinkList()
	{
		return dataLinkList;
	}

	/**
	 * Gets the process variables of the process.
	 * @return An iterator of {@link ProcessVariable} objects
	 */
	public Iterator getProcessVariables()
	{
		if (processVariableList == null)
			return EmptyIterator.getInstance();
		return processVariableList.iterator();
	}

	/**
	 * Gets a process variable by its name.
	 *
	 * @param name Name of the process variable
	 * @return The process variable or null if no such process variable exists
	 */
	public ProcessVariable getProcessVariableByName(String name)
	{
		return processVariableList != null ? (ProcessVariable) NamedObjectCollectionUtil.getByName(processVariableList, name) : null;
	}

	/**
	 * Adds a process variable.
	 * @param processVariable The process variable to add
	 */
	public void addProcessVariable(ProcessVariable processVariable)
	{
		if (processVariableList == null)
			processVariableList = new SortingArrayList();
		processVariableList.add(processVariable);

		processVariable.setProcess(this);
	}

	/**
	 * Removes a process variable.
	 *
	 * @param processVariable The process variable to remove
	 */
	public void removeProcessVariable(ProcessVariable processVariable)
	{
		CollectionUtil.removeReference(processVariableList, processVariable);
	}

	/**
	 * Clears the process variables of the process.
	 */
	public void clearProcessVariables()
	{
		processVariableList = null;
	}

	/**
	 * Gets the process variables of the process.
	 * @return A list of {@link ProcessVariable} objects
	 */
	public List getProcessVariableList()
	{
		return processVariableList;
	}

	/**
	 * Sets the process variables of the process.
	 * @param processVariableList A list of {@link ProcessVariable} objects
	 */
	public void setProcessVariableList(List processVariableList)
	{
		this.processVariableList = processVariableList;

		if (processVariableList != null)
		{
			int n = processVariableList.size();
			for (int i = 0; i < n; ++i)
			{
				ProcessVariable param = (ProcessVariable) processVariableList.get(i);
				param.setProcess(this);
			}
		}
	}

	/**
	 * Creates a new text element and assigns a new name to it.
	 * @return The new control
	 */
	public TextElement createTextElement()
	{
		TextElement textElement = new TextElementImpl();
		String name = NamedObjectCollectionUtil.createUniqueId(textElementList, "Text");
		textElement.setName(name);
		return textElement;
	}

	/**
	 * Gets the text element list.
	 * @return An iterator of {@link TextElement} objects
	 */
	public Iterator getTextElements()
	{
		if (textElementList == null)
			return EmptyIterator.getInstance();
		return textElementList.iterator();
	}

	/**
	 * Adds a text element.
	 * @param textElement The text element to add
	 */
	public void addTextElement(TextElement textElement)
	{
		if (textElementList == null)
			textElementList = new SortingArrayList();
		textElementList.add(textElement);

		textElement.setProcess(this);
	}

	/**
	 * Removes a text element.
	 *
	 * @param textElement The text element to remove
	 */
	public void removeTextElement(TextElement textElement)
	{
		CollectionUtil.removeReference(textElementList, textElement);
	}

	/**
	 * Clears the text element list.
	 */
	public void clearTextElements()
	{
		textElementList = null;
	}

	/**
	 * Gets the text element list.
	 * @return A list of {@link TextElement} objects
	 */
	public List getTextElementList()
	{
		return textElementList;
	}

	//////////////////////////////////////////////////
	// @@ Property access: Miscelleanous
	//////////////////////////////////////////////////

	/**
	 * Gets the process type.
	 * @return One of the constants defined in the {@link ProcessTypes} class
	 */
	public String getProcessType()
	{
		return processType;
	}

	/**
	 * Sets the process type.
	 * @param processType One of the constants defined in the {@link ProcessTypes} class
	 */
	public void setProcessType(String processType)
	{
		this.processType = processType;
	}

	/**
	 * Gets the name of the presentation skin.
	 * @nowarn
	 */
	public String getSkinName()
	{
		return skinName;
	}

	/**
	 * Sets the name of the presentation skin.
	 * @nowarn
	 */
	public void setSkinName(String skinName)
	{
		this.skinName = skinName;
	}

	/**
	 * Determines if the default process flag is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasDefaultProcess()
	{
		return defaultProcess;
	}

	/**
	 * Gets the default process flag.
	 * The default process will be chosen if the model is executed as a whole.
	 * @nowarn
	 */
	public boolean isDefaultProcess()
	{
		return defaultProcess;
	}

	/**
	 * Sets the default process flag.
	 * The default process will be chosen if the model is executed as a whole.
	 * @nowarn
	 */
	public void setDefaultProcess(boolean defaultProcess)
	{
		this.defaultProcess = defaultProcess;
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

	/**
	 * Gets the geometry information of the sub process node that calls this process (required by the Modeler).
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public String getNodeGeometry()
	{
		return nodeGeometry;
	}

	/**
	 * Sets the geometry information of the sub process node that calls this process (required by the Modeler).
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public void setNodeGeometry(String nodeGeometry)
	{
		this.nodeGeometry = nodeGeometry;
	}

	/**
	 * Returns the current representation of this ProcessObject or null
	 * if not set.
	 *
	 * @return The actual representation or null
	 */
	public Object getRepresentation()
	{
		return representation;
	}

	/**
	 * Sets the representation object of this ProcessObject. This can be used to store a pointer
	 * to a graphical representation in an editor or such.
	 *
	 * @param representation the new representation or null to clear
	 */
	public void setRepresentation(Object representation)
	{
		this.representation = representation;
	}
}
