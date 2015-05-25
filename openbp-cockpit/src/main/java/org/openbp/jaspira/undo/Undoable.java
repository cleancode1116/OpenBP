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

/**
 * Action that can be undone by the {@link UndoMgr}
 *
 * @author Jens Ferchland
 */
public interface Undoable
{
	/**
	 * Undo the last Action.
	 *
	 * @return boolean - true action undo was successful
	 *					 false otherwise
	 */
	public boolean undo();

	/**
	 * Returns true if the action can be redone.
	 *
	 * @return boolean - true action is reduable
	 *					 false otherwise
	 */
	public boolean isRedoable();

	/**
	 * Redo the action.
	 *
	 * @return boolean - true action redo was successful
	 *					 false otherwise
	 */
	public boolean redo();

	/**
	 * A name of the action which can be displayed on screen.
	 *
	 * @return String a name which can be displayed on screen
	 */
	public String getDisplayName();
}
