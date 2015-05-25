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

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;

/**
 * Border for Components in a ToolBar with rollover effect.
 * The same like ToolBarComponentBorder with other colors.
 *
 * @author Jens Ferchland
 */
public class ToolBarRollOverComponentBorder extends AbstractBorder
{
	private static final Insets INSETS = new Insets(1, 1, 1, 1);

	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
	{
		// set the color
		g.setColor(UIManager.getColor("Button.disabled_borderColor"));

		if (c instanceof AbstractButton)
		{
			AbstractButton b = (AbstractButton) c;
			ButtonModel model = b.getModel();
			if (b.isSelected())
				g.setColor(UIManager.getColor("Button.focusColor"));

			if (model.isRollover())
			{
				g.setColor(SkyTheme.COLOR_BORDER);
			}
		}

		// paint border
		g.drawRect(x, y, c.getWidth() - 1, c.getHeight() - 1);
	}

	public Insets getBorderInsets(Component c)
	{
		return INSETS;
	}
}
