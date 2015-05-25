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

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openbp.common.CommonUtil;
import org.openbp.jaspira.propertybrowser.editor.AbstractPropertyEditor;

/**
 * Property editor for boolean values used by the property browser.
 *
 * @author Erich Lauterbach
 */
public class BooleanEditor extends AbstractPropertyEditor
	implements ChangeListener
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public BooleanEditor()
	{
	}

	//////////////////////////////////////////////////
	// @@ AbstractPropertyEditor overrides
	//////////////////////////////////////////////////

	/**
	 * Creates the editor component of the property editor.
	 */
	public void createComponent()
	{
		JCheckBox c = new JCheckBox();

		c.setEnabled(!readonly);
		c.addKeyListener(this);
		c.addChangeListener(this);
		c.addFocusListener(this);

		component = c;
	}

	/**
	 * Sets the display component value.
	 */
	public void setComponentValue()
	{
		initializeComponent();

		boolean selected = value != null ? ((Boolean) value).booleanValue() : false;
		((JCheckBox) component).setSelected(selected);
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
		return new Boolean(((JCheckBox) component).isSelected());
	}

	//////////////////////////////////////////////////
	// @@ Listener implementations
	//////////////////////////////////////////////////

	/**
	 * Invoked when the target of the listener has changed its state.
	 *
	 * @param e  a ChangeEvent object
	 */
	public void stateChanged(ChangeEvent e)
	{
		Object newValue = getComponentValue();
		if (!CommonUtil.equalsNull(value, newValue))
		{
			propertyChanged();
			focusLost(null);
			focusGained(null);
		}
	}
}
