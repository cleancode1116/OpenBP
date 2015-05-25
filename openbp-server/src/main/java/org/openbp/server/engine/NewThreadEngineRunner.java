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

import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextService;

/**
 * Engine runner class that creates a new thread for each token context execution.
 *
 * @author Author: Heiko Erhardt
 */
public class NewThreadEngineRunner extends EngineRunner
{
	/**
	 * Default constructor.
	 */
	public NewThreadEngineRunner()
	{
	}

	/**
	 * Executes token contexts that are ready for execution in a different thread.
	 * The method will query the executable contexts using the token context service.
	 * Each context retrieved will be executed using the thread distribution
	 * strategy of the particular {@link EngineRunner} implementation.
	 * The method returns after it has processed (i. e. distributed to the threads)
	 * the last element of the context list returned by the
	 * {@link TokenContextService#getExecutableContexts} method.
	 * Since the execution will be asynchronously, this does not mean that the contexts
	 * have finished their execution at this point in time.
	 * @return The number of contexts that have been retrieved for execution and passed to the thread pool
	 */
	public int executePendingContextsInDifferentThread()
	{
		if (getNumberOfExecutingContexts() < getFetchSize())
			return super.executePendingContextsInDifferentThread();
		return 0;
	}

	/**
	 * Tries to run the given context
	 *
	 * @param context Context to execute
	 * @return true if the calling method may proceed to the next context,
	 * false to stop iterating the available contexts
	 * (e. g. since the execution thread pool is currently full).
	 */
	protected boolean runContext(TokenContext context)
	{
		if (getNumberOfExecutingContexts() < getFetchSize())
		{
			Runnable runnable = prepareEngineRunnable(context);
			if (runnable != null)
			{
				Thread t = new Thread(runnable);
				t.start();

				return true;
			}
		}

		return false;
	}
}
