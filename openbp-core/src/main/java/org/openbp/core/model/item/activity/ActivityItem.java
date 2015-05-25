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
package org.openbp.core.model.item.activity;

import java.util.Iterator;
import java.util.List;

import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.process.NodeProvider;

/**
 * OpenBP activity object.
 * An activity is a node of a process.<br>
 * The activity has a number of entries (at least and also usually one) and
 * a number of exits (also at least one).
 *
 * @author Heiko Erhardt
 */
public interface ActivityItem
	extends Item, NodeProvider
{
	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the activity sockets.
	 * @return An iterator of  ActivitySocket objects
	 */
	public Iterator getSockets();

	/**
	 * Gets the number of activity sockets.
	 * @return The number of activity sockets in the collection
	 */
	public int getNumberOfSockets();

	/**
	 * Creates a new socket and assigns a new name to it.
	 * @param name Name of the new parameter or null for the default ("Param")
	 * @return The new socket
	 */
	public ActivitySocket createSocket(String name);

	/**
	 * Adds a socket.
	 * @param socket The socket to add
	 */
	public void addSocket(ActivitySocket socket);

	/**
	 * Removes a socket.
	 * @param socket The socket to remove
	 */
	public void removeSocket(ActivitySocket socket);

	/**
	 * Gets a socket by its name.
	 *
	 * @param name Name of the socket
	 * @return The socket or null if no such socket exists
	 */
	public ActivitySocket getSocketByName(String name);

	/**
	 * Gets the default exit socket.
	 *
	 * @return The default exit socket or null
	 */
	public ActivitySocket getDefaultExitSocket();

	/**
	 * Gets the default entry socket.
	 *
	 * @return The default entry socket or null
	 */
	public ActivitySocket getDefaultEntrySocket();

	/**
	 * Returns the default socket of a node.
	 *
	 * @param isEntry
	 *		true	We are looking for the default entry socket.<br>
	 *		false	We are looking for the default exit socket.
	 * @return The socket or null if there is no such default socket
	 */
	public ActivitySocket getDefaultSocket(boolean isEntry);

	/**
	 * Clears the activity sockets.
	 */
	public void clearSockets();

	/**
	 * Gets the activity sockets.
	 * @return A list of ActivitySocket objects
	 */
	public List getSocketList();

	/**
	 * Sets the activity sockets.
	 * @param socketList A list of ActivitySocket objects
	 */
	public void setSocketList(List socketList);

	/**
	 * Gets the geometry information (required by the Modeler).
	 * @nowarn
	 */
	public String getGeometry();

	/**
	 * Sets the geometry information (required by the Modeler).
	 * @nowarn
	 */
	public void setGeometry(String geometry);
}
