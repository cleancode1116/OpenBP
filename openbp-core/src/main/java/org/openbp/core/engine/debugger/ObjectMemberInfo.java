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
package org.openbp.core.engine.debugger;

import java.io.Serializable;

/**
 * Contains information about an arbitrary object member.
 *
 * @author Heiko Erhardt
 */
public class ObjectMemberInfo
	implements Serializable, Comparable
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Name of the member */
	private String key;

	/** Data type */
	private String type;

	/** Textual representation of the member value */
	private String toStringValue;

	/** Flag if this member is a parent object itself */
	private boolean parentMember;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ObjectMemberInfo()
	{
	}

	/**
	 * Value constructor.
	 *
	 * @param key Name of the member
	 * @param type Data type
	 * @param toStringValue Textual representation of the member value
	 * @param parentMember Flag if this member is a parent object itself
	 */
	public ObjectMemberInfo(String key, String type, String toStringValue, boolean parentMember)
	{
		this.key = key;
		this.type = type;
		this.toStringValue = toStringValue;
		this.parentMember = parentMember;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the name of the member.
	 * @nowarn
	 */
	public String getKey()
	{
		return key;
	}

	/**
	 * Sets the name of the member.
	 * @nowarn
	 */
	public void setKey(String key)
	{
		this.key = key;
	}

	/**
	 * Gets the data type.
	 * @nowarn
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * Sets the data type.
	 * @nowarn
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * Gets the textual representation of the member value.
	 * @nowarn
	 */
	public String getToStringValue()
	{
		return toStringValue;
	}

	/**
	 * Sets the textual representation of the member value.
	 * @nowarn
	 */
	public void setToStringValue(String toStringValue)
	{
		this.toStringValue = toStringValue;
	}

	/**
	 * Gets the flag if this member is a parent object itself.
	 * @nowarn
	 */
	public boolean isParentMember()
	{
		return parentMember;
	}

	/**
	 * Sets the flag if this member is a parent object itself.
	 * @nowarn
	 */
	public void setParentMember(boolean parentMember)
	{
		this.parentMember = parentMember;
	}

	/**
	 * This method compares this object with another object that.
	 * The implementation delegates the comparison to the comparison
	 * method of the keys.
	 *
	 * @param that The object to compare to
	 * @return 1 if this.getKey() is alphabetically after that.getKey () etc.
	 */
	public int compareTo(Object that)
	{
		// Depending on the type of this...
		if (that instanceof ObjectMemberInfo)
		{
			// ...perform to key comparison.
			return key.compareTo(((ObjectMemberInfo) that).getKey());
		}

		// ...this means to compare apples and oranges.
		return 0;
	}
}
