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
package org.openbp.core.model.item.visual;

import java.util.List;

import org.openbp.core.MimeTypes;
import org.openbp.core.model.Association;
import org.openbp.core.model.AssociationUtil;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.activity.ActivityItemImpl;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.VisualNode;
import org.openbp.core.model.item.process.VisualNodeImpl;
import org.openbp.core.uiadapter.UIAdapterDescriptor;
import org.openbp.core.uiadapter.UIAdapterDescriptorRegistry;

/**
 * OpenBP activity object that executes a user interface template
 * (the actual implementation of the visual) and writes the
 * result of the execution to the request output stream.
 *
 * The type of the visual implementation depends on the user interface engine.<br>
 * A visual implementation might be a Java Server Page (JSP) or a Velocity template for example.
 * The UI node will store a compiled version of the visual implementation (if the user interface
 * engine supports this) to increase performance.
 *
 * @author Heiko Erhardt
 */
public class VisualItemImpl extends ActivityItemImpl
	implements VisualItem
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** The Id for this visual item */
	private String visualId;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public VisualItemImpl()
	{
		setItemType(ItemTypes.VISUAL);
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
		super.copyFrom(source, copyMode);

		VisualItemImpl src = (VisualItemImpl) source;

		visualId = src.visualId;
	}

	/**
	 * Creates a new visual node that references this visual.
	 *
	 * @return The new {@link VisualNode}
	 * @copy NodeProvider.toNode
	 */
	public Node toNode(ProcessItem process, int syncFlags)
	{
		VisualNode result = new VisualNodeImpl();

		result.setProcess(process);
		result.copyFromItem(this, syncFlags);

		return result;
	}

	//////////////////////////////////////////////////
	// @@ Associations
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.getAssociations
	 */
	public List getAssociations()
	{
		List associations = null;

		associations = AssociationUtil.addAssociation(null, - 1, "Visual", this, this, new String[]
		{
			MimeTypes.ACTIVITY_ITEM
		}, Association.PRIMARY, "");

		// Get the set of visual types from the UIAdapterRegisty.
		UIAdapterDescriptorRegistry registry = UIAdapterDescriptorRegistry.getInstance();
		String[] visualTypes = registry.getVisualTypes();

		for (int i = 0; i < visualTypes.length; i++)
		{
			String visualType = visualTypes[i];
			String visualSource = registry.getVisualPath(visualType, getVisualId(), getModel());

			associations = AssociationUtil.addAssociation(associations, - 1, registry.getDisplayText(visualType,
				UIAdapterDescriptor.ASSOCIATION_DISPLAY_NAME), visualSource, this, registry.getMimeTypes(visualType), Association.NORMAL, registry
				.getDisplayText(visualType, UIAdapterDescriptor.ASSOCIATION_HINT_MESSAGE));
		}

		associations = AssociationUtil.addAssociations(associations, - 1, super.getAssociations());

		return associations;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the Id of the visual item.
	 * @nowarn
	 */
	public String getVisualId()
	{
		return visualId;
	}

	/**
	 * Set the Id for the visual item.
	 * @nowarn
	 */
	public void setVisualId(String visualId)
	{
		this.visualId = visualId;
	}
}
