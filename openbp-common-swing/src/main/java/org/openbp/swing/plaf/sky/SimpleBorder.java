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
package org.openbp.swing.plaf.sky;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

/**
 * A rectangular border consisting of a single line.
 * The line can have a particular color and width.
 * Note that the insets of the border must be larger or equal than the width.
 *
 * @author Jens Ferchland
 */
public class SimpleBorder extends AbstractBorder
{
	/** Insets of the border */
	private Insets insets;

	/** Border width */
	private int borderWidth;

	/** Border color */
	private Color color;

	/** Standard border */
	private static final SimpleBorder standardBorder = new SimpleBorder();

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the standard instance of this border.
	 *
	 * @return A simple border with inset 1 and default width and color
	 */
	public static SimpleBorder getStandardBorder()
	{
		return standardBorder;
	}

	/**
	 * Default constructor.
	 */
	public SimpleBorder()
	{
		this(1, 1, 1, 1);
	}

	/**
	 * Creates a new <code>SimpleBorder</code> instance with the
	 * given Insets between Border and Component.
	 *
	 * @param insets The border insets
	 */
	public SimpleBorder(Insets insets)
	{
		this.insets = insets;
		borderWidth = 1;
	}

	/**
	 * Creates a new <code>SimpleBorder</code> instance with the
	 * given Insets between border and Component.
	 *
	 * @param top The top offset
	 * @param left The left offset
	 * @param bottom The bottom offset
	 * @param right The right offset
	 */
	public SimpleBorder(int top, int left, int bottom, int right)
	{
		this(new Insets(top, left, bottom, right));
	}

	/**
	 * Gets the border width.
	 * @nowarn
	 */
	public int getWidth()
	{
		return borderWidth;
	}

	/**
	 * Sets the border width.
	 * @nowarn
	 */
	public void setWidth(int borderWidth)
	{
		this.borderWidth = borderWidth;
	}

	/**
	 * Gets the border color.
	 * @return The color or null for the default (gray)
	 */
	public Color getColor()
	{
		return color;
	}

	/**
	 * Sets the border color.
	 * @param color The color or null for the default (gray)
	 */
	public void setColor(Color color)
	{
		this.color = color;
	}

	/**
	 * Implements AbstractBorder. Paints the Border
	 * around the Component.
	 *
	 * @param c a <code>Component</code> value
	 * @param g a <code>Graphics</code> value
	 * @param x an <code>int</code> value
	 * @param y an <code>int</code> value
	 * @param width an <code>int</code> value
	 * @param height an <code>int</code> value
	 */
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
	{
		g.setColor(color != null ? color : SkyTheme.COLOR_BORDER);

		if (insets.top > 0 && insets.left > 0 && insets.bottom > 0 && insets.right > 0)
		{
			// Draw the entire border layer by layer
			int n = borderWidth;
			while (n-- > 0)
			{
				g.drawRect(x, y, width - 1, height - 1);

				x += 1;
				y += 1;
				width -= 2;
				height -= 2;
			}
		}
		else
		{
			// Draw the edges that have an inset defined only
			int n = borderWidth;
			while (n-- > 0)
			{
				int xEnd = x + width;
				int yEnd = y + height;

				if (insets.top > 0)
				{
					g.drawLine(x, y, xEnd, y);
				}

				if (insets.left > 0)
				{
					g.drawLine(x, y, x, yEnd);
				}

				if (insets.bottom > 0)
				{
					g.drawLine(x, yEnd, xEnd, yEnd);
				}

				if (insets.right > 0)
				{
					g.drawLine(xEnd, y, xEnd, yEnd);
				}

				x += 1;
				y += 1;
				width -= 2;
				height -= 2;
			}
		}
	}

	/**
	 * Gets the insets of the border
	 *
	 * @param c The component the border belongs to
	 * @return The border insets
	 */
	public Insets getBorderInsets(Component c)
	{
		return insets;
	}
}
