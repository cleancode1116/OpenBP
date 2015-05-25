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
package org.openbp.cockpit.plugins.finder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.openbp.cockpit.plugins.finder.treemodel.DataMapper;
import org.openbp.cockpit.plugins.finder.treemodel.LeafNode;
import org.openbp.cockpit.plugins.finder.treemodel.NodeMapper;
import org.openbp.cockpit.plugins.finder.treemodel.PropertyNode;
import org.openbp.cockpit.plugins.finder.treemodel.Strategy;
import org.openbp.common.generic.description.DisplayObject;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.guiclient.model.ModelConnector;
import org.openbp.guiclient.plugins.displayobject.DisplayObjectPlugin;

/**
 * Strategy for the reference tree.
 *
 * @author Baumgartner Michael
 */
public class RefStrategy
	implements Strategy
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Property key for the itemtype display in the node. Used by the renderer. */
	public static final String ITEMTYPE_KEY = "itemtype";

	/** Property key for the itemtype of the reference item. */
	public static final String REFERENCE_ITEMTYPE_KEY = "referenceItemType";

	/** Property key for the priority of the nodes. Used by the comparator. */
	public static final String PRIORITY_KEY = "priority";

	//////////////////////////////////////////////////
	// @@ Member
	//////////////////////////////////////////////////

	/** Mapper to display the models and the itemtype. */
	static NodeMapper stringMapper = new StringMapper();

	/** Mapper to display the models and the itemtype. */
	static NodeMapper displayMapper = new DisplayMapper();

	/** Mapper to display the model object (leaf). */
	static NodeMapper coreMapper = new ModelObjectMapper();

	/** Comparator for the tree. */
	static Comparator refComp = new RefComparator();

	/** The item type of the model object that was searched. */
	String referenceItemType;

	public void setReferenceItemType(String itemType)
	{
		referenceItemType = itemType;
	}

	/**
	 * @copy org.openbp.cockpit.plugins.finder.treemodel.Strategy.createDataMapper
	 */
	public DataMapper createDataMapper(Object leafData)
	{
		DataMapper mapper = new RefMapper();
		mapper.init(leafData);
		return mapper;
	}

	/**
	 * @copy org.openbp.cockpit.plugins.finder.treemodel.Strategy.getTreeComparator
	 */
	public Comparator getTreeComparator()
	{
		return refComp;
	}

	//////////////////////////////////////////////////
	// @@ Data Mapper
	//////////////////////////////////////////////////

	/**
	 * The mapper for one object that is display in the tree.
	 */
	class RefMapper
		implements DataMapper
	{
		/** List with all Node of the data. */
		private List list = new ArrayList();

		/** The object to display in the tree. */
		private ModelObject leafObject;

		/**
		 * @copy org.openbp.cockpit.plugins.finder.treemodel.DataMapper.init
		 */
		public void init(Object leafObj)
		{
			this.leafObject = (ModelObject) leafObj;
			ModelQualifier qualifier = leafObject.getQualifier();

			PropertyNode modelNode = new PropertyNode();
			ModelQualifier modelQualifier = ModelQualifier.constructModelQualifier(qualifier.getModel());
			try
			{
				// Try to load the model. If it can be loaded, then store it
				// in the node. This makes it possible to toggle between the
				// display name and the name
				Model model = ModelConnector.getInstance().getModelByQualifier(modelQualifier);
				modelNode.setNodeMapper(displayMapper);
				modelNode.setPropertyData(model);
			}
			catch (OpenBPException e)
			{
				modelNode.setNodeMapper(stringMapper);
				modelNode.setPropertyData(modelQualifier.toString());
			}

			// Add properties for the comparator and the renderer
			modelNode.addProperty(ITEMTYPE_KEY, ItemTypes.MODEL);
			modelNode.addProperty(PRIORITY_KEY, Integer.valueOf(1));
			list.add(modelNode);

			// If the found references is no item, then add the item the
			// model object belongs to a part of the 'tree path'
			if (qualifier.getObjectPath() != null)
			{
				// Add the itemtype to the tree as a group
				PropertyNode objectPathNode = new PropertyNode();
				try
				{
					Item item = ModelConnector.getInstance().getItemByQualifier(qualifier, true);
					objectPathNode.setNodeMapper(displayMapper);
					objectPathNode.setPropertyData(item);
				}
				catch (OpenBPException e)
				{
					objectPathNode.setNodeMapper(stringMapper);
					objectPathNode.setPropertyData(qualifier.getItem());
				}

				// Add properties for the comparator and the renderer
				objectPathNode.addProperty(ITEMTYPE_KEY, qualifier.getItemType());
				objectPathNode.addProperty(PRIORITY_KEY, Integer.valueOf(2));
				list.add(objectPathNode);
			}
		}

		/**
		 * @copy org.openbp.cockpit.plugins.finder.treemodel.DataMapper.createLeafNode()
		 */
		public LeafNode createLeafNode()
		{
			// Create the leaf node for the found reference
			LeafNode node = new LeafNode();
			node.setNodeMapper(coreMapper);
			node.setLeafData(leafObject);

			// Add properties for the comparator and the renderer.
			node.addProperty(PRIORITY_KEY, Integer.valueOf(3));
			node.addProperty(REFERENCE_ITEMTYPE_KEY, referenceItemType);

			ModelQualifier qualifier = leafObject.getQualifier();
			if (qualifier.getObjectPath() == null)
				node.addProperty(ITEMTYPE_KEY, qualifier.getItemType());
			return node;
		}

		/**
		 * @copy org.openbp.cockpit.plugins.finder.treemodel.DataMapper.createPropertyNode
		 */
		public PropertyNode createPropertyNode(int level)
		{
			return (PropertyNode) list.get(level);
		}

		/**
		 * @see org.openbp.cockpit.plugins.finder.treemodel.DataMapper#getLevels()
		 */
		public int getLevels()
		{
			return list.size();
		}
	}
}

/**
 * Mapper for the leaf node.
 */
class ModelObjectMapper
	implements NodeMapper
{
	/**
	 * @copy org.openbp.cockpit.plugins.finder.treemodel.NodeMapper.getDisplayString
	 */
	public String getDisplayString(Object nodeData)
	{
		ModelQualifier qualifier = ((ModelObject) nodeData).getQualifier();
		String object = qualifier.getObjectPath();
		if (object == null)
		{
			DisplayObject display = (DisplayObject) nodeData;
			if (DisplayObjectPlugin.getInstance().isTitleModeText())
			{
				String displayStr = display.getDisplayName();
				if (displayStr != null)
					return displayStr;
				return display.getName();
			}
			return display.getName();
		}
		return object;
	}
}

/**
 * Mapper for the other nodes.
 */
class StringMapper
	implements NodeMapper
{
	/**
	 * @copy org.openbp.cockpit.plugins.finder.treemodel.NodeMapper.getDisplayString
	 */
	public String getDisplayString(Object nodeData)
	{
		return (String) nodeData;
	}
}

/**
 * Mapper for the nodes with display object {@link DisplayObject}
 */
class DisplayMapper
	implements NodeMapper
{
	/**
	 * @copy org.openbp.cockpit.plugins.finder.treemodel.NodeMapper.getDisplayString
	 */
	public String getDisplayString(Object nodeData)
	{
		DisplayObject display = (DisplayObject) nodeData;
		if (DisplayObjectPlugin.getInstance().isTitleModeText())
		{
			String displayStr = display.getDisplayName();
			if (displayStr != null)
				return displayStr;
			return display.getName();
		}
		return display.getName();
	}
}
