package org.openbp.server.engine;

import java.util.Iterator;

import org.openbp.common.logger.LogUtil;
import org.openbp.server.context.LifecycleRequest;
import org.openbp.server.context.LifecycleState;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextService;

/**
 * The task of the engine runner is to scan for tasks that are ready to execute and invoke the process engine on them.
 * Derived classes of this base class may provide strategies that distribute the processes that are due for
 * execution to a number of threads.
 *
 * Note that all execution methods of this class keep track of currently executing contexts,
 * allowing to determine the overall state of the process engine system concerning context execution.
 * For details, see the {@link #getNumberOfExecutingContexts} and {@link #waitForStop} methods.
 *
 * @author Heiko Erhardt
 */
public abstract class EngineRunner
{
	/** Engine */
	private Engine engine;

	/** Engine runner exception handler */
	private EngineRunnerExceptionHandler engineRunnerExceptionHandler;

	/** System name provider */
	private SystemNameProvider systemNameProvider = new DefaultSystemNameProvider();

	/** Number of executing contexts */
	private int numberOfExecutingContexts;

	private Object numberSemaphore = new Object();

	/** Maximum number of records to fetch in each iteration of the main loop or 0 for all */
	private int fetchSize;

	/** Idle time for {@link #mainExecutionLoop()} in milli seconds When there are no contexts available for execution */
	private int idleTime;

	/** Flag if the execution loop shall be stopped */
	private boolean executionLoopStopRequested;

	/** Flag if the main execution loop is currently running */
	private boolean executionLoopRuning;

	/**
	 * Default constructor.
	 */
	public EngineRunner()
	{
	}

	/**
	 * Main execution loop.
	 * Use this method in the thread that reads pending token contexts and distributes them for execution.
	 * The method returns after the {@link #requestExecutionLoopEnd} has been called and the {@link #setIdleTime} has elapsed.
	 */
	public void mainExecutionLoop()
	{
		mainExecutionLoop(idleTime);
	}

	/**
	 * Main execution loop.
	 * Use this method in the thread that reads pending token contexts and distributes them for execution.
	 * The method returns after the {@link #requestExecutionLoopEnd} has been called and the sleepTime has elapsed.
	 * @param sleepTime When there are no contexts available for execution,
	 * the method will sleep for the supplied sleep time in milli seconds.
	 */
	public void mainExecutionLoop(final int sleepTime)
	{
		try
		{
			executionLoopRuning = true;

			while (! executionLoopStopRequested)
			{
				executePendingContextsInDifferentThread();

				// This will clear the o/r mapper cache, so we won't have caching problems.
				getEngine().getTokenContextService().clearCache();

				try
				{
					Thread.sleep(sleepTime);
				}
				catch (InterruptedException e)
				{
				}
			}
		}
		finally
		{
			executionLoopRuning = false;
		}
	}

	/**
	 * Checks if the main execution loop is currently running.
	 * @nowarn
	 */
	public boolean isExecutionLoopRuning()
	{
		return executionLoopRuning;
	}

	/**
	 * Requests the end of the main execution loop.
	 * The method will request the end of the main execution loop.
	 * Use the {@link #waitForStop} method to check if execution has ended.
	 */
	public void requestExecutionLoopEnd()
	{
		if (isExecutionLoopRuning())
		{
			executionLoopStopRequested = true;
		}
	}

	/**
	 * Tries to revoke a requested termination of the main execution loop.
	 * Use this method to cancel a pending termination of the main execution loop caused by a call to {@link #requestExecutionLoopEnd}.
	 * You may check if the execution loop is still running by calling the method {@link #isExecutionLoopRuning}.
	 */
	public void revokeExecutionLoopEnd()
	{
		executionLoopStopRequested = false;
	}

	// Check all 100 ms for execution stop
	private static final long STOP_CHECK_INTERVAL = 100L;

	/**
	 * Requests the end of the main execution loop and wait until all currently
	 * executing contexts have come to an halt.
	 * The method will return if either all contexts have finished executing
	 * (i. e. reached a wait state or have ended) or if the specified timeout has elapsed.
	 *
	 * @param timeoutMS Timeout in milliseconds.
	 * If this value is 0, the method will just check if everything has completed, but will not wait for any processes.
	 * If this value is -1, no timeout will apply (i. e. the method will definately wait
	 * until all context executions have finished).
	 * @return true If no context is currently executing, false if the timeout has elapsed
	 */
	public boolean waitForStop(long timeoutMS)
	{
		// Make sure we request the end of the execution loop
		requestExecutionLoopEnd();

		// Poll for end of execution
		long remaining = timeoutMS;
		for (;;)
		{
			if (! isExecutionLoopRuning() && getNumberOfExecutingContexts() == 0)
				return true;

			if (timeoutMS >= 0)
			{
				// Timeout check
				if (remaining <= 0)
				{
					// Timeout elapsed
					break;
				}

				try
				{
					long sleepTime = STOP_CHECK_INTERVAL;
					if (sleepTime > remaining)
						sleepTime = remaining;
					remaining -= sleepTime;

					Thread.sleep(sleepTime);
				}
				catch (InterruptedException e)
				{
				}
			}
		}

		return false;
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
		TokenContextService tcs = getEngine().getTokenContextService();
		int nAcceptedContexts = 0;

		try
		{
			for (Iterator it = tcs.getExecutableContexts(fetchSize); it.hasNext() && ! executionLoopStopRequested;)
			{
				TokenContext context = (TokenContext) it.next();

				if (! runContext(context))
					break;

				++nAcceptedContexts;
			}
		}
		finally
		{
			// Reset the token context service
			tcs.rollback();
		}

		return nAcceptedContexts;
	}

	/**
	 * Executes token contexts that are ready for execution immediately in this thread.
	 * The method will query the executable contexts using the token context service.
	 * Each context retrieved will be executed directly.
	 * The method returns after it has processed (i. e. executed)
	 * the last element of the context list returned by the
	 * {@link TokenContextService#getExecutableContexts} method.
	 * Since this will block the thread, this method is intended for test cases primarily.
	 * @return true if the method has found contexts for execution, false otherwise
	 */
	public boolean executePendingContextsInThisThread()
	{
		boolean found = false;

		try
		{
			// If an exception happens during context execution,
			// we will leave the loop and check for executable contexts again the next time.
			for (Iterator it = getEngine().getTokenContextService().getExecutableContexts(0); it.hasNext() && ! executionLoopStopRequested;)
			{
				TokenContext context = (TokenContext) it.next();
				found = true;

				executeContextInThisThread(context);
			}
		}
		catch (RuntimeException e)
		{
			getEngine().getTokenContextService().rollback();
			throw e;
		}
		finally
		{
			getEngine().getTokenContextService().commit();
		}

		return found;
	}

	/**
	 * Executes the given context immediately in this thread.
	 * Since this will block the thread, this method is intended for test cases and for situations where
	 * the completion of a process execution is needed in order to continue the program.
	 *
	 * @param context Context to execute
	 */
	public void executeContextInThisThread(TokenContext context)
	{
		try
		{
			increaseNumberOfExecutingContexts();

			if (context.getLifecycleRequest() != LifecycleRequest.RESUME)
			{
				LogUtil.debug(getClass(), "Trying to run context that does not have a resumption request: $0.", context);
				return;
			}

			// We don't commit here because Engine.executeContext will do this promptly for us, so no need for two successive commits.
			engine.changeTokenState(context, LifecycleState.SELECTED, context.getLifecycleRequest());
			context.setNodeId(systemNameProvider.getSystemName());

			getEngine().executeContext(context);
		}
		finally
		{
			decreaseNumberOfExecutingContexts();
		}
	}

	/**
	 * Tries to run the given context
	 *
	 * @param context Context to execute
	 * @return true if the calling method may proceed to the next context,
	 * false to stop iterating the available contexts
	 * (e. g. since the execution thread pool is currently full).
	 */
	protected abstract boolean runContext(TokenContext context);

	/**
	 * Prepares a runnable object that can be used to execute a token context.
	 * The method will also the the lifecycle state of the context to {@link LifecycleState#SELECTED}.
	 *
	 * @param context Context to execute
	 * @return The new EngineRunnable object that refers the given context or null if an engine event observer
	 * of the vetoed the context execution 
	 */
	protected EngineRunnable prepareEngineRunnable(TokenContext context)
	{
		if (engine.hasActiveObservers(CancelableEngineEvent.SHALL_EXECUTE_TOKEN, context))
		{
			CancelableEngineEvent event = new CancelableEngineEvent(CancelableEngineEvent.SHALL_EXECUTE_TOKEN, context, engine);
			engine.fireEngineEvent(event);
			if (event.isCanceled())
			{
				// Event observer vetoed context execution
				return null;
			}
		}

		// Update the state of the context
		engine.changeTokenState(context, LifecycleState.SELECTED, context.getLifecycleRequest());
		context.setNodeId(systemNameProvider.getSystemName());

		TokenContextService tcs = getEngine().getTokenContextService();
		tcs.saveContext(context);
		tcs.commit();

		// Run it using the engine thread pool
		Object id = context.getId();
		EngineRunnable runnable = new EngineRunnable(this, id);
		return runnable;
	}

	/**
	 * Gets the maximum number of records to fetch in each iteration of the main loop or 0 for all.
	 * @nowarn
	 */
	public int getFetchSize()
	{
		return fetchSize;
	}

	/**
	 * Sets the maximum number of records to fetch in each iteration of the main loop or 0 for all.
	 * @nowarn
	 */
	public void setFetchSize(int fetchSize)
	{
		this.fetchSize = fetchSize;
	}

	/**
	 * Gets the idle time for {@link #mainExecutionLoop()} in milli seconds When there are no contexts available for execution.
	 * @nowarn
	 */
	public int getIdleTime()
	{
		return idleTime;
	}

	/**
	 * Sets the idle time for {@link #mainExecutionLoop()}  in milli seconds When there are no contexts available for execution.
	 * @nowarn
	 */
	public void setIdleTime(int idleTime)
	{
		this.idleTime = idleTime;
	}

	/**
	 * Gets the number of executing contexts.
	 * @nowarn
	 */
	public int getNumberOfExecutingContexts()
	{
		synchronized(numberSemaphore)
		{
			return numberOfExecutingContexts;
		}
	}

	/**
	 * Increases the number of currently executing contexts.
	 * Usually, you should not call this method from the application code!
	 */
	public void increaseNumberOfExecutingContexts()
	{
		synchronized(numberSemaphore)
		{
			++numberOfExecutingContexts;
		}
	}

	/**
	 * Decreases the number of currently executing contexts.
	 * Usually, you should not call this method from the application code!
	 */
	public void decreaseNumberOfExecutingContexts()
	{
		synchronized(numberSemaphore)
		{
			--numberOfExecutingContexts;
		}
	}

	/**
	 * Gets the engine.
	 * @nowarn
	 */
	public Engine getEngine()
	{
		return engine;
	}

	/**
	 * Sets the engine.
	 * @nowarn
	 */
	public void setEngine(final Engine engine)
	{
		this.engine = engine;
	}

	/**
	 * Gets the engine runner exception handler.
	 * @nowarn
	 */
	public EngineRunnerExceptionHandler getEngineRunnerExceptionHandler()
	{
		return engineRunnerExceptionHandler;
	}

	/**
	 * Sets the engine runner exception handler.
	 * @nowarn
	 */
	public void setEngineRunnerExceptionHandler(EngineRunnerExceptionHandler engineRunnerExceptionHandler)
	{
		this.engineRunnerExceptionHandler = engineRunnerExceptionHandler;
	}

	/**
	 * Gets the system name provider.
	 * @nowarn
	 */
	public SystemNameProvider getSystemNameProvider()
	{
		return systemNameProvider;
	}

	/**
	 * Sets the system name provider.
	 * @nowarn
	 */
	public void setSystemNameProvider(SystemNameProvider systemNameProvider)
	{
		this.systemNameProvider = systemNameProvider;
	}
}
