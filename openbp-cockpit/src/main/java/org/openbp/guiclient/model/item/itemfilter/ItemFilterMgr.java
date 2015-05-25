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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.openbp.common.icon.FlexibleSize;
import org.openbp.core.model.item.Item;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraToolbar;
import org.openbp.jaspira.action.JaspiraToolbarButton;
import org.openbp.jaspira.plugin.Plugin;
import org.openbp.swing.layout.VerticalFlowLayout;
import org.openbp.swing.plaf.sky.SkyTheme;

/**
 * The item filter manager enables the filtering of items according to a
 * variety of filters ({@link ItemFilter}) that have been installed for this manager.
 *
 * It provides methods to check if an item is accepted by the filters and may
 * return components that can be used to affect the mode of operation of the filters.
 * An item is declined by the manager if one of the installed filters rejects the item
 * (i. e. logical and conjunction of the filters).
 *
 * @author Heiko Erhardt
 */
public class ItemFilterMgr
	implements ItemFilterHolder
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Filter list (contains {@link ItemFilter} objects) */
	protected List filterList;

	/** Filter listener */
	private ItemFilterListener filterListener;

	/** Plugin the item filter manager is associated with (to be used as event source) */
	private Plugin plugin;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Toolbar containing the filter buttons */
	private JaspiraToolbar toolbar;

	/** Panel that contains the various filter configuration components */
	private JComponent configPane;

	/** Initialization performed */
	private boolean initDone;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ItemFilterMgr()
	{
	}

	/**
	 * Constructor.
	 *
	 * @param plugin Plugin the item filter manager is associated with (to be used as event source)
	 */
	public ItemFilterMgr(Plugin plugin)
	{
		this.plugin = plugin;
	}

	/**
	 * Initializes the filter configuration components and the filter toolbar.
	 */
	protected void initializeComponents()
	{
		if (initDone)
			return;
		initDone = true;

		if (filterList != null)
		{
			int n = filterList.size();
			for (int i = 0; i < n; ++i)
			{
				ItemFilter filter = (ItemFilter) filterList.get(i);

				// Create the toolbar button
				if (toolbar == null)
				{
					toolbar = new JaspiraToolbar();
					toolbar.setIconSize(FlexibleSize.SMALL);
				}

				FilterToolbarAction action = new FilterToolbarAction(filter);
				action.setIcon(filter.getIcon());

				JaspiraToolbarButton button = new JaspiraToolbarButton(action);
				button.setSelected(filter.isActive());

				toolbar.add(button);
				button.invalidate();

				// Create the configuration component if available
				JComponent filterComponent = filter.getConfigurationComponent();
				if (filterComponent != null)
				{
					if (configPane == null)
					{
						configPane = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));
						configPane.setBackground(SkyTheme.COLOR_BACKGROUND_LIGHT);
						configPane.setVisible(false);
					}

					// Initially invisible
					filterComponent.setVisible(false);
					filterComponent.setBackground(SkyTheme.COLOR_BACKGROUND_LIGHT);
					filterComponent.setBorder(new EmptyBorder(0, 0, 2, 2));

					configPane.add(filterComponent);
				}
			}
		}
	}

	/**
	 * Gets the plugin the item filter manager is associated with (to be used as event source).
	 * @nowarn
	 */
	public Plugin getPlugin()
	{
		return plugin;
	}

	/**
	 * Sets the plugin the item filter manager is associated with (to be used as event source).
	 * @nowarn
	 */
	public void setPlugin(Plugin plugin)
	{
		this.plugin = plugin;
	}

	//////////////////////////////////////////////////
	// @@ Public methods
	//////////////////////////////////////////////////

	/**
	 * Determines if an item is accepted by the installed filters.
	 *
	 * @param item Item to check
	 * @return
	 * true: The item is accepted by all installed filters or no filters have been installed.<br>
	 * false: At least one of the installed filters has rejected the item.
	 */
	public boolean acceptsItem(Item item)
	{
		if (filterList != null)
		{
			int n = filterList.size();
			for (int i = 0; i < n; ++i)
			{
				ItemFilter filter = (ItemFilter) filterList.get(i);
				if (!filter.acceptsItem(item))
				{
					// Item rejected
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * (Re-)applies the filter.
	 * This method will be called after the status of a filter has changed.
	 *
	 * @param filter Filter that wishes to be applied or null
	 */
	public void apply(ItemFilter filter)
	{
		if (filterListener != null)
		{
			// Notify listeners
			filterListener.applyFilter(this, filter);
		}
	}

	/**
	 * Sets the filter listener.
	 * @nowarn
	 */
	public void setFilterListener(ItemFilterListener filterListener)
	{
		this.filterListener = filterListener;
	}

	/**
	 * Gets the filter toolbar.
	 * The toolbar contains a button for each installed filter, in the order they have been added
	 * to the manager. The function of each button is as follows:<br>
	 * If the filter is inactive, a click on the button will show the filter customization
	 * component if the filter provides one or otherwise simply activate the filter.<br>
	 * If the filter is active, it will be deactivated without showing any component.<br>
	 * The buttons are toggle buttons, so their state will indicate if a filter is active.
	 *
	 * @return The toolbar or null if no filters have been installed
	 */
	public JaspiraToolbar getFilterToolbar()
	{
		initializeComponents();
		return toolbar;
	}

	/**
	 * Gets the panel that contains the various filter configuration components.
	 *
	 * @return The configuration pane or null if there are no filter configuration components
	 */
	public JComponent getConfigurationPane()
	{
		initializeComponents();
		return configPane;
	}

	//////////////////////////////////////////////////
	// @@ Filter toolbar action class
	//////////////////////////////////////////////////

	/**
	 * The filter toggle action turns its associated item type filter on or off.
	 */
	private class FilterToolbarAction extends JaspiraAction
	{
		/** Item filter this action is associated with */
		private ItemFilter filter;

		/**
		 * Constructor.
		 *
		 * @param filter Item filter this action is associated with
		 */
		public FilterToolbarAction(ItemFilter filter)
		{
			// User the filter name/description/display name for the action
			super(filter);

			this.filter = filter;
		}

		public void actionPerformed(ActionEvent ae)
		{
			JButton button = (JButton) ae.getSource();

			JComponent filterComponent = filter.getConfigurationComponent();
			if (filterComponent != null)
			{
				// Toggle display of the configuration component
				boolean visible = filterComponent.isVisible();
				visible = !visible;

				filterComponent.setVisible(visible);

				if (!visible)
				{
					// Hiding the filter means deactivating it
					filter.setActive(false);
				}

				// Update the button status
				button.setSelected(visible);

				// Make the configuration pane visible if there is at least one filter component visible
				boolean isOneComponentVisible = false;
				Component [] comps = configPane.getComponents();
				for (int i = 0; i < comps.length; ++i)
				{
					if (comps [i].isVisible())
					{
						isOneComponentVisible = true;
						break;
					}
				}
				configPane.setVisible(isOneComponentVisible);
			}
			else
			{
				// Filter has no configuration component, so simply activate or deactivate it
				boolean active = button.isSelected();

				filter.setActive(!active);

				// Update the button status
				button.setSelected(filter.isActive());
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Item filter management
	//////////////////////////////////////////////////

	/**
	 * Adds a filter.
	 * @param filter The filter to add
	 */
	public void addFilter(ItemFilter filter)
	{
		if (filterList == null)
			filterList = new ArrayList();
		filterList.add(filter);
		filter.setFilterHolder(this);
	}

	/**
	 * Gets the filter list.
	 * @return A list of {@link ItemFilter} objects
	 */
	public List getFilterList()
	{
		return filterList;
	}
}
