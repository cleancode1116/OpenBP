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
package org.openbp.guiclient.model.item.itemfilter;

/**
 * Listener interface for components that use the item filter manager class ({@link ItemFilterMgr}).
 *
 * @author Heiko Erhardt
 */
public interface ItemFilterListener
{
	/**
	 * Called if the current item filter settings have changed, i\.e\. the item filters
	 * should be (re-)applied.
	 *
	 * @param mgr Item filter manager that issues the event
	 * @param filter Item filter that has changed or null
	 */
	public void applyFilter(ItemFilterMgr mgr, ItemFilter filter);
}
