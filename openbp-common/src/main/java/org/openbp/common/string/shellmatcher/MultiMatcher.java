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
import java.util.ArrayList;
import java.util.List;

/**
 * This matcher class performs matches of objects against arbitrary search criteria.
 * Each criteria (class {@link MultiPattern}) consists of an attribute name and a pattern.
 * The pairs might be connected using an 'and' or an 'or' operation.
 *
 * The attribute name of a pattern specifies a field of the object to match.<br>
 * The matcher will look for an appropriate getter method (get\iAttributeName\i (),
 * is\iAttributeName\i for boolean values) to perform the match.
 *
 * The attribute value can be one of the following types:
 * <ul>
 * <li>
 * \cString\c: The actual attribute value must either match exactly the \cattributeValue\c
 * parameter ({@link MultiPattern#setExactMatch(boolean)} = true) or must contain the attribute value
 * ({@link MultiPattern#setExactMatch(boolean)} = false). If the pattern contains pattern matching characters
 * ("*?[]"), a pattern matching (see the {@link ShellMatcher} class) will be performed on the
 * attribute value.
 * </li>
 * <li>
 * \cString []\c: The actual attribute value must contain all strings in the array.
 * As with a single string, either a string comparison or a pattern matching will be performed.
 * </li>
 * <li>
 * Any other data type: The values will be compared using the equals method.<br>
 * Null values will match null values accordingly.
 * </li>
 * </ul>
 *
 * Example:<br>
 * @code 3
 * MultiMatcher mm = new MultiMatcher ();
 *
 * MultiPattern mp = new MultiPattern ("description");
 * mp.addPatternString ("user");
 * mp.addPatternString ("create*object");
 * mm.addPattern (mp);
 *
 * mm.addPattern (new MultiPattern ("status", "production" }));
 * mm.addPattern (new MultiPattern ("deployed", new Boolean (true) }));
 * @code
 * This will match all objects that<br>
 * - contain the word "user" and the pattern "create*object" their description (getDescription method)<br>
 * - have production status (getStatus () == "production")<br>
 * - are deployed (isDeployed () == true)
 *
 * @author Heiko Erhardt
 */
public class MultiMatcher
	implements Serializable
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** List of patterns (contains {@link MultiPattern} objects) */
	private List patterns = new ArrayList();

	/**
	 * Relation type.
	 *		true: OR relation<br>
	 *		false: AND relation
	 */
	private boolean orRelation = false;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor
	 */
	public MultiMatcher()
	{
	}

	/**
	 * Adds a pattern.
	 * @param attributeName Name of the attribute to match
	 * @param attributeValue Value of the attribute (see class comment)
	 */
	public void addPattern(String attributeName, Object attributeValue)
	{
		MultiPattern pattern = new MultiPattern(attributeName, attributeValue);
		addPattern(pattern);
	}

	/**
	 * Adds a pattern.
	 * @param pattern Pattern to add
	 */
	public void addPattern(MultiPattern pattern)
	{
		patterns.add(pattern);
	}

	//////////////////////////////////////////////////
	// @@ Attribute access
	//////////////////////////////////////////////////

	/**
	 * Gets the relation type.
	 * @return
	 *		true: OR relation<br>
	 *		false: AND relation
	 */
	public boolean isOrRelation()
	{
		return orRelation;
	}

	/**
	 * Sets the relation type.
	 * @param orRelation
	 *		true: OR relation<br>
	 *		false: AND relation
	 */
	public void setOrRelation(boolean orRelation)
	{
		this.orRelation = orRelation;
	}

	//////////////////////////////////////////////////
	// @@ Matching
	//////////////////////////////////////////////////

	/**
	 * Determines if an object matches the criteria.
	 *
	 * @param o Object to match against the pattern
	 * @return
	 *		true	The object matches the pattern.<br>
	 *		false	The object does not match.
	 */
	public boolean match(Object o)
	{
		int n = patterns.size();
		for (int i = 0; i < n; ++i)
		{
			MultiPattern pattern = (MultiPattern) patterns.get(i);

			boolean result = pattern.match(o);

			if (orRelation)
			{
				if (result)
				{
					// One of the sub matches succeeded, so the entire match succeeds
					return true;
				}
			}
			else
			{
				if (!result)
				{
					// One of the sub matches failed, so the entire match failed
					return false;
				}
			}
		}

		// No match succeeded so far -> fail
		// No match failed so far -> success
		return !orRelation;
	}
}
