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
package org.openbp.common.string;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A string buffer-based output stream.
 *
 * @author Andreas Putz
 */
public class StringBufferOutputStream extends OutputStream
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** String buffer */
	private StringBuffer buffer;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param buf String buffer
	 */
	public StringBufferOutputStream(StringBuffer buf)
	{
		buffer = buf;
	}

	//////////////////////////////////////////////////
	// @@  Outputstream implementation
	//////////////////////////////////////////////////

	/**
	 * see java.io.OutputStream#write(int)
	 * @nowarn
	 */
	public void write(int b)
		throws IOException
	{
		buffer.append((char) b);
	}
}
