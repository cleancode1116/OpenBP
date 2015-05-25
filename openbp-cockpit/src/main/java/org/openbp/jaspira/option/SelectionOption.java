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

import java.util.ArrayList;
import java.util.List;

import org.openbp.common.CommonUtil;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.jaspira.option.widget.SelectionWidget;

/**
 * A selection option provides a selection of values either as selection field widget.
 *
 * @author Heiko Erhardt
 */
public class SelectionOption extends Option
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Value name list */
	private String [] valueNames;

	/** Value list */
	private Object [] values;

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
	 * @param valueNames Value resource name list
	 * @param values Value list
	 */
	public SelectionOption(ResourceCollection res, String optionName, Object defaultValue, String [] valueNames, Object [] values)
	{
		super(res, optionName, defaultValue);

		this.resourceCollection = res;
		this.valueNames = valueNames;
		this.values = values;
	}

	//////////////////////////////////////////////////
	// @@ Option implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.option.Option#createOptionWidget()
	 */
	public OptionWidget createOptionWidget()
	{
		List selectionValues = new ArrayList();

		if (values != null)
		{
			for (int i = 0; i < values.length; ++i)
			{
				String text = resourceCollection.getOptionalString(getName() + ".values." + valueNames [i], valueNames [i]);
				selectionValues.add(new LocalizableOptionString(text, values [i]));
			}
		}

		return new SelectionWidget(this, selectionValues);
	}

	/**
	 * @see org.openbp.jaspira.option.Option#saveToString()
	 */
	public String saveToString()
	{
		Object value = getValue();

		if (value == null || CommonUtil.equalsNull(value, getDefaultValue()))
		{
			// Default value, ignore
			return null;
		}

		if (values != null)
		{
			for (int i = 0; i < values.length; ++i)
			{
				if (value.equals(values [i]))
				{
					return valueNames [i];
				}
			}
		}

		return null;
	}

	/**
	 * @see org.openbp.jaspira.option.Option#loadFromString(String)
	 */
	public Object loadFromString(String s)
	{
		if (s != null && values != null)
		{
			for (int i = 0; i < values.length; ++i)
			{
				if (s.equals(valueNames [i]))
				{
					return values [i];
				}
			}
		}

		return getDefaultValue();
	}
}
