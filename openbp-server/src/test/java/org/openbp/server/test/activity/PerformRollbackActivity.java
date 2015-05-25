package org.openbp.server.test.activity;

import org.openbp.common.logger.LogUtil;
import org.openbp.core.engine.EngineException;
import org.openbp.core.model.item.process.RollbackDataBehavior;
import org.openbp.core.model.item.process.RollbackPositionBehavior;
import org.openbp.server.engine.StandardRollbackProcessor;
import org.openbp.server.handler.Handler;
import org.openbp.server.handler.HandlerContext;

// {{*Custom imports*
// }}*Custom imports*

/**
 * Perform rollback.
 * Implementation of the PerformRollback activity handler.
 * Activity that calls the method HandlerContext.performRollback within the activity code.
 * The parameters of the method determine the rollback data and position behavior.
 * 
 * Input sockets/parameter:
 *   Socket 'In'
 *     Parameter 'RollbackDataBehavior': Rollback data behavior
 *     Parameter 'RollbackPositionBehavior': Rollback position behavior
 * 
 * Output sockets/parameter:
 *   Socket 'Out'
 */
public class PerformRollbackActivity
	// {{*Custom extends*
	// }}*Custom extends*
	// {{*Custom interfaces*
	implements Handler
	// }}*Custom interfaces*
{
	/** Parameter RollbackDataBehavior */
	private static final String PARAM_ROLLBACKDATABEHAVIOR = "RollbackDataBehavior";

	/** Parameter RollbackPositionBehavior */
	private static final String PARAM_ROLLBACKPOSITIONBEHAVIOR = "RollbackPositionBehavior";

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
		int rollbackDataBehavior = RollbackDataBehavior.UPDATE_VARIABLES;
		String rollbackDataBehaviorStr = (String) hc.getParam(PARAM_ROLLBACKDATABEHAVIOR);
		if ("UpdateVariables".equals(rollbackDataBehaviorStr))
		{
			rollbackDataBehavior = RollbackDataBehavior.UPDATE_VARIABLES;
		}
		else if ("AddVariables".equals(rollbackDataBehaviorStr))
		{
			rollbackDataBehavior = RollbackDataBehavior.ADD_VARIABLES;;
		}
		else if ("RestoreVariables".equals(rollbackDataBehaviorStr))
		{
			rollbackDataBehavior = RollbackDataBehavior.RESTORE_VARIABLES;;
		}
		else
		{
			String msg = LogUtil.error(getClass(), "Invalid $0 activity argument (argument value $1, token $2).", PARAM_ROLLBACKDATABEHAVIOR, rollbackDataBehaviorStr, hc.getTokenContext());
			throw new EngineException("InvalidActivityArgument", msg);
		}

		String rollbackPositionBehaviorStr = (String) hc.getParam(PARAM_ROLLBACKPOSITIONBEHAVIOR);
		int rollbackPositionBehavior = RollbackPositionBehavior.MAINTAIN_POSITION;;
		if ("MaintainPosition".equals(rollbackPositionBehaviorStr))
		{
			rollbackPositionBehavior = RollbackPositionBehavior.MAINTAIN_POSITION;;
		}
		else if ("RestorePosition".equals(rollbackPositionBehaviorStr))
		{
			rollbackPositionBehavior = RollbackPositionBehavior.RESTORE_POSITION;;
		}
		else
		{
			String msg = LogUtil.error(getClass(), "Invalid $0 activity argument (argument value $1, token $2).", PARAM_ROLLBACKPOSITIONBEHAVIOR, rollbackPositionBehaviorStr, hc.getTokenContext());
			throw new EngineException("InvalidActivityArgument", msg);
		}

		StandardRollbackProcessor rollbackProcessor = new StandardRollbackProcessor();
		rollbackProcessor.setRollbackDataBehavior(rollbackDataBehavior);
		rollbackProcessor.setRollbackPositionBehavior(rollbackPositionBehavior);
		rollbackProcessor.performRollback(hc);

		return true;
		// }}*Handler implementation*
	}

	// {{*Custom methods*
	// }}*Custom methods*
}

