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
package org.openbp.jaspira.decoration;

import java.util.HashSet;
import java.util.Set;

/**
 * The list decorator is backuped by an internal list of qualifying owners.
 * It supports adding and removal of owners.
 *
 * @author Stephan Moritz
 */
public abstract class ListDecorator extends FilteredDecorator
{
	/** Owners of the decorator */
	private Set owners = new HashSet();

	/**
	 * Checks for qualification of an object.
	 * @return
	 *		true	If the object is part of the internal list of this decorator.<br>
	 *		false	If the object doesn't qualify.
	 */
	public boolean qualifies(Object owner)
	{
		return owner != null && owners.contains(owner);
	}

	/**
	 * Adds an owner to the internal list,
	 *
	 * @param owner Owner to add
	 */
	public void add(Object owner)
	{
		owners.add(owner);
	}

	/**
	 * Removes an owner from the internal list.
	 *
	 * @param owner Owner to remove
	 */
	public void remove(Object owner)
	{
		owners.remove(owner);
	}

	/**
	 * Clears the owner list.
	 */
	public void clear()
	{
		owners.clear();
	}

	/**
	 * Checks if the list of owners is empty.
	 * @nowarn
	 */
	public boolean isEmpty()
	{
		return owners.isEmpty();
	}
}
