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

import java.util.Vector;

import org.openbp.common.logger.LogUtil;
import org.springframework.core.io.Resource;

/**
 * ResourceResolver is used by ResourceMgr to resolve Resource names and load resources
 * keeps a list of ResourceProvider objects, which is ordered by priority
 * "Match First" Strategy: 
 *    ordered by priority, each provider in the list will be asked to load the requested resource
 *    until it was found or a ResourceMgrException will be thrown
 * Note: One ClasspathResourceProvider will always be registered as default with prio=100 
 *
 * @author prachtp
 */
public class ResourceResolver
{
	Vector providerList = new Vector();

	public ResourceResolver()
	{
	}

	/**
	 * Attach a provider to the provider list.
	 * The provider list will be sorted by provider priority.
	 * @param provider Provider to add
	 */
	public void addProvider(ResourceProvider provider)
	{
		LogUtil.info(getClass(), "Adding resource provider {0}.", provider);

		if (providerList.isEmpty())
		{
			providerList.add(provider);
		}
		else
		{
			for (int i = 0; i < providerList.size(); i++)
			{
				ResourceProvider p = (ResourceProvider) providerList.get(i);
				if (p.getPriority() > provider.getPriority())
				{
					providerList.add(i, provider);
					return;
				}
			}

			// add the provider to the end of the list
			providerList.add(provider);
		}
	}

	/**
	 * Ask each registered providers for the requested resource until it is found
	 * 'match-first': the queries are issued in the order of the provider prio
	 *
	 * Note:
	 * If no existing resource can be found, a ResourceMgrException is thrown,
	 * indicating that none of the existing ResourceProvider could load the requested resource 
	 *
	 * @param resourceLocation name of the resource
	 * @return The resource
	 * @throws ResourceMgrException If no such resource could be found
	 */
	Resource getResource(String resourceLocation)
	{
		Resource resource = null;

		for (int i = 0; i < providerList.size(); i++)
		{
			ResourceProvider provider = (ResourceProvider) providerList.get(i);
			resource = provider.getResource(resourceLocation);

			if (resource != null)
			{
				if (resource.exists())
				{
					return resource;
				}
			}
		}

		// no existing resource found !!!
		// throw a ResourceMgrException with sufficiently detailed context

		// we cannot use logging here (otherwise Stackoverflow for logging resource loading error will occure) !!! 
		String msg = "The resource '" + resourceLocation + "' could not be found by any resource provider.";
		throw new ResourceMgrException(msg);
	}

	/**
	 * Prepare a displayable string with provider info.
	 * @return The info string
	 */
	protected String getProviderInfoString()
	{
		String providers = "";
		String separator = ";";
		for (int i = 0; i < providerList.size(); i++)
		{
			if (i > 0)
				providers += separator;
			ResourceProvider provider = (ResourceProvider) providerList.get(i);
			providers += provider.toString();
		}

		return providers;
	}

	public void clearProviderList()
	{
		providerList.clear();
	}
}
