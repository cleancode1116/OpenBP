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
package org.openbp.swing.components.tree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Expander that automatically expands a tree control so that the available drawing area of the tree
 * is filled to the maximum extend without the need of displaying a scrollbar.
 *
 * @author Baumgartner Michael
 */
public class TreeExpander
{
	//////////////////////////////////////////////////
	// @@ Member
	//////////////////////////////////////////////////

	/** The tree to expand. */
	private JTree tree;

	//////////////////////////////////////////////////
	// @@ Contructor
	//////////////////////////////////////////////////

	/**
	 * Contructor.
	 * @param tree The tree to expand
	 */
	public TreeExpander(JTree tree)
	{
		this.tree = tree;
	}

	//////////////////////////////////////////////////
	// @@ Simple expand
	//////////////////////////////////////////////////

	/**
	 * Expand the levels of a tree only if the children of the
	 * level can be displayed.
	 * @param level The level of the expansion
	 */
	public void simpleExpand(int level)
	{
		expandLevel(0);
		if (tree.getRowCount() == 0 || level == 0)
			return;

		simpleExpandLevel(1, level);
	}

	/**
	 * Expand the level only if all children of the level can
	 * be displayed.
	 * @param currentlevel The level to expand
	 * @param maxlevel The maximal level of the expansion
	 */
	private void simpleExpandLevel(int currentlevel, int maxlevel)
	{
		if (currentlevel > maxlevel)
			return;

		int rowsToExpand = getRowsToExpand();
		if (rowsToExpand <= 0)
			return;

		List childrenOfLevel = getNodesOfLevel(currentlevel);
		if (childrenOfLevel.size() > rowsToExpand || childrenOfLevel.size() == 0)
			return;

		expandLevel(currentlevel);
		simpleExpandLevel(currentlevel + 1, maxlevel);
	}

	/**
	 * Get the nodes of a level, starting from the root node.
	 * @param level The level of the nodes whereas 0 are
	 * the children of the root node.
	 * @return List with {@link TreeNode} objects
	 */
	private List getNodesOfLevel(int level)
	{
		return getNodesOfLevel((TreeNode) tree.getModel().getRoot(), level);
	}

	/**
	 * Get the nodes of a level, starting from the root node.
	 * @param parent The node to start from
	 * @param level The level of the nodes whereas 0 are
	 * the children of the root node.
	 * @return List with {@link TreeNode} objects
	 */
	private List getNodesOfLevel(TreeNode parent, int level)
	{
		if (level == 0)
		{
			List children = new ArrayList();
			for (int i = 0; i < parent.getChildCount(); i++)
			{
				children.add(parent.getChildAt(i));
			}
			return children;
		}
		List children = new ArrayList();
		for (int i = 0; i < parent.getChildCount(); i++)
		{
			children.addAll(getNodesOfLevel(parent.getChildAt(i), level - 1));
		}
		return children;
	}

	//////////////////////////////////////////////////
	// @@ Intelli expander method
	//////////////////////////////////////////////////

	/**
	 * Expand the tree in a way that no scroll bar is needed. But the
	 * display of the tree is as full as possible.
	 *
	 * @param level The level of the expansion. Level 0 expands only
	 * the children of the root node. Level 1 the children of the children of
	 * the root.
	 */
	public void intelliExpand(int level)
	{
		expandLevel(0);
		if (tree.getRowCount() == 0)
			return;

		int rowsToExpand = getRowsToExpand();
		if (rowsToExpand < 2)
			return;

		TreeNode rootNode = (TreeNode) tree.getModel().getRoot();
		TreeNode [] nodes = getPossibleExpandNodes(rootNode, rowsToExpand);
		TreePath parent = new TreePath(rootNode);
		expandRows(parent, nodes);

		if (level == 0)
			return;
		intelliExpandLevel(parent, level - 1);
	}

	/**
	 * Expand the tree in a way that no scroll bar is needed. But the
	 * display of the tree is as full as possible.
	 * @param parentPath The path to the parent node
	 * @param level The current level
	 */
	private void intelliExpandLevel(TreePath parentPath, int level)
	{
		int rowsToExpand = getRowsToExpand();
		if (rowsToExpand <= 0 || level < 0)
			return;

		TreeNode [] pathArray = createPathArray(parentPath);
		TreeNode parentNode = (TreeNode) parentPath.getLastPathComponent();
		for (int i = 0; i < parentNode.getChildCount(); i++)
		{
			TreeNode child = parentNode.getChildAt(i);
			TreeNode [] nodesToExpand = getPossibleExpandNodes(child, rowsToExpand);
			if (nodesToExpand.length != 0)
			{
				pathArray [pathArray.length - 1] = child;
				expandRows(new TreePath(pathArray), nodesToExpand);
				rowsToExpand = getRowsToExpand();
			}
			if (rowsToExpand <= 0)
				break;
		}

		rowsToExpand = getRowsToExpand();
		if (rowsToExpand <= 0)
			return;

		pathArray = createPathArray(parentPath);
		for (int i = 0; i < parentNode.getChildCount(); i++)
		{
			TreeNode child = parentNode.getChildAt(i);
			pathArray [pathArray.length - 1] = child;
			intelliExpandLevel(new TreePath(pathArray), level - 1);
			rowsToExpand = getRowsToExpand();
			if (rowsToExpand <= 0)
				break;
		}
	}

	//////////////////////////////////////////////////
	// @@ Normal expand methods
	//////////////////////////////////////////////////

	/**
	 * Expand all nodes till a level. The level 0 means all children
	 * of the root node are visible. Level 1 means all children of the
	 * children of the root are visible and so on.
	 * @param level The level to expand to
	 */
	public void expandLevel(int level)
	{
		TreeNode rootNode = (TreeNode) tree.getModel().getRoot();
		expandLevel(new TreePath(rootNode), level - 1);
	}

	/**
	 * Expands the children of the given path. If the level
	 * has been reached, all the children will be collapsed.
	 * @param parentPath The path to expand
	 * @param level The level
	 */
	private void expandLevel(TreePath parentPath, int level)
	{
		TreeNode [] pathArray = createPathArray(parentPath);
		TreeNode parentNode = (TreeNode) parentPath.getLastPathComponent();
		for (int i = 0; i < parentNode.getChildCount(); i++)
		{
			TreeNode child = parentNode.getChildAt(i);
			pathArray [pathArray.length - 1] = child;
			TreePath currentPath = new TreePath(pathArray);
			if (level < 0)
			{
				collapsePath(currentPath);
				tree.collapsePath(new TreePath(pathArray));
			}
			else
			{
				tree.expandPath(new TreePath(pathArray));
				expandLevel(new TreePath(pathArray), level - 1);
			}
		}
	}

	/**
	 * Collapse the given path.
	 * @param parentPath The path to collapse
	 */
	private void collapsePath(TreePath parentPath)
	{
		TreeNode [] pathArray = createPathArray(parentPath);
		TreeNode parentNode = (TreeNode) parentPath.getLastPathComponent();
		for (int i = 0; i < parentNode.getChildCount(); i++)
		{
			TreeNode child = parentNode.getChildAt(i);
			pathArray [pathArray.length - 1] = child;
			TreePath currentPath = new TreePath(pathArray);
			collapsePath(currentPath);
			tree.collapsePath(currentPath);
		}
	}

	//////////////////////////////////////////////////
	// @@ Support
	//////////////////////////////////////////////////

	/**
	 * Get the maximum number of rows that can be displayed.
	 * @param currentlyDisplayedRows The currently displayed row count
	 * @return maximum rows
	 */
	private int getMaxDisplayedRows(int currentlyDisplayedRows)
	{
		int height;
		if (tree.getParent() instanceof JViewport)
			height = tree.getParent().getHeight();
		else
			height = tree.getHeight();
		return height / (tree.getPreferredSize().height / currentlyDisplayedRows);
	}

	/**
	 * Get the number of rows that can be expanded.
	 * @return rows to expand
	 */
	private int getRowsToExpand()
	{
		int displayedRows = tree.getRowCount();
		int maxDisplayable = getMaxDisplayedRows(displayedRows);
		return maxDisplayable - displayedRows;
	}

	/**
	 * Create an array for a new tree path. The last element is empty.
	 * @param parent The path of the parent node
	 * @return Array of tree nodes
	 */
	private TreeNode [] createPathArray(TreePath parent)
	{
		Object [] oPath = parent.getPath();
		TreeNode [] nodePath = new TreeNode [oPath.length + 1];
		System.arraycopy(oPath, 0, nodePath, 0, oPath.length);
		return nodePath;
	}

	/**
	 * Expands the nodes of the tree
	 * @param parent The path to the parent of the nodes that are to be expanded
	 * @param nodes The node that will be expanded
	 */
	private void expandRows(TreePath parent, TreeNode [] nodes)
	{
		Object [] oPath = parent.getPath();
		TreeNode [] nodePath = new TreeNode [oPath.length + 1];
		System.arraycopy(oPath, 0, nodePath, 0, oPath.length);
		for (int i = 0; i < nodes.length; i++)
		{
			nodePath [oPath.length] = nodes [i];
			TreePath path = new TreePath(nodePath);
			tree.expandPath(path);
		}
	}

	/**
	 * Get the nodes the can be expanded and still be displayed.
	 * @param node The node whose children could be expanded
	 * @param rowsToDisplay The number of rows that can be displayed
	 * @return Array of nodes that can be expanded
	 */
	private TreeNode [] getPossibleExpandNodes(TreeNode node, int rowsToDisplay)
	{
		for (int i = 0; i < node.getChildCount(); i++)
		{
			TreeNode child = node.getChildAt(i);
			if (child.isLeaf())
				rowsToDisplay--;
		}
		if (rowsToDisplay <= 0)
			return new TreeNode [0];

		// Expand hold only one row each
		List expand = new ArrayList();
		List nodes = new ArrayList();
		for (int i = 0; i < node.getChildCount(); i++)
		{
			nodes.add(node.getChildAt(i));
			TreeRow row = new TreeRow();
			row.addRow(node.getChildAt(i));
			if (row.getRowSum() <= rowsToDisplay && (row.getRowSum() != 0))
			{
				expand.add(row);
			}
		}

		// Add a new row to the expansion
		while (true)
		{
			List expand2 = addNewRow(nodes, expand, rowsToDisplay);
			if (expand2.size() == 0)
				break;
			expand = expand2;
		}
		if (expand.size() != 0)
			return ((TreeRow) expand.get(0)).getNodes();
		return new TreeNode [0];
	}

	/**
	 * Add a new row to the list of rows that can be expanded. It is tried
	 * to add the nodes to the expander. If the expander uses already the node
	 * then the next node is tried. If the node can be added to the expander
	 * it is check if the resuling row can still be displayed. If they can then
	 * the new expander is added to the new list with possible expanders.
	 *
	 * @param nodes List of all possible nodes that can be added to
	 * the expander
	 * @param expand List with already found rows from the expander
	 * @param rowsToDisplay number of rows that can further be displayed in the tree
	 * @return The list with expanders that are possible
	 */
	private List addNewRow(List nodes, List expand, int rowsToDisplay)
	{
		List expand2 = new ArrayList();
		for (int i = 0; i < expand.size(); i++)
		{
			for (int j = 0; j < nodes.size(); j++)
			{
				TreeRow r1 = (TreeRow) expand.get(i);
				TreeNode node = (TreeNode) nodes.get(j);
				if (!r1.containsNode(node))
				{
					// The expander does not contain the node
					TreeRow row = new TreeRow();
					row.addRow(r1);
					row.addRow(node);
					if (row.getRowSum() <= rowsToDisplay && !hasListRows(expand2, row))
					{
						// The combination with the new node can be displayed.
						expand2.add(row);
					}
				}
			}
		}
		return expand2;
	}

	/**
	 * Check if the list contains already all rows from the expander
	 * @param expand The list with already possible found expansions
	 * @param row The row expander to check
	 * @return true the list contains all rows from the expander otherwise false
	 */
	private boolean hasListRows(List expand, TreeRow row)
	{
		for (int i = 0; i < expand.size(); i++)
		{
			TreeRow rowExpand = (TreeRow) expand.get(i);
			if (rowExpand.hasNodes(row.getNodes()))
				return true;
		}
		return false;
	}

	/**
	 * Wrapper for nodes that can be expanded.
	 *
	 * @author Baumgartner Michael
	 */
	public static class TreeRow
	{
		//////////////////////////////////////////////////
		// @@ Member
		//////////////////////////////////////////////////

		/** List of {@link javax.swing.tree.TreeNode} objects that can be expanded*/
		private List nodeList = new ArrayList();

		/** Number of rows, that will be displayed if the nodes are expanded. */
		private int rowSum = 0;

		//////////////////////////////////////////////////
		// @@ Getter/Setter
		//////////////////////////////////////////////////

		/**
		 * Add a new row to the expander.
		 * @param node The node to add
		 */
		public void addRow(TreeNode node)
		{
			nodeList.add(node);
			rowSum += node.getChildCount();
		}

		/**
		 * Add an existing expander.
		 * @param row The existing expander
		 */
		public void addRow(TreeRow row)
		{
			nodeList.addAll(row.nodeList);
			rowSum += row.rowSum;
		}

		/**
		 * Get the nodes that can be expanded.
		 * @return Array of {@link javax.swing.tree.TreeNode} objects
		 */
		public TreeNode [] getNodes()
		{
			return (TreeNode []) nodeList.toArray(new TreeNode [nodeList.size()]);
		}

		/**
		 * Get the number of rows that will be display, if the
		 * nodes are expanded.
		 * @return number of rows
		 */
		public int getRowSum()
		{
			return rowSum;
		}

		//////////////////////////////////////////////////
		// @@ Containment methods
		//////////////////////////////////////////////////

		/**
		 * Check if the expander contains a node.
		 * @param node The node to check for
		 * @return true if the expander contains the node otherwise false
		 */
		public boolean containsNode(TreeNode node)
		{
			return nodeList.contains(node);
		}

		/**
		 * Check if the expander contains a list of nodes
		 * @param nodes The nodes to check for
		 * @return true if all nodes are contained in the expander otherwise false
		 */
		public boolean hasNodes(TreeNode [] nodes)
		{
			for (int i = 0; i < nodes.length; i++)
			{
				if (!containsNode(nodes [i]))
					return false;
			}
			return true;
		}
	}
}
