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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openbp.common.logger.LogUtil;
import org.openbp.common.util.iterator.EmptyIterator;

/**
 * Common registry.
 * This class implements an internal registry that holds instances of core sevices.
 * It implements the service locator pattern, that - given an interface - returns the implementation of the service.
 *
 * @author Heiko Erhardt
 */
public class MappingRegistry
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Holds the map of registered objects. */
	private Map registeredObjects = new HashMap();

	/** Extender list (contains {@link MappingRegistryExtender} objects) */
	private List extenderList;


	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * The constructor.
	 */
	public MappingRegistry()
	{
		extenderList = new ArrayList();
	}

	//////////////////////////////////////////////////
	// @@ Generic object access
	//////////////////////////////////////////////////

	/**
	 * Retrieves the object that has been registered for the passed
	 * key or null if none could be found.
	 *
	 * @param key The key to lookup the object for
	 * @return The object registered for the key or null
	 */
	public Object lookup(String key)
	{
		// First, check the internal map
		Object o = registeredObjects.get(key);
		if (o == null)
		{
			// No avail, check the extenders
			for (Iterator it = getExtenders(); it.hasNext();)
			{
				MappingRegistryExtender extender = (MappingRegistryExtender) it.next();
				o = extender.lookup(key);
				if (o != null)
					break;
			}
		}

		return o;
	}

	/**
	 * Registers the passed object for the given key.
	 *
	 * @param key The key to register the object for
	 * @param object The object to be registered
	 */
	public void register(String key, Object object)
	{
		// Update the internal map.
		registeredObjects.put(key, object);
	}

	//////////////////////////////////////////////////
	// @@ Class/singleton registration
	//////////////////////////////////////////////////

	/**
	 * Retrieves an object by its class or interface name.
	 * This method is typically used to retrieve a concrete instance of an interface.
	 *
	 * @param cls The interface to lookup the implementation for
	 * @return The object registered for the key or null
	 */
	public Object lookup(Class cls)
	{
		return lookup(cls, false);
	}

	/**
	 * Retrieves a mandatory object by its class or interface name.
	 * This method is typically used to retrieve a concrete instance of an interface.
	 *
	 * @param cls The interface to lookup the implementation for
	 * @return The object registered for the key or null
	 * @throws RuntimeException If such an object could not be found
	 */
	public Object lookupMandatory(Class cls)
	{
		return lookup(cls, true);
	}

	/**
	 * Retrieves an object by its class or interface name.
	 * This method is typically used to retrieve a concrete instance of an interface.
	 *
	 * @param cls The interface to lookup the implementation for
	 * @param mandatory Specifies if an exception should be raised in case of an error
	 * @return The object registered for the key or null
	 * @throws RuntimeException If such an object could not be found and the mandatory flag has been set. Also logs the exception as error.
	 */
	private Object lookup(Class cls, boolean mandatory)
	{
		// First, check the internal map
		Object o = registeredObjects.get(cls.getName());
		if (o == null)
		{
			// No avail, check the extenders
			for (Iterator it = getExtenders(); it.hasNext();)
			{
				MappingRegistryExtender extender = (MappingRegistryExtender) it.next();
				o = extender.lookup(cls);
				if (o != null)
					return o;
			}

			if (mandatory)
			{
				String msg = LogUtil.error(MappingRegistry.class, "No mapping of type $0 could be found in the mapping registry.", new Object [] { cls.getName() });
				throw new RuntimeException(msg);
			}
		}
	
		return o;
	}

	/**
	 * Registers the passed object by its class names (see below).
	 *
	 * @param object The object to be registered
	 */
	public void register(Object object)
	{
		registerByClass(object.getClass(), object);
	}

	/**
	 * Registers the passed object by its class names and (recursively) by the names of it super classes and implemented interfaces.
	 *
	 * @param cls The class to use as key
	 * @param object The object to be registered
	 */
	public void registerByClass(Class cls, Object object)
	{
		if (lookup(cls) != null)
		{
			// Already registered
			return;
		}

		// Register the object under the class name
		register(cls.getName(), object);

		// Get super class descriptors
		Class superClass = cls.getSuperclass();
		if (superClass != null && !superClass.getName().equals("java.lang.Object"))
		{
			registerByClass(superClass, object);
		}

		// Get interface descriptors
		Class [] interfaces = cls.getInterfaces();
		for (int i = 0; i < interfaces.length; ++i)
		{
			registerByClass(interfaces[i], object);
		}
	}

	/**
	 * Unregisters the passed object by its class names (see below).
	 *
	 * @param object The object to be registered
	 */
	public void unregister(Object object)
	{
		// Remove all association to this object
		for (Iterator it = registeredObjects.values().iterator(); it.hasNext();)
		{
			Object o = it.next();
			if (o == object)
			{
				it.remove();
			}
		}
	}

	/**
	 * Unregisters all objects.
	 */
	public void unregisterAll()
	{
		registeredObjects.clear();
	}

	//////////////////////////////////////////////////
	// @@ Extenders
	//////////////////////////////////////////////////

	/**
	 * Gets the extender list.
	 * @return An iterator of {@link MappingRegistryExtender} objects
	 */
	public Iterator getExtenders()
	{
		if (extenderList == null)
			return EmptyIterator.getInstance();
		return extenderList.iterator();
	}

	/**
	 * Adds an extender.
	 * @param extender The extender to add
	 */
	public void addExtender(MappingRegistryExtender extender)
	{
		if (extenderList == null)
			extenderList = new ArrayList();
		extenderList.add(extender);
	}
}
