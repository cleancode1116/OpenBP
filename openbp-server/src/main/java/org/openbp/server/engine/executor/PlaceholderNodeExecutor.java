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
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.PlaceholderNode;
import org.openbp.server.context.TokenContext;
import org.openbp.server.engine.EngineExecutor;
import org.openbp.server.engine.ModelObjectExecutorBase;

/**
 * Executor for placeholder nodes.
 *
 * @author Heiko Erhardt
 */
public class PlaceholderNodeExecutor extends ModelObjectExecutorBase
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
		PlaceholderNode node = (PlaceholderNode) entrySocket.getNode();

		NodeSocket nextSocket = node.getDefaultExitSocket();
		if (nextSocket == null)
		{
			String msg = LogUtil.error(getClass(), "No default exit socket present for placeholder node $0. [{1}]", node.getQualifier(), context);
			throw new EngineException("NoDefaultExitSocket", msg);
		}
		context.setCurrentSocket(nextSocket);
	}
}
