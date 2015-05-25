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
package org.openbp.cockpit.modeler.drawing;

import org.openbp.cockpit.modeler.tools.ModelerToolSupport;
import org.openbp.cockpit.modeler.undo.ModelerUndoable;
import org.openbp.jaspira.gui.plugin.VisiblePlugin;
import org.openbp.jaspira.undo.Undoable;

import CH.ifa.draw.framework.DrawingEditor;

/**
 * Combination of DrawingEditor and VisiblePlugin.
 *
 * @author Stephan Moritz
 */
public interface DrawingEditorPlugin
	extends VisiblePlugin, DrawingEditor
{
	/**
	 * Corrects any refresh damage in the view.
	 */
	void repairDamage();

	/**
	 * Creates an undoable object given the display name of the operation that can be undone with this undoable.
	 * The undoable returned will be a {@link ModelerUndoable} that contains a copy of the current process.
	 * The method will also save the returned undoable so it can be retrieved with the getCurrentUndoable method.
	 * In order to provide the data after the operation and to register the undoable, call the {@link #endUndo} method.
	 *
	 * @param displayName Display name or null<br>
	 * This text will appear after the 'undo: ' text in the edit menu.
	 * @return The new undoable
	 */
	public Undoable startUndo(String displayName);

	/**
	 * Updates the current undoable with the current 'after operation' state and registers it with the undo manager.
	 * This method may be called only after the {@link #startUndo} method has been called.
	 */
	public void endUndo();

	/**
	 * Cancels the current undoable.
	 * This method may be called only after the {@link #startUndo} method has been called.
	 */
	public void cancelUndo();

	/**
	 * Checks if currently an undo operation is being recorded.
	 *
	 * @return
	 * true: {@link #startUndo} was called.<br>
	 * false: No current undoable is present.
	 */
	public boolean isUndoRecording();

	/**
	 * Gets the the modeler tool support object holding all tools.
	 * @nowarn
	 */
	public ModelerToolSupport getToolSupport();
}
