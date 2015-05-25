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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.plaf.metal.MetalTreeUI;
import javax.swing.tree.TreePath;

/**
 * PLAF for a JTree component
 *
 * We override this class in order to customize the mouse handling if the click count of the
 * tree is set to 1.
 *
 * However, this doesn't work correctly yet, so it's currently not used.
 *
 * @author Heiko Erhardt
 */
public class SkyTreeUI extends MetalTreeUI
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Flag if the current path should be expanded on mouse up */
	private TreePath expandPathOnMouseUp;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Creates a new instance of this ui.
	 *
	 * @param c Component to create this ui for
	 * @return The new ui instance
	 */
	public static ComponentUI createUI(JComponent c)
	{
		return new SkyTreeUI();
	}

	/**
	 * Default constructor.
	 */
	public SkyTreeUI()
	{
	}

	//////////////////////////////////////////////////
	// @@ MetalTreeUI overrides
	//////////////////////////////////////////////////

	protected void installListeners()
	{
		super.installListeners();

		tree.addMouseMotionListener(new MouseMotionHandler());
	}

	/**
	 * Creates the listener responsible for updating the selection based on mouse events.
	 * @return The new listener
	 */
	protected MouseListener createMouseListener()
	{
		return new MouseHandler();
	}

	/**
	 * Responsible for updating the selection based on mouse events.
	 */
	public class MouseHandler extends BasicTreeUI.MouseHandler
	{
		/**
		 * Invoked when a mouse button has been released on a component.
		 * @param e Mouse event
		 */
		public void mouseReleased(MouseEvent e)
		{
			if (expandPathOnMouseUp != null)
			{
				tree.expandPath(expandPathOnMouseUp);
				expandPathOnMouseUp = null;
			}
		}
	}

	/**
	 * Responsible for updating the selection based on mouse events.
	 */
	public class MouseMotionHandler extends MouseMotionAdapter
	{
		/**
		 * Invoked when the mouse is being moved while a mouse button is pressed.
		 * @param e Mouse event
		 */
		public void mouseDragged(MouseEvent e)
		{
			// When dragging a tree item, don't expand the tree path
			expandPathOnMouseUp = null;
		}
	}

	/**
	 * Messaged to update the selection based on a MouseEvent over a
	 * particular row. If the event is a toggle selection event, the
	 * row is either selected, or deselected. If the event identifies
	 * a multi selection event, the selection is updated from the
	 * anchor point. Otherwise the row is selected, and if the event
	 * specified a toggle event the row is expanded/collapsed.
	 * @nowarn
	 */
	protected void selectPathForEvent(TreePath path, MouseEvent event)
	{
		// Should this event toggle the selection of this row?

		if (isToggleSelectionEvent(event))
		{
			// Let the super method handle this
		}
		else if (isMultiSelectEvent(event))
		{
			// Let the super method handle this
		}
		else if (SwingUtilities.isLeftMouseButton(event))
		{
			// In single click expansio mode, toggle only if the path is not currently expanded.
			// This avoids that merely selecting a node will collapse the node.
			if (isSingleClickExpansionEvent(event))
			{
				tree.setSelectionPath(path);

				// If the tree is collapsed, expand it.
				// If the tree is expanded, *don't* collapse it.
				if (!tree.isExpanded(path))
				{
					// Expand this tree path on mouse up
					expandPathOnMouseUp = path;
				}

				return;
			}

			// Otherwise, let the super method handle this
		}

		super.selectPathForEvent(path, event);
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Checks if the given mouse event can be seen as single click tree expansion event.
	 *
	 * @param event Mouse event
	 * @return
	 *		true	If the toggle click count of the tree is 1 and we have a single click in fact.
	 *		false	Otherwise
	 */
	private boolean isSingleClickExpansionEvent(MouseEvent event)
	{
		return tree.getToggleClickCount() == 1 && event.getClickCount() == 1;
	}
}
