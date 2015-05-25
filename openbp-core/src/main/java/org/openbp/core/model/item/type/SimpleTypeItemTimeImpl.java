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

import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Locale;

/**
 * Primitive type that denots a string.
 *
 * @author Heiko Erhardt
 */
public class SimpleTypeItemTimeImpl extends SimpleTypeItemStringImpl
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public SimpleTypeItemTimeImpl()
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
			DateFormat df = DateTimeUtil.getDateFormat(member, false, true, locale);
			Date date = df.parse(strValue, new ParsePosition(0));

			if (date == null)
			{
				throw new ValidationException("Invalid time value (example for a valid time: " + df.format(new Date()) + ")");
			}

			// Convert it to a SQL date
			long l = date.getTime();
			return new java.sql.Time(l);
		}

		return null;
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
		if (value == null)
			return null;

		Date date = (Date) value;

		DateFormat df = DateTimeUtil.getDateFormat(member, false, true, locale);
		String result = df.format(date);
		return result;
	}
}
