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
package org.openbp.common.generic.propertybrowser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openbp.common.ReflectUtil;
import org.openbp.common.io.xml.XMLDriver;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.resource.ResourceMgr;
import org.openbp.common.resource.ResourceMgrException;
import org.openbp.common.string.StringUtil;
import org.springframework.core.io.Resource;

/**
 * This singleton class manages {@link ObjectDescriptor} objects.
 *
 * The {@link ObjectDescriptorMgr} class accesses object descriptors for a particular class.
 * The descriptor is expected to reside in the same location as the class file of the class
 * (named "AbcProp.xml" if the class name is "Abc").
 * The manager will automatically locate and locate the object descriptor for this class.
 *
 * After loading the OD, the object descriptor manager will iterate the property list.
 * The manager will try to resolve attribute information of a property that is not set
 * (e. g. description, type name, editor class name) from the OD of the super class
 * or from the OD of its implemented interfaces.<br>
 * This means that for common attributes you only need to specify their property name
 * in the property descriptor list; the remaining information about the property will
 * be retrieved from the base class.
 *
 * In addition to the regular object descriptors, the manager also supports custom descriptors.
 * These descriptors may override the regular descriptors. The custom descriptors are grouped
 * in custom descriptor sets. A set is located in a sub directory of the custom descriptor directory
 * ({@link #setCustomDescriptorResourcePath}). Each descriptor file of the set must be named by the
 * fully qualified class name of the class to describe with the ending "Prop.xml" appended
 * (e. g. "org.openbp.common.generic.description.DisplayObjectProp.xml").<br>
 * A set can be activated by calling {@link #loadCustomDescriptorSet}.
 *
 * @author Heiko Erhardt
 */
public final class ObjectDescriptorMgr
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/**
	 * Flag for {@link #getDescriptor}: Throw error if descriptor not found.
	 * If set, the method will throw an exception if the descriptor could not be loaded.<br>
	 * Note that if loading has already been attempted in a previous call, the method will return null.
	 * If the descriptor file is invalid, an exception will be thrown in either case.
	 */
	public static final int ODM_THROW_ERROR = (1 << 0);

	/**
	 * Flag for {@link #getDescriptor}: Consider custom descriptors.
	 * If set, the method will use only regular descriptors.
	 * Otherwise, it will look for custom descriptors also
	 * (see {@link #setCustomDescriptorResourcePath}/{@link #loadCustomDescriptorSet}).
	 */
	public static final int ODM_EXCLUDE_CUSTOM = (1 << 1);

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/**
	 * Descriptor table.
	 * Maps classes (Class objects) to either {@link ObjectDescriptor} objects if the
	 * descriptor could be loaded or Boolean(false) objects if loading has failed
	 * in a previous loading attempt.
	 */
	private Map descriptorCache = new HashMap();

	/**
	 * Custom descriptor table.
	 * Maps classes (Class objects) to {@link ObjectDescriptor} objects for all classes
	 * that custom descriptors have been found for.
	 */
	private Map customDescriptors;

	/** Resource path to folder that may contain sub folders with custom descriptors */
	private String customDescriptorResourcePath;

	/** Singleton instance */
	private static ObjectDescriptorMgr singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized ObjectDescriptorMgr getInstance()
	{
		if (singletonInstance == null)
		{
			singletonInstance = new ObjectDescriptorMgr();
		}
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private ObjectDescriptorMgr()
	{
	}

	/**
	 * Gets the descriptor for a particular object class.
	 * The method will try to retrieve the descriptor for the specified class.
	 * If this fails, it will continue with the super class and the implemented interfaces.
	 * However, the method will check one level only, i. e. it will not traverse the
	 * super class of the interfaces etc.
	 *
	 * The manager expects the descriptor file to be located in the same directory as the class file
	 * of the object. The name of the descriptor file is constructed by appending "Prop.xml" to
	 * the name of the class.
	 *
	 * The manager will cache all loaded descriptors. It will try to load a descriptor only once.
	 * If loading failed, subsequent calls of getDescriptor for the same class will always return null.
	 *
	 * @param cls Class to load the mapping for
	 * @param flags Flags the control the way the method works ({@link #ODM_THROW_ERROR}|{@link #ODM_EXCLUDE_CUSTOM})
	 * @return The descriptor or null if no such descriptor exists
	 * @throws XMLDriverException If no descriptor file exists for this class
	 */
	public ObjectDescriptor getDescriptor(Class cls, int flags)
		throws XMLDriverException
	{
		// Clear the error flag for further operations
		int workFlags = flags & ~ODM_THROW_ERROR;

		// Try to load the specified class first
		ObjectDescriptor od = getDescriptorForClass(cls, workFlags);

		if (od == null)
		{
			// Failed; look for descriptors of interfaces implemented by this class directly
			Class [] interfaces = cls.getInterfaces();
			for (int i = 0; i < interfaces.length; ++i)
			{
				od = getDescriptorForClass(interfaces [i], workFlags);
				if (od != null)
					break;
			}

			if (od == null)
			{
				// Also failed, check the super class
				Class superClass = cls.getSuperclass();
				if (superClass != null)
				{
					od = getDescriptor(superClass, workFlags);
				}
			}
		}

		if (od == null && (flags & ODM_THROW_ERROR) != 0)
		{
			throw new XMLDriverException("Object descriptor resource not found for class '" + cls.getName() + "'");
		}

		if (od != null)
		{
			// Store descriptor in cache
			descriptorCache.put(cls, od);
		}
		else
		{
			// Set indicator that loading failed
			descriptorCache.put(cls, new Boolean(false));
		}

		return od;
	}

	/**
	 * Gets the descriptor for a particular object class.
	 *
	 * The manager expects the descriptor file to be located in the same directory as the class file
	 * of the object. The name of the descriptor file is constructed by appending "Prop.xml" to
	 * the name of the class.
	 *
	 * The manager will cache all loaded descriptors. It will try to load a descriptor only once.
	 * If loading failed, subsequent calls of getDescriptor for the same class will always return null.
	 *
	 * @param cls Class to load the mapping for
	 * @param flags Flags the control the way the method works ({@link #ODM_THROW_ERROR}|{@link #ODM_EXCLUDE_CUSTOM})
	 * @return The descriptor or null if no such descriptor exists
	 * @throws XMLDriverException If no descriptor file exists for this class
	 */
	private ObjectDescriptor getDescriptorForClass(Class cls, int flags)
		throws XMLDriverException
	{
		String className = cls.getName();
		if (className.startsWith("java."))
		{
			// There are no descriptors for standard classes
			return null;
		}

		ObjectDescriptor descriptor = null;

		// Search for a custom descriptor if desired and present first
		if (customDescriptors != null && (flags & ODM_EXCLUDE_CUSTOM) == 0)
		{
			descriptor = (ObjectDescriptor) customDescriptors.get(cls);
			if (descriptor != null)
			{
				// Resolve any property information that needs to be retrieved
				// from the super class or interfaces of the object class.
				resolveProperties(descriptor, cls);

				return descriptor;
			}
		}

		// Check if the descriptor is already in the cache
		Object o = descriptorCache.get(cls);
		if (o instanceof ObjectDescriptor)
		{
			// Yes
			return (ObjectDescriptor) o;
		}
		if (o instanceof Boolean)
		{
			// Previous loading attempt failed, don't retry
			return null;
		}

		int index = className.lastIndexOf('.');
		String descriptorFileName = className.substring(index + 1);
		descriptorFileName += "Prop.xml";

		InputStream is = cls.getResourceAsStream(descriptorFileName);
		if (is == null)
		{
			// Set indicator that loading failed
			descriptorCache.put(cls, new Boolean(false));

			if ((flags & ODM_THROW_ERROR) != 0)
			{
				throw new XMLDriverException("Object descriptor resource '" + descriptorFileName + "' not found for class '" + className + "'");
			}
			return null;
		}

		// Load the mapping from the resource
		try
		{
			descriptor = (ObjectDescriptor) XMLDriver.getInstance().deserializeStream(ObjectDescriptor.class, is);
		}
		catch (XMLDriverException pe)
		{
			// Set indicator that loading failed
			descriptorCache.put(cls, new Boolean(false));

			throw new XMLDriverException("Error loading object descriptor resource for class '" + className + "'", pe);
		}

		// Set the class and class name in the descriptor
		descriptor.setObjectClass(cls);
		descriptor.setObjectClassName(cls.getName());

		// Store descriptor in cache
		descriptorCache.put(cls, descriptor);

		// Resolve any property information that needs to be retrieved
		// from the super class or interfaces of the object class.
		resolveProperties(descriptor, cls);

		return descriptor;
	}

	/**
	 * Clears the cache of the node structure manager.
	 */
	public void clearCache()
	{
		descriptorCache.clear();
	}

	//////////////////////////////////////////////////
	// @@ Custom descriptors
	//////////////////////////////////////////////////

	/**
	 * Gets the resource path to folder that may contain sub folders with custom descriptors.
	 * @nowarn
	 */
	public String getCustomDescriptorResourcePath()
	{
		return customDescriptorResourcePath;
	}

	/**
	 * Sets the resource path to folder that may contain sub folders with custom descriptors.
	 * @nowarn
	 */
	public void setCustomDescriptorResourcePath(String customDescriptorResourcePath)
	{
		this.customDescriptorResourcePath = customDescriptorResourcePath;
	}

	/**
	 * Loads the specified custom descriptor set or clears the set.
	 * The set is loaded from the sub directory with the specified name in the directory
	 * specified by {@link #setCustomDescriptorResourcePath}.<br>
	 * This method will always clear the object descriptor cache.
	 * @param customDescriptorSetName The set name or null if only the regular descriptors should be used
	 * @throws XMLDriverException If the specified set or the set directory does not exist
	 */
	public void loadCustomDescriptorSet(String customDescriptorSetName)
		throws XMLDriverException
	{
		clearCache();
		customDescriptors = null;

		if (customDescriptorResourcePath == null)
		{
			// No custom descriptors
			return;
		}
		customDescriptorResourcePath = StringUtil.normalizeDir(customDescriptorResourcePath);

		// First scan the custom descirptor directory for generic global custom object descriptor files
		loadCustomDescriptors(customDescriptorResourcePath);

		if (customDescriptorSetName != null)
		{
			// First scan the custom descirptor directory for generic set-specific custom object descriptor files
			loadCustomDescriptors(customDescriptorResourcePath + StringUtil.FOLDER_SEP + customDescriptorSetName);
		}
	}

	/**
	 * Loads custom descriptors from the specified resource path.
	 * @param resourcePath ResourcePath of the resource folder to load the descriptors from
	 * @throws XMLDriverException If the specified directory does not exist
	 */
	public void loadCustomDescriptors(String resourcePath)
		throws XMLDriverException
	{
		ResourceMgr resMgr = ResourceMgr.getDefaultInstance();
		String resourcePattern = resourcePath + "/*.xml";
		Resource[] resources = null;

		try
		{
			resources = resMgr.findResources(resourcePattern);
		}
		catch (ResourceMgrException e)
		{
			String msg = LogUtil.error(getClass(), "No custom object descriptors found in resource location $0.", resourcePath);
			throw new XMLDriverException(msg);
		}

		if (resources.length == 0)
		{
			// No descriptors present
			return;
		}

		XMLDriver xmlDriver = XMLDriver.getInstance();

		// Load the descriptors from the descriptor directory
		for (int i = 0; i < resources.length; i++)
		{
			ObjectDescriptor descriptor = null;
			String resourceName = resources[i].getDescription().toString();

			// Load the descriptor
			try
			{
				descriptor = (ObjectDescriptor) xmlDriver.deserializeFile(ObjectDescriptor.class, resourceName);
			}
			catch (XMLDriverException e)
			{
				LogUtil.error(getClass(), "Error loading custom object descriptor $0.", resourceName, e);
				continue;
			}

			// Load the descriptor
			try
			{
				descriptor = (ObjectDescriptor) xmlDriver.deserializeResource(ObjectDescriptor.class, resources[i]);
			}
			catch (XMLDriverException e)
			{
				LogUtil.error(getClass(), "Error loading custom object descriptor $0.", resourceName, e);
				continue;
			}

			// Mark this descriptor as a custom descriptor
			descriptor.setCustomDescriptor(true);

			String className = descriptor.getObjectClassName();
			Class cls = ReflectUtil.loadClass(className);
			if (cls == null)
			{
				LogUtil.error(getClass(), "Can't find class $0 specified in custom object descriptor file $1.", className, resourceName);
				continue;
			}

			// Success, add it to the table
			if (customDescriptors == null)
				customDescriptors = new HashMap();
			customDescriptors.put(cls, descriptor);
		}
	}

	//////////////////////////////////////////////////
	// @@ Property resolving
	//////////////////////////////////////////////////

	/**
	 * Resolves any property information that needs to be retrieved
	 * from the super class or interfaces of the object class.
	 *
	 * If one of the the property descriptors is not complete, the method
	 * scans the property descriptors of the super class or the implemented
	 * interfaces of the object class for the missing information.<br>
	 * The property descriptor is not considered complete if either the<br>
	 * - display name<br>
	 * - description<br>
	 * - type class<br>
	 * are missing.<br>
	 * In this case, all members of the property descriptor that are null
	 * will be copied from the property descriptor of a base class.
	 * @param descriptor The object descriptor
	 * @param cls The object descriptor
	 * @throws XMLDriverException If one of the base descriptor files is invalid
	 */
	private void resolveProperties(ObjectDescriptor descriptor, Class cls)
		throws XMLDriverException
	{
		if (descriptor.isPropertiesResolved())
			return;

		// List of base descriptors (contains {@link ObjectDescriptor} objects)
		List baseDescriptors = null;
		boolean baseDescriptorsBuilt = false;

		// First, check if we need to get the validator from the base class
		if (descriptor.getValidatorClassName() == null)
		{
			baseDescriptors = buildBaseDescriptorList(cls, descriptor.isCustomDescriptor());
			baseDescriptorsBuilt = true;

			if (baseDescriptors != null)
			{
				int n = baseDescriptors.size();
				for (int i = 0; i < n; ++i)
				{
					ObjectDescriptor baseDescriptor = (ObjectDescriptor) baseDescriptors.get(i);

					if (baseDescriptor.getValidatorClassName() != null)
					{
						// Use the first base class validator we can find
						descriptor.setValidatorClassName(baseDescriptor.getValidatorClassName());
						descriptor.setValidatorClass(baseDescriptor.getValidatorClass());
						break;
					}
				}
			}
		}

		// Now check the property informations
		for (Iterator it = descriptor.getProperties(); it.hasNext();)
		{
			PropertyDescriptor pd = (PropertyDescriptor) it.next();

			// We consider a property incomplete if the display name or the description or the property content is not present
			boolean incomplete = pd.getDisplayName() == null || pd.getDescription() == null || (pd.getEditorClassName() == null && pd.getComplexProperty() == null && pd.getCollectionDescriptor() == null);

			if (incomplete)
			{
				if (!baseDescriptorsBuilt)
				{
					baseDescriptors = buildBaseDescriptorList(cls, descriptor.isCustomDescriptor());
					baseDescriptorsBuilt = true;
				}

				boolean foundBaseProperty = false;
				if (baseDescriptors != null)
				{
					String name = pd.getName();

					// Search the base descriptors for the incomplete property
					int n = baseDescriptors.size();
					for (int i = 0; i < n; ++i)
					{
						ObjectDescriptor baseDescriptor = (ObjectDescriptor) baseDescriptors.get(i);

						PropertyDescriptor pdBase = baseDescriptor.getProperty(name);
						if (pdBase != null)
						{
							// We found it, copy all non-null fields
							pd.copyNonNull(pdBase);
							foundBaseProperty = true;
							break;
						}
					}
				}

				if (!foundBaseProperty)
				{
					// Remove incomplete properties
					it.remove();
				}
			}
		}

		descriptor.setPropertiesResolved(true);
	}

	/**
	 * Builds a list of base descriptors of the specified class.
	 * The list includes descriptor for the super class and for the implemented interfaces
	 * of the class, if present.
	 *
	 * @param cls Class
	 * @param includeSelf
	 *		true	Search the (non-custom) descriptor of the class itself first<br>
	 *		false	Start with the super class of the given class
	 * @return The list of base descriptors (contains {@link ObjectDescriptor} objects)
	 * @throws XMLDriverException If one of the base descriptor files is invalid
	 */
	private List buildBaseDescriptorList(Class cls, boolean includeSelf)
		throws XMLDriverException
	{
		List baseDescriptors = null;

		if (includeSelf)
		{
			// Make sure to exclude custom descriptors or this is likely to recurse endless...
			ObjectDescriptor od = getDescriptor(cls, ODM_EXCLUDE_CUSTOM);
			if (od != null)
			{
				if (baseDescriptors == null)
					baseDescriptors = new ArrayList();
				baseDescriptors.add(od);
			}
		}

		// Get super class descriptors
		Class superClass = cls.getSuperclass();
		if (superClass != null)
		{
			ObjectDescriptor od = getDescriptor(superClass, 0);
			if (od != null)
			{
				if (baseDescriptors == null)
					baseDescriptors = new ArrayList();
				baseDescriptors.add(od);
			}
		}

		// Get interface descriptors
		Class [] interfaces = cls.getInterfaces();
		for (int i = 0; i < interfaces.length; ++i)
		{
			ObjectDescriptor od = getDescriptor(interfaces [i], 0);
			if (od != null)
			{
				if (baseDescriptors == null)
					baseDescriptors = new ArrayList();
				baseDescriptors.add(od);
			}
		}

		return baseDescriptors;
	}
}
