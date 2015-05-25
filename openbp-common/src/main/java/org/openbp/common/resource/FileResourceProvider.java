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

import org.openbp.common.string.StringUtil;
import org.openbp.common.util.ToStringHelper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Provides access to file system-based resources.
 * Resources are retrieved from the file system from a resource directory.
 * A Spring ResourceLoader is used to retrieve file resources.
 *
 * Multiple FileResourceProviders-Obects can be registered with a common ResourceResolver.
 * This is used by the ResourceResolver to implement a "match-first" strategy,
 * where multiple sources for resources can be configured (with multiple ResourceProviders) and the first match is returned.
 *
 * @author Heiko Erhardt
 */
public class FileResourceProvider extends ResourceProvider
{
	/** Directory to load the resource files from */
	private String resourceDir;

	/**
	 * Constructor.
	 *
	 * @param priority Priority of the provider
	 * @param loader Loader
	 * @param resourceDir Resource dir
	 */
	public FileResourceProvider(int priority, ResourceLoader loader, String resourceDir)
	{
		super(priority, loader);

		setResourceDir(resourceDir);
	}

	public String toString()
	{
		return ToStringHelper.toString(this, "resourceDir", "priority");
	}

	/**
	 * file based resources are first matched with the root dir name
	 */
	public Resource getResource(String resourceLocation)
		throws ResourceMgrException
	{
		String resourceName = resourceLocation;
		resourceName = StringUtil.normalizePathName(resourceName);

		// Remove protocol prefix if already present
		// We first need to combine resourceDir with resourceName
		// file:/path/file.ext --> /path/file.ext
		if (hasPrefix(resourceName))
		{
			int index = resourceName.indexOf(':');
			resourceName = resourceName.substring(index + 1);
		}

		// Construct a full path from resourceDir + resourceName
		if (resourceDir != null && ! resourceName.startsWith(resourceDir))
		{
			resourceName = StringUtil.buildPath(resourceDir, resourceName);
		}

		// Set protocol prefix (file:/)
		resourceName = "file:" + resourceName;

		return getResourceLoader().getResource(resourceName);
	}

	public String getPrefix()
	{
		return "file:";
	}

	/**
	 * Gets the directory to load the resource files from.
	 * @nowarn
	 */
	public String getResourceDir()
	{
		return resourceDir;
	}

	/**
	 * Sets the directory to load the resource files from.
	 * @nowarn
	 */
	public void setResourceDir(String resourceDir)
	{
		this.resourceDir = StringUtil.normalizeDir(resourceDir);
	}
}
