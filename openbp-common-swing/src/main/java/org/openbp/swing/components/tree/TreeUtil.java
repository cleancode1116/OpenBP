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

import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Tree utility.
 *
 * @author Andreas Putz
 */
public class TreeUtil
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private TreeUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Expands all tree nodes or collapse these level by level.
	 *
	 * @param tree JTree
	 * @param parent The parent tree path
	 * @param expand
	 *  true  expands all nodes in the tree<br>
	 *  false collapses all nodes in the tree
	 * @param desiredLevel Maximum level that should be expanded (-1 for all)
	 */
	public static void expandTreeLevels(JTree tree, TreePath parent, boolean expand, int desiredLevel)
	{
		// Traverse children
		TreeNode node = (TreeNode) parent.getLastPathComponent();

		if (node.getChildCount() >= 0)
		{
			for (Enumeration e = node.children(); e.hasMoreElements();)
			{
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);

				if (desiredLevel > 0)
					expandTreeLevels(tree, path, expand, desiredLevel - 1);
				else if (desiredLevel != 0)
					expandTreeLevels(tree, path, expand, -1);
			}
		}

		// Expansion or collapse must be done bottom-up
		if (expand)
		{
			tree.expandPath(parent);
		}
		else
		{
			tree.collapsePath(parent);
		}
	}

	/**
	 * Expands all tree nodes or collapse these level by level.
	 *
	 * @param tree JTree
	 * @param expand
	 *  true  expands all nodes in the tree<br>
	 *  false collapses all nodes in the tree
	 * @param desiredLevel Maximum level that should be expanded (-1 for all)
	 */
	public static void expandTreeLevels(JTree tree, boolean expand, int desiredLevel)
	{
		if (desiredLevel != 0)
		{
			Object node = tree.getModel().getRoot();

			if (node instanceof TreeNode)
			{
				TreeNode root = (TreeNode) node;

				// Traverse tree from root
				expandTreeLevels(tree, new TreePath(root), expand, desiredLevel);
			}
		}
	}
}
