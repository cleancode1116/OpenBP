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
package org.openbp.cockpit.modeler.figures.spline;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import org.openbp.cockpit.modeler.ModelerColors;
import org.openbp.cockpit.modeler.ModelerGraphics;
import org.openbp.cockpit.modeler.figures.generic.Orientation;
import org.openbp.cockpit.modeler.figures.tag.TagConnector;
import org.openbp.common.CommonUtil;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.standard.AbstractHandle;

/**
 * Horizontally or vertically constrained control point handle for a poly line.
 * Used for the start and end points of the spline.
 * Sets the arc factor of the poly lines start or end point.
 * See {@link PolySplineFigure}
 *
 * @author Stephan Moritz
 */
public class ConstrainedControlPointHandle extends AbstractHandle
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Spline index of the point this controlpoint belongs to */
	private int splineIndex;

	/** Side of the point this controlpoints represents */
	private int side;

	/** Distance between control point and start/end point */
	private int distance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructs a poly line control point handle.
	 * @param owner The owning polyline figure
	 * @param splineIndex Spline index of the point this controlpoint belongs to
	 * @param splineIndex2 Spline index of a second point that determines the initial distance
	 */
	public ConstrainedControlPointHandle(PolySplineConnection owner, int splineIndex, int splineIndex2)
	{
		super(owner);

		this.splineIndex = splineIndex;

		this.side = (splineIndex == 0) ? PolySplineConnection.RIGHT_CONTROLPOINT : PolySplineConnection.LEFT_CONTROLPOINT;

		Point2D p1 = owner.getPointAt(splineIndex);
		Point2D p2 = owner.getPointAt(splineIndex2);

		double xdiff = p1.getX() - p2.getX();
		double ydiff = p1.getY() - p2.getY();

		distance = CommonUtil.rnd(Math.sqrt(xdiff * xdiff + ydiff * ydiff));
	}

	/**
	 * Gets the spline figure that owns the handle.
	 * @nowarn
	 */
	private PolySplineConnection getOwningSpline()
	{
		return (PolySplineConnection) owner();
	}

	//////////////////////////////////////////////////
	// @@ AbstractHandle overrides
	//////////////////////////////////////////////////

	/**
	 * Sets the factor according to the handle position.
	 *
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @param anchorX Document coordinate of the anchor point of the handle
	 * @param anchorY Document coordinate of the anchor point of the handle
	 * @param view View to draw on
	 * @see CH.ifa.draw.standard.AbstractHandle#invokeStep(int x, int y, int anchorX, int anchorY, DrawingView view)
	 */
	public void invokeStep(int x, int y, int anchorX, int anchorY, DrawingView view)
	{
		PolySplineConnection spline = getOwningSpline();

		Point p = locateWayPoint();

		Orientation orientation;
		if (splineIndex == 0)
		{
			orientation = ((TagConnector) spline.getStartConnector()).getOrientation();
		}
		else
		{
			orientation = ((TagConnector) spline.getEndConnector()).getOrientation();
		}
		boolean vertical = orientation == Orientation.TOP || orientation == Orientation.BOTTOM;

		double newFactor;
		if (vertical)
		{
			newFactor = Math.abs(y - p.y);
		}
		else
		{
			newFactor = Math.abs(x - p.x);
		}

		newFactor /= distance;

		if (splineIndex == 0)
		{
			spline.setStartFactor(newFactor);
		}
		else
		{
			spline.setEndFactor(newFactor);
		}
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractHandle#locate()
	 */
	public Point locate()
	{
		Point2D splinePoint = getOwningSpline().getCtrlPointAt(splineIndex, side);
		return new Point(CommonUtil.rnd(splinePoint.getX()), CommonUtil.rnd(splinePoint.getY()));
	}

	/**
	 * Locates the way point of the spline we are processing.
	 * @return The position of the waypoint in document coordinates
	 */
	protected Point locateWayPoint()
	{
		Point2D splinePoint = getOwningSpline().getPointAt(splineIndex);
		return new Point(CommonUtil.rnd(splinePoint.getX()), CommonUtil.rnd(splinePoint.getY()));
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractHandle#displayBox()
	 */
	public Rectangle displayBox()
	{
		Point p = locate();
		return new Rectangle(p.x - 5, p.y - 5, 10, 10);
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractHandle#draw(Graphics g)
	 */
	public void draw(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;

		// draw a line from ctrlpoint to wayPoint
		Point p1 = locateWayPoint();
		Point p2 = locate();

		Stroke old = g2.getStroke();

		g2.setStroke(ModelerGraphics.labelHandleStroke);
		g.setColor(ModelerColors.HANDLE_LINE);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		g2.setStroke(old);

		Rectangle r = displayBox();

		g.setColor(ModelerColors.HANDLE_WAYPOINT_FILL);
		g.fillRect(r.x, r.y, r.width, r.height);

		g.setColor(ModelerColors.HANDLE_WAYPOINT_BORDER);
		g.drawRect(r.x, r.y, r.width, r.height);
	}
}
