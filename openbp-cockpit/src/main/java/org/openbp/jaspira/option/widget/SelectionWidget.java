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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.openbp.jaspira.option.LocalizableOptionString;
import org.openbp.jaspira.option.Option;
import org.openbp.jaspira.option.OptionWidget;
import org.openbp.swing.components.popupfield.JSelectionField;

/**
 * Selection field widget.
 * This widget displays a list of text selection values in a {@link JSelectionField} component.
 *
 * @author Jens Ferchland
 */
public class SelectionWidget extends OptionWidget
	implements ActionListener
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Component that will be returned */
	private JPanel panel;

	/** Selection field */
	private JSelectionField selectionField;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param option Option the widget refers to
	 * @param values Values to display as radio buttons (arbitrary objects or {@link LocalizableOptionString} objects)
	 */
	public SelectionWidget(Option option, List values)
	{
		super(option);

		selectionField = new JSelectionField();
		selectionField.setEditable(false);
		selectionField.addActionListener(this);

		for (Iterator it = values.iterator(); it.hasNext();)
		{
			Object o = it.next();

			// if the List contains a Wrapper get the correct optionvalue to compare
			// the right object together!
			if (o instanceof LocalizableOptionString)
			{
				LocalizableOptionString opt = (LocalizableOptionString) o;
				selectionField.addItem(opt.getDisplayText(), opt.getValue());
			}
			else
			{
				selectionField.addItem(o);
			}
		}

		panel = new JPanel(new BorderLayout());
		JComponent heading = createHeading();
		if (heading != null)
		{
			panel.add(heading, BorderLayout.WEST);
		}
		panel.add(selectionField);
	}

	//////////////////////////////////////////////////
	// @@ ActionListener implementation
	//////////////////////////////////////////////////

	public void actionPerformed(ActionEvent e)
	{
		// we have radiobuttons in a buttongroup so the
		// selection will changed by itself. we just need to say update!
		notifyOptionMgrOfOptionChange();
	}

	//////////////////////////////////////////////////
	// @@ OptionWidget implementation
	//////////////////////////////////////////////////

	public Object getValue()
	{
		return selectionField.getSelectedItem();
	}

	public void setValue(Object o)
	{
		selectionField.setSelectedItem(o);
	}

	public JComponent getWidgetComponent()
	{
		return panel;
	}
}
