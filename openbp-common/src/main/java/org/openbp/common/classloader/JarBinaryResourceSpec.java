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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * XClassLoader resource specification that specifies its target using an URL to a Jar file.
 *
 * @author Heiko Erhardt
 */
public class JarBinaryResourceSpec extends BinaryResourceSpec
{
	/**
	 * Default constructor.
	 */
	public JarBinaryResourceSpec()
	{
		super();
	}

	/**
	 * Value constructor.
	 *
	 * @param url URL of the resource
	 */
	public JarBinaryResourceSpec(URL url)
	{
		super(url);
	}

	/**
	 * Value constructor.
	 *
	 * @param url URL of the resource
	 * @param content Binary content
	 */
	public JarBinaryResourceSpec(URL url, byte [] content)
	{
		super(url, content);
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
		InputStream in = null;

		try
		{
			in = new ByteArrayInputStream(content);

			XClassLoaderUtil.scanJarStream(cl, in, getUrl());
		}
		catch(Exception ex)
		{
		}
		finally
		{
			if (in != null)
			{
				in.close();
			}
		}
	}
}
