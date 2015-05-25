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
package org.openbp.common.application;

import java.io.File;

import org.openbp.common.CommonUtil;
import org.openbp.common.MsgFormat;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.setting.FixedSettingProvider;
import org.openbp.common.setting.PropertyFileProvider;
import org.openbp.common.setting.SettingResolver;
import org.openbp.common.setting.SettingUtil;
import org.openbp.common.setting.SystemPropertyProvider;
import org.openbp.common.string.StringUtil;

/**
 * This class manages the name and root directory of an application.
 *
 * Because other parts of the framework and applications built on top
 * of it rely on this setting, the class should be initialized by
 * a call to {@link #initialize} as soon as possible.
 *
 * @author Heiko Erhardt
 */
public final class Application
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** System provider name */
	public static final String SYSTEM_PROVIDER = "system";

	/** Root directory name */
	public static final String ROOTDIR_PROVIDER = "rootdir";

	//////////////////////////////////////////////////
	// @@ Static data
	//////////////////////////////////////////////////

	/** Application name */
	private static String appName;

	/** Command line arguments */
	private static String [] arguments;

	/** Optional root dir of the application */
	private static String rootDir;

	/** Flag if rootdir should be initialized */
	private static boolean dirty = true;

	/** Root directory setting provider */
	private static FixedSettingProvider rootDirProvider = new FixedSettingProvider();

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private Application()
	{
	}
	
	//////////////////////////////////////////////////
	// @@ Public methods
	//////////////////////////////////////////////////

	/**
	 * Sets the application name and the application arguments.
	 *
	 * @param name Name of the application (defines resource sub directory)
	 * @param args Command line arguments
	 * @deprecated Application name functionality has been removed; use {@link #setArguments} instead
	 */
	public static void setNameAndArguments(String name, String [] args)
	{
		appName = name;
		arguments = args;
	}

	/**
	 * Sets resource and Log4j property file locations according to the application name and root dir.
	 */
	public static void initialize()
	{
		if (!dirty)
		{
			// Nothing to do
			return;
		}

		initializeRootDir();
		initializeSettingProvidersAndLogger();

		dirty = false;
	}

	/**
	 * Initializes the root directory from the argument list or the system properties if not set yet.
	 */
	protected static void initializeRootDir()
	{
		// Check for the -rootDir argument
		if (rootDir == null && arguments != null)
		{
			for (int i = 0; i < arguments.length; ++i)
			{
				if (arguments [i].equalsIgnoreCase("-rootdir"))
				{
					if (i == arguments.length - 1)
					{
						System.err.println("-rootDir option requires an argument");
						throw new RuntimeException("Argument '-rootdir' not found");
					}
					setRootDir(arguments [i + 1]);

					++i;
				}
			}
		}

		if (rootDir == null)
		{
			setRootDir(System.getProperty("rootDir"));
		}

		// Check validity of root directory
		if (rootDir != null)
		{
			File dir = new File(rootDir);
			if (!dir.isDirectory())
			{
				String s = MsgFormat.format("Directory $0 passed as root directory does not seem to exist.", rootDir);
				System.err.println(s);
				throw new RuntimeException(s);
			}
			if (!dir.isAbsolute())
			{
				setRootDir(dir.getAbsolutePath());
			}
		}
	}

	/**
	 * Sets up the default providers of the setting manager.
	 * This method will add the {@link SystemPropertyProvider} using the name "system" and priority 100
	 * and an {@link PropertyFileProvider} that accesses the specified property file as read-only provider
	 * using the name "core" and priority 90.
	 */
	protected static void initializeSettingProvidersAndLogger()
	{
		SettingResolver resolver = SettingUtil.getStandardResolver();
		resolver.clearProvider(SYSTEM_PROVIDER);
		resolver.clearProvider(ROOTDIR_PROVIDER);

		// The Standard system property provider provides access to system properties
		SystemPropertyProvider systemPropertyProvider = SystemPropertyProvider.getInstance();
		resolver.addProvider(SYSTEM_PROVIDER, systemPropertyProvider, 50);

		// The rootdir provider provider will supply the root directory setting
		resolver.addProvider(ROOTDIR_PROVIDER, rootDirProvider, 70);

		LogUtil.updateSettings();
	}

	/**
	 * Registers a property file resource.
	 *
	 * @param resourceName Resource name
	 * @param priority Setting resource provider priority to use
	 * @param writeable true if this is a writable resource; In this case, the properties will be saved back to the property resource if changed.
	 * Note that the resource must be writable (i. e. a property file).
	 */
	public static void registerPropertyResource(String resourceName, int priority, boolean writeable)
	{
		if (! resourceName.endsWith(".properties"))
		{
			resourceName = resourceName + ".properties";
		}

		// The root configuration provider will only read options from the Core.properties
		PropertyFileProvider provider = new PropertyFileProvider();
		provider.setPropertyResourceName(resourceName);
		provider.setReadonly(! writeable);
		SettingResolver resolver = SettingUtil.getStandardResolver();
		resolver.addProvider(resourceName, provider, priority);
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the application name.
	 * @deprecated Application name functionality has been removed
	 * @nowarn
	 */
	public static String getAppName()
	{
		return appName;
	}

	/**
	 * Sets the application name.
	 * @deprecated Application name functionality has been removed
	 * @nowarn
	 */
	public static void setAppName(String appNameArg)
	{
		appName = appNameArg;
	}

	/**
	 * Gets the command line arguments.
	 * @nowarn
	 */
	public static String [] getArguments()
	{
		return arguments;
	}

	/**
	 * Sets the command line arguments.
	 * @nowarn
	 */
	public static void setArguments(String [] argumentsArg)
	{
		arguments = argumentsArg;
	}

	/**
	 * Gets the root dir.
	 *
	 * @return The root directory of the application or the current directory if the
	 * root directory cannot be determined.
	 */
	public static String getRootDir()
	{
		// Return the root directory.
		return rootDir;
	}

	/**
	 * Sets the default root dir of the application.
	 * The method will print out an error message and exit the program if the root directory is
	 * not accessible.
	 * @param rd Root directory
	 */
	public static void setRootDir(String rd)
	{
		if (!CommonUtil.equalsNull(rd, Application.rootDir))
		{
			if (rd != null)
			{
				rd = StringUtil.normalizePathName(rd);
			}
			Application.rootDir = rd;
			dirty = true;
		}
	}

	/**
	 * Defines one of the names under which the root directory should appear in the SettingUtil.
	 * Do not call before {@link #initialize}().
	 *
	 * @param name Name to add
	 */
	public static void defineRootDirSettingName(String name)
	{
		if (rootDir != null)
		{
			rootDirProvider.define(name, rootDir);
		}
	}
}
