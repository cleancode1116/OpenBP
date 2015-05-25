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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicArrowButton;

import org.openbp.common.listener.AWTListenerSupport;
import org.openbp.swing.plaf.sky.SimpleBorder;
import org.openbp.swing.plaf.sky.SkyTheme;

/**
 * Text field component that displays a small arrow button to the right that is
 * used to activate a popup menu/dialog.
 * This component is used as base class for selection fields, file path widgets etc.
 *
 * @author Heiko Erhardt
 */
public abstract class JPopupField extends JPanel
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Label */
	protected JLabel label;

	/** Arrow button */
	protected JButton arrowButton;

	/** Text field */
	protected JTextField textField;

	/** Listeners */
	private AWTListenerSupport listenerSupport;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public JPopupField()
	{
		super(new BorderLayout());
		setOpaque(true);

		setFocusable(false);

		// Create the components
		textField = createTextField();
		arrowButton = createArrowButton();

		// 2 pixels distance to the button
		textField.setBorder(new CompoundBorder(new EmptyBorder(0, 0, 0, 2), textField.getBorder()));

		// Text field to the left
		add(textField, BorderLayout.CENTER);

		// Button to the right
		add(arrowButton, BorderLayout.EAST);

		installAncestorListener();
	}

	/**
	 * Creates the text field.
	 * @nowarn
	 */
	protected JTextField createTextField()
	{
		JTextField tf = new JTextField();

		PopupFieldKeyListener keyListener = new PopupFieldKeyListener();
		addKeyListener(keyListener);
		tf.addKeyListener(keyListener);

		tf.addFocusListener(new PopupFieldFocusListener());

		return tf;
	}

	/**
	 * Creates the arrow button.
	 * @nowarn
	 */
	protected JButton createArrowButton()
	{
		JButton btn = new BasicArrowButton(SwingConstants.SOUTH);
		btn.setBorder(new SimpleBorder(0, 2, 0, 2));

		btn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (isEnabled())
				{
					showPopup();
				}
			}
		});
		btn.setEnabled(true);

		// We do not want the button to be focusable, it can be clicked only
		btn.setFocusable(false);
		btn.setRequestFocusEnabled(false);

		// Make the button the same size as the text field
		Dimension size = new Dimension(btn.getMinimumSize());
		int h = textField.getHeight();
		size.height = h;
		btn.setMinimumSize(size);
		btn.setMaximumSize(size);
		btn.setPreferredSize(size);

		return btn;
	}

	/**
	 * Installs an ancestor listener that hides the popup on any change.
	 */
	protected void installAncestorListener()
	{
		addAncestorListener(new AncestorListener()
		{
			public void ancestorAdded(AncestorEvent event)
			{
				hidePopup();
			}

			public void ancestorRemoved(AncestorEvent event)
			{
				hidePopup();
			}

			public void ancestorMoved(AncestorEvent event)
			{
				if (event.getSource() != JPopupField.this)
					hidePopup();
			}
		});
	}

	public void setHighlight()
	{
		if (!isEditable())
		{
			textField.setBackground(SkyTheme.COLOR_HIGHLIGHT);
		}
		else
		{
			textField.setSelectionStart(0);
			int length = textField.getText().length();
			textField.setSelectionEnd(length);
		}
	}

	public void resetHighlight()
	{
		if (!isEditable())
		{
			textField.setBackground(UIManager.getColor("TextField.background"));
		}
		else
		{
			textField.setCaretPosition(0);
			textField.setSelectionStart(0);
			textField.setSelectionEnd(0);
		}
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
		textField.requestFocus();
	}

	/**
	 * Gets the enabled state.
	 * @nowarn
	 */
	public boolean isEnabled()
	{
		return textField.isEnabled();
	}

	/**
	 * Sets the enabled state.
	 * @nowarn
	 */
	public void setEnabled(boolean enabled)
	{
		textField.setEnabled(enabled);
		arrowButton.setEnabled(enabled);
	}

	/**
	 * Selects a field value using a key character.
	 * By default, this method does nothing and returns false.
	 *
	 * @param selectionChar Selection character
	 * @return
	 *		true	An item was selected.<br>
	 *		false	No item was found that begins with the specified character.
	 */
	protected boolean selectWithKeyChar(char selectionChar)
	{
		return false;
	}

	//////////////////////////////////////////////////
	// @@ Popup operations
	//////////////////////////////////////////////////

	/**
	 * Shows the popup.
	 */
	public void showPopup()
	{
		setPopupVisible(true);
	}

	/**
	 * Hides the popup.
	 */
	public void hidePopup()
	{
		setPopupVisible(false);
	}

	/**
	 * Gets the popup visibility.
	 * @return
	 *		true	The popup is shown.<br>
	 *		false	The popup is hidden.
	 */
	public abstract boolean isPopupVisible();

	/**
	 * Sets the popup visibility.
	 * @param popupVisible
	 *		true	Shows the popup.<br>
	 *		false	Hides the popup.
	 */
	public abstract void setPopupVisible(boolean popupVisible);

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the text displayed in the popup field.
	 * @return The text or null if the field is empty
	 */
	public String getText()
	{
		String text = textField.getText();
		return text.length() != 0 ? text : null;
	}

	/**
	 * Sets the text displayed in the popup field.
	 * @nowarn
	 */
	public void setText(String text)
	{
		textField.setText(text);
	}

	/**
	 * Gets the text field.
	 * @nowarn
	 */
	public JTextField getTextField()
	{
		return textField;
	}

	/**
	 * Gets the label text of in the popup field.
	 * @nowarn
	 */
	public String getLabelText()
	{
		return label != null ? label.getText() : null;
	}

	/**
	 * Sets the label text of in the popup field.
	 * @nowarn
	 */
	public void setLabelText(String labelText)
	{
		if (label != null)
		{
			remove(label);
			label = null;
		}

		if (labelText != null)
		{
			label = new JLabel(labelText);
			label.setBorder(new EmptyBorder(0, 0, 0, 5));
			add(label, BorderLayout.WEST);
		}
	}

	/**
	 * Gets the editable flag.
	 * @nowarn
	 */
	public boolean isEditable()
	{
		return textField.isEditable();
	}

	/**
	 * Sets the editable flag.
	 * @nowarn
	 */
	public void setEditable(boolean editable)
	{
		textField.setEditable(editable);
	}

	//////////////////////////////////////////////////
	// @@ Listener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'action performed' message to all registered action listeners
	 * if an item has been selected from the popup menu or using keyboard selection.
	 * The action command will be "selected" in this case.
	 */
	public void fireActionPerformed()
	{
		if (listenerSupport != null && listenerSupport.containsListeners(ActionListener.class))
		{
			listenerSupport.fireActionPerformed(new ActionEvent(this, 0, "selected"));
		}
	}

	/**
	 * Adds an action listener to the listener list.
	 * An ActionEvent will get fired in response to choosing an item from the popup.
	 *
	 * @param listener The listener to be added
	 */
	public synchronized void addActionListener(ActionListener listener)
	{
		if (listenerSupport == null)
		{
			listenerSupport = new AWTListenerSupport();
		}
		listenerSupport.addListener(ActionListener.class, listener);
	}

	/**
	 * Removes an action listener from the listener list.
	 *
	 * @param listener The listener to be removed
	 */
	public synchronized void removeActionListener(ActionListener listener)
	{
		if (listenerSupport != null)
		{
			listenerSupport.removeListener(ActionListener.class, listener);
		}
	}

	/**
	 * Fires a 'popup' message to all registered popup listeners
	 * if an item has been selected from the popup menu or using keyboard selection.
	 *
	 * @param cause The cause for the popup event (see the {@link PopupEvent} class)
	 */
	public void firePopup(int cause)
	{
		if (listenerSupport != null)
		{
			PopupEvent e = null;

			for (Iterator it = listenerSupport.getListenerIterator(PopupListener.class); it.hasNext();)
			{
				if (e == null)
					e = new PopupEvent(this, cause);
				((PopupListener) it.next()).popupStateChanged(e);
			}
		}
	}

	/**
	 * Adds a popup listener to the listener list.
	 * An PopupEvent will get fired in response to choosing an item from the popup.
	 *
	 * @param listener The listener to be added
	 */
	public synchronized void addPopupListener(PopupListener listener)
	{
		if (listenerSupport == null)
		{
			listenerSupport = new AWTListenerSupport();
		}
		listenerSupport.addListener(PopupListener.class, listener);
	}

	/**
	 * Removes a popup listener from the listener list.
	 *
	 * @param listener The listener to be removed
	 */
	public synchronized void removePopupListener(PopupListener listener)
	{
		if (listenerSupport != null)
		{
			listenerSupport.removeListener(PopupListener.class, listener);
		}
	}

	//////////////////////////////////////////////////
	// @@ Helper classes
	//////////////////////////////////////////////////

	/**
	 * Key listener for the popup field.
	 */
	private class PopupFieldKeyListener extends KeyAdapter
	{
		/**
		 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
		 */
		public void keyPressed(KeyEvent e)
		{
			int keyCode = e.getKeyCode();

			switch (keyCode)
			{
			case KeyEvent.VK_SPACE:
				// SPACE shows the popup if the field is not editable (in editable fields, we might want to input a space)
				if (!isEditable() && !isPopupVisible())
				{
					e.consume();
					showPopup();
					return;
				}
				break;

			case KeyEvent.VK_ENTER:
			case KeyEvent.VK_DOWN:
				if (!isPopupVisible())
				{
					if (e.isControlDown() || e.isAltDown() || e.isAltGraphDown())
					{
						// CTRL/ALT ENTER/DOWN shows item browser dialog
						e.consume();
						showPopup();
						return;
					}

					// Simply ENTER triggers the action
					if (keyCode == KeyEvent.VK_ENTER)
					{
						fireActionPerformed();
					}
				}
				break;

			case KeyEvent.VK_TAB:
				hidePopup();
				break;

			default:
				// Perform first-character selection only in non-edit fields
				if (!isEditable())
				{
					char c = e.getKeyChar();
					if (selectWithKeyChar(c))
					{
						e.consume();
						return;
					}
				}
				break;
			}
		}
	}

	/**
	 * Focus listener for the popup field.
	 */
	private class PopupFieldFocusListener
		implements FocusListener
	{
		/**
		 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
		 */
		public void focusGained(FocusEvent e)
		{
			setHighlight();
		}

		/**
		 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
		 */
		public void focusLost(FocusEvent e)
		{
			resetHighlight();
		}
	}
}
