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
 * Mapper for an object that is displayed in the tree.
 *
 * @author Baumgartner Michael
 */
public interface DataMapper
{
	/**
	 * Get the number of levels without the leafnode.
	 * @nowarn
	 */
	public int getLevels();

	/**
	 * Create the propertyNode of the level.
	 * @param level Node level
	 * @return The property node
	 */
	public PropertyNode createPropertyNode(int level);

	/**
	 * Create the leaf node for the data object.
	 * @return The leaf node
	 */
	public LeafNode createLeafNode();

	/**
	 * Initialize the mapper with a special object.
	 * @param data Initialization data passed by the caller
	 */
	public void init(Object data);
}
