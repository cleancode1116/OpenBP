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
package org.openbp.core.model.item.process;

import org.openbp.core.model.WorkflowTaskDescriptor;

/**
 * A workflow node splits the flow of control into two or more threads.
 *
 * @author Heiko Erhardt
 */
public interface WorkflowNode
	extends MultiSocketNode
{
	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Gets the workflow task descriptor.
	 * @nowarn
	 */
	public WorkflowTaskDescriptor getWorkflowTaskDescriptor();

	/**
	 * Sets the workflow task descriptor.
	 * @nowarn
	 */
	public void setWorkflowTaskDescriptor(WorkflowTaskDescriptor workflowTaskDescriptor);

	/**
	 * Gets the assign to current user flag.
	 * @nowarn
	 */
	public boolean isAssignToCurrentUser();

	/**
	 * Sets the assign to current user flag.
	 * @nowarn
	 */
	public void setAssignToCurrentUser(boolean assignToCurrentUser);
}
