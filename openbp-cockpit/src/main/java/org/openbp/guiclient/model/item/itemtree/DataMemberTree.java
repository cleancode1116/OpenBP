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

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.type.ComplexTypeItem;
import org.openbp.core.model.item.type.DataMember;

/**
 * Data member tree.
 * Item tree suitable to display hierarchies of data members.
 * The root node is always a {@link ComplexTypeItem}
 * The {@link #getSelectedObjects} and {@link ItemTree#setSelectedObjects} methods accept
 * path specifications of member and sub members of this type.
 *
 * @author Heiko Erhardt
 */
public class DataMemberTree extends ItemTree
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public DataMemberTree()
	{
		super();

		setSelectionMode(SELECTION_SINGLE);
		setRootVisible(true);
		setShowGroups(false);

		setSupportedItemTypes(new String [] { ItemTypes.TYPE });
		setSupportedObjectClasses(new Class [] { DataMember.class });

		setSelectableItemTypes(null);
		setSelectableObjectClasses(new Class [] { DataMember.class });
	}

	//////////////////////////////////////////////////
	// @@ Overrides
	//////////////////////////////////////////////////

	/**
	 * Gets the currently selected objects.
	 *
	 * @return A list of strings .<br>
	 * The objects in the list are either items of one of the types specified in
	 * the list set by the {@link ItemTree#setSelectableItemTypes} method or objects of
	 * the model object classes set by the {@link ItemTree#setSelectableObjectClasses} method.<br>
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
					String memberPath = determineNodePath(node);
					if (list == null)
						list = new ArrayList();
					list.add(memberPath);
				}
			}
		}

		return list;
	}

	/**
	 * Finds a tree node by the object it refers to, starting
	 * at the given tree node.
	 *
	 * @param object Object to search. This is a string specifying a member path as described in the class comment.
	 * @param node Node to start the search from
	 * @return The tree node or null if no such object exists in the tree
	 */
	public ItemTreeNode findNodeByObject(Object object, ItemTreeNode node)
	{
		String nodePath = determineNodePath(node);
		if (nodePath != null && nodePath.equals(object))
			return node;

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
	 * Determines the data member path to the specified node.
	 *
	 * @param node The node
	 * @return The path, not containing the name of the top level complex type item node (e. g. "Buyer.Name")
	 * or null if the node is the root node itself
	 */
	protected String determineNodePath(ItemTreeNode node)
	{
		ItemTreeNode rootNode = (ItemTreeNode) ((DefaultTreeModel) getModel()).getRoot();

		StringBuffer sb = new StringBuffer();

		while (node != rootNode)
		{
			if (node instanceof ModelObjectNode)
			{
				ModelObject object = ((ModelObjectNode) node).getModelObject();
				String name = object.getName();

				if (sb.length() == 0)
				{
					sb.append(name);
				}
				else
				{
					sb.insert(0, ModelQualifier.OBJECT_DELIMITER);
					sb.insert(0, name);
				}
			}

			node = (ItemTreeNode) node.getParent();
		}

		if (sb.length() == 0)
			return null;
		return sb.toString();
	}
}
