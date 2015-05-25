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

import org.openbp.common.util.ToStringHelper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Provides access to resources using a HTTP connection.
 *
 * @author Heiko Erhardt
 */
public class HttpResourceProvider extends ResourceProvider
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Protocol (e. g. "http") */
	private String protocol;

	/** Host (e. g. "localhost") */
	private String host;

	/** Port (e. g. "8080") */
	private String port;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param priority Priority of the provider
	 * @param resourceLoader Resource loader
	 * @param protocol Protocol (e. g. "http")
	 * @param host Host (e. g. "localhost")
	 * @param port Port (e. g. "8080")
	 */
	protected HttpResourceProvider(int priority, ResourceLoader resourceLoader, String protocol, String host, String port)
	{
		super(priority, resourceLoader);
		this.protocol = protocol;
		this.host = host;
		this.port = port;
	}

	public String toString()
	{
		return ToStringHelper.toString(this, "urlPrefix", "priority");
	}

	public String getUrlPrefix()
	{
		// format port-string, if available
		String port = getPort();

		// format protocol string (defaults to http:)
		String protocol = getProtocol().toLowerCase();
		if (protocol == null)
		{
			protocol = "http:";
		}

		String host = getHost();
		if (host == null)
			host = "localhost";

		StringBuffer sb = new StringBuffer(protocol);
		sb.append("//");
		sb.append(host);
		if (port != null)
		{
			if (!port.startsWith(":"))
				sb.append(":");
			sb.append(port);
		}
		return sb.toString();
	}

	public Resource getResource(String resourceLocation) throws ResourceMgrException
	{
		String resourceName = resourceLocation;
		if (!hasPrefix(resourceName))
		{
			resourceName = applyPrefix(resourceName);
		}

		return getResourceLoader().getResource(resourceName);
	}

	/**
	 * Applies the protocl prefix to the given resource location.
	 * The HttpResourceProvider needs to separate the prefix from the resource name by "/"
	 * e.g.: https://localhost:8080 + '/' + resourceName
	 *
	 * @param resourceLocation Resource
	 * @return The resource location, prefixed by the prefix of the provider (e. h. "file:")
	 */
	public String applyPrefix(String resourceLocation)
	{
		String resourceName = resourceLocation;
		if (!resourceName.startsWith("/"))
		{
			resourceName = "/" + resourceName;
		}
		return getUrlPrefix() + resourceName;
	}

	/**
	 * Gets the protocol (e. g. "http").
	 * @nowarn
	 */
	public String getProtocol()
	{
		return protocol;
	}

	/**
	 * Sets the protocol (e. g. "http").
	 * @nowarn
	 */
	public void setProtocol(String protocol)
	{
		this.protocol = protocol;
	}

	/**
	 * Gets the host (e. g. "localhost").
	 * @nowarn
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * Sets the host (e. g. "localhost").
	 * @nowarn
	 */
	public void setHost(String host)
	{
		this.host = host;
	}

	/**
	 * Gets the port (e. g. "8080").
	 * @nowarn
	 */
	public String getPort()
	{
		return port;
	}

	/**
	 * Sets the port (e. g. "8080").
	 * @nowarn
	 */
	public void setPort(String port)
	{
		this.port = port;
	}
}
