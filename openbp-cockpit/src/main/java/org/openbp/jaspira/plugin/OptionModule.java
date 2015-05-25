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
package org.openbp.jaspira.plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.ReflectUtil;
import org.openbp.jaspira.option.Option;

/**
 * The option module defines the options of a plugin.
 * Options are defined as inner classes ({@link Option} or some subclass of this) of the option module.
 *
 * @author Jens Ferchland
 */
public abstract class OptionModule
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** List with all options of this module (inner and outer) (contains {@link Option} objects) */
	private List options;

	//////////////////////////////////////////////////
	// @@ Installation
	//////////////////////////////////////////////////

	/**
	 * Installs the option module and instantiates the option classes.
	 */
	public final void install()
	{
		loadOptions();
	}

	/**
	 * Uninstalls all options.
	 */
	public final void uninstall()
	{
		if (options != null)
		{
			int n = options.size();
			for (int i = 0; i < n; ++i)
			{
				Option option = (Option) options.get(i);
				option.uninstall();
			}
			options = null;
		}
	}

	//////////////////////////////////////////////////
	// @@ Overridables
	//////////////////////////////////////////////////

	/**
	 * Returns a list of external option classes.
	 * This classes will be instanciated and registered at option manager at module installation time.
	 *
	 * @return A list of Class objects or null if the module doesn't declare user options.
	 * The classes in this list must extend the {@link Option} class.
	 */
	public List getExternalOptionClasses()
	{
		return null;
	}

	//////////////////////////////////////////////////
	// @@ Final methods
	//////////////////////////////////////////////////

	/**
	 * Instantiates the internal and external options.
	 */
	protected final void loadOptions()
	{
		// Strategy: we muster search for any inner classes that are descendants of Option
		Class [] innerClasses = this.getClass().getClasses();
		for (int i = 0; i < innerClasses.length; i++)
		{
			if (Option.class.isAssignableFrom(innerClasses [i]) && !Modifier.isAbstract(innerClasses [i].getModifiers()))
			{
				try
				{
					// We retrieve the Constructor of the option that uses one single parameter
					// of the type of our enclosing class (i.e. this.getClass ())
					// Note that this constructor is an implicit, private constructor of the
					// inner class mechanism.
					Constructor contr = innerClasses [i].getDeclaredConstructors() [0];

					// Create a new instance of the module using us a reference parameter.
					Option option = (Option) contr.newInstance(new Object [] { this });

					addOption(option);
				}
				catch (InvocationTargetException e)
				{
					ExceptionUtil.printTrace(e);
				}
				catch (InstantiationException e)
				{
					ExceptionUtil.printTrace(e);
				}
				catch (IllegalAccessException e)
				{
					ExceptionUtil.printTrace(e);
				}
				catch (ClassCastException e)
				{
					ExceptionUtil.printTrace(e);
				}
			}
		}

		// Now load the external options
		List externalOptions = getExternalOptionClasses();
		if (externalOptions != null)
		{
			for (Iterator it = externalOptions.iterator(); it.hasNext();)
			{
				Class cls = (Class) it.next();

				if (Option.class.isAssignableFrom(cls))
				{
					Option option = (Option) ReflectUtil.instantiate(cls, Option.class, "external option");
					addOption(option);
				}
			}
		}
	}

	/**
	 * Adds an option to the option module.
	 *
	 * @param option Option to add
	 */
	private void addOption(Option option)
	{
		if (!ConfigMgr.getInstance().evaluate(option.getCondition()))
		{
			// Action condition evaluation failed, don't create
			return;
		}

		option.install();
		if (options == null)
			options = new ArrayList();
		options.add(option);
	}
}
