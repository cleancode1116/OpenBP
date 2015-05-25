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
package org.openbp.jaspira.plugins.errordialog;

import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.plugin.AbstractPlugin;
import org.openbp.jaspira.plugin.ApplicationUtil;
import org.openbp.jaspira.plugin.EventModule;

/**
 * Pseudo plugin to get access to a resource file and
 * have an central point to get access to the
 * options for the problem report
 *
 * @author Andreas Putz
 */
public class ErrorDialogPlugin extends AbstractPlugin
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public ErrorDialogPlugin()
	{
	}

	public String getResourceCollectionContainerName()
	{
		return "plugin.standard";
	}

	//////////////////////////////////////////////////
	// @@ Event module
	//////////////////////////////////////////////////

	/**
	 * Event module.
	 */
	public class Events extends EventModule
	{
		public String getName()
		{
			return "plugin.errordialog";
		}

		//////////////////////////////////////////////////
		// @@ Problem dialog event
		//////////////////////////////////////////////////

		/**
		 * Event handler: show error dialog.
		 *
		 * @event plugin.errordialog.showerror
		 * @param e Problem event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode showerror(ErrorEvent e)
		{
			ErrorDialog.setResourceCollection(ErrorDialogPlugin.this.getPluginResourceCollection());
			if (e.getThrowable() != null)
			{
				ErrorDialog.showDialog(ApplicationUtil.getActiveWindow(), true, e.getMessage(), e.getThrowable());
			}
			else
			{
				ErrorDialog.showDialog(ApplicationUtil.getActiveWindow(), true, e.getMessage(), e.getExceptionString());
			}

			return EVENT_CONSUMED;
		}
	}
}
