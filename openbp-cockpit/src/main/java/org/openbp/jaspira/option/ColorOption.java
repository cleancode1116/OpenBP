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

import java.awt.Color;

import org.openbp.common.rc.ResourceCollection;
import org.openbp.jaspira.option.widget.ColorWidget;

/**
 * A color option supports the selection of a color.
 * Its value is a java.awt.Color object.
 *
 * @author Baumgartner Michael
 */
public class ColorOption extends Option
{
	/**
	 * Constructor.
	 * @param res The resource for the option
	 * @param optionName The name
	 * @param defaultValue The default value
	 */
	public ColorOption(ResourceCollection res, String optionName, Object defaultValue)
	{
		super(res, optionName, defaultValue);
	}

	/**
	 * @copy Option.createOptionWidget
	 */
	public OptionWidget createOptionWidget()
	{
		return new ColorWidget(this);
	}

	/**
	 * @copy Option.saveToString
	 */
	public String saveToString()
	{
		Object o = getValue();
		if (o != null)
		{
			Color color = (Color) o;
			return Integer.toString(color.getRGB());
		}
		return null;
	}

	/**
	 * @copy Option.loadFromString
	 */
	public Object loadFromString(String cryptString)
	{
		int color = Integer.parseInt(cryptString);
		return new Color(color);
	}
}
