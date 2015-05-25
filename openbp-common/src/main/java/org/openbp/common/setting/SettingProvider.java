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

/**
 * A setting provider can retrieve and load setting values from/to persistent storage.
 * Setting values may be strings, booleans and integers.
 *
 * @seec SettingResolver
 *
 * @author Heiko Erhardt
 */
public interface SettingProvider
{
	/**
	 * Gets a setting value.
	 *
	 * @param name Name of the setting
	 * @return The setting value or null if the setting does not exist
	 */
	public Object getSetting(String name);

	/**
	 * Sets a setting.
	 *
	 * @param name Name of the setting
	 * @param value Value of the setting
	 * @return
	 *		true	If the provider was able to save the setting.<br>
	 *		false	If the provider does not feel responsible for this setting or cannot save the setting.
	 */
	public boolean setSetting(String name, Object value);

	/**
	 * Loads the settings.
	 * Forces the provider to (re-)load the settings.
	 *
	 * @return
	 *		true	If the setting were successfully loaded.
	 *		false	If the settings could not be loaded.
	 */
	public boolean loadSettings();

	/**
	 * Saves the settings.
	 * Makes the provider to save the current setting values.<br>
	 * Note that some providers may save the setting values automatically if {@link #setSetting} is called.
	 * In this case, this method will do nothing and should return true.
	 *
	 * @return
	 *		true	If the setting were successfully saved or the provider is not save-capable.
	 *		false	If there was an error saving the properties.
	 */
	public boolean saveSettings();
}
