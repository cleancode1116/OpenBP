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

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.openbp.swing.components.treetable.JTreeTable;
import org.openbp.swing.components.treetable.SimpleTreeTableModel;
import org.openbp.swing.components.treetable.TreeTableNode;

/**
 * Constraint for the column width. Each column get the preferred
 * width of the component with the maximal width.
 *
 * @author Baumgartner Michael
 */
public class ColumnSizeConstraint
{
	//////////////////////////////////////////////////
	// @@ Member
	//////////////////////////////////////////////////

	/** The absolute minimal width of a column. */
	protected int absoluteMinSize = 40;

	/** Index for max width. */
	protected static final int MAX = 0;

	/** Index for min width. */
	protected static final int MIN = 1;

	/** Index for preferred width. */
	protected static final int PREFERRED = 2;

	/** Hold the mix/max/prefered sizes for each column. */
	protected int [][] columnSizes;

	/** Minimal size of the columns. */
	protected int [] minSizeOfColumns = new int [0];

	/** Max width of tree column. */
	protected int treeColumnMaxWidth = 0;

	/** List with {@link MaxColumnWidth} object to store the maximal width of a column
	 * that is used for the automatic resize. */
	private List maxColumnSize;

	//////////////////////////////////////////////////
	// @@ Initialise
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public ColumnSizeConstraint()
	{
		maxColumnSize = new ArrayList();
	}

	/**
	 * Initialises the arrays needed for the calculations.
	 * @param treetable The treetable
	 */
	private void init(JTreeTable treetable)
	{
		int columnCount = treetable.getColumnCount();
		if (columnSizes == null || columnCount != columnSizes [0].length)
			columnSizes = new int [3] [columnCount];

		if (minSizeOfColumns.length != columnCount)
		{
			minSizeOfColumns = new int [columnCount];
			for (int i = 0; i < minSizeOfColumns.length; i++)
			{
				minSizeOfColumns [i] = absoluteMinSize;
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Calculate methods
	//////////////////////////////////////////////////

	/**
	 * Calculates the size of each column in the tree table.
	 * The results can be queried by the {@link #getMinWidthOfColumn},
	 * {@link #getMaxWidthOfColumn} and {@link #getPreferredWidthOfColumn}.
	 * The sizes don't change until the method is re-executed.
	 * @param treetable The treetable whose columns are to be resized
	 */
	public void calculateColumnSizes(JTreeTable treetable)
	{
		init(treetable);
		int columnCount = treetable.getColumnCount();

		int tWidth = treetable.getWidth();
		int tMax = tWidth - ((columnCount - 1) * absoluteMinSize);

		for (int i = 0; i < columnCount; i++)
		{
			columnSizes [MAX] [i] = tMax;
			columnSizes [PREFERRED] [i] = determineMaxColumnWidth(treetable, i);
			columnSizes [MIN] [i] = absoluteMinSize;
		}
	}

	/**
	 * Adjust the prefered width of all columns to the width of the
	 * whole tree table.
	 * @param treetable The treetable whose columns are to be resized
	 */
	public void adjustPreferredToWidth(JTreeTable treetable)
	{
		int columnCount = treetable.getColumnCount();
		int tWidth = treetable.getWidth();

		int preferred = 0;
		for (int i = 0; i < columnCount; i++)
		{
			preferred += columnSizes [PREFERRED] [i];
		}

		double div = (double) tWidth / (double) preferred;
		for (int i = 0; i < columnCount; i++)
		{
			columnSizes [PREFERRED] [i] = (int) (columnSizes [PREFERRED] [i] * div);
		}
	}

	/**
	 * Update the max width of column if they are set.
	 * @param treetable The tree table
	 */
	public void adjustMaximalWidthOfColumn(JTreeTable treetable)
	{
		int columnCount = treetable.getColumnCount();
		for (int i = 0; i < maxColumnSize.size(); i++)
		{
			MaxColumnWidth mcw = (MaxColumnWidth) maxColumnSize.get(i);
			int columnWidth = getPreferredWidthOfColumn(mcw.column);
			int maxColumnWidth = (int) (treetable.getWidth() * mcw.max);
			if (columnWidth > maxColumnWidth)
			{
				setColumnWidth(PREFERRED, i, maxColumnWidth);
				int delta = (columnWidth - maxColumnWidth) / (columnCount - 1);
				for (int j = 0; j < columnCount; j++)
				{
					if (i != j)
					{
						setColumnWidth(PREFERRED, j, getPreferredWidthOfColumn(j) + delta);
					}
				}
			}
		}
	}

	/**
	 * Determine if the preferred width of the columns
	 * needs to be re-calculated or not. Only if the width of the
	 * tree table has been changed, the values must be adjusted.
	 * @param treetable The tree table
	 * @return
	 *	true	A recalculate must be executed.
	 *	false	There is no need to recalculate the preferred width.
	 */
	public boolean isRecalculateNeeded(JTreeTable treetable)
	{
		int width = treetable.getWidth();
		int preferredWidth = 0;
		for (int i = 0; i < treetable.getColumnCount(); i++)
		{
			preferredWidth += treetable.getColumnModel().getColumn(i).getPreferredWidth();
		}
		return width != preferredWidth;
	}

	/**
	 * Get the minimal width of the column
	 * @param column The column
	 * @return the minimal width
	 */
	public int getMinWidthOfColumn(int column)
	{
		return columnSizes [MIN] [column];
	}

	/**
	 * Get the maximal widht of the column
	 * @param column The column
	 * @return the maximal width
	 */
	public int getMaxWidthOfColumn(int column)
	{
		return columnSizes [MAX] [column];
	}

	/**
	 * Get the preferred widht of the column
	 * @param column The column
	 * @return the maximal width
	 */
	public int getPreferredWidthOfColumn(int column)
	{
		return columnSizes [PREFERRED] [column];
	}

	/**
	 * Set a width for a column.
	 * @param type The type to set {@link #MAX}, {@link #MIN} or {@link #PREFERRED}
	 * @param column The column to set
	 * @param width The width to set
	 */
	protected void setColumnWidth(int type, int column, int width)
	{
		columnSizes [type] [column] = width;
	}

	/**
	 * Set the absolute minimal width of all columns. The columns
	 * can not be smaller than this.
	 * @param minSize the width
	 */
	public void setAboluteMinimun(int minSize)
	{
		absoluteMinSize = minSize;
		for (int i = 0; i < minSizeOfColumns.length; i++)
		{
			if (minSizeOfColumns [i] < minSize)
				minSizeOfColumns [i] = minSize;
		}
	}

	/**
	 * Get the absolute minimal widht of all columns.
	 * @return the width
	 */
	public int getAbsoluteMinimum()
	{
		return absoluteMinSize;
	}

	/**
	 * Set the minimal width of a column. If the width is less
	 * than the absolute minimal, then the absolute is set.
	 * @param columnWidth The minimal width
	 * @param column The index of the column
	 * @param treetable The treetable
	 */
	public void setMinimumOfColumn(int columnWidth, int column, JTreeTable treetable)
	{
		init(treetable);
		if (columnWidth > absoluteMinSize)
			minSizeOfColumns [column] = columnWidth;
	}

	//////////////////////////////////////////////////
	// @@ Column width calculation
	//////////////////////////////////////////////////

	/**
	 * Determines the maximum width of the tree column according to the text displayed.
	 *
	 * @param treetable The treetable
	 * @param column The index of the column
	 * @return The maximum width or 0 if it cannot be determined yet
	 */
	public int determineMaxColumnWidth(JTreeTable treetable, int column)
	{
		Font font = treetable.getFont();
		Graphics graphics = treetable.getGraphics();
		if (graphics == null)
			return 0;
		FontMetrics fm = graphics.getFontMetrics(font);

		int level = treetable.getTree().isRootVisible() ? 1 : 0;
		TreeTableNode node = (TreeTableNode) ((SimpleTreeTableModel) treetable.getModel()).getRoot();
		int columnMaxWidth = determineMaxColumnWidth(node, treetable.getTree().isRootVisible(), column, level, 0, fm);
		if (column == 0)
			treeColumnMaxWidth = columnMaxWidth;

		//System.out.println("Average: " + (sum / count));
		return columnMaxWidth;
	}

	/**
	 * Determines the maximum width of the tree column according to the text displayed.
	 *
	 * @param treetable The treetable
	 * @return The maximum width or 0 if it cannot be determined yet
	 */
	public int determineMaxTreeColumnWidth(JTreeTable treetable)
	{
		return determineMaxColumnWidth(treetable, 0);
	}

	/**
	 * Determines the maximum width of the tree column according to the text displayed.
	 *
	 * @param node Current node
	 * @param level Depth of the node
	 * @param max Maximum column width
	 * @param fm Font metrics
	 * @return The maximum width or 0 if it cannot be determined yet
	 */
	private int determineMaxColumnWidth(TreeTableNode node, boolean isRootVisible, int column, int level, int max, FontMetrics fm)
	{
		if (node == null)
			return max;

		if ((isRootVisible && node.getParent() == null) || node.getParent() != null)
		{
			// Only measure the width of the root node if it is visible
			int w = getWidthOfNode(node, column, fm, level);
			if (w > max)
				max = w;
		}

		int n = node.getChildCount();
		for (int i = 0; i < n; ++i)
		{
			TreeTableNode child = (TreeTableNode) node.getChildAt(i);
			max = determineMaxColumnWidth(child, isRootVisible, column, level + 1, max, fm);
		}

		return max;
	}

	/**
	 * Get the maximal width of a column.
	 * @param node The node that contains the column
	 * @param column The column index
	 * @param fm Metrics to calculate the width of the string
	 * @param level The tree level
	 * @return the width of the column
	 */
	private int getWidthOfNode(TreeTableNode node, int column, FontMetrics fm, int level)
	{
		Object colValue = column == 0 ? node.getNodeText() : node.getColumnValue(column);
		if (colValue instanceof JComponent)
		{
			return ((JComponent) colValue).getWidth();
		}

		if (colValue == null)
			return 0;

		String str = "";
		if (colValue instanceof String)
			str = (String) colValue;
		else if (level > 0)
			str = colValue.toString();

		int w = SwingUtilities.computeStringWidth(fm, str);

		if (column == 0)
		{
			// Add offset for icon
			w += level * 20;
		}

		// Add spacer
		w += 5;
		return w;
	}

	/**
	 * Add a maximal width for a column
	 * @param column The column for that the maximal width is set
	 * @param maxValue The maximal value wherea 1.0 means the column
	 * can be as width as the tree table and 0.5 means the column
	 * can be maximal half as width as the tree table.
	 */
	public void addMaxSizeForColumn(int column, double maxValue)
	{
		maxColumnSize.add(new MaxColumnWidth(column, maxValue));
	}

	/**
	 * Holder for the column and the maximal width of a column.
	 */
	class MaxColumnWidth
	{
		/** The column to set the maximal width. */
		int column;

		/** Percent of the total width, the column should get. */
		double max;

		/**
		 * Constructor.
		 * @param column The column to set the maximal width
		 * @param max Percentage of the total width, the column should get
		 */
		public MaxColumnWidth(int column, double max)
		{
			this.column = column;
			this.max = max;
		}
	}
}
