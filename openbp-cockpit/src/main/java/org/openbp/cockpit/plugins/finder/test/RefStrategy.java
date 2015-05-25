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
package org.openbp.cockpit.plugins.finder.test;

import java.util.Comparator;

import org.openbp.cockpit.plugins.finder.treemodel.DataMapper;
import org.openbp.cockpit.plugins.finder.treemodel.Strategy;

/**
 * Strategy for the grouping of the exclusions. At first they
 * are sorted by the taskname, after that they are grouped by
 * the item type of the component to exclude from the checks.
 *
 * @author Baumgartner Michael
 */
public class RefStrategy
	implements Strategy
{
	/**
	 * @see org.openbp.cockpit.plugins.finder.treemodel.Strategy#createDataMapper(Object)
	 */
	public DataMapper createDataMapper(Object leafData)
	{
		DataMapper mapper = new SimpleMapper();
		mapper.init(leafData);
		return mapper;
	}

	/**
	 * @see org.openbp.cockpit.plugins.finder.treemodel.Strategy#getTreeComparator()
	 */
	public Comparator getTreeComparator()
	{
		return null;
	}
}
