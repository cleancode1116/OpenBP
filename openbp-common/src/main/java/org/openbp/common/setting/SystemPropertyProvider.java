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
package org.openbp.common.setting;

import org.openbp.common.util.ToStringHelper;

/**
 * Implementation of a setting provider that supports access to System properties.
 * The returned setting values are always strings.
 * This class is a singleton.
 *
 * @seec SettingResolver
 *
 * @author Heiko Erhardt
 */
public final class SystemPropertyProvider
	implements SettingProvider
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Singleton instance */
	private static SystemPropertyProvider singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized SystemPropertyProvider getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new SystemPropertyProvider();
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private SystemPropertyProvider()
	{
	}

	public String toString()
	{
		return ToStringHelper.toString(this);
	}

	//////////////////////////////////////////////////
	// @@ Setting access
	//////////////////////////////////////////////////

	/**
	 * Gets a setting value.
	 *
	 * @param name Name of the setting
	 * @return The setting value or null if the setting does not exist
	 */
	public Object getSetting(String name)
	{
		return System.getProperty(name, null);
	}

	/**
	 * Sets a setting.
	 * System properties cannot be saved, so the method returns always false.
	 *
	 * @param name Name of the setting
	 * @param value Value of the setting
	 * @return false
	 */
	public boolean setSetting(String name, Object value)
	{
		return false;
	}

	/**
	 * Loads the settings.
	 * Does nothing.
	 *
	 * @return true
	 */
	public boolean loadSettings()
	{
		return true;
	}

	/**
	 * Saves the settings.
	 * Does nothing.
	 *
	 * @return true
	 */
	public boolean saveSettings()
	{
		return true;
	}
}
