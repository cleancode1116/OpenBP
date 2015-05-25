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
package org.openbp.cockpit.modeler.paramvaluewizard;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import org.openbp.cockpit.modeler.Modeler;
import org.openbp.cockpit.modeler.figures.process.SocketFigure;
import org.openbp.core.model.item.process.DataLink;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.guiclient.plugins.displayobject.DisplayObjectPlugin;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.event.InteractionEvent;

/**
 * Helper class that adds a parameter visibility sub menu to a popup menu of a node socket or a parameter.
 *
 * @author Heiko Erhardt
 */
public class ParamVisibilityHelper
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private ParamVisibilityHelper()
	{
	}

	//////////////////////////////////////////////////
	// @@ Helper methods
	//////////////////////////////////////////////////

	/**
	 * Adds the param sub menu to the popup menu refering to the given node socket.
	 *
	 * @param ie Interaction event that queries plugins for popup menus
	 * @param modeler Modeler displaying the process that contains the socket
	 * @param socketFigure The referred socket figure
	 */
	public static void addParamMenu(InteractionEvent ie, final Modeler modeler, final SocketFigure socketFigure)
	{
		// Popup menu item group
		JaspiraAction group = null;

		NodeSocket socket = socketFigure.getNodeSocket();
		for (Iterator it = socket.getParams(); it.hasNext();)
		{
			final NodeParam param = (NodeParam) it.next();

			// Create an action that toggles the parameter visibility
			JaspiraAction action = new JaspiraAction(modeler, "modeler.edit.paramvisibility.prototype")
			{
				public void actionPerformed(ActionEvent e)
				{
					// Reinitialize the socket contents
					modeler.startUndo("Show/hide Parameter");

					modeler.getDrawingView().clearSelection();

					// Toggle the parameter visibility
					param.setVisible(!param.isVisible());

					// Rebuild the parmeter list, displaying the visible parameters only
					socketFigure.reinitParams(false);

					modeler.endUndo();

					modeler.getDrawingView().redraw();
				}
			};

			// Provide the parameter name or display name as action title
			String name;
			if (DisplayObjectPlugin.getInstance().isTitleModeText())
				name = param.getDisplayText();
			else
				name = param.getName();
			action.setDisplayName(name);

			// Enable the action only if we may hide the parameter
			boolean canHide = !param.isVisible() || canHideParam(param);
			action.setEnabled(canHide);

			// The action selection state reflects the parameter visiblity
			action.setSelected(param.isVisible());

			// Add the action to the 'Parameters' group
			if (group == null)
			{
				// TODO Feature 4 Add the 'show/hide all' menu items

				// Create the 'Parameters' menu group
				group = new JaspiraAction(modeler, "modeler.edit.paramvisibility");
			}
			group.addMenuChild(action);
		}

		if (group != null)
		{
			// Add the group to the interaction event to create the sub menu
			ie.add(group);
		}
	}

	/**
	 * Determines if this parameter can be hidden.
	 *
	 * @param param Parameter to check
	 * @return
	 * true: The parameter is not connected.<br>
	 * false: The parameter is connected to a data link or has an expression assigned.
	 */
	public static boolean canHideParam(NodeParam param)
	{
		if (param.getExpression() != null)
		{
			// Expression detected
			return false;
		}

		ProcessItem process = param.getProcess();
		if (process != null)
		{
			for (Iterator it = process.getDataLinks(); it.hasNext();)
			{
				DataLink link = (DataLink) it.next();
				if (param == link.getSourceParam() || param == link.getTargetParam())
				{
					// Source or target of a data link
					return false;
				}
			}
		}

		return true;
	}
}
