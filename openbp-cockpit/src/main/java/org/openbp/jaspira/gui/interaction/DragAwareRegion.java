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

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;

/**
 * A DropRegion is a single acceptanceRegion for for a DropReceiver. It consists of a
 * region (defined by a Graphics2D region) and a list of acceptable DataFlavors.
 *
 * @author Stephan Moritz
 */
public interface DragAwareRegion
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Constants
	/////////////////////////////////////////////////////////////////////////

	/** No source actions are supported. Only here for completeness. */
	public static final int NONE = DnDConstants.ACTION_NONE;

	/** This Regions supports copy actions only. */
	public static final int COPY = DnDConstants.ACTION_COPY;

	/** This Regions supports move actions only. */
	public static final int MOVE = DnDConstants.ACTION_MOVE;

	/** This Regions supports copy and move actions. */
	public static final int COPY_OR_MOVE = DnDConstants.ACTION_COPY_OR_MOVE;

	/////////////////////////////////////////////////////////////////////////
	// @@ Visual Data
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the overlay that is to be shown when the cursor is over the region.
	 * @return The overlay or null if no overlay should be shown
	 */
	public Shape getOverlay();

	/**
	 * Returns the cursor prototype that should be used for constructing a cursor
	 * if a drag over occurs.
	 * @return The drag cursor prototype or null
	 */
	public CursorPrototype getCursor();

	/**
	 * Draws the graphical representation of this dropRegion.
	 * @param g Graphics context
	 */
	public void draw(Graphics2D g);

	/**
	 * Checks if the given point is inside the region.
	 * @param x Position in screen coordinates
	 * @param y Position in screen coordinates
	 * @nowarn
	 */
	public boolean reactsOn(int x, int y);

	/**
	 * Returns the bounding box of the region.
	 * @return The bounding box
	 */
	public Rectangle getBounds();

	/**
	 * Returns a Tooltip for this region.
	 * @return The tool tip or null if no tool tip should be displayed
	 */
	public String getToolTipText();

	/////////////////////////////////////////////////////////////////////////
	// @@ Drag/Drop
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Delivers a transferable to the drop receiver of this region.
	 *
	 * @param data The transferable to import
	 * @param p Import position in glass coordinates
	 * @return
	 *		true	If the import was successful<br>
	 *		false	Otherwise
	 */
	public boolean importData(Transferable data, Point p);

	/**
	 * Used to distinguish between active components (triggers) and real data accpetors.
	 * @nowarn
	 */
	public boolean canImport();

	/////////////////////////////////////////////////////////////////////////
	// @@ Mouse enter/exit events
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Called when the mouse enters the region.
	 *
	 * @return
	 *		true	If this event should be passed to other eglible targets<br>
	 *		false	To purge the event
	 */
	public boolean dragEnter();

	/**
	 * Called when the mouse leaves the region.
	 *
	 * @return
	 *		true	If this event should be passed to other eglible targets<br>
	 *		false	To purge the event
	 */
	public boolean dragExit();
}
