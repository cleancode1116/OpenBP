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
package org.openbp.cockpit.modeler.util;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

/**
 * Empty figure enumerator.
 * This class is a singleton.
 *
 * @author Heiko Erhardt
 */
public final class EmptyFigureEnumerator
	implements FigureEnumeration
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Singleton instance */
	private static EmptyFigureEnumerator singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized EmptyFigureEnumerator getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new EmptyFigureEnumerator();
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private EmptyFigureEnumerator()
	{
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * @see CH.ifa.draw.framework.FigureEnumeration#hasMoreElements()
	 */
	public boolean hasMoreElements()
	{
		return false;
	}

	/**
	 * @see CH.ifa.draw.framework.FigureEnumeration#nextElement()
	 */
	public Object nextElement()
	{
		return null;
	}

	/**
	 * @see CH.ifa.draw.framework.FigureEnumeration#nextFigure()
	 */
	public Figure nextFigure()
	{
		return null;
	}
}
