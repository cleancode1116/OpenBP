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
import org.openbp.core.model.item.process.JoinNode;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.server.context.LifecycleRequest;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextService;
import org.openbp.server.engine.EngineExecutor;
import org.openbp.server.engine.EngineUtil;
import org.openbp.server.engine.ModelObjectExecutorBase;

/**
 * Executor for join nodes.
 *
 * @author Heiko Erhardt
 */
public class JoinNodeExecutor extends ModelObjectExecutorBase
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
		JoinNode node = (JoinNode) entrySocket.getNode();

		NodeSocket nextSocket = null;

		TokenContext parentContext = context.getParentContext();
		if (parentContext != null)
		{
			// Our token ends here
			context.setLifecycleRequest(LifecycleRequest.STOP);

			NodeSocket parentNextSocket = null;
			if (parentContext.hasChildContext())
			{
				parentNextSocket = getEngine().resolveSocketRef(CoreConstants.INCOMPLETE_SOCKET_NAME, entrySocket, parentContext, false);
			}
			else
			{
				parentNextSocket = getEngine().resolveSocketRef(CoreConstants.SOCKET_OUT, entrySocket, parentContext, true);
			}

			if (parentNextSocket != null)
			{
				// Copy the data of the node entry socket in the current context
				// to the exit socket in the child context.
				EngineUtil.copySocketData(entrySocket, context, parentNextSocket, parentContext);

				// Continue the parent context in a new thread
				parentContext.setCurrentSocket(parentNextSocket);
				parentContext.setLifecycleRequest(LifecycleRequest.RESUME);

				TokenContextService contextService = getEngine().getTokenContextService();
				contextService.saveContext(parentContext);
			}
		}
		else
		{
			nextSocket = node.getDefaultExitSocket();
		}

		// Join implicitely commits the transaction
		if (nextSocket != null)
		{
			context.setCurrentSocket(nextSocket);

			getEngine().getTokenContextService().saveContext(context);
		}
	}
}
