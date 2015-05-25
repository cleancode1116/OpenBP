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
package org.openbp.jaspira.propertybrowser.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openbp.common.string.StringUtil;
import org.openbp.common.string.parser.StringParser;
import org.openbp.common.util.iterator.EmptyIterator;

/**
 * A String parser that parses the parameter element value obtained from the editor property file.
 * The parameters are sorted into their different identifiers through which they
 * can be accessed as a key.
 *
 * @author Erich Lauterbach
 */
public class EditorParameterParser
{
	//////////////////////////////////////////////////
	// @@ Protected members
	//////////////////////////////////////////////////

	/** Property editor that owns the parameters */
	private PropertyEditor editor;

	/** The Map containing the array lists of values for each parameter identifier. */
	private Map map;

	//////////////////////////////////////////////////
	// @@ Constructors
	//////////////////////////////////////////////////

	/**
	 * Default Constructor
	 *
	 * @param editor Property editor that owns the parameters
	 */
	public EditorParameterParser(PropertyEditor editor)
	{
		this.editor = editor;

		map = new HashMap();

		parse(editor.getParams());
	}

	//////////////////////////////////////////////////
	// @@ Publicly accessible methods
	//////////////////////////////////////////////////

	/**
	 * Gets the given parameter as an iterator of string values.
	 * Use this method if there may be more than 1 parameter.
	 *
	 * @param identifier The parameter identifier
	 * @return The iterator (may be empty)
	 */
	public Iterator get(String identifier)
	{
		List values = (List) map.get(identifier);

		if (values != null)
			return values.iterator();

		return EmptyIterator.getInstance();
	}

	/**
	 * Gets the given parameter as string value.
	 * Use this method if there may be more only 1 parameter of this type.
	 * If several such parameters have been given, the first one will be returned.
	 *
	 * @param identifier The parameter identifier
	 * @return Parameter string or null if no such parameter has been given
	 */
	public String getString(String identifier)
	{
		List values = (List) map.get(identifier);

		if (values != null && values.size() > 0)
			return values.get(0).toString();

		return null;
	}

	/**
	 * Used to get the display value from a parameter string that contains more than
	 * one value, i.e. the parameter holds two value seperated by a pipe '|';
	 *
	 * @param parameterValue The parameter value
	 * @return The display value (can be null)
	 */
	public static String determineDisplayValue(String parameterValue)
	{
		String s;
		int index = parameterValue.indexOf('|');
		if (index < 0)
			s = parameterValue;
		else
			s = parameterValue.substring(0, index);
		return StringUtil.trimNull(s);
	}

	/**
	 * Used to get the value from a parameter string that contains more than
	 * one value, i.e. the parameter holds two value seperated by a pipe '|';
	 *
	 * @param parameterValue The parameter value
	 * @return The internal value (can be null)
	 */
	public static String determineInternalValue(String parameterValue)
	{
		String s;
		int index = parameterValue.indexOf('|');
		if (index < 0)
			s = parameterValue;
		else
			s = parameterValue.substring(index + 1);
		return StringUtil.trimNull(s);
	}

	/**
	 * Gets the number of values contained for an identifier.
	 *
	 * @param identifier The parameter identifier
	 * @return The number of values maped to the specified identifier or -1 if the
	 * identifier does not exist.
	 */
	public int numberOfEntries(String identifier)
	{
		List values = (List) map.get(identifier);

		if (values != null)
			return values.size();

		return -1;
	}

	//////////////////////////////////////////////////
	// @@ Protected methods
	//////////////////////////////////////////////////

	/**
	 * Adds a value with its corresponding identifier to the map. If specified identifier
	 * has not been added to the map before, then a new ArrayList containing the value is
	 * created and added to the map with the new identifier, else the value is added to the
	 * array list maped to the identifier.
	 *
	 * @param identifier The identifier for the value
	 * @param value The value to be added to the map
	 */
	protected void add(String identifier, String value)
	{
		List values = (List) map.get(identifier);
		if (values == null)
		{
			values = new ArrayList();
			map.put(identifier, values);
		}
		values.add(value);
	}

	/**
	 * Parses the editor parameter string to populate the map with the values
	 * for each parameter identifier.
	 *
	 * @param parameters The editor parameter string to be parsed
	 */
	protected void parse(String parameters)
	{
		if (parameters == null)
			return;

		StringParser sp = new StringParser(parameters);

		sp.skipSpace();
		while (!sp.isEnd())
		{
			// Get the identifier and it's corresponding value.
			String identifier = sp.getIdentifier();
			if (identifier == null)
			{
				System.err.println("Property '" + editor.getPropertyName() + "': Missing property editor parameter identifier");
				return;
			}
			sp.skipSpace();

			String value = null;
			if (sp.getChar() == '=')
			{
				sp.nextChar();
				value = sp.getString('\0');
				if (value == null)
				{
					System.err.println("Property '" + editor.getPropertyName() + "': Invalid string constant");
				}
				sp.skipSpace();
			}

			// Add the identifier and value to the map.
			add(identifier, value);
		}
	}
}
