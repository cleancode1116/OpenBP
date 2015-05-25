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
package org.openbp.server.engine.executor;

import java.sql.Timestamp;
import java.util.List;

import org.openbp.common.property.PropertyAccessUtil;
import org.openbp.common.property.PropertyException;
import org.openbp.core.CoreConstants;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.WorkflowTaskDescriptor;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.Param;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.WorkflowNode;
import org.openbp.server.context.LifecycleRequest;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextService;
import org.openbp.server.context.TokenContextUtil;
import org.openbp.server.context.WorkflowTask;
import org.openbp.server.engine.EngineExecutor;
import org.openbp.server.engine.ModelObjectExecutorBase;

/**
 * Executor for workflow nodes.
 *
 * @author Heiko Erhardt
 */
public class WorkflowNodeExecutor extends ModelObjectExecutorBase
{
	/**
	 * Executes a process element.
	 *
	 * @param mo Process element to execute
	 * @param ee Engine executor that called this method
	 * @throws OpenBPException On error
	 */
	public void executeModelObject(ModelObject mo, EngineExecutor ee)
	{
		TokenContext context = ee.getTokenContext();
		NodeSocket entrySocket = context.getCurrentSocket();

		WorkflowTask workflowTask = createWorkflowTask(entrySocket, context);

		// Continue with the 'TaskPublished' socket
		NodeSocket nextSocket = getEngine().resolveSocketRef(CoreConstants.SOCKET_TASK_PUBLISHED, entrySocket, context, false);

		if (nextSocket != null)
		{
			Param workflowTaskParam = nextSocket.getParamByName(CoreConstants.WORKFLOWTASK_PARAM_NAME); 
			if (workflowTaskParam != null)
			{
				// If the exit socket contains a 'WorkflowTask' parameter, set it
				TokenContextUtil.setParamValue(context, workflowTaskParam, workflowTask);
			}
			context.setCurrentSocket(nextSocket);
		}
		else
		{
			// No 'TaskPublished' socket -> Stop here
			context.setLifecycleRequest(LifecycleRequest.SUSPEND_IMMEDIATE);
		}
	}

	/**
	 * Creates a new workflow task and saves it to the workflow persistence store.
	 *
	 * @param entrySocket Entry socket of the workflow node
	 * @param context Token context
	 * @return The new workflow task
	 * @throws OpenBPException Any exception that may occur during creation or save operation of the workflow
	 */
	protected WorkflowTask createWorkflowTask(NodeSocket entrySocket, TokenContext context)
	{
		TokenContextService contextService = getEngine().getTokenContextService();

		WorkflowNode node = (WorkflowNode) entrySocket.getNode();

		// Create a new workflow task
		WorkflowTask workflowTask = contextService.createWorkflowTask(context);

		// Update the current task from the entry socket and the prototype
		WorkflowTaskDescriptor prototype = node.getWorkflowTaskDescriptor();
		updateWorkflowTaskFromPrototype(workflowTask, entrySocket, context, prototype);

		// Update the creating user and creation date if not present already
		if (workflowTask.getCreatingUserId() == null)
		{
			workflowTask.setCreatingUserId(context.getUserId());
		}
		if (workflowTask.getTimeCreated() == null)
		{
			workflowTask.setTimeCreated(new Timestamp(System.currentTimeMillis()));
		}

		// This is an active workflow task now.
		workflowTask.setStatus(WorkflowTask.STATUS_ENABLED);

		workflowTask = contextService.addWorkflowTask(workflowTask);

		return workflowTask;
	}

	/**
	 * Updates the fields of the workflow task from the socket or the prototype.
	 *
	 * @param task Task
	 * @param entrySocket Entry socket or null
	 * @param context Token context of the process or null
	 * @param prototype Prototype or null
	 */
	protected void updateWorkflowTaskFromPrototype(WorkflowTask task, NodeSocket entrySocket, TokenContext context,
		WorkflowTaskDescriptor prototype)
	{
		if (prototype != null)
		{
			// These properties will be overwritten for each workflow node
			task.setStepName(prototype.getStepName());
			task.setStepDisplayName(prototype.getStepDisplayName());
			task.setStepDescription(prototype.getStepDescription());
			task.setRoleId(prototype.getRoleId());
			task.setUserId(prototype.getUserId());
			task.setDeleteAfterCompletion(prototype.isDeleteAfterCompletion());

			// These properties will be saved when specified only
			if (prototype.getPermissions() != null)
				task.setPermissions(prototype.getPermissions());
			if (prototype.getPriority() != 0)
				task.setPriority(prototype.getPriority());
		}

		// Copy title and description from the WF processs to the new task
		if (entrySocket != null)
		{
			ProcessItem workflowProcess = entrySocket.getProcess();
			task.setDisplayName(workflowProcess.getDisplayName());
			task.setDescription(workflowProcess.getDescription());

			// Inspect the parameters of the given initial node if we should assign
			// any of these to properties of the new workflow task object.
			if (context != null)
			{
				List params = entrySocket.getParamList();
				if (params != null)
				{
					int n = params.size();
					for (int i = 0; i < n; ++i)
					{
						Param param = (Param) params.get(i);
						String name = param.getName();

						Object value = TokenContextUtil.getParamValue(context, entrySocket, name);
						if (value != null)
						{
							try
							{
								PropertyAccessUtil.setProperty(task, name, value);
							}
							catch (PropertyException e)
							{
								// Ignore
							}
						}
					}
				}
			}
		}
	}
}
