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
 * Extended triangle figure.
 *
 * @author Stephan Moritz
 */
public class XTriangleFigure extends XPolygonFigure
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public XTriangleFigure()
	{
	}

	//////////////////////////////////////////////////
	// @@ AbstractFigure overrides: Geometry support
	//////////////////////////////////////////////////

	private static final int SECTOR_N = 0;

	private static final int SECTOR_NE = 1;

	private static final int SECTOR_SE = 2;

	private static final int SECTOR_S = 3;

	private static final int SECTOR_SW = 4;

	private static final int SECTOR_NW = 5;

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

		double boxCenter = box.getWidth() / 2d + 2d;

		// Don't ask...
		double alpha = Math.atan((boxCenter - yRectCenter) / (boxCenter + xRectCenter));
		double beta = Math.atan((boxCenter + yRectCenter) / (boxCenter + xRectCenter));
		double gamma = Math.atan(xRectCenter / (boxCenter + yRectCenter));
		alpha = Math.abs(alpha);
		beta = Math.abs(beta);
		gamma = Math.abs(gamma);

		alpha = CircleConstants.normalizeAngle(alpha);
		beta = CircleConstants.normalizeAngle(beta);
		gamma = CircleConstants.normalizeAngle(gamma);

		Orientation orientation = getOrientation();

		// This tedious if/else if construction determines in which
		// section of the triangle we are
		int sector;
		if (orientation == Orientation.BOTTOM)
		{
			if (angle < Math.PI / 2d - gamma || angle > 2d * Math.PI - alpha)
				sector = SECTOR_SE;
			else if (angle < Math.PI / 2d + gamma)
				sector = SECTOR_S;
			else if (angle < Math.PI + alpha)
				sector = SECTOR_SW;
			else if (angle < Math.PI + beta)
				sector = SECTOR_NW;
			else if (angle < 2d * Math.PI - beta)
				sector = SECTOR_N;
			else
				sector = SECTOR_NE;
		}
		else
		{
			if (angle < alpha || angle > 3d / 2d * Math.PI + gamma)
				sector = SECTOR_NE;
			else if (angle < beta)
				sector = SECTOR_SE;
			else if (angle < Math.PI - beta)
				sector = SECTOR_S;
			else if (angle < Math.PI - alpha)
				sector = SECTOR_SW;
			else if (angle < 3d / 2d * Math.PI - gamma)
				sector = SECTOR_NW;
			else
				sector = SECTOR_N;
		}

		double x = 0;
		double y = 0;
		double tana = Math.tan(angle);
		double temp;
		if (orientation == Orientation.BOTTOM)
		{
			switch (sector)
			{
			case SECTOR_N:
				// N (horizontal) section
				x = -((boxCenter + yRectCenter) / tana);
				y = -(boxCenter + yRectCenter);
				break;

			case SECTOR_NE:
				x = boxCenter + xRectCenter;
				y = tana * (boxCenter + xRectCenter);
				break;

			case SECTOR_SE:
				temp = ((boxCenter + yRectCenter + 2d * xRectCenter) / (2d + tana));

				x = temp;
				y = temp * tana;

				break;

			case SECTOR_S:
				// S (horizontal) section
				x = (boxCenter + yRectCenter) / tana;
				y = boxCenter + yRectCenter;
				break;

			case SECTOR_SW:
				temp = ((boxCenter + yRectCenter + 2d * xRectCenter) / (-2d + tana));

				x = temp;
				y = temp * tana;
				break;

			case SECTOR_NW:
				// W (vertical) section
				x = -(boxCenter + xRectCenter);
				y = -(tana * (boxCenter + xRectCenter));
				break;
			}
		}
		else
		{
			switch (sector)
			{
			case SECTOR_N:
				// N (horizontal) section
				x = -((boxCenter + yRectCenter) / tana);
				y = -(boxCenter + yRectCenter);
				break;

			case SECTOR_SE:
				x = boxCenter + xRectCenter;
				y = tana * (boxCenter + xRectCenter);
				break;

			case SECTOR_NE:
				temp = (-(boxCenter + yRectCenter + 2d * xRectCenter) / (-2d + tana));

				x = temp;
				y = temp * tana;

				break;

			case SECTOR_S:
				// s (horizontal) section
				x = (boxCenter + yRectCenter) / tana;
				y = boxCenter + yRectCenter;
				break;

			case SECTOR_NW:
				temp = (-(boxCenter + yRectCenter + 2d * xRectCenter) / (2d + tana));

				x = temp;
				y = temp * tana;
				break;

			case SECTOR_SW:
				// W (vertical) section
				x = -(boxCenter + xRectCenter);
				y = -(tana * (boxCenter + xRectCenter));
				break;
			}
		}

		Point c = center();
		int xPos = c.x + CommonUtil.rnd(x - xRectCenter) + 1;
		int yPos = c.y + CommonUtil.rnd(y - yRectCenter) + 2;
		rect.setLocation(xPos, yPos);
		return rect;
	}

	//////////////////////////////////////////////////
	// @@ AbstractPolygonFigure overrides: Geometry support
	//////////////////////////////////////////////////

	/**
	 * Returns the polygon describing the triangle.
	 * @nowarn
	 */
	public Polygon getPolygon()
	{
		if (polygon == null)
		{
			Rectangle r = box;
			polygon = new Polygon();

			Orientation orientation = getOrientation();
			switch (orientation)
			{
			case BOTTOM:
				polygon.addPoint(r.x + r.width / 2, r.y + r.height);
				polygon.addPoint(r.x, r.y);
				polygon.addPoint(r.x + r.width, r.y);
				break;

			case LEFT:
				polygon.addPoint(r.x, r.y + r.height / 2);
				polygon.addPoint(r.x + r.width, r.y);
				polygon.addPoint(r.x + r.width, r.y + r.height);
				break;

			case TOP:
				polygon.addPoint(r.x + r.width / 2, r.y);
				polygon.addPoint(r.x + r.width, r.y + r.height);
				polygon.addPoint(r.x, r.y + r.height);
				break;

			case RIGHT:
				polygon.addPoint(r.x + r.width, r.y + r.height / 2);
				polygon.addPoint(r.x, r.y + r.height);
				polygon.addPoint(r.x, r.y);
				break;
			}
		}

		return polygon;
	}
}
