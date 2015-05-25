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
package org.openbp.swing.layout.dock;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.openbp.common.ExceptionUtil;
import org.openbp.swing.SwingUtil;
import org.openbp.swing.components.DragGlassPane;

/**
 * Test class for the dock layout.
 */
public class TestDock1 extends JFrame
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	private DockLayout dockLayout1 = new DockLayout();

	private JLabel labelNorth = new JLabel();

	private JLabel labelCenter = new JLabel();

	private JLabel labelSouth = new JLabel();

	private JLabel labelWest = new JLabel();

	private JLabel labelEast = new JLabel();

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public TestDock1()
	{
		try
		{
			jbInit();
		}
		catch (Exception e)
		{
			ExceptionUtil.printTrace(e);
		}
	}

	private void jbInit()
		throws Exception
	{
		dockLayout1.setVgap(2);
		dockLayout1.setHgap(2);

		labelNorth.setBorder(BorderFactory.createEtchedBorder());
		labelNorth.setHorizontalAlignment(SwingConstants.CENTER);
		labelNorth.setText("labelNorth");
		labelNorth.addMouseListener(new java.awt.event.MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				labelNorth_mousePressed(e);
			}

			public void mouseReleased(MouseEvent e)
			{
				labelNorth_mouseReleased(e);
			}
		});
		labelNorth.addMouseMotionListener(new java.awt.event.MouseMotionAdapter()
		{
			public void mouseMoved(MouseEvent e)
			{
				labelNorth_mouseMoved(e);
			}

			public void mouseDragged(MouseEvent e)
			{
				labelNorth_mouseDragged(e);
			}
		});
		this.getContentPane().setLayout(dockLayout1);

		labelCenter.setAlignmentX((float) 0.5);
		labelCenter.setBorder(BorderFactory.createEtchedBorder());
		labelCenter.setMaximumSize(new Dimension(300, 200));
		labelCenter.setPreferredSize(new Dimension(300, 200));
		labelCenter.setHorizontalAlignment(SwingConstants.CENTER);
		labelCenter.setText("labelCenter");

		labelSouth.setBorder(BorderFactory.createEtchedBorder());
		labelSouth.setHorizontalAlignment(SwingConstants.CENTER);
		labelSouth.setText("labelSouth");

		labelSouth.addMouseListener(new java.awt.event.MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				labelSouth_mouseClicked(e);
			}
		});
		this.addWindowListener(new java.awt.event.WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				this_windowClosing(e);
			}
		});

		labelWest.setBorder(BorderFactory.createEtchedBorder());
		labelWest.setHorizontalAlignment(SwingConstants.CENTER);
		labelWest.setText("labelWest");
		labelEast.setBorder(BorderFactory.createEtchedBorder());
		labelEast.setHorizontalAlignment(SwingConstants.CENTER);
		labelEast.setText("labelEast");

		this.getContentPane().add(labelCenter, DockLayout.STR_CENTER);
		this.getContentPane().add(labelSouth, DockLayout.STR_SOUTH);
		this.getContentPane().add(labelNorth, DockLayout.STR_NORTH);
		this.getContentPane().add(labelWest, DockLayout.STR_WEST);
		this.getContentPane().add(labelEast, DockLayout.STR_EAST);
	}

	//////////////////////////////////////////////////
	// @@ Event handlers
	//////////////////////////////////////////////////

	int i = 0;

	void labelNorth_mouseMoved(MouseEvent e)
	{
		diag("moved", e);
	}

	void this_windowClosing(WindowEvent e)
	{
		System.exit(0);
	}

	void labelNorth_mouseDragged(MouseEvent e)
	{
		diag("dragged", e);
		drawTargetRect(e.getComponent(), e.getX(), e.getY());
	}

	void labelNorth_mousePressed(MouseEvent e)
	{
		diag("pressed", e);
	}

	void labelNorth_mouseReleased(MouseEvent e)
	{
		diag("released", e);
		clearTargetRect();
	}

	void labelSouth_mouseClicked(MouseEvent e)
	{
		System.exit(0);
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	private void diag(String msg, MouseEvent e)
	{
		++i;

		int button = e.getButton();

		String text = msg + i + ": ";
		if (button == MouseEvent.BUTTON1)
			text += "BTN1 ";
		if (button == MouseEvent.BUTTON2)
			text += "BTN2 ";
		if (button == MouseEvent.BUTTON3)
			text += "BTN3 ";
		if (SwingUtilities.isLeftMouseButton(e))
			text += "LEFT ";
		if (SwingUtilities.isMiddleMouseButton(e))
			text += "MIDDLE ";
		if (SwingUtilities.isRightMouseButton(e))
			text += "RIGHT ";
		text += " x=" + e.getX() + " y=" + e.getY();
		labelNorth.setText(text);
	}

	private void drawTargetRect(Component subject, int x, int y)
	{
		// Get the instance of the global drag glass pane used to draw the drag shadow
		DragGlassPane gp = DragGlassPane.getInstance();

		// Activate the drag shadow glass pane for the root pane container of this component
		gp.activate(this);

		// Convert the point to glass pane coordinates
		Point offset = SwingUtilities.convertPoint(subject, x, y, gp);

		// Draw the target rectangle
		Rectangle targetRect = new Rectangle(offset.x, offset.y, subject.getWidth(), subject.getHeight());
		gp.drawRectangle(targetRect, true);
	}

	private void clearTargetRect()
	{
		// Deactivate the drag shadow glass pane again
		DragGlassPane.getInstance().deactivate();
	}

	//////////////////////////////////////////////////
	// @@ Main method
	//////////////////////////////////////////////////

	/**
	 * Main method for test.
	 *
	 * @param args Argument vector
	 */
	public static void main(String [] args)
	{
		SwingUtil.startApplication(new TestDock1(), true);
	}
}
