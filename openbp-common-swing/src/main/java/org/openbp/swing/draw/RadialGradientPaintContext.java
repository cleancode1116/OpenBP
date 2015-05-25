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
import java.awt.PaintContext;
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Paint context for the {@link RadialGradientPaint} class.
 *
 * @author Heiko Erhardt
 */
public class RadialGradientPaintContext
	implements PaintContext
{
	//////////////////////////////////////////////////
	// @@ Data members
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
	 * Default constructor.
	 *
	 * @param center Center of the gradient
	 * @param radius Gradient radius
	 * @param centerColor Color of the center
	 * @param backgroundColor Background color
	 */
	public RadialGradientPaintContext(Point2D center, Color centerColor, Point2D radius, Color backgroundColor)
	{
		this.center = center;
		this.centerColor = centerColor;
		this.radius = radius;
		this.backgroundColor = backgroundColor;
	}

	//////////////////////////////////////////////////
	// @@ PaintContext implemenation
	//////////////////////////////////////////////////

	/**
	 * @see java.awt.PaintContext#dispose()
	 */
	public void dispose()
	{
	}

	/**
	 * @see java.awt.PaintContext#getColorModel()
	 */
	public ColorModel getColorModel()
	{
		return ColorModel.getRGBdefault();
	}

	/**
	 * @see java.awt.PaintContext#getRaster(int x, int y, int w, int h)
	 */
	public Raster getRaster(int x, int y, int w, int h)
	{
		WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);

		int [] data = new int [w * h * 4];
		for (int j = 0; j < h; j++)
		{
			for (int i = 0; i < w; i++)
			{
				double distance = center.distance(x + i, y + j);
				double rad = radius.distance(0, 0);
				double ratio = distance / rad;
				if (ratio > 1.0)
					ratio = 1.0;

				int base = (j * w + i) * 4;
				data [base + 0] = (int) (centerColor.getRed() + ratio * (backgroundColor.getRed() - centerColor.getRed()));
				data [base + 1] = (int) (centerColor.getGreen() + ratio * (backgroundColor.getGreen() - centerColor.getGreen()));
				data [base + 2] = (int) (centerColor.getBlue() + ratio * (backgroundColor.getBlue() - centerColor.getBlue()));
				data [base + 3] = (int) (centerColor.getAlpha() + ratio * (backgroundColor.getAlpha() - centerColor.getAlpha()));
			}
		}
		raster.setPixels(0, 0, w, h, data);

		return raster;
	}
}
