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
package org.openbp.common.generic.msgcontainer;

import org.openbp.common.MsgFormat;

/**
 * Implementation of a message container that does nothing.
 * All messages will be discarded.
 * This class is a singleton.
 *
 * @author Heiko Erhardt
 */
public final class NullMsgContainer
	implements MsgContainer
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Singleton instance */
	private static NullMsgContainer singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized NullMsgContainer getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new NullMsgContainer();
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private NullMsgContainer()
	{
	}

	//////////////////////////////////////////////////
	// @@ Adding messages w/o type
	//////////////////////////////////////////////////

	/**
	 * Adds a message to the message list.
	 * The message will be formatted according to the rules specified in the
	 * {@link MsgFormat} class.
	 *
	 * @param source Object that caused the message
	 * @param msg Message to add
	 * @param args Message arguments or null
	 */
	public void addMsg(Object source, String msg, Object [] args)
	{
	}

	/**
	 * Adds a message to the message list.
	 *
	 * @param source Object that caused the message
	 * @param msg Message to add
	 */
	public void addMsg(Object source, String msg)
	{
	}

	//////////////////////////////////////////////////
	// @@ Adding messages w/ type
	//////////////////////////////////////////////////

	/**
	 * Adds a message to the message list.
	 * The message will be formatted according to the rules specified in the
	 * {@link MsgFormat} class.
	 *
	 * @param msgType Message type
	 * @param source Object that caused the message
	 * @param msg Message to add
	 * @param args Message arguments or null
	 */
	public void addMsg(String msgType, Object source, String msg, Object [] args)
	{
	}

	/**
	 * Adds a message to the message list.
	 *
	 * @param msgType Message type
	 * @param source Object that caused the message
	 * @param msg Message to add
	 */
	public void addMsg(String msgType, Object source, String msg)
	{
	}

	//////////////////////////////////////////////////
	// @@ String representation
	//////////////////////////////////////////////////

	/**
	 * Returns a string representation of this object.
	 * @return The empty string
	 */
	public String toString()
	{
		return "";
	}
}
