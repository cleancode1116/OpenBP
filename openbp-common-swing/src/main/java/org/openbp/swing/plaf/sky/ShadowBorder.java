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
 * Border for Panels. This Border paint a Rectangle
 * and place a shadow at the right and at the bottom. This Border can be used to
 * give any component a shadow.
 *
 * @author Jens Ferchland
 */
public class ShadowBorder extends AbstractBorder
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Default insets */
	private static final Insets INSETS = new Insets(3, 3, SkyUtil.DEFAULTSHADOWDEPTH + 2, SkyUtil.DEFAULTSHADOWDEPTH + 2);

	/** Flag if the inlay border should be painted */
	private boolean inlayBorder;

	/**
	 * Creats a new shadow border.
	 * The inlay border is .
	 * @param inlayBorder
	 *		true	Paints the inlay border (a small line around the component).<br>
	 *		false	Paints the shadow only.
	 */
	public ShadowBorder(boolean inlayBorder)
	{
		this.inlayBorder = inlayBorder;
	}

	/**
	 * Creates a new ShadowBorder with a inlay border.
	 */
	public ShadowBorder()
	{
		this(true);
	}

	//////////////////////////////////////////////////
	// @@ Overwritten methods
	//////////////////////////////////////////////////

	/**
	 * see AbstractBorder#paintBorder (Component c, Graphics g, int x, int y, int width, int height)
	 * @nowarn
	 */
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
	{
		// Called by the UI before the content of the panel will painted,
		// but the Panel doesn't paint the background again, so we have to clear the background here.
		SkyUtil.paintRectShadow(g, x, y, width, height);

		if (inlayBorder)
		{
			// Paint a rect around the content
			g.setColor(SkyTheme.COLOR_BORDER);
			g.drawRect(x, y, width - SkyUtil.DEFAULTSHADOWDEPTH, height - SkyUtil.DEFAULTSHADOWDEPTH);
		}
	}

	/**
	 * See AbstractBorder#getBorderInsets (Component c)
	 * @nowarn
	 */
	public Insets getBorderInsets(Component c)
	{
		return INSETS;
	}
}
