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
package org.openbp.cockpit.modeler.figures.generic;

import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * The bar figure displays one or more horizontal bars.
 *
 * @author Heiko Erhardt
 */
public class XCircleCrossFigure extends XCircleFigure
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	public static final double PI4 = 0.70710678118654752440084436210485D;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public XCircleCrossFigure()
	{
	}

	//////////////////////////////////////////////////
	// @@ XFigure overrides
	//////////////////////////////////////////////////

	/**
	 * Draws additional frame parts.
	 *
	 * @param g Graphics to draw to
	 */
	protected void drawAppliances(Graphics g)
	{
		Rectangle r = ellipseBox();

		double xCenter = r.getCenterX();
		double yCenter = r.getCenterY();
		double radius = r.getWidth() / 2;

		g.drawLine((int) (xCenter - radius * PI4), (int) (yCenter - radius * PI4), (int) (xCenter + radius * PI4), (int) (yCenter + radius * PI4));
		g.drawLine((int) (xCenter - radius * PI4), (int) (yCenter + radius * PI4), (int) (xCenter + radius * PI4), (int) (yCenter - radius * PI4));
	}

	/**
	 * Draws the figure.
	 *
	 * @param g Graphics to draw to
	 */
	protected void drawFigure(Graphics g)
	{
		super.drawFigure(g);
	}
}
