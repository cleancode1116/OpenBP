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
package org.openbp.common.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.openbp.common.application.Application;
import org.openbp.common.logger.LogUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * The resource manager class provides a way of accessing resources bundled with the application.
 *
 * @author Heiko Erhardt
 */
public class ResourceMgr
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Assign a high priority for the user's HOME dir resource provider (25) */
	public static final int USERHOMEDIR_RESOURCE_PROVIDER_PRIO = 25;

	/** Assign a medium priority for the default root dir resource provider (50) */
	public static final int ROOTDIR_RESOURCE_PROVIDER_PRIO = 50;

	/** Assign a low priority for the default classpath resource provider (100) */
	public static final int CLASSPATH_RESOURCE_PROVIDER_PRIO = 100;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Resource loader */
	private ResourceLoader resourceLoader;

	/** Resource Resolver */
	private ResourceResolver resourceResolver;

	/** Singleton instance */
	private static ResourceMgr singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ResourceMgr()
	{
		this(new PathMatchingResourcePatternResolver());
	}

	/**
	 * Value constructor.
	 *
	 * @param resourceLoader Resource loader
	 */
	public ResourceMgr(ResourceLoader resourceLoader)
	{
		// Turn off the Jar file caching; prevents jar file lockup on Windows
		// Note that this looks rather cludgy and it in fact is.
		// Spring does not disable caching for ClassPathResources as opposed to UrlResources (see Spring bug SPR-1868).
		// In the JDK code, the defaultUseCaches member is a static variable, which is accessible through a non-static method, so we have to create a dummy URLConnection
		new URLConnection(null)
		{
			public void connect() throws IOException
			{
			}
		}.setDefaultUseCaches(false);

		this.resourceLoader = resourceLoader;

		// Initialize the resource resolver with application root dir and classpath providers.
		resourceResolver = new ResourceResolver();
		resourceResolver.addProvider(new ClasspathResourceProvider(CLASSPATH_RESOURCE_PROVIDER_PRIO, resourceLoader));
		if (Application.getRootDir() != null)
		{
			resourceResolver.addProvider(new FileResourceProvider(ROOTDIR_RESOURCE_PROVIDER_PRIO, resourceLoader, Application.getRootDir()));
		}

		String userHomeDir = System.getenv("HOME");
		if (userHomeDir != null)
		{
			resourceResolver.addProvider(new FileResourceProvider(USERHOMEDIR_RESOURCE_PROVIDER_PRIO, resourceLoader, userHomeDir));
		}
	}

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized ResourceMgr getDefaultInstance()
	{
		if (singletonInstance == null)
		{
			singletonInstance = new ResourceMgr();
		}
		return singletonInstance;
	}

	//////////////////////////////////////////////////
	// @@ Single resource access
	//////////////////////////////////////////////////

	/**
	 * Gets the specified resource.
	 * Merely constructs a resource object from the resource name.
	 *
	 * @param resourceLocation Resource location; this can be a resource name that is expected to be resolved by the underlying resource loader
	 * (e. g. "conf/MyResource.xml" that would be resolved to a component of a Jar file by a classpath-based loader)
	 * or an explicit resource url specification (e. g. "classpath:conf/MyResource.xml"). For details
	 * on this format, see the Spring reference guide.
	 * @return The resource; use Resource.exists to check if the resource really exists
	 * @throws ResourceMgrException If no such resource could be found
	 */
	public Resource getResource(String resourceLocation)
	{
		if (getResourceLoader() == null)
		{
			throw new ResourceMgrException("Resource loader not set.");
		}

		// the resource resolver allows to load resources from different sources like
		// directories, classpath, HTTP server, database, ...
		Resource res = getResourceResolver().getResource(resourceLocation);

		return res;
	}

	/**
	 * Opens an input stream to the specified resource.
	 *
	 * @param resourceLocation Resource location
	 * @return The input stream
	 * @throws ResourceMgrException If the resource does not exist
	 * @throws ResourceMgrException If no such resoure could be found
	 */
	public InputStream openResource(String resourceLocation) throws ResourceMgrException
	{
		Resource res = getResource(resourceLocation);

		InputStream is = null;
		try
		{
			is = res.getInputStream();
		}
		catch (IOException e)
		{
			String msg = LogUtil.error(getClass(), "Could not find resource  $0.", new Object [] { res.getDescription() });
			throw new ResourceMgrException(msg, e);
		}
		return is;
	}

	/**
	 * Loads the specified resource as array of bytes.
	 *
	 * @param resourceLocation Resource location
	 * @return The byte array that makes up the content of the resource
	 * @throws ResourceMgrException On read error or if the resource does not exist
	 */
	public byte [] loadByteResource(String resourceLocation) throws ResourceMgrException
	{
		Resource res = getResource(resourceLocation);
		return loadByteResource(res);
	}

	/**
	 * Loads the specified resource as array of bytes.
	 *
	 * @param res Resource
	 * @return The byte array that makes up the content of the resource
	 * @throws ResourceMgrException On read error or if the resource does not exist
	 */
	public byte [] loadByteResource(Resource res) throws ResourceMgrException
	{
		InputStream is = null;
		try
		{
			is = res.getInputStream();
		}
		catch (IOException e)
		{
			String msg = LogUtil.error(getClass(), "Could not find resource $0.", new Object [] { res.getDescription() });
			throw new ResourceMgrException(msg, e);
		}

		// Read in the bytes
		byte [] bytes = null;
		try
		{
			// We don't use Apache Common IOUtils to prevent dependancy on apache-common.io
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			byte [] buffer = new byte [4096];
			int n = 0;
			while ((n = is.read(buffer)) != -1)
			{
				os.write(buffer, 0, n);
			}

			bytes = os.toByteArray();
		}
		catch (IOException e)
		{
			String msg = LogUtil.error(getClass(), "Could not entirely read resource $0.", new Object [] { res.getDescription() });
			throw new ResourceMgrException(msg, e);
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (IOException e)
			{
				String msg = LogUtil.error(getClass(), "Could not close resource $0.", new Object [] { res.getDescription() });
				throw new ResourceMgrException(msg, e);
			}
		}

		return bytes;
	}

	//////////////////////////////////////////////////
	// @@ Resource lookup
	//////////////////////////////////////////////////

	/**
	 * Finds resources according to the given resource location pattern, preferably in the class path.
	 *
	 * @param resourceLocation Resource location pattern<br>
	 * This may specify either a file, jar url or classpath resource location, optionally using an ant-style search pattern.
	 * Examples:<br>
	 * "classpath*:META-INF/*-beans.xml" Will find all resources that end with "-beans.xml" in all class path entries (classes
	 * dirs or jar files)<br>
	 * "file:C:/Programs/MyProg/conf/*.xml" Will find all XML resources in the configuration directory of a program.<br>
	 * If no URL type has been prepended, "classpath*:" is assumed.
	 * @return The array of resources that has been found
	 * @throws ResourceMgrException On I/O errors
	 */
	public Resource [] findResources(String resourceLocation) throws ResourceMgrException
	{
		PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver(resourceLoader);
		int i = resourceLocation.indexOf(':');
		if (i < 0)
		{
			resourceLocation = "classpath*:" + resourceLocation;
		}
		try
		{
			Resource [] ret = matcher.getResources(resourceLocation);
			return ret;
		}
		catch (Exception e)
		{
			String msg = "Error during resource search for resource '" + resourceLocation + "'.";
			throw new ResourceMgrException(msg, e);
		}
	}

	/**
	 * Returns the URL of the provided resource.
	 * Merely constructs a resource object from the resource name.
	 *
	 * @param resourceLocation Resource location; this can be a resource name that is expected to be resolved by the underlying resource loader
	 * (e. g. "conf/MyResource.xml" that would be resolved to a component of a Jar file by a classpath-based loader)
	 * or an explicit resource url specification (e. g. "classpath:conf/MyResource.xml"). For details
	 * on this format, see the Spring reference guide.
	 * @return url to the specified filename
	 */
	public URL getURL(String resourceLocation)
	{
		Resource res = getResource(resourceLocation);
		URL url = null;
		try
		{
			url = res.getURL();
		}
		catch (IOException e)
		{
			String msg = LogUtil.error(getClass(), "Error during resource search for resource $0.", new Object [] { resourceLocation });
			throw new ResourceMgrException(msg, e);
		}

		return url;
	}

	//////////////////////////////////////////////////
	// @@ Configuration
	//////////////////////////////////////////////////

	/**
	 * Gets the resource loader.
	 * @nowarn
	 */
	public ResourceLoader getResourceLoader()
	{
		return resourceLoader;
	}

	/**
	 * Gets the value of the resourceResolver property.
	 * @return The property value
	 */
	public ResourceResolver getResourceResolver()
	{
		return resourceResolver;
	}
}
