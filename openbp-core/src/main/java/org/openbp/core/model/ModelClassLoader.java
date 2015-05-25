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
package org.openbp.core.model;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.openbp.common.classloader.XClassLoader;
import org.openbp.common.classloader.XClassLoaderConfiguration;
import org.openbp.core.CoreConstants;

/**
 * Class loader for classes that belong to a model.
 * The class loader will look for the class or resource in the 'classes' directory
 * or in the jars of the 'lib' directory of the model. If the class could not be found,
 * the loading request will be deferred to the class loaders of the imported models.
 *
 * @author Heiko Erhardt
 */
public class ModelClassLoader extends XClassLoader
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Model the loader belongs to */
	private final Model model;

	/** Cache the system model */
	private ClassLoader systemModelLoader;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param config The configuration to be used for the class loader
	 * @param model Model the loader belongs to
	 * @throws Exception On any error that occurs while scanning the repositories specified in the class loader configuration
	 */
	public ModelClassLoader(XClassLoaderConfiguration config, Model model)
		throws Exception
	{
		super(config);
		this.model = model;

		if (! model.getQualifier().equals(CoreConstants.SYSTEM_MODEL_QUALIFIER))
		{
			Model systemModel = model.getModelMgr().getModelByQualifier(CoreConstants.SYSTEM_MODEL_QUALIFIER);
			if (systemModel != null)
			{
				systemModelLoader = systemModel.getClassLoader();
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ ClassLoader overrides
	//////////////////////////////////////////////////

	/**
	 * Loads a class using the system class loader.
	 *
	 * @param className Name of the class
	 * @param resolve If true, all dependents are loaded too
	 * @return The class object
	 * @exception ClassNotFoundException If the class was not found
	 */
	public Class loadClass(String className, boolean resolve)
		throws ClassNotFoundException
	{
		Class cls;

		cls = loadClassFromPath(className, resolve);
		if (cls != null)
			return cls;

		// Check the imports if not found in System model
		List importedModels = model.getImportedModelList();
		if (importedModels != null)
		{
			int n = importedModels.size();
			for (int i = 0; i < n; ++i)
			{
				Model importedModel = (Model) importedModels.get(i);

				ClassLoader loader = importedModel.getClassLoader();
				try
				{
					cls = loader.loadClass(className);
					if (cls != null)
						return cls;
				}
				catch (ClassNotFoundException e)
				{
					// Ignore
				}
			}
		}

		if (systemModelLoader != null)
		{
			// Try the system model
			try
			{
				cls = systemModelLoader.loadClass(className);
				if (cls != null)
					return cls;
			}
			catch (ClassNotFoundException e)
			{
				// Ignore
			}
		}

		// Note that this will throw a ClassNotFoundException on error!
		return findSystemClass(className);
	}

	/**
	 * Determines the URL of a resource with a given name.
	 * This method returns null if no
	 * resource with this name is found.
	 *
	 * @param name Name of the desired resource
	 * @return The URL or null
	 */
	public URL getResource(String name)
	{
		URL url = getResourceFromPath(name);
		if (url != null)
			return url;

		// Check the imports if not found in System model
		List importedModels = model.getImportedModelList();
		if (importedModels != null)
		{
			int n = importedModels.size();
			for (int i = 0; i < n; ++i)
			{
				Model importedModel = (Model) importedModels.get(i);

				ClassLoader loader = importedModel.getClassLoader();
				url = loader.getResource(name);
				if (url != null)
					return url;
			}
		}

		if (systemModelLoader != null)
		{
			// Try the system model
			url = systemModelLoader.getResource(name);
			if (url != null)
				return url;
		}

		return null;
	}

	/**
	 * Finds a resource with a given name.
	 * This method returns null if no
	 * resource with this name is found.
	 *
	 * @param name Name of the desired resource
	 * @return An input stream to the resource or null
	 */
	public InputStream getResourceAsStream(String name)
	{
		InputStream stream = loadResourceFromPath(name);
		if (stream != null)
			return stream;

		// Check the imports if not found in System model
		List importedModels = model.getImportedModelList();
		if (importedModels != null)
		{
			int n = importedModels.size();
			for (int i = 0; i < n; ++i)
			{
				Model importedModel = (Model) importedModels.get(i);

				ClassLoader loader = importedModel.getClassLoader();
				stream = loader.getResourceAsStream(name);
				if (stream != null)
					return stream;
			}
		}

		if (systemModelLoader != null)
		{
			// Try the system model
			stream = systemModelLoader.getResourceAsStream(name);
			if (stream != null)
				return stream;
		}

		return null;
	}
}
