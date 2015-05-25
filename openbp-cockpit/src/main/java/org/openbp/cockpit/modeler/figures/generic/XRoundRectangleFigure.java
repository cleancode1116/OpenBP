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
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

import CH.ifa.draw.framework.Connector;

/**
 * Extended round rectangle figure.
 */
public class XRoundRectangleFigure extends XRectangleFigure
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Arc width of the rectanlge corner (100 = hor. semicircle) */
	private int arcWidth;

	/** Arc height of the rectanlge corner (100 = vert. semicircle) */
	private int arcHeight;

	/** Default arc of the rectanlge corner */
	private static final int DEFAULT_ARC = 8;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public XRoundRectangleFigure()
	{
		arcWidth = arcHeight = DEFAULT_ARC;
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
			arcWidth = descriptor.getCustomIntAttributeValue("arc-width", arcWidth);
			arcHeight = descriptor.getCustomIntAttributeValue("arc-height", arcHeight);
		}
	}

	/**
	 * @see CH.ifa.draw.figures.EllipseFigure#connectionInsets ()
	 */
	public Insets connectionInsets()
	{
		return new Insets(realArcHeight() / 2, realArcWidth() / 2, realArcHeight() / 2, realArcWidth() / 2);
	}

	/**
	 * @see CH.ifa.draw.figures.EllipseFigure#connectorAt (int x, int y)
	 */
	public Connector connectorAt(int x, int y)
	{
		return null;
	}

	/**
	 * Draws the figure.
	 *
	 * @param g Graphics to draw to
	 */
	protected void drawFigure(Graphics g)
	{
		g.fillRoundRect(box.x, box.y, box.width, box.height, realArcWidth(), realArcHeight());
	}

	/**
	 * Draws the frame of the figure.
	 *
	 * @param g Graphics to draw to
	 */
	protected void drawFrame(Graphics g)
	{
		Rectangle r = box;

		g.drawRoundRect(r.x, r.y, r.width - 1, r.height - 1, realArcWidth(), realArcHeight());

		if (frameCount > 1)
		{
			r = new Rectangle(r);

			for (int i = 1; i < frameCount; ++i)
			{
				r.grow(-frameDistance, -frameDistance);

				g.drawRoundRect(r.x, r.y, r.width - 1, r.height - 1, realArcWidth(), realArcHeight());
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ AbstractFigure overrides: Geometry support
	//////////////////////////////////////////////////

	/**
	 * Creates a shape object that defines the outline of the figure.
	 *
	 * @return The shape (a java.awt.geom.RoundRectangle2D)
	 */
	public Shape createShape()
	{
		return createRectangularShape();
	}

	/**
	 * Creates a rectangular shape object that defines the outline of the figure.
	 *
	 * @return The shape (a java.awt.geom.RoundRectangle2D)
	 */
	public RectangularShape createRectangularShape()
	{
		// Create the shape
		RoundRectangle2D shape = new RoundRectangle2D.Double();

		// Apply the current figure dimensions to it
		Rectangle r = displayBox();
		shape.setRoundRect(r.getX(), r.getY(), r.getWidth(), r.getHeight(), realArcWidth(), realArcHeight());

		return shape;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the arc width of the rectanlge corner.
	 * @return 100 = hor. semicircle, default = 8
	 */
	public int getArcWidth()
	{
		return arcWidth;
	}

	/**
	 * Sets the arc width of the rectanlge corner.
	 * @param arcWidth 100 = hor. semicircle, default = 8
	 */
	public void setArcWidth(int arcWidth)
	{
		this.arcWidth = arcWidth;
	}

	/**
	 * Gets the arc height of the rectanlge corner.
	 * @return 100 = vert. semicircle, default = 8
	 */
	public int getArcHeight()
	{
		return arcHeight;
	}

	/**
	 * Sets the arc height of the rectanlge corner.
	 * @param arcHeight 100 = vert. semicircle, default = 8
	 */
	public void setArcHeight(int arcHeight)
	{
		this.arcHeight = arcHeight;
	}

	public int realArcWidth()
	{
		if (arcWidth < 100)
			return arcWidth;
		int n = Math.min(box.width, box.height);
		return n;
	}

	public int realArcHeight()
	{
		if (arcHeight < 100)
			return arcHeight;
		int n = Math.min(box.width, box.height);
		return n;
	}
}
