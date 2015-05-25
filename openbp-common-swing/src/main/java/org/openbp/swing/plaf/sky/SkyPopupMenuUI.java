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

import java.awt.AWTException;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPopupMenuUI;

/**
 * This is the sky look and feel of e JPopipMenu
 *
 * @author Jens Ferchland
 */
public class SkyPopupMenuUI extends BasicPopupMenuUI
{
	private static Image oldbg;

	private static Rectangle oldrect;

	private Image background;

	private MediaTracker tracker;

	/**
	 * Create a UI.
	 *
	 * @param c a <code>JComponent</code> value
	 * @return a <code>ComponentUI</code> value
	 */
	public static ComponentUI createUI(JComponent c)
	{
		return new SkyPopupMenuUI();
	}

	/**
	 * Updates the background for transparency effect.
	 */
	protected void updateBackground()
	{
		try
		{
			Robot rob = new Robot();
			Rectangle r = popupMenu.getBounds();
			Point p = popupMenu.getLocationOnScreen();
			r.x = p.x;
			r.y = p.y;

			// Enlarge capturing area - for safety
			r.width += SkyUtil.DEFAULTSHADOWDEPTH + 4;
			r.height += SkyUtil.DEFAULTSHADOWDEPTH + 4;

			if (r.width != 0 && r.height != 0 && rob != null)
			{
				background = rob.createScreenCapture(r);
			}

			tracker = new MediaTracker(popupMenu);
			tracker.addImage(background, 1);
			tracker.waitForAll();

			// bildausschnitt new berechnen fals überlappungen exestieren

			if (oldrect != null)
			{
				Graphics g = background.getGraphics();
				g.drawImage(oldbg, oldrect.x - r.x, oldrect.y - r.y, null);
			}

			oldrect = r;
			oldbg = background;
		}
		catch (InterruptedException ie)
		{
		}
		catch (AWTException e)
		{
			// Is unhandled - if no robot is created the
			// shadow feature works without background.
		}
	}

	/**
	 * Installs the UI on the Component.
	 * @nowarn
	 */
	public void installUI(JComponent c)
	{
		super.installUI(c);
		c.setOpaque(false);
	}

	/**
	 * Paint the popupmenu.
	 * See javax.swing.plaf.ComponentUI#paint(Graphics, JComponent)
	 * @nowarn
	 */
	public void paint(Graphics g, JComponent comp)
	{
		updateBackground();

		if (background != null)
		{
			g.drawImage(background, 0, 0, null);
		}

		g.setColor(comp.getBackground());

		Insets ins = comp.getInsets();
		g.fillRect(ins.left, ins.top, comp.getWidth() - ins.left - ins.right, comp.getHeight() - ins.top - ins.bottom);

		super.paint(g, comp);

		SkyUtil.paintRectShadow(g, 0, 0, comp.getWidth(), comp.getHeight());
	}
}
