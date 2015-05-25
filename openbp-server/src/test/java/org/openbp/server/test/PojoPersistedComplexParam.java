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
package org.openbp.server.test;

import java.io.Serializable;

/**
 * Complex workflow parameter that is managed by Hibernate.
 *
 * @author Heiko Erhardt
 */
public class PojoPersistedComplexParam
	implements PersistedComplexParam, Serializable

{
	/** Id */
	private int id;

	/** Title of the workflow task */
	private String title;

	/** Result value */
	private int result;

	/**
	 * Default constructor.
	 */
	public PojoPersistedComplexParam()
	{
	}

	/**
	 * Gets the id.
	 * @nowarn
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Sets the id.
	 * @nowarn
	 */
	public void setId(int id)
	{
		this.id = id;
	}

	/**
	 * Gets the title of the workflow task.
	 * @nowarn
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * Sets the title of the workflow task.
	 * @nowarn
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * Gets the result value.
	 * @nowarn
	 */
	public int getResult()
	{
		return result;
	}

	/**
	 * Sets the result value.
	 * @nowarn
	 */
	public void setResult(int result)
	{
		this.result = result;
	}
}
