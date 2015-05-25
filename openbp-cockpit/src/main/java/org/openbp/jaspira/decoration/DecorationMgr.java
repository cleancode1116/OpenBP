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
package org.openbp.jaspira.decoration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.plugin.Plugin;

/**
 * The decoration manager provides static methods that manage decorators.
 * All decorators that are active in the system should run this this decoration manager.
 * The decorationManager is an integral part of the JaspiraFramework
 *
 * @author Stephan Moritz
 */
public class DecorationMgr
{
	/** Map containing the decorators for a given key as a key (String) - List (containing {@link Decorator} objects) pair */
	private static Map decorators = new HashMap();

	/////////////////////////////////////////////////////////////////////////
	// @@ No Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	private DecorationMgr()
	{
		super();
	}

	/**
	 * Decorates an object.
	 *
	 * @param owner Owner of the object to decorate.
	 * The decorator may use this to decide if the object should be dorated.
	 * This parameter is passed to the {@link Decorator#decorate} method.
	 * @param key Key under which the decorator is accessed
	 * @param value Object to decorate (also passed to the {@link Decorator#decorate} method)
	 * @return The decorated object (or the original object if no decorator was active)
	 */
	public static Object decorate(Object owner, String key, Object value)
	{
		synchronized (decorators)
		{
			List list = (List) decorators.get(key);

			if (list != null)
			{
				for (Iterator it = list.iterator(); it.hasNext();)
				{
					value = ((Decorator) it.next()).decorate(owner, key, value);
				}
			}
		}

		return value;
	}

	/**
	 * Adds a decorator to the list of decorators.
	 *
	 * @param provider The plugin who installed this decorator or null.
	 * If provided, the global.decoration.added event will be fired.
	 * @param key Key under which the decorator is accessed
	 * @param decorator The decorator itself
	 */
	public static void addDecorator(Plugin provider, String key, Decorator decorator)
	{
		synchronized (decorators)
		{
			List list = (List) decorators.get(key);
			if (list == null)
			{
				list = new LinkedList();
				decorators.put(key, list);
			}
			else
			{
				if (list.contains(decorator))
				{
					// Don't add it twice
					return;
				}
			}

			list.add(decorator);
		}

		if (provider != null)
		{
			provider.fireEvent(new JaspiraEvent(provider, "global.decoration.added", key));
		}
	}

	/**
	 * Removes a decorator from the list of decorators.
	 *
	 * @param provider The plugin who installed this decorator or null.
	 * If provided, the global.decoration.removed event will be fired.
	 * @param key Key under which the decorator has been added
	 * @param decorator The decorator itself
	 */
	public static void removeDecorator(Plugin provider, String key, Decorator decorator)
	{
		synchronized (decorators)
		{
			List list = (List) decorators.get(key);
			if (list == null)
				return;

			list.remove(decorator);
			if (list.isEmpty())
			{
				decorators.remove(key);
			}
		}

		if (provider != null)
		{
			provider.fireEvent(new JaspiraEvent(provider, "global.decoration.removed", key));
		}
	}
}
