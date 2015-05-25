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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.collections.comparators.ComparableComparator;

/**
 * Array list that sorts its contents.
 * If the {@link #setAutoSort} property is set to true, the list will sort after any add operation to the list.
 * Otherwise, the {@link #sort} method must be called.
 *
 * The list will use the comparator 
 * You may set the sorter using {@link #setComparator} method.
 *
 * @author Heiko Erhardt
 */
public class SortingArrayList
	extends ArrayList
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Element comparator */
	private Comparator comparator = ComparableComparator.getInstance();

	/** Auto sort property */
	private boolean autoSort = true;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public SortingArrayList()
	{
	}

	/**
	 * Gets the element comparator.
	 * @nowarn
	 */
	public Comparator getComparator()
	{
		return comparator;
	}

	/**
	 * Sets the element comparator.
	 * @nowarn
	 */
	public void setComparator(Comparator comparator)
	{
		this.comparator = comparator;
	}

	/**
	 * Gets the auto sort property.
	 * @nowarn
	 */
	public boolean isAutoSort()
	{
		return autoSort;
	}

	/**
	 * Sets the auto sort property.
	 * @nowarn
	 */
	public void setAutoSort(boolean autoSort)
	{
		this.autoSort = autoSort;
	}

	/**
	 * Sorts the list.
	 */
	public void sort()
	{
		if (size() > 1)
		{
			if (comparator != null)
				Collections.sort(this, comparator);
			else
				Collections.sort(this);
		}
	}

	/**
	 * Invokes the automatic sorting if enabled.
	 */
	protected void autoSort()
	{
		if (autoSort)
		{
			sort();
		}
	}

	//////////////////////////////////////////////////
	// @@ ArrayList overrides
	//////////////////////////////////////////////////

	public boolean add(Object o)
	{
		boolean ret = super.add(o);
		autoSort();
		return ret;
	}

	public void add(int index, Object element)
	{
		super.add(index, element);
		autoSort();
	}

	public boolean addAll(Collection c)
	{
		boolean ret = super.addAll(c);
		autoSort();
		return ret;
	}

	public boolean addAll(int index, Collection c)
	{
		boolean ret = super.addAll(index, c);
		autoSort();
		return ret;
	}
}
