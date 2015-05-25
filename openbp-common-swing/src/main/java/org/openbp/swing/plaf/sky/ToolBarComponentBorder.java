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
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;

/**
 * Border for Components in a ToolBar. This Border paint
 * a colored RoundRect around the Component without shadow.
 *
 * @author Jens Ferchland
 */
public class ToolBarComponentBorder extends AbstractBorder
{
	private static final Insets INSETS = new Insets(1, 1, 1, 1);

	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
	{
		// set the color
		g.setColor(UIManager.getColor("Button.disabled_borderColor"));

		if (c.isEnabled())
			g.setColor(UIManager.getColor("Button.enabled_borderColor"));

		if (c instanceof AbstractButton)
			if (((AbstractButton) c).isSelected())
				g.setColor(UIManager.getColor("Button.focusColor"));

		// paint border
		g.drawRect(x, y, c.getWidth() - 1, c.getHeight() - 1);
	}

	public Insets getBorderInsets(Component c)
	{
		return INSETS;
	}
}
