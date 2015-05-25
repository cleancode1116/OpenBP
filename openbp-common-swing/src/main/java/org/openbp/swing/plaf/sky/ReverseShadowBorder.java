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
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import javax.swing.border.AbstractBorder;

/**
 * Border for Panels. This Border paint a RoundRect
 * and place a shadow at the right and at the bottom. This Border can be used to
 * give any component a round shadow.
 *
 * @author Jens Ferchland
 */
public class ReverseShadowBorder extends AbstractBorder
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	private static final Color DARKCOLOR = new Color(0, 0, 0, 120);

	private static final Color LIGHTCOLOR = new Color(255, 255, 255, 100);

	/** Default insets */
	private static final Insets INSETS = new Insets(4, 5, 2, 2);

	//////////////////////////////////////////////////
	// @@ Overwritten methods
	//////////////////////////////////////////////////

	/**
	 * see AbstractBorder#paintBorder (Component c, Graphics g, int x, int y, int width, int height)
	 * @nowarn
	 */
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
	{
		Shape clip = g.getClip();

		int smooth = (int) Math.min((double) width, (double) height) / 2;
		if (smooth > 4)
			smooth = 4;

		// calculate clip for shadow
		Area area = new Area(new Rectangle2D.Double(x, y, width, height));

		area.subtract(new Area(new Rectangle2D.Double(x + smooth, y + smooth, width - 1, height - 1)));

		// Be sure we don't paint in a foreign area.
		area.intersect(new Area(clip.getBounds()));

		g.setClip(area);

		g.setColor(DARKCOLOR);
		g.fillRect(x, y, width - 1, height - 1);

		g.setColor(LIGHTCOLOR);

		// paint parts of shadow
		for (int i = 0; i < smooth; i++)
		{
			g.fillRect(x + i, y + i, width - (2 * i), height - (2 * i));
		}

		// set old clip
		g.setClip(clip);

		// paint a round rect around the content
		g.setColor(SkyTheme.COLOR_BORDER);
		g.drawRect(x, y, width - 2, height - 2);
	}

	/**
	 * see AbstractBorder#getBorderInsets (Component c)
	 * @nowarn
	 */
	public Insets getBorderInsets(Component c)
	{
		return INSETS;
	}
}
