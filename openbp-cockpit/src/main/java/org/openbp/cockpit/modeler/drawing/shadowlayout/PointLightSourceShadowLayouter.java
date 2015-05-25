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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

/**
 * @author Stephan Moritz
 */
public class PointLightSourceShadowLayouter
	implements ShadowLayouter
{
	private double lightX;

	private double lightY;

	private AffineTransform to = new AffineTransform();

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public PointLightSourceShadowLayouter(DrawingView view, double factor)
	{
		this.lightX = view.getSize().getWidth() / 2;
		this.lightY = view.getSize().getHeight() / 2;

		to.translate(lightX, lightY);
		to.scale(factor, factor);
		to.translate(-lightX, -lightY);
	}

	public void drawShadows(FigureEnumeration en, Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;

		AffineTransform trans = g2.getTransform();
		g2.transform(to);

		while (en.hasMoreElements())
		{
			Figure next = en.nextFigure();
			next.draw(g);
		}

		g2.setTransform(trans);
	}

	public Rectangle transformRectangle(Rectangle r)
	{
		Point2D c1 = new Point2D.Double(r.getMinX(), r.getMinY());
		Point2D c2 = new Point2D.Double(r.getMaxX(), r.getMaxY());

		c1 = to.transform(c1, null);

		Point p = new Point((int) c1.getX(), (int) c1.getY());

		Rectangle rec = new Rectangle(p);
		rec.add(to.transform(c2, null));

		return rec;
	}

	public void releaseShadowLayouter()
	{
	}
}
