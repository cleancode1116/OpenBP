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
package org.openbp.common.resource;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * BaseClass for various types of ResourceProviders:
 * E.g.: 
 * - FileResourceProvider 
 * - ClassPathResourceProvider
 * - HttpResourceProvider
 * - DatabaseResourceProvider
 * Multiple ResourceProviders are used by a ResourceResolver to resolve
 * a resource name and load a resource requested by the ResourceManager.
 * 
 * @author prachtp
 */
public abstract class ResourceProvider
{
	public static final String PROTOCOL_PREFIX_SEPARATOR = ":";

	/** Resource loader used by this provider */
	protected ResourceLoader resourceLoader;

	/** Priority of this provider; small numbers will be tried before larger numbers */
	private int priority;

	/**
	 * Constructor.
	 *
	 * @param priority Priority of the provider
	 * @param resourceLoader Resource loader
	 */
	protected ResourceProvider(int priority, ResourceLoader resourceLoader)
	{
		this.priority = priority;
		this.resourceLoader = resourceLoader;
	}

	/**
	 * Gets a resource.
	 *
	 * @param resourceLocation Resource location
	 * @return The resource
	 * @throws ResourceMgrException If the provider is unable to find the specified resource
	 */
	public abstract Resource getResource(String resourceLocation)
		throws ResourceMgrException;

	/**
	 * Protocol prefix of this provider
	 * @return E.g. "file:", "classpath:", "http:"; default is ""
	 */
	public String getPrefix()
	{
		return "";
	}

	/**
	 * Checks if a resourceName is prefixed with a protocol.
	 *
	 * @param resourceLocation Spring framework-style resource location
	 * @return true if the resource has a protocol prefix
	 */
	public boolean hasPrefix(String resourceLocation)
	{
		// check if a colon is present 
		if (resourceLocation.indexOf(PROTOCOL_PREFIX_SEPARATOR) > 0)
		{
			// need to check for all known protocol prefixes
			resourceLocation = resourceLocation.toLowerCase();
			if (resourceLocation.startsWith("file:") || resourceLocation.startsWith("classpath:") || resourceLocation.startsWith("http:"))
				return true;
		}

		return false;
	}

	/**
	 * Applies the protocl prefix to the given resource location.
	 *
	 * @param resourceLocation Resource
	 * @return The resource location, prefixed by the prefix of the provider (e. h. "file:")
	 */
	public String applyPrefix(String resourceLocation)
	{
		// only add prefix if no prefix set
		if (hasPrefix(resourceLocation))
			return resourceLocation;

		return getPrefix() + resourceLocation;
	}

	/**
	 * Gets the resource loader used by this provider.
	 * @nowarn
	 */
	public ResourceLoader getResourceLoader()
	{
		return resourceLoader;
	}

	/**
	 * Sets the resource loader used by this provider.
	 * @nowarn
	 */
	public void setResourceLoader(ResourceLoader resourceLoader)
	{
		this.resourceLoader = resourceLoader;
	}

	/**
	 * Gets the priority of this provider; small numbers will be tried before larger numbers.
	 * @nowarn
	 */
	public int getPriority()
	{
		return priority;
	}

	/**
	 * Sets the priority of this provider; small numbers will be tried before larger numbers.
	 * @nowarn
	 */
	public void setPriority(int priority)
	{
		this.priority = priority;
	}
}
