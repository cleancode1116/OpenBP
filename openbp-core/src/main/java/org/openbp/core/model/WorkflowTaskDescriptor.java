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
package org.openbp.core.model;

import java.io.Serializable;

import org.openbp.common.generic.Copyable;
import org.openbp.common.util.ToStringHelper;
import org.openbp.core.model.item.ConfigurationBean;

/**
 * Workflow task descriptor.
 * A workflow task is a portion of work to perform by a particular user or role as part of a workflow process.
 * A workflow task is created by a workflow node.<br>
 * A workflow task is assigned either to a particular user (i. e. only this user can process the workflow task) or to a role (indicating that any user fullfilling this role can process the workflow task).<br>
 * Optionally, the workflow task has a defined due time. If the due time expires (or if the workflow task is assigned to another user or cancelled), a status change will be triggered in the process that created the workflow task.
 *
 * The workflow task has a state that indicates if it can be selected for processing, is being processed or has been completed.<br>
 * The state may also indicate that an error occured during processing.
 * Note that the process engine does not set the STATUS_ERROR value explicitely due to transaction reasons.
 * The error indicator is meant to be set by the application program.
 *
 * Note that the bean implements the java.io.Serializable interface.
 * Beans that are used in OpeenBP processes need to be serializable in order to support token context serialization for clustering.
 * If you add a member that references another object, either make sure that this object are also serializable
 * or prefix the member with the 'transient' keyword if you don't want it to be serialized.
 */
public class WorkflowTaskDescriptor
	implements ConfigurationBean, Serializable
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/**
	 * Step system name.
	 * System name of the workflow step that is about to be performed.
	 * This name can be used to reference the step independent of the locale.
	 */
	private String stepName;

	/**
	 * Step display name.
	 * Title of the workflow step that is about to be performed.
	 * This display name may also appear as task title in the worklist.
	 * The display name should provide a short but functional information on the pending workflow step.
	 * It might be listed in the workflow task user interface in order to aid the user in selecting a workflow task to process.
	 */
	private String stepDisplayName;

	/**
	 * Step description.
	 * Detailled description of the workflow step that is to be performed.
	 * This description might be displayed in a dialog that appears if the user accepts the workflow.
	 */
	private String stepDescription;

	/**
	 * Role.
	 * Id of the role this workflow task is assigned to (public worklist) or null.
	 */
	private String roleId;

	/**
	 * User.
	 * Id of the user this workflow task is assigned to (private worklist) or null.
	 */
	private String userId;

	/**
	 * Permissions.
	 * Permissions that apply for this workflow task.
	 * The permissions specify what a user is allowed to do with the task. The permissions are being matched agains the permissions of the user or the role the user has.
	 *
	 * The permissions string consists of arbitrary comma-separated entries. Standard permissions are:
	 *
	 * - execute
	 * - view
	 * - edit
	 * - delete
	 * - disable
	 * - enable
	 * - forward
	 * - reschedule.
	 */
	private String permissions;

	/**
	 * Priority.
	 * Priority of this workflow task.
	 * This can be any number. We recommend using values between 1 (highest) and 5 (lowest).
	 * By default, the workflow task items will be sorted by due time and priority.
	 */
	private int priority;

	/** Delete after completion */
	private boolean deleteAfterCompletion;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public WorkflowTaskDescriptor()
	{
	}

	/**
	 * Creates a clone of this object.
	 * @return The clone (a deep copy of this object)
	 * @throws CloneNotSupportedException If the cloning of one of the contained members failed
	 */
	public Object clone()
		throws CloneNotSupportedException
	{
		WorkflowTaskDescriptor clone = (WorkflowTaskDescriptor) super.clone();

		// Perform a deep copy
		clone.copyFrom(this, Copyable.COPY_DEEP);

		return clone;
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

		WorkflowTaskDescriptor src = (WorkflowTaskDescriptor) source;

		stepName = src.stepName;
		stepDisplayName = src.stepDisplayName;
		stepDescription = src.stepDescription;
		roleId = src.roleId;
		userId = src.userId;
		permissions = src.permissions;
		priority = src.priority;
	}

	/**
	 * Determines if the values of the members of this bean have default values.
	 * This method is used internally in order to determine if a bean needs to be saved or not.
	 * The default implementation always returns false.
	 *
	 * @return
	 *		true	All members have default values.
	 *		false	At least one member has a value different from the default value.
	 */
	public boolean hasDefaultValues()
	{
		if (stepName != null)
			return false;

		if (stepDisplayName != null)
			return false;

		if (stepDescription != null)
			return false;

		if (roleId != null)
			return false;

		if (userId != null)
			return false;

		if (permissions != null)
			return false;

		if (priority != 0)
			return false;

		return true;
	}

	/**
	 * Returns a string representation of this object.
	 *
	 * @return The string in the form "Workflow task (id 1234)"
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, new String [] { "stepName" });
	}

	/**
	 * Gets the step system name.
	 * @return The step name
	 */
	public String getStepName()
	{
		return stepName;
	}

	/**
	 * Sets the step system name.
	 * @param stepName The step name to be set
	 */
	public void setStepName(String stepName)
	{
		this.stepName = stepName;
	}

	/**
	 * Gets the step display name.
	 * @return The step display name
	 */
	public String getStepDisplayName()
	{
		return stepDisplayName;
	}

	/**
	 * Sets the step display name.
	 * @param stepDisplayName The step display name to be set
	 */
	public void setStepDisplayName(String stepDisplayName)
	{
		this.stepDisplayName = stepDisplayName;
	}

	/**
	 * Gets the step description.
	 * @return The step description
	 */
	public String getStepDescription()
	{
		return stepDescription;
	}

	/**
	 * Sets the step description.
	 * @param stepDescription The step description to be set
	 */
	public void setStepDescription(String stepDescription)
	{
		this.stepDescription = stepDescription;
	}

	/**
	 * Gets the role id.
	 * @return The role id
	 */
	public String getRoleId()
	{
		return roleId;
	}

	/**
	 * Sets the role id.
	 * @param roleId The role id to be set
	 */
	public void setRoleId(String roleId)
	{
		this.roleId = roleId;
	}

	/**
	 * Gets the user id.
	 * @return The user id
	 */
	public String getUserId()
	{
		return userId;
	}

	/**
	 * Sets the user id.
	 * @param userId The user id to be set
	 */
	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	/**
	 * Gets the permissions.
	 * @return The permissions
	 */
	public String getPermissions()
	{
		return permissions;
	}

	/**
	 * Sets the permissions.
	 * @param permissions The permissions to be set
	 */
	public void setPermissions(String permissions)
	{
		this.permissions = permissions;
	}

	/**
	 * Gets the priority.
	 * @return The priority
	 */
	public int getPriority()
	{
		return priority;
	}

	/**
	 * Sets the priority.
	 * @param priority The priority to be set
	 */
	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	/**
	 * Checks if the delete after completion flag is set.
	 * @nowarn
	 */
	public boolean hasDeleteAfterCompletion()
	{
		return isDeleteAfterCompletion();
	}

	/**
	 * Gets the delete after completion flag.
	 * @nowarn
	 */
	public boolean isDeleteAfterCompletion()
	{
		return deleteAfterCompletion;
	}

	/**
	 * Sets the delete after completion flag.
	 * @nowarn
	 */
	public void setDeleteAfterCompletion(boolean deleteAfterCompletion)
	{
		this.deleteAfterCompletion = deleteAfterCompletion;
	}
}
