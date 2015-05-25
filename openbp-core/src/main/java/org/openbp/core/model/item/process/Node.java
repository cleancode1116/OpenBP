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

import org.openbp.core.handler.HandlerDefinition;

/**
 * Process node object.
 * A process consists of a number of nodes that are connected by control and data links.<br>
 * A node has a number of entries and a number of exits.
 *
 * @author Heiko Erhardt
 */
public interface Node
	extends ProcessObject, NodeProvider
{
	/**
	 * Determines if this node supports multiple control links on a single exit socket.
	 *
	 * @return
	 *		true	More than one control link can be attached to the exit sockets of this node.
	 *		false	An exit socket of this node supports one control link at a time only.
	 */
	public boolean isMultiExitLinkNode();

	/**
	 * Gets the name of the node for parameter value context access ("node").
	 * @nowarn
	 */
	public String getContextName();

	//////////////////////////////////////////////////
	// @@ Execution
	//////////////////////////////////////////////////

	/**
	 * Gets the default exit socket.
	 *
	 * @return The default exit socket or null
	 */
	public NodeSocket getDefaultExitSocket();

	/**
	 * Gets the default entry socket.
	 *
	 * @return The default entry socket or null
	 */
	public NodeSocket getDefaultEntrySocket();

	//////////////////////////////////////////////////
	// @@ Property access
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
	 * Gets the number of node sockets.
	 * @return The number of node sockets in the collection
	 */
	public int getNumberOfSockets();

	/**
	 * Gets a socket by its name.
	 *
	 * @param name Name of the socket
	 * @return The socket or null if no such socket exists
	 */
	public NodeSocket getSocketByName(String name);

	/**
	 * Returns a socket that we can connect to.
	 * The type of socket is specified by the 'isEntry' parameter.
	 *
	 * The strategy that is used to determine a connectable socket depends
	 * on the node implementation.
	 *
	 * @param isEntry
	 *		true	An entry socket is required.<br>
	 *		false	An exit socket is required.
	 * @return The socket or null if there is no available socket
	 */
	public NodeSocket getConnectableSocket(boolean isEntry);

	/**
	 * Gets the process the node belongs to.
	 * @nowarn
	 */
	public ProcessItem getProcess();

	/**
	 * Sets the process the node belongs to.
	 * @nowarn
	 */
	public void setProcess(ProcessItem process);

	/**
	 * Gets the type of queue for the current node.
	 * The queue type may be used to process different node or activity types by different engine instances (e. g. different servers).
	 * @nowarn
	 */
	public String getQueueType();

	/**
	 * Sets the type of queue for the current node.
	 * The queue type may be used to process different node or activity types by different engine instances (e. g. different servers).
	 * @nowarn
	 */
	public void setQueueType(String queueType);

	/**
	 * Gets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public String getGeometry();

	/**
	 * Sets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public void setGeometry(String geometry);

	/**
	 * Gets the event handler definition.
	 * @nowarn
	 */
	public HandlerDefinition getEventHandlerDefinition();

	/**
	 * Sets the event handler definition.
	 * @nowarn
	 */
	public void setEventHandlerDefinition(HandlerDefinition handlerDefinition);
}
