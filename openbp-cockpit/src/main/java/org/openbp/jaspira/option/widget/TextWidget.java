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
package org.openbp.jaspira.option.widget;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;

import org.openbp.jaspira.option.Option;
import org.openbp.jaspira.option.OptionWidget;

/**
 * This is a simple OptionWidget whitch shows a TextField and
 * manages the input events.
 *
 * @author Jens Ferchland
 */
public class TextWidget extends OptionWidget
	implements DocumentListener
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Component that will be returned */
	private JPanel panel;

	/** the input field for information */
	private JTextField textField;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param option Option the widget refers to
	 * @param numberOfCharacters Number of characters (columns) to display
	 */
	public TextWidget(Option option, int numberOfCharacters)
	{
		this(option, numberOfCharacters, new DefaultStyledDocument());
	}

	/**
	 * Constructor.
	 *
	 * @param option Option the widget refers to
	 * @param numberOfCharacters Number of characters (columns) to display
	 * @param doc Text document
	 */
	public TextWidget(Option option, int numberOfCharacters, Document doc)
	{
		super(option);

		textField = new JTextField(doc, (String) option.getValue(), numberOfCharacters);
		textField.getDocument().addDocumentListener(this);

		panel = new JPanel(new BorderLayout());
		JComponent heading = createHeading();
		if (heading != null)
		{
			panel.add(heading, BorderLayout.WEST);
		}
		panel.add(textField);
	}

	//////////////////////////////////////////////////
	// @@ DocumentListener implementation
	//////////////////////////////////////////////////

	/**
	 * @see javax.swing.event.DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e)
	{
		notifyOptionMgrOfOptionChange();
	}

	/**
	 * @see javax.swing.event.DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{
		notifyOptionMgrOfOptionChange();
	}

	/**
	 * @see javax.swing.event.DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
		notifyOptionMgrOfOptionChange();
	}

	//////////////////////////////////////////////////
	// @@ OptionWidget implementation
	//////////////////////////////////////////////////

	public Object getValue()
	{
		return textField.getText();
	}

	public void setValue(Object o)
	{
		textField.setText(o != null ? o.toString() : null);
	}

	public JComponent getWidgetComponent()
	{
		return panel;
	}
}
