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
package org.openbp.common.classloader;

import org.openbp.common.logger.LogLevel;

/**
 * XClassLoader that logs errors to System.err.
 *
 * @author Falk Hartmann
 */
public class XClassLoaderSystemErr extends XClassLoaderBase
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** This holds the currently adjusted log level. */
	// private String currentLogLevel;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * The constructor.
	 *
	 * @param config A configuration for the class loader to be constructed
	 * @param logLevel The log level to be used for the class loader as in {@link LogLevel}
	 * @throws Exception On any error that occurs while scanning the repositories specified in the class loader configuration
	 */
	public XClassLoaderSystemErr(XClassLoaderConfiguration config, String logLevel)
		throws Exception
	{
		super();

		// currentLogLevel = logLevel;

		init(config);
	}

	//////////////////////////////////////////////////
	// @@ Logging
	//////////////////////////////////////////////////

	/**
	 * @copy XClassLoaderBase.setupLogger()
	 */
	protected void setupLogger()
	{
	}

	/**
	 * @copy XClassLoaderBase.writeLog(String, String, Exception)
	 */
	protected void writeLog(String logLevel, String msg, Exception e)
	{
		// TODO if (logLevel >= currentLogLevel)
		if (true)
		{
			if (e != null)
			{
				System.err.println("XClassLoaderSystemErr: " + msg + "\n" + e);
			}
			else
			{
				System.err.println("XClassLoaderSystemErr: " + msg);
			}
		}
	}

	/**
	 * @copy XClassLoaderBase.isLogEnabled(String)
	 */
	protected boolean isLogEnabled(String logLevel)
	{
		// TODO return logLevel >= currentLogLevel;
		return true;
	}
}
