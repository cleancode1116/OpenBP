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
package org.openbp.core.model.item.type;

import java.util.Locale;

import org.openbp.core.CoreConstants;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemImpl;
import org.openbp.core.model.item.ItemTypes;

/**
 * An OpenBP data type.
 *
 * The name of the type must follow Java naming conventions and must begin with an uppercase letter.
 *
 * @author Heiko Erhardt
 */
public abstract class DataTypeItemImpl extends ItemImpl
	implements DataTypeItem
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Name of the Java class of this type */
	private String className;

	/** Name of the base type of this class */
	private String baseTypeName;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Java class of this type */
	private transient Class javaClass;

	/** Base type of this type */
	private transient DataTypeItem baseType;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public DataTypeItemImpl()
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

		DataTypeItemImpl src = (DataTypeItemImpl) source;

		className = src.className;
		baseTypeName = src.baseTypeName;

		javaClass = src.javaClass;
		baseType = src.baseType;
	}

	//////////////////////////////////////////////////
	// @@ Pre save/post load processing and validation
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.maintainReferences
	 */
	public void maintainReferences(int flag)
	{
		super.maintainReferences(flag);

		if ((flag & RESOLVE_GLOBAL_REFS) != 0)
		{
			try
			{
				baseType = (DataTypeItem) resolveItemRef(baseTypeName, ItemTypes.TYPE);
			}
			catch (ModelException e)
			{
				getModelMgr().getMsgContainer().addMsg(this, "Cannot resolve base type $0.", new Object[]
				{
					baseTypeName, e
				});
			}

			if (baseType != null)
			{
				// Make sure this base type does not lead to a type recursion.
				validateBaseTypeRecursion();
			}
		}

		if ((flag & SYNC_GLOBAL_REFNAMES) != 0)
		{
			if (baseType != null)
			{
				// We don't use determineItemRef to prevent recursions for the base type;
				// Instead we use the fully qualified name.
				baseTypeName = baseType != null ? baseType.getQualifier().toString() : null;
			}
		}
	}

	/**
	 * @copy ModelObject.validate
	 */
	public boolean validate(int flag)
	{
		// Check for an object name first
		boolean success = super.validate(flag);

		if (success)
		{
			Model systemModel = getModel().getModelMgr().getModelByQualifier(CoreConstants.SYSTEM_MODEL_QUALIFIER);

			if (! getModel().getQualifier().equals(systemModel.getQualifier()))
			{
				DataTypeItem systemType = (DataTypeItem) systemModel.getItem(getName(), ItemTypes.TYPE, false);
				if (systemType != null && systemType.isSimpleType())
				{
					getModelMgr().getMsgContainer().addMsg(this, "Type name conflicts with a type name of the System model.");
					success = false;
				}

				if (baseTypeName != null)
				{
					if (baseTypeName.equals(getName()))
					{
						getModelMgr()
							.getMsgContainer()
							.addMsg(
								this,
								"A type cannot have itself as base type. If you want to refer to a type from another model, use the fully qualified notation (e. g. \"/Model/Type\").");
						success = false;
					}

					systemType = (DataTypeItem) systemModel.getItem(baseTypeName, ItemTypes.TYPE, false);
					if (systemType != null && systemType.isSimpleType())
					{
						getModelMgr().getMsgContainer().addMsg(this, "Base type name $0 conflicts with a type name of the System model.",
							new Object[]
							{
								baseTypeName
							});
						success = false;
					}
				}
			}
		}

		return success;
	}

	/**
	 * Checks that the base type of this type does not lead to a type recursion.
	 * Logs any errors to the message container of this item.
	 */
	protected void validateBaseTypeRecursion()
	{
		ModelQualifier thisQualifier = getQualifier();

		for (DataTypeItem t = this; t != null;)
		{
			DataTypeItem bt = t.getBaseType();
			if (bt != null && bt.getQualifier().equals(thisQualifier))
			{
				getModelMgr()
					.getMsgContainer()
					.addMsg(
						this,
						"The base type hierarchy of this data type leads to an infinite recursion.\nUsing this base type when executing the model is likely to cause an error.\nPlease choose another base type.");
				break;
			}
			t = bt;
		}
	}

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

		if (className != null)
		{
			// Resolve the bean class
			try
			{
				// Use the model's class loader to load it
				javaClass = getModel().getClassLoader().loadClass(className);
			}
			catch (ClassNotFoundException e)
			{
				getModelMgr().getMsgContainer().addMsg(this, "Data type class $0 not found.", new Object[]
				{
					className
				});
			}
		}
		else
		{
			javaClass = null;
		}
	}

	/**
	 * Configures a data member of this type with default values.
	 * Does nothing by default.
	 */
	public void performDefaultDataMemberConfiguration(DataMember member)
	{
		member.setLength(0);
		member.setPrecision(0);
	}

	//////////////////////////////////////////////////
	// @@ String conversion
	//////////////////////////////////////////////////

	/**
	 * @copy DataTypeItem.convertFromString
	 */
	public Object convertFromString(String strValue, DataMember member, Locale locale)
		throws ValidationException
	{
		return strValue;
	}

	/**
	 * Converts an instance of this data type to its string representation.
	 * Used for example to generate a request parameter string value.
	 *
	 * @param value Object to print
	 * @param member Data member definition if the value that should be parsed denotes
	 * a member of a {@link DataTypeItem} class or null if the value is to be seen as stand-alone data item.
	 * @param locale Current locale of the request or null if the locale is unknown
	 * @return String value of the object value or null<br>
	 * The default implementation of this method could e. g. use the toString method of the object.
	 */
	public String convertToString(Object value, DataMember member, Locale locale)
	{
		return value != null ? value.toString() : null;
	}

	//////////////////////////////////////////////////
	// @@ Type hierarchy
	//////////////////////////////////////////////////

	/**
	 * Checks if this type is a base type of the given type.
	 *
	 * @param otherType Possible extended type
	 * @return
	 *		true	The given type extends this type.<br>
	 *		false	The types are not compatible.
	 */
	public boolean isBaseTypeOf(DataTypeItem otherType)
	{
		if (otherType != null)
		{
			for (DataTypeItem bt = otherType.getBaseType(); bt != null; bt = bt.getBaseType())
			{
				if (bt == this)
					return true;
			}
		}
		return false;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the name of the Java class of this type.
	 * @nowarn
	 */
	public String getClassName()
	{
		return className;
	}

	/**
	 * Sets the name of the Java class of this type.
	 * @nowarn
	 */
	public void setClassName(String className)
	{
		this.className = className;
	}

	/**
	 * Gets the name of the base type of this type.
	 * The base type must be another valid typedef in the system.
	 * @nowarn
	 */
	public String getBaseTypeName()
	{
		return baseTypeName;
	}

	/**
	 * Sets the name of the base type of this type.
	 * The base type must be another valid typedef in the system.
	 * @nowarn
	 */
	public void setBaseTypeName(String baseTypeName)
	{
		this.baseTypeName = baseTypeName;
	}

	/**
	 * Gets the java class of this type.
	 * @nowarn
	 */
	public Class getJavaClass()
	{
		return javaClass;
	}

	/**
	 * Sets the java class of this type.
	 * @nowarn
	 */
	public void setJavaClass(Class javaClass)
	{
		this.javaClass = javaClass;
	}

	/**
	 * Gets the base type of this type.
	 * @nowarn
	 */
	public DataTypeItem getBaseType()
	{
		return baseType;
	}

	/**
	 * Sets the base type of this type.
	 * @nowarn
	 */
	public void setBaseType(DataTypeItem baseType)
	{
		this.baseType = baseType;
	}
}
