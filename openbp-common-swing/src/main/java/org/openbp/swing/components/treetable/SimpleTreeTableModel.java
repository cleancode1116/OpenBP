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
package org.openbp.swing.components.treetable;

import java.io.Serializable;
import java.util.EventListener;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * The {@link SimpleTreeTableModel} can be thought of as an abstract model, although all required
 * methods have been implemented. When wanting to write an own TreeTableModel, it is suggested
 * to extend this model and override only those methods as required.
 *
 * @author Erich Lauterbach
 */
public class SimpleTreeTableModel extends AbstractTableModel
	implements TreeTableModel, Serializable
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** The root element used by the tree. */
	protected TreeTableNode root;

	/** The JTreeTable using this model. */
	protected JTreeTable treeTable;

	/** The header information for the columns */
	protected String [] columnHeader;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	// TODO Optimize: This is a pretty chaotic listener and event model structure...

	/**
	 * Default Constructor
	 * Note: When using this constructor the tree table reference to this
	 * model will have to be explicity set. Rather use the other constructor.
	 *
	 * @param rootNode The root node for this model
	 */
	public SimpleTreeTableModel(TreeTableNode rootNode)
	{
		this(rootNode, null);
	}

	/**
	 * Default preferred constructor.
	 *
	 * @param rootNode The root element of the tree
	 * @param treeTable The treeTable using this model
	 */
	public SimpleTreeTableModel(TreeTableNode rootNode, JTreeTable treeTable)
	{
		root = rootNode;
		addTreeModelListener(new SimpleTreeTableModelListener());
		if (treeTable != null)
		{
			setTreeTable(treeTable);
		}
	}

	/**
	 * If the constructor was used in which the {@link JTreeTable} was not set, this method
	 * should be called directly after instantiating the model to set the JTreeTable.
	 *
	 * @param treeTable The {@link JTreeTable} using this model
	 */
	public void setTreeTable(JTreeTable treeTable)
	{
		this.treeTable = treeTable;
		treeTable.getTree().addTreeExpansionListener(new TreeTableExpansionListener());
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Set the column headers.
	 *
	 * @param columnHeader A string array
	 */
	public void setColumnHeader(String [] columnHeader)
	{
		this.columnHeader = columnHeader;
	}

	//////////////////////////////////////////////////
	// @@ AbstractTableModel overidden methods.
	//////////////////////////////////////////////////

	/**
	 * Returns the number of rows contained in the table.
	 *
	 * @return The number of rows in the table
	 */
	public int getRowCount()
	{
		if (treeTable == null || treeTable.getTree() == null)
		{
			return 0;
		}

		return treeTable.getTree().getRowCount();
	}

	/**
	 * Returns the class type for column number column.
	 * By default for column 0 a {@link TreeTableModel} class is returned, and the rest
	 * are Object classes.
	 *
	 * @param column The column of interest
	 * @return The class for a specified column
	 */
	public Class getColumnClass(int column)
	{
		switch (column)
		{
		case 0:
			return TreeTableModel.class;

		default:
			return Object.class;
		}
	}

	/**
	 * Returns if a cell at a specified row and column is editable. By default true is returned
	 * for all cells.
	 *
	 * @param row The cell's row
	 * @param column The cell's column
	 * @return True if the specified cell is editable<br>
	 *             False if the cell is NOT editable.
	 */
	public boolean isCellEditable(int row, int column)
	{
		if (TreeTableModel.class.isAssignableFrom(getColumnClass(column)))
			return true;

		return false;
	}

	/**
	 * Sets the value for node node,
	 * at column number column.
	 * This method is not implemented an will throw an IllegalStateException, stating that
	 * the method needs to overridden. Reasons for this method not to be absract is that
	 * in some special cases this method might not be required.
	 *
	 * @param aValue The object to be set
	 * @param node The node to which the object is to be set
	 * @param column The column index where the object is to set
	 */
	public void setValueAt(Object aValue, Object node, int column)
	{
		throw new IllegalStateException("This method is not implemented. Please overwrite this one.");
	}

	/**
	 * Gets the object value for a specified cell for the receiver node.
	 *
	 * @param rowIndex The row index of the specified cell
	 * @param columnIndex The column index for the specified cell
	 * @return Object The object for the cell
	 */
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		if (treeTable == null)
		{
			return null;
		}

		TreePath path = treeTable.getTree().getPathForRow(rowIndex);
		if (path == null)
		{
			return null;
		}

		TreeTableNode node = (TreeTableNode) path.getLastPathComponent();

		return node.getColumnValue(columnIndex);
	}

	/**
	 * Gets the number of columns contained to be used in the {@link JTreeTable}
	 *
	 * @return The number of columns
	 */
	public int getColumnCount()
	{
		return columnHeader == null ? 0 : columnHeader.length;
	}

	/**
	 * Gets the column header for the specified column index.
	 *
	 * @param columnIndex Index of the column
	 * @return The column header
	 */
	public String getColumnName(int columnIndex)
	{
		if (columnHeader == null)
		{
			return super.getColumnName(columnIndex);
		}

		if (columnHeader.length < columnIndex)
		{
			return super.getColumnName(columnIndex);
		}

		return columnHeader [columnIndex];
	}

	//////////////////////////////////////////////////
	// @@ Default implementations for methods in
	//    the TreeModel interface.
	//////////////////////////////////////////////////

	/**
	 * Returns the root of the tree.  Returns null
	 * only if the tree has no nodes.
	 *
	 * @return  the root of the tree
	 */
	public Object getRoot()
	{
		return root;
	}

	/**
	 * Sets the root.
	 * @param root The root to set
	 */
	public void setRoot(TreeTableNode root)
	{
		Object oldRoot = this.root;

		this.root = root;
		if (root == null && oldRoot != null)
		{
			fireTreeStructureChanged(this, null);
		}
		else
		{
			fireNodeStructureChanged(root);
		}
	}

	/**
	 * Returns the number of children DefaultTreeTableNodes the receiver
	 * contains. See javax.swing.tree.TreeModel.getChildCount(Object).
	 *
	 * @param parent The node from which the child count is required
	 * @return The number of children the parent node contains
	 */
	public int getChildCount(Object parent)
	{
		if (parent instanceof TreeTableNode)
		{
			return ((TreeTableNode) parent).getChildCount();
		}

		return 0;
	}

	/**
	 * Returns true if the receiver is a leaf. For further information see
	 * javax.swing.tree.TreeModel.isLeaf(Object)
	 *
	 * @param node The node to be examined
	 * @return True if the node is a leaf, else false
	 */
	public boolean isLeaf(Object node)
	{
		if (node instanceof TreeTableNode)
		{
			return ((TreeTableNode) node).isLeaf();
		}

		return false;
	}

	/**
	 * Returns the child of parent at index index
	 * in the parent's child array. See javax.swing.tree.TreeModel.getChild(Object, int)
	 *
	 * @param parent The node in the tree, obtained from this data source
	 * @param index The index of the child node in the parent array
	 * @return The child of parent at index index
	 */
	public Object getChild(Object parent, int index)
	{
		if (parent instanceof TreeTableNode)
		{
			Object child = ((TreeTableNode) parent).getChildAt(index);

			return child;
		}

		return null;
	}

	/**
	 * Returns the index of child in parent.
	 * If either the parent or child is null or not of type
	 * DefaultTreeTableNode, returns -1.
	 * @param parent a note in the tree, obtained from this data source
	 * @param child the node we are interested in
	 * @return the index of the child in the parent, or -1
	 *    if either the parent or the child is null
	 */
	public int getIndexOfChild(Object parent, Object child)
	{
		if (parent instanceof TreeTableNode && child instanceof TreeTableNode)
		{
			return ((TreeTableNode) parent).getIndex((TreeTableNode) child);
		}

		return -1;
	}

	/**
	 * Messaged when the user has altered the value for the item identified
	 * by path to newValue.
	 * If newValue signifies a truly new value
	 * the model should post a treeNodesChanged event.
	 *
	 * @param path path to the node that the user has altered
	 * @param newValue the new value from the TreeCellEditor
	 */
	public void valueForPathChanged(TreePath path, Object newValue)
	{
		// This is not called in the JTree's default mode: use a native implementation.
	}

	/**
	 * Used by the tree table to determine if a specified cell is selectable.
	 *
	 * @param row Row index of the specified cell
	 * @param column Column index of the specified cell
	 *
	 * @return
	 *		true	if the cell is selectable<br>
	 *		false	if the cell isn't selectable.
	 */
	public boolean isCellSelectable(int row, int column)
	{
		return treeTable != null && column == treeTable.getTreeCol();
	}

	/**
	 * Adds a listener for the TreeModelEvent posted after the tree changes.
	 *
	 * @param l the listener to add
	 */
	public void addTreeModelListener(TreeModelListener l)
	{
		listenerList.add(TreeModelListener.class, l);
	}

	/**
	 * Removes a listener previously added with addTreeModelListener.
	 *
	 * @param l the listener to remove
	 */
	public void removeTreeModelListener(TreeModelListener l)
	{
		listenerList.remove(TreeModelListener.class, l);
	}

	/**
	 * Builds the parents of node up to and including the root node,
	 * where the original node is the last element in the returned array.
	 * The length of the returned array gives the node's depth in the
	 * tree.
	 *
	 * @param aNode the TreeNode to get the path for
	 * @return an array of TreeNodes giving the path from the root to the specified node
	 */
	public TreeNode [] getPathToRoot(TreeNode aNode)
	{
		return getPathToRoot(aNode, 0);
	}

	/**
	 * Builds the parents of node up to and including the root node,
	 * where the original node is the last element in the returned array.
	 * The length of the returned array gives the node's depth in the
	 * tree.
	 *
	 * @param aNode  the TreeNode to get the path for
	 * @param depth  an int giving the number of steps already taken towards
	 *        the root (on recursive calls), used to size the returned array
	 * @return an array of TreeNodes giving the path from the root to the
	 *         specified node
	 */
	protected TreeNode [] getPathToRoot(TreeNode aNode, int depth)
	{
		TreeNode [] retNodes;

		// This method recurses, traversing towards the root in order
		// size the array. On the way back, it fills in the nodes,
		// starting from the root and working back to the original node.

		/* Check for null, in case someone passed in a null node, or
		 they passed in an element that isn't rooted at root. */
		if (aNode == null)
		{
			if (depth == 0)
			{
				return null;
			}
			retNodes = new TreeNode [depth];
		}
		else
		{
			depth++;
			if (aNode == root)
			{
				retNodes = new TreeNode [depth];
			}
			else
			{
				retNodes = getPathToRoot(aNode.getParent(), depth);
			}

			retNodes [retNodes.length - depth] = aNode;
		}

		return retNodes;
	}

	//////////////////////////////////////////////////
	// @@ Model listeners
	//////////////////////////////////////////////////

	/**
	 * Returns an array of all the tree model listeners
	 * registered on this model.
	 *
	 * @return all of this model's TreeModelListeners or an empty
	 *         array if no tree model listeners are currently registered
	 */
	public TreeModelListener [] getTreeModelListeners()
	{
		return listenerList.getListeners(TreeModelListener.class);
	}

	/**
	 * Invoke this method if you've totally changed the children of
	 * node and its childrens children...  This will post a
	 * treeStructureChanged event.
	 *
	 * @param node The node where the structture has changed
	 */
	public void fireNodeStructureChanged(TreeNode node)
	{
		if (node != null)
		{
			fireTreeStructureChanged(node, getPathToRoot(node), null, null);
		}
	}

	/**
	 * Invoke this method if you've inserted a new node into the model.
	 *
	 * @param parent Parent of the node that was inserted
	 */
	public void fireNodeInserted(TreeNode parent)
	{
		if (parent != null)
		{
			fireTreeNodesInserted(parent, getPathToRoot(parent), null, null);
		}
	}

	/**
	 * Invoke this method if you've removed a node from the model.
	 *
	 * @param parent Parent of the node that was removed
	 */
	public void fireNodeRemoved(TreeNode parent)
	{
		if (parent != null)
		{
			fireTreeNodesRemoved(parent, getPathToRoot(parent), null, null);
		}
	}

	/**
	 * Invoke this method if you've totally changed the children of
	 * node and its childrens children...  This will post a
	 * treeStructureChanged event.
	 *
	 * @param node The node where the structure has changed
	 */
	public void fireNodeChanged(TreeNode node)
	{
		if (node != null)
		{
			fireTreeNodesChanged(node, getPathToRoot(node), null, null);
		}
	}

	/**
	 * Notifies all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 *
	 * @param source the node being changed
	 * @param path the path to the root node
	 * @param childIndices the indices of the changed elements
	 * @param children the changed elements
	 */
	protected void fireTreeNodesChanged(Object source, Object [] path, int [] childIndices, Object [] children)
	{
		// Guaranteed to return a non-null array
		Object [] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
		{
			if (listeners [i] == TreeModelListener.class)
			{
				// Lazily create the event:
				if (e == null)
				{
					e = new TreeModelEvent(source, path, childIndices, children);
				}

				if (listeners [i + 1] instanceof TreeTableModelListener)
				{
					((TreeTableModelListener) listeners [i + 1]).beforeChanges(TableModelEvent.UPDATE);
				}

				((TreeModelListener) listeners [i + 1]).treeNodesChanged(e);
			}
		}
	}

	/**
	 * Notifies all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 *
	 * @param source the node where new elements are being inserted
	 * @param path the path to the root node
	 * @param childIndices the indices of the new elements
	 * @param children the new elements
	 */
	protected void fireTreeNodesInserted(Object source, Object [] path, int [] childIndices, Object [] children)
	{
		// Guaranteed to return a non-null array
		Object [] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
		{
			if (listeners [i] == TreeModelListener.class)
			{
				// Lazily create the event:
				if (e == null)
				{
					e = new TreeModelEvent(source, path, childIndices, children);
				}

				if (listeners [i + 1] instanceof TreeTableModelListener)
				{
					((TreeTableModelListener) listeners [i + 1]).beforeChanges(TableModelEvent.INSERT);
				}

				((TreeModelListener) listeners [i + 1]).treeNodesInserted(e);
			}
		}
	}

	/**
	 * Notifies all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 *
	 * @param source the node where elements are being removed
	 * @param path the path to the root node
	 * @param childIndices the indices of the removed elements
	 * @param children the removed elements
	 */
	protected void fireTreeNodesRemoved(Object source, Object [] path, int [] childIndices, Object [] children)
	{
		// Guaranteed to return a non-null array
		Object [] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
		{
			if (listeners [i] == TreeModelListener.class)
			{
				// Lazily create the event:
				if (e == null)
				{
					e = new TreeModelEvent(source, path, childIndices, children);
				}

				if (listeners [i + 1] instanceof TreeTableModelListener)
				{
					((TreeTableModelListener) listeners [i + 1]).beforeChanges(TableModelEvent.DELETE);
				}

				((TreeModelListener) listeners [i + 1]).treeNodesRemoved(e);
			}
		}
	}

	/**
	 * Notifies all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 *
	 * @param source the node where the tree model has changed
	 * @param path the path to the root node
	 * @param childIndices the indices of the affected elements
	 * @param children the affected elements
	 */
	protected void fireTreeStructureChanged(Object source, Object [] path, int [] childIndices, Object [] children)
	{
		// Guaranteed to return a non-null array
		Object [] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
		{
			if (listeners [i] == TreeModelListener.class)
			{
				// Lazily create the event:
				if (e == null)
				{
					e = new TreeModelEvent(source, path, childIndices, children);
				}

				/* TODO Cleanup 5: Dirty hack to prevent NPE
				 java.lang.reflect.InvocationTargetException
				 caused by : java.lang.NullPointerException
				 at javax.swing.tree.VariableHeightLayoutCache.rebuild (VariableHeightLayoutCache.java : 724)
				 at javax.swing.tree.VariableHeightLayoutCache.treeStructureChanged (VariableHeightLayoutCache.java : 626)
				 at javax.swing.plaf.basic.BasicTreeUI$TreeModelHandler.treeStructureChanged (BasicTreeUI.java : 2469)
				 ... treetable.SimpleTreeTableModel.fireTreeStructureChanged (SimpleTreeTableModel.java : 683)
				 ... treetable.SimpleTreeTableModel.fireNodeStructureChanged (SimpleTreeTableModel.java : 486)
				 ... treetable.SimpleTreeTableModel.setRoot (SimpleTreeTableModel.java : 272)
				 at org.openbp.cockpit.modeler.plugins.debugger.InspectorPlugin.refresh (InspectorPlugin.java : 161)
				 at org.openbp.cockpit.modeler.plugins.debugger.InspectorPlugin$ServerEvents.nodeentry (InspectorPlugin.java : 367)
				 at sun.reflect.GeneratedMethodAccessor142.invoke (Unknown Source)
				 at sun.reflect.DelegatingMethodAccessorImpl.invoke (DelegatingMethodAccessorImpl.java : 25)
				 at java.lang.reflect.Method.invoke (Method.java : 324)
				 ... EventModule.eventFired (EventModule.java : 265)
				 ... AbstractPlugin.doHandleEvent (AbstractPlugin.java : 571)
				 ... AbstractPlugin.handleEvent (AbstractPlugin.java : 551)
				 ... AbstractPlugin.inheritEvent (AbstractPlugin.java : 652)
				 ... AbstractPlugin.passDown (AbstractPlugin.java : 501)
				 ... AbstractPlugin.receiveEvent (AbstractPlugin.java : 607)
				 ... AbstractPlugin.receiveEvent (AbstractPlugin.java : 623)
				 ... AbstractPlugin.fireEvent (AbstractPlugin.java : 451)
				 at org.openbp.cockpit.modeler.plugins.debugger.DebuggerPlugin$PollingThread.run (DebuggerPlugin.java : 1539)
				 */
				try
				{
					((TreeModelListener) listeners [i + 1]).treeStructureChanged(e);
				}
				catch (Exception ex)
				{
				}
			}
		}
	}

	/**
	 * Notifies all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 *
	 * @param source the node where the tree model has changed
	 * @param path the path to the root node
	 */
	private void fireTreeStructureChanged(Object source, TreePath path)
	{
		// Guaranteed to return a non-null array
		Object [] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
		{
			if (listeners [i] == TreeModelListener.class)
			{
				// Lazily create the event:
				if (e == null)
				{
					e = new TreeModelEvent(source, path);
				}

				if (listeners [i + 1] instanceof TreeTableModelListener)
				{
					((TreeTableModelListener) listeners [i + 1]).beforeChanges(TreeTableModelListener.NO_TABLE_MODEL_EVENT_TYPE);
				}

				((TreeModelListener) listeners [i + 1]).treeStructureChanged(e);
			}
		}
	}

	/**
	 * Notifies all tree table listeners that all changes have been completed.
	 *
	 * @param tableEventType The table event type of TableModelEvent
	 */
	public void fireTableChangesCompleted(int tableEventType)
	{
		// Guaranteed to return a non-null array
		Object [] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
		{
			if (listeners [i] == TreeModelListener.class)
			{
				if (!(listeners [i + 1] instanceof TreeTableModelListener))
					continue;

				((TreeTableModelListener) listeners [i + 1]).afterChanges(tableEventType);
			}
		}
	}

	/**
	 * Returns an array of all the objects currently registered
	 * as FooListeners
	 * upon this model.
	 * FooListeners are registered using the
	 * addFooListener method.
	 *
	 * <p>
	 *
	 * You can specify the listenerType argument
	 * with a class literal,
	 * such as
	 * FooListener.class.
	 * For example, you can query a
	 * DefaultTreeModel m
	 * for its tree model listeners with the following code:
	 *
	 * <pre>TreeModelListener[] tmls = (TreeModelListener[])(m.getListeners(TreeModelListener.class));</pre>
	 *
	 * If no such listeners exist, this method returns an empty array.
	 *
	 * @param listenerType the type of listeners requested; this parameter
	 *          should specify an interface that descends from
	 *          java.util.EventListener
	 * @return an array of all objects registered as
	 *          FooListeners on this component,
	 *          or an empty array if no such
	 *          listeners have been added
	 * @exception ClassCastException if listenerType
	 *          doesn't specify a class or interface that implements
	 *          java.util.EventListener
	 */
	public EventListener [] getListeners(Class listenerType)
	{
		return listenerList.getListeners(listenerType);
	}

	/**
	 * Invokes fireTableDataChanged after all the pending events have been
	 * processed. SwingUtilities.invokeLater is used to handle this.
	 *
	 * @param tableEventType The table event type of TableModelEvent
	 */
	protected void delayedFireTableDataChanged(int tableEventType)
	{
		SwingUtilities.invokeLater(new DataChangeRunnable(tableEventType));
	}

	/**
	 * Private runnable class to pass the table event type.
	 */
	private class DataChangeRunnable
		implements Runnable
	{
		//////////////////////////////////////////////////
		// @@ Members
		//////////////////////////////////////////////////

		/* Table event type */
		private int tableEventType;

		//////////////////////////////////////////////////
		// @@ Construction
		//////////////////////////////////////////////////

		/**
		 * Constructor.
		 *
		 * @param tableEventType The table event type of TableModelEvent
		 */
		private DataChangeRunnable(int tableEventType)
		{
			this.tableEventType = tableEventType;
		}

		/**
		 * Runnable implementation.
		 */
		public void run()
		{
			if (treeTable == null)
				return;

			fireTableStructureChanged();
			fireTableChangesCompleted(tableEventType);
		}
	}

	/**
	 * A TreeModelListener that can update the table when the tree has changed. The use of
	 * delayedFireTableDataChanged is made since no guarantee is given that the tree will
	 * have finished processing the event before this was fired.
	 */
	class SimpleTreeTableModelListener
		implements TreeModelListener
	{
		/**
		 * See javax.swing.tree.TreeModelListener.treeNodesChanged (TreeModelEvent e).
		 * @nowarn
		 */
		public void treeNodesChanged(TreeModelEvent e)
		{
			delayedFireTableDataChanged(TableModelEvent.UPDATE);
		}

		/**
		 * See javax.swing.tree.TreeModelListener.treeNodesInserted (TreeModelEvent e).
		 * @nowarn
		 */
		public void treeNodesInserted(TreeModelEvent e)
		{
			delayedFireTableDataChanged(TableModelEvent.INSERT);
		}

		/**
		 * See javax.swing.tree.TreeModelListener.treeNodesRemoved (TreeModelEvent e).
		 * @nowarn
		 */
		public void treeNodesRemoved(TreeModelEvent e)
		{
			delayedFireTableDataChanged(TableModelEvent.DELETE);
		}

		/**
		 * See javax.swing.tree.TreeModelListener.treeStructureChanged (TreeModelEvent e).
		 * @nowarn
		 */
		public void treeStructureChanged(TreeModelEvent e)
		{
			delayedFireTableDataChanged(TreeTableModelListener.NO_TABLE_MODEL_EVENT_TYPE);
		}
	}

	/**
	 * A TreeExpansionListener that notifies the table that changes in the tree have occurred.
	 */
	class TreeTableExpansionListener
		implements TreeExpansionListener
	{
		/**
		 * See javax.swing.tree.TreeExpansionListener.treeExpanded (TreeExpansionEvent event).
		 * @nowarn
		 */
		public void treeExpanded(TreeExpansionEvent event)
		{
			fireTableDataChanged();
		}

		/**
		 * See javax.swing.tree.TreeExpansionListener.treeCollapsed (TreeExpansionEvent event).
		 * @nowarn
		 */
		public void treeCollapsed(TreeExpansionEvent event)
		{
			fireTableDataChanged();
		}
	}
}
