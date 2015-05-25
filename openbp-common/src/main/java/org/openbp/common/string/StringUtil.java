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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.openbp.common.CollectionUtil;

/**
 * This class contains various static string utilitiy methods.
 * In addition, it contains some other methods of very generic type.
 *
 * @author Heiko Erhardt
 */
public final class StringUtil
{
	/** Folder separator for usage withing resource and file system paths */
	public static final String FOLDER_SEP = "/";

	/** Folder separator for usage withing resource and file system paths */
	public static final char FOLDER_SEP_CHAR = '/';

	//////////////////////////////////////////////////
	// @@ Private constructor
	//////////////////////////////////////////////////

	/**
	 * Do not instantiate this class!
	 */
	private StringUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Tokenizing/Detokenizing
	//////////////////////////////////////////////////

	/**
	 * This method tokenizer the passed string using the given separator
	 * and returns the resulting fragment as a String array.
	 * Calls {@link #tokenize(String, String)}
	 *
	 * @param string The string to be tokenized
	 * @param separator The separator
	 * @return The fragments of the string
	 */
	public static String [] tokenize(String string, String separator)
	{
		return tokenize(string, separator, false);
	}

	/**
	 * This method tokenizes the passed string using the given separator
	 * and returns the resulting fragment as a String array.
	 *
	 * @param string The string to be tokenized
	 * @param separator The separator
	 * @param trim
	 *	true	The tokens have been trimmed before the were added<br>
	 *	false	The raw tokens will be used
	 * @return The fragments of the string
	 */
	public static String [] tokenize(String string, String separator, boolean trim)
	{
		StringTokenizer tokenizer = new StringTokenizer(string, separator);

		ArrayList parts = new ArrayList();
		while (tokenizer.hasMoreTokens())
		{
			String token = tokenizer.nextToken();

			if (trim)
				token = token.trim();

			parts.add(token);
		}

		return CollectionUtil.toStringArray(parts);
	}

	/**
	 * This method returns a string constructed from each element in the passed string array
	 * separated by the passed character.
	 *
	 * @param strings The strings to be concatenated
	 * @param separatorChar The character to be used for seperation
	 * @return The resulting string
	 */
	public static String detokenize(String [] strings, char separatorChar)
	{
		// Check arguments.
		if (strings == null || strings.length == 0)
		{
			return null;
		}

		// Create result.
		StringBuffer result = new StringBuffer(strings [0]);

		// Start detokenizing.
		for (int i = 1; i < strings.length; i++)
		{
			result.append(separatorChar);
			result.append(strings [i]);
		}

		// Return the result.
		return result.toString();
	}

	//////////////////////////////////////////////////
	// @@ String substitution
	//////////////////////////////////////////////////

	/**
	 * indexOf method that is not case-sensitive.
	 *
	 * @param s Sting to operate on or null
	 * @param startIndex Index to start the search at
	 * @param pattern Pattern to search for
	 *
	 * @return Position of pattern or -1 if not found
	 */
	public static int indexOfIgnoreCase(String s, int startIndex, String pattern)
	{
		if (s == null || pattern == null)
			return -1;

		int patLen = pattern.length();
		if (patLen == 0)
			return -1;

		int len = s.length();
		for (int i = startIndex; i < len; ++i)
		{
			if (s.regionMatches(true, i, pattern, 0, patLen))
			{
				return i;
			}
		}
		return -1;
	}

	/**
	 * Substitutes alls occurences of a pattern in a string, ignores character casing.
	 *
	 * @param s String to work on or null
	 * @param pattern Pattern to search for
	 * @param replacement String to replace for the pattern
	 * @return The result string or s itself if the pattern was not found
	 */
	public static String substituteIgnoreCase(String s, String pattern, String replacement)
	{
		if (s == null || pattern == null)
			return s;
		int patLen = pattern.length();
		if (patLen == 0)
			return s;

		// We are allocating the string buffer only if we really need it,
		// i. e. if there is something to replace
		StringBuffer sb = null;

		int len = s.length();
		for (int i = 0; i < len; ++i)
		{
			if (s.regionMatches(true, i, pattern, 0, patLen))
			{
				if (sb == null)
				{
					// First replace action, copy the string so far
					sb = new StringBuffer(len > 16 ? len : 16);
					sb.append(s.substring(0, i));
				}
				sb.append(replacement);
				i += patLen - 1;
			}
			else
			{
				if (sb != null)
					sb.append(s.charAt(i));
			}
		}
		return sb != null ? sb.toString() : s;
	}

	/**
	 * Substitutes alls occurences of several patterns in a string.
	 * Ignores character casing when comparing the string to the patterns.
	 *
	 * @param s String to work on
	 * @param map Map containing the patterns as keys and the replacements as values
	 * @return The result string or s itself if the string does not contain any of the patterns
	 */
	public static String substituteIgnoreCase(String s, Map map)
	{
		return substitute(s, map, true);
	}

	/**
	 * Substitutes alls occurences of a pattern in a string.
	 *
	 * @param s String to work on or null
	 * @param pattern Pattern to search for
	 * @param replacement String to replace for the pattern or null for nothing
	 * @return The result string or s itself if the pattern was not found
	 */
	public static String substitute(String s, String pattern, String replacement)
	{
		if (s == null || pattern == null)
			return s;
		if (s.indexOf(pattern) < 0)
		{
			// Prevents unnecessary allocation and parsing
			// in case the string does not contain the pattern
			return s;
		}

		int patLen = pattern.length();
		if (patLen == 0)
			return s;

		int len = s.length();
		StringBuffer sb = new StringBuffer(len > 16 ? len : 16);

		for (int i = 0; i < len; ++i)
		{
			if (s.startsWith(pattern, i))
			{
				if (replacement != null)
				{
					sb.append(replacement);
				}
				i += patLen - 1;
			}
			else
			{
				sb.append(s.charAt(i));
			}
		}
		return sb.toString();
	}

	/**
	 * Substitutes alls occurences of several patterns in a string.
	 * The pattern comparisons case-dependent.
	 *
	 * @param s String to work on
	 * @param map Map containing the patterns as keys and the replacements as values
	 * @return The result string or s itself if the string does not contain any of the patterns
	 */
	public static String substitute(String s, Map map)
	{
		return substitute(s, map, false);
	}

	/**
	 * Substitutes alls occurences of several patterns in a string.
	 *
	 * @param s String to work on
	 * @param map Map containing the patterns as keys and the replacements as values
	 * @param ignoreCase
	 *		true	Ignores character casing when comparing the string to the patterns<br>
	 *		false	Comparisons are case-dependent
	 * @return The result string or s itself if the string does not contain any of the patterns
	 */
	private static String substitute(String s, Map map, boolean ignoreCase)
	{
		if (s == null || map == null)
			return s;

		// We need to iterate the
		Set patternSet = map.keySet();
		if (patternSet.size() == 0)
			return s;

		// We are allocating the string buffer only if we really need it,
		// i. e. if there is something to replace
		StringBuffer sb = null;

		int len = s.length();
		for (int i = 0; i < len; ++i)
		{
			String matchingPattern = null;

			for (Iterator it = patternSet.iterator(); it.hasNext();)
			{
				String pattern = (String) it.next();

				boolean match = ignoreCase ? s.regionMatches(true, i, pattern, 0, pattern.length()) : s.startsWith(pattern, i);

				if (match)
				{
					matchingPattern = pattern;
					break;
				}
			}
			if (matchingPattern != null)
			{
				if (sb == null)
				{
					// First replace action, copy the string so far
					sb = new StringBuffer(len > 16 ? len : 16);
					sb.append(s.substring(0, i));
				}
				sb.append((String) map.get(matchingPattern));
				i += matchingPattern.length() - 1;
			}
			else
			{
				if (sb != null)
					sb.append(s.charAt(i));
			}
		}

		return sb != null ? sb.toString() : s;
	}

	//////////////////////////////////////////////////
	// @@ Name processing
	//////////////////////////////////////////////////

	/**
	 * Uppercases the first character of a string.
	 *
	 * @param name String to capitalize
	 * @return The capitalized string
	 */
	public static String capitalize(String name)
	{
		if (name != null && name.length() > 0)
		{
			char c = name.charAt(0);
			if (Character.isLowerCase(c))
			{
				StringBuffer sb = new StringBuffer(name);
				sb.setCharAt(0, Character.toUpperCase(c));
				name = sb.toString();
			}
		}
		return name;
	}

	/**
	 * Lowercases the first character of a string.
	 *
	 * @param name String to decapitalize
	 * @return The decapitalized string
	 */
	public static String decapitalize(String name)
	{
		if (name != null && name.length() > 0)
		{
			char c = name.charAt(0);
			if (Character.isUpperCase(c))
			{
				StringBuffer sb = new StringBuffer(name);
				sb.setCharAt(0, Character.toLowerCase(c));
				name = sb.toString();
			}
		}
		return name;
	}

	//////////////////////////////////////////////////
	// @@ Resource/path/file name processing
	//////////////////////////////////////////////////

	/**
	 * Converts a path name to its absolute form.
	 * Also converts Windows path separator ('\') to '/'.
	 *
	 * @param s Path to normalize or null
	 * @return The normalized path or null
	 */
	public static String absolutePathName(String s)
	{
		if (s != null)
		{
			try
			{
				s = new File(s).getCanonicalPath();
			}
			catch (IOException e)
			{
				// In case of file system access errors, we return the path as it is.
			}

			s = s.replace('\\', FOLDER_SEP_CHAR);
		}
		return s;
	}

	/**
	 * Normalizes a resource or file name by removing all "/./" or "/../" references.
	 * Also converts Windows path separator ('\') to '/'.
	 *
	 * @param path Path to normalize or null
	 * @return The normalized path or null
	 */
	public static String normalizePathName(String path)
	{
		if (path != null && path.length() > 0)
		{
			path = path.replace('\\', FOLDER_SEP_CHAR);

			if (path.indexOf("./") >= 0 || path.indexOf("/.") >= 0)
			{
				// Retain prefixes like "file:" or "C:"
				String prefix = null;
				int prefixIndex = path.indexOf(":");
				if (prefixIndex >= 0)
				{
					prefix = path.substring(0, prefixIndex + 1);
					path = path.substring(prefixIndex + 1);
				}

				boolean isRootPath = false;
				if (path.charAt(0) == FOLDER_SEP_CHAR)
				{
					// First token will be eaten by StringTokenizer, remember it was there
					isRootPath = true;
				}

				String [] dirs = tokenize(path, FOLDER_SEP, false);

				List res = new ArrayList();

				// Try to get rid of the "." and ".." parts of the path so we have a pretty strait path name
				for (int i = 0; i < dirs.length; ++i)
				{
					String s = dirs[i];

					if (s.equals(".."))
					{
						int rn = res.size();
						if (rn >= 1 && ! res.get(rn - 1).equals(".."))
						{
							res.remove(rn - 1);
							continue;
						}
					}
					else if (s.equals("."))
					{
						continue;
					}
					res.add(dirs[i]);
				}

				StringBuffer sb = new StringBuffer();
				if (prefix != null)
					sb.append(prefix);
				if (isRootPath)
					sb.append(FOLDER_SEP_CHAR);

				boolean first = true;
				for (Iterator it = res.iterator(); it.hasNext();)
				{
					if (first)
						first = false;
					else
						sb.append(FOLDER_SEP);
					sb.append(it.next());
				}

				path = sb.toString();
			}
			else
			{
				int n = path.length();
				if (path.charAt(n - 1) == FOLDER_SEP_CHAR)
				{
					path = path.substring (0, n - 1);
				}
			}
		}

		return path;
	}

	/**
	 * Normalizes a directory specification.
	 * Executes {@link #normalizePathName} and removes leading or trailing folder delimiters.
	 * Also converts Windows path separator ('\') to '/'.
	 *
	 * @param path Path to normalize or null
	 * @return The normalized path or null
	 */
	public static String normalizeDir(String path)
	{
		if (path != null)
		{
			path = normalizePathName(path);
			if (path.charAt(0) == StringUtil.FOLDER_SEP_CHAR)
				path = path.substring(1);
			if (path.charAt(path.length() - 1) == StringUtil.FOLDER_SEP_CHAR)
				path = path.substring(0, path.length() - 1);
		}
		return path;
	}

	/**
	 * Builds a result path from two path components.
	 * Leading or trailing path separtors will be considered
	 *
	 * @param p1 First path component or null
	 * @param p2 Second path component or null
	 * @return The resulting path or null
	 */
	public static String buildPath(String p1, String p2)
	{
		p1 = normalizePathName(p1);
		p2 = normalizePathName(p2);

		if (p1 != null && p1.length() == 0)
			p1 = null;
		if (p2 != null && p2.length() == 0)
			p2 = null;

		if (p2 == null)
			return p1;
		if (p1 == null)
			return p2;

		if (p1.charAt(p1.length() - 1) == StringUtil.FOLDER_SEP_CHAR)
			p1 = p1.substring(0, p1.length() - 1);
		if (p2.charAt(0) == StringUtil.FOLDER_SEP_CHAR)
			p2 = p2.substring(1);

		return p1 + FOLDER_SEP + p2;
	}

	//////////////////////////////////////////////////
	// 
	//////////////////////////////////////////////////

	/**
	 * Appends 2 strings to a string buffer.
	 * @nowarn
	 */
	public static void append(StringBuffer sb, String s1, String s2)
	{
		sb.append(s1);
		sb.append(s2);
	}

	/**
	 * Appends 3 strings to a string buffer.
	 * @nowarn
	 */
	public static void append(StringBuffer sb, String s1, String s2, String s3)
	{
		sb.append(s1);
		sb.append(s2);
		sb.append(s3);
	}

	/**
	 * Appends 4 strings to a string buffer.
	 * @nowarn
	 */
	public static void append(StringBuffer sb, String s1, String s2, String s3, String s4)
	{
		sb.append(s1);
		sb.append(s2);
		sb.append(s3);
		sb.append(s4);
	}

	/**
	 * Appends 5 strings to a string buffer.
	 * @nowarn
	 */
	public static void append(StringBuffer sb, String s1, String s2, String s3, String s4, String s5)
	{
		sb.append(s1);
		sb.append(s2);
		sb.append(s3);
		sb.append(s4);
		sb.append(s5);
	}

	/**
	 * Append a string and a new line character to a string buffer.
	 * @nowarn
	 */
	public static void appendLine(StringBuffer sb, String s1)
	{
		sb.append(s1);
		sb.append('\n');
	}

	/**
	 * Append 2 strings and a new line character to a string buffer.
	 * @nowarn
	 */
	public static void appendLine(StringBuffer sb, String s1, String s2)
	{
		append(sb, s1, s2);
		sb.append('\n');
	}

	/**
	 * Append 3 strings and a new line character to a string buffer.
	 * @nowarn
	 */
	public static void appendLine(StringBuffer sb, String s1, String s2, String s3)
	{
		append(sb, s1, s2, s3);
		sb.append('\n');
	}

	/**
	 * Append 4 strings and a new line character to a string buffer.
	 * @nowarn
	 */
	public static void appendLine(StringBuffer sb, String s1, String s2, String s3, String s4)
	{
		append(sb, s1, s2, s3, s4);
		sb.append('\n');
	}

	/**
	 * Append 5 strings and a new line character to a string buffer.
	 * @nowarn
	 */
	public static void appendLine(StringBuffer sb, String s1, String s2, String s3, String s4, String s5)
	{
		append(sb, s1, s2, s3, s4, s5);
		sb.append('\n');
	}

	//////////////////////////////////////////////////
	// @@ Small helpers
	//////////////////////////////////////////////////

	public static char safeCharAt(String s, int index)
	{
		if (index >= 0 && index < s.length())
			return s.charAt(index);
		return '\0';
	}

	/**
	 * Trims a string, returning null if the trimmed length of the string is 0.
	 *
	 * @param s String to trim or null
	 * @return The trimmed string or null
	 */
	public static String trimNull(String s)
	{
		if (s != null)
		{
			s = s.trim();
			if (s.length() == 0)
				s = null;
		}
		return s;
	}

	/**
	 * Converts an integer to its string representation.
	 * @nowarn
	 */
	public static String int2String(int i)
	{
		return Integer.toString(i, 10);
	}

	/**
	 * Converts a string to its boolean representation.
	 *
	 * @param value String value
	 *
	 * @return
	 *  true    The value string is 'true'<br>
	 *  false   The value string is 'false'.
	 *
	 * @throws IllegalArgumentException The value string doesn't represent a boolean value
	 */
	public static boolean string2boolean(String value)
	{
		boolean b = new Boolean(value).booleanValue();
		if (b)
			return b;

		if (value != null && "false".equalsIgnoreCase(value))
			return b;

		throw new IllegalArgumentException("The value '" + value + "' is not a boolean value.");
	}

	/**
	 * Trims a string from characters.
	 *
	 * @param origin String to trim
	 * @param trimCharacters Array of characters
	 *
	 * @return The trimmed string
	 */
	public static String trim(String origin, char [] trimCharacters)
	{
		if (origin == null)
			return null;

		StringBuffer buf = new StringBuffer();
		int lenOrigin = origin.length();
		for (int i = 0; i < lenOrigin; i++)
		{
			char c = origin.charAt(i);
			boolean characterFound = false;
			for (int j = 0; j < trimCharacters.length; j++)
			{
				if (c == trimCharacters [j])
				{
					characterFound = true;
					break;
				}
			}

			if (!characterFound)
			{
				buf.append(origin.substring(i));
				break;
			}
		}

		int lenBuffer = buf.length();
		for (int i = lenBuffer; i > 0; i--)
		{
			char c = buf.charAt(i - 1);
			boolean characterFound = false;

			for (int j = 0; j < trimCharacters.length; j++)
			{
				if (c == trimCharacters [j])
				{
					characterFound = true;
					break;
				}
			}

			if (!characterFound)
			{
				buf.setLength(i);
				break;
			}
		}

		return buf.toString();
	}

	/**
	 * Checks if the given string list contains the given string.
	 *
	 * @param s String to search for or null
	 * @param list String list or null
	 * @return
	 *		true	If the string is not null and the string list contains the string
	 *		false	Otherwise
	 */
	public static boolean contains(String s, String [] list)
	{
		if (s != null && list != null)
		{
			for (int i = 0; i < list.length; ++i)
			{
				if (s.equals(list [i]))
					return true;
			}
		}
		return false;
	}
}
