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
import org.openbp.core.OpenBPException;
import org.openbp.core.engine.EngineException;
import org.openbp.core.handler.HandlerTypes;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.process.ActivityNode;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.server.context.TokenContext;
import org.openbp.server.engine.EngineExecutor;
import org.openbp.server.engine.ModelObjectExecutorBase;
import org.openbp.server.handler.HandlerContext;

/**
 * Executor for activity nodes.
 *
 * @author Heiko Erhardt
 */
public class ActivityNodeExecutor extends ModelObjectExecutorBase
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
		ActivityNode node = (ActivityNode) entrySocket.getNode();

		NodeSocket nextSocket = node.getDefaultExitSocket();

		// Activity reference missing, try the node's handler
		TokenContext oldContext = context;
		HandlerContext hc = getEngine().executeHandler(node.getActivityHandlerDefinition(), HandlerTypes.ACTIVITY, context, context.getCurrentSocket(), nextSocket);
		if (hc != null)
		{
			context = hc.getTokenContext();
			if (context != oldContext)
			{
				// Token context instance has changed due to rollback
				ee.setTokenContext(context);
			}

			nextSocket = hc.getNextSocket();
			if (nextSocket == null)
			{
				String msg = LogUtil.error(getClass(), "Handler of node $0 has set a null next socket. [{1}]", node.getQualifier(), context);
				throw new EngineException("MissingNextSocket", msg);
			}
		}
		else
		{
			// No handler present, try the default socket
			if (nextSocket != null)
			{
				LogUtil.warn(getClass(), "No activity handler defined for activity node $0, using default exit socket $1.", node.getQualifier(), nextSocket.getName());
			}
			else
			{
				String msg = LogUtil.error(getClass(), "No activity handler found for activity node $0 and no default socket present. [{1}]", node.getQualifier(), context);
				throw new EngineException("NoDefaultExitSocket", msg);
			}
		}

		context.setCurrentSocket(nextSocket);
	}
}
