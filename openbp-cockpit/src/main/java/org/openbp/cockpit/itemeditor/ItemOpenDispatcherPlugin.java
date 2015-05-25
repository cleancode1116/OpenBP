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
package org.openbp.cockpit.itemeditor;

import org.openbp.core.MimeTypes;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.activity.ActivityItem;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.type.ComplexTypeItem;
import org.openbp.guiclient.event.OpenEvent;
import org.openbp.guiclient.event.OpenEventInfo;
import org.openbp.guiclient.model.ModelConnector;
import org.openbp.guiclient.model.item.ItemEditor;
import org.openbp.guiclient.model.item.ItemEditorRegistry;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.event.RequestEvent;
import org.openbp.jaspira.gui.plugin.JaspiraPage;
import org.openbp.jaspira.plugin.AbstractPlugin;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.swing.components.JMsgBox;

/**
 * Plugin that handles open events for components and models.
 * Activity and process items are opened in the component editor, others are displayed in the model browser.
 *
 * @author Stephan Moritz
 */
public class ItemOpenDispatcherPlugin extends AbstractPlugin
{
	public String getResourceCollectionContainerName()
	{
		return "plugin.cockpit";
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Event module
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Event module.
	 */
	public class Events extends EventModule
	{
		/** Modeler page */
		protected JaspiraPage modelerPage;

		public String getName()
		{
			return "global.edit";
		}

		/**
		 * Gets the module priority.
		 * We are high priority.
		 *
		 * @return The priority. 0 is lowest, 100 is highest.
		 */
		public int getPriority()
		{
			return 2;
		}

		/**
		 * Event handler: Opens a process in the modeler.
		 *
		 * @event open.componenteditor
		 * @param oe Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode open_componenteditor(final OpenEvent oe)
		{
			Object o = oe.getObject();

			if (o instanceof ModelQualifier)
			{
				// Get the item specified by the model qualifier
				ModelQualifier qualifier = (ModelQualifier) o;
				try
				{
					o = ModelConnector.getInstance().getItemByQualifier(qualifier, true);
				}
				catch (ModelException sie)
				{
					String msg = "The component '" + qualifier + "' does not exist.";
					JMsgBox.show(null, msg, JMsgBox.ICON_ERROR);
					return EVENT_CONSUMED;
				}

				// Put the object we retrieved back into the event in case the event gets redistributed
				oe.setObject(o);
			}

			if (o instanceof ActivityItem || o instanceof ProcessItem || o instanceof ComplexTypeItem)
			{
				// Open actions and processes in the item wizard
				Item item = (Item) o;

				ItemEditor editor = ItemEditorRegistry.getInstance().lookupItemEditor(item.getItemType());
				if (editor != null)
				{
					// For compatibility with pre-2.0 process items
					StandardItemEditor.ensureProcessType(item);

					editor.openItem(item, EditedItemStatus.EXISTING);
				}

				return EVENT_CONSUMED;
			}
			else if (o instanceof Model || o instanceof Item)
			{
				// Display all other items in the model browser
				fireEvent("plugin.itembrowser.open", oe);

				return EVENT_CONSUMED;
			}

			return EVENT_IGNORED;
		}

		/**
		 * Event handler: Check supported mime types for the open event.
		 * Adds the event names for open events for process and action items
		 * to the result of the poll event.
		 *
		 * @event plugin.association.supports
		 * @param event Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode plugin_association_supports(RequestEvent event)
		{
			String mimeType = (String) event.getObject();

			if (MimeTypes.ACTIVITY_ITEM.equals(mimeType) || MimeTypes.PROCESS_ITEM.equals(mimeType) || MimeTypes.COMPLEX_TYPE_ITEM.equals(mimeType))
			{
				event.addResult(new OpenEventInfo("open.componenteditor", mimeType, getPluginResourceCollection().getRequiredString("title.association.editor")));
				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
		}
	}
}
