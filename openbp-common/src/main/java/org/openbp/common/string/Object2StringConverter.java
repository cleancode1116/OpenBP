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
package org.openbp.common.string;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converter which converts arbitray object sets (usually things like integer enumerations) to string values and vice versa.
 * Usually used for serialization/deserialization of integer values to more meaningful strings.
 *
 * @author Heiko Erhardt
 */
public class Object2StringConverter
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Value to string mapping table */
	private Map value2Str = new LinkedHashMap();

	/** String to value mapping table */
	private Map str2Value = new LinkedHashMap();

	/** Display names of the values */
	private ArrayList displayNames = new ArrayList();

	/** Default value */
	private Object defaultValue;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public Object2StringConverter()
	{
	}

	//////////////////////////////////////////////////
	// @@ Generic methods
	//////////////////////////////////////////////////

	/**
	 * Adds a value to the converter.
	 *
	 * @param value Value to add
	 * @param name Corresponding string value
	 * @param displayName Display name
	 */
	public void addValue(Object value, String name, String displayName)
	{
		value2Str.put(value, name);
		str2Value.put(name, value);
		displayNames.add(displayName);
	}

	/**
	 * Converts a string to its corresponding value.
	 *
	 * @param str String to convert
	 * @return The corresponding value or the default value if the string is null
	 * or if it cannot be found in the mapping table.
	 */
	public Object str2Value(String str)
	{
		Object ret = defaultValue;

		if (str != null)
		{
			Object o = str2Value.get(str);
			if (o != null)
			{
				ret = o;
			}
		}

		return ret;
	}

	/**
	 * Converts an integer value to its corresponding string value.
	 *
	 * @param value Value to convert
	 * @return The corresponding string value or null if the value equals the default value
	 * or if it cannot be found in the mapping table.
	 */
	public String value2Str(Object value)
	{
		String ret = null;

		if (value != null && (defaultValue == null || !defaultValue.equals(value)))
		{
			ret = (String) value2Str.get(value);
		}

		return ret;
	}

	/**
	 * Checks if the supplied value is valid.
	 *
	 * @param value Value to check
	 * @return
	 *		true	The value is either null or a valid value.<br>
	 *		false	The value is unknown
	 */
	public boolean checkValue(Object value)
	{
		if (value != null)
		{
			if (value2Str.get(value) == null)
				return false;
		}

		return true;
	}

	/**
	 * Gets the values.
	 * @return An iterator of objects.
	 * The objects are returned in the order they have been added.
	 */
	public Iterator getValues()
	{
		return value2Str.keySet().iterator();
	}

	/**
	 * Gets the string.
	 * @return An iterator of String objects.
	 * The objects are returned in the order they have been added.
	 */
	public Iterator getStrings()
	{
		return str2Value.keySet().iterator();
	}

	/**
	 * Get the display names.
	 * @return An iterator of String objects.
	 * The objects are returned in the order they have been added.
	 */
	public Iterator getDisplayNames()
	{
		return displayNames.iterator();
	}

	/**
	 * Gets the default value.
	 * @nowarn
	 */
	public Object getDefaultValue()
	{
		return defaultValue;
	}

	/**
	 * Sets the default value.
	 * @nowarn
	 */
	public void setDefaultValue(Object defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	//////////////////////////////////////////////////
	// @@ Integer convenience methods
	//////////////////////////////////////////////////

	/**
	 * Adds an integer value to the converter.
	 *
	 * @param value Value to add
	 * @param name Corresponding string value
	 * @param displayName Display name
	 */
	public void addIntValue(int value, String name, String displayName)
	{
		addValue(Integer.valueOf(value), name, displayName);
	}

	/**
	 * Converts a string value to its corresponding integer value.
	 *
	 * @param str String value to convert
	 * @return The corresponding integer value or the default value if the string is null
	 * or if it cannot be found in the mapping table.
	 */
	public int str2IntValue(String str)
	{
		Object o = str2Value(str);
		if (o instanceof Integer)
			return ((Integer) o).intValue();
		if (defaultValue instanceof Integer)
			return ((Integer) defaultValue).intValue();
		return -1;
	}

	/**
	 * Converts an integer value to its corresponding string value.
	 *
	 * @param value Value to convert
	 * @return The corresponding string value or null if the value equals the default value
	 * or if it cannot be found in the mapping table.
	 */
	public String intValue2Str(int value)
	{
		return value2Str(Integer.valueOf(value));
	}

	/**
	 * Checks if the supplied value is valid.
	 *
	 * @param value Value to check
	 * @return
	 *		true	The value is either null or a valid value.<br>
	 *		false	The value is unknown
	 */
	public boolean checkIntValue(int value)
	{
		return value == -1 || checkValue(Integer.valueOf(value));
	}
}
