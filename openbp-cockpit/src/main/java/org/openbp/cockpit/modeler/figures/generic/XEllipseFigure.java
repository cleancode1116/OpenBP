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
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RectangularShape;

import org.openbp.common.CommonUtil;

import CH.ifa.draw.figures.ChopEllipseConnector;
import CH.ifa.draw.framework.Connector;

/**
 * Extended ellipse figure.
 *
 * @author Heiko Erhardt
 */
public class XEllipseFigure extends XRectangleFigure
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Number of frames */
	protected int frameCount = 1;

	/** Distance between figure and frame */
	private int frameDistance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public XEllipseFigure()
	{
	}

	/**
	 * Returns the rectangle that determines the ellipse.
	 * Usually, this equals the display box.
	 * However, if some subclass wants to constrain the ellipse, this may differ.
	 *
	 * @return The ellipse rectangle
	 */
	public Rectangle ellipseBox()
	{
		return box;
	}

	//////////////////////////////////////////////////
	// @@ AbstractFigure overrides
	//////////////////////////////////////////////////

	/**
	 * Initializes the figure after all information from the figure descriptor has been set.
	 * Used to parse tagged values of the figure descriptor:<br>
	 * arc-width<br>
	 * arc-height<br>
	 */
	public void initialize()
	{
		super.initialize();

		XFigureDescriptor descriptor = getDescriptor();
		if (descriptor != null)
		{
			frameDistance = descriptor.getCustomIntAttributeValue("frame-distance", frameDistance);
			frameCount = descriptor.getCustomIntAttributeValue("frame-count", frameCount);
		}
	}

	/**
	 * @see CH.ifa.draw.figures.EllipseFigure#containsPoint (int x, int y)
	 */
	public boolean containsPoint(int x, int y)
	{
		Shape shape = createShape();
		return shape.contains(x, y);
	}

	/**
	 * @see CH.ifa.draw.figures.EllipseFigure#connectionInsets ()
	 */
	public Insets connectionInsets()
	{
		Rectangle r = ellipseBox();
		int cx = r.width / 2;
		int cy = r.height / 2;
		return new Insets(cy, cx, cy, cx);
	}

	/**
	 * @see CH.ifa.draw.figures.EllipseFigure#connectorAt (int x, int y)
	 */
	public Connector connectorAt(int x, int y)
	{
		return new ChopEllipseConnector(this);
	}

	//////////////////////////////////////////////////
	// @@ AbstractFigure overrides: Geometry support
	//////////////////////////////////////////////////

	private static final int SECTOR_E = 1;

	private static final int SECTOR_SE = 2;

	private static final int SECTOR_S = 3;

	private static final int SECTOR_SW = 4;

	private static final int SECTOR_W = 5;

	private static final int SECTOR_NW = 6;

	private static final int SECTOR_N = 7;

	private static final int SECTOR_NE = 8;

	/**
	 * Places the given rectangle so that its center is in the given direction
	 * and it exactly touches the node, without crossing any lines.
	 * @param rect The rectangle to adjust
	 * @param angle The direction in which the rectangle should be placed
	 * @return The translated rectangle
	 */
	public Rectangle placeAdjacent(Rectangle rect, double angle)
	{
		double xRectCenter = rect.getWidth() / 2;
		double yRectCenter = rect.getHeight() / 2;

		Rectangle r = ellipseBox();
		double boxCenter = r.getHeight() / 2;

		double alpha = Math.atan((yRectCenter / (boxCenter + xRectCenter)));
		alpha = CircleConstants.normalizeAngle(alpha);

		double beta = Math.atan((yRectCenter + boxCenter) / xRectCenter);
		beta = CircleConstants.normalizeAngle(beta);

		double correction = Math.PI / (2d * (beta - alpha));

		// This tedious if/else if construction determines in which
		// section of the circle we are
		int sector;
		if (angle < alpha || angle > 2d * Math.PI - alpha)
			sector = SECTOR_E;
		else if (angle < beta)
			sector = SECTOR_SE;
		else if (angle < Math.PI - beta)
			sector = SECTOR_S;
		else if (angle < Math.PI - alpha)
			sector = SECTOR_SW;
		else if (angle < Math.PI + alpha)
			sector = SECTOR_W;
		else if (angle < Math.PI + beta)
			sector = SECTOR_NW;
		else if (angle < 2d * Math.PI - beta)
			sector = SECTOR_N;
		else
			sector = SECTOR_NE;

		double x = 0;
		double y = 0;
		double newAngle = angle;
		switch (sector)
		{
		case SECTOR_E:
			x = boxCenter + xRectCenter;
			y = Math.tan(newAngle) * (boxCenter + xRectCenter);
			break;

		case SECTOR_SE:
			// We are in the se (round) section
			newAngle = correction * (newAngle - alpha);

			x = boxCenter * Math.cos(newAngle) + xRectCenter;
			y = boxCenter * Math.sin(newAngle) + yRectCenter;
			break;

		case SECTOR_S:
			// s (horizontal) section
			x = (boxCenter + yRectCenter) / Math.tan(newAngle);
			y = boxCenter + yRectCenter;
			break;

		case SECTOR_SW:
			// sw (round) section
			newAngle = correction * (newAngle - Math.PI + beta) + Math.PI / 2d;

			x = boxCenter * Math.cos(newAngle) - xRectCenter;
			y = boxCenter * Math.sin(newAngle) + yRectCenter;
			break;

		case SECTOR_W:
			// W (vertical) section
			x = -(boxCenter + xRectCenter);
			y = -(Math.tan(newAngle) * (boxCenter + xRectCenter));
			break;

		case SECTOR_NW:
			// NW (round) section
			newAngle = correction * (newAngle - Math.PI - alpha) + Math.PI;

			x = boxCenter * Math.cos(newAngle) - xRectCenter;
			y = boxCenter * Math.sin(newAngle) - yRectCenter;
			break;

		case SECTOR_N:
			// N (horizontal) section
			x = -((boxCenter + yRectCenter) / Math.tan(newAngle));
			y = -(boxCenter + yRectCenter);
			break;

		case SECTOR_NE:
			// ne (round) section
			newAngle = correction * (newAngle - 2d * Math.PI + beta) + 3d * Math.PI / 2d;

			x = boxCenter * Math.cos(newAngle) + xRectCenter;
			y = boxCenter * Math.sin(newAngle) - yRectCenter;
			break;
		}

		Point c = center();
		int xPos = c.x + CommonUtil.rnd(x - xRectCenter);
		int yPos = c.y + CommonUtil.rnd(y - yRectCenter);
		rect.setLocation(xPos, yPos);

		return rect;
	}

	/**
	 * Creates a shape object that defines the outline of the figure.
	 *
	 * @return The shape (a java.awt.geom.Ellipse2D)
	 */
	public Shape createShape()
	{
		return createRectangularShape();
	}

	/**
	 * Creates a rectangular shape object that defines the outline of the figure.
	 *
	 * @return The shape (a java.awt.geom.Ellipse2D)
	 */
	public RectangularShape createRectangularShape()
	{
		// Create the shape
		Ellipse2D shape = new Ellipse2D.Double();

		// Apply the current figure dimensions to it
		Rectangle r = ellipseBox();
		shape.setFrame(r);

		return shape;
	}

	//////////////////////////////////////////////////
	// @@ XFigure overrides
	//////////////////////////////////////////////////

	/**
	 * Draws the figure.
	 *
	 * @param g Graphics to draw to
	 */
	protected void drawFigure(Graphics g)
	{
		Rectangle r = ellipseBox();

		if (frameDistance > 0)
		{
			r = new Rectangle(r);
			r.grow(-frameDistance, -frameDistance);
		}

		g.fillOval(r.x, r.y, r.width, r.height);
	}

	/**
	 * Draws the frame of the figure.
	 *
	 * @param g Graphics to draw to
	 */
	protected void drawFrame(Graphics g)
	{
		Rectangle r = ellipseBox();

		g.drawOval(r.x, r.y, r.width - 1, r.height - 1);

		if (frameCount > 1)
		{
			r = new Rectangle(r);

			for (int i = 1; i < frameCount; ++i)
			{
				r.grow(-frameDistance, -frameDistance);

				g.drawOval(r.x, r.y, r.width - 1, r.height - 1);
			}
		}
	}
}
