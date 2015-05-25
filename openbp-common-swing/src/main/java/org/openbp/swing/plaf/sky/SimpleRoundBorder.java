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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

/**
 * A Round Border with one dark line.
 *
 * @author Jens Ferchland
 */
public class SimpleRoundBorder extends AbstractBorder
{
	private Insets insets;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Creates a new <code>SimpleRoundBorder</code> instance with the
	 * given Insets between Component and Border.
	 *
	 * @param insets an <code>Insets</code> value
	 */
	public SimpleRoundBorder(Insets insets)
	{
		this.insets = insets;
	}

	/**
	 * Creates a new <code>SimpleRoundBorder</code> instance with the given
	 * Insets between Componetn and Border.
	 *
	 * @param top the top offset
	 * @param left the left offset
	 * @param bottom the bottom offset
	 * @param right the right offset
	 */
	public SimpleRoundBorder(int top, int left, int bottom, int right)
	{
		this(new Insets(top, left, bottom, right));
	}

	/**
	 * Creates a new <code>SimpleRoundBorder</code> instance with no Insets.
	 */
	public SimpleRoundBorder()
	{
		this(0, 0, 0, 0);
	}

	/**
	 * Implements AbstracBorder and paints the Border around the Component.
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
		g.setColor(SkyTheme.COLOR_BORDER);
		g.drawRoundRect(x, y, width - 1, height - 1, SkyUtil.RADIUS, SkyUtil.RADIUS);
	}

	/**
	 * Implements AbstractBorder and returns teh current Insets.
	 *
	 * @param c a <code>Component</code> value
	 * @return an <code>Insets</code> value
	 */
	public Insets getBorderInsets(Component c)
	{
		return insets;
	}
}
