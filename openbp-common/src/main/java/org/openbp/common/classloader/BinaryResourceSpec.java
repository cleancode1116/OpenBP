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
 * XClassLoader resource specification that directly provides its contents as byte array.
 * The {@link #setUrl(URL)} may specify the source of the binary content.
 *
 * @author Heiko Erhardt
 */
public abstract class BinaryResourceSpec extends UrlResourceSpec
{
	/** Binary content */
	public byte [] content;

	/**
	 * Default constructor.
	 */
	public BinaryResourceSpec()
	{
	}

	/**
	 * Value constructor.
	 *
	 * @param url URL of the resource
	 */
	public BinaryResourceSpec(URL url)
	{
		super(url);
	}

	/**
	 * Value constructor.
	 *
	 * @param url URL of the resource
	 * @param content Binary content
	 */
	public BinaryResourceSpec(URL url, byte [] content)
	{
		super(url);
		this.content = content;
	}

	/**
	 * Gets the binary content.
	 * @nowarn
	 */
	public byte [] getContent()
	{
		return content;
	}

	/**
	 * Sets the binary content.
	 * @nowarn
	 */
	public void setContent(byte [] content)
	{
		this.content = content;
	}

	/**
	 * Gets the URL representation of this resource specification.
	 * @nowarn
	 */
	public URL toUrl()
	{
		URL sourceUrl = getUrl();
		if (content != null)
		{
			// Return an URL that refers to the repository and returns an input stream to the byte array if requested
			try
			{
				ByteArrayURLStreamHandler handler = new ByteArrayURLStreamHandler(getContent());
				URL ret = new URL(sourceUrl.getProtocol(), sourceUrl.getHost(), sourceUrl.getPort(), sourceUrl.getFile(), handler);
				return ret;
			}
			catch (MalformedURLException e)
			{
			}
		}
		return sourceUrl;
	}
}
