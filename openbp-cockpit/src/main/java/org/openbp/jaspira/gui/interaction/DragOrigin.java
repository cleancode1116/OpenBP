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

import java.awt.Point;
import java.awt.datatransfer.Transferable;

import org.openbp.common.icon.MultiIcon;

/**
 * A drag origin is an object that may provide an object to drag.
 *
 * @author Stephan Moritz
 */
public interface DragOrigin
{
	/**
	 * Returns the Transferable for a drag Operation starting at
	 * .
	 * @param p The location at which the drag started in component local coordinates
	 * @return The Transferable to drag
	 */
	public Transferable getTranferableAt(Point p);

	/**
	 * Called when the drag has been accepted by a InteractionClient, but before it is actually imported.
	 *
	 * @param t Dragged transferable
	 */
	public void dropAccepted(Transferable t);

	/**
	 * Called when the drop has been performed and the object has been inserted.
	 *
	 * @param t Dragged transferable
	 */
	public void dropPerformed(Transferable t);

	/**
	 * Called when the drag action has been aborted, either by dropping upon a non-
	 * qualifying target or because the target reported an unsuccessful drop.
	 *
	 * @param t Dragged transferable
	 */
	public void dropCanceled(Transferable t);

	/**
	 * If this returns false, no dragging is permitted.
	 * @nowarn
	 */
	public boolean canDrag();

	/**
	 * Gets the image for the construction of the drag cursor.
	 *
	 * @return The image or null if no special drag image is to be constructed
	 */
	public MultiIcon getDragImage();
}
