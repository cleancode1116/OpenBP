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
package org.openbp.server.scheduler;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.openbp.common.logger.LogUtil;
import org.openbp.common.resource.ResourceMgr;
import org.openbp.common.setting.SettingUtil;
import org.openbp.core.OpenBPException;
import org.openbp.server.ProcessFacade;
import org.openbp.server.ProcessServer;
import org.openbp.server.context.TokenContext;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Implementation of the process scheduler interface using the well-known Quartz scheduler.
 *
 * @author Heiko Erhardt
 */
public class QuartzProcessScheduler
	implements ProcessScheduler
{
	static final String PROPERTIES_FILE_NAME = "OpenBP-Quartz.properties";

	/** Quartz scheduler instance */
	private Scheduler quartzInstance;

	/** Process server reference */
	private ProcessServer processServer;

	/**
	 * Default constructor.
	 */
	public QuartzProcessScheduler()
	{
	}

	/**
	 * Initializes the scheduler.
	 *
	 * Quartz uses the data source provided by the persistence context, so
	 * the persistenceContextProvider must be set before calling this method.
	 */
	public void initialize()
	{
		InputStream is = null;
		try
		{
			Properties props = new Properties();
			is = ResourceMgr.getDefaultInstance().openResource(PROPERTIES_FILE_NAME);
			props.load(is);

			// Perform property variable expansion
			Properties exandedProps = SettingUtil.expandProperties(props);

			SchedulerFactory quartzFactory = new StdSchedulerFactory(exandedProps);
			quartzInstance = quartzFactory.getScheduler();

			ScheduledProcessJobFactory jobFactory = new ScheduledProcessJobFactory();
			jobFactory.setProcessServer(processServer);
			quartzInstance.setJobFactory(jobFactory);

			start();
		}
		catch (SchedulerException e)
		{
			throw new OpenBPException("scheduler.start", "Error starting scheduler.", e);
		}
		catch (IOException e)
		{
			throw new OpenBPException("scheduler.start", "Error reading scheduler configuration.", e);
		}
		finally
		{
			try
			{
				if (is != null)
				{
					is.close();
				}
			}
			catch (Exception e)
			{
				LogUtil.error(getClass(), "Error closing resource " + PROPERTIES_FILE_NAME);
			}
		}
	}

	/**
	 * Shuts down the scheduler.
	 */
	public void shutdown()
	{
		try
		{
			// shutdown() does not return until executing Jobs complete execution
			quartzInstance.shutdown(true);
		}
		catch (SchedulerException e)
		{
			throw new OpenBPException("scheduler.shutdown", "Error shutting down scheduler.", e);
		}
	}

	/**
	 * Starts the scheduler.
	 */
	public void start()
	{
		try
		{
			quartzInstance.start();
		}
		catch (SchedulerException e)
		{
			throw new OpenBPException("scheduler.start", "Error starting scheduler.", e);
		}
	}

	/**
	 * Suspends the scheduler.
	 */
	public void suspend()
	{
		try
		{
			quartzInstance.standby();
		}
		catch (SchedulerException e)
		{
			throw new OpenBPException("scheduler.suspend", "Error suspending scheduler.", e);
		}
	}

	/**
	 * Schedules a process using the supplied trigger.
	 * If a job with the job and group name specified in the descriptor already exists,
	 * the method will perform an update of the job, deleting all previous triggers.
	 *
	 * @param desc See the constructors and setts of {@link ProcessJobDescriptor} for details.
	 * @param trigger Trigger that indicates when to fire the process job
	 */
	public void scheduleProcess(ProcessJobDescriptor desc, Trigger trigger)
	{
		try
		{
			prepareDescriptorForScheduling(desc);

			JobDetail job = createJobDetail(desc);

			quartzInstance.deleteJob(desc.getJobName(), desc.getJobGroup());

			quartzInstance.scheduleJob(job, trigger);
		}
		catch (SchedulerException e)
		{
			throw new OpenBPException("scheduler.scheduleProcess", "Error scheduling process.", e);
		}
	}

	/**
	 * Schedules a process using the triggers defined by the job descriptor.
	 * If a job with the job and group name specified in the descriptor already exists,
	 * the method will perform an update of the job, deleting all previous triggers.
	 *
	 * @param desc See the constructors and setts of {@link ProcessJobDescriptor} for details.
	 */
	public void scheduleProcess(ProcessJobDescriptor desc)
	{
		try
		{
			prepareDescriptorForScheduling(desc);

			JobDetail job = createJobDetail(desc);

			quartzInstance.deleteJob(desc.getJobName(), desc.getJobGroup());

			quartzInstance.addJob(job, true);

			for (Iterator it = desc.getTriggers(); it.hasNext();)
			{
				Trigger trigger = (Trigger) it.next();
				trigger.setJobName(desc.getJobName());
				trigger.setJobGroup(desc.getJobGroup());
				quartzInstance.scheduleJob(trigger);
			}
		}
		catch (SchedulerException e)
		{
			throw new OpenBPException("scheduler.scheduleProcess", "Error scheduling process.", e);
		}
	}

	/**
	 * Deletes the specified job.
	 *
	 * @param jobName Job name
	 * @param jobGroup Job group
	 */
	public void deleteJob(String jobName, String jobGroup)
	{
		try
		{
			quartzInstance.deleteJob(jobName, jobGroup);
		}
		catch (SchedulerException e)
		{
			throw new OpenBPException("scheduler.deleteJob", "Error deleting process.", e);
		}
	}

	/**
	 * Gets the group names of all jobs of the underlying Quartz scheduler.
	 *
	 * @return The job group name list includes group names of process tasks and non-process tasks
	 */
	public String [] getJobGroupNames()
	{
		try
		{
			return quartzInstance.getJobGroupNames();
		}
		catch (SchedulerException e)
		{
			throw new OpenBPException("scheduler.getJobGroupNames", "Error scheduling process.", e);
		}
	}

	/**
	 * Gets the job names of all jobs of the specified job group of the underlying Quartz scheduler.
	 *
	 * @param groupName Group name
	 * @return The job name list includes group names of process tasks and non-process tasks
	 */
	public String [] getJobNames(String groupName)
	{
		try
		{
			return quartzInstance.getJobNames(groupName);
		}
		catch (SchedulerException e)
		{
			throw new OpenBPException("scheduler.getJobNames", "Error scheduling process.", e);
		}
	}

	/**
	 * Gets a process job descriptor by its job name and group name.
	 *
	 * @param jobName Job name
	 * @param groupName Group name
	 * @return The descriptor or null if the job/group name does not specify a process job
	 */
	public ProcessJobDescriptor getProcessJobDescriptor(String jobName, String groupName)
	{
		try
		{
			JobDetail jobDetail = quartzInstance.getJobDetail(jobName, groupName);
			ProcessJobDescriptor desc = createJobDescriptor(jobDetail);
			if (desc != null)
			{
				Trigger[] triggers = quartzInstance.getTriggersOfJob(jobName, groupName);
				for (int i = 0; i < triggers.length; ++i)
				{
					desc.addTrigger(triggers[i]);
				}
			}
			return desc;
		}
		catch (SchedulerException e)
		{
			throw new OpenBPException("scheduler.getTriggersOfJob", "Error scheduling process.", e);
		}
	}

	protected void prepareDescriptorForScheduling(ProcessJobDescriptor desc)
	{
		if (desc.getTokenContext() == null)
		{
			desc.setInitialPositionRef(desc.getPositionRef());
		}
	}

	protected JobDetail createJobDetail(ProcessJobDescriptor desc)
	{
		JobDetail jobDetail = new JobDetail(desc.getJobName(), desc.getJobGroup(), ScheduledProcessJob.class);
		JobDataMap dataMap = jobDetail.getJobDataMap();

		String positionRef = desc.getPositionRef();
		if (positionRef == null)
		{
			// Not a process descriptor
			return null;
		}

		dataMap.put(ScheduledProcessJob.KEY_POSITION_REF, positionRef);
		dataMap.put(ScheduledProcessJob.KEY_INITIAL_POSITION_REF, desc.getInitialPositionRef());
		if (desc.getTokenContext() != null)
		{
			dataMap.put(ScheduledProcessJob.KEY_TOKEN_ID, desc.getTokenContext().getId());
		}
		dataMap.put(ScheduledProcessJob.KEY_START_MODE, desc.getStartMode());
		dataMap.put(ScheduledProcessJob.KEY_EXECUTION_MODE, desc.getExecutionMode());
		dataMap.put(ScheduledProcessJob.KEY_DISABLED, desc.isDisabled());

		if (desc.getInputValues() != null)
		{
			for (Iterator it = desc.getInputValues().entrySet().iterator(); it.hasNext();)
			{
				Map.Entry entry = (Map.Entry) it.next();
				dataMap.put(ScheduledProcessJob.KEY_PARAM_PREFIX + entry.getKey(), entry.getValue());
			}
		}

		if (desc.getRuntimeAttributes() != null)
		{
			for (Iterator it = desc.getRuntimeAttributes().entrySet().iterator(); it.hasNext();)
			{
				Map.Entry entry = (Map.Entry) it.next();
				dataMap.put(ScheduledProcessJob.KEY_RUNTIME_ATRIBUTE_PREFIX + entry.getKey(), entry.getValue());
			}
		}

		return jobDetail;
	}

	protected ProcessJobDescriptor createJobDescriptor(JobDetail jobDetail)
	{
		ProcessJobDescriptor desc = new ProcessJobDescriptor();
		desc.setJobName(jobDetail.getName());
		desc.setJobGroup(jobDetail.getGroup());
		JobDataMap dataMap = jobDetail.getJobDataMap();

		if (desc.getTokenContext() != null)
		{
			String tokenId = dataMap.getString(ScheduledProcessJob.KEY_TOKEN_ID);
			if (tokenId != null)
			{
				try
				{
					ProcessFacade processFacade = processServer.getProcessFacade();
					TokenContext tc = processFacade.getTokenById(tokenId);
					desc.setTokenContext(tc);
				}
				catch (Exception e)
				{
					String msg = LogUtil.error(getClass(), "Cannot find the scheduled token context (id $0) for job $1.", tokenId,
						jobDetail.getGroup() + "." + jobDetail.getName());
					OpenBPException ex = new OpenBPException("scheduler.missingTokenContext", msg);
					throw ex;
				}
			}
		}
		desc.setPositionRef(dataMap.getString(ScheduledProcessJob.KEY_POSITION_REF));
		desc.setInitialPositionRef(dataMap.getString(ScheduledProcessJob.KEY_INITIAL_POSITION_REF));
		desc.setStartMode(dataMap.getString(ScheduledProcessJob.KEY_START_MODE));
		desc.setExecutionMode(dataMap.getString(ScheduledProcessJob.KEY_EXECUTION_MODE));
		Object o = dataMap.get(ScheduledProcessJob.KEY_DISABLED);
		if (o != null)
		{
			desc.setDisabled(((Boolean) o).booleanValue());
		}

		HashMap inputValues = null;
		for (Iterator it = dataMap.entrySet().iterator(); it.hasNext();)
		{
			Map.Entry entry = (Map.Entry) it.next();
			String key = (String) entry.getKey();
			if (key.startsWith(ScheduledProcessJob.KEY_PARAM_PREFIX))
			{
				key = key.substring(ScheduledProcessJob.KEY_PARAM_PREFIX.length());
				Object value = entry.getValue();
				if (inputValues == null)
				{
					inputValues = new HashMap();
				}
				inputValues.put(key, value);
			}
			else if (key.startsWith(ScheduledProcessJob.KEY_RUNTIME_ATRIBUTE_PREFIX))
			{
				key = key.substring(ScheduledProcessJob.KEY_RUNTIME_ATRIBUTE_PREFIX.length());
				desc.setRuntimeAttribute(key, entry.getValue());
			}
		}
		desc.setInputValues(inputValues);

		return desc;
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

	/**
	 * Gets the Quartz scheduler instance.
	 * @nowarn
	 */
	public Scheduler getQuartzInstance()
	{
		return quartzInstance;
	}
}
