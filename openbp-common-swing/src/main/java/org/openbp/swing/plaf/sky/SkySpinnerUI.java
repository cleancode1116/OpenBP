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

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSpinnerUI;

/**
 * This is the SpinnerUI for the sky look&feel.
 *
 * @author Jens Ferchland
 */
public class SkySpinnerUI extends BasicSpinnerUI
{
	/** border for buttons in the spinner */
	static final Border BUTTONBORDER = new SimpleBorder(0, 0, 0, 0);

	/**
	 * Creates a new UI for a spinner.
	 *
	 * @see javax.swing.plaf.ComponentUI#createUI(JComponent)
	 */
	public static ComponentUI createUI(JComponent comp)
	{
		return new SkySpinnerUI();
	}

	/**
	 * @see javax.swing.plaf.basic.BasicSpinnerUI#createNextButton()
	 */
	protected Component createNextButton()
	{
		Component c = super.createNextButton();

		if (c instanceof JComponent)
		{
			((JComponent) c).setBorder(BUTTONBORDER);
		}

		return c;
	}

	/**
	 * @see javax.swing.plaf.basic.BasicSpinnerUI#createPreviousButton()
	 */
	protected Component createPreviousButton()
	{
		Component c = super.createNextButton();

		if (c instanceof JComponent)
		{
			((JComponent) c).setBorder(BUTTONBORDER);
		}

		return c;
	}
}
