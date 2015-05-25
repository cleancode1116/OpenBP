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
package org.openbp.common.setting;

import java.util.Enumeration;
import java.util.Properties;

import org.openbp.common.MsgFormat;
import org.openbp.common.logger.LogUtil;
import org.springframework.core.NestedRuntimeException;

/**
 * The setting util class provides utility methods for access to setting using the SettingResolver mechanism.
 * It also keeps a standard setting resolver.
 * Note that the application is resonsible for setting up the standard resolver!
 * Setting values may be strings, booleans and integers.
 *
 * Settings can be retrieved using one of the methods<br>
 * {@link #getStringSetting(String)}<br>
 * {@link #getBooleanSetting(String, boolean)}<br>
 * {@link #getIntSetting(String, int)}
 *
 * In order to store a setting value, call
 * {@link #setStringSetting(String, String)}<br>
 * {@link #setBooleanSetting(String, boolean)}<br>
 * {@link #setIntSetting(String, int)}
 *
 * For each method, there is a version that allows to specify the setting resolver and a version that
 * delegates to the standard resolver.
 *
 * Some setting providers may save the settings automatically if one of the set methods is called,
 * whereas others may cache the settings and save them to the persistent storage after the
 * {@link #saveSettings()} method has been called.
 *
 * @author Heiko Erhardt
 */
public final class SettingUtil
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Provider separator */
	public static final char PROVIDER_SEPARATOR = ':';

	/** Standard resolver */
	private static SettingResolver standardResolver;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor.
	 */
	private SettingUtil()
	{
	}

	/**
	 * Gets the standard resolver.
	 * @nowarn
	 */
	public static SettingResolver getStandardResolver()
	{
		if (standardResolver == null)
		{
			standardResolver = new SettingResolver();
		}
		return standardResolver;
	}

	/**
	 * Sets the standard resolver.
	 * @nowarn
	 */
	public static void setStandardResolver(SettingResolver standardResolverArg)
	{
		standardResolver = standardResolverArg;
	}

	//////////////////////////////////////////////////
	// @@ Convenience methods - supplied resolver
	//////////////////////////////////////////////////

	/**
	 * Gets a string setting value or the default using the supplied setting resolver.
	 *
	 * @param name Name of the setting
	 * @param dflt Default value of the setting
	 * @param resolver Resolver to use
	 * @return The setting value or the default value if the setting does not exist
	 */
	public static String getStringSetting(String name, String dflt, SettingResolver resolver)
	{
		Object value = null;
		if (resolver != null)
		{
			value = resolver.getSetting(name, dflt);
		}
		if (value == null)
			return dflt;

		if (value != null)
		{
			return value.toString();
		}

		return null;
	}

	/**
	 * Gets a boolean setting value or the default using the supplied setting resolver.
	 *
	 * @param name Name of the setting
	 * @param dflt Default value of the setting
	 * @param resolver Resolver to use
	 * @return The setting value or the default value if the setting does not exist
	 */
	public static boolean getBooleanSetting(String name, boolean dflt, SettingResolver resolver)
	{
		Object value = resolver.getSetting(name, null);
		if (value == null)
			return dflt;

		if (value instanceof Boolean)
		{
			return ((Boolean) value).booleanValue();
		}

		if (value instanceof String)
		{
			return Boolean.valueOf((String) value).booleanValue();
		}

		return dflt;
	}

	/**
	 * Sets a string setting value using the supplied setting resolver.
	 *
	 * @param name Name of the setting
	 * @param value value of the setting
	 * @param resolver Resolver to use
	 * @return The setting provider that saved the setting
	 */
	public static SettingProvider setStringSetting(String name, String value, SettingResolver resolver)
	{
		return resolver.setSetting(name, value);
	}

	/**
	 * Gets an integer setting value or the default using the supplied setting resolver.
	 *
	 * @param name Name of the setting
	 * @param dflt Default value of the setting
	 * @param resolver Resolver to use
	 * @return The setting value or the default value if the setting does not exist
	 */
	public static int getIntSetting(String name, int dflt, SettingResolver resolver)
	{
		Object value = resolver.getSetting(name, null);
		if (value == null)
			return dflt;

		if (value instanceof Integer)
		{
			return ((Integer) value).intValue();
		}

		if (value instanceof String)
		{
			try
			{
				return Integer.parseInt((String) value);
			}
			catch (NumberFormatException e)
			{
			}
		}

		return dflt;
	}

	/**
	 * Sets a boolean setting value using the supplied setting resolver.
	 *
	 * @param name Name of the setting
	 * @param value value of the setting
	 * @param resolver Resolver to use
	 * @return The setting provider that saved the setting
	 */
	public static SettingProvider setBooleanSetting(String name, boolean value, SettingResolver resolver)
	{
		return resolver.setSetting(name, new Boolean(value));
	}

	/**
	 * Sets an integer setting value using the supplied setting resolver.
	 *
	 * @param name Name of the setting
	 * @param value value of the setting
	 * @param resolver Resolver to use
	 * @return The setting provider that saved the setting
	 */
	public static SettingProvider setIntSetting(String name, int value, SettingResolver resolver)
	{
		return resolver.setSetting(name, Integer.valueOf(value));
	}

	/**
	 * Substitutes variables in the given string using the supplied setting resolver.
	 * Will replace variables of the form "${var}" with the respective setting value or the empty string.
	 * Can optionally hold prefixes or suffixes of the form ${?value-prefix?default-prefix?var?value-suffix?default-suffix}
	 * that will be included depending if the variable is set or not.
	 *
	 * @param s String to process
	 * @param mandatory 
	 *		true	If an unknown variable should result in an exception.<br>
	 *		false	If an unknown variable should be sustituted by an empty string.
	 * @param resolver Resolver to use
	 * @return The string with the substitutes applied
	 * @throws RuntimeException On an unknown mandatory variable
	 */
	public static String expandVariables(String s, boolean mandatory, SettingResolver resolver)
	{
		if (s == null)
			return null;

		int l = s.length();
		if (l == 0)
			return "";

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < l; ++i)
		{
			char c = s.charAt(i);
			if (c == '$' && i + 1 < l && s.charAt(i + 1) == '{')
			{
				StringBuffer valuePrefix = new StringBuffer();
				StringBuffer valueSuffix = new StringBuffer();
				StringBuffer defaultPrefix = new StringBuffer();
				StringBuffer defaultSuffix = new StringBuffer();

				i += 2;
				int end = s.indexOf('}', i);
				if (end < 0)
				{
					break;
				}

				String text = s.substring(i, end);

				String key = null;
				int iKeyStart = getQMarkExpr(text, 0, valuePrefix, defaultPrefix);

				int iQmark = text.indexOf("?", iKeyStart);
				if (iQmark >= 0)
				{
					key = text.substring(iKeyStart, iQmark);
					getQMarkExpr(text, iQmark, valueSuffix, defaultSuffix);
				}
				else
				{
					key = text.substring(iKeyStart);
				}

				String value = getStringSetting(key, null, resolver);
				if (value == null)
				{
					if (mandatory)
					{
						String msg = MsgFormat.format("Variable $0 cannot be resolved.", new Object [] { key });
						throw new RuntimeException(msg);
					}
				}

				if (value != null)
				{
					sb.append(valuePrefix);
					sb.append(value);
					sb.append(valueSuffix);
				}
				else
				{
					sb.append(defaultPrefix);
					sb.append(defaultSuffix);
				}

				i = end;
			}
			else
			{
				sb.append(c);
			}
		}

		return sb.toString();
	}

	private static int getQMarkExpr(String text, int iStart, StringBuffer valueBuf, StringBuffer defaultBuf)
	{
		int ret = iStart;
		if (text.length() > iStart && text.charAt(iStart) == '?')
		{
			++iStart;
			int iEnd = text.indexOf('?', iStart);
			if (iEnd < 0)
			{
				String msg = MsgFormat.format("Expected '?' in variable expression ${{0}}.", new Object [] { text });
				throw new RuntimeException(msg);
			}
			valueBuf.append(text.substring(iStart, iEnd));

			iStart = iEnd + 1;
			iEnd = text.indexOf('?', iStart);
			if (iEnd < 0)
			{
				String msg = MsgFormat.format("Expected '?' in variable expression ${{0}}.", new Object [] { text });
				throw new RuntimeException(msg);
			}
			defaultBuf.append(text.substring(iStart, iEnd));

			ret = iEnd + 1;
		}
		return ret;
	}

	/**
	 * Saves the current setting values of the setting provider of the specified setting resolver.
	 *
	 * @param resolver Resolver to use
	 * @return
	 *		true	If the setting were successfully saved
	 *		false	If there was an error saving the properties of at least one provider.
	 */
	public static boolean saveSettings(SettingResolver resolver)
	{
		if (resolver == null)
			resolver = getStandardResolver();
		return resolver.saveSettings(null);
	}

	//////////////////////////////////////////////////
	// @@ Property file support
	//////////////////////////////////////////////////

	/**
	 * Performs expandVariables on all values of the given properties.
	 *
	 * @param props Props to check
	 * @return Expanded properties
	 */
	public static Properties expandProperties(Properties props)
	{
		Properties ret = new Properties();

		for (Enumeration e = props.keys(); e.hasMoreElements();)
		{
			String key = (String) e.nextElement();
			String value = props.getProperty(key);
			value = expandVariables(value, true, getStandardResolver());
			ret.put(key, value);
		}

		return ret;
	}

	/**
	 * Performs a overwrite support for all properties keys of the given properties.
	 *
	 * @param props Props to check
	 * @return overwriten properties
	 */
	public static Properties overwriteProperties(Properties props)
	{
		Properties ret = new Properties();

		for (Enumeration e = props.keys(); e.hasMoreElements();)
		{
			String key = (String) e.nextElement();
			String value = props.getProperty(key);
			String replacement = getStringSetting(key, value);
			ret.put(key, replacement);
		}

		return ret;
	}

	//////////////////////////////////////////////////
	// @@ Convenience methods - standard resolve using the supplied setting resolverr
	//////////////////////////////////////////////////

	/**
	 * Gets a string setting value using the standard setting resolver.
	 *
	 * @param name Name of the setting
	 * @return The setting value or null if the setting does not exist
	 */
	public static String getStringSetting(String name)
	{
		return getStringSetting(name, null, getStandardResolver());
	}

	/**
	 * Gets a string setting value or the default using the standard setting resolver.
	 *
	 * @param name Name of the setting
	 * @param dflt Default value of the setting
	 * @return The setting value or the default value if the setting does not exist
	 */
	public static String getStringSetting(String name, String dflt)
	{
		return getStringSetting(name, dflt, getStandardResolver());
	}

	/**
	 * Gets a boolean setting value or the default using the standard setting resolver.
	 *
	 * @param name Name of the setting
	 * @param dflt Default value of the setting
	 * @return The setting value or the default value if the setting does not exist
	 */
	public static boolean getBooleanSetting(String name, boolean dflt)
	{
		return getBooleanSetting(name, dflt, getStandardResolver());
	}

	/**
	 * Sets a string setting value using the standard setting resolver.
	 *
	 * @param name Name of the setting
	 * @param value value of the setting
	 * @return The setting provider that saved the setting
	 */
	public static SettingProvider setStringSetting(String name, String value)
	{
		return setStringSetting(name, value, getStandardResolver());
	}

	/**
	 * Gets an integer setting value or the default using the standard setting resolver.
	 *
	 * @param name Name of the setting
	 * @param dflt Default value of the setting
	 * @return The setting value or the default value if the setting does not exist
	 */
	public static int getIntSetting(String name, int dflt)
	{
		return getIntSetting(name, dflt, getStandardResolver());
	}

	/**
	 * Sets a boolean setting value using the standard setting resolver.
	 *
	 * @param name Name of the setting
	 * @param value value of the setting
	 * @return The setting provider that saved the setting
	 */
	public static SettingProvider setBooleanSetting(String name, boolean value)
	{
		return setBooleanSetting(name, value, getStandardResolver());
	}

	/**
	 * Sets an integer setting value using the standard setting resolver.
	 *
	 * @param name Name of the setting
	 * @param value value of the setting
	 * @return The setting provider that saved the setting
	 */
	public static SettingProvider setIntSetting(String name, int value)
	{
		return setIntSetting(name, value, getStandardResolver());
	}

	/**
	 * Gets a mandatory string setting value using the standard setting resolver.
	 *
	 * @param name Name of the setting
	 * @return The setting value or null if the setting does not exist
	 * @throws RuntimeException If the setting is not present
	 */
	public static String getMandatoryStringSetting(String name)
	{
		return getStringSetting(name, null, getStandardResolver());
	}

	/**
	 * Gets a mandatory boolean setting value or the default using the standard setting resolver.
	 *
	 * @param name Name of the setting
	 * @return The setting value or the default value if the setting does not exist
	 * @throws RuntimeException If the setting is not present
	 */
	public static boolean getMandatoryBooleanSetting(String name)
	{
		Object value = getStandardResolver().getSetting(name, null);
		if (value != null)
		{
			if (value instanceof Boolean)
			{
				return ((Boolean) value).booleanValue();
			}

			if (value instanceof String)
			{
				return Boolean.valueOf((String) value).booleanValue();
			}
		}

		String msg = LogUtil.error(SettingUtil.class, "Setting value $0 has not been configured.", new Object [] { name });
		throw new RuntimeException(msg);
	}

	/**
	 * Gets a mandatory integer setting value or the default using the standard setting resolver.
	 *
	 * @param name Name of the setting
	 * @return The setting value or the default value if the setting does not exist
	 * @throws RuntimeException If the setting is not present
	 */
	public static int getMandatoryIntSetting(String name)
	{
		Object value = getStandardResolver().getSetting(name, null);
		if (value != null)
		{
			if (value instanceof Integer)
			{
				return ((Integer) value).intValue();
			}

			if (value instanceof String)
			{
				try
				{
					return Integer.parseInt((String) value);
				}
				catch (NumberFormatException e)
				{
				}
			}
		}

		String msg = LogUtil.error(SettingUtil.class, "Setting value $0 has not been configured.", new Object [] { name });
		throw new RuntimeException(msg);
	}

	/**
	 * Substitutes variables in the given string using the standard setting resolver.
	 * Will replace variables of the form "${var}" with the respective setting value or the empty string.
	 *
	 * @param s String to process
	 * @param mandatory 
	 *		true	If an unknown variable should result in an exception.<br>
	 *		false	If an unknown variable should be sustituted by an empty string.
	 * @return The string with the substitutes applied
	 * @throws NestedRuntimeException On an unknown mandatory variable
	 */
	public static String expandVariables(String s, boolean mandatory)
	{
		return expandVariables(s, mandatory, getStandardResolver());
	}

	/**
	 * Saves the current setting values of the setting provider of the standard setting resolver.
	 *
	 * @return
	 *		true	If the setting were successfully saved
	 *		false	If there was an error saving the properties of at least one provider.
	 */
	public static boolean saveSettings()
	{
		return standardResolver.saveSettings(null);
	}
}
