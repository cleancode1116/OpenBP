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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.MsgFormat;
import org.openbp.common.generic.PrintNameProvider;
import org.openbp.common.generic.description.DescriptionObject;
import org.openbp.common.logger.LogLevel;
import org.openbp.common.logger.LogUtil;

/**
 * Description object of a message.
 * The message will be formatted according to the rules specified in the
 * {@link MsgFormat} class.<br>
 * The message source references the object that caused the message.
 *
 * @author Heiko Erhardt
 */
public class MsgItem
	implements Serializable
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Source of the message (may be null) */
	private transient Object source;

	/**
	 * Message type.
	 * See the constants of {@link LogLevel} interface.
	 */
	private String msgType;

	/** The message */
	private String msg;

	/** Message arguments (may be null) */
	private Object [] msgArgs;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public MsgItem()
	{
	}

	/**
	 * Value constructor.
	 *
	 * @param msgType Message type
	 * See the constants of {@link LogLevel} interface.
	 * @param source Source of the message (may be null)
	 * @param msg The message
	 * @param msgArgs Message arguments (may be null)
	 */
	public MsgItem(String msgType, Object source, String msg, Object [] msgArgs)
	{
		this.msgType = msgType;
		this.source = source;
		this.msg = msg;
		this.msgArgs = msgArgs;
	}

	/**
	 * Gets a string representation of this object.
	 *
	 * @return Same as {@link #getFormattedMsgWithSource}
	 */
	public String toString()
	{
		return getFormattedMsgWithSource();
	}

	/**
	 * Gets the name of the source of the message.
	 * @return The print name in case of a {@link PrintNameProvider} object, the name for a {@link DescriptionObject}
	 * or otherwise the result of toString(). Can be null if there is no source object for this description.
	 */
	public String getSourceString()
	{
		String ret = null;
		if (source != null)
		{
			if (source instanceof PrintNameProvider)
				ret = ((PrintNameProvider) source).getPrintName();
			if (ret == null && (source instanceof DescriptionObject))
				ret = ((DescriptionObject) source).getName();
			if (ret == null)
				ret = source.toString();
		}
		return ret;
	}

	/**
	 * Gets the formatted message including message source specification.
	 * @return The formatted message or null if neither message source nor
	 * message was provided.
	 */
	public String getFormattedMsgWithSource()
	{
		String ret = getSourceString();
		String msg = getFormattedMsg();
		if (msg != null)
		{
			if (ret != null)
				ret += ": " + msg;
			else
				ret = msg;
		}
		return ret;
	}

	/**
	 * Gets the formatted message.
	 * @return The formatted message or null if no message was provided
	 */
	public String getFormattedMsg()
	{
		// TOLOCALIZE
		String s;
		if (!msgType.equals(LogLevel.ERROR))
			s = msgType + ": " + msg;
		else
			s = msg;

		if (s != null && msgArgs != null)
		{
			// Format the message
			try
			{
				// Perform null argument and throwable argument checks
				int l = msgArgs.length;
				for (int i = 0; i < l; i++)
				{
					if (msgArgs [i] == null)
						msgArgs [i] = "(null)";
				}

				if (msgArgs != null)
					s = MsgFormat.format(s, msgArgs);

				Object o = msgArgs [l - 1];
				if (o instanceof Throwable)
				{
					// We have an excpetion object as last argument
					// Iterate down any nested exceptions and append the excption message
					Throwable throwable = (Throwable) o;
					String exceptionMsg = ExceptionUtil.getNestedMessage(throwable);
					if (exceptionMsg != null)
					{
						s += ": ";
						s += exceptionMsg;
					}
				}
			}
			catch (Exception e)
			{
				// Error in message format, probably wrong number of args
				LogUtil.error(getClass(), "Logging error: Can't format message $0 with {1} arguments.", s, Integer.valueOf(msgArgs.length), e);
			}
		}

		return s;
	}

	//////////////////////////////////////////////////
	// @@ Serialization support
	//////////////////////////////////////////////////

	/**
	 * This method is implemented here to support serialization of the source object.
	 * See {@link java.io.Serializable} for more information on this method.
	 *
	 * @param out The current object output stream
	 * @throws IOException if an I/O problem occured.
	 */
	private void writeObject(ObjectOutputStream out)
		throws IOException
	{
		// All non-transient members
		out.defaultWriteObject();

		// Write the source string instead of the source object
		out.writeObject(getSourceString());
	}

	/**
	 * This method is implemented here to support serialization of the source object.
	 * See {@link java.io.Serializable} for more information on this method.
	 *
	 * @param in The current object input stream<br>
	 * @throws IOException if an I/O problem occured.
	 * @throws ClassNotFoundException if a class could not be found
	 */
	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException
	{
		// All non-transient members
		in.defaultReadObject();

		// Assign the source string as source object
		source = in.readObject();
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Sets the source of the message.
	 * @nowarn
	 */
	public void setSource(Object source)
	{
		this.source = source;
	}

	/**
	 * Gets the message type.
	 * See the constants of {@link LogLevel} interface.
	 * @nowarn
	 */
	public String getMsgType()
	{
		return msgType;
	}

	/**
	 * Sets the message type.
	 * See the constants of {@link LogLevel} interface.
	 * @nowarn
	 */
	public void setMsgType(String msgType)
	{
		this.msgType = msgType;
	}

	/**
	 * Gets the message.
	 * @nowarn
	 */
	public String getMsg()
	{
		return msg;
	}

	/**
	 * Sets the message.
	 * @nowarn
	 */
	public void setMsg(String msg)
	{
		this.msg = msg;
	}

	/**
	 * Gets the message arguments.
	 * @nowarn
	 */
	public Object [] getMsgArgs()
	{
		return msgArgs;
	}

	/**
	 * Sets the message arguments.
	 * @nowarn
	 */
	public void setMsgArgs(Object [] msgArgs)
	{
		this.msgArgs = msgArgs;
	}
}
