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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openbp.common.CommonUtil;
import org.openbp.common.generic.description.DescriptionObject;
import org.openbp.common.generic.description.DescriptionObjectImpl;
import org.openbp.common.string.StringUtil;
import org.openbp.common.util.iterator.EmptyIterator;
import org.openbp.jaspira.propertybrowser.editor.AbstractPropertyEditor;
import org.openbp.jaspira.propertybrowser.editor.EditorParameterParser;
import org.openbp.swing.components.popupfield.JSelectionField;
import org.openbp.swing.components.popupfield.PopupEvent;
import org.openbp.swing.components.popupfield.PopupListener;

/**
 * A property editor for collection of strings, displayed in a combo box.
 * The combo box by default is NOT editable, unless otherwise specified in the
 * XML property file assigned to the specified object.
 *
 * @author Erich Lauterbach
 */
public class SelectionEditor extends AbstractPropertyEditor
	implements ActionListener, PopupListener, DocumentListener
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Flag to prevent property change event when initalizing the text component */
	private boolean settingValue;

	/** Editable flag */
	private boolean editable;

	/** Editor component of the combo box */
	private JTextField textField;

	/**
	 * List of combo box values (contains {@link DescriptionObject} objects).
	 * The description member (or the name member if not present) is presented to the user,
	 * the name member will be stored in the property the editor operates on.
	 */
	private List selectionValueList;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public SelectionEditor()
	{
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Parses the editor parameters specified in the property descriptor.
	 *
	 * @param parser Editor parameter parser or null if there are no parameters defined
	 */
	protected void parseParams(EditorParameterParser parser)
	{
		String editableStr = parser.getString("editable");
		if (editableStr != null && editableStr.equalsIgnoreCase("true"))
		{
			editable = true;
		}

		Iterator iter = parser.get("selection");
		while (iter.hasNext())
		{
			String param = (String) iter.next();

			String selectionName = EditorParameterParser.determineInternalValue(param);
			String selectionValue = EditorParameterParser.determineDisplayValue(param);

			addSelectionValue(selectionName, selectionValue);
		}
	}

	/**
	 * Creates the editor component of the property editor.
	 */
	public void createComponent()
	{
		JSelectionField c = new JSelectionField();

		c.setEditable(editable && ! readonly);

		textField = c.getTextField();

		// Fill the editor combo box with the list of available selections.
		if (selectionValueList != null && ! readonly)
		{
			int n = selectionValueList.size();
			for (int i = 0; i < n; ++i)
			{
				DescriptionObject d = (DescriptionObject) selectionValueList.get(i);

				String name = d.getName();
				String text = d.getDescription();
				if (text != null)
				{
					c.addItem(text, name);
				}
				else
				{
					c.addItem(name);
				}
			}
		}

		c.addFocusListener(this);
		if (! readonly)
		{
			c.addActionListener(this);
			c.addKeyListener(this);
			c.addPopupListener(this);
		}

		textField.addKeyListener(this);
		textField.getDocument().addDocumentListener(this);
		textField.addFocusListener(this);

		// Configure the component for usage by the property browser
		if (propertyBrowser != null)
			propertyBrowser.configureSubComponent(textField);

		component = c;
	}

	/**
	 * Sets the display component value.
	 */
	public void setComponentValue()
	{
		initializeComponent();

		String v = getSafeString(value);

		if (component instanceof JLabel)
		{
			String display = determineDisplayValueFor(v);
			if (display == null)
				display = "";
			((JLabel) component).setText(display);
		}
		else
		{
			settingValue = true;
			JSelectionField c = (JSelectionField) component;
			c.setSelectedItem(v);
			settingValue = false;
		}
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
		if (component == null)
			return null;

		String text = null;

		if (editable)
		{
			text = textField.getText();
			text = StringUtil.trimNull(text);
			text = determineInternalValueFor(text);
		}
		else
		{
			text = (String) ((JSelectionField) component).getSelectedItem();
		}

		return text;
	}

	/**
	 * Highlights the content of the component.
	 * @param on
	 *		true	Turns the highlight on if the component has the focus<br>
	 *		false	Turns the highlight off
	 */
	public void highlight(boolean on)
	{
		if (editable)
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
		else
		{
			JSelectionField selectionField = (JSelectionField) component;

			if (on && textField.hasFocus())
			{
				selectionField.setHighlight();
			}
			else
			{
				selectionField.resetHighlight();
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the editable flag.
	 * @nowarn
	 */
	public boolean isEditable()
	{
		return editable;
	}

	/**
	 * Sets the editable flag.
	 * @nowarn
	 */
	public void setEditable(boolean editable)
	{
		this.editable = editable;
	}

	/**
	 * Gets the list of combo box values.
	 * @return An iterator of {@link DescriptionObject} objects<br>
	 * The description member (or the name member if not present) is presented to the user,
	 * the name member will be stored in the property the editor operates on.
	 */
	public Iterator getSelectionValues()
	{
		if (selectionValueList == null)
			return EmptyIterator.getInstance();
		return selectionValueList.iterator();
	}

	/**
	 * Gets the number of selection values.
	 * @return The number of selection values in the collection
	 */
	public int getNumberOfSelectionValues()
	{
		return selectionValueList != null ? selectionValueList.size() : 0;
	}

	/**
	 * Gets a selection value by its collection index.
	 *
	 * @param index Collection index (must be in the range [0..{@link #getNumberOfSelectionValues}]
	 * @return The selection value<br>
	 * The description member (or the name member if not present) is presented to the user,
	 * the name member will be stored in the property the editor operates on.
	 */
	public DescriptionObject getSelectionValueAt(int index)
	{
		return (DescriptionObject) selectionValueList.get(index);
	}

	/**
	 * Adds a selection value.
	 * @param selectionName The name of the selection
	 * @param selectionValue The selection value to add
	 */
	public void addSelectionValue(String selectionName, String selectionValue)
	{
		addSelectionValue(new DescriptionObjectImpl(selectionName, selectionValue));
	}

	/**
	 * Adds a selection value.
	 * @param selectionName The name and value of the selection
	 */
	public void addSelectionValue(String selectionName)
	{
		addSelectionValue(new DescriptionObjectImpl(selectionName));
	}

	/**
	 * Adds a selection value.
	 * @param selectionValue The selection value to add
	 */
	public void addSelectionValue(DescriptionObject selectionValue)
	{
		if (selectionValueList == null)
			selectionValueList = new ArrayList();
		selectionValueList.add(selectionValue);
	}

	/**
	 * Clears the list of combo box values.
	 */
	public void clearSelectionValues()
	{
		selectionValueList = null;
	}

	/**
	 * Gets the list of combo box values.
	 * @return A list of {@link DescriptionObject} objects or null<br>
	 * The description member (or the name member if not present) is presented to the user,
	 * the name member will be stored in the property the editor operates on.
	 */
	public List getSelectionValueList()
	{
		return selectionValueList;
	}

	/**
	 * Sets the list of combo box values.
	 * @param selectionValueList A list of {@link DescriptionObject} objects or null<br>
	 * The description member (or the name member if not present) is presented to the user,
	 * the name member will be stored in the property the editor operates on.
	 */
	public void setSelectionValueList(List selectionValueList)
	{
		this.selectionValueList = selectionValueList;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Determines the display value for the given internal selection value.
	 *
	 * @param s The internal value of the selection
	 * @return The display value
	 */
	private String determineDisplayValueFor(String s)
	{
		if (selectionValueList != null)
		{
			int n = selectionValueList.size();
			for (int i = 0; i < n; ++i)
			{
				DescriptionObject d = (DescriptionObject) selectionValueList.get(i);

				String name = d.getName();
				if (CommonUtil.equalsNull(name, s))
				{
					return d.getDescription();
				}
			}
		}
		return s;
	}

	/**
	 * Determines the internal value for the given display selection value.
	 *
	 * @param s The display value of the selection
	 * @return The internal value
	 */
	private String determineInternalValueFor(String s)
	{
		if (selectionValueList != null)
		{
			int n = selectionValueList.size();
			for (int i = 0; i < n; ++i)
			{
				DescriptionObject d = (DescriptionObject) selectionValueList.get(i);

				String desc = d.getDescription();
				if (CommonUtil.equalsNull(desc, s))
				{
					return d.getName();
				}
			}
		}
		return s;
	}

	//////////////////////////////////////////////////
	// @@ Listener implementations
	//////////////////////////////////////////////////

	/**
	 * Action performed.
	 * @nowarn
	 */
	public void actionPerformed(ActionEvent event)
	{
		if (!settingValue)
		{
			propertyChanged();

			// When selecting a value from the list, automatically save the property
			saveProperty();
		}
	}

	/**
	 * Gives notification that there was an insert into the document.  The
	 * range given by the DocumentEvent bounds the freshly inserted region.
	 *
	 * @param e Document event
	 */
	public void insertUpdate(DocumentEvent e)
	{
		if (!settingValue)
		{
			propertyChanged();
		}
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
		{
			propertyChanged();
		}
	}

	/**
	 * Gives notification that an attribute or set of attributes changed.
	 *
	 * @param e Document event
	 */
	public void changedUpdate(DocumentEvent e)
	{
		if (!settingValue)
		{
			propertyChanged();
		}
	}

	/**
	 * Invoked when a key has been pressed.
	 */
	public void keyPressed(KeyEvent e)
	{
		int keyCode = e.getKeyCode();

		JSelectionField selectionField = (JSelectionField) component;

		// Check for navigation keys
		switch (keyCode)
		{
		case KeyEvent.VK_ESCAPE:
			if (selectionField.isPopupVisible())
			{
				selectionField.setPopupVisible(false);
				e.consume();
				return;
			}
			break;

		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_HOME:
		case KeyEvent.VK_END:
		case KeyEvent.VK_UP:
		case KeyEvent.VK_DOWN:
			JTextField tf = null;
			boolean shiftPressed = e.isShiftDown();
			Object sourceObject = e.getSource();

			if (selectionField.isPopupVisible())
			{
				// As long as the combo box popup is visible, all keys should be handled by the combo box
				return;
			}

			if (sourceObject != null && sourceObject.equals(textField))
				tf = (JTextField) sourceObject;

			int pos = tf != null ? tf.getCaretPosition() : 0;
			int selSize = tf != null ? tf.getSelectionEnd() - tf.getSelectionStart() : 0;

			// Check for navigation key
			switch (keyCode)
			{
			case KeyEvent.VK_LEFT:
				if (tf == null || (pos == 0 && !shiftPressed && selSize == 0))
				{
					forwardKeyEvent(e);
					return;
				}

				// Let the component handle the key
				return;

			case KeyEvent.VK_RIGHT:
				// Ignore movement inside the text
				if (tf == null || (pos == tf.getText().length() && !shiftPressed && selSize == 0))
				{
					forwardKeyEvent(e);
					return;
				}

				// Let the component handle the key
				return;

			case KeyEvent.VK_HOME:
				if (tf == null || (pos == 0 && !shiftPressed && selSize == 0))
				{
					forwardKeyEvent(e);
					return;
				}

				if (shiftPressed)
				{
					tf.setCaretPosition(0);
					tf.setSelectionStart(0);
					tf.setSelectionEnd(pos);
				}
				else
				{
					tf.setCaretPosition(0);
				}
				e.consume();
				return;

			case KeyEvent.VK_END:
				if (tf == null || (pos == tf.getText().length() && !shiftPressed && selSize == 0))
				{
					forwardKeyEvent(e);
					return;
				}

				int len = tf.getText().length();
				if (shiftPressed)
				{
					tf.setSelectionStart(pos);
					tf.setSelectionEnd(len);
				}
				else
				{
					tf.setCaretPosition(len);
				}
				e.consume();
				return;

			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
				if (e.isControlDown() || e.isAltDown() || e.isAltGraphDown())
				{
					// CTRL/ALT UP/DOWN pops up selection list
					return;
				}
			}
		}

		// Pass any key we do not handle ourself to the super class
		super.keyPressed(e);
	}

	/**
	 * @see org.openbp.swing.components.popupfield.PopupListener#popupStateChanged (PopupEvent)
	 */
	public void popupStateChanged(PopupEvent e)
	{
		if (listener != null)
		{
			// Prevents modification of the property when chaning the component values
			settingValue = true;
			try
			{
				((SelectionEditorListener) listener).popup(SelectionEditor.this, e.getCause());
			}
			finally
			{
				settingValue = false;
			}
		}
	}
}
