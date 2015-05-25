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
package org.openbp.core.model.item.type;

import java.util.Locale;

/**
 * Primitive type that denots a string.
 *
 * @author Heiko Erhardt
 */
public class SimpleTypeItemStringImpl extends SimpleTypeItemImpl
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public SimpleTypeItemStringImpl()
	{
	}

	//////////////////////////////////////////////////
	// @@ DataTypeItem overrides
	//////////////////////////////////////////////////

	/**
	 * Configures a data member of this type with default values.
	 * @param member Member to initialize
	 */
	public void performDefaultDataMemberConfiguration(DataMember member)
	{
		member.setLength(40);
		member.setPrecision(0);
	}

	/**
	 * @copy DataTypeItem.convertFromString
	 */
	public Object convertFromString(String strValue, DataMember member, Locale locale)
		throws ValidationException
	{
		String result = strValue;

		int strLen = 0;
		if (result != null)
		{
			// Trim the field value and make it null if empty
			result = result.trim();
			strLen = result.length();
			if (strLen == 0)
				result = null;
		}

		return result;
	}
}
