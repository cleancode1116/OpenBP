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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTextField;

import org.openbp.common.CommonUtil;
import org.openbp.common.util.iterator.EmptyIterator;
import org.openbp.swing.components.treetable.JTreeTable;

/**
 * Selection field component.
 * This component works similar to a JComboBox.
 * However, it's ui is a little different and it has much less overhead than
 * the JComboBox (well, it doesn't support all that model and ui stuff the combo box does).
 * In contrast to the JComboBox, it can also be used as cell component of a {@link JTreeTable}
 * (well, the reason why a JComboBox doesn't work - the focus gets lost when leaving the combo
 * box, also the selection of the first item does not always work - is undetermined).
 *
 * The selection box support localization in that way that it's item list
 * may optionally contain a display name beside the actual item value.
 *
 * Same as the combo box, the selection field can be editable (which allows text input by
 * the user) or a simple list selection field (which provides the values in the item list
 * only).
 *
 * @author Heiko Erhardt
 */
public class JSelectionField extends JPopupField
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Item list (contains objects) */
	private List itemList;

	/** Maximum row count to display in the popup */
	private int maximumRowCount = 10;

	/** Status flag: Popup closed */
	// private boolean popupClosed;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Popup menu containing the selection list */
	protected JSelectionPopup popup;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public JSelectionField()
	{
		super();

		add(textField, BorderLayout.CENTER);
	}

	/**
	 * Creates the text field.
	 * @nowarn
	 */
	protected JTextField createTextField()
	{
		JTextField tf = super.createTextField();

		tf.addMouseListener(new SelectionMouseListener());

		return tf;
	}

	/**
	 * Adjusts the preferred size of the selection text field according to the length of the selection list texts.
	 */
	public void adjustPreferredSize()
	{
		int n = getNumberOfItems();
		if (n > 0)
		{
			Dimension size = textField.getMinimumSize();
			int max = size.width;

			Font font = getFont();
			FontMetrics fm = getFontMetrics(font);

			for (int i = 0; i < n; ++i)
			{
				Object o = itemList.get(i);
				String s = o != null ? o.toString() : null;
				if (s != null)
				{
					int w = fm.stringWidth(s);
					if (w > max)
						max = w;
				}
			}

			Dimension prefSize = arrowButton.getPreferredSize();
			max += prefSize.width;
			if (label != null)
			{
				prefSize = label.getPreferredSize();
				max += prefSize.width;
			}
			max += 8;

			size = getMaximumSize();
			size.width = max;
			setMaximumSize(size);
		}
	}

	//////////////////////////////////////////////////
	// @@ JComponent overrides
	//////////////////////////////////////////////////

	/**
	 * Selects an item using a key character.
	 * This will select the next item that starts with the given character from the item list,
	 * wrapping around if the item cannot be found from the current position downwards.
	 *
	 * @param selectionChar Selection character
	 * @return
	 *		true	An item was selected.<br>
	 *		false	No item was found that begins with the specified character.
	 */
	protected boolean selectWithKeyChar(char selectionChar)
	{
		int n = getNumberOfItems();
		if (n == 0)
			return false;

		int index = getSelectedIndex();

		// Start from current position
		for (int i = index + 1; i < n; ++i)
		{
			Object o = itemList.get(i);
			String s = o != null ? o.toString() : null;

			if (s != null && s.length() > 0)
			{
				char c = Character.toLowerCase(s.charAt(0));
				if (c == selectionChar)
				{
					setSelectedIndex(i);
					fireActionPerformed();
					return true;
				}
			}
		}

		// Wrap around
		for (int i = 0; i < index; ++i)
		{
			Object o = itemList.get(i);
			String s = o != null ? o.toString() : null;

			if (s != null && s.length() > 0)
			{
				char c = Character.toLowerCase(s.charAt(0));
				if (c == selectionChar)
				{
					setSelectedIndex(i);
					fireActionPerformed();
					return true;
				}
			}
		}

		return false;
	}

	//////////////////////////////////////////////////
	// @@ Popup operations
	//////////////////////////////////////////////////

	/**
	 * Gets the popup visibility.
	 * @return
	 *		true	The popup is shown.<br>
	 *		false	The popup is hidden.
	 */
	public boolean isPopupVisible()
	{
		return popup != null && popup.isVisible();
	}

	/**
	 * Sets the popup visibility.
	 * @param popupVisible
	 *		true	Shows the popup.<br>
	 *		false	Hides the popup.
	 */
	public void setPopupVisible(boolean popupVisible)
	{
		if (popupVisible)
		{
			if (popup == null)
			{
				popup = new JSelectionPopup(this);
			}

			// popupClosed = false;
			popup.showPopup();
		}
		else
		{
			if (popup != null)
			{
				popup.hidePopup();
			}
		}
	}

	void notifyPopupClosed()
	{
		// popupClosed = true;
	}

	//////////////////////////////////////////////////
	// @@ Selection/text access
	//////////////////////////////////////////////////

	/**
	 * Gets the selected item.
	 * @return The selected item or null if no item of the item list has been selected.
	 * In the latter case, use the getText method to retrieve the text the user has entered in
	 * the selection field if the selection field is editable.
	 */
	public Object getSelectedItem()
	{
		if (itemList != null)
		{
			String text = getText();
			if (text != null)
			{
				// Check if the field text matches the display text of an item in the list
				int n = itemList.size();
				for (int i = 0; i < n; ++i)
				{
					Object o = itemList.get(i);
					if (o == null)
						continue;

					String s = o.toString();
					if (CommonUtil.equalsNull(s, text))
					{
						if (o instanceof DisplayableObject)
						{
							// Return the associated item
							return ((DisplayableObject) o).getItem();
						}

						// Return the object itself as item
						return o;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Sets the selected item.
	 * @param selectedItem Item to select.<br>
	 * The item must have been added to the field using the {@link #addItem(Object)} method.
	 */
	public void setSelectedItem(Object selectedItem)
	{
		if (itemList != null)
		{
			// Check if the field text matches the display text of an item in the list
			int n = itemList.size();
			for (int i = 0; i < n; ++i)
			{
				Object o = itemList.get(i);

				if (o instanceof DisplayableObject)
				{
					// Return the associated item
					DisplayableObject dobj = (DisplayableObject) o;
					if (CommonUtil.equalsNull(selectedItem, dobj.getItem()))
					{
						setText(dobj.toString());
						return;
					}
				}
			}
		}

		setText(selectedItem != null ? selectedItem.toString() : null);
	}

	/**
	 * Gets the index of the selected item in the item list.
	 * @return The index or -1 if no item of the item list is currently selected
	 */
	public int getSelectedIndex()
	{
		if (itemList != null)
		{
			String text = getText();

			// Check if the field text matches the display text of an item in the list
			int n = itemList.size();
			for (int i = 0; i < n; ++i)
			{
				Object o = itemList.get(i);
				String s = o != null ? o.toString() : null;

				if (CommonUtil.equalsNull(s, text))
				{
					return i;
				}
			}
		}

		return -1;
	}

	/**
	 * Sets the index of the selected item in the item list.
	 * @param selectedIndex The index or -1 to clear the selection field
	 */
	public void setSelectedIndex(int selectedIndex)
	{
		if (selectedIndex < 0 || selectedIndex >= getNumberOfItems())
		{
			setText(null);
			return;
		}

		Object o = itemList.get(selectedIndex);
		setText(o != null ? o.toString() : null);
	}

	//////////////////////////////////////////////////
	// @@ Item list access
	//////////////////////////////////////////////////

	/**
	 * Gets the item list.
	 * @return An iterator of objects
	 */
	public Iterator getItems()
	{
		if (itemList == null)
			return EmptyIterator.getInstance();
		return itemList.iterator();
	}

	/**
	 * Gets the number of items.
	 * @return The number of items in the collection
	 */
	public int getNumberOfItems()
	{
		return itemList != null ? itemList.size() : 0;
	}

	/**
	 * Gets an item by its collection index.
	 *
	 * @param index Collection index (must be in the range [0..{@link #getNumberOfItems}]
	 * @return The item
	 */
	public String getItemTextAt(int index)
	{
		Object o = itemList.get(index);
		return o != null ? o.toString() : null;
	}

	/**
	 * Gets an item by its collection index.
	 *
	 * @param index Collection index (must be in the range [0..{@link #getNumberOfItems}]
	 * @return The item
	 */
	public Object getItemAt(int index)
	{
		return itemList.get(index);
	}

	/**
	 * Adds an item.
	 * @param item The item to add
	 */
	public void addItem(Object item)
	{
		if (item != null && item.equals(""))
			item = null;

		if (itemList == null)
			itemList = new ArrayList();
		itemList.add(item);
	}

	/**
	 * Adds an item.
	 * Constructs a helper object that holds the item and the text and adds
	 * it to the item list.
	 * @param text Text to display for the item
	 * @param item The item to add
	 */
	public void addItem(String text, Object item)
	{
		if (text != null && text.equals(""))
			text = null;
		if (item != null && item.equals(""))
			item = null;

		if (itemList == null)
			itemList = new ArrayList();
		itemList.add(new DisplayableObject(text, item));
	}

	/**
	 * Clears the item list.
	 */
	public void clearItems()
	{
		itemList = null;
	}

	/**
	 * Gets the item list.
	 * @return A list of objects
	 */
	public List getItemList()
	{
		return itemList;
	}

	/**
	 * Sets the item list.
	 * @param itemList A list of objects
	 */
	public void setItemList(List itemList)
	{
		this.itemList = itemList;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the maximum row count to display in the popup.
	 * @return The row count (default: 10)
	 */
	public int getMaximumRowCount()
	{
		return maximumRowCount;
	}

	/**
	 * Sets the maximum row count to display in the popup.
	 * @nowarn
	 */
	public void setMaximumRowCount(int maximumRowCount)
	{
		this.maximumRowCount = maximumRowCount;
	}

	//////////////////////////////////////////////////
	// @@ Helper classes
	//////////////////////////////////////////////////

	/**
	 * This class holds an arbitrary object (i\.e\. the item) and a display name that should
	 * be displayed in the selection field for this item.
	 */
	private static class DisplayableObject
	{
		/** Item held by the object */
		private Object item;

		/** Text to display for the item */
		private String text;

		/**
		 * Constructor.
		 *
		 * @param text Text to display for the item
		 * @param item Item held by the object
		 */
		public DisplayableObject(String text, Object item)
		{
			this.text = text;
			this.item = item;
		}

		/**
		 * Gets a string representation of this object.
		 *
		 * @return The text
		 */
		public String toString()
		{
			return text;
		}

		/**
		 * Gets the item held by the object.
		 * @nowarn
		 */
		public Object getItem()
		{
			return item;
		}
	}

	/**
	 * Mouse listener for the selection field.
	 */
	private class SelectionMouseListener extends MouseAdapter
	{
		/**
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e)
		{
			// Show the popup immediately on click if the field is not editable
			if (!isPopupVisible())
			{
				if (!isEditable() && isEnabled())
				{
					showPopup();
				}

				/*
				 if (! isEditable () && ! popupClosed)
				 {
				 showPopup ();
				 }
				 */
				// popupClosed = false;
			}
		}
	}
}
