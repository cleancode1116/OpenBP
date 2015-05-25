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
package org.openbp.guiclient.model.item.itemfilter;

import javax.swing.JComponent;

import org.openbp.common.generic.description.DisplayObject;
import org.openbp.common.icon.MultiIcon;
import org.openbp.core.model.item.Item;
import org.openbp.jaspira.plugin.Plugin;

/**
 * An item filter can be used in conjunction with the item filter manager ({@link ItemFilterMgr})
 * to filter sets of items.
 * The filter extends the DisplayObject interface. Its name will be used to manage the filter
 * and can be any arbitrary name. The display name (getDisplayName method) will appear as
 * button or menu item text. The description (getDescription) will be used as button or menu
 * tool tip.<br>
 * A filter may also provide a configuration component, which can be used to customize the filter.
 *
 * @author Heiko Erhardt
 */
public interface ItemFilter
	extends DisplayObject
{
	/**
	 * Gets the icon representing this filter.
	 * @nowarn
	 */
	public MultiIcon getIcon();

	/**
	 * Gets the user interface component that can be used to configure this filter.
	 *
	 * @return The component or null if the filter cannot be configured
	 */
	public JComponent getConfigurationComponent();

	/**
	 * Checks if the filter is active.
	 * @nowarn
	 */
	public boolean isActive();

	/**
	 * Activates or deactivates the filter.
	 * @nowarn
	 */
	public void setActive(boolean active);

	/**
	 * Gets the plugin this filter belongs to.
	 * @nowarn
	 */
	public Plugin getPlugin();

	/**
	 * Sets the plugin this filter belongs to.
	 * @nowarn
	 */
	public void setPlugin(Plugin plugin);

	/**
	 * Gets the item filter holder this filter belongs to.
	 * @nowarn
	 */
	public ItemFilterHolder getFilterManager();

	/**
	 * Sets the item filter holder this filter belongs to.
	 * @nowarn
	 */
	public void setFilterHolder(ItemFilterHolder filterHolder);

	/**
	 * Determines if an item is accepted by this filter.
	 *
	 * @param item Item to check
	 * @return
	 *		true	The item is accepted by the filter<br>
	 *		false	The filter rejects this item
	 */
	public boolean acceptsItem(Item item);
}
