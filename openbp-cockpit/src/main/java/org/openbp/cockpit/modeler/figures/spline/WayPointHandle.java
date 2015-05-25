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

import java.awt.Point;
import java.awt.geom.Point2D;

import org.openbp.common.CommonUtil;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.standard.AbstractHandle;

/**
 * Handle for a waypoint of a spline.
 *
 * @author Stephan Moritz
 */
public class WayPointHandle extends AbstractHandle
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Index of the waypoint in the owning spline */
	private int splineIndex;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 * @param owner The owning polyline figure
	 * @param splineIndex Spline index of the point this controlpoint belongs to
	 */
	public WayPointHandle(PolySplineFigure owner, int splineIndex)
	{
		super(owner);

		this.splineIndex = splineIndex;
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
	 * Sets the position of the way point.
	 * @see CH.ifa.draw.standard.AbstractHandle#invokeStep(int x, int y, int anchorX, int anchorY, DrawingView view)
	 */
	public void invokeStep(int x, int y, int anchorX, int anchorY, DrawingView view)
	{
		getOwningSpline().setPointAt(splineIndex, new Point(x, y));
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
		Point2D splinePoint = getOwningSpline().getPointAt(splineIndex);
		return new Point(CommonUtil.rnd(splinePoint.getX()), CommonUtil.rnd(splinePoint.getY()));
	}
}
