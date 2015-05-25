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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.openbp.common.generic.propertybrowser.ObjectDescriptor;
import org.openbp.common.icon.FlexibleSize;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypeDescriptor;
import org.openbp.core.model.item.ItemTypeRegistry;
import org.openbp.guiclient.model.ModelConnector;
import org.openbp.guiclient.model.item.ItemIconMgr;
import org.openbp.guiclient.model.item.ItemUtil;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraToolbar;
import org.openbp.jaspira.action.JaspiraToolbarButton;
import org.openbp.jaspira.plugin.Plugin;
import org.openbp.swing.plaf.sky.SkyTheme;

/**
 * Item type filter.
 * The item type filter filters items according to their item type.
 * The user may select the types to be filtered from the supported types.
 * The filter will provide a toobar button for each supported item type in its
 * configuration component.
 *
 * @author Heiko Erhardt
 */
public class ItemTypeFilter extends AbstractItemFilter
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Supported item types */
	private String [] supportedItemTypes;

	/** List of filtered item types (contains strings) */
	private List filteredItemTypes;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Configuration component containing the item type toolbar */
	private JPanel configurationComponent;

	/** Action list (contains {@link JaspiraAction} objects) */
	private List actionList;

	/** Item type toolbar */
	private JaspiraToolbar toolbar;

	/** Prefix text for filter toolbar buttons */
	private String filterPrefix;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 * Adds all standard item types to the list of supported item types.
	 */
	public ItemTypeFilter()
	{
		this(null);
	}

	/**
	 * Constructor.
	 *
	 * @param supportedItemTypes List of supported item types<br>
	 * This this parameter is null, all standard item types will be added.
	 */
	public ItemTypeFilter(String [] supportedItemTypes)
	{
		setSupportedItemTypes(supportedItemTypes);

		filteredItemTypes = new ArrayList();

		if (filterPrefix == null)
		{
			filterPrefix = getItemFilterResource().getRequiredString("filterprefix");
		}
	}

	//////////////////////////////////////////////////
	// @@ ItemFilter implementation
	//////////////////////////////////////////////////

	/**
	 * Determines if an item is accepted by this filter.
	 *
	 * @param item Item to check
	 * @return
	 * true: The item is accepted by the filter<br>
	 * false: The filter rejects this item
	 */
	public boolean acceptsItem(Item item)
	{
		return acceptsItemType(item.getItemType());
	}

	/**
	 * Determines if a particular item type is accepted by this filter.
	 *
	 * @param itemType Item type to check
	 * @return
	 * true: The item type is accepted by the filter<br>
	 * false: The filter rejects this item type
	 */
	public boolean acceptsItemType(String itemType)
	{
		if (filteredItemTypes.size() == 0)
		{
			// No filter active, accept all
			return true;
		}

		if (containsItemType(itemType))
			return true;

		return false;
	}

	/**
	 * Gets the configuration component that can be used to configure the installed item filters.
	 *
	 * @return The component or null if the item filter does not provide one
	 */
	public JComponent getConfigurationComponent()
	{
		if (configurationComponent == null)
		{
			if (supportedItemTypes != null)
			{
				List actions = getActions();

				if (actions != null)
				{
					toolbar = new JaspiraToolbar();
					toolbar.setIconSize(FlexibleSize.SMALL);
					toolbar.setBackground(SkyTheme.COLOR_BACKGROUND_LIGHT);

					int n = actions.size();
					for (int i = 0; i < n; i++)
					{
						ItemTypeToggleAction action = (ItemTypeToggleAction) actions.get(i);
						String itemType = action.getItemTypeDescriptor().getItemType();

						JaspiraToolbarButton toggle = new JaspiraToolbarButton(action);
						toggle.setSelected(containsItemType(itemType));
						toggle.setBackground(SkyTheme.COLOR_BACKGROUND_LIGHT);

						toolbar.add(toggle);

						toggle.invalidate();
						toolbar.invalidate();
					}

					JLabel label = new JLabel(getItemFilterResource().getRequiredString("type"));
					label.setBorder(new EmptyBorder(0, 0, 0, 5));

					configurationComponent = new JPanel(new BorderLayout());

					configurationComponent.add(label, BorderLayout.WEST);
					configurationComponent.add(toolbar);
				}
			}
		}

		return configurationComponent;
	}

	/**
	 * Checks if the filter is active.
	 * The filter is active if it contains at least one filtered item type.
	 * @nowarn
	 */
	public boolean isActive()
	{
		return filteredItemTypes.size() > 0;
	}

	/**
	 * Activates or deactivates the filter.
	 * @nowarn
	 */
	public void setActive(boolean active)
	{
		if (!active)
		{
			if (filteredItemTypes.size() > 0)
			{
				filteredItemTypes.clear();
				apply();
			}

			List actions = getActions();
			if (actions != null)
			{
				int n = actions.size();
				for (int i = 0; i < n; i++)
				{
					ItemTypeToggleAction action = (ItemTypeToggleAction) actions.get(i);
					action.setSelected(false);
				}
			}
		}
	}

	/**
	 * Enables or disables the filter controls.
	 * @nowarn
	 */
	public void setEnabled(boolean enabled)
	{
		List actions = getActions();

		if (actions != null)
		{
			int n = actions.size();
			for (int i = 0; i < n; i++)
			{
				ItemTypeToggleAction action = (ItemTypeToggleAction) actions.get(i);
				action.setEnabled(enabled);
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the action list.
	 * @return A list of JaspiraAction objects or null
	 */
	public List getActions()
	{
		if (actionList == null)
		{
			if (supportedItemTypes != null)
			{
				actionList = new ArrayList();

				for (int i = 0; i < supportedItemTypes.length; i++)
				{
					String itemType = supportedItemTypes [i];

					ItemTypeDescriptor itd = ModelConnector.getInstance().getItemTypeDescriptor(itemType);

					ItemTypeToggleAction action = new ItemTypeToggleAction(getPlugin(), itd);

					String filterDescription = filterPrefix;
					ObjectDescriptor od = ItemUtil.obtainObjectDescriptor(itd);
					if (od != null)
					{
						filterDescription += " " + od.getDisplayName();
					}
					action.setDescription(filterDescription);
					action.setIcon(ItemIconMgr.getMultiIcon(ItemIconMgr.getInstance().getIcon(itemType, FlexibleSize.SMALL)));

					actionList.add(action);
				}
			}
		}

		return actionList;
	}

	/**
	 * Gets the supported item types.
	 * @nowarn
	 */
	public String [] getSupportedItemTypeInfos()
	{
		return supportedItemTypes;
	}

	/**
	 * Sets the supported item types.
	 * @nowarn
	 */
	public void setSupportedItemTypes(String [] supportedItemTypes)
	{
		if (supportedItemTypes == null)
		{
			supportedItemTypes = ModelConnector.getInstance().getItemTypes(ItemTypeRegistry.SKIP_MODEL | ItemTypeRegistry.SKIP_INVISIBLE);
		}
		this.supportedItemTypes = supportedItemTypes;
	}

	/**
	 * Gets the item type toolbar.
	 * @nowarn
	 */
	public JaspiraToolbar getToolbar()
	{
		return toolbar;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Determines if the filter contains the given item type.
	 *
	 * @param itemType Item type to check
	 * @return
	 * true: The item type has been added.<br>
	 * false: The item type filter does not contain the item type.
	 */
	public boolean containsItemType(String itemType)
	{
		int n = filteredItemTypes.size();
		for (int i = 0; i < n; ++i)
		{
			if (itemType.equals(filteredItemTypes.get(i)))
				return true;
		}

		return false;
	}

	/**
	 * Adds an item type to the filter.
	 *
	 * @param itemType Item type to add
	 */
	protected void addItemType(String itemType)
	{
		if (!filteredItemTypes.contains(itemType))
		{
			filteredItemTypes.add(itemType);
		}
	}

	/**
	 * Removes an item type from the filter.
	 *
	 * @param itemType Item type to remove
	 */
	protected void removeItemType(String itemType)
	{
		filteredItemTypes.remove(itemType);
	}

	//////////////////////////////////////////////////
	// @@ Action class
	//////////////////////////////////////////////////

	/**
	 * The filter toggle action turns its associated item type filter on or off.
	 */
	private class ItemTypeToggleAction extends JaspiraAction
	{
		/** Item type descriptor this filter action referes to */
		private ItemTypeDescriptor itd;

		/**
		 * Constructor.
		 * @param plugin Plugin that is associated with this filter
		 * @param itd Item type descriptor this filter action referes to
		 */
		public ItemTypeToggleAction(Plugin plugin, ItemTypeDescriptor itd)
		{
			super(ItemUtil.obtainObjectDescriptor(itd));

			// Use the sequence number of the descriptor as priority, so the actions will appear in the correct order
			setPriority(itd.getSequence());

			this.itd = itd;
		}

		public void actionPerformed(ActionEvent e)
		{
			JaspiraToolbarButton button = (JaspiraToolbarButton) e.getSource();

			JaspiraAction action = button.getJaspiraAction();

			// Toggle
			boolean selected = !action.isSelected();

			String itemType = itd.getItemType();
			if (selected)
				addItemType(itemType);
			else
				removeItemType(itemType);

			action.setSelected(selected);

			// Apply the new filter settings
			apply();
		}

		/**
		 * Gets the item type descriptor this filter action referes to.
		 * @nowarn
		 */
		public ItemTypeDescriptor getItemTypeDescriptor()
		{
			return itd;
		}
	}
}
