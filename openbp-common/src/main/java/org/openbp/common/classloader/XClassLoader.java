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

import org.openbp.common.logger.LogUtil;

/**
 * This class is a class loader that supports the features as implemented
 * by {@link XClassLoaderBase} and enhances them with LogUtil-based logging.
 *
 * @author Falk Hartmann
 */
public class XClassLoader extends XClassLoaderBase
{
	/**
	 * The constructor.
	 *
	 * @param config The configuration to be used for this class
	 * @throws Exception On any error that occurs while scanning the repositories specified in the class loader configuration
	 */
	public XClassLoader(XClassLoaderConfiguration config)
		throws Exception
	{
		super(config);
	}

	//////////////////////////////////////////////////
	// @@ Log method implementation
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
		if (getConfiguration().isLoggingEnabled())
		{
			LogUtil.log(logLevel, XClassLoader.class, msg, e);
		}
	}

	/**
	 * @copy XClassLoaderBase.isLogEnabled(String)
	 */
	protected boolean isLogEnabled(String logLevel)
	{
		if (getConfiguration().isLoggingEnabled())
		{
			return LogUtil.isLoggerEnabled(logLevel, XClassLoader.class);
		}
		return false;
	}
}
