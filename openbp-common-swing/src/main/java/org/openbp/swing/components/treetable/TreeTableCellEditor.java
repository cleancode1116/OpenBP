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

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

/**
 * Default TreeTableCellEditor used by the {@link JTreeTable} to obtain the editor component
 * for the tree if set to editable.
 *
 * @author Erich Lauterbach
 */
public class TreeTableCellEditor extends DefaultCellEditor
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** TreeTable mediator used to communicate with the other Tree Tabel elements. */
	protected JTreeTable treeTable;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default Constructor
	 *
	 * @param treeTable Tree table this cell editor is used for
	 */
	public TreeTableCellEditor(JTreeTable treeTable)
	{
		super(new TreeTableTextField());
		this.treeTable = treeTable;
		treeTable.setCellEditor(this);
	}

	/**
	 * See javax.swing.DefaultCellEditor.getTableCellEditorComponent (JTable table, Object value, boolean isSelected, int row, int column)
	 * @nowarn
	 */
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		Component component = super.getTableCellEditorComponent(table, value, isSelected, row, column);

		if (component instanceof TreeTableTextField && value instanceof TreeTableNode)
		{
			((TreeTableTextField) component).setText(((TreeTableNode) value).getNodeText());
			return null;
		}

		JTree tree = treeTable.getTree();
		Rectangle bounds = tree.getRowBounds(row);
		int offset = bounds.x;
		TreeCellRenderer tcr = tree.getCellRenderer();

		if (tcr instanceof DefaultTreeCellRenderer)
		{
			Object node = tree.getPathForRow(row).getLastPathComponent();
			Icon icon;
			if (tree.getModel().isLeaf(node))
				icon = ((DefaultTreeCellRenderer) tcr).getLeafIcon();
			else if (tree.isExpanded(row))
				icon = ((DefaultTreeCellRenderer) tcr).getOpenIcon();
			else
				icon = ((DefaultTreeCellRenderer) tcr).getClosedIcon();

			if (icon != null)
				offset += ((DefaultTreeCellRenderer) tcr).getIconTextGap() + icon.getIconWidth();
		}

		((TreeTableTextField) getComponent()).offset = offset;
		return component;
	}

	/**
	 * Overridden to return false, and if the event is a mouse event
	 * it is forwarded to the tree.
	 *
	 * The behavior for this is debatable, and should really be offered
	 * as a property. By returning false, all keyboard actions are
	 * implemented in terms of the table. By returning true, the
	 * tree would get a chance to do something with the keyboard
	 * events. For the most part this is ok. But for certain keys,
	 * such as left/right, the tree will expand/collapse where as
	 * the table focus should really move to a different column. Page
	 * up/down should also be implemented in terms of the table.
	 * By returning false this also has the added benefit that clicking
	 * outside of the bounds of the tree node, but still in the tree
	 * column will select the row, whereas if this returned true
	 * that wouldn't be the case.
	 *
	 * By returning false we are also enforcing the policy that
	 * the tree will never be editable (at least by a key sequence).
	 * @nowarn
	 */
	public boolean isCellEditable(EventObject e)
	{
		if (e instanceof MouseEvent)
		{
			JTree tree = treeTable.getTree();
			MouseEvent me = (MouseEvent) e;
			int clickedRow = -1;
			int treeColumn = -1;

			for (int i = 0; i < treeTable.getColumnCount(); i++)
			{
				if (treeTable.getColumnClass(i) == TreeTableModel.class)
				{
					clickedRow = tree.getRowForLocation(me.getX() - treeTable.getCellRect(0, i, true).x, me.getY());
					treeColumn = i;
					break;
				}
			}

			if (treeColumn != -1)
			{
				MouseEvent newME = new MouseEvent(tree, me.getID(), me.getWhen(), me.getModifiers(), me.getX(), me.getY(), me.getClickCount(), me.isPopupTrigger());

				tree.dispatchEvent(newME);
			}
			else
			{
				int [] selectedRow = tree.getSelectionRows();

				if (selectedRow != null && selectedRow.length == 1 && selectedRow [0] == clickedRow)
				{
					// Already selected
					return true;
				}
				treeTable.selectCell(clickedRow, treeColumn);
			}
		}

		return false;
	}

	/**
	 * See javax.swing.DefaultCellEditor.getTableCellEditorComponent (JTable table, Object value, boolean isSelected, int row, int column)
	 * By default this always returns a null.
	 * @nowarn
	 */
	public Object getCellEditorValue()
	{
		return null;
	}

	/**
	 * Component used by TreeTableCellEditor. The only thing this does
	 * is to override the reshape method, and to ALWAYS
	 * make the x location be offset.
	 */
	static class TreeTableTextField extends JTextField
	{
		public int offset;

		/**
		 * Overriden method to reshape the the JTextField to fit the cell.
		 * @nowarn
		 */
		public void setBounds(int x, int y, int w, int h)
		{
			int newX = Math.max(x, offset);

			super.setBounds(newX, y, w - (newX - x), h);
		}
	}
}
