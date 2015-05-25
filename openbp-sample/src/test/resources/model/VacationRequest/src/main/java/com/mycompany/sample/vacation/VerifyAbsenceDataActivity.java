package com.mycompany.sample.vacation;

import org.openbp.server.handler.Handler;
import org.openbp.server.handler.HandlerContext;

// {{*Custom imports*
// }}*Custom imports*

/**
 * null.
 * Implementation of the null activity handler.
 * 
 * Input sockets/parameter:
 *   Socket 'In'
 *     Parameter 'Data'
 * 
 * Output sockets/parameter:
 *   Socket 'Invalid'
 *     Parameter 'ErrMsg'
 *   Socket 'Out'
 */
public class VerifyAbsenceDataActivity
	// {{*Custom extends*
	// }}*Custom extends*
	// {{*Custom interfaces*
	implements Handler
	// }}*Custom interfaces*
{
	/** Output socket Invalid */
	private static final String SOCKET_INVALID = "Invalid";

	/** Parameter Data */
	private static final String PARAM_DATA = "Data";

	/** Parameter ErrMsg */
	private static final String PARAM_ERRMSG = "ErrMsg";

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
		/* Input parameter access code
		VacationData data = (VacationData) hc.getParam(PARAM_DATA);
		*/

		/* Output parameter access code
		String errMsg;
		hc.setResult(PARAM_ERRMSG, errMsg);
		*/

		// {{*Handler implementation*
		return true;
		// }}*Handler implementation*
	}

	// {{*Custom methods*
	// }}*Custom methods*
}

