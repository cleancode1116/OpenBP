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
package org.openbp.server.engine.script;

import java.util.HashMap;
import java.util.Map;

/**
 * Standard implementation of an expression context.
 * This implementation is very simple implementation that contains no more than a hash map to store the objects.
 *
 * @author Heiko Erhardt
 */
public class StandardExpressionContext
	implements ExpressionContext
{
	/** Table of context objects (maps object names (Strings) to object values (Objects)) */
	private Map objectMap;

	/**
	 * Default constructor.
	 */
	public StandardExpressionContext()
	{
	}

	/**
	 * Gets an object from the context.
	 *
	 * @param name Name of the object
	 * @return Value of the object or null if no such object exists
	 */
	public Object getObject(String name)
	{
		return objectMap != null ? objectMap.get(name) : null;
	}

	/**
	 * Adds an object to the context.
	 *
	 * @param name Name of the object
	 * @param value Value of the object
	 */
	public void setObject(String name, Object value)
	{
		if (objectMap == null)
			objectMap = new HashMap();
		objectMap.put(name, value);
	}

	/**
	 * Gets the table of context objects (maps object names (Strings) to object values (Objects)).
	 * @nowarn
	 */
	public Map getObjectMap()
	{
		return objectMap;
	}

	/**
	 * Sets the table of context objects (maps object names (Strings) to object values (Objects)).
	 * @nowarn
	 */
	public void setObjectMap(Map objectMap)
	{
		this.objectMap = objectMap;
	}
}
