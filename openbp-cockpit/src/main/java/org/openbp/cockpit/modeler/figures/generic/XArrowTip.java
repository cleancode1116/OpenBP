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

import java.awt.Polygon;
import java.io.IOException;

import org.openbp.common.CommonUtil;

import CH.ifa.draw.figures.AbstractLineDecoration;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

/**
 * An arrow tip line decoration.
 *
 * @seec PolySplineFigure
 *
 * @author Stephan Moritz
 */
public class XArrowTip extends AbstractLineDecoration
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Pointiness of arrow */
	private double angle;

	/** Outer radius */
	private double outerRadius;

	/** Inner radius */
	private double innerRadius;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 * Angle: 0.4, radius: 8
	 */
	public XArrowTip()
	{
		this(0.40, 8, 8);
	}

	/**
	 * Value constructor.
	 *
	 * @param angle Pointiness of arrow
	 * @param outerRadius Outer radius
	 * @param innerRadius Inner radius
	 */
	public XArrowTip(double angle, double outerRadius, double innerRadius)
	{
		this.angle = angle;
		this.outerRadius = outerRadius;
		this.innerRadius = innerRadius;
	}

	/**
	 * Calculates the outline of an arrow tip.
	 * @param x1 Arrow tip
	 * @param y1 Arrow tip
	 * @param x2 Arrow base
	 * @param y2 Arrow base
	 * @return The polygon describing the outline
	 */
	public Polygon outline(int x1, int y1, int x2, int y2)
	{
		double direction = Math.PI / 2 - Math.atan2(x2 - x1, y2 - y1);
		int x = x1;
		int y = y1;

		Polygon shape = new Polygon();

		shape.addPoint(x, y);

		addPointRelative(shape, x, y, outerRadius, direction - angle);
		if (innerRadius >= 0)
		{
			addPointRelative(shape, x, y, innerRadius, direction);
		}
		addPointRelative(shape, x, y, outerRadius, direction + angle);

		// Close the polygon
		shape.addPoint(x, y);

		return shape;
	}

	private void addPointRelative(Polygon shape, int x, int y, double radius, double angle)
	{
		shape.addPoint(x + CommonUtil.rnd(radius * Math.cos(angle)), y + CommonUtil.rnd(radius * Math.sin(angle)));
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Gets the pointiness of arrow.
	 * A smaller angle leads to a pointier arrow.
	 * The angle is measured between the arrow line and one of the points
	 * at the side of the arrow. Thus, the total angle at the arrow tip
	 * is the double of the angle specified.
	 * @nowarn
	 */
	public double getAngle()
	{
		return angle;
	}

	/**
	 * Sets the pointiness of arrow.
	 * A smaller angle leads to a pointier arrow.
	 * The angle is measured between the arrow line and one of the points
	 * at the side of the arrow. Thus, the total angle at the arrow tip
	 * is the double of the angle specified.
	 * @nowarn
	 */
	public void setAngle(double angle)
	{
		this.angle = angle;
	}

	/**
	 * Gets the outer radius.
	 * @nowarn
	 */
	public double getOuterRadius()
	{
		return outerRadius;
	}

	/**
	 * Sets the outer radius.
	 * @nowarn
	 */
	public void setOuterRadius(double outerRadius)
	{
		this.outerRadius = outerRadius;
	}

	/**
	 * Gets the inner radius.
	 * @nowarn
	 */
	public double getInnerRadius()
	{
		return innerRadius;
	}

	/**
	 * Sets the inner radius.
	 * @nowarn
	 */
	public void setInnerRadius(double innerRadius)
	{
		this.innerRadius = innerRadius;
	}

	//////////////////////////////////////////////////
	// @@ Storable implementation
	//////////////////////////////////////////////////

	/**
	 * Stores the arrow tip to a StorableOutput.
	 * @nowarn
	 */
	public void write(StorableOutput dw)
	{
		dw.writeDouble(angle);
		dw.writeDouble(outerRadius);
		dw.writeDouble(innerRadius);
		super.write(dw);
	}

	/**
	 * Reads the arrow tip from a StorableInput.
	 * @nowarn
	 */
	public void read(StorableInput dr)
		throws IOException
	{
		angle = dr.readDouble();
		outerRadius = dr.readDouble();
		innerRadius = dr.readDouble();
		super.read(dr);
	}
}
