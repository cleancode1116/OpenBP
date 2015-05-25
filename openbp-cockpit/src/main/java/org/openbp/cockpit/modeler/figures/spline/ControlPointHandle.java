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
import org.openbp.common.CommonUtil;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.standard.AbstractHandle;

/**
 * Control point handle for a poly line.
 * See {@link PolySplineFigure}
 *
 * @author Stephan Moritz
 */
public class ControlPointHandle extends AbstractHandle
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Spline index of the point this controlpoint belongs to */
	private int splineIndex;

	/** Side of the point this controlpoints represents */
	private int side;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructs a poly line control point handle.
	 * @param owner The owning polyline figure
	 * @param splineIndex Spline index of the point this controlpoint belongs to
	 * @param side Side of the point this controlpoints represents
	 */
	public ControlPointHandle(PolySplineFigure owner, int splineIndex, int side)
	{
		super(owner);

		this.splineIndex = splineIndex;
		this.side = side;
	}

	/**
	 * Gets the spline figure that owns the handle.
	 * @nowarn
	 */
	private PolySplineFigure getOwningSpline()
	{
		return (PolySplineFigure) owner();
	}

	//////////////////////////////////////////////////
	// @@ AbstractHandle overrides
	//////////////////////////////////////////////////

	/**
	 * @see CH.ifa.draw.standard.AbstractHandle#invokeStart(int x, int y, DrawingView view)
	 */
	public void invokeStart(int x, int y, DrawingView view)
	{
	}

	/**
	 * Sets the position of the control point.
	 * @see CH.ifa.draw.standard.AbstractHandle#invokeStep(int x, int y, int anchorX, int anchorY, DrawingView view)
	 */
	public void invokeStep(int x, int y, int anchorX, int anchorY, DrawingView view)
	{
		getOwningSpline().setCtrlPointAt(splineIndex, side, new Point(x, y));
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractHandle#invokeEnd(int x, int y, int anchorX, int anchorY, DrawingView view)
	 */
	public void invokeEnd(int x, int y, int anchorX, int anchorY, DrawingView view)
	{
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

		Stroke old = ((Graphics2D) g).getStroke();

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
