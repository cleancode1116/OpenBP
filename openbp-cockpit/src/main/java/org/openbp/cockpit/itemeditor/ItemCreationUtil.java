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
package org.openbp.cockpit.itemeditor;

import org.openbp.cockpit.modeler.skins.SkinMgr;
import org.openbp.core.CoreConstants;
import org.openbp.core.model.Model;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypeDescriptor;
import org.openbp.core.model.item.activity.ActivityItem;
import org.openbp.core.model.item.activity.ActivitySocket;
import org.openbp.core.model.item.activity.ActivitySocketImpl;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessUtil;
import org.openbp.guiclient.model.ModelConnector;
import org.openbp.guiclient.model.item.ItemEditor;
import org.openbp.guiclient.model.item.ItemEditorRegistry;

/**
 * Item creation utilities.
 *
 * @author Heiko Erhardt
 */
public class ItemCreationUtil
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private ItemCreationUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Creates and initializes a new item.
	 *
	 * @param model Model the item shall belong to
	 * @param suggestedName Suggested name or null for a default item-type-specific name.
	 * The method guarantees that the generated name will be unique within the items of this type of this model.
	 * @param suggestedDisplayName Suggested display name or null
	 * @param itemType Type of the item to create
	 * @param processType Process type in case of a process item
	 * @return The new item
	 */
	public static Item createItem(Model model, String suggestedName, String suggestedDisplayName, String itemType, String processType)
	{
		ItemTypeDescriptor itd = ModelConnector.getInstance().getItemTypeDescriptor(itemType);
		return createItem(model, suggestedName, suggestedDisplayName, itd, processType);
	}

	/**
	 * Creates and initializes a new item.
	 *
	 * @param itd Type of the item to create
	 * @param suggestedName Suggested name or null for a default item-type-specific name.
	 * The method guarantees that the generated name will be unique within the items of this type of this model.
	 * @param suggestedDisplayName Suggested display name or null
	 * @param processType Process type in case of a process item
	 * @return The new item
	 */
	public static Item createItem(Model model, String suggestedName, String suggestedDisplayName, ItemTypeDescriptor itd, String processType)
	{
		Item item = itd.getItemFactory().createItem(model, suggestedName, suggestedDisplayName);

		if (item instanceof ProcessItem)
		{
			ProcessItem process = (ProcessItem) item;

			// Initialize some process properties from the default skin settings
			SkinMgr.getInstance().getDefaultSkin().initalizeNewProcess(process);

			// Supply the process type
			process.setProcessType(processType);
		}

		// Reinitialize the item's standard configuration now that we have fixed the process type
		ItemCreationUtil.setupStandardConfiguration(item);

		ItemEditor editor = ItemEditorRegistry.getInstance().lookupItemEditor(itd.getItemType());
		if (editor != null)
		{
			// Edit the item
			item = editor.openItem(item, EditedItemStatus.NEW);
		}
		else
		{
			if (!ModelConnector.getInstance().saveItem(item, true))
				item = null;
		}

		return item;
	}

	public static void setupStandardConfiguration(Item item)
	{
		if (item instanceof ProcessItem)
		{
			setupProcessStandardConfiguration((ProcessItem) item);
		}
		else if (item instanceof ActivityItem)
		{
			setupActionStandardConfiguration((ActivityItem) item);
		}
	}

	private static void setupProcessStandardConfiguration(ProcessItem process)
	{
		ProcessUtil.setupProcessStandardConfiguration(process);
	}

	private static void setupActionStandardConfiguration(ActivityItem item)
	{
		ActivitySocket socket = new ActivitySocketImpl();
		socket.setName(CoreConstants.SOCKET_IN);
		socket.setEntrySocket(true);
		socket.setDefaultSocket(true);
		item.addSocket(socket);

		socket = new ActivitySocketImpl();
		socket.setName(CoreConstants.SOCKET_OUT);
		socket.setEntrySocket(false);
		socket.setDefaultSocket(true);
		item.addSocket(socket);
	}
}
