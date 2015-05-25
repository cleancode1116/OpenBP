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
package org.openbp.jaspira.gui.interaction;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for usage by drop clients.
 *
 * @author Heiko Erhardt
 */
public class DropClientUtil
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private DropClientUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	/**
	 * Returns a list of all regions of the given client AND possible sub clients.
	 *
	 * @param client Drop client
	 * @param flavors List of data flavors to check
	 * @param data Transferable to import
	 * @param mouseEvent Mouse event that initiated the drag action
	 * @return A list of {@link DragAwareRegion} object or null if the drop client
	 * or one of its sub clients cannot satisfy at least one of the supplied data flavors
	 */
	public static List getAllDropRegions(InteractionClient client, List flavors, Transferable data, MouseEvent mouseEvent)
	{
		List result = null;

		List subClients = client.getSubClients();
		if (subClients != null)
		{
			int n = subClients.size();
			for (int i = 0; i < n; ++i)
			{
				InteractionClient subClient = (InteractionClient) subClients.get(i);

				List subRegions = subClient.getAllDropRegions(flavors, data, mouseEvent);
				if (subRegions != null)
				{
					if (result == null)
						result = new ArrayList();
					result.addAll(subRegions);
				}
			}
		}

		List regions = client.getDropRegions(flavors, data, mouseEvent);
		if (regions != null)
		{
			if (result == null)
				result = new ArrayList();
			result.addAll(regions);
		}

		return result;
	}

	/**
	 * Returns all importers which will be accepted at the given point by the given client
	 * or one of its sub clients.
	 *
	 * @param client Drop client
	 * @param p Current mouse position in screen coordinates
	 * @return A list of {@link Importer} objects or null
	 */
	public static List getAllImportersAt(InteractionClient client, Point p)
	{
		List result = null;

		List subClients = client.getSubClients();
		if (subClients != null)
		{
			int n = subClients.size();
			for (int i = 0; i < n; ++i)
			{
				InteractionClient subClient = (InteractionClient) subClients.get(i);

				List subImporters = subClient.getAllImportersAt(p);
				if (subImporters != null)
				{
					if (result == null)
						result = new ArrayList();
					result.addAll(subImporters);
				}
			}
		}

		List importers = client.getImportersAt(p);
		if (importers != null)
		{
			if (result == null)
				result = new ArrayList();
			result.addAll(importers);
		}

		return result;
	}

	/**
	 * Called when a dragging has been started.
	 *
	 * @param client Drop client
	 * @param transferable Transferable to be dragged
	 */
	public static void dragStarted(InteractionClient client, Transferable transferable)
	{
		List subClients = client.getSubClients();
		if (subClients != null)
		{
			int n = subClients.size();
			for (int i = 0; i < n; ++i)
			{
				InteractionClient subClient = (InteractionClient) subClients.get(i);

				subClient.dragStarted(transferable);
			}
		}
	}

	/**
	 * called when a dragging has ended.
	 *
	 * @param client Drop client
	 * @param transferable Transferable that has been dragged
	 */
	public static void dragEnded(InteractionClient client, Transferable transferable)
	{
		List subClients = client.getSubClients();
		if (subClients != null)
		{
			int n = subClients.size();
			for (int i = 0; i < n; ++i)
			{
				InteractionClient subClient = (InteractionClient) subClients.get(i);

				subClient.dragEnded(transferable);
			}
		}
	}
}
