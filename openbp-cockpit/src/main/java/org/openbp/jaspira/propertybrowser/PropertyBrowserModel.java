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
package org.openbp.jaspira.propertybrowser;

import java.util.Enumeration;

import javax.swing.tree.TreePath;

import org.openbp.common.rc.ResourceCollection;
import org.openbp.jaspira.propertybrowser.editor.PropertyEditor;
import org.openbp.jaspira.propertybrowser.nodes.AbstractNode;
import org.openbp.jaspira.propertybrowser.nodes.ObjectNode;
import org.openbp.swing.components.treetable.SimpleTreeTableModel;
import org.openbp.swing.components.treetable.TreeTableModel;

/**
 * The property browser is used for displayment and modifcation of object.
 * The object is always expected to have properties with its corresponding
 * values, which are displayed in appropriate property editors. The property browser consits of
 * a TreeTable combination. This model is used by the JTreeTable to build up its structure.
 *
 * The model class also serves as property change listener for the tree nodes and will forward
 * any property changes to the fireObjectModified event of the property browser itself.
 *
 * @author Andreas Putz
 */
public class PropertyBrowserModel extends SimpleTreeTableModel
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Column headers */
	private static String columnHeaders[];

	//////////////////////////////////////////////////
	// @@ Property browser members
	//////////////////////////////////////////////////

	/** Property browser */
	private PropertyBrowserImpl propertyBrowser;

	/** Array of property names that should be displayed or null for all */
	private String[] visibleMembers;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default Constructor.
	 */
	public PropertyBrowserModel()
	{
		super(null);

		// Load the all the resources for the property browser.
		columnHeaders = new String[2];
	}

	/**
	 * Default Constructor.
	 *
	 * @param resourceCollection Used for column headers
	 */
	public PropertyBrowserModel(final ResourceCollection resourceCollection)
	{
		this();
		setResourceCollection(resourceCollection);
	}

	/**
	 * Sets the property browser resource.
	 *
	 * @param resourceCollection Resource used for column headers
	 */
	public void setResourceCollection(final ResourceCollection resourceCollection)
	{
		if (resourceCollection != null)
		{
			columnHeaders[0] = resourceCollection
				.getOptionalString("propertybrowser.header.columnproperty", "Property");
			columnHeaders[1] = resourceCollection.getOptionalString("propertybrowser.header.columnvalue", "Value");
			// columnHeaders[2] = resourceCollection.getOptionalString ("propertybrowser.header.columndescription", "Description");
		}
	}

	/**
	 * Gets the array of property names that should be displayed or null for all.
	 * This can be used to limit the number of properties that are displayed for complex objects.
	 * @nowarn
	 */
	public String[] getVisibleMembers()
	{
		return visibleMembers;
	}

	/**
	 * Sets the array of property names that should be displayed or null for all.
	 * This can be used to limit the number of properties that are displayed for complex objects.
	 * @nowarn
	 */
	public void setVisibleMembers(final String[] visibleMembers)
	{
		this.visibleMembers = visibleMembers;
	}

	//////////////////////////////////////////////////
	// @@ Object access
	//////////////////////////////////////////////////

	/**
	 * Sets the object to edit.
	 *
	 * The model will create an appropriate property browser tree based on the class of the object.
	 * @param object The object to set
	 */
	public void setObject(final Object object)
	{
		setObject(object, null);
	}

	/**
	 * Sets the object to edit.
	 *
	 * @param object The object to set
	 * @param rootNode Root node of the property browser tree or null if the model
	 * should create an appropriate property browser tree based on the class of the object
	 */
	public void setObject(final Object object, ObjectNode rootNode)
	{
		ObjectNode oldRoot = (ObjectNode) getRoot();
		if (oldRoot != null)
		{
			oldRoot.setPropertyBrowser(null);
			oldRoot.setObject(null);
		}

		if (object != null)
		{
			if (rootNode == null)
			{
				rootNode = NodeStructureMgr.getInstance().createEditorStructureFor(object.getClass());
			}

			if (rootNode != null)
			{
				// Apply property node filter if defined
				rootNode.filterPropertyNodes(visibleMembers);
				rootNode.setPropertyBrowser(propertyBrowser);
				rootNode.setObject(object);
			}

			setRoot(rootNode);

			if (rootNode != null)
			{
				performDefaultExpansion(rootNode);
			}
		}
		else
		{
			setRoot(null);
		}
	}

	/**
	 * Gets the property browser.
	 * @nowarn
	 */
	public PropertyBrowser getPropertyBrowser()
	{
		return propertyBrowser;
	}

	/**
	 * Sets the property browser.
	 * @nowarn
	 */
	public void setPropertyBrowser(final PropertyBrowserImpl propertyBrowser)
	{
		this.propertyBrowser = propertyBrowser;
	}

	//////////////////////////////////////////////////
	// @@ Tree table model implementation
	//////////////////////////////////////////////////

	/**
	 * By default, make the column with the Tree in it the only editable one.
	 * Making this column editable causes the JTable to forward mouse
	 * and keyboard events in the Tree column to the underlying JTree.
	 */
	public boolean isCellEditable(final int row, final int column)
	{
		if (getColumnClass(column) == TreeTableModel.class)
			return true;

		if (getColumnClass(column) == PropertyEditor.class)
		{
			// The editor component itself will care for read-only mode, so we assume all cells containing editors to be editable.
			/*
			PropertyDescriptor pd = null;

			Object o = getValueAt(row, 0);

			if (o instanceof PropertyDescriptor)
				pd = (PropertyDescriptor) o;
			else if (o instanceof PropertyNode)
				pd = ((PropertyNode) o).getPropertyDescriptor();
			if (pd != null)
				return ! pd.isReadOnly();
			return false;
			 */
			return true;
		}

		return false;
	}

	/** Used by the JTreeTable if a specified cell is seletable.
	 *
	 * @param row The row index of the specified cell;
	 * @param column The column index of the specified cell;
	 *
	 * @return true - if the cell is selectable<br>
	 *		   false - if the cell isn't selectable.
	 */
	public boolean isCellSelectable(final int row, final int column)
	{
		return isCellEditable(row, column);
	}

	/**
	 * Returns the number of available columns.
	 */
	public int getColumnCount()
	{
		return columnHeaders.length;
	}

	/**
	 * Returns the name for column number <code>column</code>.
	 */
	public String getColumnName(final int column)
	{
		return columnHeaders[column];
	}

	/**
	 * Returns the class type for column number <code>column</code>.
	 */
	public Class getColumnClass(final int column)
	{
		switch (column)
		{
		case 0:
			return TreeTableModel.class;

		case 1:
			return PropertyEditor.class;

		default:
			return Object.class;
		}
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Performs the default expansion.
	 * Expands all nodes that should be initially visible.
	 *
	 * @param node Parent node to start the default expansion with.
	 * The method will go down recursively the node tree.
	 */
	protected void performDefaultExpansion(final AbstractNode node)
	{
		// Expand the current node if we should
		if (node.shouldExpand())
		{
			// Check if the parent is expanded
			if (node.getParent() != null)
			{
				TreePath parentPath = propertyBrowser.getPathByNode(node.getParent());
				if (! propertyBrowser.getTree().isExpanded(parentPath))
					// Parent path not expanded, return
					return;
			}

			TreePath path = propertyBrowser.getPathByNode(node);
			propertyBrowser.expandPath(path);
		}

		// Expansion must be done botton-up, so process the children first
		if (node.getChildCount() >= 0)
		{
			for (Enumeration e = node.children(); e.hasMoreElements();)
			{
				AbstractNode n = (AbstractNode) e.nextElement();
				performDefaultExpansion(n);
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ StructureChangeListener implementation
	//////////////////////////////////////////////////

	/**
	 * Called when the tree structure changed
	 * @nowarn
	 */
	public void structureChanged(final AbstractNode node)
	{
		fireNodeStructureChanged(node);
	}
}
