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
package org.openbp.jaspira.action;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The (singleton) action manager provides access to Jaspira actions.
 * This can be used to globally activate or deactivate a single action that is to be
 * shared between plugins (load, save, etc.).
 *
 * @author Stephan Moritz
 */
public final class ActionMgr
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Data members
	/////////////////////////////////////////////////////////////////////////

	/** Table that maps action names to actions ({@link JaspiraAction} objects) */
	private Map actions;

	/** Singleton instance. */
	private static ActionMgr singleton;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor. Do not use, use getInstance () instead.
	 */
	private ActionMgr()
	{
		super();

		actions = new HashMap();

		// put the menu root as fixed action
		actions.put(JaspiraAction.MENU_ROOT, new JaspiraAction(JaspiraAction.MENU_ROOT, null, null, null, null, 0, JaspiraAction.TYPE_GROUP));
		actions.put(JaspiraAction.TOOLBAR_ROOT, new JaspiraAction(JaspiraAction.TOOLBAR_ROOT, null, null, null, null, 0, JaspiraAction.TYPE_GROUP));
	}

	/**
	 * Returns the singleton instance of the ActionMgr.
	 * @return The ActionMgr, is created if necessary
	 */
	public static synchronized ActionMgr getInstance()
	{
		if (singleton == null)
		{
			singleton = new ActionMgr();
		}

		return singleton;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Action access
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Add a new Action to the manager. Returns true if the action is already
	 * existant. If the action is already registered, it is NOT replaced.
	 * @param action the action to add
	 */
	public void addAction(JaspiraAction action)
	{
		String name = action.getName();

		JaspiraAction current = (JaspiraAction) actions.get(name);

		if (current == null)
		{
			// Action not present yet, add to list
			current = action;
			actions.put(name, current);
		}

		// Increase reference counter
		current.increaseCounter();

		// Add as child to the menu parent if given
		String menuparentname = current.getActionPropertyString(JaspiraAction.PROPERTY_MENU_PARENT);
		if (menuparentname != null)
		{
			// Check if the parent has already been registered
			JaspiraAction menuparent = getAction(menuparentname);
			if (menuparent == null)
			{
				menuparent = new JaspiraAction(current.getActionResource(), menuparentname);
				addAction(menuparent);
			}

			// register this action at its parent
			menuparent.addMenuChild(current);
		}

		// Add as child to the toolbar parent if given
		String toolbarparentname = current.getActionPropertyString(JaspiraAction.PROPERTY_TOOLBAR_PARENT);
		if (toolbarparentname != null)
		{
			// Check if the parent has already been registered
			JaspiraAction toolbarparent = getAction(toolbarparentname);
			if (toolbarparent == null)
			{
				// No, create it on the fly
				toolbarparent = new JaspiraAction(current.getActionResource(), toolbarparentname);
				addAction(toolbarparent);
			}

			// register this action at its parent
			toolbarparent.addToolbarChild(current);
		}
	}

	/**
	 * Removes a JaspiraAction from the manager. Returns the removed action or
	 * null if an action with the given name is not existant.
	 *
	 * @param name The name of the action to remove
	 * @return The removed action or null if the action was not registered
	 */
	public JaspiraAction removeAction(String name)
	{
		JaspiraAction action = (JaspiraAction) actions.get(name);

		if (action == null)
		{
			return null;
		}

		if (!action.decreaseCounter())
		{
			actions.remove(name);
		}

		return action;
	}

	/**
	 * Removes a JaspiraAction from the manager. Returns the removed action or
	 * null if an action with the given name is not existant.
	 *
	 * @param action The action to be removed
	 * @return The removed action or null if the action was not registered
	 */
	public JaspiraAction removeAction(JaspiraAction action)
	{
		return removeAction(action.getName());
	}

	/**
	 * Removes all actions in the given collection to the manager.
	 *
	 * @param actions A collection containing the actions to remove
	 */
	public void removeAllActions(Collection actions)
	{
		if (actions != null)
		{
			for (Iterator it = actions.iterator(); it.hasNext();)
			{
				removeAction((JaspiraAction) it.next());
			}
		}
	}

	/**
	 * Returns the action with the given name or null if their is no such
	 * action.
	 * @param name The name (id) of the action to retrieve
	 * @return The action or null if no such action exists
	 */
	public JaspiraAction getAction(String name)
	{
		return (JaspiraAction) actions.get(name);
	}

	//////////////////////////////////////////////////
	// @@ Static methods
	//////////////////////////////////////////////////

	/**
	 * Returns the mnemonic char of a string.
	 *
	 * @param s The raw string
	 * @return The mnemonic char or ? if the string does not contain the mnmonic delimiter
	 */
	public static char getMnemonicChar(String s)
	{
		int pos = s.indexOf(JaspiraAction.MNEMONIC_DELIMITER);
		if (pos != -1)
		{
			return s.charAt(pos + 1);
		}

		return '\0';
	}

	/**
	 * Removes the mnemonic delimiter from a string.
	 *
	 * @param s The raw string
	 * @return The clean string
	 */
	public static String getStringWithoutMnemonicDelimiter(String s)
	{
		if (s == null)
			return null;

		int mnemonicpos = s.indexOf(JaspiraAction.MNEMONIC_DELIMITER);

		if (mnemonicpos == -1)
		{
			// no delimiter found
			return s;
		}

		return s.substring(0, mnemonicpos) + s.substring(mnemonicpos + 1);
	}
}
