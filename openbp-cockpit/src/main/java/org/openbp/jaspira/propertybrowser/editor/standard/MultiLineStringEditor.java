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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.string.StringUtil;
import org.openbp.jaspira.propertybrowser.editor.AbstractPropertyEditor;
import org.openbp.jaspira.propertybrowser.editor.EditorParameterParser;
import org.openbp.swing.components.treetable.JTreeTable;
import org.openbp.swing.plaf.sky.SimpleBorder;

/**
 * Editor for multi line strings.
 *
 * @author Andreas Putz
 */
public class MultiLineStringEditor extends AbstractPropertyEditor
	implements DocumentListener
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Text area */
	protected JTextArea textArea;

	/** Flag to prevent property change event when initalizing the text component */
	private boolean settingValue;

	/** Number of lines */
	private int lines = 3;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public MultiLineStringEditor()
	{
	}

	//////////////////////////////////////////////////
	// @@ AbstractPropertyEditor overrides
	//////////////////////////////////////////////////

	/**
	 * Parses the editor parameters specified in the property descriptor.
	 *
	 * @param parser Editor parameter parser or null if there are no parameters defined
	 */
	protected void parseParams(EditorParameterParser parser)
	{
		String linesStr = parser.getString("lines");
		if (linesStr != null)
		{
			try
			{
				lines = Integer.parseInt(linesStr);
			}
			catch (NumberFormatException e)
			{
				ExceptionUtil.printTrace(e);
			}
		}
	}

	/**
	 * Creates the editor component of the property editor.
	 */
	public void createComponent()
	{
		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setTabSize(4);
		textArea.setBorder(null);

		textArea.setEditable(!readonly);
		if (readonly)
		{
			textArea.setBackground(((JTreeTable) getPropertyBrowser()).getBackground());
		}

		textArea.addKeyListener(this);
		textArea.getDocument().addDocumentListener(this);
		textArea.addFocusListener(this);

		// Configure the component for usage by the property browser
		if (propertyBrowser != null)
			propertyBrowser.configureSubComponent(textArea);

		JScrollPane sp = new JScrollPane(textArea);
		sp.setBorder(SimpleBorder.getStandardBorder());

		// Special focus listener on the scroll pane, that will immidiatly delegate the focus
		// to the text area when the scroll pane receives the focus.
		sp.addFocusListener(new FocusDelegator());

		Dimension size = determineSize(textArea);
		sp.setPreferredSize(size);

		component = sp;
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
		if (textArea == null)
			return null;
		String text = textArea.getText();
		text = StringUtil.trimNull(text);
		return text;
	}

	/**
	 * Sets the display component value.
	 */
	public void setComponentValue()
	{
		initializeComponent();

		settingValue = true;
		textArea.setText(getSafeString(value));
		settingValue = false;

		resetScrollPos();
	}

	/**
	 * Highlights the content of the component.
	 * @param on
	 *		true	Turns the highlight on if the component has the focus<br>
	 *		false	Turns the highlight off
	 */
	public void highlight(boolean on)
	{
		if (component != null && component.getParent() != null)
		{
			boolean show = on && (component.hasFocus() || textArea.hasFocus());
			if (show)
			{
				int len = textArea.getText().length();
				textArea.setCaretPosition(len);
				textArea.setSelectionStart(0);
				textArea.setSelectionEnd(len);
			}
			else
			{
				textArea.setCaretPosition(0);
				textArea.setSelectionStart(0);
				textArea.setSelectionEnd(0);
			}

			// Show/hide the caret
			textArea.getCaret().setVisible(show);
			textArea.getCaret().setSelectionVisible(show);

			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					textArea.repaint();
				}
			});
		}
	}

	/**
	 * Resets the scroll position of the text area.
	 */
	private void resetScrollPos()
	{
		textArea.setCaretPosition(0);
		component.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Gets the preferred dimesion for a JTextArea with reference to the wanted line number.
	 */
	private Dimension determineSize(JTextArea textArea)
	{
		int lineHeight = textArea.getFont().getSize() + 5;

		return new Dimension(textArea.getPreferredSize().width, lines * lineHeight);
	}

	/**
	 * Checks if the text of the component is completely selected.
	 * @nowarn
	 */
	private boolean isCompletelySelected(JTextArea textArea)
	{
		int selLength = textArea.getSelectionEnd() - textArea.getSelectionStart();
		int textLength = textArea.getText().length();
		return selLength == textLength;
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
	 * Invoked when a component loses the keyboard focus.
	 * @nowarn
	 */
	public void focusLost(FocusEvent e)
	{
		resetScrollPos();

		super.focusLost(e);
	}

	/**
	 * Invoked when a key has been pressed.
	 */
	public void keyPressed(KeyEvent e)
	{
		int keyCode = e.getKeyCode();
		boolean shiftPressed = e.isShiftDown();

		// Check for navigation keys
		switch (keyCode)
		{
		case KeyEvent.VK_SPACE:
			if (shiftPressed)
			{
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
		case KeyEvent.VK_TAB:
		case KeyEvent.VK_ENTER:
			int pos = textArea.getCaretPosition();
			int selSize = textArea.getSelectionEnd() - textArea.getSelectionStart();

			switch (keyCode)
			{
			case KeyEvent.VK_LEFT:
				// Ignore movement inside the text
				if (pos == 0 && !shiftPressed && selSize == 0)
				{
					forwardKeyEvent(e);
					return;
				}

				// Let the component handle the key
				return;

			case KeyEvent.VK_RIGHT:
				// Ignore movement inside the text
				if (pos == textArea.getText().length() && !shiftPressed && selSize == 0)
				{
					forwardKeyEvent(e);
					return;
				}

				// Let the component handle the key
				return;

			case KeyEvent.VK_HOME:
				// Go to start of text by default
				if (pos == 0 && !shiftPressed && selSize == 0)
				{
					forwardKeyEvent(e);
					return;
				}
				{
					int newPos = 0;

					if (!isCompletelySelected(textArea))
					{
						try
						{
							int currentLine = textArea.getLineOfOffset(pos);
							int linePos = textArea.getLineStartOffset(currentLine);
							if (linePos != pos)
							{
								// If not at end of line, go to end of line
								newPos = linePos;
							}
						}
						catch (BadLocationException ex)
						{
							// Never happens
						}
					}

					if (shiftPressed)
					{
						textArea.setCaretPosition(newPos);
						textArea.setSelectionStart(newPos);
						textArea.setSelectionEnd(pos);
					}
					else
					{
						textArea.setCaretPosition(newPos);
					}
				}
				e.consume();
				return;

			case KeyEvent.VK_END:
				// Go to end of text by default
				int len = textArea.getText().length();
				if (pos == len && !shiftPressed && selSize == 0)
				{
					forwardKeyEvent(e);
					return;
				}
				{
					int newPos = len;

					if (!isCompletelySelected(textArea))
					{
						try
						{
							int currentLine = textArea.getLineOfOffset(pos);
							int linePos = textArea.getLineEndOffset(currentLine);
							if (linePos != pos)
							{
								// If not at end of line, go to end of line
								newPos = linePos;
							}
						}
						catch (BadLocationException ex)
						{
							// Never happens
						}
					}

					if (shiftPressed)
					{
						textArea.setCaretPosition(newPos);
						textArea.setSelectionStart(pos);
						textArea.setSelectionEnd(newPos);
					}
					else
					{
						textArea.setCaretPosition(newPos);
					}
				}
				e.consume();
				return;

			case KeyEvent.VK_UP:
				if (isCompletelySelected(textArea))
				{
					// Move one row up if the entire text is selected
					forwardKeyEvent(e);
					return;
				}

				// Let the component handle the key
				return;

			case KeyEvent.VK_DOWN:
				if (isCompletelySelected(textArea))
				{
					// Move one row down if the entire text is selected
					forwardKeyEvent(e);
					return;
				}

				// Let the component handle the key
				return;

			case KeyEvent.VK_TAB:
				if (e.isControlDown())
				{
					textArea.insert("\t", pos);
					e.consume();
				}
				else
				{
					forwardKeyEvent(e);
				}
				return;

			case KeyEvent.VK_ENTER:
				// CTRL-ENTER inserts new line
				if (e.isControlDown())
				{
					textArea.insert("\n", pos);
					e.consume();
				}
				else
				{
					forwardKeyEvent(e);
				}
				return;
			}
		}

		// Pass any key we do not handle ourself to the super class
		super.keyPressed(e);
	}

	//////////////////////////////////////////////////
	// @@ Inner classes
	//////////////////////////////////////////////////

	class FocusDelegator extends FocusAdapter
	{
		public void focusGained(FocusEvent e)
		{
			if (e.getSource() instanceof JScrollPane)
			{
				if (textArea != null)
					textArea.requestFocus();
			}
		}
	}
}
