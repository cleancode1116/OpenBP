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
package org.openbp.cockpit.modeler.undo;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.openbp.cockpit.modeler.Modeler;
import org.openbp.cockpit.modeler.drawing.WorkspaceDrawingView;
import org.openbp.cockpit.modeler.figures.process.ProcessElementContainer;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessObject;
import org.openbp.jaspira.undo.Undoable;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

/**
 * 'Brute force' implemenation of an undoable object for the process modeler.
 * This undoable simply contains a before and after operation image (clone) of the process.
 * This is the easy way because we don't need to thing about how to revert an operation, but
 * it's definately memory-consuming.
 *
 * @author Heiko Erhardt
 */
public class ModelerUndoable
	implements Undoable
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Display name */
	private String displayName;

	/** Modeler */
	private Modeler modeler;

	/** Process before the operation */
	private ProcessItem processBefore;

	/** Selection before the operation (contains {@link ModelQualifier} objects) */
	private List selectionBefore;

	/** Process after the operation */
	private ProcessItem processAfter;

	/** Selection after the operation (contains {@link ModelQualifier} objects) */
	private List selectionAfter;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param modeler Modeler
	 */
	public ModelerUndoable(Modeler modeler)
	{
		this.modeler = modeler;

		modeler.getDrawing().encodeGeometry();

		processBefore = copyProcess(modeler.getProcess());
		selectionBefore = saveSelection();
	}

	//////////////////////////////////////////////////
	// @@ Undoable implementation
	//////////////////////////////////////////////////

	/**
	 * @see Undoable#undo()
	 */
	public boolean undo()
	{
		// Save the current modeler state for redo
		if (processAfter == null)
		{
			modeler.getDrawing().encodeGeometry();
			processAfter = copyProcess(modeler.getProcess());
			selectionAfter = saveSelection();
		}

		// Clear the selection first
		modeler.getDrawingView().clearSelection();

		// Set the process
		modeler.setProcessByUndoRedo(processBefore);

		// Restore the selection
		restoreSelection(selectionBefore);

		return true;
	}

	/**
	 * @see Undoable#isRedoable()
	 */
	public boolean isRedoable()
	{
		return true;
	}

	/**
	 * @see Undoable#redo()
	 */
	public boolean redo()
	{
		// Clear the selection first
		modeler.getDrawingView().clearSelection();

		// Set the process
		modeler.setProcessByUndoRedo(processAfter);

		// Restore the selection
		restoreSelection(selectionAfter);

		return true;
	}

	/**
	 * Gets the display name.
	 * @nowarn
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Sets the display name.
	 * @nowarn
	 */
	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Copies a process.
	 * Clones the process and clears all figures references from the clone.
	 *
	 * @param process Process to clone
	 * @return The clone
	 */
	protected ProcessItem copyProcess(ProcessItem process)
	{
		// Update the reference names of the process elements
		process.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.SYNC_LOCAL_REFNAMES);

		try
		{
			process = (ProcessItem) process.clone();
		}
		catch (CloneNotSupportedException e)
		{
			// Never happens
		}

		// Rebuild the references and remove the links to the modeler figures
		process.maintainReferences(ModelObject.RESOLVE_LOCAL_REFS | ModelObject.UNLINK_FROM_REPRESENTATION);

		return process;
	}

	/**
	 * Saves the current selection of the associated modeler.
	 *
	 * @return A list of {@link ModelQualifier} objects denoting the selected elements or null if no elements were selected
	 */
	protected List saveSelection()
	{
		List result = null;

		for (FigureEnumeration fe = modeler.getDrawingView().selectionElements(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();

			if (f instanceof ProcessElementContainer)
			{
				ProcessElementContainer pec = (ProcessElementContainer) f;
				ProcessObject pe = pec.getProcessElement();
				ModelQualifier qualifier = pe.getQualifier();

				if (result == null)
				{
					result = new ArrayList();
				}
				result.add(qualifier);
			}
		}

		return result;
	}

	/**
	 * Restores the current selection.
	 *
	 * @param selectionQualifiers A list of {@link ModelQualifier} objects denoting the selected elements or null if no elements were selected
	 */
	protected void restoreSelection(List selectionQualifiers)
	{
		if (selectionQualifiers == null)
			return;

		WorkspaceDrawingView view = modeler.getDrawingView();
		Rectangle rect = null;

		// Restore the selected objects by looking up the figures by theirmodel qualifier 
		int n = selectionQualifiers.size();
		for (int i = 0; i < n; ++i)
		{
			ModelQualifier qualifier = (ModelQualifier) selectionQualifiers.get(i);

			ProcessElementContainer pec = modeler.getDrawing().getFigureByQualifier(qualifier);
			if (pec != null)
			{
				view.addToSelection(pec);

				Rectangle db = pec.displayBox();
				if (rect == null)
					rect = new Rectangle(db);
				else
					rect = rect.union(db);
			}
		}

		if (rect != null)
		{
			// Try to position the selection into view again
			rect.grow(50, 50);
			view.scrollRectToVisible(rect);
		}
	}
}
