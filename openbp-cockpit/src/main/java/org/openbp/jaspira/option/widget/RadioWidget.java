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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JRadioButton;

import org.openbp.jaspira.option.LocalizableOptionString;
import org.openbp.jaspira.option.Option;
import org.openbp.jaspira.option.OptionWidget;

/**
 * @author Jens Ferchland
 */
public class RadioWidget extends OptionWidget
	implements ActionListener
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Component that will be returned */
	private Box box;

	/** All values we can choose of (maps the radio button model to the object value) */
	private Map values;

	/** All radio buttons (maps the value represed by the radio button to the radio button itself) */
	private Map radios;

	/** Group for our radiobuttons */
	private ButtonGroup group;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param option Option the widget refers to
	 * @param values Values to display as radio buttons (arbitrary objects or {@link LocalizableOptionString} objects)
	 */
	public RadioWidget(Option option, List values)
	{
		super(option);

		this.values = new HashMap();
		this.radios = new HashMap();

		group = new ButtonGroup();
		box = Box.createVerticalBox();

		for (Iterator it = values.iterator(); it.hasNext();)
		{
			Object o = it.next();

			JRadioButton radio = new JRadioButton(o != null ? o.toString() : null);

			radio.setOpaque(false);
			radio.addActionListener(this);

			// if the List contains a Wrapper get the correct optionvalue to compare
			// the right object together!
			if (o instanceof LocalizableOptionString)
			{
				o = ((LocalizableOptionString) o).getValue();
			}

			this.values.put(radio.getModel(), o);
			this.radios.put(o, radio);

			box.add(radio);

			group.add(radio);
		}
	}

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
		return values.get(group.getSelection());
	}

	public void setValue(Object o)
	{
		((JRadioButton) radios.get(o)).setSelected(true);
	}

	public JComponent getWidgetComponent()
	{
		return box;
	}
}
