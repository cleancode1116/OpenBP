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

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Text utilities.
 *
 * @author Heiko Erhardt
 */
public final class TextUtil
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private TextUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	/**
	 * Extracts a summary from an object description.
	 * The summary is the text up to the first point that is followed by a whitespace
	 * or up to the first new line in the string.
	 * Any white spaces in the string will be compressed to a single space.
	 *
	 * @param text Description text
	 * @return The summary string or null if the text is null or empty
	 */
	public static String extractSummary(String text)
	{
		if (text == null)
			return null;
		text = text.trim();
		int len = text.length();
		if (len == 0)
			return null;

		StringBuffer sb = new StringBuffer();

		boolean seenSpace = false;
		for (int i = 0; i < len; ++i)
		{
			char c = text.charAt(i);

			if (c == '\n')
				break;

			if (Character.isWhitespace(c))
			{
				if (!seenSpace)
				{
					sb.append(' ');
					seenSpace = true;
				}
				continue;
			}

			sb.append(c);
			seenSpace = false;

			if (c == '.')
			{
				// Break at the dot if we are at the end of the string or it is followed by a white space.
				if (i + 1 >= len || Character.isWhitespace(text.charAt(i + 1)))
					break;
			}
		}

		return sb.length() != 0 ? sb.toString() : null;
	}

	//////////////////////////////////////////////////
	// @@ Line processing
	//////////////////////////////////////////////////

	/** Trim characters for {@link #breakIntoLines} */
	private static final char [] TRIM_CHARACTERS = new char [] { ' ', '\t', '\r' };

	/**
	 * Breaks a single string into a list of lines.
	 * Every line is limited to a particular length.
	 * Internally, a java.text.BreakIterator is used to break the lines,
	 * maintaining punctaction and hyphenated words.
	 * Newline characters are considered line breaks also (they will be removed
	 * from the resulting lines).
	 *
	 * @param s The string to break
	 * @param trim
	 *  true    Remove leading and trailing spaces and tabulators from each line<br>
	 *  false  Do not trim the lines
	 * @param maxLength Maximum length per line or -1 for unlimited line length
	 * @return A list of line strings
	 */
	public static List breakIntoLines(final String s, boolean trim, int maxLength)
	{
		// List of lines to return;
		List lines = new ArrayList();

		// Check for valid arguments
		if (s == null)
			return lines;

		String restString = s;

		while (restString != null)
		{
			int lineEnd = restString.indexOf('\n');
			String line = null;

			if (lineEnd == -1)
			{
				line = restString;
				restString = null;
			}
			else
			{
				line = restString.substring(0, lineEnd);
				if (restString.length() > (lineEnd + 1))
					restString = restString.substring(lineEnd + 1);
				else
					restString = null;
			}

			if (trim)
				line = StringUtil.trim(line, TRIM_CHARACTERS);

			// Check if the line is longer than allowed
			if (maxLength >= 0 && line.length() > maxLength)
			{
				BreakIterator iterator = null;

				// Line is too long, break it into pieces
				for (;;)
				{
					line = line.trim();
					if (line.length() == 0)
						break;

					if (iterator == null)
						iterator = BreakIterator.getLineInstance();
					iterator.setText(line);

					int breakPos = 0;
					for (;;)
					{
						int next = iterator.next();

						if (next == BreakIterator.DONE)
						{
							// No more lines to wrap
							break;
						}

						if (next < maxLength)
						{
							// Remember position to break
							breakPos = next;
						}
						else
						{
							if (breakPos == 0)
							{
								// No remembered position, use this one
								breakPos = next;
							}
							break;
						}
					}

					// Line part to add
					String singleLine = line.substring(0, breakPos).trim();

					if (!(lines.size() == 0 && line.trim().length() == 0))
						lines.add(singleLine);

					line = line.substring(breakPos);
				}
			}
			else
			{
				// Avoid the adding if the line is empty and it would be the first line
				if (!(lines.size() == 0 && line.trim().length() == 0))
					lines.add(line);
			}
		}

		// Trim the last lines that do not contain any content
		for (int i = lines.size(); --i >= 0;)
		{
			if (!"".equals(lines.get(i)))
				break;
			lines.remove(i);
		}

		return lines;
	}

	/**
	 * Concatenates several lines into one string.
	 * The lines are separated by a '\n' character.
	 *
	 * @param lineIterator Iterator of String objects
	 * @return The result string or null
	 */
	public static String concatLines(Iterator lineIterator)
	{
		StringBuffer sb = new StringBuffer();
		while (lineIterator.hasNext())
		{
			String line = (String) lineIterator.next();
			if (line != null)
			{
				if (sb.length() != 0)
					sb.append('\n');
				sb.append(line);
			}
		}
		return sb.length() != 0 ? sb.toString() : null;
	}

	//////////////////////////////////////////////////
	// @@ Decoding/encoding of multi line strings
	//////////////////////////////////////////////////

	/**
	 * Trims the string and converts all newline and tab character to an escape representation.
	 *
	 * Newlines: "\n"<br>
	 * Tabs: "\t"
	 *
	 * @param s String to encode
	 * @return The encoded string or null if the string is null or contains white space only
	 */
	public static String encodeMultiLineString(String s)
	{
		if (s != null)
		{
			s = s.trim();
			if (s.length() == 0)
			{
				s = null;
			}
			else if (s.indexOf('\n') >= 0 || s.indexOf('\t') >= 0)
			{
				StringBuffer sb = new StringBuffer();

				boolean seenSpace = false;
				int n = s.length();
				for (int i = 0; i < n; ++i)
				{
					char c = s.charAt(i);
					if (c == '\n')
					{
						sb.append("\\n");
						seenSpace = true;
					}
					else if (c == '\t')
					{
						sb.append("\\t");
						seenSpace = true;
					}
					else if (c == ' ')
					{
						if (seenSpace)
						{
							sb.append("\\s");
						}
						else
						{
							sb.append(" ");
							seenSpace = true;
						}
					}
					else
					{
						sb.append(c);
						seenSpace = false;
					}
				}

				s = sb.toString();
			}
		}
		return s;
	}

	/**
	 * Trims the string and converts all newline and tab escape representations to their character values.
	 *
	 * Newlines: "\n"<br>
	 * Tabs: "\t"
	 *
	 * @param s String to decode
	 * @return The decoded string or null if the string is null or contains white space only
	 */
	public static String decodeMultiLineString(String s)
	{
		if (s != null)
		{
			s = s.trim();
			if (s.length() == 0)
			{
				s = null;
			}
			else if (s.indexOf('\n') >= 0 || s.indexOf('\\') >= 0)
			{
				StringBuffer sb = new StringBuffer();

				boolean skip = false;

				int n = s.length();
				for (int i = 0; i < n; ++i)
				{
					char c = s.charAt(i);
					if (c == '\\')
					{
						if (i + 1 < n)
						{
							char c2 = s.charAt(i + 1);
							switch (c2)
							{
							case 'n':
								c = '\n';
								++i;
								skip = true;
								break;

							case 't':
								c = '\t';
								++i;
								skip = true;
								break;

							case 's':
								c = ' ';
								++i;
								skip = true;
								break;

							default:
								skip = false;
							}
						}
						else
						{
							skip = false;
						}
					}
					else if (c == ' ' || c == '\t' || c == '\n')
					{
						if (skip)
						{
							// Eat white space after newlines
							continue;
						}
					}
					else
					{
						skip = false;
					}

					sb.append(c);
				}

				s = sb.length() != 0 ? sb.toString() : null;
			}
		}
		return s;
	}

	//////////////////////////////////////////////////
	// @@ HTML text processing
	//////////////////////////////////////////////////

	/**
	 * Converts regular text to an HTML form that can be used for display in browsers or tool tips.
	 * Newlines will be substituted by their HTML counterpart.
	 *
	 * @param paragraphs Array of strings, each one denoting a single paragraph or null
	 * @param boldTitle
	 *		true	Treat the first paragraph as title and print it bold.<br>
	 *		false	No special handling for the first paragraph
	 * @param separatorPos Position for the separator line (-1 if no separator should be used,
	 * 0 for separator after paragraphs[0] etc.
	 * @param maxLength Maximum length per line or -1 for unlimited line length
	 * @return The HTML text or null
	 */
	public static String convertToHTML(String [] paragraphs, boolean boldTitle, int separatorPos, int maxLength)
	{
		if (paragraphs == null || paragraphs.length == 0)
			return null;

		StringBuffer output = new StringBuffer();
		output.append("<html>");

		boolean lineAdded = false;

		for (int i = 0; i < paragraphs.length; ++i)
		{
			if (paragraphs [i] == null)
				continue;

			if (lineAdded)
			{
				output.append("<br>");
			}

			if (i == separatorPos + 1 && lineAdded)
			{
				// Create separator line
				output.append("<hr>");
			}

			if (i == 0 && boldTitle)
			{
				output.append("<b>");
			}

			appendHTMLLine(paragraphs [i], output, maxLength);
			lineAdded = true;

			if (i == 0 && boldTitle)
			{
				output.append("</b>");
			}
		}

		output.append("</html>");
		return output.toString();
	}

	/**
	 * Appends the given text as HTML line to the output.
	 *
	 * @param line Line to append
	 * @param output Output string buffer
	 * @param maxLength Maximum length per line or -1 for unlimited line length
	 */
	public static void appendHTMLLine(String line, StringBuffer output, int maxLength)
	{
		List lines = breakIntoLines(line, false, maxLength);
		int n = lines.size();
		for (int i = 0; i < n; ++i)
		{
			if (i > 0)
				output.append("<br>");
			output.append((String) lines.get(i));
		}
	}
}
