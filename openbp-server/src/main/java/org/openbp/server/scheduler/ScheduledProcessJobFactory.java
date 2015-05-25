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
package org.openbp.server.scheduler;

import org.openbp.server.ProcessServer;
import org.quartz.Job;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

/**
 * A factory class for {@link ScheduledProcessJob} objects.
 * Provides the new objects with a reference to the process server so they can start or resume processes.
 *
 * @author Heiko Erhardt
 */
public class ScheduledProcessJobFactory
	implements JobFactory
{
	/** Process server reference */
	private ProcessServer processServer;

	/**
	 * Default constructor.
	 */
	public ScheduledProcessJobFactory()
	{
	}

	//////////////////////////////////////////////////
	// @@ JobFactory implementation
	//////////////////////////////////////////////////

	/**
	 * Creates a new job.
	 * Called by the scheduler at the time of the trigger firing, in order to produce a Job instance on which to call execute.
	 *
	 * @param bundle Bundle
	 * @return The new job
	 * @throws SchedulerException On error
	 */
	public Job newJob(TriggerFiredBundle bundle)
		throws SchedulerException
	{
		ScheduledProcessJob job = new ScheduledProcessJob();
		job.setProcessServer(processServer);
		return job;
	}

	/**
	 * Gets the process server reference.
	 * @nowarn
	 */
	public ProcessServer getProcessServer()
	{
		return processServer;
	}

	/**
	 * Sets the process server reference.
	 * @nowarn
	 */
	public void setProcessServer(ProcessServer processServer)
	{
		this.processServer = processServer;
	}
}
