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
 * Convenience class that can serve as an empty Iterator.
 * In order to use the class, retrieve an instance using the {@link #getInstance} method.
 *
 * @author Heiko Erhardt
 */
public final class EmptyIterator
	implements Iterator
{
	/** The only instance */
	private static EmptyIterator singletonInstance = null;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	private EmptyIterator()
	{
	}

	/**
	 * Returns a global instance of this object.
	 * No problem, because this class does not contain any data.
	 * @nowarn
	 */
	public static synchronized EmptyIterator getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new EmptyIterator();
		return singletonInstance;
	}

	//////////////////////////////////////////////////
	// @@ implementation of java.util.Iterator interface
	//////////////////////////////////////////////////

	/**
	 * Implementation of Iterator interface.
	 * @return Always false
	 */
	public boolean hasNext()
	{
		return false;
	}

	/**
	 * Implementation of the Iterator interface.
	 * @return Nothing, throws a NoSuchElementException instead
	 */
	public Object next()
	{
		throw new IllegalStateException("Next operation not supported by EmptyIterator");
	}

	/**
	 * Implementation of the Iterator interface.
	 * Always throws an IllegalStateException.
	 */
	public void remove()
	{
		throw new IllegalStateException("Remove operation not supported by EmptyIterator");
	}
}
