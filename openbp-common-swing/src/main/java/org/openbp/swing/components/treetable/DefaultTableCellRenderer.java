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

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.openbp.common.string.TextUtil;

/**
 * Table cell renderer for JComponents. This renderer is used by the {@link JTreeTable}
 * as the default cell renderer for any JComponent.
 *
 * @author Erich Lauterbach
 */
public class DefaultTableCellRenderer extends javax.swing.table.DefaultTableCellRenderer
{
	//////////////////////////////////////////////////
	// @@ Static variables
	//////////////////////////////////////////////////

	/** Distance border */
	private static final EmptyBorder distanceBorder = new EmptyBorder(1, 0, 0, 0);

	/** Single line text for HTML conversion */
	private String [] singleLineText = new String [1];

	//////////////////////////////////////////////////
	// @@ Default table cell renderer implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the rendered component.
	 * see javax.swing.table.TableCellRenderer.getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)
	 * @nowarn
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		if (!(value instanceof JComponent))
		{
			// We use a new label for the renderer because the SkyLabelUI might attach a tool tip to the component
			// and the DefaultTableCellRenderer uses a single component for all cells

			JLabel l = new JLabel();

			processLabelAttributes(l, table, isSelected, hasFocus, row, column);

			if (value != null)
			{
				if (value instanceof String [])
				{
					// See {@link AbstractNode#createTextValue}
					String [] multiText = (String []) value;

					// Hack: Add some space to the text to prevent it from touching the column to the left
					String text = multiText [0];
					if (text != null)
						text = " " + text;
					l.setText(text);

					l.setToolTipText(TextUtil.convertToHTML(multiText, false, 0, 50));
				}
				else
				{
					String text = value.toString();

					// Hack: Add some space to the text to prevent it from touching the column to the left
					if (text != null)
					{
						text = " " + text;
						l.setText(text);
	
						if (text.length() > 40)
						{
							singleLineText [0] = text;
							l.setToolTipText(TextUtil.convertToHTML(singleLineText, false, 0, 50));
						}
					}
					
				}
			}

			// Make the text appear at the top of large cells
			l.setVerticalAlignment(SwingConstants.NORTH);

			return l;
		}

		return (Component) value;
	}

	/**
	 * Processs color and font attributes of a label that is about to be used as cell renderer.
	 * This has been copied from javax.swing.table.DefaultTableCellRenderer
	 *
	 * @param label Label component
	 * @param table Table
	 * @param isSelected
	 *		true	The cell is selected.
	 *		false	The cell is not selected.
	 * @param hasFocus
	 *		true	The cell has the focus.
	 *		false	The cell is not focused.
	 * @param row Current row
	 * @param column Current column
	 */
	public void processLabelAttributes(JLabel label, JTable table, boolean isSelected, boolean hasFocus, int row, int column)
	{
		if (isSelected)
		{
			label.setForeground(table.getSelectionForeground());
			label.setBackground(table.getSelectionBackground());
		}
		else
		{
			label.setForeground(table.getForeground());
			label.setBackground(table.getBackground());
		}

		label.setFont(table.getFont());

		if (hasFocus)
		{
			setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
			if (table.isCellEditable(row, column))
			{
				label.setForeground(UIManager.getColor("Table.focusCellForeground"));
				label.setBackground(UIManager.getColor("Table.focusCellBackground"));
			}
		}
		else
		{
			setBorder(noFocusBorder);
		}

		// Optimization to avoid painting background
		Color back = label.getBackground();
		boolean colorMatch = (back != null) && back.equals(table.getBackground()) && table.isOpaque();
		label.setOpaque(!colorMatch);

		// Table labels have an offset to tree labels of 1 pixel; let's correct this
		label.setBorder(distanceBorder);
	}
}
