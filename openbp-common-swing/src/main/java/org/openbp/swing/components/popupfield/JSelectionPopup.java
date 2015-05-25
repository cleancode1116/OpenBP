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
package org.openbp.swing.components.popupfield;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListDataListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

/**
 * Popup for the {@link JSelectionField} class.
 *
 * @author Heiko Erhardt
 */
public class JSelectionPopup extends JPopupMenu
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Associated selection field */
	private JSelectionField selectionField;

	/** List component that holds the selectable items */
	private JList list;

	/** Scroll bar that scrolls the list */
	private JScrollPane scroller;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param selectionField Associated selection field
	 */
	public JSelectionPopup(JSelectionField selectionField)
	{
		setInvoker(selectionField);

		setLightWeightPopupEnabled(true);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorderPainted(true);
		setBorder(BorderFactory.createLineBorder(Color.black));
		setOpaque(false);
		setDoubleBuffered(true);

		this.selectionField = selectionField;

		// Create and configure the components of the popup
		createComponent();

		add(scroller);
	}

	/**
	 * Creates the list component that is displayed in the popup and its enclosing scrollbar.
	 */
	protected void createComponent()
	{
		// Make a new list using a model that accesses the selection field items
		list = new JList(createSelectionFieldListModel());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Set the ui properties of the list
		JTextField textField = selectionField.getTextField();
		list.setFont(textField.getFont());
		list.setForeground(UIManager.getColor("TextField.foreground"));
		list.setBackground(UIManager.getColor("TextField.background"));
		list.setSelectionForeground(UIManager.getColor("ComboBox.selectionForeground"));
		list.setSelectionBackground(UIManager.getColor("ComboBox.selectionBackground"));

		// Attach handlers
		MouseInputListener listMouseHandler = new ListMouseHandler();
		list.addMouseListener(listMouseHandler);
		list.addMouseMotionListener(listMouseHandler);
		list.addKeyListener(new ListKeyHandler());
		list.addFocusListener(new ListFocusHandler());

		// Create the scrollbar (vertical scroll only)
		scroller = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setRequestFocusEnabled(false);
		scroller.getVerticalScrollBar().setRequestFocusEnabled(false);
	}

	/**
	 * Creates a list model that maps to the items of the selection field.
	 * @return The new model
	 */
	protected ListModel createSelectionFieldListModel()
	{
		return new ListModel()
		{
			public int getSize()
			{
				return selectionField.getNumberOfItems();
			}

			public Object getElementAt(int index)
			{
				Object o = selectionField.getItemTextAt(index);
				if (o == null)
					o = " ";
				return o;
			}

			public void addListDataListener(ListDataListener l)
			{
			}

			public void removeListDataListener(ListDataListener l)
			{
			}
		};
	}

	/**
	 * Gets the list component that holds the selectable items.
	 * @nowarn
	 */
	public JList getList()
	{
		return list;
	}

	//////////////////////////////////////////////////
	// @@ JComponent overrides
	//////////////////////////////////////////////////

	/**
	 * Requests the focus for this component.
	 */
	public void requestFocus()
	{
		// Delegate the focus to the text field
		list.requestFocus();
	}

	/**
	 * Overridden to unconditionally return false.
	 * @nowarn
	 */
	public boolean isFocusTraversable()
	{
		return false;
	}

	//////////////////////////////////////////////////
	// @@ Show/hide methods
	//////////////////////////////////////////////////

	/**
	 * Shows the popup.
	 */
	public void showPopup()
	{
		selectionField.firePopup(PopupEvent.POPUP_OPENING);

		int x = 0;
		int y = selectionField.getHeight();
		int w = selectionField.getWidth() - 2;
		int h = computePopupHeightForRowCount(selectionField.getMaximumRowCount());
		if (selectionField.label != null)
		{
			int labelWidth = selectionField.label.getWidth();
			x += labelWidth;
			w -= labelWidth;
		}

		Rectangle popupBounds = computePopupBounds(x, y, w, h);
		Dimension popupSize = popupBounds.getSize();
		scroller.setMaximumSize(popupSize);
		scroller.setPreferredSize(popupSize);
		scroller.setMinimumSize(popupSize);

		list.invalidate();
		updateListIndex();

		show(selectionField, popupBounds.x, popupBounds.y);

		list.requestFocus();

		selectionField.firePopup(PopupEvent.POPUP_OPENED);
	}

	/**
	 * Cancels the popup menu without accepting the selection.
	 */
	public void cancelPopup()
	{
		list.setSelectedIndex(-1);
		if (isVisible())
		{
			hidePopup();
		}
	}

	/**
	 * Hides the popup.
	 */
	public void hidePopup()
	{
		selectionField.notifyPopupClosed();

		selectionField.firePopup(PopupEvent.POPUP_CLOSING);

		setVisible(false);

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				selectionField.repaint();
				selectionField.requestFocus();
			}
		});

		selectionField.firePopup(PopupEvent.POPUP_CLOSED);
	}

	/**
	 * Transfers the list selection to the selection field and closes the popup.
	 */
	void acceptSelection()
	{
		updateSelectionFieldIndex();
		cancelPopup();
	}

	/**
	 * Updates the current selection of the list according to the current selection field index.
	 */
	void updateListIndex()
	{
		int selectedIndex = selectionField.getSelectedIndex();

		if (selectedIndex == -1)
		{
			list.clearSelection();
		}
		else
		{
			list.setSelectedIndex(selectedIndex);
		}
		list.ensureIndexIsVisible(selectedIndex);
	}

	/**
	 * Updates the selection field index according to the current selection of the list.
	 */
	void updateSelectionFieldIndex()
	{
		int index = list.getSelectedIndex();
		if (index >= 0)
		{
			selectionField.setSelectedIndex(index);
			selectionField.fireActionPerformed();
		}
	}

	//////////////////////////////////////////////////
	// @@ List event listeners
	//////////////////////////////////////////////////

	protected class ListMouseHandler extends MouseInputAdapter
	{
		/**
		 * Updates the selection field index and hides the popup when the mouse is released in the list.
		 */
		public void mouseReleased(MouseEvent e)
		{
			acceptSelection();
		}

		/**
		 * Changes the selected item as you move the mouse over the list.
		 * The selection change is not committed to the model, this is for user feedback only.
		 */
		public void mouseMoved(MouseEvent e)
		{
			Point location = e.getPoint();

			Rectangle r = new Rectangle();
			list.computeVisibleRect(r);
			if (r.contains(location))
			{
				updateListBoxSelectionForEvent(e);
			}
		}
	}

	public class ListKeyHandler extends KeyAdapter
	{
		/**
		 * This listener watches for the spacebar or enter being pressed and shows/hides the popup accordingly.
		 * @nowarn
		 */
		public void keyPressed(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER)
			{
				if (isVisible())
				{
					acceptSelection();
				}
				else
				{
					showPopup();
				}
				e.consume();
				return;
			}

			if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				cancelPopup();
				e.consume();
				return;
			}

			if (e.getKeyCode() == KeyEvent.VK_TAB)
			{
				acceptSelection();
				e.consume();
				return;
			}
		}
	}

	public class ListFocusHandler extends FocusAdapter
	{
		/**
		 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
		 */
		public void focusLost(FocusEvent e)
		{
			cancelPopup();
		}
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Calculates the height of the popup given a maximum row count.
	 * If there are less items in the selection field, the popup will be smaller.
	 *
	 * @param maxRowCount Maximum row count
	 * @return The height (default of 100 if computation fails for any reason)
	 */
	protected int computePopupHeightForRowCount(int maxRowCount)
	{
		ListCellRenderer renderer = list.getCellRenderer();
		int n = selectionField.getNumberOfItems();
		int rowCount = Math.min(maxRowCount, n);
		int height = 0;

		for (int i = 0; i < rowCount; ++i)
		{
			Object value = list.getModel().getElementAt(i);
			if (value == null)
				value = "x";
			Component c = renderer.getListCellRendererComponent(list, value, i, false, false);
			height += c.getPreferredSize().height;
		}

		return height == 0 ? 100 : height;
	}

	/**
	 * Computes the bounds of the popup.
	 * Positions the popup beneath the selection field.
	 *
	 * @param px X coordinate (relative to the selection field)
	 * @param py Y coordinate (relative to the selection field)
	 * @param pw Width
	 * @param ph Height
	 * @return The bounds (relative to the selection field)
	 */
	protected Rectangle computePopupBounds(int px, int py, int pw, int ph)
	{
		// The popup must not exceed the container boundaries.
		Component container = SwingUtilities.getAncestorOfClass(JScrollPane.class, selectionField);
		if (container == null)
		{
			container = SwingUtilities.getWindowAncestor(selectionField);
		}
		Point containerLocation = container.getLocationOnScreen();
		int minY = containerLocation.y;
		int maxY = containerLocation.y + container.getHeight();

		Point selectionLocation = selectionField.getLocationOnScreen();

		// Check if the popup fits below the field
		int endy = selectionLocation.y + py + ph;
		if (endy < maxY)
		{
			return new Rectangle(px, py, pw, ph);
		}

		// Check if the popup fits above the field
		int starty = selectionLocation.y + py + ph - 1;
		if (starty > minY)
		{
			return new Rectangle(px, -ph - 1, pw, ph);
		}

		int diffBelow = maxY - py;
		int diffAbove = selectionLocation.y - minY - 1;
		if (diffBelow > diffAbove)
		{
			return new Rectangle(px, py, pw, diffBelow);
		}
		return new Rectangle(px, -diffAbove - 1, pw, diffAbove);
	}

	/**
	 * Given a mouse event, changes the list selection to the list item below the mouse.
	 *
	 * @param e Mouse event (refering to list coordinates)
	 */
	protected void updateListBoxSelectionForEvent(MouseEvent e)
	{
		Point location = e.getPoint();
		if (list == null)
			return;

		int index = list.locationToIndex(location);
		if (index == -1)
		{
			if (location.y < 0)
				index = 0;
			else
				index = selectionField.getNumberOfItems() - 1;
		}

		if (list.getSelectedIndex() != index)
		{
			list.setSelectedIndex(index);
		}
	}
}
