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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.common.MsgFormat;
import org.openbp.common.generic.PrintNameProvider;
import org.openbp.common.generic.description.DescriptionObject;
import org.openbp.common.logger.LogLevel;
import org.openbp.common.util.iterator.EmptyIterator;

/**
 * Standard implementation of a message container.
 * The class will collect all messages that are added. The messages can be retrieved
 * using the {@link #getMsgs} method.
 *
 * @author Heiko Erhardt
 */
public class StandardMsgContainer
	implements MsgContainer, Serializable
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Message list (contains {@link MsgItem} objects, may be null) */
	private List msgList;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public StandardMsgContainer()
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
		addMsg(LogLevel.ERROR, source, msg, args);
	}

	/**
	 * Adds a message to the message list.
	 *
	 * @param source Object that caused the message
	 * @param msg Message to add
	 */
	public void addMsg(Object source, String msg)
	{
		addMsg(source, msg, null);
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
		MsgItem item = new MsgItem(msgType, source, msg, args);
		addMsg(item);
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
		addMsg(msgType, source, msg, null);
	}

	//////////////////////////////////////////////////
	// @@ String representation
	//////////////////////////////////////////////////

	/**
	 * Returns a string representation of this object.
	 *
	 * @return The string representation lists all messages that have been added to the
	 * message container. The messages will be separated by the '\n' character.<br>
	 * The object that caused the message will display its qualified name if it implements
	 * the {@link PrintNameProvider} interface or its name if it implements the {@link DescriptionObject}
	 * interface. Otherwise, the toString representation of the object will be printed.<br>
	 * If there are no messages present, the empty string will be returned.
	 */
	public String toString()
	{
		if (msgList != null)
		{
			StringBuffer sb = null;

			int n = msgList.size();
			for (int i = 0; i < n; ++i)
			{
				MsgItem msgItem = (MsgItem) msgList.get(i);

				String msg = msgItem.getFormattedMsgWithSource();

				if (msg != null)
				{
					if (sb == null)
						sb = new StringBuffer();
					else
						sb.append('\n');
					sb.append(msg);
				}
			}

			if (sb != null)
				return sb.toString();
		}
		return "";
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Checks if the message container contains messages.
	 * @nowarn
	 */
	public boolean isEmpty()
	{
		return msgList == null;
	}

	/**
	 * Gets the message list.
	 * @return An iterator of {@link MsgItem} objects
	 */
	public Iterator getMsgs()
	{
		if (msgList == null)
			return EmptyIterator.getInstance();
		return msgList.iterator();
	}

	/**
	 * Gets the message list.
	 * @return A list of {@link MsgItem} objects or null if there are no messages
	 */
	public List getList()
	{
		return msgList;
	}

	/**
	 * Adds a message to the message list.
	 * @param msg The message to add
	 */
	public void addMsg(MsgItem msg)
	{
		if (msgList == null)
			msgList = new ArrayList();
		msgList.add(msg);
	}

	/**
	 * Clears the message list.
	 */
	public void clearMsgs()
	{
		msgList = null;
	}
}
