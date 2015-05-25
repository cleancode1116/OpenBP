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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import org.openbp.common.ExceptionUtil;
import org.openbp.swing.SwingUtil;

/**
 * Test program for LookAndFeel
 *
 * @author Jens Ferchland
 */
public class TestSky
{
	public static void main(String [] args)
	{
		// set SkyLookAndFeel
		try
		{
			UIManager.setLookAndFeel("org.openbp.swing.plaf.sky.SkyLookAndFeel");
		}

		catch (Exception e)
		{
			ExceptionUtil.printTrace(e);
		}

		JFrame frame = new JFrame("SkyTest");
		frame.setSize(640, 480);

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		JButton test1 = new JButton("Test1");
		JButton test2 = new JButton("Test2");
		toolBar.add(test1);
		toolBar.add(test2);

		DrawPane pan = new DrawPane();

		SimpleBorder errorBorder = new SimpleBorder(2, 2, 2, 2);
		errorBorder.setWidth(2);
		errorBorder.setColor(Color.RED);
		pan.setBorder(errorBorder);

		frame.getContentPane().add(toolBar, BorderLayout.NORTH);
		frame.getContentPane().add(pan);

		SwingUtil.show(frame);
	}

	public static class DrawPane extends JPanel
	{
		/* (non-Javadoc)
		 * @see javax.swing.JComponent#paint(java.awt.Graphics)
		 */
		public void paint(Graphics g)
		{
			Graphics2D g2 = (Graphics2D) g;

			super.paint(g);

			g.setColor(Color.black);

			Rectangle rect1 = new Rectangle(0, 0, 200, 100);
			Rectangle rect2 = new Rectangle(10, 5, 200, 300);
			Rectangle rect3 = new Rectangle(100, 50, 200, 100);

			g.drawRect(rect1.x, rect1.y, rect1.width, rect1.height);
			g.drawRect(rect2.x, rect2.y, rect2.width, rect2.height);
			g.drawRect(rect3.x, rect3.y, rect3.width, rect3.height);

			Area area = new Area(rect2);

			area.subtract(new Area(rect1));

			area.intersect(new Area(rect3));

			g.setColor(Color.red);
			g2.fill(area);

			g.setColor(Color.blue);
			g.setClip(area);
			g.fillRect(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
		}
	}
}
