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

import java.util.Iterator;

import org.openbp.common.CollectionUtil;
import org.openbp.common.logger.LogUtil;
import org.openbp.core.CoreConstants;
import org.openbp.core.OpenBPException;
import org.openbp.core.engine.EngineException;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.process.ForkNode;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.Param;
import org.openbp.server.context.LifecycleRequest;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextUtil;
import org.openbp.server.engine.EngineExecutor;
import org.openbp.server.engine.EngineUtil;
import org.openbp.server.engine.ModelObjectExecutorBase;

/**
 * Executor for fork nodes.
 *
 * @author Heiko Erhardt
 */
public class ForkNodeExecutor extends ModelObjectExecutorBase
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
		ForkNode node = (ForkNode) entrySocket.getNode();

		NodeSocket defaultOutSocket = node.getDefaultExitSocket();
		if (defaultOutSocket == null)
		{
			String msg = LogUtil.error(getClass(), "No default exit socket present for fork node $0. [{1}]", node.getQualifier(), context);
			throw new EngineException("NoDefaultExitSocket", msg);
		}

		Object collectionParamValue = TokenContextUtil.getParamValue(context, entrySocket, CoreConstants.FORK_COLLECTION_PARAM);
		if (collectionParamValue != null)
		{
			// Automatic fork node based on input collection
			if (defaultOutSocket.getParamByName(CoreConstants.FORK_COLLECTION_ELEMENT_PARAM) == null)
			{
				String msg = LogUtil.error(getClass(), "Fork node having a $0 input parameter requires a $1 output parameter. [{2}]", CoreConstants.FORK_COLLECTION_PARAM, CoreConstants.FORK_COLLECTION_ELEMENT_PARAM, context);
				throw new EngineException("NoCollectionElementForFork", msg);
			}

			Iterator it = CollectionUtil.iterator(collectionParamValue);
			while (it.hasNext())
			{
				Object collectionElement = it.next();

				// Create a new child context
				TokenContext childContext = getEngine().getTokenContextService().createChildContext(context);

				// Provide the collection element to it
				Param outParam = defaultOutSocket.getParamByName(CoreConstants.FORK_COLLECTION_ELEMENT_PARAM); 
				if (outParam != null)
				{
					// If the exit socket contains a 'WorkflowTask' parameter, set it
					TokenContextUtil.setParamValue(childContext, outParam, collectionElement);
				}

				// Copy the data of the node entry socket in the current context
				// to the exit socket in the child context.
				EngineUtil.copySocketData(entrySocket, context, defaultOutSocket, childContext);

				childContext.setCurrentSocket(defaultOutSocket);
				getEngine().resumeToken(childContext);
			}
		}
		else
		{
			// Iterate all exit sockets
			for (Iterator itOutSockets = node.getSockets(false); itOutSockets.hasNext();)
			{
				final NodeSocket outSocket = (NodeSocket) itOutSockets.next();

				if (outSocket.getName().equals(CoreConstants.FORK_RESUME))
					continue;

				// Create a new child context
				TokenContext childContext = getEngine().getTokenContextService().createChildContext(context);

				// Copy the data of the node entry socket in the current context
				// to the exit socket in the child context.
				EngineUtil.copySocketData(entrySocket, context, outSocket, childContext);

				childContext.setCurrentSocket(outSocket);
				getEngine().resumeToken(childContext);
			}
		}

		// When there is a 'Resume' exit socket, let's continue there; otherwise simply end
		NodeSocket nextSocket = getEngine().resolveSocketRef(CoreConstants.FORK_RESUME, entrySocket, context, false);
		if (nextSocket != null)
		{
			context.setCurrentSocket(nextSocket);
		}
		else
		{
			// No socket to continue, suspend this token until join
			context.setLifecycleRequest(LifecycleRequest.SUSPEND_IMMEDIATE);
		}
	}
}
