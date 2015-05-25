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
package org.openbp.server.engine.script;

import java.io.IOException;
import java.io.OutputStream;

import org.openbp.common.logger.LogUtil;
import org.openbp.common.string.StringUtil;

/**
 * This class is used by the script engine to redirect Beanshell output
 * into the logger.
 *
 * @author Falk Hartmann
 */
public class ScriptEngineOutputStream extends OutputStream
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** This accumulates the data that has been written to the stream. */
	private StringBuffer buffer = new StringBuffer();

	/** Log level */
	private String logLevel;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * The construtor.
	 *
	 * @param logLevel Log level
	 */
	public ScriptEngineOutputStream(String logLevel)
	{
		this.logLevel = logLevel;
	}

	//////////////////////////////////////////////////
	// @@ Stream implementation
	//////////////////////////////////////////////////

	static char[] TRIM_CHARS = new char[] { '\n', '\r' };
	
	public void write(int intCh)
		throws IOException
	{
		char ch = (char) intCh;

		// Append to the buffer.
		buffer.append(ch);

		// If the last character was a newline, log buffer.
		if (ch == '\n')
		{
			if (LogUtil.isLoggerEnabled(logLevel, ScriptEngineImpl.class))
			{
				String line = handleLogLine(buffer.toString());
				buffer.setLength(0);

				String trimmed = line.trim();
				if (! "".equals(trimmed) && ! ";".equals(trimmed))
				{
					trimmed = StringUtil.trim(line, TRIM_CHARS);
					LogUtil.log(logLevel, ScriptEngineImpl.class, "script>\t" + trimmed);
				}
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Helper methods
	//////////////////////////////////////////////////

	/**
	 * This method handles a log line as printed by the bean shell,
	 * this means, the prefixes used by the BeanShell are removed from the line.
	 *
	 *
	 * @param line The line printed by the BeanShell
	 * @return The line without prefix
	 */
	public static String handleLogLine(String line)
	{
		if (line.startsWith("// Error: "))
		{
			return line.substring(10);
		}
		else if (line.startsWith("// Debug: "))
		{
			return line.substring(10);
		}
		else if (line.startsWith("// "))
		{
			return line.substring(3);
		}
		return line;
	}
}
