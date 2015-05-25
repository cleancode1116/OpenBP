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
package org.openbp.jaspira.gui.plugin;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.lang.ref.WeakReference;

import javax.swing.DefaultFocusManager;
import javax.swing.FocusManager;

/**
 * Focus manager which manages the focusation of plugins.
 * This class is a singleton.
 *
 * @author Jens Ferchland
 */
public final class PluginFocusMgr extends DefaultFocusManager
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Singleton instance */
	private static PluginFocusMgr singletonInstance;

	/**
	 * Plugin that had the focus last (contains a {@link VisiblePlugin}).
	 * This is a temporary variable only, so we use weak references here in order to prevent memory leaks.
	 */
	protected WeakReference lastPluginRef;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Returns the current focus manager if it is a PluginFocusMgr or some subclass of it
	 * or otherwise the only instance of this class.
	 *
	 * @return The plugin focus manager
	 */
	public static synchronized PluginFocusMgr getInstance()
	{
		FocusManager focusMgr = FocusManager.getCurrentManager();
		if (focusMgr instanceof PluginFocusMgr)
		{
			return (PluginFocusMgr) focusMgr;
		}

		if (singletonInstance == null)
			singletonInstance = new PluginFocusMgr();
		return singletonInstance;
	}

	/**
	 * Constructor.
	 */
	private PluginFocusMgr()
	{
		super();
	}

	/**
	 * @see java.awt.DefaultKeyboardFocusManager#dispatchEvent(AWTEvent)
	 */
	public boolean dispatchEvent(AWTEvent e)
	{
		if (e instanceof FocusEvent)
		{
			VisiblePlugin plugin = AbstractVisiblePlugin.getPluginFromComponentHierarchy((Component) e.getSource());
			if (plugin != null)
			{
				changeFocus(plugin);
			}
		}
		return super.dispatchEvent(e);
	}

	/**
	 * Changes the focus from the currently focused plugin to the given plugin.
	 * If the plugin already has the focus, nothing will happen.
	 *
	 * @param plugin Plugin to receive the focus
	 */
	public void changeFocus(VisiblePlugin plugin)
	{
		// Get the last focused plugin from the weak reference
		VisiblePlugin lastPlugin = null;
		if (lastPluginRef != null)
			lastPlugin = (VisiblePlugin) lastPluginRef.get();

		if (plugin != lastPlugin)
		{
			// Save the new focused plugin as weak reference
			lastPluginRef = new WeakReference(plugin);

			if (lastPlugin != null)
			{
				lastPlugin.firePluginFocusLost();
			}

			plugin.firePluginFocusGained();

			JaspiraPage page = plugin.getPage();
			if (page != null)
			{
				// Save the currently focused plugin in the page for focus restoration
				// in case of page switch
				page.setFocusedPlugin(plugin);
			}
		}
	}

	/**
	 * Returns the currently focused plugin.
	 * If the currently focused plugin has been garbage-collected,
	 * the method will try a fallback to the active plugin of the currenly focused frame.
	 * @return The focused plugin or null if no plugin is focused
	 */
	public VisiblePlugin getFocusedPlugin()
	{
		VisiblePlugin lastPlugin = null;
		if (lastPluginRef != null)
		{
			lastPlugin = (VisiblePlugin) lastPluginRef.get();
			if (lastPlugin != null)
				return lastPlugin;
		}

		VisiblePlugin plugin = ApplicationBase.getInstance().getActivePlugin();
		if (plugin != null)
			return plugin;

		return null;
	}

	/**
	 * Resets the focus cache of the plugin focus manager.
	 * Call this method if you want a plugin that already has the focus to be focused explicitely again
	 * if its {@link VisiblePlugin#focusPlugin} method is being called.
	 * This is used e. g. if a plugin is dragged to another container to repaint the plugin panel title.
	 */
	public void resetFocusCache()
	{
		lastPluginRef = null;
	}
}
