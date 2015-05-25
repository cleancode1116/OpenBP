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

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

/**
 * The UI for toggle buttons.
 */
public class SkyToggleButtonUI extends SkyButtonUI
{
	/**
	 * Create and returns the UI of the JButton.
	 *
	 * @param c a <code>JComponent</code> value
	 * @return a <code>ComponentUI</code> value
	 */
	public static ComponentUI createUI(JComponent c)
	{
		return new SkyToggleButtonUI();
	}

	/**
	 * ToggleButtons have another shadow.
	 * @nowarn
	 */
	protected int calculateShadowDepth(AbstractButton b)
	{
		if (isShadowOn())
		{
			int depth = SkyUtil.DEFAULTSHADOWDEPTH;
			if (b.isSelected())
				depth -= 4;
			if (b.getModel().isPressed())
				depth -= 2;
			else if (b.getModel().isRollover())
				depth += 2;
			return depth;
		}
		else
			return 0;
	}
}
