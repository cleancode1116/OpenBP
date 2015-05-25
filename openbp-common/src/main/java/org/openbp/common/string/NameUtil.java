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

/**
 * Java name utilities.
 *
 * @author Heiko Erhardt
 */
public final class NameUtil
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private NameUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	/**
	 * Converts a Java name into a human readable form.
	 * A mixed-case Java name will be split at upper case letters, i. e. a space will be inserted before
	 * each upper case letter and the letter itself will be converted to lower case if it is not the first character.<br>
	 * If the name is all upper case, it will be returned as it is if it does not contain
	 * an underscore character. Otherwise, all underscore characters will be replaced by
	 * a space and the remaining characters will be converted to lowercase (except the first character).
	 *
	 * Examples:<br>
	 * "AddToDatabase" -&gt; "Add to database"
	 * "ZIP" -&gt; "ZIP"
	 * "COMPANY_ID" -&gt; "Company id"
	 *
	 * @param name Name to convert or null
	 * @return Converted string or null
	 */
	public static String makeDisplayName(String name)
	{
		if (name == null)
			return null;

		int l = name.length();

		boolean isUpper = l > 1 && isUpperCase(name);

		if (isUpper && name.indexOf('_') < 0)
		{
			// Return the all uppercase name as it is
			return name;
		}

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < l; ++i)
		{
			char c = name.charAt(i);

			if (isUpper)
			{
				if (c == '_')
				{
					if (i > 0)
						sb.append(' ');
					continue;
				}

				if (i > 0)
				{
					// Convert all uppercase letters except the first one to lower case
					c = Character.toLowerCase(c);
				}
			}
			else
			{
				if (i > 0)
				{
					if (Character.isUpperCase(c))
					{
						// Append a space and convert the uppercase letter to lower case
						sb.append(' ');
						c = Character.toLowerCase(c);
					}
				}
				else
				{
					// Always convert the first character to upper case
					if (Character.isLowerCase(c))
					{
						c = Character.toUpperCase(c);
					}
				}

				if (c == '_')
				{
					// This is totally mixed, just convert the underscore to a space
					c = ' ';
				}
			}

			sb.append(c);
		}

		return sb.toString();
	}

	/**
	 * Converts a Java name into a name suitable for a Java data member name.
	 * Actually converts the first character to lower case.
	 *
	 * Examples:<br>
	 * "companyName" -&gt; "companyName"
	 *
	 * @param name Name to convert or null
	 * @return Converted string or null
	 */
	public static String makeMemberName(String name)
	{
		return StringUtil.decapitalize(name);
	}

	/**
	 * Converts a Java name into an xml-like name.
	 * Example: "AddToDatabase" will become "add-to-database"
	 *
	 * @param name Name to convert or null
	 * @return Converted string or null
	 */
	public static String makeXMLName(String name)
	{
		if (name == null)
			return null;

		int l = name.length();

		boolean isUpper = l > 1 && isUpperCase(name);
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < l; ++i)
		{
			char c = name.charAt(i);

			if (isUpper)
			{
				if (c == '_')
				{
					if (i > 0)
						sb.append('-');
					continue;
				}
				c = Character.toLowerCase(c);
			}
			else
			{
				if (Character.isUpperCase(c))
				{
					if (i > 0)
						sb.append('-');
					c = Character.toLowerCase(c);
				}

				if (c == '_')
					c = '-';
			}

			sb.append(c);
		}

		return sb.toString();
	}

	/**
	 * Checks if a Java name is an upper case name.
	 *
	 * @param name Name to convert or null
	 * @return
	 *		true	The name does not contain lowercase characters.<br>
	 *		false	The name contains at least one lowercase character.
	 */
	public static boolean isUpperCase(String name)
	{
		int l = name.length();
		for (int i = 0; i < l; ++i)
		{
			char c = name.charAt(i);

			if (Character.isLowerCase(c))
				return false;
		}
		return true;
	}
}
