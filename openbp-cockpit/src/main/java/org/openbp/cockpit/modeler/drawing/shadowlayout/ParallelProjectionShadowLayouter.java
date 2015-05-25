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
package org.openbp.cockpit.modeler.drawing.shadowlayout;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

/**
 * @author Stephan Moritz
 */
public class ParallelProjectionShadowLayouter
	implements ShadowLayouter
{
	private double dx;

	private double dy;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ParallelProjectionShadowLayouter(int dx, int dy)
	{
		this.dx = dx;
		this.dy = dy;
	}

	// implementation of org.openbp.cockpit.modeler.ShadowLayouter interface

	public void drawShadows(FigureEnumeration en, Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;

		AffineTransform trans = g2.getTransform();
		g2.translate(dx, dy);

		while (en.hasMoreElements())
		{
			Figure next = en.nextFigure();

			next.draw(g);
		}

		g2.setTransform(trans);
	}

	public Rectangle transformRectangle(Rectangle r)
	{
		Rectangle rec = new Rectangle(r);

		rec.translate((int) dx, (int) dy);
		return rec;
	}

	public void releaseShadowLayouter()
	{
	}
}
