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
package org.openbp.core.model.item.process;

import org.openbp.core.model.item.Item;

/**
 * An item provider is an object that can be converted into an item.
 * Typical examples are activity nodes, which provide activities.
 *
 * @author Stephan Moritz
 */
public interface ItemProvider
{
	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Copy between the tem and the item provider.
	 * Copies all data values that can be mapped between the two types.<br>
	 * Also creates a new, unique name for the node.
	 *
	 * @param item Item to copy from
	 * @param syncFlags Synchronization flags (see the constants of the {@link ItemSynchronization} class)
	 */
	public void copyFromItem(Item item, int syncFlags);

	/**
	 * Copy between item provider and item.
	 *
	 * @param item Item to copy to
	 * @param syncFlags Synchronization flags (see the constants of the {@link ItemSynchronization} class)
	 */
	public void copyToItem(Item item, int syncFlags);
}
