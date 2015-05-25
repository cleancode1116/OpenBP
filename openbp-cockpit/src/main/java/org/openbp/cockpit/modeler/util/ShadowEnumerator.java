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

import org.openbp.cockpit.modeler.figures.generic.ShadowDropper;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.standard.CompositeFigure;

/**
 * Enumerator for the shadow figures of a given figure and its sub figures.
 * This class has a private constructor. Use the static {@link #enumerate} method.
 *
 * @author Jens Ferchland
 */
public class ShadowEnumerator
	implements FigureEnumeration
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Outer enumeration */
	private FigureEnumeration enOuter;

	/** Inner enumeration */
	private FigureEnumeration enInner;

	/** Next element to return */
	private Figure nextElement;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Static method that enumerates the shadows of the given figure.
	 *
	 * @param f Base figure
	 * @return The shadow enumeration
	 */
	public static FigureEnumeration enumerate(Figure f)
	{
		if ((f instanceof CompositeFigure) && (((CompositeFigure) f).figureCount() > 0))
		{
			return new ShadowEnumerator(f);
		}

		if (f instanceof ShadowDropper)
		{
			Figure shadow = ((ShadowDropper) f).getShadow();
			if (shadow != null)
				return new SingleFigureEnumerator(shadow);
		}

		return EmptyFigureEnumerator.getInstance();
	}

	/**
	 * Private constructor.
	 *
	 * @param figure Base figure
	 */
	private ShadowEnumerator(Figure figure)
	{
		enOuter = figure.figures();
		enInner = enumerate(enOuter.nextFigure());

		if (figure instanceof ShadowDropper)
		{
			nextElement = ((ShadowDropper) figure).getShadow();
			if (nextElement != null)
				return;
		}

		determineNext();
	}

	/**
	 * Determines the next element to return.
	 */
	private void determineNext()
	{
		nextElement = null;

		if (enInner.hasMoreElements())
		{
			nextElement = enInner.nextFigure();
		}
		else
		{
			while (enOuter.hasMoreElements() && !(enInner = enumerate(enOuter.nextFigure())).hasMoreElements())
			{
			}

			if (enInner.hasMoreElements())
			{
				nextElement = enInner.nextFigure();
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ FigureEnumeration implementation
	//////////////////////////////////////////////////

	public Object nextElement()
	{
		Figure f = nextElement;
		determineNext();
		return f;
	}

	public boolean hasMoreElements()
	{
		return nextElement != null;
	}

	public Figure nextFigure()
	{
		return (Figure) nextElement();
	}
}
