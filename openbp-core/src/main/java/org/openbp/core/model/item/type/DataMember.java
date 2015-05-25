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

import org.openbp.common.generic.description.DisplayObject;
import org.openbp.core.model.ModelObject;

/**
 * OpenBP data type member definition object.
 * An member definition object (or short memberdef) describes a member of a OpenBP data type ({@link DataTypeItem}).
 *
 * The name of the member must follow Java naming conventions and must begin with an uppercase letter.
 *
 * Actually, DataMember would not have to implement DisplayObject, DescriptionObject would do because
 * data type members usually don't need to be displayed in an editor workspace in a special manner.<br>
 * However, we use the display name for form generation and error message output when the automatic
 * form validation fails.<br>
 * Note that DescriptionObject/DisplayObject currently do not support localization. This should be added soon.
 *
 * @author Heiko Erhardt
 */
public interface DataMember
	extends DisplayObject, ModelObject
{
	//////////////////////////////////////////////////
	// @@ General property access
	//////////////////////////////////////////////////

	/**
	 * Gets the data type name of the member.
	 * @return The type name (may not be null)
	 */
	public String getTypeName();

	/**
	 * Sets the data type name of the member.
	 * @param typeName The type name (may not be null)
	 */
	public void setTypeName(String typeName);

	/**
	 * Gets the data type this member refers if no primitive type.
	 * @nowarn
	 */
	public DataTypeItem getDataType();

	/**
	 * Sets the data type this member refers if no primitive type.
	 * @nowarn
	 */
	public void setDataType(DataTypeItem dataType);

	/**
	 * Gets the collection type.
	 * @return The collection type if the member is a collection or null otherwise.
	 * See the {@link org.openbp.common.generic.propertybrowser.CollectionTypes} class for a list of supported
	 * collection classes.
	 */
	public String getCollectionType();

	/**
	 * Sets the collection type.
	 * @param collectionType The collection type if the member is a collection or null otherwise.
	 * See the {@link org.openbp.common.generic.propertybrowser.CollectionTypes} class for a list of supported collection classes.
	 */
	public void setCollectionType(String collectionType);

	/**
	 * Gets the required field flag.
	 * @return
	 *		true	if the field needs to have a value (not null)<br>
	 *		false	if the field can also be null.
	 */
	public boolean isRequired();

	/**
	 * Sets the required field flag.
	 * @param required
	 *		true	if the field needs to have a value (not null)<br>
	 *		false	if the field can also be null.
	 */
	public void setRequired(boolean required);

	/**
	 * Gets the maximum length of the field data.
	 * @return
	 *		Strings: Max. length of the string; 0: No limitation<br>
	 *		Numeric types: Max. number of digits including precision and decimal separator<br>
	 *		Other types: 0
	 */
	public int getLength();

	/**
	 * Determines if the maximum length of the field data is set.
	 * (for Castor serialization to keep the XML file small)
	 * @nowarn
	 */
	public boolean hasLength();

	/**
	 * Sets the maximum length of the field data.
	 * @param length
	 *		Strings: Max. length of the string; 0: No limitation<br>
	 *		Numeric types: Max. number of digits including precision and decimal separator<br>
	 *		Other types: 0
	 */
	public void setLength(int length);

	/**
	 * Gets the precision of the field data.
	 * @return
	 *		Numeric types: Precision of the number<br>
	 *		Other types: 0.
	 */
	public int getPrecision();

	/**
	 * Determines if the maximum precision of the field data is set.
	 * (for Castor serialization to keep the XML file small)
	 * @nowarn
	 */
	public boolean hasPrecision();

	/**
	 * Sets the precision of the field dataNumeric types: Precision of the numberOther types: 0.
	 * @param precision
	 *		Numeric types: Precision of the number<br>
	 *		Other types: 0.
	 */
	public void setPrecision(int precision);

	/**
	 * Gets the display format for numeric and date types.
	 * This can also be custom format information for complex types.
	 * @nowarn
	 */
	public String getFormat();

	/**
	 * Sets the display format for numeric and date types.
	 * This can also be custom format information for complex types.
	 * @nowarn
	 */
	public void setFormat(String format);

	/**
	 * Gets the default value.
	 * The default value will be assigned to the data member on construction and after
	 * a form reset.
	 * @return The default value or null to use the system's default behavior
	 * (Objects: null, numbers: 0, booleans: false)
	 */
	public String getDefaultValue();

	/**
	 * Sets the default value.
	 * The default value will be assigned to the data member on construction and after
	 * a form reset.
	 * @param defaultValue The default value or null to use the system's default behavior
	 * (Objects: null, numbers: 0, booleans: false)
	 */
	public void setDefaultValue(String defaultValue);

	/**
	 * Gets the primary key flag.
	 * Indicates if the field is a part of the primary key of the database table.
	 * The primary key can span multiple fields, however.
	 * @nowarn
	 */
	public boolean isPrimaryKey();

	/**
	 * Sets the primary key flag.
	 * Indicates if the field is a part of the primary key of the database table.
	 * The primary key can span multiple fields, however.
	 * @nowarn
	 */
	public void setPrimaryKey(boolean primaryKey);

	//////////////////////////////////////////////////
	// @@ Linked object property access
	//////////////////////////////////////////////////

	/**
	 * Gets the data type the member belongs to.
	 * @nowarn
	 */
	public ComplexTypeItem getParentDataType();

	/**
	 * Sets the data type the member belongs to.
	 * @nowarn
	 */
	public void setParentDataType(ComplexTypeItem parentDataType);
}
