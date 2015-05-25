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
package org.openbp.core.model.modelinspection;

import java.io.Serializable;

import org.openbp.common.CommonUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.generic.description.DescriptionObject;
import org.openbp.common.generic.description.DisplayObject;

/**
 * Workflow step descriptor.
 * Implementation of the WorkflowStepDescriptor data type component.
 * Data container that describes a workflow step.
 * Used by internal API functions.
 *
 * Note that the bean implements the java.io.Serializable interface.
 * Beans that are used in OpeenBP processes need to be serializable in order to support token context serialization for clustering.
 * If you add a member that references another object, either make sure that this object are also serializable
 * or prefix the member with the 'transient' keyword if you don't want it to be serialized.
 */
public class WorkflowStepDescriptor
	implements DisplayObject, Serializable
{
	/**
	 * System name.
	 * Technical name of the workflow (i. e. name of the workflow process).
	 */
	protected String name;

	/**
	 * Display name.
	 * Title of the workflow process.
	 */
	protected String displayName;

	/**
	 * Description.
	 * Description of the workflow process.
	 */
	protected String description;

	/**
	 * Role id.
	 * Id of the role this workflow task is assigned to (public worklist) or null.
	 */
	protected String roleId;

	/**
	 * User id.
	 * Id of the user this workflow task is assigned to (private worklist) or null.
	 */
	protected String userId;

	/**
	 * Default constructor.
	 */
	public WorkflowStepDescriptor()
	{
	}

	/**
	 * Gets the name.
	 * @return The name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name.
	 * @param name The name to be set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the display name.
	 * @return The display name
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/**
	 * Sets the display name.
	 * @param displayName The display name to be set
	 */
	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	/**
	 * Gets the description.
	 * @return The description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Sets the description.
	 * @param description The description to be set
	 */
	public void setDescription(String description)
	{
		this.description = description;
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
	 * Creates a clone of this object.
	 * @return The clone (a deep copy of this object)
	 * @throws CloneNotSupportedException If the cloning of one of the contained members failed
	 */
	public Object clone()
		throws CloneNotSupportedException
	{
		WorkflowStepDescriptor clone = (WorkflowStepDescriptor) super.clone();

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

		WorkflowStepDescriptor src = (WorkflowStepDescriptor) source;

		name = src.name;
		displayName = src.displayName;
		description = src.description;
		roleId = src.roleId;
		userId = src.userId;
	}

	//////////////////////////////////////////////////
	// @@ DescriptionObject implemenation
	//////////////////////////////////////////////////

	/**
	 * Gets text that can be used to describe this object.
	 * By default, this is the description text as returned by the {@link #getDescription} method.
	 * @nowarn
	 */
	public String getDescriptionText()
	{
		return getDescription();
	}


	//////////////////////////////////////////////////
	// @@ Displayable implementation
	//////////////////////////////////////////////////

	/**
	 * Gets text that can be used to display this object.
	 * In this case, this is the regular name ({@link DescriptionObject#setName(String)}) of the object itself.
	 * @nowarn
	 */
	public String getDisplayText()
	{
		return getName();
	}

	//////////////////////////////////////////////////
	// @@ Comparable implementation
	//////////////////////////////////////////////////

	/**
	 * Compares this object to another Object.
	 * If the object is a WorkflowTask, it will compar the {@link #setName} values of the two objects.
	 * Otherwise, it throws a ClassCastException (as WorkflowTasks are comparable only to other WorkflowTasks).
	 *
	 * @param o Object to be compared
	 * @return  The value 0 if the argument is a string lexicographically equal to this object;<br>
	 * a value less than 0 if the argument is a string lexicographically greater than this object;<br>
	 * and a value greater than 0 if the argument is a string lexicographically less than this object.
	 * @throws ClassCastException if the argument is not a WorkflowTask.
	 */
	public int compareTo(Object o)
	{
		WorkflowStepDescriptor w1 = this;
		WorkflowStepDescriptor w2 = (WorkflowStepDescriptor) o;
		int ret;

		String x1 = w1.getDisplayName();
		String x2 = w2.getDisplayName();
		ret = CommonUtil.compareNull(x1, x2);
		if (ret != 0)
			return ret;

		String y1 = w1.getName();
		String y2 = w2.getName();
		return CommonUtil.compareNull(y1, y2);
	}
}
