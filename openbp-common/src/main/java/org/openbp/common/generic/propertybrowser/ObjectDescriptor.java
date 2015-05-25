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
package org.openbp.common.generic.propertybrowser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.common.generic.Copyable;
import org.openbp.common.generic.description.DisplayObjectImpl;
import org.openbp.common.util.CopyUtil;
import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.common.util.iterator.EmptyIterator;

/**
 * An object descriptor describes an object that can be edited by an property browser.
 *
 * A particular object descriptor always refers to the class of the object to be edited.
 * This class can also be an interface.
 *
 * The OD describes the object in general (display name, description) and all of its
 * properties ({@link PropertyDescriptor} objects).
 *
 * The object descriptor can be serialized to/deserialized from XML.
 * The XML file will reside in the same location as the object's class file.
 *
 * Use the {@link ObjectDescriptorMgr} class to access object descriptors for a particular class.
 * The manager will automatically locate and locate the object descriptor for this class.
 *
 * After loading the OD, the object descriptor manager will iterate the property list.
 * The manager will try to resolve attribute information of a property that is not set
 * (e. g. description, type name, editor class name) from the OD of the super class
 * or from the OD of its implemented interfaces.<br>
 * This means that for common attributes you only need to specify their property name
 * in the property descriptor list; the remaining information about the property will
 * be retrieved from the base class.
 *
 * @author Heiko Erhardt
 */
public class ObjectDescriptor extends DisplayObjectImpl
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Name of the object class this descriptor is suitable for */
	private String objectClassName;

	/** Validator class name */
	private String validatorClassName;

	/** List of properties (contains {@link PropertyDescriptor} objects) */
	private List propertyList;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Object class this descriptor is suitable for */
	private Class objectClass;

	/** Validator class */
	private Class validatorClass;

	/** Flag if this descriptor is a custom descriptor (see {@link ObjectDescriptorMgr}) */
	private boolean customDescriptor;

	/** Flag if the properties of this descriptor have been resolved (see implementation of {@link ObjectDescriptorMgr}) */
	private boolean propertiesResolved;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ObjectDescriptor()
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

		ObjectDescriptor src = (ObjectDescriptor) source;

		objectClassName = src.objectClassName;
		validatorClassName = src.validatorClassName;
		objectClass = src.objectClass;
		validatorClass = src.validatorClass;

		if (copyMode == Copyable.COPY_FIRST_LEVEL || copyMode == Copyable.COPY_DEEP)
		{
			// Create deep clones of collection members
			propertyList = (List) CopyUtil.copyCollection(src.propertyList, copyMode == Copyable.COPY_DEEP ? CopyUtil.CLONE_VALUES : CopyUtil.CLONE_NONE);
		}
		else
		{
			// Shallow clone
			propertyList = src.propertyList;
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the name of the object class this descriptor is suitable for.
	 * @nowarn
	 */
	public String getObjectClassName()
	{
		return objectClassName;
	}

	/**
	 * Sets the name of the object class this descriptor is suitable for.
	 * @nowarn
	 */
	public void setObjectClassName(String objectClassName)
	{
		this.objectClassName = objectClassName;
	}

	/**
	 * Gets the object class this descriptor is suitable for.
	 * @nowarn
	 */
	public Class getObjectClass()
	{
		return objectClass;
	}

	/**
	 * Sets the object class this descriptor is suitable for.
	 * @nowarn
	 */
	public void setObjectClass(Class objectClass)
	{
		this.objectClass = objectClass;
	}

	/**
	 * Gets the validator class name.
	 * @return Specifies a class that implements the ObjectValidator interface
	 */
	public String getValidatorClassName()
	{
		return validatorClassName;
	}

	/**
	 * Sets the validator class name.
	 * @param validatorClassName Specifies a class that implements the ObjectValidator interface
	 */
	public void setValidatorClassName(String validatorClassName)
	{
		this.validatorClassName = validatorClassName;
	}

	/**
	 * Gets the validator class.
	 * @return A class that implements the ObjectValidator interface
	 */
	public Class getValidatorClass()
	{
		return validatorClass;
	}

	/**
	 * Sets the validator class.
	 * @param validatorClass A class that implements the ObjectValidator interface
	 */
	public void setValidatorClass(Class validatorClass)
	{
		this.validatorClass = validatorClass;
	}

	/**
	 * Gets the property descriptor for a particular property.
	 * @param name Name of the property
	 * @return The property descriptor or null if no such property exists
	 */
	public PropertyDescriptor getProperty(String name)
	{
		if (propertyList != null)
			return (PropertyDescriptor) NamedObjectCollectionUtil.getByName(propertyList, name);
		return null;
	}

	/**
	 * Gets the list of properties.
	 * @return An iterator of {@link PropertyDescriptor} objects
	 */
	public Iterator getProperties()
	{
		if (propertyList == null)
			return EmptyIterator.getInstance();
		return propertyList.iterator();
	}

	/**
	 * Adds a property.
	 * @param property The property to add
	 */
	public void addProperty(PropertyDescriptor property)
	{
		if (propertyList == null)
			propertyList = new ArrayList();
		propertyList.add(property);
	}

	/**
	 * Clears the list of properties.
	 */
	public void clearProperties()
	{
		propertyList = null;
	}

	/**
	 * Gets the list of properties.
	 * @return A list of {@link PropertyDescriptor} objects
	 */
	public List getPropertyList()
	{
		return propertyList;
	}

	/**
	 * Gets the flag if this descriptor is a custom descriptor (see {@link ObjectDescriptorMgr}).
	 * @nowarn
	 */
	public boolean isCustomDescriptor()
	{
		return customDescriptor;
	}

	/**
	 * Sets the flag if this descriptor is a custom descriptor (see {@link ObjectDescriptorMgr}).
	 * @nowarn
	 */
	public void setCustomDescriptor(boolean customDescriptor)
	{
		this.customDescriptor = customDescriptor;
	}

	/**
	 * Gets the flag if the properties of this descriptor have been resolved (see implementation of {@link ObjectDescriptorMgr}).
	 * @nowarn
	 */
	public boolean isPropertiesResolved()
	{
		return propertiesResolved;
	}

	/**
	 * Sets the flag if the properties of this descriptor have been resolved (see implementation of {@link ObjectDescriptorMgr}).
	 * @nowarn
	 */
	public void setPropertiesResolved(boolean propertiesResolved)
	{
		this.propertiesResolved = propertiesResolved;
	}
}
