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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openbp.jaspira.option.LocalizableOptionString;
import org.openbp.jaspira.option.Option;
import org.openbp.jaspira.option.OptionWidget;

/**
 * @author Andreas Putz
 */
public class MultiCheckBoxWidget extends OptionWidget
	implements ChangeListener
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Component that will be returned */
	private Box box;

	/** All check boxes (maps the value represented by the check box to the check box itself) */
	private Map checkBoxes;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param option Option the widget refers to
	 * @param values Values to display in the list (arbitrary objects or {@link LocalizableOptionString} objects)
	 */
	public MultiCheckBoxWidget(Option option, List values)
	{
		super(option);

		this.checkBoxes = new HashMap();

		box = Box.createVerticalBox();

		for (Iterator it = values.iterator(); it.hasNext();)
		{
			Object o = it.next();

			JCheckBox checkBox = new JCheckBox(o != null ? o.toString() : null);

			checkBox.setOpaque(false);
			checkBox.addChangeListener(this);

			if (o instanceof LocalizableOptionString)
			{
				o = ((LocalizableOptionString) o).getValue();
			}

			this.checkBoxes.put(o, checkBox);

			box.add(checkBox);
		}
	}

	/**
	 * see javax.swing.event.ChangeListener#stateChanged(ChangeEvent)
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
		List checked = new ArrayList();
		Iterator it = checkBoxes.keySet().iterator();
		while (it.hasNext())
		{
			Integer key = (Integer) it.next();
			if (((JCheckBox) checkBoxes.get(key)).isSelected())
			{
				checked.add(key);
			}
		}
		return checked;
	}

	public void setValue(Object o)
	{
		List list = null;
		if (o instanceof List)
		{
			list = (List) o;
			if (list.isEmpty())
				list = null;
		}

		Iterator it = checkBoxes.keySet().iterator();
		while (it.hasNext())
		{
			Integer key = (Integer) it.next();
			if (list == null)
			{
				((JCheckBox) checkBoxes.get(key)).setSelected(false);
			}
			else
			{
				JCheckBox cb = (JCheckBox) checkBoxes.get(key);
				cb.setSelected(list.indexOf(key) != -1);
			}
		}
	}

	public JComponent getWidgetComponent()
	{
		return box;
	}
}
