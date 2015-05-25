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

import java.util.Iterator;
import java.util.List;

import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.core.MimeTypes;
import org.openbp.core.model.Association;
import org.openbp.core.model.AssociationUtil;
import org.openbp.core.model.ModelObjectSymbolNames;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.activity.ActivityItem;
import org.openbp.core.model.item.activity.ActivitySocket;
import org.openbp.core.model.item.activity.ActivitySocketImpl;
import org.openbp.core.model.item.activity.PlaceholderItem;

/**
 * Standard implementation of a placeholder node.
 * When executed, the placeholder forwards control to its default exit socket.
 *
 * @author Heiko Erhardt
 */
public class PlaceholderNodeImpl extends MultiSocketNodeImpl
	implements PlaceholderNode
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Reference path to some process element */
	private String referencePath;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public PlaceholderNodeImpl()
	{
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

		PlaceholderNodeImpl src = (PlaceholderNodeImpl) source;

		// Copy member data
		referencePath = src.referencePath;
	}

	/**
	 * @copy ItemProvider.copyFromItem
	 */
	public void copyFromItem(Item item, int syncFlags)
	{
		ActivityItem ai = (ActivityItem) item;

		// Make node name unique
		String newName = ai.getName();
		if (getProcess() != null)
		{
			newName = NamedObjectCollectionUtil.createUniqueId(getProcess().getNodeList(), newName);
		}
		setName(newName);
		setDisplayName(item.getDisplayName());
		setDescription(item.getDescription());

		// Copy the sockets
		clearSockets();
		for (Iterator it = ai.getSockets(); it.hasNext();)
		{
			ActivitySocket activitySocket = (ActivitySocket) it.next();

			NodeSocket nodeSocket = new NodeSocketImpl();
			nodeSocket.copyFromActivitySocket(activitySocket, syncFlags);
			addSocket(nodeSocket);
		}

		// Copy description and display name
		ItemSynchronization.syncDisplayObjects(this, ai, syncFlags);

		setGeometry(ai.getGeometry());

		if (item instanceof PlaceholderItem)
		{
			referencePath = ((PlaceholderItem) item).getReferencePath();
		}
	}

	/**
	 * @copy ItemProvider.copyToItem
	 */
	public void copyToItem(Item item, int syncFlags)
	{
		ActivityItem ai = (ActivityItem) item;

		ai.setDisplayName(getDisplayName());
		ai.setDescription(getDescription());

		// Copy the sockets
		ai.clearSockets();
		for (Iterator it = getSockets(); it.hasNext();)
		{
			NodeSocket nodeSocket = (NodeSocket) it.next();

			ActivitySocket activitySocket = new ActivitySocketImpl();
			nodeSocket.copyToActivitySocket(activitySocket, syncFlags);
			ai.addSocket(activitySocket);
		}

		ai.setGeometry(getGeometry());

		if (item instanceof PlaceholderItem)
		{
			((PlaceholderItem) item).setReferencePath(referencePath);
		}
	}

	/**
	 * Gets the name of the standard icon of this object.
	 * The icon name can be used by the client-side IconModel to retrieve an icon for the object.
	 *
	 * @return The icon name or null if the object does not have a particular icon
	 */
	public String getModelObjectSymbolName()
	{
		return ModelObjectSymbolNames.PLACEHOLDER_NODE;
	}

	/**
	 * Determines if this node supports multiple control links on a single exit socket.
	 *
	 * @return
	 *		true	More than one control link can be attached to the exit sockets of this node.
	 *		false	An exit socket of this node supports one control link at a time only.
	 */
	public boolean isMultiExitLinkNode()
	{
		return true;
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

		if (referencePath != null && referencePath.indexOf(ModelQualifier.PATH_DELIMITER_CHAR) < 0)
		{
			// Prepend model qualifier for local path specifications
			referencePath = getOwningModel().getQualifier().toString() + ModelQualifier.PATH_DELIMITER_CHAR + referencePath;
		}

		// By default, we assume we are referencing an entire process
		String mimeType = MimeTypes.PROCESS_ITEM;
		if (referencePath != null && referencePath.indexOf(ModelQualifier.OBJECT_DELIMITER_CHAR) < 0)
		{
			// We are referencing some element within a process
			mimeType = MimeTypes.PROCESS_NODE;
		}

		associations = AssociationUtil.addAssociation(associations, -1, "Reference", referencePath, this, new String [] { mimeType }, Association.PRIMARY, "No process or process element reference has been specified for this node");

		return associations;
	}

	//////////////////////////////////////////////////
	// @@ Info text
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.core.model.ModelObject#getInfoText()
	 */
	public String [] getInfoText()
	{
		String dt = getDisplayText();

		if (referencePath != null)
		{
			dt = dt + "\n-> " + referencePath;
		}

		return createInfoText(getName(), dt, getDescriptionText());
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the reference path to some process element.
	 * @nowarn
	 */
	public String getReferencePath()
	{
		return referencePath;
	}

	/**
	 * Sets the reference path to some process element.
	 * @nowarn
	 */
	public void setReferencePath(String referencePath)
	{
		this.referencePath = referencePath;
	}
}
