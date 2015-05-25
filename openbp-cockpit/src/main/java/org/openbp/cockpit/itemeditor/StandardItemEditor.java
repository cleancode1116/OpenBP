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

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.openbp.cockpit.CockpitConstants;
import org.openbp.cockpit.generator.Generator;
import org.openbp.cockpit.generator.GeneratorContext;
import org.openbp.cockpit.generator.GeneratorMgr;
import org.openbp.cockpit.generator.GeneratorSettings;
import org.openbp.cockpit.generator.wizard.GeneratorWizard;
import org.openbp.cockpit.generator.wizard.WizardSelectionPage;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.generic.msgcontainer.StandardMsgContainer;
import org.openbp.common.generic.propertybrowser.PropertyDescriptor;
import org.openbp.common.icon.FlexibleSize;
import org.openbp.common.io.xml.XMLDriver;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.rc.ResourceCollectionMgr;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypeDescriptor;
import org.openbp.core.model.item.activity.PlaceholderItem;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessTypes;
import org.openbp.guiclient.model.ModelConnector;
import org.openbp.guiclient.model.item.ItemEditor;
import org.openbp.guiclient.model.item.ItemEditorRegistry;
import org.openbp.guiclient.model.item.ItemIconMgr;
import org.openbp.jaspira.gui.interaction.DragDropPane;
import org.openbp.jaspira.gui.interaction.DropableDialog;
import org.openbp.jaspira.gui.plugin.PluginFocusMgr;
import org.openbp.jaspira.gui.plugin.VisiblePlugin;
import org.openbp.jaspira.gui.wizard.JaspiraWizardObjectPage;
import org.openbp.jaspira.plugin.ApplicationUtil;
import org.openbp.jaspira.propertybrowser.NodeStructureMgr;
import org.openbp.jaspira.propertybrowser.PropertyBrowser;
import org.openbp.jaspira.propertybrowser.nodes.ObjectNode;
import org.openbp.jaspira.propertybrowser.nodes.PropertyNode;
import org.openbp.swing.components.JMsgBox;
import org.openbp.swing.components.wizard.WizardEvent;

/**
 * The standard item wizard displays a wizard that lets the user define an item.
 * Do not construct this class explicitely.
 * Instead, use the {@link ItemEditorRegistry#lookupItemEditor(String)} method to retrieve the
 * instance of the wizard and call {@link #openItem} on it.
 *
 * @author Heiko Erhardt
 */
public class StandardItemEditor extends GeneratorWizard
	implements ItemEditor
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	// The page names are also used to access the title and description resources:
	// wizard.pagename.title/wizard.itemtype.pagename.title
	// wizard.pagename.description/wizard.itemtype.pagename.description

	/** Item property page of the wizard */
	public static final String PROPERTY_PAGE = "property";

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Property page */
	private PropertyPage propertyPage;

	/** Dialog that displays the wizard */
	private DropableDialog dialog;

	/** DragDropPane of the wizard dialog responsible for DnD actions */
	private DragDropPane dndPane;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 * Do not invoke the constructor explicitely.
	 * The constructor is provided in order to let the {@link ItemTypeDescriptor} instantiate
	 * this class. Use the {@link ItemEditorRegistry#lookupItemEditor(String)} method to retrieve the
	 * instance of the wizard and call {@link #openItem} on it.
	 */
	public StandardItemEditor()
	{
		super(null);

		// Use the wizard resource from the 'Cockpit' resource component
		setWizardResource(ResourceCollectionMgr.getDefaultInstance().getResource(CockpitConstants.RESOURCE_COCKPIT, getClass()));

		if (propertyPage == null)
			propertyPage = new PropertyPage(this);

		// Create a modal dialog that displays the wizard and supports Jaspira DnD
		JFrame activeFrame = ApplicationUtil.getActiveWindow();
		dialog = new DropableDialog(activeFrame, true);

		// Initialize drag and drop
		DragDropPane.installDragDropPane(dialog);
		dndPane = dialog.getDragDropPane();

		dialog.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				fireWizardEvent(WizardEvent.CANCEL);
			}
		});

		dialog.getContentPane().add(this);
	}

	/**
	 * Adds the property page of the standard item wizard to the wizard.
	 */
	protected void addPropertyPage()
	{
		addAndLinkPage(PROPERTY_PAGE, propertyPage);
	}

	/**
	 * Gets the initial size of the dialog.
	 * @nowarn
	 */
	protected Dimension getInitialSize()
	{
		return new Dimension(800, 600);
	}

	//////////////////////////////////////////////////
	// @@ GeneratorWizard overrides
	//////////////////////////////////////////////////

	/**
	 * Sets up the wizard pages.
	 */
	protected void setupPages()
	{
		// This may take some time, so display the wait cursor
		ApplicationUtil.waitCursorOn();

		try
		{
			clearPages();

			Item item = context.getItem();

			// Add the generator selection page only if the item does not contain generator info
			if (context.getSelectedGenerator() == null)
			{
				// Check if there are any generators defined for this item type that operate on items of this type
				List list = GeneratorMgr.getInstance().getGenerators(item.getItemType(), true);
				if (list != null)
				{
					// Create the selection page displaying all available
					// generators and add it to the wizard
					int n = list.size();
					if (n > 1)
					{
						WizardSelectionPage selectionPage = new WizardSelectionPage(this);

						for (int i = 0; i < n; ++i)
						{
							Generator generator = (Generator) list.get(i);
							selectionPage.addGenerator(generator);
						}

						selectionPage.expandTree();
						addAndLinkPage(GeneratorWizard.SELECTION_PAGE, selectionPage);
					}
					else if (n == 1)
					{
						Generator gen = (Generator) list.get(0);
						if (gen != null)
						{
							// Initialize the generator context and settings as if we would have gotten them from an item of newer type
							context.setSelectedGenerator(gen);

							GeneratorSettings settings = new GeneratorSettings();
							settings.setGeneratorName(gen.getName());
							context.setGeneratorSettings(settings);
							context.setProperty(GeneratorWizard.SETTINGS_PAGE, settings);
						}
					}
				}
			}

			// Add the property page of the standard item wizard
			addPropertyPage();
		}
		finally
		{
			// Reset the wait cursor
			ApplicationUtil.waitCursorOff();
		}
	}

	/**
	 * Sets up the initial page position.
	 * Default: First page.
	 */
	protected void setupPosition()
	{
		displayFirst();
	}

	//////////////////////////////////////////////////
	// @@ ItemEditor implementation
	//////////////////////////////////////////////////

	/**
	 * Opens an item in the item wizard.
	 * This method usually displays the wizard dialog.
	 * The item wizard displays and updates the item structure and/or advanced item properties.
	 *
	 * @param item item to open
	 * @param editedItemStatus Status of the item
	 * @return The edited item or null if the user cancelled the wizard
	 */
	public Item openItem(Item item, int editedItemStatus)
	{
		boolean isNewItem = editedItemStatus == EditedItemStatus.NEW || editedItemStatus == EditedItemStatus.COPIED;

		ImageIcon icon = (ImageIcon) ItemIconMgr.getInstance().getIcon(item, FlexibleSize.HUGE);
		setDefaultWizardImage(icon);

		// Use the item type as optional resource prefix for page titles and descriptions
		setPageResourcePrefix(item.getItemType().toLowerCase());

		// Get the dialog title from the wizard resource
		String title = getPageResourceString(null, "title");
		dialog.setTitle(title);

		// Initializes the subect.
		context = new GeneratorContext();
		if (!isNewItem)
		{
			// Save the original item for name uniqueness check reference and status restoration
			context.setOriginalItem(item);

			// Clone existing items in order to prevent modifications to the original
			try
			{
				item = (Item) item.clone();
			}
			catch (CloneNotSupportedException e)
			{
				ExceptionUtil.printTrace(e);
			}

			// This should be done after the clone in order to rebuild the internal references
			// (i. e. from node sockets to their connected control links etc.)
			item.maintainReferences(ModelObject.RESOLVE_GLOBAL_REFS | ModelObject.RESOLVE_LOCAL_REFS);
		}

		context.setItem(item);
		context.setNewItem(isNewItem);
		if (editedItemStatus == EditedItemStatus.NEW)
		{
			context.setEmptyItem(true);
		}

		// Read any generator info from the item and adjust the generator pages accordingly
		if (!parseGeneratorInfo(item))
		{
			if (item instanceof ProcessItem)
			{
				// If a process type is given, we know our generator.
				determineGeneratorFromProcessType((ProcessItem) item);
			}
		}

		if (item instanceof PlaceholderItem)
		{
			dialog.setSize(new Dimension(600, 450));
		}
		else
		{
			dialog.setSize(getInitialSize());
		}
		dialog.setLocationRelativeTo(ApplicationUtil.getActiveWindow());

		// Initialize the wizard pages and initial position
		setupPages();

		// Update the wizard according to the selected generator, if any
		updateGeneratorPageSequence();

		// Initialize the initial position
		setupPosition();

		// Save the current focus
		VisiblePlugin focusedPlugin = PluginFocusMgr.getInstance().getFocusedPlugin();

		// Show the dialog
		dialog.setVisible(true);

		if (focusedPlugin != null)
		{
			// Restore the current focus
			focusedPlugin.focusPlugin();
		}

		// The the edited item and reset the item member for garbage collection
		Item ret = context.getItem();
		context = null;
		return ret;
	}

	/**
	 * Parses the item's generator info.
	 *
	 * @param item Item to parse
	 * @return
	 * true: The item contained valid generator info.<br>
	 * false: The item does not contain generator info.
	 */
	protected boolean parseGeneratorInfo(Item item)
	{
		// This will ensure that any generator setting classes are registered
		// with the XML driver, so they can be deserialized.
		GeneratorMgr.getInstance();

		String generatorInfo = item.getGeneratorInfo();
		if (generatorInfo != null)
		{
			// Prepend xml method tag
			generatorInfo = "<?xml version=\"1.0\" encoding=\"" + XMLDriver.getInstance().getEncoding() + "\"?>" + generatorInfo;

			ByteArrayInputStream is = new ByteArrayInputStream(generatorInfo.getBytes());

			GeneratorSettings settings;
			try
			{
				settings = (GeneratorSettings) XMLDriver.getInstance().deserializeStream(GeneratorSettings.class, is);

				// Perform post-processing
				StandardMsgContainer msgs = new StandardMsgContainer();
				settings.setModel(item.getModel());
				settings.afterDeserialization(msgs);
				if (!msgs.isEmpty())
				{
					// Report errors that occured during the post-processing
					JMsgBox.show(null, msgs.toString(), JMsgBox.TYPE_OKLATER | JMsgBox.ICON_ERROR);
				}
			}
			catch (XMLDriverException e)
			{
				ExceptionUtil.printTrace(e);
				return false;
			}

			String name = settings.getGeneratorName();
			Generator gen = GeneratorMgr.getInstance().getGenerator(name);
			if (gen != null)
			{
				context.setSelectedGenerator(gen);
				context.setGeneratorSettings(settings);
				context.setProperty(GeneratorWizard.SETTINGS_PAGE, settings);
			}

			return true;
		}

		return false;
	}

	/**
	 * Determines the type of generator to be used according to the process type.
	 * Needed when the process type has been specified up front when creating new processes.
	 *
	 * @param item Item to check
	 */
	private void determineGeneratorFromProcessType(ProcessItem item)
	{
		String processType = item.getProcessType();
		if (processType != null)
		{
			String generatorName = "Process" + processType;

			Generator gen = GeneratorMgr.getInstance().getGenerator(generatorName);
			if (gen != null)
			{
				// Initialize the generator context and settings as if we would have gotten them from an item of newer type
				context.setSelectedGenerator(gen);

				GeneratorSettings settings = new GeneratorSettings();
				settings.setGeneratorName(generatorName);
				context.setGeneratorSettings(settings);
				context.setProperty(GeneratorWizard.SETTINGS_PAGE, settings);
			}
		}
	}

	/**
	 * For compatibility with pre-2\.0 process items: Ensures that a process item has a process type associated with it.
	 * Will assign the default process type 'UserInterface' to any process item that don't have a process type.
	 *
	 * @param item Item (can be a process item or any other)
	 */
	public static void ensureProcessType(Item item)
	{
		if (item instanceof ProcessItem)
		{
			ProcessItem process = (ProcessItem) item;
			if (process.getProcessType() == null)
			{
				process.setProcessType(ProcessTypes.USERINTERFACE);
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the dialog that displays the wizard.
	 * @nowarn
	 */
	public DropableDialog getDialog()
	{
		return dialog;
	}

	/**
	 * Gets the dragDropPane of the wizard dialog responsible for DnD actions.
	 * @nowarn
	 */
	public DragDropPane getDndPane()
	{
		return dndPane;
	}

	//////////////////////////////////////////////////
	// @@ Property page
	//////////////////////////////////////////////////

	/**
	 * Property page of the node editor wizard.
	 * Contains an property browser that is used to edit the properties of the node.
	 */
	private class PropertyPage extends JaspiraWizardObjectPage
	{
		/**
		 * Default constructor.
		 *
		 * @param wizard Wizard that owns the page
		 */
		public PropertyPage(StandardItemEditor wizard)
		{
			super(wizard);

			canFinish = true;
		}

		/**
		 * Handles a wizard event caused by this wizard page.
		 *
		 * @param event Event to handle
		 */
		public void handleWizardEvent(WizardEvent event)
		{
			if (event.eventType == WizardEvent.SHOW)
			{
				initializePropertyBrowser();
			}
			else if (event.eventType == WizardEvent.FINISH || event.eventType == WizardEvent.NEXT || event.eventType == WizardEvent.BACK)
			{
				boolean failed = false;
				PropertyBrowser oe = getPropertyBrowser();
				if (!oe.saveObject())
				{
					failed = true;
					if (event.eventType == WizardEvent.BACK)
					{
						// We will ignore error if the user hit the back button
						clearPropertyBrowser();
					}
					else
					{
						// Save failed, don't continue
						event.cancel = true;
						return;
					}
				}
				else
				{
					clearPropertyBrowser();
				}

				Item item = context.getItem();

				if (context.isNewItem() && !context.isItemSaved() && !failed)
				{
					// Check if the item already exists if not created yet
					try
					{
						ModelQualifier itemQualifier = item.getQualifier();

						if (item instanceof Model)
						{
							if (ModelConnector.getInstance().getOptionalModelByQualifier(itemQualifier) != null)
							{
								// TOLOCALIZE already exists
								String msg = "The model '" + itemQualifier + "' already exists.\nPlease choose another name.";
								JMsgBox.show(null, msg, JMsgBox.ICON_WARNING);
								event.cancel = true;
							}
						}
						else
						{
							if (ModelConnector.getInstance().getItemByQualifier(itemQualifier, false) != null)
							{
								// TOLOCALIZE
								String msg = "The component '" + itemQualifier + "' already exists.\nPlease choose another name.";
								JMsgBox.show(null, msg, JMsgBox.ICON_WARNING);
								event.cancel = true;
							}
						}
					}
					catch (Exception e)
					{
						// Doesn't happen
					}
				}
			}
			else if (event.eventType == WizardEvent.CANCEL)
			{
				clearPropertyBrowser();
			}
		}

		/**
		 * Called by the property browser when the object needs to be saved.
		 *
		 * @param pb Property browser
		 * @return
		 * true: Save successful.<br>
		 * false: Save failed.
		 */
		public boolean executeSave(PropertyBrowser pb)
		{
			Item modifiedItem = (Item) pb.getModifiedObject();

			try
			{
				context.getItem().copyFrom(modifiedItem, Copyable.COPY_FIRST_LEVEL);
			}
			catch (CloneNotSupportedException e)
			{
				ExceptionUtil.printTrace(e);
			}

			return true;
		}

		/**
		 * Sets the object to edit in the property browser.
		 */
		private void initializePropertyBrowser()
		{
			GeneratorContext context = getContext();
			Item item = context.getItem();

			PropertyBrowser oe = getPropertyBrowser();
			oe.setObjectModified(false);
			oe.setOriginalObject(context.getOriginalItem());

			try
			{
				if (context.isNewItem() && !context.isItemSaved())
				{
					// Make the object name editable if it's a new item

					// Create the property browser tree and clone it
					ObjectNode rootNode = NodeStructureMgr.getInstance().createEditorStructureFor(item.getClass());

					// Set the read-only property of the 'Name' property to false
					changeReadOnlyModeOfPropertyDescriptor(rootNode, "Name", false);
					// TODO Feature 3 Allow for selecting the model for new items; implement setOwningModelQualifier
					// changeReadOnlyModeOfPropertyDescriptor(rootNode, "OwningModelQualifier", false);

					// Set the item; we consider it new (needed for the ModelObjectValidator, which
					// checks name uniqueness) only if the item has not been saved yet.
					boolean newFlag = !context.isItemSaved();
					oe.setObject(item, newFlag, rootNode);
				}
				else
				{
					oe.setObject(item, false);
				}
			}
			catch (XMLDriverException e)
			{
				ExceptionUtil.printTrace(e);
			}
			catch (CloneNotSupportedException e)
			{
				ExceptionUtil.printTrace(e);
			}
		}

		/**
		 * Changes the 'read only' mode of property descriptor.
		 *
		 * @param rootNode Rooot node that contains the property descriptor
		 * @param propertyName Name of the property to change
		 * @param readOnly
		 * true: Change to read only.<br>
		 * false: Change to modifyable
		 */
		private void changeReadOnlyModeOfPropertyDescriptor(ObjectNode rootNode, String propertyName, boolean readOnly)
		{
			PropertyNode propertyNode = (PropertyNode) rootNode.findNodeForProperty(propertyName);
			if (propertyNode != null)
			{
				// The clone method of the ObjectNode doesn't clone
				// the property descriptor, so we have to do this manually.
				PropertyDescriptor pd = propertyNode.getPropertyDescriptor();
				try
				{
					pd = (PropertyDescriptor) pd.clone();
					pd.setReadOnly(readOnly);
					propertyNode.setPropertyDescriptor(pd);
				}
				catch (CloneNotSupportedException e)
				{
					ExceptionUtil.printTrace(e);
				}
			}
		}

		/**
		 * Clears the property browser.
		 */
		private void clearPropertyBrowser()
		{
			PropertyBrowser oe = getPropertyBrowser();
			try
			{
				// Clear the property browser for later use
				oe.setObjectModified(false);
				oe.setObject(null, false);
				oe.setOriginalObject(null);
			}
			catch (XMLDriverException e)
			{
				// Never happens
			}
			catch (CloneNotSupportedException e)
			{
				// Never happens
			}
		}
	}
}
