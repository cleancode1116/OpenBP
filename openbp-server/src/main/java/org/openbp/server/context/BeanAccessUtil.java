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
package org.openbp.server.context;

import org.openbp.common.logger.LogUtil;
import org.openbp.common.property.PropertyAccessUtil;
import org.openbp.common.property.PropertyException;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.type.DataMember;

/**
 * Bean value access util.
 *
 * @author Heiko Erhardt
 */
public final class BeanAccessUtil
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private BeanAccessUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Parameter value binding
	//////////////////////////////////////////////////

	/**
	 * Internal error output method.
	 *
	 * @param arg Current member (can be a {@link ModelObject} or a string denoting the object)
	 * @param msg Message to add
	 * @param t Throwable object that caused the message or null
	 * @return The error message
	 */
	public static String errDiag(Object arg, String msg, Throwable t)
	{
		StringBuffer sb = new StringBuffer(200);
		if (arg != null)
		{
			if (arg instanceof ModelObject)
			{
				arg = ((ModelObject) arg).getQualifier();
			}

			sb.append(arg.toString());
			sb.append(": ");
		}

		sb.append(msg);

		if (t != null)
		{
			// Log message
			LogUtil.error(BeanAccessUtil.class, sb.toString(), t);
		}
		else
		{
			// Log message
			LogUtil.error(BeanAccessUtil.class, sb.toString());
		}

		return msg;
	}

	//////////////////////////////////////////////////
	// @@ Bean value access
	//////////////////////////////////////////////////

	/**
	 * Retrieve the value of a member using its getter method or value map access.
	 *
	 * @param bean Bean to operate on
	 * @param memberName Name of the member to get
	 * @param errMsg
	 *		true	Log an error message if member access method/field/map not found<br>
	 *		false	Silently ignore failures
	 * @return The member value or null if the value could not be accessed or is null
	 */
	public static Object getMemberValue(Object bean, String memberName, boolean errMsg)
	{
		// There might be a native getter/setter for this member
		// Try to call it using reflections

		Object value = null;
		try
		{
			value = PropertyAccessUtil.getProperty(bean, memberName);
		}
		catch (PropertyException e)
		{
			if (errMsg)
			{
				errDiag(memberName, "Error getting bean value.", e);
			}
		}

		return value;
	}

	/**
	 * Retrieve the value of a member using its getter method or value map access.
	 *
	 * @param bean Bean to operate on
	 * @param member Definition of the member to get
	 * @return The member value or null if the value could not be accessed or is null
	 */
	public static Object getMemberValue(Object bean, DataMember member)
	{
		String memberName = member.getName();
		return getMemberValue(bean, memberName, true);
	}

	/**
	 * Assign a value to a member using its setter method or value map access.
	 *
	 * @param bean Bean to operate on
	 * @param memberName Name of the member to set
	 * @param value Member value to assign
	 * @return
	 *		true	The value was successfully set.<br>
	 *		false	An error occurred when setting the value.
	 *				An error description has been written to the log file.
	 */
	public static boolean setMemberValue(Object bean, String memberName, Object value)
	{
		// There is not data type assigned to this bean, so try setting the member directly
		// without any type information. PropertyAccessUtil will try to guess the type from
		// the value supplied. Note that this might be a problem with null values...
		try
		{
			PropertyAccessUtil.setProperty(bean, memberName, value);
			return true;
		}
		catch (PropertyException e)
		{
			errDiag(memberName, "Error setting bean value.", e);
			return false;
		}
	}
}
