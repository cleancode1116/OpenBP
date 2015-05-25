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
package org.openbp.swing.components.wizard;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.SwingConstants;
import javax.swing.border.Border;

/**
 * Edge border.
 *
 * @author Heiko Erhardt
 */
public class EdgeBorder
	implements Border, SwingConstants
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Raised border */
	public static final int RAISED = 1;

	/** Lowered border */
	public static final int LOWERED = 2;

	/**
	 * Edge position
	 * (SwingConstants.NORTH/SwingConstants.SOUTH/SwingConstants.EAST/SwingConstants.WEST)
	 */
	protected int edgePosition = NORTH;

	/** Border style (RAISED/LOWERED) */
	protected int style = LOWERED;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public EdgeBorder()
	{
		this(NORTH);
	}

	/**
	 * Constructor.
	 *
	 * @param edgePosition Edge position (SwingConstants.NORTH/SwingConstants.SOUTH/SwingConstants.EAST/SwingConstants.WEST)
	 */
	public EdgeBorder(int edgePosition)
	{
		this.edgePosition = edgePosition;
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Gets the border insets according to the edge position.
	 *
	 * @param comp Not used
	 * @return The insets
	 */
	public Insets getBorderInsets(Component comp)
	{
		switch (edgePosition)
		{
		case SOUTH:
			return new Insets(0, 0, 2, 0);

		case EAST:
			return new Insets(0, 2, 0, 0);

		case WEST:
			return new Insets(0, 0, 0, 2);

		default:
			return new Insets(2, 0, 0, 0);
		}
	}

	/**
	 * Determines if the border is opaque.
	 * @return Always true
	 */
	public boolean isBorderOpaque()
	{
		return true;
	}

	//////////////////////////////////////////////////
	// @@ Painting
	//////////////////////////////////////////////////

	/**
	 * Paints the border.
	 *
	 * @param component Component
	 * @param g Graphics context
	 * @param x X position
	 * @param y Y position
	 * @param w Width
	 * @param h Height
	 */
	public void paintBorder(Component component, Graphics g, int x, int y, int w, int h)
	{
		if (style == RAISED)
			g.setColor(component.getBackground().brighter());
		else
			g.setColor(component.getBackground().darker());

		switch (edgePosition)
		{
		case SOUTH:
			g.drawLine(x, y + h - 2, w, y + h - 2);
			break;
		case EAST:
			g.drawLine(x + w - 2, y, x + w - 2, y + h);
			break;
		case WEST:
			g.drawLine(x + 1, y, x + 1, y + h);
			break;
		default:
			g.drawLine(x, y, x + w, y);
		}

		if (style == RAISED)
			g.setColor(component.getBackground().darker());
		else
			g.setColor(component.getBackground().brighter());

		switch (edgePosition)
		{
		case SOUTH:
			g.drawLine(x, y + h - 1, w, y + h - 1);
			break;
		case EAST:
			g.drawLine(x + w - 1, y, x + w - 1, y + h);
			break;
		case WEST:
			g.drawLine(x + 1, y, x + 1, y + h);
			break;
		default:
			g.drawLine(x, y + 1, x + w, y + 1);
		}
	}
}
