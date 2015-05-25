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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.openbp.common.CommonUtil;
import org.openbp.common.resource.ResourceMgr;
import org.openbp.common.resource.ResourceMgrException;
import org.openbp.common.util.ToStringHelper;
import org.springframework.core.io.Resource;

/**
 * Implementation of a setting provider that supports access to property files.
 * Property files contain lines of the form
 * @code 3
 * name=value
 * @code
 * The returned setting values are always strings.
 *
 * The provider can be prevented to save properties by setting the {@link #setReadonly} property.
 * You may also force it to accept only properties that area already present in the property
 * file by setting the {@link #setSaveExistingPropertiesOnly} flag.
 *
 * @seec SettingResolver
 *
 * @author Heiko Erhardt
 */
public class PropertyFileProvider
	implements SettingProvider
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Property file name */
	private String propertyResourceName;

	/** Property file header */
	private String fileHeader;

	/** Readonly flag */
	private boolean readonly;

	/** Mandatory flag */
	private boolean mandatory;

	/** Flag to save only properties that already exist in the property file */
	private boolean saveExistingPropertiesOnly;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Table of all properties */
	private Properties properties;

	/** Dirty flag; set if properties have been changed */
	private boolean dirty;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public PropertyFileProvider()
	{
		properties = new Properties();
	}

	/**
	 * Clears all property settings.
	 */
	public void clear()
	{
		properties.clear();
	}

	public String toString()
	{
		return ToStringHelper.toString(this, new String [] { "propertyResourceName", "propertyResourceUrl" });
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the property file name.
	 * @nowarn
	 */
	public String getPropertyResourceName()
	{
		return propertyResourceName;
	}

	/**
	 * Sets the property file name.
	 * @nowarn
	 */
	public void setPropertyResourceName(String propertyResourceName)
	{
		this.propertyResourceName = propertyResourceName;
	}

	/**
	 * Gets the URL of the property resource.
	 *
	 * @return The URL or null if it could not be determined
	 */
	public URL getPropertyResourceUrl()
	{
		URL url = null;
		try
		{
			if (propertyResourceName != null)
			{
				url = ResourceMgr.getDefaultInstance().getURL(propertyResourceName);
			}
		}
		catch (ResourceMgrException e)
		{
			// No properties file present, silently ignored
		}
		return url;
	}

	/**
	 * Gets the property file header.
	 * @nowarn
	 */
	public String getFileHeader()
	{
		return fileHeader;
	}

	/**
	 * Sets the property file header.
	 * @nowarn
	 */
	public void setFileHeader(String fileHeader)
	{
		this.fileHeader = fileHeader;
	}

	/**
	 * Gets the table of all properties.
	 * @nowarn
	 */
	public Properties getProperties()
	{
		return properties;
	}

	/**
	 * Sets the table of all properties.
	 * @nowarn
	 */
	public void setProperties(Properties properties)
	{
		this.properties = properties;
	}

	/**
	 * Gets the readonly flag.
	 * @nowarn
	 */
	public boolean isReadonly()
	{
		return readonly;
	}

	/**
	 * Sets the readonly flag.
	 * @nowarn
	 */
	public void setReadonly(boolean readonly)
	{
		this.readonly = readonly;
	}

	/**
	 * Gets the mandatory flag.
	 * @nowarn
	 */
	public boolean isMandatory()
	{
		return mandatory;
	}

	/**
	 * Sets the mandatory flag.
	 * @nowarn
	 */
	public void setMandatory(boolean mandatory)
	{
		this.mandatory = mandatory;
	}

	/**
	 * Gets the flag to save only properties that already exist in the property file.
	 * @nowarn
	 */
	public boolean isSaveExistingPropertiesOnly()
	{
		return saveExistingPropertiesOnly;
	}

	/**
	 * Sets the flag to save only properties that already exist in the property file.
	 * @nowarn
	 */
	public void setSaveExistingPropertiesOnly(boolean saveExistingPropertiesOnly)
	{
		this.saveExistingPropertiesOnly = saveExistingPropertiesOnly;
	}

	//////////////////////////////////////////////////
	// @@ SettingProvider implementation
	//////////////////////////////////////////////////

	/**
	 * Gets a setting value.
	 *
	 * @param name Name of the setting
	 * @return The setting value or null if the setting does not exist
	 */
	public Object getSetting(String name)
	{
		return properties.getProperty(name, null);
	}

	/**
	 * Sets a setting.
	 * Note that you have to call {@link #saveSettings} in order to write the settings
	 * back to the property file.
	 *
	 * @param name Name of the setting
	 * @param value Value of the setting
	 * @return
	 *		true	If the provider was able to save the setting.<br>
	 *		false	If the provider does not feel responsible for this setting or cannot save the setting.
	 */
	public boolean setSetting(String name, Object value)
	{
		Object oldValue = getSetting(name);

		if (saveExistingPropertiesOnly || readonly)
		{
			// Accept existing properties only
			if (oldValue == null)
				return false;
		}

		if (!CommonUtil.equalsNull(value, oldValue))
		{
			// Save only if changed

			if (value == null)
			{
				properties.remove(name);
			}
			else
			{
				properties.setProperty(name, value.toString());
			}
			dirty = true;
		}

		return true;
	}

	/**
	 * Loads the settings.
	 * Forces the provider to (re-)load the settings.
	 *
	 * @return
	 *		true	If the setting were successfully loaded.
	 *		false	If the settings could not be loaded.
	 */
	public boolean loadSettings()
	{
		dirty = false;

		if (propertyResourceName == null)
		{
			return true;
		}

		InputStream is = null;
		try
		{
			is = ResourceMgr.getDefaultInstance().openResource(propertyResourceName);
			properties.load(is);
		}
		catch (ResourceMgrException e)
		{
			// No properties file present
			if (mandatory)
			{
				throw new RuntimeException("Property resource '" + propertyResourceName + "' not found.", e);
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error reading property resource '" + propertyResourceName + "'.", e);
		}
		finally
		{
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (IOException e)
				{
					// Ignore
				}
			}
		}

		return true;
	}

	/**
	 * Saves the settings.
	 * Makes the provider to save the current setting values.<br>
	 * Note that some providers may save the setting values automatically if {@link #setSetting} is called.
	 * In this case, this method will do nothing and should return true.
	 *
	 * @return
	 *		true	If the setting were successfully saved or the provider is not save-capable.
	 *		false	If there was an error saving the properties.
	 */
	public boolean saveSettings()
	{
		if (readonly || !dirty)
			return true;

		if (propertyResourceName == null)
		{
			System.err.println("Missing property file name");
			return false;
		}

		Resource res = ResourceMgr.getDefaultInstance().getResource(propertyResourceName);

		FileOutputStream out = null;

		try
		{
			File file = res.getFile();
			out = new FileOutputStream(file);
			properties.store(out, fileHeader);
			out.flush();
		}
		catch (IOException e)
		{
			// Echo errors before logging is available to stderr
			System.err.println("Error writing property file '" + propertyResourceName + "':" + e.getMessage());
			return false;
		}
		finally
		{
			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (IOException e)
				{
				}
			}
		}

		dirty = false;
		return true;
	}
}
