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
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

/**
 * General-purpose tree node that contains a parent tree node link
 * and a list of child tree nodes.
 *
 * @author Heiko Erhardt
 */
public class DefaultTreeNode
	implements TreeNode
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Parent tree node */
	protected TreeNode parent;

	/** Child list (contains {@link javax.swing.tree.TreeNode} objects) */
	private List childList;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public DefaultTreeNode()
	{
	}

	/**
	 * Default constructor.
	 *
	 * @param parent Parent tree node
	 */
	public DefaultTreeNode(TreeNode parent)
	{
		this.parent = parent;
	}

	//////////////////////////////////////////////////
	// @@ TreeNode implementation
	//////////////////////////////////////////////////

	/**
	 * Returns an Enumeration off all children.
	 *
	 * @return an Enumeration value
	 */
	public Enumeration children()
	{
		return Collections.enumeration(childList);
	}

	/**
	 * Returns the index of this child node.
	 *
	 * @param treeNode The tree node
	 * @return 0-based index or -1 if the node is not a child node of this one
	 */
	public int getIndex(TreeNode treeNode)
	{
		if (childList != null)
		{
			return childList.indexOf(treeNode);
		}
		return -1;
	}

	/**
	 * Determines if this is a leaf node.
	 *
	 * @return
	 *		true	This node has no children.<br>
	 *		false	This node has at least one child.
	 */
	public boolean isLeaf()
	{
		return childList == null || childList.size() == 0;
	}

	/**
	 * Returns the child at the given position.
	 *
	 * @param index The 0-based position
	 * @return The node at this position or 0 if no such position exists
	 */
	public TreeNode getChildAt(int index)
	{
		if (childList != null && index >= 0 && index < childList.size())
		{
			return (TreeNode) childList.get(index);
		}
		return null;
	}

	/**
	 * Returns the number of children in the entry.
	 *
	 * @return an int value
	 */
	public int getChildCount()
	{
		return childList != null ? childList.size() : 0;
	}

	/**
	 * Returns false.
	 * @nowarn
	 */
	public boolean getAllowsChildren()
	{
		return false;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the parent tree node.
	 * @nowarn
	 */
	public TreeNode getParent()
	{
		return parent;
	}

	/**
	 * Sets the parent tree node.
	 * @nowarn
	 */
	public void setParent(TreeNode parent)
	{
		this.parent = parent;
	}

	/**
	 * Adds a child.
	 * @param child The child to add
	 */
	public void addChild(DefaultTreeNode child)
	{
		if (childList == null)
			childList = new ArrayList();
		childList.add(child);
		child.setParent(this);
	}

	/**
	 * Inserts a child into the child list.
	 * @param child The child to add
	 * @param index 0-based index of the node the should preceede the new child
	 */
	public void insertChild(DefaultTreeNode child, int index)
	{
		if (childList == null)
		{
			addChild(child);
			return;
		}
		childList.add(index, child);
		child.setParent(this);
	}

	/**
	 * Removes a child.
	 * @param child The child to remove
	 */
	public void removeChild(DefaultTreeNode child)
	{
		if (childList != null)
		{
			child.setParent(null);
			childList.remove(child);
		}
	}

	/**
	 * Clears the child list.
	 */
	public void clearChilds()
	{
		childList = null;
	}

	/**
	 * Gets the child list.
	 * @return A list of {@link javax.swing.tree.TreeNode} objects
	 */
	public List getChildList()
	{
		return childList;
	}

	/**
	 * Sets the child list.
	 * @param childList A list of {@link javax.swing.tree.TreeNode} objects
	 */
	public void setChildList(List childList)
	{
		this.childList = childList;
	}
}
