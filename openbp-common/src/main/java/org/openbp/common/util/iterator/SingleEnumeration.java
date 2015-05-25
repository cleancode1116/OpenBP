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
import java.util.NoSuchElementException;

/**
 * This class provides an enumerator over a single object.
 * This is useful for Tree-like enumerations (as used by CascadeEnumeration for example).
 * (Composite-pattern)
 *
 * @author Stephan Moritz
 */
class SingleEnumeration
	implements Enumeration
{
	//////////////////////////////////////////////////
	// @@ Private Members
	//////////////////////////////////////////////////

	/** have we already returned the object ? */
	private boolean more = true;

	/** the object itself */
	private Object single = null;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Creates an enumeration of its argument.
	 *
	 * @param single the object to "enumerate" over
	 */
	public SingleEnumeration(Object single)
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
	public boolean hasMoreElements()
	{
		return more;
	}

	/**
	 * Return the object if we have not already done so.
	 *
	 * @return the object
	 */
	public Object nextElement()
	{
		if (!more)
		{
			throw new NoSuchElementException("SingleEnumeration finished");
		}

		more = false;
		return single;
	}
}
