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
package org.openbp.guiclient.model.item.itemfinder;

import java.util.List;

import org.openbp.core.model.ModelObject;

/**
 * Implementation to help other finder.
 *
 * @author Baumgartner Michael
 */
public abstract class FinderImpl
	implements Finder
{
	/**
	 * Check if two model object are the same. If true, then the given core
	 * object is added to the list.
	 * @param objectToAdd The model object that will be added to the list
	 * @param itemToCheck The model object to be checked
	 * @param refItem The model object to search for
	 * @param coreList The list with previously found {@link ModelObject} objects
	 */
	protected void addIfMatch(ModelObject objectToAdd, ModelObject itemToCheck, ModelObject refItem, List coreList)
	{
		if (itemToCheck == null)
		{
			// The node does not contain an item, when the item was deleted.
			// The auditor checks this error.
			return;
		}
		if (itemToCheck == refItem)
			coreList.add(objectToAdd);
	}
}
