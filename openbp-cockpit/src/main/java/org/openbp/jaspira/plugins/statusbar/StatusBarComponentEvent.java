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
package org.openbp.jaspira.plugins.statusbar;

import java.awt.Component;

import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.plugin.Plugin;

/**
 * Event used to control the statusbar and its components.
 *
 * @author Jens Ferchland
 */
public class StatusBarComponentEvent extends JaspiraEvent
{
	//////////////////////////////////////////////////
	// @@ constants
	//////////////////////////////////////////////////

	/** Adds a component to the statusbar */
	public static final int ADD = 1;

	/** Removes a Component from the statusbar */
	public static final int REMOVE = 2;

	/** updates/replaces a component in the statusbar */
	public static final int REPLACE = 3;

	//////////////////////////////////////////////////
	// @@ memebers
	//////////////////////////////////////////////////

	/** Type of operation to perform */
	private int operation;

	/** The new Component */
	private Component newComponent;

	/** The old Component */
	private Component oldComponent;

	//////////////////////////////////////////////////
	// @@ construction
	//////////////////////////////////////////////////

	/**
	 * Constructor for {@link #ADD} or {@link #REMOVE} operations.
	 *
	 * @param plugin Source plugin
	 * @param operation Type of operation to perform
	 * @param comp Component to add or remve
	 */
	public StatusBarComponentEvent(Plugin plugin, int operation, Component comp)
	{
		super(plugin, "statusbar.managecomponent");

		this.operation = operation;
		switch (operation)
		{
		case ADD:
			newComponent = comp;
			break;

		default:
			oldComponent = comp;
			break;
		}
	}

	/**
	 * Constructor for {@link #REPLACE} operations.
	 *
	 * @param plugin Source plugin
	 * @param oldComponent Component to replace
	 * @param newComponent New component
	 */
	public StatusBarComponentEvent(Plugin plugin, Component oldComponent, Component newComponent)
	{
		super(plugin, "statusbar.managecomponent");

		this.operation = REPLACE;
		this.oldComponent = oldComponent;
		this.newComponent = newComponent;
	}

	//////////////////////////////////////////////////
	// @@ member access
	//////////////////////////////////////////////////

	/**
	 * Gets the type of operation to perform.
	 * @nowarn
	 */
	public int getOperation()
	{
		return operation;
	}

	/**
	 * Gets the the new Component.
	 * @nowarn
	 */
	public Component getNewComponent()
	{
		return newComponent;
	}

	/**
	 * Gets the the old Component.
	 * @nowarn
	 */
	public Component getOldComponent()
	{
		return oldComponent;
	}
}
