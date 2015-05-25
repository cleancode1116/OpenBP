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

import java.lang.reflect.Array;
import java.util.Vector;

/**
 * This class contains various array and vector utilities.
 * @author Heiko Erhardt
 */
public final class ArrayUtil
{
	/**
	 * Do not instantiate this class!
	 */
	private ArrayUtil()
	{
	}

	/**
	 * Converts an Object[] array to an array of a given element class.
	 *
	 * @param objects Object[] array or null
	 * @param clazz The element class
	 *
	 * @return An array convertible to the element class array type
	 */
	public static Object [] toArray(Object [] objects, Class clazz)
	{
		if (objects == null)
			return null;
		Object [] result = (Object []) Array.newInstance(clazz, objects.length);
		System.arraycopy(objects, 0, result, 0, result.length);
		return result;
	}

	/**
	 * Converts a vector to an array of a given element class.
	 *
	 * @param v Vector
	 * @param clazz Element class of the array
	 *
	 * @return The array convertible to an array of the element class
	 */
	public static Object [] toArray(Vector v, Class clazz)
	{
		Object [] objects = (Object []) Array.newInstance(clazz, v.size());
		v.copyInto(objects);
		return objects;
	}

	/**
	 * Converts a vector to an array of Objects.
	 *
	 * @param v Vector
	 * @return The array
	 */
	public static Object [] toArray(Vector v)
	{
		Object [] objects = new Object [v.size()];
		v.copyInto(objects);
		return objects;
	}

	/**
	 * Converts an array to a vector.
	 *
	 * @param array Array
	 * @return The vector
	 */
	public static Vector toVector(Object [] array)
	{
		Vector vector = new Vector();

		for (int cnt = 0; cnt < array.length; cnt++)
		{
			vector.add(array [cnt]);
		}

		return vector;
	}
}
