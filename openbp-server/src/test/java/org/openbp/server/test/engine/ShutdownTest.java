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
package org.openbp.server.test.engine;

import java.util.ArrayList;

import org.openbp.server.context.TokenContext;
import org.openbp.server.test.base.TestCaseBase;
import org.openbp.server.test.base.TestCaseSyncMgr;

/**
 * Test case that tests the capabilities of the ThreadPoolTaskExecutor to run a particular number of threads in parallel w/o queuing them.
 *
 * @author Heiko Erhardt
 */
public class ShutdownTest extends TestCaseBase
{
	// We run 3 processes in parallel
	public static final int NUM_THREADS = 2;

	public ShutdownTest()
	{
	}

	public void performTest()
		throws Exception
	{
		setThreadPoolSize(NUM_THREADS);

		// Start executor
		new Thread()
		{
			public void run()
			{
				getProcessFacade().mainExecutionLoop(500);
			}
		}.start();

		// Start 3 processes that consist of a signal wait activity only
		// and make them wait for the signal id "Sync".
		final ArrayList contextList = new ArrayList();
		for (int i = 0; i < NUM_THREADS; ++i)
		{
			TokenContext tc = startSimpleSignalWaitProcess("Signal" + i, 0);
			contextList.add(tc);
		}

		// In 3 seconds, we will send the signal to the processes
		new Thread()
		{
			public void run()
			{
				try
				{
					Thread.sleep(3000L);
				}
				catch (InterruptedException e)
				{
				}

				// Make all processes continue by setting the desired signal
				for (int i = 0; i < NUM_THREADS; ++i)
				{
					TestCaseSyncMgr.getInstance().setSignal(null, "Signal" + i, "Done");
				}
			}
		}.start();

		// Wait max. 10 sec. The server should have been terminated normally then
		boolean ret = getProcessFacade().waitForStop(20000L);
		assertEquals(true, ret);
	}
}
