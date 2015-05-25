package org.openbp.server.test.activity;

import org.openbp.common.logger.LogUtil;
import org.openbp.server.context.TokenContext;
import org.openbp.server.handler.Handler;
import org.openbp.server.handler.HandlerContext;
import org.openbp.server.test.base.TestCaseSyncMgr;

// {{*Custom imports*
// }}*Custom imports*

/**
 * Set signal.
 * Implementation of the SetSignal activity handler.
 * Sets a signal in the test case signal manager.
 * 
 * Input sockets/parameter:
 *   Socket 'In'
 *     Parameter 'SignalId'
 *     Parameter 'SignalValue'
 * 
 * Output sockets/parameter:
 *   Socket 'Out'
 */
public class SetSignalActivity
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
		Object signalValue = hc.getParam(PARAM_SIGNALVALUE);

		LogUtil.debug(getClass(), "Context $0 setting signal $1 to $2...", context.getId(), signalId, signalValue);

		TestCaseSyncMgr.getInstance().setSignal(signalId, signalValue);

		return true;
		// }}*Handler implementation*
	}

	// {{*Custom methods*
	// }}*Custom methods*
}
