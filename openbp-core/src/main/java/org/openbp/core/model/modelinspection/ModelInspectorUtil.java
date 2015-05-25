/*
 *   Copyright 2008 skynamics AG
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
package org.openbp.core.model.modelinspection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.common.CollectionUtil;
import org.openbp.core.model.WorkflowTaskDescriptor;
import org.openbp.core.model.item.process.ControlLink;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.WorkflowNode;

/**
 * Utility methods that produce workflow and socket descriptors.
 */
public final class ModelInspectorUtil
{
	/**
	 * Private constructor prevents instantiation.
	 */
	private ModelInspectorUtil()
	{
	}

	/**
	 * Extracts a workflow process descriptor from a given process.
	 *
	 * @param process Process
	 * @return The step desriptor or null if the process does not contain workflow nodes
	 */
	public static WorkflowProcessDescriptor extractWorkflowProcessDescriptor(ProcessItem process)
	{
		WorkflowProcessDescriptor pd = new WorkflowProcessDescriptor();

		pd.setQualifiedName(process.getQualifier().toString());
		pd.setName(process.getName());
		pd.setDisplayName(process.getDisplayName());
		pd.setDescription(process.getDescription());

		boolean foundStep = false;
		for (Iterator it = process.getNodes(); it.hasNext();)
		{
			Node node = (Node) it.next();

			if (node instanceof WorkflowNode)
			{
				WorkflowTaskDescriptor task = ((WorkflowNode) node).getWorkflowTaskDescriptor();

				WorkflowStepDescriptor sd = new WorkflowStepDescriptor();
				sd.setName(task.getStepName());
				sd.setDisplayName(task.getStepDisplayName());
				sd.setDescription(task.getStepDescription());
				sd.setRoleId(task.getRoleId());
				sd.setUserId(task.getUserId());
				pd.addStep(sd);
				foundStep = true;
			}
		}

		if (! foundStep)
			return null;

		return pd;
	}

	/**
	 * Determines the exits of the next visual node of a particular type to be executed.
	 *
	 * Given the position of a particular node within a process, this activity determines the names and titles of all exit sockets of the next node of a particular type.
	 *
	 * The target node is either the current node itself (if the current position refers to a node of the desired type) or the next visual node that is found within the flow control path using the default exits of the nodes.
	 *
	 * This can be used for dynamically determining possible decision paths e. g. for constructing a context menu in rich client environments.
	 *
	 * @param currentSocket Current position to start the search from
	 * @param desiredNodeClass Class of the desired nodes (must implement the {@link Node} interface)
	 * @param sequenceIdSocketsOnly true if only sockets that have a sequence id assigned
	 * should be considered. In this case, the list will be ordered by the sequence id. False for all sockets.
	 * @return The list of exit sockets (contains {@link SocketDescriptor} objects) or null if no such node could be found
	 */
	public static List determinePossibleExits(NodeSocket currentSocket, Class desiredNodeClass, boolean sequenceIdSocketsOnly)
	{
		Node node = findNextMatchingNode(currentSocket, desiredNodeClass);
		if (node == null)
			return null;

		List ret = new ArrayList();

		// Get sockets in order of sequence number and add to return list
		Iterator itSockets;
		if (sequenceIdSocketsOnly)
			itSockets = node.getOrderedSockets();
		else
			itSockets = node.getSockets();

		while (itSockets.hasNext())
		{
			NodeSocket socket = (NodeSocket) itSockets.next();

			SocketDescriptor desc = new SocketDescriptor();
			desc.setName(socket.getName());
			desc.setDisplayName(socket.getDisplayText());
			desc.setDescription(socket.getDescription());
			desc.setExitSocket(socket.isExitSocket());
			desc.setDefaultSocket(socket.isDefaultSocket());
			desc.setRole(socket.getRole());

			desc.setSequenceId(socket.getSequenceId());

			ret.add(desc);
		}

		return ret;
	}

	private static Node findNextMatchingNode(NodeSocket currentSocket, Class desiredNodeClass)
	{
		ArrayList visitedNodes = new ArrayList();

		for (Node node = currentSocket.getNode(); node != null;)
		{
			if (desiredNodeClass == null || desiredNodeClass.isInstance(node))
				return node;

			if (CollectionUtil.containsReference(visitedNodes, node))
			{
				// We have already visited this node; return to prevent endless recursion
				break;
			}
			visitedNodes.add(node);

			// Advance to next node
			NodeSocket socket = node.getDefaultExitSocket();
			if (socket == null)
				break;

			if (! socket.hasControlLinks())
				break;

			// Follow the first control link to the next node
			ControlLink link = (ControlLink) socket.getControlLinks().next();
			socket = link.getTargetSocket();
			node = socket.getNode();
		}

		return null;
	}
}
