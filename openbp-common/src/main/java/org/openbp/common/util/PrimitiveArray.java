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
package org.openbp.common.util;

/**
 * Contains an array of a primitive data type, which is
 * specified by the set method and provides methods to access
 * this array easily.
 *
 * @author Andreas Putz
 */
public class PrimitiveArray
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Ensured capacity */
	private int capacity = 0;

	/** old capacity */
	private int oldCapacity = 0;

	/** Size of the array */
	private int size = 0;

	/** Array */
	private Object array;

	/** Default null return value */
	private int defaultReturn = -1;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public PrimitiveArray()
	{
		this(10);
	}

	/**
	 * Constructor.
	 * @param capacity The initial capacity of the array
	 */
	public PrimitiveArray(int capacity)
	{
		this.capacity = capacity;
	}

	/**
	 * Initializes the array.
	 */
	private void initialize()
	{
		array = new int [capacity];
		oldCapacity = capacity;
		clear();
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Ensures the capacity of the array.
	 *
	 * @param capacity The capacity of the array
	 * which is to ensure
	 */
	public void ensureCapacity(int capacity)
	{
		if (capacity > this.capacity)
			this.capacity = capacity;
	}

	/**
	 * Gets the length of the array.
	 * @nowarn
	 */
	public int getSize()
	{
		return size;
	}

	/**
	 * Removes all of the elements from this list. The list will
	 * be empty after this call returns.
	 */
	public void clear()
	{
		clear(0);
	}

	/**
	 * Clear the array beginning from the index.
	 *
	 * @param index The start index to clear
	 */
	public void clear(int index)
	{
		if (array == null)
			return;

		if (array instanceof int [])
		{
			int [] intArray = (int []) array;
			for (int i = index; i < intArray.length; i++)
			{
				intArray [i] = defaultReturn;
			}

			size = index;
		}
	}

	//////////////////////////////////////////////////
	// @@ Integer setter and getter
	//////////////////////////////////////////////////

	/**
	 * Sets the integer value at the index.
	 *
	 * @param index The array index
	 * @param value The integer value
	 */
	public void set(int index, int value)
	{
		if (array == null)
			initialize();

		int [] intArray = ensureIntCapacity();

		intArray [index] = value;

		size = size < index ? index : size;
		size++;
	}

	/**
	 * Gets the integer value by the index.
	 *
	 * @param index The array index
	 *
	 * @return The integer value
	 */
	public int get(int index)
	{
		if (array == null || index >= size)
			return defaultReturn;

		return ((int []) array) [index];
	}

	/**
	 * Adds a integer value.
	 *
	 * @param value The integer value to add
	 */
	public void add(int value)
	{
		if (array == null)
			initialize();

		int [] intArray = ensureIntCapacity();

		intArray [size++] = value;
	}

	/**
	 * Sets the default return value.
	 * @nowarn
	 */
	public void setDefaultReturnValue(int value)
	{
		defaultReturn = value;
	}

	//////////////////////////////////////////////////
	// @@ Integer helper methods
	//////////////////////////////////////////////////

	/**
	 * Ensures the capacity of the int array member.
	 *
	 * @return The int array
	 */
	private int [] ensureIntCapacity()
	{
		if (size == capacity)
			capacity = capacity * 3 / 2 + 1;
		if (oldCapacity < capacity || size == capacity)
		{
			int [] intArray = new int [capacity];
			copyArray(intArray);
			return intArray;
		}

		return (int []) array;
	}

	//////////////////////////////////////////////////
	// @@ Common helper methods
	//////////////////////////////////////////////////

	/**
	 * Copies an array object to the destination and set
	 * this one to the new one array member.
	 *
	 * @param destinationArray Array of any type to copy in the old values
	 * and set as member
	 */
	private void copyArray(Object destinationArray)
	{
		if (array == null)
			return;
		System.arraycopy(array, 0, destinationArray, 0, oldCapacity);
		oldCapacity = capacity;
		array = destinationArray;
	}
}
