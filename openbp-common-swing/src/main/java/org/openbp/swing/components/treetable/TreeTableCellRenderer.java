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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;

/**
 * The tree table cell renderer that is a javax.swing.JTree. This renederer is implemented
 * in the 0 column of the {@link JTreeTable} and is used to draw each tree node in an individual row
 * of the table.
 *
 * @author Erich Lauterbach
 */
public class TreeTableCellRenderer extends JTree
	implements TableCellRenderer, TreeExpansionListener
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Last table/tree row asked to renderer. */
	private int visibleRow;

	/** Table which contains the tree. Parent container. */
	protected JTreeTable treeTable;

	/** Foreground color for selected cells */
	private Color selectionForeground;

	/** Background color for selected cells */
	private Color selectionBackground;

	/** Foreground color */
	private Color foreground;

	/** Background color */
	private Color background;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Creates a new TreeTableCellRenderer object.
	 *
	 * @param treeTable The {@link JTreeTable} using this Cell Renderer
	 * @param model The {@link TreeTableModel} assigned to the {@link JTreeTable}
	 */
	public TreeTableCellRenderer(JTreeTable treeTable, TreeTableModel model)
	{
		super(model);
		this.treeTable = treeTable;
		this.setRowHeight(0);
		this.setCellRenderer(new DefaultTreeCellRenderer(treeTable));
		this.addTreeExpansionListener(this);
		this.putClientProperty("JTree.lineStyle", "None");
		this.setDefaultColors();
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Sets the background color.
	 *
	 * @param background The background color to set or null for default
	 */
	public void setCellBackground(Color background)
	{
		if (background == null)
			this.background = UIManager.getColor("Table.background");
		else
			this.background = background;
	}

	/**
	 * Sets the foreground color.
	 *
	 * @param foreground The foreground color to set or null for default
	 */
	public void setCellForeground(Color foreground)
	{
		if (foreground == null)
			this.foreground = UIManager.getColor("Table.foreground");
		else
			this.foreground = foreground;
	}

	/**
	 * Sets the selection background color.
	 *
	 * @param selectionBackground The selection background color to set or null
	 * for default
	 */
	public void setCellSelectionBackground(Color selectionBackground)
	{
		if (selectionBackground == null)
			this.selectionBackground = UIManager.getColor("Table.selectionBackground");
		else
			this.selectionBackground = selectionBackground;
	}

	/**
	 * Sets the selection foreground color.
	 *
	 * @param selectionForeground The selection foreground color to set or null
	 * for default
	 */
	public void setCellSelectionForeground(Color selectionForeground)
	{
		if (selectionForeground == null)
			this.selectionForeground = UIManager.getColor("Table.selectionForeground");
		else
			this.selectionForeground = selectionForeground;
	}

	/**
	 * Sets the UI colors as default.
	 */
	public void setDefaultColors()
	{
		setCellBackground(null);
		setCellForeground(null);
		setCellSelectionBackground(null);
		setCellSelectionForeground(null);
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * updateUI is overridden to set the colors of the Tree's renderer
	 * to match that of the table.
	 */
	public void updateUI()
	{
		super.updateUI();
		if (treeTable != null)
		{
			treeTable.sizeRowsToFit();
		}
	}

	/**
	 * This is overridden to set the height to match that of the JTable.
	 * @nowarn
	 */
	public void setBounds(int x, int y, int w, int h)
	{
		super.setBounds(x, 0, w, treeTable.getHeight());
	}

	/**
	 * Sublcassed to translate the graphics such that the last visible
	 * row will be drawn at 0,0.
	 * @nowarn
	 */
	public void paint(Graphics g)
	{
		int shift = 0;

		for (int i = 0; i < visibleRow; i++)
		{
			shift += treeTable.getRowHeight(i);
		}

		g.translate(0, -shift);
		super.paint(g);
	}

	//////////////////////////////////////////////////
	// @@ TableCellRenderer implementation
	//////////////////////////////////////////////////

	/**
	 * TreeCellRenderer method. Overridden to update the visible row.
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)
	 * @nowarn
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		if (isSelected)
		{
			if (treeTable.getRowSelectionAllowed())
				setColor(selectionForeground, selectionBackground);
			else
				setColor(foreground, background);
		}
		else if (hasFocus)
		{
			setColor(selectionForeground, selectionBackground);
		}
		else
		{
			setColor(foreground, background);
		}

		visibleRow = row;
		return this;
	}

	//////////////////////////////////////////////////
	// @@ Protected methods
	//////////////////////////////////////////////////

	protected void setColor(Color foreground, Color background)
	{
		TreeCellRenderer tcr = getCellRenderer();
		if (tcr instanceof DefaultTreeCellRenderer)
		{
			DefaultTreeCellRenderer dtcr = (DefaultTreeCellRenderer) tcr;
			dtcr.setForeground(foreground);
			dtcr.setBackground(background);
			dtcr.setBackgroundSelectionColor(background);
			dtcr.setBackgroundNonSelectionColor(background);
		}
		setForeground(foreground);
		setBackground(background);
	}

	//////////////////////////////////////////////////
	// @@ Inner Class
	//////////////////////////////////////////////////

	/**
	 */
	public void treeCollapsed(TreeExpansionEvent event)
	{
		treeTable.sizeRowsToFit();
	}

	/**
	 */
	public void treeExpanded(TreeExpansionEvent event)
	{
		treeTable.sizeRowsToFit();
	}
}
