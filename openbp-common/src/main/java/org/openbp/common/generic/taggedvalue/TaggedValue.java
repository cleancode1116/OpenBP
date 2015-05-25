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
package org.openbp.common.generic.taggedvalue;

import java.io.Serializable;

import org.openbp.common.CommonUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.generic.description.Displayable;

/**
 * A tagged value consists of a name and a string value.
 *
 * @author Heiko Erhardt
 */
public class TaggedValue
	implements Displayable, Comparable, Serializable, Cloneable, Copyable
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Name of the attribute */
	private String name;

	/** Value of the attribute */
	private String value;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public TaggedValue()
	{
	}

	/**
	 * Creates a clone of this object.
	 * @return The clone (a deep copy of this object)
	 * @throws CloneNotSupportedException If the cloning of one of the contained members failed
	 */
	public Object clone()
		throws CloneNotSupportedException
	{
		TaggedValue clone = (TaggedValue) super.clone();

		// Perform a deep copy
		clone.copyFrom(this, Copyable.COPY_DEEP);

		return clone;
	}

	/**
	 * Copies the values of the source object to this object.
	 *
	 * @param source The source object. Must be of the same type as this object.
	 * @param copyMode Determines if a deep copy, a first level copy or a shallow copy is to be
	 * performed. See the constants of the org.openbp.common.generic.description.Copyable class.
	 * @throws CloneNotSupportedException If the cloning of one of the contained objects failed
	 */
	public void copyFrom(Object source, int copyMode)
		throws CloneNotSupportedException
	{
		if (source == this)
			return;

		TaggedValue src = (TaggedValue) source;

		name = src.name;
		value = src.value;
	}

	/**
	 * Returns a string representation of this object.
	 *
	 * @return The value
	 */
	public String toString()
	{
		return value;
	}

	//////////////////////////////////////////////////
	// @@ Displayable implementation
	//////////////////////////////////////////////////

	/**
	 * Gets text that can be used to display this object.
	 *
	 * @return The display text (should usually not be null)
	 */
	public String getDisplayText()
	{
		return name;
	}

	//////////////////////////////////////////////////
	// @@ Comparable implementation
	//////////////////////////////////////////////////

	/**
	 * Compares this object to another Object.
	 * If the object is a tagged value, it will compar the {@link #setName} values of the two objects.
	 * Otherwise, it throws a ClassCastException (as TaggedValues are comparable only to other TaggedValues).
	 *
	 * @param o Object to be compared
	 * @return  The value 0 if the argument is a string lexicographically equal to this object;<br>
	 * a value less than 0 if the argument is a string lexicographically greater than this object;<br>
	 * and a value greater than 0 if the argument is a string lexicographically less than this object.
	 * @throws ClassCastException if the argument is not a TaggedValue.
	 */
	public int compareTo(Object o)
	{
		String n1 = getName();
		String n2 = ((TaggedValue) o).getName();
		return CommonUtil.compareNull(n1, n2);
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the name of the attribute.
	 * @nowarn
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name of the attribute.
	 * @nowarn
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the value of the attribute.
	 * @nowarn
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * Sets the value of the attribute.
	 * @nowarn
	 */
	public void setValue(String value)
	{
		this.value = value;
	}
}
