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
package org.openbp.common.icon;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * Icon that can be placed over an existing icon. This class is usally used
 * in accordance with the flyweight pattern, i.e. one instance is used for
 * multiple icons.
 *
 * @author Stephan Moritz
 */
public class OverlayIcon extends MultiImageIcon
{
	/////////////////////////////////////////////////////////////////////////
	// @@ members
	/////////////////////////////////////////////////////////////////////////

	/** The background over which this icon is to be drawn. */
	private Icon background;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new OverlayIcon with master out of master.
	 *
	 * @param master The icon that should be the master of this overlay icon
	 */
	public OverlayIcon(MultiImageIcon master)
	{
		super(master);
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ methods
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Sets the background.
	 * @param background The background to set
	 * @return The changed OverlayIcon
	 */
	public OverlayIcon setBackground(Icon background)
	{
		this.background = background;

		setIconSize(background.getIconHeight());

		return this;
	}

	/**
	 * Paints first the background and the overlay on top of each other.
	 *
	 * @param c The component to paint on
	 * @param g The graphics object to be used for painting
	 * @param x The x coordinate where the icon should be painted
	 * @param y The y coordinate where the icon should be painted
	 */
	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		// draw background
		background.paintIcon(c, g, x, y);

		super.paintIcon(c, g, x, y);
	}
}
