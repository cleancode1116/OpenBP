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
import org.quartz.CronTrigger;
import org.quartz.Trigger;

/**
 * 
 *
 * @author Heiko Erhardt
 */
public class SchedulerMgmtTest extends TestCaseBase
{
	private static final String STARTREF = "/TestCase/SchedulerTest.Start";

	/** Scheduler */
	private QuartzProcessScheduler scheduler;

	public SchedulerMgmtTest()
	{
	}

	public void performTest()
		throws Exception
	{
		scheduler = (QuartzProcessScheduler) getProcessServer().getProcessScheduler();
		scheduler.suspend();

		String [] groupNames = scheduler.getJobGroupNames();
		assertEquals(0, groupNames.length);

		ProcessJobDescriptor job1 = new ProcessJobDescriptor();
		job1.setJobName("job1");
		job1.setJobGroup("SchedulerMgmtTest");
		job1.setPositionRef(STARTREF);
		job1.setExecutionMode(ProcessJobDescriptor.EXECUTION_MODE_SYNCHRONOUS);
		Trigger t1 = new CronTrigger("CronTestTrigger1", "SchedulerMgmtTest", "0/5 * * * * ?");
		scheduler.scheduleProcess(job1, t1);

		ProcessJobDescriptor job2 = new ProcessJobDescriptor();
		job2.setJobName("job2");
		job2.setJobGroup("SchedulerMgmtTest");
		job2.setPositionRef(STARTREF);
		job2.setExecutionMode(ProcessJobDescriptor.EXECUTION_MODE_SYNCHRONOUS);
		Trigger t2 = new CronTrigger("CronTestTrigger2", "SchedulerMgmtTest", "0/5 * * * * ?");
		scheduler.scheduleProcess(job2, t2);

		String [] groupNames2 = scheduler.getJobGroupNames();
		assertEquals(1, groupNames2.length);

		String [] jobNames = scheduler.getJobNames("SchedulerMgmtTest");
		assertEquals(2, jobNames.length);

		ProcessJobDescriptor job1Copy = scheduler.getProcessJobDescriptor("job1", "SchedulerMgmtTest");
		assertNotNull(job1Copy);

		scheduler.deleteJob("job1", "SchedulerMgmtTest");
		String [] groupNames3 = scheduler.getJobGroupNames();
		assertEquals(1, groupNames3.length);

		scheduler.deleteJob("job2", "SchedulerMgmtTest");
		String [] groupNames4 = scheduler.getJobGroupNames();
		assertEquals(0, groupNames4.length);

		scheduler.start();
	}
}
