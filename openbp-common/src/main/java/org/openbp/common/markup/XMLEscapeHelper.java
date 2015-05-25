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
package org.openbp.common.markup;

/**
 * This class contains various static string utility methods that process special
 * characters in XML strings.
 */
public final class XMLEscapeHelper
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private XMLEscapeHelper()
	{
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Escapes any characters considered special by the XML parser (e\. g\. &lt;, &amp;
	 * etc\.).
	 *
	 * @param str String to convert
	 * @return The converted string
	 */
	public static String encodeXMLContent(String str)
	{
		if (str == null)
			return "";
		int len = str.length();
		if (len == 0)
			return "";

		// Allocate string buffer 10 % larger than original string
		StringBuffer sb = new StringBuffer(len > 16 ? len * 110 / 100 : 16);

		for (int i = 0; i < len; i++)
		{
			char c = str.charAt(i);
			switch (c)
			{
			case '<':
				sb.append("&lt;");
				break;

			case '>':
				sb.append("&gt;");
				break;

			case '&':
				sb.append("&amp;");
				break;

			case '\'':
				sb.append("&apos;");
				break;

			case '"':
				sb.append("&quot;");
				break;

			default:
				if (c > 126)
				{
					int integer = c;
					sb.append("&#" + integer + ";");
				}
				else
				{
					sb.append(c);
				}
				break;
			}
		}

		return sb.toString();
	}

	/**
	 * Escapes all characters that are not 7 bit ASCII characters by their
	 * markup language representation, i\. e\. &"#207;"
	 *
	 * @param str String to convert
	 * @return The converted string; Can never be null
	 */
	public static String encodeXMLString(String str)
	{
		if (str == null)
			return "";
		int len = str.length();
		if (len == 0)
			return "";

		// Allocate string buffer 10 % larger than original string
		StringBuffer sb = new StringBuffer(len > 16 ? len * 110 / 100 : 16);

		for (int i = 0; i < len; i++)
		{
			char c = str.charAt(i);

			if (c > 126)
			{
				int integer = c;
				sb.append("&#" + integer + ";");
			}
			else
				sb.append(c);
		}

		return sb.toString();
	}
}
