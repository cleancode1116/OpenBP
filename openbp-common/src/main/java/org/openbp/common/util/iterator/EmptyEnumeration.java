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

/**
 * Convenience class that can serve as an empty Enumeration.
 * In order to use the class, retrieve an instance using the {@link #getInstance} method.
 *
 * @author Heiko Erhardt
 */
public final class EmptyEnumeration
	implements Enumeration
{
	/** The only instance */
	private static EmptyEnumeration singletonInstance = null;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	private EmptyEnumeration()
	{
	}

	/**
	 * Returns a global instance of this object.
	 * No problem, because this class does not contain any data.
	 * @nowarn
	 */
	public static synchronized EmptyEnumeration getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new EmptyEnumeration();
		return singletonInstance;
	}

	//////////////////////////////////////////////////
	// @@ implementation of java.util.Enumeration interface
	//////////////////////////////////////////////////

	/**
	 * Implementation of the Enumeration interface.
	 * @return Always false
	 */
	public boolean hasMoreElements()
	{
		return false;
	}

	/**
	 * Implementation of the Enumeration interface.
	 * @return Nothing, throws a NoSuchElementException instead
	 */
	public Object nextElement()
	{
		throw new IllegalStateException("Next operation not supported by EmptyEnumerator");
	}
}
