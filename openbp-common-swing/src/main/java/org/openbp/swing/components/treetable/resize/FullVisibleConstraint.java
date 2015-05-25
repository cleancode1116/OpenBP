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
package org.openbp.swing.components.treetable.resize;

import org.openbp.swing.components.treetable.JTreeTable;

/**
 * Constrain to set column that should be fully visible.
 *
 * @author Baumgartner Michael
 */
public class FullVisibleConstraint extends ColumnSizeConstraint
{
	/** Columns that should be fully visible or not. */
	private boolean [] visible;

	/**
	 * Constructor.
	 * @param visible Array with flag, whether to show the column full or nor
	 */
	public FullVisibleConstraint(boolean [] visible)
	{
		super();
		this.visible = visible;
	}

	/**
	 * @copy ColumnSizeConstraint.calculateColumnSizes
	 */
	public void calculateColumnSizes(JTreeTable treetable)
	{
		super.calculateColumnSizes(treetable);
		int width = treetable.getWidth();
		int parts = 0;

		// Set the width for the columns that should be fully visible
		for (int i = 0; i < treetable.getColumnCount(); i++)
		{
			if (i < visible.length && visible [i])
				width -= getPreferredWidthOfColumn(i);
			else
				parts++;
		}

		// Set the rest width evenly for the other columns
		int partSize = width / parts;
		for (int i = 0; i < treetable.getColumnCount(); i++)
		{
			if (i >= visible.length || !visible [i])
				setColumnWidth(PREFERRED, i, partSize);
		}
	}
}
