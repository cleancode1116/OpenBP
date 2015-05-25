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
package org.openbp.server.persistence;

import java.io.Serializable;

import org.openbp.common.util.CheckedIdGenerator;
import org.openbp.common.util.ToStringHelper;

/**
 * Base class for all engine objects that can be persisted to a persistence store.
 *
 * @author Heiko Erhardt
 */
public class PersistentObjectBase
	implements PersistentObject
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Primary key */
	private Serializable id;

	/** Object version for versioned data check (for concurrent modification check by o/r mappers) */
	private Integer version;

	/** Cashed hash code */
	private transient int hashCodeCache = Integer.MIN_VALUE;

	/** Constant value serial version UID */
	private static final long serialVersionUID = 1L;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public PersistentObjectBase()
	{
	}

	/**
	 * Creates a new UUID as primary key of this object.
	 */
	public void createId()
	{
		setId(CheckedIdGenerator.getInstance().getID());
	}

	//////////////////////////////////////////////////
	// @@ Lifecycle support
	//////////////////////////////////////////////////

	/**
	 * Template method that is called when the object has been created.
	 * The default implementation does nothing.
	 */
	public void onCreate()
	{
		createId();
	}

	/**
	 * Template method that is called after the object has been fetched.
	 * The default implementation does nothing.
	 */
	public void onLoad()
	{
	}

	/**
	 * Template method that is called before the object is inserted or updated.
	 * The default implementation does nothing.
	 */
	public void beforeSave()
	{
	}

	/**
	 * Template method that is called after the object has been inserted or updated.
	 * The default implementation does nothing.
	 */
	public void afterSave()
	{
	}

	/**
	 * Template method that is called before the object is deleted.
	 * The default implementation does nothing.
	 */
	public void beforeDelete()
	{
	}

	/**
	 * Template method that is called after the object has been deleted.
	 * The default implementation does nothing.
	 */
	public void afterDelete()
	{
	}

	//////////////////////////////////////////////////
	// @@ Primary key
	//////////////////////////////////////////////////

	/**
	 * Gets the primary key.
	 * @nowarn
	 */
	public Serializable getId()
	{
		return id;
	}

	/**
	 * Sets the primary key.
	 * @nowarn
	 */
	public void setId(final Serializable id)
	{
		this.id = id;
		hashCodeCache = Integer.MIN_VALUE;
	}

	/**
	 * Gets the object version for versioned data check (for concurrent modification check by o/r mappers).
	 * @nowarn
	 */
	public Integer getVersion()
	{
		return version;
	}

	/**
	 * Sets the object version for versioned data check (for concurrent modification check by o/r mappers).
	 * @nowarn
	 */
	public void setVersion(final Integer version)
	{
		this.version = version;
	}

	//////////////////////////////////////////////////
	// @@ Stanard java.lang.Object method overrides
	//////////////////////////////////////////////////

	/**
	 * Object equality check.
	 * @param other Object to compare to
	 * @return This object is considered equal to the given object if both object have ids assigned
	 * and the ids are equal.
	 */
	public boolean equals(final Object other)
	{
		if (! other.getClass().equals(getClass()))
			return false;

		Object otherId = ((PersistentObjectBase) other).getId();
		Object thisId = getId();

		if (otherId != null && thisId != null)
			return thisId.equals(otherId);

		return super.equals(other);
	}

	/**
	 * Hash code computation.
	 * @return A hash code based on the class name and the id if present, otherwise the value of Object.hashCode.
	 */
	public int hashCode()
	{
		if (hashCodeCache == Integer.MIN_VALUE)
		{
			if (getId() == null)
				return super.hashCode();
			hashCodeCache = getId().hashCode();
		}
		return hashCodeCache;
	}

	/**
	 * Compares this object to the given object.
	 * @param obj Object to compare to
	 * @return The comparison value based on hash code comparisons
	 */
	public int compareTo(final Object obj)
	{
		if (obj.hashCode() > hashCode())
			return 1;
		else if (obj.hashCode() < hashCode())
			return - 1;
		else
			return 0;
	}

	/**
	 * Returns a simple string representation of this object
	 * @return Class name w/o package and the object id
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "id");
	}
}
