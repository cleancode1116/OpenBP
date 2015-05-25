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

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalToolBarUI;

/**
 * This class describe a tool bar ui.
 *
 * @author Jens Ferchland
 */
public class SkyToolBarUI extends MetalToolBarUI
{
	/**
	 * Create a UI.
	 *
	 * @param c a <code>JComponent</code> value
	 * @return a <code>ComponentUI</code> value
	 */
	public static ComponentUI createUI(JComponent c)
	{
		return new SkyToolBarUI();
	}

	public void installUI(JComponent c)
	{
		super.installUI(c);
		if (c instanceof JToolBar)
			((JToolBar) c).putClientProperty("JToolBar.isRollover", Boolean.TRUE);
	}

	/**
	 * Create a border for mouse rollover effect.
	 *
	 * @return a <code>Border</code> value
	 */
	protected Border createRolloverBorder()
	{
		return new ToolBarRollOverComponentBorder();
	}

	/**
	 * Create a border for a normal component in the toolbar.
	 *
	 * @return a <code>Border</code> value
	 */
	protected Border createNonRolloverBorder()
	{
		return new ToolBarComponentBorder();
	}

	protected void setBorderToRollover(Component c)
	{
		if (c instanceof AbstractButton)
		{
			AbstractButton b = (AbstractButton) c;
			ComponentUI ui = b.getUI();
			if (ui instanceof SkyButtonUI)
				((SkyButtonUI) ui).setShadowOn(false);

			b.setBorder(createRolloverBorder());
		}
		super.setBorderToRollover(c);
	}

	protected void setBorderToNonRollover(Component c)
	{
		if (c instanceof AbstractButton)
		{
			AbstractButton b = (AbstractButton) c;
			ComponentUI ui = b.getUI();
			if (ui instanceof SkyButtonUI)
				((SkyButtonUI) ui).setShadowOn(false);
			b.setBorder(createNonRolloverBorder());
		}
		super.setBorderToRollover(c);
	}
}
