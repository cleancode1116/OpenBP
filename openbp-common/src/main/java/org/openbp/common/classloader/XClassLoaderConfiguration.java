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

import java.io.File;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.openbp.common.logger.LogLevel;
import org.openbp.common.string.StringUtil;

/**
 * This class contains all information that is necessary to create a class loader.
 * The information is concentrated here to allow a proper construction of the class loader.
 * Without this class, it would be hard to design the class loader in a way, that is multi-threaded
 * to use because it is hard to decide, whether the class loader is fully initialized (a call
 * adding a directory to the class path would be possible at any time). To ensure, that a call changing
 * the configuration of the class loader doesn't influence the class loader, the configuration is
 * cloned when passed to a class loader, thus the configuration implements {@link java.lang.Cloneable}
 *
 * @author Falk Hartmann
 */
public class XClassLoaderConfiguration
	implements Cloneable
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** A flag that tells, whether we should first give the standard class loader a try. */
	private boolean tryStandardClassLoaderFirst = true;

	/** The name of the class loader. */
	private String name;

	/** A list of repositories (i.e., directories or jar/zip files). */
	private List repositories;

	/** A list of standard packages. */
	private PackageList standardPackages;

	/** A list of non standard packages. */
	private PackageList nonStandardPackages;

	/** The parent class loader. */
	private ClassLoader parentClassLoader;

	/** Flag if logging is to be done at all (calling loggers in class loaders may lead to stack overflows on log-related resource loading errors) */
	private boolean loggingEnabled;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constrcutor.
	 */
	public XClassLoaderConfiguration()
	{
		// Initialize internal data structures.
		repositories = new Vector();
		standardPackages = new PackageList();
		nonStandardPackages = new PackageList();

		// Include some standard packages in out standard package list.
		standardPackages.addPackage("java.*");
		standardPackages.addPackage("javax.*");
		standardPackages.addPackage("sun.*");
		standardPackages.addPackage("com.sun.*");

		// Initialize parent class loader.
		parentClassLoader = getClass().getClassLoader();
	}

	/**
	 * Prints debug output to the given class loader's logger.
	 *
	 * @param cl Class loader
	 */
	public void printDebug(XClassLoaderBase cl)
	{
		cl.log(LogLevel.INFO, "Class loader configuration:");
		cl.log(LogLevel.INFO, "Using standard loaders first: {0}", tryStandardClassLoaderFirst ? "true" : "false");
		cl.log(LogLevel.INFO, "Parent class loader: {0}", parentClassLoader != null ? parentClassLoader.toString() : "none");

		standardPackages.printDebug("Standard package", cl);
		nonStandardPackages.printDebug("Non-standard package", cl);
	}

	//////////////////////////////////////////////////
	// @@ Class Loader Behaviour
	//////////////////////////////////////////////////

	/**
	 * Adds a standard package that should be loaded by the standard class loader.
	 *
	 * @param packageName Package name<br>
	 * If the package name ends with '*', it is treated as a wildcard and thus covers all
	 * sub packages of the package. Otherwise, the package name must match exactly.
	 */
	public void addStandardPackage(String packageName)
	{
		standardPackages.addPackage(packageName);
	}

	/**
	 * Adds a package that should \bnot\b be loaded by the standard class loader.
	 *
	 * @param packageName Package name<br>
	 * If the package name ends with '*', it is treated as a wildcard and thus covers all
	 * sub packages of the package. Otherwise, the package name must match exactly.
	 */
	public void addNonStandardPackage(String packageName)
	{
		nonStandardPackages.addPackage(packageName);
	}

	/**
	 * Returns the list of standard packages.
	 *
	 * @return The list of standard packages in this configuration
	 */
	public PackageList getStandardPackages()
	{
		return standardPackages;
	}

	/**
	 * Returns the list of non-standard packages.
	 *
	 * @return The list of non-standard packages in this configuration
	 */
	public PackageList getNonStandardPackages()
	{
		return nonStandardPackages;
	}

	/**
	 * This method sets, whether the standard class loader should be tried before
	 * a lookup in the registered repositories.
	 *
	 * @param tryStandardClassLoaderFirst If the standard loader should be tried first
	 */
	public void setTryStandardClassLoaderFirst(boolean tryStandardClassLoaderFirst)
	{
		this.tryStandardClassLoaderFirst = tryStandardClassLoaderFirst;
	}

	/**
	 * This method determines, whether the standard class loader should be tried before
	 * a lookup in the registered repositories.
	 *
	 * @return Whether the standard loader should be tried first
	 */
	public boolean getTryStandardClassLoaderFirst()
	{
		return tryStandardClassLoaderFirst;
	}

	//////////////////////////////////////////////////
	// @@ Repository maintenance
	//////////////////////////////////////////////////

	/**
	 * This method adds a list of repository names, passed as string (each repository seperated
	 * by the standard {@link java.io.File#pathSeparator} from the others).
	 *
	 * @param repositoryList A list of repository names
	 */
	public void addRepositories(String repositoryList)
	{
		if (repositoryList != null)
		{
			StringTokenizer t = new StringTokenizer(repositoryList, File.pathSeparator);
			while (t.hasMoreTokens())
			{
				String s = t.nextToken();
				addRepository(s);
			}
		}
	}

	/**
	 * This method adds a single repository given by name to the list of repositories maintained
	 * by this configuration.
	 *
	 * @param repository The repository to be added
	 */
	public void addRepository(String repository)
	{
		// Normalize file name
		repository = StringUtil.absolutePathName(repository);

		// Only add the repository if it's not already there
		if (!repositories.contains(repository))
		{
			repositories.add(repository);
		}
	}

	/**
	 * This method returns the list of currently registered repositories.
	 *
	 * @return The list of registered repositories
	 */
	public List getRepositories()
	{
		return repositories;
	}

	//////////////////////////////////////////////////
	// @@ Miscellaneous
	//////////////////////////////////////////////////

	/**
	 * This method sets the name for the class loader.
	 *
	 * @param name The name to be set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * This method returns the name to be used for the class loader.
	 *
	 * @return The name to be used
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * This method sets the parent class loader.
	 *
	 * @param parentClassLoader The parent class loader to be set
	 */
	public void setParentClassLoader(ClassLoader parentClassLoader)
	{
		this.parentClassLoader = parentClassLoader;
	}

	/**
	 * This returns the parent class loader to be used.
	 *
	 * @return The parent class loader to be used
	 */
	public ClassLoader getParentClassLoader()
	{
		return parentClassLoader;
	}

	/**
	 * Gets the flag if logging is to be done at all (calling loggers in class loaders may lead to stack overflows on log-related resource loading errors).
	 * @nowarn
	 */
	public boolean isLoggingEnabled()
	{
		return loggingEnabled;
	}

	/**
	 * Sets the flag if logging is to be done at all (calling loggers in class loaders may lead to stack overflows on log-related resource loading errors).
	 * @nowarn
	 */
	public void setLoggingEnabled(boolean loggingEnabled)
	{
		this.loggingEnabled = loggingEnabled;
	}

	/**
	 * Configuration's are cloneable, so clone is implemented as public method here.
	 *
	 * @return A clone of this configuration
	 */
	public Object clone()
	{
		try
		{
			return super.clone();
		}
		catch (CloneNotSupportedException cnse)
		{
			return null;
		}
	}
}
