/*
 *   Copyright 2008 skynamics AG
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
package org.openbp.server.engine;

import org.openbp.common.logger.LogUtil;
import org.openbp.core.CoreConstants;
import org.openbp.server.context.LifecycleRequest;
import org.openbp.server.context.LifecycleState;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextService;

/**
 * Runnable implementation for the EngineRunner class.
 *
 * @author Heiko Erhardt
 */
public class EngineRunnable
	implements Runnable
{
	/** Engine runner */
	private final EngineRunner engineRunner;

	/** Context id */
	private final Object contextId;

	/**
	 * Constructor.
	 *
	 * @param engineRunner Engine runner
	 * @param contextId Context id
	 */
	public EngineRunnable(EngineRunner engineRunner, Object contextId)
	{
		this.engineRunner = engineRunner;
		this.contextId = contextId;
	}

	/**
	 * Run method.
	 */
	public void run()
	{
		TokenContextService tokenContextService = engineRunner.getEngine().getTokenContextService();
		TokenContext context = tokenContextService.getContextById(contextId);

		String oldThreadName = null;
		try
		{
			engineRunner.increaseNumberOfExecutingContexts();

			// Note that the Runnable might be invoked at a time when this same context
			// has already been processed or deleted.
			// Check if the context still exists and if its request and state are ok.
			if (context == null)
			{
				LogUtil.debug(getClass(), "Trying to run non-existing context with id $0.", contextId);
				return;
			}
			if (context.getLifecycleRequest() != LifecycleRequest.RESUME)
			{
				LogUtil.debug(getClass(), "Trying to run context that does not have a resumption request: $0.", context);
				return;
			}
			if (context.getLifecycleState() != LifecycleState.SELECTED)
			{
				LogUtil.debug(getClass(), "Trying to run context that has not been selected for execution: $0.", context);
				return;
			}

			// Set the name of the executing thread: Either process variable value or short name
			Object objThreadName = context.getProcessVariableValue(CoreConstants.PROCESSVAR_THREAD_NAME);
			if (objThreadName != null)
			{
				String threadName = objThreadName.toString();
				oldThreadName = Thread.currentThread().getName();
				Thread.currentThread().setName("Context execution (" + threadName + ")");
			}

			engineRunner.getEngine().executeContext(context);
		}
		catch (Throwable t)
		{
			boolean handled = false;
			EngineRunnerExceptionHandler handler = engineRunner.getEngineRunnerExceptionHandler();
			if (handler != null)
			{
				handled = handler.handleException(context, t);
			}

			if (! handled)
			{
				LogUtil.error(getClass(), "Unhandled error occured while executing a process.", t);
			}
		}
		finally
		{
			if (oldThreadName != null)
			{
				Thread.currentThread().setName(oldThreadName);
			}

			engineRunner.decreaseNumberOfExecutingContexts();
			tokenContextService.clearCache();
		}
	}

	/**
	 * Gets the engine runner.
	 * @nowarn
	 */
	public EngineRunner getEngineRunner()
	{
		return engineRunner;
	}

	/**
	 * Gets the context id.
	 * @nowarn
	 */
	public Object getContextId()
	{
		return contextId;
	}
};
