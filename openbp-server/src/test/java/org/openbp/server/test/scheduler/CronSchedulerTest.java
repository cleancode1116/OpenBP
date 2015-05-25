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

import org.openbp.server.scheduler.ProcessJobDescriptor;
import org.openbp.server.scheduler.QuartzProcessScheduler;
import org.openbp.server.test.base.TestCaseBase;
import org.openbp.server.test.base.TestCaseSyncMgr;
import org.quartz.CronTrigger;
import org.quartz.Trigger;

/**
 * 
 *
 * @author Heiko Erhardt
 */
public class CronSchedulerTest extends TestCaseBase
{
	private static final String STARTREF = "/TestCase/CronSchedulerTest.Start";

	/** Scheduler */
	private QuartzProcessScheduler scheduler;

	public CronSchedulerTest()
	{
	}

	public void performTest()
		throws Exception
	{
		scheduler = (QuartzProcessScheduler) getProcessServer().getProcessScheduler();

		ProcessJobDescriptor job1 = new ProcessJobDescriptor();
		job1.setJobName("job1");
		job1.setJobGroup("SchedulerTest");
		job1.setPositionRef(STARTREF);

		// For this test case, we will execute the job directly within the job handler.
		// In an application, you should rather use the asynchronous execution mode using the execution thread pool.
		job1.setExecutionMode(ProcessJobDescriptor.EXECUTION_MODE_SYNCHRONOUS);

		TestCaseSyncMgr.getInstance().setSignal("Counter", Integer.valueOf(0));

		// Fire trigger each 5 seconds
		Trigger t = new CronTrigger("CronTestTrigger", "SchedulerTest", "0/5 * * * * ?");
		scheduler.scheduleProcess(job1, t);

		try
		{
			for (int i = 0;; ++i)
			{
				if (i > 20)
				{
					fail("Timeout for scheduler job reached.");
				}

				Integer signalValue = (Integer) TestCaseSyncMgr.getInstance().getSignal(null, "Counter");
				if (signalValue != null)
				{
					if (signalValue.intValue() == 3)
						break;
				}

				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
				}
			}
		}
		finally
		{
			scheduler.getQuartzInstance().unscheduleJob("CronTestTrigger", "SchedulerTest");
		}
	}
}
