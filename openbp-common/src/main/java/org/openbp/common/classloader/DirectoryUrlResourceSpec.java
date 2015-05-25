/*
 *   Copyright 2009 skynamics AG
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
package org.openbp.common.classloader;

import java.io.File;
import java.net.URL;

import org.openbp.common.string.StringUtil;

/**
 * XClassLoader resource specification that specifies its target using an URL to a directory.
 *
 * @author Heiko Erhardt
 */
public class DirectoryUrlResourceSpec extends UrlResourceSpec
{
	/**
	 * Default constructor.
	 */
	public DirectoryUrlResourceSpec()
	{
		super();
	}

	/**
	 * Value constructor.
	 *
	 * @param url URL of the resource
	 */
	public DirectoryUrlResourceSpec(URL url)
	{
		super(url);
	}

	/**
	 * Adds all entries denoted by this resource specification to the class loader.
	 *
	 * @param cl The class loader
	 * @throws Exception On any error that occurs while scanning or reading the resource
	 */
	public void addEntriesToClassLoader(XClassLoader cl)
		throws Exception
	{
		File dir = new File(getUrl().toURI());
		String basePath = dir.getAbsolutePath();

		scanDirectory(cl, dir, basePath);
	}

	/**
	 * This is a recursive method for mapping all of the files that are considered to be
	 * java classes into our entry table.
	 * By maintaining a list of all classes, we can greatly reduce the amount of time
	 * taken to retrieve the bytes for a specific class
	 * and can easily deduce when the required class is not available.
	 *
	 * @param cl The class loader
	 * @param dir Directory to map
	 * @param basePath Base directory, used when deducing which directories are package names
	 * @throws Exception On any error that occurs while scanning the repositories specified in the class loader configuration
	 */
	private void scanDirectory(XClassLoader cl, File dir, String basePath)
		throws Exception
	{
		int basePathLen = basePath.length();

		String [] files = dir.list();
		for (int i = 0; i < files.length; i++)
		{
			File f = new File(dir.getPath(), files [i]);
			if (f.isDirectory())
			{
				// Recurse
				scanDirectory(cl, f, basePath);
			}
			else
			{
				String path = f.getAbsolutePath();

				// Store the class file for access via getResource().
				String resourceName = path.substring(basePathLen + 1);
				resourceName = StringUtil.normalizePathName(resourceName);

				if (cl.getResourceEntry(resourceName) != null)
				{
					// This entry is already defined, ignore
					continue;
				}

				byte [] content = XClassLoaderUtil.readFileContent(f);

				// Store the resource name of the zip entry in the table
				ResourceEntry entry = new ResourceEntry(f.toURL(), content);
				cl.addResourceEntry(resourceName, entry);

				// For class files, store a mapping of the class name
				cl.addResourceEntryForClass(resourceName, entry);
			}
		}
	}
}
