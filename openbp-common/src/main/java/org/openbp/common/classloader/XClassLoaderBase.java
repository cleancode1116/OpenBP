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
package org.openbp.common.classloader;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.openbp.common.MsgFormat;
import org.openbp.common.logger.LogLevel;
import org.openbp.common.string.StringUtil;

/**
 * This class implements a sophisticated classloader that supports
 * loading from classes from various repositories (which might be
 * \.jar, \.zip-files or directories) and a caching mechanism for
 * loaded classes.
 *
 * To avoid multi-threading problems, this class loader is immutable after having
 * been constructed. This means, all configuration data necessary is passed as
 * argument to the constructor, and changing the configuration object
 * (see {@link XClassLoaderConfiguration}) after being passed to the constructor
 * doesn't influence the class loader any more (which is achieved by cloning the
 * configuration). Consequentially, the configuration can and should be reused.
 *
 * In order to support a broad field of applications for this class,
 * it is kept free of specific logging code, instead, own logging levels together
 * with an abstract logging method are used to allow easy integration of
 * the appropriate logging mechanism.
 *
 * Implementation note: Actually, the class loader wouldn't have to extend URLClassLoader.
 * This is done only to make the Tomcat Jasper JSP engine happy, which hard-wiredly expects an URLClassLoader.
 *
 * @author Heiko Erhardt
 */
public abstract class XClassLoaderBase extends URLClassLoader
{
	//////////////////////////////////////////////////
	// @@ Log level definitions
	//////////////////////////////////////////////////

	/** Class loading time debug flag */
	public static boolean DEBUG_LOAD_TIME = false;

	private static URL [] noUrls = new URL [0];

	//////////////////////////////////////////////////
	// @@ Private data
	//////////////////////////////////////////////////

	/** A clone of the configuration passed during construction of the class loader. */
	protected XClassLoaderConfiguration configuration;

	/** The class cache (maps class names to Class objects). */
	private Hashtable classCache;

	/**
	 * Table mapping resource names (i\.e\. class names) to {@link ResourceEntry} objects (which may specify a file-
	 * or zip-based resource).
	 */
	private Map entries;

	/** The time spent while loading classes. */
	private long loadClassTime;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param configuration The configuration to be used for this class loader. Must not be null.
	 * @throws Exception On any error that occurs while scanning the repositories specified in the class loader configuration
	 */
	public XClassLoaderBase(XClassLoaderConfiguration configuration)
		throws Exception
	{
		super(noUrls);
		init(configuration);
	}

	/**
	 * Protected constructor. Should only be used by subclasses that need to take essential
	 * actions before starting with the configuration processing. If this constructor was used,
	 * it is absolute necessary to call {@link #init}!
	 */
	protected XClassLoaderBase()
	{
		super(noUrls);
	}

	/**
	 * This method initializes the class loader with the configuration passed.
	 *
	 * @param configuration The configuration to be used for this class loader. Must not be null.
	 * @throws Exception On any error that occurs while scanning the repositories specified in the class loader configuration
	 */
	protected void init(XClassLoaderConfiguration configuration)
		throws Exception
	{
		// Construction without configuration is not possible.
		if (configuration == null)
		{
			throw new IllegalArgumentException("Creation of classloader without configuration is not supported.");
		}

		// Clone configuration to keep the classloader's configuration immutable.
		this.configuration = (XClassLoaderConfiguration) configuration.clone();

		// Initialize the logger
		setupLogger();

		if (isLogEnabled(LogLevel.INFO))
		{
			configuration.printDebug(this);
		}

		// Set up internal data structures.
		classCache = new Hashtable(1000);
		entries = new HashMap(1000);

		// Scan repositories for classes.
		scanRepositories();
	}

	/**
	 * Clears all information about classes in the path and rescans the specified class path. This
	 * method should be called if a class file was added to a directory of the class path
	 * programatically (e. g. by an invoked compilation or byte code construction).
	 * @throws Exception On any error that occurs while scanning the repositories specified in the class loader configuration
	 */
	public void rescan()
		throws Exception
	{
		// Clear lists of classes found.
		entries.clear();

		// Scan repositories again.
		scanRepositories();
	}

	/**
	 * This returns the parent class loader of this class loader
	 *
	 * @return The parent class loader or null if no explicit parent class loader has been specified when creating this class loader
	 */
	public ClassLoader getParentClassLoader()
	{
		return configuration.getParentClassLoader();
	}

	/**
	 * Gets the a clone of the configuration passed during construction of the class loader..
	 * @nowarn
	 */
	public XClassLoaderConfiguration getConfiguration()
	{
		return configuration;
	}

	/**
	 * Gets a resource entry by its key.
	 *
	 * @param key Key
	 * @return The entry or null if no such entry exists
	 */
	public ResourceEntry getResourceEntry(String key)
	{
		return (ResourceEntry) entries.get(key);
	}

	/**
	 * Adds a resource entry to the class loader.
	 *
	 * @param key Key of the entry (e. g. a fully qualified class name)
	 * @param entry Entry to add
	 */
	public void addResourceEntry(String key, ResourceEntry entry)
	{
		entries.put(key, entry);
	}

	/**
	 * Adds the given resource entry as class mapping of the entry specifies a class file.
	 * Does nothing if the entry does not denote a class file.
	 *
	 * @param resourceName Resource name (relative path name or zip entry name)
	 * @param entry Entry to add
	 */
	protected void addResourceEntryForClass(String resourceName, ResourceEntry entry)
	{
		// For class files, store a mapping of the class name
		int index;
		if ((index = resourceName.indexOf(".class")) >= 0)
		{
			String className = resourceName.substring(0, index);
			className = className.replace('/', '.');
			className = className.replace('\\', '.');
			addResourceEntry(className, entry);
		}
	}

	//////////////////////////////////////////////////
	// @@ ClassLoader overrides
	//////////////////////////////////////////////////

	/**
	 * Overrides the loadClass method from the standard java class loader.
	 *
	 * @param className Name of the class to be loaded
	 * @param resolve If true, all dependents are loaded too
	 * @return The class object
	 * @throws ClassNotFoundException If the class was not found
	 */
	public Class loadClass(String className, boolean resolve)
		throws ClassNotFoundException
	{
		Class result = null;

		// Get current time.
		long time = 0;
		if (DEBUG_LOAD_TIME && isLogEnabled(LogLevel.DEBUG))
		{
			time = System.currentTimeMillis();
		}

		// Try to find class in our own repositories...
		Class cls = loadClassFromPath(className, resolve);

		// ...if found...
		if (cls != null)
		{
			// ...return it.
			result = cls;
		}
		else
		{
			// ...or give the System class loader a chance.
			// Note that this will throw a ClassNotFoundException on error!
			result = findSystemClass(className);
		}

		// Log profiling info.
		if (DEBUG_LOAD_TIME && isLogEnabled(LogLevel.DEBUG))
		{
			// Calculate time we've spent in the method.
			time = System.currentTimeMillis() - time;

			// Add to load class time.
			loadClassTime += time;

			log(LogLevel.DEBUG, "loadClass('" + className + "') took " + time + " ms " + "(accumulated: " + loadClassTime + " ms)");
		}

		// Return the result.
		return result;
	}

	/**
	 * Determines the URL of a resource with a given name.
	 * This method returns null if no resource with this name is found.
	 *
	 * @param name Name of the desired resource
	 * @return The URL or null
	 */
	public URL getResource(String name)
	{
		return getResourceFromPath(name);
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
		return loadResourceFromPath(name);
	}

	//////////////////////////////////////////////////
	// @@ Class loading
	//////////////////////////////////////////////////

	/**
	 * Loads a class from a directory or zip file in the class path of the custom class loader.
	 *
	 * @param className Fully qualified name of the class to be loaded
	 * @param resolve If true, classes referenced by the class should be loaded, too
	 * @return The loaded class object or null if the class could not be loaded
	 */
	protected synchronized Class loadClassFromPath(String className, boolean resolve)
	{
		// Synchronize in order to prevent cache access conflicts.
		// Note that we may not synchronize on classCache (results in deadlock with synchronized(this) in parent class)!

		// Search the class cache first.
		Class cls = loadClassFromCache(className);
		if (cls != null)
		{
			return cls;
		}

		// Determine, whether we should try to load the class using the standard mechanism first.
		boolean standardFirst = (configuration.getTryStandardClassLoaderFirst() || configuration.getStandardPackages().containsClass(className)) && !configuration.getNonStandardPackages().containsClass(className);

		// Depending on whether we should try the standard mechanism first...
		if (standardFirst)
		{
			// First: Try loading using standard mechanism.
			cls = tryStandardLoaders(className);
			if (cls != null)
				return cls;

			// Second: Try loading from our own repositories.
			cls = tryRepositories(className, resolve);
			if (cls != null)
				return cls;
		}
		else
		{
			// First: Try loading from our own repositories.
			cls = tryRepositories(className, resolve);
			if (cls != null)
				return cls;

			// Second: Try loading usings standard mechanism.
			cls = tryStandardLoaders(className);
			if (cls != null)
				return cls;
		}

		// Log that the class couldn't be found.
		log(LogLevel.DEBUG, "Unresolved class: {0}", className);

		return null;
	}

	/**
	 * This method tries to find the class given by the passed name in any of our repositories.
	 * On successful load, the class is added to the cache. However, synchronization on the class
	 * cache access shouldn't be necessary here, because this method is invoked only with acquired
	 * lock on the cache.
	 *
	 * @param className The name of the class to be found
	 * @param resolve If true, any classes referenced by the found class should also be loaded
	 * @return Class
	 */
	private Class tryRepositories(String className, boolean resolve)
	{
		Class result = null;

		// A lot of things may fail (mostly because of I/O).
		try
		{
			byte [] data = null;

			// Try to find a matching entry
			ResourceEntry entry = (ResourceEntry) entries.get(className);

			if (entry != null)
			{
				data = entry.getBytes();

				if (data != null)
				{
					log(LogLevel.DEBUG, "Class resolved by custom loader: {0} from repository {1}", className, entry.getRepositoryUrl().toString());

					// Create the class (The class name is fully qualified, so we can pass it to
					// createClass to save decoding the class).
					result = createClass(className, data, resolve);

					// ...and store it in the cache.
					addClassToCache(className, result);
				}
			}
			else
			{
				log(LogLevel.DEBUG, "Class not found in any repository: {0}", className);
			}
		}
		catch (Exception e)
		{
			log(LogLevel.ERROR, "Exception loading class: {0}", className, e);
		}

		// Return the resulting class (or null in case of problems).
		return result;
	}

	/**
	 * Tries to load the class from the standard class loaders. As a side-effect, a successfully loaded
	 * class is put to the class cache.  However, synchronization on the class cache access shouldn't
	 * be necessary here, because this method is invoked only with acquired lock on the cache.
	 *
	 * @param className Fully qualified name of the class
	 * @return The class if it could be loaded or null
	 */
	private Class tryStandardLoaders(String className)
	{
		Class result = null;

		// We might run into ClassNotFoundException's using the standard class loaders.
		try
		{
			// Try the standard class loaders first.
			if (configuration.getParentClassLoader() != null)
			{
				result = configuration.getParentClassLoader().loadClass(className);
			}
			else
			{
				ClassLoader cl = getClass().getClassLoader();
				if (cl != null && cl != this)
				{
					result = cl.loadClass(className);
				}
				else
				{
					result = Class.forName(className);
				}
			}

			// If we got a result, put it into the class cache.
			if (result != null)
			{
				addClassToCache(className, result);

				log(LogLevel.DEBUG, "Class resolved by standard loader: {0}", className);
			}
		}
		catch (ClassNotFoundException e)
		{
		}

		// Return the result or null.
		return result;
	}

	//////////////////////////////////////////////////
	// @@ Class creation helper
	//////////////////////////////////////////////////

	/**
	 * Creates a class object from its binary data.
	 *
	 * @param className Name of the class
	 * @param data Class data (from file)
	 * @param resolve If true, all dependents are loaded too
	 * @return The class object
	 */
	public Class createClass(String className, byte [] data, boolean resolve)
	{
		Class clazz = defineClass(className, data, 0, data.length);
		if (resolve)
		{
			resolveClass(clazz);
		}
		return clazz;
	}

	//////////////////////////////////////////////////
	// @@ Resource management
	//////////////////////////////////////////////////

	/**
	 * Loads a resource from the class path.
	 *
	 * @param resourceName Name of the resource file
	 * @return An input stream to the resource or null if the resource was not found
	 */
	protected InputStream loadResourceFromPath(String resourceName)
	{
		log(LogLevel.DEBUG, "Loading resource: {0}", resourceName);

		// I/O operations might fail.
		try
		{
			byte [] data = null;

			// Try to find a matching entry
			String searchName = StringUtil.normalizePathName(resourceName);
			ResourceEntry entry = (ResourceEntry) entries.get(searchName);

			if (entry != null)
			{
				data = entry.getBytes();

				if (data != null)
				{
					log(LogLevel.DEBUG, "Resource resolved by custom loader: {0} from repository {1}", resourceName, entry.getRepositoryUrl().toString());

					// Return a stream based on the loaded data.
					return new ByteArrayInputStream(data);
				}
			}
		}
		catch (Exception e)
		{
			log(LogLevel.ERROR, "Exception loading resource: {0}", resourceName, e);
		}

		// If we have a parent loader, we should give it a chance to load the resource now.
		if (configuration.getParentClassLoader() != null)
		{
			InputStream stream = configuration.getParentClassLoader().getResourceAsStream(resourceName);
			if (stream != null)
			{
				log(LogLevel.DEBUG, "Resource resolved by standard loader: {0}", resourceName);
				return stream;
			}
		}

		// Log that we haven't found anything to return here.
		log(LogLevel.DEBUG, "Unresolved resource: {0}", resourceName);
		return null;
	}

	/**
	 * Loads a resource from the class path.
	 *
	 * @param resourceName Name of the resource file
	 * @return A URL of the resource or null if the resource could not be found
	 */
	protected URL getResourceFromPath(String resourceName)
	{
		log(LogLevel.DEBUG, "Getting resource: {0}", resourceName);

		// Try to find a matching entry
		String searchName = StringUtil.normalizePathName(resourceName);
		ResourceEntry entry = (ResourceEntry) entries.get(searchName);

		if (entry != null)
		{
			URL url = entry.createURL();
			log(LogLevel.DEBUG, "Resource URL resolved by custom loader: {0}", url.toString());
			return url;
		}

		// If we have no URL yet, give the parent class loader a chance...
		if (configuration.getParentClassLoader() != null)
		{
			URL url = configuration.getParentClassLoader().getResource(resourceName);
			if (url != null)
			{
				log(LogLevel.DEBUG, "Resource resolved by standard loader: {0}", resourceName);
				return url;
			}
		}

		// Log the we had no luck with the resource...
		log(LogLevel.DEBUG, "Unresolved resource: {0}", resourceName);
		return null;
	}

	//////////////////////////////////////////////////
	// @@ Class cache maintainance.
	//////////////////////////////////////////////////

	/**
	 * Adds a class to the class cache.
	 *
	 * @param className Fully qualified class name
	 * @param cls Class object
	 */
	protected void addClassToCache(String className, Class cls)
	{
		classCache.put(className, cls);
	}

	/**
	 * Tries to load the class from the cache.
	 *
	 * @param className Fully qualified name of the class
	 * @return The class if it is in the cache or null
	 */
	protected Class loadClassFromCache(String className)
	{
		return (Class) classCache.get(className);
	}

	//////////////////////////////////////////////////
	// @@ Repository management
	//////////////////////////////////////////////////

	/**
	 * This method scans all repositories in the configuration for classes.
	 * @throws Exception On any error that occurs while scanning the repositories specified in the class loader configuration
	 */
	protected void scanRepositories()
		throws Exception
	{
		long time = 0;
		if (DEBUG_LOAD_TIME && isLogEnabled(LogLevel.DEBUG))
		{
			time = System.currentTimeMillis();
		}

		List repositories = configuration.getRepositories();

		int n = repositories.size();
		for (int i = 0; i < n; i++)
		{
			String repositoryName = (String) repositories.get(i);
			File f = new File(repositoryName);
			try
			{
				String path = f.getAbsolutePath();

				if (f.isDirectory())
				{
					log(LogLevel.INFO, "Adding directory: {0}", path);

					scanDirectory(f, path);
				}
				else if (repositoryName.endsWith(".jar") || repositoryName.endsWith(".zip"))
				{
					scanJarFile(f);
				}
			}
			catch (Exception e)
			{
				log(LogLevel.ERROR, "Exception adding repository to class path: {0}", repositoryName, e);
			}
		}

		if (DEBUG_LOAD_TIME && isLogEnabled(LogLevel.DEBUG))
		{
			time = System.currentTimeMillis() - time;

			log(LogLevel.DEBUG, "scanRepositories took " + time + " ms.");
		}
	}

	/**
	 * This is a recursive method for mapping all of the files that are considered to be
	 * java classes into our entry table.
	 * By maintaining a list of all classes, we can greatly reduce the amount of time
	 * taken to retrieve the bytes for a specific class
	 * and can easily deduce when the required class is not available.
	 *
	 * @param dir Directory to map
	 * @param basePath Base directory, used when deducing which directories are package names
	 * @throws Exception On any error that occurs while scanning the repositories specified in the class loader configuration
	 */
	private void scanDirectory(File dir, String basePath)
		throws Exception
	{
		int basePathLen = basePath.length();

		String [] files = dir.list();
		for (int i = 0; i < files.length; i++)
		{
			File f = new File(dir.getPath(), files [i]);
			if (f.isDirectory())
			{
				// Recurse
				scanDirectory(f, basePath);
			}
			else
			{
				String path = f.getAbsolutePath();

				// Store the class file for access via getResource().
				String resourceName = path.substring(basePathLen + 1);
				resourceName = StringUtil.normalizePathName(resourceName);

				if (entries.get(resourceName) != null)
				{
					// This entry is already defined, ignore
					continue;
				}

				byte [] content = readContent(f);

				// Store the resource name of the zip entry in the table
				ResourceEntry entry = new ResourceEntry(f.toURL(), content);
				addResourceEntry(resourceName, entry);

				// For class files, store a mapping of the class name
				addResourceEntryForClass(resourceName, entry);
			}
		}
	}

	/**
	 * Reads the specified zip file, adding all of its contents that are java class files
	 * to the entry table.
	 *
	 * @param f Jar file to be mapped
	 * @throws Exception On any error that occurs while scanning the repositories specified in the class loader configuration
	 */
	private void scanJarFile(File f)
		throws Exception
	{
		String zipFileName = f.getAbsolutePath();
		log(LogLevel.INFO, "Adding zip file: {0}", zipFileName);

		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(zipFileName);
			scanJarFile(new BufferedInputStream(fis), f.toURL());
		}
		finally
		{
			if (fis != null)
			{
				fis.close();
			}
		}
	}

	/**
	 * Reads the zip file from the given input stream, adding all of its contents that are java class files
	 * to the entry table.
	 *
	 * @param in Input stream to the jar file
	 * @param jarFileUrl URL of the Jar file
	 * @throws Exception On any error that occurs while scanning the repositories specified in the class loader configuration
	 */
	private void scanJarFile(InputStream in, URL jarFileUrl)
		throws Exception
	{
		ZipInputStream zis = null;
		try
		{
			zis = new ZipInputStream(new BufferedInputStream(in));
			ZipEntry ze;

			// Read each entry from the ZipInputStream until no more entry found
			while ((ze = zis.getNextEntry()) != null)
			{
				String resourceName = ze.getName();
				if (entries.get(resourceName) != null)
				{
					// This entry is already defined, ignore
					continue;
				}

				int size = (int) ze.getSize();
				if (size == 0)
					continue;

				ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
				byte[] buffer = new byte[4096];
				int nRead;
				while ((nRead = zis.read(buffer)) > 0)
				{
					bos.write(buffer, 0, nRead);
				}
				byte [] content = bos.toByteArray();

				// Store the resource name of the zip entry in the table
				URL jarEntryUrl = buildJarEntryUrl(jarFileUrl, resourceName);
				ResourceEntry entry = new ResourceEntry(jarEntryUrl, content);
				addResourceEntry(resourceName, entry);

				// For class files, store a mapping of the class name
				addResourceEntryForClass(resourceName, entry);
			}
		}
		finally
		{
			if (zis != null)
			{
				zis.close();
			}
		}
	}

	/**
	 * Produces a jar URL from a given jar file and entry name.
	 *
	 * @param jarFileUrl The jar file
	 * @param entryName Name of the entry, e. g. "java/net/URL.class"
	 * @return A jar URL of the form jar:file:///path/to/jarfile.jar!/resourcename.
	 */
	private static URL buildJarEntryUrl(URL jarFileUrl, String entryName)
		throws MalformedURLException
	{
		// Append jar: protocol.
		StringBuffer result = new StringBuffer("jar:");

		// Append file URL to the archive file.
		result.append(jarFileUrl);

		// Append seperator.
		result.append("!/");

		// Adjust resource name and append it.
		result.append(entryName);

		// Return the result.
		return new URL(result.toString());
	}

	/**
	 * Reads the resource from the repository.
	 *
	 * @param file File to read
	 * @return Bytes that define the content of the entry (i. e. the class code)
	 * @throws Exception On any error that occurs while scanning the repositories specified in the class loader configuration
	 */
	public static byte [] readContent(File file)
		throws Exception
	{
		int size = (int) file.length();
		byte [] buffer = new byte [size];

		BufferedInputStream in = new BufferedInputStream(new FileInputStream(file), 4096);

		try
		{
			int pos = 0;
			while (pos < size)
			{
				pos += in.read(buffer, pos, size - pos);
			}
		}
		finally
		{
			in.close();
		}

		return buffer;
	}

	//////////////////////////////////////////////////
	// @@ Log support
	//////////////////////////////////////////////////

	/**
	 * Logs a message.
	 *
	 * @param logLevel Log level as defined by {@link LogLevel}
	 * @param msg The message to be logged
	 * @param e An exception that should be logged together with the message (might be null)
	 */
	protected void log(String logLevel, String msg, Exception e)
	{
		if (isLogEnabled(logLevel))
		{
			writeLog(logLevel, "(" + configuration.getName() + ") - " + msg, e);
		}
	}

	/**
	 * Logs a message.
	 *
	 * @param logLevel Log level as defined by {@link LogLevel}
	 * @param msg The message to be logged
	 * @param arg message argument
	 * @param e An exception that should be logged together with the message (might be null)
	 */
	protected void log(String logLevel, String msg, String arg, Exception e)
	{
		if (isLogEnabled(logLevel))
		{
			msg = MsgFormat.format(msg, arg);
			writeLog(logLevel, "(" + configuration.getName() + ") - " + msg, e);
		}
	}

	/**
	 * Logs a message.
	 *
	 * @param logLevel Log level as defined by {@link LogLevel}
	 * @param msg The message to be logged
	 * @param arg message argument
	 */
	protected void log(String logLevel, String msg, String arg)
	{
		if (isLogEnabled(logLevel))
		{
			msg = MsgFormat.format(msg, arg);
			writeLog(logLevel, "(" + configuration.getName() + ") - " + msg, null);
		}
	}

	/**
	 * Logs a message.
	 *
	 * @param logLevel Log level as defined by {@link LogLevel}
	 * @param msg The message to be logged
	 * @param arg message argument
	 * @param arg2 message argument
	 */
	protected void log(String logLevel, String msg, String arg, String arg2)
	{
		if (isLogEnabled(logLevel))
		{
			msg = MsgFormat.format(msg, arg, arg2);
			writeLog(logLevel, "(" + configuration.getName() + ") - " + msg, null);
		}
	}

	/**
	 * A convenience method that allows logging without an exception.
	 *
	 * @param logLevel Log level as defined by {@link LogLevel}
	 * @param msg The message to be logged
	 */
	protected void log(String logLevel, String msg)
	{
		if (isLogEnabled(logLevel))
		{
			writeLog(logLevel, "(" + configuration.getName() + ") - " + msg, null);
		}
	}

	/**
	 * Initializes the logger.
	 * This will be called before any log output is being written.
	 */
	protected abstract void setupLogger();

	/**
	 * Writes a message to the log.
	 * This abstract method has to be implemented by sub classes of this class to
	 * implement support of a custom log mechanism.
	 *
	 * @param logLevel Log level as defined by {@link LogLevel}
	 * @param msg Message to be logged
	 * @param e An exception that should be logged together with the message (might be null)
	 */
	protected abstract void writeLog(String logLevel, String msg, Exception e);

	/**
	 * Checks if the specified log level is enabled.
	 * This abstract method has to be implemented by sub classes of this class to
	 * implement support of a custom log mechanism.
	 *
	 * @param logLevel Log level as defined by {@link LogLevel}
	 * @return
	 *		true	The log level is enabled.
	 *		false	The log level is disabled.
	 */
	protected abstract boolean isLogEnabled(String logLevel);

	//////////////////////////////////////////////////
	// @@ Object overrides
	//////////////////////////////////////////////////

	/**
	 * Overridden toString method.
	 * @nowarn
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(getClass().getName());
		buf.append(" (");
		buf.append(configuration.getName());
		buf.append(") : ");
		buf.append(super.toString());
		return buf.toString();
	}
}
