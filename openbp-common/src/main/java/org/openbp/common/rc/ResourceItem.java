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
package org.openbp.common.rc;

import org.w3c.dom.Element;

/**
 * Abstract {@link ResourceItem} implementation.
 *
 * @author Andreas Putz
 */
public abstract class ResourceItem
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** XML 'name' attribute for the property tag */
	public static final String ATTRIBUTE_PROPERTY_NAME = "name";

	/** XML 'type' attribute for the property tag */
	public static final String ATTRIBUTE_PROPERTY_TYPE = "type";

	/** The delimiter for the full qualified name */
	public static final String GROUP_DELIMITER = ".";

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Resource name */
	protected String name;

	/** Resource object */
	protected Object resourceObject;

	/** Resource this item belongs to */
	protected ResourceCollection resourceCollection;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	protected ResourceItem()
	{
	}

	//////////////////////////////////////////////////
	// @@ Resource item interface implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the name of the resource item.
	 * @nowarn
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Gets the object associated with this resource.
	 * Automatically tries to load the object if not done yet.
	 *
	 * @return The resource object or null if it could not be loaded
	 */
	public Object getObject()
	{
		if (resourceObject == null)
		{
			resourceObject = loadResourceObject();
		}
		return resourceObject;
	}

	/**
	 * Gets the resource this item belongs to.
	 * @nowarn
	 */
	public ResourceCollection getResourceCollection()
	{
		return resourceCollection;
	}

	/**
	 * Loads the object associated with the resource.
	 * The default implementation does nothing and returns null.
	 *
	 * @return The object or null on error
	 */
	protected Object loadResourceObject()
	{
		return null;
	}

	/**
	 * Determines the information from the DOM element
	 * and set this to the resource item.
	 *
	 * @param resourceCollection Resource the item belongs to
	 * @param source Resource item node
	 * @param group Resource group name or null
	 */
	public void initializeFromDOM(ResourceCollection resourceCollection, Element source, String group)
	{
		this.resourceCollection = resourceCollection;
		name = source.getAttribute(ResourceItem.ATTRIBUTE_PROPERTY_NAME);
		if (group != null)
		{
			name = group + ResourceItem.GROUP_DELIMITER + name;
		}
	}

	/**
	 * Returns a string representation of this resource item.
	 * @nowarn
	 */
	public String toString()
	{
		return resourceObject != null ? resourceObject.toString() : "(empty)";
	}

	/**
	 * Returns a string that identifies the resource item in error messages.
	 * @nowarn
	 */
	protected String getErrorName()
	{
		return getResourceCollection().getErrorName() + ":" + getName();
	}
}
