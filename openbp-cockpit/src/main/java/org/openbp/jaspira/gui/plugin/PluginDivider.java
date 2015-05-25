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

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.common.util.ToStringHelper;
import org.openbp.jaspira.gui.interaction.DropClientUtil;
import org.openbp.jaspira.gui.interaction.InteractionClient;
import org.openbp.jaspira.gui.interaction.JaspiraTransferHandler;
import org.openbp.jaspira.plugin.Plugin;
import org.openbp.swing.components.JMultiSplitPane;

/**
 * This class is a container for Plugins. It contains a number of Slots.
 * each slot can contain eihter a single plugin or a pluginCOntainer.
 *
 * @author Stephan Moritz
 */
public class PluginDivider extends JMultiSplitPane
	implements PluginContainer, InteractionClient
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public PluginDivider()
	{
		super();
		this.setTransferHandler(new JaspiraTransferHandler());
	}

	/**
	 * Constructor.
	 * @param orientation Orientation of the container ({@link JMultiSplitPane#VERTICAL_SPLIT}/{@link JMultiSplitPane#HORIZONTAL_SPLIT})
	 */
	public PluginDivider(int orientation)
	{
		super(orientation);
		this.setTransferHandler(new JaspiraTransferHandler());
	}

	/**
	 * Returns a string representation of this object.
	 * @nowarn
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "plugins");
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Plugin adding/removal
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Wraps a {@link TabbedPluginContainer} around a plugin panel and adds it.
	 *
	 * @param p Plugin to add
	 */
	public void addPlugin(VisiblePlugin p)
	{
		addContainer(new TabbedPluginContainer(p));
	}

	/**
	 * Inserts a child plugin container at the end of this divider (right or bottom).
	 *
	 * @param pc Container to add
	 */
	public void addContainer(PluginContainer pc)
	{
		insertContainerAt(pc, -1);
	}

	/**
	 * Adds a child plugin container.
	 *
	 * @param pc Container to add
	 * @param slot Slot to add the plugin into
	 */
	public void insertContainerAt(PluginContainer pc, int slot)
	{
		addClient((Component) pc, slot);
		linkToPage(pc);
	}

	public void sliceContainer(PluginContainer toInsert, PluginContainer currentContainer, String constraint)
	{
		// matches the slice tagOrientation our splitdirection?
		// if so, we simply need to add the slicer directly
		// (if we are horiz. aligned and slice at EAST or WEST or the opposite)
		if (getOrientation() == VERTICAL_SPLIT)
		{
			// We are Horizontal
			if (PluginContainer.SOUTH.equals(constraint))
			{
				// Add to the end
				addClientAfter((Component) currentContainer, (Component) toInsert);
			}
			else if (PluginContainer.NORTH.equals(constraint))
			{
				addClientBefore((Component) currentContainer, (Component) toInsert);
			}
			else if (PluginContainer.WEST.equals(constraint))
			{
				PluginDivider sub = new PluginDivider(HORIZONTAL_SPLIT);

				sub.addClient((Component) toInsert);
				replaceClient((Component) currentContainer, sub);
				sub.addClient((Component) currentContainer);
			}
			else if (PluginContainer.EAST.equals(constraint))
			{
				PluginDivider sub2 = new PluginDivider(HORIZONTAL_SPLIT);

				sub2.addClient((Component) toInsert);
				replaceClient((Component) currentContainer, sub2);
				sub2.addClient((Component) currentContainer, 0);
			}
		}
		else
		{
			// We are Vertical
			if (PluginContainer.EAST.equals(constraint))
			{
				addClientAfter((Component) currentContainer, (Component) toInsert);
			}
			else if (PluginContainer.WEST.equals(constraint))
			{
				addClientBefore((Component) currentContainer, (Component) toInsert);
			}
			else if (PluginContainer.NORTH.equals(constraint))
			{
				PluginDivider sub = new PluginDivider(VERTICAL_SPLIT);

				sub.addClient((Component) toInsert);
				replaceClient((Component) currentContainer, sub);
				sub.addClient((Component) currentContainer);
			}
			else if (PluginContainer.SOUTH.equals(constraint))
			{
				PluginDivider sub2 = new PluginDivider(VERTICAL_SPLIT);

				sub2.addClient((Component) toInsert);
				replaceClient((Component) currentContainer, sub2);
				sub2.addClient((Component) currentContainer, 0);
			}
		}

		linkToPage(toInsert);
	}

	/**
	 * Removes the given plugin.
	 */
	public void removePlugin(VisiblePlugin p)
	{
		removeClient(p.getPluginPanel());
	}

	private void linkToPage(PluginContainer pc)
	{
		JaspiraPage page = getPage();
		if (page != null)
		{
			for (Iterator it = pc.getPlugins().iterator(); it.hasNext();)
			{
				((Plugin) it.next()).setParentPlugin(page);
			}
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Plugin Access
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the currently active plugin.
	 *
	 * Not yet implemented.
	 */
	public VisiblePlugin getActivePlugin()
	{
		// TODO Cleanup 6: implement getActivePlugin ()
		return null;
	}

	/**
	 * Returns our parent container.
	 */
	public PluginContainer getParentContainer()
	{
		// We assume that a plugin that is associated with some parent of the divider must be
		// a Jaspira page. A little bit tricky, however...
		VisiblePlugin plugin = AbstractVisiblePlugin.getPluginFromComponentHierarchy(this);
		if (plugin != null)
			return (JaspiraPage) plugin;
		return null;
	}

	/**
	 * Returns all plugins of this component
	 * @return A list of {@link VisiblePlugin} objects
	 */
	public List getPlugins()
	{
		List result = new ArrayList();

		Component [] comps = getClients();
		for (int i = 0; i < comps.length; i++)
		{
			if (comps [i] instanceof PluginContainer)
			{
				result.addAll(((PluginContainer) comps [i]).getPlugins());
			}
		}

		return result;
	}

	/**
	 * Returns all plugins that are currently visible, i\.e\. shown in their tabbed containers.
	 * @return A list of {@link VisiblePlugin} objects
	 */
	public List getVisiblePlugins()
	{
		List result = new ArrayList();

		Component [] comps = getClients();
		for (int i = 0; i < comps.length; i++)
		{
			if (comps [i] instanceof PluginContainer)
			{
				List l = ((PluginContainer) comps [i]).getVisiblePlugins();
				if (l != null)
				{
					result.addAll(l);
				}
			}
		}

		return result;
	}

	public JaspiraPage getPage()
	{
		PluginContainer parent = getParentContainer();

		return parent != null ? parent.getPage() : null;
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
	}

	/**
	 * Right now, a plugin divider has no regions
	 *
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getDropRegions(List, Transferable, MouseEvent)
	 */
	public List getDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		return null;
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
	 * @return Always false
	 */
	public boolean importData(Object regionId, Transferable data, Point p)
	{
		return false;
	}

	/**
	 * Returns all sub clients.
	 *
	 * @return A list of the components (clients) of this divider
	 */
	public List getSubClients()
	{
		List result = null;

		Component [] comps = getClients();
		for (int i = 0; i < comps.length; i++)
		{
			if (result == null)
				result = new ArrayList();
			result.add(comps [i]);
		}

		return result;
	}
}
