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
package org.openbp.common.string.shellmatcher;

/**
 * The ShellMatcher implements a UNIX-shell-compatible string pattern matcher.
 * For a description of the meta characters that can be used in a pattern, see the {@link #setPattern} method.<br>
 * The methods of this class throw the ShellMatcherException runtime exception on argument
 * or pattern syntax error.
 *
 * @author Heiko Erhardt
 */
public class ShellMatcher
{
	private String pattern;

	private String str;

	private boolean ignoreCase;

	private int pl;

	private int sl;

	/**
	 * Default constructor
	 */
	public ShellMatcher()
	{
	}

	/**
	 * Value constructor.
	 *
	 * @param patternArg The pattern to match strings against
	 *
	 * @seem setPattern
	 */
	public ShellMatcher(String patternArg)
	{
		setPattern(patternArg);
	}

	/**
	 * Sets the pattern.
	 *
	 * @param pattern The pattern may contain the following meta characters:<br>
	 *		\c*\ \ \ \ \ \ \       \c 0 or more arbitrary characters<br>
	 *		\c?\ \ \ \ \ \ \       \c exactly 1 arbitrary character<br>
	 *		\c[\ilist\i]\ \	   \c one of the characters in the list<br>
	 *		\c[^\ilist\i]\         \c one of the characters not in the list<br>
	 *		\c[\il\i-\ir\i]\ \ \   \c one of the characters between l and r<br>
	 *		\c[^\il\i-\ir\i]\ \    \c one of the characters not between l and r<br>
	 *		\c\\ \ \ \ \ \ \       \c escape character
	 */
	public void setPattern(String pattern)
	{
		this.pattern = pattern;
		pl = pattern != null ? pattern.length() : 0;
	}

	/**
	 * Sets the ignore case option.
	 *
	 * @param ignoreCase
	 *		true	The character case will be ignored when comparing literals.<br>
	 *		false	The pattern matcher will distinguish between lower and upper case.
	 */
	public void setIgnoreCase(boolean ignoreCase)
	{
		this.ignoreCase = ignoreCase;
	}

	/**
	 * Matches a string against the current pattern.
	 * A pattern must have been specified before calling this method.
	 *
	 * @param s The String to match against the pattern
	 * @return @return
	 *		true	The string matches the pattern<br>
	 *		false	otherwise
	 * @throws ShellMatcherException On argument or pattern syntax error
	 */
	public boolean match(String s)
	{
		if (s == null)
			throw new ShellMatcherException("Invalid string argument");

		if (pattern == null)
			throw new ShellMatcherException("Invalid pattern argument");

		str = s;
		sl = str.length();
		return match(0, 0);
	}

	/**
	 * Does the actual match, starting at the specified string indices
	 *
	 * @param si String index
	 * @param pi Pattern index
	 * @return
	 *		true	The string matches the pattern.<br>
	 *		false	otherwise
	 * @throws ShellMatcherException On pattern syntax error
	 */
	private boolean match(int si, int pi)
	{
		boolean inclusive = false;

		char sc;
		char pc;
		for (;; ++si, ++pi)
		{
			pc = pi < pl ? pattern.charAt(pi) : '\0';
			if (si < sl)
			{
				sc = str.charAt(si);
				if (ignoreCase)
					sc = Character.toLowerCase(sc);
			}
			else
			{
				sc = '\0';
			}

			switch (pc)
			{
			case '\0':
				// End of pattern matches if end of string is reached
				return sc == '\0';

			case '?':
				// '?' matches exactly one character
				if (sc == '\0')
					return false;
				continue;

			case '[':
				// '[]' defines a set of acceptable characters
				inclusive = true; // Default: Inclusive list
				for (;;)
				{
					pc = getPatternChar(++pi);

					if (pc == ']')
					{
						// End of list reached
						if (inclusive)
							return false;
						break;
					}

					if (pc == '^')
					{
						// Exclusive list
						inclusive = false;
						continue;
					}

					if (pc == '-')
					{
						// Limited set ("A-Z" e.g.)
						char pc2 = pattern.charAt(pi - 1);
						if (pc2 == '[')
							throw new ShellMatcherException("Syntax error in pattern, pos " + pi);

						pc = getPatternChar(++pi);
						if (pc == ']')
						{
							// Syntax error
							return false;
						}

						if (sc >= pc2 && sc <= pc)
						{
							if (inclusive)
							{
								do
								{
									pc = getPatternChar(++pi);
								}
								while (pc != ']');
								break;
							}
							return false;
						}
						else if (inclusive)
							return false;
						continue;
					}

					if (sc == pc)
					{
						if (!inclusive)
							return false;

						do
						{
							pc = getPatternChar(++pi);
						}
						while (pc != ']');
						break;
					}
				}
				continue;

			case '*':
				// '*' matches zero or more characters
				++pi;
				while (sc != '\0')
				{
					if (match(si++, pi))
						return true;
					sc = si < sl ? str.charAt(si) : '\0';
				}
				if (pi >= pl)
					return true;
				return false;

			case '\\':
				// Escape character
				pc = getPatternChar(++pi);

			// FALLTHROUGH

			default:
				// Any other character must exactly match
				if (sc == pc)
					continue;
				if (ignoreCase)
				{
					if (sc == Character.toLowerCase(pc))
						continue;
				}
				return false;
			}
		}
	}

	/**
	 * Gets a character from the pattern string.
	 *
	 * @param pi Pattern index
	 * @return The character
	 * @throws ShellMatcherException If there are no more characters
	 */
	private char getPatternChar(int pi)
	{
		if (pi >= pl)
			throw new ShellMatcherException("Syntax error in pattern, pos " + pi);
		char c = pattern.charAt(pi);
		if (ignoreCase)
			c = Character.toLowerCase(c);
		return c;
	}
}
