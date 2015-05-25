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
package org.openbp.jaspira.gui.plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openbp.common.icon.FlexibleSize;
import org.openbp.common.icon.MultiIcon;
import org.openbp.common.util.ToStringHelper;
import org.openbp.jaspira.gui.interaction.BasicDragReactor;
import org.openbp.jaspira.gui.interaction.BasicDropRegion;
import org.openbp.jaspira.gui.interaction.BasicDropRegionId;
import org.openbp.jaspira.gui.interaction.DragAwareRegion;
import org.openbp.jaspira.gui.interaction.DragInitiator;
import org.openbp.jaspira.gui.interaction.DragOrigin;
import org.openbp.jaspira.gui.interaction.DropClientUtil;
import org.openbp.jaspira.gui.interaction.InteractionClient;
import org.openbp.jaspira.gui.interaction.RectangleSegment;
import org.openbp.jaspira.plugin.Plugin;
import org.openbp.jaspira.plugin.PluginMgr;
import org.openbp.jaspira.plugin.PluginState;
import org.openbp.jaspira.util.StandardFlavors;
import org.openbp.swing.plaf.sky.ShadowBorder;

/**
 * A tabbed plugin container displays plugins in a combination of tabbed panes and tabs themselves.
 * It supports DnD of tabs to other plugin containers.
 *
 * @author Stephan Moritz
 */
public class TabbedPluginContainer extends JPanel
	implements PluginContainer, InteractionClient, DragOrigin, ChangeListener
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Constants
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Contains the actions for selecting a given tab. Can be static since the
	 * corresponding ActionEvent contains the source.
	 */
	public static final NumberAction [] numberActions;

	static
	{
		numberActions = new NumberAction [10];

		numberActions [0] = new NumberAction(9);

		for (int i = 1; i < 10; i++)
		{
			numberActions [i] = new NumberAction(i - 1);
		}
	}

	/** Action for select previous tab. */
	// private static final Action leftAction = new ChangeAction (-1);
	/** Action for select next tab. */
	// private static final Action rightAction = new ChangeAction (1);
	/** The empty plugin used if the 'solid' property is set and no other plugins are to present */
	private static VisiblePlugin emptyPlugin = new AbstractVisiblePlugin()
	{
		/**
		 * No resource for this, so this does nothing.
		 */
		protected void initializeResources()
		{
		}

		/**
		 * Does nothing.
		 */
		protected void initializeComponents()
		{
		}

		/**
		 * Cannot be dragged.
		 */
		public boolean canDrag()
		{
			return false;
		}

		public String getResourceCollectionContainerName()
		{
			return null;
		}
	};

	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** List of plugins shown by this container (contains {@link VisiblePlugin} objects) */
	private List plugins;

	/** The TabbedPane which holds the actual data. */
	protected JTabbedPane tabbedPane;

	/** Flag if the container is solid, i\.e\. doesn't remove itself if it is empty */
	private boolean solid;

	/** Flag if the container should always show tabs, even if only one plugin is showed */
	private boolean alwaysTabs;

	/** No focus on selection change */
	private boolean preventFocusOnSelectionChange;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public TabbedPluginContainer()
	{
		super(new BorderLayout());

		setBorder(new ShadowBorder());

		plugins = new ArrayList(4);

		initKeySupport();
	}

	/**
	 * Constructor.
	 *
	 * @param plugin Plugin to add to the container
	 */
	public TabbedPluginContainer(VisiblePlugin plugin)
	{
		this();

		addPlugin(plugin);
	}

	/**
	 * Constructor.
	 *
	 * @param state Creates a container that contains a new plugin that will be
	 * constructed from the given plugin state using the {@link PluginMgr}
	 */
	public TabbedPluginContainer(PluginState state)
	{
		this();

		addPlugin(PluginMgr.getInstance().createVisibleInstance(state, null));
	}

	/**
	 * Returns a string representation of this object.
	 * @nowarn
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "plugins");
	}

	/**
	 * Install key support for the plugin.
	 */
	private void initKeySupport()
	{
		ActionMap am = getActionMap();
		InputMap im = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		/* CTRL-LEFT/RIGHT commented out due to conflict with standard behavior of CTRL-LEFT/RIGHT in text field
		 am.put ("left", leftAction);
		 am.put ("right", rightAction);

		 im.put (KeyStroke.getKeyStroke ("control released RIGHT"), "right");
		 im.put (KeyStroke.getKeyStroke ("control released LEFT"), "left");
		 */
		for (int i = 0; i < 10; i++)
		{
			Integer next = Integer.valueOf(i);

			am.put(next, numberActions [i]);
			im.put(KeyStroke.getKeyStroke("control released " + i), next);
		}
	}

	/**
	 * Creates the tab pane if not done yet.
	 */
	protected void createTabPane()
	{
		if (tabbedPane == null)
		{
			tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
			DragInitiator.makeDraggable(tabbedPane, this);
			tabbedPane.addChangeListener(this);
		}
	}

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/**
	 * Gets the flag if the container is solid, i\.e\. doesn't remove itself if it is empty.
	 * @nowarn
	 */
	public boolean isSolid()
	{
		return solid;
	}

	/**
	 * Sets the flag if the container is solid, i\.e\. doesn't remove itself if it is empty.
	 * @nowarn
	 */
	public void setSolid(boolean solid)
	{
		if (this.solid != solid)
		{
			this.solid = solid;
			synchronizeComponents(null, true);
		}
	}

	/**
	 * Gets the flag if the container should always show tabs, even if only one plugin is showed.
	 * @nowarn
	 */
	public boolean isAlwaysTabs()
	{
		return alwaysTabs;
	}

	/**
	 * Sets the flag if the container should always show tabs, even if only one plugin is showed.
	 * @nowarn
	 */
	public void setAlwaysTabs(boolean alwaysTabs)
	{
		if (this.alwaysTabs != alwaysTabs)
		{
			this.alwaysTabs = alwaysTabs;
			synchronizeComponents(null, true);
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Adding and removal of plugins
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Adds all Plugins of the given Container to this pane.
	 */
	public void addContainer(PluginContainer pc)
	{
		for (Iterator iter = pc.getPlugins().iterator(); iter.hasNext();)
		{
			VisiblePlugin plugin = (VisiblePlugin) iter.next();

			addPlugin(plugin);
		}
	}

	/**
	 * Adds a plugin as last tab and show it.
	 */
	public void addPlugin(VisiblePlugin p)
	{
		addPlugin(p, true);
	}

	/**
	 * Adds a plugin as the last tab.
	 * If activateplugin is true the plugin will be shown.
	 */
	public void addPlugin(VisiblePlugin p, boolean activateplugin)
	{
		if (p == null)
			return;

		JaspiraPage page = getPage();
		if (page != null)
		{
			// Make the page the parent of the plugin
			p.setParentPlugin(page);
		}

		plugins.add(p);
		synchronizeComponents(activateplugin ? p : null, true);
	}

	/**
	 * Adds a plugin to a given slot, i\.e\. at a certain tabindex.
	 * Calling this should only be necessary during initialization.
	 */
	public void addPlugin(VisiblePlugin p, int slot)
	{
		if (slot == -1)
		{
			addPlugin(p);
			return;
		}

		JaspiraPage page = getPage();
		if (page != null)
		{
			// Make the page the parent of the plugin
			p.setParentPlugin(page);
		}

		plugins.add(slot, p);
		synchronizeComponents(p, true);
	}

	/**
	 * Hand the slice request up to the parent.
	 * Should never be called.
	 * @param constraint {@link PluginContainer#NORTH}/{@link PluginContainer#SOUTH}/{@link PluginContainer#EAST}/{@link PluginContainer#WEST}
	 */
	public void sliceContainer(PluginContainer toInsert, PluginContainer currentContainer, String constraint)
	{
		getParentContainer().sliceContainer(toInsert, currentContainer, constraint);
	}

	/**
	 * Remove a plugin from this container.
	 */
	public void removePlugin(VisiblePlugin p)
	{
		plugins.remove(p);
		synchronizeComponents(null, false);
	}

	/**
	 * Synchronizes the components of the container with the list of plugins.
	 *
	 * @param pluginToActivate Plugin to active after the synchronization or null
	 * @param preventFocusChange
	 *		true	Does not change the currently focused plugin.<br>
	 *		false	Changes the focused plugin to the currently selected plugin.
	 */
	protected void synchronizeComponents(VisiblePlugin pluginToActivate, boolean preventFocusChange)
	{
		// We don't want the state change listener to respond to tab changes, so we disable it temporarily.
		boolean oldPreventFocusOnSelectionChange = preventFocusOnSelectionChange;
		preventFocusOnSelectionChange = preventFocusChange;

		try
		{
			Component containerComponent = getComponentCount() > 0 ? getComponent(0) : null;

			int nPlugins = plugins.size();
			if (nPlugins == 0)
			{
				// Never focus an empty container
				preventFocusOnSelectionChange = true;

				if (tabbedPane != null)
					tabbedPane.removeAll();

				if (!solid)
				{
					// Container is empty and not solid, so remove it if we can
					PluginContainer parent = getParentContainer();

					if (parent instanceof PluginDivider)
					{
						// If we are placed inside of a PluginDivider
						// We can remove ourselves
						((PluginDivider) parent).removeClient(this);
						focusParent();
						return;
					}
				}

				// Display the empty plugin as entire container content,
				PluginPanel emptyPanel = emptyPlugin.getPluginPanel();
				if (containerComponent != emptyPanel)
				{
					removeAll();
					add(emptyPanel);
					repaint();
				}

				focusParent();
				return;
			}

			if (nPlugins == 1 && !alwaysTabs)
			{
				if (tabbedPane != null)
					tabbedPane.removeAll();

				// Display the single plugin as entire container content
				PluginPanel pluginPanel = ((VisiblePlugin) plugins.get(0)).getPluginPanel();
				if (containerComponent != pluginPanel)
				{
					removeAll();
					add(pluginPanel);
					repaint();
				}
				setSelectedPlugin(pluginPanel.getPlugin());
				return;
			}

			// we have to display the list of plugins in the tabbed pane;
			// Synchronize it with the list.

			createTabPane();
			int oldIndex = tabbedPane.getSelectedIndex();

			// First remove all plugins from the tab that are not in the list
			for (int iTab = 0; iTab < tabbedPane.getTabCount(); ++iTab)
			{
				PluginPanel pp = (PluginPanel) tabbedPane.getComponentAt(iTab);
				VisiblePlugin plugin = pp.getPlugin();
				if (!plugins.contains(plugin))
				{
					// The plugin at the current tab position is part of the plugin list, so remove it
					tabbedPane.remove(pp);
				}
			}

			// Now add the plugins that are not yet displayed in the tab
			for (int iPlugin = 0; iPlugin < nPlugins; ++iPlugin)
			{
				VisiblePlugin plugin = (VisiblePlugin) plugins.get(iPlugin);
				PluginPanel pp = plugin.getPluginPanel();

				PluginPanel ppTab = iPlugin < tabbedPane.getTabCount() ? (PluginPanel) tabbedPane.getComponentAt(iPlugin) : null;
				if (ppTab == pp)
				{
					// Plugin already at the right place
					continue;
				}

				// The plugin at the current tab position does not match the plugin at the current position, so insert it.
				tabbedPane.insertTab(plugin.getTitle(), plugin.getIcon().getIcon(FlexibleSize.SMALL), pp, plugin.getDescription(), iPlugin);
			}

			if (containerComponent != tabbedPane)
			{
				// Add the tabbed pane as container component
				removeAll();
				add(tabbedPane);
			}

			if (pluginToActivate == null)
			{
				if (oldIndex < 0)
				{
					pluginToActivate = (VisiblePlugin) plugins.get(0);
				}
				else if (oldIndex >= nPlugins)
				{
					pluginToActivate = (VisiblePlugin) plugins.get(nPlugins - 1);
				}
				else
				{
					pluginToActivate = (VisiblePlugin) plugins.get(oldIndex);
				}
			}
			setSelectedPlugin(pluginToActivate);

			repaint();
		}
		finally
		{
			preventFocusOnSelectionChange = oldPreventFocusOnSelectionChange;
		}
	}

	/**
	 * Focuses the parent plugin of this container.
	 */
	private void focusParent()
	{
		VisiblePlugin parentPlugin = AbstractVisiblePlugin.getPluginFromComponentHierarchy(this);
		if (parentPlugin != null)
		{
			parentPlugin.focusPlugin();
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Plugin access
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the parent Container of this tabbed pane (always a {@link PluginDivider}).
	 */
	public PluginContainer getParentContainer()
	{
		Component c = getParent();

		while (!(c instanceof PluginDivider) && c != null)
		{
			c = c.getParent();
		}

		return (PluginDivider) c;
	}

	/**
	 * Returns the plugin that is currently visible.
	 * @nowarn
	 */
	public VisiblePlugin getSelectedPlugin()
	{
		if (tabbedPane != null && tabbedPane.getTabCount() > 0)
		{
			PluginPanel pp = (PluginPanel) tabbedPane.getSelectedComponent();
			if (pp != null)
				return pp.getPlugin();
		}
		return plugins.size() > 0 ? (VisiblePlugin) plugins.get(0) : null;
	}

	/**
	 * Makes the given plugin visible.
	 *
	 * @param plugin Plugin to show
	 */
	public void setSelectedPlugin(VisiblePlugin plugin)
	{
		if (getNumberOfPlugins() > 1)
		{
			boolean oldPreventFocusOnSelectionChange = preventFocusOnSelectionChange;
			preventFocusOnSelectionChange = true;
			tabbedPane.setSelectedComponent(plugin.getPluginPanel());
			preventFocusOnSelectionChange = oldPreventFocusOnSelectionChange;
		}
	}

	/**
	 * Returns the number of plugins in this container.
	 * @nowarn
	 */
	public int getNumberOfPlugins()
	{
		return plugins.size();
	}

	/**
	 * Returns the list of plugins held by this container.
	 * @return A list of {@link VisiblePlugin} objects
	 */
	public List getPlugins()
	{
		return plugins;
	}

	/**
	 * Returns a list containing the currently visible plugin.
	 * @return A list of {@link VisiblePlugin} objects or null
	 */
	public List getVisiblePlugins()
	{
		Plugin p = getSelectedPlugin();
		if (p != null)
		{
			ArrayList l = new ArrayList(1);
			l.add(p);
			return l;
		}
		return null;
	}

	/**
	 * Returns the currently active plugin, which is incidently the visible plugin.
	 */
	public VisiblePlugin getActivePlugin()
	{
		return getSelectedPlugin();
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ ChangeListener implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Request the focus for the newly activated plugin on tab change by the user.
	 * @nowarn
	 */
	public void stateChanged(ChangeEvent e)
	{
		// We want this listener to respond to user actions only, so we disable it temporary
		// when changing the selected tab by the program. Check if this is the case.
		if (!preventFocusOnSelectionChange)
		{
			VisiblePlugin p = getSelectedPlugin();
			if (p != null)
			{
				p.focusPlugin();
			}
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ DropReceiver
	/////////////////////////////////////////////////////////////////////////

	/**
	 * We do not react to any drag events per default.
	 *
	 * @param transferable Transferable to be dragged
	 */
	public void dragStarted(Transferable transferable)
	{
		DropClientUtil.dragStarted(this, transferable);
	}

	/**
	 * We do not react to any drag events per default.
	 *
	 * @param transferable Transferable that has been dragged
	 */
	public void dragEnded(Transferable transferable)
	{
		DropClientUtil.dragEnded(this, transferable);
	}

	/**
	 * We do not react to any action events per default.
	 */
	public void dragActionTriggered(Object regionId, Point p)
	{
		if ("tabs".equals(regionId))
		{
			int index = tabbedPane.indexAtLocation(p.x, p.y);

			if (index != -1)
			{
				boolean oldPreventFocusOnSelectionChange = preventFocusOnSelectionChange;
				preventFocusOnSelectionChange = true;
				tabbedPane.setSelectedIndex(index);
				preventFocusOnSelectionChange = oldPreventFocusOnSelectionChange;

				ApplicationBase.getInstance().getActiveFrame().getDragDropPane().regionsInvalidated();
			}
		}
	}

	/**
	 * Standard regions for a plugin container are regions at the four corners as well as the on the plugin itself.
	 *
	 * In order to register your own zones, do not override this, override getUserRegions () instead.
	 *
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getDropRegions(List, Transferable, MouseEvent)
	 */
	public List getDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		List result = new ArrayList();

		// Tabswitcher
		if (tabbedPane != null && tabbedPane.getTabCount() > 0)
		{
			// We need the coordinates of the tabs... bad way...
			Rectangle shape = SwingUtilities.getLocalBounds(tabbedPane);

			int height = tabbedPane.getSelectedComponent().getBounds().height;
			shape.y += height;
			shape.height -= height;

			// We have tabs, we generate a tabswitch region
			DragAwareRegion region = new BasicDragReactor("tabs", this, shape, tabbedPane, 1000);

			result.add(region);
		}

		// Our standardRegions only react to pluginStatesFlavor, so
		// this is what we check for.
		if (flavors.contains(StandardFlavors.PLUGIN) || flavors.contains(StandardFlavors.PLUGIN_STATE))
		{
			// Create and add our standard regions
			BasicDropRegion region;

			// Identifier for NORTH, this is DropCLient, shape, framecolor black, no Stroke,
			// Fill Red, default cursor, no overlay, copy and move actions, this component.
			region = new BasicDropRegion(REGION_NORTH, this, new RectangleSegment(this, 15, PluginContainer.NORTH), this);
			region.setFrameColor(Color.BLACK);
			region.setPaint(Color.GREEN);
			result.add(region);

			region = new BasicDropRegion(REGION_SOUTH, this, new RectangleSegment(this, 15, PluginContainer.SOUTH), this);
			region.setFrameColor(Color.BLACK);
			region.setPaint(Color.GREEN);
			result.add(region);

			region = new BasicDropRegion(REGION_WEST, this, new RectangleSegment(this, 15, PluginContainer.WEST), this);
			region.setFrameColor(Color.BLACK);
			region.setPaint(Color.GREEN);
			result.add(region);

			region = new BasicDropRegion(REGION_EAST, this, new RectangleSegment(this, 15, PluginContainer.EAST), this);
			region.setFrameColor(Color.BLACK);
			region.setPaint(Color.GREEN);
			result.add(region);
		}

		return result;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getAllDropRegions(List, Transferable, MouseEvent)
	 */
	public final List getAllDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		return DropClientUtil.getAllDropRegions(this, flavors, data, mouseEvent);
	}

	public List getImportersAt(Point p)
	{
		return null;
	}

	public List getAllImportersAt(Point p)
	{
		return DropClientUtil.getAllImportersAt(this, p);
	}

	/**
	 * Imports the data from the given DragAwareRegion.
	 * Handles imports from the plugin's standard regions.
	 */
	public boolean importData(Object regionId, Transferable data, Point p)
	{
		// Is it one of our default regions?
		if (regionId instanceof BasicDropRegionId)
		{
			try
			{
				VisiblePlugin plugin = null;

				if (data.isDataFlavorSupported(StandardFlavors.PLUGIN))
				{
					// We have been passed a plugin directly, unlink its holder.
					// This will cause the plugin to be removed from its current container.
					plugin = (VisiblePlugin) data.getTransferData(StandardFlavors.PLUGIN);
					PluginHolder holder = plugin.getPluginHolder();
					if (holder != null)
					{
						if (holder instanceof PluginPanel)
						{
							TabbedPluginContainer currentContainer = ((PluginPanel) holder).getTabbedContainer();
							if (currentContainer == this)
							{
								// We do not allow import into the current container
								return false;
							}
						}

						holder.unlinkHolder();
					}
				}
				else if (data.isDataFlavorSupported(StandardFlavors.PLUGIN_STATE))
				{
					// Instantiate the plugin from the plugin state
					PluginState state = ((PluginState) data.getTransferData(StandardFlavors.PLUGIN_STATE));
					plugin = PluginMgr.getInstance().createVisibleInstance(state, null);
				}

				if (plugin != null)
				{
					TabbedPluginContainer pc = new TabbedPluginContainer(plugin);

					String constraint = ((BasicDropRegionId) regionId).getConstraint();

					if (constraint == null || CENTER.equals(constraint))
					{
						addContainer(pc);
					}
					else
					{
						sliceContainer(pc, this, constraint);
					}

					// Force a re-focus of the plugin
					PluginFocusMgr.getInstance().resetFocusCache();
					plugin.focusPlugin();

					return true;
				}
			}
			catch (UnsupportedFlavorException e)
			{
			}
			catch (IOException e)
			{
			}

			return false;
		}

		return false;
	}

	/**
	 * Returns all sub clients.
	 *
	 * @return A list containing the active plugin of this container or null
	 */
	public List getSubClients()
	{
		VisiblePlugin plugin = getActivePlugin();
		if (plugin != null)
		{
			return Collections.singletonList(plugin.getPluginPanel());
		}

		return null;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Drag and Drop support
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Rebuilds the enviroment of the container (title, toolbar, menu).
	 *
	 * @param fullRebuild
	 *		true	Causes a full container environment including menu and toolbar rebuild<br>
	 *		false	Updates the container title only
	 */
	public void updateContainer(boolean fullRebuild)
	{
		if (tabbedPane != null)
		{
			int count = tabbedPane.getTabCount();

			for (int i = 0; i < count; i++)
			{
				PluginPanel pp = (PluginPanel) tabbedPane.getComponentAt(i);

				tabbedPane.setTitleAt(i, pp.getSubTitle());
				if (fullRebuild)
				{
					tabbedPane.setIconAt(i, pp.getIcon().getIcon(FlexibleSize.SMALL));
				}
			}
		}
	}

	/**
	 * Returns the page level parent plugin of this plugin.
	 * @return The page that holds this plugin or null
	 */
	public JaspiraPage getPage()
	{
		PluginContainer parent = getParentContainer();

		return parent != null ? parent.getPage() : null;
	}

	public boolean canDrag()
	{
		return true;
	}

	public void dropAccepted(Transferable t)
	{
	}

	public void dropCanceled(Transferable t)
	{
	}

	public void dropPerformed(Transferable t)
	{
	}

	public Transferable getTranferableAt(Point p)
	{
		VisiblePlugin plugin = getSelectedPlugin();

		if (plugin != null && plugin.canDrag())
		{
			return new PluginTransferable(plugin);
		}
		return null;
	}

	public MultiIcon getDragImage()
	{
		VisiblePlugin plugin = getSelectedPlugin();
		if (plugin != null)
			return plugin.getIcon();
		return null;
	}

	//////////////////////////////////////////////////
	// @@ Keyboard plugin activation support
	//////////////////////////////////////////////////

	/**
	 * Action that selects the next component to the left.
	 */
	public static class ChangeAction extends AbstractAction
	{
		/** Which component should be selected next? */
		private int offset;

		ChangeAction(int offset)
		{
			this.offset = offset;
		}

		public void actionPerformed(ActionEvent e)
		{
			TabbedPluginContainer tpc = (TabbedPluginContainer) e.getSource();
			tpc.setSelectedTab(tpc.getSelectedTab() + offset);
		}
	}

	/**
	 * Action that selects the component with the given number.
	 */
	public static class NumberAction extends AbstractAction
	{
		/** Number of the tab that should be selected */
		private int number;

		/**
		 * Creates a new Action with the given number.
		 * @param number Tab index number
		 */
		NumberAction(int number)
		{
			this.number = number;
		}

		public void actionPerformed(ActionEvent e)
		{
			TabbedPluginContainer tpc = (TabbedPluginContainer) e.getSource();
			tpc.setSelectedTab(number);
		}
	}

	/**
	 * Returns the index of the currently selected plugin.
	 * @return The index or 0 if the container does not contain a plugin
	 */
	int getSelectedTab()
	{
		int n = 0;
		if (tabbedPane != null)
		{
			n = tabbedPane.getSelectedIndex();
			if (n < 0)
				n = 0;
		}
		return n;
	}

	/**
	 * Sets the selected tab (if there are tabs).
	 * @param tab Number of the tab to select, starting with 0.<br>
	 * If tabs is higher than the actual tab count, the first tab is selected,
	 * likewise if lower than 0, the last.
	 */
	void setSelectedTab(int tab)
	{
		if (tabbedPane == null)
			return;

		int max = getNumberOfPlugins();
		if (max <= 1)
			return;

		if (tab < 0)
		{
			// Wrap around
			tab = max - 1;
		}
		else if (tab >= max)
		{
			// Wrap around
			tab = 0;
		}

		tabbedPane.setSelectedIndex(tab);
	}
}
