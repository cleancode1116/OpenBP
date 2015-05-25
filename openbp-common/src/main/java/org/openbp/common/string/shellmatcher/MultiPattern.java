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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.openbp.common.ReflectUtil;
import org.openbp.common.string.StringUtil;

/**
 * A multi pattern defines a single search criteria for the {@link MultiMatcher} class.
 *
 * @author Heiko Erhardt
 */
public class MultiPattern
	implements Serializable
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Attribute name */
	private String attributeName;

	/** Pattern value (contains arbitrary objects) */
	private List patternValues = new ArrayList();

	/** String matches must be exact matches. */
	private boolean exactMatch = false;

	/** Name of the attribute access method */
	private transient String methodName;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public MultiPattern()
	{
	}

	/**
	 * Value constructor.
	 *
	 * @param attributeName Name of the attribute
	 * @param patternValue Pattern value
	 */
	public MultiPattern(String attributeName, Object patternValue)
	{
		setAttributeName(attributeName);
		addPatternValue(patternValue);
	}

	//////////////////////////////////////////////////
	// @@ Matching
	//////////////////////////////////////////////////

	/**
	 * Determines if an object matches the pattern.
	 *
	 * @param o Object to match against the pattern
	 * @return
	 *		true	The object matches the pattern.<br>
	 *		false	The object does not match.
	 */
	public boolean match(Object o)
	{
		int nPatterns = patternValues.size();

		if (methodName != null)
		{
			// Determine the method name
			if (nPatterns > 0 && (patternValues.get(0) instanceof Boolean))
				methodName = "is";
			else
				methodName = "get";
			methodName += StringUtil.capitalize(attributeName);
		}

		Class cls = o.getClass();

		try
		{
			// Determine the getter method
			Method method = cls.getMethod(methodName, (Class []) null);

			// Get the attribute value
			Object value = method.invoke(o, (Object []) null);

			for (int i = 0; i < nPatterns; ++i)
			{
				if (! matchValue(value, patternValues.get(i)))
					return false;
			}

			// All matches succeeded
			return true;
		}
		catch (NoSuchMethodException nsme)
		{
			throw new ShellMatcherException("Cannot find method '" + methodName + "' in class '" + ReflectUtil.getPrintableClassName(cls) + "'");
		}
		catch (IllegalAccessException iae)
		{
			throw new ShellMatcherException("Cannot execute method '" + methodName + "' in class '" + ReflectUtil.getPrintableClassName(cls) + "'");
		}
		catch (InvocationTargetException iae)
		{
			throw new ShellMatcherException("Error executing method '" + methodName + "' in class '" + ReflectUtil.getPrintableClassName(cls) + "'");
		}
	}

	private boolean matchValue(Object value, Object patternValue)
	{
		// Check for null values
		if (value == null && patternValue == null)
		{
			// Both null
			return true;
		}
		if (patternValue == null || value == null)
			return false;

		if (value instanceof String)
		{
			// Perform string/pattern pattern match
			String valueString = (String) value;

			if (patternValue instanceof String)
			{
				return matchString(valueString, (String) patternValue);
			}

			if (patternValue instanceof ShellMatcher)
			{
				ShellMatcher matcher = (ShellMatcher) patternValue;
				return matcher.match(valueString);
			}

			return false;
		}

		// Arbitrary object match
		return value.equals(patternValue);
	}

	private boolean matchString(String value, String pattern)
	{
		if (exactMatch)
		{
			return value.equalsIgnoreCase(pattern);
		}

		int vl = value.length();
		int pl = pattern.length();
		int max = vl - pl;
		for (int i = 0; i <= max; ++i)
		{
			if (value.regionMatches(true, i, pattern, 0, pl))
				return true;
		}

		return false;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Sets the attribute name.
	 * @nowarn
	 */
	public void setAttributeName(String attributeName)
	{
		this.attributeName = attributeName;
	}

	/**
	 * Adds a pattern value.
	 * @nowarn
	 */
	public void addPatternValue(Object patternValue)
	{
		if (patternValue instanceof String)
		{
			String s = (String) patternValue;

			if (isPattern(s))
			{
				ShellMatcher matcher = new ShellMatcher(s);
				matcher.setIgnoreCase(true);
				addPatternValue(matcher);
				return;
			}
		}
		addPatternValue(patternValue);
	}

	/**
	 * Sets the string matching strategy.
	 * @param exactMatch
	 *		true	String matches must be exact matches.<br>
	 *		false	String must contain the pattern.
	 */
	public void setExactMatch(boolean exactMatch)
	{
		this.exactMatch = exactMatch;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Checks if the string contains the specified character
	 * and if it is not escaped.
	 * @nowarn
	 */
	private boolean isPattern(String s)
	{
		boolean escape = false;
		int l = s.length();
		for (int i = 0; i < l; ++i)
		{
			char c = s.charAt(i);

			switch (c)
			{
			case '\\':
				escape = !escape;
				continue;

			case '*':
			case '?':
			case '[':
				if (!escape)
				{
					// Found a pattern character
					return true;
				}
				break;

			default:
			}
			escape = false;
		}

		return false;
	}
}
