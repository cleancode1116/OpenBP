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
package org.openbp.jaspira.propertybrowser.editor.standard;

import java.awt.Color;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openbp.common.string.StringUtil;
import org.openbp.jaspira.propertybrowser.editor.AbstractPropertyEditor;
import org.openbp.swing.components.treetable.JTreeTable;

/**
 * A property editor for string values used by the property browser. By default, components returned
 * to the property browser for both editing and displaying are both induvidual JTextFields, however,
 * should the porperty be read only, then the display component will be a JLable, and the editor
 * component will return a null.
 *
 * @author Andreas Putz
 */
public class StringEditor extends AbstractPropertyEditor
	implements DocumentListener
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/**
	 * Text field as property editor component.
	 * (same as AbstractPropertyEditor.component, used to prevent casts)
	 */
	protected JTextField textField;

	/** Flag to prevent property change event when initalizing the text component */
	private boolean settingValue;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public StringEditor()
	{
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Creates the editor component of the property editor.
	 */
	public void createComponent()
	{
		if (readonly)
		{
			textField = new JTextField();
			textField.setEditable(false);
			textField.addKeyListener(this);
			textField.addFocusListener(this);

			textField.setBackground(((JTreeTable) getPropertyBrowser()).getBackground());
			textField.setSelectionColor(Color.LIGHT_GRAY);

			component = textField;
		}
		else
		{
			textField = new JTextField();

			textField.addKeyListener(this);
			textField.addFocusListener(this);
			textField.getDocument().addDocumentListener(this);

			component = textField;
		}
	}

	/**
	 * Sets the display component value.
	 */
	public void setComponentValue()
	{
		initializeComponent();

		settingValue = true;
		if (textField != null)
		{
			textField.setText(getSafeString(value));
		}
		else
		{
			((JLabel) component).setText(getSafeString(value));
		}
		settingValue = false;
	}

	/**
	 * Gets the current editor component value.
	 *
	 * @return The current value of the component (can be null)<br>
	 * Note that this value might be different from the actual property value
	 * if the property hasn't been saved yet.
	 */
	public Object getComponentValue()
	{
		if (textField != null)
		{
			String text = textField.getText();
			return StringUtil.trimNull(text);
		}
		return null;
	}

	/**
	 * Highlights the content of the component.
	 * @param on
	 *		true	Turns the highlight on if the component has the focus<br>
	 *		false	Turns the highlight off
	 */
	public void highlight(boolean on)
	{
		if (textField != null && textField.getParent() != null && textField.isVisible())
		{
			boolean show = on && textField.hasFocus();
			if (show)
			{
				textField.setSelectionStart(0);
				int length = textField.getText().length();
				textField.setSelectionEnd(length);
			}
			else
			{
				textField.setCaretPosition(0);
				textField.setSelectionStart(0);
				textField.setSelectionEnd(0);
			}

			// Show/hide the caret
			textField.getCaret().setVisible(show);
			textField.getCaret().setSelectionVisible(show);

			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					textField.repaint();
				}
			});
		}
	}

	//////////////////////////////////////////////////
	// @@ Listener implementations
	//////////////////////////////////////////////////

	/**
	 * Gives notification that there was an insert into the document.  The
	 * range given by the DocumentEvent bounds the freshly inserted region.
	 *
	 * @param e Document event
	 */
	public void insertUpdate(DocumentEvent e)
	{
		if (!settingValue)
			propertyChanged();
	}

	/**
	 * Gives notification that a portion of the document has been
	 * removed.  The range is given in terms of what the view last
	 * saw (that is, before updating sticky positions).
	 *
	 * @param e Document event
	 */
	public void removeUpdate(DocumentEvent e)
	{
		if (!settingValue)
			propertyChanged();
	}

	/**
	 * Gives notification that an attribute or set of attributes changed.
	 *
	 * @param e Document event
	 */
	public void changedUpdate(DocumentEvent e)
	{
		if (!settingValue)
			propertyChanged();
	}

	/**
	 * Invoked when a key has been pressed.
	 */
	public void keyPressed(KeyEvent e)
	{
		int keyCode = e.getKeyCode();

		// Check for navigation keys
		switch (keyCode)
		{
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_HOME:
		case KeyEvent.VK_END:
			int pos = textField.getCaretPosition();
			boolean shiftPressed = e.isShiftDown();
			int selSize = textField.getSelectionEnd() - textField.getSelectionStart();

			switch (keyCode)
			{
			case KeyEvent.VK_LEFT:
				// Ignore movement inside the text or if selection is active
				if (pos == 0 && !shiftPressed && selSize == 0)
				{
					forwardKeyEvent(e);
					return;
				}

				// Let the component handle the key
				return;

			case KeyEvent.VK_RIGHT:
				// Ignore movement inside the text or if selection is active
				if (pos == textField.getText().length() && !shiftPressed && selSize == 0)
				{
					forwardKeyEvent(e);
					return;
				}

				// Let the component handle the key
				return;

			case KeyEvent.VK_HOME:
				if (pos == 0 && !shiftPressed && selSize == 0)
				{
					forwardKeyEvent(e);
					return;
				}

				if (shiftPressed)
				{
					textField.setCaretPosition(0);
					textField.setSelectionStart(0);
					textField.setSelectionEnd(pos);
				}
				else
				{
					textField.setCaretPosition(0);
				}
				e.consume();
				return;

			case KeyEvent.VK_END:
				int len = textField.getText().length();
				if (pos == len && !shiftPressed && selSize == 0)
				{
					forwardKeyEvent(e);
					return;
				}

				if (shiftPressed)
				{
					// textField.setCaretPosition (newPos);
					textField.setSelectionStart(pos);
					textField.setSelectionEnd(len);
				}
				else
				{
					textField.setCaretPosition(len);
				}
				e.consume();
				return;
			}
		}

		// Pass any key we do not handle ourself to the super class
		super.keyPressed(e);
	}
}
