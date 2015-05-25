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
package org.openbp.core.model.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openbp.common.CollectionUtil;
import org.openbp.common.io.xml.XMLDriver;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.resource.ResourceMgr;
import org.openbp.common.resource.ResourceMgrException;
import org.openbp.common.util.SortingArrayList;
import org.openbp.core.CoreConstants;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.ModelException;
import org.springframework.core.io.Resource;

/**
 * Registry that holds a list of item types.
 *
 * @author Heiko Erhardt
 */
public class ItemTypeRegistry
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Flag for {@link #getItemTypes}: All item types */
	public static final int ALL_TYPES = 0;

	/** Flag for {@link #getItemTypes}: Skip the model type descriptor */
	public static final int SKIP_MODEL = (1 << 0);

	/** Flag for {@link #getItemTypes}: Skip invisible types (e. g. placeholders) */
	public static final int SKIP_INVISIBLE = (1 << 1);

	//////////////////////////////////////////////////
	// @@ Private data
	//////////////////////////////////////////////////

	/** Type list (contains {@link ItemTypeDescriptor} objects) */
	private final List typeList;

	/** Hash table mapping names (Strings) to item types ({@link ItemTypeDescriptor} objects) */
	private final Map nameMap;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ItemTypeRegistry()
	{
		nameMap = new HashMap();
		typeList = new SortingArrayList();
	}

	/**
	 * Adds an item type to the registry.
	 *
	 * @param itd Item type descriptor
	 */
	public void addItemTypeDescriptor(ItemTypeDescriptor itd)
	{
		String itemType = itd.getItemType();

		if (nameMap.get(itemType) == null)
		{
			// Make sure all classes and the object descriptor are resolved
			try
			{
				itd.initialize();
			}
			catch (ModelException e)
			{
				// Some class could not be loaded, log and ignore this type
				LogUtil.error(getClass(), "Error initializing item type registry.", e);
				return;
			}

			nameMap.put(itemType, itd);

			typeList.add(itd);
		}
	}

	/**
	 * Initializes the standard item type registry.
	 *
	 * @throws OpenBPException If there were errors reading the standard item type descriptors
	 */
	public void loadStandardItemTypeDescriptors()
	{
		// Add all item types in the server/config/componenttype directory
		loadItemTypeDescriptors(CoreConstants.FOLDER_COMPONENTTYPE);

		if (getItemTypeDescriptors(ItemTypeRegistry.SKIP_MODEL | ItemTypeRegistry.SKIP_INVISIBLE) == null)
			throw new ModelException("Initialization", "No standard component types present in resource location '"
				+ CoreConstants.FOLDER_COMPONENTTYPE + "'");
	}

	/**
	 * Initializes the standard item type registry.
	 *
	 * @param resourcePath Directory containing the item type descriptor xml files
	 * @throws OpenBPException If there were errors reading the standard item type descriptors
	 */
	public void loadItemTypeDescriptors(String resourcePath)
	{
		ResourceMgr resMgr = ResourceMgr.getDefaultInstance();
		String resourcePattern = resourcePath + "/*.xml";
		Resource[] resources = null;

		try
		{
			resources = resMgr.findResources(resourcePattern);
		}
		catch (ResourceMgrException e)
		{
			throw new ModelException("MissingItemTypeDescriptors", "No item type files found matching '" + resourcePattern + "'.");
		}

		if (resources.length == 0)
			throw new ModelException("MissingItemTypeDescriptors", "No item type files found matching '" + resourcePattern + "'.");

		XMLDriver driver = XMLDriver.getInstance();
		ItemTypeDescriptor itd;

		for (int i = 0; i < resources.length; i++)
		{
			// Deserialize item descriptor file
			try
			{
				itd = (ItemTypeDescriptor) driver.deserializeResource(ItemTypeDescriptor.class, resources[i]);
			}
			catch (XMLDriverException pe)
			{
				throw new ModelException("Initialization", "Error reading component type descriptor resource '" + resources[i].getDescription()
					+ "': " + pe.getMessage());
			}

			// Add the item to the model
			addItemTypeDescriptor(itd);
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the list of item types (see the constants of the {@link ItemTypes} class).
	 * The list does not contain invisible item types (like the placeholder).
	 * @param mode  {@link #ALL_TYPES} / {@link #SKIP_MODEL}|{@link #SKIP_INVISIBLE}
	 * @nowarn
	 */
	public String[] getItemTypes(int mode)
	{
		List list = getTypeList(mode);

		int n = list.size();
		String[] types = new String[n];
		for (int i = 0; i < n; ++i)
		{
			types[i] = ((ItemTypeDescriptor) list.get(i)).getItemType();
		}

		return types;
	}

	/**
	 * Gets the item type descriptors.
	 * The list does not contain invisible item types (like the placeholder).
	 * @param mode  {@link #ALL_TYPES} / {@link #SKIP_MODEL}|{@link #SKIP_INVISIBLE}
	 * @return A list of {@link ItemTypeDescriptor} objects or null
	 */
	public ItemTypeDescriptor[] getItemTypeDescriptors(int mode)
	{
		List list = getTypeList(mode);
		return (ItemTypeDescriptor[]) CollectionUtil.toArray(list, ItemTypeDescriptor.class);
	}

	/**
	 * Gets the item type descriptor for a particular type.
	 * @param type The item type
	 * @return The item type descriptor or null if no such type can be found in the registry
	 */
	public ItemTypeDescriptor getItemTypeDescriptor(String type)
	{
		return nameMap != null ? (ItemTypeDescriptor) nameMap.get(type) : null;
	}

	/**
	 * Creates a customized list of item type descriptors.
	 *
	 * @param mode  {@link #ALL_TYPES} / {@link #SKIP_MODEL}|{@link #SKIP_INVISIBLE}
	 * @return The list
	 */
	private List getTypeList(int mode)
	{
		if (mode == 0)
			return typeList;

		ArrayList list = new ArrayList();

		int n = typeList.size();
		for (int i = 0; i < n; ++i)
		{
			ItemTypeDescriptor itd = (ItemTypeDescriptor) typeList.get(i);

			if ((mode & SKIP_MODEL) != 0 && itd.getItemType().equals(ItemTypes.MODEL))
				continue;

			if ((mode & SKIP_INVISIBLE) != 0 && ! itd.isVisible())
				continue;

			list.add(itd);
		}

		return list;
	}
}
