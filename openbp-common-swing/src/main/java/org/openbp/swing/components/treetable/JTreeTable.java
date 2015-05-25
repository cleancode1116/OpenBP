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
import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.CellEditor;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.openbp.common.CollectionUtil;
import org.openbp.common.listener.ListenerSupport;
import org.openbp.common.string.TextUtil;
import org.openbp.swing.components.tree.TreeUtil;
import org.openbp.swing.components.treetable.resize.ColumnSizeConstraint;
import org.openbp.swing.components.treetable.resize.RatioConstraint;

/**
 * This JTreeTable is based on the same concept (placing a JTree into a column of a JTable)
 * as the JTreeTable provided by Sun Microsystems, with the exception that the individual JTree
 * nodes will adapt to the height of JTables row in which they have been placed. This
 * functionality is brought about by providing a new type of TreeNode, the TreeTableNode,
 * that contains methods that will deliver the maximum preferred height of the row to the tree
 * and the table. Further, to enable provision of such information, it is expected that all cell
 * objects for a row are kept inside the individual node.<br>
 *
 * As on the standard Java API, the {@link JTreeTable} requires a model for data provision, the
 * {@link TreeTableModel} This interface actually brings the TreeModel and TableModel interfaces
 * together in one, with a few extra added methods. In order not to have to implement the entire
 * model, a partially implemented model, the {@link SimpleTreeTableModel} with most generic methods
 * provided. If the user wishes not to implement the model at all, then the
 * {@link DefaultTreeTableModel} is provided the can be used with the {@link DefaultTreeTableNode}
 *
 * @author Erich Lauterbach
 */
public class JTreeTable extends JTable
	implements MouseListener
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Selection mode: No selection */
	public static final int SELECTION_NONE = 0;

	/** Selection mode: Single selection */
	public static final int SELECTION_SINGLE = ListSelectionModel.SINGLE_SELECTION;

	/** Selection mode: Multiple selection */
	public static final int SELECTION_MULTI = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;

	//////////////////////////////////////////////////
	// @@ Static members
	//////////////////////////////////////////////////

	/** Command: No key */
	public static final int CMD_NONE = 0;

	/** Command: Mouse key */
	public static final int CMD_MOUSE = 1;

	/** Command: Tab */
	public static final int CMD_TAB = 2;

	/** Command: Backtab */
	public static final int CMD_BACKTAB = 3;

	/** Command: Enter */
	public static final int CMD_ENTER = 4;

	/** Command: Space */
	public static final int CMD_SPACE = 5;

	/** Command: Cursor left */
	public static final int CMD_LEFT = 6;

	/** Command: Cursor right */
	public static final int CMD_RIGHT = 7;

	/** Command: Cursor up */
	public static final int CMD_UP = 8;

	/** Command: Cursor down */
	public static final int CMD_DOWN = 9;

	/** Command: Page up */
	public static final int CMD_PGUP = 10;

	/** Command: Page down */
	public static final int CMD_PGDN = 11;

	/** Command: To the top */
	public static final int CMD_TOP = 12;

	/** Command: To the bottom */
	public static final int CMD_BOTTOM = 13;

	/** Command: Home */
	public static final int CMD_HOME = 14;

	/** Command: End */
	public static final int CMD_END = 15;

	/** Command: Escape */
	public static final int CMD_ESC = 16;

	/** Command modifier: Set selection */
	public static final int CMD_SET_SELECTION = (1 << 10);

	/** Command modifier: Extend selection */
	public static final int CMD_EXTEND_SELECTION = (1 << 11);

	/** Command modifier: Toggle selection */
	public static final int CMD_TOGGLE_SELECTION = (1 << 12);

	/** Command modifier: Switch from tree to table and vice versa */
	public static final int CMD_SWITCH_TREE = (1 << 13);

	/** Command modifier mask: Code mask */
	public static final int CMD_CODE_MASK = 0xff;

	/** Command modifier mask: Selection mask */
	public static final int CMD_SELECTION_MASK = (CMD_SET_SELECTION | CMD_EXTEND_SELECTION | CMD_TOGGLE_SELECTION);

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** The cell rendered used by the JTreeTable, that contains the Tree. */
	private TreeTableCellRenderer tree;

	/** Default table renderer */
	private static DefaultTableCellRenderer defaultTableRenderer;

	/** Default table cell editor */
	private static DefaultTableCellEditor defaultTableCellEditor;

	/** Input map for focused keys of the tree table */
	protected InputMap focusInputMap;

	/** Input map for ancestor-focused keys of the tree table */
	protected InputMap focusAncestorInputMap;

	/** Action map of the tree table */
	protected ActionMap actionMap;

	/** Listener support object holding the listeners */
	protected ListenerSupport listenerSupport;

	/** Default row height */
	private int defaultRowHeight;

	/** We assume the tree column to be column 0 */
	private int treeCol;

	/** Current colum */
	protected int currentCol = -1;

	/** Current row */
	protected int currentRow = -1;

	/** Saved current column */
	private int savedCurrentCol = -1;

	/** Saved current row */
	private int savedCurrentRow = -1;

	/** Defines if a cell is being edited or not. */
	private boolean performingEditCellAt;

	/** Selection mode */
	private int selectionMode = SELECTION_SINGLE;

	/** The constraint for the column size. */
	private ColumnSizeConstraint csc;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default Constructor. This constructor creates a {@link DefaultTreeTableNode} for a root node,
	 * and adds this to a {@link DefaultTreeTableModel} which it registers with this JTreeTable.
	 */
	public JTreeTable()
	{
		super();

		DefaultTreeTableNode rootNode = new DefaultTreeTableNode();
		rootNode.addColumn("Root Node");
		TreeTableModel model = new DefaultTreeTableModel(rootNode);

		init(model);
	}

	/**
	 * Constructor.
	 *
	 * @param treeTableModel The {@link TreeTableModel} to be used with this JTreeTable
	 * @nowarn
	 */
	public JTreeTable(TreeTableModel treeTableModel)
	{
		super();

		init(treeTableModel);
	}

	/**
	 * Initialises this JTreeTable, by registering the default renderers and editors on the
	 * table, tree and model.
	 *
	 * @param treeTableModel The {@link TreeTableModel} to be used by this JTreeTable
	 * @nowarn
	 */
	private void init(TreeTableModel treeTableModel)
	{
		defaultRowHeight = 16;

		tree = new TreeTableCellRenderer(this, treeTableModel);

		// Open tree on single click
		tree.setToggleClickCount(2);

		treeTableModel.setTreeTable(this);
		treeTableModel.addTreeModelListener(new TTModelListener());

		// Install a tableModel representing the visible rows in the tree.
		super.setModel(treeTableModel);

		// Install the tree renderer and editor
		setDefaultRenderer(TreeTableModel.class, tree);
		setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor(this));

		// Install the default table cell renderers and editor
		if (defaultTableRenderer == null)
			defaultTableRenderer = new DefaultTableCellRenderer();
		setDefaultRenderer(String.class, defaultTableRenderer);
		setDefaultRenderer(String [].class, defaultTableRenderer);
		setDefaultRenderer(Object.class, defaultTableRenderer);

		if (defaultTableCellEditor == null)
			defaultTableCellEditor = new DefaultTableCellEditor();
		setDefaultEditor(Object.class, defaultTableCellEditor);

		// Initialise the rest of the JTreeTable look and key mappings.
		getSelectionModel().setSelectionMode(selectionMode);
		setRowSelectionAllowed(true);

		// Set the default column constraint, where the first column
		// is always completely visible and the rest of the width is evenly
		// distributed between the other columns.
		// If the first column is bigger than the half of the table width
		// the tree column is set to the half table width. The user can
		// still make the column smaller or bigger.
		ColumnSizeConstraint csc = new RatioConstraint(true);
		csc.addMaxSizeForColumn(0, 0.5);
		setAutoResizeMode(AUTO_RESIZE_NEXT_COLUMN);
		setColumnSizeConstraint(csc);

		setShowGrid(false);

		addMouseListener(this);

		setupKeyBindings();

		setSurrendersFocusOnKeystroke(true);

		configureSubComponent(this);
	}

	//////////////////////////////////////////////////
	// @@ JTable overrides
	//////////////////////////////////////////////////

	/**
	 * Override to prevent NPE in default cut/copy/paste transfer handler when copying table data.
	 * @see javax.swing.JTable#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int column)
	{
		Object ret = super.getValueAt(row, column);
		if (ret == null)
			ret = "";
		return ret;
	}

	/**
	 * This was overridden to message super and forward the method to the tree. This is
	 * done since the tree is not actually in the component hierarchy, this it will never
	 * receive the updateUI message.
	 */
	public void updateUI()
	{
		super.updateUI();
		if (tree != null)
		{
			// Do this so that the editor is referencing the current renderer
			// from the tree. The renderer can potentially change each time
			// laf changes.
			if (getDefaultEditor(TreeTableModel.class) == null)
			{
				setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor(this));
			}

			tree.updateUI();
		}
	}

	/**
	 * JTable override. This method has been overridden such that the selection
	 * for both the tree and the table take place.<br>
	 * See javax.swing.table.editCellAt (int row, int column)
	 *
	 * @param row Row of the cell
	 * @param col Column of the cell
	 * @return
	 *		true	If the specified column can be edited.<br>
	 *		false	If the specified column can not be edited.
	 */
	public boolean editCellAt(int row, int col)
	{
		return editCellAt(row, col, null);
	}

	/**
	 * Overriden method. This method has been overridden such that the selection
	 * for both the tree and the table take place.<br>
	 * See javax.swing.table.editCellAt (int row, int column)
	 *
	 * @param row Row of the cell
	 * @param col Column of the cell
	 * @param e Event object of the event that caused the call
	 * @return
	 *		true	If the specified column can be edited.<br>
	 *		false	If the specified column can not be edited.
	 */
	public boolean editCellAt(int row, int col, EventObject e)
	{
		performingEditCellAt = true;
		boolean success = super.editCellAt(row, col, e);
		performingEditCellAt = false;

		if (success && editorComp instanceof JComponent)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					if (editorComp != null)
					{
						editorComp.requestFocus();
					}
				}
			});
		}

		return success;
	}

	/**
	 * JTable override. This is done to forward the selection to the tree,
	 * when a new row is selected. The selection method has been implemented in this manner,
	 * since it is more responsive than sharing the ListSelectionModel of the tree with the table.
	 *
	 * Updates the selection models of the table and the tree, depending on the state of the two flags:
	 * toggle and extend. All changes to the selection that are the
	 * result of keyboard or mouse events received by the UI are channeled through this method
	 * so that the behavior may be overridden by a subclass.
	 *
	 * This implementation uses the following conventions:
	 * <ul>
	 *    <li> toggle: <em>false</em>,
	 *         extend: <em>false</em>.
	 *         Clear the previous selection and ensure the new cell is selected.
	 *    <li> toggle: <em>false</em>,
	 *         extend: <em>true</em>.
	 *         Extend the previous selection to include the specified cell.
	 *    <li> toggle: <em>true</em>,
	 *         extend: <em>false</em>.
	 *         If the specified cell is selected, deselect it. If it is not selected, select it.
	 *    <li> toggle: <em>true</em>,
	 *         extend: <em>true</em>.
	 *         Leave the selection state as it is, but move the anchor index to the specified location.
	 * </ul>
	 *
	 * @param newRow Index of the row to select
	 * @param newCol Index of the column to select
	 * @param toggle See description above
	 * @param extend If true, extend the current selection
	 */
	public void changeSelection(int newRow, int newCol, boolean toggle, boolean extend)
	{
		if (currentCol >= 0 && currentRow >= 0)
		{
			CellEditor cellEditor = getCellEditor(currentRow, currentCol);
			if (cellEditor != null)
			{
				cellEditor.stopCellEditing();
			}
		}

		// If the method is being called from inside the editCellAt method, fall back to the base implementation
		if (!performingEditCellAt)
		{
			// Edit the cell if we can
			editCellAt(newRow, newCol);
		}

		super.changeSelection(newRow, newCol, toggle, extend);
		tree.setSelectionRow(newRow);

		if (currentCol >= 0 && currentRow >= 0)
		{
			final int oldCol = currentCol;
			final int oldRow = currentRow;

			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					repaintCell(oldRow, oldCol);
				}
			});
		}

		currentCol = newCol;
		currentRow = newRow;
	}

	/**
	 * Repaints the given cell.
	 *
	 * @param row Row of the cell
	 * @param col Column of the cell
	 */
	protected void repaintCell(int row, int col)
	{
		Rectangle dirtyRect = getCellRect(row, col, false);
		repaint(dirtyRect);
	}

	/**
	 * Workaround for BasicTableUI anomaly. Make sure the UI never tries to
	 * paint the editor. The UI currently uses different techniques to
	 * paint the renderers and editors and overriding setBounds() below
	 * is not the right thing to do for an editor. Returning -1 for the
	 * editing row in this case, ensures the editor is never painted.
	 *
	 * @return The current row being edited
	 */
	public int getEditingRow()
	{
		return (getColumnClass(editingColumn) == TreeTableModel.class) ? -1 : editingRow;
	}

	/**
	 * Sets the tree table model.
	 *
	 * @param model The model to be set for the tree table
	 */
	public void setModel(TreeTableModel model)
	{
		tree.setModel(model);
		model.setTreeTable(this);

		// Install a tableModel representing the visible rows in the tree.
		super.setModel(model);
	}

	/**
	 * Override of the super method that takes special considerations for tree cell renderers.
	 * @nowarn
	 */
	public String getToolTipText(MouseEvent me)
	{
		String tip = null;
		Point p = me.getPoint();

		// Locate the renderer under the event location
		int hitColumnIndex = columnAtPoint(p);
		int hitRowIndex = rowAtPoint(p);

		if (hitColumnIndex != -1 && hitRowIndex != -1)
		{
			TableCellRenderer renderer = getCellRenderer(hitRowIndex, hitColumnIndex);
			Component component = prepareRenderer(renderer, hitRowIndex, hitColumnIndex);

			// Now have to see if the component is a JComponent before
			// getting the tip
			if (component instanceof JComponent)
			{
				// Tree table cell renderers need to know the table position, so don't translate
				if (!(component instanceof TreeTableCellRenderer))
				{
					// Convert the event to the renderer's coordinate system
					Rectangle cellRect = getCellRect(hitRowIndex, hitColumnIndex, false);
					p.translate(-cellRect.x, -cellRect.y);
				}

				MouseEvent newEvent = new MouseEvent(component, me.getID(), me.getWhen(), me.getModifiers(), p.x, p.y, me.getClickCount(), me.isPopupTrigger());

				tip = ((JComponent) component).getToolTipText(newEvent);
			}
		}

		// No tip from the renderer get our own tip
		if (tip == null)
			tip = getToolTipText();

		return tip;
	}

	//////////////////////////////////////////////////
	// @@ Various public methods
	//////////////////////////////////////////////////

	/**
	 * Returns the tree used by the TreeTable.
	 *
	 * @return The JTree used by the TreeTalbe
	 */
	public JTree getTree()
	{
		return tree;
	}

	/**
	 * Returns the {@link TreeTableModel} that provides the data displayed by this
	 * {@link JTreeTable}
	 *
	 * @return  the {@link TreeTableModel} that provides the data displayed by this {@link JTreeTable}
	 */
	public TreeTableModel getTreeTableModel()
	{
		return (TreeTableModel) dataModel;
	}

	/**
	 * Gets the scroll pane that is associated with the table.
	 *
	 * @return The scroll pane or null if there is none
	 */
	protected JScrollPane getScrollPane()
	{
		Container p = getParent();
		if (p instanceof JViewport)
		{
			Container gp = p.getParent();
			if (gp instanceof JScrollPane)
			{
				return (JScrollPane) gp;
			}
		}
		return null;
	}

	/**
	 * Sets the root visible in the tree.
	 *
	 * @param visible
	 *		true	To show the root<br>
	 *		false	To hide it
	 */
	public void setRootVisible(boolean visible)
	{
		tree.setRootVisible(visible);
	}

	/**
	 * Gets the we assume the tree column to be column 0.
	 * @nowarn
	 */
	public int getTreeCol()
	{
		return treeCol;
	}

	/**
	 * Gets the default row height.
	 * @nowarn
	 */
	public int getDefaultRowHeight()
	{
		return defaultRowHeight;
	}

	/**
	 * Sets the default row height.
	 * @nowarn
	 */
	public void setDefaultRowHeight(int defaultRowHeight)
	{
		this.defaultRowHeight = defaultRowHeight;
	}

	/**
	 * Resizes all rows in the table to adjust to the height of the the components contained
	 * in the individual cells.
	 * The row height will be set to the largest preferred size of all components contained in a row.
	 */
	public void sizeRowsToFit()
	{
		if (tree == null)
			return;

		// Adjust the row heights
		int numberOfRows = tree.getRowCount();
		for (int i = 0; i < numberOfRows; i++)
		{
			TreePath path = tree.getPathForRow(i);
			if (path != null)
			{
				TreeTableNode node = (TreeTableNode) path.getLastPathComponent();

				if (node.getLastHeight() > 0)
				{
					setRowHeight(i, node.getLastHeight());
				}
			}
		}

		revalidate();
		repaint();
	}

	/**
	 * Resizes all cells in the table to adjust to the width of the the components contained
	 * in the individual cells.
	 * The column size will be set according to the minimum and maximum sizes that have been
	 * specified for each column and the relation of the column sizes in percent.
	 */
	public void sizeColumnsToFit()
	{
		sizeColumnsToFit(-1);
	}

	/**
	 * Resizes all cells in the table to adjust to the width of the the components contained
	 * in the individual cells.
	 * Override of JTable.sizeColumnsToFit.<br>
	 * The column size will be set according to the minimum and maximum sizes that have been
	 * specified for each column and the relation of the column sizes in percent.
	 *
	 * @param resizingColumn Column index of the column that has been resized if the request
	 * originates from a column header being dragged. If this value is != -1, the request
	 * will be delegated to the super method.
	 */
	public void sizeColumnsToFit(int resizingColumn)
	{
		if (resizingColumn >= 0)
		{
			super.sizeColumnsToFit(resizingColumn);
			return;
		}

		if (tree == null)
			return;

		// If a column is resized, then let the table handle it
		JTableHeader header = getTableHeader();
		if (header != null && header.getResizingColumn() != null)
			return;

		// Update the width from the viewport and calculate the new width only
		// if the preferred width must be adapted to the table width.
		if (!csc.isRecalculateNeeded(this))
			return;

		int total = getWidth();
		if (total <= 0)
		{
			// Set at least the minimal width of the columns
			for (int i = 0; i < getColumnCount(); i++)
			{
				TableColumn column = getColumnModel().getColumn(i);
				column.setMinWidth(csc.getAbsoluteMinimum());
			}
			return;
		}

		// Calculate the preferred width of the columns, then
		// adjust all maximal width constraints
		csc.calculateColumnSizes(this);
		int treePref = csc.determineMaxTreeColumnWidth(this);
		if (treePref <= 0)
			return;
		csc.adjustMaximalWidthOfColumn(this);

		// Set the width of the columns
		for (int i = 0; i < getColumnCount(); i++)
		{
			TableColumn column = getColumnModel().getColumn(i);
			column.setMaxWidth(csc.getMaxWidthOfColumn(i));
			column.setMinWidth(csc.getMinWidthOfColumn(i));
			column.setPreferredWidth(csc.getPreferredWidthOfColumn(i));
		}
	}

	/**
	 * Set the constraint for the column size.
	 * @param csc The constraint class
	 */
	public void setColumnSizeConstraint(ColumnSizeConstraint csc)
	{
		this.csc = csc;
	}

	/**
	 * Handle the layout of the rows and columns.
	 */
	public void doLayout()
	{
		// If the table as a whole has changed use the
		// ColumnSizeConstraint from the tree table, otherwise
		// use the default resize from the table.

		JTableHeader header = getTableHeader();
		if (header != null && header.getResizingColumn() == null)
		{
			// The whole table has changed, adjust the
			// column width and redraw the table
			sizeColumnsToFit();
			int count = getColumnModel().getColumnCount();
			for (int i = 0; i < count; i++)
			{
				TableColumn column = getColumnModel().getColumn(i);
				column.setWidth(column.getPreferredWidth());
			}
			resizeAndRepaint();

			// Don't use the super doLayout method, because
			// the table would do the layout of the columns again
			getParent().doLayout();
		}
		else
		{
			super.doLayout();
		}
	}

	//////////////////////////////////////////////////
	// @@ Tree path/node access
	//////////////////////////////////////////////////

	/**
	 * Gets the tree path by a given row.
	 *
	 * @param row Row index
	 * @return The tree path or null if the row index is invalid
	 */
	public TreePath getPathByRow(int row)
	{
		return row >= 0 ? tree.getPathForRow(row) : null;
	}

	/**
	 * Gets the TreePath for a specified {@link TreeTableNode} in the visible tree table.
	 *
	 * @param treeNode The {@link TreeTableNode} for which the TreePath is desired
	 * @return The TreePath, if the node exists in the visible tree, else null
	 */
	public TreePath getPathByNode(TreeNode treeNode)
	{
		int rows = tree.getRowCount();
		for (int i = 0; i < rows; i++)
		{
			TreePath path = tree.getPathForRow(i);
			if (path.getLastPathComponent() == treeNode)
				return path;
		}
		return null;
	}

	/**
	 * Gets the tree path located at the specified position relative to the table.
	 *
	 * @param point Location in table component coordinates
	 * @return The path or null
	 */
	public TreePath getPathByPoint(Point point)
	{
		Point treePoint = convertTableToTreePoint(point);
		if (treePoint == null)
			return null;
		TreePath path = tree.getClosestPathForLocation(treePoint.x, treePoint.y);
		return path;
	}

	/**
	 * Gets the tree node by a given row.
	 *
	 * @param row Row index
	 * @return The tree node or null if the row index is invalid
	 */
	public TreeTableNode getNodeByRow(int row)
	{
		return getNodeByPath(getPathByRow(row));
	}

	/**
	 * Gets the tree table node by a given tree path.
	 *
	 * @param path Tree path or null
	 * @return The node or null if the path is null
	 */
	public TreeTableNode getNodeByPath(TreePath path)
	{
		return path != null ? (TreeTableNode) path.getLastPathComponent() : null;
	}

	/**
	 * Gets the tree table node located at the specified position relative to the table.
	 *
	 * @param point Location in table component coordinates
	 * @return The node or null if the point lies not within valid a column/row
	 */
	public TreeTableNode getNodeByPoint(Point point)
	{
		return getNodeByPath(getPathByPoint(point));
	}

	/**
	 * Determines the row by the tree path.
	 *
	 * @param path Tree path
	 * @return The row or -1
	 */
	public int getRowByPath(TreePath path)
	{
		return path != null ? tree.getRowForPath(path) : -1;
	}

	//////////////////////////////////////////////////
	// @@ Visible row access
	//////////////////////////////////////////////////

	/**
	 * Gets the first row that is fully visible in the scroll pane.
	 *
	 * @return The row index or -1 if there are no rows
	 */
	public int getFirstVisibleRow()
	{
		// First, get the scroll pane
		JScrollPane scrollPane = getScrollPane();
		if (scrollPane == null)
		{
			return tree.getRowCount() > 0 ? 0 : -1;
		}

		Rectangle viewRect = scrollPane.getViewport().getViewRect();

		int y = viewRect.y;
		int rowHeight = getRowHeight(0);
		y += rowHeight / 2;
		Point pos = new Point(0, y);

		TreePath path = getPathByPoint(pos);
		if (path == null)
			return -1;

		int row = tree.getRowForPath(path);
		return row;
	}

	/**
	 * Gets the last row that is fully visible in the scroll pane.
	 * If there is no scroll pane surrounding the table, the row count - 1 will be returned.
	 *
	 * @return The row index or -1 if there are no rows
	 */
	public int getLastVisibleRow()
	{
		// First, get the scroll pane
		JScrollPane scrollPane = getScrollPane();
		if (scrollPane == null)
		{
			return tree.getRowCount() - 1;
		}

		Rectangle viewRect = scrollPane.getViewport().getViewRect();

		// We want the last row that is fully visible
		int y = viewRect.y + viewRect.height;
		int rowHeight = getRowHeight(0);
		y -= rowHeight / 2;
		Point pos = new Point(0, y);

		TreePath path = getPathByPoint(pos);
		if (path == null)
			return -1;

		int row = tree.getRowForPath(path);
		return row;
	}

	/**
	 * Gets the number of visible rows in the scroll pane.
	 *
	 * @return The number of visible rows (at least 0) or 0 if there are no rows
	 */
	public int getVisibleRowCount()
	{
		int last = getLastVisibleRow();
		if (last < 0)
			return 0;
		int first = getFirstVisibleRow();
		int n = last - first + 1;
		if (n < 1)
			n = 1;
		return n;
	}

	/**
	 * Configures a component that shall be used in a tree table cell for usage in the tree.
	 * Basically, this method disables the focus traversal keys in order to let the tree table
	 * control the focus traversal between the cells of the tree.
	 *
	 * @param comp Comp
	 */
	public void configureSubComponent(JComponent comp)
	{
		comp.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
		comp.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
	}

	//////////////////////////////////////////////////
	// @@ Cell selection
	//////////////////////////////////////////////////

	/**
	 * Checks of a cell is selectable.
	 *
	 * The column is specified in the table view's display
	 * order, and not in the TableModel's column order.
	 * This is an important distinction because as the user rearranges the columns in the table,
	 * the column at a given index in the view will change.
	 * Meanwhile the user's actions never affect the model's column ordering.
	 *
	 * @param row Row whose value is to be queried
	 * @param col Column whose value is to be queried
	 * @nowarn
	 */
	public boolean isCellSelectable(int row, int col)
	{
		return getTreeTableModel().isCellSelectable(row, convertColumnIndexToModel(col));
	}

	/**
	 * Selects the specified cell.
	 *
	 * @param newRow Index of the row to select
	 * @param newCol Index of the column to select
	 */
	public void selectCell(int newRow, int newCol)
	{
		changeSelection(newRow, newCol, false, false);
	}

	/**
	 * Selects the default cell.
	 * Selects the the first valid content cell or the first tree cell if there is no content cell.
	 * @return
	 *		true	A cell was found we can set the focus to.<br>
	 *		false	No appropriate cell was found.
	 */
	public boolean selectDefaultCell()
	{
		if (!selectDefaultCell(false))
		{
			return selectDefaultCell(true);
		}
		return true;
	}

	/**
	 * Selects the first cell that is selectable.
	 * The method will start at the first row and search downwards for a cell that we can select.
	 * The columns in a row are searched from left to right.
	 *
	 * @param treeCell
	 *		true	The cell to select must be a tree cell.
	 *		false	The cell to select must be a content cell.
	 * @return
	 *		true	A cell was found we can set the focus to.<br>
	 *		false	No appropriate cell was found.
	 */
	public boolean selectDefaultCell(boolean treeCell)
	{
		int colCount = getColumnCount();
		int rowCount = getRowCount();

		for (int row = 0; row < rowCount; ++row)
		{
			for (int col = 0; col < colCount; ++col)
			{
				if (treeCell ? (col == treeCol) : (col != treeCol))
				{
					if (isCellSelectable(row, col))
					{
						selectCell(row, col);
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Sets the selection to a specified node.
	 *
	 * @param node The node to set the selection to
	 * @return true if the selection was successful<br>
	 *			false is not.
	 */
	public boolean selectNode(TreeNode node)
	{
		TreePath path = this.getPathByNode(node);
		if (path == null)
			return false;
		selectCell(tree.getRowForPath(path), treeCol);
		return true;
	}

	//////////////////////////////////////////////////
	// @@ Row selection
	//////////////////////////////////////////////////

	/**
	 * Gets the selection.
	 *
	 * @return An array of tree paths that denote the selected elements or null
	 * if no element has been selected.
	 */
	public TreePath [] getSelection()
	{
		ListSelectionModel selectionModel = getSelectionModel();
		int min = selectionModel.getMinSelectionIndex();
		int max = selectionModel.getMaxSelectionIndex();
		if (min >= 0)
		{
			List paths = new ArrayList();

			for (int i = min; i <= max; i++)
			{
				if (selectionModel.isSelectedIndex(i))
				{
					paths.add(tree.getPathForRow(i));
				}
			}

			return (TreePath []) CollectionUtil.toArray(paths, TreePath.class);
		}

		return null;
	}

	/**
	 * Gets the selection mode.
	 * @return {@link #SELECTION_NONE}/{@link #SELECTION_SINGLE}/{@link #SELECTION_MULTI}
	 */
	public int getSelectionMode()
	{
		return selectionMode;
	}

	/**
	 * Sets the selection mode.
	 * @param selectionMode {@link #SELECTION_NONE}/{@link #SELECTION_SINGLE}/{@link #SELECTION_MULTI}
	 */
	public void setSelectionMode(int selectionMode)
	{
		this.selectionMode = selectionMode;
		getSelectionModel().setSelectionMode(selectionMode);
	}

	/**
	 * Saves the current position (i\.e\. the currently selected cell).
	 */
	public void saveCurrentPosition()
	{
		savedCurrentCol = currentCol;
		savedCurrentRow = currentRow;
	}

	/**
	 * Restores the current position (i\.e\. the currently selected cell).
	 */
	public void restoreCurrentPosition()
	{
		if (savedCurrentCol != -1)
		{
			selectCell(savedCurrentRow, savedCurrentCol);
			savedCurrentCol = savedCurrentRow = -1;
		}
	}

	//////////////////////////////////////////////////
	// @@ Expansion/collapsion
	//////////////////////////////////////////////////

	/**
	 * Expands all tree nodes or collapse these level by level.
	 *
	 * @param expand
	 *  true  expands all nodes in the tree<br>
	 *  false collapses all nodes in the tree
	 * @param numberOfLevelThatAreNeededToBeExpanded The number of level that
	 * are needed to be expand. The value '-1' expands all levels.
	 */
	public void expandTreeLevels(boolean expand, int numberOfLevelThatAreNeededToBeExpanded)
	{
		TreeUtil.expandTreeLevels(tree, expand, numberOfLevelThatAreNeededToBeExpanded);
		sizeRowsToFit();
		sizeColumnsToFit();
	}

	/**
	 * Expands all tree nodes or collapse these.
	 *
	 * @param expand
	 *  true  expands all nodes in the tree<br>
	 *  false collapses all nodes in the tree
	 */
	public void expandAll(boolean expand)
	{
		expandTreeLevels(expand, -1);
	}

	/**
	 * Expands all tree nodes or collapse these.
	 *
	 * @param parent The parent tree path
	 * @param expand
	 *  true  expands all nodes in the tree<br>
	 *  false collapses all nodes in the tree
	 */
	public void expandAll(TreePath parent, boolean expand)
	{
		expandTreeLevels(parent, expand, -1);
	}

	/**
	 * Expands all tree nodes or collapse these level by level.
	 *
	 * @param parent The parent tree path
	 * @param expand
	 *  true  expands all nodes in the tree<br>
	 *  false collapses all nodes in the tree
	 * @param numberOfLevelThatAreNeededToBeExpanded The number of level that
	 * are needed to be expand. The value '-1' expands all levels.
	 */
	public void expandTreeLevels(TreePath parent, boolean expand, int numberOfLevelThatAreNeededToBeExpanded)
	{
		TreeUtil.expandTreeLevels(tree, parent, expand, numberOfLevelThatAreNeededToBeExpanded);
		sizeRowsToFit();
		sizeColumnsToFit();
	}

	/**
	 * Ensures that the node in the specified row is either collapsed or expanded depending on
	 * the state or the node.
	 *
	 * @param row An integer specifying a display row, where 0 is the first row in the display
	 */
	public void toggleRow(int row)
	{
		TreePath path = tree.getPathForRow(row);
		togglePath(path);
	}

	/**
	 * Ensures that the node in the specified path is either collapsed or expanded depending on
	 * the state or the node.
	 *
	 * @param path  the TreePath identifying a node
	 */
	public void togglePath(TreePath path)
	{
		if (tree.isCollapsed(path))
		{
			expandPath(path);
		}
		else
		{
			collapsePath(path);
		}
	}

	/**
	 * Ensures that the node in the specified row is collapsed.
	 * <p>
	 * If row is < 0 or >= getRowCount this
	 * will have no effect.
	 *
	 * @param row An integer specifying a display row, where 0 is the first row in the display
	 */
	public void collapseRow(int row)
	{
		tree.collapseRow(row);
	}

	/**
	 * Ensures that the node in the specified row is expanded and viewable.
	 *
	 * If row is < 0 or >= getRowCount this
	 * will have no effect.
	 *
	 * @param row An integer specifying a display row, where 0 is the first row in the display
	 */
	public void expandRow(int row)
	{
		tree.expandRow(row);
	}

	/**
	 * Ensures that the node identified by the specified path is
	 * collapsed and viewable.
	 *
	 * @param path  the TreePath identifying a node
	 */
	public void collapsePath(TreePath path)
	{
		tree.collapsePath(path);
	}

	/**
	 * Ensures that the node identified by the specified path is
	 * expanded and viewable.
	 *
	 * @param path  the TreePath identifying a node
	 */
	public void expandPath(TreePath path)
	{
		tree.expandPath(path);
	}

	//////////////////////////////////////////////////
	// @@ Keyboard handling
	//////////////////////////////////////////////////

	/**
	 * Sets up the key bindings of the component.
	 */
	private void setupKeyBindings()
	{
		if (focusInputMap != null)
		{
			// Already installed
			return;
		}

		// Create new input and action maps, using the original ones as parent
		focusInputMap = new InputMap();
		focusInputMap.setParent(SwingUtilities.getUIInputMap(this, WHEN_FOCUSED));

		focusAncestorInputMap = new InputMap();
		focusAncestorInputMap.setParent(SwingUtilities.getUIInputMap(this, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));

		actionMap = new ActionMap();
		actionMap.setParent(SwingUtilities.getUIActionMap(this));

		// Set all key bindings we are interested in
		defineKey(KeyEvent.VK_ENTER, 0, CMD_ENTER);
		defineKey(KeyEvent.VK_TAB, 0, CMD_TAB);
		defineKey(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK, CMD_BACKTAB);
		defineKey(KeyEvent.VK_SPACE, 0, CMD_SPACE);
		defineKey(KeyEvent.VK_LEFT, 0, CMD_LEFT);
		defineKey(KeyEvent.VK_RIGHT, 0, CMD_RIGHT);
		defineKey(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK, CMD_LEFT | CMD_SWITCH_TREE);
		defineKey(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK, CMD_RIGHT | CMD_SWITCH_TREE);
		defineKey(KeyEvent.VK_UP, 0, CMD_UP);
		defineKey(KeyEvent.VK_DOWN, 0, CMD_DOWN);
		defineKey(KeyEvent.VK_PAGE_UP, 0, CMD_PGUP);
		defineKey(KeyEvent.VK_PAGE_DOWN, 0, CMD_PGDN);
		defineKey(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_MASK, CMD_TOP);
		defineKey(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_MASK, CMD_BOTTOM);
		defineKey(KeyEvent.VK_HOME, 0, CMD_HOME | CMD_SWITCH_TREE);
		defineKey(KeyEvent.VK_END, 0, CMD_END | CMD_SWITCH_TREE);
		defineKey(KeyEvent.VK_ESCAPE, 0, CMD_ESC);

		// Install the new input and action maps
		SwingUtilities.replaceUIInputMap(this, WHEN_FOCUSED, focusInputMap);
		SwingUtilities.replaceUIInputMap(this, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, focusAncestorInputMap);
		SwingUtilities.replaceUIActionMap(this, actionMap);
	}

	/** Counter for generated action ids */
	private static int actionIdCounter;

	/**
	 * Defines a key binding.
	 *
	 * @param keyCode Key code
	 * @param keyModifier Key modifier
	 * @param command Command to execute on the key stroke
	 */
	private void defineKey(int keyCode, int keyModifier, int command)
	{
		String actionId = "TT" + actionIdCounter++;

		// Register the action id for the provided key code
		KeyStroke ks = KeyStroke.getKeyStroke(keyCode, keyModifier);
		focusInputMap.put(ks, actionId);
		focusAncestorInputMap.put(ks, actionId);

		// Determine the command
		actionMap.put(actionId, new CommandAction(actionId, command));
	}

	private class CommandAction extends AbstractAction
	{
		/** Command to be processed */
		int command;

		/**
		 * Constructor.
		 *
		 * @param actionId Action id
		 * @param command Command to be processed
		 */
		public CommandAction(String actionId, int command)
		{
			super(actionId);
			this.command = command;
		}

		public void actionPerformed(ActionEvent e)
		{
			processCommand(command);
		}
	}

	/**
	 * Processses a key event.
	 *
	 * @param e Event to process
	 */
	public void handleKeyEvent(KeyEvent e)
	{
		processKeyEvent(e);
		e.consume();
	}

	// Directions
	private static final int LEFT = 1;

	private static final int RIGHT = 2;

	private static final int UP = 3;

	private static final int DOWN = 4;

	/**
	 * Handles a command.
	 *
	 * @param command Command to process
	 */
	protected void processCommand(int command)
	{
		if (fireCommand(command))
		{
			// Command has been processed by an event listener
			return;
		}

		// Note: We assume the tree column to be column 0

		int colCount = getColumnCount();
		int rowCount = getRowCount();

		int minCol = treeCol;
		int maxCol = colCount - 1;

		// Start using the currently selected cell
		int col = currentCol;
		if (col < 0)
		{
			col = getSelectedColumn();
			if (col < 0)
				col = 0;
		}
		int row = currentRow;
		if (row < 0)
		{
			row = getSelectedRow();
			if (row < 0)
				row = 0;
		}

		if (command == CMD_TAB || command == CMD_BACKTAB)
		{
			// We restrict movement to non-tree columns for the TAB key
			minCol = 1;
		}
		else if ((command & CMD_SWITCH_TREE) != 0)
		{
			// We should switch from tree to table columns
			if (col == treeCol)
			{
				// We are in the tree column, restrict to table columns
				minCol = treeCol + 1;
			}
			else
			{
				// We are in the table column, restrict to tree column
				maxCol = treeCol;
			}
		}
		else
		{
			// We should not switch from tree to table columns
			if (col == treeCol)
			{
				// We are in the tree column, restrict to table columns
				maxCol = treeCol;
			}
			else
			{
				// We are in the table column, restrict to tree column
				minCol = treeCol + 1;
			}
		}

		if (col < 0)
			col = 0;
		if (row < 0)
			row = 0;

		command &= CMD_CODE_MASK;
		if (command == CMD_NONE)
			return;

		int direction = 0;
		boolean wrap = false;

		if (col == treeCol && minCol == treeCol && (command == CMD_ENTER || command == CMD_SPACE || command == CMD_LEFT || command == CMD_RIGHT))
		{
			// We are in the tree column and the current key
			// is supposed to expand or collapse the tree

			TreePath path = tree.getPathForRow(row);
			if (path != null)
			{
				TreeTableNode node = (TreeTableNode) path.getLastPathComponent();
				boolean isFolder = !node.isLeaf();

				switch (command)
				{
				case CMD_ENTER:
					if (isFolder)
					{
						expandPath(path);
					}
					if (row + 1 < rowCount)
					{
						row++;
					}
					direction = DOWN;
					wrap = true;
					break;

				case CMD_SPACE:
					if (isFolder)
					{
						togglePath(path);
					}
					break;

				case CMD_LEFT:
					if (isFolder && !tree.isCollapsed(path))
					{
						collapsePath(path);
					}
					else
					{
						// Go to parent node if collapsed
						TreePath parentPath = path.getParentPath();
						row = tree.getRowForPath(parentPath);
						if (row < 0)
						{
							// Happens when the root node is not visible
							row = 0;
						}
					}
					direction = UP;
					break;

				case CMD_RIGHT:
					if (isFolder && tree.isCollapsed(path))
					{
						expandPath(path);
					}
					else if (row + 1 < rowCount)
					{
						// One row down if expanded
						row++;
					}
					direction = DOWN;
					break;
				}
			}
		}
		else
		{
			switch (command)
			{
			case CMD_SPACE:
				// Space does not have any function here, ignore
				return;

			case CMD_ENTER:
			case CMD_TAB:
				++col;
				direction = RIGHT;
				wrap = true;
				break;

			case CMD_BACKTAB:
				--col;
				direction = LEFT;
				wrap = true;
				break;

			case CMD_LEFT:
				--col;
				direction = LEFT;
				break;

			case CMD_RIGHT:
				++col;
				direction = RIGHT;
				break;

			case CMD_UP:
				direction = UP;
				--row;
				break;

			case CMD_DOWN:
				direction = DOWN;
				++row;
				break;

			case CMD_PGUP:
			{
				int firstRow = getFirstVisibleRow();
				if (firstRow >= 0)
				{
					if (firstRow == row)
					{
						// We are already at the top row, scroll back
						firstRow -= getVisibleRowCount();
						if (firstRow < 0)
							firstRow = 0;
					}
				}
				else
				{
					firstRow = row;
				}
				row = firstRow;
				direction = DOWN;
			}
				break;

			case CMD_PGDN:
			{
				int lastRow = getLastVisibleRow();
				if (lastRow >= 0)
				{
					if (lastRow == row)
					{
						// We are already at the top row, scroll back
						lastRow += getVisibleRowCount();
						if (lastRow >= getRowCount())
							lastRow = getRowCount() - 1;
					}
				}
				else
				{
					lastRow = row;
				}
				row = lastRow;
				direction = UP;
			}
				break;

			case CMD_TOP:
				row = 0;
				direction = DOWN;
				break;

			case CMD_BOTTOM:
				row = getRowCount() - 1;
				direction = UP;
				break;

			case CMD_HOME:
				col = minCol;
				direction = RIGHT;
				break;

			case CMD_END:
				col = maxCol;
				direction = LEFT;
				break;
			}
		}

		// Find a cell we can select
		int initalRow = row;
		int initalCol = col;

		// We loop as long as we move outside the restricted column area or the current cell is not editable
		int i = 0;
		while (col < minCol || col > maxCol || !isCellEditable(row, col))
		{
			if (++i > 100)
			{
				// Maximum loop counter just for safety...
				break;
			}

			switch (direction)
			{
			case RIGHT:
				if (col < maxCol)
				{
					col++;
				}
				else
				{
					col = minCol;

					if (row + 1 < rowCount)
					{
						row++;
					}
					else
					{
						if (!wrap)
						{
							// We don't wrap around
							return;
						}
						row = 0;
					}
				}
				break;

			case LEFT:
				if (col > minCol)
				{
					col--;
				}
				else
				{
					col = maxCol;

					if (row - 1 >= 0)
					{
						row--;
					}
					else
					{
						if (!wrap)
						{
							// We don't wrap around
							return;
						}
						row = rowCount - 1;
					}
				}
				break;

			case UP:
				if (row > 0)
				{
					row--;
				}
				else
				{
					if (!wrap)
					{
						// We don't wrap around
						return;
					}
					row = rowCount - 1;
				}
				break;

			case DOWN:
				if (row + 1 < rowCount)
				{
					row++;
				}
				else
				{
					if (!wrap)
					{
						// We don't wrap around
						return;
					}
					row = 0;
				}
				break;
			}

			if (initalRow == row && initalCol == col)
			{
				// We came back where we started, kick it...
				row = 0;
				break;
			}
		}

		// Make sure we have a valid row
		if (row < 0)
			row = 0;
		else if (row >= rowCount)
			row = rowCount - 1;

		if (!isCellEditable(row, col))
		{
			// Failure
			return;
		}

		// Finally, set the selection
		selectCell(row, col);
	}

	//////////////////////////////////////////////////
	// @@ Listener support
	//////////////////////////////////////////////////

	/**
	 * Fires a 'command' event to all registered tree table listeners.
	 *
	 * @param command Command to execute
	 * @return
	 *		true	The event has been consumed, no further processing desired.<br>
	 *		false	The event has not been consumed.
	 */
	protected boolean fireCommand(int command)
	{
		if (listenerSupport != null && listenerSupport.containsListeners(TreeTableListener.class))
		{
			TreeTableEvent e = null;
			for (Iterator it = listenerSupport.getListenerIterator(TreeTableListener.class); it.hasNext();)
			{
				if (e == null)
				{
					e = new TreeTableEvent(this, TreeTableEvent.EVENT_COMMAND);
					e.setCommand(command);
				}

				((TreeTableListener) it.next()).processCommand(e);
				if (e.isConsumed())
					return true;
			}
		}

		return false;
	}

	/**
	 * Adds a tree table listener to the listener list.
	 *
	 * @param listener The listener to be added
	 */
	public synchronized void addTreeTableListener(TreeTableListener listener)
	{
		if (listenerSupport == null)
		{
			listenerSupport = new ListenerSupport();
		}
		listenerSupport.addListener(TreeTableListener.class, listener);
	}

	/**
	 * Adds a tree table listener to the listener list as weak listener.
	 * The listener is registered a WEAK listener, i. e. it may
	 * be garbage-collected if not referenced otherwise.<br>
	 * ATTENTION: Never add an automatic class (i. e new TreeTableListener () { ... }) or an inner
	 * class that is not referenced otherwise as a weak listener to the list. These objects
	 * will be cleared by the garbage collector during the next gc run!
	 *
	 * @param listener The listener to be added
	 */
	public synchronized void addWeakTreeTableListener(TreeTableListener listener)
	{
		if (listenerSupport == null)
		{
			listenerSupport = new ListenerSupport();
		}
		listenerSupport.addWeakListener(TreeTableListener.class, listener);
	}

	/**
	 * Removes a tree table listener from the listener list.
	 *
	 * @param listener The listener to be removed
	 */
	public synchronized void removeTreeTableListener(TreeTableListener listener)
	{
		if (listenerSupport != null)
		{
			listenerSupport.removeListener(TreeTableListener.class, listener);
		}
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Converts the tabel point coordinates to tree point coordinates, relative to the position
	 * of the tree column in the table.
	 *
	 * @param tablePoint The point position relative to the table
	 * @return The point position relative to the tree. If the point does not lie over the
	 * tree column then null is returned.
	 */
	private Point convertTableToTreePoint(Point tablePoint)
	{
		int width = 0;

		for (int i = 0; i < getColumnCount(); i++)
		{
			width = width + getColumnModel().getColumn(i).getWidth();
			if ((tablePoint.x - width) < 0)
			{
				if (i == treeCol)
				{
					int treeXPos = getColumnModel().getColumn(i).getWidth() - width + tablePoint.x;
					return new Point(treeXPos, tablePoint.y);
				}
			}
		}

		return null;
	}

	//////////////////////////////////////////////////
	// @@ MouseListener implementation
	//////////////////////////////////////////////////

	/**
	 * Delegates any mouse event that occurred on the tree column and was not Button 1
	 * to the tree with new pointer coordinates relative to the tree.
	 *
	 * Invoked when the mouse has been clicked on a component.
	 * @nowarn
	 */
	public void mouseClicked(MouseEvent me)
	{
		forwardMouseEventToTree(me);
	}

	/**
	 * Invoked when the mouse button is pressed.
	 * @nowarn
	 */
	public void mousePressed(MouseEvent me)
	{
	}

	/**
	 * Invoked when the mouse button is released.
	 * @nowarn
	 */
	public void mouseReleased(MouseEvent me)
	{
	}

	/**
	 * Invoked when the mouse pointer enteres a region.
	 * @nowarn
	 */
	public void mouseEntered(MouseEvent me)
	{
	}

	/**
	 * Invoked when the mouse pointer exits a region.
	 * @nowarn
	 */
	public void mouseExited(MouseEvent me)
	{
	}

	/**
	 * Forwards the given mouse event to thee tree component after correcting the x position
	 * if it occured in the tree column.
	 *
	 * @param me Event
	 */
	private void forwardMouseEventToTree(MouseEvent me)
	{
		int xPosition = me.getPoint().x;
		int width = 0;
		int col = -1;

		for (int i = 0; i < getColumnCount(); i++)
		{
			width = width + getColumnModel().getColumn(i).getWidth();
			if ((xPosition - width) < 0)
			{
				col = i;
				break;
			}
		}

		if (col == treeCol && me.getButton() != MouseEvent.BUTTON1)
		{
			MouseEvent newME = new MouseEvent(tree, me.getID(), me.getWhen(), me.getModifiers(), xPosition - width, me.getY(), me.getClickCount(), me.isPopupTrigger());

			tree.dispatchEvent(newME);
		}
	}

	//////////////////////////////////////////////////
	// @@ Static utilities
	//////////////////////////////////////////////////

	/**
	 * Creates a text value for display in a text-only cell (e\. g\. a description cell).
	 * The method will check if the text is multi line text.
	 * If yes, the method returns an array or two strings, the first string the text to display
	 * in the cell, the second string the text that should appear as tool tip.
	 * Otherwise, the text will be returned as string.<br>
	 * The returned object will be recognized by the {@link DefaultTableCellRenderer} which will create
	 * a formatted tool tip from the full text.
	 *
	 * @param text Text to display
	 * @return A string, an array of Strings of size 2 or null
	 */
	public static Object createDescriptionCellValue(String text)
	{
		if (text != null)
		{
			String summary = TextUtil.extractSummary(text);
			if (!text.equals(summary))
			{
				text = text.substring(summary.length());
				text = text.trim();

				String [] a = new String [2];
				a [0] = summary;
				a [1] = text;
				return a;
			}
		}
		return text;
	}

	//////////////////////////////////////////////////
	// @@ Helper classes
	//////////////////////////////////////////////////

	/**
	 * Tree table model listener.
	 */
	private class TTModelListener
		implements TreeTableModelListener
	{
		/** The selected rows */
		private int [] selectedRows = null;

		public void beforeChanges(int tableEventType)
		{
			if (tableEventType == TableModelEvent.UPDATE)
			{
				if (selectedRows == null)
					selectedRows = getSelectedRows();
			}
		}

		public void afterChanges(int tableEventType)
		{
			sizeRowsToFit();
			sizeColumnsToFit();

			if (tableEventType == TreeTableModelListener.NO_TABLE_MODEL_EVENT_TYPE)
				return;

			if (selectedRows != null)
			{
				for (int i = 0; i < selectedRows.length; i++)
				{
					changeSelection(selectedRows [i], currentCol, false, true);
				}
				selectedRows = null;
			}
		}

		public void treeNodesChanged(TreeModelEvent e)
		{
		}

		public void treeNodesInserted(TreeModelEvent e)
		{
			updateUI();

			TreePath path = e.getTreePath();
			TreeTableNode node = (TreeTableNode) path.getLastPathComponent();
			selectNode(node);
		}

		public void treeNodesRemoved(TreeModelEvent e)
		{
			TreePath path = e.getTreePath();
			int row = tree.getRowForPath(path) - 1;

			updateUI();

			TreePath newPath = tree.getPathForRow(row);
			TreeTableNode node = (newPath == null) ? null : (TreeTableNode) newPath.getLastPathComponent();
			if (node != null)
				selectNode(node);
		}

		public void treeStructureChanged(TreeModelEvent e)
		{
		}
	}
}
