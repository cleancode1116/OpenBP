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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.openbp.cockpit.CockpitConstants;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.ReflectUtil;
import org.openbp.common.classloader.XClassLoader;
import org.openbp.common.classloader.XClassLoaderConfiguration;
import org.openbp.common.io.xml.XMLDriver;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.resource.ResourceMgr;
import org.openbp.common.resource.ResourceMgrException;
import org.openbp.common.setting.SettingUtil;
import org.openbp.common.util.iterator.EmptyIterator;
import org.openbp.jaspira.gui.plugin.ApplicationBase;
import org.openbp.jaspira.gui.plugin.VisiblePlugin;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * The PluginMgr is the central instance for managing classes and instances of plugins.
 * It has methods for loading plugin-classr files, creating new instances
 * of a given plugin and retrieving instances of already created plugins.
 *
 * @author Stephan Moritz
 */
public final class PluginMgr
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Constants
	/////////////////////////////////////////////////////////////////////////

	/** Plugin entry status: The class file has not yet been loaded. */
	public static final int PLUGIN_NOT_LOADED = 0;

	/** Plugin entry status: The class file has been loaded, but there a no instances. */
	public static final int PLUGIN_CLASS_LOADED = 1;

	/** Plugin entry status: The plugin has instances. */
	public static final int PLUGIN_HAS_INSTANCES = 2;

	/** Plugin entry status: An exception occurred during loading of class or instantiation. */
	public static final int PLUGIN_LOAD_FAILED = -1;

	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** Singleton instance */
	private static PluginMgr singletonInstance = new PluginMgr();

	/** Table of all available plugins as class name - {@link PluginMgr.PluginEntry} pairs */
	private Map pluginClasses;

	/** Classloader used to load the plugins. */
	private transient ClassLoader classloader;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction/Instance access
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 * We do not want this to be called directly (singleton pattern).
	 */
	private PluginMgr()
	{
		pluginClasses = new HashMap();
	}

	/**
	 * Returns the singleton instance of the plugin manager.
	 * @nowarn
	 */
	public static synchronized PluginMgr getInstance()
	{
		if (singletonInstance == null)
		{
			singletonInstance = new PluginMgr();
		}

		return singletonInstance;
	}

	//////////////////////////////////////////////////
	// @@ Public methods
	//////////////////////////////////////////////////

	/**
	 * Returns the (already existing instance) of a plugin with the given
	 * unique ID or null if there is no such instance.
	 * @nowarn
	 */
	public Plugin getPlugin(String uniqueId)
	{
		String classname = uniqueId.substring(0, uniqueId.indexOf(Plugin.ID_DELIMETER));

		PluginEntry entry = (PluginEntry) pluginClasses.get(classname);

		if (entry != null)
		{
			return entry.getPluginInstance(uniqueId);
		}

		return null;
	}

	/**
	 * Creates a new instance of the given plugin.
	 *
	 * @param classname Plugin class
	 * @param parent Parent for the new plugin in the plugin tree
	 * @return The new plugin or null if it could not be instantiated
	 */
	public Plugin createInstance(String classname, Plugin parent)
	{
		PluginEntry entry = (PluginEntry) pluginClasses.get(classname);
		if (entry == null)
		{
			entry = new PluginEntry(classname);
			pluginClasses.put(classname, entry);
		}

		return entry.createInstance(parent);
	}

	/**
	 * Creates a new instance of the given plugin.
	 *
	 * @param profile Plugin profile
	 * @param pluginClassLoader Separate class loader for the plugin or null
	 * @param parent Parent for the new plugin in the plugin tree
	 * @return The new plugin or null if it could not be instantiated
	 */
	public Plugin createInstance(PluginProfile profile, ClassLoader pluginClassLoader, Plugin parent)
	{
		PluginEntry entry = (PluginEntry) pluginClasses.get(profile.getClassName());
		if (entry == null)
		{
			entry = new PluginEntry(profile);
			entry.setPluginClassLoader(pluginClassLoader);
			pluginClasses.put(profile.getClassName(), entry);
		}

		return entry.createInstance(parent);
	}

	/**
	 * Create a new instance of the given class.
	 *
	 * @param cls Plugin class
	 * @param parent Parent for the new plugin in the plugin tree
	 * @return The new plugin or null if it could not be instantiated
	 */
	public Plugin createInstance(Class cls, Plugin parent)
	{
		return createInstance(cls.getName(), parent);
	}

	/**
	 * Creates a new instance via a supplied state object.
	 *
	 * @param state Plugin state for the new plugin
	 * @param parent Parent for the new plugin in the plugin tree
	 * @return The new plugin or null if it could not be instantiated
	 */
	public Plugin createInstance(PluginState state, Plugin parent)
	{
		String classname = state.getPluginClassName();

		PluginEntry entry = (PluginEntry) pluginClasses.get(classname);
		if (entry == null)
		{
			entry = new PluginEntry(classname);
			pluginClasses.put(classname, entry);
		}

		return entry.createInstance(state, parent);
	}

	/**
	 * Creates a new instance of the given plugin.
	 *
	 * @param classname Plugin class
	 * @param parent Parent for the new plugin in the plugin tree
	 * @return The new plugin or null if it could not be instantiated
	 */
	public VisiblePlugin createVisibleInstance(String classname, Plugin parent)
	{
		return (VisiblePlugin) createInstance(classname, parent);
	}

	/**
	 * Create a new instance of the given class.
	 *
	 * @param cls Plugin class
	 * @param parent Parent for the new plugin in the plugin tree
	 * @return The new plugin or null if it could not be instantiated
	 */
	public VisiblePlugin createVisibleInstance(Class cls, Plugin parent)
	{
		return (VisiblePlugin) createInstance(cls, parent);
	}

	/**
	 * Creates a new instance via a supplied state object.
	 *
	 * @param state Plugin state for the new plugin
	 * @param parent Parent for the new plugin in the plugin tree
	 * @return The new plugin or null if it could not be instantiated
	 */
	public VisiblePlugin createVisibleInstance(PluginState state, Plugin parent)
	{
		return (VisiblePlugin) createInstance(state, parent);
	}

	/**
	 * Creates a duplicate of the supplied plugin via its state object.
	 *
	 * @param source Plugin to copy
	 * @return The new plugin or null if it could not be instantiated
	 */
	public Plugin createCopy(Plugin source)
	{
		return createInstance(source.getPluginState(), source.getParentPlugin());
	}

	/**
	 * Registers an externally created Plugin with the manager.
	 * This should only be used in special cases.
	 *
	 * @param plugin Plugin to register
	 */
	public void registerPlugin(Plugin plugin)
	{
		String classname = plugin.getClass().getName();

		PluginEntry entry = (PluginEntry) pluginClasses.get(classname);
		if (entry == null)
		{
			entry = new PluginEntry(classname);
			pluginClasses.put(classname, entry);
		}

		entry.addInstance(plugin);
	}

	/**
	 * Removes the instance of a plugin with the given uniqueID.
	 *
	 * @param uniqueId Id of the plugin to remove
	 * @return
	 *		true	There are no instances of this plugin left.<br>
	 *		false	There is at least one instance alive.
	 */
	public boolean removeInstance(String uniqueId)
	{
		return removeInstance(getPlugin(uniqueId));
	}

	/**
	 * Removes a plugin from its container, uninstalls it and deletes it.
	 *
	 * @param plugin Plugin to remove
	 * @return
	 *		true	There are no instances of this plugin left.<br>
	 *		false	There is at least one instance alive.
	 */
	public boolean removeInstance(Plugin plugin)
	{
		if (plugin == null)
		{
			return true;
		}

		PluginEntry entry = (PluginEntry) pluginClasses.get(plugin.getClass().getName());
		if (entry != null)
		{
			return entry.removeInstance(plugin);
		}

		return true;
	}

	/**
	 * Gets all currently available instances of a given plugin (identified by its class name).
	 *
	 * @param classname Plugin class
	 * @return An iterator of {@link Plugin} objects
	 */
	public Iterator getPluginInstances(String classname)
	{
		PluginEntry entry = (PluginEntry) pluginClasses.get(classname);

		return entry != null ? entry.getPluginInstances() : EmptyIterator.getInstance();
	}

	/**
	 * Gets the instance of a given plugin (identified by its class name).
	 * The plugin is expected to be present only once in the system.
	 *
	 * @param classname Plugin class
	 * @return The instance or null if no such plugin exists
	 */
	public Plugin getPluginInstance(String classname)
	{
		PluginEntry entry = (PluginEntry) pluginClasses.get(classname);
		if (entry != null)
		{
			Iterator it = entry.getPluginInstances();
			if (it.hasNext())
			{
				return (Plugin) it.next();
			}
		}

		return null;
	}

	/**
	 * Returns a list of all loaded plugins.
	 *
	 * @return A list of {@link Plugin} objects
	 */
	public List getPluginInstances()
	{
		List list = new ArrayList();

		for (Iterator it = pluginClasses.values().iterator(); it.hasNext();)
		{
			PluginEntry entry = (PluginEntry) it.next();

			for (Iterator it2 = entry.getPluginInstances(); it2.hasNext();)
			{
				list.add(it2.next());
			}
		}

		return list;
	}

	/**
	 * Returns the class loader used to load plugins.
	 * Instantiated if not present yet.
	 * @nowarn
	 */
	ClassLoader getClassLoader()
	{
		if (classloader == null)
		{
			classloader = this.getClass().getClassLoader();
		}

		return classloader;
	}

	//////////////////////////////////////////////////
	// @@ Custom plugin support
	//////////////////////////////////////////////////

	/**
	 * This method loads custom plugins that may be specified in a property file.
	 * The property file entry (e. g. in the Cockpit.properties file) - if present - is expected to contain
	 * a space- or comma-separated list of plugin class names to load.
	 * Any error will be printed to the output, but ignored otherwise.
	 *
	 * \bNote\b: This function is preliminary until a full-fledged xml-based plugin loading mechanism
	 * has been implemented.
	 *
	 * @param settingName Name of the property file entry (e. g.
	 */
	public void loadCustomPlugins(String settingName)
	{
		String strList = SettingUtil.getStringSetting(settingName, null);
		if (strList != null)
		{
			StringTokenizer st = new StringTokenizer(strList, " ,");
			while (st.hasMoreTokens())
			{
				String pluginClassName = st.nextToken();
				if (pluginClassName.length() != 0)
				{
					try
					{
						createInstance(pluginClassName, ApplicationBase.getInstance());
					}
					catch (Exception e)
					{
						// Log as warning
						LogUtil.error(getClass(), "Error loading custom plugin $0.", pluginClassName, e);
						ExceptionUtil.printTrace(e);
					}
				}
			}
		}
	}

	/**
	 * This method loads custom plugins from the 'plugin' directory.
	 * This directory contains a sub directory for each plugin.
	 * Each sub directory contains a plugin profile ending with '.plugin.xml' and any jar files needed by the plugin.
	 */
	public void loadPluginsFromResource()
	{
		XMLDriver xmlDriver = XMLDriver.getInstance();
		try
		{
			Class [] profileClasses = { PluginProfile.class, };
			xmlDriver.loadMappings(profileClasses);
		}
		catch (XMLDriverException e)
		{
			ExceptionUtil.printTrace(e);
			return;
		}

		ResourceMgr resMgr = ResourceMgr.getDefaultInstance();
		String resourcePattern = CockpitConstants.PLUGIN + "/*/*.plugin.xml";
		Resource[] resources = null;

		try
		{
			resources = resMgr.findResources(resourcePattern);
		}
		catch (ResourceMgrException e)
		{
			return;
		}

		for (int i = 0; i < resources.length; i++)
		{
			PluginProfile profile = null;
			try
			{
				profile = (PluginProfile) xmlDriver.deserializeResource(PluginProfile.class, resources[i]);
			}
			catch (XMLDriverException e)
			{
				LogUtil.error(getClass(), "Error reading plugin profile $0.", resources[i].getDescription(), e);
				continue;
			}


			String pluginName = profile.getName();
			if (pluginName == null)
			{
				pluginName = profile.getClassName();
			}
			LogUtil.info(getClass(), "Loading plugin $0.", pluginName);

			try
			{
				ClassLoader pluginClassLoader = obtainPluginClassLoader(resources[i], pluginName);
				createInstance(profile, pluginClassLoader, ApplicationBase.getInstance());
			}
			catch (Throwable t)
			{
				// Log as warning
				LogUtil.error(getClass(), "Error loading custom plugin $0.", resources[i].getDescription(), t);
			}
		}
	}

	private ClassLoader obtainPluginClassLoader(Resource r, String pluginName)
	{
		ClassLoader cl = getClassLoader();
		if (r instanceof FileSystemResource)
		{
			XClassLoaderConfiguration config = new XClassLoaderConfiguration();
			config.setName("Plugin '" + pluginName + "' class loader");
			config.setParentClassLoader(cl);
			config.setLoggingEnabled(false);

			FilenameFilter fnf = new FilenameFilter()
			{
				public boolean accept(File dir, String name)
				{
					return name.endsWith(".jar");
				}
			};
			File pluginDir = null;
			try
			{
				pluginDir = r.getFile().getParentFile();
				File [] jars = pluginDir.listFiles(fnf);
				for (int i = 0; i < jars.length; ++i)
				{
					config.addRepository(jars[i].getAbsolutePath());
					LogUtil.info(getClass(), "Loading repository for plugin $0: $1.", pluginName, jars[i]);
				}

				cl = new XClassLoader(config);
			}
			catch (Exception e)
			{
				ExceptionUtil.printTrace(e);
			}
		}
		return cl;
	}


	/////////////////////////////////////////////////////////////////////////
	// @@ Inner classes
	/////////////////////////////////////////////////////////////////////////

	/**
	 * A PluginEntry contains various information about a particular plugin type.
	 */
	protected class PluginEntry
	{
		/////////////////////////////////////////////////////////////////////////
		// @@ Members
		/////////////////////////////////////////////////////////////////////////

		/** Class name */
		private String pluginClassName;

		/** Separate class loader for the plugin or null */
		private ClassLoader pluginClassLoader;

		/** Plugin profile */
		private PluginProfile profile;

		/** Class of this plugin. Can be null if the class is not yet loaded. */
		protected transient Class pluginClass;

		/** Table containing the existing instances of this plugin as unique id (String) - {@link Plugin} pair. */
		protected Map instances;

		/** Status of this plugin */
		protected int status;

		/////////////////////////////////////////////////////////////////////////
		// @@ Construction
		/////////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new plugin entry for a plugin with the given name.
		 *
		 * @param pluginClassName Plugin class name
		 */
		public PluginEntry(String pluginClassName)
		{
			this.pluginClassName = pluginClassName;
			status = PLUGIN_NOT_LOADED;
		}

		/**
		 * Creates a new plugin entry for a plugin based on the given profile.
		 *
		 * @param profile Plugin profile
		 */
		public PluginEntry(PluginProfile profile)
		{
			this.profile = profile;
			this.pluginClassName = profile.getClassName();
			status = PLUGIN_NOT_LOADED;
		}

		/////////////////////////////////////////////////////////////////////////
		// @@ Member Access
		/////////////////////////////////////////////////////////////////////////

		/**
		 * Gets the class name.
		 * @nowarn
		 */
		public String getPluginClassName()
		{
			return pluginClassName;
		}

		/**
		 * Gets the separate class loader for the plugin or null.
		 * @nowarn
		 */
		public ClassLoader getPluginClassLoader()
		{
			if (pluginClassLoader != null)
			{
				return pluginClassLoader;
			}
			return getClassLoader();
		}

		/**
		 * Sets the separate class loader for the plugin or null.
		 * @nowarn
		 */
		public void setPluginClassLoader(ClassLoader pluginClassLoader)
		{
			this.pluginClassLoader = pluginClassLoader;
		}

		/**
		 * Returns the class-object of the plugin. If it has not been
		 * loaded yet, load it via the class loader of the pluginManager.
		 * @nowarn
		 */
		private Class getPluginClass()
		{
			if (pluginClass == null && status != PLUGIN_LOAD_FAILED)
			{
				try
				{
					ClassLoader cl = getPluginClassLoader();
					pluginClass = cl.loadClass(pluginClassName);
					status = PLUGIN_CLASS_LOADED;
				}
				catch (ClassNotFoundException e)
				{
					ExceptionUtil.printTrace(e);
					status = PLUGIN_LOAD_FAILED;
				}
			}

			return pluginClass;
		}

		/**
		 * Returns the instance of the plugin with the given unique id
		 * or null if none such present.
		 * @nowarn
		 */
		public Plugin getPluginInstance(String uniqueId)
		{
			return (Plugin) instances.get(uniqueId);
		}

		/**
		 * Returns an iterator over all instances of this plugin.
		 * @nowarn
		 */
		public Iterator getPluginInstances()
		{
			return instances == null ? EmptyIterator.getInstance() : instances.values().iterator();
		}

		/////////////////////////////////////////////////////////////////////////
		// @@ Instantiation
		/////////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of a plugin with the given state data.
		 *
		 * @param state Plugin state for the new plugin
		 * @param parent Parent for the new plugin in the plugin tree
		 * @return The new plugin or null if it could not be instantiated
		 */
		public Plugin createInstance(PluginState state, Plugin parent)
		{
			Plugin instance = createInstance(parent);

			if (instance != null)
			{
				instance.setPluginState(state);
			}

			return instance;
		}

		/**
		 * Creates a new instance of the plugin.
		 *
		 * @param parent Parent for the new plugin in the plugin tree
		 * @return The new plugin or null if it could not be instantiated
		 */
		public Plugin createInstance(Plugin parent)
		{
			// We retrieve the class
			Class appClass = getPluginClass();

			Plugin plugin = null;

			if (appClass != null)
			{
				try
				{
					// Create the plugin instance
					ClassLoader cl = getPluginClassLoader();
					plugin = (Plugin) ReflectUtil.instantiate(appClass, cl, Plugin.class, "plugin");

					if (!ConfigMgr.getInstance().evaluate(plugin.getCondition()))
					{
						// Plugin condition evaluation failed, don't create
						return null;
					}

					if (profile != null && (plugin instanceof AbstractPlugin))
					{
						((AbstractPlugin) plugin).initializeFromPluginProfile(profile);
					}
					plugin.initializePlugin();

					status = PLUGIN_HAS_INSTANCES;

					addInstance(plugin);

					plugin.setParentPlugin(parent);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					status = PLUGIN_LOAD_FAILED;
				}
			}

			return plugin;
		}

		/**
		 * Adds an existing instance of a plugin.
		 *
		 * @param plugin Plugin to add
		 */
		public void addInstance(Plugin plugin)
		{
			if (instances == null)
			{
				instances = new HashMap();
			}

			if (instances.isEmpty())
			{
				// First instance loaded
				plugin.installFirstPlugin();
			}

			plugin.installPlugin();

			instances.put(plugin.getUniqueId(), plugin);
		}

		/**
		 * Removes the given plugin from the user interface structure as well as from the plugin manager itself.
		 * Causes the Plugin to uninstall itself before closing.
		 *
		 * @param plugin Plugin to remove
		 * @return
		 *		true	There are no instances of this plugin left.<br>
		 *		false	There is at least one instance alive.
		 */
		public boolean removeInstance(Plugin plugin)
		{
			// Check if this instance is still registered, don't uninstall twice
			if (instances.get(plugin.getUniqueId()) != null)
			{
				// Remove plugin from the ui if visible
				if (plugin instanceof VisiblePlugin)
				{
					VisiblePlugin vp = (VisiblePlugin) plugin;
					if (vp.getPluginHolder() != null)
					{
						vp.getPluginHolder().unlinkHolder();
					}
				}

				plugin.uninstallPlugin();

				instances.remove(plugin.getUniqueId());
				if (instances.size() == 0)
				{
					plugin.uninstallLastPlugin();
					status = PLUGIN_CLASS_LOADED;
				}
			}

			return instances.isEmpty();
		}
	}
}
