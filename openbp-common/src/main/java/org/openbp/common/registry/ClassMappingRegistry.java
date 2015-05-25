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
package org.openbp.common.registry;

import java.util.Iterator;
import java.util.Map;

import org.openbp.common.MsgFormat;
import org.openbp.common.ReflectException;
import org.openbp.common.ReflectUtil;

/**
 * Class mapping registry.
 *
 * @author Heiko Erhardt
 */
public class ClassMappingRegistry
{
	private MappingRegistry registry = new MappingRegistry();

	/**
	 * Default constructor.
	 */
	public ClassMappingRegistry()
	{
	}

	/**
	 * Gets the mapping for a given class.
	 *
	 * @param cls Class to map
	 * @return The mapped class or the argument if there is no mapping for this class
	 */
	public Class getMappedClass(Class cls)
	{
		Class ret = (Class) registry.lookup(cls);
		if (ret == null)
			ret = cls;
		return ret;
	}

	/**
	 * Maps a class to another class.
	 *
	 * @param from Class to map
	 * @param to Replacement for the class to map
	 */
	public void addShallowMapping(Class from, Class to)
	{
		registry.register(from.getName(), to);
	}

	/**
	 * Maps a class and all its super classes to another class.
	 *
	 * @param from Class to map
	 * @param to Replacement for the class to map
	 */
	public void addDeepMapping(Class from, Class to)
	{
		registry.registerByClass(from, to);
	}

	/**
	 * Maps a class to another class (class name version).
	 *
	 * @param fromName Class name to map
	 * @param toName Replacement for the class name to map
	 */
	public void addShallowMapping(String fromName, String toName)
	{
		Class to = ReflectUtil.loadClass(toName);
		if (to == null)
		{
			throw new ReflectException(MsgFormat.format("Result class $0 for class mapping not found.", toName));
		}

		registry.register(fromName, to);
	}

	/**
	 * Maps a class and all its super classes to another class (class name version).
	 *
	 * @param fromName Class name to map
	 * @param toName Replacement for the class name to map
	 */
	public void addDeepMapping(String fromName, String toName)
	{
		Class from = ReflectUtil.loadClass(fromName);
		if (from == null)
		{
			throw new ReflectException(MsgFormat.format("Mapped class $0 for class mapping not found.", fromName));
		}

		Class to = ReflectUtil.loadClass(toName);
		if (to == null)
		{
			throw new ReflectException(MsgFormat.format("Result class $0 for class mapping not found.", toName));
		}

		registry.registerByClass(from, to);
	}

	/**
	 * Sets shallow mapppings given as arguments.
	 * Primarily for Spring framework configuration support.
	 *
	 * @param map Classes to map. Must contain class names as key and values.
	 */
	public void setShallowMapppings(Map map)
	{
		for (Iterator it = map.entrySet().iterator(); it.hasNext();)
		{
			Map.Entry entry = (Map.Entry) it.next();
			addShallowMapping((String) entry.getKey(), (String) entry.getValue());
		}
	}
}
