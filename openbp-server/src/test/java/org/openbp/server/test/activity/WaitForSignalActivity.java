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
package org.openbp.server.test.activity;

import org.openbp.common.logger.LogUtil;
import org.openbp.server.context.TokenContext;
import org.openbp.server.handler.Handler;
import org.openbp.server.handler.HandlerContext;
import org.openbp.server.test.base.TestCaseSyncMgr;

// {{*Custom imports*
// }}*Custom imports*

/**
 * Wait for signal.
 * Implementation of the WaitForSignal activity handler.
 * 
 * Input sockets/parameter:
 *   Socket 'In'
 *     Parameter 'SignalId': Signal id
 * 
 * Output sockets/parameter:
 *   Socket 'Out'
 *     Parameter 'SignalValue': Signal value
 */
public class WaitForSignalActivity
	// {{*Custom extends*
	// }}*Custom extends*
	// {{*Custom interfaces*
	implements Handler
// }}*Custom interfaces*
{
	/** Parameter SignalId */
	private static final String PARAM_SIGNALID = "SignalId";

	/** Parameter SignalValue */
	private static final String PARAM_SIGNALVALUE = "SignalValue";

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
		TokenContext context = hc.getTokenContext();
		String signalId = (String) hc.getParam(PARAM_SIGNALID);

		LogUtil.debug(getClass(), "Context $0 waiting for signal $1...", context.getId(), signalId);

		Object signalValue = TestCaseSyncMgr.getInstance().receiveSignal(null, signalId, 10);

		LogUtil.debug(getClass(), "Context $0 received signal $1, value $2.", context.getId(), signalId, signalValue);

		hc.setResult(PARAM_SIGNALVALUE, signalValue);

		return true;
		// }}*Handler implementation*
	}

	// {{*Custom methods*
	// }}*Custom methods*
}
