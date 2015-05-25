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
package org.openbp.core.model.item.process;

import java.util.Iterator;
import java.util.List;

import org.openbp.core.model.item.ConfigurationBean;

/**
 * A multi socket node is a node that can have a variable number of node sockets.
 * Examples for msns are activity nodes, sub process nodes etc.
 *
 * @author Heiko Erhardt
 */
public interface MultiSocketNode
	extends Node, ItemProvider
{
	/**
	 * Gets the node setting bean.
	 * @nowarn
	 */
	public ConfigurationBean getConfigurationBean();

	/**
	 * Sets the configuration bean.
	 * @nowarn
	 */
	public void setConfigurationBean(ConfigurationBean configurationBean);

	//////////////////////////////////////////////////
	// @@ Property access: Sockets
	//////////////////////////////////////////////////

	/**
	 * Gets the node sockets.
	 * @return An iterator of {@link NodeSocket} objects
	 */
	public Iterator getSockets();

	/**
	 * Gets the node sockets in the order determined by the {@link NodeSocket#setSequenceId(String)} property.
	 * @return An iterator of {@link NodeSocket} objects
	 */
	public Iterator getOrderedSockets();

	/**
	 * Gets the node sockets of a particular type.
	 * @param isEntry
	 *		true	Collect entry sockets<br>
	 *		false	Collect exit sockets
	 * @return An iterator of {@link NodeSocket} objects
	 */
	public Iterator getSockets(boolean isEntry);

	/**
	 * Creates a new socket and assigns a new name to it.
	 * @param name Name of the new parameter or null for the default ("Param")
	 * @return The new socket
	 */
	public NodeSocket createSocket(String name);

	/**
	 * Adds a socket.
	 * @param socket The socket to add
	 */
	public void addSocket(NodeSocket socket);

	/**
	 * Removes a socket.
	 * @param socket The socket to remove
	 */
	public void removeSocket(NodeSocket socket);

	/**
	 * Gets a socket by its name.
	 *
	 * @param name Name of the socket
	 * @return The socket or null if no such socket exists
	 */
	public NodeSocket getSocketByName(String name);

	/**
	 * Clears the node sockets.
	 */
	public void clearSockets();

	/**
	 * Gets the node sockets.
	 * @return A list of {@link NodeSocket} objects
	 */
	public List getSocketList();

	/**
	 * Returns a socket that we can connect to.
	 * The type of socket is specified by the 'isEntry' parameter.
	 *
	 * If an entry socket is required, the method will return the default
	 * entry socket or the first socket if no default socket has been
	 * specified.
	 *
	 * If an exit socket is required, the method will return the default
	 * exit socket or the first socket if no default socket has been
	 * specified. If there is already a control link attached to this socket,
	 * the next available socket will be choosen.
	 *
	 * @param isEntry
	 *		true	An entry socket is required.<br>
	 *		false	An exit socket is required.
	 * @return The socket or null if there is no available socket
	 */
	public NodeSocket getConnectableSocket(boolean isEntry);

	/**
	 * Returns the default socket of a node.
	 *
	 * @param isEntry
	 *		true	We are looking for the default entry socket.<br>
	 *		false	We are looking for the default exit socket.
	 * @return The socket or null if there is no such default socket
	 */
	public NodeSocket getDefaultSocket(boolean isEntry);

	/**
	 * Gets the image path.
	 * The image path is either relative to the model directory (recommended) or absolute.
	 * @nowarn
	 */
	public String getImagePath();

	/**
	 * Sets the image path.
	 * The image path is either relative to the model directory (recommended) or absolute.
	 * @nowarn
	 */
	public void setImagePath(String imagePath);

	/**
	 * Gets the flag if only the image should be displayed instead of node drawing + image.
	 * @nowarn
	 */
	public boolean isImageOnly();

	/**
	 * Sets the flag if only the image should be displayed instead of node drawing + image.
	 * @nowarn
	 */
	public void setImageOnly(boolean imageOnly);

	/**
	 * Gets the flag if the image should be resized with the node.
	 * @nowarn
	 */
	public boolean isImageResize();

	/**
	 * Sets the flag if the image should be resized with the node.
	 * @nowarn
	 */
	public void setImageResize(boolean imageResize);
}
