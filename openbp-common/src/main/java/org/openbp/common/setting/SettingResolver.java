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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.common.logger.LogUtil;
import org.openbp.common.util.ToStringHelper;

/**
 * The setting resolver manages a chain of setting providers.
 * The providers may retrieve or save settings.
 *
 * The setting resolver will access the registered setting providers in order to
 * resolve (i. e. retrieve or store) the setting.<br>
 * A setting provider may be e. g. a provider that store settings in a property file,
 * but also a provider that may store them in a database under the name of the current user.
 *
 * When a provider registers with the resolver, it can be assigned a priority.
 * The priority will determine the order in which the resolver will ask the providers
 * if they can resolve the setting.
 *
 * A setting is identified by its name, which can include sub setting specifications (sections),
 * which are separated by the '.' character, e. g. "section1.section2.name".<br>
 * The setting name may optionally include the name of the setting provider,
 * e. g. "user:section1.section2.name".
 *
 * Some setting providers may save the settings automatically if one of the set methods is called,
 * whereas others may cache the settings and save them to the persistent storage after the
 * {@link #saveSettings} method has been called.
 *
 * @author Heiko Erhardt
 */
public class SettingResolver
	implements Cloneable
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Provider separator */
	public static final char PROVIDER_SEPARATOR = ':';

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Provider list (contains {@link SettingProvider} objects) */
	private List providerInfoList;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public SettingResolver()
	{
		providerInfoList = new ArrayList();
	}

	/**
	 * Creates a clone of this object.
	 * Copies the list of providers, but not the providers and the ProviderInfo objects themself.
	 * @return The clone (a deep copy of this object)
	 * @throws CloneNotSupportedException Never
	 */
	public Object clone()
		throws CloneNotSupportedException
	{
		SettingResolver clone = (SettingResolver) super.clone();

		clone.providerInfoList = new ArrayList();

		for (Iterator it = this.providerInfoList.iterator(); it.hasNext();)
		{
			ProviderInfo pi = (ProviderInfo) it.next();
			clone.providerInfoList.add(pi);
		}

		return clone;
	}

	public String toString()
	{
		return ToStringHelper.toString(this, new String [] { "providerInfoList" });
	}

	//////////////////////////////////////////////////
	// @@ Basic setting access
	//////////////////////////////////////////////////

	/**
	 * Gets a setting value or the default.
	 *
	 * @param name Name of the setting
	 * @param dflt Default value of the setting
	 * @return The setting value or the default value if the setting does not exist
	 */
	public Object getSetting(String name, Object dflt)
	{
		if (name == null)
			return dflt;

		Object value = null;

		int index = name.indexOf(PROVIDER_SEPARATOR);
		if (index > 0)
		{
			String providerName = name.substring(0, index);
			name = name.substring(index + 1);

			SettingProvider provider = getProvider(providerName);
			if (provider != null)
			{
				value = provider.getSetting(name);
			}
		}
		else
		{
			int n = providerInfoList.size();
			for (int i = 0; i < n; ++i)
			{
				ProviderInfo pi = (ProviderInfo) providerInfoList.get(i);

				value = pi.getProvider().getSetting(name);
				if (value != null)
					break;
			}
		}

		if (value == null)
			value = dflt;

		return value;
	}

	/**
	 * Sets a setting.
	 *
	 * @param name Name of the setting<br>
	 * If the setting name contains a provider specification, the specified provider will be used.
	 * Otherwise, the first provider (according to their priority) that can save the setting,
	 * will be used.
	 * @param value Value of the setting
	 * @return The setting provider that saved the setting or null if no provider was able
	 * to save this setting
	 */
	public SettingProvider setSetting(String name, Object value)
	{
		if (name == null)
			return null;

		// Convert "" to null
		if (value != null && value.equals(""))
			value = null;

		int index = name.indexOf(PROVIDER_SEPARATOR);

		if (index > 0)
		{
			String providerName = name.substring(0, index);
			name = name.substring(index + 1);

			SettingProvider provider = getProvider(providerName);
			if (provider != null)
			{
				if (provider.setSetting(name, value))
				{
					return provider;
				}
			}
		}
		else
		{
			int n = providerInfoList.size();
			for (int i = 0; i < n; ++i)
			{
				ProviderInfo pi = (ProviderInfo) providerInfoList.get(i);

				if (pi.getProvider().setSetting(name, value))
				{
					return pi.getProvider();
				}
			}
		}

		return null;
	}

	/**
	 * Loads the current setting values of a setting provider.
	 *
	 * @param providerName Name of the provider to save or null for all
	 * @return
	 *		true	If the setting were successfully loaded.
	 *		false	If there was an error loading the properties of at least one provider.
	 */
	public boolean loadSettings(String providerName)
	{
		boolean ret = true;

		int n = providerInfoList.size();
		for (int i = 0; i < n; ++i)
		{
			ProviderInfo pi = (ProviderInfo) providerInfoList.get(i);

			if (providerName == null || pi.getName().equals(providerName))
			{
				if (!pi.getProvider().loadSettings())
				{
					ret = false;
				}
			}
		}

		return ret;
	}

	/**
	 * Saves the current setting values of a setting provider.
	 *
	 * @param providerName Name of the provider to save or null for all
	 * @return
	 *		true	If the setting were successfully saved or the provider is not save-capable.
	 *		false	If there was an error saving the properties of at least one provider.
	 */
	public boolean saveSettings(String providerName)
	{
		boolean ret = true;

		int n = providerInfoList.size();
		for (int i = 0; i < n; ++i)
		{
			ProviderInfo pi = (ProviderInfo) providerInfoList.get(i);

			if (providerName == null || pi.getName().equals(providerName))
			{
				if (!pi.getProvider().saveSettings())
				{
					ret = false;
				}
			}
		}

		return ret;
	}

	//////////////////////////////////////////////////
	// @@ Providers
	//////////////////////////////////////////////////

	/**
	 * Adds a provider.
	 * Also calls {@link SettingProvider#loadSettings}.
	 *
	 * @param name Name of the provider
	 * @param provider The provider to add
	 * @param priority Priority of the provider; 0 is top priority
	 */
	public void addProvider(String name, SettingProvider provider, int priority)
	{
		LogUtil.info(getClass(), "Adding setting provider {0}, priority {1}.", provider, Integer.valueOf(priority));

		if (!provider.loadSettings())
			return;

		ProviderInfo newPI = new ProviderInfo(name, provider, priority);

		int n = providerInfoList.size();
		for (int i = 0; i < n; ++i)
		{
			ProviderInfo pi = (ProviderInfo) providerInfoList.get(i);

			if (pi.getProvider() == provider)
			{
				// Don't insert twice
				return;
			}

			if (pi.getPriority() > priority)
			{
				// Insert the element at in the correct priority order
				providerInfoList.add(i, newPI);
				return;
			}
		}

		providerInfoList.add(newPI);
	}

	/**
	 * Clears the specified setting provider.
	 *
	 * @param name Name of the provider to remove
	 */
	public void clearProvider(String name)
	{
		for (Iterator it = providerInfoList.iterator(); it.hasNext();)
		{
			ProviderInfo pi = (ProviderInfo) it.next();
			if (pi.getName().equals(name))
			{
				it.remove();
				return;
			}
		}
	}

	/**
	 * Gets a provider by its name.
	 * @param name Name of the provider
	 * @return The provider or null if not found
	 */
	public SettingProvider getProvider(String name)
	{
		int n = providerInfoList.size();
		for (int i = 0; i < n; ++i)
		{
			ProviderInfo pi = (ProviderInfo) providerInfoList.get(i);

			if (pi.getName().equals(name))
			{
				return pi.getProvider();
			}
		}

		return null;
	}

	/**
	 * Gets the provider list.
	 * @nowarn
	 */
	public List getProviderInfoList()
	{
		return providerInfoList;
	}
}
