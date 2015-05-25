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
package org.openbp.guiclient.model.item.itemfinder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.core.model.Model;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.activity.ActivityItem;
import org.openbp.core.model.item.activity.ActivityParam;
import org.openbp.core.model.item.activity.ActivitySocket;
import org.openbp.core.model.item.process.MultiSocketNode;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.SingleSocketNode;
import org.openbp.core.model.item.type.ComplexTypeItem;
import org.openbp.core.model.item.type.DataMember;
import org.openbp.core.model.item.type.DataTypeItem;

/**
 * Finder implementation for datatype items.
 *
 * @author Baumgartner Michael
 */
public class DataTypeFinder extends FinderImpl
{
	//////////////////////////////////////////////////
	// @@ Finder implementation
	//////////////////////////////////////////////////

	/**
	 * @copy org.openbp.guiclient.model.item.itemfinder.Finder.findModelObjectInModel
	 */
	public List findModelObjectInModel(ModelObject mo, Model model)
	{
		DataTypeItem item = (DataTypeItem) mo;

		List foundModelObjects = new ArrayList();

		// Search all nodesockets for the datatype
		Iterator processes = model.getItems(ItemTypes.PROCESS);
		while (processes.hasNext())
		{
			ProcessItem process = (ProcessItem) processes.next();
			Iterator nodes = process.getNodes();
			while (nodes.hasNext())
			{
				Node node = (Node) nodes.next();
				List modelObjects = findInNode(node, item);
				if (modelObjects.size() != 0)
					foundModelObjects.addAll(modelObjects);
			}
		}

		// Search all Activity items
		Iterator activityItems = model.getItems(ItemTypes.ACTIVITY);
		while (activityItems.hasNext())
		{
			ActivityItem activity = (ActivityItem) activityItems.next();
			Iterator sockets = activity.getSockets();
			while (sockets.hasNext())
			{
				ActivitySocket activitySocket = (ActivitySocket) sockets.next();
				foundModelObjects.addAll(findInActivitySocket(activitySocket, item));
			}
		}

		// Now search the datatypes itself, e.g. complex data types
		Iterator dataTypes = model.getItems(ItemTypes.TYPE);
		while (dataTypes.hasNext())
		{
			DataTypeItem dt = (DataTypeItem) dataTypes.next();
			if (dt instanceof ComplexTypeItem)
			{
				foundModelObjects.addAll(findInComplexDataType((ComplexTypeItem) dt, item));
			}
		}
		return foundModelObjects;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Search the complex data type, its members and the base type if the match
	 * the item that is searched.
	 *
	 * @param complex The complex data type that is searched for the datatype
	 * @param item The datatype to search for
	 * @return List containing {@link ModelObject} object that match the datatype
	 */
	private List findInComplexDataType(ComplexTypeItem complex, DataTypeItem item)
	{
		List modelObjects = new ArrayList();
		if (complex == item)
			return modelObjects;

		// Check the base type
		DataTypeItem dti = complex.getBaseType();

		// Add the type to the list if found
		addIfMatch(complex, dti, item, modelObjects);

		// Check the members
		Iterator member = complex.getAllMembers();
		while (member.hasNext())
		{
			DataMember dm = (DataMember) member.next();
			DataTypeItem memberItem = dm.getDataType();
			addIfMatch(dm, memberItem, item, modelObjects);
		}
		return modelObjects;
	}

	/**
	 * Search the node if there is any parameter with the datatype that
	 * is searched.
	 *
	 * @param node The node that is scaned
	 * @param item The datatype to search for
	 * @return List containing {@link ModelObject} object that match the datatype
	 */
	private List findInNode(Node node, DataTypeItem item)
	{
		// Get the list of sockets from the node
		List socketList = new ArrayList();
		if (node instanceof MultiSocketNode)
			socketList = ((MultiSocketNode) node).getSocketList();
		else
			socketList.add(((SingleSocketNode) node).getSocket());

		List modelObjects = new ArrayList();
		for (int i = 0; i < socketList.size(); i++)
		{
			NodeSocket socket = (NodeSocket) socketList.get(i);
			modelObjects.addAll(findInNodeSocket(socket, item));
		}

		return modelObjects;
	}

	/**
	 * Search the socket for the parameter.
	 * @param socket The socket to search in
	 * @param item The item to search for
	 * @return List with found references containing {@link ModelObject} objects
	 */
	private List findInNodeSocket(NodeSocket socket, DataTypeItem item)
	{
		List foundObject = new ArrayList();
		Iterator parameter = socket.getParams();
		while (parameter.hasNext())
		{
			NodeParam param = (NodeParam) parameter.next();
			DataTypeItem dataType = param.getDataType();
			addIfMatch(param, dataType, item, foundObject);
		}
		return foundObject;
	}

	/**
	 * Search the socket for the parameter.
	 * @param socket The socket to search in
	 * @param item The item to search for
	 * @return List with found references containing {@link ModelObject} objects
	 */
	private List findInActivitySocket(ActivitySocket socket, DataTypeItem item)
	{
		List foundObject = new ArrayList();
		Iterator parameter = socket.getParams();
		while (parameter.hasNext())
		{
			ActivityParam param = (ActivityParam) parameter.next();
			DataTypeItem dataType = param.getDataType();
			addIfMatch(param, dataType, item, foundObject);
		}
		return foundObject;
	}
}
