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

import org.openbp.common.CommonRegistry;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.string.StringUtil;
import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.core.CoreConstants;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.type.DataTypeItem;
import org.openbp.core.model.modelmgr.ModelMgr;

/**
 * Utility methods for synchronizing process items and sub process nodes.
 *
 * @author Heiko Erhardt
 */
public final class ProcessUtil
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Default initial node x position */
	public static final int POS_ENTRY_Y = 100;

	/** Default final node x position */
	public static final int POS_EXIT_Y = 600;

	/** Y offset from one node to the next */
	public static final int OFFSET_X = 150;

	/**
	 * Default entry/final node size.
	 * This should be equal to the node size specified in the process skin of the modeler.
	 */
	public static final int NODE_SIZE = 35;

	public static final String SYSTEM_TYPE_OBJECT = "Object";
	public static final String SYSTEM_TYPE_STRING = "String";
	public static final String SYSTEM_TYPE_INTEGER = "Integer";
	public static final String SYSTEM_TYPE_WORKFLOWTASK = "WorkflowTask";

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private ProcessUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Standard contents
	//////////////////////////////////////////////////

	/**
	 * Initializes a new process with standard element (nodes and transitions) according to the process type.
	 *
	 * @param process Process to initialize
	 */
	public static void setupProcessStandardConfiguration(ProcessItem process)
	{
		process.clearControlLinks();
		process.clearNodes();

		boolean createEntry = false;
		boolean createExit = false;

		String processType = process.getProcessType();
		if (ProcessTypes.USERINTERFACE.equals(processType))
		{
			createEntry = true;
		}
		else if (ProcessTypes.SUBPROCESS.equals(processType))
		{
			createEntry = true;
			createExit = true;
		}

		InitialNodeImpl initialNode = null;
		FinalNodeImpl finalNode = null;

		if (createEntry)
		{
			initialNode = createStandardInitialNode();
			initialNode.setDefaultEntry(true);
			initialNode.setGeometry("origin:" + OFFSET_X + ":" + POS_ENTRY_Y + "|size:" + NODE_SIZE);
			process.addNode(initialNode);
		}
		if (createExit)
		{
			finalNode = createStandardFinalNode();
			finalNode.setGeometry("origin:" + OFFSET_X + ":" + POS_EXIT_Y + "|size:" + NODE_SIZE);
			process.addNode(finalNode);
		}

		if (initialNode != null && finalNode != null)
		{
			// Link entry and final node
			ControlLink link = process.createControlLink();
			link.link(initialNode.getSocket(), finalNode.getSocket());

			// Add the control link and make sure its name references are up to date
			process.addControlLink(link);
			link.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.SYNC_LOCAL_REFNAMES);
		}
	}

	//////////////////////////////////////////////////
	// @@ Node/item synchronization
	//////////////////////////////////////////////////

	/**
	 * Performs a synchronization from the process to the sub process node.
	 * Copies all data values that can be mapped between the two types.<br>
	 * Also creates a new, unique name for the node.
	 *
	 * @param process Process providing the information
	 * @param node Node to update
	 * @param syncFlags Synchronization flags (see the constants of the {@link ItemSynchronization} class)
	 * @param socketNamesToIgnore List of socket names that should be skipped when synchronizing
	 * (i. e. these sockets won't be created for the node or removed from the node) or null
	 * @param ignoreEntries
	 *		true	Ignore initial nodes<br>
	 *		false	Consider entry and final nodes
	 */
	public static void itemToNode(ProcessItem process, SubprocessNode node, int syncFlags, String [] socketNamesToIgnore, boolean ignoreEntries)
	{
		node.setSubprocess(process);
		node.setSubprocessName(node.getProcess().determineItemRef(process));

		// Make node name unique
		String newName = process.getName();
		if (node.getProcess() != null)
		{
			newName = NamedObjectCollectionUtil.createUniqueId(process.getNodeList(), newName);
		}
		node.setName(newName);

		// Copy description and display name
		ItemSynchronization.syncDisplayObjects(node, process, syncFlags);

		// Copy geometry information from the sub process node to the process
		node.setGeometry(process.getNodeGeometry());

		// Copy the sockets
		node.clearSockets();
		for (Iterator it = process.getNodes(); it.hasNext();)
		{
			Node pn = (Node) it.next();

			SingleSocketNode ssn = null;
			boolean isEntrySocket = false;

			if (pn instanceof InitialNode)
			{
				if (ignoreEntries)
					continue;

				if ((syncFlags & ItemSynchronization.SYNC_HIDE_PRIVATE_ENTRIES) != 0)
				{
					if (((InitialNode) pn).getEntryScope() == InitialNode.SCOPE_PRIVATE)
					{
						// Hide private initial nodes
						continue;
					}
				}

				ssn = (SingleSocketNode) pn;
				isEntrySocket = true;
			}
			else if (pn instanceof FinalNode)
			{
				if ((syncFlags & ItemSynchronization.SYNC_HIDE_PRIVATE_ENTRIES) != 0)
				{
					if (((FinalNode) pn).getExitScope() == FinalNode.SCOPE_PRIVATE)
					{
						// Hide private initial nodes
						continue;
					}
				}

				// Jump target final nodes should  not be converted to sockets
				String cont = ((FinalNode) pn).getJumpTarget();
				if (cont == null)
				{
					ssn = (SingleSocketNode) pn;
					isEntrySocket = false;
				}
			}

			if (ssn == null)
			{
				// We are interested in entry and final nodes only, ignore this one
				continue;
			}

			String name = ssn.getName();

			if (StringUtil.contains(name, socketNamesToIgnore))
			{
				// We shall ignore this one
				continue;
			}

			NodeSocket ssnSocket = ssn.getSocket();
			NodeSocket nodeSocket = new NodeSocketImpl();

			nodeSocket.setName(name);

			// Copy description and display name
			ItemSynchronization.syncDisplayObjects(nodeSocket, ssn, syncFlags);

			nodeSocket.setEntrySocket(isEntrySocket);

			// Sockets that correspond to the default initial node or to nodes
			// named "In" or "Out" are considered to be default sockets.
			boolean defaultSocket = false;
			if ((ssn instanceof InitialNode) && ((InitialNode) ssn).isDefaultEntry() || name.equals(CoreConstants.DEFAULT_INITIAL_NODE_NAME) || name.equals(CoreConstants.DEFAULT_FINAL_NODE_NAME))
			{
				defaultSocket = true;
			}
			nodeSocket.setDefaultSocket(defaultSocket);

			if (ssn instanceof InitialNode)
			{
				nodeSocket.setRole(((InitialNode) ssn).getRole());
			}
			nodeSocket.setGeometry(ssn.getSocketGeometry());

			// Copy the parameters of the entry/final node socket
			// to the new socket of the sub process node
			List paramList = ssnSocket.getParamList();
			if (paramList != null)
			{
				int n = paramList.size();
				for (int i = 0; i < n; ++i)
				{
					NodeParam param = (NodeParam) paramList.get(i);

					try
					{
						NodeParam newParam = (NodeParam) param.clone();
						nodeSocket.addParam(newParam);
					}
					catch (CloneNotSupportedException e)
					{
						// Never happens
					}
				}
			}

			// Make the new socket belong to this node
			node.addSocket(nodeSocket);
			nodeSocket.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.SYNC_LOCAL_REFNAMES);
		}
	}

	/**
	 * Performs a synchronization from the sub process node to the process.
	 * Copies all data values that can be mapped between the two types.
	 *
	 * @param process Process to update
	 * @param node Node providing the information
	 * @param syncFlags Synchronization flags (see the constants of the {@link ItemSynchronization} class)
	 * @param socketNamesToIgnore List of socket names that should be skipped when synchronizing
	 * (i. e. these sockets won't appear in the process as initial node) or null
	 */
	public static void nodeToItem(SubprocessNode node, ProcessItem process, int syncFlags, String [] socketNamesToIgnore)
	{
		if ((syncFlags & ItemSynchronization.SYNC_CLEAR_TARGET) != 0)
		{
			// Clear all process contents if we are copying to a new item
			process.clearControlLinks();
			process.clearDataLinks();
			process.clearProcessVariables();
			process.clearNodeGroups();
			process.clearTextElements();
			process.clearNodes();
		}

		NodeSocket defaultEntrySocket = null;
		NodeSocket defaultExitSocket = null;

		int xMax = determineMaxX(process, socketNamesToIgnore);
		int xOffset = OFFSET_X;
		int xPosEntry = xMax + xOffset;
		int xPosExit = xMax + xOffset;
		int yPosEntry = POS_ENTRY_Y; // Initial nodes to the top
		int yPosExit = POS_EXIT_Y; // Final nodes to the bottom

		// Create entry and final nodes for each socket
		for (Iterator it = node.getSockets(); it.hasNext();)
		{
			NodeSocket nodeSocket = (NodeSocket) it.next();
			String name = nodeSocket.getName();

			if (StringUtil.contains(name, socketNamesToIgnore))
			{
				// We shall ignore this one
				continue;
			}

			SingleSocketNode processNode = null;
			NodeSocket processSocket = null;

			// Check if a corresponding entry/final node already exists
			Node pn = process.getNodeByName(name);

			if (pn != null)
			{
				if (nodeSocket.isEntrySocket())
				{
					if (!(pn instanceof InitialNode))
					{
						// There is a node, but it does not match the expected type, so ignore it.
						continue;
					}
				}
				else
				{
					if (!(pn instanceof FinalNode))
					{
						// There is a node, but it does not match the expected type, so ignore it.
						continue;
					}
				}

				processNode = (SingleSocketNode) pn;
				processSocket = processNode.getSocket();
			}

			if (processNode == null)
			{
				// There is no such entry/final node, create one
				int x;
				int y;

				processSocket = new NodeSocketImpl();

				// Create an entry or final node
				if (nodeSocket.isEntrySocket())
				{
					InitialNodeImpl initialNode = new InitialNodeImpl();
					processNode = initialNode;

					processSocket.setName(CoreConstants.SOCKET_OUT);
					processSocket.setEntrySocket(false);
					processSocket.setGeometry(createSocketGeometry("s"));

					if (nodeSocket.isDefaultSocket())
					{
						// The default socket will produce the default initial node
						initialNode.setDefaultEntry(true);
					}

					x = xPosEntry;
					y = yPosEntry;
					xPosEntry += xOffset;
				}
				else
				{
					processNode = new FinalNodeImpl();

					processSocket.setName(CoreConstants.SOCKET_IN);
					processSocket.setEntrySocket(true);
					processSocket.setGeometry(createSocketGeometry("n"));

					x = xPosExit;
					y = yPosExit;
					xPosExit += xOffset;
				}

				processNode.setName(name);

				// Assign the new pi node a socket
				processNode.setSocket(processSocket);

				// Create geometry information
				processNode.setGeometry("origin:" + x + ":" + y + "|size:" + NODE_SIZE);

				// Add the new node to the process
				process.addNode(processNode);
			}

			if (nodeSocket.isDefaultSocket())
			{
				if (nodeSocket.isEntrySocket())
					defaultEntrySocket = processSocket;
				else
					defaultExitSocket = processSocket;
			}

			// Copy description and display name
			ItemSynchronization.syncDisplayObjects(processNode, nodeSocket, syncFlags);

			processNode.setSocketGeometry(nodeSocket.getGeometry());

			// Copy parameter data from the current sub process node socket
			// to the single socket of the entry or final node
			List paramList = nodeSocket.getParamList();
			if (paramList != null && processSocket != null)
			{
				int n = paramList.size();
				for (int i = 0; i < n; ++i)
				{
					NodeParam param = (NodeParam) paramList.get(i);

					NodeParam processVariable = processSocket.getParamByName(param.getName());

					try
					{
						if (processVariable != null)
						{
							// Update existing parameter
							processVariable.copyFrom(param, Copyable.COPY_DEEP);
						}
						else
						{
							// Parameter not present, create a new one
							processVariable = (NodeParam) param.clone();
							processSocket.addParam(processVariable);
						}
					}
					catch (CloneNotSupportedException e)
					{
						// Never happens
					}
				}
			}
		}

		if (defaultEntrySocket != null && defaultExitSocket != null && !defaultEntrySocket.hasControlLinks())
		{
			// Link the default entry to the default output node
			ControlLink link = process.createControlLink();
			link.link(defaultEntrySocket, defaultExitSocket);

			// Add the control link and make sure its name references are up to date
			process.addControlLink(link);
			link.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.SYNC_LOCAL_REFNAMES);
		}

		// Remove each final node that doesn't appear as exit socket
		/* TODO Fix 5: Control link handling for node to item conversion
		 for (Iterator itNodes = process.getNodes (); itNodes.hasNext ();)
		 {
		 Node pn = (Node) itNodes.next ();

		 if (pn instanceof FinalNode)
		 {
		 FinalNode finalNode = (FinalNode) pn;
		 if (finalNode.getJumpTarget () == null)
		 {
		 if (node.getSocketByName (finalNode.getName ()) == null)
		 {
		 // Final node that doesn't appear as socket, remove
		 for (Iterator itLinks = finalNode.getSocket ().getControlLinks (); itLinks.hasNext ();)
		 {
		 ControlLink link = (ControlLink) itLinks.next ();

		 link.unlink ();
		 itLinks.remove ();
		 }

		 itNodes.remove ();
		 }
		 }
		 }
		 }
		 */
		// Copy description and display name
		ItemSynchronization.syncDisplayObjects(process, node, syncFlags);

		// Copy geometry information from the sub process node to the process
		process.setNodeGeometry(node.getGeometry());
	}

	//////////////////////////////////////////////////
	// @@ Standard process element factory methods
	//////////////////////////////////////////////////

	/**
	 * Creates a standard initial node.
	 *
	 * @return The new node
	 */
	public static InitialNodeImpl createStandardInitialNode()
	{
		InitialNodeImpl node = new InitialNodeImpl();
		node.setName(CoreConstants.DEFAULT_INITIAL_NODE_NAME);

		NodeSocket socket = createNodeSocket(CoreConstants.SOCKET_OUT, false);
		socket.setGeometry(createSocketGeometry("s"));
		node.setSocket(socket);

		return node;
	}

	/**
	 * Creates a standard final node.
	 *
	 * @return The new node
	 */
	public static FinalNodeImpl createStandardFinalNode()
	{
		FinalNodeImpl node = new FinalNodeImpl();
		node.setName(CoreConstants.DEFAULT_FINAL_NODE_NAME);

		NodeSocket socket = createNodeSocket(CoreConstants.SOCKET_IN, true);
		socket.setGeometry(createSocketGeometry("n"));
		node.setSocket(socket);

		return node;
	}

	/**
	 * Creates a standard decision node.
	 *
	 * @return The new node
	 */
	public static DecisionNodeImpl createStandardDecisionNode()
	{
		DecisionNodeImpl node = new DecisionNodeImpl();
		createStandardName(node);

		NodeSocket inSocket = createNodeSocket(CoreConstants.DEFAULT_INITIAL_NODE_NAME, true);
		NodeSocket yesSocket = createNodeSocket(CoreConstants.SOCKET_YES, false);
		NodeSocket noSocket = createNodeSocket(CoreConstants.SOCKET_NO, false);
		noSocket.setDefaultSocket(false);

		inSocket.setGeometry(createSocketGeometry("n"));
		yesSocket.setGeometry(createSocketGeometry("s"));
		noSocket.setGeometry(createSocketGeometry("e"));

		node.addSocket(inSocket);
		node.addSocket(yesSocket);
		node.addSocket(noSocket);

		return node;
	}

	/**
	 * Creates a standard fork node.
	 *
	 * @return The new node
	 */
	public static ForkNodeImpl createStandardForkNode()
	{
		ForkNodeImpl node = new ForkNodeImpl();
		createStandardName(node);

		NodeSocket inSocket = createNodeSocket(CoreConstants.DEFAULT_INITIAL_NODE_NAME, true);
		NodeSocket outSocket = createNodeSocket(CoreConstants.DEFAULT_FINAL_NODE_NAME, false);

		createNodeParam(CoreConstants.FORK_COLLECTION_PARAM, "Collection", "If present, for each element in the collection, a separate token will be created. The element can be retrieved at the outgoing token of the fork node using the CollectionElement parameter.", SYSTEM_TYPE_OBJECT, false, true, inSocket);
		createNodeParam(CoreConstants.FORK_COLLECTION_ELEMENT_PARAM, "Collection element", "Collection element that spawned the token.\nNote that the leaving token can run in non-interactive mode only. No visuals may be displayed here.", SYSTEM_TYPE_OBJECT, false, true, outSocket);

		node.addSocket(inSocket);
		node.addSocket(outSocket);

		return node;
	}

	/**
	 * Creates a standard join node.
	 *
	 * @return The new node
	 */
	public static JoinNodeImpl createStandardJoinNode()
	{
		JoinNodeImpl node = new JoinNodeImpl();
		configureStandardNode(node);
		return node;
	}

	/**
	 * Creates a standard wait state node.
	 *
	 * @return The new node
	 */
	public static WaitStateNodeImpl createStandardWaitStateNode()
	{
		WaitStateNodeImpl node = new WaitStateNodeImpl();
		configureStandardNode(node);
		return node;
	}

	/**
	 * Creates a standard placeholder node.
	 *
	 * @return The new node
	 */
	public static PlaceholderNodeImpl createStandardPlaceholderNode()
	{
		PlaceholderNodeImpl node = new PlaceholderNodeImpl();
		configureStandardNode(node);
		return node;
	}

	/**
	 * Creates a standard activity node.
	 *
	 * @return The new node
	 */
	public static ActivityNodeImpl createStandardActivityNode()
	{
		ActivityNodeImpl node = new ActivityNodeImpl();
		configureStandardNode(node);
		return node;
	}

	/**
	 * Creates a standard visual node.
	 *
	 * @return The new node
	 */
	public static VisualNodeImpl createStandardVisualNode()
	{
		VisualNodeImpl node = new VisualNodeImpl();
		configureStandardNode(node);
		return node;
	}

	/**
	 * Creates a standard workflow node.
	 *
	 * @return The new node
	 */
	public static WorkflowNodeImpl createStandardWorkflowNode()
	{
		WorkflowNodeImpl node = new WorkflowNodeImpl();

		createStandardName(node);

		NodeSocket inSocket = createNodeSocket(CoreConstants.DEFAULT_INITIAL_NODE_NAME, true);
		NodeSocket publishedSocket = createNodeSocket(CoreConstants.SOCKET_TASK_PUBLISHED, false);
		NodeSocket acceptedSocket = createNodeSocket(CoreConstants.SOCKET_TASK_ACCEPTED, false);
		publishedSocket.setDefaultSocket(false);

		// Create some (by default hidden and optional) workflow parameters at the 'In' socket
		createNodeParam("StepName", "Step name", "System name of this workflow step for dynamic assignment", SYSTEM_TYPE_STRING, false, true, inSocket);
		createNodeParam("StepDisplayName", "Step title", "Title of this workflow step for dynamic assignment", SYSTEM_TYPE_STRING, false, true, inSocket);
		createNodeParam("StepDescription", "Step description", "Detailled description of this workflow step for dynamic assignment", SYSTEM_TYPE_STRING, false, true, inSocket);
		createNodeParam("RoleId", "Role id", "Id of the role this workflow task is assigned to (public worklist) for dynamic assignment", SYSTEM_TYPE_STRING, false, true, inSocket);
		createNodeParam("UserId", "User id", "Id of the user this workflow task is assigned to (private worklist) for dynamic assignment", SYSTEM_TYPE_STRING, false, true, inSocket);
		createNodeParam("Priority", "Priority", "Priority of this workflow task (1-5) for dynamic assignment", SYSTEM_TYPE_INTEGER, false, true, inSocket);
		createNodeParam("RoleDefinitions", "Role definitions", "XML string describing ad-hoc role definitions that may override standard user-role assignments", SYSTEM_TYPE_STRING, false, true, inSocket);
		createNodeParam("DocumentIds", "Document ids", "List of ids (strings) or a single id of the documents or the dodcument that is processed by this workflow", SYSTEM_TYPE_OBJECT, false, true, inSocket);
		createNodeParam("DocumentMode", "Document mode", "Identifier that describes the processing mode of the document (application-specific, i. e. 'read', 'write' etc.", SYSTEM_TYPE_STRING, false, true, inSocket);

		// Create the hidden and optional WorkflowTask parameter at all sockets
		createNodeParam("WorkflowTask", "Workflow task", "Workflow task in case of reassignment of an existing task", SYSTEM_TYPE_WORKFLOWTASK, false, true, inSocket);
		createNodeParam("WorkflowTask", "Workflow task", "Created workflow task", SYSTEM_TYPE_WORKFLOWTASK, false, true, publishedSocket);
		createNodeParam("WorkflowTask", "Workflow task", "Resumed workflow task", SYSTEM_TYPE_WORKFLOWTASK, false, true, acceptedSocket);

		node.addSocket(inSocket);
		node.addSocket(publishedSocket);
		node.addSocket(acceptedSocket);

		return node;
	}

	/**
	 * Creates a standard workflow end node.
	 *
	 * @return The new node
	 */
	public static WorkflowEndNodeImpl createStandardWorkflowEndNode()
	{
		WorkflowEndNodeImpl node = new WorkflowEndNodeImpl();

		createStandardName(node);

		NodeSocket inSocket = createNodeSocket(CoreConstants.DEFAULT_INITIAL_NODE_NAME, true);

		// Create the hidden and optional WorkflowTask parameter
		createNodeParam("WorkflowTask", "Workflow task", "Workflow task that identifies the current workflow group or null to end all workflows that run in the current workflow group", SYSTEM_TYPE_WORKFLOWTASK, false, true, inSocket);

		node.addSocket(inSocket);

		return node;
	}

	/**
	 * Ensures that the given socket contains a workflow task parameter and adds one if not present.
	 *
	 * @param socket Socket to check
	 */
	public static void ensureWorkflowTaskParameter(NodeSocket socket)
	{
		if (socket.getParamByName("WorkflowTask") == null)
		{
			createNodeParam("WorkflowTask", "Workflow task", "Resumed workflow task", SYSTEM_TYPE_WORKFLOWTASK, false, true, socket);
		}
	}

	/**
	 * Creates a standard merge node.
	 *
	 * @return The new node
	 */
	public static MergeNodeImpl createStandardMergeNode()
	{
		MergeNodeImpl node = new MergeNodeImpl();
		createStandardName(node);

		NodeSocket inSocket1 = createNodeSocket(CoreConstants.DEFAULT_INITIAL_NODE_NAME + "1", true);
		NodeSocket inSocket2 = createNodeSocket(CoreConstants.DEFAULT_INITIAL_NODE_NAME + "2", true);
		NodeSocket outSocket = createNodeSocket(CoreConstants.DEFAULT_FINAL_NODE_NAME, false);
		inSocket1.setDefaultSocket(false);
		inSocket2.setDefaultSocket(false);

		inSocket1.setGeometry(createSocketGeometry("n"));
		inSocket2.setGeometry(createSocketGeometry("e"));
		outSocket.setGeometry(createSocketGeometry("s"));

		node.addSocket(inSocket1);
		node.addSocket(inSocket2);
		node.addSocket(outSocket);

		return node;
	}

	/**
	 * Creates a new node socket.
	 * The socket will be a default socket.
	 *
	 * @param name Name of the socket
	 * @param isEntry
	 *		true	The node socket will be an entry socket.
	 *		false	The node socket will be an exit socket.
	 * @return The new socket
	 */
	public static NodeSocket createNodeSocket(String name, boolean isEntry)
	{
		NodeSocket socket = new NodeSocketImpl();
		socket.setName(name);

		socket.setEntrySocket(isEntry);
		socket.setDefaultSocket(true);
		return socket;
	}

	private static void configureStandardNode(MultiSocketNode node)
	{
		createStandardName(node);

		NodeSocket inSocket = createNodeSocket(CoreConstants.DEFAULT_INITIAL_NODE_NAME, true);
		NodeSocket outSocket = createNodeSocket(CoreConstants.DEFAULT_FINAL_NODE_NAME, false);

		node.addSocket(inSocket);
		node.addSocket(outSocket);
	}

	/**
	 * Creates a new node parameter.
	 *
	 * @param name Name
	 * @param displayName Display name
	 * @param description Description
	 * @param systemTypeName Type name of a data type of the system model
	 * @param visible
	 *		true	The node parameter is visible by default
	 *		false	The node parameter is hidden by default
	 * @param optional
	 *		true	This is an optional parameter.<br>
	 *		false	The parameter is required.
	 * @param socket Socket
	 * @return The new parameter
	 */
	private static NodeParam createNodeParam(String name, String displayName, String description, String systemTypeName, boolean visible, boolean optional, NodeSocket socket)
	{
		NodeParam param = new NodeParamImpl();

		param.setName(name);
		param.setDisplayName(displayName);
		param.setDescription(description);

		ModelQualifier typeQualifier = new ModelQualifier(CoreConstants.SYSTEM_MODEL_NAME, systemTypeName, ItemTypes.TYPE, null);
		DataTypeItem dataType = null;
		ModelMgr modelMgr = (ModelMgr) CommonRegistry.lookup(ModelMgr.class);
		try
		{
			dataType = (DataTypeItem) modelMgr.getItemByQualifier(typeQualifier, true);
		}
		catch (ModelException e)
		{
			ExceptionUtil.printTrace(e);
			return null;
		}
		param.setDataType(dataType);
		param.setTypeName(systemTypeName);

		param.setVisible(visible);
		param.setOptional(optional);

		if (socket != null)
		{
			socket.addParam(param);
		}

		return param;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Determines the maximum x coordinate used by nodes of the process.
	 *
	 * @param process Process
	 * @param socketNamesToIgnore List of socket names that should be skipped when computing
	 * the process size
	 * @return The bottom coordinate of the processes bounding rectangle
	 */
	public static int determineMaxX(ProcessItem process, String [] socketNamesToIgnore)
	{
		int xMax = 0;

		for (Iterator it = process.getNodes(); it.hasNext();)
		{
			Node node = (Node) it.next();

			String name = node.getName();
			if (StringUtil.contains(name, socketNamesToIgnore))
			{
				// We shall ignore this one
				continue;
			}

			String geometry = node.getGeometry();
			if (geometry != null)
			{
				// Search for "orgin:"
				int index = geometry.indexOf("origin:");
				if (index >= 0)
				{
					// Search for the next ":"
					int index1 = geometry.indexOf(":", index + 7);
					if (index1 >= 0)
					{
						String s = geometry.substring(index + 7, index1);

						try
						{
							int x = Integer.parseInt(s);
							if (x > xMax)
								xMax = x;
						}
						catch (NumberFormatException e)
						{
							// Ignore
						}
					}
				}
			}
		}

		return xMax;
	}

	/**
	 * Creates a geometry for the given socket that corresponds to the given orientation.
	 *
	 * @param orientation Orientation ('e', 's', 'w', 'n')
	 * @return The geomtry or null on argument error
	 */
	private static String createSocketGeometry(String orientation)
	{
		return "angle:" + determineAngle(orientation);
	}

	/**
	 * Determines the tag angle that corresponds to the given orientation.
	 *
	 * @param orientation Orientation ("e", "s", "w", "n")
	 * @return The angle
	 */
	public static double determineAngle(String orientation)
	{
		// Default: North
		double angle = 3 * Math.PI / 2;

		if ("e".equals(orientation))
		{
			angle = 0;
		}
		else if ("s".equals(orientation))
		{
			angle = Math.PI / 2;
		}
		else if ("w".equals(orientation))
		{
			angle = Math.PI;
		}

		return angle;
	}

	private static void createStandardName(Node node)
	{
		String s = node.getClass().getName();
		int i = s.lastIndexOf('.');
		s = s.substring (i + 1);
		i = s.lastIndexOf("Node");
		if (i > 0)
			s = s.substring(0, i);
		node.setName(s);
	}
}
