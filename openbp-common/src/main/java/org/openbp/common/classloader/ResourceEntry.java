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

import java.net.MalformedURLException;
import java.net.URL;

import org.openbp.common.net.ByteArrayURLStreamHandler;

/**
 * This class defines an entry in the class/resource table of the {@link XClassLoader}
 *
 * @author Heiko Erhardt
 */
public class ResourceEntry
{
	/** Name of the repository this entry belongs to */
	private URL repositoryUrl;

	/** Cached contents of the entry */
	private byte [] content;

	/**
	 * Constructor.
	 */
	public ResourceEntry()
	{
	}

	/**
	 * Constructor.
	 *
	 * @param repositoryUrl Name of the repository this entry belongs to
	 */
	public ResourceEntry(URL repositoryUrl)
	{
		this.repositoryUrl = repositoryUrl;
	}

	/**
	 * Constructor.
	 *
	 * @param repositoryUrl Name of the repository this entry belongs to
	 * @param content Cached contents of the entry
	 */
	public ResourceEntry(URL repositoryUrl, byte [] content)
	{
		this.repositoryUrl = repositoryUrl;
		this.content = content;
	}

	/**
	 * Gets the name of the repository this entry belongs to.
	 * @nowarn
	 */
	public URL getRepositoryUrl()
	{
		return repositoryUrl;
	}

	/**
	 * Sets the name of the repository this entry belongs to.
	 * @nowarn
	 */
	public void setRepositoryUrl(URL repositoryUrl)
	{
		this.repositoryUrl = repositoryUrl;
	}

	/**
	 * Gets the cached contents of the entry.
	 * @nowarn
	 */
	public byte [] getContent()
	{
		return content;
	}

	/**
	 * Sets the cached contents of the entry.
	 * @nowarn
	 */
	public void setContent(byte [] content)
	{
		this.content = content;
	}

	/**
	 * Reads the resource from the repository.
	 *
	 * @return Bytes that define the content of the entry (i. e. the class code)
	 */
	public byte [] getBytes()
	{
		return content;
	}

	/**
	 * Creates an URL to this object.
	 *
	 * @return The URL string or null on error
	 */
	protected URL createURL()
	{
		// Return an URL that refers to the repository and returns an input stream to the byte array if requested
		try
		{
			ByteArrayURLStreamHandler handler = new ByteArrayURLStreamHandler(content);
			URL url = new URL(repositoryUrl.getProtocol(), repositoryUrl.getHost(), repositoryUrl.getPort(), repositoryUrl.getFile(), handler);
			return url;
		}
		catch (MalformedURLException e)
		{
		}
		return null;
	}
}
