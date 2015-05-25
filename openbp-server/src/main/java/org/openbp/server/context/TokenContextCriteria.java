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
public class TokenContextCriteria extends CriteriaBase
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Primary key */
	private Object id;

	/** Executing model */
	private Model model;

	/** Context */
	private TokenContext context;

	/** Lifecycle request */
	private Integer lifecycleRequest;

	/** Lifecycle state */
	private Integer lifecycleState;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public TokenContextCriteria()
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
	 * Gets the context.
	 * @nowarn
	 */
	public TokenContext getContext()
	{
		return context;
	}

	/**
	 * Sets the context.
	 * @nowarn
	 */
	public void setContext(TokenContext context)
	{
		this.context = context;
	}

	/**
	 * Gets the lifecycle request.
	 * @nowarn
	 */
	public Integer getLifecycleRequest()
	{
		return lifecycleRequest;
	}

	/**
	 * Sets the lifecycle request.
	 * @nowarn
	 */
	public void setLifecycleRequest(Integer lifecycleRequest)
	{
		this.lifecycleRequest = lifecycleRequest;
	}

	/**
	 * Gets the lifecycle state.
	 * @return A constant of the {@link LifecycleState} class as Integer or null
	 */
	public Integer getLifecycleState()
	{
		return lifecycleState;
	}

	/**
	 * Sets the lifecycle state.
	 * @param lifecycleState A constant of the {@link LifecycleState} class as Integer or null
	 */
	public void setLifecycleState(Integer lifecycleState)
	{
		this.lifecycleState = lifecycleState;
	}
}
