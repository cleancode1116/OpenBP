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
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.ProcessItem;

/**
 * Finder that is only used if the refrences can only be in the nodes
 * e.g. ActivityFinder.
 *
 * @author Baumgartner Michael
 */
public abstract class NodeFinder extends FinderImpl
{
	//////////////////////////////////////////////////
	// @@ Finder implementation
	//////////////////////////////////////////////////

	/**
	 * @copy org.openbp.guiclient.model.item.itemfinder.Finder.findModelObjectInModel
	 */
	public List findModelObjectInModel(ModelObject mo, Model model)
	{
		List foundModelObjects = new ArrayList();
		Iterator processes = model.getItems(ItemTypes.PROCESS);
		while (processes.hasNext())
		{
			ProcessItem process = (ProcessItem) processes.next();
			Iterator nodes = process.getNodes();
			while (nodes.hasNext())
			{
				Node node = (Node) nodes.next();
				List modelObjects = findModelObjectInNode(node, mo);
				if (modelObjects.size() != 0)
					foundModelObjects.addAll(modelObjects);
			}
		}
		foundModelObjects.addAll(scanModel(model, mo));
		return foundModelObjects;
	}

	//////////////////////////////////////////////////
	// @@ Abstract method
	//////////////////////////////////////////////////

	/**
	 * Scan the node if it uses the model object
	 * @param node The node to scan
	 * @param mo The model object that references are searched
	 * @return List containing {@link ModelObject} object that match or an empty list
	 */
	protected abstract List findModelObjectInNode(Node node, ModelObject mo);

	//////////////////////////////////////////////////
	// @@ Template method
	//////////////////////////////////////////////////

	/**
	 * Method that enables the finder to scan the elements of the model too.
	 * @param model The model to scan
	 * @param core The model object to search for
	 * @return List containing {@link ModelObject} object that match or an empty list
	 */
	protected List scanModel(Model model, ModelObject core)
	{
		return new ArrayList();
	}
}
