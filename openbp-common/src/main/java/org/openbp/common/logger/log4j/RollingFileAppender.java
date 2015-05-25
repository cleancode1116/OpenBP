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
package org.openbp.common.logger.log4j;

import java.io.IOException;

import org.openbp.common.logger.LogUtil;

/**
 * This appender extends the {@link org.apache.log4j.RollingFileAppender} by
 * allowing a relative log file name to be passed as log file.
 * If a relative file name is passed, it is considered as relative
 * to the root dir of the application.
 *
 * @author Falk Hartmann
 */
public class RollingFileAppender extends org.apache.log4j.RollingFileAppender
{
	/**
	 * This method, inherited from the superclass is extended here to call
	 * LogUtil.handleLogFileName to get an absolute log file name and to ensure
	 * that the directory the file resides is existing.
	 *
	 * @param fileName The file name (relative or absolute)
	 * @param append Wheter log information should be appended
	 * @param bufferedIO Whether I/O operations should be buffered.
	 * @param bufferSize The size of the buffer to be used for buffering, if enabled
	 * @throws IOException if an I/O problem occurred.
	 */
	public synchronized void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize)
		throws IOException
	{
		super.setFile(LogUtil.handleLogFileName(fileName), append, bufferedIO, bufferSize);
	}
}
