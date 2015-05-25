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
package org.openbp.common.application;

import org.openbp.common.setting.SettingProvider;

/**
 * Provider that resolves the read-only property 'RootDir' to the application root directory (see {@link Application#getRootDir}).
 *
 * @author Heiko Erhardt
 */
public class RootDirProvider
	implements SettingProvider
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public RootDirProvider()
	{
	}

	//////////////////////////////////////////////////
	// @@ SettingProvider implementation
	//////////////////////////////////////////////////

	/**
	 * Gets a setting value.
	 *
	 * @param name Name of the setting
	 * @return The setting value or null if the setting does not exist
	 */
	public Object getSetting(String name)
	{
		String value;
		if ("RootDir".equals(name))
			value = Application.getRootDir();
		else
			value = null;
		return value;
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
