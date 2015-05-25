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

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Stroke;

import javax.swing.AbstractButton;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.ComponentUI;

/**
 * for Buttons. Use this Border only for Buttons. This border
 * contains functions for displaying the focus.
 *
 * @author Jens Ferchland
 */
public class ButtonBorder extends AbstractBorder
{
	private static final BasicStroke LINESTYLE = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

	private static final Insets INSETS = new Insets(4, 12, 4, 12);

	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
	{
		// place the button in the right height
		int shadowheight = 0;
		if (c instanceof AbstractButton)
		{
			ComponentUI ui = ((AbstractButton) c).getUI();
			if (ui instanceof SkyButtonUI)
				shadowheight = ((SkyButtonUI) ui).getShadowDepth();
		}

		// set the color
		String colorName = null;
		if (c.isEnabled())
		{
			colorName = c.hasFocus() ? "Button.focusColor" : "Button.enabled_borderColor";
		}
		else
		{
			colorName = "Button.disabled_borderColor";
		}
		g.setColor(UIManager.getColor(colorName));

		if (!(g instanceof Graphics2D))
			return;

		Graphics2D g2 = (Graphics2D) g;

		Stroke backup = g2.getStroke();

		// paint border
		g2.setStroke(LINESTYLE);
		g.drawRect(x + SkyUtil.MAXSHADOWDEPTH - shadowheight + 1, y + SkyUtil.MAXSHADOWDEPTH - shadowheight + 1, c.getWidth() - SkyUtil.MAXSHADOWDEPTH - 2, c.getHeight() - SkyUtil.MAXSHADOWDEPTH - 2);
		g2.setStroke(backup);

		g.setColor(SkyTheme.COLOR_BORDER);
		g.drawRect(x + SkyUtil.MAXSHADOWDEPTH - shadowheight, y + SkyUtil.MAXSHADOWDEPTH - shadowheight, c.getWidth() - SkyUtil.MAXSHADOWDEPTH, c.getHeight() - SkyUtil.MAXSHADOWDEPTH);

		SkyUtil.paintRectShadow(g, SkyUtil.MAXSHADOWDEPTH, SkyUtil.MAXSHADOWDEPTH, c.getWidth() - SkyUtil.MAXSHADOWDEPTH, c.getHeight() - SkyUtil.MAXSHADOWDEPTH, shadowheight);
	}

	public Insets getBorderInsets(Component c)
	{
		return INSETS;
	}
}
