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
package org.openbp.model.jms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openbp.common.CommonRegistry;
import org.openbp.core.engine.EngineException;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.server.handler.Handler;
import org.openbp.server.handler.HandlerContext;
import org.openbp.server.jms.JMSExecutionRequester;

// {{*Custom imports*
// }}*Custom imports*

/**
 * Send process execution request.
 * Implementation of the SendProcessExecutionRequest activity handler.
 * Requests the execution of a process via a JMS server (asynchronous process execution).
 * Any additional arguments provided to the activity will be passed to the specified process entry.
 * The activity immediately returns. Any return values of the called process will be lost.
 * 
 * Input sockets/parameter:
 *   Socket 'In'
 *     Parameter 'ProcessRef': Process reference
 * 
 * Output sockets/parameter:
 *   Socket 'Out'
 */
public class SendProcessExecutionRequestHandler
	// {{*Custom extends*
	// }}*Custom extends*
	// {{*Custom interfaces*
	implements Handler
	// }}*Custom interfaces*
{
	/** Parameter ProcessRef */
	private static final String PARAM_PROCESSREF = "ProcessRef";

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
		String processRef = (String) hc.getParam(PARAM_PROCESSREF);

		JMSExecutionRequester requester = (JMSExecutionRequester) CommonRegistry.lookup(JMSExecutionRequester.class);
		if (requester == null)
			throw new EngineException("JMS.NoRequester", "JMS functionality is available only when the OpenBP server was started in JMS server mode.");

		Map parameters = null;
		for (Iterator it = hc.getCurrentSocket().getParams(); it.hasNext();)
		{
			NodeParam param = (NodeParam) it.next();
			String name = param.getName();

			if (name.equals(PARAM_PROCESSREF))
			{
				continue;
			}

			if (parameters == null)
				parameters = new HashMap();
			parameters.put(name, hc.getParam(name));
		}

		try
		{
			requester.requestExecution(processRef, parameters);
		}
		catch (Exception e)
		{
			throw new EngineException("JMS.RequesterError", "Error requesting process execution via JMS.", e);
		}
		return true;
		// }}*Handler implementation*
	}

	// {{*Custom methods*
	// }}*Custom methods*
}

