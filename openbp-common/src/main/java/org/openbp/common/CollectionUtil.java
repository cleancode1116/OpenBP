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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.openbp.common.util.iterator.EmptyIterator;
import org.openbp.common.util.iterator.SingleIterator;

/**
 * Various static utility methods for collections, lists, maps etc.
 *
 * @author Heiko Erhardt
 */
public final class CollectionUtil
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Empty vector */
	public static final Vector EMPTY_VECTOR = new Vector(0);

	/**
	 * Private constructor prevents instantiation.
	 */
	private CollectionUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Object support
	//////////////////////////////////////////////////

	/**
	 * Checks if a collection contains the specified element.
	 * In contrast to Collection.contains, the comparison is done by reference
	 * ("==" instead of "equals" check), which improves performance.
	 *
	 * @param c Collection to search or null
	 * @param object Object to search for
	 * @return
	 *		true	The collection contains the specified object.<br>
	 *		false	The object has not been found in the collection
	 */
	public static boolean containsReference(Collection c, Object object)
	{
		if (c != null)
		{
			if (c instanceof ArrayList)
			{
				// Due to performance reasons, we try to avoid using iterators when traversing array lists
				ArrayList list = (ArrayList) c;
				int n = list.size();
				for (int i = 0; i < n; ++i)
				{
					Object o = list.get(i);
					if (o == object)
						return true;
				}
			}
			else
			{
				// For any other collection use an iterator to traverse it
				for (Iterator it = c.iterator(); it.hasNext();)
				{
					Object o = it.next();
					if (o == object)
						return true;
				}
			}
		}

		// Not found
		return false;
	}

	/**
	 * Removes an object from a collection.
	 * In contrast to Collection.contains, the comparison is done by reference
	 * ("==" instead of "equals" check), which improves performance.<br>
	 * Nothing happens if the collection does not contain the object.
	 *
	 * @param c Collection to search or null
	 * @param object Object to remove
	 * @throws UnsupportedOperationException if the \cremove\c operation is not supported
	 * by the iterator returned by c.iterator ().
	 */
	public static void removeReference(Collection c, Object object)
	{
		if (c != null)
		{
			for (Iterator it = c.iterator(); it.hasNext();)
			{
				Object o = it.next();
				if (o == object)
				{
					it.remove();
					return;
				}
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Iterator support
	//////////////////////////////////////////////////

	/**
	 * This method returns an iterator built based on the passed object.
	 * If the object is null, an empty iterator will be returned. If
	 * the object is an iterator, it is returned without conversion.
	 * If a collection or an array is passed, an iterator over the elements
	 * is returned. For any other object, an iterator
	 * containing the object as only element will be returned.
	 *
	 * @param o The object to be converted into an iterator
	 * @return The iterator (constructed as described above)
	 */
	public static Iterator iterator(Object o)
	{
		if (o == null)
		{
			return EmptyIterator.getInstance();
		}
		if (o instanceof Iterator)
		{
			return (Iterator) o;
		}
		if (o instanceof Collection)
		{
			return ((Collection) o).iterator();
		}
		if (o instanceof Map)
		{
			return ((Map) o).values().iterator();
		}
		if (o.getClass().isArray())
		{
			return Arrays.asList((Object []) o).iterator();
		}

		return new SingleIterator(o);
	}

	/**
	 * This method returns a collection built based on the passed object.
	 * If the object is null, null will be returned.
	 * A collection will be passed back directly.
	 * If the object is an iterator or an array, an array list containing its contents is returned.
	 * Otherwise, an array list containing the argument will be returned.
	 *
	 * @param o The object to be converted into an iterator
	 * @return The iterator (constructed as described above)
	 */
	public static Collection collection(Object o)
	{
		if (o == null)
		{
			return null;
		}
		if (o instanceof Collection)
		{
			return (Collection) o;
		}
		if (o instanceof Iterator)
		{
			ArrayList list = new ArrayList();
			for (Iterator it = (Iterator) o; it.hasNext();)
			{
				list.add(it.next());
			}
			return list;
		}
		if (o.getClass().isArray())
		{
			int l = Array.getLength(o);
			if (l == 0)
				return null;

			ArrayList list = new ArrayList();
			for (int i = 0; i < l; ++i)
			{
				list.add(Array.get(o, i));
			}
			return list;
		}

		ArrayList list = new ArrayList();
		list.add(o);
		return list;
	}

	/**
	 * Adds the elements of an enumeration to a collection.
	 * In order to add the elements of a collection itself, rather use the
	 * Collection.addAll method, which is optimized for this.
	 *
	 * @param c Collection to add the objects to
	 * @param it Iterator to add
	 */
	public static void addAll(Collection c, Iterator it)
	{
		if (it != null && c != null)
		{
			while (it.hasNext())
			{
				Object o = it.next();
				c.add(o);
			}
		}
	}

	/**
	 * Adds the elements of an enumeration to a collection.
	 * In order to add the elements of a collection itself, rather use the
	 * Collection.addAll method, which is optimized for this.
	 *
	 * @param c Collection to add the objects to
	 * @param en Enumeration to add
	 */
	public static void addAll(Collection c, Enumeration en)
	{
		if (en != null && c != null)
		{
			while (en.hasMoreElements())
			{
				Object o = en.nextElement();
				c.add(o);
			}
		}
	}

	/**
	 * Adds the elements of an array to a collection.
	 *
	 * @param c Collection to add the objects to
	 * @param array Array to add
	 */
	public static void addAll(Collection c, Object [] array)
	{
		if (array == null)
			return;

		for (int i = 0; i < array.length; ++i)
		{
			c.add(array [i]);
		}
	}

	/**
	 * Creates ArrayList from a iterator
	 *
	 * @param it Iterator
	 * @return ArrayList to create
	 */
	public static ArrayList iteratorToArrayList(Iterator it)
	{
		if (it != null)
		{
			ArrayList list = null;
			while (it.hasNext())
			{
				// Use lazy allocation to prevent calling hasNext() two times; some Iterator implementations
				// advance to the next record by calling hasNext() instead of next().
				if (list == null)
					list = new ArrayList();
				Object o = it.next();
				list.add(o);
			}
			return list;
		}
		return null;
	}

	//////////////////////////////////////////////////
	// @@ Array support
	//////////////////////////////////////////////////

	/**
	 * Gets the objects of a collection as type-safe array (copy).
	 * The array elements are of the specified class.
	 * If the collection contains an element of a different class, the method
	 * will generate a ClassCastException.
	 *
	 * @param c Collection of objects or null
	 * @param cls The type of the array elements or null to generate a generic Object array
	 * @return The object array of type \ccls[]\c or null if the collection is empty
	 */
	public static Object [] toArray(Collection c, Class cls)
	{
		if (c == null)
			return null;
		int n = c.size();
		if (n == 0)
			return null;

		Object [] result;
		if (cls != null)
			result = (Object []) Array.newInstance(cls, n);
		else
			result = new Object [n];

		Iterator it = c.iterator();
		for (int i = 0; i < n; ++i)
		{
			Object o = it.next();
			if (o != null && cls != null && !cls.isAssignableFrom(o.getClass()))
				throw new ClassCastException("Cant cast object of the type '" + o.getClass().getName() + "' to '" + cls.getName() + "'");
			result [i] = o;
		}

		return result;
	}

	/**
	 * Gets the objects of a collection as array of strings.
	 * The toString of each object is used to generate the object string representations.
	 *
	 * @param c Collection of objects or null
	 * @return The string array or null if the collection is empty
	 */
	public static String [] toStringArray(Collection c)
	{
		if (c == null)
			return null;
		int n = c.size();
		if (n == 0)
			return null;

		String [] result = new String [n];
		Iterator it = c.iterator();
		for (int i = 0; i < n; ++i)
		{
			Object o = it.next();
			result [i] = o != null ? o.toString() : null;
		}

		return result;
	}

	/**
	 * Adds the elements of the given collection to the given list in reverse order.
	 *
	 * @param col Collection to add
	 * @param result Result list<br>
	 * The collection elements will be added in reverse order to the end of the list.<br>
	 * Due to performance reasons, we suggest that the result list should be a LinkedList.
	 */
	public static void addReverseList(Collection col, List result)
	{
		int pos = result.size();

		for (Iterator iter = col.iterator(); iter.hasNext();)
		{
			result.add(pos, iter.next());
		}
	}

	//////////////////////////////////////////////////
	// @@ List support
	//////////////////////////////////////////////////

	/**
	 * Adds a object to a list and extends the list by null values
	 * if the index is greater than the size.
	 *
	 * @param list Any list object
	 * @param index The index position within the list
	 * @param element The element to add
	 */
	public static void add(List list, int index, Object element)
	{
		int size = list.size();

		if (index < size)
		{
			list.add(index, element);
			return;
		}

		for (int i = size; i < index; i++)
		{
			list.add(null);
		}

		list.add(element);
	}
}
