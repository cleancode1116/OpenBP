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
package org.openbp.common.template.writer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openbp.common.CollectionUtil;
import org.openbp.common.util.OrderedMap;

/**
 * A placeholder in a template file will be substituted when the file is written for the actual content of the placeholder variable.
 *
 * @author Andreas Putz
 */
public class Placeholder
{
	//////////////////////////////////////////////////
	// @@ Symbolic Constants
	//////////////////////////////////////////////////

	/** List type int value */
	public static final int LIST_TYPE = 0;

	/** Hash type int value */
	public static final int HASH_TYPE = 1;

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Hash container */
	private Map hash;

	/** List container */
	private List list;

	/** Type of the placeholder */
	private int type;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 * @param type Placeholder type {@link #LIST_TYPE} or {@link #HASH_TYPE}
	 */
	public Placeholder(int type)
	{
		this.type = type;
		clear();
	}

	/**
	 * Clears the container.
	 */
	public void clear()
	{
		if (type == HASH_TYPE)
			hash = new OrderedMap();
		else if (type == LIST_TYPE)
			list = new ArrayList();
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Adds a string to the content.
	 * @nowarn
	 */
	public void add(String text)
	{
		if (type == HASH_TYPE)
			hash.put(text, text);
		else
			list.add(text);
	}

	/**
	 * Adds a key/text pair to the content.
	 * @nowarn
	 */
	public void add(String key, String text)
	{
		if (type == HASH_TYPE)
			hash.put(key, text);
		else
			list.add(text);
	}

	/**
	 * Checks if the placeholder contains the specified text.
	 *
	 * @param pattern Key or text (in case of list types) to search for
	 * @nowarn
	 */
	public boolean contains(String pattern)
	{
		if (type == HASH_TYPE)
		{
			if (hash.get(pattern) != null)
				return true;
		}
		else
		{
			int n = list.size();
			for (int i = 0; i < n; ++i)
			{
				String s = (String) list.get(i);
				if (s.equals(pattern))
					return true;
			}
		}
		return false;
	}

	/**
	 * Determines if the placeholder is empty.
	 * @nowarn
	 */
	public boolean isEmpty()
	{
		if (type == HASH_TYPE)
		{
			return hash.isEmpty();
		}
		return list.isEmpty();
	}

	/**
	 * Gets the content as array.
	 * @nowarn
	 */
	public String [] get()
	{
		if (type == HASH_TYPE)
		{
			// Sort hash
			String [] retVal = (String []) CollectionUtil.toArray(hash.values(), String.class);
			Arrays.sort(retVal);
			return retVal;
		}

		// No sort, retain the add order
		return (String []) CollectionUtil.toArray(list, String.class);
	}

	/**
	 * Gets the placeholder type.
	 * @nowarn
	 */
	public int getType()
	{
		return type;
	}
}
