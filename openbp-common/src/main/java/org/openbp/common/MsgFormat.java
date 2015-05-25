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
package org.openbp.common;

import java.text.MessageFormat;

/**
 * Custom message format class.
 * This class works similar to the {@link java.text.MessageFormat} class except that<br>
 * - it provides an additional syntax for quoted arguments<br>
 * - it provides some convenience methods that avoid explicit argument array construction.
 *
 * \bQuoted Arguments\b
 *
 * Often, file and object names are enclosed in single quotes in a message.
 * Due to a strange behavior of the original MessageFormat class, they have to be written
 * as double single quotes in the message pattern (e. g. "...''{0}''...").
 * This is unhandy and a potential cause of errors. An additional syntax has been
 * introduced that automatically wraps arguments in single quotes:<br>
 * \c"...$n..."\c will be converted to "...''{0}''..." automatically.
 * If the "$" is followed by a non-digit character, it will be printed as it is.
 *
 * @author Heiko Erhardt
 */
public final class MsgFormat
{
	/** Constant value serial version UID */
	private static final long serialVersionUID = 1L;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Pattern constructor.
	 *
	 * @param pattern The pattern to be used to format messages.<br>
	 * For details on the pattern argument placeholder syntax, see the {@link java.text.MessageFormat} class.
	 */
	private MsgFormat(String pattern)
	{
	}

	//////////////////////////////////////////////////
	// @@ Convenience methods
	//////////////////////////////////////////////////

	/**
	 * Formats a message using the given pattern and arguments.
	 * @copy format (String pattern, Object arg1, Object arg2, Object arg3, Object arg4)
	 * @param args Array of message arguments
	 */
	public static String format(String pattern, Object[] args)
	{
		String s = substituteQuotedPlaceholders(pattern);
		MessageFormat format = new MessageFormat(s);
		return format.format(args);
	}

	/**
	 * @copy format (String pattern, Object arg1, Object arg2, Object arg3, Object arg4)
	 */
	public static String format(String pattern, Object arg1)
	{
		return format(pattern, new Object[]
		{
			arg1
		});
	}

	/**
	 * @copy format (String pattern, Object arg1, Object arg2, Object arg3, Object arg4)
	 */
	public static String format(String pattern, Object arg1, Object arg2)
	{
		return format(pattern, new Object[]
		{
			arg1, arg2
		});
	}

	/**
	 * @copy format (String pattern, Object arg1, Object arg2, Object arg3, Object arg4)
	 */
	public static String format(String pattern, Object arg1, Object arg2, Object arg3)
	{
		return format(pattern, new Object[]
		{
			arg1, arg2, arg3
		});
	}

	/**
	 * Formats a message using the given pattern and arguments.
	 * Throws several types of RuntimeException if the pattern parsing or application fails.
	 *
	 * @param pattern The pattern to be used to format messages.<br>
	 * For details on the pattern argument placeholder syntax, see the {@link java.text.MessageFormat} class.
	 * @param arg1 First message argument
	 * @param arg2 Second message argument
	 * @param arg3 Third message argument
	 * @param arg4 Fourth messageObject argument
	 * @return The formatted message
	 */
	public static String format(String pattern, Object arg1, Object arg2, Object arg3, Object arg4)
	{
		return format(pattern, new Object[]
		{
			arg1, arg2, arg3, arg4
		});
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Substitutes the quoted placeholder syntax ("$n") for the the syntax understood by MessageFormat ("''{n}''").
	 *
	 * @param pattern Message to parse
	 * @return String containing MessageFormat-compatible placeholders
	 */
	private static String substituteQuotedPlaceholders(String pattern)
	{
		if (pattern.indexOf('$') < 0)
			// No quoted placeholder syntax in pattern.
			return pattern;

		StringBuffer sb = new StringBuffer();
		int consumed = 0;
		int pos = 0;
		while (true)
		{
			pos = pattern.indexOf('$', consumed);
			if (pos == - 1)
			{
				break;
			}

			sb.append(pattern.substring(consumed, pos));
			consumed = pos;

			if (pos + 1 < pattern.length() && Character.isDigit(pattern.charAt(pos + 1)))
			{
				sb.append("''{");
				sb.append(pattern.charAt(pos + 1));
				sb.append("}''");

				consumed += 2;
			}
			else
			{
				sb.append("$");

				consumed += 1;
			}
		}

		sb.append(pattern.substring(consumed));

		return sb.toString();
	}
}
