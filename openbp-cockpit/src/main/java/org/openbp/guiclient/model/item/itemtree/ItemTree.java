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
package org.openbp.guiclient.model.item.itemtree;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.openbp.common.CollectionUtil;
import org.openbp.common.generic.description.DisplayObject;
import org.openbp.common.generic.description.DisplayObjectImpl;
import org.openbp.common.icon.FlexibleSize;
import org.openbp.common.string.TextUtil;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypeRegistry;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.guiclient.model.ModelConnector;
import org.openbp.guiclient.model.item.ItemComparator;
import org.openbp.guiclient.model.item.ItemIconMgr;
import org.openbp.guiclient.model.item.itemfilter.ItemFilter;
import org.openbp.guiclient.model.item.itemfilter.ItemFilterListener;
import org.openbp.guiclient.model.item.itemfilter.ItemFilterMgr;
import org.openbp.guiclient.model.item.itemfilter.ItemTypeFilter;
import org.openbp.guiclient.util.DisplayObjectComparator;
import org.openbp.guiclient.util.DisplayObjectTreeNode;
import org.openbp.jaspira.decoration.DecorationMgr;
import org.openbp.jaspira.gui.StdIcons;
import org.openbp.swing.SwingUtil;
import org.openbp.swing.components.tree.TreeExpander;

/**
 * The item browser displays the models of the system and their items in a tree structure.
 * It supports dragging an item (e. g. onto the workspace).<br>
 * The items can be filtered by the installed item filters (currently item type and
 * item text filter). The filter components (used to adjust the filters) are shown
 * in a panel above the tree and can be turned on and off using the item filter buttons
 * in the plugin's toolbar.
 *
 * @author Heiko Erhardt
 */
public class ItemTree extends JTree
	implements ItemFilterListener
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** No selection */
	public static final int SELECTION_NONE = 0;

	/** Single selection */
	public static final int SELECTION_SINGLE = TreeSelectionModel.SINGLE_TREE_SELECTION;

	/** Multiple selection */
	public static final int SELECTION_MULTI = TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION;

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** The root object of the item tree or null for the root of the model tree */
	private ModelObject rootObject;

	/** Item filter manager */
	private ItemFilterMgr filterMgr;

	/** Item type filter */
	private ItemTypeFilter itemTypeFilter;

	/** Item types supported by this browser (null for the standard item types) */
	private String [] supportedItemTypes;

	/** Item types selectable by the user (null if no item types selectable) */
	private String [] selectableItemTypes;

	/** model object classes supported by this item browser (null if no model objects displayed) */
	private Class [] supportedObjectClasses;

	/** model object classes selectable by the user (null if no model objects selectable) */
	private Class [] selectableObjectClasses;

	/** Functional group display flag */
	private boolean showGroups = true;

	/** Selection mode */
	private int selectionMode;

	/** Auto-expander of the item tree */
	private TreeExpander treeExpander;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** List containing all item tree listeners. */
	protected EventListenerList listenerList;

	/** Comparator instance for the supported item types */
	private ItemComparator itemComparator;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ItemTree()
	{
		itemComparator = new ItemComparator();

		treeExpander = new TreeExpander(this);

		ToolTipManager.sharedInstance().registerComponent(this);

		setRootVisible(true);
		setToggleClickCount(2);

		// putClientProperty ("JTree.lineStyle", "Angled");
		setSelectionMode(SELECTION_NONE);

		addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_ENTER)
				{
					ItemTreeNode treeNode = (ItemTreeNode) getLastSelectedPathComponent();

					if (treeNode instanceof ModelNode || treeNode instanceof GroupNode)
					{
						TreeNode [] nodes = ((DefaultTreeModel) getModel()).getPathToRoot(treeNode);
						TreePath path = new TreePath(nodes);

						if (isExpanded(path))
							collapsePath(path);
						else
							expandPath(path);
						return;
					}

					ItemTreeEvent event = new ItemTreeEvent(ItemTree.this, ItemTreeEvent.OPEN);
					event.treeNode = (ItemTreeNode) getLastSelectedPathComponent();
					event.keyEvent = e;
					fireItemTreeEvent(event);
				}
			}
		});

		addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON3)
				{
					// Show popup on right mouse button
					TreePath path = getPathForLocation(e.getX(), e.getY());
					ItemTreeEvent event = new ItemTreeEvent(ItemTree.this, ItemTreeEvent.POPUP);

					if (path != null)
					{
						// Make sure the selection contains the node under the cursor
						addSelectionPath(path);

						event.treeNode = (ItemTreeNode) path.getLastPathComponent();
					}
					else
					{
						clearSelection();
					}

					event.mouseEvent = e;
					fireItemTreeEvent(event);
				}

				else if (e.getClickCount() >= 2)
				{
					// Open item on double click
					TreePath path = getSelectionPath();

					if (path != null)
					{
						ItemTreeEvent event = new ItemTreeEvent(ItemTree.this, ItemTreeEvent.OPEN);
						event.treeNode = (ItemTreeNode) path.getLastPathComponent();
						event.mouseEvent = e;
						fireItemTreeEvent(event);
					}
				}
			}
		});

		addTreeSelectionListener(new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent e)
			{
				ItemTreeEvent event = new ItemTreeEvent(ItemTree.this, ItemTreeEvent.SELECTION_CHANGED);
				event.treeNode = (ItemTreeNode) getLastSelectedPathComponent();
				fireItemTreeEvent(event);
			}
		});

		setCellRenderer(new ItemTreeCellRenderer());
	}

	/**
	 * Rebuilds the model/item tree.
	 */
	public void rebuildTree()
	{
		ItemTreeState state = saveState();

		ItemTreeNode rootNode = null;
		if (rootObject != null)
		{
			if (rootObject instanceof Model)
			{
				rootNode = new ModelNode((Model) rootObject, null);
			}
			else if (rootObject instanceof Item)
			{
				rootNode = new ItemNode((Item) rootObject, null);
			}
			else
			{
				rootNode = new ModelObjectNode(rootObject, null);
			}
		}
		else
		{
			// Map the entire model tree
			rootNode = new ModelNode();
		}
		rootNode.addChildObjects();

		rootNode.removeEmptyChildren();
		((DefaultTreeModel) getModel()).setRoot(rootNode);

		restoreState(state);
	}

	//////////////////////////////////////////////////
	// @@ Tree state
	//////////////////////////////////////////////////

	/**
	 * Saves the state of the item tree.
	 *
	 * @return The saved state
	 */
	public ItemTreeState saveState()
	{
		ItemTreeState state = new ItemTreeState();

		List expandedQualifiers = null;
		int nRows = getRowCount();
		for (int iRow = 0; iRow < nRows; iRow++)
		{
			TreePath path = getPathForRow(iRow);
			if (isExpanded(path))
			{
				Object o = path.getLastPathComponent();
				if (!(o instanceof ItemTreeNode))
				{
					// Necessary to prevent a CCE from the default contents of the DefaultMutableTreeModel
					continue;
				}
				ItemTreeNode node = (ItemTreeNode) o;

				if (expandedQualifiers == null)
					expandedQualifiers = new ArrayList();
				expandedQualifiers.add(node.getQualifier());
			}
		}
		state.setExpandedQualifiers(expandedQualifiers);

		TreePath [] paths = getSelectionPaths();
		if (paths != null)
		{
			List selectedQualifiers = new ArrayList();

			for (int i = 0; i < paths.length; ++i)
			{
				Object o = paths [i].getLastPathComponent();
				if (!(o instanceof ItemTreeNode))
				{
					// Necessary to prevent a CCE from the default contents of the DefaultMutableTreeModel
					continue;
				}
				ItemTreeNode node = (ItemTreeNode) o;

				selectedQualifiers.add(node.getQualifier());
			}

			state.setSelectedQualifiers(selectedQualifiers);
		}

		return state;
	}

	/**
	 * Restores the state of the item tree.
	 *
	 * @param state State to restore
	 */
	public void restoreState(ItemTreeState state)
	{
		List expandedQualifiers = state.getExpandedQualifiers();
		if (expandedQualifiers != null)
		{
			int n = expandedQualifiers.size();
			for (int i = 0; i < n; ++i)
			{
				ModelQualifier qualifier = (ModelQualifier) expandedQualifiers.get(i);

				ItemTreeNode node = findNodeByQualifier(qualifier);
				if (node != null)
				{
					TreeNode [] nodes = ((DefaultTreeModel) getModel()).getPathToRoot(node);
					TreePath path = new TreePath(nodes);
					expandPath(path);
				}
			}
		}

		clearSelection();

		List selectedQualifiers = state.getSelectedQualifiers();
		if (selectedQualifiers != null)
		{
			int n = selectedQualifiers.size();
			for (int i = 0; i < n; ++i)
			{
				ModelQualifier qualifier = (ModelQualifier) selectedQualifiers.get(i);

				ItemTreeNode node = findNodeByQualifier(qualifier);
				if (node != null)
				{
					TreeNode [] nodes = ((DefaultTreeModel) getModel()).getPathToRoot(node);
					TreePath path = new TreePath(nodes);

					TreePath parentPath = path.getParentPath();
					if (parentPath != null)
					{
						expandPath(parentPath);
					}

					addSelectionPath(path);
					if (i == 0)
					{
						// Make the first selected node visible
						scrollPathToVisible(path);
					}
				}
			}
		}
	}

	/**
	 * Expands the tree up to the desired level.
	 *
	 * @param level Maximum level to display
	 */
	public void expand(int level)
	{
		treeExpander.simpleExpand(level);
	}

	/**
	 * Gets the auto-expander of the item tree.
	 * @nowarn
	 */
	public TreeExpander getTreeExpander()
	{
		return treeExpander;
	}

	//////////////////////////////////////////////////
	// @@ Selection
	//////////////////////////////////////////////////

	/**
	 * Gets the model that owns the currently selected object.
	 *
	 * @return The model or null
	 */
	public Model determineSelectedModel()
	{
		TreePath [] paths = getSelectionPaths();
		if (paths == null)
			return null;

		for (int i = 0; i < paths.length; ++i)
		{
			ItemTreeNode node = (ItemTreeNode) paths [i].getLastPathComponent();
			if (node instanceof ModelObjectNode)
			{
				ModelObject object = ((ModelObjectNode) node).getModelObject();
				return object.getOwningModel();
			}
			else if (node instanceof GroupNode)
			{
				return ((ModelNode) node.getParent()).getModel();
			}
		}

		return null;
	}

	/**
	 * Gets the currently selected objects.
	 *
	 * @return A list of {@link ModelObject} s.<br>
	 * The objects in the list are either items of one of the types specified in
	 * the list set by the {@link #setSelectableItemTypes} method or objects of
	 * the model object classes set by the {@link #setSelectableObjectClasses} method.<br>
	 * If no object matching this criteria is selected, null will be returned.
	 */
	public List getSelectedObjects()
	{
		TreePath [] paths = getSelectionPaths();
		if (paths == null)
			return null;

		List list = null;

		for (int i = 0; i < paths.length; ++i)
		{
			ItemTreeNode node = (ItemTreeNode) paths [i].getLastPathComponent();
			if (node instanceof ModelObjectNode)
			{
				ModelObject object = ((ModelObjectNode) node).getModelObject();
				if (isSelectable(object))
				{
					if (list == null)
						list = new ArrayList();
					list.add(object);
				}
			}
		}

		return list;
	}

	/**
	 * Selects the given objects.
	 * The current selection will be cleared.
	 * Call this method after the tree has been initialized using the {@link #rebuildTree} method.
	 *
	 * @param objects A list of {@link ModelObject} s or null
	 */
	public void setSelectedObjects(List objects)
	{
		clearSelection();

		if (objects != null)
		{
			int n = objects.size();
			for (int i = 0; i < n; ++i)
			{
				Object o = objects.get(i);
				ItemTreeNode node = findNodeByObject(o);
				if (node != null)
				{
					TreeNode [] nodes = ((DefaultTreeModel) getModel()).getPathToRoot(node);
					TreePath path = new TreePath(nodes);
					addSelectionPath(path);
				}
			}
		}
	}

	/**
	 * Selects the element that should be selected if the currently selected element is deleted.
	 * Searches downwards in the current level first, then upwards and then tries the parent level
	 */
	public void performAlternativeSelection()
	{
		TreePath newPath = determineAlternativeSelection();
		if (newPath != null)
		{
			clearSelection();
			addSelectionPath(newPath);
		}
	}

	/**
	 * Returns the tree path of the element that should be selected if the currently selected element is deleted.
	 * Searches downwards in the current level first, then upwards and then tries the parent level
	 *
	 * @return The tree path or null if the tree would be entirely empty or if no element is selected currently
	 */
	public TreePath determineAlternativeSelection()
	{
		TreePath [] paths = getSelectionPaths();
		if (paths == null || paths.length == 0)
			return null;

		TreeNode node = (TreeNode) paths [0].getLastPathComponent();
		TreeNode parentNode = node.getParent();
		if (parentNode == null)
		{
			// No parent for top level element
			return null;
		}

		// The parent node is the fallback
		TreeNode ret = parentNode;

		// Search the children
		int nChilds = parentNode.getChildCount();
		for (int i = 0; i < nChilds; i++)
		{
			TreeNode childNode = parentNode.getChildAt(i);

			if (childNode.equals(node))
			{
				// Found current node
				if (i + 1 < nChilds)
					ret = parentNode.getChildAt(i + 1);
				else if (i != 0)
					ret = parentNode.getChildAt(i - 1);
				break;
			}
		}

		return new TreePath(((DefaultTreeModel) getModel()).getPathToRoot(ret));
	}

	//////////////////////////////////////////////////
	// @@ Finder methods
	//////////////////////////////////////////////////

	/**
	 * Finds a tree node by the object it refers to.
	 *
	 * @param object Object to search (usually a {@link ModelObject})
	 * @return The tree node or null if no such object exists in the tree
	 */
	public ItemTreeNode findNodeByObject(Object object)
	{
		return findNodeByObject(object, (ItemTreeNode) ((DefaultTreeModel) getModel()).getRoot());
	}

	/**
	 * Finds a tree node by the object it refers to, starting
	 * at the given tree node.
	 *
	 * @param object Object to search (usually a {@link ModelObject})
	 * @param node Node to start the search from
	 * @return The tree node or null if no such object exists in the tree
	 */
	public ItemTreeNode findNodeByObject(Object object, ItemTreeNode node)
	{
		if (node instanceof ModelObjectNode)
		{
			ModelObject o = ((ModelObjectNode) node).getModelObject();
			if (o == object)
				return node;
		}

		int n = node.getChildCount();
		for (int i = 0; i < n; ++i)
		{
			ItemTreeNode child = (ItemTreeNode) node.getChildAt(i);
			ItemTreeNode ret = findNodeByObject(object, child);
			if (ret != null)
				return ret;
		}

		return null;
	}

	/**
	 * Finds a tree node by the model qualifier of the object it refers to.
	 *
	 * @param qualifier Themodel qualifier 
	 * @return The tree node or null if no such object exists in the tree
	 */
	public ItemTreeNode findNodeByQualifier(ModelQualifier qualifier)
	{
		return findNodeByQualifier(qualifier, (ItemTreeNode) ((DefaultTreeModel) getModel()).getRoot());
	}

	/**
	 * Finds a tree node by the model qualifier of the object it refers to, starting
	 * at the given tree node.
	 *
	 * @param qualifier Themodel qualifier 
	 * @param node Node to start the search from
	 * @return The tree node or null if no such object exists in the tree
	 */
	public ItemTreeNode findNodeByQualifier(ModelQualifier qualifier, ItemTreeNode node)
	{
		ModelQualifier j = node.getQualifier();
		if (j.equals(qualifier))
			return node;

		int n = node.getChildCount();
		for (int i = 0; i < n; ++i)
		{
			ItemTreeNode child = (ItemTreeNode) node.getChildAt(i);
			ItemTreeNode ret = findNodeByQualifier(qualifier, child);
			if (ret != null)
				return ret;
		}

		return null;
	}

	/**
	 * Gets the path to the node representing the given object.
	 *
	 * @param o Object to search (usually a {@link ModelObject})
	 * @return The tree path or null if no such node exists
	 */
	public TreePath findPathByObject(Object o)
	{
		ItemTreeNode node = findNodeByObject(o);
		if (node != null)
		{
			TreeNode [] nodes = ((DefaultTreeModel) getModel()).getPathToRoot(node);
			TreePath path = new TreePath(nodes);
			return path;
		}
		return null;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the the root object of the item tree or null for the root of the model tree.
	 * @nowarn
	 */
	public ModelObject getRootObject()
	{
		return rootObject;
	}

	/**
	 * Sets the the root object of the item tree or null for the root of the model tree.
	 * @nowarn
	 */
	public void setRootObject(ModelObject rootObject)
	{
		this.rootObject = rootObject;
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
	 * Sets the item types supported by this browser (null for the standard tem types).
	 * Call this method before the plugin is installed (best use the constructor of derived classes).
	 * @nowarn
	 */
	public void setSupportedItemTypes(String [] supportedItemTypes)
	{
		this.supportedItemTypes = supportedItemTypes;
	}

	/**
	 * Gets the item types selectable by the user (null if no item types selectable).
	 * @nowarn
	 */
	public String [] getSelectableItemTypes()
	{
		return selectableItemTypes;
	}

	/**
	 * Sets the item types selectable by the user (null if no item types selectable).
	 * @nowarn
	 */
	public void setSelectableItemTypes(String [] selectableItemTypes)
	{
		this.selectableItemTypes = selectableItemTypes;
	}

	/**
	 * Gets the model object classes supported by this item browser (null if no model objects displayed).
	 * @nowarn
	 */
	public Class [] getSupportedObjectClasses()
	{
		return supportedObjectClasses;
	}

	/**
	 * Sets the model object classes supported by this item browser (null if no model objects displayed).
	 * @nowarn
	 */
	public void setSupportedObjectClasses(Class [] supportedObjectClasses)
	{
		this.supportedObjectClasses = supportedObjectClasses;
	}

	/**
	 * Gets the model object classes selectable by the user (null if no model objects selectable).
	 * @nowarn
	 */
	public Class [] getSelectableObjectClasses()
	{
		return selectableObjectClasses;
	}

	/**
	 * Sets the model object classes selectable by the user (null if no model objects selectable).
	 * @nowarn
	 */
	public void setSelectableObjectClasses(Class [] selectableObjectClasses)
	{
		this.selectableObjectClasses = selectableObjectClasses;
	}

	/**
	 * Gets the item filter manager.
	 * @return The filter manager or null if not item filters are used
	 */
	public ItemFilterMgr getFilterMgr()
	{
		return filterMgr;
	}

	/**
	 * Sets the item filter manager.
	 * @param filterMgr The filter manager or null if not item filters should be used
	 */
	public void setFilterMgr(ItemFilterMgr filterMgr)
	{
		this.filterMgr = filterMgr;
	}

	/**
	 * Gets the item type filter.
	 * @return The filter manager or null if not item type filter is used
	 */
	public ItemTypeFilter getItemTypeFilter()
	{
		return itemTypeFilter;
	}

	/**
	 * Sets the item type filter.
	 * @param itemTypeFilter The filter or null if no item type filter should be used.<br>
	 * Note that the item type filter must be added to the item filter manager, however.
	 * The item tree will use the filter only to determine the types to be retrieved
	 * in order to improve the performance.
	 */
	public void setItemTypeFilter(ItemTypeFilter itemTypeFilter)
	{
		this.itemTypeFilter = itemTypeFilter;
	}

	/**
	 * Gets the group display flag.
	 * @nowarn
	 */
	public boolean isShowGroups()
	{
		return showGroups;
	}

	/**
	 * Sets the group display flag.
	 * @nowarn
	 */
	public void setShowGroups(boolean showGroups)
	{
		this.showGroups = showGroups;
	}

	/**
	 * Gets the selection mode.
	 * @return {@link #SELECTION_NONE}/{@link #SELECTION_SINGLE}/{@link #SELECTION_MULTI}
	 */
	public int getSelectionMode()
	{
		return selectionMode;
	}

	/**
	 * Sets the selection mode.
	 * @param selectionMode {@link #SELECTION_NONE}/{@link #SELECTION_SINGLE}/{@link #SELECTION_MULTI}
	 */
	public void setSelectionMode(int selectionMode)
	{
		this.selectionMode = selectionMode;

		int x = selectionMode;
		if (x == SELECTION_NONE)
		{
			// We won't select an object, but the tree needs at least single selection mode
			x = TreeSelectionModel.SINGLE_TREE_SELECTION;
		}
		getSelectionModel().setSelectionMode(x);
	}

	//////////////////////////////////////////////////
	// @@ ItemTreeListener support
	//////////////////////////////////////////////////

	// TODO Optimize 6: Use ListenerSupport here

	/**
	 * Adds an item tree listener.
	 *
	 * @param listener Listener to add
	 */
	public void addItemTreeListener(ItemTreeListener listener)
	{
		if (listenerList == null)
		{
			listenerList = new EventListenerList();
		}
		else
		{
			if (SwingUtil.containsListener(listenerList, ItemTreeListener.class, listener))
				return;
		}
		listenerList.add(ItemTreeListener.class, listener);
	}

	/**
	 * Removes an item tree listener.
	 *
	 * @param listener Listener to remove
	 */
	public void removeItemTreeListener(ItemTreeListener listener)
	{
		if (listenerList != null)
		{
			listenerList.remove(ItemTreeListener.class, listener);
		}
	}

	/**
	 * Notifies all registered item tree listeners of the given item tree event.
	 *
	 * @param e Event
	 */
	public void fireItemTreeEvent(ItemTreeEvent e)
	{
		if (listenerList != null)
		{
			// Guaranteed to return a non-null array
			Object [] listeners = listenerList.getListenerList();

			// Process the listeners last to first, notifying
			// those that are interested in this event
			for (int i = listeners.length - 2; i >= 0; i -= 2)
			{
				if (listeners [i] == ItemTreeListener.class)
				{
					((ItemTreeListener) listeners [i + 1]).handleItemTreeEvent(e);
					if (e.cancel)
					{
						// Event was cancelled, don't submit to further listeners
						break;
					}
				}
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Determines if the given object is supported (i\.e\. should be displayed) by this item tree.
	 *
	 * @param o Object to check
	 * @return
	 * true: The object is either an item of one of the types specified in the list set by the {@link #setSupportedItemTypes} method or one of the model object classes set by the {@link #setSupportedObjectClasses} method.<br>
	 * false: The object should not be displayed.
	 */
	protected boolean isSupported(ModelObject o)
	{
		return checkObject(o, supportedItemTypes, supportedObjectClasses, ItemTreeEvent.IS_SUPPORTED);
	}

	/**
	 * Determines if the given object is selectable by the user.
	 *
	 * @param o Object to check
	 * @return
	 * true: The object is either an item of one of the types specified in the list set by the {@link #setSelectableItemTypes} method or one of the model object classes set by the {@link #setSelectableObjectClasses} method.<br>
	 * false: The object may not be selected.
	 */
	protected boolean isSelectable(ModelObject o)
	{
		return checkObject(o, selectableItemTypes, selectableObjectClasses, ItemTreeEvent.IS_SELECTABLE);
	}

	/**
	 * Determines if the given object is either an item of one of the types specified in
	 * the given list or one of the model object classes contained in the given class list.
	 *
	 * @param o Object to check
	 * @return
	 *		false	The object may not be selected.
	 */
	private boolean checkObject(ModelObject o, String [] itemList, Class [] classList, int eventType)
	{
		boolean ret = false;

		if (o instanceof Item)
		{
			// Check item type
			if (itemList != null)
			{
				String itemType = ((Item) o).getItemType();
				for (int i = 0; i < itemList.length; ++i)
				{
					if (itemType.equals(itemList [i]))
					{
						ret = true;
						break;
					}
				}
			}
		}
		else
		{
			// Check object class
			if (classList != null)
			{
				Class cls = o.getClass();
				for (int i = 0; i < classList.length; ++i)
				{
					if (classList [i].isAssignableFrom(cls))
					{
						ret = true;
						break;
					}
				}
			}
		}

		if (ret)
		{
			// Passed the basic check, now let the event handler decide
			ItemTreeEvent event = new ItemTreeEvent(ItemTree.this, eventType);
			event.object = o;
			fireItemTreeEvent(event);
			if (event.cancel)
			{
				// Handler vetoes
				ret = false;
			}
		}

		return ret;
	}

	//////////////////////////////////////////////////
	// @@ ItemFilterListener implementation
	//////////////////////////////////////////////////

	/**
	 * Called if the current item filter settings have changed, i\.e\. the item filters
	 * should be (re-)applied.
	 *
	 * @param mgr Item filter manager that issues the event
	 * @param filter Item filter that has changed or null
	 */
	public void applyFilter(ItemFilterMgr mgr, ItemFilter filter)
	{
		rebuildTree();
	}

	//////////////////////////////////////////////////
	// @@ Tree cell renderer
	//////////////////////////////////////////////////

	/**
	 * Default tree cell renderer Override.
	 */
	private class ItemTreeCellRenderer extends DefaultTreeCellRenderer
	{
		/**
		 * Constructor.
		 */
		private ItemTreeCellRenderer()
		{
			super();
		}

		/**
		 * Overridden in order to set the node icon.
		 * @nowarn
		 */
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			if (c instanceof JLabel)
			{
				if (value instanceof ItemTreeNode)
				{
					ItemTreeNode node = (ItemTreeNode) value;

					Icon icon = node.getIcon(expanded);
					icon = (Icon) DecorationMgr.decorate(node.getObject(), "icon", icon);
					String [] infoText = node.getInfoText();

					JLabel label = (JLabel) c;
					label.setIcon(icon);

					if (infoText != null)
					{
						label.setToolTipText(TextUtil.convertToHTML(infoText, true, 1, 50));
					}
				}
			}
			return c;
		}
	}

	//////////////////////////////////////////////////
	// @@ Item browser nodes
	//////////////////////////////////////////////////

	/**
	 * Tree node representing a model.
	 */
	public abstract class ItemTreeNode extends DisplayObjectTreeNode
	{
		/**
		 * Default constructor.
		 */
		public ItemTreeNode()
		{
		}

		/**
		 * Default constructor.
		 *
		 * @param obj Object represented by this node
		 * @param parent Parent tree node
		 */
		public ItemTreeNode(DisplayObject obj, ItemTreeNode parent)
		{
			super(obj, parent);
		}

		/**
		 * Gets the icon for this node.
		 * @param isExpanded
		 * true: The tree path is expanded.<br>
		 * false: The tree path is collapsed.
		 * @return The icon or null
		 */
		public abstract Icon getIcon(boolean isExpanded);

		/**
		 * Gets the info text of this node (used as tool tip).
		 *
		 * @copy ModelObject.getInfoText
		 * @return Always returns null
		 */
		public String [] getInfoText()
		{
			return null;
		}

		/**
		 * Creates the model qualifier of this node.
		 * @nowarn
		 */
		public abstract ModelQualifier getQualifier();

		/**
		 * Checks if the model qualifier of this node matches the given model qualifier.
		 * @nowarn
		 */
		public boolean matchesQualifier(ModelQualifier other)
		{
			if (getQualifier().matches(other))
				return true;
			return false;
		}

		/**
		 * Adds the child objects of this object.
		 */
		protected void addChildObjects()
		{
		}

		/**
		 * Removes empty child nodes.
		 */
		protected void removeEmptyChildren()
		{
			List childs = getChildList();
			if (childs != null)
			{
				for (int i = 0; i < childs.size(); ++i)
				{
					ItemTreeNode child = (ItemTreeNode) childs.get(i);

					if (child instanceof ModelObjectNode)
					{
						// Recurse down
						((ModelObjectNode) child).removeEmptyChildren();

						if (!(child instanceof ModelNode) && child.getChildCount() == 0)
						{
							if (!child.isLeaf())
							{
								removeChild(child);
								--i;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Tree node representing a model object (model, item, node etc\.).
	 */
	public class ModelObjectNode extends ItemTreeNode
	{
		/**
		 * Default constructor.
		 */
		public ModelObjectNode()
		{
		}

		/**
		 * Default constructor.
		 *
		 * @param object Model object represented by this node
		 * @param parent Parent tree node
		 */
		public ModelObjectNode(ModelObject object, ItemTreeNode parent)
		{
			super(object, parent);
		}

		/**
		 * Gets the model represented by this node.
		 * @nowarn
		 */
		public ModelObject getModelObject()
		{
			return (ModelObject) object;
		}

		/**
		 * Gets the icon for this node.
		 * @param isExpanded
		 * true: The tree path is expanded.<br>
		 * false: The tree path is collapsed.
		 * @return The icon or null
		 */
		public Icon getIcon(boolean isExpanded)
		{
			if (getModelObject() != null)
			{
				String iconName = getModelObject().getModelObjectSymbolName();
				if (iconName != null)
				{
					return ItemIconMgr.getInstance().getIcon(iconName, FlexibleSize.SMALL);
				}
			}
			return null;
		}

		/**
		 * Gets the info text of this node (used as tool tip).
		 *
		 * @copy ModelObject.getInfoText
		 */
		public String [] getInfoText()
		{
			if (getModelObject() != null)
			{
				return getModelObject().getInfoText();
			}
			return null;
		}

		/**
		 * Creates the model qualifier of this node.
		 * @nowarn
		 */
		public ModelQualifier getQualifier()
		{
			if (getModelObject() != null)
			{
				return getModelObject().getQualifier();
			}

			// Root node
			return new ModelQualifier();
		}

		/**
		 * Adds the child objects of this model object.
		 */
		protected void addChildObjects()
		{
			if (supportedObjectClasses == null)
			{
				// No child objects desired
				return;
			}

			if (getModelObject() != null)
			{
				List children = getModelObject().getChildren();
				if (children != null)
				{
					// Sort
					Collections.sort(children, DisplayObjectComparator.getInstance());

					// Recurse down
					int n = children.size();
					for (int i = 0; i < n; ++i)
					{
						ModelObject child = (ModelObject) children.get(i);

						if (isSupported(child))
						{
							ModelObjectNode objectNode = new ModelObjectNode(child, this);
							addChild(objectNode);

							objectNode.addChildObjects();
						}
					}
				}
			}
		}
	}

	/**
	 * Tree node representing a model.
	 */
	public class ModelNode extends ModelObjectNode
	{
		/**
		 * Default constructor.
		 */
		public ModelNode()
		{
		}

		/**
		 * Default constructor.
		 *
		 * @param model Model represented by this node
		 * @param parent Parent tree node
		 */
		public ModelNode(Model model, ItemTreeNode parent)
		{
			super(model, parent);
		}

		/**
		 * Adds the child objects of this model object.
		 */
		protected void addChildObjects()
		{
			Model model = getModel();
			if (model == null)
			{
				addModels();
			}
			else
			{
				addItems();
			}
		}

		/**
		 * Gets the model represented by this node.
		 * @nowarn
		 */
		public Model getModel()
		{
			return (Model) object;
		}

		/**
		 * Determine if this node is a leaf node.
		 * @nowarn
		 */
		public boolean isLeaf()
		{
			if (supportedObjectClasses == null && supportedItemTypes != null && supportedItemTypes.length == 1 && supportedItemTypes [0].equals(ItemTypes.MODEL))
			{
				// If only models are displayed, we consider this model a leaf item
				// if it has not children
				return super.isLeaf();
			}

			// Otherwise, models are never leaf items
			return false;
		}

		/**
		 * Adds the sub models of this model.
		 */
		private void addModels()
		{
			// Add the sub models
			List models = ModelConnector.getInstance().getModels();
			if (models != null)
			{
				// Sort
				Collections.sort(models, DisplayObjectComparator.getInstance());

				// Recurse down
				int n = models.size();
				for (int i = 0; i < n; ++i)
				{
					Model subModel = (Model) models.get(i);

					ModelNode modelNode = new ModelNode(subModel, this);
					addChild(modelNode);

					modelNode.addChildObjects();
				}
			}
		}

		/**
		 * Adds the items of this model.
		 */
		private void addItems()
		{
			// Items that do not have a groups
			List ungroupedItems = null;

			// Table mapping groups to a list of items of this group
			HashMap groupTable = null;

			String [] types = getSupportedItemTypes();
			if (types == null)
			{
				// Supported item types not explicitely given, get the standard from the model connector
				types = ModelConnector.getInstance().getItemTypes(ItemTypeRegistry.SKIP_MODEL | ItemTypeRegistry.SKIP_INVISIBLE);
				setSupportedItemTypes(types);
			}
			itemComparator.setItemTypes(types);

			// Add the items of this model
			for (int iTypes = 0; iTypes < types.length; ++iTypes)
			{
				String itemType = types [iTypes];
				if (itemTypeFilter != null && itemTypeFilter.isActive())
				{
					if (!itemTypeFilter.containsItemType(itemType))
					{
						// Ignore this item type
						continue;
					}
				}

				for (Iterator itItems = getModel().getItems(itemType); itItems.hasNext();)
				{
					Item item = (Item) itItems.next();

					if (filterMgr == null || filterMgr.acceptsItem(item))
					{
						if (ungroupedItems == null)
							ungroupedItems = new ArrayList();
						ungroupedItems.add(item);
					}
				}

				if (ungroupedItems != null)
				{
					if (showGroups)
					{
						// Iterate the items and store the grouped one in the group table
						for (Iterator it = ungroupedItems.iterator(); it.hasNext();)
						{
							Item item = (Item) it.next();

							String group = item.getFunctionalGroup();
							if (group != null)
							{
								// Remove the item from the lis of ungrouped items
								it.remove();

								if (groupTable == null)
								{
									groupTable = new HashMap();
								}

								List items = (List) groupTable.get(group);
								if (items == null)
								{
									items = new ArrayList();
									groupTable.put(group, items);
								}
								items.add(item);
							}
						}
					}
				}
			}

			if (ungroupedItems != null && ungroupedItems.size() > 0)
			{
				// Add the ungrouped items to the model node first

				// Sort
				Collections.sort(ungroupedItems, itemComparator);

				// Recurse down
				int nItems = ungroupedItems.size();
				for (int iItems = 0; iItems < nItems; ++iItems)
				{
					Item item = (Item) ungroupedItems.get(iItems);

					ItemNode itemNode = new ItemNode(item, this);
					addChild(itemNode);

					itemNode.addChildObjects();
				}
			}

			if (groupTable != null)
			{
				// Add the item groups

				// Sort the groups
				List groups = CollectionUtil.iteratorToArrayList(groupTable.keySet().iterator());
				Collections.sort(groups);

				// Add the groups to the model node in sorted order
				int nGroups = groups.size();
				for (int iGroups = 0; iGroups < nGroups; ++iGroups)
				{
					String group = (String) groups.get(iGroups);

					List items = (List) groupTable.get(group);

					GroupNode groupNode = new GroupNode(group, this);
					addChild(groupNode);

					Collections.sort(items, itemComparator);

					int nItems = items.size();
					for (int iItems = 0; iItems < nItems; ++iItems)
					{
						Item item = (Item) items.get(iItems);

						ItemNode itemNode = new ItemNode(item, groupNode);
						groupNode.addChild(itemNode);

						itemNode.addChildObjects();
					}
				}
			}
		}
	}

	/**
	 * Tree node representing an item.
	 */
	public class ItemNode extends ModelObjectNode
	{
		/**
		 * Default constructor.
		 *
		 * @param item Item represented by this node
		 * @param parent Parent tree node
		 */
		public ItemNode(Item item, ItemTreeNode parent)
		{
			super(item, parent);
		}

		/**
		 * Gets the item represented by this node.
		 * @nowarn
		 */
		public Item getItem()
		{
			return (Item) object;
		}

		/**
		 * Determine if this node is a leaf node.
		 * @nowarn
		 */
		public boolean isLeaf()
		{
			if (supportedObjectClasses != null)
			{
				// If we are showing something below items, items are not leafs any more
				return false;
			}
			return true;
		}
	}

	/**
	 * Tree node representing an item group.
	 */
	public class GroupNode extends ItemTreeNode
	{
		/**
		 * Default constructor.
		 *
		 * @param group Item group represented by this node
		 * @param parent Parent tree node
		 */
		public GroupNode(String group, ItemTreeNode parent)
		{
			super(new DisplayObjectImpl(group), parent);
		}

		/**
		 * Determine if this node is a leaf node.
		 * @nowarn
		 */
		public boolean isLeaf()
		{
			return false;
		}

		/**
		 * Gets the icon for this node.
		 * @param isExpanded
		 * true: The tree path is expanded.<br>
		 * false: The tree path is collapsed.
		 * @return The icon or null
		 */
		public Icon getIcon(boolean isExpanded)
		{
			return isExpanded ? StdIcons.openFolderIcon : StdIcons.closedFolderIcon;
		}

		/**
		 * Creates the model qualifier of this node.
		 * @nowarn
		 */
		public ModelQualifier getQualifier()
		{
			ModelQualifier j = ((ModelNode) getParent()).getQualifier();
			j.setObjectPath(object.getName());
			return j;
		}
	}
}
