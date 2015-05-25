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

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.metal.MetalLabelUI;
import javax.swing.text.View;

/**
 * UI for JLabels.
 * This class is a singleton.
 *
 * @author Heiko Erhardt
 */
public class SkyLabelUI extends MetalLabelUI
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/** Singleton instance */
	private static SkyLabelUI singletonInstance = new SkyLabelUI();

	/**
	 * Returns the singleton instance of the UI.
	 *
	 * @param c Component to use the ui for
	 * @return The ui
	 */
	public static ComponentUI createUI(JComponent c)
	{
		return singletonInstance;
	}

	/**
	 * Default constructor.
	 */
	public SkyLabelUI()
	{
	}

	//////////////////////////////////////////////////
	// @@ Painting
	//////////////////////////////////////////////////

	/* These rectangles/insets are allocated once for this shared LabelUI
	 * implementation.  Re-using rectangles rather than allocating
	 * them in each paint call halved the time it took paint to run.
	 */
	private static Rectangle paintIconR = new Rectangle();

	private static Rectangle paintTextR = new Rectangle();

	private static Rectangle paintViewR = new Rectangle();

	private static Insets paintViewInsets = new Insets(0, 0, 0, 0);

	/**
	 * Paint method of the label.
	 * Copied from BasicLabelUI.
	 * If the label text is clipped (i. e. ends with "..."), the tool tip of the label
	 * is set to display the entire text if it is not set already.
	 *
	 * @param g Graphics context
	 * @param c Component to paint
	 */
	public void paint(Graphics g, JComponent c)
	{
		JLabel label = (JLabel) c;
		String text = label.getText();
		Icon icon = (label.isEnabled()) ? label.getIcon() : label.getDisabledIcon();

		if ((icon == null) && (text == null))
		{
			return;
		}

		FontMetrics fm = g.getFontMetrics();
		paintViewInsets = c.getInsets(paintViewInsets);

		paintViewR.x = paintViewInsets.left;
		paintViewR.y = paintViewInsets.top;
		paintViewR.width = c.getWidth() - (paintViewInsets.left + paintViewInsets.right);
		paintViewR.height = c.getHeight() - (paintViewInsets.top + paintViewInsets.bottom);

		paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
		paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;

		String clippedText = layoutCL(label, fm, text, icon, paintViewR, paintIconR, paintTextR);

		if (clippedText != null && clippedText.endsWith("..."))
		{
			// TODO Feature 3: Somehow this does not really work in JTables...
			// Clipped, set the entire text as tool tip if no tool tip provided yet
			if (label.getToolTipText() == null)
			{
				label.setToolTipText(text);
			}
		}

		if (icon != null)
		{
			icon.paintIcon(c, g, paintIconR.x, paintIconR.y);
		}

		if (text != null)
		{
			View v = (View) c.getClientProperty(BasicHTML.propertyKey);
			if (v != null)
			{
				v.paint(g, paintTextR);
			}
			else
			{
				int textX = paintTextR.x;
				int textY = paintTextR.y + fm.getAscent();

				if (label.isEnabled())
				{
					paintEnabledText(label, g, clippedText, textX, textY);
				}
				else
				{
					paintDisabledText(label, g, clippedText, textX, textY);
				}
			}
		}
	}
}
