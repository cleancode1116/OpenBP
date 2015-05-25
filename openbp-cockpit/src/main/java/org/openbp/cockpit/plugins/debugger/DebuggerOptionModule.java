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
package org.openbp.cockpit.plugins.debugger;

import java.util.ArrayList;
import java.util.List;

import org.openbp.common.rc.ResourceCollection;
import org.openbp.jaspira.option.BooleanOption;
import org.openbp.jaspira.option.LocalizableOptionString;
import org.openbp.jaspira.option.Option;
import org.openbp.jaspira.option.OptionWidget;
import org.openbp.jaspira.option.StringOption;
import org.openbp.jaspira.option.widget.SelectionWidget;
import org.openbp.jaspira.plugin.ExternalOptionModule;
import org.openbp.jaspira.plugin.Plugin;

/**
 * Option module containing all debugger options.
 *
 * @author Jens Ferchland
 */
public class DebuggerOptionModule extends ExternalOptionModule
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Link trace flag: Skip link */
	private static final String LINKTRACE_SKIP_STR = "skip";

	/** Link trace flag: Show link target only */
	private static final String LINKTRACE_TARGET_STR = "target";

	/** Link trace flag: Show link animation and stop at target */
	private static final String LINKTRACE_ANIMATION_STOP_STR = "animationstop";

	/** Link trace flag: Show link animation and continue execution */
	private static final String LINKTRACE_ANIMATION_GO_STR = "animationgo";

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param plugin The plugin this module is associated with
	 */
	public DebuggerOptionModule(Plugin plugin)
	{
		super(plugin);
	}

	//////////////////////////////////////////////////
	// @@ Autosave
	//////////////////////////////////////////////////

	/**
	 * Boolean option that determines if edited objects should be saved automatically when issueing a run/step command.
	 */
	public class AutosaveOption extends BooleanOption
	{
		public AutosaveOption()
		{
			super(getPlugin().getPluginResourceCollection(), "debugger.autosave", Boolean.TRUE);
		}
	}

	/**
	 * Boolean option that determines if visual processes should be skipped during single-stepping.
	 */
	public class SkipSystemModelOption extends BooleanOption
	{
		public SkipSystemModelOption()
		{
			super(getPlugin().getPluginResourceCollection(), "debugger.skipsystemmodel", Boolean.TRUE);
		}
	}

	/**
	 * Boolean option that determines if visual processes should be skipped during single-stepping.
	 */
	public class DebuggerIdOption extends StringOption
	{
		public DebuggerIdOption()
		{
			super(getPlugin().getPluginResourceCollection(), "debugger.debuggerid", null);
		}
	}

	//////////////////////////////////////////////////
	// @@ Control and data link trace
	//////////////////////////////////////////////////

	/**
	 * This option defines the way control links are handled by the debugger
	 */
	/* TODO Feature 6: Debugger control/data link trace mode
	 public class ControlLinkTraceModeOption extends LinkTraceOption
	 {
	 public ControlLinkTraceModeOption ()
	 {
	 super ("debugger.controllinktracemode", DebuggerPlugin.LINKTRACE_ANIMATION_STOP);
	 }
	 }
	 */
	/**
	 * This option defines the way data links are handled by the debugger
	 */
	/* TODO Feature 6: Debugger control/data link trace mode
	 public class DataLinkTraceModeOption extends LinkTraceOption
	 {
	 public DataLinkTraceModeOption ()
	 {
	 super ("debugger.datalinktracemode", DebuggerPlugin.LINKTRACE_ANIMATION_STOP);
	 }
	 }
	 */
	//////////////////////////////////////////////////
	// @@ Generic link trace
	//////////////////////////////////////////////////
	/**
	 * This is the Option that defines the shadow of the editor components.
	 */
	public abstract class LinkTraceOption extends Option
	{
		//////////////////////////////////////////////////
		// @@ construction
		//////////////////////////////////////////////////

		/**
		 * Constructor.
		 *
		 * @param name Name of the option
		 * @param defaultValue Default value of the option
		 */
		public LinkTraceOption(String name, Integer defaultValue)
		{
			super(getPlugin().getPluginResourceCollection(), name, defaultValue);
		}

		//////////////////////////////////////////////////
		// @@ option implementation
		//////////////////////////////////////////////////

		/**
		 * @see org.openbp.jaspira.option.Option#createOptionWidget()
		 */
		public OptionWidget createOptionWidget()
		{
			List values = new ArrayList(4);

			ResourceCollection res = getPlugin().getPluginResourceCollection();
			values.add(new LocalizableOptionString(res.getRequiredString("debugger.option.linktrace.skip"), DebuggerPlugin.LINKTRACE_SKIP));
			values.add(new LocalizableOptionString(res.getRequiredString("debugger.option.linktrace.target"), DebuggerPlugin.LINKTRACE_TARGET));
			values.add(new LocalizableOptionString(res.getRequiredString("debugger.option.linktrace.animationstop"), DebuggerPlugin.LINKTRACE_ANIMATION_STOP));
			values.add(new LocalizableOptionString(res.getRequiredString("debugger.option.linktrace.animationgo"), DebuggerPlugin.LINKTRACE_ANIMATION_GO));

			return new SelectionWidget(this, values);
		}

		/**
		 * @see org.openbp.jaspira.option.Option#loadFromString(String)
		 */
		public Object loadFromString(String str)
		{
			Object value = DebuggerPlugin.LINKTRACE_ANIMATION_STOP;

			if (str.equals(LINKTRACE_SKIP_STR))
				value = DebuggerPlugin.LINKTRACE_SKIP;
			else if (str.equals(LINKTRACE_TARGET_STR))
				value = DebuggerPlugin.LINKTRACE_TARGET;
			else if (str.equals(LINKTRACE_ANIMATION_STOP_STR))
				value = DebuggerPlugin.LINKTRACE_ANIMATION_STOP;
			else if (str.equals(LINKTRACE_ANIMATION_GO_STR))
				value = DebuggerPlugin.LINKTRACE_ANIMATION_GO;

			return value;
		}

		/**
		 * @see org.openbp.jaspira.option.Option#saveToString()
		 */
		public String saveToString()
		{
			Object value = getValue();
			String str = LINKTRACE_ANIMATION_STOP_STR;

			if (value == DebuggerPlugin.LINKTRACE_SKIP)
				str = LINKTRACE_SKIP_STR;
			else if (value == DebuggerPlugin.LINKTRACE_TARGET)
				str = LINKTRACE_TARGET_STR;
			else if (value == DebuggerPlugin.LINKTRACE_ANIMATION_STOP)
				str = LINKTRACE_ANIMATION_STOP_STR;
			else if (value == DebuggerPlugin.LINKTRACE_ANIMATION_GO)
				str = LINKTRACE_ANIMATION_GO_STR;

			return str;
		}
	}
}
