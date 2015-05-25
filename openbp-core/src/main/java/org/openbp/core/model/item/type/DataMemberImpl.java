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

import java.util.Iterator;
import java.util.List;

import org.openbp.common.generic.description.DescriptionObjectImpl;
import org.openbp.common.generic.propertybrowser.CollectionTypes;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelObjectImpl;
import org.openbp.core.model.ModelObjectSymbolNames;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemTypes;

/**
 * OpenBP data type member definition object.
 * A member definition object (or short memberdef) describes an member of a OpenBP data type ({@link DataTypeItem}).
 *
 * The name of the member must follow Java naming conventions and must begin with an uppercase letter.
 *
 * Actually, DataMember would not have to implement DisplayObject, DescriptionObject would do because
 * data type members usually don't need to be displayed in an editor workspace in a special manner.<br>
 * However, we use the display name for form generation and error message output when the automatic
 * form validation fails.<br>
 * Note that DescriptionObject/DisplayObject currently do not support localization. This should be added soon.
 *
 * A data member can have either a simple type or a complex type. Simple types represent actual values
 * whereas complex types always represent references to complex objects.
 *
 * <h3>References</h3>
 *
 * A member can optionally refer to another object.
 * There are two types of references:<br>
 * <ul>
 * <li>
 * A link reference references exactly one object (or no object if the link is empty).<br>
 * This implements a 1:0..1 relation.<br>
 * The type of the member that holds the reference is always a complex data type. The type
 * either matches the data type of the object to be referenced or is a base class of the
 * referenced object.
 * </li>
 * <li>
 * A collection reference references a set of objects (or no object if the collection is empty).
 * This implements a 1:0..n relation.<br>
 * The member that holds the references must be defined as collection member.
 * The member type must always be a complex data type. The type either matches the data type
 * of the objects to be referenced or is a base class of the referenced objects.
 * </li>
 * </ul>
 *
 * <h3>Database reference processing</h3>
 *
 * References can be automatically resolved by the system when an object is read from the data base.<br>
 * However, you must define how the objects are related.<br>
 * Suppose we have the following two data types defined, which map to database tables:
 *
 * @code 3
 *
 * Type Company
 * Database Table COMPANY
 * Member CompanyName
 * - Type: String
 * - Database Field: CO_NAME
 * - Database Type: VARCHAR
 * Member CompanyId
 * - Type: Integer
 * - Database Field: CO_ID
 * - Database Type: INTEGER
 *
 * Type Employee
 * Database Table EMPLOYEE
 * Member FullName
 * - Type: String
 * - Database Field: EMP_FULL_NAME
 * - Database Type: VARCHAR
 * Member EmployeeId
 * - Type: Integer
 * - Database Field: EMP_ID
 * - Database Type: INTEGER
 * Member EmployingCompanyId
 * - Type: Integer
 * - Database Field: EMP_CO_ID
 * - Database Type: INTEGER
 *
 * @code
 *
 * A company can have an arbitrary number of employees. We have a 1:0..n relationsship between the Company
 * and the Employee type. If you select a company, you also automatically want the employees of the company
 * to be selected and linked with the selected company.<br>
 * In order to achieve this behavior, you would extend the Company datatype by adding an additional collection
 * field and defining the relation to the objects it should contain:
 *
 * @code 3
 *
 * Type Company
 * ...
 * Member Employees
 * - Type Employee
 * - Reference Member: CompanyId
 * - Foreign Member: Employee.EmployingCompanyId
 * - Autoload Count: -1
 *
 * @code
 *
 * When a company is retrieved from the database, an additional select operation will be executed using the
 * following statement:<br>
 * @code 3
 * select * from Employee where EMP_CO_ID = $CompanyId
 * @code
 * You may also specify an order clause for the reference, which will be considered when selecting the
 * referenced entities.
 *
 * The results of this select operation will be stored in the Employees collection member.
 * You may access the collection for example in a visual as loop variable (e. g. for Velocity
 * "$Company.Employees") in order to display all records. A good idea is to copy the code for the
 * record display from a generated browser form (in our example the Employee Browser).
 *
 * Note that only the List, Vector, Array and Iterator collection types are supported for database operations.
 *
 * Note that references are supported for select operations only. Insert, update and delete operations
 * do not account for references (however, this may depend on the underlying database layer in the future).
 *
 * \i\bTip:\i\b
 *
 * You might want the Company object retrieve its employees only in certain situations (e. g. for regular
 * company maintenance it is not required to have all the employees retrieved). However, in those cases
 * where you need the employees, you want them retrieved automatically.<br>
 * To solve this problem, define a regular Company data type w/o the collection member. In addition,
 * define an Object CompanyWithEmployees that has the Company type as its base type (i. e. extends Company)
 * and defines the collection member.
 *
 * @author Heiko Erhardt
 */
public class DataMemberImpl extends ModelObjectImpl
	implements DataMember
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Package containing ui component settings objects */
	public static final String UI_COMPONENT_PACKAGE = "org.openbp.core.model.item.visual.ui";

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	// General properties

	/** Name of the data type of the member */
	private String typeName;

	/** Collection type specification */
	private String collectionType;

	/** Required member flag (not null) */
	private boolean required;

	/** Maximum length of the field data */
	private int length;

	/** Precision of the field data */
	private int precision;

	/** Display format for numeric and date types */
	private String format;

	/** Default value */
	private String defaultValue;

	/** Primary key flag */
	private boolean primaryKey;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Data type this member refers to */
	private transient DataTypeItem dataType;

	/** Data type the member belongs to */
	private transient ComplexTypeItem parentDataType;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

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

		DataMemberImpl src = (DataMemberImpl) source;

		typeName = src.typeName;
		collectionType = src.collectionType;
		length = src.length;
		precision = src.precision;
		required = src.required;
		format = src.format;
		defaultValue = src.defaultValue;

		primaryKey = src.primaryKey;

		dataType = src.dataType;
		parentDataType = src.parentDataType;
	}

	//////////////////////////////////////////////////
	// @@ ModelObject implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the model the item belongs to.
	 * @nowarn
	 */
	public Model getOwningModel()
	{
		return parentDataType != null ? parentDataType.getOwningModel() : null;
	}

	/**
	 * Gets the name of the standard icon of this object.
	 * The icon name can be used by the client-side IconModel to retrieve an icon for the object.
	 *
	 * @return The icon name equals the icon name of a {@link DataTypeItem}
	 */
	public String getModelObjectSymbolName()
	{
		return ModelObjectSymbolNames.TYPE_ITEM;
	}

	/**
	 * Gets text that can be used to display this object.
	 * @nowarn
	 */
	public String getDisplayText()
	{
		String n = getDisplayName();
		if (n == null)
			n = getName();
		if (isPrimaryKey())
			n += " *";
		return n;
	}

	/**
	 * Gets text that can be used to describe this object.
	 * By default, this is the description text as returned by the {@link DescriptionObjectImpl#getDescription} method.
	 * @nowarn
	 */
	public String getDescriptionText()
	{
		return buildDescriptionText(getDescription());
	}

	/**
	 * Gets text that can be used to describe this object for a particular locale.
	 * By default, this is the description text as returned by the {@link DescriptionObjectImpl#getDescription} method.
	 * @nowarn
	 */
	public String buildDescriptionText(String originalDescription)
	{
		String text = null;

		if (dataType != null)
		{
			if (collectionType != null)
			{
				text = collectionType + " of " + dataType.getName();
			}
			else
			{
				text = dataType.getName();
			}
		}

		if (originalDescription != null)
		{
			if (text != null)
			{
				text += " - ";
				text += originalDescription;
			}
			else
			{
				text = originalDescription;
			}
		}

		return text;
	}

	/**
	 * Gets the container object (i. e. the parent) of this object.
	 *
	 * @return The container object or null if this object doesn't have a container.
	 * If the parent of this object references only a single object of this type,
	 * the method returns null.
	 */
	public ModelObject getContainer()
	{
		return parentDataType;
	}

	/**
	 * Gets an iterator of the children of the container this object belongs to.
	 * This can be used to check on name clashes between objects of this type.
	 * By default, the method returns null.
	 *
	 * @return The iterator if this object is part of a collection or a map.
	 * If the parent of this object references only a single object of this type,
	 * the method returns null.
	 */
	public Iterator getContainerIterator()
	{
		return parentDataType.getMembers();
	}

	/**
	 * Gets the children (direct subordinates) of this object.
	 * The method returns the members of the data type of this member.
	 *
	 * @return The list of objects or null if the object does not have children
	 */
	public List getChildren()
	{
		if (dataType != null)
			return dataType.getChildren();

		return null;
	}

	/**
	 * Gets the reference to the object.
	 * @return The qualified name
	 */
	public ModelQualifier getQualifier()
	{
		return new ModelQualifier(parentDataType, getName());
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

		if (parentDataType != null)
		{
			if ((flag & RESOLVE_GLOBAL_REFS) != 0)
			{
				// Resolve the base type
				try
				{
					dataType = (DataTypeItem) parentDataType.resolveItemRef(typeName, ItemTypes.TYPE);
				}
				catch (ModelException e)
				{
					getModelMgr().getMsgContainer().addMsg(this, "Cannot resolve member type $0.", new Object[]
					{
						typeName, e
					});
				}
			}

			if ((flag & SYNC_GLOBAL_REFNAMES) != 0)
			{
				// Resolve the base type
				if (dataType != null)
				{
					typeName = parentDataType.determineItemRef(dataType);
				}
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

		if (typeName == null)
		{
			getModelMgr().getMsgContainer().addMsg(this, "No member type specified for this data member.");
			success = false;
		}

		return success;
	}

	//////////////////////////////////////////////////
	// @@ General property access
	//////////////////////////////////////////////////

	/**
	 * Gets the data type name of the member.
	 * @return The type name (may not be null)
	 */
	public String getTypeName()
	{
		return typeName;
	}

	/**
	 * Sets the data type name of the member.
	 * @param typeName The type name (may not be null)
	 */
	public void setTypeName(String typeName)
	{
		this.typeName = typeName;
	}

	/**
	 * Gets the data type this member refers if no primitive type.
	 * @nowarn
	 */
	public DataTypeItem getDataType()
	{
		return dataType;
	}

	/**
	 * Sets the data type this member refers if no primitive type.
	 * @nowarn
	 */
	public void setDataType(DataTypeItem dataType)
	{
		this.dataType = dataType;
	}

	/**
	 * Gets the collection type.
	 * @return The collection type if the member is a collection or null otherwise.
	 * See the {@link CollectionTypes} class for a list of supported collection classes.
	 */
	public String getCollectionType()
	{
		return collectionType;
	}

	/**
	 * Sets the collection type.
	 * @param collectionType The collection type if the member is a collection or null otherwise.
	 * See the {@link CollectionTypes} class for a list of supported collection classes.
	 */
	public void setCollectionType(String collectionType)
	{
		this.collectionType = collectionType;
	}

	/**
	 * Gets the required field flag.
	 * @return
	 *		true	if the field needs to have a value (not null)<br>
	 *		false	if the field can also be null.
	 */
	public boolean isRequired()
	{
		return required;
	}

	/**
	 * Determines if the required field flag is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasRequired()
	{
		return required;
	}

	/**
	 * Sets the required field flag.
	 * @param required
	 *		true	if the field needs to have a value (not null)<br>
	 *		false	if the field can also be null.
	 */
	public void setRequired(boolean required)
	{
		this.required = required;
	}

	/**
	 * Gets the maximum length of the field data.
	 * @return
	 *		Strings: Max. length of the string; 0: No limitation<br>
	 *		Numeric types: Max. number of digits including precision and decimal separator<br>
	 *		Other types: 0
	 */
	public int getLength()
	{
		return length;
	}

	/**
	 * Determines if the maximum length of the field data is set.
	 * (for Castor serialization to keep the XML file small)
	 * @nowarn
	 */
	public boolean hasLength()
	{
		return length > 0;
	}

	/**
	 * Sets the maximum length of the field data.
	 * @param length
	 *		Strings: Max. length of the string; 0: No limitation<br>
	 *		Numeric types: Max. number of digits including precision and decimal separator<br>
	 *		Other types: 0
	 */
	public void setLength(int length)
	{
		this.length = length;
	}

	/**
	 * Gets the precision of the field data.
	 * @return
	 *		Numeric types: Precision of the number<br>
	 *		Other types: 0.
	 */
	public int getPrecision()
	{
		return precision;
	}

	/**
	 * Determines if the maximum precision of the field data is set.
	 * (for Castor serialization to keep the XML file small)
	 * @nowarn
	 */
	public boolean hasPrecision()
	{
		return precision > 0;
	}

	/**
	 * Sets the precision of the field dataNumeric types: Precision of the numberOther types: 0.
	 * @param precision
	 *		Numeric types: Precision of the number<br>
	 *		Other types: 0.
	 */
	public void setPrecision(int precision)
	{
		this.precision = precision;
	}

	/**
	 * Gets the display format for numeric and date types.
	 * This can also be custom format information for complex types.
	 * @nowarn
	 */
	public String getFormat()
	{
		return format;
	}

	/**
	 * Sets the display format for numeric and date types.
	 * This can also be custom format information for complex types.
	 * @nowarn
	 */
	public void setFormat(String format)
	{
		this.format = format;
	}

	/**
	 * Gets the default value.
	 * The default value will be assigned to the data member on construction and after
	 * a form reset.
	 * @return The default value or null to use the system's default behavior
	 * (Objects: null, numbers: 0, booleans: false)
	 */
	public String getDefaultValue()
	{
		return defaultValue;
	}

	/**
	 * Sets the default value.
	 * The default value will be assigned to the data member on construction and after
	 * a form reset.
	 * @param defaultValue The default value or null to use the system's default behavior
	 * (Objects: null, numbers: 0, booleans: false)
	 */
	public void setDefaultValue(String defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	/**
	 * Gets the primary key flag.
	 * Indicates if the field is a part of the primary key of the database table.
	 * The primary key can span multiple fields, however.
	 * @nowarn
	 */
	public boolean isPrimaryKey()
	{
		return primaryKey;
	}

	/**
	 * Determines if the primary key flag is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasPrimaryKey()
	{
		return primaryKey;
	}

	/**
	 * Sets the primary key flag.
	 * Indicates if the field is a part of the primary key of the database table.
	 * The primary key can span multiple fields, however.
	 * @nowarn
	 */
	public void setPrimaryKey(boolean primaryKey)
	{
		this.primaryKey = primaryKey;
	}

	//////////////////////////////////////////////////
	// @@ Linked object property access
	//////////////////////////////////////////////////

	/**
	 * Gets the data type the member belongs to.
	 * @nowarn
	 */
	public ComplexTypeItem getParentDataType()
	{
		return parentDataType;
	}

	/**
	 * Sets the data type the member belongs to.
	 * @nowarn
	 */
	public void setParentDataType(ComplexTypeItem parentDataType)
	{
		this.parentDataType = parentDataType;
	}
}
