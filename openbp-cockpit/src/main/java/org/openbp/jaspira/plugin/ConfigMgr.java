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
package org.openbp.jaspira.plugin;

import org.openbp.common.application.Application;
import org.openbp.common.commandline.CommandLineParser;
import org.openbp.common.commandline.CommandLineParserException;
import org.openbp.common.setting.PropertyFileProvider;
import org.openbp.common.setting.SettingUtil;

/**
 * The configuration manager stores a number of configuration values that can be used to customize the application and its plugins.
 * This class is a singleton.
 *
 * @author Heiko Erhardt
 */
public final class ConfigMgr
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Property file provider */
	private PropertyFileProvider provider;

	/** Config property resource name */
	private String configResourceName;

	/** Standard Config.properties file in $rootdir/$appname */
	public static final String STD_CONFIG_FILE = "config";

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Singleton instance */
	private static ConfigMgr singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized ConfigMgr getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new ConfigMgr();
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private ConfigMgr()
	{
	}

	/**
	 * Gets the config property resource name.
	 * @nowarn
	 */
	public String getConfigResourceName()
	{
		return configResourceName;
	}

	/**
	 * Sets the config property resource name.
	 * @nowarn
	 */
	public void setConfigResourceName(String configResourceName)
	{
		this.configResourceName = configResourceName;
	}

	/**
	 * Initializes the configuration manager.
	 * Reads configuration values from the config.properties file in the application's root directory.
	 */
	public void initialize()
	{
		boolean first = false;

		if (provider == null)
		{
			provider = new PropertyFileProvider();
			provider.setReadonly(true);
			first = true;
		}
		else
		{
			provider.clear();
		}

		// Read the standard property file first
		if (getConfigResourceName() != null)
		{
			readPropertyResource(getConfigResourceName(), false);
		}

		// Check for the configuration-related arguments
		String [] args = Application.getArguments();
		if (args != null)
		{
			CommandLineParser cp = new CommandLineParser();
			cp.setAcceptUnknownOptions(true);

			cp.addRepeatableOption("propertyFile", "Name of a property resource containing configuration information");
			cp.addRepeatableOption("property", "Configuration property value");

			try
			{
				cp.parse(args);

				String [] propertyFiles = cp.getRepeatableOption("propertyFile");
				if (propertyFiles != null)
				{
					for (int i = 0; i < propertyFiles.length; ++i)
					{
						String file = propertyFiles[i];
						readPropertyResource(file, true);
					}
				}

				String [] propertyKeys = cp.getRepeatableOption("property");
				if (propertyKeys != null)
				{
					for (int i = 0; i < propertyKeys.length; ++i)
					{
						String prop = propertyKeys[i];
						String key = null;
						String value = "true";

						int index = prop.indexOf("=");
						if (index > 0)
						{
							key = prop.substring(0, index);
							value = prop.substring(index + 1);
						}
						else
						{
							key = prop;
						}
						setValue(key, value);
					}
				}
			}
			catch (CommandLineParserException e)
			{
				System.err.println(e.getMessage());
				System.err.println("");
				cp.printUsageAndExit();
			}
		}

		if (first)
		{
			SettingUtil.getStandardResolver().addProvider("ConfigMgr", provider, 200);
		}
	}

	/**
	 * Reads the specified property file.
	 *
	 * @param propertyResourceName Full path name of the property file
	 */
	public void readPropertyResource(String propertyResourceName, boolean mandatory)
	{
		provider.setPropertyResourceName(propertyResourceName);
		provider.setMandatory(mandatory);
		provider.loadSettings();
	}

	//////////////////////////////////////////////////
	// @@ Public methods
	//////////////////////////////////////////////////

	/**
	 * Evaluates a configuration expression.
	 *
	 * @param cond The condition expression may contain:<br>
	 * IDENT Configuration value<br>
	 * || OR operator<br>
	 * &aamp;&aamp;  AND operator<br>
	 * ! NOT operator
	 *
	 * If the expression is null, the method evaluates to true.
	 * @nowarn
	 */
	public boolean evaluate(String cond)
	{
		if (cond == null)
			return true;

		// TODO Feature 6: This is a *very* basic expression support implementation
		return isDefined(cond);
	}

	/**
	 * checks if the given key defined.
	 * @nowarn
	 */
	public boolean isDefined(String key)
	{
		String value = getValue(key);
		if (value == null || value.equals("false") || value.equals("no") || value.equals("null"))
			return false;
		return true;
	}

	/**
	 * Gets the configuration value for the given key.
	 * @return The value or null
	 * @nowarn
	 */
	public String getValue(String key)
	{
		return SettingUtil.getStringSetting(key);
	}

	/**
	 * Sets a configuration value.
	 * @nowarn
	 */
	public void setValue(String key, String value)
	{
		provider.setSetting(key, value);
	}
}
