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
package org.openbp.cockpit.generator;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.openbp.cockpit.generator.wizard.GeneratorWizard;
import org.openbp.cockpit.generator.wizard.WizardSelectionPage;
import org.openbp.common.icon.FlexibleSize;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.activity.JavaActivityItemImpl;
import org.openbp.core.model.item.process.ActivityNode;
import org.openbp.core.model.item.process.ItemProvider;
import org.openbp.core.model.item.process.ItemSynchronization;
import org.openbp.guiclient.model.item.ItemIconMgr;
import org.openbp.guiclient.util.ClientFlavors;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.event.InteractionEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.plugin.AbstractPlugin;
import org.openbp.jaspira.plugin.ApplicationUtil;
import org.openbp.jaspira.plugin.InteractionModule;
import org.openbp.jaspira.plugin.Plugin;

/**
 * Invisible plugin that provides access to the generator wizard.
 *
 * @author Andreas Putz
 */
public class GeneratorPlugin extends AbstractPlugin
{
	//////////////////////////////////////////////////
	// @@ Symbolic constants
	//////////////////////////////////////////////////

	/** First call flag */
	private boolean first = true;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public GeneratorPlugin()
	{
	}

	public String getResourceCollectionContainerName()
	{
		return "plugin.generator";
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
		 * Gets the module priority.
		 * We are high priority.
		 *
		 * @return The priority. 0 is lowest, 100 is highest.
		 */
		public int getPriority()
		{
			return 3;
		}

		//////////////////////////////////////////////////
		// @@ Overridden methods
		//////////////////////////////////////////////////

		/**
		 * Standard event handler that is called when a popup menu is to be shown.
		 * Adds the 'Generate' popup menu entry for models, items and activity nodes.
		 *
		 * @event global.interaction.popup
		 * @param ie Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode popup(InteractionEvent ie)
		{
			Item item = null;

			if (ie.isDataFlavorSupported(ClientFlavors.MODEL))
			{
				item = (Item) ie.getSafeTransferData(ClientFlavors.MODEL);
			}
			else if (ie.isDataFlavorSupported(ClientFlavors.ITEM))
			{
				item = (Item) ie.getSafeTransferData(ClientFlavors.ITEM);
			}
			else if (ie.isDataFlavorSupported(ClientFlavors.ACTIVITY_NODE))
			{
				// Activity node
				ActivityNode activityNode = (ActivityNode) ie.getSafeTransferData(ClientFlavors.ACTIVITY_NODE);

				// Create a dummy item, which can hold the activity handler definition,
				// so we can start the activity handler source code generator.
				item = new JavaActivityItemImpl();
				item.setModel(activityNode.getOwningModel());

				// Copy the properties of the handler definition from the node to the dummy item 
				int syncFlags = ItemSynchronization.SYNC_ALL;
				((ItemProvider) activityNode).copyToItem(item, syncFlags);
			}
			else
				return EVENT_IGNORED;

			if (item != null)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					// Reload the generator info in case it was modified (for generator development only!)
					// TODO Feature 5 Auto-reload of generator definitions should be a modeler option
					// GeneratorMgr.getInstance().reload();
				}

				// Check if there are any generators defined for this item type that operate on an existing item
				List list = GeneratorMgr.getInstance().getGenerators(item.getItemType(), false);
				if (list != null)
				{
					// Instantiate the generate wizard
					GeneratorWizard wizard = new GeneratorWizard(getPluginResourceCollection(), item);

					// Use the item's icon as wizard icon
					ImageIcon icon = (ImageIcon) ItemIconMgr.getInstance().getIcon(item.getItemType(), FlexibleSize.HUGE);
					wizard.setDefaultWizardImage(icon);

					// Create the selection page displaying all available
					// generators and add it to the wizard
					WizardSelectionPage selectionPage = new WizardSelectionPage(wizard);
					int n = list.size();
					for (int i = 0; i < n; ++i)
					{
						Generator generator = (Generator) list.get(i);
						selectionPage.addGenerator(generator);
					}

					selectionPage.expandTree();
					wizard.addAndLinkPage(GeneratorWizard.SELECTION_PAGE, selectionPage);

					// The following page(s) will be added by this page dynamically if needed
					wizard.displayFirst();

					// Create an action that starts up the wizard
					GeneratorAction action = new GeneratorAction(GeneratorPlugin.this, "generator.action", wizard);
					ie.add(action);
				}
			}

			return EVENT_HANDLED;
		}
	}

	/**
	 * Static generator action class.
	 */
	public static class GeneratorAction extends JaspiraAction
	{
		/** Wizard */
		private final GeneratorWizard wizard;

		/** Title of the wizard dialog */
		private final String title;

		/**
		 * Constructor.
		 * The action retrieves its properties from the resources of its owner plugin.
		 *
		 * @param owner Plugin that owns the action
		 * @param name Resource name
		 * @param wizard Wizard
		 */
		public GeneratorAction(Plugin owner, String name, GeneratorWizard wizard)
		{
			super(owner, name);
			this.wizard = wizard;

			title = owner.getPluginResourceCollection().getOptionalString("generator.wizard.title");
		}

		/**
		 * Executes the action.
		 *
		 * @param ae Event
		 */
		public void actionPerformed(ActionEvent ae)
		{
			// Create the wizard dialog
			JFrame activeFrame = ApplicationUtil.getActiveWindow();
			JDialog dialog = new JDialog(activeFrame, true);

			dialog.setSize(new Dimension(600, 700));
			dialog.setLocationRelativeTo(activeFrame);

			dialog.setTitle(title);

			dialog.getContentPane().add(wizard);

			// Show the dialog
			dialog.setVisible(true);
		}
	}
}
