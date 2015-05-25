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
package org.openbp.cockpit.plugins.finder.treemodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultTreeModel;

/**
 * Generic model of the tree.
 *
 * @author Baumgartner Michael
 */
public class GenericModel extends DefaultTreeModel
{
	//////////////////////////////////////////////////
	// @@ Member
	//////////////////////////////////////////////////

	/** List of added item nodes */
	private List addedNodes;

	/** List of removed item nodes */
	private List removedNodes;

	/** List of all leaf item nodes */
	private Map dataInformation;

	/** The used strategy for the tree. */
	private Strategy strategy;

	/** Flag whether to fire event after changing the tree or not. */
	private boolean fireEvents;

	/**
	 * Constructor.
	 * @param usedStrategy Strategy to use
	 * @param fireEvents Flag whether to fire event, when the tree was changed
	 */
	public GenericModel(Strategy usedStrategy, boolean fireEvents)
	{
		super(new PropertyNode());
		getRootNode().setNodeMapper(new RootNodeMapper());
		this.strategy = usedStrategy;
		this.fireEvents = fireEvents;

		addedNodes = new ArrayList();
		removedNodes = new ArrayList();
	}

	/**
	 * Get the root node.
	 * @return root node
	 */
	private PropertyNode getRootNode()
	{
		return (PropertyNode) root;
	}

	//////////////////////////////////////////////////
	// @@ Add new data
	//////////////////////////////////////////////////

	/**
	 * Add a new object to the tree. The data mapper of the
	 * strategy must understand the object in order
	 * to create the path to the root.
	 * @param data the object
	 * @return iterator over all added {@link GenericNode} objects
	 */
	public Iterator addNewData(Object data)
	{
		addedNodes.clear();
		removedNodes.clear();
		addDataToNodes(data, strategy.createDataMapper(data), getRootNode(), 0);
		return addedNodes.iterator();
	}

	/**
	 * Add a new object to the tree.
	 * @param data the object
	 * @param mapper The data mapper to use with the object
	 * @param parentNode the parent node
	 * @param level The current level in the tree
	 */
	private void addDataToNodes(Object data, DataMapper mapper, GenericNode parentNode, int level)
	{
		if (level == mapper.getLevels())
		{
			// The current level should contain the leaf node
			LeafNode leaf = mapper.createLeafNode();
			parentNode.addChild(leaf, strategy);
			if (fireEvents)
			{
				nodesWereInserted(parentNode, new int [] { parentNode.getIndex(leaf) });
			}

			return;
		}

		// Get an existring property node, if none exist create one
		PropertyNode group = mapper.createPropertyNode(level);
		PropertyNode existingGroup = parentNode.getPropertyNode(group.getPropertyData());
		if (existingGroup == null)
		{
			// Create the property node
			existingGroup = group;
			parentNode.addChild(group, strategy);
			existingGroup.setLevel(level);
			if (fireEvents)
			{
				nodesWereInserted(parentNode, new int [] { parentNode.getIndex(group) });
			}
		}

		addDataToNodes(data, mapper, existingGroup, level + 1);
	}

	//////////////////////////////////////////////////
	// @@ rebuild the whole tree
	//////////////////////////////////////////////////

	/**
	 * Reload the model with an array of objects.
	 * @param data The objects that will be displayed in the tree
	 */
	public void reload(Object [] data)
	{
		clearModel();
		for (int i = 0; i < data.length; i++)
		{
			addNewData(data [i]);
		}

		if (fireEvents)
			nodeStructureChanged(root);
	}

	/**
	 * Reload the model with an iterator.
	 * @param iterator Iterator over a list of objects
	 */
	public void reload(Iterator iterator)
	{
		clearModel();
		while (iterator.hasNext())
		{
			addNewData(iterator.next());
		}

		if (fireEvents)
			nodeStructureChanged(root);
	}

	/**
	 * Remove all nodes from the tree.
	 */
	public void clearModel()
	{
		dataInformation = new HashMap();
		clearNode(getRootNode());
		getRootNode().clearChilds();
		if (fireEvents)
			nodeStructureChanged(root);
	}

	/**
	 * Remove all references from the tree structure
	 * @param node The node whose references should be removed
	 */
	private void clearNode(GenericNode node)
	{
		if (node.isLeaf())
		{
			node.nodeData = null;
			return;
		}

		for (int i = 0; i < node.getChildCount(); i++)
		{
			GenericNode genericNode = (GenericNode) node.getChildAt(i);
			clearNode(genericNode);
			genericNode.clearChilds();
		}
	}

	//////////////////////////////////////////////////
	// @@ Remove data
	//////////////////////////////////////////////////

	/**
	 * Remove a data object from the tree.
	 * @param data The data to remove
	 * @return iterator over removed nodes
	 */
	public Iterator removeData(Object data)
	{
		Information info = (Information) dataInformation.get(data);
		return removeNode(info.node);
	}

	/**
	 * Remove a node from the tree.
	 * @param node The node to remove
	 * @return iteator over removed nodes
	 */
	public Iterator removeNode(GenericNode node)
	{
		addedNodes.clear();
		removedNodes.clear();
		removeNodeFromTree(node);
		return removedNodes.iterator();
	}

	/**
	 * Remove a node from the tree.
	 * @param node The node to remove
	 */
	private void removeNodeFromTree(GenericNode node)
	{
		GenericNode parentNode = (GenericNode) node.getParent();
		int index = parentNode.getIndex(node);
		parentNode.removeChild(node);
		removedNodes.add(node);

		if (fireEvents)
			nodesWereRemoved(parentNode, new int [] { index }, new Object [] { node });

		if (parentNode.getParent() == null)
			return;

		if (parentNode.isLeaf())
			removeNodeFromTree(parentNode);
	}

	/**
	 * Check if the tree contains a special data object
	 * @param data The data object to check for
	 * @return true if the tree contains the data otherwise false
	 */
	public boolean containsDataObject(Object data)
	{
		return dataInformation.get(data) != null;
	}

	/**
	 * Get an iterator of all data objects the tree contains.
	 * @return iterator over data objects
	 */
	public Iterator getDataObjects()
	{
		return dataInformation.keySet().iterator();
	}

	/**
	 * Wrapper to hold the data object and the leaf node of the data object.
	 */
	class Information
	{
		LeafNode node;

		Object data;

		//////////////////////////////////////////////////
		// @@ Construction
		//////////////////////////////////////////////////

		/**
		 * Constructor.
		 * Set information from the data to this.
		 *
		 * @param data The data object
		 */
		protected Information(Object data)
		{
			this.data = data;
		}
	}

	/**
	 * Simple root node mapper.
	 */
	class RootNodeMapper
		implements NodeMapper
	{
		public String getDisplayString(Object nodeData)
		{
			return "root";
		}
	}
}
