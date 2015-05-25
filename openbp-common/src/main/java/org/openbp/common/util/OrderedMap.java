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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Ordered map is a map which keeps the order of the incoming elements.
 * It is based on a list and a map.
 *
 * @author Andreas Putz
 */
public class OrderedMap extends HashMap
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Key list */
	private List keyList;

	/** Key set */
	private Set keySet;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public OrderedMap()
	{
	}

	/**
	 * Constructor.
	 *
	 * @param initialCapacity The initial capacity
	 */
	public OrderedMap(int initialCapacity)
	{
		super(initialCapacity);
		keyList = new ArrayList(initialCapacity);
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Gets a value by the index.
	 *
	 * @param index The index
	 *
	 * @return The value
	 */
	public Object getValue(int index)
	{
		return super.get(getKey(index));
	}

	/**
	 * Gets a key by the index.
	 *
	 * @param index The index
	 *
	 * @return The vkey
	 */
	public Object getKey(int index)
	{
		return keyList.get(index);
	}

	/**
	 * Gets the index of an object.
	 *
	 * @param key The key object
	 *
	 * @return The index or -1
	 */
	public int indexOf(Object key)
	{
		if (keyList == null)
			return -1;

		return keyList.indexOf(key);
	}

	/**
	 * Removes the last entry.
	 */
	public void removeLast()
	{
		if (keyList == null || keyList.isEmpty())
			return;

		Object key = keyList.get(keyList.size() - 1);
		remove(key);
	}

	/**
	 * Gets the last value.
	 *
	 * @return The value
	 */
	public Object getLastValue()
	{
		if (keyList == null || keyList.isEmpty())
			return null;

		return getValue(size() - 1);
	}

	/**
	 * Gets the last key.
	 *
	 * @return The key
	 */
	public Object getLastKey()
	{
		if (keyList == null || keyList.isEmpty())
			return null;

		return getKey(size() - 1);
	}

	//////////////////////////////////////////////////
	// @@ Map overridden methods
	/////////////////////////////////////////////////

	/**
	 * Adds an element to the map.
	 *
	 * @param key Key of the element to add
	 * @param value Element value
	 * @return The old value of the element or null
	 */
	public Object put(Object key, Object value)
	{
		if (keyList == null)
			keyList = new ArrayList();

		if (!keyList.contains(key))
			keyList.add(key);

		return super.put(key, value);
	}

	/**
	 * Removes an element from the map.
	 *
	 * @param key Key of the element to remove
	 * @return The old value of the element or null
	 */
	public Object remove(Object key)
	{
		if (keyList != null)
		{
			keyList.remove(key);
		}
		return super.remove(key);
	}

	/**
	 * Clears the map.
	 */
	public void clear()
	{
		if (keyList == null)
			return;
		keyList.clear();
	}

	/**
	 * Returns a set of all element keys in the correct order.
	 * @return The key set
	 */
	public Set keySet()
	{
		if (keyList == null)
			return null;
		Set ks = keySet;
		return (ks != null ? ks : (keySet = new KeySet()));
	}

	/**
	 * Returns a collection of all element values in the correct order.
	 * @return The values
	 */
	public Collection values()
	{
		int size = size();
		if (size == 0)
			return null;

		List list = new ArrayList(size);
		Iterator iterator = keyList.iterator();

		while (iterator.hasNext())
		{
			list.add(get(iterator.next()));
		}
		return list;
	}

	/**
	 * Key set implementation.
	 */
	private class KeySet extends AbstractSet
	{
		/**
		 * Clears the set.
		 */
		public void clear()
		{
			OrderedMap.this.clear();
		}

		/**
		 * Returns an iterator over the collection elements.
		 * @nowarn
		 */
		public Iterator iterator()
		{
			return keyList.iterator();
		}

		/**
		 * Removes an element from the set.
		 * @param o Object to remove
		 * @return
		 *		true	The element existed in the underlying map.<br>
		 *		false	No such element was found in the map.
		 */
		public boolean remove(Object o)
		{
			return OrderedMap.this.remove(o) != null;
		}

		/**
		 * Returns the size of the set.
		 * @nowarn
		 */
		public int size()
		{
			return keyList.size();
		}

		/**
		 * Checks if the set contains the specified key.
		 * @nowarn
		 */
		public boolean contains(Object o)
		{
			return containsKey(o);
		}
	}
}
