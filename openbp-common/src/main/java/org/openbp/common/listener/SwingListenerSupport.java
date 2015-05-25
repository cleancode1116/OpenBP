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
package org.openbp.common.listener;

import java.util.Iterator;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuDragMouseEvent;
import javax.swing.event.MenuDragMouseListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.tree.ExpandVetoException;

/**
 * Convenience class that adds event firing methods for listeners of the javax.swing package to the listener support class.
 *
 * @author Heiko Erhardt
 */
public class SwingListenerSupport extends ListenerSupport
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public SwingListenerSupport()
	{
	}

	//////////////////////////////////////////////////
	// @@ AncestorListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'ancestor added' message to all registered ancestor listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireAncestorAdded(AncestorEvent e)
	{
		for (Iterator it = getListenerIterator(AncestorListener.class); it.hasNext();)
		{
			((AncestorListener) it.next()).ancestorAdded(e);
		}
	}

	/**
	 * Fires an 'ancestor removed' message to all registered ancestor listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireAncestorRemoved(AncestorEvent e)
	{
		for (Iterator it = getListenerIterator(AncestorListener.class); it.hasNext();)
		{
			((AncestorListener) it.next()).ancestorRemoved(e);
		}
	}

	/**
	 * Fires an 'ancestor moved' message to all registered ancestor listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireAncestorMoved(AncestorEvent e)
	{
		for (Iterator it = getListenerIterator(AncestorListener.class); it.hasNext();)
		{
			((AncestorListener) it.next()).ancestorMoved(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ CaretListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'caret update' message to all registered caret listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireCaretUpdate(CaretEvent e)
	{
		for (Iterator it = getListenerIterator(CaretListener.class); it.hasNext();)
		{
			((CaretListener) it.next()).caretUpdate(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ CellEditorListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'editing stopped' message to all registered cell editor listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireEditingStopped(ChangeEvent e)
	{
		for (Iterator it = getListenerIterator(CellEditorListener.class); it.hasNext();)
		{
			((CellEditorListener) it.next()).editingStopped(e);
		}
	}

	/**
	 * Fires an 'editing canceled' message to all registered cell editor listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireEditingCanceled(ChangeEvent e)
	{
		for (Iterator it = getListenerIterator(CellEditorListener.class); it.hasNext();)
		{
			((CellEditorListener) it.next()).editingCanceled(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ ChangeListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'state changed' message to all registered change listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireStateChanged(ChangeEvent e)
	{
		for (Iterator it = getListenerIterator(ChangeListener.class); it.hasNext();)
		{
			((ChangeListener) it.next()).stateChanged(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ DocumentListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'insert update' message to all registered document listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireInsertUpdate(DocumentEvent e)
	{
		for (Iterator it = getListenerIterator(DocumentListener.class); it.hasNext();)
		{
			((DocumentListener) it.next()).insertUpdate(e);
		}
	}

	/**
	 * Fires an 'remove update' message to all registered document listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireRemoveUpdate(DocumentEvent e)
	{
		for (Iterator it = getListenerIterator(DocumentListener.class); it.hasNext();)
		{
			((DocumentListener) it.next()).removeUpdate(e);
		}
	}

	/**
	 * Fires an 'changed update' message to all registered document listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireChangedUpdate(DocumentEvent e)
	{
		for (Iterator it = getListenerIterator(DocumentListener.class); it.hasNext();)
		{
			((DocumentListener) it.next()).changedUpdate(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ HyperlinkListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'hyperlink update' message to all registered hyperlink listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireHyperlinkUpdate(HyperlinkEvent e)
	{
		for (Iterator it = getListenerIterator(HyperlinkListener.class); it.hasNext();)
		{
			((HyperlinkListener) it.next()).hyperlinkUpdate(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ InternalFrameListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'internal frame opened' message to all registered internal frame listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireInternalFrameOpened(InternalFrameEvent e)
	{
		for (Iterator it = getListenerIterator(InternalFrameListener.class); it.hasNext();)
		{
			((InternalFrameListener) it.next()).internalFrameOpened(e);
		}
	}

	/**
	 * Fires an 'internal frame closing' message to all registered internal frame listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireInternalFrameClosing(InternalFrameEvent e)
	{
		for (Iterator it = getListenerIterator(InternalFrameListener.class); it.hasNext();)
		{
			((InternalFrameListener) it.next()).internalFrameClosing(e);
		}
	}

	/**
	 * Fires an 'internal frame closed' message to all registered internal frame listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireInternalFrameClosed(InternalFrameEvent e)
	{
		for (Iterator it = getListenerIterator(InternalFrameListener.class); it.hasNext();)
		{
			((InternalFrameListener) it.next()).internalFrameClosed(e);
		}
	}

	/**
	 * Fires an 'internal frame iconified' message to all registered internal frame listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireInternalFrameIconified(InternalFrameEvent e)
	{
		for (Iterator it = getListenerIterator(InternalFrameListener.class); it.hasNext();)
		{
			((InternalFrameListener) it.next()).internalFrameIconified(e);
		}
	}

	/**
	 * Fires an 'internal frame deiconified' message to all registered internal frame listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireInternalFrameDeiconified(InternalFrameEvent e)
	{
		for (Iterator it = getListenerIterator(InternalFrameListener.class); it.hasNext();)
		{
			((InternalFrameListener) it.next()).internalFrameDeiconified(e);
		}
	}

	/**
	 * Fires an 'internal frame activated' message to all registered internal frame listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireInternalFrameActivated(InternalFrameEvent e)
	{
		for (Iterator it = getListenerIterator(InternalFrameListener.class); it.hasNext();)
		{
			((InternalFrameListener) it.next()).internalFrameActivated(e);
		}
	}

	/**
	 * Fires an 'internal frame deactivated' message to all registered internal frame listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireInternalFrameDeactivated(InternalFrameEvent e)
	{
		for (Iterator it = getListenerIterator(InternalFrameListener.class); it.hasNext();)
		{
			((InternalFrameListener) it.next()).internalFrameDeactivated(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ ListDataListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'interval added' message to all registered list data listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireIntervalAdded(ListDataEvent e)
	{
		for (Iterator it = getListenerIterator(ListDataListener.class); it.hasNext();)
		{
			((ListDataListener) it.next()).intervalAdded(e);
		}
	}

	/**
	 * Fires an 'interval removed' message to all registered list data listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireIntervalRemoved(ListDataEvent e)
	{
		for (Iterator it = getListenerIterator(ListDataListener.class); it.hasNext();)
		{
			((ListDataListener) it.next()).intervalRemoved(e);
		}
	}

	/**
	 * Fires an 'contents changed' message to all registered list data listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireContentsChanged(ListDataEvent e)
	{
		for (Iterator it = getListenerIterator(ListDataListener.class); it.hasNext();)
		{
			((ListDataListener) it.next()).contentsChanged(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ ListSelectionListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'value changed' message to all registered list selection listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireValueChanged(ListSelectionEvent e)
	{
		for (Iterator it = getListenerIterator(ListSelectionListener.class); it.hasNext();)
		{
			((ListSelectionListener) it.next()).valueChanged(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ MenuDragMouseListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'menu drag mouse entered' message to all registered menu drag mouse listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireMenuDragMouseEntered(MenuDragMouseEvent e)
	{
		for (Iterator it = getListenerIterator(MenuDragMouseListener.class); it.hasNext();)
		{
			((MenuDragMouseListener) it.next()).menuDragMouseEntered(e);
		}
	}

	/**
	 * Fires an 'menu drag mouse exited' message to all registered menu drag mouse listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireMenuDragMouseExited(MenuDragMouseEvent e)
	{
		for (Iterator it = getListenerIterator(MenuDragMouseListener.class); it.hasNext();)
		{
			((MenuDragMouseListener) it.next()).menuDragMouseExited(e);
		}
	}

	/**
	 * Fires an 'menu drag mouse dragged' message to all registered menu drag mouse listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireMenuDragMouseDragged(MenuDragMouseEvent e)
	{
		for (Iterator it = getListenerIterator(MenuDragMouseListener.class); it.hasNext();)
		{
			((MenuDragMouseListener) it.next()).menuDragMouseDragged(e);
		}
	}

	/**
	 * Fires an 'menu drag mouse released' message to all registered menu drag mouse listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireMenuDragMouseReleased(MenuDragMouseEvent e)
	{
		for (Iterator it = getListenerIterator(MenuDragMouseListener.class); it.hasNext();)
		{
			((MenuDragMouseListener) it.next()).menuDragMouseReleased(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ MenuKeyListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'menu key typed' message to all registered menu key listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireMenuKeyTyped(MenuKeyEvent e)
	{
		for (Iterator it = getListenerIterator(MenuKeyListener.class); it.hasNext();)
		{
			((MenuKeyListener) it.next()).menuKeyTyped(e);
		}
	}

	/**
	 * Fires an 'menu key pressed' message to all registered menu key listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireMenuKeyPressed(MenuKeyEvent e)
	{
		for (Iterator it = getListenerIterator(MenuKeyListener.class); it.hasNext();)
		{
			((MenuKeyListener) it.next()).menuKeyPressed(e);
		}
	}

	/**
	 * Fires an 'menu key released' message to all registered menu key listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireMenuKeyReleased(MenuKeyEvent e)
	{
		for (Iterator it = getListenerIterator(MenuKeyListener.class); it.hasNext();)
		{
			((MenuKeyListener) it.next()).menuKeyReleased(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ MenuListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'menu selected' message to all registered menu listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireMenuSelected(MenuEvent e)
	{
		for (Iterator it = getListenerIterator(MenuListener.class); it.hasNext();)
		{
			((MenuListener) it.next()).menuSelected(e);
		}
	}

	/**
	 * Fires an 'menu deselected' message to all registered menu listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireMenuDeselected(MenuEvent e)
	{
		for (Iterator it = getListenerIterator(MenuListener.class); it.hasNext();)
		{
			((MenuListener) it.next()).menuDeselected(e);
		}
	}

	/**
	 * Fires an 'menu canceled' message to all registered menu listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireMenuCanceled(MenuEvent e)
	{
		for (Iterator it = getListenerIterator(MenuListener.class); it.hasNext();)
		{
			((MenuListener) it.next()).menuCanceled(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ PopupMenuListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'popup menu will become visible' message to all registered popup menu listeners.
	 *
	 * @param e Event to fire
	 */
	public void firePopupMenuWillBecomeVisible(PopupMenuEvent e)
	{
		for (Iterator it = getListenerIterator(PopupMenuListener.class); it.hasNext();)
		{
			((PopupMenuListener) it.next()).popupMenuWillBecomeVisible(e);
		}
	}

	/**
	 * Fires an 'popup menu will become invisible' message to all registered popup menu listeners.
	 *
	 * @param e Event to fire
	 */
	public void firePopupMenuWillBecomeInvisible(PopupMenuEvent e)
	{
		for (Iterator it = getListenerIterator(PopupMenuListener.class); it.hasNext();)
		{
			((PopupMenuListener) it.next()).popupMenuWillBecomeInvisible(e);
		}
	}

	/**
	 * Fires an 'popup menu canceled' message to all registered popup menu listeners.
	 *
	 * @param e Event to fire
	 */
	public void firePopupMenuCanceled(PopupMenuEvent e)
	{
		for (Iterator it = getListenerIterator(PopupMenuListener.class); it.hasNext();)
		{
			((PopupMenuListener) it.next()).popupMenuCanceled(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ TableColumnModelListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'column added' message to all registered table column model listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireColumnAdded(TableColumnModelEvent e)
	{
		for (Iterator it = getListenerIterator(TableColumnModelListener.class); it.hasNext();)
		{
			((TableColumnModelListener) it.next()).columnAdded(e);
		}
	}

	/**
	 * Fires an 'column removed' message to all registered table column model listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireColumnRemoved(TableColumnModelEvent e)
	{
		for (Iterator it = getListenerIterator(TableColumnModelListener.class); it.hasNext();)
		{
			((TableColumnModelListener) it.next()).columnRemoved(e);
		}
	}

	/**
	 * Fires an 'column moved' message to all registered table column model listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireColumnMoved(TableColumnModelEvent e)
	{
		for (Iterator it = getListenerIterator(TableColumnModelListener.class); it.hasNext();)
		{
			((TableColumnModelListener) it.next()).columnMoved(e);
		}
	}

	/**
	 * Fires an 'column margin changed' message to all registered table column model listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireColumnMarginChanged(ChangeEvent e)
	{
		for (Iterator it = getListenerIterator(TableColumnModelListener.class); it.hasNext();)
		{
			((TableColumnModelListener) it.next()).columnMarginChanged(e);
		}
	}

	/**
	 * Fires an 'column selection changed' message to all registered table column model listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireColumnSelectionChanged(ListSelectionEvent e)
	{
		for (Iterator it = getListenerIterator(TableColumnModelListener.class); it.hasNext();)
		{
			((TableColumnModelListener) it.next()).columnSelectionChanged(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ TableModelListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'table changed' message to all registered table model listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireTableChanged(TableModelEvent e)
	{
		for (Iterator it = getListenerIterator(TableModelListener.class); it.hasNext();)
		{
			((TableModelListener) it.next()).tableChanged(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ TreeExpansionListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'tree expanded' message to all registered tree expansion listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireTreeExpanded(TreeExpansionEvent e)
	{
		for (Iterator it = getListenerIterator(TreeExpansionListener.class); it.hasNext();)
		{
			((TreeExpansionListener) it.next()).treeExpanded(e);
		}
	}

	/**
	 * Fires an 'tree collapsed' message to all registered tree expansion listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireTreeCollapsed(TreeExpansionEvent e)
	{
		for (Iterator it = getListenerIterator(TreeExpansionListener.class); it.hasNext();)
		{
			((TreeExpansionListener) it.next()).treeCollapsed(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ TreeModelListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'tree nodes changed' message to all registered tree model listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireTreeNodesChanged(TreeModelEvent e)
	{
		for (Iterator it = getListenerIterator(TreeModelListener.class); it.hasNext();)
		{
			((TreeModelListener) it.next()).treeNodesChanged(e);
		}
	}

	/**
	 * Fires an 'tree nodes inserted' message to all registered tree model listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireTreeNodesInserted(TreeModelEvent e)
	{
		for (Iterator it = getListenerIterator(TreeModelListener.class); it.hasNext();)
		{
			((TreeModelListener) it.next()).treeNodesInserted(e);
		}
	}

	/**
	 * Fires an 'tree nodes removed' message to all registered tree model listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireTreeNodesRemoved(TreeModelEvent e)
	{
		for (Iterator it = getListenerIterator(TreeModelListener.class); it.hasNext();)
		{
			((TreeModelListener) it.next()).treeNodesRemoved(e);
		}
	}

	/**
	 * Fires an 'tree structure changed' message to all registered tree model listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireTreeStructureChanged(TreeModelEvent e)
	{
		for (Iterator it = getListenerIterator(TreeModelListener.class); it.hasNext();)
		{
			((TreeModelListener) it.next()).treeStructureChanged(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ TreeSelectionListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'value changed' message to all registered tree selection listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireValueChanged(TreeSelectionEvent e)
	{
		for (Iterator it = getListenerIterator(TreeSelectionListener.class); it.hasNext();)
		{
			((TreeSelectionListener) it.next()).valueChanged(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ TreeWillExpandListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'tree will expand' message to all registered tree will expand listeners.
	 *
	 * @param e Event to fire
	 * @throws ExpandVetoException if one of the registered listeners wants to veto the expansion
	 */
	public void fireTreeWillExpand(TreeExpansionEvent e)
		throws ExpandVetoException
	{
		for (Iterator it = getListenerIterator(TreeWillExpandListener.class); it.hasNext();)
		{
			((TreeWillExpandListener) it.next()).treeWillExpand(e);
		}
	}

	/**
	 * Fires an 'tree will collapse' message to all registered tree will expand listeners.
	 *
	 * @param e Event to fire
	 * @throws ExpandVetoException if one of the registered listeners wants to veto the expansion
	 */
	public void fireTreeWillCollapse(TreeExpansionEvent e)
		throws ExpandVetoException
	{
		for (Iterator it = getListenerIterator(TreeWillExpandListener.class); it.hasNext();)
		{
			((TreeWillExpandListener) it.next()).treeWillCollapse(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ UndoableEditListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'undoable edit happened' message to all registered undoable edit listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireUndoableEditHappened(UndoableEditEvent e)
	{
		for (Iterator it = getListenerIterator(UndoableEditListener.class); it.hasNext();)
		{
			((UndoableEditListener) it.next()).undoableEditHappened(e);
		}
	}
}
