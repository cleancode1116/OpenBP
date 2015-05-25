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
 * Lifecycle request values.
 *
 * @author Heiko Erhardt
 */
public final class LifecycleRequest
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private LifecycleRequest()
	{
	}

	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	/** No request */
	public static final int NONE = 0;

	/** Resume process execution */
	public static final int RESUME = 1;

	/** Suspand process immediately */
	public static final int SUSPEND_IMMEDIATE = 2;

	/** Suspand process at next transaction boundary */
	public static final int SUSPEND_TRANSACTION = 3;

	/** Suspand process immediately */
	public static final int SUSPEND_MEMORY = 4;

	/** Stop execution (commit) */
	public static final int STOP = 5;

	/** Abort execution (rollback) */
	public static final int ABORT = 6;

	/** User-specific lifecycle request 1 */
	public static final int USER1 = 101;

	/** User-specific lifecycle request 2 */
	public static final int USER2 = 102;

	/** User-specific lifecycle request 1 */
	public static final int USER3 = 103;

	private static final HashMap value2Name = new HashMap();
	static
	{
		value2Name.put(Integer.valueOf(NONE), "NONE");
		value2Name.put(Integer.valueOf(RESUME), "RESUME");
		value2Name.put(Integer.valueOf(SUSPEND_IMMEDIATE), "SUSPEND_IMMEDIATE");
		value2Name.put(Integer.valueOf(SUSPEND_TRANSACTION), "SUSPEND_TRANSACTION");
		value2Name.put(Integer.valueOf(SUSPEND_MEMORY), "SUSPEND_MEMORY");
		value2Name.put(Integer.valueOf(STOP), "STOP");
		value2Name.put(Integer.valueOf(ABORT), "ABORT");
		value2Name.put(Integer.valueOf(USER1), "USER1");
		value2Name.put(Integer.valueOf(USER2), "USER2");
		value2Name.put(Integer.valueOf(USER3), "USER3");
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
