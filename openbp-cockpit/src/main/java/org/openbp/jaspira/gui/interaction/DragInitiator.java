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
package org.openbp.jaspira.gui.interaction;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.openbp.swing.SwingUtil;

/**
 * The drag initiator supports the Jaspira-internal drag and drop mechanism for components.
 * It will add mouse listeners that handle the dnd operations to a given component.
 * Use the static method {@link #makeDraggable} to make a component a drag initiator.
 *
 * @author Stephan Moritz
 */
public class DragInitiator
	implements MouseListener, MouseMotionListener, KeyListener
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** Drag origin that we are associated with */
	private DragOrigin owner;

	/** Listened component */
	private Component component;

	//////////////////////////////////////////////////
	// @@ Status variables
	//////////////////////////////////////////////////

	/** Coordinates of the last click, used to recognize dragging gestures. */
	private int clickX, clickY;

	/** Helper used to identify drag gestures */
	private boolean dragging;

	/** Drang and drop pane that we are communicating with */
	private DragDropPane dndPane;

	//////////////////////////////////////////////////
	// @@ Static methods
	//////////////////////////////////////////////////

	/**
	 * Makes the given component draggable.
	 * Adds mouse and mouse motion listeners to the component that initiate and
	 * control a DnD operation.<br>
	 * Call this method only once of a particular component.
	 *
	 * @param comp Component to make draggable
	 * @param owner Owner of the dragged object (to be used as origin of the DnD operation)
	 */
	public static void makeDraggable(Component comp, DragOrigin owner)
	{
		new DragInitiator(owner, comp);
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param owner Owner of the dragged object (to be used as origin of the DnD operation)
	 * @param component Component to add the listeners to
	 */
	private DragInitiator(DragOrigin owner, Component component)
	{
		this.owner = owner;
		this.component = component;

		// We add ourselves as listeners
		component.addMouseListener(this);
		component.addMouseMotionListener(this);
		component.addKeyListener(this);
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ MouseListener implementation
	/////////////////////////////////////////////////////////////////////////

	public void mouseClicked(MouseEvent e)
	{
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void mousePressed(MouseEvent e)
	{
		clickX = e.getPoint().x;
		clickY = e.getPoint().y;

		dragging = true;
	}

	public void mouseReleased(MouseEvent e)
	{
		dragging = false;

		if (dndPane != null)
		{
			// Pass the event upward
			Point p = SwingUtil.convertToGlassCoords(e.getPoint(), component);
			dndPane.mouseReleased(p);

			// Reset dnd pane to enable garbage collection
			dndPane = null;
		}
	}

	//////////////////////////////////////////////////
	// @@ KeyListener implementation
	//////////////////////////////////////////////////

	/**
	 * Invoked when a key has been released.
	 * @nowarn
	 */
	public void keyReleased(KeyEvent e)
	{
	}

	/**
	 * Invoked when a key has been pressed.
	 * @nowarn
	 */
	public void keyPressed(KeyEvent e)
	{
		int keyCode = e.getKeyCode();

		if (keyCode == KeyEvent.VK_ESCAPE)
		{
			if (dndPane != null)
			{
				dndPane.cancelDrag();
				e.consume();
			}
		}
	}

	/**
	 * Invoked when a key has been typed.
	 * @nowarn
	 */
	public void keyTyped(KeyEvent e)
	{
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ MouseMotionListener
	/////////////////////////////////////////////////////////////////////////

	public void mouseDragged(MouseEvent e)
	{
		Point p = e.getPoint();
		if (dragging && Math.abs(clickX - p.x) + Math.abs(clickY - p.y) > 4)
		{
			// Start of drag recognized, initiate dragging
			if (owner.canDrag())
			{
				Component glassPane = SwingUtil.getGlassPane(component);

				Transferable t = owner.getTranferableAt(new Point(clickX, clickY));

				if (glassPane instanceof DragDropPane && t != null)
				{
					dndPane = (DragDropPane) glassPane;

					// Pass the transferable to the drag and drop pane
					dndPane.startDrag(t, owner, e);
				}
			}

			dragging = false;
		}

		if (dndPane != null)
		{
			// We are already in the process of DnD, we just hand our dragEvent upward
			p = SwingUtil.convertToGlassCoords(p, component);
			dndPane.mouseDragged(p);
		}
	}

	public void mouseMoved(MouseEvent e)
	{
	}
}
