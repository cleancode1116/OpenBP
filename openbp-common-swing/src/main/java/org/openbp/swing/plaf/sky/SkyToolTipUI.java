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
import javax.swing.JToolTip;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalToolTipUI;

import org.openbp.swing.AdvancedAccelerator;

/**
 * UI for a ToolTip.
 *
 * @author Jens Ferchland
 */
public class SkyToolTipUI extends MetalToolTipUI
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** the tooltip of this ui */
	private JToolTip tooltip;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor for SkyToolTipUI.
	 */
	private SkyToolTipUI(JToolTip tip)
	{
		super();
		this.tooltip = tip;
	}

	/**
	 * Create and returns the UI of a ToolTip.
	 *
	 * @param c a <code>JComponent</code> value
	 * @return a <code>ComponentUI</code> value
	 */
	public static ComponentUI createUI(JComponent c)
	{
		return new SkyToolTipUI((JToolTip) c);
	}

	/**
	 * Installs the UI on a componente.
	 * @nowarn
	 */
	public void installUI(JComponent c)
	{
		super.installUI(c);

		if (tooltip != c)
		{
			if (c instanceof JToolTip)
			{
				this.tooltip = (JToolTip) c;
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Overrides
	//////////////////////////////////////////////////

	/**
	 * @see javax.swing.plaf.metal.MetalToolTipUI#getAcceleratorString()
	 */
	public String getAcceleratorString()
	{
		JComponent parent = tooltip.getComponent();

		if (parent instanceof AdvancedAccelerator)
		{
			String s = ((AdvancedAccelerator) parent).getAcceleratorString();
			if (s == null)
			{
				// The super class doesn't like null pointers...
				s = "";
			}
			return s;
		}

		return super.getAcceleratorString();
	}
}
