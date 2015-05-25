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
package org.openbp.common.net;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * To be used for URLs that refer to byte arrays in memory.
 *
 * @author Stephan Pauxberger
 */
public class ByteArrayURLStreamHandler extends URLStreamHandler
{
	/** Byte array */
	byte[] bytes;

	/**
	 * Constructor.
	 *
	 * @param bytes Byte array
	 */
	public ByteArrayURLStreamHandler(byte[] bytes)
	{
		super();
		this.bytes = bytes;
	}

	public URLConnection openConnection(URL url)
	{
		return new ByteArrayURLConnection(url, bytes);
	}
}
