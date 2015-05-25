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
import org.openbp.core.model.item.process.DecisionNode;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.server.context.TokenContext;
import org.openbp.server.engine.EngineExecutor;
import org.openbp.server.engine.ModelObjectExecutorBase;
import org.openbp.server.engine.script.ScriptEngine;

/**
 * Executor for decision nodes.
 *
 * @author Heiko Erhardt
 */
public class DecisionNodeExecutor extends ModelObjectExecutorBase
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
		DecisionNode node = (DecisionNode) entrySocket.getNode();

		boolean result = false;

		String expression = node.getExpression();
		if (expression != null)
		{
			// Evaluate a script expression
			ScriptEngine scriptEngine = getEngine().getScriptEngineFactory().obtainScriptEngine(context);
			try
			{
				// Evaluate the expression
				scriptEngine.prepareNodeSocketExecution(entrySocket);
				Object value = scriptEngine.executeScript(expression, "Decision node script", entrySocket.getNode()
														  .getQualifier().toString());
				scriptEngine.finishNodeSocketExecution(entrySocket);

				// Assign the result to the parameter
				if (value != null && ! value.equals(Boolean.FALSE))
					result = true;
			}
			finally
			{
				getEngine().getScriptEngineFactory().releaseScriptEngine(scriptEngine);
			}
		}

		String socketName = result ? CoreConstants.SOCKET_YES : CoreConstants.SOCKET_NO;
		NodeSocket nextSocket = getEngine().resolveSocketRef(socketName, entrySocket, context, true);
		context.setCurrentSocket(nextSocket);
	}
}
