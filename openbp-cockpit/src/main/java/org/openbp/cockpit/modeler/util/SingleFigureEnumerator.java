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
 * Enumerator for a single figure.
 *
 * @author Jens Ferchland
 */
public class SingleFigureEnumerator
	implements FigureEnumeration
{
	/** Figure */
	private Figure figure;

	/**
	 * Constructor.
	 *
	 * @param figure Figure to enumerate
	 */
	public SingleFigureEnumerator(Figure figure)
	{
		this.figure = figure;
	}

	//////////////////////////////////////////////////
	// @@ FigureEnumeration implementation
	//////////////////////////////////////////////////

	public Object nextElement()
	{
		Figure f = figure;
		figure = null;
		return f;
	}

	public boolean hasMoreElements()
	{
		return figure != null;
	}

	public Figure nextFigure()
	{
		return (Figure) nextElement();
	}
}
