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
package org.openbp.jaspira.event;

/**
 * Simple integer class used for return codes of Jaspira event handler methods.
 * This class is used for easy detection of event handler methods by reflection.
 * It should be used solely for this purpose.
 *
 * @author Heiko Erhardt
 */
public class JaspiraEventHandlerCode
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Event handler return code */
	private String code;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 * @param code Code represented by this object
	 */
	public JaspiraEventHandlerCode(String code)
	{
		this.code = code;
	}

	/**
	 * Returns a string representation of this object.
	 *
	 * @return The code
	 */
	public String toString()
	{
		return code;
	}

	/**
	 * Gets the event handler return code.
	 * @nowarn
	 */
	public String getCode()
	{
		return code;
	}
}
