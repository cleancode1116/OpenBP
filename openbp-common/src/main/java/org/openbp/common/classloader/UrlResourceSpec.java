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

import java.net.URL;

/**
 * XClassLoader resource that specifies its target using an URL.
 *
 * @author Heiko Erhardt
 */
public abstract class UrlResourceSpec
	implements ResourceSpec
{
	/** URL of the resource */
	private URL url;

	/**
	 * Default constructor.
	 */
	public UrlResourceSpec()
	{
	}

	/**
	 * Value constructor.
	 *
	 * @param url URL of the resource
	 */
	public UrlResourceSpec(URL url)
	{
		this.url = url;
	}

	/**
	 * Gets the uRL of the resource.
	 * @nowarn
	 */
	public URL getUrl()
	{
		return url;
	}

	/**
	 * Sets the uRL of the resource.
	 * @nowarn
	 */
	public void setUrl(URL url)
	{
		this.url = url;
	}

	/**
	 * Gets the URL representation of this resource specification.
	 * @nowarn
	 */
	public URL toUrl()
	{
		return url;
	}
}
