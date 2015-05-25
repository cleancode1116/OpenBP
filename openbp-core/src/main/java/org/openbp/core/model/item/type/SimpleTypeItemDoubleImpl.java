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

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Primitive type that denots a string.
 *
 * @author Heiko Erhardt
 */
public class SimpleTypeItemDoubleImpl extends SimpleTypeItemStringImpl
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public SimpleTypeItemDoubleImpl()
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
			// Determine the number format and validate the text characters
			NumberFormat nf = NumberUtil.getNumberFormat(member, locale);
			NumberUtil.validateNumberString(s, nf, false);

			try
			{
				Number number = nf.parse(s);
				if (!(number instanceof Double))
					number = new Double(number.doubleValue());
				return number;
			}
			catch (ParseException e)
			{
				throw new ValidationException("Value of field is not a number");
			}
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

		Number number = (Number) value;

		StringBuffer sb = new StringBuffer();

		NumberFormat nf = NumberUtil.getNumberFormat(member, locale);
		nf.format(number, sb, new FieldPosition(0));

		return sb.toString();
	}
}
