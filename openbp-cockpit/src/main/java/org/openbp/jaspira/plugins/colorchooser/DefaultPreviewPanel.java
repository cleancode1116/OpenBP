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
package org.openbp.jaspira.plugins.colorchooser;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.datatransfer.Transferable;

import javax.swing.JPanel;

import org.openbp.common.icon.MultiIcon;
import org.openbp.jaspira.gui.interaction.BasicTransferable;
import org.openbp.jaspira.gui.interaction.DragInitiator;
import org.openbp.jaspira.gui.interaction.DragOrigin;

/**
 * Default Preview Panel used in the RGBColorChooser.
 */
public class DefaultPreviewPanel extends JPanel
	implements DragOrigin
{
	/** Holds the icon used while dragging. */
	private MultiIcon dragIcon;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Creates a new ToolBoxItem with a title, a icon and a Transferable that is send if a Drag is started.
	 */
	public DefaultPreviewPanel()
	{
		DragInitiator.makeDraggable(this, this);
		dragIcon = ColorChooserPlugin.createColorDragIcon(getForeground());
	}

	/**
	 * Indicates if a drag event can occur.
	 *
	 * @return A value of true means drag is allowed, false means not allowed
	 */
	public boolean canDrag()
	{
		return true;
	}

	public void dropAccepted(final Transferable t)
	{
	}

	public void dropCanceled(final Transferable t)
	{
	}

	public void dropPerformed(final Transferable t)
	{
	}

	/**
	 * Get the transferable object at the given point.
	 *
	 * @param p the point reference
	 *
	 * @return The transferable at the given point
	 */
	public Transferable getTranferableAt(final Point p)
	{
		return getTransferable();
	}

	/**
	 * Get the icon representation of the drag.
	 *
	 * @return The image to use as an icon while dragging
	 */
	public MultiIcon getDragImage()
	{
		return dragIcon;
	}

	/**
	 * Returns the Transferable object of this Item.
	 *
	 * @return The Foreground color as a transferable object
	 */
	public Transferable getTransferable()
	{
		return new BasicTransferable(getForeground());
	}

	/**
	 * @see javax.swing.JComponent#setForeground(Color)
	 */
	public void setForeground(final Color color)
	{
		super.setForeground(color);
		dragIcon = ColorChooserPlugin.createColorDragIcon(color);
	}

	/**
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	public void paint(final Graphics g)
	{
		super.paint(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(getForeground());

		// The shadow border will leave 2 pixels space between the border and the content;
		// Remove them in order to fill the content entirely
		Insets insets = getInsets();
		g.fillRect(insets.left - 2, insets.top - 2, getWidth() - insets.left - insets.right + 4, getHeight() - insets.top - insets.bottom + 4);
	}
}
