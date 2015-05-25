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

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

import org.openbp.common.CommonUtil;

/**
 * Extended diamond figure.
 *
 * @author Erich Lauterbach
 */
public class XDiamondFigure extends XPolygonFigure
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Angle tolerance for snap points */
	public static final double TOLERANCE = 0.20;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public XDiamondFigure()
	{
	}

	/**
	 * Places the given rectangle so that its center is in the given direction
	 * and it exactly touches the node, without crossing any lines.
	 * @param rect The rectangle to adjust
	 * @param angle The direction in which the rectangle should be placed
	 * @return The translated rectangle
	 */
	public Rectangle placeAdjacent(Rectangle rect, double angle)
	{
		// half width of the box
		double b = box.getWidth() / 2d + 2d;

		// half height of the box
		double h = box.getHeight() / 2d + 2d;

		double recHeight = rect.getHeight();
		double recWidth = rect.getWidth();

		double x = 0;
		double y = 0;
		double recX = 0;
		double recY = 0;
		double normalizedAngle = convertToStandardAngle(angle);

		// If the angle is in a given range at one of the points of the diamond,
		// then we set the sector to either N, S, E, or W, such that the rectangle is snapped
		// to the center of the point.
		if (Math.PI * 2 - TOLERANCE < normalizedAngle || normalizedAngle < TOLERANCE)
		{
			// For E sector
			recX = b;
			recY = recHeight / 2;
		}
		else if (Math.PI / 2 - TOLERANCE < normalizedAngle && normalizedAngle < Math.PI / 2 + TOLERANCE)
		{
			// For N sector
			recX = -recWidth / 2;
			recY = h + recHeight;
		}
		else if (Math.PI - TOLERANCE < normalizedAngle && normalizedAngle < Math.PI + TOLERANCE)
		{
			// For W sector
			recX = -b - recWidth;
			recY = recHeight / 2;
		}
		else if (Math.PI * 3 / 2 - TOLERANCE < normalizedAngle && normalizedAngle < Math.PI * 3 / 2 + TOLERANCE)
		{
			// For S sector
			recX = -recWidth / 2;
			recY = -h;
		}
		else
		{
			// If the sector has not been set yet, then set the sector according to the quarter in which we are presently
			// We also calculate the tangent of the angle here, since it should be ensured that PI / 2 and 3 PI / 2 can not occure here.
			// At these points the gradient is infinite.
			Quarter quarter = CircleConstants.determineQuarter(angle, rect);

			// Denpending on the quarter in which we are, we calcute the new x and y coordinates for the rectangle
			double tana = Math.tan(normalizedAngle);
			switch (quarter)
			{
			case NE:
				x = b * h / ((b * tana) + h);
				y = h * (1 - (h / ((b * tana) + h)));
				recX = x;
				recY = y + recHeight;
				break;

			case NW:
				x = b * h / ((b * tana) - h);
				y = h * (1 + (h / ((b * tana) - h)));
				recX = x - recWidth;
				recY = y + recHeight;
				break;

			case SW:
				x = -(b * h) / ((b * tana) + h);
				y = -h * (1 - (h / ((b * tana) + h)));
				recX = x - recWidth;
				recY = y;
				break;

			case SE:
				x = -(b * h) / ((b * tana) - h);
				y = -h * (1 + (h / ((b * tana) - h)));
				recX = x;
				recY = y;
				break;
			}
		}

		Point c = center();
		int xPos = c.x + CommonUtil.rnd(recX);
		int yPos = c.y - CommonUtil.rnd(recY);
		rect.setLocation(xPos, yPos);

		return rect;
	}

	//////////////////////////////////////////////////
	// @@ AbstractPolygonFigure overrides: Geometry support
	//////////////////////////////////////////////////

	/**
	 * Returns the polygon describing the diamond.
	 * @nowarn
	 */
	public Polygon getPolygon()
	{
		if (polygon == null)
		{
			Rectangle r = box;
			polygon = new Polygon();

			// Top
			polygon.addPoint(r.x + r.width / 2, r.y);

			// Left
			polygon.addPoint(r.x, r.y + r.height / 2);

			// Bottom
			polygon.addPoint(r.x + r.width / 2, r.y + r.height);

			// Right
			polygon.addPoint(r.x + r.width, r.y + r.height / 2);
		}

		return polygon;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Normalize the angle to the standard convention
	 *
	 * @param angle The angle to be normalized
	 * @return The normalized angle
	 */
	public double convertToStandardAngle(double angle)
	{
		return 2d * Math.PI - angle;
	}
}
