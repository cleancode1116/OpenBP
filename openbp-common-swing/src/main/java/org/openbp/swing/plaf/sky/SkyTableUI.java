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
package org.openbp.swing.plaf.sky;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * PLAF for a JTable component
 *
 * We override this class just in order to prevent some NPEs that happen occasionally
 * in JTreeTable components (absolutely no idea why, but, well, this JTable/JTree stuff is
 * absolutely horrible, so no wonder that the JTreeTable does not always what it should).
 *
 * All the code below is copied from the BasicTableUI class (which uses private methods, so there is
 * no other way). We just added the try/catch(Exception) in the paintCell method.
 *
 * @author Heiko Erhardt
 */
public class SkyTableUI extends BasicTableUI
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Creates a new instance of this ui.
	 *
	 * @param c Component to create this ui for
	 * @return The new ui instance
	 */
	public static ComponentUI createUI(JComponent c)
	{
		return new SkyTableUI();
	}

	/**
	 * Default constructor.
	 */
	public SkyTableUI()
	{
	}

	//////////////////////////////////////////////////
	// @@ BasicTableUI overrides
	//////////////////////////////////////////////////

	/** Paint a representation of the <code>table</code> instance
	 * that was set in installUI().
	 * @nowarn
	 */
	public void paint(Graphics g, JComponent c)
	{
		if (table.getRowCount() <= 0 || table.getColumnCount() <= 0)
		{
			return;
		}

		Rectangle clip = g.getClipBounds();
		Point minLocation = clip.getLocation();
		Point maxLocation = new Point(clip.x + clip.width - 1, clip.y + clip.height - 1);
		int rMin = table.rowAtPoint(minLocation);
		int rMax = table.rowAtPoint(maxLocation);

		// This should never happen.
		if (rMin == -1)
		{
			rMin = 0;
		}

		// If the table does not have enough rows to fill the view we'll get -1.
		// Replace this with the index of the last row.
		if (rMax == -1)
		{
			rMax = table.getRowCount() - 1;
		}
		int cMin = table.columnAtPoint(minLocation);
		int cMax = table.columnAtPoint(maxLocation);

		// This should never happen.
		if (cMin == -1)
		{
			cMin = 0;
		}

		// If the table does not have enough columns to fill the view we'll get -1.
		// Replace this with the index of the last column.
		if (cMax == -1)
		{
			cMax = table.getColumnCount() - 1;
		}

		// Paint the grid.
		paintGrid(g, rMin, rMax, cMin, cMax);

		// Paint the cells.
		paintCells(g, rMin, rMax, cMin, cMax);
	}

	/*
	 * Paints the grid lines within <I>aRect</I>, using the grid
	 * color set with <I>setGridColor</I>. Paints vertical lines
	 * if <code>getShowVerticalLines()</code> returns true and paints
	 * horizontal lines if <code>getShowHorizontalLines()</code>
	 * returns true.
	 */
	private void paintGrid(Graphics g, int rMin, int rMax, int cMin, int cMax)
	{
		g.setColor(table.getGridColor());

		Rectangle minCell = table.getCellRect(rMin, cMin, true);
		Rectangle maxCell = table.getCellRect(rMax, cMax, true);

		if (table.getShowHorizontalLines())
		{
			int tableWidth = maxCell.x + maxCell.width;
			int y = minCell.y;
			for (int row = rMin; row <= rMax; row++)
			{
				y += table.getRowHeight(row);
				g.drawLine(0, y - 1, tableWidth - 1, y - 1);
			}
		}

		if (table.getShowVerticalLines())
		{
			TableColumnModel cm = table.getColumnModel();
			int tableHeight = maxCell.y + maxCell.height;
			int x = minCell.x;
			for (int column = cMin; column <= cMax; column++)
			{
				x += cm.getColumn(column).getWidth();
				g.drawLine(x - 1, 0, x - 1, tableHeight - 1);
			}
		}
	}

	private int viewIndexForColumn(TableColumn aColumn)
	{
		TableColumnModel cm = table.getColumnModel();
		for (int column = 0; column < cm.getColumnCount(); column++)
		{
			if (cm.getColumn(column) == aColumn)
			{
				return column;
			}
		}
		return -1;
	}

	private void paintCells(Graphics g, int rMin, int rMax, int cMin, int cMax)
	{
		JTableHeader header = table.getTableHeader();
		TableColumn draggedColumn = (header == null) ? null : header.getDraggedColumn();

		TableColumnModel cm = table.getColumnModel();
		int columnMargin = cm.getColumnMargin();

		for (int row = rMin; row <= rMax; row++)
		{
			Rectangle cellRect = table.getCellRect(row, cMin, false);
			for (int column = cMin; column <= cMax; column++)
			{
				TableColumn aColumn = cm.getColumn(column);
				int columnWidth = aColumn.getWidth();
				cellRect.width = columnWidth - columnMargin;
				if (aColumn != draggedColumn)
				{
					paintCell(g, cellRect, row, column);
				}
				cellRect.x += columnWidth;
			}
		}

		// Paint the dragged column if we are dragging.
		if (draggedColumn != null && header != null)
		{
			paintDraggedArea(g, rMin, rMax, draggedColumn, header.getDraggedDistance());
		}

		// Remove any renderers that may be left in the rendererPane.
		rendererPane.removeAll();
	}

	private void paintDraggedArea(Graphics g, int rMin, int rMax, TableColumn draggedColumn, int distance)
	{
		int draggedColumnIndex = viewIndexForColumn(draggedColumn);

		Rectangle minCell = table.getCellRect(rMin, draggedColumnIndex, true);
		Rectangle maxCell = table.getCellRect(rMax, draggedColumnIndex, true);

		Rectangle vacatedColumnRect = minCell.union(maxCell);

		// Paint a gray well in place of the moving column.
		g.setColor(table.getParent().getBackground());
		g.fillRect(vacatedColumnRect.x, vacatedColumnRect.y, vacatedColumnRect.width, vacatedColumnRect.height);

		// Move to the where the cell has been dragged.
		vacatedColumnRect.x += distance;

		// Fill the background.
		g.setColor(table.getBackground());
		g.fillRect(vacatedColumnRect.x, vacatedColumnRect.y, vacatedColumnRect.width, vacatedColumnRect.height);

		// Paint the vertical grid lines if necessary.
		if (table.getShowVerticalLines())
		{
			g.setColor(table.getGridColor());
			int x1 = vacatedColumnRect.x;
			int y1 = vacatedColumnRect.y;
			int x2 = x1 + vacatedColumnRect.width - 1;
			int y2 = y1 + vacatedColumnRect.height - 1;

			// Left
			g.drawLine(x1 - 1, y1, x1 - 1, y2);

			// Right
			g.drawLine(x2, y1, x2, y2);
		}

		for (int row = rMin; row <= rMax; row++)
		{
			// Render the cell value
			Rectangle r = table.getCellRect(row, draggedColumnIndex, false);
			r.x += distance;
			paintCell(g, r, row, draggedColumnIndex);

			// Paint the (lower) horizontal grid line if necessary.
			if (table.getShowHorizontalLines())
			{
				g.setColor(table.getGridColor());
				Rectangle rcr = table.getCellRect(row, draggedColumnIndex, true);
				rcr.x += distance;
				int x1 = rcr.x;
				int y1 = rcr.y;
				int x2 = x1 + rcr.width - 1;
				int y2 = y1 + rcr.height - 1;
				g.drawLine(x1, y2, x2, y2);
			}
		}
	}

	private void paintCell(Graphics g, Rectangle cellRect, int row, int column)
	{
		try
		{
			if (table.isEditing() && table.getEditingRow() == row && table.getEditingColumn() == column)
			{
				Component component = table.getEditorComponent();
				component.setBounds(cellRect);
				component.validate();
			}
			else
			{
				TableCellRenderer renderer = table.getCellRenderer(row, column);
				Component component = table.prepareRenderer(renderer, row, column);
				rendererPane.paintComponent(g, component, table, cellRect.x, cellRect.y, cellRect.width, cellRect.height, true);
			}
		}
		catch (Exception e)
		{
			// Catch any exception here
		}
	}
}
