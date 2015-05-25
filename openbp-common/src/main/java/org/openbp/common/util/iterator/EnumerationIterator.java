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

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Enumerates from an iterated object.
 *
 * @author Peter Kalmbach
 */
public class EnumerationIterator
	implements Iterator
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Collection */
	private Enumeration enumeration;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param enumeration The enumeration to iterate
	 */
	public EnumerationIterator(Enumeration enumeration)
	{
		this.enumeration = enumeration;
	}

	//////////////////////////////////////////////////
	// @@ Enumation implementation
	//////////////////////////////////////////////////

	/**
	 * Tests if this enumeration contains more elements.
	 *
	 * @return  <code>true</code> if and only if this enumeration object
	 *           contains at least one more element to provide;
	 *          <code>false</code> otherwise.
	 */
	public boolean hasNext()
	{
		return enumeration.hasMoreElements();
	}

	/**
	 * Returns the next element of this enumeration if this enumeration
	 * object has at least one more element to provide.
	 *
	 * @return the next element of this enumeration
	 * @exception  NoSuchElementException  if no more elements exist.
	 */
	public Object next()
	{
		return enumeration.nextElement();
	}

	/**
	 * Removes from the underlying collection the last element returned by the iterator
	 * (optional operation). This method can be called only once per call to next.
	 *
	 * @exception  UnsupportedOperationException  The remove operation is not supported by this Iterator.
	 */
	public void remove()
	{
		throw new UnsupportedOperationException("The remove operation is not supported by this Iterator.");
	}
}
