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

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import org.openbp.cockpit.modeler.ModelerColors;
import org.openbp.cockpit.modeler.figures.process.TextElementFigure;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Locator;
import CH.ifa.draw.standard.LocatorHandle;

/**
 * Handle for a waypoint of a spline.
 *
 * @author Stephan Moritz
 */
public class XFontSizeHandle extends LocatorHandle
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Current font */
	private Font currentFont;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 * @param owner The owning text figure
	 * @param locator Handle locator
	 */
	public XFontSizeHandle(TextElementFigure owner, Locator locator)
	{
		super(owner, locator);
	}

	//////////////////////////////////////////////////
	// @@ AbstractHandle overrides
	//////////////////////////////////////////////////

	/**
	 * @see CH.ifa.draw.standard.LocatorHandle#invokeStart(int x, int y, DrawingView view)
	 */
	public void invokeStart(int x, int y, DrawingView view)
	{
		TextElementFigure textOwner = (TextElementFigure) owner();
		currentFont = textOwner.getFont();
	}

	/**
	 * @see CH.ifa.draw.standard.LocatorHandle#invokeStep(int x, int y, int anchorX, int anchorY, DrawingView view)
	 */
	public void invokeStep(int x, int y, int anchorX, int anchorY, DrawingView view)
	{
		TextElementFigure textOwner = (TextElementFigure) owner();

		// Compute the distance to the original position; apply a factor so the resizing effect won't be too strong
		int xDiff = x - anchorX;
		int yDiff = y - anchorY;
		int dist = (int) Math.sqrt(xDiff * xDiff + yDiff * yDiff) / 10;
		if (xDiff + yDiff < 0)
		{
			// Moving to the upper left means smaller
			dist = -dist;
		}

		int newSize = currentFont.getSize() + dist;
		if (newSize < 8)
		{
			// No smaller than 8 pt
			newSize = 8;
		}

		textOwner.setFont(new Font(currentFont.getName(), currentFont.getStyle(), newSize));
	}

	/**
	 * @see CH.ifa.draw.standard.LocatorHandle#invokeEnd(int x, int y, int anchorX, int anchorY, DrawingView view)
	 */
	public void invokeEnd(int x, int y, int anchorX, int anchorY, DrawingView view)
	{
	}

	/**
	 * @see CH.ifa.draw.standard.LocatorHandle#draw(Graphics g)
	 */
	public void draw(Graphics g)
	{
		Rectangle r = displayBox();

		g.setColor(ModelerColors.HANDLE_TEXTSIZE_FILL);
		g.fillOval(r.x, r.y, r.width, r.height);

		g.setColor(ModelerColors.HANDLE_LINE);
		g.drawOval(r.x, r.y, r.width, r.height);
	}
}
