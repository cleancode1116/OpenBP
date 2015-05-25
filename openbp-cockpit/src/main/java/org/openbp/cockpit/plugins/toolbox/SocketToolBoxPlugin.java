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
package org.openbp.cockpit.plugins.toolbox;

import java.awt.event.KeyEvent;

import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.NodeSocketImpl;

/**
 * The ToolBox with two Sockets
 *
 * @author Jens Ferchland
 */
public class SocketToolBoxPlugin extends ToolBoxPlugin
{
	/**
	 * @see org.openbp.cockpit.plugins.toolbox.ToolBoxPlugin#addStandardToolBoxItems()
	 */
	protected void addStandardToolBoxItems()
	{
		NodeSocket socket = new NodeSocketImpl();

		// Add entry socket
		socket = new NodeSocketImpl();
		socket.setEntrySocket(true);
		addToolBoxItem(socket, "toolboxitem.socketentry.tooltip");

		// Add exit socket
		socket = new NodeSocketImpl();
		socket.setEntrySocket(false);
		addToolBoxItem(socket, "toolboxitem.socketexit.tooltip");
	}

	/**
	 * @see org.openbp.cockpit.plugins.toolbox.ToolBoxPlugin#canTitleChange()
	 */
	protected boolean canTitleChange()
	{
		// Disable title change
		return false;
	}

	/**
	 * @see org.openbp.cockpit.plugins.toolbox.ToolBoxPlugin#acceptDrop()
	 */
	protected boolean acceptDrop()
	{
		// Disable drop
		return false;
	}

	/**
	 * @see org.openbp.cockpit.plugins.toolbox.ToolBoxPlugin#acceptFlyWheelKey(int)
	 */
	protected boolean acceptFlyWheelKey(int key)
	{
		// Trigger this toolbox using the space key
		return key == KeyEvent.VK_SPACE;
	}
}
