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
package org.openbp.common.util.iterator;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Array enumerator class.
 *
 * Implements the Enumerator interface to get an enumerator for objects contained
 * in an array.
 *
 * @author Heiko Erhardt
 */
public class ArrayEnumerator
	implements Enumeration
{
	/** Array to enumerate */
	private Object [] array;

	/** Number of elements to iterate up to */
	private int count;

	/** Current index into array */
	private int index;

	/**
	 * Default constructor.
	 * The enumerator will enumerate all items in the array.
	 *
	 * @param array Array to enumerate
	 */
	public ArrayEnumerator(Object [] array)
	{
		this.array = array;
		index = 0;
		count = array.length;
	}

	/**
	 * Constructor.
	 * The enumerator will enumerate all items in the array, starting with index 'start'
	 * and ending with index 'start' + 'count'.
	 *
	 * @param array Array to enumerate
	 * @param start Index of the first object to return
	 * @param count Number of objects to enumerate
	 */
	public ArrayEnumerator(Object [] array, int start, int count)
	{
		this.array = array;
		this.index = start;
		if (start >= array.length)
			throw new NoSuchElementException("Invalid ArrayEnumerator index value");
		this.count = start + count;
		if (this.count > array.length)
			throw new NoSuchElementException("Invalid ArrayEnumerator index value");
	}

	/**
	 * Checks if there are more array elements.
	 * @nowarn
	 */
	public boolean hasMoreElements()
	{
		return index < count;
	}

	/**
	 * Gets the current array element and advances the current position.
	 *
	 * @return The object at the current position
	 */
	public Object nextElement()
	{
		if (index < count)
		{
			return array [index++];
		}
		throw new NoSuchElementException("ArrayEnumerator");
	}
}
