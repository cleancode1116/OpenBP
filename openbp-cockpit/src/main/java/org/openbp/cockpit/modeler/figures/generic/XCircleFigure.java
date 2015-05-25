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

import java.awt.Rectangle;

/**
 * Extended circle figure.
 *
 * @author Heiko Erhardt
 */
public class XCircleFigure extends XEllipseFigure
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public XCircleFigure()
	{
	}

	//////////////////////////////////////////////////
	// @@ AbstractEllipseFigure overrides
	//////////////////////////////////////////////////

	/**
	 * Returns the rectangle that determines the ellipse.
	 * Usually, this equals the display box.
	 * However, if some subclass wants to constrain the ellipse, this may differ.
	 *
	 * @return The ellipse rectangle
	 */
	public Rectangle ellipseBox()
	{
		Rectangle r = box;
		if (r.width != r.height)
		{
			// Force the ellipse width to be equal to the figure height
			int xOffset = (r.width - r.height) / 2;
			r = new Rectangle(r.x + xOffset, r.y, r.height, r.height);
		}
		return r;
	}
}
