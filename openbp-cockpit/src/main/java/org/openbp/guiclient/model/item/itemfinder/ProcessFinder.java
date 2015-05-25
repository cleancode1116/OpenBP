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
import java.util.List;

import org.openbp.core.model.Model;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.SubprocessNode;

/**
 * Finder implementation for process items.
 *
 * @author Baumgartner Michael
 */
public class ProcessFinder extends NodeFinder
{
	/**
	 * @copy org.openbp.guiclient.model.item.itemfinder.NodeFinder.findModelObjectInNode
	 */
	protected List findModelObjectInNode(Node node, ModelObject item)
	{
		List list = new ArrayList();
		if (node instanceof SubprocessNode)
		{
			addIfMatch(node, ((SubprocessNode) node).getSubprocess(), item, list);
		}
		return list;
	}

	/**
	 * @copy org.openbp.guiclient.model.item.itemfinder.NodeFinder.scanModel
	 */
	protected List scanModel(Model model, ModelObject core)
	{
		List found = super.scanModel(model, core);
		return found;
	}
}
