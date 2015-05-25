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

import org.openbp.common.generic.LifecycleSupport;
import org.quartz.Trigger;

/**
 * Process scheduler.
 *
 * @author Heiko Erhardt
 */
public interface ProcessScheduler
	extends LifecycleSupport
{
	/**
	 * Starts the scheduler.
	 */
	public void start();

	/**
	 * Suspends the scheduler.
	 */
	public void suspend();

	/**
	 * Schedules a process using the supplied trigger.
	 * If a job with the job and group name specified in the descriptor already exists,
	 * the method will perform an update of the job, deleting all previous triggers.
	 *
	 * @param desc See the constructors and setts of {@link ProcessJobDescriptor} for details.
	 * @param trigger Trigger that indicates when to fire the process job
	 */
	public void scheduleProcess(ProcessJobDescriptor desc, Trigger trigger);

	/**
	 * Schedules a process using the triggers defined by the job descriptor.
	 * If a job with the job and group name specified in the descriptor already exists,
	 * the method will perform an update of the job, deleting all previous triggers.
	 *
	 * @param desc See the constructors and setts of {@link ProcessJobDescriptor} for details.
	 */
	public void scheduleProcess(ProcessJobDescriptor desc);

	/**
	 * Deletes the specified job.
	 *
	 * @param jobName Job name
	 * @param jobGroup Job group
	 */
	public void deleteJob(String jobName, String jobGroup);

	/**
	 * Gets the group names of all jobs of the underlying Quartz scheduler.
	 *
	 * @return The job group name list includes group names of process tasks and non-process tasks
	 */
	public String [] getJobGroupNames();

	/**
	 * Gets the job names of all jobs of the specified job group of the underlying Quartz scheduler.
	 *
	 * @param groupName Group name
	 * @return The job name list includes group names of process tasks and non-process tasks
	 */
	public String [] getJobNames(String groupName);

	/**
	 * Gets a process job descriptor by its job name and group name.
	 *
	 * @param jobName Job name
	 * @param groupName Group name
	 * @return The descriptor or null if the job/group name does not specify a process job
	 */
	public ProcessJobDescriptor getProcessJobDescriptor(String jobName, String groupName);
}
