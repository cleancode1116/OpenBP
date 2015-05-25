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
package org.openbp.jaspira.propertybrowser.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openbp.common.ReflectUtil;

/**
 * Manager class that keeps track of property editors and packages that may contain editors.
 *
 * @author Andreas Putz
 */
public final class PropertyEditorMgr
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Table of packages that may contain property editors (contains Strings) */
	private List packages;

	/**
	 * Table of all loaded editor classes.
	 * Maps unqualified editor class names to editor classes ({@link PropertyEditor} objects)
	 */
	private Map editors;

	/**
	 * Table of all loaded validator classes.
	 * Maps unqualified validator class names to validator classes ({@link PropertyValidator} objects)
	 */
	private Map validators;

	/** Singleton instance */
	private static PropertyEditorMgr singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction and Singleton access
	//////////////////////////////////////////////////

	/**
	 * Private constructor.
	 */
	private PropertyEditorMgr()
	{
		editors = new HashMap();
		validators = new HashMap();

		addPackage("org.openbp.jaspira.propertybrowser.editor.standard");
	}

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized PropertyEditorMgr getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new PropertyEditorMgr();
		return singletonInstance;
	}

	/**
	 * Resets the loaded classes.
	 */
	public static void reset()
	{
		getInstance().editors.clear();
		getInstance().validators.clear();
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Adds a package.
	 * @param pkg The package to add
	 */
	public void addPackage(String pkg)
	{
		if (packages == null)
			packages = new ArrayList();
		packages.add(pkg);
	}

	/**
	 * Looks up the specified property editor.
	 * The method will search all packages added by {@link #addPackage} and
	 * return the editor class if it could be found in one of
	 * the packages.
	 *
	 * @param className Unqualified name of the class
	 *
	 * @return The editor class (a {@link PropertyEditor}) or null if no such editor could be found
	 */
	public Class findPropertyEditor(String className)
	{
		// Search chache first
		Class cls = (Class) editors.get(className);

		if (cls == null)
		{
			// Check the property editor packages
			cls = ReflectUtil.findClassInPackageList(className, packages);
			if (cls != null)
			{
				if (PropertyEditor.class.isAssignableFrom(cls))
				{
					// Add to cache
					editors.put(className, cls);
				}
				else
				{
					// No property editor
					System.err.println("Property editor '" + cls.getName() + "' does not implement the PropertyEditor interface");
					cls = null;
				}
			}
			else
			{
				System.err.println("Property editor '" + className + "' not found");
			}
		}

		return cls;
	}

	/**
	 * Looks up the specified property validator.
	 * The method will search all packages added by {@link #addPackage} and
	 * return the validator class if it could be found in one of
	 * the packages.
	 *
	 * @param className Unqualified name of the class
	 *
	 * @return The validator class (a {@link PropertyValidator}) or null if no such validator could be found
	 */
	public Class findPropertyValidator(String className)
	{
		// Search chache first
		Class cls = (Class) validators.get(className);

		if (cls == null)
		{
			// Check the property validator packages
			cls = ReflectUtil.findClassInPackageList(className, packages);
			if (cls != null)
			{
				if (PropertyValidator.class.isAssignableFrom(cls))
				{
					// Add to cache
					validators.put(className, cls);
				}
				else
				{
					// No property validator
					System.err.println("Property validator '" + cls.getName() + "' does not implement the PropertyValidator interface");
					cls = null;
				}
			}
			else
			{
				System.err.println("Property validator '" + className + "' not found");
			}
		}

		return cls;
	}
}
