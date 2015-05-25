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

/**
 * Extends the {@link MappingRegistry} in order to provide services external to the registry itself.
 * For example, this might be an EJB lookup.
 *
 * @author Heiko Erhardt
 */
public interface MappingRegistryExtender
{
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
	public Object lookup(String key);

	/**
	 * Retrieves an object by its class or interface name.
	 * This method is typically used to retrieve a concrete instance of an interface.
	 *
	 * @param cls The interface to lookup the implementation for
	 * @return The object registered for the key or null
	 */
	public Object lookup(Class cls);
}
