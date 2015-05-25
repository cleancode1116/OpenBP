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
import java.util.NoSuchElementException;

/**
 * This class provides an iterator over a single object.
 * This is useful for Tree-like iterator (as used by CascadeIterator for example).
 * (Composite-pattern)
 *
 * @author Stephan Moritz
 */
public class SingleIterator
	implements Iterator
{
	//////////////////////////////////////////////////
	// @@ Private Members
	//////////////////////////////////////////////////

	/** the object itself */
	private Object single;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Creates an enumeration of its argument.
	 *
	 * @param single the object to "iterate" over
	 */
	public SingleIterator(Object single)
	{
		this.single = single;
	}

	//////////////////////////////////////////////////
	// @@ implementation of java.util.Iterator interface
	//////////////////////////////////////////////////

	/**
	 * Have we not yet returned the object.
	 *
	 * @return if the object is still unreturned
	 */
	public boolean hasNext()
	{
		return single != null;
	}

	/**
	 * Return the object if we have not already done so.
	 *
	 * @return the object
	 */
	public Object next()
	{
		if (single == null)
		{
			throw new NoSuchElementException("SingleIterator finished");
		}

		Object ret = single;
		single = null;
		return ret;
	}

	/**
	 * Always throws an UnsupportedOperationException removal is not supported by this iterator.
	 */
	public void remove()
	{
		throw new UnsupportedOperationException("remove operation not supported by SingleIterator");
	}
}
