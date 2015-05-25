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
 * Constrain to divide the space the columns on a ratio. If there
 * is no ratio, the columns are divided evenly.
 *
 * @author Baumgartner Michael
 */
public class RatioConstraint extends ColumnSizeConstraint
{
	//////////////////////////////////////////////////
	// @@ Member
	//////////////////////////////////////////////////

	/** The ratios for the columns. */
	private int [] ratio;

	/** The parts, the width of the table must be divided. */
	private int parts;

	/** Flag to fully show the first column. */
	private boolean isFirstColumnFull;

	//////////////////////////////////////////////////
	// @@ Constructor
	//////////////////////////////////////////////////

	/**
	 * Constructor. Only the first column is shown fully. The other
	 * are evenly distributed.
	 * @param firstColumnFull Flag to show the fist column,
	 *	true	Set the size to the maximal width of the column
	 *	false	all columns are evenly distributed
	 */
	public RatioConstraint(boolean firstColumnFull)
	{
		super();
		this.isFirstColumnFull = firstColumnFull;
		ratio = new int [0];
	}

	/**
	 * Constructor. Divide the available space on a ratio for
	 * each column
	 * @param ratio The ratio for the columns. If the first column is
	 *   shown fully, then the first ratio is ignored. If there are not
	 * enought ratios for the table, the other get a ratio of one.
	 * All ratios tha exceed the column count are ignored.
	 * @param firstColumnFull Flag to show the fist column,
	 */
	public RatioConstraint(int [] ratio, boolean firstColumnFull)
	{
		super();
		this.isFirstColumnFull = firstColumnFull;

		this.ratio = new int [ratio.length];
		System.arraycopy(ratio, 0, this.ratio, 0, ratio.length);
	}

	//////////////////////////////////////////////////
	// @@ Implementation
	//////////////////////////////////////////////////

	/**
	 * Initialises the arrays needed for the calculations.
	 * @param treetable The treetable
	 */
	public void calculateColumnSizes(JTreeTable treetable)
	{
		super.calculateColumnSizes(treetable);
		initRatio(treetable);

		int width = treetable.getWidth();
		int partSize = treetable.getWidth() / parts;
		int newParts = parts;

		if (isFirstColumnFull)
		{
			width = width - treeColumnMaxWidth;
			partSize = width / (parts - ratio [0]);
			newParts = parts - ratio [0];
			setColumnWidth(PREFERRED, 0, treeColumnMaxWidth);
		}
		else
		{
			width = width - partSize * ratio [0];
			newParts = parts - ratio [0];
			setColumnWidth(PREFERRED, 0, partSize * ratio [0]);
		}

		for (int i = 1; i < treetable.getColumnCount(); i++)
		{
			int newSize = partSize * ratio [i];
			if (newSize < minSizeOfColumns [i])
			{
				newSize = minSizeOfColumns [i];
				width -= newSize;
				newParts = newParts - ratio [i];
				partSize = width / (newParts == 0 ? 1 : newParts);
			}
			setColumnWidth(PREFERRED, i, newSize);
		}
	}

	//////////////////////////////////////////////////
	// @@ Initialise
	//////////////////////////////////////////////////

	/**
	 * Initialize the ratios for the table.
	 * @param treetable The tree table
	 */
	private void initRatio(JTreeTable treetable)
	{
		if (parts != 0)
			return;

		parts = 0;
		int columns[] = new int [treetable.getColumnCount()];
		for (int i = 0; i < columns.length; i++)
		{
			if (i < ratio.length)
			{
				// There is a ratio for the column
				columns [i] = ratio [i] > 0 ? ratio [i] : 1;
				parts += this.ratio [i];
			}
			else
			{
				// There is no ratio for the column, so set one as ratio
				columns [i] = 1;
				parts++;
			}
		}
		ratio = columns;
	}
}
