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
package org.openbp.guiclient.plugins.server;

import org.openbp.common.ExceptionUtil;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.modelmgr.ModelNotificationService;
import org.openbp.core.remote.ClientSession;
import org.openbp.guiclient.remote.ServerConnection;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.event.JaspiraEventMgr;
import org.openbp.jaspira.plugin.AbstractPlugin;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.swing.components.JMsgBox;

/**
 * Unvisible plugin for common options
 *
 * @author Andreas Putz
 */
public class ServerConnectionPlugin extends AbstractPlugin
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public ServerConnectionPlugin()
	{
	}

	public String getResourceCollectionContainerName()
	{
		return "plugin.standard";
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Modules
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Event module.
	 */
	public class Events extends EventModule
	{
		public String getName()
		{
			return "plugin.serverconnection";
		}

		//////////////////////////////////////////////////
		// @@ Event handling methods
		//////////////////////////////////////////////////

		/**
		 * Event handler: Triggers the reloads of classes at the server.
		 *
		 * @event plugin.serverconnection.reload
		 * @param jae Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode reload(JaspiraActionEvent jae)
		{
			ServerConnection serverConnection = ServerConnection.getInstance();

			if (! serverConnection.isConnected())
			{
				try
				{
					// If we are not connected, try to connect to the server
					serverConnection.connect(true);

					JaspiraEventMgr.fireGlobalEvent("plugin.serverconnection.reconnect");

					String message = getPluginResourceCollection().getRequiredString("reconnect.okmessage");
					JMsgBox.show(null, message, JMsgBox.ICON_INFO);
				}
				catch (OpenBPException e)
				{
					ExceptionUtil.printTrace(e);
					String message = getPluginResourceCollection().getRequiredString("reconnect.failedmessage");
					String exceptionMessage = ExceptionUtil.getNestedMessage(e);
					JMsgBox.showFormat(null, message, exceptionMessage, JMsgBox.ICON_ERROR);
				}
			}
			else
			{
				try
				{
					ClientSession session = ServerConnection.getInstance().getSession();

					ModelNotificationService mns = (ModelNotificationService) serverConnection.lookupService(ModelNotificationService.class);
					mns.requestModelReset(session);

					String message = getPluginResourceCollection().getRequiredString("okmessage");
					JMsgBox.show(null, message, JMsgBox.ICON_INFO);
				}
				catch (OpenBPException e)
				{
					ExceptionUtil.printTrace(e);
					String message = getPluginResourceCollection().getRequiredString("failedmessage");
					JMsgBox.show(null, message, JMsgBox.ICON_ERROR);
				}
			}

			return EVENT_CONSUMED;
		}
	}
}
