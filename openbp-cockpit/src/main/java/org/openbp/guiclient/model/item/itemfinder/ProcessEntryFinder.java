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
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.FinalNode;
import org.openbp.core.model.item.process.InitialNode;
import org.openbp.core.model.item.process.MultiSocketNode;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.SubprocessNode;

/**
 * Find all subprocess nodes that use the entry, after that
 * was searched.
 *
 * @author Baumgartner Michael
 */
public class ProcessEntryFinder
	implements Finder
{
	/**
	 * @copy org.openbp.guiclient.model.item.itemfinder.Finder.findModelObjectInModel
	 */
	public List findModelObjectInModel(ModelObject mo, Model model)
	{
		List foundReferences = new ArrayList();
		InitialNode entry = (InitialNode) mo;
		ProcessItem process = entry.getProcess();
		Iterator modelProcess = model.getItems(ItemTypes.PROCESS);
		while (modelProcess.hasNext())
		{
			ProcessItem pi = (ProcessItem) modelProcess.next();
			foundReferences.addAll(findInProcess(process, entry, pi));
		}
		return foundReferences;
	}

	/**
	 * Scan the nodes in the process for a subprocess node.
	 * @param searchFor The process to search for
	 * @param entry The entry to search for
	 * @param scaned The process to scan for the subprocess
	 * @return List with found {@link ModelObject} references
	 */
	private List findInProcess(ProcessItem searchFor, InitialNode entry, ProcessItem scaned)
	{
		List foundReferences = new ArrayList();
		Iterator nodeList = scaned.getNodes();
		while (nodeList.hasNext())
		{
			Node node = (Node) nodeList.next();
			if (node instanceof SubprocessNode)
			{
				SubprocessNode subprocessNode = (SubprocessNode) node;
				ProcessItem subprocess = subprocessNode.getSubprocess();
				if (subprocess == null || !subprocess.getQualifier().matches(searchFor.getQualifier(), ModelQualifier.COMPARE_ALL))
					continue;
				NodeSocket socket = subprocessNode.getSocketByName(entry.getName());
				if (socket != null)
					foundReferences.add(socket);
			}

			if (scaned.getQualifier().matches(searchFor.getQualifier(), ModelQualifier.COMPARE_ALL))
			{
				// Search the continous nodes and exit sockets to the searched entry only in the process
				// with the initial node
				if (node instanceof FinalNode)
				{
					if (entry.getName().equals(((FinalNode) node).getJumpTarget()))
						foundReferences.add(node);
				}
				else if (node instanceof MultiSocketNode)
				{
					MultiSocketNode socketNode = (MultiSocketNode) node;
					Iterator exitSockets = socketNode.getSockets(false);
					while (exitSockets.hasNext())
					{
						NodeSocket exit = (NodeSocket) exitSockets.next();
						if (! exit.hasControlLinks() && exit.getName().equals(entry.getName()))
							foundReferences.add(exit);
					}
				}
			}
		}

		return foundReferences;
	}
}
