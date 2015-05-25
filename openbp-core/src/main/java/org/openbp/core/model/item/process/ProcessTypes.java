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

/**
 * Process types.
 *
 * @author Heiko Erhardt
 */
public final class ProcessTypes
{
	//////////////////////////////////////////////////
	// @@ Type constants
	//////////////////////////////////////////////////

	/**
	 * Process type 'BusinessProcess'
	 */
	public static final String BUSINESSPROCESS = "BusinessProcess";

	/**
	 * Process type 'TopLevel'
	 */
	public static final String TOPLEVEL = "TopLevel";

	/**
	 * Process type 'UserInterface'
	 */
	public static final String USERINTERFACE = "UserInterface";

	/**
	 * Process type 'Subprocess'
	 */
	public static final String SUBPROCESS = "Subprocess";

	/**
	 * Process type 'UseCase'
	 */
	public static final String USECASE = "UseCase";

	//////////////////////////////////////////////////
	// @@ Type descriptors
	//////////////////////////////////////////////////

	/** Table of all possible values */
	private static String [] allValues = { BUSINESSPROCESS, TOPLEVEL, USERINTERFACE, SUBPROCESS, USECASE, };

	/** Table of all possible values */
	private static String [] displayNames = { "Business process", "Top level", "User interface", "Subprocess", "Use Case", };

	/**
	 * Returns an array of possible values of this class.
	 * @nowarn
	 */
	public static String [] getValues()
	{
		return allValues;
	}

	/**
	 * Returns the display name of a process type.
	 *
	 * @param type Type to look for (one of the constants of this class)
	 * @return The display name or null if this is not a defined process type
	 */
	public static String getDisplayName(String type)
	{
		for (int i = 0; i < allValues.length; ++i)
		{
			if (allValues [i].equals(type))
				return displayNames [i];
		}
		return null;
	}

	/**
	 * Private constructor prevents instantiation
	 */
	private ProcessTypes()
	{
	}
}
