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
package org.openbp.core.model.modelinspection;

import java.io.Serializable;

/**
 * Exit socket descriptor.
 * Implementation of the SocketDescriptor data type component.
 * Data container that describes an socket of a process node.
 */
public class SocketDescriptor
	implements Serializable
{
	/** Unqualified socket name */
	protected String name;

	/** Socket title */
	protected String displayName;

	/** Descriptive text */
	protected String description;

	/** Flag if this socket is an exit socket */
	protected boolean exitSocket;

	/** Flag if this socket is a default socket */
	protected boolean defaultSocket;

	/** Sequence id */
	protected String sequenceId;

	/** Role or list of roles (comma-separated) that have the permission for this socket */
	private String role;

	/**
	 * Default constructor.
	 */
	public SocketDescriptor()
	{
	}

	/**
	 * Gets the unqualified socket name.
	 * @nowarn
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the unqualified socket name.
	 * @nowarn
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the socket title.
	 * @nowarn
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/**
	 * Sets the socket title.
	 * @nowarn
	 */
	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	/**
	 * Gets the descriptive text.
	 * @nowarn
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Sets the descriptive text.
	 * @nowarn
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * Gets the flag if this socket is an exit socket.
	 * @nowarn
	 */
	public boolean isExitSocket()
	{
		return exitSocket;
	}

	/**
	 * Sets the flag if this socket is an exit socket.
	 * @nowarn
	 */
	public void setExitSocket(boolean exitSocket)
	{
		this.exitSocket = exitSocket;
	}

	/**
	 * Gets the flag if this socket is a default socket.
	 * @nowarn
	 */
	public boolean isDefaultSocket()
	{
		return defaultSocket;
	}

	/**
	 * Sets the flag if this socket is a default socket.
	 * @nowarn
	 */
	public void setDefaultSocket(boolean defaultSocket)
	{
		this.defaultSocket = defaultSocket;
	}

	/**
	 * Gets the role or list of roles (comma-separated) that have the permission for this socket.
	 * @nowarn
	 */
	public String getRole()
	{
		return role;
	}

	/**
	 * Sets the role or list of roles (comma-separated) that have the permission for this socket.
	 * @nowarn
	 */
	public void setRole(String role)
	{
		this.role = role;
	}

	/**
	 * Gets the sequence id.
	 * @nowarn
	 */
	public String getSequenceId()
	{
		return sequenceId;
	}

	/**
	 * Sets the sequence id.
	 * @nowarn
	 */
	public void setSequenceId(String sequenceId)
	{
		this.sequenceId = sequenceId;
	}
}
