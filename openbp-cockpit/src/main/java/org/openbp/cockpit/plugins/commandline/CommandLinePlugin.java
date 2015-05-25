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
package org.openbp.cockpit.plugins.commandline;

import org.openbp.common.application.Application;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.guiclient.event.OpenEvent;
import org.openbp.jaspira.plugin.AbstractPlugin;

/**
 * Plugin that checks the command line options of the cockpit and loads processes
 * if specified by the -process option.
 *
 * @author Jens Ferchland
 */
public class CommandLinePlugin extends AbstractPlugin
{
	/** Open process command line option */
	public static final String OPENPROCESS = "-process";

	public String getResourceCollectionContainerName()
	{
		return "plugin.cockpit";
	}

	/**
	 * @see org.openbp.jaspira.plugin.Plugin#installFirstPlugin()
	 */
	public void installFirstPlugin()
	{
		super.installFirstPlugin();

		String [] args = Application.getArguments();
		for (int i = 0; i < args.length; i++)
		{
			if (args [i].equals(OPENPROCESS))
			{
				if (i == args.length - 1)
				{
					System.err.println("Missing argument for " + OPENPROCESS + " option");
					continue;
				}

				ModelQualifier qualifier = new ModelQualifier(args [i + 1]);
				qualifier.setItemType(ItemTypes.PROCESS);
				fireEvent(new OpenEvent(this, "open.modeler", qualifier));
			}
		}
	}
}
