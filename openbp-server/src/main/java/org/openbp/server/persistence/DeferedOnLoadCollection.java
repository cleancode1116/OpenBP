/*
 *   Copyright 2010 skynamics AG
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
package org.openbp.server.persistence;

import java.util.Collection;
import java.util.Iterator;

import org.openbp.common.util.iterator.WrappingIterator;

/**
 * List that calls onLoad for each element that is being accessed.
 *
 * @author Heiko Erhardt
 */
public class DeferedOnLoadCollection implements Collection
{
	/** Base list */
	private Collection root;

	/** Persistence context that loaded the list */
	private PersistenceContext pc;

	/**
	 * Default constructor.
	 *
	 * @param root Base list
	 * @param pc Persistence context that loaded the list
	 */
	public DeferedOnLoadCollection(Collection root, PersistenceContext pc)
	{
		this.root = root;
		this.pc = pc;
	}

	public Iterator iterator()
	{
		Iterator basis = root.iterator();
		Iterator ret = new WrappingIterator (basis)
		{
			/**
			 * Retrieves current object by querying the underlying iterator.
			 * @param basis The underlying iterator
			 * @return The current object or null if the end of the underlying iterator has been reached.
			 */
			protected Object retrieveCurrentObject(Iterator basis)
			{
				if (basis.hasNext())
				{
					Object o = basis.next();
					pc.getPersistenceContextProvider().fireOnLoad(o, pc);
					return o;
				}

				return null;
			}
		};
		return ret;
	}

	public boolean add(Object e)
	{
		return root.add(e);
	}

	public boolean addAll(Collection c)
	{
		return root.addAll(c);
	}

	public void clear()
	{
		root.clear();
	}

	public boolean contains(Object o)
	{
		return root.contains(o);
	}

	public boolean containsAll(Collection c)
	{
		return root.containsAll(c);
	}

	public boolean isEmpty()
	{
		return root.isEmpty();
	}

	public boolean remove(Object o)
	{
		return root.remove(o);
	}

	public boolean removeAll(Collection c)
	{
		return root.removeAll(c);
	}

	public boolean retainAll(Collection c)
	{
		return root.retainAll(c);
	}

	public int size()
	{
		return root.size();
	}

	public Object[] toArray()
	{
		return root.toArray();
	}

	public Object[] toArray(Object[] a)
	{
		return root.toArray(a);
	}

	@Override
	public boolean equals(Object obj)
	{
		return root.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return root.hashCode();
	}
}
