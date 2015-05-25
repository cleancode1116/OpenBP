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
package org.openbp.cockpit.plugins.toolbox;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openbp.common.icon.FlexibleSize;
import org.openbp.core.model.Model;
import org.openbp.core.model.item.Item;
import org.openbp.guiclient.model.item.ItemIconMgr;
import org.openbp.guiclient.model.item.ItemTransferable;
import org.openbp.guiclient.util.ClientFlavors;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.event.InteractionEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.plugin.InteractionModule;

/**
 * This is a ToolBox where the User can put several Items.
 *
 * @author Jens Ferchland
 */
public class UserToolBoxPlugin extends ToolBoxPlugin
{
	/**
	 * @see org.openbp.cockpit.plugins.toolbox.ToolBoxPlugin#acceptFlyWheelKey(int)
	 */
	protected boolean acceptFlyWheelKey(int key)
	{
		// Trigger this toolbox using the '1' key
		return key == KeyEvent.VK_1;
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
		 * We are low priority.
		 *
		 * @return The priority. 0 is lowest, 100 is highest.
		 */
		public int getPriority()
		{
			return 99;
		}

		/**
		 * Standard event handler that is called when a popup menu is to be shown.
		 * Adds the 'Add to toolbox' popup menu entry for items.
		 *
		 * @event global.interaction.popup
		 * @param ie Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode popup(InteractionEvent ie)
		{
			// Ignore events from other toolboxes
			if (ie.getSourcePlugin() instanceof ToolBoxPlugin)
			{
				return EVENT_IGNORED;
			}

			if (ie.isDataFlavorSupported(ClientFlavors.ITEM))
			{
				final Item item = (Item) ie.getSafeTransferData(ClientFlavors.ITEM);

				if (!(item instanceof Model))
				{
					// Add the 'Add to toolbox' action
					JaspiraAction action = new JaspiraAction(UserToolBoxPlugin.this, "toolbox.add")
					{
						public void actionPerformed(ActionEvent ae)
						{
							addToolBoxItem(new ToolBoxItem(item.getDisplayText(), ItemIconMgr.getInstance().getIcon(item, FlexibleSize.MEDIUM), item.getDescriptionText(), new ItemTransferable(item)));
							refreshContent();

							// Bring the toolbox to the front, but don't switch pages
							showPlugin(false);
						}
					};

					String s = action.getDisplayName();
					String tt = getToolBoxTitle();
					if (tt != null)
					{
						// Append the name of the toolbox to the action title
						s = s + ": " + tt;
					}
					action.setDisplayName(s);

					ie.add(action);
				}
			}

			return EVENT_HANDLED;
		}
	}
}
