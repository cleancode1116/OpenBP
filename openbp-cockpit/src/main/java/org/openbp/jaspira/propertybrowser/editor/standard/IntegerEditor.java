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

/**
 * A property editor for integer values used by the property browser. By default, components returned
 * to the property browser for both editing and displaying are both induvidual JTextFields, however,
 * should the porperty be read only, then the display component will be a JLable, and the editor
 * component will return a null.
 *
 * @author Andreas Putz
 */
public class IntegerEditor extends StringEditor
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public IntegerEditor()
	{
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
		String s = (String) super.getComponentValue();
		if (s == null)
			return null;
		try
		{
			return Integer.valueOf(s);
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}
}
