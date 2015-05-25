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
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * A drop client is an item that may receive drag and drop events.
 * Each client defines drop regions that may accept drops.
 *
 * @author Stephan Moritz
 */
public interface InteractionClient
{
	/**
	 * Returns subordinate clients of this client.
	 *
	 * @return A list of {@link InteractionClient} objects or null if this drop client doesn't have sub drop clients
	 */
	public List getSubClients();

	/**
	 * Returns a list of drop regions of this client that are compatible with
	 * the provided data flavors.
	 *
	 * @param flavors List of data flavors to check
	 * @param data Transferable to be imported
	 * @param mouseEvent Mouse event that initiated the drag action
	 * @return A list of {@link DragAwareRegion} object or null if the drop client
	 * cannot satisfy at least one of the supplied data flavors.<br>
	 * The list will contain only regions that belong directly to this drop client.
	 */
	public List getDropRegions(List flavors, Transferable data, MouseEvent mouseEvent);

	/**
	 * Returns a list of all regions of this client AND possible sub clients.
	 *
	 * @param flavors List of data flavors to check
	 * @param data Transferable to import
	 * @param mouseEvent Mouse event that initiated the drag action
	 * @return A list of {@link DragAwareRegion} object or null if the drop client
	 * or one of its sub clients cannot satisfy at least one of the supplied data flavors
	 */
	public List getAllDropRegions(List flavors, Transferable data, MouseEvent mouseEvent);

	/**
	 * Imports the given transferable into the given region.
	 *
	 * @param regionId Id of the region to import into (see BasicDropRegion.getId)
	 * @param data Transferable to import
	 * @param p Drop point in glass coordinates
	 * @return
	 *		true	The data was successfully imported.<br>
	 *		false	An error occured while importing the data.
	 */
	public boolean importData(Object regionId, Transferable data, Point p);

	/**
	 * Called when a dragging has been started.
	 *
	 * @param transferable Transferable to be dragged
	 */
	public void dragStarted(Transferable transferable);

	/**
	 * called when a dragging has ended.
	 *
	 * @param transferable Transferable that has been dragged
	 */
	public void dragEnded(Transferable transferable);

	/**
	 * Called to signal additional actions,
	 * such as hovering for a certain time over the region.
	 *
	 * @param regionId Id of the region to import into (see BasicDropRegion.getId)
	 * @param p Current mouse position in screen coordinates
	 */
	public void dragActionTriggered(Object regionId, Point p);

	/**
	 * Returns all importers which will be accepted at the given point by this client.
	 *
	 * @param p Current mouse position in screen coordinates
	 * @return A list of {@link Importer} objects or null
	 */
	public List getImportersAt(Point p);

	/**
	 * Returns all importers which will be accepted at the given point by this client
	 * or one of its sub clients.
	 *
	 * @param p Current mouse position in screen coordinates
	 * @return A list of {@link Importer} objects or null
	 */
	public List getAllImportersAt(Point p);
}
