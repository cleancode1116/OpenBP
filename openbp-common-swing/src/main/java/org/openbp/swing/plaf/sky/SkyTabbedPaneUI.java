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

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalTabbedPaneUI;

/**
 * LookAndFeel for a TabbedPane
 *
 * @author Jens Ferchland
 */
public class SkyTabbedPaneUI extends MetalTabbedPaneUI
{
	// line style for focus
	private static final BasicStroke LINESTYLE = new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

	/**
	 * Creates a new Instance of the SkyTabbedPaneUI.
	 *
	 * @param c a <code>JComponent</code> value
	 * @return a <code>ComponentUI</code> value
	 */
	public static ComponentUI createUI(JComponent c)
	{
		return new SkyTabbedPaneUI();
	}

	/**
	 * Paint a tab when its positioned on top of the pane.
	 *
	 * @param tabIndex an <code>int</code> value
	 * @param g a <code>Graphics</code> value
	 * @param x an <code>int</code> value
	 * @param y an <code>int</code> value
	 * @param w an <code>int</code> value
	 * @param h an <code>int</code> value
	 * @param btm an <code>int</code> value
	 * @param rght an <code>int</code> value
	 * @param isSelected a <code>boolean</code> value
	 */
	protected void paintTopTabBorder(int tabIndex, Graphics g, int x, int y, int w, int h, int btm, int rght, boolean isSelected)
	{
		int currentRun = getRunForTab(tabPane.getTabCount(), tabIndex);
		boolean leftToRight = tabPane.getComponentOrientation().isLeftToRight();
		int bottom = h - 1;
		int right = w - 1;

		if (shouldFillGap(currentRun, tabIndex, x, y))
		{
			g.translate(x, y);

			if (leftToRight)
			{
				g.setColor(getColorForGap(currentRun, x, y + 1));
				g.fillRect(1, 0, 5, 3);
				g.fillRect(1, 3, 2, 2);
			}
			else
			{
				g.setColor(getColorForGap(currentRun, x + w - 1, y + 1));
				g.fillRect(right - 5, 0, 5, 3);
				g.fillRect(right - 2, 3, 2, 2);
			}

			g.translate(-x, -y);
		}

		g.translate(x, y);

		g.setColor(SkyTheme.COLOR_BORDER);
		g.drawRect(0, 0, right, bottom);

		g.translate(-x, -y);
	}

	/**
	 * Paint a tab when its positioned on the left of the pane.
	 *
	 * @param tabIndex an <code>int</code> value
	 * @param g a <code>Graphics</code> value
	 * @param x an <code>int</code> value
	 * @param y an <code>int</code> value
	 * @param w an <code>int</code> value
	 * @param h an <code>int</code> value
	 * @param btm an <code>int</code> value
	 * @param rght an <code>int</code> value
	 * @param isSelected a <code>boolean</code> value
	 */
	protected void paintLeftTabBorder(int tabIndex, Graphics g, int x, int y, int w, int h, int btm, int rght, boolean isSelected)
	{
		int currentRun = getRunForTab(tabPane.getTabCount(), tabIndex);
		boolean leftToRight = tabPane.getComponentOrientation().isLeftToRight();
		int bottom = h - 1;
		int right = w - 1;

		if (shouldFillGap(currentRun, tabIndex, x, y))
		{
			g.translate(x, y);

			if (leftToRight)
			{
				g.setColor(getColorForGap(currentRun, x, y + 1));
				g.fillRect(1, 0, 5, 3);
				g.fillRect(1, 3, 2, 2);
			}
			else
			{
				g.setColor(getColorForGap(currentRun, x + w - 1, y + 1));
				g.fillRect(right - 5, 0, 5, 3);
				g.fillRect(right - 2, 3, 2, 2);
			}

			g.translate(-x, -y);
		}

		g.translate(x, y);

		g.setColor(SkyTheme.COLOR_BORDER);
		g.drawRect(0, 0, right, bottom);

		g.translate(-x, -y);
	}

	/**
	 * Paint a tab when its positioned on bottom of the pane.
	 *
	 * @param tabIndex an <code>int</code> value
	 * @param g a <code>Graphics</code> value
	 * @param x an <code>int</code> value
	 * @param y an <code>int</code> value
	 * @param w an <code>int</code> value
	 * @param h an <code>int</code> value
	 * @param btm an <code>int</code> value
	 * @param rght an <code>int</code> value
	 * @param isSelected a <code>boolean</code> value
	 */
	protected void paintBottomTabBorder(int tabIndex, Graphics g, int x, int y, int w, int h, int btm, int rght, boolean isSelected)
	{
		int currentRun = getRunForTab(tabPane.getTabCount(), tabIndex);
		boolean leftToRight = tabPane.getComponentOrientation().isLeftToRight();
		int bottom = h - 1;
		int right = w - 1;

		if (shouldFillGap(currentRun, tabIndex, x, y))
		{
			g.translate(x, y);

			if (leftToRight)
			{
				g.setColor(getColorForGap(currentRun, x, y + 1));
				g.fillRect(1, 0, 5, 3);
				g.fillRect(1, 3, 2, 2);
			}
			else
			{
				g.setColor(getColorForGap(currentRun, x + w - 1, y + 1));
				g.fillRect(right - 5, 0, 5, 3);
				g.fillRect(right - 2, 3, 2, 2);
			}

			g.translate(-x, -y);
		}

		g.translate(x, y);

		g.setColor(SkyTheme.COLOR_BORDER);
		g.drawRect(0, 0, right, bottom);

		g.translate(-x, -y);
	}

	/**
	 * Paint a tab when its positioned on the right of the pane.
	 *
	 * @param tabIndex an <code>int</code> value
	 * @param g a <code>Graphics</code> value
	 * @param x an <code>int</code> value
	 * @param y an <code>int</code> value
	 * @param w an <code>int</code> value
	 * @param h an <code>int</code> value
	 * @param btm an <code>int</code> value
	 * @param rght an <code>int</code> value
	 * @param isSelected a <code>boolean</code> value
	 */
	protected void paintRightTabBorder(int tabIndex, Graphics g, int x, int y, int w, int h, int btm, int rght, boolean isSelected)
	{
		int currentRun = getRunForTab(tabPane.getTabCount(), tabIndex);
		boolean leftToRight = tabPane.getComponentOrientation().isLeftToRight();
		int bottom = h - 1;
		int right = w - 1;

		if (shouldFillGap(currentRun, tabIndex, x, y))
		{
			g.translate(x, y);

			if (leftToRight)
			{
				g.setColor(getColorForGap(currentRun, x, y + 1));
				g.fillRect(1, 0, 5, 3);
				g.fillRect(1, 3, 2, 2);
			}
			else
			{
				g.setColor(getColorForGap(currentRun, x + w - 1, y + 1));
				g.fillRect(right - 5, 0, 5, 3);
				g.fillRect(right - 2, 3, 2, 2);
			}

			g.translate(-x, -y);
		}

		g.translate(x, y);

		g.setColor(SkyTheme.COLOR_BORDER);
		g.drawRect(0, 0, right, bottom);

		g.translate(-x, -y);
	}

	/**
	 * Paint the focus - if necessary the normal focusindicator.
	 *
	 * @param g a <code>Graphics</code> value
	 * @param tabPlacement an <code>int</code> value
	 * @param rects a <code>Rectangle[]</code> value
	 * @param tabIndex an <code>int</code> value
	 * @param iconRect a <code>Rectangle</code> value
	 * @param textRect a <code>Rectangle</code> value
	 * @param isSelected a <code>boolean</code> value
	 */
	protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle [] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected)
	{
		// tabPane is the current TabbedPane (from the superclass)
		if (tabPane.hasFocus() && isSelected)
		{
			// check if extendend Graphic is available
			if (!(g instanceof Graphics2D))
				return;

			String title = tabPane.getTitleAt(tabIndex);
			if (title == null || title.trim().equals(""))
				return;

			Graphics2D g2 = (Graphics2D) g;

			// backup the set linestyle
			Stroke backup = g2.getStroke();

			// set a thick linestyle and paint the focusline
			g2.setStroke(LINESTYLE);
			g2.setColor(UIManager.getColor("TabbedPane.focus"));
			g2.drawLine(textRect.x, textRect.y + textRect.height, textRect.x + textRect.width, textRect.y + textRect.height);
			g2.setStroke(backup);
		}
	}

	/**
	 * Paint the background of a tab.
	 *
	 * @param g a <code>Graphics</code> value
	 * @param tabPlacement an <code>int</code> value
	 * @param tabIndex an <code>int</code> value
	 * @param x an <code>int</code> value
	 * @param y an <code>int</code> value
	 * @param w an <code>int</code> value
	 * @param h an <code>int</code> value
	 * @param isSelected a <code>boolean</code> value
	 */
	protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected)
	{
		if (!isSelected)
			g.setColor(UIManager.getColor("TabbedPane.background"));
		else
			g.setColor(UIManager.getColor("TabbedPane.selected"));

		g.fillRect(x, y, w, h);
	}
}
