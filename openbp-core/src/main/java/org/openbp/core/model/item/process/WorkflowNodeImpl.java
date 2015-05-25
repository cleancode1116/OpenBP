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

import org.openbp.core.model.ModelObjectSymbolNames;
import org.openbp.core.model.WorkflowTaskDescriptor;

/**
 * Standard implementation of a workflow node.
 * A workflow node splits the flow of control into two or more threads.
 *
 * @author Heiko Erhardt
 */
public class WorkflowNodeImpl extends MultiSocketNodeImpl
	implements WorkflowNode
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Workflow task descriptor */
	private WorkflowTaskDescriptor workflowTaskDescriptor;

	/** Assign to current user flag */
	private boolean assignToCurrentUser;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public WorkflowNodeImpl()
	{
		workflowTaskDescriptor = new WorkflowTaskDescriptor();
	}

	/**
	 * Copies the values of the source object to this object.
	 *
	 * @param source The source object. Must be of the same type as this object.
	 * @param copyMode Determines if a deep copy, a first level copy or a shallow copy is to be
	 * performed. See the constants of the org.openbp.common.generic.description.Copyable class.
	 * @throws CloneNotSupportedException If the cloning of one of the contained objects failed
	 */
	public void copyFrom(Object source, int copyMode)
		throws CloneNotSupportedException
	{
		if (source == this)
			return;
		super.copyFrom(source, copyMode);

		WorkflowNodeImpl src = (WorkflowNodeImpl) source;

		if (src.workflowTaskDescriptor != null)
		{
			workflowTaskDescriptor = (WorkflowTaskDescriptor) src.workflowTaskDescriptor.clone();
		}
		assignToCurrentUser = src.assignToCurrentUser;
	}

	/**
	 * Gets the name of the standard icon of this object.
	 * The icon name can be used by the client-side IconModel to retrieve an icon for the object.
	 *
	 * @return The icon name or null if the object does not have a particular icon
	 */
	public String getModelObjectSymbolName()
	{
		return ModelObjectSymbolNames.WORKFLOW_NODE;
	}

	//////////////////////////////////////////////////
	// @@ ModelObject overrides
	//////////////////////////////////////////////////

	/**
	 * Gets text that can be used to display this object.
	 *
	 * @nowarn
	 */
	public String getDisplayText()
	{
		String text = getDisplayName();
		if (text != null)
			return text;

		return "";
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the workflow task descriptor.
	 * @nowarn
	 */
	public WorkflowTaskDescriptor getWorkflowTaskDescriptor()
	{
		return workflowTaskDescriptor;
	}

	/**
	 * Sets the workflow task descriptor.
	 * @nowarn
	 */
	public void setWorkflowTaskDescriptor(WorkflowTaskDescriptor workflowTaskDescriptor)
	{
		this.workflowTaskDescriptor = workflowTaskDescriptor;
	}

	/**
	 * Gets the assign to current user flag.
	 * @nowarn
	 */
	public boolean isAssignToCurrentUser()
	{
		return assignToCurrentUser;
	}

	/**
	 * Sets the assign to current user flag.
	 * @nowarn
	 */
	public void setAssignToCurrentUser(boolean assignToCurrentUser)
	{
		this.assignToCurrentUser = assignToCurrentUser;
	}
}
