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
import org.openbp.core.model.item.activity.JavaActivityItem;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.server.context.TokenContext;
import org.openbp.server.engine.EngineExecutor;
import org.openbp.server.engine.ModelObjectExecutorBase;
import org.openbp.server.handler.HandlerContext;

/**
 * Executor for Java activity items.
 *
 * @author Heiko Erhardt
 */
public class JavaActivityItemExecutor extends ModelObjectExecutorBase
{
	// TOREMOVE Is thing method called anywhere?
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
		JavaActivityItem activity = (JavaActivityItem) mo;

		NodeSocket nextSocket = null;

		TokenContext oldContext = context;
		HandlerContext hc = getEngine().executeHandler(activity.getHandlerDefinition(), HandlerTypes.ACTIVITY, context, context.getCurrentSocket(), nextSocket);
		if (hc != null)
		{
			context = hc.getTokenContext();
			if (context != oldContext)
			{
				// Token context instance has changed due to rollback
				ee.setTokenContext(context);
				return;
			}

			nextSocket = hc.getNextSocket();
			if (nextSocket == null)
			{
				String msg = LogUtil.error(getClass(), "Handler of activity $0 did not provide a a socket to continue with. [{1}]", activity.getQualifier(), context);
				throw new EngineException("MissingNextSocket", msg);
			}
		}

		if (nextSocket != null)
		{
			context.setCurrentSocket(nextSocket);
		}
	}
}
