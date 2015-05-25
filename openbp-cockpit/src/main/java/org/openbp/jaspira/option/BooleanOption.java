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

import org.openbp.common.rc.ResourceCollection;
import org.openbp.jaspira.option.widget.CheckBoxWidget;

/**
 * A boolean option can take either a true or false value and is represented as a check box.
 *
 * @author Jens Ferchland
 */
public class BooleanOption extends Option
{
	//////////////////////////////////////////////////
	// @@ construction
	//////////////////////////////////////////////////

	/**
	 * Resource constructor.
	 *
	 * @param res Resource defining the option
	 * @param optionName Name of the option
	 * @param defaultValue Default Value of the option
	 */
	public BooleanOption(ResourceCollection res, String optionName, Boolean defaultValue)
	{
		super(res, optionName, defaultValue);
	}

	/**
	 * Value constructor.
	 *
	 * @param optionName Name of the option
	 * @param displayName Display name of the option
	 * @param description Description of the option
	 * @param defaultValue Default Value of the option
	 * @param parent Option parent or null
	 * @param prio Priority of the option
	 */
	public BooleanOption(String optionName, String displayName, String description, Boolean defaultValue, Option parent, int prio)
	{
		super(optionName, displayName, description, defaultValue, TYPE_OPTION, parent, prio);
	}

	//////////////////////////////////////////////////
	// @@ Option implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.option.Option#createOptionWidget()
	 */
	public OptionWidget createOptionWidget()
	{
		return new CheckBoxWidget(this);
	}

	/**
	 * @see org.openbp.jaspira.option.Option#saveToString()
	 */
	public String saveToString()
	{
		return ((Boolean) getValue()).booleanValue() ? "true" : "false";
	}

	/**
	 * @see org.openbp.jaspira.option.Option#loadFromString(String)
	 */
	public Object loadFromString(String cryptString)
	{
		return Boolean.valueOf(cryptString);
	}
}
