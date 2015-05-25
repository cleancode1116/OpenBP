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
package org.openbp.common;

import java.util.Properties;

/**
 * General utility functions.
 *
 * @author Heiko Erhardt
 */
public final class CommonUtil
{
	//////////////////////////////////////////////////
	// @@ Static members
	//////////////////////////////////////////////////

	/** Java version as string (e\.g\. "1.4.1") */
	private static String javaVersionStr;

	/** Java version as 3 digit integer (e\.g\. 141) */
	private static int javaVersionInt;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private CommonUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ General
	//////////////////////////////////////////////////

	/**
	 * Checks if two objects are equal or both null.
	 *
	 * @param o1 First object
	 * @param o2 Second object
	 * @return
	 *		true	Both objects are null or equal (using Objects.equals)<br>
	 *		false	The objects are not equal or only one of them is null.
	 */
	public static boolean equalsNull(Object o1, Object o2)
	{
		if (o1 == null)
		{
			return o2 == null;
		}
		else if (o2 == null)
		{
			return false;
		}
		return o1.equals(o2);
	}

	/**
	 * Compares two objects that implement the java.lang.Comparable interface that may also be null.
	 *
	 * @param o1 First object or null
	 * @param o2 Second object or null
	 * @return
	 *		0	Both objects are null or equal<br>
	 *		&lt; 0	The first object os lexicographically smaller than the second object<br>
	 *		&gt; 0	The first object os lexicographically larger than the second object<br>
	 * @throws ClassCastException If one of the objects does not implement the Comparable interface
	 */
	public static int compareNull(Object o1, Object o2)
	{
		Comparable c1 = (Comparable) o1;
		Comparable c2 = (Comparable) o2;
		if (c1 == null)
		{
			if (c2 == null)
				return 0;
			return -1;
		}
		if (c2 == null)
			return 1;
		return c1.compareTo(c2);
	}

	/**
	 * Null-safe toString implementation
	 *
	 * @param o Object to print
	 * @return o != null ? o.toString : null
	 */
	public static String toStringNull(Object o)
	{
		return o != null ? o.toString() : null;
	}

	/**
	 * Rounds a float to an integer value using neares-number-rounding.
	 *
	 * @param v Value to round
	 * @return (int) Math.floor (v + 0.5f)
	 */
	public static int rnd(float v)
	{
		return (int) Math.floor(v + 0.5f);
	}

	/**
	 * Rounds a double to an integer value using neares-number-rounding.
	 *
	 * @param v Value to round
	 * @return (int) Math.floor (v + 0.5d)
	 */
	public static int rnd(double v)
	{
		return (int) Math.floor(v + 0.5d);
	}

	/**
	 * Sets the spcecified system property, including null value check.
	 * System.setProperty will cause a null pointer exception if the value is null.
	 * This method will remove the property from the system properties on null values.
	 *
	 * @param key Property name
	 * @param value Value or null
	 * @return The old property value
	 */
	public static String setSystemProperty(String key, String value)
	{
		String ret;

		if (value != null)
		{
			ret = System.setProperty(key, value);
		}
		else
		{
			// If no value has been specified, we have to remove the property
			ret = System.getProperty(key);

			if (ret != null)
			{
				Properties props = System.getProperties();
				props.remove(key);
			}
		}

		return ret;
	}

	//////////////////////////////////////////////////
	// @@ Java version check
	//////////////////////////////////////////////////

	/**
	 * Checks if the current Java version is 1\.4 or higher.
	 * @nowarn
	 */
	public static boolean isJava14()
	{
		return getJavaVersionInt() >= 140;
	}

	/**
	 * Gets the java version as string (e\.g\. "1.4.1").
	 * @nowarn
	 */
	public static String getJavaVersionStr()
	{
		determineJavaVersion();
		return javaVersionStr;
	}

	/**
	 * Gets the java version as 3 digit integer (e\.g\. 141).
	 * @nowarn
	 */
	public static int getJavaVersionInt()
	{
		determineJavaVersion();
		return javaVersionInt;
	}

	private static void determineJavaVersion()
	{
		if (javaVersionStr == null)
		{
			javaVersionStr = System.getProperty("java.version");

			int l = javaVersionStr.length();
			javaVersionInt = (javaVersionStr.charAt(0) - '0') * 100;
			if (l > 2)
			{
				javaVersionInt += (javaVersionStr.charAt(2) - '0') * 10;
			}
			if (l > 4)
			{
				javaVersionInt += (javaVersionStr.charAt(4) - '0') * 1;
			}
		}
	}
}
