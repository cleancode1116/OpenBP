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
package org.openbp.jaspira.propertybrowser.nodes;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.tree.TreeNode;

import org.openbp.common.CollectionUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.util.iterator.EmptyEnumeration;
import org.openbp.jaspira.propertybrowser.PropertyBrowser;
import org.openbp.jaspira.propertybrowser.PropertyBrowserImpl;
import org.openbp.jaspira.propertybrowser.PropertyBrowserModel;
import org.openbp.swing.components.treetable.JTreeTable;
import org.openbp.swing.components.treetable.TreeTableNode;

/**
 * Base class for all nodes of the property browser tree.
 *
 * @author Erich Lauterbach
 */
public abstract class AbstractNode
	implements TreeTableNode, Cloneable, Copyable
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Vector containing all the sibling nodes. */
	protected Vector children;

	/** Last height used by this node. */
	private int lastHeight;

	/** The parent node to this node. */
	protected AbstractNode parentNode;

	/** The object for which this node will display a property. */
	protected Object object;

	/** Property browser */
	protected PropertyBrowserImpl propertyBrowser;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default Constructor
	 */
	public AbstractNode()
	{
	}

	/**
	 * Clones this node and all its child nodes, thus performing a
	 * deep clone.
	 */
	public Object clone()
		throws CloneNotSupportedException
	{
		AbstractNode clone = (AbstractNode) super.clone();

		// Perform a deep copy
		clone.copyFrom(this, Copyable.COPY_DEEP);

		return clone;
	}

	/**
	 * Copies the values of the source object to this object.
	 *
	 * @param source The source object. Must be of the same type as this object.
	 * @param copyMode Determines if a deep copy, a first level copy or a shallow copy is to be
	 * performed. See the constants of the org.openbp.common.generic.description.Copyable class.
	 * @throws CloneNotSupportedException If the cloning of one of the contained objects failed
	 */
	public void copyFrom(Object source, int copyMode)
		throws CloneNotSupportedException
	{
		if (source == this)
			return;

		AbstractNode src = (AbstractNode) source;

		children = null;
		lastHeight = 0;
		parentNode = null;
		object = null;
		propertyBrowser = null;

		if (src.children != null)
		{
			Enumeration originalChildren = src.children();
			while (originalChildren.hasMoreElements())
			{
				AbstractNode element = (AbstractNode) originalChildren.nextElement();
				this.add((AbstractNode) element.clone());
			}
		}
		else
		{
			children = null;
		}
	}

	/**
	 * Reloads the content of this node.
	 */
	public void reload()
	{
		setPropertyBrowser(propertyBrowser);
		setObject(object);
	}

	/**
	 * Checks if this node represents the given property.
	 *
	 * @param propertyName Name of the property to check
	 * @nowarn
	 */
	public boolean representsProperty(String propertyName)
	{
		return false;
	}

	//////////////////////////////////////////////////
	// @@ Miscelleanous
	//////////////////////////////////////////////////

	/**
	 * Removes this node from the structure.
	 * Also removes the parent node if empty.
	 */
	public void remove()
	{
		AbstractNode parent = (AbstractNode) getParent();
		if (parent != null && parent.children != null)
		{
			parent.children.remove(this);

			if (parent.children.size() > 0)
			{
				// There are still children left.
				if (propertyBrowser != null)
				{
					// Notify the oe model that a node has been removed
					((PropertyBrowserModel) propertyBrowser.getModel()).fireNodeRemoved(parent);
				}
			}
			else
			{
				// No children left, remove the parent node also.
				parent.remove();
			}
		}
	}

	/**
	 * Finds the specified node.
	 * The method will perform a recursive search, starting at this node.
	 *
	 * @param propertyName This may specifiy:<br>
	 * - The property name of a {@link PropertyNode}<br>
	 * - The associated property name of a {@link CollectionNode}<br>
	 * - The associated property name of a {@link ObjectNode}
	 * @return The node or null if no such property has been found
	 */
	public AbstractNode findNodeForProperty(String propertyName)
	{
		if (representsProperty(propertyName))
			return this;

		int n = getChildCount();
		for (int i = 0; i < n; ++i)
		{
			AbstractNode childNode = (AbstractNode) getChildAt(i);
			AbstractNode result = childNode.findNodeForProperty(propertyName);
			if (result != null)
				return result;
		}

		return null;
	}

	/**
	 * Forces a reload of a particular property from the modified object and redisplays the specified property.
	 * This method can be used by validators to redisplay properties they have changed.
	 *
	 * @param propertyName Property name
	 */
	public void reloadProperty(String propertyName)
	{
		// Delegate reloading to the object descriptor node
		// that is the direct or indirect parent of this node

		ObjectNode on = getObjectNode();
		if (on != null)
		{
			AbstractNode propertyNode = on.findNodeForProperty(propertyName);
			if (propertyNode != null)
			{
				propertyNode.reload();
			}
		}
	}

	/**
	 * Filters out (removes) all property nodes from the property nodes below this node
	 * that do not match one of the specified properties.
	 * This method can be used to reduce the number of properties of an object that are displayed
	 * if there are many object properties.
	 *
	 * @param visibleMembers Array of allowed property names
	 */
	public void filterPropertyNodes(String [] visibleMembers)
	{
		if (visibleMembers == null)
			return;

		// Remove all properties that do not appear in the 'visibleMembers' table
		for (Iterator it = getPropertyNodes(); it.hasNext();)
		{
			AbstractNode node = (AbstractNode) it.next();

			boolean found = false;
			for (int i = 0; i < visibleMembers.length; ++i)
			{
				if (node.representsProperty(visibleMembers [i]))
				{
					found = true;
					break;
				}
			}

			if (!found)
			{
				// Not present in visible members, remove the node and its parent if empty
				node.remove();
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ TreeTableNode Implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the height used last by the {@link JTreeTable} for this node.
	 *
	 * @return The last height used
	 */
	public int getLastHeight()
	{
		return lastHeight;
	}

	/**
	 * Sets the current height being used by the {@link JTreeTable} for later use.
	 *
	 * @param height The height being used
	 */
	public void setLastHeight(int height)
	{
		this.lastHeight = height;
	}

	/**
	 * Gets the preferred height of the node in the tree.
	 *
	 * @return The height as an int in pixels
	 */
	public Dimension getPreferredSize()
	{
		return new Dimension(0, 0);
	}

	//////////////////////////////////////////////////
	// @@ TreeNode implementation
	//////////////////////////////////////////////////

	/**
	 * Returns true if the receiver is a leaf.
	 * @nowarn
	 */
	public boolean isLeaf()
	{
		if (children == null)
			return true;

		return (children.size() > 0) ? false : true;
	}

	/**
	 * Returns the child TreeNode at index
	 * childIndex.
	 * @nowarn
	 */
	public TreeNode getChildAt(int childIndex)
	{
		return (children == null) ? null : (TreeNode) children.get(childIndex);
	}

	/**
	 * Returns the number of children TreeNodes the receiver contains.
	 * @nowarn
	 */
	public int getChildCount()
	{
		return (children == null) ? -1 : children.size();
	}

	/**
	 * Returns the parent TreeNode of the receiver.
	 * @nowarn
	 */
	public TreeNode getParent()
	{
		return parentNode;
	}

	/**
	 * Returns the index of node in the receivers children.
	 * If the receiver does not contain node, -1 will be
	 * returned.
	 * @nowarn
	 */
	public int getIndex(TreeNode node)
	{
		if (children != null)
		{
			for (int i = 0; i < children.size(); i++)
			{
				if (children.get(i).equals(node))
					return i;
			}
		}

		return -1;
	}

	/**
	 * Returns true if the receiver allows children.
	 * @nowarn
	 */
	public boolean getAllowsChildren()
	{
		return true;
	}

	/**
	 * Returns the children of the receiver as an Enumeration.
	 * @nowarn
	 */
	public Enumeration children()
	{
		return (children == null) ? EmptyEnumeration.getInstance() : children.elements();
	}

	/**
	 * Creates an iteration over all property nodes below this node.
	 *
	 * @return An iterator of {@link PropertyNode} and {@link CollectionNode} objects<br>
	 * Note that the iterator does not contain the properties of subordinate {@link ObjectNode} elements.
	 */
	public Iterator getPropertyNodes()
	{
		ArrayList list = new ArrayList();

		for (int i = 0; i < children.size(); i++)
		{
			AbstractNode node = (AbstractNode) children.get(i);

			if (node instanceof GroupNode)
			{
				// Add sub properties of group node
				Iterator groupPropertyNodes = node.getPropertyNodes();
				CollectionUtil.addAll(list, groupPropertyNodes);
			}
			else if (node instanceof PropertyNode || node instanceof CollectionNode || (node instanceof ObjectNode && ((ObjectNode) node).getPropertyDescriptor() != null))
			{
				// Property-related, add to list
				list.add(node);
			}
		}

		return list.iterator();
	}

	//////////////////////////////////////////////////
	// @@ Children accessors
	//////////////////////////////////////////////////

	/**
	 * Adds a new AbstractNode to the receiver node. This method is used internaly and does
	 * not determine if the node is allowed to be added. It also force the receriver node
	 * to be set as the parent node of the node that is being added.
	 *
	 * @param node The node to be added
	 */
	protected void add(AbstractNode node)
	{
		if (children == null)
			children = new Vector();

		children.add(node);
		node.setParent(this);
	}

	/**
	 * Adds a new AbstractNode to the receiver node. This method is used internaly and does
	 * not determine if the node is allowed to be added. It also force the receriver node
	 * to be set as the parent node of the node that is being added.
	 *
	 * @param node The node to be added
	 * @param index The index where the node is to be added
	 */
	protected void add(int index, AbstractNode node)
	{
		if (children == null)
			children = new Vector();

		children.add(index, node);
		node.setParent(this);
	}

	//////////////////////////////////////////////////
	// @@ Node Accessors
	//////////////////////////////////////////////////

	/**
	 * Sets the parent tree node of this node.
	 *
	 * @param node The new parent
	 */
	public void setParent(AbstractNode node)
	{
		this.parentNode = node;
	}

	/**
	 * Checks if the node node should be expanded on initial display.
	 *
	 * @return
	 *		true	If the node should be expanded<br>
	 *		false	If the node should be collapsed
	 */
	public boolean shouldExpand()
	{
		if (isLeaf())
			return false;
		if (children != null)
		{
			int n = children.size();
			for (int i = 0; i < n; ++i)
			{
				AbstractNode child = (AbstractNode) children.get(i);
				if (child.shouldExpand())
					return true;
			}
		}
		return false;
	}

	/**
	 * Gets the parent object descriptor node of this node.
	 *
	 * @return The node or null
	 */
	public ObjectNode getObjectNode()
	{
		for (TreeNode node = this; node != null; node = node.getParent())
		{
			if (node instanceof ObjectNode)
			{
				return (ObjectNode) node;
			}
		}

		return null;
	}

	/**
	 * Gets the collection descriptor node that is directly associated with this node.
	 * If this node is a collection descriptor node itself, it is returned.
	 * Otherwise, if the parent of this node is one, it will be returned.
	 *
	 * @return The collection descriptor node or null if this node is not directly
	 * associated with a collection.
	 */
	public CollectionNode getAssociatedCollectionNode()
	{
		for (TreeNode node = this; node != null; node = node.getParent())
		{
			if (node instanceof CollectionNode)
			{
				return (CollectionNode) node;
			}
		}

		return null;
	}

	//////////////////////////////////////////////////
	// @@ Object accessors
	//////////////////////////////////////////////////

	/**
	 * Sets the object containing the values for the porperty.
	 *
	 * @param object The object to be set
	 */
	public void setObject(Object object)
	{
		this.object = object;
		if (children != null)
		{
			for (int i = 0; i < children.size(); i++)
			{
				((AbstractNode) children.get(i)).setObject(object);
			}
		}
	}

	/**
	 * Gets the object containing the values for the property.
	 *
	 * @return The object
	 */
	public Object getObject()
	{
		return object;
	}

	/**
	 * Sets the property browser that owns the node.
	 *
	 * @param propertyBrowser The object to be set
	 */
	public void setPropertyBrowser(PropertyBrowserImpl propertyBrowser)
	{
		this.propertyBrowser = propertyBrowser;
		if (children != null)
		{
			for (int i = 0; i < children.size(); i++)
			{
				((AbstractNode) children.get(i)).setPropertyBrowser(propertyBrowser);
			}
		}
	}

	/**
	 * Gets the property browser that owns the node.
	 * @nowarn
	 */
	public PropertyBrowser getPropertyBrowser()
	{
		return propertyBrowser;
	}
}
