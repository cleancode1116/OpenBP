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

/**
 * Primitive type that denots a string.
 *
 * @author Heiko Erhardt
 */
public class SimpleTypeItemBooleanImpl extends SimpleTypeItemStringImpl
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Possible string representations of the boolean value 'true' */
	private static String [] trueValues = { "true", "t", "yes", "y", "j", "1", };

	/** Possible string representations of the boolean value 'false' */
	private static String [] falseValues = { "false", "f", "no", "n", "0", };

	/** Boolean 'true' object value */
	private Boolean trueObject = new Boolean(true);

	/** Boolean 'false' object value */
	private Boolean falseObject = new Boolean(false);

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public SimpleTypeItemBooleanImpl()
	{
	}

	//////////////////////////////////////////////////
	// @@ DataTypeItem overrides
	//////////////////////////////////////////////////

	/**
	 * Configures a data member of this type with default values.
	 */
	public void performDefaultDataMemberConfiguration(DataMember member)
	{
		member.setLength(0);
		member.setPrecision(0);
	}

	/**
	 * @copy DataTypeItem.convertFromString
	 */
	public Object convertFromString(String strValue, DataMember member, Locale locale)
		throws ValidationException
	{
		// Get the trimmed field value
		String s = (String) super.convertFromString(strValue, member, locale);

		if (s != null)
		{
			for (int i = 0; i < trueValues.length; ++i)
			{
				if (s.equalsIgnoreCase(trueValues [i]))
					return trueObject;
			}

			for (int i = 0; i < falseValues.length; ++i)
			{
				if (s.equalsIgnoreCase(falseValues [i]))
					return falseObject;
			}

			throw new ValidationException("'" + s + "' is not a boolean value");
		}

		return null;
	}
}
