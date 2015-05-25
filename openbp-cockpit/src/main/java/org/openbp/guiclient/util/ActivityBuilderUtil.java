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
package org.openbp.guiclient.util;

import org.openbp.common.generic.description.DisplayObject;
import org.openbp.core.model.item.activity.ActivityItem;
import org.openbp.core.model.item.activity.ActivityParam;
import org.openbp.core.model.item.activity.ActivityParamImpl;
import org.openbp.core.model.item.activity.ActivitySocket;
import org.openbp.core.model.item.activity.ActivitySocketImpl;
import org.openbp.core.model.item.type.DataTypeItem;

/**
 * Utility methods for the construction of action items.
 *
 * @author Heiko Erhardt
 */
public class ActivityBuilderUtil
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Flag for {@link #makeSocket}: Entry socket */
	public static final int MAKESOCKET_ENTRY = (1 << 0);

	/** Flag for {@link #makeSocket}: Default socket */
	public static final int MAKESOCKET_DEFAULT = (1 << 1);

	/** Flag for {@link #makeSocket}: Arrange socket */
	public static final int MAKESOCKET_ARRANGE = (1 << 2);

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private ActivityBuilderUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Socket manipulation
	//////////////////////////////////////////////////

	/**
	 * Creates or updates an activity socket.
	 *
	 * @param item Visual item
	 * @param socketName Socket name
	 * @param flags {@link #MAKESOCKET_ENTRY} | {@link #MAKESOCKET_DEFAULT} | {@link #MAKESOCKET_ARRANGE}
	 * @param newParamList List of parameters to add to the socket
	 * @return
	 *		true	The socket was created or updated
	 *		false	No change to the socket was made
	 */
	public static boolean makeSocket(ActivityItem item, String socketName, int flags, ActivityParam [] newParamList)
	{
		boolean update = false;

		ActivitySocket socket = item.getSocketByName(socketName);
		if (socket == null)
		{
			socket = new ActivitySocketImpl();
			socket.setName(socketName);
			item.addSocket(socket);
			update = true;
		}

		boolean entrySocket = (flags & MAKESOCKET_ENTRY) != 0;
		boolean defaultSocket = (flags & MAKESOCKET_DEFAULT) != 0;
		boolean arrangeSocket = (flags & MAKESOCKET_ARRANGE) != 0;

		if (socket.isEntrySocket() != entrySocket)
		{
			socket.setEntrySocket(entrySocket);
			update = true;
		}

		if (socket.isDefaultSocket() != defaultSocket)
		{
			socket.setDefaultSocket(defaultSocket);
			update = true;
		}

		if (arrangeSocket && socket.getGeometry() != null)
		{
			socket.setGeometry(null);
		}

		if (newParamList != null)
		{
			for (int i = 0; i < newParamList.length; ++i)
			{
				ActivityParam newParam = newParamList [i];
				ActivityParam param = socket.getParamByName(newParam.getName());
				if (param != null)
				{
					// Parameter exists, update the data type if necessary
					if (param.getDataType() != newParam.getDataType())
					{
						param.setDataType(newParam.getDataType());
						update = true;
					}
				}
				else
				{
					// Parameter does not exist yet, add it
					socket.addParam(newParam);
					update = true;
				}
			}
		}

		return update;
	}

	/**
	 * Removes an activity socket.
	 *
	 * @param item Visual item
	 * @param socketName Socket name
	 * @return
	 *		true	The socket was removed
	 *		false	No change to the socket was made
	 */
	public static boolean removeSocket(ActivityItem item, String socketName)
	{
		ActivitySocket socket = item.getSocketByName(socketName);
		if (socket != null)
		{
			item.removeSocket(socket);
			return true;
		}

		return false;
	}

	//////////////////////////////////////////////////
	// @@ Parameter manipulation
	//////////////////////////////////////////////////

	/**
	 * Creates an activity parameter.
	 *
	 * @param name Parameter name
	 * @param displayName Parameter display name
	 * @param description Parameter description
	 * @param type Data type of the parameter
	 * @param optional
	 *		true	The parameter is optional.<br>
	 *		false	The parmeter is required.
	 * @return The new parameter
	 */
	public static ActivityParam makeParam(String name, String displayName, String description, DataTypeItem type, boolean optional)
	{
		ActivityParam param = new ActivityParamImpl();

		param.setName(name);
		param.setDisplayName(displayName);
		param.setDescription(description);

		param.setDataType(type);

		param.setOptional(optional);

		return param;
	}

	/**
	 * Creates an activity parameter.
	 * The method will use the name and description name of the supplied data type
	 * to initialize the parameter.
	 *
	 * @param dob Description object to use for the parameter name and description
	 * @param type Data type of the parameter
	 * @param optional
	 *		true	The parameter is optional.<br>
	 *		false	The parmeter is required.
	 * @return The new parameter
	 */
	public static ActivityParam makeParam(DisplayObject dob, DataTypeItem type, boolean optional)
	{
		ActivityParam param = new ActivityParamImpl();

		param.setName(dob.getName());
		param.setDisplayName(dob.getDisplayName());
		param.setDescription(dob.getDescription());

		param.setDataType(type);

		param.setOptional(optional);

		return param;
	}
}
