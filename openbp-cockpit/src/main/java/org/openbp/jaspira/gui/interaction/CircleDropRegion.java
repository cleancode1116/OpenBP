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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;

/**
 * A circular drop region is a region that is only visible by displaying a small circle in its center.
 *
 * @author Stephan Moritz
 */
public class CircleDropRegion extends BasicDropRegion
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** Shape to display in the center of the region. */
	private Ellipse2D circle;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 * @param diameter Diameter of the circle
	 * @copy BasicDropRegion.BasicDropRegion (Object,InteractionClient,RectangularShape,Component)
	 * @nowarn
	 */
	public CircleDropRegion(Object id, InteractionClient parent, RectangularShape shape, int diameter, Component origin)
	{
		super(id, parent, shape, origin);

		// Super constructor already converts shape to glasscoords... Somewhat messy
		Point2D center = new Point2D.Double(shape.getCenterX(), shape.getCenterY());

		circle = new Ellipse2D.Double(center.getX() - diameter / 2d, center.getY() - diameter / 2d, diameter, diameter);
	}

	/**
	 * Draws the region with the given attributes.
	 * @param g Graphics context
	 */
	public void draw(Graphics2D g)
	{
		// Save graphics attributes
		g.setPaint(getPaint());
		g.fill(circle);
	}

	/**
	 * Returns the bounding box of the region.
	 * @return The bounding box
	 */
	public Rectangle getBounds()
	{
		return circle.getBounds();
	}
}
