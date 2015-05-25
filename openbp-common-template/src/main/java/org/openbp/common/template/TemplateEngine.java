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

import org.openbp.common.logger.LogUtil;
import org.openbp.common.template.writer.CancelException;

/**
 * Template engine.
 *
 * @author Heiko Erhardt
 */
public class TemplateEngine
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Output dir */
	private String outputDir;

	/**
	 * Table of generator properties that may be used in the template-based generation process.
	 * Maps property names to arbitrary objects.
	 */
	private Map properties;

	/** Current result file list */
	private TemplateEngineResult currentResult;

	/** Class loader used to load template classes */
	private ClassLoader classLoader;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public TemplateEngine()
	{
	}

	//////////////////////////////////////////////////
	// @@ Template processing
	//////////////////////////////////////////////////

	/**
	 * Processes the given template.
	 *
	 * @param templateClassName Name of the template to execute
	 * @param templateArgs Template arguments provided by the caller or null
	 * @return The result of the generation process or null if no files have been produced
	 * @throws Exception On error
	 */
	public TemplateEngineResult processTemplate(String templateClassName, Object [] templateArgs)
		throws Exception
	{
		currentResult = new TemplateEngineResult();
		callTemplate(templateClassName, templateArgs);
		return currentResult;
	}

	/**
	 * Loads and executes the specified (sub) template.
	 *
	 * @param templateClassName Template name
	 * @param templateArgs Template arguments provided by the caller or null
	 * @throws TemplateException On error
	 */
	protected void callTemplate(String templateClassName, Object [] templateArgs)
	{
		TemplateBase template = loadTemplateClass(templateClassName);

		template.setEngine(this);
		template.setOutputDir(outputDir);
		template.setProperties(properties);

		// Make the custom class loader active
		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(classLoader);

		try
		{
			template.generate(templateArgs);
		}
		catch (CancelException e)
		{
		}
		catch (RuntimeException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = LogUtil.error(getClass(), "Error in template class $0.", template.getClass().getName(), e);
			throw new TemplateException(msg, e);
		}
		finally
		{
			// Make the custom class loader active
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}

	/**
	 * Loads the specified template class.
	 * Searches the class in the template set hierarchy and tries to instantiate it.
	 *
	 * @param templateClassName Template name
	 * @return The new instance of the template class
	 * @throws TemplateException If the class was not found or could not be instantiated
	 */
	protected TemplateBase loadTemplateClass(String templateClassName)
	{
		try
		{
			// Try to load the class
			Class cls = null;
			try
			{
				cls = classLoader.loadClass(templateClassName);
			}
			catch (ClassNotFoundException e)
			{
				// Root level and still no class, give up
				String msg = LogUtil.error(getClass(), "Cannot find template class for template $0.", templateClassName, e);
				throw new TemplateException(msg, e);
			}

			// Check if it's really a template
			if (!TemplateBase.class.isAssignableFrom(cls))
			{
				String msg = LogUtil.error(getClass(), "Template class $0 is not a sub class of $1.", templateClassName, TemplateBase.class.getName());
				throw new TemplateException(msg);
			}

			TemplateBase template = (TemplateBase) cls.newInstance();
			return template;
		}
		catch (InstantiationException e)
		{
			String msg = LogUtil.error(getClass(), "Cannot instantiate template class $0.", templateClassName, e);
			throw new TemplateException(msg, e);
		}
		catch (IllegalAccessException e)
		{
			String msg = LogUtil.error(getClass(), "Cannot access template class $0.", templateClassName, e);
			throw new TemplateException(msg, e);
		}
	}

	//////////////////////////////////////////////////
	// @@ Various methods
	//////////////////////////////////////////////////

	/**
	 * Adds information about a file that was written to the list of generated files of the generator.
	 *
	 * @param fileName Fully qualified file name
	 * @param mimeType Mime type of the file
	 */
	public void addResultFileInfo(String fileName, String mimeType)
	{
		currentResult.add(fileName, mimeType);
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the output directory.
	 * @nowarn
	 */
	public String getOutputDir()
	{
		return outputDir;
	}

	/**
	 * Sets the output directory.
	 * @nowarn
	 */
	public void setOutputDir(String outputDir)
	{
		this.outputDir = outputDir;
	}

	/**
	 * Gets the table of generator properties that may be used in the template-based generation process.
	 * Maps property names to arbitrary objects.
	 * @return The properties or null if no properties have been defined
	 */
	public Map getProperties()
	{
		return properties;
	}

	/**
	 * Sets the table of generator properties that may be used in the template-based generation process.
	 * Maps property names to arbitrary objects.
	 * @param properties The properties or null if no properties have been defined
	 */
	public void setProperties(Map properties)
	{
		this.properties = properties;
	}

	/**
	 * Gets the class loader used to load template classes.
	 * @nowarn
	 */
	public ClassLoader getClassLoader()
	{
		return classLoader;
	}

	/**
	 * Sets the class loader used to load template classes.
	 * @nowarn
	 */
	public void setClassLoader(ClassLoader classLoader)
	{
		this.classLoader = classLoader;
	}
}
