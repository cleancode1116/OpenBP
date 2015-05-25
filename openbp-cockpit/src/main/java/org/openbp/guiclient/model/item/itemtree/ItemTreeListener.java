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
package org.openbp.guiclient.model.item.itemtree;

import java.util.EventListener;

/**
 * An item tree listener may register with the item tree in order to react
 * on item tree-related events.
 *
 * @author Heiko Erhardt
 */
public interface ItemTreeListener
	extends EventListener
{
	/**
	 * Called when an item tree event has happened.
	 *
	 * @param e Item tree event holding the event information
	 */
	public void handleItemTreeEvent(ItemTreeEvent e);
}
