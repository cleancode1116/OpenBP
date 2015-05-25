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
package org.openbp.swing.draw;

import java.awt.Color;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

/**
 * A round color gradient.
 * This gradient defines a color at a point; the gradient blends into another color as a function
 * of the distance from that point. The end result is a big, fuzzy spot.
 *
 * @author Heiko Erhardt
 */
public class RadialGradientPaint
	implements Paint
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Center of the gradient */
	protected Point2D center;

	/** Gradient radius */
	protected Point2D radius;

	/** Color of the center */
	protected Color centerColor;

	/** Background color */
	protected Color backgroundColor;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param center Center of the gradient
	 * @param radius Gradient radius
	 * @param centerColor Color of the center
	 * @param backgroundColor Background color
	 */
	public RadialGradientPaint(Point2D center, Color centerColor, Point2D radius, Color backgroundColor)
	{
		if (radius.distance(0, 0) <= 0)
			throw new IllegalArgumentException("Radius must be greater than 0.");

		this.center = center;
		this.centerColor = centerColor;
		this.radius = radius;
		this.backgroundColor = backgroundColor;
	}

	//////////////////////////////////////////////////
	// @@ Paint implementation
	//////////////////////////////////////////////////

	/**
	 * @see java.awt.Paint#createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints)
	 */
	public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints)
	{
		Point2D transformedPoint = xform.transform(center, null);
		Point2D transformedRadius = xform.deltaTransform(radius, null);
		return new RadialGradientPaintContext(transformedPoint, centerColor, transformedRadius, backgroundColor);
	}

	/**
	 * @see java.awt.Paint#getTransparency()
	 */
	public int getTransparency()
	{
		int a1 = centerColor.getAlpha();
		int a2 = backgroundColor.getAlpha();
		return (((a1 & a2) == 0xff) ? OPAQUE : TRANSLUCENT);
	}
}
