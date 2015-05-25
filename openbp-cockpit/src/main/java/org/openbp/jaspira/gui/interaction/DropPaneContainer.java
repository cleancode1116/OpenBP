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

import javax.swing.RootPaneContainer;

/**
 * Interface that defines a top level container that supports the Jaspira DnD mechanism.
 * A Frame capable of using the Jaspira DnD mechanism needs to implement this interface.
 * Note that standard Swing-toplevel-components (JWindows, JFrame, JDialog, JApplet
 * and JInternalFrame) already implement the methods of RootPaneContainer.
 *
 * The interface provides methods for registering {@link InteractionClient} s with the Container
 * and ultimatively with the {@link DragDropPane} associated with it.
 *
 * Note that it is NOT necessary to register Jaspira-Plugins, these are inherently
 * added and removed through the Jaspira page mechanism.
 *
 * @author Stephan Moritz
 */
public interface DropPaneContainer
	extends RootPaneContainer
{
	/**
	 * Adds a drop client to the list of the container's drop clients.
	 *
	 * @param client Client to add
	 */
	public void addDropClient(InteractionClient client);

	/**
	 * Removes a drop client to the list of the container's drop clients.
	 *
	 * @param client Client to remove
	 */
	public void removeDropClient(InteractionClient client);

	/**
	 * Returns the drad and drop page of this container.
	 * This is simply a casted wrapper for getGlassPane ().
	 * @nowarn
	 */
	public DragDropPane getDragDropPane();

	/**
	 * Sets up the given drag and drop pane as glass pane of the container.
	 *
	 * @param pane The new glass pane
	 */
	public void setDragDropPane(DragDropPane pane);
}
