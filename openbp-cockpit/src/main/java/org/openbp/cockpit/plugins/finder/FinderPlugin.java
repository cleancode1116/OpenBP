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
package org.openbp.cockpit.plugins.finder;

import java.util.Iterator;

import javax.swing.SwingUtilities;

import org.openbp.cockpit.modeler.ModelerPage;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.process.InitialNode;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.guiclient.util.ClientFlavors;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.event.AskEvent;
import org.openbp.jaspira.event.InteractionEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.gui.plugin.TabbedPluginContainer;
import org.openbp.jaspira.plugin.AbstractPlugin;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.jaspira.plugin.InteractionModule;
import org.openbp.jaspira.plugin.PluginMgr;

/**
 * The plugin of the finder.
 */
public class FinderPlugin extends AbstractPlugin
{
	//////////////////////////////////////////////////
	// @@ Member
	//////////////////////////////////////////////////

	/** Action to audit model. */
	protected JaspiraAction findReferenceAction;

	/** The plugin itself. */
	protected FinderPlugin plugin;

	/** The item from that the references are created. */
	protected ModelObject contextModelObject;

	//////////////////////////////////////////////////
	// @@ Plugin methods
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public FinderPlugin()
	{
		plugin = this;
	}

	/**
	 * @copy org.openbp.jaspira.plugin.AbstractPlugin.getResourceCollectionContainerName()
	 */
	public String getResourceCollectionContainerName()
	{
		return "plugin.finder";
	}

	/**
	 * @see org.openbp.jaspira.plugin.AbstractPlugin#pluginInstalled()
	 */
	protected void pluginInstalled()
	{
		super.pluginInstalled();

		findReferenceAction = getAction("plugin.finder.findref");
	}

	//////////////////////////////////////////////////
	// @@ Event handling module
	//////////////////////////////////////////////////

	/**
	 * Event module.
	 */
	public class Events extends EventModule
	{
		/**
		 * @copy org.openbp.jaspira.plugin.EventModule.getName()
		 */
		public String getName()
		{
			return "plugin.finder";
		}

		/**
		 * Event handler: Find references of a component.
		 *
		 * @param je Event
		 *
		 * @return The event status code
		 *
		 * @event plugin.finder.findref
		 */
		public JaspiraEventHandlerCode findref(JaspiraActionEvent je)
		{
			if (contextModelObject != null)
			{
				// Start the representation of the results. Create an instance of the finder plugin, if
				// it does not exist already. Then register the finder result plugin in the modeler in
				// the container with the Component Browser. Set the focus to the finder plugin and send
				// the event in order to show the results of the finder.
				SwingUtilities.invokeLater(new Thread()
				{
					public void run()
					{
						AskEvent ae = new AskEvent(plugin, "plugin.finder.register");
						plugin.fireEvent(ae);
						((FinderResultPlugin) ae.getAnswer()).focusPlugin();
						plugin.fireEvent("plugin.finderresult.present", contextModelObject);
					}
				});
			}

			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Create the instance of the finder result plugin, if it
		 * does not exist already. Then register the plugin in the modeler if it
		 * is not already registered. The instance of the finder plugin is returned
		 * as the answer.
		 *
		 * @param ae AskEvent
		 *
		 * @return The event status code
		 *
		 * @event plugin.finder.register
		 */
		public JaspiraEventHandlerCode register(AskEvent ae)
		{
			ae.setAnswer(determineFinderResultPlugin());
			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Add the finder result plugin to the modeler page.
		 *
		 * @param je JaspiraEvent
		 *
		 * @return The event status code
		 *
		 * @event global.page.added
		 */
		public JaspiraEventHandlerCode global_page_added(JaspiraEvent je)
		{
			if (je.getObject() instanceof ModelerPage)
			{
				// This will install the Finder in the modeler page
				determineFinderResultPlugin();
				return EVENT_HANDLED;
			}
			return EVENT_IGNORED;
		}
	}

	//////////////////////////////////////////////////
	// @@ Interaction module
	//////////////////////////////////////////////////

	/**
	 * Interaction module.
	 */
	public class InteractionEvents extends InteractionModule
	{
		/**
		 * Get the module priority. We are super-low priority.
		 *
		 * @return The priority. 0 is lowest, 100 is highest.
		 */
		public int getPriority()
		{
			return 0;
		}

		/**
		 * Standard event handler that is called when a popup menu is to be shown.
		 * Adds the popup menu entries for process items.
		 *
		 * @param ie Event
		 *
		 * @return The event status code
		 *
		 * @event global.interaction.popup
		 */
		public JaspiraEventHandlerCode popup(InteractionEvent ie)
		{
			if (!ie.isDataFlavorSupported(ClientFlavors.MODEL_OBJECT) || findReferenceAction == null)
				return EVENT_IGNORED;

			if (ie.isDataFlavorSupported(ClientFlavors.ITEM))
			{
				contextModelObject = (Item) ie.getSafeTransferData(ClientFlavors.ITEM);
			}
			else
			{
				// Use only model objects that have an underlying item.
				ModelObject core = (ModelObject) ie.getSafeTransferData(ClientFlavors.MODEL_OBJECT);
				extractModelObject(core);
			}

			if (contextModelObject != null)
			{
				JaspiraAction group = new JaspiraAction("popup.finder", null, null, null, null, 120, JaspiraAction.TYPE_GROUP);
				group.addMenuChild(findReferenceAction);
				ie.add(group);

				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
		}
	}

	/**
	 * Extract the item from a model object. If the model object is a node then the
	 * underlying item is returned. If it is a parameter then the datatype is returned.
	 * It is necessary because a datalink is a coreobject too, but it can not be
	 * used by the finder.
	 *
	 * @param core The model object
	 */
	void extractModelObject(ModelObject core)
	{
		contextModelObject = null;
		if (core instanceof Item)
			contextModelObject = core;
		else if (core instanceof NodeParam)
			contextModelObject = ((NodeParam) core).getDataType();
		else if (core instanceof InitialNode)
			contextModelObject = core;
	}

	/**
	 * Create the instance of the finder result plugin, if it does not exist  already.
	 * Then register the plugin in the modeler if
	 * it is not already  registered.
	 *
	 * @return The instance of the finder result plugin
	 */
	FinderResultPlugin determineFinderResultPlugin()
	{
		// Get the instance of the audit result plugin
		Iterator arpList = PluginMgr.getInstance().getPluginInstances("org.openbp.cockpit.plugins.finder.FinderResultPlugin");
		FinderResultPlugin plugin;

		if (!arpList.hasNext())
			plugin = (FinderResultPlugin) PluginMgr.getInstance().createVisibleInstance(FinderResultPlugin.class, this);
		else
			plugin = (FinderResultPlugin) arpList.next();

		// If the result plugin isn't registered in an plugin container, then register
		// it in the same as the Item Browser
		TabbedPluginContainer tpc = plugin.getPluginPanel().getTabbedContainer();
		if (tpc == null)
		{
			JaspiraEvent je = new JaspiraEvent(this, "global.plugin.addtocontainer", plugin);
			je.setTargetClassName("org.openbp.cockpit.plugins.itembrowser.ItemBrowserPlugin");
			fireEvent(je);
		}

		return plugin;
	}
}
