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
package org.openbp.jaspira.event;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.openbp.jaspira.plugin.Plugin;

/**
 * Event that may contain Runnable actions that are executed when the {@link #performActions} method is called.
 *
 * @author Stephan Moritz
 */
public class StackActionEvent extends JaspiraEvent
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Data members
	/////////////////////////////////////////////////////////////////////////

	/**
	 * List of actions that should be performed when the {@link #performActions} method is called.
	 * Contains Runnable objects.
	 */
	private List actions;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 * @nowarn
	 */
	public StackActionEvent(Plugin source, String eventName, Object object, int type, int level, int flags)
	{
		super(source, eventName, object, type, level, flags);
	}

	/**
	 * Constructor.
	 * @nowarn
	 */
	public StackActionEvent(Plugin source, String eventName)
	{
		super(source, eventName);
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Adds an action to this action stack.
	 *
	 * @param action Action to add
	 */
	public void addAction(Runnable action)
	{
		if (actions == null)
		{
			actions = new LinkedList();
		}

		actions.add(action);
	}

	/**
	 * Performs all actions in the stack.
	 */
	public void performActions()
	{
		if (actions == null)
		{
			return;
		}

		for (Iterator it = actions.iterator(); it.hasNext();)
		{
			((Runnable) it.next()).run();
			it.remove();
		}
	}
}
