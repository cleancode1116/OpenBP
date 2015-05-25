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

import org.openbp.common.logger.LogUtil;
import org.openbp.core.CoreConstants;
import org.openbp.core.OpenBPException;
import org.openbp.core.engine.EngineException;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.InitialNode;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.SubprocessNode;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextUtil;
import org.openbp.server.engine.EngineExecutor;
import org.openbp.server.engine.EngineUtil;
import org.openbp.server.engine.ModelObjectExecutorBase;

/**
 * Executor for sub process nodes.
 *
 * @author Heiko Erhardt
 */
public class SubprocessNodeExecutor extends ModelObjectExecutorBase
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
		SubprocessNode node = (SubprocessNode) entrySocket.getNode();

		// If we do not have a sub process, simply continue with the default exit socket
		NodeSocket nextSocket = node.getDefaultExitSocket();

		ProcessItem subprocess = node.getSubprocess();

		// Dynamic sub processes:
		// If we have a Process Name parameter and its value is a string...
		Object result = TokenContextUtil.getParamValue(context, entrySocket, CoreConstants.DYNAMIC_SUBPROCESS_PARAM_NAME);
		if (result != null && result instanceof String)
		{
			// ...try to lookup and return a corresponding process.
			try
			{
				subprocess = (ProcessItem) context.getExecutingModel().resolveItemRef((String) result, ItemTypes.PROCESS);
			}
			catch (ModelException re)
			{
				// Fall thru to the default.
			}
		}

		if (subprocess != null)
		{
			// The default exit socket of the initial node of the sub process
			// that has the same name as the initial node of this sub process
			// node is our initial socket for execution of the sub process.
			String entryName = entrySocket.getName();
			Node subprocessInitialNode = subprocess.getNodeByName(entryName);

			if (subprocessInitialNode == null)
			{
				String msg = LogUtil.error(getClass(), "Initial node $0 called by $1 not found in process $2. [{3}]", entryName, node.getQualifier(), subprocess.getQualifier(), context);
				throw new EngineException("InitialNodeNotFound", msg);
			}

			if (!(subprocessInitialNode instanceof InitialNode))
			{
				String msg = LogUtil.error(getClass(), "Node $0 called by $1 is not an initial node. [{2}]", subprocessInitialNode.getQualifier(), node.getQualifier(), context);
				throw new EngineException("NotAnInitialNode", msg);
			}

			// Continue with the initial node of the sub process
			nextSocket = subprocessInitialNode.getDefaultExitSocket();
			if (nextSocket == null)
			{
				String msg = LogUtil.error(getClass(), "No default exit socket present for sub process initial node $0. [{1}]", subprocessInitialNode.getQualifier(), context);
				throw new EngineException("NoDefaultExitSocket", msg);
			}

			// Push the current position onto the call stack
			context.getCallStack().pushSubprocess(entrySocket);

			// Create the process variables of the subprocess
			EngineUtil.createProcessVariables(subprocess, context);

			// Copy the data of the node entry socket in the current context
			// to the initial node of the sub process in the new context
			EngineUtil.copySocketData(entrySocket, context, nextSocket, context);
		}
		else
		{
			String msg = LogUtil.error(getClass(), "Missing sub process for sub process node $0. [{1}]", node.getQualifier(), context);
			throw new EngineException("MissingSubprocess", msg);
		}

		context.setCurrentSocket(nextSocket);
	}
}
