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
package org.openbp.jaspira.gui.interaction;

import java.awt.Component;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import javax.swing.SwingUtilities;

import org.openbp.jaspira.gui.plugin.PluginContainer;

/**
 * A rectangular shape that corresponds to one of the five main
 * regions of a Rectangle (4 sides and center).
 * Side segments are trapez-shaped, while the center is a rectangle.
 *
 * @author Stephan Moritz
 */
public class RectangleSegment extends RectangularShape
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Constants
	/////////////////////////////////////////////////////////////////////////

	/** Constraint: Center segment */
	public static final String CENTER = PluginContainer.CENTER;

	/** Constraint: North segment */
	public static final String NORTH = PluginContainer.NORTH;

	/** Constraint: South segment */
	public static final String SOUTH = PluginContainer.SOUTH;

	/** Constraint: East segment */
	public static final String EAST = PluginContainer.EAST;

	/** Constraint: West segment */
	public static final String WEST = PluginContainer.WEST;

	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** The polygon that we internally map to */
	private Polygon polygon;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param p Creates a rectangle segment of the rectangle between 0,0 and p
	 * @param width Controls the thickness of the border segements
	 * @param constraint Type of the segment (see above)
	 */
	public RectangleSegment(Point p, int width, String constraint)
	{
		this(p.x, p.y, width, constraint);
	}

	/**
	 * Constructor.
	 *
	 * @param comp Creates a rectangle segment of the component's bounding box
	 * @param width Controls the thickness of the border segements
	 * @param constraint Type of the segment (see above)
	 */
	public RectangleSegment(Component comp, int width, String constraint)
	{
		this(SwingUtilities.getLocalBounds(comp), width, constraint);
	}

	/**
	 * Constructor.
	 *
	 * @param rectangle Creates a rectangle segment of the given rectangle
	 * @param width Controls the thickness of the border segements
	 * @param constraint Type of the segment (see above)
	 */
	public RectangleSegment(Rectangle rectangle, int width, String constraint)
	{
		this(rectangle.width, rectangle.height, width, constraint);
	}

	/**
	 * Constructor.
	 *
	 * Creates a rectangle segment using the given geometry
	 * @param x X coordinated
	 * @param y Y coordinated
	 * @param width Width of the rectangle
	 * @param constraint Type of the segment (see above)
	 */
	public RectangleSegment(int x, int y, int width, String constraint)
	{
		// We construct our polygon out of the given arguments.

		if (NORTH.equals(constraint))
		{
			polygon = new Polygon(new int [] { 0, x, x - width, width }, new int [] { 0, 0, width, width }, 4);
		}
		else if (SOUTH.equals(constraint))
		{
			polygon = new Polygon(new int [] { 0, x, x - width, width }, new int [] { y, y, y - width, y - width }, 4);
		}
		else if (WEST.equals(constraint))
		{
			polygon = new Polygon(new int [] { 0, width, width, 0 }, new int [] { 0, width, y - width, y }, 4);
		}
		else if (EAST.equals(constraint))
		{
			polygon = new Polygon(new int [] { x, x - width, x - width, x }, new int [] { y, y - width, width, 0 }, 4);
		}
		else
		{
			// CENTER
			polygon = new Polygon(new int [] { width, x - width, x - width, width }, new int [] { width, width, y - width, y - width }, 4);
		}
	}

	/**
	 * Gets the left coordinate.
	 * @nowarn
	 */
	public double getX()
	{
		return polygon.getBounds().getX();
	}

	/**
	 * Gets the top coordinate.
	 * @nowarn
	 */
	public double getY()
	{
		return polygon.getBounds().getY();
	}

	/**
	 * Gets the width.
	 * @nowarn
	 */
	public double getWidth()
	{
		return polygon.getBounds().getWidth();
	}

	/**
	 * Gets the height.
	 * @nowarn
	 */
	public double getHeight()
	{
		return polygon.getBounds().getHeight();
	}

	/**
	 * @see java.awt.geom.RectangularShape#isEmpty()
	 */
	public boolean isEmpty()
	{
		return false;
	}

	/**
	 * We ignore the size part of setFrame and move it only to the given position.
	 *
	 * ATTENTION: This is contrary to the RectangularShape interface!
	 *
	 * @see java.awt.geom.RectangularShape#setFrame(double, double, double, double)
	 */
	public void setFrame(double x, double y, double w, double h)
	{
		polygon.translate((int) (x - getX()), (int) (y - getY()));
	}

	/**
	 * @see java.awt.Shape#getBounds2D()
	 */
	public Rectangle2D getBounds2D()
	{
		return polygon.getBounds2D();
	}

	/**
	 * @see java.awt.Shape#contains(double, double)
	 */
	public boolean contains(double x, double y)
	{
		return polygon.contains(x, y);
	}

	/**
	 * @see java.awt.Shape#intersects(double, double, double, double)
	 */
	public boolean intersects(double x, double y, double w, double h)
	{
		return polygon.intersects(x, y, w, h);
	}

	/**
	 * @see java.awt.Shape#contains(double, double, double, double)
	 */
	public boolean contains(double x, double y, double w, double h)
	{
		return polygon.contains(x, y, w, h);
	}

	/**
	 * @see java.awt.Shape#getPathIterator(AffineTransform)
	 */
	public PathIterator getPathIterator(AffineTransform at)
	{
		return polygon.getPathIterator(at);
	}
}
