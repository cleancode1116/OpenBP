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
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openbp.jaspira.option.Option;
import org.openbp.jaspira.option.OptionWidget;

/**
 * This is a widget that displayes a double number option.
 *
 * Use this Widget only in
 *
 * @author Jens Ferchland
 */
public class DoubleWidget extends OptionWidget
	implements ChangeListener
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Component that will be returned */
	private JPanel panel;

	/** Numberic field */
	private JSpinner spinner;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param option Option the widget refers to
	 * @param min Minimum value
	 * @param max Maximum value
	 */
	public DoubleWidget(Option option, double min, double max)
	{
		super(option);

		spinner = new JSpinner(new SpinnerNumberModel(min, min, max, 2));
		spinner.addChangeListener(this);

		panel = new JPanel(new BorderLayout());
		JComponent heading = createHeading();
		if (heading != null)
		{
			panel.add(heading, BorderLayout.WEST);
		}
		panel.add(spinner);
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
		return spinner.getValue();
	}

	public void setValue(Object o)
	{
		spinner.setValue(o);
	}

	public JComponent getWidgetComponent()
	{
		return panel;
	}
}
