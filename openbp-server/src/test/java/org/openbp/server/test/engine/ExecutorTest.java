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
public class ExecutorTest extends TestCaseBase
{
	// We run 3 processes in parallel
	public static final int NUM_THREADS = 3;

	public ExecutorTest()
	{
	}

	public void performTest()
		throws Exception
	{
		// Set thread pool capacity to number of threads minus 1 (= 2)
		setThreadPoolSize(NUM_THREADS - 1);

		// Start 3 processes that consist of a signal wait activity only
		// and make them wait for the signal id "Sync".
		ArrayList contextList = new ArrayList();
		for (int i = 0; i < NUM_THREADS; ++i)
		{
			TokenContext tc = startSimpleSignalWaitProcess("Sync" + i, 0);
			contextList.add(tc);
		}

		// Thread pool capacity is 2, so the executor should pick exactly 2 processes for execution.
		int n = getProcessFacade().executePendingContextsInDifferentThread();
		assertEquals(NUM_THREADS - 1, n);

		// Make all processes continue by setting the desired signal
		for (int i = 0; i < NUM_THREADS; ++i)
		{
			TestCaseSyncMgr.getInstance().setSignal(null, "Sync" + i, "Done");
		}

		try
		{
			Thread.sleep(2000);
		}
		catch (InterruptedException e)
		{
		}

		// There should be one process remaining, which should be found now by another executor call.
		n = getProcessFacade().executePendingContextsInDifferentThread();
		assertEquals(1, n);
	}
}
