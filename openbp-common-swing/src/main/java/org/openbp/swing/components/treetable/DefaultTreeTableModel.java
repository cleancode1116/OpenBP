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

import javax.swing.tree.TreePath;

/**
 * The DefaultTreeTable model implements the TreeTableModel interface and extends the
 * AbstractTableModel. The TreeTableModel combines the TableModel and TreeModel interfaces,
 * thus providing a combined model for the JTreeTable. The model structure for the tree table
 * is based on the DefaultTreeTableNode, that implements functionality for holding all the column
 * objects of the table in a tree node. This provides a mechanism where the DefaultTreeTableNode
 * presents both a table row and a tree node. In order to build the structure for the tree table,
 * a DefaultTreeTableNode containing all the column objects should be added to the
 * DefaultTreeTableModel with the addNode() method. It is important to note that column value (0)
 * is the value used to display the value in the tree. This object may either be a String, or a
 * JLabel. In the case of  a String, the String will be embedded into a JLabel with the appropriate
 * system icon for open, closed or leaf nodes. In the case of a JLabel, this label will be used
 * with the icon implemented in the label.
 *
 * @author Erich Lauterbach
 */
public class DefaultTreeTableModel extends SimpleTreeTableModel
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Holds the column count for the node with the maximum column count. */
	private int maxColumnCount;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default Constructor
	 *
	 * @param root The root node for the TreeTable
	 */
	public DefaultTreeTableModel(DefaultTreeTableNode root)
	{
		super(root);
	}

	//////////////////////////////////////////////////
	// @@ TableModel implementation
	//////////////////////////////////////////////////

	/**
	 * Returns the number of columns contained in the table.
	 *
	 * @return The number of columns contained in the table
	 */
	public int getColumnCount()
	{
		if (maxColumnCount == 0)
			maxColumnCount = super.getColumnCount();

		return maxColumnCount;
	}

	//////////////////////////////////////////////////
	// @@ TreeModel implementation
	//////////////////////////////////////////////////

	/**
	 * This sets the user object of the TreeNode identified by path
	 * and posts a node changed.  If you use custom user objects in
	 * the TreeModel you're going to need to subclass this and
	 * set the user object of the changed node to something meaningful.
	 *
	 * @param path The treePath for identification
	 * @param newValue The new user object
	 */
	public void valueForPathChanged(TreePath path, Object newValue)
	{
		Object pathNode = path.getLastPathComponent();

		if (pathNode instanceof DefaultTreeTableNode)
		{
			DefaultTreeTableNode node = (DefaultTreeTableNode) pathNode;
			node.setColumnValue(0, newValue);

			// TODO Minor: Have to fire an event to inform that the tree value changed.
		}
	}

	//////////////////////////////////////////////////
	// @@ Public methods
	//////////////////////////////////////////////////

	/**
	 * Adds a new node to the tree. The node is of type
	 * DefaultTreeTableNode, thus should containe all the
	 * column objects.
	 *
	 * @param parent The parent node to which the specified node
	 * is to be added. The parent the parent is null, then the node
	 * will use the parent set within the node, and if this is null,
	 * then the node will added to the root node.
	 * @param child Node to add
	 */
	public void addNode(DefaultTreeTableNode parent, DefaultTreeTableNode child)
	{
		if (parent == null)
			parent = (DefaultTreeTableNode) child.getParent();

		if (parent == null)
			parent = (DefaultTreeTableNode) root;

		parent.addChild(child);

		// Set the column counter
		if (child.getColumnCount() > maxColumnCount)
			maxColumnCount = child.getColumnCount();

		fireNodeStructureChanged(parent);
	}

	/**
	 * Removed the specified node from the model. Note!: If the node contains
	 * children, the these will also be removed.
	 *
	 * @param node The node to be removed from the model
	 */
	public void removeNode(DefaultTreeTableNode node)
	{
		DefaultTreeTableNode parent = (DefaultTreeTableNode) node.getParent();
		parent.removeChild(node);

		fireNodeStructureChanged(parent);
	}
}
