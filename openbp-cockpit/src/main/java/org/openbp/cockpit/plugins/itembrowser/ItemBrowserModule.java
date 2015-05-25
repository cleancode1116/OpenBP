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
package org.openbp.cockpit.plugins.itembrowser;

import org.openbp.cockpit.modeler.Modeler;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.MsgFormat;
import org.openbp.core.MimeTypes;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.guiclient.event.OpenEvent;
import org.openbp.guiclient.event.OpenEventInfo;
import org.openbp.guiclient.model.ModelConnector;
import org.openbp.guiclient.plugins.displayobject.DisplayObjectPlugin;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.action.JaspiraPopupMenu;
import org.openbp.jaspira.action.JaspiraToolbar;
import org.openbp.jaspira.event.AskEvent;
import org.openbp.jaspira.event.InteractionEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.event.RequestEvent;
import org.openbp.jaspira.event.VetoableEvent;
import org.openbp.jaspira.gui.plugin.JaspiraPage;
import org.openbp.jaspira.gui.plugin.JaspiraPageContainer;
import org.openbp.jaspira.plugin.ExternalEventModule;
import org.openbp.jaspira.plugin.Plugin;
import org.openbp.jaspira.plugins.propertybrowser.PropertyBrowserSaveEvent;
import org.openbp.swing.components.JMsgBox;

/**
 * External event module item browser.
 *
 * @author Heiko Erhardt
 */
public class ItemBrowserModule extends ExternalEventModule
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Item browser */
	private final ItemBrowserPlugin itemBrowser;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param itemBrowser Item browser
	 */
	public ItemBrowserModule(Plugin itemBrowser)
	{
		super(itemBrowser);

		this.itemBrowser = (ItemBrowserPlugin) itemBrowser;
	}

	//////////////////////////////////////////////////
	// @@ Module implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.plugin.EventModule#getName()
	 */
	public String getName()
	{
		return "plugin.itembrowser";
	}

	//////////////////////////////////////////////////
	// @@ Item browser events
	//////////////////////////////////////////////////

	/**
	 * Event handler: Toggle functional group display.
	 * Shows or hides the functional group.
	 *
	 * @event standard.file.new
	 * @param jae Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode standard_file_new(JaspiraActionEvent jae)
	{
		// Create a popup menu containing the item types that we can display
		InteractionEvent iae = new InteractionEvent(itemBrowser, InteractionEvent.MENU, null);

		// New item group
		itemBrowser.addNewItemActions(iae);

		// Create the popup
		JaspiraPopupMenu menu = iae.createPopupMenu();
		if (menu == null)
			return EVENT_IGNORED;

		// Show the menu below the main toolbar
		JaspiraPage page = itemBrowser.getPage();
		JaspiraPageContainer pagecontainer = (JaspiraPageContainer) page.getParentContainer();
		JaspiraToolbar toolbar = pagecontainer.getToolbar();

		int height = toolbar.getHeight();
		menu.show(toolbar, 0, height);

		return EVENT_CONSUMED;
	}

	/**
	 * Event handler: Toggle functional group display.
	 * Shows or hides the functional group.
	 *
	 * @event plugin.itembrowser.togglefunctionalgroup
	 * @param jae Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode togglefunctionalgroup(JaspiraActionEvent jae)
	{
		itemBrowser.itemTree.setShowGroups(! itemBrowser.itemTree.isShowGroups());
		itemBrowser.rebuildTree();

		return EVENT_CONSUMED;
	}

	/**
	 * Event handler: Remove an item or a model.
	 * Removes an item using the model connector.
	 * The model connector will inform the item browser to refresh itself.
	 *
	 * @event plugin.itembrowser.remove
	 * @param jae Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode remove(JaspiraActionEvent jae)
	{
		if (! itemBrowser.isPluginFocused())
			// Probably activated using a keyboard shortcut from somewhere else...
			return EVENT_IGNORED;

		// Get the model and the current item
		Item item = itemBrowser.getSelectedItem();

		String msg = itemBrowser.getPluginResourceCollection().getRequiredString(item instanceof Model ? "msg.removemodel" : "msg.removeitem");
		int response = JMsgBox.showFormat(null, msg, item.getQualifier(), JMsgBox.TYPE_YESNO);

		if (response == JMsgBox.TYPE_YES)
		{
			VetoableEvent ve = new VetoableEvent(itemBrowser, "standard.file.askdelete", item.getQualifier());
			itemBrowser.fireEvent(ve);

			if (! ve.isVetoed())
			{
				// Select something different than the current node
				itemBrowser.performAlternativeSelection();

				try
				{
					if (item instanceof Model)
					{
						ModelConnector.getInstance().removeModel((Model) item);
					}
					else
					{
						ModelConnector.getInstance().removeItem(item);
					}
				}
				catch (ModelException ex)
				{
					ExceptionUtil.printTrace(ex);
					return EVENT_CONSUMED;
				}
			}
		}

		return EVENT_CONSUMED;
	}

	/**
	 * Event handler: Run a process.
	 *
	 * @event plugin.itembrowser.run
	 * @param jae Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode run(JaspiraActionEvent jae)
	{
		Item item = itemBrowser.getSelectedItem();

		if (item instanceof ProcessItem || item instanceof Model)
		{
			// Fire the event that runs the model
			itemBrowser.fireEvent(new JaspiraEvent(itemBrowser, "debugger.client.run", item));
			return EVENT_CONSUMED;
		}

		return EVENT_IGNORED;
	}

	/**
	 * Event handler: Publish a model.
	 *
	 * @event plugin.itembrowser.publish
	 * @param jae Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode publish(JaspiraActionEvent jae)
	{
		Model model = itemBrowser.getSelectedModel(0);
		if (model == null)
			return EVENT_IGNORED;

		// Fire the event that starts the publisher wizard
		itemBrowser.fireEvent("plugin.publisher.publish", model);

		return EVENT_CONSUMED;
	}

	/**
	 * The titlemode option of the role manager ({@link DisplayObjectPlugin}) has been changed.
	 *
	 * @event displayobject.changed.titlemode
	 * @param event Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode displayobject_changed_titlemode(JaspiraEvent event)
	{
		itemBrowser.itemTree.repaint();

		return EVENT_HANDLED;
	}

	/**
	 * Event handler: A modeler view has become active.
	 *
	 * @event modeler.view.activated
	 * @eventobject Editor that owns the view ({@link Modeler})
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode modeler_view_activated(JaspiraEvent je)
	{
		Object o = je.getObject();

		if (o instanceof Modeler)
		{
			ProcessItem currentProcess = ((Modeler) o).getProcess();
			itemBrowser.currentProcessQualifier = currentProcess.getQualifier();

			return EVENT_HANDLED;
		}

		return EVENT_IGNORED;
	}

	/**
	 * Event handler: A modeler view has become inactive.
	 *
	 * @event modeler.view.closed
	 * @eventobject Editor that owns the view ({@link Modeler})
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode modeler_view_closed(JaspiraEvent je)
	{
		Object o = je.getObject();

		if (o instanceof Modeler)
		{
			itemBrowser.currentProcessQualifier = null;

			return EVENT_HANDLED;
		}

		return EVENT_IGNORED;
	}

	//////////////////////////////////////////////////
	// @@ Association/open events
	//////////////////////////////////////////////////

	/**
	 * Event handler: Check supported mime types for the open event.
	 * Adds the event names for open events for items
	 * to the result of the poll event.
	 *
	 * @event plugin.association.supports
	 * @param event Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode plugin_association_supports(RequestEvent event)
	{
		String mimeType = (String) event.getObject();

		if (MimeTypes.ITEM.equals(mimeType))
		{
			event.addResult(new OpenEventInfo("plugin.itembrowser.open", mimeType, itemBrowser.getTitle()));
			return EVENT_HANDLED;
		}

		return EVENT_IGNORED;
	}

	/**
	 * Event handler: Open an object.
	 *
	 * @event plugin.itembrowser.open
	 * @param openEvent Open event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode open(OpenEvent openEvent)
	{
		Object object = openEvent.getObject();

		if (! (object instanceof Item) && ! (object instanceof Item))
			return EVENT_IGNORED;

		itemBrowser.setSelectedObject(object);

		itemBrowser.focusPlugin();

		return EVENT_CONSUMED;
	}

	//////////////////////////////////////////////////
	// @@ Property browser events
	//////////////////////////////////////////////////

	/**
	 * Event handler: Save an item displayed in the property browser.
	 *
	 * @event plugin.propertybrowser.executesave
	 * @param oee Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode plugin_propertybrowser_executesave(PropertyBrowserSaveEvent oee)
	{
		if (! (oee.original instanceof Item))
			return EVENT_IGNORED;

		Item originalItem = (Item) oee.original;
		Item modifiedItem = (Item) oee.getObject();

		// We fire an ask event to determine if this item is currently beeing edited
		// If so, we will ignore this save request, it will be handled by the editor that edits the item.
		AskEvent ae = new AskEvent(itemBrowser, "global.edit.geteditedinstance", originalItem);
		itemBrowser.fireEvent(ae);
		Item editedItem = (Item) ae.getAnswer();
		if (editedItem != null)
			// We will use the edited item instead of the current one as node provider (may be newer!)
			return EVENT_IGNORED;

		oee.saved = updateItem(originalItem, modifiedItem);

		return EVENT_CONSUMED;
	}

	/**
	 * Updates a item in the tree.
	 *
	 * @param originalItem Item which is used by the tree node
	 * @param modifiedItem Item which will set to the tree node
	 *
	 * @return
	 * true: The update was sucessful<br>
	 * false: The update failed
	 */
	private boolean updateItem(Item originalItem, Item modifiedItem)
	{
		try
		{
			ModelConnector modelConnector = ModelConnector.getInstance();

			if (originalItem instanceof Model)
			{
				modelConnector.updateModel((Model) modifiedItem);
			}
			else
			{
				String newName = modifiedItem.getName();

				ModelQualifier destinationQualifier = new ModelQualifier(originalItem.getQualifier());
				destinationQualifier.setItem(newName);

				// Move the item if necessary
				modelConnector.moveItem(modifiedItem, destinationQualifier);
				modelConnector.updateItem(modifiedItem);
			}
		}
		catch (ModelException e)
		{
			String msg = MsgFormat.format("{0}\nComponent or model $1 could not be saved.", e.getMessage(), originalItem.getQualifier());

			// When this method is being called during e. g. a 'focus lost' event,
			// displaying a message box will stall AWT's focus handling.
			// So we defer the message box display after the current event has been processed
			// by using JMsgBox.TYPE_OKLATER.
			JMsgBox.show(null, msg, JMsgBox.ICON_ERROR | JMsgBox.TYPE_OKLATER);

			return false;
		}

		return true;
	}
}
