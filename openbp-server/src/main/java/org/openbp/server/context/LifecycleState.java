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

import java.util.HashMap;

/**
 * Lifecycle state values.
 *
 * @author Heiko Erhardt
 */
public final class LifecycleState
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private LifecycleState()
	{
	}

	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	/** Process was just created */
	public static final int CREATED = 0;

	/** Process state was suspended */
	public static final int SUSPENDED = 1;

	/** Process state was selected for execution */
	public static final int SELECTED = 2;

	/** Process is currently running */
	public static final int RUNNING = 3;

	/** Execution was terminated normally (programmatically or by final node) */
	public static final int COMPLETED = 4;

	/** Execution was aborted */
	public static final int ABORTED = 5;

	/** Execution was stopped due to an unrecoverable exception */
	public static final int ERROR = 6;

	/** Process received a memory suspend request and is idling until resumption */
	public static final int IDLING = 7;

	private static final HashMap value2Name = new HashMap();
	static
	{
		value2Name.put(Integer.valueOf(SUSPENDED), "SUSPENDED");
		value2Name.put(Integer.valueOf(SELECTED), "SELECTED");
		value2Name.put(Integer.valueOf(RUNNING), "RUNNING");
		value2Name.put(Integer.valueOf(COMPLETED), "COMPLETED");
		value2Name.put(Integer.valueOf(ABORTED), "ABORTED");
		value2Name.put(Integer.valueOf(ERROR), "ERROR");
		value2Name.put(Integer.valueOf(IDLING), "IDLING");
	}

	/**
	 * Returns the string representation of the provided enumeratin value.
	 * @param x Enumeration value
	 * @return A string or null on unknown enumeration value
	 */
	public static final String toString(int x)
	{
		String ret = (String) value2Name.get(Integer.valueOf(x));
		if (ret == null)
			ret = "(" + x + ")";
		return ret;
	}
}
