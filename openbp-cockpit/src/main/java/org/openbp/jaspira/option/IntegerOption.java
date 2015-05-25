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
import org.openbp.jaspira.option.widget.IntegerWidget;

/**
 * An integer option can have a minimum or maximum value and is represented as text field
 * with a spinner button.
 *
 * @author Jens Ferchland
 */
public class IntegerOption extends Option
{
	//////////////////////////////////////////////////
	// @@ memebers
	//////////////////////////////////////////////////

	/** the minimum value of this option*/
	private int min;

	/** the maximum value of this option */
	private int max;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Resource constructor.
	 *
	 * @param res Resource defining the option
	 * @param optionName Name of the option
	 * @param defaultValue Default Value of the option
	 * @param min Minimum value
	 * @param max Maximum value
	 */
	public IntegerOption(ResourceCollection res, String optionName, Integer defaultValue, int min, int max)
	{
		super(res, optionName, defaultValue);

		this.max = max;
		this.min = min;
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
	 * @param min Minimum value
	 * @param max Maximum value
	 */
	public IntegerOption(String optionName, String displayName, String description, Integer defaultValue, Option parent, int prio, int min, int max)
	{
		super(optionName, displayName, description, defaultValue, TYPE_OPTION, parent, prio);

		this.min = min;
		this.max = max;
	}

	//////////////////////////////////////////////////
	// @@ Option implementation
	//////////////////////////////////////////////////#

	/**
	 * @see org.openbp.jaspira.option.Option#createOptionWidget()
	 */
	public OptionWidget createOptionWidget()
	{
		return new IntegerWidget(this, min, max);
	}

	/**
	 * @see org.openbp.jaspira.option.Option#saveToString()
	 */
	public String saveToString()
	{
		return ((Integer) getValue()).toString();
	}

	/**
	 * @see org.openbp.jaspira.option.Option#loadFromString(String)
	 */
	public Object loadFromString(String cryptString)
	{
		return Integer.valueOf(cryptString);
	}
}
