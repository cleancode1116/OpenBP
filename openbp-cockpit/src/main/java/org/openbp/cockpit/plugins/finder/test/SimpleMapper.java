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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.openbp.cockpit.plugins.finder.treemodel.DataMapper;
import org.openbp.cockpit.plugins.finder.treemodel.LeafNode;
import org.openbp.cockpit.plugins.finder.treemodel.NodeMapper;
import org.openbp.cockpit.plugins.finder.treemodel.PropertyNode;

/**
 * Data mapper test class.
 *
 * @author Baumgartner Michael
 */
public class SimpleMapper
	implements DataMapper
{
	private List list = new ArrayList();

	private static NodeMapper nodeMapper = new StringMapper();

	/**
	 * @see org.openbp.cockpit.plugins.finder.treemodel.DataMapper#init(Object)
	 */
	public void init(Object leafObject)
	{
		String str = (String) leafObject;
		StringTokenizer token = new StringTokenizer(str, "/");
		while (token.hasMoreTokens())
		{
			list.add(token.nextToken());
		}
	}

	/**
	 * @see org.openbp.cockpit.plugins.finder.treemodel.DataMapper#createLeafNode()
	 */
	public LeafNode createLeafNode()
	{
		LeafNode node = new LeafNode();
		node.setNodeMapper(nodeMapper);
		node.setLeafData(list.get(list.size() - 1));
		return node;
	}

	/**
	 * @see org.openbp.cockpit.plugins.finder.treemodel.DataMapper#createPropertyNode(int)
	 */
	public PropertyNode createPropertyNode(int level)
	{
		PropertyNode node = new PropertyNode();
		node.setNodeMapper(nodeMapper);
		node.setPropertyData(list.get(level));
		return node;
	}

	/**
	 * @see org.openbp.cockpit.plugins.finder.treemodel.DataMapper#getLevels()
	 */
	public int getLevels()
	{
		return list.size() - 1;
	}
}

class StringMapper
	implements NodeMapper
{
	/**
	 * @see org.openbp.cockpit.plugins.finder.treemodel.NodeMapper#getDisplayString(Object)
	 */
	public String getDisplayString(Object nodeData)
	{
		return (String) nodeData;
	}
}
