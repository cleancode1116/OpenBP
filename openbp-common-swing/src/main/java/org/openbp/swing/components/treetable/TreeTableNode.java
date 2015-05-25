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
package org.openbp.swing.components.treetable;

import java.awt.Dimension;

import javax.swing.tree.TreeNode;

/**
 * The TreeTableNode is an extension of the javax.swing.tree.TreeNode, and is specifically
 * used by the tree contained in the {@link JTreeTable} It provides some extra methods required
 * by the {@link JTreeTable} to determine the variable heights of components that may be contained
 * in a cell. The {@link #setLastHeight} and {@link #getLastHeight} are exclusively used by the
 * {@link JTreeTable}
 *
 * @author Erich Lauterbach
 */
public interface TreeTableNode
	extends TreeNode
{
	/**
	 * Gets the text field to be displayed for the node.
	 *
	 * @return The string value of the text
	 */
	public String getNodeText();

	/**
	 * Gets the preferred height of the node in the tree.
	 *
	 * @return The height as an int in pixels
	 */
	public Dimension getPreferredSize();

	/**
	 * Returns the column value for specified column index.
	 *
	 * @param columnIndex The of the column object to be returned
	 * @return The column value as an object
	 */
	public Object getColumnValue(int columnIndex);

	/**
	 * Gets the height used last by the {@link JTreeTable} for this node.
	 *
	 * @return The last height used
	 */
	public int getLastHeight();

	/**
	 * Sets the current height being used by the {@link JTreeTable} for later use.
	 *
	 * @param height The height being used
	 */
	public void setLastHeight(int height);
}
