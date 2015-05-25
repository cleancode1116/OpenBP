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

/**
 * An object that has a name, a display name and a description.
 *
 * \bNote that this object does not support localization currently!\b<br>
 * \bThe 'locale' parameter of all methods is ignored.\b
 *
 * @author Heiko Erhardt
 */
public class DisplayObjectImpl extends DescriptionObjectImpl
	implements DisplayObject
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Display name of this object */
	private String displayName;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public DisplayObjectImpl()
	{
	}

	/**
	 * Value constructor.
	 *
	 * @param name The internal name of the object
	 */
	public DisplayObjectImpl(String name)
	{
		super(name);
	}

	/**
	 * Value constructor.
	 *
	 * @param name The internal name of the object
	 * @param displayName Display name of this object
	 * @param description Description of this object
	 */
	public DisplayObjectImpl(String name, String displayName, String description)
	{
		super(name, description);
		this.displayName = displayName;
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
		super.copyFrom(source, copyMode);

		DisplayObjectImpl src = (DisplayObjectImpl) source;

		displayName = src.displayName;
	}

	//////////////////////////////////////////////////
	// @@ DisplayObject implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the default display name of this object.
	 * The display name is the human-readable name of the object (i. e. a name that is
	 * displayed in the user interface).
	 * @nowarn
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/**
	 * Sets the default display name of this object.
	 * The display name is the human-readable name of the object (i. e. a name that is
	 * displayed in the user interface).
	 * @nowarn
	 */
	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	/**
	 * Gets text that can be used to display this object.
	 * This is either the display name ({@link #setDisplayName}) or the regular name
	 * ({@link DescriptionObject#setName(String)}) of the object itself.
	 * @nowarn
	 */
	public String getDisplayText()
	{
		String n = getDisplayName();
		return n != null ? n : getName();
	}
}
