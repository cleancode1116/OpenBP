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
package org.openbp.common.io.xml.data;

import org.openbp.common.generic.description.DisplayObjectImpl;

/**
 * Test class for the XML driver.
 *
 * @author Heiko Erhardt
 */
public class TestProduct extends DisplayObjectImpl
	implements ITestProduct
{
	//////////////////////////////////////////////////
	// @@ Private data
	//////////////////////////////////////////////////

	/** Name */
	protected String name;

	/** City */
	protected String version;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor.
	 */
	public TestProduct()
	{
	}

	//////////////////////////////////////////////////
	// @@ Attributes
	//////////////////////////////////////////////////

	/**
	 * Gets the city.
	 * @nowarn
	 */
	public String getVersion()
	{
		return version;
	}

	/**
	 * Sets the city.
	 * @nowarn
	 */
	public void setVersion(String version)
	{
		this.version = version;
	}
}
