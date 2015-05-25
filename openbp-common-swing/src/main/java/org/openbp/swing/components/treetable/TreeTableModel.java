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

import javax.swing.table.TableModel;
import javax.swing.tree.TreeModel;

/**
 * The TreeTableModel brings the TableModel and the TreeModel together
 * into one interface.
 *
 * @author Erich Lauterbach
 */
public interface TreeTableModel
	extends TableModel, TreeModel
{
	/**
	 * Used by the JTreeTable to set a reference to itself.
	 *
	 * @param treeTable The {@link JTreeTable} using this model
	 */
	public void setTreeTable(JTreeTable treeTable);

	/**
	 * Determines if a specified cell is selectable.
	 *
	 * @param row The row index of the specified cell
	 * @param column The column index of the specified cell
	 * @nowarn
	 */
	public boolean isCellSelectable(int row, int column);
}
