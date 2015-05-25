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
package org.openbp.model.system.misc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextUtil;
import org.openbp.server.handler.Handler;
import org.openbp.server.handler.HandlerContext;

// {{*Custom imports*
// }}*Custom imports*

/**
 * Remove process variable.
 * Implementation of the RemoveVariable activity handler.
 * Removes a process variable from the session.
 * If the parameter value is equal to '*' all process variables will be removed, else only the named variable will be removed.
 * If you want to remove more variables use the ';' as delimiter without spaces.
 * 
 * Input sockets/parameter:
 *   Socket 'In'
 *     Parameter 'VariableNames': Variable names
 * 
 * Output sockets/parameter:
 *   Socket 'Out'
 */
public class RemoveVariableHandler
	// {{*Custom extends*
	// }}*Custom extends*
	// {{*Custom interfaces*
	implements Handler
	// }}*Custom interfaces*
{
	/** Parameter VariableNames */
	private static final String PARAM_VARIABLENAMES = "VariableNames";

	// {{*Custom constants*
	// }}*Custom constants*

	// {{*Custom members*
	// Note: If you define member variables, consider the fact that the same handler instance may be executed
	// by multiple threads in parallel, so you have to make sure that your implementation is thread safe.
	// In general, member variables should be defined for global-like data only.
	// }}*Custom members*

	/**
	 * Executes the handler.
	 *
	 * @param hc Handler context that contains execution parameters
	 * @return true if the handler handled the event, false to apply the default handling to the event
	 * @throws Exception Any exception that may occur during execution of the handler will be
	 * propagated to an exception handler if defined or abort the process execution otherwise.
	 */
	public boolean execute(HandlerContext hc)
		throws Exception
	{
		// {{*Handler implementation*
		String variableNames = (String) hc.getParam(PARAM_VARIABLENAMES);

		if (variableNames != null)
		{
			StringTokenizer st = new StringTokenizer(variableNames, ";");
			while (st.hasMoreTokens())
			{
				String token = st.nextToken().trim();
				if (token.equals("*"))
				{
					// Remove all process variables
					removeAllProcessVariables(hc.getTokenContext());
					break;
				}

				// Remove variable
				hc.getTokenContext().removeProcessVariableValue(token);
			}
		}

		return true;
		// }}*Handler implementation*
	}

	// {{*Custom methods*

	private void removeAllProcessVariables(TokenContext context)
	{
		// Cache variable names to prevent concurrent modification exception
		ArrayList variables = new ArrayList();
		Map paramValues = context.getParamValues();
		for (Iterator it = paramValues.keySet().iterator(); it.hasNext();)
		{
			String name = (String) it.next();
			if (TokenContextUtil.isProcessVariableIdentifier(name))
			{
				variables.add(name);
			}
		}

		// Remove the process variables
		for (Iterator it = variables.iterator(); it.hasNext();)
		{
			String name = (String) it.next();
			context.removeParamValue(name);
		}
	}

	// }}*Custom methods*
}

