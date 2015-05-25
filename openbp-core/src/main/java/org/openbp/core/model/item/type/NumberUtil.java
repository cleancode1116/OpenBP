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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utilities for parsing and formatting of numbers.
 *
 * @author Heiko Erhardt
 */
public final class NumberUtil
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private NumberUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	/**
	 * Validates a number string according to the supplied number format.
	 *
	 * @param text Text to validate
	 * @param nf Number format
	 * @param convertSeparator
	 *		true	Converts the decimal separator to '.'<br>
	 *		false	Returns the string as it is
	 * @return The 'text' argument with the optionally converted separator
	 * @throws ValidationException On error
	 */
	public static String validateNumberString(String text, NumberFormat nf, boolean convertSeparator)
		throws ValidationException
	{
		if (text == null)
			return null;
		int len = text.length();
		if (len == 0)
			return null;

		StringBuffer sb = null;
		if (convertSeparator)
		{
			sb = new StringBuffer();
		}

		DecimalFormatSymbols symbols = ((DecimalFormat) nf).getDecimalFormatSymbols();

		char zeroDigit = symbols.getZeroDigit();
		char decimalSeparator = symbols.getDecimalSeparator();
		char monetaryDecimalSeparator = symbols.getMonetaryDecimalSeparator();
		char minus = symbols.getMinusSign();

		int i = 0;
		if (text.charAt(i) == minus)
		{
			if (sb != null)
			{
				sb.append('-');
			}
			++i;
		}

		int maxPrecision = nf.getMaximumFractionDigits();
		int precision = -1;

		for (; i < len; ++i)
		{
			char c = text.charAt(i);

			if (Character.isDigit(c) || c == zeroDigit)
			{
				if (precision >= 0)
				{
					if (++precision > maxPrecision)
					{
						throw new ValidationException("Number has a maximum precision of " + maxPrecision + " digits.");
					}
				}

				if (sb != null)
				{
					sb.append(c);
				}
				continue;
			}

			if (c == decimalSeparator || c == monetaryDecimalSeparator)
			{
				if (precision >= 0)
				{
					throw new ValidationException("Number cannot contain two separator characters.");
				}
				precision = 0;
				if (sb != null)
				{
					sb.append('.');
				}
				continue;
			}

			throw new ValidationException("Number contains invalid character '" + c + "'");
		}

		if (sb != null)
		{
			return sb.toString();
		}

		return text;
	}

	/**
	 * Gets a number format object suitable to format or parse a number value.
	 *
	 * @param member Data member definition if the value that should be parsed denotes
	 * a member of a {@link DataTypeItem} class or null if the value is to be seen as stand-alone data item.
	 * @param locale Current locale of the request or null if the locale is unknown
	 * @return The number format
	 */
	public static NumberFormat getNumberFormat(DataMember member, Locale locale)
	{
		if (locale == null)
			locale = Locale.getDefault();

		String format = null;
		if (member != null)
			format = member.getFormat();

		boolean haveCurrency = false;
		boolean haveGrouping = false;

		if (format != null)
		{
			int l = format.length();

			for (int i = 0; i < l; ++i)
			{
				switch (format.charAt(i))
				{
				case 'c':
					// Currency
					haveCurrency = true;
					break;

				case 'g':
					// Grouping
					haveGrouping = true;
					break;

				default:
				}
			}
		}

		NumberFormat nf;
		if (haveCurrency)
		{
			// Don't need no precision for currency values
			nf = NumberFormat.getCurrencyInstance(locale);
		}
		else
		{
			nf = NumberFormat.getNumberInstance(locale);

			if (member != null)
			{
				int definedPrecision = member.getPrecision();
				if (definedPrecision >= 0)
				{
					nf.setMaximumFractionDigits(definedPrecision);
				}
			}
		}

		if (haveGrouping)
			nf.setGroupingUsed(haveGrouping);

		return nf;
	}
}
