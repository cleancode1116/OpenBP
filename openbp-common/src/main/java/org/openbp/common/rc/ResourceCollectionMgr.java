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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.openbp.common.resource.ResourceMgr;
import org.openbp.common.resource.ResourceMgrException;
import org.openbp.common.string.StringReplacer;
import org.xml.sax.SAXException;

/**
 * Manages resources ({@link ResourceCollection}).
 *
 * The class can be instantiated for usage within a particular context, but also provides a global
 * default instance that should be used for access to regular application resources.
 *
 * @author Andreas Putz
 */
public class ResourceCollectionMgr
{
	/** Default locale directory */
	public static final String RC_FOLDER = "rc";

	/** Default locale directory */
	public static final String DEFAULT_LOCALE = "default";

	/** Media file suffix */
	public static final String MEDIA_SUFFIX = ".rc.xml";

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/**
	 * Table mapping container-locale-resource names to {@link ResourceCollection} objects.
	 */
	private Map resourceTable;

	/** Global resource variable text replacements */
	private StringReplacer resourceVariableReplacer;

	/** Resource manager used to read the resource files */
	private ResourceMgr resourceMgr;

	/** Singleton instance */
	private static ResourceCollectionMgr singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public ResourceCollectionMgr()
	{
		resourceTable = new HashMap();
		resourceMgr = ResourceMgr.getDefaultInstance();
	}

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized ResourceCollectionMgr getDefaultInstance()
	{
		if (singletonInstance == null)
		{
			singletonInstance = new ResourceCollectionMgr();
		}
		return singletonInstance;
	}

	//////////////////////////////////////////////////
	// @@ Resource access
	//////////////////////////////////////////////////

	/**
	 * Gets the resource using the default locale.
	 *
	 * @param containerName Name of the resource container or null
	 * @param resourceName Name of the resource
	 * @return Resource object
	 */
	public ResourceCollection getResource(String containerName, String resourceName)
	{
		return getResource(containerName, resourceName, null);
	}

	/**
	 * Gets the resource for the given class using the default locale.
	 * The class name of the resource is used as resource name. Note that the prefix "org.openbp.",
	 * will be removed from the class name in order to shorten the resource file name.
	 *
	 * @param containerName Name of the resource container or null
	 * @param cls Class of the object requesting the resource
	 * @return Resource object
	 */
	public ResourceCollection getResource(String containerName, Class cls)
	{
		return getResource(containerName, cls, null);
	}

	/**
	 * Gets the resource for the given class using the given locale.
	 * The class name of the resource is used as resource name. Note that the prefix "org.openbp.",
	 * will be removed from the class name in order to shorten the resource file name.
	 *
	 * @param containerName Name of the resource container or null
	 * @param cls Class of the object requesting the resource
	 * @param locale Locale to use; If the locale is null, the default locale will be used
	 * @return Resource object
	 */
	public ResourceCollection getResource(String containerName, Class cls, Locale locale)
	{
		String name = cls.getName();
		if (name.startsWith("org.openbp."))
		{
			name = name.substring("org.openbp.".length());
		}

		return getResource(containerName, name, locale);
	}

	/**
	 * Gets the resource using the given locale.
	 *
	 * @param containerName Name of the resource container or null
	 * @param resourceName Name of the resource
	 * @param locale Locale to use; If the locale is null, the default locale will be used
	 * @return Resource object or null if no such resource was found
	 * @throws ResourceCollectionException if no such resource collection in any matching locale could be found
	 */
	public ResourceCollection getResource(String containerName, String resourceName, Locale locale)
	{
		if (locale == null)
		{
			locale = Locale.getDefault();
		}
		Locale initialLocale = locale;
		ResourceMgrException recentException = null;
		String key = null;
		
		ResourceCollection res = null;

		for (;;)
		{
			key = constructResourceCollectionLocalPath (containerName, locale, resourceName);

			Object o = resourceTable.get(key);

			if (o instanceof Boolean)
			{
				// This means resource not available
				break;
			}

			if (o instanceof ResourceCollection)
			{
				res = (ResourceCollection) o;
				break;
			}

			// Try to read resource
			try
			{
				InputStream stream = resourceMgr.openResource(key + MEDIA_SUFFIX);

				res = new ResourceCollection(containerName, resourceName, locale, this, stream);

				resourceTable.put(key, res);
			}
			catch (ResourceMgrException e)
			{
				// Resource doesn't exist, ignore and try next one in chain
				recentException = e;
			}
			catch (IOException e)
			{
				throw new ResourceCollectionException("I/O error reading resource collection '" + key + "'.", e);
			}
			catch (SAXException e)
			{
				throw new ResourceCollectionException("XML error reading resource collection '" + key + "'.", e);
			}

			if (locale == null)
				break;
			locale = determineParentLocale(locale);
		}

		if (res == null)
		{
			// Resource not available
			resourceTable.put(key, Boolean.FALSE);
		}

		if (locale != initialLocale)
		{
			// Save in cache unter initial locale, too
			String initialKey = constructResourceCollectionLocalPath (containerName, initialLocale, resourceName);
			if (res != null)
				resourceTable.put(initialKey, res);
			else
				resourceTable.put(initialKey, Boolean.FALSE);
		}
		
		if (res == null && recentException != null)
		{
			throw new ResourceCollectionException("Resource collection '" + containerName + "/" + resourceName + "' could not be found.", recentException);
		}

		return res;
	}

	/**
	 * Constructs the local path to a resource collection.
	 *
	 * @param containerName Container name or null
	 * @param locale Locale
	 * @param resourceName Resource name
	 * @return The path name
	 */
	public static String constructResourceCollectionLocalPath (String containerName, Locale locale, String resourceName)
	{
		StringBuffer sb = new StringBuffer(255);
		sb.append(RC_FOLDER);
		sb.append("/");

		if (containerName != null)
		{
			sb.append(containerName);
			sb.append("/");
		}

		String localeName = null;
		if (locale != null)
		{
			localeName = locale.toString();
		}
		else
		{
			localeName = DEFAULT_LOCALE;
		}
		sb.append(localeName);
		sb.append("/");

		sb.append(resourceName);

		return sb.toString();
	}

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/**
	 * Determines the parent locale of the given locale.
	 * Example: If the locale is 'de_DE' then is the parent locale 'de'.
	 *
	 * @param locale Locale to check
	 * @return The parent locale or null if the locale is a top-level locale
	 */
	public Locale determineParentLocale(Locale locale)
	{
		String variant = locale.getVariant();
		String country = locale.getCountry();
		String language = locale.getLanguage();

		Locale parentLocale = null;
		if (!variant.equals(""))
		{
			parentLocale = new Locale(language, country, "");
		}
		else if (!country.equals(""))
		{
			parentLocale = new Locale(language, "", "");
		}
		else if (!language.equals(""))
		{
			parentLocale = null;
		}

		return parentLocale;
	}

	//////////////////////////////////////////////////
	// @@ Text replacements
	//////////////////////////////////////////////////

	/**
	 * Adds a global resource text variable.
	 * The variable will be replaced for its value in every resource string (including image path names).
	 *
	 * @param pattern The pattern to search for
	 * @param replacement The substitute that will replace the pattern in the string
	 * or null to remove the pattern from the string
	 */
	public void addResourceVariable(String pattern, String replacement)
	{
		if (resourceVariableReplacer == null)
			resourceVariableReplacer = new StringReplacer();
		resourceVariableReplacer.addReplacement(pattern, replacement);
	}

	/**
	 * Performs any text replacements defined by {@link #addResourceVariable}.
	 *
	 * @param text Text to replace or null
	 * @return The result text or null
	 */
	public String performVariableReplacement(String text)
	{
		if (resourceVariableReplacer != null && text != null)
		{
			text = resourceVariableReplacer.process(text);
		}
		return text;
	}

	/**
	 * Gets the resource manager used to read the resource files.
	 * @nowarn
	 */
	public ResourceMgr getResourceMgr()
	{
		return resourceMgr;
	}
}
