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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openbp.common.logger.LogUtil;
import org.openbp.server.ProcessFacade;
import org.openbp.server.ProcessServer;
import org.openbp.server.context.TokenContext;
import org.openbp.server.engine.Engine;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

/**
 * A Quartz job that is used to trigger scheduled process invocations and resumptions.
 * This class implements StatefulJob in order to prevent simultaneous execution.
 *
 * @author Heiko Erhardt
 */
public class ScheduledProcessJob
	implements StatefulJob
{
	public static final String KEY_TOKEN_ID = "TokenId";

	public static final String KEY_POSITION_REF = "PositionRef";

	public static final String KEY_INITIAL_POSITION_REF = "InitialPositionRef";

	public static final String KEY_PARAM_PREFIX = "Param.";

	public static final String KEY_RUNTIME_ATRIBUTE_PREFIX = "RuntimeAtribute.";

	public static final String KEY_START_MODE = "StartMode";

	public static final String KEY_EXECUTION_MODE = "ExecutionMode";

	public static final String KEY_DISABLED = "Disabled";

	/** Process server reference */
	private ProcessServer processServer;

	/**
	 * Default constructor.
	 */
	public ScheduledProcessJob()
	{
	}

	//////////////////////////////////////////////////
	// @@ Job implementation
	//////////////////////////////////////////////////

	public void execute(JobExecutionContext context)
		throws JobExecutionException
	{
		Object tokenId = "?";

		try
		{
			ProcessFacade processFacade = processServer.getProcessFacade();

			// TODO Fix 3 This should be JobDataMap dataMap = context.getJobDataMap();
			JobDataMap dataMap = context.getJobDetail().getJobDataMap();

			boolean disabled = dataMap.getBoolean(KEY_DISABLED);
			if (disabled)
				return;

			TokenContext tc = null;
			tokenId = dataMap.get(KEY_TOKEN_ID);
			if (tokenId == null)
			{
				// No token given, create new one
				tc = processFacade.createToken();
				processFacade.prepareTokenForScheduler(tc);
				tokenId = tc.getId();
			}
			else
			{
				// Use existing token
				tc = processFacade.getTokenById(tokenId);
				if (tc == null)
				{
					String msg = LogUtil.error(getClass(), "Cannot find the scheduled token context (id $0) for job $1.", tokenId, context
						.getJobDetail().getGroup() + "." 
						+ context.getJobDetail().getName());
					JobExecutionException ex = new JobExecutionException(msg);
					ex.setUnscheduleAllTriggers(true);
					throw ex;
				}
			}

			String positionRef = dataMap.getString(KEY_POSITION_REF);
			String executionMode = dataMap.getString(KEY_EXECUTION_MODE);
			String startMode = dataMap.getString(KEY_START_MODE);

			Map inputParamValues = null;

			for (Iterator it = dataMap.entrySet().iterator(); it.hasNext();)
			{
				Map.Entry entry = (Map.Entry) it.next();

				String key = (String) entry.getKey();
				if (key.startsWith(KEY_PARAM_PREFIX))
				{
					if (inputParamValues == null)
					{
						inputParamValues = new HashMap();
					}
					key = key.substring(KEY_PARAM_PREFIX.length());
					inputParamValues.put(key, entry.getValue());
				}
				else if (key.startsWith(KEY_RUNTIME_ATRIBUTE_PREFIX))
				{
					key = key.substring(KEY_RUNTIME_ATRIBUTE_PREFIX.length());
					tc.setRuntimeAttribute(key, entry.getValue());
				}
			}

			Engine engine = processServer.getEngine();

			if (ProcessJobDescriptor.START_MODE_RESUME.equals(startMode))
			{
				// Resume an existing token
				if (engine.hasActiveObservers(SchedulerEngineEvent.RESUME_JOB, tc))
				{
					ProcessJobDescriptor desc = ((QuartzProcessScheduler) processServer.getProcessScheduler()).createJobDescriptor(context.getJobDetail());
					engine.fireEngineEvent(new SchedulerEngineEvent(SchedulerEngineEvent.RESUME_JOB, tc, desc, engine));
				}
				processFacade.resumeToken(tc, positionRef, inputParamValues);
			}
			else
			{
				// Start a new token
				if (engine.hasActiveObservers(SchedulerEngineEvent.START_JOB, tc))
				{
					ProcessJobDescriptor desc = ((QuartzProcessScheduler) processServer.getProcessScheduler()).createJobDescriptor(context.getJobDetail());
					engine.fireEngineEvent(new SchedulerEngineEvent(SchedulerEngineEvent.START_JOB, tc, desc, engine));
				}
				processFacade.startToken(tc, positionRef, inputParamValues);
			}

			// Process the token immediately if desired, otherwise let the execution thread pool do this.
			if (ProcessJobDescriptor.EXECUTION_MODE_SYNCHRONOUS.equals(executionMode))
			{
				// We do not need to set the lifecycle to LifecycleState.SELECTED, it will be set to LifecycleState.RUNNING by the executeContext method
				tc = processFacade.getTokenById(tokenId);
				if (tc == null)
				{
					String msg = LogUtil.error(getClass(), "Cannot find the scheduled token context (id $0) for job $1.", tokenId, context
						.getJobDetail().getGroup()
						+ context.getJobDetail().getName());
					JobExecutionException ex = new JobExecutionException(msg);
					ex.setUnscheduleAllTriggers(true);
					throw ex;
				}
				engine.executeContext(tc);
				processFacade.commitTokenContextTransaction();
			}
		}
		catch (Exception e)
		{
			JobExecutionException jex = null;
			if (e instanceof JobExecutionException)
			{
				jex = (JobExecutionException) e;
			}
			else
			{
				String msg = LogUtil.error(getClass(), "Error occured while processing scheduled token context (id $0) for job $1.", tokenId, context
					.getJobDetail().getGroup()
					+ context.getJobDetail().getName(), e);
				jex = new JobExecutionException(msg, e, true);
			}
			jex.setUnscheduleAllTriggers(true);
			// TODO This will lead to an endless loop
			// throw jex;
		}
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
