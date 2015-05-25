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

import java.util.HashMap;
import java.util.Map;

/**
 * HTML-related utilities, i\.e\. to convert convert regular text to HTML text.
 */
public final class HTMLEscapeHelper
{
	//////////////////////////////////////////////////
	// @@ Special character data
	//////////////////////////////////////////////////

	/**
	 * Table of special characters like umlaut characters that will be
	 * converted to their respective HTML representation.
	 * Based on http://hotwired.lycos.com/webmonkey/reference/special_characters
	 */
	static Object [][] entities = { {
	// apostrophe
		"#39", Integer.valueOf(39) }, {
	// slash
		"#47", Integer.valueOf('/') }, {
	// backslash
		"#92", Integer.valueOf('\\') }, {
	// double-quote
		"quot", Integer.valueOf(34) }, {
	// ampersand
		"amp", Integer.valueOf(38), }, {
	// less-than
		"lt", Integer.valueOf(60) }, {
	// greater-than
		"gt", Integer.valueOf(62) }, {
	// breaking space
		"nbsp", Integer.valueOf(160) }, {
	// copyright
		"copy", Integer.valueOf(169) }, {
	// registered trademark
		"reg", Integer.valueOf(174) }, {
	// uppercase A, grave accent
		"Agrave", Integer.valueOf(192) }, {
	// uppercase A, acute accent
		"Aacute", Integer.valueOf(193) }, {
	// uppercase A, circumflex accent
		"Acirc", Integer.valueOf(194) }, {
	// uppercase A, tilde
		"Atilde", Integer.valueOf(195) }, {
	// uppercase A, umlaut
		"Auml", Integer.valueOf(196) }, {
	// uppercase A, ring
		"Aring", Integer.valueOf(197) }, {
	// uppercase AE
		"AElig", Integer.valueOf(198) }, {
	// uppercase C, cedilla
		"Ccedil", Integer.valueOf(199) }, {
	// uppercase E, grave accent
		"Egrave", Integer.valueOf(200) }, {
	// uppercase E, acute accent
		"Eacute", Integer.valueOf(201) }, {
	// uppercase E, circumflex accent
		"Ecirc", Integer.valueOf(202) }, {
	// uppercase E, umlaut
		"Euml", Integer.valueOf(203) }, {
	// uppercase I, grave accent
		"Igrave", Integer.valueOf(204) }, {
	// uppercase I, acute accent
		"Iacute", Integer.valueOf(205) }, {
	// uppercase I, circumflex accent
		"Icirc", Integer.valueOf(206) }, {
	// uppercase I, umlaut
		"Iuml", Integer.valueOf(207) }, {
	// uppercase Eth, Icelandic
		"ETH", Integer.valueOf(208) }, {
	// uppercase N, tilde
		"Ntilde", Integer.valueOf(209) }, {
	// uppercase O, grave accent
		"Ograve", Integer.valueOf(210) }, {
	// uppercase O, acute accent
		"Oacute", Integer.valueOf(211) }, {
	// uppercase O, circumflex accent
		"Ocirc", Integer.valueOf(212) }, {
	// uppercase O, tilde
		"Otilde", Integer.valueOf(213) }, {
	// uppercase O, umlaut
		"Ouml", Integer.valueOf(214) }, {
	// uppercase O, slash
		"Oslash", Integer.valueOf(216) }, {
	// uppercase U, grave accent
		"Ugrave", Integer.valueOf(217) }, {
	// uppercase U, acute accent
		"Uacute", Integer.valueOf(218) }, {
	// uppercase U, circumflex accent
		"Ucirc", Integer.valueOf(219) }, {
	// uppercase U, umlaut
		"Uuml", Integer.valueOf(220) }, {
	// uppercase Y, acute accent
		"Yacute", Integer.valueOf(221) }, {
	// uppercase THORN, Icelandic
		"THORN", Integer.valueOf(222) }, {
	// lowercase sharps, German
		"szlig", Integer.valueOf(223) }, {
	// lowercase a, grave accent
		"agrave", Integer.valueOf(224) }, {
	// lowercase a, acute accent
		"aacute", Integer.valueOf(225) }, {
	// lowercase a, circumflex accent
		"acirc", Integer.valueOf(226) }, {
	// lowercase a, tilde
		"atilde", Integer.valueOf(227) }, {
	// lowercase a, umlaut
		"auml", Integer.valueOf(228) }, {
	// lowercase a, ring
		"aring", Integer.valueOf(229) }, {
	// lowercase ae
		"aelig", Integer.valueOf(230) }, {
	// lowercase c, cedilla
		"ccedil", Integer.valueOf(231) }, {
	// lowercase e, grave accent
		"egrave", Integer.valueOf(232) }, {
	// lowercase e, acute accent
		"eacute", Integer.valueOf(233) }, {
	// lowercase e, circumflex accent
		"ecirc", Integer.valueOf(234) }, {
	// lowercase e, umlaut
		"euml", Integer.valueOf(235) }, {
	// lowercase i, grave accent
		"igrave", Integer.valueOf(236) }, {
	// lowercase i, acute accent
		"iacute", Integer.valueOf(237) }, {
	// lowercase i, circumflex accent
		"icirc", Integer.valueOf(238) }, {
	// lowercase i, umlaut
		"iuml", Integer.valueOf(239) }, {
	// lowercase eth, Icelandic
		"eth", Integer.valueOf(240) }, {
	// lowercase n, tilde
		"ntilde", Integer.valueOf(241) }, {
	// lowercase o, grave accent
		"ograve", Integer.valueOf(242) }, {
	// lowercase o, acute accent
		"oacute", Integer.valueOf(243) }, {
	// lowercase o, circumflex accent
		"ocirc", Integer.valueOf(244) }, {
	// lowercase o, tilde
		"otilde", Integer.valueOf(245) }, {
	// lowercase o, umlaut
		"ouml", Integer.valueOf(246) }, {
	// lowercase o, slash
		"oslash", Integer.valueOf(248) }, {
	// lowercase u, grave accent
		"ugrave", Integer.valueOf(249) }, {
	// lowercase u, acute accent
		"uacute", Integer.valueOf(250) }, {
	// lowercase u, circumflex accent
		"ucirc", Integer.valueOf(251) }, {
	// lowercase u, umlaut
		"uuml", Integer.valueOf(252) }, {
	// lowercase y, acute accent
		"yacute", Integer.valueOf(253) }, {
	// lowercase thorn, Icelandic
		"thorn", Integer.valueOf(254) }, {
	// lowercase y, umlaut
		"yuml", Integer.valueOf(255) }, {
	// Euro symbol
		"euro", Integer.valueOf(8364) }, };

	/**
	 * Hashtable of special characters.
	 */
	private static Map i2e = new HashMap();

	static
	{
		for (int i = 0; i < entities.length; ++i)
		{
			i2e.put(entities [i] [1], entities [i] [0]);
		}
	}

	/**
	 * Private constructor prevents instantiation.
	 */
	private HTMLEscapeHelper()
	{
	}

	//////////////////////////////////////////////////
	// @@ Escaped character data
	//////////////////////////////////////////////////

	/**
	 * Table of supported escaped characters like "\n" that will be
	 * converted to their HTML equivalents.
	 */
	private static Object [][] maskedEntities = { { "\n", "<br>" }, { "\t", "&nbsp;&nbsp;&nbsp;&nbsp;" } };

	/**
	 * Hashtable of escaped characters.
	 */
	private static Map masked2html = new HashMap();

	static
	{
		for (int i = 0; i < maskedEntities.length; ++i)
		{
			masked2html.put(maskedEntities [i] [0], maskedEntities [i] [1]);
		}
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Turns funky characters into HTML entity equivalents<p>
	 * e.g. <tt>"bread" & "butter"</tt>
	 * =>
	 * <tt>&amp;quot;bread&amp;quot; &amp;amp; &amp;quot;butter&amp;quot;</tt>.
	 *
	 * Supports nearly all HTML entities, including funky accents.
	 * See the source code for more detail.
	 *
	 * In addition, "\n" characters will be converted to HTML line breaks and "\t" to
	 * 4 non-breaking spaces.
	 *
	 * @param s String to escape
	 * @return Escaped string
	 */
	public static String htmlescape(String s)
	{
		if (s == null)
			return "";
		StringBuffer buf = new StringBuffer();

		int n = s.length();
		for (int i = 0; i < n; ++i)
		{
			char ch = s.charAt(i);

			String entity = (String) i2e.get(Integer.valueOf(ch));

			if (entity == null)
			{
				String htmlEntity = (String) masked2html.get(String.valueOf(ch));
				if (htmlEntity != null)
				{
					buf.append(htmlEntity);
					continue;
				}
			}

			if (entity == null)
			{
				if (ch > 128)
				{
					buf.append("&#" + ((int) ch) + ";");
				}
				else
				{
					buf.append(ch);
				}
			}
			else
			{
				buf.append("&" + entity + ";");
			}
		}
		return buf.toString();
	}
}
