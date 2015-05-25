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
package org.openbp.server.test.scheduler;

import java.util.Date;
import java.util.HashMap;

import org.openbp.server.context.TokenContext;
import org.openbp.server.scheduler.ProcessJobDescriptor;
import org.openbp.server.scheduler.QuartzProcessScheduler;
import org.openbp.server.test.base.TestCaseBase;
import org.openbp.server.test.base.TestCaseSyncMgr;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

/**
 * 
 *
 * @author Heiko Erhardt
 */
public class SchedulerTest extends TestCaseBase
{
	private static final String STARTREF = "/TestCase/SchedulerTest.Start";

	private static final String SIGNAL1 = "Signal 1";

	private static final String SIGNAL2 = "Signal 2";

	public static final String TEST_PARAM_VALUE = "TestParam";

	/** Scheduler */
	private QuartzProcessScheduler scheduler;

	public SchedulerTest()
	{
	}

	public void performTest()
		throws Exception
	{
		scheduler = (QuartzProcessScheduler) getProcessServer().getProcessScheduler();

		TokenContext tc = createToken();
		getProcessServer().getProcessFacade().prepareTokenForScheduler(tc);
		getProcessServer().getProcessFacade().commitTokenContextTransaction();

		ProcessJobDescriptor job1 = new ProcessJobDescriptor();
		job1.setJobName("job1");
		job1.setJobGroup("SchedulerTest");
		job1.setTokenContext(tc);
		job1.setPositionRef(STARTREF);

		// For this test case, we will execute the job directly within the job handler.
		// In an application, you should rather use the asynchronous execution mode using the execution thread pool.
		job1.setExecutionMode(ProcessJobDescriptor.EXECUTION_MODE_SYNCHRONOUS);

		HashMap inputValues = new HashMap();
		inputValues.put("StringParam", TEST_PARAM_VALUE);
		job1.setInputValues(inputValues);

		// Start in 5 sec.
		Trigger t1 = new SimpleTrigger("trigger1", "SchedulerTest");
		long currentTime = System.currentTimeMillis();
		Date d1 = new Date(currentTime + 5000L);
		t1.setStartTime(d1);

		scheduler.scheduleProcess(job1, t1);

		// Wait 10 sec
		Object v1 = wait(SIGNAL1, 10);
		assertEquals(TEST_PARAM_VALUE, v1);

		// The process has been executed as desired. The execution of the process stopped
		// at the wait state node.

		// The next job will resume the workflow from its current position.
		ProcessJobDescriptor job2 = new ProcessJobDescriptor();
		job2.setJobName("job2");
		job2.setJobGroup("SchedulerTest");
		job2.setTokenContext(tc);
		job2.setPositionRef("Continue");
		job2.setStartMode(ProcessJobDescriptor.START_MODE_RESUME);

		// For this test case, we will execute the job directly within the job handler.
		// In an application, you should rather use the asynchronous execution mode using the execution thread pool.
		job2.setExecutionMode(ProcessJobDescriptor.EXECUTION_MODE_SYNCHRONOUS);

		// Again, 5 sec. delay
		Trigger t2 = new SimpleTrigger("trigger2", "SchedulerTest");
		currentTime = System.currentTimeMillis();
		Date d2 = new Date(currentTime + 5000L);
		t2.setStartTime(d2);

		scheduler.scheduleProcess(job2, t2);

		// Wait 10 sec
		Object v2 = wait(SIGNAL2, 10);
		assertEquals(TEST_PARAM_VALUE, v2);
	}

	protected Object wait(String signalId, int sec)
	{
		Object signalValue = TestCaseSyncMgr.getInstance().receiveSignal(null, signalId, sec);
		if (signalValue == null)
		{
			fail("Timeout while waiting for signal " + signalId);
		}
		return signalValue;
	}
}
