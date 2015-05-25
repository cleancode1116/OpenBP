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
package org.openbp.guiclient.objectvalidators;

import java.util.Iterator;

import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.jaspira.propertybrowser.PropertyBrowser;

/**
 * Object validator for the {@link NodeSocket} class.
 * If the 'default socket' flag has been set for the given node socket, the validator will
 * clear the flag from the other sockets of the node the socket belongs to that have the
 * same type (entry/exit) as the given socket.
 * This ensures that there is always only one default entry or exit socket.
 *
 * @author Heiko Erhardt
 */
public class NodeSocketValidator extends ModelObjectValidator
{
	/**
	 * Validates the entire object before it will be saved.
	 *
	 * @param editedObject Edited object that contains the property
	 * @param pb Property browser that edits the object
	 * @return
	 *		true	The object value is valid and can be saved.<br>
	 *		false	The object value is invalid. In this case, the save operation should be aborted and
	 *				the focus should not be set outside the property browser.
	 */
	public boolean validateObject(Object editedObject, PropertyBrowser pb)
	{
		if (!super.validateObject(editedObject, pb))
		{
			return false;
		}

		NodeSocket editedSocket = (NodeSocket) editedObject;
		if (editedSocket.isDefaultSocket())
		{
			boolean isEntry = editedSocket.isEntrySocket();

			Node node = editedSocket.getNode();
			for (Iterator it = node.getSockets(); it.hasNext();)
			{
				NodeSocket socket = (NodeSocket) it.next();
				if (socket.isEntrySocket() != isEntry)
				{
					// Socket type does not match
					continue;
				}

				if (socket.isDefaultSocket())
				{
					// Clear the default socket flag if we are not iterating the edited socket
					// (note that we also have to check the original object because the property browser
					// clones the object)
					if (socket != editedSocket && socket != pb.getObject() && socket != pb.getOriginalObject())
					{
						socket.setDefaultSocket(false);
					}
				}
			}
		}

		return true;
	}
}
