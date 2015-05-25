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

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openbp.jaspira.option.Option;
import org.openbp.jaspira.option.OptionWidget;

/**
 * This Widget displays a checkbox for a single boolean option.
 *
 * @author Jens Ferchland
 */
public class CheckBoxWidget extends OptionWidget
	implements ChangeListener
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Check box */
	private JCheckBoxMenuItem checkBox;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param option Option the widget refers to
	 */
	public CheckBoxWidget(Option option)
	{
		super(option);

		String text = option.getHeading();
		if (text == null)
			text = option.getDisplayName();
		checkBox = new JCheckBoxMenuItem(text);

		checkBox.addChangeListener(this);
	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		notifyOptionMgrOfOptionChange();
	}

	//////////////////////////////////////////////////
	// @@ OptionWidget implementation
	//////////////////////////////////////////////////

	public Object getValue()
	{
		return new Boolean(checkBox.isSelected());
	}

	public void setValue(Object o)
	{
		checkBox.setSelected(((Boolean) o).booleanValue());
	}

	public JComponent getWidgetComponent()
	{
		return checkBox;
	}
}
