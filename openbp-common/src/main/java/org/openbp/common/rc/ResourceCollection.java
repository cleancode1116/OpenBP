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
package org.openbp.common.rc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.openbp.common.logger.LogUtil;
import org.openbp.common.markup.XMLUtil;
import org.openbp.common.rc.text.TPlain;
import org.openbp.common.string.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implements the {@link ResourceCollection} interface.
 *
 * @author Andreas Putz
 */
public class ResourceCollection
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Property file extension */
	protected static final String RESOURCE_EXTENSION = ".xml";

	/** XML tag properties is the document root */
	public static final String TAG_RESOURCE = "resource";

	/** XML attribute for the root tag for preloading resource item generally */
	public static final String ATTRIBUTE_PRELOAD = "preload";

	/** XML tag property is a resource item */
	public static final String TAG_RESOURCE_ITEM = "item";

	/** XML tag property is a import of another resource */
	public static final String TAG_IMPORT = "import";

	/** XML attribute for the import tag to get the import resource (required) */
	public static final String ATTRIBUTE_IMPORT_RESOURCE = "resource";

	/** XML attribute for the import tag to get the container of the import resource (optional) */
	public static final String ATTRIBUTE_IMPORT_CONTAINER = "container";

	/** XML tag property is a resource group */
	public static final String TAG_GROUP = "group";

	/** XML attribute for the group tag getting the name */
	public static final String ATTRIBUTE_GROUP_NAME = "name";

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Path of the resource folder this resource resides in */
	private String resourcePath;

	/** Name of the resource */
	private String resourceName;

	/** Resource container name */
	protected String containerName;

	/** Locale */
	private Locale locale;

	/** Parent resource */
	protected ResourceCollection parentResourceCollection;

	/** Resource mgr */
	private ResourceCollectionMgr resourceCollectionMgr;

	/** Resource items */
	private Map resourceItems;

	/** Resource imports */
	private ArrayList resourceImports;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 * Determines the container and the parent resource.
	 *
	 * @param containerName Resource container name or null
	 * @param resourceName Name of the resource
	 * @param locale Locale
	 * @param resourceCollectionMgr Resource mgr
	 * @param stream Input stream of the resource XML file
	 * @throws IOException On i/o error
	 * @throws SAXException On XML error
	 */
	public ResourceCollection(String containerName, String resourceName, Locale locale, ResourceCollectionMgr resourceCollectionMgr, InputStream stream)
		throws IOException, SAXException
	{
		this.containerName = containerName;
		this.resourceName = resourceName;
		this.locale = locale;
		this.resourceCollectionMgr = resourceCollectionMgr;

		resourceName = StringUtil.normalizeDir(resourceName);

		String s = ResourceCollectionMgr.constructResourceCollectionLocalPath (containerName, locale, resourceName);
		int i = s.lastIndexOf (StringUtil.FOLDER_SEP_CHAR);
		resourcePath = s.substring(0, i);
		resourceName = s.substring(i + 1);

		resourceItems = new HashMap();

		readFromInput(stream);
	}

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/**
	 * Gets the path of the resource folder this resource resides in.
	 * @nowarn
	 */
	public String getResourcePath()
	{
		return resourcePath;
	}

	/**
	 * Gets the name of the resource.
	 *
	 * @return Resource name
	 */
	public String getName()
	{
		return resourceName;
	}

	/**
	 * Gets the resource container name.
	 * @nowarn
	 */
	public String getContainerName()
	{
		return containerName;
	}

	/**
	 * Gets the locale.
	 * @nowarn
	 */
	public Locale getLocale()
	{
		return locale;
	}

	/**
	 * Gets the parent resource.
	 * @nowarn
	 */
	public ResourceCollection getParentResource()
	{
		return parentResourceCollection;
	}

	/**
	 * Sets the parent resource.
	 * @nowarn
	 */
	public void setParentResource(ResourceCollection parentResourceCollection)
	{
		this.parentResourceCollection = parentResourceCollection;
	}

	/**
	 * Gets the locale resource manager.
	 * @nowarn
	 */
	public ResourceCollectionMgr getResourceCollectionMgr()
	{
		return resourceCollectionMgr;
	}

	//////////////////////////////////////////////////
	// @@ Resource interface implementation
	//////////////////////////////////////////////////

	/**
	 * Gets a resource value as string.
	 * Writes a log message if no such item exists.
	 *
	 * @param resourceItemName Name of the resource item to get
	 * @return The string value of the item or the resource item name if no such item exists
	 */
	public String getRequiredString(String resourceItemName)
	{
		Object o = getRequiredObject(resourceItemName);

		if (o != null)
		{
			// Found the resource item, return it after performing global replacements
			return performVariableReplacement(o);
		}

		// Nothing found; an error message has been written to the log.
		// Return the resource item name, so we can continue at least.
		return resourceItemName;
	}

	/**
	 * Gets a resource value.
	 * Writes a log message if no such item exists.
	 *
	 * @param resourceItemName Name of the resource item to get
	 * @return The string value of the item or null if no such item exists
	 */
	public Object getRequiredObject(String resourceItemName)
	{
		Object obj = getResourceObject(resourceItemName);
		if (obj == null)
		{
			if (parentResourceCollection != null)
			{
				obj = parentResourceCollection.getOptionalObject(resourceItemName);
			}

			if (obj == null)
			{
				LogUtil.error(getClass(), "Resource $0: Cannot find resource item $1.", getErrorName(), resourceItemName);
			}
		}
		return obj;
	}

	/**
	 * Gets a resource value as string.
	 *
	 * @param resourceItemName Name of the resource item to get
	 * @return The string value of the item or null if no such item exists
	 */
	public String getOptionalString(String resourceItemName)
	{
		Object o = getOptionalObject(resourceItemName);

		if (o != null)
		{
			// Found the resource item, return it after performing global replacements
			return performVariableReplacement(o);
		}

		return null;
	}

	/**
	 * Gets a resource value as string.
	 *
	 * @param resourceItemName Name of the resource item to get
	 * @param dflt Default value
	 * @return The string value of the item or the default value if no such item exists
	 */
	public String getOptionalString(String resourceItemName, String dflt)
	{
		Object o = getOptionalObject(resourceItemName);

		if (o != null)
		{
			// Found the resource item, return it after performing global replacements
			return performVariableReplacement(o);
		}

		return dflt;
	}

	private String performVariableReplacement(Object o)
	{
		// Found the resource item, return it after performing global replacements
		String result = o.toString();
		result = getResourceCollectionMgr().performVariableReplacement(result);
		return result;
	}

	/**
	 * Gets a resource value.
	 *
	 * @param resourceItemName Name of the resource item to get
	 * @return The string value of the item or null if no such item exists
	 */
	public Object getOptionalObject(String resourceItemName)
	{
		Object obj = getResourceObject(resourceItemName);
		if (obj == null)
		{
			if (parentResourceCollection != null && parentResourceCollection != this)
			{
				obj = parentResourceCollection.getOptionalObject(resourceItemName);
			}
		}
		return obj;
	}

	/**
	 * Gets a resource value.
	 *
	 * @param resourceItemName Name of the resource item to get
	 * @param dflt Default value
	 * @return The string value of the item or the default value if no such item exists
	 */
	public Object getOptionalObject(String resourceItemName, Object dflt)
	{
		Object obj = getResourceObject(resourceItemName);
		if (obj == null)
		{
			if (parentResourceCollection != null)
			{
				obj = parentResourceCollection.getOptionalObject(resourceItemName);
			}

			if (obj == null)
				obj = dflt;
		}
		return obj;
	}

	/**
	 * Adds the resource item to the resource.
	 *
	 * @param item Resource item
	 */
	public void addResourceItem(ResourceItem item)
	{
		resourceItems.put(item.getName(), item);
	}

	/**
	 * Add a imported resource to this resource.
	 *
	 * @param importedResourceCollection The imported resource
	 */
	public void addImport(ResourceCollection importedResourceCollection)
	{
		if (resourceImports == null)
			resourceImports = new ArrayList();

		if (!resourceImports.contains(importedResourceCollection))
			resourceImports.add(importedResourceCollection);
	}

	/**
	 * Get all resource keys.
	 *
	 * @return Key iterator
	 */
	public Iterator getKeys()
	{
		HashMap retVal = new HashMap();

		if (parentResourceCollection != null)
		{
			Iterator parentKeys = parentResourceCollection.getKeys();
			while (parentKeys.hasNext())
			{
				retVal.put(parentKeys.next(), Boolean.TRUE);
			}
		}

		for (Iterator keys = resourceItems.keySet().iterator(); keys.hasNext();)
		{
			retVal.put(keys.next(), Boolean.TRUE);
		}

		if (resourceImports != null)
		{
			Iterator it = resourceImports.iterator();
			while (it.hasNext())
			{
				ResourceCollection res = (ResourceCollection) it.next();
				Iterator keys = res.getKeys();
				while (keys.hasNext())
				{
					retVal.put(keys.next(), Boolean.TRUE);
				}
			}
		}

		return retVal.keySet().iterator();
	}

	/**
	 * Gets the resource item specified by its name.
	 *
	 * @param resourceItemName Resource item name
	 * @return The resource item or null if no such item exists
	 */
	public ResourceItem getResourceItem(String resourceItemName)
	{
		Object o = resourceItems.get(resourceItemName);

		if (o instanceof ResourceItem)
		{
			return (ResourceItem) o;
		}

		if (o == Boolean.FALSE)
		{
			// Boolean.FALSE => do not try to get it again
			return null;
		}

		// not found -> determine resource item - resource assignment
		if (resourceImports != null)
		{
			Iterator it = resourceImports.iterator();
			while (it.hasNext())
			{
				ResourceCollection importedResourceCollection = (ResourceCollection) it.next();

				ResourceItem item = importedResourceCollection.getResourceItem(resourceItemName);
				if (item != null)
				{
					addResourceItem(item);
					return item;
				}
			}
		}

		if (parentResourceCollection != null && parentResourceCollection != this)
		{
			ResourceItem item = parentResourceCollection.getResourceItem(resourceItemName);
			if (item != null)
				return item;
		}

		resourceItems.put(resourceItemName, Boolean.FALSE);
		return null;
	}

	/**
	 * Returns a string that identifies the resource in error messages.
	 * @nowarn
	 */
	public String getErrorName()
	{
		return getResourcePath() + "/" + getName();
	}

	/**
	 * Get an object from a resource.
	 *
	 * @param resourceItemName Resource item name
	 * @return Resource item value
	 */
	protected Object getResourceObject(String resourceItemName)
	{
		ResourceItem resourceItem = getResourceItem(resourceItemName);
		if (resourceItem != null)
		{
			return resourceItem.getObject();
		}

		return null;
	}

	//////////////////////////////////////////////////
	// @@ Resource parsing
	//////////////////////////////////////////////////

	/**
	 * Reads the DOM tree of a resource and adds all resource items it finds.
	 *
	 * @param stream XML input stream
	 *
	 * @throws IOException On I/O error
	 * @throws SAXException On XML error
	 */
	protected void readFromInput(InputStream stream)
		throws IOException, SAXException
	{
		InputSource source = new InputSource(stream);

		// Create a DOM document
		Document doc = XMLUtil.parseDocument(source);
		Element root = doc.getDocumentElement();

		boolean preload = Boolean.getBoolean(root.getAttribute(ResourceCollection.ATTRIBUTE_PRELOAD));

		parseResource(root, null, preload);
	}

	/**
	 * Parses the DOM tree of a resource and adds all resource items it finds.
	 *
	 * @param parentNode Parent node to parse
	 * @param groupPath Resource group name to prepend to all resource items or null
	 * @param preload
	 *		true	Preload any resource objects (images etc.)<br>
	 *		false	Load the resource objects only when accessing the resource item.
	 */
	protected void parseResource(Element parentNode, String groupPath, boolean preload)
	{
		// Get all children
		NodeList items = parentNode.getChildNodes();

		int nItems = items.getLength();
		for (int i = 0; i < nItems; i++)
		{
			// We check each child... if it is a resourceitem, we add it, else - if a group - we process the group
			if (!(items.item(i) instanceof Element))
				continue;

			Element child = (Element) items.item(i);
			String tagName = child.getTagName();

			if (tagName.equals(ResourceCollection.TAG_RESOURCE_ITEM))
			{
				// We have an item, add it
				ResourceItem item = createResourceItem(child, groupPath, preload);
				if (item != null)
				{
					addResourceItem(item);
				}
			}
			else if (tagName.equals(TAG_GROUP))
			{
				// We have a group, recurse...!
				String groupName = child.getAttribute(ATTRIBUTE_GROUP_NAME);
				String path = groupPath == null ? groupName : groupPath + ResourceItem.GROUP_DELIMITER + groupName;
				parseResource(child, path, preload);
			}
			else if (tagName.equals(TAG_IMPORT))
			{
				String importResourceName = child.getAttribute(ATTRIBUTE_IMPORT_RESOURCE);
				String importPBContainerName = child.getAttribute(ATTRIBUTE_IMPORT_CONTAINER);

				ResourceCollection importedResourceCollection = null;
				if (importPBContainerName != null)
					importPBContainerName = importPBContainerName.trim();
				if (importPBContainerName == null || importPBContainerName.length() == 0)
				{
					importPBContainerName = getContainerName();
				}
				importedResourceCollection = getResourceCollectionMgr().getResource(importPBContainerName, importResourceName, locale);

				if (importedResourceCollection != null)
				{
					addImport(importedResourceCollection);
				}

				// TODO Fix 4: Watch for import recursion here
			}
			else
			{
				LogUtil.error(getClass(), "Resource $0: Unexpected resource type $1.", getErrorName(), tagName);
			}
		}
	}

	/**
	 * Create a resource object.
	 *
	 * @param resourceItemNode Resource item xml element
	 * @param group Group of the item or null
	 * @param preload
	 *	true	The preload attribute of the resource file was set to true<br>
	 *	false	The preload attribute of the resource file was set to false
	 * @return Resource item or null on error
	 */
	protected ResourceItem createResourceItem(Element resourceItemNode, String group, boolean preload)
	{
		// Determine the mime-type
		String mimeType = resourceItemNode.getAttribute(ResourceItem.ATTRIBUTE_PROPERTY_TYPE);
		if (mimeType == null || mimeType.length() == 0)
			mimeType = "text/plain";

		// Determine the resource item
		ResourceItem resourceItem = null;
		try
		{
			// Instantiate the resource item according to the mime type
			if (mimeType.equals("text/plain"))
			{
				// Speed shortcut for plain text to avoid reflection
				resourceItem = new TPlain();
			}
			else
			{
				Class resourceItemClass = ResourceItemTypes.determineResourceItemClass(mimeType);

				if (resourceItemClass == null)
				{
					LogUtil.error(getClass(), "Resource $0: Unsupported resource mime type $1.", getErrorName(), mimeType);
					return null;
				}

				resourceItem = (ResourceItem) resourceItemClass.newInstance();
			}

			// Initialize the resource item
			resourceItem.initializeFromDOM(this, resourceItemNode, group);

			if (preload)
			{
				// Accessing the resource object will load it
				resourceItem.getObject();
			}
		}
		catch (IllegalAccessException e)
		{
			LogUtil.error(getClass(), "Resource $0: Access failed to the resource class for mime type $1.", getErrorName(), mimeType);
			return null;
		}
		catch (InstantiationException e)
		{
			LogUtil.error(getClass(), "Resource $0: Cannot instantiate resource class for mime type $1.", getErrorName(), mimeType);
			return null;
		}

		return resourceItem;
	}

	/**
	 * Reads the (byte data) content of a resource collection item.
	 *
	 * @param relativePath Relative path of the item resource
	 * @return Byte Array or null if not found
	 */
	public byte [] readResourceItem(String relativePath)
	{
		String path = getResourceItemPath(relativePath);

		byte [] data = getResourceCollectionMgr().getResourceMgr().loadByteResource(path);
		return data;
	}

	/**
	 * Determines the file path of a file within a container for error message output.
	 *
	 * @param relativePath Relative path of the file
	 * @return The path
	 */
	public String getResourceItemPath(String relativePath)
	{
		String path = getResourcePath() + StringUtil.FOLDER_SEP + relativePath;
		path = StringUtil.normalizePathName(path);
		return path;
	}
}
