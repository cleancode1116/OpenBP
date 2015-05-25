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
package org.openbp.common.generic.description;

import org.openbp.common.CommonUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.generic.msgcontainer.MsgContainer;
import org.openbp.common.string.TextUtil;
import org.openbp.common.util.ToStringHelper;

/**
 * An object that has a name and a description.
 *
 * \bNote that this class does not support localization currently!\b<br>
 * \bThe 'locale' parameter of all methods is ignored.\b
 *
 * @author Heiko Erhardt
 */
public class DescriptionObjectImpl
	implements DescriptionObject, Cloneable, Copyable, Validatable
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Name of this object */
	private String name;

	/** Description of this object */
	private String description;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public DescriptionObjectImpl()
	{
	}

	/**
	 * Value constructor.
	 *
	 * @param name The internal name of the object
	 */
	public DescriptionObjectImpl(String name)
	{
		this.name = name;
	}

	/**
	 * Value constructor.
	 *
	 * @param name The internal name of the object
	 * @param description Description of this object
	 */
	public DescriptionObjectImpl(String name, String description)
	{
		this.name = name;
		this.description = description;
	}

	/**
	 * Creates a clone of this object.
	 * @return The clone (a deep copy of this object)
	 * @throws CloneNotSupportedException If the cloning of one of the contained members failed
	 */
	public Object clone()
		throws CloneNotSupportedException
	{
		DescriptionObjectImpl clone = (DescriptionObjectImpl) super.clone();

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

		DescriptionObjectImpl src = (DescriptionObjectImpl) source;

		name = src.name;
		description = src.description;
	}

	/**
	 * Returns a string representation of this object.
	 * @return "(unqualified class name): '(name)'"
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "name");
	}

	//////////////////////////////////////////////////
	// @@ DescriptionObject implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the name of this object.
	 * This name is the internal name of the object (i. e. a class name).
	 * @nowarn
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name of this object.
	 * This name is the internal name of the object (i. e. a class name).
	 * @nowarn
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the default description of this Object.
	 *
	 * @return The description or null if there is no description available
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Sets the default description of this Object.
	 * The description will be trimmed and spaces after newlines will be removed.
	 * (we need this in order to remove spaces inserted by Castor when serializing
	 * multi-line text content)
	 *
	 * @param description The new description or null
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * Gets the escape representation of the default description of this Object.
	 * In the escape representation, a newline escape ("\n") has been added before any newline character.
	 * Tab characters are replaced by the tab escape ("\t").
	 * (we need this in order to remove spaces inserted by Castor when serializing
	 * multi-line text content)
	 *
	 * @return The description or null if there is no description available
	 */
	public String getDescriptionEscape()
	{
		return TextUtil.encodeMultiLineString(description);
	}

	/**
	 * Sets the escape representation of the default description of this Object.
	 * In the escape representation, a newline escape ("\n") has been added before any newline character.
	 * Tab characters are replaced by the tab escape ("\t").
	 * (we need this in order to remove spaces inserted by Castor when serializing
	 * multi-line text content)
	 *
	 * @param description The new description or null
	 */
	public void setDescriptionEscape(String description)
	{
		this.description = TextUtil.decodeMultiLineString(description);
	}

	/**
	 * Gets text that can be used to describe this object.
	 * By default, this is the description text as returned by the {@link #getDescription} method.
	 * @nowarn
	 */
	public String getDescriptionText()
	{
		return getDescription();
	}

	//////////////////////////////////////////////////
	// @@ Displayable implementation
	//////////////////////////////////////////////////

	/**
	 * Gets text that can be used to display this object.
	 * In this case, this is the regular name ({@link DescriptionObject#setName}) of the object itself.
	 * @nowarn
	 */
	public String getDisplayText()
	{
		return getName();
	}

	//////////////////////////////////////////////////
	// @@ Comparable implementation
	//////////////////////////////////////////////////

	/**
	 * Compares this object to another Object.
	 * If the object is a description object, it will compar the {@link #setName} values of the two objects.
	 * Otherwise, it throws a ClassCastException (as DescriptionObjects are comparable only to other DescriptionObjects).
	 *
	 * @param o Object to be compared
	 * @return  The value 0 if the argument is a string lexicographically equal to this object;<br>
	 * a value less than 0 if the argument is a string lexicographically greater than this object;<br>
	 * and a value greater than 0 if the argument is a string lexicographically less than this object.
	 * @throws ClassCastException if the argument is not a DescriptionObject.
	 */
	public int compareTo(Object o)
	{
		String n1 = getName();
		String n2 = ((DescriptionObject) o).getName();
		return CommonUtil.compareNull(n1, n2);
	}

	//////////////////////////////////////////////////
	// @@ Validatable implementation
	//////////////////////////////////////////////////

	/**
	 * Checks if the object is valid.
	 * The object is invalid if no {@link #setName} has been specified.
	 * @param msgContainer Any errors will be logged to this message container
	 * @return
	 *		true	The object is valid.<br>
	 *		false	Errors were found within the object or its sub objects.
	 */
	public boolean validate(MsgContainer msgContainer)
	{
		if (getName() == null)
		{
			msgContainer.addMsg(this, "No object name specified");
			return false;
		}

		return true;
	}
}
