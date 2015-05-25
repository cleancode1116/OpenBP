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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.openbp.common.CollectionUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.util.CopyUtil;
import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.common.util.SortingArrayList;
import org.openbp.common.util.iterator.EmptyIterator;
import org.openbp.core.model.item.ConfigurationBean;
import org.openbp.core.model.item.Item;

/**
 * Standard implementation of a node that has multiple sockets.
 *
 * @author Heiko Erhardt
 */
public abstract class MultiSocketNodeImpl extends NodeImpl
	implements MultiSocketNode
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Node sockets (contains {@link NodeSocket} objects, may be null) */
	private List socketList;

	/** Configuration bean */
	private ConfigurationBean configurationBean;

	/** Image path */
	private String imagePath;

	/** Flag if only the image should be displayed instead of node drawing + image */
	private boolean imageOnly;

	/** Flag if the image size should be resized with the node */
	private boolean imageResize;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public MultiSocketNodeImpl()
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

		MultiSocketNodeImpl src = (MultiSocketNodeImpl) source;

		// Copy member data
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

		if (src.configurationBean != null)
			configurationBean = (ConfigurationBean) src.configurationBean.clone();
		else
			configurationBean = null;

		imagePath = src.imagePath;
		imageOnly = src.imageOnly;
		imageResize = src.imageResize;
	}

	/**
	 * Gets the children (direct subordinates) of this object.
	 * The method returns the sockets of the node.
	 *
	 * @return The list of objects or null if the object does not have children
	 */
	public List getChildren()
	{
		return socketList;
	}

	//////////////////////////////////////////////////
	// @@ Standard implementations - to be overridden by subclasses
	//////////////////////////////////////////////////

	/**
	 * @copy ItemProvider.copyFromItem
	 */
	public void copyFromItem(Item item, int syncFlags)
	{
		// Not supported for this node
	}

	/**
	 * @copy ItemProvider.copyToItem
	 */
	public void copyToItem(Item item, int syncFlags)
	{
		// Not supported for this type of node
	}

	//////////////////////////////////////////////////
	// @@ Property access: Sockets
	//////////////////////////////////////////////////

	/**
	 * Gets the node sockets.
	 * @return An iterator of {@link NodeSocket} objects
	 */
	public Iterator getSockets()
	{
		if (socketList == null)
			return EmptyIterator.getInstance();
		return socketList.iterator();
	}

	/**
	 * Gets the node sockets in the order determined by the {@link NodeSocket#setSequenceId(String)} property.
	 * The method will perform an alphabetic comparison.
	 * @return An iterator of {@link NodeSocket} objects<br>
	 * The iterator contains only those node sockets that have the {@link NodeSocket#setSequenceId(String)} defined.
	 */
	public Iterator getOrderedSockets()
	{
		if (socketList == null)
			return EmptyIterator.getInstance();

		// Copy the socket list and sort it according to the order defined by the {@link NodeSocket#compare} method.
		List list = null;
		try
		{
			list = (List) CopyUtil.copyCollection(socketList, CopyUtil.CLONE_NONE);
		}
		catch (CloneNotSupportedException e)
		{
			// Doesn't happen
			return null;
		}

		// Remove all sockets that do not have a sequence id
		for (Iterator it = list.iterator(); it.hasNext();)
		{
			NodeSocket socket = (NodeSocket) it.next();
			if (socket.getSequenceId() == null)
				it.remove();
		}

		// Sort the list
		Comparator socketComparator = new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				NodeSocket ns1 = (NodeSocket) o1;
				NodeSocket ns2 = (NodeSocket) o2;

				String s1 = ns1.getSequenceId();
				String s2 = ns2.getSequenceId();

				// First, try the sequence number
				int result = s1.compareTo(s2);

				if (result == 0)
				{
					// Not present or equal, compare by display text
					s1 = ns1.getDisplayText();
					s2 = ns2.getDisplayText();
					if (s1 != null && s2 != null)
					{
						result = s1.compareTo(s2);
					}
				}

				return result;
			}
		};

		Collections.sort(list, socketComparator);

		return list.iterator();
	}

	/**
	 * Gets the node sockets of a particular type.
	 * @param isEntry
	 *		true	Collect entry sockets<br>
	 *		false	Collect exit sockets
	 * @return An iterator of {@link NodeSocket} objects
	 */
	public Iterator getSockets(boolean isEntry)
	{
		if (socketList == null)
			return EmptyIterator.getInstance();

		List list = null;

		int n = socketList.size();
		for (int i = 0; i < n; ++i)
		{
			NodeSocket socket = (NodeSocket) socketList.get(i);

			if (isEntry)
			{
				if (socket.isEntrySocket())
				{
					if (list == null)
						list = new ArrayList();
					list.add(socket);
				}
			}
			else
			{
				if (socket.isExitSocket())
				{
					if (list == null)
						list = new ArrayList();
					list.add(socket);
				}
			}
		}

		return list != null ? list.iterator() : EmptyIterator.getInstance();
	}

	/**
	 * Gets the number of node sockets.
	 * @return The number of node sockets in the collection
	 */
	public int getNumberOfSockets()
	{
		return socketList != null ? socketList.size() : 0;
	}

	/**
	 * Creates a new socket and assigns a new name to it.
	 * @param name Name of the new parameter or null for the default ("Socket")
	 * @return The new socket
	 */
	public NodeSocket createSocket(String name)
	{
		NodeSocket socket = new NodeSocketImpl();

		if (name == null)
			name = "Socket";
		name = NamedObjectCollectionUtil.createUniqueId(socketList, name);
		socket.setName(name);

		return socket;
	}

	/**
	 * Adds a socket.
	 * @param socket The socket to add
	 */
	public void addSocket(NodeSocket socket)
	{
		if (socketList == null)
			socketList = new SortingArrayList();
		socketList.add(socket);

		socket.setNode(this);
	}

	/**
	 * Removes a socket.
	 * @param socket The socket to remove
	 */
	public void removeSocket(NodeSocket socket)
	{
		CollectionUtil.removeReference(socketList, socket);
	}

	/**
	 * Gets a socket by its name.
	 *
	 * @param name Name of the socket
	 * @return The socket or null if no such socket exists
	 */
	public NodeSocket getSocketByName(String name)
	{
		return socketList != null ? (NodeSocket) NamedObjectCollectionUtil.getByName(socketList, name) : null;
	}

	/**
	 * Clears the node sockets.
	 */
	public void clearSockets()
	{
		socketList = null;
	}

	/**
	 * Gets the node sockets.
	 * @return A list of {@link NodeSocket} objects
	 */
	public List getSocketList()
	{
		return socketList;
	}

	/**
	 * Gets the default exit socket.
	 *
	 * @return The default exit socket or null
	 */
	public NodeSocket getDefaultExitSocket()
	{
		return getDefaultSocket(false);
	}

	/**
	 * Gets the default entry socket.
	 *
	 * @return The default entry socket or null
	 */
	public NodeSocket getDefaultEntrySocket()
	{
		return getDefaultSocket(true);
	}

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
	public NodeSocket getConnectableSocket(boolean isEntry)
	{
		if (socketList != null)
		{
			NodeSocket defaultSocket = getDefaultSocket(isEntry);

			if (defaultSocket != null)
			{
				if (isEntry)
				{
					// The default entry socket will always be chosen regardless if there is
					// already a connector or not
					return defaultSocket;
				}

				if (!defaultSocket.hasControlLinks())
				{
					// The default exit socket will be chosen only
					// if there is no link connected to it
					return defaultSocket;
				}
			}

			// Iterate all matching sockets in order to find the next available one
			for (Iterator it = getSockets(isEntry); it.hasNext();)
			{
				NodeSocket socket = (NodeSocket) it.next();

				if (socket == defaultSocket)
				{
					// We already checked the default socket
					continue;
				}

				if (isEntry)
				{
					// An entry socket will always be chosen regardless if there is
					// already a connector or not
					return socket;
				}
				if (!socket.hasControlLinks())
				{
					// An exit socket will be chosen only
					// if there is no link connected to it
					return socket;
				}
			}
		}

		return null;
	}

	/**
	 * Returns the default socket of a node.
	 *
	 * @param isEntry
	 *		true	We are looking for the default entry socket.<br>
	 *		false	We are looking for the default exit socket.
	 * @return The socket or null if there is no such default socket
	 */
	public NodeSocket getDefaultSocket(boolean isEntry)
	{
		if (socketList != null)
		{
			int n = socketList.size();

			// First, check if we can find a socket that is defined as default
			for (int i = 0; i < n; ++i)
			{
				NodeSocket socket = (NodeSocket) socketList.get(i);

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
			NodeSocket semiDefaultSocket = null;
			for (int i = 0; i < n; ++i)
			{
				NodeSocket socket = (NodeSocket) socketList.get(i);

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

	//////////////////////////////////////////////////
	// @@ Property access: Item
	//////////////////////////////////////////////////

	/**
	 * Determines if configuration bean of the node has values different than the default values.
	 * @nowarn
	 */
	public boolean hasConfigurationBean()
	{
		return configurationBean != null && !configurationBean.hasDefaultValues();
	}

	/**
	 * Gets the node setting bean.
	 * @nowarn
	 */
	public ConfigurationBean getConfigurationBean()
	{
		return configurationBean;
	}

	/**
	 * Sets the configuration bean.
	 * @nowarn
	 */
	public void setConfigurationBean(ConfigurationBean configurationBean)
	{
		this.configurationBean = configurationBean;
	}

	/**
	 * Gets the image path.
	 * The image path is either relative to the model directory (recommended) or absolute.
	 * @nowarn
	 */
	public String getImagePath()
	{
		return imagePath;
	}

	/**
	 * Sets the image path.
	 * The image path is either relative to the model directory (recommended) or absolute.
	 * @nowarn
	 */
	public void setImagePath(String imagePath)
	{
		this.imagePath = imagePath;
	}

	/**
	 * Checks the flag if only the image should be displayed instead of node drawing + image is set.
	 * @nowarn
	 */
	public boolean hasImageOnly()
	{
		return imageOnly;
	}

	/**
	 * Gets the flag if only the image should be displayed instead of node drawing + image.
	 * @nowarn
	 */
	public boolean isImageOnly()
	{
		return imageOnly;
	}

	/**
	 * Sets the flag if only the image should be displayed instead of node drawing + image.
	 * @nowarn
	 */
	public void setImageOnly(boolean imageOnly)
	{
		this.imageOnly = imageOnly;
	}

	/**
	 * Checks the flag if the image should be resized with the node.
	 * @nowarn
	 */
	public boolean hasImageResize()
	{
		return imageResize;
	}

	/**
	 * Gets the flag if the image should be resized with the node.
	 * @nowarn
	 */
	public boolean isImageResize()
	{
		return imageResize;
	}

	/**
	 * Sets the flag if the image should be resized with the node.
	 * @nowarn
	 */
	public void setImageResize(boolean imageResize)
	{
		this.imageResize = imageResize;
	}
}
