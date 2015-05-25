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

import java.util.ArrayList;
import java.util.List;

import org.openbp.common.CollectionUtil;

/**
 * Utility class that replaces patterns in a string with replacement strings.
 * Supports case-insensitive replacements. Also optionally trims spaces inside the string.
 *
 * @author Heiko Erhardt
 */
public class StringReplacer
{
	//////////////////////////////////////////////////
	// @@ Private data
	//////////////////////////////////////////////////

	/** Trim spaces and newlines */
	private boolean trim;

	/** Ignore case? */
	private boolean ignoreCase;

	/** Table of patterns */
	private String [] patterns;

	/** Length of the pattern objects */
	private int [] patLens;

	/** List of patterns */
	private List vPattern;

	/** Table of replacements */
	private String [] replacements;

	/** List of substitutes */
	private List vReplacements;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public StringReplacer()
	{
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Adds a pattern to the replacer.
	 *
	 * @param pattern The pattern
	 * @param replacement The substitute that will replace the pattern in the string
	 * or null to remove the pattern from the string
	 */
	public void addReplacement(String pattern, String replacement)
	{
		// Reset pattern and replacements table
		patterns = null;
		replacements = null;

		if (vPattern == null)
			vPattern = new ArrayList();
		vPattern.add(pattern);
		if (vReplacements == null)
			vReplacements = new ArrayList();
		vReplacements.add(replacement);
	}

	/**
	 * Sets the ignore case option.
	 *
	 * @param ignoreCase
	 *		true	The character case will be ignored when comparing strings.<br>
	 *		false	The pattern matcher will distinguish between lower and upper case.
	 */
	public void setIgnoreCase(boolean ignoreCase)
	{
		this.ignoreCase = ignoreCase;
	}

	/**
	 * Sets the trim mode.
	 *
	 * @param trim
	 *		true	Removes overflous spaces and newlines, i. e. skips succeeding
	 *				newslines and spaces<br>
	 *		false	Leaves spaces and newlines as they are
	 */
	public void setTrim(boolean trim)
	{
		this.trim = trim;
	}

	/**
	 * Performs the processing.
	 *
	 * @param s String to process
	 * @return The result of the processing
	 */
	public String process(String s)
	{
		prepare();

		int len = s.length();
		StringBuffer retVal = new StringBuffer(len);

		boolean seenSpace = false;
		boolean seenNewLine = false;
		int nPattern = patterns != null ? patterns.length : 0;

		for (int i = 0; i < len; i++)
		{
			char c = s.charAt(i);

			if (trim)
			{
				if (c == ' ' || c == '\t')
				{
					if (seenSpace)
						continue;
					seenSpace = true;
					retVal.append(' ');
					continue;
				}
				else if (c == '\n' || c == '\r')
				{
					if (seenNewLine)
						continue;
					seenNewLine = true;
					seenSpace = true;
					retVal.append(c == '\r' ? "\r\n" : "\n");
					continue;
				}
				else
				{
					seenSpace = false;
					seenNewLine = false;
				}
			}

			if (nPattern != 0)
			{
				int iPattern;
				for (iPattern = 0; iPattern < nPattern; iPattern++)
				{
					if (s.regionMatches(ignoreCase, i, patterns [iPattern], 0, patLens [iPattern]))
					{
						break;
					}
				}

				if (iPattern < nPattern)
				{
					String value = replacements [iPattern];
					if (value != null)
						retVal.append(value);

					i += patLens [iPattern] - 1;
					continue;
				}
			}

			retVal.append(c);
		}

		return retVal.toString();
	}

	/**
	 * Prepare for processing.
	 */
	private void prepare()
	{
		if (patterns == null)
		{
			if (vPattern != null)
			{
				patterns = CollectionUtil.toStringArray(vPattern);
				if (patterns != null)
				{
					int nPattern = patterns.length;
					patLens = new int [nPattern];
					for (int i = 0; i < nPattern; ++i)
					{
						patLens [i] = patterns [i].length();
					}
				}
			}
		}

		if (replacements == null)
		{
			if (vReplacements != null)
			{
				replacements = CollectionUtil.toStringArray(vReplacements);
			}
		}
	}
}
