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

import org.openbp.core.model.Model;

/**
 * Search criteria object for the selection of token contexts.
 *
 * @author Heiko Erhardt
 */
public class WorkflowTaskCriteria extends CriteriaBase
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Primary key */
	private Object id;

	/** Executing model */
	private Model model;

	/** Name of the workflow task */
	protected String name;

	/** System name of the workflow step that is about to be performed */
	private String stepName;

	/** Id of the role this workflow task is assigned to (public worklist) or null */
	private String roleId;

	/** Id of the user this workflow task is assigned to (private worklist) or null */
	private String userId;

	/** Processing status of the workflow task. */
	protected Integer status;

	/** Token context of this workflow task */
	private TokenContext tokenContext;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public WorkflowTaskCriteria()
	{
	}

	//////////////////////////////////////////////////
	// @@ Data member access
	//////////////////////////////////////////////////

	/**
	 * Gets the primary key.
	 * @nowarn
	 */
	public Object getId()
	{
		return id;
	}

	/**
	 * Sets the primary key.
	 * @nowarn
	 */
	public void setId(Object id)
	{
		this.id = id;
	}

	/**
	 * Gets the executing model.
	 * @nowarn
	 */
	public Model getModel()
	{
		return model;
	}

	/**
	 * Sets the executing model.
	 * @nowarn
	 */
	public void setModel(Model model)
	{
		this.model = model;
	}

	/**
	 * Gets the name of the workflow task.
	 * @nowarn
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name of the workflow task.
	 * @nowarn
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the system name of the workflow step that is about to be performed.
	 * @nowarn
	 */
	public String getStepName()
	{
		return stepName;
	}

	/**
	 * Sets the system name of the workflow step that is about to be performed.
	 * @nowarn
	 */
	public void setStepName(String stepName)
	{
		this.stepName = stepName;
	}

	/**
	 * Gets the id of the role this workflow task is assigned to (public worklist) or null.
	 * @nowarn
	 */
	public String getRoleId()
	{
		return roleId;
	}

	/**
	 * Sets the id of the role this workflow task is assigned to (public worklist) or null.
	 * @nowarn
	 */
	public void setRoleId(String roleId)
	{
		this.roleId = roleId;
	}

	/**
	 * Gets the id of the user this workflow task is assigned to (private worklist) or null.
	 * @nowarn
	 */
	public String getUserId()
	{
		return userId;
	}

	/**
	 * Sets the id of the user this workflow task is assigned to (private worklist) or null.
	 * @nowarn
	 */
	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	/**
	 * Gets the processing status of the workflow task..
	 * @return {@link WorkflowTask#STATUS_DISABLED}/{@link WorkflowTask#STATUS_ENABLED}/{@link WorkflowTask#STATUS_RESUMED}/{@link WorkflowTask#STATUS_COMPLETED} as Integer
	 */
	public Integer getStatus()
	{
		return status;
	}

	/**
	 * Sets the processing status of the workflow task..
	 * @param status {@link WorkflowTask#STATUS_DISABLED}/{@link WorkflowTask#STATUS_ENABLED}/{@link WorkflowTask#STATUS_RESUMED}/{@link WorkflowTask#STATUS_COMPLETED} as Integer
	 */
	public void setStatus(Integer status)
	{
		this.status = status;
	}

	/**
	 * Gets the token context of this workflow task.
	 * @nowarn
	 */
	public TokenContext getTokenContext()
	{
		return tokenContext;
	}

	/**
	 * Sets the token context of this workflow task.
	 * @nowarn
	 */
	public void setTokenContext(TokenContext tokenContext)
	{
		this.tokenContext = tokenContext;
	}
}
