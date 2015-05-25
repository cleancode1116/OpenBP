/*
 *   Copyright 2010 skynamics AG
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
package org.openbp.server.context;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Criteria base.
 *
 * @author Heiko Erhardt
 */
public abstract class CriteriaBase
{
	/** Custom search criteria */
	private Map<String, Object> customCriteria = new HashMap<String, Object>();

	/**
	 * Default constructor.
	 */
	public CriteriaBase()
	{
	}

	/**
	 * Adds a custom criterium to the search criteria.
	 * Use this in order to search by custom properties of subclasses of the TokenContextImpl class.
	 *
	 * @param key Name of the property
	 * @param value Property value
	 */
	public void addCustomCriteria (String key, Object value)
	{
		customCriteria.put(key, value);
	}

	/**
	 * Gets custom criteria keys.
	 *
	 * @return An iterator of custom criterium property names
	 */
	public Iterator<String> getCustomCriteriaKeys()
	{
		return customCriteria.keySet().iterator();
	}

	/**
	 * Gets the value of a custom criterium.
	 *
	 * @param key Name of the property
	 * @return The value or null
	 */
	public Object getCustomCriteriaValue(String key)
	{
		return customCriteria.get(key);
	}
}
