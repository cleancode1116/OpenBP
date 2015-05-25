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

import org.openbp.common.generic.description.DisplayObjectImpl;
import org.openbp.common.icon.MultiIcon;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.rc.ResourceCollectionMgr;
import org.openbp.guiclient.GUIClientConstants;
import org.openbp.jaspira.plugin.Plugin;

/**
 * Basic implementation of an item filter.
 *
 * This class expects the item filter resources in itemfilter/'name'.xml<br>
 * 'name' is the abbreviated filter name, i. e. the class name with 'org.openbp.' removed.
 *
 * @author Heiko Erhardt
 */
public abstract class AbstractItemFilter extends DisplayObjectImpl
	implements ItemFilter
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Active */
	private boolean active;

	/** Plugin this filter belongs to */
	private Plugin plugin;

	/** Item filter holder this filter belongs to */
	private ItemFilterHolder filterHolder;

	/** Resource of this filter */
	private ResourceCollection resourceCollection;

	/** Filter icon */
	private MultiIcon icon;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public AbstractItemFilter()
	{
		// Use the class name as internal filter name
		setName(getClass().getName());

		resourceCollection = ResourceCollectionMgr.getDefaultInstance().getResource(GUIClientConstants.RESOURCE_GUICLIENT, getClass());

		setDisplayName(resourceCollection.getRequiredString("title"));
		setDescription(resourceCollection.getOptionalString("description"));
		icon = (MultiIcon) resourceCollection.getRequiredObject("icon");
	}

	//////////////////////////////////////////////////
	// @@ ItemFilter implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the icon representing this filter.
	 * @nowarn
	 */
	public MultiIcon getIcon()
	{
		return icon;
	}

	/**
	 * Gets the user interface component that can be used to configure this filter.
	 *
	 * @return The component or null if the filter cannot be configured<br>
	 * The component should be a JPanel, which should also contain something like an 'Apply' button
	 * if necessary (otherwise the actions the user performs on the sub components of the panel
	 * should have a direct effect on the filter).
	 */
	public JComponent getConfigurationComponent()
	{
		return null;
	}

	/**
	 * Checks if the filter is active.
	 * @nowarn
	 */
	public boolean isActive()
	{
		return active;
	}

	/**
	 * Activates or deactivates the filter.
	 * @nowarn
	 */
	public void setActive(boolean active)
	{
		if (this.active != active)
		{
			this.active = active;
			apply();
		}
	}

	/**
	 * Gets the plugin this filter belongs to.
	 * @nowarn
	 */
	public Plugin getPlugin()
	{
		return plugin;
	}

	/**
	 * Sets the plugin this filter belongs to.
	 * @nowarn
	 */
	public void setPlugin(Plugin plugin)
	{
		this.plugin = plugin;
	}

	/**
	 * Gets the item filter holder this filter belongs to.
	 * @nowarn
	 */
	public ItemFilterHolder getFilterManager()
	{
		return filterHolder;
	}

	/**
	 * Sets the item filter holder this filter belongs to.
	 * @nowarn
	 */
	public void setFilterHolder(ItemFilterHolder filterHolder)
	{
		this.filterHolder = filterHolder;
		if (filterHolder != null && plugin == null)
		{
			setPlugin(filterHolder.getPlugin());
		}
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Applies the filter to the filter manager it belongs to.
	 * Should be called if the user has changed a filter configuration component.
	 */
	protected void apply()
	{
		if (filterHolder != null)
		{
			filterHolder.apply(this);
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the resource of this filter.
	 * @nowarn
	 */
	public ResourceCollection getItemFilterResource()
	{
		return resourceCollection;
	}
}
