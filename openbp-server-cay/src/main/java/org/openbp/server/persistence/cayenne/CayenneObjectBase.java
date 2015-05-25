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
package org.openbp.server.persistence.cayenne;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.DataObjectUtils;
import org.apache.cayenne.validation.ValidationResult;
import org.openbp.common.CollectionUtil;
import org.openbp.common.CommonUtil;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.util.CheckedIdGenerator;
import org.openbp.common.util.ToStringHelper;
import org.openbp.server.persistence.PersistentObject;

// }}*Custom imports*

/**
 * Cayenne-enabled implementation of the WorkflowTask interface.
 * Objects to be persisted by Cayenne must implement the Cayenne DataObject interface,
 * which involves operations that may change from Cayenne version to Cayenne version.
 * So this class extends the CayenneDataObject class (why the hell does Java not support multiple inheritance?).
 * The functionality of the class itself is achieved by copying the code from the WorkflowTaskImpl class :-(.
 */
public abstract class CayenneObjectBase extends CayenneDataObject
	implements PersistentObject
{
	/** Constant value serial version UID */
	private static final long serialVersionUID = 1L;

	/** Primary key */
	private Serializable id;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public CayenneObjectBase()
	{
	}

	/**
	 * This method is just defined for compatibility to DisplayObject.
	 * Do not call!
	 * @return The base method return value
	 */
	public Object clone()
		throws CloneNotSupportedException
	{
		return super.clone();
	}

	public String toString()
	{
		return ToStringHelper.toString(this, "id");
	}

	//////////////////////////////////////////////////
	// @@ Lifecycle support
	//////////////////////////////////////////////////

	/*
	 * @seem PersistentObject.onCreate
	 */
	public void onCreate()
	{
		createId();
	}

	/*
	 * @seem PersistentObject.onLoad
	 */
	public void onLoad()
	{
		// After fetch, retrieve all values from the delegate and store them into the this object's members.
		readValuesFromCayenne();
	}

	/*
	 * @seem PersistentObject.beforeSave
	 */
	public void beforeSave()
	{
		// Before persisting the data, copy the object's members to Cayenne's value map.
		writeValuesToCayenne();
	}

	/*
	 * @seem PersistentObject.afterSave
	 */
	public void afterSave()
	{
	}

	/**
	 * @seem PersistentObject.beforeDelete
	 */
	public void beforeDelete()
	{
	}

	/**
	 * @seem PersistentObject.afterDelete
	 */
	public void afterDelete()
	{
	}

	//////////////////////////////////////////////////
	// @@ Copied from PersistentObjectBase
	//////////////////////////////////////////////////

	/**
	 * Creates a new UUID as primary key of this object.
	 */
	public void createId()
	{
		Object id = CheckedIdGenerator.getInstance().getID();
		writeChangedProperty("id", id);
	}

	/**
	 * Gets the primary key.
	 * @nowarn
	 */
	public Serializable getId()
	{
		determineId();
		return id;
	}

	/**
	 * Gets the object version for versioned data check (for concurrent modification check by o/r mappers).
	 * @nowarn
	 */
	public Integer getVersion()
	{
		return (Integer) readProperty("version");
	}

	/**
	 * Sets the object version for versioned data check (for concurrent modification check by o/r mappers).
	 * @nowarn
	 */
	public void setVersion(final Integer version)
	{
		writeChangedProperty("version", version);
	}

	//////////////////////////////////////////////////
	// @@ Cayenne-specific functionality
	//////////////////////////////////////////////////

	public void validateForSave(final ValidationResult validationResult)
	{
		// Trigger the PersistentObject template method
		beforeSave();
		super.validateForSave(validationResult);
	}


	/**
	 * Copies the values from the Cayenne value map to the members of this class.
	 */
	protected abstract void readValuesFromCayenne();

	/**
	 * Copies the values from  the members of this class to the Cayenne value map.
	 */
	protected abstract void writeValuesToCayenne();

	//////////////////////////////////////////////////
	// @@ Utilities
	//////////////////////////////////////////////////

	/**
	 * Version of the "writeProperty" method that will update changed properties only.
	 * Compares the new and the old property value.
	 *
	 * @param propName Property name
	 * @param value New attribute value
	 */
	protected void writeChangedProperty(final String propName, final Object value)
	{
		try
		{
			Object oldValue = readProperty(propName);
			if (! CommonUtil.equalsNull(value, oldValue))
			{
				writeProperty(propName, value);
			}
		}
		catch (Exception e)
		{
			LogUtil.error(getClass(), "Error in Cayenne", e);
		}
	}

	/**
	 * Version of the "SetToOneTarget" method that will update changed relations only.
	 * Compares the new and the old property value.
	 *
	 * @param propName Relation name
	 * @param target New relation target
	 */
	protected void setChangedToOneTarget(final String propName, final DataObject target)
	{
		DataObject oldTarget = (DataObject) readProperty(propName);
		if (target != oldTarget)
		{
			setToOneTarget(propName, target, true);
		}
	}

	/**
	 * Version of the "SetToOneTarget" method that will update changed relations only.
	 * Compares the new and the old property value.
	 *
	 * @param propName Relation name
	 * @param target New relation target
	 */
	protected void addChangedToManyTarget(final String propName, final DataObject target)
	{
		List oldTargets = (List) readProperty(propName);
		for (Iterator it = CollectionUtil.iterator(oldTargets); it.hasNext();)
		{
			DataObject oldTarget = (DataObject) it.next();
			if (target == oldTarget)
				return;
		}
		addToManyTarget(propName, target, true);
	}

	/**
	 * Simple convenience integer object conversion.
	 *
	 * @param value Integer object
	 * @return Integer value
	 */
	protected int toInt(final Object value)
	{
		if (value != null)
			return ((Integer) value).intValue();
		return 0;
	}

	/**
	 * Simple convenience boolean object conversion.
	 *
	 * @param value Boolean object
	 * @return Boolean value
	 */
	protected boolean toBoolean(final Object value)
	{
		if (value != null)
			return ((Boolean) value).booleanValue();
		return false;
	}

	/**
	 * Tries to determines the id (primary key) of the object.
	 */
	protected void determineId()
	{
		if (id == null)
		{
			Object value;
			try
			{
				value = DataObjectUtils.pkForObject(this);
				if (value instanceof Number)
				{
					id = new Long(((Number) value).longValue());
				}
				else
				{
					id = (Serializable) value;
				}
			}
			catch (Exception e)
			{
				// Ignore, maybe the object is not flushed to the database yet.
				// In this case, we simply give up.
			}
		}
	}
}
