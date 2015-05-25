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
package org.openbp.common.property;

/**
 * A runtime attribute container is an object that accepts key-value pairs as runtime attributes.
 * Runtime attributes are used to store arbitrary values that cannot be determined at the
 * time of the implementation of the object.<br>
 * This is usually implemented using a simple map. However, the map should be allocated only
 * if an attribute is set in order to save memory.<br>
 * They are usually not written to/read from persistent storage when the object
 * is being serialized/deserialized, thus the name runtime attributes.
 *
 * The dynamic property access mechanism implemented by the PropertyAccessUtil class supports
 * runtime attribute containers.
 *
 * @author Heiko Erhardt
 */
public interface RuntimeAttributeContainer
{
	/**
	 * Gets a runtime attribute value.
	 *
	 * @param key Name of the attribute
	 * @return The value of the attribute or null if no such attribute exists
	 */
	public Object getRuntimeAttribute(String key);

	/**
	 * Sets a runtime attribute value.
	 *
	 * @param key Name of the attribute
	 * @param value Value of the attribute
	 */
	public void setRuntimeAttribute(String key, Object value);

	/**
	 * Removes a runtime attribute.
	 *
	 * @param key Name of the attribute
	 */
	public void removeRuntimeAttribute(String key);
}
