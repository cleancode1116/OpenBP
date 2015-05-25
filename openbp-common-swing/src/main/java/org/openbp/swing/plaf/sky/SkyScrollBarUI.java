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

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalScrollBarUI;

/**
 * The Sky look and feel of a ScrollBar
 *
 * @author Jens Ferchland
 */
public class SkyScrollBarUI extends MetalScrollBarUI
{
	/**
	 * Creates a new MetalSplitPaneUI instance
	 * @nowarn
	 */
	public static ComponentUI createUI(JComponent comp)
	{
		return new SkyScrollBarUI();
	}

	/**
	 * See javax.swing.plaf.ComponentUI#installUI(JComponent)
	 * @nowarn
	 */
	public void installUI(JComponent comp)
	{
		super.installUI(comp);

		JScrollBar bar = (JScrollBar) comp;

		bar.setUnitIncrement(UIManager.getInt("ScrollBar.scrollUnitStep"));
		bar.setBlockIncrement(UIManager.getInt("ScrollBar.scrollBlockStep"));
	}
}
