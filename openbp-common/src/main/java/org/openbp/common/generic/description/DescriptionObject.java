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

import java.io.Serializable;

/**
 * An object that has a name and a description.
 *
 * @author Heiko Erhardt
 */
public interface DescriptionObject
	extends Displayable, Comparable, Serializable
{
	/**
	 * Creates a clone of this object.
	 * @return The clone (a deep copy of this object)
	 * @throws CloneNotSupportedException If the cloning of one of the contained members failed
	 */
	public Object clone()
		throws CloneNotSupportedException;

	/**
	 * Gets the name of this Object.
	 * This name is the internal name of the object (i. e. a class name).
	 *
	 * @return The name
	 */
	public String getName();

	/**
	 * Sets the name of this Object.
	 * This name is the internal name of the object (i. e. a class name).
	 *
	 * @param name The new name (may not be null)
	 */
	public void setName(String name);

	/**
	 * Gets the default description of this Object.
	 *
	 * @return The description or null if there is no description available
	 */
	public String getDescription();

	/**
	 * Sets the default description of this Object.
	 *
	 * @param description The new description or null
	 */
	public void setDescription(String description);

	/**
	 * Gets text that can be used to describe this object.
	 * By default, this is the description text as returned by the {@link #getDescription} method.
	 * @nowarn
	 */
	public String getDisplayText();

	/**
	 * Gets text that can be used to describe this object.
	 * By default, this is the description text as returned by the {@link #getDescription} method.
	 * @nowarn
	 */
	public String getDescriptionText();
}
