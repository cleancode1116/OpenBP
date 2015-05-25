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
package org.openbp.server.engine.executor;

import org.openbp.core.CoreConstants;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.process.FinalNode;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.server.context.CallStackItem;
import org.openbp.server.context.LifecycleRequest;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextUtil;
import org.openbp.server.engine.EngineExecutor;
import org.openbp.server.engine.EngineUtil;
import org.openbp.server.engine.ModelObjectExecutorBase;

/**
 * Executor for final nodes.
 *
 * @author Heiko Erhardt
 */
public class FinalNodeExecutor extends ModelObjectExecutorBase
{
	/**
	 * Executes a process element.
	 *
	 * @param mo Process element to execute
	 * @param ee Engine executor that called this method
	 * @throws OpenBPException On error
	 */
	public void executeModelObject(ModelObject mo, EngineExecutor ee)
	{
		TokenContext context = ee.getTokenContext();
		NodeSocket entrySocket = context.getCurrentSocket();
		FinalNode node = (FinalNode) entrySocket.getNode();

		NodeSocket nextSocket = null;

		String jumpTarget = (String) TokenContextUtil.getParamValue(context, entrySocket, CoreConstants.DYNAMIC_JUMPTARGET_PARAM_NAME);
		if (jumpTarget == null)
			jumpTarget = node.getJumpTarget();

		if (jumpTarget != null)
		{
			// Execute a jump target final node
			nextSocket = executeJumpTargetFinalNode(entrySocket, context, jumpTarget);
		}
		else
		{
			// Execute a regular return final node
			nextSocket = executeReturnFinalNode(node, entrySocket, context);
		}

		if (nextSocket != null)
		{
			// Copy the data of the exit socket of the sub process in the
			// sub process context to the exit socket of this activity in the current context
			EngineUtil.copySocketData(entrySocket, context, nextSocket, context);

			context.setCurrentSocket(nextSocket);
		}
		else
		{
			// Our token ends here
			context.setLifecycleRequest(LifecycleRequest.STOP);
		}
	}

	/**
	 * Executes a regular return final node.
	 *
	 * @param node Node to be executed
	 * @param entrySocket Entry that is being used to execute the node
	 * @param context Token context of the process
	 * @return The exit socket the node exited on or null if exit socket was chosen by the node
	 * @throws OpenBPException Any exception in the activity implementation class
	 * will be wrapped by this exception
	 */
	private NodeSocket executeReturnFinalNode(Node node, NodeSocket entrySocket, TokenContext context)
	{
		// TODO Commented out due to showcase: Split final and return nodes!

		// A top-level final node will end the process
		NodeSocket nextSocket = null;

		// Check whether a return is possible.
		if (context.getCallStack().getCallDepth() != 0)
		{
			// Pop the reference to the calling sub proces node from the call stack
			CallStackItem stackItem = context.getCallStack().pop();

			// Get the payload socket of the call stack item
			NodeSocket callStackSocket = stackItem.getNodeSocket();
			if (callStackSocket != null)
			{
				// Regular call stack item (sub process).
				// Determine the exit socket of the sub process node we shall continue with
				// using the name of the final node of the sub process.
				String exitName = node.getName();
				nextSocket = getEngine().resolveSocketRef(exitName, callStackSocket, context, true);
			}
		}

		return nextSocket;
	}

	/**
	 * Executes a jump target final node.
	 *
	 * @param entrySocket Entry that is being used to execute the node
	 * @param context Token context of the process
	 * @param target Jump target of the node
	 * @return The exit socket the node exited on or null if exit socket was chosen by the node
	 * @throws OpenBPException Any exception in the activity implementation class
	 * will be wrapped by this exception
	 */
	private NodeSocket executeJumpTargetFinalNode(NodeSocket entrySocket, TokenContext context, String target)
	{
		return getEngine().resolveSocketRef(target, context.getCurrentSocket(), context, true);
	}
}
