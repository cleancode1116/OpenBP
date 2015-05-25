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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Vector;

import org.openbp.common.CommonUtil;

import CH.ifa.draw.standard.BoxHandleKit;

/**
 * Extended rectangle figure.
 *
 * @author Heiko Erhardt
 */
public class XRectangleFigure extends XFigure
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Distance between figure and frame */
	protected int frameDistance;

	/** Number of frames */
	protected int frameCount = 1;

	/** Bar height */
	protected int barHeight = 10;

	/** Number of bars to draw */
	protected int nBars;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Display box */
	protected Rectangle box;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public XRectangleFigure()
	{
		box = new Rectangle();
	}

	//////////////////////////////////////////////////
	// @@ AbstractFigure overrides
	//////////////////////////////////////////////////

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#basicDisplayBox(Point origin, Point corner)
	 */
	public void basicDisplayBox(Point origin, Point corner)
	{
		box = new Rectangle(origin);
		box.add(corner);
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#handles()
	 */
	public Vector handles()
	{
		Vector handles = new Vector();
		BoxHandleKit.addHandles(this, handles);
		return handles;
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#displayBox()
	 */
	public Rectangle displayBox()
	{
		return new Rectangle(box);
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#basicMoveBy(int x, int y)
	 */
	protected void basicMoveBy(int x, int y)
	{
		box.translate(x, y);
	}

	//////////////////////////////////////////////////
	// @@ XFigure overrides
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
			barHeight = descriptor.getCustomIntAttributeValue("bar-height", barHeight);
			nBars = descriptor.getCustomIntAttributeValue("bar-number", nBars);
		}
	}

	/**
	 * Draws the figure.
	 *
	 * @param g Graphics to draw to
	 */
	protected void drawFigure(Graphics g)
	{
		Rectangle r = box;

		if (frameDistance > 0 || frameCount > 1)
		{
			r = new Rectangle(r);
			r.grow(-frameDistance * frameCount, -frameDistance * frameCount);
		}

		g.fillRect(r.x, r.y, r.width, r.height);
	}

	/**
	 * Draws the frame of the figure.
	 *
	 * @param g Graphics to draw to
	 */
	protected void drawFrame(Graphics g)
	{
		Rectangle r = box;

		g.drawRect(r.x, r.y, r.width - 1, r.height - 1);

		if (frameCount > 1)
		{
			r = new Rectangle(r);

			for (int i = 1; i < frameCount; ++i)
			{
				r.grow(-frameDistance, -frameDistance);

				g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
			}
		}
	}

	/**
	 * Draws additional frame parts.
	 *
	 * @param g Graphics to draw to
	 */
	protected void drawAppliances(Graphics g)
	{
		if (nBars > 0)
		{
			if (nBars > 1)
			{
				// Distribute multiple bars evenly
				int barDistance = (box.height - nBars * barHeight) / (nBars - 1);

				int y = box.y;
				for (int i = 0; i < nBars; ++i)
				{
					g.fillRect(box.x, y, box.width, barHeight);
					g.drawRect(box.x, y, box.width - 1, barHeight - 1);
					y += barHeight + barDistance;
				}
			}
			else
			{
				// Center single bar in display box
				int y = box.y + (box.height - barHeight) / 2;
				g.fillRect(box.x, y, box.width, barHeight);
				g.drawRect(box.x, y, box.width - 1, barHeight - 1);
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ AbstractFigure overrides: Geometry support
	//////////////////////////////////////////////////

	private static final int SECTOR_E = 1;

	private static final int SECTOR_S = 2;

	private static final int SECTOR_W = 3;

	private static final int SECTOR_N = 4;

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

		Rectangle db = displayBox();
		db.grow(-1, -1);
		double xBoxCenter = db.getWidth() / 2;
		double yBoxCenter = db.getHeight() / 2;

		// This tedious if/else if construction determines in which
		// section of the rectangle we are
		int sector;
		double alpha = Math.atan(db.getHeight() / db.getWidth());

		if (0 <= angle && angle <= alpha)
		{
			sector = SECTOR_E;
		}
		else if (angle > alpha && angle < Math.PI - alpha)
		{
			sector = SECTOR_N;
		}
		else if (angle >= Math.PI - alpha && angle <= Math.PI + alpha)
		{
			sector = SECTOR_W;
		}
		else if (angle > Math.PI + alpha && angle < 2 * Math.PI - alpha)
		{
			sector = SECTOR_S;
		}
		else
		{
			sector = SECTOR_E;
		}

		double ratio = xBoxCenter / yBoxCenter;

		double x = 0;
		double y = 0;
		double tana = Math.tan(angle);
		switch (sector)
		{
		case SECTOR_E:
			x = xBoxCenter + xRectCenter;
			y = tana * (yBoxCenter + yRectCenter) / (1d / ratio);
			break;

		case SECTOR_S:
			// s (horizontal) section
			x = -((xBoxCenter + xRectCenter) / tana) / ratio;
			y = -(yBoxCenter + yRectCenter);
			break;

		case SECTOR_W:
			// W (vertical) section
			x = -(xBoxCenter + xRectCenter);
			y = -(tana * (yBoxCenter + yRectCenter)) / (1d / ratio);
			break;

		case SECTOR_N:
			// N (horizontal) section
			x = (xBoxCenter + xRectCenter) / tana / ratio;
			y = yBoxCenter + yRectCenter;
			break;
		}

		x -= xRectCenter;
		y -= yRectCenter;

		Point c = center();
		int xPos = c.x + CommonUtil.rnd(x);
		int yPos = c.y + CommonUtil.rnd(y);
		rect.setLocation(xPos, yPos);

		return rect;
	}

	/**
	 * Creates a shape object that defines the outline of the figure.
	 *
	 * @return The shape (a java.awt.geom.Rectangle2D)
	 */
	public Shape createShape()
	{
		return createRectangularShape();
	}

	/**
	 * Creates a rectangular shape object that defines the outline of the figure.
	 *
	 * @return The shape (a java.awt.geom.Rectangle2D)
	 */
	public RectangularShape createRectangularShape()
	{
		// Create the shape
		Rectangle2D shape = new Rectangle2D.Double();

		// Apply the current figure dimensions to it
		Rectangle r = displayBox();
		shape.setFrame(r);

		return shape;
	}
}
