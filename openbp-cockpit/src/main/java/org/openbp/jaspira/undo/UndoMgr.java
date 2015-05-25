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
package org.openbp.jaspira.undo;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.openbp.common.CollectionUtil;
import org.openbp.jaspira.action.ActionMgr;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.option.Option;
import org.openbp.jaspira.option.OptionMgr;

/**
 * This Manager controls the states of the application. If any plugin is
 * performing an action and changing the state of the application it has to
 * notify the Manager.
 *
 * Transitions are used to undo one or a set of actions. If a state changing
 * process is being started a new Transition has to be created. All undoable
 * actions fired after this will be saved in the transition.
 *
 * @author Jens Ferchland
 */
public class UndoMgr
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Unlimited history - all transitions will be saved */
	public static final int UNLIMITED_HISTORY_SIZE = -1;

	/** Default history size: 10 operations */
	public static final int DEFAULT_HISTORY_SIZE = 25;

	/** Name for the max history size option */
	public static final String TRANSITION_OPTION_NAME = "undo.history";

	//////////////////////////////////////////////////
	// @@ Static data
	//////////////////////////////////////////////////

	/** Display name of redo action */
	private static String redoText;

	/** Display name of undo action */
	private static String undoText;

	/** Description of redo action */
	private static String redoDescription;

	/** Description of undo action */
	private static String undoDescription;

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** All undo transitions */
	private LinkedList undoStack;

	/** All redo transitions */
	private LinkedList redoStack;

	/** The current transition or null */
	private Transaction currentTransition;

	/** Maximum transition history size */
	private int transitionHistorySize;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public UndoMgr()
	{
		undoStack = new LinkedList();
		redoStack = new LinkedList();

		if (redoText == null)
		{
			JaspiraAction redo = ActionMgr.getInstance().getAction("undo.redo");
			if (redo != null)
			{
				redoText = redo.getDisplayName();
				redoDescription = redo.getDescription();
			}
		}

		if (undoText == null)
		{
			JaspiraAction undo = ActionMgr.getInstance().getAction("undo.undo");
			if (undo != null)
			{
				undoText = undo.getDisplayName();
				undoDescription = undo.getDescription();
			}
		}

		reloadHistorySize();
	}

	//////////////////////////////////////////////////
	// @@ State handling
	//////////////////////////////////////////////////

	/**
	 * Starts a new Transaction and returns it.
	 * All next registrations of undoable objects will be recorded in
	 * this transition.
	 *
	 * @param displayName Displayable name of the transaction.<br>
	 * This name might be displayed in the undo menu to identify the action to be undone.
	 * @return The new transition. If a transition is already active,
	 * no new transition will be created and null will be returned.
	 */
	public Transaction startTransaction(String displayName)
	{
		if (currentTransition != null)
		{
			return null;
		}

		currentTransition = new Transaction(displayName);

		return currentTransition;
	}

	/**
	 * Stops the Transaction. To be sure the right plugin stops the transition
	 * the transition object is used for identification.
	 *
	 * @param trans - the current transition for identification
	 */
	public void stopTransaction(Transaction trans)
	{
		if (trans == null)
		{
			// don't stop the transition.
			return;
		}

		if (currentTransition != trans)
		{
			// something goes wrong - we get a wrong transition
			throw new RuntimeException("The transition: " + trans + "can't be stoped! It isn't active!");
		}

		undoStack.addFirst(currentTransition);

		// cut history
		if (transitionHistorySize != 0 && undoStack.size() > transitionHistorySize)
		{
			undoStack.removeLast();
		}

		currentTransition = null;

		// After a new Transaction clear the redoStack!
		redoStack.clear();

		updateActions();
	}

	/**
	 * Registers an undoable action. If no transition is active a new transition
	 * will be create.
	 *
	 * @param undo - the action, to undo
	 */
	public void registerUndoable(Undoable undo)
	{
		if (currentTransition == null)
		{
			// We have no current transaction - create a one with a single undoable object.
			startTransaction(undo.getDisplayName());
			currentTransition.addUndoable(undo);
			stopTransaction(currentTransition);
		}
		else
		{
			currentTransition.addUndoable(undo);
		}
	}

	/**
	 * Returns true if a redoable transition exists.
	 *
	 * @return boolean - true: redo transition exists.
	 *					 false: otherwise.
	 */
	public boolean canRedo()
	{
		return !redoStack.isEmpty();
	}

	/**
	 * Redoes the next redo transition.
	 */
	public void redo()
	{
		if (canRedo())
		{
			Transaction trans = (Transaction) redoStack.removeFirst();
			undoStack.addFirst(trans);
			trans.redo();
		}

		updateActions();
	}

	/**
	 * Returns true if a undoable transition exists.
	 *
	 * @return boolean - true: undo transition exists.
	 *					 false: otherwise.
	 */
	public boolean canUndo()
	{
		return !undoStack.isEmpty();
	}

	/**
	 * Undoes the last transition.
	 */
	public void undo()
	{
		if (canUndo())
		{
			Transaction trans = (Transaction) undoStack.removeFirst();
			redoStack.addFirst(trans);
			trans.undo();
		}

		updateActions();
	}

	/**
	 * Updates the actions undo and redo.
	 * Set the actions active or not.
	 */
	public void updateActions()
	{
		JaspiraAction undo = ActionMgr.getInstance().getAction("undo.undo");
		if (undo != null)
		{
			if (canUndo())
			{
				String cmdName = ((Transaction) undoStack.getFirst()).getDisplayName();

				if (cmdName != null)
				{
					String text = undoText + ": " + cmdName;
					undo.setDisplayName(text);
					undo.setDescription(text);
				}
				else
				{
					undo.setDisplayName(undoText);
					undo.setDescription(undoDescription);
				}
				undo.setEnabled(true);
			}
			else
			{
				undo.setDisplayName(undoText);
				undo.setEnabled(false);
			}
		}

		JaspiraAction redo = ActionMgr.getInstance().getAction("undo.redo");
		if (redo != null)
		{
			if (canRedo())
			{
				String cmdName = ((Transaction) redoStack.getFirst()).getDisplayName();

				if (cmdName != null)
				{
					String text = redoText + ": " + cmdName;
					redo.setDisplayName(text);
					redo.setDescription(text);
				}
				else
				{
					redo.setDisplayName(redoText);
					redo.setDescription(redoDescription);
				}
				redo.setEnabled(true);
			}
			else
			{
				redo.setDisplayName(redoText);
				redo.setEnabled(false);
			}
		}
	}

	/**
	 * Reloads the history size.
	 */
	public void reloadHistorySize()
	{
		Option opt = OptionMgr.getInstance().getOption(TRANSITION_OPTION_NAME);
		if (opt != null)
		{
			transitionHistorySize = ((Integer) opt.getValue()).intValue();
		}
		else
		{
			transitionHistorySize = DEFAULT_HISTORY_SIZE;
		}

		// if we have a new limit we have to cut the current history.
		if (transitionHistorySize != UNLIMITED_HISTORY_SIZE)
		{
			// cut the redo stack
			if (redoStack.size() > transitionHistorySize)
			{
				int del = redoStack.size() - transitionHistorySize;
				for (int i = 1; i <= del; i++)
				{
					redoStack.removeLast();
				}
			}

			// cut the undo stack
			if (undoStack.size() > transitionHistorySize)
			{
				int del = undoStack.size() - transitionHistorySize;
				for (int i = 1; i <= del; i++)
				{
					undoStack.removeLast();
				}
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Inner classes
	//////////////////////////////////////////////////

	/**
	 * A Transaction is a set of actions.
	 */
	public class Transaction
	{
		//////////////////////////////////////////////////
		// @@ Members
		//////////////////////////////////////////////////

		private String displayName;

		private LinkedList undoList;

		//////////////////////////////////////////////////
		// @@ Construction
		//////////////////////////////////////////////////

		public Transaction(String displayName)
		{
			this.displayName = displayName;

			undoList = new LinkedList();
		}

		//////////////////////////////////////////////////
		// @@ Member access
		//////////////////////////////////////////////////

		/**
		 * Adds an undoable object to the transition.
		 *
		 * @param undo The undoable to add
		 */
		public void addUndoable(Undoable undo)
		{
			undoList.addFirst(undo);
		}

		/**
		 * Returns true if the Transaction can be redo.
		 *
		 * @return boolean - true: is reduable
		 *					 false: is not reduable
		 */
		public boolean isRedoable()
		{
			for (Iterator iter = undoList.iterator(); iter.hasNext();)
			{
				if (!((Undoable) iter.next()).isRedoable())
				{
					return false;
				}
			}
			return true;
		}

		/**
		 * Undo the Transaction.
		 */
		public void undo()
		{
			for (Iterator iter = undoList.iterator(); iter.hasNext();)
			{
				((Undoable) iter.next()).undo();
			}
		}

		/**
		 * Undo the Transaction.
		 */
		public void redo()
		{
			// Add the elements of the undo list to the redo list in reverse order
			List redoList = new LinkedList();
			CollectionUtil.addReverseList(undoList, redoList);

			for (Iterator iter = redoList.iterator(); iter.hasNext();)
			{
				((Undoable) iter.next()).redo();
			}
		}

		/**
		 * Returns the Name of the Transaction. This name can be used to
		 * display the Trasnaction on screen.
		 *
		 * @return String name of the Transaction
		 */
		public String getDisplayName()
		{
			return displayName;
		}
	}
}
