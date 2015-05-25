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
package org.openbp.guiclient.plugins.displayobject;

import java.util.ArrayList;
import java.util.List;

import org.openbp.common.rc.ResourceCollection;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.gui.plugin.ApplicationBase;
import org.openbp.jaspira.option.BooleanOption;
import org.openbp.jaspira.option.LocalizableOptionString;
import org.openbp.jaspira.option.Option;
import org.openbp.jaspira.option.OptionMgr;
import org.openbp.jaspira.option.OptionWidget;
import org.openbp.jaspira.option.StringOption;
import org.openbp.jaspira.option.widget.RadioWidget;
import org.openbp.jaspira.plugin.AbstractPlugin;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.jaspira.plugin.OptionModule;
import org.openbp.jaspira.plugin.PluginMgr;

/**
 * The role option manager keeps track of various settings that depend on the role
 * of the current user.
 * This class is a plugin, but is also accessible as a singleton.
 *
 * @author Heiko Erhardt
 */
public final class DisplayObjectPlugin extends AbstractPlugin
{
	//////////////////////////////////////////////////
	// @@ Title mode
	//////////////////////////////////////////////////

	/**
	 * Object title display mode.
	 * This option corresponds to the option "roleoption.titlemode".
	 */
	protected int titleMode = TITLE_TEXT;

	/** Display object name as title */
	public static final int TITLE_NAME = 1;

	/** Display object display text as title */
	public static final int TITLE_TEXT = 2;

	/** Titlemode option */
	private static final String TITLEMODE_OPTION = "displayobject.options.titlemode";

	/** Auto display name option */
	public static final String AUTODISPLAYNAME_OPTION = "displayobject.options.autodisplayname";

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Singleton instance */
	private static DisplayObjectPlugin singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	// TODO Cleanup 4 Move this to cockpit

	public String getResourceCollectionContainerName()
	{
		return "plugin.standard";
	}

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized DisplayObjectPlugin getInstance()
	{
		if (singletonInstance == null)
		{
			singletonInstance = (DisplayObjectPlugin) PluginMgr.getInstance().createInstance(DisplayObjectPlugin.class, ApplicationBase.getInstance());
		}
		return singletonInstance;
	}

	/**
	 * Fires an event to indicate that a role option has changed.
	 *
	 * @param eventName Name of the event to fire.<br>
	 * This should be one of the "displayobject.changed...." events
	 */
	private void fireChanged(String eventName)
	{
		fireEvent(eventName);
	}

	//////////////////////////////////////////////////
	// @@ Title mode
	//////////////////////////////////////////////////

	/**
	 * Determines if the object title display mode is set to 'text'.
	 * @return
	 *		true	Display the object display text.<br>
	 *		false	Display the object name.
	 */
	public boolean isTitleModeText()
	{
		return titleMode == TITLE_TEXT;
	}

	/**
	 * Gets the object title display mode.
	 * @return {@link #TITLE_NAME}/{@link #TITLE_TEXT}
	 */
	public int getTitleMode()
	{
		return titleMode;
	}

	/**
	 * Sets the object title display mode.
	 * @param titleMode {@link #TITLE_NAME}/{@link #TITLE_TEXT}
	 */
	public void setTitleMode(int titleMode)
	{
		this.titleMode = titleMode;
	}

	/**
	 * Reads the current value of the title mode from the option manager.
	 */
	// TODO Feature 5: Private method unused; made protected to prevent compiler warning 
	protected void readTitleModeOption()
	{
		String value = OptionMgr.getInstance().getStringOption(TITLEMODE_OPTION, "text");

		if ("name".equals(value))
			setTitleMode(TITLE_NAME);
		else
			setTitleMode(TITLE_TEXT);
	}

	/**
	 * Saves the current value of the title mode to the option manager.
	 * Also issues the displayobject.changed.titlemode event
	 */
	public void saveTitleModeOption()
	{
		String value = null;
		if (getTitleMode() == TITLE_NAME)
			value = "name";
		else
			value = "text";

		Option option = OptionMgr.getInstance().getOption(TITLEMODE_OPTION);
		option.setValue(value);

		fireChanged("displayobject.changed.titlemode");
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Interaction and action modules
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Event module.
	 */
	public class Events extends EventModule
	{
		public String getName()
		{
			return "plugin.displayobject";
		}

		/**
		 * Action event handler for the 'Title mode toggle' toolbar button.
		 *
		 * @event displayobject.options.titlemode
		 * @nowarn
		 */
		public JaspiraEventHandlerCode titlemode(JaspiraActionEvent jae)
		{
			if (titleMode == TITLE_NAME)
				titleMode = TITLE_TEXT;
			else
				titleMode = TITLE_NAME;
			saveTitleModeOption();

			return EVENT_HANDLED;
		}
	}

	//////////////////////////////////////////////////
	// @@ Option module
	//////////////////////////////////////////////////

	/**
	 * Global Cockpit options.
	 */
	public class RoleOptionModule extends OptionModule
	{
		/**
		 * Global option to set the title display mode.
		 * Either displays object names or object display text as title of an object.
		 */
		public class TitleOption extends StringOption
		{
			public TitleOption()
			{
				super(getPluginResourceCollection(), TITLEMODE_OPTION, "text");
			}

			/**
			 * @see org.openbp.jaspira.option.Option#createOptionWidget()
			 */
			public OptionWidget createOptionWidget()
			{
				List values = new ArrayList();

				ResourceCollection res = getPluginResourceCollection();
				values.add(new LocalizableOptionString(res.getRequiredString("displayobject.options.titlemode.value.text"), "text"));
				values.add(new LocalizableOptionString(res.getRequiredString("displayobject.options.titlemode.value.name"), "name"));

				return new RadioWidget(this, values);
			}
		}

		/**
		 * Boolean option that is true if the parameter value wizard is enabled.
		 */
		public class AutoGenerateDisplayNameOption extends BooleanOption
		{
			public AutoGenerateDisplayNameOption()
			{
				super(getPluginResourceCollection(), AUTODISPLAYNAME_OPTION, Boolean.FALSE);
			}
		}
	}
}
