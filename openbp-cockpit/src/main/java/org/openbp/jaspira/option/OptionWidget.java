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
package org.openbp.jaspira.option;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

/**
 * A option widget is a visual representation of an option.
 * It has a visual component that allows display and editing of the option value.
 * If the user edits the option value, an option widget event will be sent.
 *
 * @author Jens Ferchland
 */
public abstract class OptionWidget
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Option this widget refers to */
	protected Option option;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param option Option this widget refers to
	 */
	public OptionWidget(Option option)
	{
		this.option = option;
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Gets the option.
	 * @nowarn
	 */
	public Option getOption()
	{
		return option;
	}

	/**
	 * Gets the description component of the option.
	 * @return The default is a JLabel holding the description of the option
	 */
	public JComponent getDescriptionComponent()
	{
		JTextPane text = new JTextPane();
		text.setContentType("text/html");
		text.setText(option.getDescription());
		text.setEditable(false);
		return text;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Notifies the option manager of an option change.
	 */
	public void notifyOptionMgrOfOptionChange()
	{
		for (Component c = getWidgetComponent(); c != null; c = c.getParent())
		{
			if (c instanceof OptionDialog)
			{
				OptionDialog dlg = (OptionDialog) c;
				dlg.notifyOptionChange(this);
			}
		}
	}

	/**
	 * Creates a label that holds the option heading, if defined.
	 *
	 * @return The new label component or null if there is no heading
	 */
	protected JLabel createHeading()
	{
		String heading = option.getHeading();
		if (heading == null)
			return null;

		JLabel label = new JLabel(heading);
		label.setBorder(new EmptyBorder(0, 0, 0, 5));
		return label;
	}

	////////////////////////////////////////////////
	// @@ Abstract methods
	////////////////////////////////////////////////

	/**
	 * Returns the value of the option widget.
	 * This value might be different from the actual option value
	 * as long as the option has not been comitted.
	 * @nowarn
	 */
	public abstract Object getValue();

	/**
	 * Sets the value that will be displayed in the option widget.
	 * @nowarn
	 */
	public abstract void setValue(Object o);

	/**
	 * Gets the widget component that visualizes the option.
	 * @nowarn
	 */
	public abstract JComponent getWidgetComponent();
}
