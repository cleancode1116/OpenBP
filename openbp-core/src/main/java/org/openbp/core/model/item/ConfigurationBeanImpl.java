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
package org.openbp.core.model.item;

/**
 * Basic implementation of a property object that can hold component-specific settings for an item or process node.
 *
 * @author Heiko Erhardt
 */
public class ConfigurationBeanImpl
	implements ConfigurationBean
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ConfigurationBeanImpl()
	{
	}

	/**
	 * Creates a clone of this object.
	 * @return The clone (a deep copy of this object)
	 * @throws CloneNotSupportedException If the cloning of one of the contained members failed
	 */
	public Object clone()
		throws CloneNotSupportedException
	{
		return super.clone();
	}

	/**
	 * Determines if the values of the members of this bean have default values.
	 * This method is used internally in order to determine if a bean needs to be saved or not.
	 * The default implementation always returns false.
	 *
	 * @return
	 *		true	All members have default values.
	 *		false	At least one member has a value different from the default value.
	 */
	public boolean hasDefaultValues()
	{
		return false;
	}
}
