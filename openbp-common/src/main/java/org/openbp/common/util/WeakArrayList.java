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

import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * A weak list works pretty identical to the java.util.ArrayList class except that
 * it usually refers its elements using a weak reference.
 * Weak references enable the garbage collector to remove objects though they are
 * still referenced through the weak ref. In this case, the reference returns null
 * (i. e. through the {@link #get(int)} method).<br>
 * Note that the list does not change its size though the references may be garbage-collected.
 * However, if you invoke the {@link #trim} method, 'dead' references are removed.
 *
 * Though the implementation of the java.util.List methods always add the elements
 * as weak references, it is also possible to insert elements as hard references using
 * the {@link #addHardReference(Object)} method, allowing to mix weak and hard references in a
 * single list. This is e. g. used by the {@link org.openbp.common.listener.ListenerSupport}
 * class, which stores the listeners as weak references, but the listener classes as regular
 * references in order to minimize object allocation.
 *
 * Note the the Iterator returned by the {@link java.util.AbstractList#iterator} method always returns
 * elements that are validly referenced by the list. The references in the list can be copied
 * to a list that contains regular (hard) references using the {@link #createHardList} method.
 *
 * @author Heiko Erhardt
 */
public class WeakArrayList extends AbstractList
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/* list of weak references */
	private ArrayList list;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public WeakArrayList()
	{
		list = new ArrayList();
	}

	/**
	 * Default constructor.
	 *
	 * @param initialCapacity Initial capacity of the list
	 */
	public WeakArrayList(int initialCapacity)
	{
		list = new ArrayList(initialCapacity);
	}

	//////////////////////////////////////////////////
	// @@ List implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the element at the given index.
	 *
	 * @param index Index into the list
	 * @return The element at the given position or null if the
	 * element has been garbage-collected.
	 */
	public Object get(int index)
	{
		return resolveReference(list.get(index));
	}

	/**
	 * Gets the size of the list.
	 *
	 * @return The size includes already collected objects
	 */
	public int size()
	{
		return list.size();
	}

	/**
	 * Sets the object at the given index as a weak reference.
	 *
	 * @param index List index
	 * @param element Element to set
	 * @return The previous object at the given index or null
	 * if the object was already collected
	 */
	public Object set(int index, Object element)
	{
		Object o = element != null ? new WeakReference(element) : null;
		return resolveReference(list.set(index, o));
	}

	/**
	 * Inserts the object at the given position in the list as a weak reference.
	 * Automatically creates a weak reference to the object.
	 *
	 * @param index List index
	 * @param element Element to add
	 */
	public void add(int index, Object element)
	{
		Object o = element != null ? new WeakReference(element) : null;
		list.add(index, o);
	}

	/**
	 * Removes the object at the given index.
	 *
	 * @param index The index of the element to be removed
	 * @return The removed element or null if it was already collected
	 */
	public Object remove(int index)
	{
		return resolveReference(list.remove(index));
	}

	//////////////////////////////////////////////////
	// @@ Additional methods
	//////////////////////////////////////////////////

	/**
	 * Adds the object at the end of the list as a regular (hard) reference.
	 *
	 * @param element Element to add
	 */
	public void addHardReference(Object element)
	{
		list.add(list.size(), element);
	}

	/**
	 * Inserts the object at the given position in the list as a regular (hard) reference.
	 *
	 * @param index List index
	 * @param element Element to add
	 */
	public void addHardReference(int index, Object element)
	{
		list.add(index, element);
	}

	/**
	 * Creates a list of hard references to the objects.
	 *
	 * @return A new list containing only the valid references of this list
	 */
	public List createHardList()
	{
		List result = new ArrayList();

		for (int i = 0; i < size(); i++)
		{
			Object tmp = get(i);
			if (tmp != null)
				result.add(tmp);
		}

		return result;
	}

	/**
	 * Trims the list by removing 'dead' references, i\. e\. collected elements.
	 */
	public void trim()
	{
		for (int i = size(); --i >= 0;)
		{
			if (get(i) == null)
			{
				remove(i);
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Extract the hard reference from the weak reference.
	 * @param o Object to extract from
	 * @return The referenced object if the argument is a living weak reference;<br>
	 * null if the object is a dead weak reference;<br>
	 * the argument itself if the object is not a weak reference
	 */
	private Object resolveReference(Object o)
	{
		if (o != null && o instanceof WeakReference)
			return ((WeakReference) o).get();
		return o;
	}
}
