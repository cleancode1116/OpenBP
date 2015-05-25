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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.ReflectUtil;
import org.openbp.common.generic.description.DescriptionObject;
import org.openbp.common.generic.propertybrowser.CollectionDescriptor;
import org.openbp.common.generic.propertybrowser.PropertyDescriptor;
import org.openbp.common.property.PropertyAccessUtil;
import org.openbp.common.property.PropertyException;
import org.openbp.common.string.StringUtil;
import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.common.util.ToStringHelper;
import org.openbp.jaspira.propertybrowser.NodeStructureMgr;
import org.openbp.jaspira.propertybrowser.PropertyBrowserModel;
import org.openbp.swing.components.treetable.JTreeTable;

/**
 * Property browser tree node that implements the parent node of a collection member
 * of the object.
 *
 * @author Erich Lauterbach
 */
public class CollectionNode extends AbstractNode
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** {@link PropertyDescriptor} that contains the {@link CollectionDescriptor} for this node. */
	private PropertyDescriptor propertyDescriptor;

	/** List containing the objects. */
	private List list;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default Constructor
	 *
	 * @param propertyDescriptor The {@link PropertyDescriptor} that contains the {@link CollectionDescriptor}
	 */
	public CollectionNode(PropertyDescriptor propertyDescriptor)
	{
		this.propertyDescriptor = propertyDescriptor;
	}

	/**
	 * Returns a string representation of this object.
	 * @nowarn
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "propertyDescriptor.name");
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

		if (children != null)
			children.clear();

		list = null;

		super.copyFrom(source, copyMode);
		CollectionNode src = (CollectionNode) source;

		propertyDescriptor = src.propertyDescriptor;
	}

	/**
	 * Reloads the content of this node.
	 */
	public void reload()
	{
		// Assign object and property browser references
		super.reload();

		populateChildren();

		if (propertyBrowser != null)
		{
			propertyBrowser.saveCurrentPosition();

			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					if (propertyBrowser != null)
					{
						PropertyBrowserModel model = (PropertyBrowserModel) propertyBrowser.getModel();
						if (model != null)
						{
							model.fireNodeChanged(CollectionNode.this);
							SwingUtilities.invokeLater(new Runnable()
							{
								public void run()
								{
									if (propertyBrowser != null)
									{
										propertyBrowser.restoreCurrentPosition();
									}
								}
							});
						}
					}
				}
			});
		}
	}

	/**
	 * Checks if this node represents the given property.
	 *
	 * @param propertyName Name of the property to check
	 * @nowarn
	 */
	public boolean representsProperty(String propertyName)
	{
		return propertyDescriptor != null && propertyDescriptor.getName().equals(propertyName);
	}

	/**
	 * Gets the {@link PropertyDescriptor} that contains the {@link CollectionDescriptor} for this node..
	 * @nowarn
	 */
	public PropertyDescriptor getPropertyDescriptor()
	{
		return propertyDescriptor;
	}

	/**
	 * Gets the {@link CollectionDescriptor} associated with this node..
	 * @nowarn
	 */
	public CollectionDescriptor getCollectionDescriptor()
	{
		return propertyDescriptor != null ? propertyDescriptor.getCollectionDescriptor() : null;
	}

	//////////////////////////////////////////////////
	// @@ TreeTableNode overrides
	//////////////////////////////////////////////////

	/**
	 * @copy TreeTableNode.getColumnValue(int)
	 */
	public Object getColumnValue(int columnIndex)
	{
		switch (columnIndex)
		{
		case 0:
			return propertyDescriptor;

		case 1:
			// Return indicator for list size
			if (children == null || list == null || children.size() != list.size())
			{
				populateChildren();
			}

			if (children != null && children.size() != 0)
				return "[" + children.size() + "]";
			return "[ ]";

		case 2:
			return propertyDescriptor != null ? JTreeTable.createDescriptionCellValue(propertyDescriptor.getDescription()) : null;

		default:
			return null;
		}
	}

	/**
	 * @copy TreeTableNode.getNodeText()
	 */
	public String getNodeText()
	{
		return propertyDescriptor != null ? propertyDescriptor.getDisplayName() : null;
	}

	public boolean getAllowsChildren()
	{
		return true;
	}

	public TreeNode getChildAt(int childIndex)
	{
		if (children == null || list == null || children.size() != list.size())
		{
			populateChildren();
		}

		return super.getChildAt(childIndex);
	}

	public int getChildCount()
	{
		int size = list != null ? list.size() : 0;

		return size;
	}

	public boolean isLeaf()
	{
		return false;
	}

	//////////////////////////////////////////////////
	// @@ AbstractNode overrides
	//////////////////////////////////////////////////

	/**
	 * Checks if the node node should be expanded on initial display.
	 *
	 * @return
	 *		true	If the node should be expanded<br>
	 *		false	If the node should be collapsed
	 */
	public boolean shouldExpand()
	{
		return propertyDescriptor.isExpanded();
	}

	/**
	 * Sets the object containing the values for the property.
	 *
	 * @param object The object to be set
	 */
	public void setObject(Object object)
	{
		this.object = object;

		// Get the list of objects to be manipulated.
		try
		{
			if (object != null)
			{
				list = (List) PropertyAccessUtil.getProperty(object, propertyDescriptor.getName());
			}
			else
			{
				list = null;
			}
		}
		catch (PropertyException e)
		{
			ExceptionUtil.printTrace(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ Node addition/removal
	//////////////////////////////////////////////////

	/**
	 * Adds a new node of the same type as the exisiting child nodes and inserts it into the
	 * child list at the given position.
	 *
	 * @param posNode Node that determines the position of the new node in the collection<br>
	 * If this is null or the collection node itself, the new node will be added at the end of the list.
	 * Otherwise, if this node is a member of the collection, the new node will be inserted after the given node.
	 * @param instance Object instance to be added or null to create a new instance
	 * @return The new node or null if no node may/could be added
	 */
	public AbstractNode addNewNodeAfter(AbstractNode posNode, Object instance)
	{
		if (instance == null)
		{
			instance = createNewMember();
			if (instance == null)
				return null;
		}
		else
		{
			if (instance instanceof DescriptionObject)
			{
				DescriptionObject d = (DescriptionObject) instance;
				String name = NamedObjectCollectionUtil.createUniqueId(list, d.getName());
				d.setName(name);
			}
		}

		int index = getChildIndex(posNode);
		if (index < 0)
		{
			index = getChildCount();
		}
		else
		{
			++index;
		}

		if (list == null)
			list = new ArrayList();
		list.add(index, instance);

		ObjectNode node = NodeStructureMgr.getInstance().createEditorStructureFor(instance.getClass());

		node.setObject(instance);
		node.setPropertyBrowser(propertyBrowser);

		add(index, node);

		saveList();

		((PropertyBrowserModel) propertyBrowser.getModel()).fireNodeInserted(node);

		return node;
	}

	/**
	 * Removes a sepcified node from the collection.
	 *
	 * @param node The node to be removed
	 * @return The node that should become the current node after removal of this node<br>
	 * This is the node that succeeds the current node in the collection or - if not present -
	 * the node that preceeds the current node or - if the list is empty - the collection node itself
	 * (or null if not removal has taken place).
	 */
	public AbstractNode removeNode(AbstractNode node)
	{
		int index = getChildIndex(node);
		if (index < 0)
		{
			return null;
		}

		if (list != null)
		{
			list.remove(node.getObject());
			saveList();
		}

		if (children != null)
		{
			children.remove(node);
			((PropertyBrowserModel) propertyBrowser.getModel()).fireNodeRemoved(this);
		}

		int n = getChildCount();
		if (index >= n)
		{
			// No successor, try predecessor
			if (n > 0)
			{
				// The predecessor is the last element in the list
				index = n - 1;
			}
			else
			{
				// Empty list
				index = -1;
			}
		}

		if (index >= 0)
		{
			// The new current node
			return (AbstractNode) getChildAt(index);
		}

		// Empty list, make the collection node the current node
		return this;
	}

	/**
	 * Removes all nodes from the collection.
	 */
	public void removeAllNodes()
	{
		if (list != null)
		{
			list = null;
			saveList();
		}

		if (children != null)
		{
			children.clear();
			((PropertyBrowserModel) propertyBrowser.getModel()).fireNodeRemoved(this);
		}
	}

	/**
	 * Moves the given node one position up in the child node list.
	 *
	 * @param node Node to move
	 */
	public void moveNodeUp(AbstractNode node)
	{
		int index = getChildIndex(node);
		if (index < 1)
			return;

		list.remove(node.getObject());
		children.remove(node);

		--index;
		list.add(index, node.getObject());
		add(index, node);

		afterMove(node);
	}

	/**
	 * Moves the given node one position down in the child node list.
	 *
	 * @param node Node to move
	 */
	public void moveNodeDown(AbstractNode node)
	{
		int index = getChildIndex(node);
		if (index < 0 || index >= getChildCount() - 1)
			return;

		list.remove(node.getObject());
		children.remove(node);

		++index;
		list.add(index, node.getObject());
		add(index, node);

		afterMove(node);
	}

	/**
	 * Performs post processing after a move operation.
	 *
	 * @param node Node to move
	 */
	private void afterMove(AbstractNode node)
	{
		saveList();

		((PropertyBrowserModel) propertyBrowser.getModel()).structureChanged((AbstractNode) node.getParent());

		propertyBrowser.selectNode(node);
	}

	/**
	 * Defines if a receiver node may be dragged into another position.
	 *
	 * @return true if the node may be dragged<br>
	 *			false otherwise.
	 */
	public boolean allowedToDrag()
	{
		return false;
	}

	/**
	 * Determines if node addition is allowed.
	 *
	 * @return true if node addition is allowed<br>
	 *			false if no node addition is allowed.
	 */
	public boolean allowsNodeAddition()
	{
		return getCollectionDescriptor().isAddEnabled();
	}

	/**
	 * Determinces if a node removal is allowed.
	 *
	 * @return true if nodes may be removed.<br>
	 *			false is nodes may not be removed.
	 */
	public boolean allowsNodeRemoval()
	{
		return getCollectionDescriptor().isDeleteEnabled();
	}

	/**
	 * Determinces if a node reordering is allowed.
	 *
	 * @return true if nodes may be reordered.<br>
	 *			false is nodes may not be reordered.
	 */
	public boolean allowsNodeReordering()
	{
		return getCollectionDescriptor().isReorderEnabled();
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Populates the children vector with {@link ObjectNode} 's, from the objects
	 * contained in the list.
	 */
	protected void populateChildren()
	{
		if (children != null)
			children.clear();

		if (list != null)
		{
			for (int i = 0; i < list.size(); i++)
			{
				Object listObject = list.get(i);
				ObjectNode node = NodeStructureMgr.getInstance().createEditorStructureFor(listObject.getClass());

				node.setObject(listObject);
				node.setPropertyBrowser(propertyBrowser);

				add(node);
			}
		}
	}

	/**
	 * Gets the child index of the given node.
	 *
	 * @param node Node to search
	 * @return The child index or -1 if not found
	 */
	public int getChildIndex(AbstractNode node)
	{
		if (node != null && node != this)
		{
			int n = getChildCount();
			for (int i = 0; i < n; ++i)
			{
				if (getChildAt(i) == node)
				{
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Saves the list with the modified objects back into the object.
	 */
	protected void saveList()
	{
		// Saving the list back into the object.
		try
		{
			if (list != null && list.size() == 0)
				list = null;

			PropertyAccessUtil.setProperty(object, propertyDescriptor.getName(), list);

			propertyBrowser.fireObjectModified();
		}
		catch (PropertyException e)
		{
			ExceptionUtil.printTrace(e);
		}
	}

	/**
	 * Creates a new member node to be added to the list.
	 * @return The new member instance or null if the cretion failed
	 */
	protected Object createNewMember()
	{
		Object o = null;

		try
		{
			// Search for a 'createPropertyElement' method
			String methodName = "create" + propertyDescriptor.getName() + "Element";
			Method method = object.getClass().getMethod(methodName, (Class[]) null);
			if (method != null)
			{
				try
				{
					o = method.invoke(object, (Object[]) null);
				}
				catch (InvocationTargetException e)
				{
					// Something went wrong, warn and exit
					ExceptionUtil.printTrace(e);
					return null;
				}
				catch (IllegalAccessException e)
				{
					// Something went wrong, warn and exit
					ExceptionUtil.printTrace(e);
					return null;
				}

				// Also return if o is null, in this case the method obviously intentionally returned null
				return o;
			}
		}
		catch (NoSuchMethodException e)
		{
			// Doesn't matter if we can't find a create method
		}

		// Try to instantiate a new object using the default constructor
		CollectionDescriptor cd = getCollectionDescriptor();
		Class elementClass = cd.getSafeTypeClass();

		if (elementClass == null && list != null && list.size() > 0)
		{
			// If we don't have class info, try to guess from the list contents
			Object element = list.get(0);
			if (element != null)
				elementClass = element.getClass();
		}

		if (elementClass != null)
		{
			try
			{
				o = ReflectUtil.instantiate(elementClass, null, "collection element");
			}
			catch (Exception e)
			{
				// Something went wrong, warn and exit
				ExceptionUtil.printTrace(e);
				return null;
			}
		}

		if (o != null)
		{
			try
			{
				// Try to set name and display name for description/display objects
				String text = StringUtil.capitalize(propertyDescriptor.getName());
				if (text.endsWith("List"))
					text = text.substring(0, text.length() - 4);
				text = "New" + text;
				PropertyAccessUtil.setProperty(o, "name", text);
			}
			catch (Exception e)
			{
				// Ignore if not possible
			}
		}

		return o;
	}
}
