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
package org.openbp.guiclient.model.item;

import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypeDescriptor;

/**
 * This interface defines a wizard class that can be used to specify item properties.
 * The wizard class can be used to define new items, but may also be used to edit
 * the properties of an existing item.<br>
 * In contrast to the regular property browser, the item wizard guides the user
 * through the process of the item definition and may show more information (e. g.
 * the graphical representation of the item)
 * A good example is the action wizard for action items.
 *
 * For each item wizard for a particular item type, only one instance exists.
 * The item wizard should display itself as modal dialog.
 *
 * The item wizard is referenced by the {@link ItemTypeDescriptor} class.
 *
 * @author Heiko Erhardt
 */
public interface ItemEditor
{
	/**
	 * Opens an item in the item wizard.
	 * This method usually displays the wizard dialog.
	 * The item wizard displays and updates the item structure and/or advanced item properties.
	 *
	 * @param item item to open
	 * @param editedItemStatus Status of the item
	 * @return The edited item or null if the user cancelled the wizard
	 */
	public Item openItem(Item item, int editedItemStatus);
}
