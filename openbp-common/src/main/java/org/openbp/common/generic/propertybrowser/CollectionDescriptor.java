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

import org.openbp.common.ReflectUtil;
import org.openbp.common.generic.Copyable;

/**
 * The collection descriptor defines the type and settings of a collection property.
 *
 * @author Heiko Erhardt
 */
public class CollectionDescriptor
	implements Cloneable, Copyable
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Type class name */
	private String typeClassName;

	/** Add enabled */
	private boolean addEnabled;

	/** Delete enabled */
	private boolean deleteEnabled;

	/** Reorder enabled */
	private boolean reorderEnabled;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Type class */
	private Class typeClass;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public CollectionDescriptor()
	{
	}

	/**
	 * Creates a clone of this object.
	 * @return The clone (a deep copy of this object)
	 * @throws CloneNotSupportedException If the cloning of one of the contained members failed
	 */
	public Object clone()
		throws CloneNotSupportedException
	{
		CollectionDescriptor clone = (CollectionDescriptor) super.clone();

		// Perform a deep copy
		clone.copyFrom(this, Copyable.COPY_DEEP);

		return clone;
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

		CollectionDescriptor src = (CollectionDescriptor) source;

		typeClassName = src.typeClassName;
		addEnabled = src.addEnabled;
		deleteEnabled = src.deleteEnabled;
		reorderEnabled = src.reorderEnabled;
		typeClass = src.typeClass;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the type class name.
	 * @nowarn
	 */
	public String getTypeClassName()
	{
		return typeClassName;
	}

	/**
	 * Sets the type class name.
	 * @nowarn
	 */
	public void setTypeClassName(String typeClassName)
	{
		this.typeClassName = typeClassName;
	}

	/**
	 * Gets the add enabled.
	 * @nowarn
	 */
	public boolean isAddEnabled()
	{
		return addEnabled;
	}

	/**
	 * Determines if the add enabled is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasAddEnabled()
	{
		return addEnabled;
	}

	/**
	 * Sets the add enabled.
	 * @nowarn
	 */
	public void setAddEnabled(boolean addEnabled)
	{
		this.addEnabled = addEnabled;
	}

	/**
	 * Gets the delete enabled.
	 * @nowarn
	 */
	public boolean isDeleteEnabled()
	{
		return deleteEnabled;
	}

	/**
	 * Determines if the delete enabled is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasDeleteEnabled()
	{
		return deleteEnabled;
	}

	/**
	 * Sets the delete enabled.
	 * @nowarn
	 */
	public void setDeleteEnabled(boolean deleteEnabled)
	{
		this.deleteEnabled = deleteEnabled;
	}

	/**
	 * Gets the reorder enabled.
	 * @nowarn
	 */
	public boolean isReorderEnabled()
	{
		return reorderEnabled;
	}

	/**
	 * Determines if the reorder enabled is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasReorderEnabled()
	{
		return reorderEnabled;
	}

	/**
	 * Sets the reorder enabled.
	 * @nowarn
	 */
	public void setReorderEnabled(boolean reorderEnabled)
	{
		this.reorderEnabled = reorderEnabled;
	}

	/**
	 * Gets the type class, instantiating it if possible.
	 * @nowarn
	 */
	public Class getSafeTypeClass()
	{
		if (typeClass == null)
		{
			if (typeClassName != null)
			{
				typeClass = ReflectUtil.loadClass(typeClassName);
			}
		}

		return typeClass;
	}

	/**
	 * Gets the type class.
	 * @nowarn
	 */
	public Class getTypeClass()
	{
		return typeClass;
	}

	/**
	 * Sets the type class.
	 * @nowarn
	 */
	public void setTypeClass(Class typeClass)
	{
		this.typeClass = typeClass;
	}
}
