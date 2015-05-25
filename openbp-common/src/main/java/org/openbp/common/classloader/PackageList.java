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

import java.util.Enumeration;
import java.util.Hashtable;

import org.openbp.common.logger.LogLevel;

/**
 * This class maintains a list of packages and methods allowing to check
 * whether a given class is within the packages in the list.
 *
 * @author Falk Hartmann
 */
public class PackageList
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** A flag denoting that a package should stand only for itself, i\.e\. without its subpackages. */
	private static final Boolean EXACT_MATCH = Boolean.TRUE;

	/** A flag denoting that a package should be considered as containing all its subpackages. */
	private static final Boolean WILDCARD_MATCH = Boolean.FALSE;

	/** The postfix denoting a package that should be considered containg all its subpackages. */
	private static final String WILDCARD_POSTFIX = "*";

	/** A hashtable mapping package names (String) to Boolean flags (defined above). */
	private Hashtable packages;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public PackageList()
	{
		packages = new Hashtable();
	}

	//////////////////////////////////////////////////
	// @@ Access methods
	//////////////////////////////////////////////////

	/**
	 * This method adds a package name to this list. If the package name ends with an asterisk,
	 * it is considered as containing all its subpackages.
	 *
	 * @param packageName The name of the package to be added
	 */
	public void addPackage(String packageName)
	{
		// Check for package wildcard.
		if (packageName.endsWith(WILDCARD_POSTFIX))
		{
			// Remove wildcard, if prepended.
			packages.put(packageName.substring(0, packageName.length() - WILDCARD_POSTFIX.length() - 1), WILDCARD_MATCH);
		}
		else
		{
			// Hold exact package name.
			packages.put(packageName, EXACT_MATCH);
		}
	}

	/**
	 * Prints debug output to the given class loader's logger.
	 *
	 * @param prefix Prefix to prepend to the log output
	 * @param cl Class loader
	 */
	public void printDebug(String prefix, XClassLoaderBase cl)
	{
		// Iterate over all keys.
		Enumeration keys = packages.keys();
		while (keys.hasMoreElements())
		{
			// Get the key.
			String key = (String) keys.nextElement();

			// Get, whether the package is marked as stand-alone (as opposite to containing its subpackages).
			boolean exactMatch = ((Boolean) packages.get(key)).booleanValue();

			cl.log(LogLevel.INFO, prefix + ": " + key + (exactMatch ? "" : ".*"));
		}
	}

	/**
	 * This method checks, whether the package with the given name is contained
	 * in this package list.
	 *
	 * @param packageName The package name to check for
	 * @return true, if the package is contained in this list
	 */
	public boolean containsPackage(String packageName)
	{
		// No package name, no containment.
		if (packageName == null)
		{
			return false;
		}

		// Iterate over all keys.
		Enumeration keys = packages.keys();
		while (keys.hasMoreElements())
		{
			// Get the key.
			String key = (String) keys.nextElement();

			// Check, whether we have an exact match.
			if (packageName.equals(key))
			{
				return true;
			}

			// Get, whether the package is marked as stand-alone (as opposite to containing its subpackages).
			boolean exactMatch = ((Boolean) packages.get(key)).booleanValue();

			// Check, whether we have a non-exact match with a wildcard package.
			if (!exactMatch && packageName.startsWith(key))
			{
				return true;
			}
		}

		// No containment at all.
		return false;
	}

	/**
	 * This method checks whether the class with the given name is in a package that is
	 * contained in this package list. Package-less classes are always considered as not contained.
	 *
	 * @param className The name of the class to check for
	 * @return false, if the class is package-less or in a package not contained in this package list
	 */
	public boolean containsClass(String className)
	{
		// Check for package.
		if (className.indexOf(".") == -1)
		{
			// No package, no containment.
			return false;
		}

		// Check for the package of the class.
		return containsPackage(className.substring(0, className.lastIndexOf('.')));
	}
}
