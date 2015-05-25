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
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalButtonUI;

/**
 * This class discribes the JButton UI. This is a Singleton.
 *
 * @author Jens Ferchland
 */
public class SkyButtonUI extends MetalButtonUI
{
	private Rectangle iconrect = new Rectangle();

	private Rectangle textrect = new Rectangle();

	private Rectangle viewrect = new Rectangle();

	private String text;

	private int shadowDepth = SkyUtil.DEFAULTSHADOWDEPTH;

	private boolean isShadowOn = true;

	/**
	 * Create and returns the Singleton-instance of the JButton UI.
	 *
	 * @param c a <code>JComponent</code> value
	 * @return a <code>ComponentUI</code> value
	 */
	public static ComponentUI createUI(JComponent c)
	{
		return new SkyButtonUI();
	}

	/**
	 * Installs the UI to a JButton. This method sets all Listeners,
	 * which are used to control the button.
	 *
	 * @param c a <code>JComponent</code> value
	 */
	public void installUI(JComponent c)
	{
		AbstractButton b = (AbstractButton) c;
		super.installUI(b);

		LookAndFeel.installColorsAndFont(b, "Button.enabled_background", "Button.enabled_foreground", "Button.enabled_font");
		LookAndFeel.installBorder(b, "Button.border");
	}

	/**
	 * Returns true if the shadow is painted.
	 */
	public boolean isShadowOn()
	{
		return isShadowOn;
	}

	/**
	 * Switchs the shadow on and off.
	 */
	public void setShadowOn(boolean shadowOn)
	{
		this.isShadowOn = shadowOn;
	}

	/**
	 * Sets the depth of the shadow.
	 */
	public void setShadowDepth(int depth)
	{
		this.shadowDepth = depth;
	}

	/**
	 * Returns the depth of the shadow.
	 */
	public int getShadowDepth()
	{
		return shadowDepth;
	}

	/**
	 * Calculates the depth of the shadow of a button.
	 */
	private int calculateShadowDepth(AbstractButton b)
	{
		if (isShadowOn())
		{
			int depth = SkyUtil.DEFAULTSHADOWDEPTH;
			if (b.getModel().isPressed())
				depth -= 2;
			else if (b.getModel().isRollover())
				depth += 2;
			return depth;
		}
		else
			return 0;
	}

	/**
	 * Paint the GUI of a JButton.
	 *
	 * @param g a <code>Graphics</code> value
	 * @param c a <code>JComponent</code> value
	 */
	public void paint(Graphics g, JComponent c)
	{
		// the border is painted automaticaly by the component,
		// so we needn't to paint the border here

		Graphics2D g2d = (Graphics2D) g;
		AbstractButton b = (AbstractButton) c;
		g.setFont(b.getFont());

		setShadowDepth(calculateShadowDepth(b));

		if (isShadowOn())
		{
			// calculating the original button region
			viewrect.x = SkyUtil.MAXSHADOWDEPTH - getShadowDepth();
			viewrect.y = SkyUtil.MAXSHADOWDEPTH - getShadowDepth();
			viewrect.width = b.getWidth() - SkyUtil.MAXSHADOWDEPTH;
			viewrect.height = b.getHeight() - SkyUtil.MAXSHADOWDEPTH;
		}
		else
		{
			viewrect.x = 0;
			viewrect.y = 0;
			viewrect.width = b.getWidth();
			viewrect.height = b.getHeight();
		}

		// clear background
		g2d.setColor(b.getParent().getBackground());
		g2d.fillRect(0, 0, b.getWidth(), b.getHeight());

		// paint button background
		g2d.setColor(b.getBackground());
		if (b.isSelected())
		{
			g2d.setColor(UIManager.getColor("Button.selectedBackground"));
		}
		else if (b.getModel().isPressed())
		{
			g2d.setColor(UIManager.getColor("Button.pressedBackground"));
		}
		g2d.fillRect(viewrect.x, viewrect.y, viewrect.width, viewrect.height);

		// lay out the button - if the button is to small a shorter text will returned (like <...>)
		iconrect.x = iconrect.y = iconrect.width = iconrect.height = 0;
		textrect.x = textrect.y = textrect.width = textrect.height = 0;
		text = SwingUtilities.layoutCompoundLabel(b, g.getFontMetrics(), b.getText(), b.getIcon(), b.getVerticalAlignment(), b.getHorizontalAlignment(), b.getVerticalTextPosition(), b.getHorizontalTextPosition(), viewrect, iconrect, textrect, b.getText() == null ? 0 : b.getIconTextGap());

		// paint the icon
		try
		{
			paintIcon(g, b, iconrect);
		}
		catch (NullPointerException ex)
		{
			// TODO Cleanup 5: This happens sometimes in Message Boxes...
		}

		// paint the text
		paintText(g, c, textrect, text);

		if (b.isFocusOwner())
		{
			// Button has focus - display it
			int fx1 = textrect.x;
			int fy = textrect.y + textrect.height - 1;
			int fx2 = fx1 + textrect.width;

			g2d.setColor(UIManager.getColor("Button.focusColor"));

			// Paint a single line below the text
			g2d.drawLine(fx1, fy, fx2, fy);
		}
	}

	/**
	 * Returns the preferred size of an button including shadow.
	 *
	 * @param c a <code>JComponent</code> value
	 * @return a <code>Dimension</code> value
	 */
	public Dimension getPreferredSize(JComponent c)
	{
		// get the Size of superobject and add shadow
		Dimension d = super.getPreferredSize(c);

		if (d == null)
			return null;

		d.width += SkyUtil.MAXSHADOWDEPTH;
		d.height += SkyUtil.MAXSHADOWDEPTH;
		return d;
	}
}
