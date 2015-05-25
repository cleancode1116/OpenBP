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
package org.openbp.swing.components.wizard;

import javax.swing.event.ChangeListener;

/**
 * The data collection model stores arbitrary data
 * and notifies any change listeners when the data changes.
 * Values may be added, removed or looked up.
 *
 * @author Heiko Erhardt
 */
public interface WizardDataModel
{
	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Checks if a property value is present.
	 *
	 * @param key Name of the property
	 * @nowarn
	 */
	public boolean containsKey(Object key);

	/**
	 * Gets a property value.
	 *
	 * @param key Name of the property
	 * @return The property value
	 */
	public Object get(Object key);

	/**
	 * Gets a property value, providing a default.
	 *
	 * @param key Name of the property
	 * @param def Default value
	 * @return The property value
	 */
	public Object get(Object key, Object def);

	/**
	 * Stores a property value.
	 *
	 * @param key Name of the property
	 * @param value Property value
	 * @return The old property value
	 */
	public Object put(Object key, Object value);

	/**
	 * Removes a property.
	 *
	 * @param key Name of the property
	 * @return The old property value
	 */
	public Object remove(Object key);

	//////////////////////////////////////////////////
	// @@ Change listeners
	//////////////////////////////////////////////////

	/**
	 * Adds a change listener.
	 * The listener will be notified each time a property value changes.
	 *
	 * @param listener Listener
	 */
	public void addChangeListener(ChangeListener listener);

	/**
	 * Removes a change listener.
	 *
	 * @param listener Listener
	 */
	public void removeChangeListener(ChangeListener listener);
}
