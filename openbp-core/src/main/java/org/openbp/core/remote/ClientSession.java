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
package org.openbp.core.remote;

import java.io.Serializable;

/**
 * This class represents a connection the client is maintaining with
 * the OpenBP server.
 *
 * @author Falk Hartmann
 */
public class ClientSession
	implements Serializable
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	private String sessionID = null;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor (needed for Serialization).
	 */
	public ClientSession()
	{
	}

	/**
	 * Constructor.
	 *
	 * @param sessionID The session ID for this session
	 */
	public ClientSession(String sessionID)
	{
		this.sessionID = sessionID;
	}

	//////////////////////////////////////////////////
	// @@ Access methods.
	//////////////////////////////////////////////////

	public String getSessionID()
	{
		return sessionID;
	}

	//////////////////////////////////////////////////
	// @@ Miscellanous
	//////////////////////////////////////////////////

	/**
	 * This method is overwritten here to ensure that equivalence is given
	 * between a stored session and one which has been created using
	 * serialization.
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		// To be equal to a ClientSession, one needs to be a ClientSession, too.
		if (obj instanceof ClientSession)
		{
			// Two sessions are considered equal if their ID's are equal.
			return sessionID.equals(((ClientSession) obj).getSessionID());
		}

		return false;
	}

	/**
	 * This method is overwritten here to allow Sessions to be kept in container
	 * classes.
	 *
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return sessionID.hashCode();
	}
}
