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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.common.generic.Copyable;
import org.openbp.common.util.iterator.EmptyIterator;
import org.openbp.common.util.iterator.SingleIterator;

/**
 * Standard implementation of a node that has one socket only.
 *
 * @author Heiko Erhardt
 */
public abstract class SingleSocketNodeImpl extends NodeImpl
	implements SingleSocketNode
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** The only node socket (may not be null) */
	protected NodeSocket socket;

	/** Geometry information of the sub process socket that calls this node (required by the Modeler) */
	private String socketGeometry;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public SingleSocketNodeImpl()
	{
	}

	/**
	 * Copies the values of the source object to this object.
	 *
	 * @param source The source object. Must be of the same type as this object.
	 * @param copyMode Determines if a deep copy, a first level copy or a shallow copy is to be
	 * performed. See the constants of the org.openbp.common.generic.description.Copyable class.
	 * @throws CloneNotSupportedException If the cloning of one of the contained objects failed
	 */
	public void copyFrom(Object source, int copyMode)
		throws CloneNotSupportedException
	{
		if (source == this)
			return;
		super.copyFrom(source, copyMode);

		SingleSocketNodeImpl src = (SingleSocketNodeImpl) source;

		socketGeometry = src.socketGeometry;

		if (copyMode == Copyable.COPY_DEEP)
		{
			// Copy the socket only for deep copies; otherwise, the Modeler will get out of sync
			if (src.socket != null)
				socket = (NodeSocket) src.socket.clone();
			else
				socket = null;
		}
		else
		{
			socket = src.socket;
		}
	}

	/**
	 * Gets the children (direct subordinates) of this object.
	 * The method returns the only socket of the node.
	 *
	 * @return The list of objects or null if the object does not have children
	 */
	public List getChildren()
	{
		List list = new ArrayList();
		list.add(socket);
		return list;
	}

	//////////////////////////////////////////////////
	// @@ Node implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the node sockets.
	 * @return An iterator of {@link NodeSocket} objects
	 */
	public Iterator getSockets()
	{
		// Node has a single node socket only
		if (socket != null)
			return new SingleIterator(socket);
		return EmptyIterator.getInstance();
	}

	/**
	 * Gets the node sockets in the order determined by the {@link NodeSocket#setSequenceId(String)} property.
	 * @return An iterator of {@link NodeSocket} objects
	 */
	public Iterator getOrderedSockets()
	{
		return getSockets();
	}

	/**
	 * Gets the number of node sockets.
	 * @return The number of node sockets in the collection
	 */
	public int getNumberOfSockets()
	{
		// Node has no a single node socket only
		return 1;
	}

	/**
	 * Gets a socket by its name.
	 *
	 * @param name Name of the socket
	 * @return The only socket if the name matches, null otherwise
	 */
	public NodeSocket getSocketByName(String name)
	{
		if (socket != null && socket.getName().equals(name))
		{
			// Return the only socket
			return socket;
		}
		return null;
	}

	/**
	 * Gets the default entry socket.
	 *
	 * @return The default entry socket or null
	 */
	public NodeSocket getDefaultEntrySocket()
	{
		// Single socket is combined entry and exit socket
		return socket;
	}

	/**
	 * Gets the default exit socket.
	 *
	 * @return The default exit socket or null
	 */
	public NodeSocket getDefaultExitSocket()
	{
		// Single socket is combined entry and exit socket
		return socket;
	}

	/**
	 * Returns a socket that we can connect to.
	 * The type of socket is specified by the 'isEntry' parameter.
	 *
	 * If an entry socket is required, the method will always return the only entry socket
	 * or null if there is no entry socket.
	 *
	 * If an exit socket is required, the method will return the only exit socket
	 * or null if there is no exit socket or there is already a control link attached
	 * to this socket,
	 *
	 * @param isEntry
	 *		true	An entry socket is required.<br>
	 *		false	An exit socket is required.
	 * @return The socket or null if there is no available socket
	 */
	public NodeSocket getConnectableSocket(boolean isEntry)
	{
		if (socket != null)
		{
			if (isEntry)
			{
				// The default entry socket will always be chosen regardless if there is
				// already a connector or not
				if (socket.isEntrySocket())
					return socket;
			}
			else
			{
				// The default exit socket will be chosen only
				// if there is no link connected to it
				if (socket.isExitSocket() && !socket.hasControlLinks())
					return socket;
			}
		}

		return null;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the the only node exit socket.
	 * @nowarn
	 */
	public NodeSocket getSocket()
	{
		return socket;
	}

	/**
	 * Sets the the only node exit socket.
	 * @nowarn
	 */
	public void setSocket(NodeSocket socket)
	{
		this.socket = socket;

		if (socket != null)
		{
			// Back reference
			socket.setNode(this);
		}
	}

	/**
	 * Gets the geometry information of the sub process socket that calls this node (required by the Modeler).
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public String getSocketGeometry()
	{
		return socketGeometry;
	}

	/**
	 * Sets the geometry information of the sub process socket that calls this node (required by the Modeler).
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public void setSocketGeometry(String socketGeometry)
	{
		this.socketGeometry = socketGeometry;
	}
}
