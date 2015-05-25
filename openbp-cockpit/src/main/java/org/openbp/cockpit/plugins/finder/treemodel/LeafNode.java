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
package org.openbp.cockpit.plugins.finder.treemodel;

/**
 * Leaf node of the tree.
 *
 * @author Baumgartner Michael
 */
public class LeafNode extends GenericNode
{
	/**
	 * Get the data of the leaf node.
	 * @return the data
	 */
	public Object getLeafData()
	{
		return nodeData;
	}

	/**
	 * Sset the data of the leaf node.
	 * @param leafData the data
	 */
	public void setLeafData(Object leafData)
	{
		this.nodeData = leafData;
	}
}
