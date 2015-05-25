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

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * This class symbols an iterator that insures that similar objects are returned only once.
 *
 * @author Stephan Moritz
 */
public class DoubleSafeIterator
	implements Iterator
{
	/** The Iterator that we wrap around. */
	private Iterator internal;

	/** Already visited objects are kept in this vector. */
	private List visited = new Vector();

	/** contains the next object to return */
	private Object next = null;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Creates a new DoubleSafeIterator around a given one.
	 *
	 * @param base the iterator to wrap
	 */
	public DoubleSafeIterator(Iterator base)
	{
		internal = base;

		if (internal.hasNext())
			skipAhead();
	}

	//////////////////////////////////////////////////
	// @@ implementation of java.util.Iterator interface
	//////////////////////////////////////////////////

	/**
	 * Returns whether there are any more elements.
	 *
	 * @return true if there are objects left
	 */
	public boolean hasNext()
	{
		return next != null;
	}

	/**
	 * Returns the next element
	 *
	 * @return the element
	 */
	public Object next()
	{
		if (next == null)
		{
			throw new NoSuchElementException("no more elements");
		}

		Object result = next;
		visited.add(next);

		skipAhead();

		return result;
	}

	/**
	 * Always throws an UnsupportedOperationException removal is not supported by this iterator.
	 */
	public void remove()
	{
		throw new UnsupportedOperationException("remove operation not supported by ArrayIterator");
	}

	//////////////////////////////////////////////////
	// @@ Internal methods
	//////////////////////////////////////////////////

	/**
	 * Advances internal counter, skipping doublettes.
	 */
	protected void skipAhead()
	{
		next = internal.next();

		while (visited.contains(next))
		{
			if (internal.hasNext())
				next = internal.next();
			else
			{
				next = null;
				break;
			}
		}
	}
}
