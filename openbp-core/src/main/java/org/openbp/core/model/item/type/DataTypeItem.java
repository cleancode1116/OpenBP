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

import org.openbp.core.model.item.Item;

/**
 * An OpenBP data type.
 * This interface is implemented by both complex types ({@link ComplexTypeItem}) as well as primitive
 * types (SimpleTypeItem).
 *
 * @author Heiko Erhardt
 */
public interface DataTypeItem
	extends Item
{
	//////////////////////////////////////////////////
	// @@ String conversion
	//////////////////////////////////////////////////

	/**
	 * Tries to convert a string value to a valid instance of this data type.
	 * Used for example to convert a request parameter to a data value.
	 *
	 * @param strValue String value to parse
	 * @param member Data member definition if the value that should be parsed denotes
	 * a member of a {@link DataTypeItem} class or null if the value is to be seen as stand-alone data item.
	 * @param locale Current locale of the request or null if the locale is unknown
	 * @return Instance of this type constructed from the string value or null
	 * @throws ValidationException If the conversion failed.
	 * The exception contains a detailed error message.
	 */
	public Object convertFromString(String strValue, DataMember member, Locale locale)
		throws ValidationException;

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
	public String convertToString(Object value, DataMember member, Locale locale);

	/**
	 * Configures a data member of this type with default values.
	 * Does nothing by default.
	 *
	 * @param member The member to be configured
	 */
	public void performDefaultDataMemberConfiguration(DataMember member);

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
	public boolean isBaseTypeOf(DataTypeItem otherType);

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the name of the Java class of this type.
	 * @nowarn
	 */
	public String getClassName();

	/**
	 * Sets the name of the Java class of this type.
	 * @nowarn
	 */
	public void setClassName(String className);

	/**
	 * Gets the name of the base type of this type.
	 * The base type must be another valid typedef in the system.
	 * @nowarn
	 */
	public String getBaseTypeName();

	/**
	 * Sets the name of the base type of this type.
	 * The base type must be another valid typedef in the system.
	 * @nowarn
	 */
	public void setBaseTypeName(String baseTypeName);

	/**
	 * Gets the java class of this type.
	 * @nowarn
	 */
	public Class getJavaClass();

	/**
	 * Sets the java class of this type.
	 * @nowarn
	 */
	public void setJavaClass(Class javaClass);

	/**
	 * Gets the base type of this type.
	 * @nowarn
	 */
	public DataTypeItem getBaseType();

	/**
	 * Sets the base type of this type.
	 * @nowarn
	 */
	public void setBaseType(DataTypeItem baseType);

	/**
	 * Determines if this type is a simple type or a complex type.
	 * @nowarn
	 */
	public boolean isSimpleType();
}
