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
package org.openbp.core.model.item.activity;

import java.util.List;

import org.openbp.common.generic.Copyable;
import org.openbp.common.logger.LogLevel;
import org.openbp.core.MimeTypes;
import org.openbp.core.handler.HandlerDefinition;
import org.openbp.core.model.Association;
import org.openbp.core.model.AssociationUtil;
import org.openbp.core.model.ModelObject;

/**
 * Activity object that executes a Java handler.
 *
 * This activity wraps an activity handler that is being called when the activity is executed.
 *
 * @author Heiko Erhardt
 */
public class JavaActivityItemImpl extends ActivityItemImpl
	implements JavaActivityItem
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Handler definition */
	private HandlerDefinition handlerDefinition;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public JavaActivityItemImpl()
	{
		super();
		setHandlerDefinition(new HandlerDefinition());
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

		JavaActivityItemImpl src = (JavaActivityItemImpl) source;

		setHandlerDefinition(new HandlerDefinition());
		handlerDefinition.copyFrom(src.handlerDefinition, Copyable.COPY_DEEP);
		handlerDefinition.setOwner(this);
	}

	//////////////////////////////////////////////////
	// @@ Pre save/post load processing and validation
	//////////////////////////////////////////////////

	/**
	 * Instantiates objects the item might reference.
	 * It usually instantiates classes that are referenced by activities, data types etc.
	 * Those classes might not be present on the client side, so this method
	 * should be called on the server side only.<br>
	 * Make sure you call this method \iafter\i calling the {@link ModelObject#maintainReferences} method,
	 * so any references needed for the instantiation process can be expected to be resolved.
	 *
	 * Any errors will be logged to the message container of the model manager that
	 * loaded the object.
	 */
	public void instantiate()
	{
		super.instantiate();

		handlerDefinition.instantiate();
	}

	/**
	 * @copy ModelObject.validate
	 */
	public boolean validate(int flag)
	{
		// Check for an object name first
		boolean success = super.validate(flag);

		if (handlerDefinition.getHandlerClassName() == null && handlerDefinition.getScript() == null && (flag & VALIDATE_RUNTIME) != 0)
		{
			getModelMgr().getMsgContainer().addMsg(LogLevel.WARN, this, "Neither handler class nor script specified for Java activity.");
			success = false;
		}

		return success;
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

		associations = AssociationUtil.addAssociation(associations, - 1, "Activity", this, this, new String[]
		{
			MimeTypes.ACTIVITY_ITEM
		}, Association.PRIMARY, "");

		associations = handlerDefinition.addHandlerAssociations(associations, "Activity component handler class", Association.PRIMARY);

		associations = AssociationUtil.addAssociations(associations, - 1, super.getAssociations());

		return associations;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Checks if the handler definition is not empty.
	 * Used by Castor to decide wether an entry is necessary in the XML file.
	 * @nowarn
	 */
	public boolean hasHandlerDefinition()
	{
		return handlerDefinition.isDefined();
	}

	/**
	 * Gets the handler definition.
	 * @nowarn
	 */
	public HandlerDefinition getHandlerDefinition()
	{
		return handlerDefinition;
	}

	/**
	 * Sets the handler definition.
	 * @nowarn
	 */
	public void setHandlerDefinition(HandlerDefinition handlerDefinition)
	{
		this.handlerDefinition = handlerDefinition;
		if (handlerDefinition != null)
		{
			handlerDefinition.setOwner(this);
		}
	}
}
