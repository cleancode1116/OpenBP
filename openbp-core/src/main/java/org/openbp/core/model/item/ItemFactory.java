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
package org.openbp.core.model.item;

import org.openbp.core.model.Model;

/**
 * Factory interface used for instantiating items.
 * @seec ItemTypeDescriptor
 *
 * @author Heiko Erhardt
 */
public interface ItemFactory
{
	//////////////////////////////////////////////////
	// @@ Factory methods
	//////////////////////////////////////////////////

	/**
	 * Creates a new item.
	 * The default implementation just performs Class.newInstance on the item class
	 * and assigns a name to the item that
	 *
	 * @param model Model the item shall belong to
	 * @param suggestedName Suggested name or null for a default item-type-specific name.
	 * The method guarantees that the generated name will be unique within the items of this type of this model.
	 * @param suggestedDisplayName Suggested display name or null
	 * @return The new item instance or null on error
	 */
	public abstract Item createItem(Model model, String suggestedName, String suggestedDisplayName);
}
