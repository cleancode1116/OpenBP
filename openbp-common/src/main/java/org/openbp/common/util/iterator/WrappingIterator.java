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

/**
 * Base class for an iterator that wraps another iterator.
 *
 * @author Heiko Erhardt
 */
public abstract class WrappingIterator
	implements Iterator
{
	/** Current object */
	private Object current;

	/** Contains the iterator we are based on */
	private Iterator basis;

	/**
	 * Default constructor.
	 */
	public WrappingIterator()
	{
	}

	/**
	 * Constructor.
	 *
	 * @param basis Contains the iterator we are based on
	 */
	public WrappingIterator(Iterator basis)
	{
		this.basis = basis;
	}

	/**
	 * Implementation of Iterator interface.
	 * @return Always false
	 */
	public boolean hasNext()
	{
		if (current == null)
		{
			current = retrieveCurrentObject(basis);
		}
		if (current != null)
			return true;
		return false;
	}

	/**
	 * Implementation of the Iterator interface.
	 * @return Nothing, throws a NoSuchElementException instead
	 */
	public Object next()
	{
		// Make sure there is a next element
		hasNext();

		Object ret = current;
		current = null;
		return ret;
	}

	/**
	 * Removes the current element (not supported).
	 * @throws UnsupportedOperationException Always
	 */
	public void remove()
	{
		throw new UnsupportedOperationException("Remove not supported by SessionAwareContextIterator class");
	}

	/**
	 * Retrieves current object by querying the underlying iterator.
	 * @param basis The underlying iterator
	 * @return The current object or null if the end of the underlying iterator has been reached.
	 */
	protected abstract Object retrieveCurrentObject(Iterator basis);
}
