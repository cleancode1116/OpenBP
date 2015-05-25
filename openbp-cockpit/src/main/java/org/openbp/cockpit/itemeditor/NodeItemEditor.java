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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.JPanel;

import org.openbp.cockpit.generator.Generator;
import org.openbp.cockpit.generator.GeneratorMgr;
import org.openbp.cockpit.generator.GeneratorSettings;
import org.openbp.cockpit.generator.wizard.GeneratorWizard;
import org.openbp.cockpit.generator.wizard.WizardSelectionPage;
import org.openbp.cockpit.modeler.skins.Skin;
import org.openbp.cockpit.modeler.util.FigureUtil;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.activity.ActivityItem;
import org.openbp.core.model.item.process.ItemProvider;
import org.openbp.core.model.item.process.ItemSynchronization;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeProvider;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessItemImpl;
import org.openbp.guiclient.model.item.ItemEditorRegistry;
import org.openbp.jaspira.gui.wizard.JaspiraWizardPage;
import org.openbp.jaspira.plugin.ApplicationUtil;
import org.openbp.jaspira.plugin.PluginMgr;
import org.openbp.swing.components.wizard.WizardEvent;

/**
 * Item editor for modeler items.
 * This wizard displays two property pages as the standard item wizards does plus
 * an additional graphical editor page that lets the user define the layout and sockets
 * of the item he wants to create.
 *
 * Do not construct this class explicitely.
 * Instead, use the {@link ItemEditorRegistry#lookupItemEditor(String)} method to retrieve the
 * instance of the wizard and call {@link #openItem} on it.
 *
 * @author Heiko Erhardt
 */
public class NodeItemEditor extends StandardItemEditor
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** process name for the dummy process that holds the edited node */
	public static final String NODEEDITOR_PROCESS_NAME = "NodeEditorDummyProcess";

	/** Editor page of the wizard */
	public static final String EDITOR_PAGE = "editor";

	//////////////////////////////////////////////////
	// @@ Data Members
	//////////////////////////////////////////////////

	/** Editor page */
	private EditorPage editorWizardPage;

	/** The nodeEditor view. */
	protected NodeItemEditorPage editorJaspiraPage;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 * Do not invoke the constructor explicitely.
	 * The constructor is provided in order to let the {@link ItemEditorRegistry} instantiate
	 * this class. Use the {@link ItemEditorRegistry#lookupItemEditor(String)} method to retrieve the
	 * instance of the wizard and call {@link #openItem} on it.
	 */
	public NodeItemEditor()
	{
		super();
	}

	//////////////////////////////////////////////////
	// @@ StandardItemEditor overrides
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
		Item ret = super.openItem(item, editedItemStatus);

		disposeEditorJaspiraPage(editorWizardPage.getContentPanel());

		return ret;
	}

	/**
	 * Gets the initial size of the dialog.
	 * @nowarn
	 */
	protected Dimension getInitialSize()
	{
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

		// Hack: Subtract height to prevent the buttons from being obscured by the Windows task bar
		size.height -= 30;

		if (size.width > 900)
			size.width = 900;
		if (size.height > 800)
			size.height = 800;
		return size;
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

			// Add the node editor page
			if (editorWizardPage == null)
				editorWizardPage = new EditorPage(this);

			addAndLinkPage(EDITOR_PAGE, editorWizardPage);
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
		if (context.isNewItem())
		{
			// Start at the beginning if the item is a new one
			displayFirst();
		}
		else
		{
			// Existing item
			String page = null;

			// Check if we should display a particular page
			Generator generator = context.getSelectedGenerator();
			if (generator != null)
			{
				page = generator.getDefaultStartPageName();
			}

			if (page == null)
			{
				// Display the editor page by default
				page = EDITOR_PAGE;
			}

			displayPage(page);
		}
	}

	/**
	 * Fires a wizard event.
	 *
	 * @param event Event to fire
	 * @return
	 * true: Processing can be continued.<br>
	 * false: The cancel flag of the event has been set by an event listener.
	 */
	protected boolean fireWizardEvent(WizardEvent event)
	{
		if (event.eventType == WizardEvent.FINISH || event.eventType == WizardEvent.NEXT)
		{
			String pageName = event.wizard.getManager().getCurrent();
			if (pageName.equals(PROPERTY_PAGE))
			{
				// Customize the node structure and geometry according to the process type
				// when leaving the settings page
				if (context.isEmptyItem())
				{
					// The item was not edited visually yet, customize it

					Item item = context.getItem();

					if (item instanceof ActivityItem)
					{
						// Update the geometry (i. e. the size) of the item node according to the default skin
						FigureUtil.updateItemGeometry(null, item);
					}
				}
			}
		}

		return super.fireWizardEvent(event);
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Creates the Jaspira page that holds the node modeler plugin and its support plugins.
	 *
	 * @param contentPanel Content panel to add the new Jaspira page to
	 */
	void createEditorJaspiraPage(JPanel contentPanel)
	{
		// Add the standard pages
		editorJaspiraPage = (NodeItemEditorPage) PluginMgr.getInstance().createInstance(NodeItemEditorPage.class, null);

		getDndPane().addDropClient(editorJaspiraPage.getNodeEditor().getPluginPanel());

		// Add the editor component of the view to the wizard page
		contentPanel.add(editorJaspiraPage.getPluginComponent(), BorderLayout.CENTER);
	}

	/**
	 * Disposes the Jaspira page that holds the node modeler plugin and its support plugins.
	 *
	 * @param contentPanel Content panel to remove the Jaspira page from
	 */
	private void disposeEditorJaspiraPage(JPanel contentPanel)
	{
		contentPanel.removeAll();

		// This will remove the node editor's drawing view form the DnD pane of the dialog
		getDndPane().clearDropClients();

		// Uninstall the view
		PluginMgr.getInstance().removeInstance(editorJaspiraPage);
		editorJaspiraPage = null;
	}

	//////////////////////////////////////////////////
	// @@ Node editor page
	//////////////////////////////////////////////////

	/**
	 * Property page of the node editor wizard.
	 * Contains some kind of 'mini-modeler' that allows the user to edit a single node only.
	 */
	public class EditorPage extends JaspiraWizardPage
	{
		//////////////////////////////////////////////////
		// @@ Data members
		//////////////////////////////////////////////////

		/** Node that was created from the item */
		private Node node;

		//////////////////////////////////////////////////
		// @@ Construction
		//////////////////////////////////////////////////

		/**
		 * Default constructor.
		 *
		 * @param wizard Wizard that owns the page
		 */
		public EditorPage(NodeItemEditor wizard)
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
			Item item = context.getItem();

			if (event.eventType == WizardEvent.SHOW)
			{
				// This may take some time, so display the wait cursor
				ApplicationUtil.waitCursorOn();

				try
				{
					if (editorJaspiraPage == null)
					{
						createEditorJaspiraPage(getContentPanel());
					}

					// Convert the item to its respective node and add it to a dummy process
					ProcessItem dummyProcess = new ProcessItemImpl();
					dummyProcess.setName(NODEEDITOR_PROCESS_NAME);
					dummyProcess.setModel(item.getModel());

					// Determine which skin the process should use and initialize some process properties from the skin settings
					Skin processSkin = FigureUtil.determineProcessSkin(dummyProcess);
					processSkin.initalizeNewProcess(dummyProcess);

					node = ((NodeProvider) item).toNode(dummyProcess, ItemSynchronization.SYNC_ALL);

					dummyProcess.addNode(node);

					// Make the item editor display the node
					editorJaspiraPage.setNode(node);
				}
				finally
				{
					// Reset the wait cursor
					ApplicationUtil.waitCursorOff();
				}
			}
			else if (event.eventType == WizardEvent.FINISH || event.eventType == WizardEvent.CANCEL || event.eventType == WizardEvent.NEXT || event.eventType == WizardEvent.BACK)
			{
				if (event.eventType != WizardEvent.CANCEL)
				{
					// Cause the update of the editor's geometry information back to the node
					editorJaspiraPage.saveNode();

					// Copy the edited properties of the node back to the item
					int syncFlags = ItemSynchronization.SYNC_ALL;
					if (context.isEmptyItem())
					{
						syncFlags |= ItemSynchronization.SYNC_CLEAR_TARGET;
					}

					// Copy the sub process node properties back to the item
					((ItemProvider) node).copyToItem(item, syncFlags);
				}
			}
		}
	}
}
