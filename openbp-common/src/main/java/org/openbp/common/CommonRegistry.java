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
package org.openbp.common;

import org.openbp.common.registry.MappingRegistry;

/**
 * General purpose mapping registry.
 * This class implements an internal registry that holds instances of core services.
 * It implements the service locator pattern, that - given an interface - returns the implementation of the service.
 *
 * @author Heiko Erhardt
 */
public final class CommonRegistry
{
	/** Mapping registry */
	private static MappingRegistry mappingRegistry = new MappingRegistry();

	/**
	 * The constructor.
	 */
	private CommonRegistry()
	{
	}

	/**
	 * Retrieves the object that has been registered for the passed
	 * key or null if none could be found.
	 *
	 * @param key The key to lookup the object for
	 * @return The object registered for the key or null
	 */
	public static Object lookup(String key)
	{
		return mappingRegistry.lookup(key);
	}

	/**
	 * Registers the passed object for the given key.
	 *
	 * @param key The key to register the object for
	 * @param object The object to be registered
	 */
	public static void register(String key, Object object)
	{
		mappingRegistry.register(key, object);
	}

	/**
	 * Retrieves an object by its class or interface name.
	 * This method is typically used to retrieve a concrete instance of an interface.
	 *
	 * @param cls The interface to lookup the implementation for
	 * @return The object registered for the key or null
	 */
	public static Object lookup(Class cls)
	{
		return mappingRegistry.lookup(cls);
	}

	/**
	 * Retrieves a mandatory object by its class or interface name.
	 * This method is typically used to retrieve a concrete instance of an interface.
	 *
	 * @param cls The interface to lookup the implementation for
	 * @return The object registered for the key or null
	 * @throws RuntimeException If such an object could not be found
	 */
	public static Object lookupMandatory(Class cls)
	{
		return mappingRegistry.lookupMandatory(cls);
	}

	/**
	 * Registers the passed object by its class names (see below).
	 *
	 * @param object The object to be registered
	 */
	public static void register(Object object)
	{
		mappingRegistry.register(object);
	}

	/**
	 * Registers the passed object by its class names and (recursively) by the names of it super classes and implemented interfaces.
	 *
	 * @param cls The class to use as key
	 * @param object The object to be registered
	 */
	public static void registerByClass(Class cls, Object object)
	{
		mappingRegistry.registerByClass(cls, object);
	}

	/**
	 * Unregisters the passed object by its class names (see below).
	 *
	 * @param object The object to be registered
	 */
	public static void unregister(Object object)
	{
		mappingRegistry.unregister(object);
	}

	/**
	 * Unregisters all objects.
	 */
	public void unregisterAll()
	{
		mappingRegistry.unregisterAll();
	}

	/**
	 * Gets the underlying mapping registry.
	 * @nowarn
	 */
	public static MappingRegistry getMappingRegistry()
	{
		return mappingRegistry;
	}
}
