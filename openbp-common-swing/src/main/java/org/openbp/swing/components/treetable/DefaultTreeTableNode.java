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

import java.awt.Dimension;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.tree.TreeNode;

import org.openbp.common.util.iterator.EmptyEnumeration;

/**
 * Default implementation for the TreeTableNode, which is used in conjunction
 * with the {@link DefaultTreeTableModel}
 *
 * @author Erich Lauterbach
 */
public class DefaultTreeTableNode
	implements TreeTableNode
{
	//////////////////////////////////////////////////
	// @@ Static internal members
	//////////////////////////////////////////////////

	/**
	 * Empty label that is used to replace the object
	 * contained in the column vector, when that
	 * object is removed. This is done so that
	 * the vector does not adjust its length, and
	 * thus move all trailing columns when a
	 * column object is removed from the vector.
	 */
	public static final JLabel EMPTY_COLUMN = new JLabel(" ");

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** The column objects. */
	protected List columnObjects;

	/** The preferred size for this node. */
	protected Dimension preferredSize;

	/** TreeTableNode children. */
	protected Vector children;

	/** The Parent TreeTableNode to this one. */
	protected DefaultTreeTableNode parent;

	/** The last height used for this node. */
	private int lastHeight;

	//////////////////////////////////////////////////
	// @@ TreeTableNode implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the text field to be displayed for the node.
	 *
	 * @return The string value of the text
	 */
	public String getNodeText()
	{
		if (columnObjects != null && columnObjects.get(0) != null)
		{
			return columnObjects.get(0).toString();
		}

		return null;
	}

	/**
	 * Gets the preferred height of the node in the tree.
	 *
	 * @return The height as an int in pixels
	 */
	public Dimension getPreferredSize()
	{
		if (preferredSize == null)
		{
			preferredSize = new Dimension(0, 0);
			if (columnObjects != null)
			{
				for (int i = 0; i < columnObjects.size(); i++)
				{
					Object o = columnObjects.get(i);

					if (o instanceof JComponent)
					{
						JComponent component = (JComponent) o;
						Dimension componentSize = component.getPreferredSize();

						if (preferredSize.getHeight() < componentSize.getHeight())
							preferredSize = componentSize;
					}
				}
			}
		}

		return preferredSize;
	}

	/**
	 * Returns the child TreeNode at index
	 * childIndex.
	 *
	 * @param childIndex The index of the child of interest
	 * @return The javax.swing.tree.TreeNode child.
	 */
	public TreeNode getChildAt(int childIndex)
	{
		return (TreeTableNode) children.get(childIndex);
	}

	/**
	 * Returns the number of children TreeNodes the receiver
	 * contains.
	 *
	 * @return The number of children contained in receiver javax.swing.tree.TreeNode.
	 */
	public int getChildCount()
	{
		return children == null ? 0 : children.size();
	}

	/**
	 * Returns the parent TreeNode of the receiver.
	 *
	 * @return The parent javax.swing.tree.TreeNode.
	 */
	public TreeNode getParent()
	{
		return parent;
	}

	/**
	 * Returns the index of node in the receivers children.
	 * If the receiver does not contain node, -1 will be
	 * returned.
	 *
	 * @param node The node for which the index is required
	 * @return The index of the node of interest. If the node is non-existing, the -1 is returned.
	 */
	public int getIndex(TreeNode node)
	{
		if (node instanceof TreeTableNode)
		{
			TreeTableNode treeTableNode = (TreeTableNode) node;
			return children.indexOf(treeTableNode);
		}

		return -1;
	}

	/**
	 * Returns true if the receiver allows children. In the case
	 * of the DefaultTreeTable node it always allows children,
	 * thus always returns true.
	 *
	 * @return true if the receiver node allows children,<br>
	 *             false if no children are allowed.
	 */
	public boolean getAllowsChildren()
	{
		return true;
	}

	/**
	 * Determines if the receiver node is a leaf.
	 *
	 * @return true if the receiver node is a leaf. <br>
	 *             false is the receiver node is NOT a leaf.
	 */
	public boolean isLeaf()
	{
		return getChildCount() == 0;
	}

	/**
	 * Returns the children of the receiver as an Enumeration. If the receiver
	 * does not contain any children the an empty enumeration is returned.
	 *
	 * @return All the children contained in the receiver node as an Enumeration
	 */
	public Enumeration children()
	{
		if (children != null)
			return children.elements();

		return EmptyEnumeration.getInstance();
	}

	//////////////////////////////////////////////////
	// @@ DefaultTreeTableNode managment methods
	//////////////////////////////////////////////////

	/**
	 * Adds a TreeTable node as an child of the receiver node.
	 *
	 * @param child The child node to be added
	 * @nowarn
	 */
	public void addChild(DefaultTreeTableNode child)
	{
		if (children == null)
			children = new Vector();
		child.setParent(this);
		children.add(child);
	}

	/**
	 * Removes the child node from the receiver node.
	 *
	 * @param child The child node to be removed
	 * @return True if the child was successfully removed. <br>
	 *             False if an error occurred while removing the child.
	 */
	public boolean removeChild(DefaultTreeTableNode child)
	{
		return children.remove(child);
	}

	/**
	 * Removes all child nodes from the receiver node.
	 */
	public void removeChildren()
	{
		children.clear();
	}

	/**
	 * Adds a child at the given index.
	 * child will be messaged with setParent.
	 *
	 * @param child The {@link DefaultTreeTableNode} child node to be added
	 * @param index The index where the child is to be added
	 */
	public void insertChild(DefaultTreeTableNode child, int index)
	{
		if (children == null)
		{
			addChild(child);
			return;
		}

		child.setParent(this);
		children.add(index, child);
	}

	/**
	 * Set the parent node for the receiver node.
	 *
	 * @param parent The parent {@link DefaultTreeTableNode}
	 */
	public void setParent(DefaultTreeTableNode parent)
	{
		this.parent = parent;
	}

	//////////////////////////////////////////////////
	// @@ Column access methods
	//////////////////////////////////////////////////

	/**
	 * Adds a column object to this node.
	 * Use this mehtod to add a value to the node.
	 *
	 * @param columnValue The column object to be added
	 */
	public void addColumn(Object columnValue)
	{
		if (columnObjects == null)
			columnObjects = new Vector();

		columnObjects.add(columnValue);
	}

	/**
	 * Removes a column object from this node.
	 *
	 * @param columnValue The column object to be removed
	 * @return True if the object was successfully removed.
	 *             False if object was NOT successfully removed.
	 */
	public boolean removeColumn(Object columnValue)
	{
		int objectPos = columnObjects.indexOf(columnValue);

		if (objectPos > 0)
		{
			columnObjects.set(objectPos, EMPTY_COLUMN);
			return true;
		}

		return false;
	}

	/**
	 * Sets the object contained by column with specified index
	 * with the specified object.
	 * Overwrites the value in the node. ATTENTION: The value must be set
	 * before, the column has to be added before by calling the {@link #addColumn} method.
	 *
	 * @param index The column index
	 * @param columnValue The new object to be set for the specified column index
	 */
	public void setColumnValue(int index, Object columnValue)
	{
		columnObjects.set(index, columnValue);
	}

	/**
	 * Returns the column value for a specified index for the
	 * receiver node.
	 *
	 * @param index The column index
	 * @return The object contained in that column for this node. If no objext is available, then
	 * a null is returned.
	 */
	public Object getColumnValue(int index)
	{
		try
		{
			if (index == 0)
				return this;

			if (columnObjects == null)
				return null;

			return columnObjects.get(index);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			return null;
		}
	}

	/**
	 * Returns the number of column objects contained in the receiver node.
	 *
	 * @return The number of column objects
	 */
	public int getColumnCount()
	{
		return columnObjects == null ? 0 : columnObjects.size();
	}

	/**
	 * Returns the lastHeight used by the {@link JTreeTable}
	 *
	 * @return The last height used
	 */
	public int getLastHeight()
	{
		return lastHeight;
	}

	/**
	 * Sets the lastHeight by the {@link JTreeTable} This method should never be called
	 * directly and is actually only meant for internal use by the {@link JTreeTable}
	 *
	 * @param lastHeight The lastHeight to set
	 */
	public void setLastHeight(int lastHeight)
	{
		this.lastHeight = lastHeight;
	}
}
