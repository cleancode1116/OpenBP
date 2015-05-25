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
package org.openbp.jaspira.plugins.propertybrowser;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.generic.description.DescriptionObject;
import org.openbp.common.icon.MultiIcon;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.event.InteractionEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin;
import org.openbp.jaspira.gui.plugin.JaspiraPage;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.jaspira.plugin.InteractionModule;
import org.openbp.jaspira.plugin.PluginState;
import org.openbp.jaspira.propertybrowser.ObjectChangeListener;
import org.openbp.jaspira.propertybrowser.PropertyBrowser;
import org.openbp.jaspira.propertybrowser.PropertyBrowserEvent;
import org.openbp.jaspira.propertybrowser.PropertyBrowserImpl;
import org.openbp.jaspira.propertybrowser.PropertyBrowserListener;
import org.openbp.jaspira.propertybrowser.SaveStrategy;
import org.openbp.jaspira.propertybrowser.nodes.ObjectNode;
import org.openbp.swing.plaf.sky.SimpleBorder;

/**
 * Property browser plugin.
 * The oe plugin can be used to display arbitrary objects.<br>
 * There can be multiple oe plugin instances in an application.
 * However, there should be only one oe plugin instance per view.
 *
 * The outside world communicates with the plugin using the property browser events (see the event module).
 * When the oe is instructed to display an object (using the plugin.propertybrowser.setobject
 * event), it will provide information on this object in a custom plugin state object
 * ({@link PropertyBrowserPluginState}). If the oe is currently visible, the object
 * will be displayed and the state (newState member) will be cleared. If not, this will
 * happen when the oe is being activated.<br>
 * This construct has been introduced to prevent the oe from consuming system performance
 * when it is not even visible.
 *
 * The property browser provides to edit properties of any object.
 *
 * @author Andreas Putz
 */
public class PropertyBrowserPlugin extends AbstractVisiblePlugin
	implements ObjectChangeListener, SaveStrategy, PropertyBrowserListener, FocusListener
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Title icon */
	private MultiIcon icon;

	/** Name of the edited object as base for the plugin window title */
	private String titleBase;

	/** Title of the plugin */
	private String title;

	/** Sub title (tab text) of the plugin */
	private String subTitle;

	/** Description of the plugin */
	private String description;

	/** 'Read only' tag to append to plugin title " (read only)" */
	private String readonlyText;

	/** 'Modified' tag to append to plugin title " *" */
	private String modifiedText;

	/** Component that that will hold the red error border */
	private JComponent errorPane;

	/** The property browser */
	private PropertyBrowserImpl propertyBrowser;

	/**
	 * The plugin state object that needs to be shown by the editor.
	 * Will be null if the editor is currently displaying the object.
	 */
	private PropertyBrowserPluginState newState;

	/**
	 * The plugin state object that specifies the object currently shown by the editor.
	 * Will be null if the editor is not displaying any object.
	 */
	private PropertyBrowserPluginState currentState;

	/** Border around the scroll pane in case of validation errors */
	private static SimpleBorder errorBorder;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	public String getResourceCollectionContainerName()
	{
		return "plugin.standard";
	}

	/**
	 * Constructor.
	 */
	public PropertyBrowserPlugin()
	{
	}

	public void initializePlugin()
	{
		super.initializePlugin();

		// Let the editor initially display an empty window
		newState = new PropertyBrowserPluginState(this);

		propertyBrowser = new PluginPropertyBrowser(this, getPluginResourceCollection());
		propertyBrowser.setShowTooltips(false);
		propertyBrowser.addObjectChangeListener(this);
		propertyBrowser.addPropertyBrowserListener(this);

		JScrollPane scrollPane = new JScrollPane(propertyBrowser);
		getContentPane().add(scrollPane);
		errorPane = scrollPane;

		if (errorBorder == null)
		{
			errorBorder = new SimpleBorder(2, 2, 2, 2);
			errorBorder.setWidth(2);
			errorBorder.setColor(Color.RED);
		}

		// The original title should always remain as sub title
		subTitle = getPluginResourceCollection().getOptionalString("title");

		// Get various text from the resource
		readonlyText = getPluginResourceCollection().getOptionalString("plugin.propertybrowser.readonly");
		if (readonlyText != null)
			readonlyText = " " + readonlyText;

		modifiedText = getPluginResourceCollection().getOptionalString("plugin.propertybrowser.modified");
		if (modifiedText != null)
			modifiedText = " " + modifiedText;
	}

	protected void initializeComponents()
	{
		// Since we need to access the property browser in {@link #getExternalActions},
		// we do the component initialization in the constructor, so this method is empty
	}

	protected Collection getExternalActions()
	{
		ArrayList list = new ArrayList();
		list.add(propertyBrowser.getAddAction());
		list.add(propertyBrowser.getCopyAction());
		list.add(propertyBrowser.getCutAction());
		list.add(propertyBrowser.getPasteAction());
		list.add(propertyBrowser.getRemoveAction());
		list.add(propertyBrowser.getMoveUpAction());
		list.add(propertyBrowser.getMoveDownAction());
		return list;
	}

	//////////////////////////////////////////////////
	// @@ VisiblePlugin overrides
	//////////////////////////////////////////////////

	public boolean hasCloseButton()
	{
		return false;
	}

	/**
	 * @see org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin#getToolbarType()
	 */
	public int getToolbarType()
	{
		return TOOLBAR_DYNAMIC;
	}

	/**
	 * Returns the sub title of this plugin.
	 * In the title bar, we also display the object type and name.
	 * The sub title should stay constant.
	 */
	public String getSubTitle()
	{
		return subTitle != null ? subTitle : getTitle();
	}

	/**
	 * This method is called before a page change takes place. It allows the
	 * plugin to veto the change by returning false. Override this to check for
	 * veto conditions.
	 *
	 * @param oldPage Current page
	 * @param newPage Page that should be made the new current page
	 * @return
	 *		true	If the plugin accepts the page change<br>
	 *		false	Otherwise
	 */
	protected boolean canPageChange(JaspiraPage oldPage, JaspiraPage newPage)
	{
		if (!propertyBrowser.saveObject())
		{
			// There was an error saving the current object, abort
			return false;
		}
		return true;
	}

	//////////////////////////////////////////////////
	// @@ Plugin state handling
	//////////////////////////////////////////////////

	public PluginState getPluginState()
	{
		if (newState != null)
			return newState;
		return currentState;
	}

	public void setPluginState(PluginState state)
	{
		super.setPluginState(state);

		newState = (PropertyBrowserPluginState) state;
		if (newState == null || propertyBrowser == null)
			return;

		try
		{
			propertyBrowser.setObject(newState.unmodifiedObject, newState.modifiedObject, newState.isObjectNew, icon, null);
			propertyBrowser.setOriginalObject(newState.originalObject);

			// Set new title etc. if set operation was successful
			if (propertyBrowser.getModifiedObject() == null)
			{
				titleBase = newState.title;
				description = newState.description;
				icon = newState.icon;

				propertyBrowser.setReadOnly(newState.readOnly);
				propertyBrowser.setSaveImmediately(newState.saveImmediately);

				// Clear the error frame
				errorPane.setBorder(null);

				updateTitle();
				updatePluginContainer(false);

				// Bring oe to front if we display something of interest.
				// The null check on newState is due to a mysterious NPE, which seems to be some thread issue...
				if (newState != null && newState.unmodifiedObject != null)
				{
					showPlugin(false);
				}
			}

			// The new state has been applied, we don't need it any more
			currentState = newState;
			newState = null;
		}
		catch (XMLDriverException e)
		{
			// Should not happen
			ExceptionUtil.printTrace(e);
		}
		catch (CloneNotSupportedException e)
		{
			// Does not happen
			ExceptionUtil.printTrace(e);
		}
	}

	/**
	 * Gets the current object.
	 *
	 * @return Either the object that has been set in the new state object,
	 * but has not been applied or the current object of the editor.<br>
	 * Note that always the original object will be returned, regardless if
	 * it has been modified or not.
	 */
	public Object getCurrentObject()
	{
		if (newState != null)
		{
			// Not applied yet
			return newState.unmodifiedObject;
		}

		return propertyBrowser.getObject();
	}

	//////////////////////////////////////////////////
	// @@ Save strategy implementation
	//////////////////////////////////////////////////

	/**
	 * Executes the save procedure for the current item of the specified property browser.
	 *
	 * @param editor The property browser
	 *
	 * @return
	 *		true	If object was saved successfully.<br>
	 *		false	There were errors during the save operations or the user
	 *				choosed to cancel the save operation.
	 *				The implementor of the strategy should issue an error message
	 *				if appropriate.
	 */
	public boolean executeSave(PropertyBrowser editor)
	{
		Object modObject = editor.getModifiedObject();

		// Fire the executesave event so any plugin that feels responsible
		// to save this type of object can do its job
		PropertyBrowserSaveEvent oee = new PropertyBrowserSaveEvent(this, "plugin.propertybrowser.executesave", modObject, editor.getObject());
		fireEvent(oee);

		if (!oee.saved)
		{
			// Surround the property browser with an error frame
			errorPane.setBorder(errorBorder);
		}
		else
		{
			// In order to make updateStatus() work properly, we reset the
			// modification flag of the property browser
			propertyBrowser.setObjectModified(false);
		}

		updateStatus();

		return oee.saved;
	}

	//////////////////////////////////////////////////
	// @@ Object change listener
	//////////////////////////////////////////////////

	/**
	 * Is performed if the object was changed.
	 *
	 * @param original The object without any changes
	 * @param modified The modified object
	 */
	public void objectChanged(Object original, Object modified)
	{
		updateStatus();
	}

	//////////////////////////////////////////////////
	// @@ PropertyBrowserListener implementation
	//////////////////////////////////////////////////

	public void handlePropertyBrowserEvent(PropertyBrowserEvent e)
	{
		switch (e.eventType)
		{
		case PropertyBrowserEvent.FOCUS_GAINED:
		case PropertyBrowserEvent.SELECTION_CHANGED:

			// Communicate the HTML tooltip text to the info panel
			if (e.node != null)
			{
				Object eventArg = null;
				if (e.node instanceof ObjectNode)
					eventArg = ((ObjectNode) e.node).getObject();
				else
					eventArg = e.node.getColumnValue(0);

				if (eventArg instanceof DescriptionObject)
				{
					fireEvent(new JaspiraEvent(this, "plugin.infopanel.setinfotext", eventArg));
				}
			}

			break;
		}
	}

	//////////////////////////////////////////////////
	// @@ FocusListener implementation
	//////////////////////////////////////////////////

	public void focusGained(FocusEvent e)
	{
	}

	public void focusLost(FocusEvent e)
	{
		fireEvent(new JaspiraEvent(this, "plugin.infopanel.clearinfotext", null));
	}

	//////////////////////////////////////////////////
	// @@ Title
	//////////////////////////////////////////////////

	/**
	 * Updates the status of the property browser (title bar/buttons).
	 */
	private void updateStatus()
	{
		if (updateTitle())
		{
			updatePluginContainer(false);
		}
	}

	/**
	 * Updates the plugin window title.
	 * @return
	 *		true	The title has changed.<br>
	 *		false	The title stays the same.
	 */
	private boolean updateTitle()
	{
		String newTitle = titleBase != null ? titleBase : "";

		if (propertyBrowser.isReadOnly() && readonlyText != null)
		{
			newTitle += readonlyText;
		}

		if (propertyBrowser.isObjectModified() && modifiedText != null)
		{
			newTitle += modifiedText;
		}

		if (title == null || !title.equals(newTitle))
		{
			title = newTitle;
			return true;
		}
		return false;
	}

	public String getTitle()
	{
		if (title == null)
			return super.getTitle();
		return title;
	}

	public String getDescription()
	{
		if (description == null)
			return super.getDescription();
		return description;
	}

	public MultiIcon getIcon()
	{
		if (icon == null)
			return super.getIcon();
		return icon;
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
		 * Standard event handler that is called when a toolbar is (re-)generated.
		 * Adds the toolbar entries of this plugin.
		 *
		 * @event global.interaction.toolbar
		 * @param ie Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode toolbar(InteractionEvent ie)
		{
			if (ie.getSourcePlugin() != PropertyBrowserPlugin.this)
				return EVENT_IGNORED;

			JaspiraAction group;

			group = new JaspiraAction("propertybrowser.modify", null, null, null, null, 1, JaspiraAction.TYPE_GROUP);
			group.addToolbarChild(propertyBrowser.getAddAction());
			group.addToolbarChild(propertyBrowser.getCopyAction());
			group.addToolbarChild(propertyBrowser.getCutAction());
			group.addToolbarChild(propertyBrowser.getPasteAction());
			group.addToolbarChild(propertyBrowser.getRemoveAction());
			ie.add(group);

			group = new JaspiraAction("propertybrowser.order", null, null, null, null, 1, JaspiraAction.TYPE_GROUP);
			group.addToolbarChild(propertyBrowser.getMoveUpAction());
			group.addToolbarChild(propertyBrowser.getMoveDownAction());
			ie.add(group);

			group = new JaspiraAction("propertybrowser.save", null, null, null, null, 1, JaspiraAction.TYPE_GROUP);
			group.addToolbarChild(getAction("standard.file.save"));
			ie.add(group);

			return EVENT_HANDLED;
		}
	}

	//////////////////////////////////////////////////
	// @@ Event module
	//////////////////////////////////////////////////

	/**
	 * Event module.
	 */
	public class Events extends EventModule
	{
		public String getName()
		{
			return "plugin.propertybrowser";
		}

		//////////////////////////////////////////////////
		// @@ Global event handlers
		//////////////////////////////////////////////////

		/**
		 * Event method: Sets the object to be edited.
		 * This event will be sent by any plugin that wants the property browser to display an object.
		 *
		 * @event plugin.propertybrowser.setobject
		 * @param oe The event
		 * @return The event status code
		 */
		public synchronized JaspiraEventHandlerCode setobject(PropertyBrowserSetEvent oe)
		{
			// First, save the current object
			if (propertyBrowser != null)
			{
				if (!propertyBrowser.saveObject())
				{
					// There was an error saving the current object, abort
					return EVENT_CONSUMED;
				}
			}

			Object newObject = oe.getObject();

			if (!oe.reedit && newObject == getCurrentObject())
			{
				// We are alredy editing this exact object instance;
				return EVENT_CONSUMED;
			}

			// Prepare the plugin state that contains the working information for the property browser
			PropertyBrowserPluginState state = new PropertyBrowserPluginState(PropertyBrowserPlugin.this);
			state.unmodifiedObject = newObject;
			state.originalObject = oe.originalObject;
			state.modifiedObject = null;
			state.isObjectNew = oe.isObjectNew;
			state.title = oe.title;
			state.description = oe.description;
			state.icon = oe.icon;
			state.readOnly = oe.readOnly;
			state.saveImmediately = oe.saveImmediately;

			// This will transfer the plugin state to the oe plugin and will cause the property browser
			// to display the object if it is active
			setPluginState(state);

			return EVENT_CONSUMED;
		}

		/**
		 * Event method: Re-sets the current object (applying new object values or a new object descriptor maybe).
		 *
		 * @event plugin.propertybrowser.refresh
		 * @param je The event
		 * @return The event status code
		 */
		public synchronized JaspiraEventHandlerCode refresh(JaspiraEvent je)
		{
			// First, save the current object
			if (propertyBrowser != null)
			{
				if (!propertyBrowser.saveObject())
				{
					// There was an error saving the current object, abort
					// Do not consume the event, this should go to other property browsers, too
					return EVENT_HANDLED;
				}
			}

			// This will transfer the plugin state to the oe plugin and will cause the property browser
			// to display the object if it is active
			setPluginState(currentState);

			// Do not consume the event, this should go to other property browsers, too
			return EVENT_HANDLED;
		}
	}

	/**
	 * Event module that manages the save operation.
	 */
	public class SaveEvents extends EventModule
	{
		public String getName()
		{
			return "standard.file";
		}

		/**
		 * Gets the module priority.
		 * In cases where a higher-level editor (as the Modeler for example) contains an object
		 * editor plugin to edit a part of a structure, the oe's save command should be executed
		 * before the editor's save command, so assign it a priority higher than the standard priority 50.
		 *
		 * @return The priority. 0 is lowest, 100 is highest.
		 */
		public int getPriority()
		{
			return 25;
		}

		/////////////////////////////////////////////////////////////////////////
		// @@ EventHandling
		/////////////////////////////////////////////////////////////////////////

		/**
		 * Event handler: Save contents of the property browser (triggered by the standard toolbar).
		 *
		 * @event standard.file.save
		 * @param jae Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode save(JaspiraActionEvent jae)
		{
			if (!getPluginComponent().isShowing())
			{
				// Ignore if not visible
				return EVENT_IGNORED;
			}

			if (propertyBrowser != null)
			{
				propertyBrowser.saveObject();
				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
		}
	}

	/**
	 * Property browser override for usage within this plugin.
	 */
	private class PluginPropertyBrowser extends PropertyBrowserImpl
	{
		/**
		 * Default Constructor.
		 *
		 * @param saveStrategy Strategy class used to save the object
		 * @param res Resource containing the column headers
		 */
		public PluginPropertyBrowser(SaveStrategy saveStrategy, ResourceCollection res)
		{
			super(saveStrategy, res);
		}

		/**
		 * Save the object.
		 *
		 * Override of PropertyBrowserImpl.saveObject.
		 *
		 * @return
		 *	true	The object has been successfully saved or the change was discarded<br>
		 *	false	The object has not been saved, return
		 */
		public boolean saveObject()
		{
			boolean ret = super.saveObject();

			if (!ret)
			{
				// Surround the property browser with an error frame
				errorPane.setBorder(errorBorder);
			}
			else
			{
				// Clear the error frame
				errorPane.setBorder(null);
			}

			return ret;
		}
	}
}
