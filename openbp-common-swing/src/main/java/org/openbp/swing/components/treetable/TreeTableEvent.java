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
package org.openbp.swing.components.treetable;

import java.util.EventObject;

/**
 * Tree table event.
 *
 * @author Heiko Erhardt
 */
public class TreeTableEvent extends EventObject
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Command event */
	public static final int EVENT_COMMAND = 1;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Flag that the event has been consumed */
	private boolean consumed;

	/** Event type */
	private int type;

	/** Command. (See the JTreeTable.CMD_* constants) */
	private int command;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param source Tree table that fired the event
	 * @param type Event type (see the constants of this class)
	 */
	public TreeTableEvent(JTreeTable source, int type)
	{
		super(source);
		this.type = type;
	}

	/**
	 * Sets the flag that the event has been consumed.
	 */
	public void consume()
	{
		consumed = true;
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Gets the flag that the event has been consumed.
	 * @nowarn
	 */
	public boolean isConsumed()
	{
		return consumed;
	}

	/**
	 * Gets the event type.
	 * @nowarn
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * Gets the command.
	 * @return See the JTreeTable.CMD_* constants
	 */
	public int getCommand()
	{
		return command;
	}

	/**
	 * Sets the command. (See the JTreeTable.CMD_* constants).
	 * @param command See the JTreeTable.CMD_* constants
	 */
	public void setCommand(int command)
	{
		this.command = command;
	}
}
