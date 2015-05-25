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
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Vector;

import org.openbp.common.CollectionUtil;

/**
 * Extended triangle figure.
 *
 * @author Heiko Erhardt
 */
public abstract class XPolygonFigure extends XRectangleFigure
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Cached polygon of the triangle */
	protected Polygon polygon;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public XPolygonFigure()
	{
	}

	//////////////////////////////////////////////////
	// @@ XFigure overrides
	//////////////////////////////////////////////////

	/**
	 * @see CH.ifa.draw.figures.EllipseFigure#containsPoint (int x, int y)
	 */
	public boolean containsPoint(int x, int y)
	{
		Shape shape = createShape();
		return shape.contains(x, y);
	}

	/**
	 * Draws the figure.
	 *
	 * @param g Graphics to draw to
	 */
	protected void drawFigure(Graphics g)
	{
		Polygon p = getPolygon();
		g.fillPolygon(p);
	}

	/**
	 * Draws the frame of the figure.
	 *
	 * @param g Graphics to draw to
	 */
	protected void drawFrame(Graphics g)
	{
		Polygon p = getPolygon();
		g.drawPolygon(p);
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#changed()
	 */
	public void changed()
	{
		super.changed();

		// Clear polygon cache
		polygon = null;
	}

	//////////////////////////////////////////////////
	// @@ AbstractFigure overrides
	//////////////////////////////////////////////////

	/**
	 * @see CH.ifa.draw.contrib.TriangleFigure#handles()
	 */
	public Vector handles()
	{
		return CollectionUtil.EMPTY_VECTOR;
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#connectionInsets()
	 */
	public Insets connectionInsets()
	{
		Insets insets = null;

		Rectangle r = box;
		Orientation orientation = getOrientation();
		switch (orientation)
		{
		case BOTTOM:
			insets = new Insets(0, r.width / 2, r.height, r.width / 2);
			break;

		case LEFT:
			insets = new Insets(r.height / 2, r.width, r.height / 2, 0);
			break;

		case TOP:
			insets = new Insets(r.height, r.width / 2, 0, r.width / 2);
			break;

		case RIGHT:
			insets = new Insets(r.height / 2, 0, r.height / 2, r.width);
			break;
		}

		return insets;
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#center()
	 */
	public Point center()
	{
		return new Point((int) box.getCenterX(), (int) box.getCenterY());
	}

	//////////////////////////////////////////////////
	// @@ AbstractFigure overrides: Geometry support
	//////////////////////////////////////////////////

	/**
	 * Creates a shape object that defines the outline of the figure.
	 *
	 * @return The shape (a java.awt.geom.Rectangle2D)
	 */
	public Shape createShape()
	{
		return getPolygon();
	}

	//////////////////////////////////////////////////
	// @@ AbstractPolygonFigure geometry support
	//////////////////////////////////////////////////

	/**
	 * Returns the polygon describing the triangle.
	 * @nowarn
	 */
	public abstract Polygon getPolygon();
}
