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
package org.openbp.common.generic;

/**
 * Indicates that an object supports copying it's values
 * and provides a public clone method.
 *
 * @author Heiko Erhardt
 */
public interface Copyable
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/**
	 * Copy mode: Shallow copy.
	 * Reference copy. The original object and the copied object
	 * will reference the same dependent objects.
	 */
	public static final int COPY_SHALLOW = 0;

	/**
	 * Copy mode: First level copy.
	 * Collections and maps will be cloned, but the keys and values
	 * will reference to the same objects.
	 */
	public static final int COPY_FIRST_LEVEL = 1;

	/**
	 * Copy mode: Deep copy.
	 * All dependent objects (including collection and map keys/values)
	 * will be copied recursively.
	 */
	public static final int COPY_DEEP = 2;

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Copies the values of the source object to this object.
	 *
	 * @param source The source object. Must be of the same type as this object.
	 * @param copyMode Determines if a deep copy, a first level copy or a shallow copy is to be
	 * performed. See the constants of the {@link Copyable} class.
	 * @throws CloneNotSupportedException If the cloning of one of the contained objects failed
	 */
	public void copyFrom(Object source, int copyMode)
		throws CloneNotSupportedException;

	/**
	 * Returns a clone of this.
	 *
	 * The clone method is defined by default to be protected.
	 * However, we define it to be public in order to be able to invoke it directly
	 * from outside the class and not needing to use reflections to call it.
	 *
	 * @return The clone (a deep copy of this object)
	 * @throws CloneNotSupportedException If the cloning of one of the contained objects failed
	 */
	public Object clone()
		throws CloneNotSupportedException;
}
