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
 * XClassLoader resource specification that specifies its target using an URL to a class file.
 *
 * @author Heiko Erhardt
 */
public class FileUrlResourceSpec extends UrlResourceSpec
{
	/**
	 * Default constructor.
	 */
	public FileUrlResourceSpec()
	{
		super();
	}

	/**
	 * Value constructor.
	 *
	 * @param url URL of the resource
	 */
	public FileUrlResourceSpec(URL url)
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
		URL url = getUrl();
		File file = new File(url.toURI());

		byte [] content = XClassLoaderUtil.readFileContent(file);

		String resourceName = file.getAbsolutePath();
		resourceName = StringUtil.normalizePathName(resourceName);
		ResourceEntry entry = new ResourceEntry(url, content);
		cl.addResourceEntry(resourceName, entry);
	}
}
