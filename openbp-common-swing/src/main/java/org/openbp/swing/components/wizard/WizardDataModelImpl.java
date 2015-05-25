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

import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Property data model.
 * Actually a property map with change listeners.
 *
 * @author Heiko Erhardt
 */
public class WizardDataModelImpl extends Properties
	implements WizardDataModel
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Change listeners */
	private List listeners;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public WizardDataModelImpl()
	{
		super();
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets a property value, providing a default.
	 *
	 * @param key Name of the property
	 * @param def Default value
	 * @return The property value
	 */
	public Object get(Object key, Object def)
	{
		if (containsKey(key))
			return super.get(key);
		return def;
	}

	/**
	 * Stores a property value.
	 *
	 * @param key Name of the property
	 * @param value Property value
	 * @return The old property value
	 */
	public Object put(Object key, Object value)
	{
		Object oldValue = super.put(key, value);
		fireChangeEvent();
		return oldValue;
	}

	/**
	 * Removes a property.
	 *
	 * @param key Name of the property
	 * @return The old property value
	 */
	public Object remove(Object key)
	{
		Object oldValue = super.remove(key);
		fireChangeEvent();
		return oldValue;
	}

	//////////////////////////////////////////////////
	// @@ Change listeners
	//////////////////////////////////////////////////

	/**
	 * Adds a change listener.
	 * The listener will be notified each time a property value changes.
	 *
	 * @param listener Listener
	 */
	public void addChangeListener(ChangeListener listener)
	{
		if (listeners == null)
			listeners = new Vector();
		listeners.add(listener);
	}

	/**
	 * Removes a change listener.
	 *
	 * @param listener Listener
	 */
	public void removeChangeListener(ChangeListener listener)
	{
		listeners.remove(listener);
		if (listeners.isEmpty())
			listeners = null;
	}

	/**
	 * Fires a property change event.
	 */
	protected void fireChangeEvent()
	{
		if (listeners != null)
		{
			ChangeEvent event = new ChangeEvent(this);
			for (int i = 0; i < listeners.size(); i++)
			{
				ChangeListener listener = (ChangeListener) listeners.get(i);
				listener.stateChanged(event);
			}
		}
	}
}
