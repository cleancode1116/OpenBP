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
package org.openbp.server.engine.script;

import org.openbp.core.engine.ExpressionConstants;

/**
 * Script-related utility methods.
 *
 * @author Heiko Erhardt
 */
public final class ScriptUtil
{
	/**
	 * Maximum number of characters to collect for a toString value.
	 * (used by the expression parser)
	 */
	public static final int MAX_TOSTRINGVALUE_LENGTH = 100;

	/**
	 * Private constructor prevents instantiation.
	 */
	private ScriptUtil()
	{
	}

	/**
	 * Determines if the expression is a constant expression.
	 *
	 * @param expression Expression to evaluate
	 * @return
	 *		true	The expression consists of constants only and can be executed without
	 *				an expression parser.<br>
	 *		false	We will need an expression parser to evaluate the expression.
	 */
	public static boolean isConstantExpression(String expression)
	{
		if (expression != null)
		{
			if (expression.equals("true") || expression.equals("false") || expression.equals("java.lang.Boolean.TRUE") || expression.equals("java.lang.Boolean.FALSE"))
			{
				return true;
			}

			char c = expression.charAt(0);

			// String constant?
			if (c == '"')
			{
				return isStringConstant(expression, '"');
			}
			if (c == '\'')
			{
				return isStringConstant(expression, '\'');
			}

			// String or number constant?
			if (Character.isDigit(c))
			{
				return isNumericConstant(expression);
			}
		}

		return false;
	}

	/**
	 * Determines the value of a simple constant expression.
	 * Make sure to call this method only if you are sure that the expression is really
	 * a simple constant (see the {@link #isConstantExpression} method).
	 *
	 * @param expression Expression to evaluate
	 * @return The expression value or null
	 */
	public static Object getConstantExpressionValue(String expression)
	{
		if (expression != null)
		{
			if (expression.equals("true") || expression.equals("java.lang.Boolean.TRUE"))
			{
				return Boolean.TRUE;
			}

			if (expression.equals("false") || expression.equals("java.lang.Boolean.FALSE"))
			{
				return Boolean.FALSE;
			}

			char c = expression.charAt(0);

			// String constant?
			if (c == '"' || c == '\'')
			{
				return expression.substring(1, expression.length() - 1);
			}

			// String or number constant?
			if (Character.isDigit(c))
			{
				return Integer.valueOf(expression);
			}
		}

		return null;
	}

	/**
	 * Creates a name for a map element from the key value.
	 *
	 * @param key Key value of the map
	 * @param includeParens
	 *		true	Include parenthesis ("[]") in the name<br>
	 *		false	Do not include parenthesis
	 *
	 * @return Name (usually "[key]")
	 */
	public static String createMapElementName(Object key, boolean includeParens)
	{
		StringBuilder ident = new StringBuilder();
		if (includeParens)
			ident.append('[');

		if (key != null)
		{
			String tmp = key.toString();

			// To prevent conflicts with expr. separator
			tmp = tmp.replace(ExpressionConstants.MEMBER_OPERATOR_CHAR, '-');
			if (tmp.length() > MAX_TOSTRINGVALUE_LENGTH)
			{
				ident.append(tmp.substring(0, MAX_TOSTRINGVALUE_LENGTH));
				ident.append("...");
			}
			else
			{
				ident.append(tmp);
			}
		}
		else
		{
			ident.append("null");
		}

		if (includeParens)
			ident.append(']');

		return ident.toString();
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Determines if the expression is a string constant.
	 *
	 * @param expression Expression to check
	 * @param delimiter String delimiter character
	 * @nowarn
	 */
	private static boolean isStringConstant(String expression, char delimiter)
	{
		int len = expression.length();
		if (expression.charAt(0) != delimiter || expression.charAt(len - 1) != delimiter)
			return false;

		--len;
		for (int i = 1; i < len; ++i)
		{
			if (expression.charAt(i) == delimiter)
				return false;
		}

		return true;
	}

	/**
	 * Determines if the expression is a numeric constant.
	 *
	 * @param expression Expression to check
	 * @nowarn
	 */
	private static boolean isNumericConstant(String expression)
	{
		int len = expression.length();
		for (int i = 0; i < len; ++i)
		{
			if (!Character.isDigit(expression.charAt(i)))
				return false;
		}

		return true;
	}
}
