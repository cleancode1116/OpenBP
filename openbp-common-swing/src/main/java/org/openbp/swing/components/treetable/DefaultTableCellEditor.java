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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;

/**
 * TODOC
 *
 * @author Erich Lauterbach
 */
public class DefaultTableCellEditor extends AbstractCellEditor
	implements TableCellEditor
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/**
	 * The delegate class which handles all methods sent from the
	 * CellEditor.
	 */
	private EditorDelegate delegateObject;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public DefaultTableCellEditor()
	{
		delegateObject = new EditorDelegate();
	}

	//////////////////////////////////////////////////
	// @@ Public methods
	//////////////////////////////////////////////////

	public Object getCellEditorValue()
	{
		return delegateObject.getCellEditorValue();
	}

	public boolean isCellEditable(EventObject anEvent)
	{
		return delegateObject.isCellEditable(anEvent);
	}

	public boolean shouldSelectCell(EventObject anEvent)
	{
		return delegateObject.shouldSelectCell(anEvent);
	}

	public boolean stopCellEditing()
	{
		return delegateObject.stopCellEditing();
	}

	public void cancelCellEditing()
	{
		delegateObject.cancelCellEditing();
	}

	public void setValue(Object value)
	{
		delegateObject.setValue(value);
	}

	//////////////////////////////////////////////////
	// @@ Cell Editor implementation
	//////////////////////////////////////////////////

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		if (value instanceof String)
		{
			final JTextField textField = new JTextField();
			delegateObject = new EditorDelegate()
			{
				public void setValue(Object value)
				{
					textField.setText((value != null) ? value.toString() : "");
				}

				public Object getCellEditorValue()
				{
					return textField.getText();
				}
			};
			textField.addActionListener(delegateObject);
			delegateObject.setValue(value);
			return textField;
		}
		if (value instanceof JComponent)
		{
			JComponent component = (JComponent) value;
			if (component instanceof JCheckBox && isSelected)
			{
				component.setForeground(UIManager.getColor("Table.selectionForeground"));
				component.setBackground(UIManager.getColor("Table.selectionBackground"));
			}
			return component;
		}
		return null;
	}

	//////////////////////////////////////////////////
	// @@ Inner Class
	//////////////////////////////////////////////////

	/**
	 * The protected EditorDelegate class.
	 */
	protected class EditorDelegate
		implements ActionListener, ItemListener, Serializable
	{
		/**  The value of this cell. */
		private Object value;

		/**
		 * Returns the value of this cell.
		 * @nowarn
		 */
		public Object getCellEditorValue()
		{
			return value;
		}

		/**
		 * Sets the value of this cell.
		 * @nowarn
		 */
		public void setValue(Object value)
		{
			this.value = value;
		}

		/**
		 * Returns true.
		 * @nowarn
		 */
		public boolean isCellEditable(EventObject anEvent)
		{
			return true;
		}

		/**
		 * Returns true to indicate that the editing cell may be selected.
		 * @nowarn
		 */
		public boolean shouldSelectCell(EventObject anEvent)
		{
			return true;
		}

		/**
		 * Returns true to indicate that editing has begun.
		 * @nowarn
		 */
		public boolean startCellEditing(EventObject anEvent)
		{
			return true;
		}

		/**
		 * Stops editing and returns true to indicate that editing has stopped.
		 * This method calls fireEditingStopped.
		 * @nowarn
		 */
		public boolean stopCellEditing()
		{
			fireEditingStopped();
			return true;
		}

		/**
		 * Cancels editing.
		 * This method calls fireEditingCanceled.
		 */
		public void cancelCellEditing()
		{
			fireEditingCanceled();
		}

		/**
		 * When an action is performed, editing is ended.
		 * @nowarn
		 */
		public void actionPerformed(ActionEvent e)
		{
			DefaultTableCellEditor.this.stopCellEditing();
		}

		/**
		 * When an item's state changes, editing is ended.
		 * @nowarn
		 */
		public void itemStateChanged(ItemEvent e)
		{
			DefaultTableCellEditor.this.stopCellEditing();
		}
	}
}
