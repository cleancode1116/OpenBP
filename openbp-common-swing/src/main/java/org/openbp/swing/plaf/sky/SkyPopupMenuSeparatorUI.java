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

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalPopupMenuSeparatorUI;

/**
 * This is the default UI for a PopupMenuSeparator.
 *
 * @author Jens Ferchland
 */
public class SkyPopupMenuSeparatorUI extends MetalPopupMenuSeparatorUI
{
	private static final Dimension SIZE = new Dimension(0, 1);

	/**
	 * Create and returns a instance of the UI.
	 *
	 * @param c the Separator
	 * @return the
	 */
	public static ComponentUI createUI(JComponent c)
	{
		return new SkyPopupMenuSeparatorUI();
	}

	/**
	 * Installs the UI to a JButton. This method sets all Listeners,
	 * which are used to control the button.
	 *
	 * @param c a <code>JComponent</code> value
	 */
	public void installUI(JComponent c)
	{
		LookAndFeel.installColors(c, "Separator.background", "Separator.foreground");
	}

	/**
	 * Paints the UI.
	 * @nowarn
	 */
	public void paint(Graphics g, JComponent c)
	{
		Dimension s = c.getSize();

		g.setColor(c.getBackground());
		g.drawLine(0, 0, s.width, 0);
	}

	/**
	 * Returns the size of the Separator.
	 * @nowarn
	 */
	public Dimension getPreferredSize(JComponent c)
	{
		return SIZE;
	}
}
