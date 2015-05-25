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
package org.openbp.cockpit.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openbp.cockpit.CockpitConstants;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.application.Application;
import org.openbp.common.classloader.XClassLoader;
import org.openbp.common.classloader.XClassLoaderConfiguration;
import org.openbp.common.generic.description.DescriptionObjectImpl;
import org.openbp.common.generic.description.DisplayObjectImpl;
import org.openbp.common.generic.propertybrowser.CollectionDescriptor;
import org.openbp.common.generic.propertybrowser.ObjectDescriptor;
import org.openbp.common.generic.propertybrowser.PropertyDescriptor;
import org.openbp.common.io.xml.XMLDriver;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.resource.ResourceMgr;
import org.openbp.common.resource.ResourceMgrException;
import org.openbp.common.string.StringUtil;
import org.springframework.core.io.Resource;

/**
 * The generator manager maintains a list of generator descriptors.
 * This class is a singleton.<br>
 * Each descriptor defines a possible generation procedure for a particular item type
 * (see {@link Generator}.<br>
 * The descriptors are loaded from a the client's generator directory
 * ($OpenBP/cockpit/generator).<br>
 * The generators are grouped by the item type.
 *
 * @author Andreas Putz
 */
public final class GeneratorMgr
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** List with all generators (contains {@link Generator} objects */
	private List generatorList;

	/** Singleton instance */
	private static GeneratorMgr singletonInstance;

	/** Mapped classes for the xml driver */
	private static Class [] standardMappedClasses = { DescriptionObjectImpl.class, DisplayObjectImpl.class, CollectionDescriptor.class, ObjectDescriptor.class, PropertyDescriptor.class, Generator.class, GeneratorPageDescriptor.class, GeneratorProperty.class, GeneratorSettings.class };

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized GeneratorMgr getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new GeneratorMgr();
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private GeneratorMgr()
	{
		loadGenerators();
	}

	/**
	 * Reload all generator infos and template sets.
	 */
	public void reload()
	{
		loadGenerators();
	}

	//////////////////////////////////////////////////
	// @@ Public methods
	//////////////////////////////////////////////////

	/**
	 * Retrieves the specified generator.
	 *
	 * @param name Name of the generator as defined in the generator descriptor
	 * @return The generator or null if not found
	 */
	public Generator getGenerator(String name)
	{
		int n = generatorList.size();
		for (int i = 0; i < n; ++i)
		{
			Generator generator = (Generator) generatorList.get(i);

			if (generator.getName().equals(name))
				return generator;
		}

		return null;
	}

	/**
	 * Creates a sorted iterator of generators for a particular item type.
	 *
	 * @param itemType The item type or null to retrieve a list of all generators
	 * @param createItem
	 * true: Returns only generators that create an item of the specified type.<br>
	 * false: Returns generator that operate on an existing type.
	 * @return A list of {@link Generator} objects
	 */
	public List getGenerators(String itemType, boolean createItem)
	{
		if (itemType == null)
			return generatorList;

		int n = generatorList.size();
		if (n == 0)
			return null;

		List list = new ArrayList();
		for (int i = 0; i < n; i++)
		{
			Generator generator = (Generator) generatorList.get(i);
			if (itemType.equalsIgnoreCase(generator.getItemType()) && createItem == generator.isItemGenerator())
			{
				if (generator.checkPrecondition())
				{
					list.add(generator);
				}
			}
		}

		if (list.size() == 0)
			return null;

		Collections.sort(list);
		return list;
	}

	//////////////////////////////////////////////////
	// @@ Private methods
	//////////////////////////////////////////////////

	/**
	 * Loads all generator descriptors.
	 */
	private void loadGenerators()
	{
		// Load the standard mappings for the standard XML driver
		XMLDriver xmlDriver = XMLDriver.getInstance();
		try
		{
			xmlDriver.loadMappings(standardMappedClasses);
		}
		catch (XMLDriverException e)
		{
			ExceptionUtil.printTrace(e);
			return;
		}

		generatorList = new ArrayList();

		ClassLoader classLoader = getClass().getClassLoader();
		String classesDir = Application.getRootDir() + StringUtil.FOLDER_SEP + CockpitConstants.GENERATOR + StringUtil.FOLDER_SEP + "classes";
		if (new File(classesDir).isDirectory())
		{
			// Generator classes directory exists, set up class loader
			XClassLoaderConfiguration config = new XClassLoaderConfiguration();
			config.setName("Generator template class loader");
			config.setParentClassLoader(getClass().getClassLoader());
			config.addRepository(classesDir);
			try
			{
				classLoader = new XClassLoader(config);
			}
			catch (Exception e)
			{
				ExceptionUtil.printTrace(e);
				return;
			}
		}

		ResourceMgr resMgr = ResourceMgr.getDefaultInstance();
		String resourcePattern = CockpitConstants.GENERATOR + "/*.xml";
		Resource[] resources = null;

		try
		{
			resources = resMgr.findResources(resourcePattern);
		}
		catch (ResourceMgrException e)
		{
			return;
		}
		if (resources.length == 0)
			return;

		for (int i = 0; i < resources.length; i++)
		{
			Generator generator = null;
			try
			{
				generator = (Generator) xmlDriver.deserializeResource(Generator.class, resources[i]);
			}
			catch (XMLDriverException e)
			{
				ExceptionUtil.printTrace(e);
				continue;
			}
			generator.setGeneratorMgr(this);
			generator.setClassLoader(classLoader);

			// Create a generator-local XML driver and load the standard mappings
			XMLDriver generatorXmlDriver = new XMLDriver(classLoader);
			try
			{
				generatorXmlDriver.loadMappings(standardMappedClasses);
				generator.setXmlDriver(generatorXmlDriver);
			}
			catch (XMLDriverException e)
			{
				ExceptionUtil.printTrace(e);
				continue;
			}

			// Create the customizer if required
			try
			{
				generator.createCustomizer();
			}
			catch (Exception e)
			{
				ExceptionUtil.printTrace(e);
				continue;
			}

			// Load the XML files specified by the customizer
			try
			{
				// Load the mapping for specific generator settings defined by the customizer of the generator
				GeneratorCustomizer customizer = generator.getCustomizer();
				if (customizer != null)
				{
					Class [] dependentClasses = customizer.getDependentSettingsClasses();
					if (dependentClasses != null)
					{
						for (int iClasses = 0; iClasses < dependentClasses.length; ++iClasses)
						{
							generatorXmlDriver.loadMapping(dependentClasses [iClasses]);
						}
					}

					Class settingsClass = customizer.getSettingsClass();
					if (settingsClass != null)
					{
						generatorXmlDriver.loadMapping(settingsClass);
					}
				}
			}
			catch (XMLDriverException e)
			{
				ExceptionUtil.printTrace(e);
				continue;
			}

			// Successful, add it to the list
			generatorList.add(generator);
		}

		// Sort by XML loader sequence
		Collections.sort(generatorList, new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				int s1 = ((Generator) o1).getXmlLoaderSequenceNr();
				int s2 = ((Generator) o2).getXmlLoaderSequenceNr();
				return s1 - s2;
			}
		});
	}
}
