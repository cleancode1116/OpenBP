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
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Static date and time utility methods.
 *
 * @author Heiko Erhardt
 */
public final class DateTimeUtil
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private DateTimeUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	/**
	 * Gets a date format object suitable to format or parse a date value.
	 *
	 * @param member Data member definition if the value that should be parsed denotes
	 * a member of a {@link DataTypeItem} class or null if the value is to be seen as stand-alone data item.
	 * @param haveDate true if the format should have a date component
	 * @param haveTime true if the format should have a time component
	 * @param locale Current locale of the request or null if the locale is unknown
	 * @return The date format
	 */
	protected static DateFormat getDateFormat(DataMember member, boolean haveDate, boolean haveTime, Locale locale)
	{
		if (locale == null)
			locale = Locale.getDefault();

		String format = null;
		if (member != null)
			format = member.getFormat();

		int style = DateFormat.DEFAULT;

		DateFormat df = null;
		if (format != null)
		{
			int l = format.length();
			if (l > 3)
			{
				// Use the format as pattern for a simple date format
				return new SimpleDateFormat(format, locale);
			}

			// We consider the format of a date format type specification

			for (int i = 0; i < l; ++i)
			{
				switch (format.charAt(i))
				{
				case 'd':
					// Include date
					haveDate = true;
					break;

				case 't':
					// Include time
					haveTime = true;
					break;

				case 'f':
					// Full style pattern
					style = DateFormat.FULL;
					break;

				case 'l':
					// Long style pattern
					style = DateFormat.LONG;
					break;

				case 'm':
					// Medium style pattern
					style = DateFormat.MEDIUM;
					break;

				case 's':
					// Short style pattern
					style = DateFormat.SHORT;
					break;

				default:
				}
			}
		}

		if (haveDate && haveTime)
			df = DateFormat.getDateTimeInstance(style, style, locale);
		else if (haveTime)
			df = DateFormat.getTimeInstance(style, locale);
		else
			df = DateFormat.getDateInstance(style, locale);

		return df;
	}
}
