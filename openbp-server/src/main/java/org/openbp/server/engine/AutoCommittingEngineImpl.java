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
package org.openbp.server.engine;

import org.openbp.server.context.LifecycleState;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.WorkflowTask;

/**
 * Implementation of the OpenBP process engine that automatically committs any operation.
 * The class calls the {@link EngineImpl#commitTokenContextTransaction} method after calling the {@link #startToken}, {@link #resumeToken} or
 * {@link #resumeWorkflow} methods.
 *
 * @author Heiko Erhardt
 */
public class AutoCommittingEngineImpl extends EngineImpl
{
	/**
	 * Default constructor.
	 */
	public AutoCommittingEngineImpl()
	{
	}

	/**
	 * Method override that automatically commits the transaction.
	 * The token is marked for further execution by the process engine.
	 * Note that the current position within the token must have been set before.
	 * The method commits the changes to the database.
	 *
	 * @param context Token context
	 */
	public void startToken(final TokenContext context)
	{
		super.startToken(context);
		commitTokenContextTransaction();
	}

	/**
	 * Method override that automatically commits the transaction.
	 *
	 * @param context Token context
	 */
	public void resumeToken(final TokenContext context)
	{
		int lifecycleState = context.getLifecycleState();
		super.resumeToken(context);
		if (lifecycleState != LifecycleState.IDLING)
		{
			commitTokenContextTransaction();
		}
	}

	/**
	 * Method override that automatically commits the transaction.
	 *
	 * @param workflowTask Workflow task this workflow refers to
	 * @param socketName Name of the socket of the workflow node to continue with or null for the default socket
	 * @param currentUserId Id of the user that accepts this workflow (may be null);
	 * will be assigned to the 'AcceptingUser' property of the workflow and to the 'UserId' of the workflow
	 * if the 'AssignToCurrentUser' property of the workflow has been set.
	 */
	public void resumeWorkflow(final WorkflowTask workflowTask, final String socketName, final String currentUserId)
	{
		super.resumeWorkflow(workflowTask, socketName, currentUserId);
		commitTokenContextTransaction();
	}
}
