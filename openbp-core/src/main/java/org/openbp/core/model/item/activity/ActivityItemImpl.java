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

import org.openbp.common.generic.Copyable;
import org.openbp.common.util.CopyUtil;
import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.common.util.SortingArrayList;
import org.openbp.common.util.iterator.EmptyIterator;
import org.openbp.core.model.item.ItemImpl;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.ActivityNode;
import org.openbp.core.model.item.process.ActivityNodeImpl;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.ProcessItem;

/**
 * Implementation if an activity object.
 * An activity is a node of a process.<br>
 * The activity has a number of entries (at least and also usually one) and
 * a number of exits (also at least one).
 *
 * @author Heiko Erhardt
 */
public class ActivityItemImpl extends ItemImpl
	implements ActivityItem
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Execution mode: Immediate execution */
	public static final String STR_IMMEDIATE = "Immediate";

	/** Execution mode: Asynchronous execution */
	public static final String STR_ASYNCHRONOUS = "Asynchronous";

	/** Execution mode: Persisted execution */
	public static final String STR_PERSISTED = "Persisted";

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Activity sockets (contains {@link ActivitySocket} objects) */
	private List socketList;

	/** Geometry information (required by the Modeler) */
	private String geometry;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ActivityItemImpl()
	{
		setItemType(ItemTypes.ACTIVITY);
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

		ActivityItemImpl src = (ActivityItemImpl) source;

		if (copyMode == Copyable.COPY_FIRST_LEVEL || copyMode == Copyable.COPY_DEEP)
		{
			// Create deep clones of collection members
			socketList = (List) CopyUtil.copyCollection(src.socketList, copyMode == Copyable.COPY_DEEP ? CopyUtil.CLONE_VALUES : CopyUtil.CLONE_NONE);
		}
		else
		{
			// Shallow clone
			socketList = src.socketList;
		}

		geometry = src.geometry;
	}

	/**
	 * Creates a new activity node that references this activity.
	 *
	 * @return The new {@link ActivityNode}
	 * @copy NodeProvider.toNode
	 */
	public Node toNode(ProcessItem process, int syncFlags)
	{
		ActivityNode result = new ActivityNodeImpl();

		result.setProcess(process);
		result.copyFromItem(this, syncFlags);

		return result;
	}

	/**
	 * Gets the children (direct subordinates) of this object.
	 * The method returns the sockets of the activity.
	 *
	 * @return The list of objects or null if the object does not have children
	 */
	public List getChildren()
	{
		return socketList;
	}

	/**
	 * Gets the geometry information (required by the Modeler).
	 * @nowarn
	 */
	public String getGeometry()
	{
		return geometry;
	}

	/**
	 * Sets the geometry information (required by the Modeler).
	 * @nowarn
	 */
	public void setGeometry(String geometry)
	{
		this.geometry = geometry;
	}

	//////////////////////////////////////////////////
	// @@ Pre save/post load processing and validation
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.maintainReferences
	 */
	public void maintainReferences(int flag)
	{
		super.maintainReferences(flag);

		for (Iterator it = getSockets(); it.hasNext();)
		{
			ActivitySocket socket = (ActivitySocket) it.next();

			// Establish back reference
			socket.setActivity(this);

			socket.maintainReferences(flag);
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the activity sockets.
	 * @return An iterator of  ActivitySocket objects
	 */
	public Iterator getSockets()
	{
		if (socketList == null)
			return EmptyIterator.getInstance();
		return socketList.iterator();
	}

	/**
	 * Gets the number of activity sockets.
	 * @return The number of activity sockets in the collection
	 */
	public int getNumberOfSockets()
	{
		return socketList != null ? socketList.size() : 0;
	}

	/**
	 * Creates a new socket and assigns a new name to it.
	 * @param name Name of the new parameter or null for the default ("Param")
	 * @return The new socket
	 */
	public ActivitySocket createSocket(String name)
	{
		ActivitySocket socket = new ActivitySocketImpl();

		if (name == null)
			name = "Socket";
		name = NamedObjectCollectionUtil.createUniqueId(socketList, name);
		socket.setName(name);

		return socket;
	}

	/**
	 * Creates a new socket.
	 *
	 * @return The new socket
	 */
	public ActivitySocket createSocket()
	{
		return createSocket(null);
	}

	/**
	 * Adds a socket.
	 * @param socket The socket to add
	 */
	public void addSocket(ActivitySocket socket)
	{
		if (socketList == null)
			socketList = new SortingArrayList();
		socketList.add(socket);

		socket.setActivity(this);
	}

	/**
	 * Removes a socket.
	 * @param socket The socket to remove
	 */
	public void removeSocket(ActivitySocket socket)
	{
		if (socketList != null)
		{
			if (socketList.remove(socket))
			{
				socket.setActivity(null);
				if (socketList.isEmpty())
				{
					socketList = null;
				}
			}
		}
	}

	/**
	 * Gets a socket by its name.
	 *
	 * @param name Name of the socket
	 * @return The socket or null if no such socket exists
	 */
	public ActivitySocket getSocketByName(String name)
	{
		return socketList != null ? (ActivitySocket) NamedObjectCollectionUtil.getByName(socketList, name) : null;
	}

	/**
	 * Gets the default exit socket.
	 *
	 * @return The default exit socket or null
	 */
	public ActivitySocket getDefaultExitSocket()
	{
		return getDefaultSocket(false);
	}

	/**
	 * Gets the default entry socket.
	 *
	 * @return The default entry socket or null
	 */
	public ActivitySocket getDefaultEntrySocket()
	{
		return getDefaultSocket(true);
	}

	/**
	 * Returns the default socket of a node.
	 *
	 * @param isEntry
	 *		true	We are looking for the default entry socket.<br>
	 *		false	We are looking for the default exit socket.
	 * @return The socket or null if there is no such default socket
	 */
	public ActivitySocket getDefaultSocket(boolean isEntry)
	{
		if (socketList != null)
		{
			int n = socketList.size();

			// First, check if we can find a socket that is defined as default
			for (int i = 0; i < n; ++i)
			{
				ActivitySocket socket = (ActivitySocket) socketList.get(i);

				if (socket.isDefaultSocket())
				{
					if (isEntry)
					{
						if (socket.isEntrySocket())
							return socket;
					}
					else
					{
						if (socket.isExitSocket())
							return socket;
					}
				}
			}

			// No default socket present, check if there is only one socket of the desired type.
			// If so, return it.
			ActivitySocket semiDefaultSocket = null;
			for (int i = 0; i < n; ++i)
			{
				ActivitySocket socket = (ActivitySocket) socketList.get(i);

				if (isEntry)
				{
					if (socket.isEntrySocket())
					{
						if (semiDefaultSocket != null)
						{
							// We have more than one socket of the desired type,
							// so we are not able to determine a default socket
							return null;
						}
						semiDefaultSocket = socket;
					}
				}
				else
				{
					if (socket.isExitSocket())
					{
						if (semiDefaultSocket != null)
						{
							// We have more than one socket of the desired type,
							// so we are not able to determine a default socket
							return null;
						}
						semiDefaultSocket = socket;
					}
				}
			}

			if (semiDefaultSocket != null)
				return semiDefaultSocket;
		}

		return null;
	}

	/**
	 * Clears the activity sockets.
	 */
	public void clearSockets()
	{
		socketList = null;
	}

	/**
	 * Gets the activity sockets.
	 * @return A list of ActivitySocket objects
	 */
	public List getSocketList()
	{
		return socketList;
	}

	/**
	 * Sets the activity sockets.
	 * @param socketList A list of ActivitySocket objects
	 */
	public void setSocketList(List socketList)
	{
		this.socketList = socketList;

		if (socketList != null)
		{
			int n = socketList.size();
			for (int i = 0; i < n; ++i)
			{
				ActivitySocket socket = (ActivitySocket) socketList.get(i);
				socket.setActivity(this);
			}
		}
	}
}
