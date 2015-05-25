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
import org.openbp.jaspira.option.widget.PasswordWidget;

/**
 * A password option is a text option that has a complex ui to enter a password.
 *
 * @author Andreas Putz
 */
public class PasswordOption extends Option
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Resource */
	private ResourceCollection resourceCollection;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Resource constructor.
	 *
	 * @param res Resource defining the option
	 * @param optionName Name of the option
	 * @param defaultValue Default Value of the option
	 */
	public PasswordOption(ResourceCollection res, String optionName, String defaultValue)
	{
		super(res, optionName, defaultValue);
		resourceCollection = res;
	}

	//////////////////////////////////////////////////
	// @@ Option implementation
	//////////////////////////////////////////////////

	/**
	 * @copy org.openbp.jaspira.option.Option.createOptionWidget
	 */
	public OptionWidget createOptionWidget()
	{
		return new PasswordWidget(this, resourceCollection);
	}

	/**
	 * @see org.openbp.jaspira.option.Option#saveToString()
	 */
	public String saveToString()
	{
		// TODO Fix 5: Encrypt password
		Object o = getValue();
		if (o != null)
		{
			String s = o.toString();
			if (!"".equals(s))
				return s;
		}
		return null;
	}

	/**
	 * @see org.openbp.jaspira.option.Option#loadFromString(String)
	 */
	public Object loadFromString(String cryptString)
	{
		// TODO Fix 5: Encrypt password
		return cryptString;
	}
}
