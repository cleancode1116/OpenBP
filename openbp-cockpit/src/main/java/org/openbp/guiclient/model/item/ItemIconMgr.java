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
package org.openbp.guiclient.model.item;

import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openbp.common.icon.FlexibleSize;
import org.openbp.common.icon.MultiIcon;
import org.openbp.common.icon.MultiImageIcon;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.type.ComplexTypeItem;
import org.openbp.core.model.item.type.DataTypeItem;

/**
 * The item icon manager manages and caches icons for particular item types.
 * All icons have to be registered using the {@link #registerIcon(String, Icon)} method.
 *
 * @author Jens Ferchland
 */
public final class ItemIconMgr
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Singleton instance */
	private static ItemIconMgr singletonInstance;

	/**
	 * Table of loaded icons.
	 * Maps icon names to ImageIcon objects.
	 * The map may contain also Boolean(false) objects as indicator that there
	 * was an attempt to load the icon that has failed (this prevents multiple
	 * server accesses for icons that do not exist).
	 * We use a hashtable for synchronization support here.
	 */
	private Hashtable iconMap;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance for this class.
	 * @nowarn
	 */
	public static synchronized ItemIconMgr getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new ItemIconMgr();

		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private ItemIconMgr()
	{
		iconMap = new Hashtable();
	}

	//////////////////////////////////////////////////
	// @@ Generic icon access
	//////////////////////////////////////////////////

	/**
	 * Returns the standard icon for the given item.
	 *
	 * @param item The item for which the icon should be retrieved
	 * @param preferredSize The preferred size of the icon in pixel
	 * @return The icon or null if not present
	 */
	public Icon getIcon(Item item, int preferredSize)
	{
		return getIcon(null, item, preferredSize);
	}

	/**
	 * Returns the standard icon for the given item and the given skin.
	 *
	 * @param skinName Name of the skin this icon should belong to; null for the default skin
	 * @param item The item for which the icon should be retrieved
	 * @param preferredSize The preferred size of the icon in pixel
	 * @return The icon or null if not present
	 */
	public Icon getIcon(String skinName, Item item, int preferredSize)
	{
		return getIcon(skinName, item.getModelObjectSymbolName(), preferredSize);
	}

	/**
	 * Returns an icon for a data type item that can be used to visualize parameters.
	 * The icon will reflect the data type of the type item.
	 *
	 * @param item The item for which the icon should be retrieved
	 * @param preferredSize The preferred size of the icon in pixel
	 * @return The icon
	 */
	public Icon getTypeIcon(DataTypeItem item, int preferredSize)
	{
		return getTypeIcon(null, item, preferredSize);
	}

	/**
	 * Returns an icon for a data type item and the given skin that can be used to visualize parameters.
	 * The icon will reflect the data type of the type item.
	 *
	 * @param skinName Name of the skin this icon should belong to; null for the default skin
	 * @param item The item for which the icon should be retrieved
	 * @param preferredSize The preferred size of the icon in pixel
	 * @return The icon
	 */
	public Icon getTypeIcon(String skinName, DataTypeItem item, int preferredSize)
	{
		Icon icon = null;

		if (item != null)
		{
			// First, check if there is a special icon for this type
			icon = getIcon(skinName, "Type" + item.getName(), preferredSize);
			if (icon != null)
			{
				return icon;
			}

			if (item instanceof ComplexTypeItem)
			{
				// For beans, try the standard bean icon
				icon = getIcon(skinName, "TypeBean", preferredSize);
				if (icon != null)
				{
					return icon;
				}
			}
		}

		// Try the default parameter type icon
		icon = getIcon(skinName, "TypeAny", preferredSize);
		if (icon != null)
		{
			return icon;
		}

		// This should lead to a result, finally
		icon = getIcon(skinName, "Type", preferredSize);
		if (icon != null)
		{
			return icon;
		}

		return null;
	}

	/**
	 * Returns an icon that has been registered with the icon model by its name.
	 *
	 * @param iconName Name of the icon
	 * @param preferredSize The preferred size of the icon in pixel
	 * @return The icon or null if no such icon could be found
	 */
	public Icon getIcon(String iconName, int preferredSize)
	{
		return getIcon(null, iconName, preferredSize);
	}

	/**
	 * Returns an icon that has been registered with the icon model by its name and the given skin.
	 *
	 * @param skinName Name of the skin this icon should belong to; null for the default skin
	 * @param iconName Name of the icon
	 * @param preferredSize The preferred size of the icon in pixel
	 * @return The icon or null if no such icon could be found
	 */
	public Icon getIcon(String skinName, String iconName, int preferredSize)
	{
		String key = skinName != null ? skinName + "." + iconName : iconName;

		Object o = iconMap.get(key);
		if (o instanceof MultiImageIcon)
		{
			// Multi size icon, get the right size
			MultiImageIcon mii = (MultiImageIcon) o;
			if (preferredSize != FlexibleSize.UNDETERMINED)
			{
				return mii.getIcon(preferredSize);
			}
			return mii;
		}
		else if (o instanceof ImageIcon)
		{
			// Single size, return directly
			return (ImageIcon) o;
		}

		// No matching icon
		return null;
	}

	/**
	 * Registers an icon with the icon model.
	 *
	 * @param name Name of the icon
	 * @param icon Icon to add
	 */
	public void registerIcon(String name, Icon icon)
	{
		iconMap.put(name, icon);
	}

	/**
	 * Registers an icon with the icon model for the given skin.
	 *
	 * @param skinName Name of the skin this icon should belong to; null for the default skin
	 * @param name Name of the icon
	 * @param icon Icon to add
	 */
	public void registerIcon(String skinName, String name, Icon icon)
	{
		String key = skinName != null ? skinName + "." + name : name;

		iconMap.put(key, icon);
	}

	//////////////////////////////////////////////////
	// @@ Static methods
	//////////////////////////////////////////////////

	/**
	 * Converts a regular icon to a multi icon.
	 * @param icon Icon to convert (if this is a MultiIcon itself, it will be returned directly)
	 * @return The multi icon or null if the icon is not a MultiIcon or an ImageIcon
	 */
	public static MultiIcon getMultiIcon(Icon icon)
	{
		if (icon instanceof ImageIcon)
			return new MultiImageIcon((ImageIcon) icon);

		if (icon instanceof MultiIcon)
			return (MultiIcon) icon;

		return null;
	}
}
