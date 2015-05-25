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
 * Element of the setting provider list.
 *
 * @author Heiko Erhardt
 */
public class ProviderInfo
{
	/** Name of the provider */
	private String name;

	/** Setting provider */
	private SettingProvider provider;

	/** Priority */
	private int priority;

	/**
	 * Constructor.
	 *
	 * @param name Name of the provider
	 * @param provider Setting provider
	 * @param priority Priority
	 */
	public ProviderInfo(String name, SettingProvider provider, int priority)
	{
		this.name = name;
		this.provider = provider;
		this.priority = priority;
	}

	public String toString()
	{
		return ToStringHelper.toString(this, new String [] { "name" });
	}

	/**
	 * Gets the name of the provider.
	 * @nowarn
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name of the provider.
	 * @nowarn
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the setting provider.
	 * @nowarn
	 */
	public SettingProvider getProvider()
	{
		return provider;
	}

	/**
	 * Sets the setting provider.
	 * @nowarn
	 */
	public void setProvider(SettingProvider provider)
	{
		this.provider = provider;
	}

	/**
	 * Gets the priority.
	 * @nowarn
	 */
	public int getPriority()
	{
		return priority;
	}

	/**
	 * Sets the priority.
	 * @nowarn
	 */
	public void setPriority(int priority)
	{
		this.priority = priority;
	}
}
