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
package org.openbp.cockpit.generator;

/**
 * Container object for a generator property value.
 *
 * @author Heiko Erhardt
 */
public class GeneratorProperty
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Name */
	private String name;

	/** Value */
	private String value;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public GeneratorProperty()
	{
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the name.
	 * @nowarn
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name.
	 * @nowarn
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the value.
	 * @nowarn
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * Sets the value.
	 * @nowarn
	 */
	public void setValue(String value)
	{
		this.value = value;
	}
}
