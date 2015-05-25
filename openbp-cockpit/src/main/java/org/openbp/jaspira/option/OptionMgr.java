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
package org.openbp.jaspira.option;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.tree.TreeNode;

import org.openbp.common.CommonUtil;
import org.openbp.common.setting.SettingUtil;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.gui.plugin.ApplicationBase;
import org.openbp.jaspira.plugin.AbstractPlugin;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.swing.SwingUtil;

/**
 * Singleton class that manages all options of the application.
 *
 * @author Jens Ferchland
 */
public final class OptionMgr extends AbstractPlugin
{
	//////////////////////////////////////////////////
	// @@ Static members
	//////////////////////////////////////////////////

	/** root of all otions in the optiontree */
	public static final String OPTIONROOT = "optionroot";

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Singleton instance */
	private static OptionMgr singletonInstance;

	/** Table of options mapping the option names to {@link Option} objects */
	private Map options;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of the option manager.
	 * @nowarn
	 */
	public static synchronized OptionMgr getInstance()
	{
		if (singletonInstance == null)
		{
			singletonInstance = new OptionMgr();
		}
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private OptionMgr()
	{
		options = new HashMap();

		// Create and add the root option to the manager
		addOption(new GroupOption(OPTIONROOT, "", "", null, 0));

		initializePlugin();
		installPlugin();

		// Set the application base as parent - to get all events.
		if (ApplicationBase.hasInstance())
		{
			setParentPlugin(ApplicationBase.getInstance());
		}
	}

	public String getResourceCollectionContainerName()
	{
		return "plugin.standard";
	}

	//////////////////////////////////////////////////
	// @@ Option access and management
	//////////////////////////////////////////////////

	/**
	 * Gets an option.
	 * @param optionName Name of the option
	 * @return The option or null if no such option exists
	 */
	public Option getOption(String optionName)
	{
		return (Option) options.get(optionName);
	}

	/**
	 * Gets a string option (convenience method).
	 *
	 * @param optionName Name of the option
	 * @param dfltValue Default value of the option
	 * @return The string value of the option or the default value if no such option exists
	 */
	public String getStringOption(String optionName, String dfltValue)
	{
		Option option = (Option) options.get(optionName);
		if (option != null)
		{
			Object o = option.getValue();
			if (o != null)
			{
				if (o instanceof String)
					return (String) o;
				System.err.println("Warning: Option '" + optionName + "' is not a string");
			}
		}
		return dfltValue;
	}

	/**
	 * Gets a boolean option (convenience method).
	 *
	 * @param optionName Name of the option
	 * @param dfltValue Default value of the option
	 * @return The boolean value of the option or the default value if no such option exists
	 */
	public boolean getBooleanOption(String optionName, boolean dfltValue)
	{
		Option option = (Option) options.get(optionName);
		if (option != null)
		{
			Object o = option.getValue();
			if (o != null)
			{
				if (o instanceof Boolean)
					return ((Boolean) o).booleanValue();
				System.err.println("Warning: Option '" + optionName + "' is not a boolean");
			}
		}
		return dfltValue;
	}

	/**
	 * Gets a integer option (convenience method).
	 *
	 * @param optionName Name of the option
	 * @param dfltValue Default value of the option
	 * @return The integer value of the option or the default value if no such option exists
	 */
	public int getIntegerOption(String optionName, int dfltValue)
	{
		Option option = (Option) options.get(optionName);
		if (option != null)
		{
			Object o = option.getValue();
			if (o != null)
			{
				if (o instanceof Integer)
					return ((Integer) o).intValue();
				System.err.println("Warning: Option '" + optionName + "' is not an integer");
			}
		}
		return dfltValue;
	}

	/**
	 * Sets an option value and fires an option event.
	 *
	 * @param optionName Name of the option to set
	 * @param optionValue New value of the option
	 */
	public void setOption(String optionName, Object optionValue)
	{
		Option option = (Option) options.get(optionName);
		if (option != null)
		{
			setOption(option, optionValue);
		}
	}

	/**
	 * Sets an option value and fires an option event.
	 * The name of the event equals the name of the option.
	 * The event object is the option itself.
	 *
	 * @param option Option to set
	 * @param newValue New value of the option
	 */
	public void setOption(Option option, Object newValue)
	{
		Object oldValue = option.getValue();
		if (!CommonUtil.equalsNull(oldValue, newValue))
		{
			option.setValue(newValue);

			// Fire the Event that the option has changed.
			fireEvent(option.getName(), option);
		}
	}

	/**
	 * Adds an option to the option manager.
	 * @nowarn
	 */
	public void addOption(Option option)
	{
		options.put(option.getName(), option);
	}

	/**
	 * Removes a option from the manager.
	 * @nowarn
	 */
	public void removeOption(Option option)
	{
		options.remove(option.getName());
	}

	/**
	 * Saves the option values to persistent storage.
	 */
	public void saveOptions()
	{
		SettingUtil.saveSettings(null);
	}

	/**
	 * Returns a tree node containing all options of the option manager.
	 * Called by the {@link OptionDialog}
	 *
	 * @return The root node of the option tree
	 */
	TreeNode createOptionTree()
	{
		return getOption(OPTIONROOT);
	}

	//////////////////////////////////////////////////
	// @@ Event module
	//////////////////////////////////////////////////

	/**
	 * Event module.
	 * Handles the global.interaction.openoptioneditor event.
	 */
	public class Events extends EventModule
	{
		public String getName()
		{
			return "optionmanager";
		}

		/**
		 * Gets the module priority.
		 * We are low priority.
		 *
		 * @return The priority. 0 is lowest, 100 is highest.
		 */
		public int getPriority()
		{
			return 100;
		}

		/**
		 * Event handler: Opens the option editor.
		 *
		 * @event optionmanager.openoptiondialog
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode openoptiondialog(JaspiraActionEvent je)
		{
			OptionDialog dlg = new OptionDialog(getPluginResourceCollection().getRequiredString("optiondialogtitle"));
			SwingUtil.show(dlg);

			// Reset the cached widgets, so they will be reconstructed next time
			// the option editor is shown.
			for (Iterator it = options.values().iterator(); it.hasNext();)
			{
				Option option = (Option) it.next();
				option.resetCachedOptionWidget();
			}

			return EVENT_CONSUMED;
		}
	}
}
