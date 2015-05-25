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
package org.openbp.common.template;

import java.util.Map;

/**
 * Base class for file templates.
 * This class is used as base class for any file generator template.
 *
 * @author Heiko Erhardt
 */
public abstract class TemplateBase
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Output directory (equals the directory of the model the subject item belongs to) */
	private String outputDir;

	/** Template properties */
	private Map properties;

	/** Template engine that executes the template */
	private TemplateEngine engine;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public TemplateBase()
	{
	}

	//////////////////////////////////////////////////
	// @@ Abstract methods
	//////////////////////////////////////////////////

	/**
	 * Performs the template-based generation of the target file(s).
	 *
	 * @param templateArgs Template arguments provided by the caller or null
	 * @throws Exception On error
	 */
	public abstract void generate(Object [] templateArgs)
		throws Exception;

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Loads and executes the specified (sub) template.
	 *
	 * @param templateClassName Template name
	 * @param templateArgs Template arguments provided by the caller or null
	 * @throws TemplateException On error
	 */
	protected void callTemplate(String templateClassName, Object [] templateArgs)
	{
		engine.callTemplate(templateClassName, templateArgs);
	}

	/**
	 * Adds information about a file that was written to the list of generated files of the generator.
	 *
	 * @param fileName Fully qualified file name
	 * @param mimeType Mime type of the file
	 */
	public void addResultFileInfo(String fileName, String mimeType)
	{
		engine.addResultFileInfo(fileName, mimeType);
	}

	/**
	 * Gets the output directory (equals the directory of the model the subject item belongs to).
	 * @nowarn
	 */
	public String getOutputDir()
	{
		return outputDir;
	}

	/**
	 * Gets a property value by the key.
	 * @param key Key of the property
	 * @return Value object
	 */
	protected Object getProperty(Object key)
	{
		return properties.get(key);
	}

	/**
	 * Adds a property.
	 * @param key Property key
	 * @param value Property Value
	 */
	protected void setProperty(Object key, Object value)
	{
		properties.put(key, value);
	}

	/**
	 * Gets the template properties.
	 * @nowarn
	 */
	public Map getProperties()
	{
		return properties;
	}

	//////////////////////////////////////////////////
	// @@ Internal methods
	//////////////////////////////////////////////////

	/**
	 * Sets the template engine that executes the template.
	 * @nowarn
	 */
	void setEngine(TemplateEngine engine)
	{
		this.engine = engine;
	}

	/**
	 * Sets the template properties.
	 * @nowarn
	 */
	void setProperties(Map properties)
	{
		this.properties = properties;
	}

	/**
	 * Sets the output directory (equals the directory of the model the subject item belongs to).
	 * @nowarn
	 */
	void setOutputDir(String outputDir)
	{
		this.outputDir = outputDir;
	}
}
