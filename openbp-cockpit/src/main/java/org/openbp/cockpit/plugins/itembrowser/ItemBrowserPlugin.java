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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.openbp.cockpit.itemeditor.EditedItemStatus;
import org.openbp.cockpit.itemeditor.ItemCreationUtil;
import org.openbp.cockpit.itemeditor.StandardItemEditor;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.icon.FlexibleSize;
import org.openbp.common.icon.MultiIcon;
import org.openbp.common.io.xml.XMLDriver;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.string.StringUtil;
import org.openbp.common.util.observer.EventObserver;
import org.openbp.common.util.observer.ObserverEvent;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelObjectSymbolNames;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypeDescriptor;
import org.openbp.core.model.item.ItemTypeRegistry;
import org.openbp.core.model.item.activity.JavaActivityItem;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.guiclient.model.ModelConnector;
import org.openbp.guiclient.model.item.ItemEditor;
import org.openbp.guiclient.model.item.ItemEditorRegistry;
import org.openbp.guiclient.model.item.ItemIconMgr;
import org.openbp.guiclient.model.item.ItemTransferable;
import org.openbp.guiclient.model.item.ItemUtil;
import org.openbp.guiclient.model.item.itemfilter.ItemFilter;
import org.openbp.guiclient.model.item.itemfilter.ItemFilterListener;
import org.openbp.guiclient.model.item.itemfilter.ItemFilterMgr;
import org.openbp.guiclient.model.item.itemfilter.ItemTextFilter;
import org.openbp.guiclient.model.item.itemfilter.ItemTypeFilter;
import org.openbp.guiclient.model.item.itemtree.ItemTree;
import org.openbp.guiclient.model.item.itemtree.ItemTreeEvent;
import org.openbp.guiclient.model.item.itemtree.ItemTreeListener;
import org.openbp.guiclient.util.ClientFlavors;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraToolbar;
import org.openbp.jaspira.event.AskEvent;
import org.openbp.jaspira.event.InteractionEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.gui.interaction.BasicTransferable;
import org.openbp.jaspira.gui.interaction.DragInitiator;
import org.openbp.jaspira.gui.interaction.DragOrigin;
import org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin;
import org.openbp.jaspira.plugin.ApplicationUtil;
import org.openbp.jaspira.plugin.InteractionModule;
import org.openbp.jaspira.plugins.propertybrowser.PropertyBrowserSetEvent;

/**
 * The item browser displays the models of the system and their items in a tree structure.
 * It supports dragging an item (e. g. onto the workspace).<br>
 * The items can be filtered by the installed item filters (currently item type and
 * item text filter). The filter components (used to adjust the filters) are shown
 * in a panel above the tree and can be turned on and off using the item filter buttons
 * in the plugin's toolbar.
 *
 * @author Jens Ferchland
 */
public class ItemBrowserPlugin extends AbstractVisiblePlugin
	implements EventObserver, DragOrigin, ItemTreeListener, FocusListener
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Flag for getSelectedModel: Guess model from selected item */
	public static final int GUESS_MODEL = (1 << 0);

	/** Flag for getSelectedModel: Use model of current process if none selected in the item browser */
	public static final int USE_CURRENT_MODEL = (1 << 1);

	/** Tree used to display the items */
	ItemTree itemTree;

	/** Item filter manager */
	private ItemFilterMgr filterMgr;

	/** Item type filter */
	private ItemTypeFilter itemTypeFilter;

	/** Item types supported by this browser (null for the standard item types) */
	private String [] supportedItemTypes;

	/** Model qualifier of the process edited in this instance of the modeler */
	ModelQualifier currentProcessQualifier;

	/** Current item drag image */
	private MultiIcon dragImage;

	/** Display context menu wait cursor */
	private boolean displayContextMenuWaitCursor = true;

	//////////////////////////////////////////////////
	// @@ Actions and toolbar
	//////////////////////////////////////////////////

	/** Action for removing processes */
	private JaspiraAction removeAction;

	/** Action for running models in the external web  browser */
	private JaspiraAction runAction;

	/** Toolbar of the plugin */
	private JaspiraToolbar toolbar;

	/////////////////////////////////////////////////////////////////////////
	// @@ Plugin overrides
	/////////////////////////////////////////////////////////////////////////

	public String getResourceCollectionContainerName()
	{
		return "plugin.cockpit";
	}

	/**
	 * @see org.openbp.jaspira.plugin.AbstractPlugin#getExternalEventModuleClasses()
	 */
	protected Class [] getExternalEventModuleClasses()
	{
		return new Class [] { ItemBrowserModule.class };
	}

	/**
	 * @see org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin#hasCloseButton()
	 */
	public boolean hasCloseButton()
	{
		return false;
	}

	/**
	 * @see org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin#initializeComponents()
	 */
	protected void initializeComponents()
	{
		getContentPane().removeAll();

		setupActions();
		setupItemFilter();
		setupItemTree();

		// Build the tree using the model root
		rebuildTree();

		getContentPane().add(new JScrollPane(itemTree), BorderLayout.CENTER);

		updateActionStatus(null);

		ModelConnector.getInstance().registerObserver(this, null);

		addPluginFocusListener(this);
	}

	private void setupItemFilter()
	{
		// Set up the item filter
		filterMgr = new ItemFilterMgr(this);

		// We will respond to filter messages by rebuilding the item tree
		filterMgr.setFilterListener(new ItemFilterListener()
		{
			public void applyFilter(ItemFilterMgr mgr, ItemFilter filter)
			{
				rebuildTree();

				if (filter instanceof ItemTextFilter)
				{
					// When applying a text filter, fully expand the item tree
					itemTree.expand(3);
				}
			}
		});

		// Add the item type filter
		itemTypeFilter = new ItemTypeFilter();
		if (supportedItemTypes != null)
		{
			itemTypeFilter.setSupportedItemTypes(supportedItemTypes);
		}
		filterMgr.addFilter(itemTypeFilter);

		// Add the item text filter
		filterMgr.addFilter(new ItemTextFilter());

		// Set up the item filter pane
		JComponent filterPane = filterMgr.getConfigurationPane();
		getContentPane().add(filterPane, BorderLayout.NORTH);
	}

	private void setupItemTree()
	{
		// Set up the item tree
		itemTree = new ItemTree();
		DragInitiator.makeDraggable(itemTree, this);

		// Make all item types selectable by default
		String [] selectableItemTypes = ModelConnector.getInstance().getItemTypes(ItemTypeRegistry.SKIP_INVISIBLE);
		itemTree.setSelectableItemTypes(selectableItemTypes);
		itemTree.setSupportedItemTypes(supportedItemTypes);

		itemTree.setSelectionMode(ItemTree.SELECTION_SINGLE);

		// In order to save space, make the '+' signs at top level disappear
		itemTree.setRootVisible(false);

		itemTree.addItemTreeListener(this);
		itemTree.setFilterMgr(filterMgr);
		itemTree.setItemTypeFilter(itemTypeFilter);

		setupKeyBindings();
	}

	private void setupActions()
	{
		// Get the action references
		removeAction = getAction("plugin.itembrowser.remove");
		runAction = getAction("plugin.itembrowser.run");
	}

	/**
	 * Updates the enable/disable status of the actions of this plugin according to the current selection.
	 *
	 * @param item Current item or null
	 */
	private void updateActionStatus(Item item)
	{
		Model model = item != null ? item.getOwningModel() : null;

		if (removeAction != null)
		{
			removeAction.setEnabled(item != null && model.isModifiable());
		}

		if (runAction != null)
		{
			runAction.setEnabled(model != null);
		}
	}

	/**
	 * Called before the plugin is uninstalled.
	 */
	protected void pluginUninstalled()
	{
		ModelConnector.getInstance().unregisterObserver(this);
	}

	/**
	 * @see org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin#getPluginFocusComponent()
	 */
	public Component getPluginFocusComponent()
	{
		return itemTree;
	}

	/**
	 * @see org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin#createToolbar()
	 */
	public JaspiraToolbar createToolbar()
	{
		if (toolbar == null)
		{
			toolbar = new JaspiraToolbar();

			toolbar.add(filterMgr.getFilterToolbar());

			JaspiraAction action = getAction("plugin.itembrowser.togglefunctionalgroup");
			if (action != null)
			{
				toolbar.addSeparator();
				action.setEnabled(true);
				toolbar.add(action);
			}
		}

		return toolbar;
	}

	/**
	 * Called after the application has been initialized AND the plugin initialization is finished.
	 */
	public void applicationReady()
	{
		super.applicationReady();

		itemTree.setSelectionRow(0);

		// We want the model browser to have the focus initially
		focusPlugin();
	}

	//////////////////////////////////////////////////
	// @@ Internal functions
	//////////////////////////////////////////////////

	/**
	 * Gets the selected model.
	 *
	 * @return The selected model or the model of the selected item or null
	 */
	public Model getSelectedModel(int flag)
	{
		Model ret = null;

		if ((flag & ItemBrowserPlugin.GUESS_MODEL) != 0)
		{
			ret = itemTree.determineSelectedModel();
			if (ret != null)
				return ret;
		}

		List list = itemTree.getSelectedObjects();
		if (list != null)
		{
			int n = list.size();
			for (int i = 0; i < n; ++i)
			{
				Object o = list.get(i);

				if (o instanceof Model)
				{
					return (Model) o;
				}
			}
		}

		if (ret == null && (flag & ItemBrowserPlugin.USE_CURRENT_MODEL) != 0)
		{
			if (currentProcessQualifier != null)
			{
				// Use the modeler processes' current model
				ModelQualifier modelQualifier = ModelQualifier.constructModelQualifier(currentProcessQualifier.getModel());
				ret = ModelConnector.getInstance().getOptionalModelByQualifier(modelQualifier);
			}
		}

		return ret;
	}

	/**
	 * Gets the selected item.
	 *
	 * @return The selected item or the item of the selected item or null
	 */
	public Item getSelectedItem()
	{
		Item ret = null;

		List list = itemTree.getSelectedObjects();
		if (list != null)
		{
			int n = list.size();
			for (int i = 0; i < n; ++i)
			{
				Object o = list.get(i);

				if (o instanceof Item)
				{
					Item item = (Item) o;

					if (ret != null && ret != item)
					{
						// More than 1 item selected will be treated as 'no item selected'.
						return null;
					}

					ret = item;
				}
			}
		}

		return ret;
	}

	/**
	 * Sets the currently selected object.
	 *
	 * @param object Object to select (item or model) or null
	 */
	public void setSelectedObject(Object object)
	{
		if (object != null)
		{
			ArrayList list = new ArrayList();
			list.add(object);
			itemTree.setSelectedObjects(list);
		}
		else
		{
			itemTree.setSelectedObjects(null);
		}
	}

	/**
	 * Gets the item types supported by this browser (null for the standard item types).
	 * @nowarn
	 */
	public String [] getSupportedItemTypes()
	{
		return supportedItemTypes;
	}

	/**
	 * Sets the item types supported by this browser (null for the standard item types).
	 * Call this method before the plugin is installed (best use the constructor of derived classes).
	 * @nowarn
	 */
	public void setSupportedItemTypes(String [] supportedItemTypes)
	{
		this.supportedItemTypes = supportedItemTypes;
	}

	/**
	 * Rebuilds the model/item tree.
	 */
	protected void rebuildTree()
	{
		itemTree.rebuildTree();
	}

	/**
	 * Selects the element that should be selected if the currently selected element is deleted.
	 * Searches downwards in the current level first, then upwards and then tries the parent level
	 */
	public void performAlternativeSelection()
	{
		itemTree.performAlternativeSelection();
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Plugin overrides: Clipboard support
	/////////////////////////////////////////////////////////////////////////

	// TODONOW Delete

	public boolean canCopy()
	{
		// We can copy/cut if there is an item on the clipboard.
		// Not supported for models.
		Item item = getSelectedItem();
		return item != null && !(item instanceof Model);
	}

	public boolean canCut()
	{
		return canCopy();
	}

	public boolean canPaste(Transferable transferable)
	{
		// We can paste if there is an item on the clipboard
		Model model = getSelectedModel(ItemBrowserPlugin.GUESS_MODEL | ItemBrowserPlugin.USE_CURRENT_MODEL);
		if (model != null && model.isModifiable() && transferable != null && transferable.isDataFlavorSupported(ClientFlavors.ITEM))
			return true;
		return false;
	}

	public Transferable copy()
	{
		Item item = getSelectedItem();
		return new ItemTransferable(item);
	}

	public Transferable cut()
	{
		Item item = getSelectedItem();

		// Select something different than the current node
		performAlternativeSelection();

		try
		{
			ModelConnector.getInstance().removeItem(item);
		}
		catch (ModelException ex)
		{
			ExceptionUtil.printTrace(ex);
			return null;
		}

		return new ItemTransferable(item);
	}

	public void paste(Transferable content)
	{
		try
		{
			if (content.isDataFlavorSupported(ClientFlavors.ITEM))
			{
				// We need to copy the item from the clipboard, since we are about to modify it.
				Item source = (Item) content.getTransferData(ClientFlavors.ITEM);

				// Serialize to a byte array
				ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
				XMLDriver.getInstance().serialize(source, os);
				byte[] bytes = os.toByteArray();
				ByteArrayInputStream is = new ByteArrayInputStream(bytes);
				Item item = (Item) XMLDriver.getInstance().deserializeStream(null, is);

				item.setGeneratorInfo(source.getGeneratorInfo());

				// TODONOW Item item = (Item) source.clone();

				Model model = getSelectedModel(ItemBrowserPlugin.GUESS_MODEL | ItemBrowserPlugin.USE_CURRENT_MODEL);
				item.setModel(model);

				item.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.RESOLVE_LOCAL_REFS);

				// Find a new name for the item; we generate the name by appending a running number to the item type
				String oldName = item.getName();
				ItemUtil.ensureUniqueItemName(item, model);
				String newName = item.getName();

				if (item instanceof JavaActivityItem)
				{
					// In case of activity items, change the name of the action
					// implementation class also when renaming the activity
					JavaActivityItem activity = (JavaActivityItem) item;
					String oldClassName = activity.getHandlerDefinition().getHandlerClassName();
					if (oldClassName != null && oldName != null)
					{
						// ...substitute ".OldName" to ".NewName"
						String className = StringUtil.substitute(oldClassName, "." + oldName, "." + newName);

						// Update the object
						activity.getHandlerDefinition().setHandlerClassName(className);
					}
				}

				// Get the item type editor associated with this item
				ItemEditor editor = ItemEditorRegistry.getInstance().lookupItemEditor(item.getItemType());
				if (editor != null)
				{
					// For compatibility with pre-2.0 process items
					StandardItemEditor.ensureProcessType(item);

					// Open the item wizard for the item
					final Item newItem = editor.openItem(item, EditedItemStatus.COPIED);
					if (newItem != null)
					{
						if (ModelConnector.getInstance().saveItem(item, true))
						{
							SwingUtilities.invokeLater(new Runnable()
							{
								public void run()
								{
									ItemBrowserPlugin.this.setSelectedObject(newItem);
								}
							});
						}
					}
				}
			}
		}
		catch (XMLDriverException e)
		{
			ExceptionUtil.printTrace(e);
		}
		catch (UnsupportedFlavorException e)
		{
			ExceptionUtil.printTrace(e);
		}
		catch (IOException e)
		{
			ExceptionUtil.printTrace(e);
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ DragOrigin implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.gui.interaction.DragOrigin#canDrag()
	 */
	public boolean canDrag()
	{
		return true;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.DragOrigin#dropAccepted(Transferable)
	 */
	public void dropAccepted(Transferable t)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.DragOrigin#dropCanceled(Transferable)
	 */
	public void dropCanceled(Transferable t)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.DragOrigin#dropPerformed(Transferable)
	 */
	public void dropPerformed(Transferable t)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.DragOrigin#getTranferableAt(Point)
	 */
	public Transferable getTranferableAt(Point p)
	{
		TreePath path = itemTree.getPathForLocation(p.x, p.y);

		if (path == null)
			return null;

		TreeNode treeNode = (TreeNode) path.getLastPathComponent();
		if (treeNode instanceof ItemTree.ItemNode)
		{
			ItemTree.ItemNode itemnode = (ItemTree.ItemNode) treeNode;

			Item item = itemnode.getItem();
			dragImage = ItemIconMgr.getMultiIcon(ItemIconMgr.getInstance().getIcon(item, FlexibleSize.MEDIUM));
			return new BasicTransferable(item);
		}

		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.DragOrigin#getDragImage()
	 */
	public MultiIcon getDragImage()
	{
		return dragImage;
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
		// public JaspiraEventHandlerCode toolbar (InteractionEvent ie)
		// {
		// 	return addActions (ie, false, false) ? EVENT_HANDLED : EVENT_IGNORED;
		// }

		/**
		 * Standard event handler that is called when a popup menu is to be shown.
		 * Adds the popup menu entries for models.
		 *
		 * @event global.interaction.popup
		 * @param ie Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode popup(InteractionEvent ie)
		{
			return addActions(ie, true, true) ? EVENT_HANDLED : EVENT_IGNORED;
		}

		/**
		 * Adds the actions of the plugin to the interaction event.
		 *
		 * @param ie Interaction event
		 * @param isMenu
		 * true: Add the actions as menu actions<br>
		 * false: Add the actions as toolbar actions
		 * @param isPopup
		 * true: We are creating a popup menu<br>
		 * false: We are creating a regular menu or a toolbar
		 * @return
		 * true: For return code EVENT_HANDLED<br>
		 * false: For return code EVENT_IGNORED
		 */
		private boolean addActions(InteractionEvent ie, boolean isMenu, boolean isPopup)
		{
			if (ie.getSourcePlugin() != ItemBrowserPlugin.this)
				return false;

			boolean haveModel = ie.isDataFlavorSupported(ClientFlavors.MODEL);
			boolean haveItem = ie.isDataFlavorSupported(ClientFlavors.ITEM);
			boolean haveProcess = ie.isDataFlavorSupported(ClientFlavors.PROCESS_ITEM);

			JaspiraAction group;

			if (isMenu)
			{
				// New item group
				group = new JaspiraAction(ItemBrowserPlugin.this, "submenu.new");

				addNewItemActions(group);

				ie.add(group);
			}

			// Manage items group
			group = new JaspiraAction("popup.itembrowser.manage", null, null, null, null, 1, JaspiraAction.TYPE_GROUP);

			if (removeAction != null)
			{
				if (isMenu)
				{
					if (isPopup && (haveModel | haveItem))
					{
						group.addMenuChild(removeAction);
					}
				}
				else
				{
					group.addToolbarChild(removeAction);
				}
			}

			ie.add(group);

			// Item extras group
			group = new JaspiraAction("popup.itembrowser.extras", null, null, null, null, 2, JaspiraAction.TYPE_GROUP);

			if (isMenu)
			{
				if (isPopup && (haveModel | haveProcess))
				{
					if (runAction != null)
					{
						group.addMenuChild(runAction);
					}
				}
			}
			else
			{
				if (runAction != null)
				{
					group.addToolbarChild(runAction);
				}
			}

			ie.add(group);

			return true;
		}
	}

	//////////////////////////////////////////////////
	// @@ FocusListener implementation
	//////////////////////////////////////////////////

	/**
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	public void focusGained(FocusEvent e)
	{
		// Display current item in property browser
		Item item = getSelectedItem();
		firePropertyBrowserEvent(item);
	}

	/**
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	public void focusLost(FocusEvent e)
	{
	}

	//////////////////////////////////////////////////
	// @@ ItemTreeListener implementation
	//////////////////////////////////////////////////

	/**
	 * Called when an item tree event has happened.
	 *
	 * @param e Item tree event holding the event information
	 */
	public void handleItemTreeEvent(ItemTreeEvent e)
	{
		if (e.eventType == ItemTreeEvent.SELECTION_CHANGED)
		{
			Item item = getSelectedItem();

			// Update buttons and context menu
			updateActionStatus(item);

			// Update cut/copy/past buttons
			fireEvent("global.clipboard.updatestatus");

			// Display in property browser
			firePropertyBrowserEvent(item);
		}

		else if (e.eventType == ItemTreeEvent.POPUP)
		{
			// Select the object below the cursor first

			// Right-click
			Item item = getSelectedItem();

			MouseEvent me = e.mouseEvent;
			showPopup(item, me.getComponent(), me.getX(), me.getY());
		}

		else if (e.eventType == ItemTreeEvent.OPEN)
		{
			// Double-click or ENTER -> Open the item
			// (except for models; double-clicking a model expands the tree)
			Item item = getSelectedItem();

			if (item != null && !(item instanceof Model))
			{
				// Open the item using the primary association
				fireEvent("plugin.association.open", new BasicTransferable(item));
			}
		}
	}

	private void showPopup(Item item, Component comp, int x, int y)
	{
		if (displayContextMenuWaitCursor)
		{
			// Turn on the wait cursor
			ApplicationUtil.waitCursorOn();
		}

		InteractionEvent iae = null;
		try
		{
			// Broadcast an interaction event to collect the popup menu entries to display
			iae = new InteractionEvent(this, InteractionEvent.POPUP, new ItemTransferable(item));
			fireEvent(iae);
		}
		finally
		{
			if (displayContextMenuWaitCursor)
			{
				// Reset the wait cursor
				ApplicationUtil.waitCursorOff();

				// Display the wait cursor for the first time only
				displayContextMenuWaitCursor = false;
			}
		}

		iae.createPopupMenu().show(comp, x, y);
	}

	/**
	 * Creates an property browser event which will send to the property browser later.
	 *
	 * @param item An item or a model
	 */
	protected void firePropertyBrowserEvent(Item item)
	{
		String title = null;
		String desc = null;
		MultiIcon icon = null;
		boolean readOnly = false;
		Item originalItem = item;

		if (item != null)
		{
			// We fire an ask event to determine if this item is currently beeing edited
			// If so, we will display the edited instance (probably a copy of the original item)
			AskEvent ae = new AskEvent(this, "global.edit.geteditedinstance", item);
			fireEvent(ae);
			Item editedItem = (Item) ae.getAnswer();
			if (editedItem != null)
			{
				// We will use the edited item instead of the current one as node provider (may be newer!)
				item = editedItem;
			}

			if (item instanceof Model)
			{
				icon = ItemIconMgr.getMultiIcon(ItemIconMgr.getInstance().getIcon(ModelObjectSymbolNames.MODEL, FlexibleSize.SMALL));
			}
			else
			{
				icon = ItemIconMgr.getMultiIcon(ItemIconMgr.getInstance().getIcon(item, FlexibleSize.SMALL));
			}

			title = item.getDisplayText();
			String modelObjectTypeName = item.getModelObjectTypeName();
			if (modelObjectTypeName != null)
			{
				title = title + " (" + modelObjectTypeName + ")";
			}

			desc = item.getDescriptionText();
			readOnly |= !item.isModifiable();
		}
		else
		{
			readOnly = true;
		}

		PropertyBrowserSetEvent oee = new PropertyBrowserSetEvent(this, item, originalItem, false, desc, title, icon, readOnly, false);
		fireEvent(oee);

		// Show object description in info panel
		fireEvent(new JaspiraEvent(this, "plugin.infopanel.setinfotext", item));
	}

	//////////////////////////////////////////////////
	// @@ Keyboard handling
	//////////////////////////////////////////////////

	/**
	 * Sets up the key bindings of the component.
	 */
	private void setupKeyBindings()
	{
		// Create new input and action maps, using the original ones as parent
		InputMap focusInputMap = new InputMap();
		focusInputMap.setParent(SwingUtilities.getUIInputMap(itemTree, JComponent.WHEN_FOCUSED));

		InputMap focusAncestorInputMap = new InputMap();
		focusAncestorInputMap.setParent(SwingUtilities.getUIInputMap(itemTree, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));

		ActionMap actionMap = new ActionMap();
		actionMap.setParent(SwingUtilities.getUIActionMap(itemTree));

		// Register the action id for the provided key code
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
		focusInputMap.put(ks, "space");
		focusAncestorInputMap.put(ks, "space");

		// Determine the command
		actionMap.put("space", new AbstractAction("space")
		{
			public void actionPerformed(ActionEvent e)
			{
				// SPACE shows the popup menu for the current item
				Item item = getSelectedItem();

				// Show the popup directly below the current row
				TreePath [] paths = itemTree.getSelectionPaths();
				if (paths != null && paths.length > 0)
				{
					TreePath path = paths [0];
					Rectangle r = itemTree.getPathBounds(path);

					showPopup(item, itemTree, r.x, r.y + r.height);
				}
			}
		});

		// Install the new input and action maps
		SwingUtilities.replaceUIInputMap(itemTree, JComponent.WHEN_FOCUSED, focusInputMap);
		SwingUtilities.replaceUIInputMap(itemTree, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, focusAncestorInputMap);
		SwingUtilities.replaceUIActionMap(itemTree, actionMap);
	}

	//////////////////////////////////////////////////
	// @@ Observer implementation
	//////////////////////////////////////////////////

	public void observeEvent(ObserverEvent event)
	{
		// This causes UI updates, so wrap with invokeLater in order to perform them in the event dispatch thread
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				rebuildTree();
			}
		});
	}

	//////////////////////////////////////////////////
	// @@ New item action class
	//////////////////////////////////////////////////

	/**
	 * Adds the 'New' sub menu and its menu item to the given action group.
	 *
	 * @param group Action group to addd the actions to
	 */
	void addNewItemActions(JaspiraAction group)
	{
		String [] itemTypes = ModelConnector.getInstance().getItemTypes(ItemTypeRegistry.SKIP_INVISIBLE);

		for (int i = 0; i < itemTypes.length; ++i)
		{
			ItemTypeDescriptor itd = ModelConnector.getInstance().getItemTypeDescriptor(itemTypes [i]);

			NewAction action = new NewAction(itd);
			action.setPriority(i);

			group.addMenuChild(action);
		}
	}

	/**
	 * Adds the 'New' sub menu and its menu item to the given action group.
	 *
	 * @param iae Interaction event to add the actions to
	 */
	void addNewItemActions(InteractionEvent iae)
	{
		String [] itemTypes = ModelConnector.getInstance().getItemTypes(ItemTypeRegistry.SKIP_INVISIBLE);

		for (int i = 0; i < itemTypes.length; ++i)
		{
			ItemTypeDescriptor itd = ModelConnector.getInstance().getItemTypeDescriptor(itemTypes [i]);

			NewAction action = new NewAction(itd);
			action.setPriority(i);

			iae.add(action);
		}
	}

	/**
	 * Action listener that creates a new item.
	 */
	private class NewAction extends JaspiraAction
	{
		/** Item type descriptor */
		private ItemTypeDescriptor itd;

		/**
		 * Constructor.
		 *
		 * @param itd Item type descriptor
		 */
		public NewAction(ItemTypeDescriptor itd)
		{
			// Make the first character of the item type the mnemonic for the menu item
			super("new." + itd.getItemType(), "_" + itd.getItemType(), null, ItemIconMgr.getMultiIcon(ItemIconMgr.getInstance().getIcon(null, itd.getItemType(), FlexibleSize.SMALL)), null, JaspiraAction.DFLT_PRIORITY, JaspiraAction.TYPE_ACTION);

			this.itd = itd;
		}

		/**
		 * Executes the action.
		 *
		 * @param ae Event
		 */
		public void actionPerformed(ActionEvent ae)
		{
			// Gets the model
			Model model = getSelectedModel(ItemBrowserPlugin.GUESS_MODEL | ItemBrowserPlugin.USE_CURRENT_MODEL);

			Item item = ItemCreationUtil.createItem(model, null, null, itd, null);
			if (item != null)
			{
				final Item newItem = item;
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						setSelectedObject(newItem);
						focusPlugin();

						if (newItem instanceof ProcessItem)
						{
							fireEvent("plugin.association.open", new BasicTransferable(newItem));
						}
					}
				});
			}
		}
	}
}
