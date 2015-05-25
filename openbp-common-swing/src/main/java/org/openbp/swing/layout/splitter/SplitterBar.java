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
package org.openbp.swing.layout.splitter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.openbp.swing.components.DragGlassPane;

/**
 * A class that implements a splitter bar.
 * The component is intended to be used in a {@link SplitterLayout} only and
 * implements one of the resize handles of the splitter layout.
 *
 * By default, the splitter bar will display a shadow image of itself when
 * dragging the splitter. The actual resize operation will take place after
 * releasing the mouse.<br>
 * If the {@link #setLiveLayout} property has been set, the layout will be updated
 * immediately when moving the mouse.
 *
 * Because it is dervied from Panel, a SplitterBar can do anything a normal
 * panel can do. This means that you can embed components into the splitter bar.
 * However, if you add any components to this panel, the resize handle will not
 * be accessible.
 * In this case like this, you also need to add a {@link SplitterSpace} component
 * to the splitter bar. The SplitterSpace component will be recognized by the
 * SplitterBar as a place to use as resize handle.
 *
 * @seec SplitterLayout
 * @seec SplitterSpace
 */
public class SplitterBar extends JPanel
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	private static final Cursor VERT_CURSOR = new Cursor(Cursor.N_RESIZE_CURSOR);

	private static final Cursor HORIZ_CURSOR = new Cursor(Cursor.E_RESIZE_CURSOR);

	private static final Cursor DEF_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Orientation property */
	private int orientation = SplitterLayout.VERTICAL;

	/** Change layout while dragging splittr bar */
	private boolean liveLayout = false;

	/** Status: Mouse is inside the splitter bar */
	private boolean mouseInside = false;

	/** Starting point of drag operation in parent coordinates */
	private static Point dragStartPos;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public SplitterBar()
	{
		setBorder(new EmptyBorder(0, 0, 0, 0));

		//		setBackground (Color.lightGray);

		addMouseMotionListener(new MouseMotionAdapter()
		{
			public void mouseDragged(MouseEvent e)
			{
				mouseDrag(e);
			}
		});

		addMouseListener(new MouseAdapter()
		{
			public void mouseEntered(MouseEvent e)
			{
				mouseEnter(e);
			}

			public void mouseExited(MouseEvent e)
			{
				mouseExit(e);
			}

			public void mouseReleased(MouseEvent e)
			{
				mouseRelease(e);
			}
		});
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the orientation of the layout.
	 * @return The orientation ({@link SplitterLayout#VERTICAL} or {@link SplitterLayout#HORIZONTAL})
	 */
	public int getOrientation()
	{
		/* Returns the orientation property value. */
		return orientation;
	}

	/**
	 * Sets the orientation of the layout.
	 * @param orientation The new orientation
	 * ({@link SplitterLayout#VERTICAL} or {@link SplitterLayout#HORIZONTAL})
	 */
	public void setOrientation(int orientation)
	{
		this.orientation = orientation;
	}

	/**
	 * Gets the layout update mode.
	 * @return
	 *		true	Update the layout while dragging the splitter.<br>
	 *		false	Update the layout after dragging the splitter.
	 */
	public boolean isLiveLayout()
	{
		return liveLayout;
	}

	/**
	 * Sets the layout update mode.
	 * @param liveLayout
	 *		true	Update the layout while dragging the splitter.<br>
	 *		false	Update the layout after dragging the splitter.
	 */
	public void setLiveLayout(boolean liveLayout)
	{
		this.liveLayout = liveLayout;
	}

	//////////////////////////////////////////////////
	// @@ Mouse event handlers
	//////////////////////////////////////////////////

	/**
	 * The mouse is being dragged.
	 * @nowarn
	 */
	void mouseDrag(MouseEvent e)
	{
		SplitterLayout layout = getSplitterLayout();
		if (layout == null)
			return;

		// Mouse event is local to this, convert to parent coordinates
		int localX = e.getX();
		int localY = e.getY();
		int x = localX + getX();
		int y = localY + getY();

		// Compute the delta to the current position
		Point delta;

		if (dragStartPos == null)
		{
			// Save the start point
			dragStartPos = new Point(x, y);
			delta = new Point(0, 0);
			repaint();

			if (!liveLayout)
			{
				// Activate the drag shadow glass pane for the root pane container
				// of this component if live layout is not active.
				DragGlassPane.getInstance().activate(this);
			}
		}
		else
		{
			// Compute the delta to the current position
			delta = new Point(x - dragStartPos.x, y - dragStartPos.y);

			// Make sure that the control is not moved when the cursor is not in
			// the correct position
			boolean handleMovement = false;
			if (orientation == SplitterLayout.VERTICAL)
			{
				if (delta.y > 0)
				{
					// Moving down, move only when cursor in/below control
					if (localY >= 0)
						handleMovement = true;
				}
				else
				{
					// Moving up, move only when cursor in/above control
					if (localY <= getHeight())
						handleMovement = true;
				}

				delta.x = 0;
			}
			else
			{
				if (delta.x > 0)
				{
					// Moving right, move only when cursor in/to the right of control
					if (localX >= 0)
						handleMovement = true;
				}
				else
				{
					// Moving left, move only when cursor in/to the left of control
					if (localX <= getWidth())
						handleMovement = true;
				}

				delta.y = 0;
			}

			if (handleMovement)
			{
				// Defer to layout manager; will update layout if live layout is active.
				// Will also modify delta if component minimum sizes are reached.
				layout.processSplitterDrag(this, delta, liveLayout);
			}

			if (liveLayout || !handleMovement)
			{
				// We made the change or ignore the movement, so reset start position
				dragStartPos.x = x;
				dragStartPos.y = y;
			}
		}

		if (!liveLayout)
		{
			// Draw the shadow

			// Get the instance of the global drag glass pane used to draw the drag shadow
			DragGlassPane gp = DragGlassPane.getInstance();

			// Convert the point to glass pane coordinates
			Point barLoc = SwingUtilities.convertPoint(this, delta, gp);

			// Draw the target rectangle
			Rectangle targetRect = new Rectangle(barLoc.x, barLoc.y, getWidth(), getHeight());
			gp.drawRectangle(targetRect, true);
		}
	}

	/**
	 * A mouse button has been released.
	 * @nowarn
	 */
	void mouseRelease(MouseEvent e)
	{
		SplitterLayout layout = getSplitterLayout();
		if (layout == null)
			return;

		if (dragStartPos == null)
			return;

		if (!liveLayout)
		{
			// Mouse event is local to this, convert to parent coordinates
			int x = e.getX() + getX();
			int y = e.getY() + getY();

			// Activate the drag shadow glass pane for the root pane container of this component
			DragGlassPane.getInstance().deactivate();

			// Compute the delta to the current position
			Point delta = new Point(x - dragStartPos.x, y - dragStartPos.y);
			if (orientation == SplitterLayout.VERTICAL)
				delta.x = 0;
			else
				delta.y = 0;

			// Defer to layout manager and update layout.
			layout.processSplitterDrag(this, delta, true);
		}

		dragStartPos = null;
		updateStatus();
	}

	/**
	 * The mouse enters the comonents area.
	 * @nowarn
	 */
	void mouseEnter(MouseEvent e)
	{
		mouseInside = true;
		updateStatus();
	}

	/**
	 * The mouse leaves the comonents area.
	 * @nowarn
	 */
	void mouseExit(MouseEvent e)
	{
		mouseInside = false;
		updateStatus();
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Repaint the component and update the cursor shape according to current mouse pos
	 * and component state.
	 */
	protected void updateStatus()
	{
		repaint();

		Cursor cursor = DEF_CURSOR;
		if (mouseInside || dragStartPos != null)
		{
			cursor = (orientation == SplitterLayout.VERTICAL) ? VERT_CURSOR : HORIZ_CURSOR;
		}
		setCursor(cursor);
	}

	/**
	 * Gets the splitter layout manager of the container or this splitter.
	 *
	 * @return The splitter layout or null if no splitter layout manager has been assigned
	 */
	private SplitterLayout getSplitterLayout()
	{
		LayoutManager layout = getParent().getLayout();
		if (layout instanceof SplitterLayout)
			return (SplitterLayout) layout;
		return null;
	}

	//////////////////////////////////////////////////
	// @@ Component painting
	//////////////////////////////////////////////////

	/**
	 * Called by AWT to update the image produced by the splitter bar.
	 *
	 * @param g Component graphics
	 */
	public void update(Graphics g)
	{
		paint(g);
	}

	// TODO Cleanup 6: Use L&F manager here
	public static final Color FOCUSCOLOR = new Color(255, 212, 142, 254);

	public static final Color ENABLED_BORDERCOLOR = new Color(149, 176, 203, 254);

	/**
	 * Paints the component.
	 * If nothing was added to the SplitterBar, this image will only be a thin,
	 * 3D raised line that will act like a handle for moving the SplitterBar.
	 * If other components were added to the SplitterBar, the thin 3D raised
	 * line will only appear where {@link SplitterSpace} components were added.
	 *
	 * @param g Component graphics
	 */
	public void paint(Graphics g)
	{
		super.paint(g);

		// Color focusColor = UIManager.getColor ("Button.focus");
		if (mouseInside && dragStartPos == null)
			g.setColor(FOCUSCOLOR);
		else
			g.setColor(ENABLED_BORDERCOLOR);

		Component [] c = getComponents();
		if (c != null && c.length > 0)
		{
			// We have components within the splitter bar
			for (int i = 0; i < c.length; i++)
			{
				if (c [i] instanceof SplitterSpace)
				{
					// Only draw boxes where SplitterSpace components appear
					Rectangle r = c [i].getBounds();
					draw3DRect(g, r);
				}
			}
		}
		else
		{
			Rectangle r = getBounds();
			r.x = r.y = 0;
			draw3DRect(g, r);
		}
	}

	/**
	 * Draws a 3D rectangle indicating the bar itself.
	 *
	 * @param g Component graphics
	 * @param r Rectangle denoting the size of the bar to draw
	 */
	private void draw3DRect(Graphics g, Rectangle r)
	{
		if (orientation == SplitterLayout.VERTICAL)
			g.fill3DRect(2, r.height / 2 - 1, r.width - 5, 3, true);
		else
			g.fill3DRect(r.width / 2 - 1, 2, 3, r.height - 5, true);
	}

	//////////////////////////////////////////////////
	// @@ Miscelleanous
	//////////////////////////////////////////////////

	/**
	 * Returns a string representation of the state of this object.
	 * @nowarn
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer(getClass().getName());
		sb.append("[");
		sb.append("orientation=");
		sb.append(orientation == SplitterLayout.VERTICAL ? "vertical" : "horizontal");
		sb.append("liveLayout=");
		sb.append(liveLayout ? "true" : "false");
		sb.append("]");
		return sb.toString();
	}
}
