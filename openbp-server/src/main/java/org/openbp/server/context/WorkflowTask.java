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
package org.openbp.server.context;

import java.sql.Timestamp;

import org.openbp.common.generic.description.DisplayObject;
import org.openbp.server.persistence.PersistentObject;

// }}*Custom imports*

/**
 * Workflow task.
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
public interface WorkflowTask extends PersistentObject, DisplayObject
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Workflow task state: Open issue. */
	public static final int STATUS_UNKNOWN = 0;

	/** Workflow task state: Open issue (enabled). */
	public static final int STATUS_DISABLED = 1;

	/** Workflow task state: Open issue (disabled). */
	public static final int STATUS_ENABLED = 2;

	/** Workflow task state: Task has been resumed as is being processed. */
	public static final int STATUS_RESUMED = 3;

	/** Workflow task state: Task has been completed. */
	public static final int STATUS_COMPLETED = 4;

	/** Workflow task state: An error occured while processing the task.. */
	public static final int STATUS_ERROR = 5;

	/** Workflow task state string: Work task is disabled */
	public static final String STATUS_STR_DISABLED = "disabled";

	/** Workflow task state string: Open issue */
	public static final String STATUS_STR_ENABLED = "enabled";

	/** Workflow task state string: Task has been resumed as is being processed */
	public static final String STATUS_STR_RESUMED = "resumed";

	/** Workflow task state string: Task has been completed */
	public static final String STATUS_STR_COMPLETED = "completed";

	/** Workflow task state string: An error occured while processing the task */
	public static final String STATUS_STR_ERROR = "error";

	/**
	 * Creates an unique name for the workflow task.
	 * The name is an UUID of length 20.
	 */
	public void createName();

	/**
	 * Gets the time created.
	 * @return The time created
	 */
	public Timestamp getTimeCreated();

	/**
	 * Sets the time created.
	 * @param timeCreated The time created to be set
	 */
	public void setTimeCreated(Timestamp timeCreated);

	/**
	 * Gets the time accepted.
	 * @return The time accepted
	 */
	public Timestamp getTimeAccepted();

	/**
	 * Sets the time accepted.
	 * @param timeAccepted The time accepted to be set
	 */
	public void setTimeAccepted(Timestamp timeAccepted);

	/**
	 * Gets the time completed.
	 * @return The time completed
	 */
	public Timestamp getTimeCompleted();

	/**
	 * Sets the time completed.
	 * @param timeCompleted The time completed to be set
	 */
	public void setTimeCompleted(Timestamp timeCompleted);

	/**
	 * Gets the creating user id.
	 * @return The creating user id
	 */
	public String getCreatingUserId();

	/**
	 * Sets the creating user id.
	 * @param creatingUserId The creating user id to be set
	 */
	public void setCreatingUserId(String creatingUserId);

	/**
	 * Gets the accepting user id.
	 * @return The accepting user id
	 */
	public String getAcceptingUserId();

	/**
	 * Sets the accepting user id.
	 * @param acceptingUserId The accepting user id to be set
	 */
	public void setAcceptingUserId(String acceptingUserId);

	/**
	 * Gets the due time.
	 * @return The due time
	 */
	public Timestamp getDueTime();

	/**
	 * Sets the due time.
	 * @param dueTime The due time to be set
	 */
	public void setDueTime(Timestamp dueTime);

	/**
	 * Gets the status.
	 * @return The status
	 */
	public int getStatus();

	/**
	 * Sets the status.
	 * @param status The status to be set
	 */
	public void setStatus(int status);

	/**
	 * Gets the token context of this workflow task.
	 * @nowarn
	 */
	public TokenContext getTokenContext();

	/**
	 * Sets the token context of this workflow task.
	 * @nowarn
	 */
	public void setTokenContext(TokenContext tokenContext);

	//////////////////////////////////////////////////
	// @@ Copied from WorkflowTaskDescriptor
	//////////////////////////////////////////////////

	// The following methods are copied from WorkflowTaskDescriptor, since Java does not support multiple inheritance

	/**
	 * Gets the step system name.
	 * @return The step name
	 */
	public String getStepName();

	/**
	 * Sets the step system name.
	 * @param stepName The step name to be set
	 */
	public void setStepName(String stepName);

	/**
	 * Gets the step display name.
	 * @return The step display name
	 */
	public String getStepDisplayName();

	/**
	 * Sets the step display name.
	 * @param stepDisplayName The step display name to be set
	 */
	public void setStepDisplayName(String stepDisplayName);

	/**
	 * Gets the step description.
	 * @return The step description
	 */
	public String getStepDescription();

	/**
	 * Sets the step description.
	 * @param stepDescription The step description to be set
	 */
	public void setStepDescription(String stepDescription);

	/**
	 * Gets the role id.
	 * @return The role id
	 */
	public String getRoleId();

	/**
	 * Sets the role id.
	 * @param roleId The role id to be set
	 */
	public void setRoleId(String roleId);

	/**
	 * Gets the user id.
	 * @return The user id
	 */
	public String getUserId();

	/**
	 * Sets the user id.
	 * @param userId The user id to be set
	 */
	public void setUserId(String userId);

	/**
	 * Gets the permissions.
	 * @return The permissions
	 */
	public String getPermissions();

	/**
	 * Sets the permissions.
	 * @param permissions The permissions to be set
	 */
	public void setPermissions(String permissions);

	/**
	 * Gets the priority.
	 * @return The priority
	 */
	public int getPriority();

	/**
	 * Sets the priority.
	 * @param priority The priority to be set
	 */
	public void setPriority(int priority);

	/**
	 * Gets the delete after completion flag.
	 * @nowarn
	 */
	public boolean isDeleteAfterCompletion();

	/**
	 * Sets the delete after completion flag.
	 * @nowarn
	 */
	public void setDeleteAfterCompletion(boolean deleteAfterCompletion);
}
