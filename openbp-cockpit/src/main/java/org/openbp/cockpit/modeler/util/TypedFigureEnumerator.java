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
import CH.ifa.draw.standard.CompositeFigure;

/**
 * Figure enumerator that returns only figures of the specified type.
 * This class is a singleton.
 *
 * @author Heiko Erhardt
 */
public class TypedFigureEnumerator
	implements FigureEnumeration
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Enumeration of all child figures of the parent figure */
	private FigureEnumeration en;

	/** Class of the figures to return */
	private Class figureClass;

	/** Current figure to return */
	private Figure currentFigure;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Creates a forward enumeration of matching figures.
	 *
	 * @param parentFigure Parent figure to enumerate
	 * @param figureClass Class of the figures to return
	 */
	public TypedFigureEnumerator(Figure parentFigure, Class figureClass)
	{
		this(parentFigure, figureClass, false);
	}

	/**
	 * Constructor.
	 *
	 * @param parentFigure Parent figure to enumerate
	 * @param figureClass Class of the figures to return
	 * @param reverse
	 * true: Return the figures in reverse order (only supported for CompositeFigure objects)<br>
	 * false: Return the figures in regular order
	 */
	public TypedFigureEnumerator(Figure parentFigure, Class figureClass, boolean reverse)
	{
		if (reverse && parentFigure instanceof CompositeFigure)
		{
			en = ((CompositeFigure) parentFigure).figuresReverse();
		}
		else
		{
			en = parentFigure.figures();
		}

		advance();
	}

	//////////////////////////////////////////////////
	// @@ FigureEnumeration implementation
	//////////////////////////////////////////////////

	/**
	 * @see CH.ifa.draw.framework.FigureEnumeration#hasMoreElements()
	 */
	public boolean hasMoreElements()
	{
		return currentFigure != null;
	}

	/**
	 * @see CH.ifa.draw.framework.FigureEnumeration#nextElement()
	 */
	public Object nextElement()
	{
		return nextFigure();
	}

	/**
	 * @see CH.ifa.draw.framework.FigureEnumeration#nextFigure()
	 */
	public Figure nextFigure()
	{
		Figure ret = currentFigure;
		advance();
		return ret;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Advances to the next figure of the matching type.
	 */
	private void advance()
	{
		currentFigure = null;

		while (en.hasMoreElements())
		{
			Object o = en.nextElement();
			if (figureClass.isInstance(o))
			{
				// Found a matching element
				currentFigure = (Figure) o;
				break;
			}
		}
	}
}
