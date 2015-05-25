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
package org.openbp.server.test.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import junit.framework.TestCase;

import org.openbp.common.application.Application;
import org.openbp.common.logger.LogUtil;
import org.openbp.core.model.Model;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.server.ProcessFacade;
import org.openbp.server.ProcessServer;
import org.openbp.server.ProcessServerFactory;
import org.openbp.server.ServerConstants;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.WorkflowTask;
import org.openbp.server.context.WorkflowTaskCriteria;
import org.openbp.server.engine.ThreadPoolEngineRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


/**
 * Base class for workflow test cases.
 *
 * @author Heiko Erhardt
 */
public abstract class TestCaseBase extends TestCase
{
	/** Process server */
	private static ProcessServer processServer;

	/** Name of the OpenBP spring configuration file */
	private static String springConfigFileName;

	/** Engine event counter */
	private static EngineEventCounter engineEventCounter = new EngineEventCounter();

	/** Table used for thread synchronisations in test cases */
	private final Hashtable threadSignals = new Hashtable();

	/** Level */
	private static int initLevel;

	/*
	private Lock lock = new ReentrantLock();
	 */

	public TestCaseBase()
	{
	}

	public TokenContext createToken()
	{
		TokenContext token = getProcessFacade().createToken();
		token.setDebuggerId("Deb1");
		return token;
	}

	public void testMain()
		throws Exception
	{
		initializeTest();
		performTest();
		shutdownTest();
	}

	public void performTest()
		throws Exception
	{
	}

	public void initializeTest()
		throws Exception
	{
		LogUtil.info(getClass(), "*** Beginning test case " + getClass().getName());

		basicInitialize();
	}

	public void shutdownTest()
	{
		if (--initLevel == 0)
		{
			processServer.shutdown(false);
			processServer = null;
		}
	}

	public static void basicInitialize()
	{
		++initLevel;
		if (processServer == null)
		{
			Application.setArguments(null);

			// Create a new process server
			if (springConfigFileName == null)
			{
				// Do not use SettingUtil here, not yet initialized!
				springConfigFileName = System.getProperty(ServerConstants.SYSPROP_SPRING_CONFIG_FILE);
			}
			processServer = new ProcessServerFactory().createProcessServer(springConfigFileName);

			// Register the event counter for all event types
			processServer.getEngine().registerObserver(engineEventCounter, null);
		}
	}

	/**
	 * Gets the process server.
	 * @nowarn
	 */
	public ProcessServer getProcessServer()
	{
		return processServer;
	}

	/**
	 * Gets the process server.
	 * @nowarn
	 */
	public ProcessFacade getProcessFacade()
	{
		return processServer.getProcessFacade();
	}

	/**
	 * Gets the name of the OpenBP spring configuration file.
	 * @nowarn
	 */
	public String getSpringConfigFileName()
	{
		return springConfigFileName;
	}

	/**
	 * Sets the name of the OpenBP spring configuration file.
	 * @nowarn
	 */
	public void setSpringConfigFileName(String springConfigFileNameArg)
	{
		springConfigFileName = springConfigFileNameArg;
	}

	/**
	 * Gets the engine event counter.
	 * @nowarn
	 */
	public EngineEventCounter getEngineEventCounter()
	{
		return engineEventCounter;
	}

	/**
	 * Checks the current node.
	 * @param context Token context
	 * @param expectedCurrentNode Unqualified name of the expected current node
	 */
	public void assertCurrentNode(TokenContext context, String expectedCurrentNode)
	{
		NodeSocket socket = context.getCurrentSocket();
		if (socket == null)
		{
			fail("Current socket is null.");
		}
		else
		{
			String name = socket.getNode().getName();
			if (! expectedCurrentNode.equals(name))
			{
				fail("Current node = '" + name + "', should be '" + expectedCurrentNode + "'.");
			}
		}
	}

	/**
	 * Gets a list of all enabled workflow tasks that operate on the given model.
	 *
	 * @param model Model to check
	 * @return Collection of {@link WorkflowTask} objects
	 */
	public Collection getWorkflowTasks(Model model)
	{
		WorkflowTaskCriteria criteria = new WorkflowTaskCriteria();
		criteria.setModel(model);
		criteria.setStatus(WorkflowTask.STATUS_ENABLED);

		Iterator it = getProcessFacade().getworkflowTasks(criteria);

		ArrayList ret = new ArrayList();
		while (it.hasNext())
		{
			ret.add(it.next());
		}

		return ret;
	}

	protected void startSignal(String sigId)
	{
		/*
		Condition cond = lock.newCondition();
		threadSignals.put(sigId, cond);
		 */
	}

	protected void waitForSignal(String sigId)
	{
		for (;;)
		{
			Object o = threadSignals.get(sigId);
			if (o != null)
				break;
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
			}
		}
		/*
		Condition cond = (Condition) threadSignals.get(sigId);
		if (cond == null)
		{
			throw new RuntimeException("Unknown test case signal '" + sigId + "'.");
		}
		try
		{
			cond.await();
		}
		catch (InterruptedException e)
		{
		}
		 */
	}

	protected void signal(String sigId)
	{
		threadSignals.put(sigId, sigId);
		/*
		Condition cond = (Condition) threadSignals.get(sigId);
		if (cond == null)
		{
			throw new RuntimeException("Unknown test case signal '" + sigId + "'.");
		}
		cond.signalAll();
		 */
	}

	protected void executeWait(final String sigId, final Runnable r)
	{
		startSignal(sigId);
		new Thread()
		{
			public void run()
			{
				r.run();
				signal(sigId);
			}
		}.start();
		waitForSignal(sigId);

		/*
		lock.lock();
		try
		{
			startSignal(sigId);
			new Thread()
			{
				public void run()
				{
					r.run();
					signal(sigId);
				}
			}.start();
			waitForSignal(sigId);
		}
		finally
		{
			lock.unlock();
		}
		 */
	}

	protected void setThreadPoolSize(int n)
	{
		getProcessServer().getEngineRunner().setFetchSize(n);
		if (getProcessServer().getEngineRunner() instanceof ThreadPoolEngineRunner)
		{
			ThreadPoolTaskExecutor executor = ((ThreadPoolEngineRunner) getProcessServer().getEngineRunner()).getExecutor();
			executor.setCorePoolSize(n);
			executor.setMaxPoolSize(n);
			executor.setQueueCapacity(0);
			executor.initialize();
		}
	}

	// Standard process that consists of one 'wait for signal' activity and takes the signal id as parameter
	public static final String SIMPLE_SIGNAL_WAIT_PROCESS_STARTREF = "/TestCase/SimpleWaitProcess.Start";

	// Standard process that consists of one 'set signal' activity and takes the signal id and signal value as parameter
	public static final String SIMPLE_SIGNAL_SET_PROCESS_STARTREF = "/TestCase/SimpleSignalProcess.Start";

	/**
	 * Starts a processes that consist of a signal wait activity only and make it wait for the given signal id.
	 *
	 * @param signalId Signal id
	 * @param priority Desired process priority
	 * @return The token context of the new process
	 */
	public TokenContext startSimpleSignalWaitProcess(String signalId, int priority)
	{
		HashMap initialParams = new HashMap();
		initialParams.put("SignalId", signalId);

		TokenContext tc = createToken();
		tc.setPriority(priority);
		getProcessFacade().startToken(tc, SIMPLE_SIGNAL_WAIT_PROCESS_STARTREF, initialParams);
		return tc;
	}

	/**
	 * Starts a processes that consist of a set signal activity only and make it wait for the given signal id.
	 *
	 * @param signalId Signal id
	 * @param signalValue Signal value
	 * @param priority Desired process priority
	 * @return The token context of the new process
	 */
	public TokenContext startSimpleSignalSetProcess(String signalId, Object signalValue, int priority)
	{
		HashMap initialParams = new HashMap();
		initialParams.put("SignalId", signalId);
		initialParams.put("SignalValue", signalValue);

		TokenContext tc = createToken();
		tc.setPriority(priority);
		getProcessFacade().startToken(tc, SIMPLE_SIGNAL_SET_PROCESS_STARTREF, initialParams);
		return tc;
	}
}
