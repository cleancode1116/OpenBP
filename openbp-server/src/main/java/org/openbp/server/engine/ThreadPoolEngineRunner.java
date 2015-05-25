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

import java.util.concurrent.RejectedExecutionException;

import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextService;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Engine runner class that uses a thread pool for the token context execution.
 * This engine runner will use the Spring Framework's ThreadPoolTaskExecutor class
 * to execute the pending contexts using a thread pool.
 * You may configure the thread pool's properties in the OpenBP Spring configuration file.
 * For a description of its properties, see the
 * <a href="http://static.springframework.org/spring/docs/2.0.x/api/org/springframework/scheduling/concurrent/ThreadPoolTaskExecutor.html">Spring Framework ThreadPoolTaskExecutor class</a>.
 *
 * @author Heiko Erhardt
 */
public class ThreadPoolEngineRunner extends EngineRunner
{
	/** Executor used to execute the pending contexts */
	private ThreadPoolTaskExecutor executor;

	/**
	 * Default constructor.
	 */
	public ThreadPoolEngineRunner()
	{
	}

	/**
	 * Tries to run the given context.
	 *
	 * @param context Context to execute
	 * @return true if the calling method may proceed to the next context,
	 * false to stop iterating the available contexts
	 * (e. g. since the execution thread pool is currently full).
	 */
	protected boolean runContext(TokenContext context)
	{
		boolean rejected = true;
		int previousLifecycleState = context.getLifecycleState();
		try
		{
			Runnable runnable = prepareEngineRunnable(context);
			if (runnable != null)
			{
				getExecutor().execute(runnable);
				rejected = false;
			}
		}
		catch (RejectedExecutionException e)
		{
			// Ok, expected exception
		}
		catch (TaskRejectedException e)
		{
			// Ok, expected exception
		}
		if (rejected)
		{
			// Thread pool rejects any execution, so reset the state of the context
			getEngine().changeTokenState(context, previousLifecycleState, context.getLifecycleRequest());
			TokenContextService tcs = getEngine().getTokenContextService();
			tcs.saveContext(context);
			tcs.commit();
		}

		return ! rejected;
	}

	/**
	 * Gets the executor used to execute the pending contexts.
	 * @nowarn
	 */
	public ThreadPoolTaskExecutor getExecutor()
	{
		return executor;
	}

	/**
	 * Sets the executor used to execute the pending contexts.
	 * @nowarn
	 */
	public void setExecutor(final ThreadPoolTaskExecutor executor)
	{
		this.executor = executor;
	}
}
