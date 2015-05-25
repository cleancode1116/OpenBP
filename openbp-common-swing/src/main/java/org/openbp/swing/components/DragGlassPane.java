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
package org.openbp.swing.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.RootPaneContainer;

import org.openbp.swing.SwingUtil;

/**
 * This glass pane component can draw a shadow-like rectangle over its component.
 * The rectangle can be used for drag operations (i. e. for splitter bars, docking windows etc.).
 * Though the constructor of this class is public, we recommend to use this class as
 * singleton by calling the {@link #getInstance} method of this class because only one drag
 * action will be active at a time.
 */
public final class DragGlassPane extends JComponent
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** The one and only instance of this class */
	private static DragGlassPane singletonInstance = null;

	/** Property: Border width of the rectangle */
	private int borderWidth = 3;

	/** Target rectangle */
	private Rectangle targetRect;

	/** Fill rectangle or draw rectangle shape? */
	private boolean fill = false;

	/** Current container the glass pane was assigned to */
	private RootPaneContainer container;

	/** Original glass pane of the container the glass pane was assigned to */
	private Component originalGlassPane;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of the add-in manager.
	 * @nowarn
	 */
	public static synchronized DragGlassPane getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new DragGlassPane();
		return singletonInstance;
	}

	/**
	 * Default constructor.
	 */
	private DragGlassPane()
	{
	}

	//////////////////////////////////////////////////
	// @@ Public methods
	//////////////////////////////////////////////////

	/**
	 * Activates this glass pane for the given container.
	 * Note that the glass pane can be active for only one container at a time.
	 * The {@link #deactivate} method will reset the container's glass pane to the initial value.
	 * @param c Component that shall be gouverned by the glass pane. The glass pane will be
	 * assigned to the root pane that is an ancestor of this component.
	 * @throws IllegalArgumentException if the component is not contained in a RootPaneContainer
	 * or is not a RootPaneContainer pane container itself
	 */
	public void activate(Component c)
	{
		deactivate();

		// Find the root pane container of this component
		for (; c != null; c = c.getParent())
		{
			if (c instanceof RootPaneContainer)
			{
				container = (RootPaneContainer) c;
				break;
			}
		}

		if (container == null)
		{
			throw new IllegalArgumentException("The active component is not contained in a root pane container.");
		}

		originalGlassPane = container.getGlassPane();
		container.setGlassPane(this);
		setSize(container.getContentPane().getSize());
	}

	/**
	 * Deactivates this glass pane for the current container.
	 * Note that the glass pane can be active for only one container at a time.
	 * The {@link #activate} method must have been called before calling this method.
	 */
	public void deactivate()
	{
		if (container != null)
		{
			container.setGlassPane(originalGlassPane);

			// Make all associated objects available for GC again
			container = null;
			originalGlassPane = null;
			targetRect = null;
		}
	}

	/**
	 * Draws the target rectangle.
	 *
	 * @param newTargetRect Target rectangle in the local coordinate system
	 * or null to remove the rectangle.
	 * @param fill
	 *		true	Fill the entire rectangle.<br>
	 *		false	Draw the rectangle border only (see {@link #setBorderWidth}).
	 */
	public void drawRectangle(Rectangle newTargetRect, boolean fill)
	{
		// Invalidate old and new rectangle
		Rectangle paintArea = null;
		if (targetRect != null)
		{
			if (newTargetRect != null)
			{
				paintArea = targetRect.union(newTargetRect);
			}
			else
			{
				paintArea = targetRect;
			}
		}
		else
		{
			paintArea = newTargetRect;
		}

		this.targetRect = newTargetRect;
		this.fill = fill;

		if (paintArea != null)
		{
			// Trigger repainting immediately
			SwingUtil.inflateRectangle(paintArea, 1, 1);
			repaint(paintArea.x, paintArea.y, paintArea.width, paintArea.height);
		}

		setVisible(true);
	}

	/**
	 * Clears the target rectangle.
	 */
	public void clearRectangle()
	{
		if (targetRect != null)
		{
			Rectangle paintArea = new Rectangle(targetRect);
			targetRect = null;

			SwingUtil.inflateRectangle(paintArea, 1, 1);
			repaint(paintArea.x, paintArea.y, paintArea.width, paintArea.height);
		}

		setVisible(false);
	}

	/**
	 * Gets the stroke width of the rectangle.
	 * @nowarn
	 */
	public int getBorderWidth()
	{
		return borderWidth;
	}

	/**
	 * Sets the stroke width of the rectangle.
	 * Default: 3 pixel.
	 * @nowarn
	 */
	public void setBorderWidth(int borderWidth)
	{
		this.borderWidth = borderWidth;
	}

	//////////////////////////////////////////////////
	// @@ Overrides
	//////////////////////////////////////////////////

	/**
	 * Override of the method.
	 *
	 * @param visible
	 *		true	Shows the component.<br>
	 *		false	Hides the component.
	 */
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);

		// Clear last target rectangle
		if (!visible)
			targetRect = null;
	}

	/**
	 * Paints the component.
	 *
	 * @param g Component graphics
	 */
	public void paint(Graphics g)
	{
		if (targetRect != null)
		{
			g.setColor(Color.black);
			g.setXORMode(Color.gray);

			if (fill)
			{
				g.fillRect(targetRect.x, targetRect.y, targetRect.width, targetRect.height);
			}
			else
			{
				Rectangle r = new Rectangle(targetRect);
				for (int i = borderWidth; i > 0; --i)
				{
					g.drawRect(r.x, r.y, r.width, r.height);
					if (r.width <= 2 || r.height <= 2)
						break;
					SwingUtil.inflateRectangle(r, -1, -1);
				}
			}
		}
	}
}
