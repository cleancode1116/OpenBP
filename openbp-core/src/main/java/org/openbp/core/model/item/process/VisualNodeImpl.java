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
package org.openbp.core.model.item.process;

import java.util.List;

import org.openbp.core.model.Association;
import org.openbp.core.model.AssociationUtil;
import org.openbp.core.model.ModelObjectSymbolNames;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.uiadapter.UIAdapterDescriptor;
import org.openbp.core.uiadapter.UIAdapterDescriptorRegistry;

/**
 * Standard implementation of a visual node.
 *
 * @author Heiko Erhardt
 */
public class VisualNodeImpl extends ActivityNodeImpl
	implements VisualNode
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** The Id for this visual item */
	private String visualId;

	/** Flag that denotes if the process state should be persisted */
	private boolean waitStateNode;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public VisualNodeImpl()
	{
		activityItemType = ItemTypes.VISUAL;
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

		VisualNodeImpl src = (VisualNodeImpl) source;

		// Copy member data
		visualId = src.visualId;
		waitStateNode = src.waitStateNode;
	}

	/**
	 * Gets the name of the standard icon of this object.
	 * The icon name can be used by the client-side IconModel to retrieve an icon for the object.
	 *
	 * @return The icon name or null if the object does not have a particular icon
	 */
	public String getModelObjectSymbolName()
	{
		return ModelObjectSymbolNames.VISUAL_NODE;
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

		// Get the set of visual types from the UIAdapterRegisty.
		UIAdapterDescriptorRegistry registry = UIAdapterDescriptorRegistry.getInstance();
		String[] visualTypes = registry.getVisualTypes();

		for (int i = 0; i < visualTypes.length; i++)
		{
			String visualType = visualTypes[i];
			String visualSource = registry.getVisualPath(visualType, getVisualId(), getProcess().getModel());

			associations = AssociationUtil.addAssociation(associations, - 1, registry.getDisplayText(visualType,
				UIAdapterDescriptor.ASSOCIATION_DISPLAY_NAME), visualSource, this, registry.getMimeTypes(visualType), Association.NORMAL, registry
				.getDisplayText(visualType, UIAdapterDescriptor.ASSOCIATION_HINT_MESSAGE));
		}

		return associations;
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Gets the Id of the visual item.
	 * @nowarn
	 */
	public String getVisualId()
	{
		return this.visualId;
	}

	/**
	 * Sets the Id of the visual item.
	 * @nowarn
	 */
	public void setVisualId(String visualId)
	{
		this.visualId = visualId;
	}

	/**
	 * Checks the flag that denotes if the process state should be persisted.
	 * @nowarn
	 */
	public boolean hasWaitStateNode()
	{
		return waitStateNode;
	}

	/**
	 * Gets the flag that denotes if the process state should be persisted.
	 * @nowarn
	 */
	public boolean isWaitStateNode()
	{
		return waitStateNode;
	}

	/**
	 * Sets the flag that denotes if the process state should be persisted.
	 * @nowarn
	 */
	public void setWaitStateNode(boolean waitStateNode)
	{
		this.waitStateNode = waitStateNode;
	}

	//////////////////////////////////////////////////
	// @@ Info text
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.core.model.ModelObject#getInfoText()
	 */
	public String[] getInfoText()
	{
		return createInfoText(getName(), getDisplayText(), getDescriptionText());
	}
}
