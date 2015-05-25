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

import org.openbp.jaspira.plugin.Plugin;

/**
 * An item filter holder is a component that 'employs' an item filter.
 * It will be notified by the item filter if the filter settings change.
 *
 * @author Heiko Erhardt
 */
public interface ItemFilterHolder
{
	/**
	 * Gets the plugin the item filter holder is associated with (to be used as event source).
	 * @nowarn
	 */
	public Plugin getPlugin();

	/**
	 * (Re-)applies the filter.
	 * This method will be called after the status of a filter has changed.
	 *
	 * @param filter Filter that wishes to be applied or null
	 */
	public void apply(ItemFilter filter);
}
