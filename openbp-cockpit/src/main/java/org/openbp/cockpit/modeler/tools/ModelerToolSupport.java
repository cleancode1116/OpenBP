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
package org.openbp.cockpit.modeler.tools;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.openbp.cockpit.modeler.ModelerGraphics;
import org.openbp.cockpit.modeler.drawing.DrawingEditorPlugin;
import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.drawing.WorkspaceDrawingView;
import org.openbp.cockpit.modeler.figures.VisualElement;
import org.openbp.cockpit.modeler.figures.VisualElementEvent;
import org.openbp.cockpit.modeler.figures.process.ProcessElementContainer;
import org.openbp.cockpit.modeler.util.InputState;
import org.openbp.jaspira.event.JaspiraEventMgr;
import org.openbp.jaspira.gui.interaction.BasicTransferable;
import org.openbp.jaspira.plugins.statusbar.StatusBarTextEvent;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.Handle;
import CH.ifa.draw.standard.AbstractTool;

/**
 * The node manipulation tool is the standard tool used by the modeler.
 * It handles regular object selection as well as rubber band tracking, double click on objects
 * and figure popup menu invocations.
 * It also supports moving the workspace view while the ALT key is pressed (track).
 *
 * @author Stephan Pauxberger
 */
public class ModelerToolSupport extends AbstractTool
{
	/**
	 * Editor that owns the tool.
	 * (This equals the return value of editor(), however we cache it to avoid casts.)
	 */
	private DrawingEditorPlugin editor;

	/**
	 * View we refer to.
	 * (This equals the return value of view(), however we cache it to avoid casts.)
	 */
	private WorkspaceDrawingView view;

	/** Current tool */
	private ModelerTool currentTool;

	/** In place editor tool for process elements */
	private ProcessElementInPlaceEditorTool inPlaceEditorTool;

	/** Last postion of the cursor (view coordinates). Also used by the break out box mechanism */
	private Point lastPoint;

	/** Last figure that has been hovered with the mouse */
	private VisualElement lastFigure;

	/** Last handle that has been hovered with the mouse */
	private Handle lastHandle;

	/** Delay timer for the in place editor (to catch the double click) */
	private Timer inPlaceEditorDelayTimer;

	/** Default track rectangle size */
	private static final int TRACK_SIZE = 75;

	/** Table holding all tool references and tool use cases */
	private List<ModelerToolDecisionTableEntry> tooDecisionTable;

	/**
	 * Constructs the tool for the given editor.
	 *
	 * @param editor Editor that owns the tool
	 */
	public ModelerToolSupport(DrawingEditorPlugin editor)
	{
		super(editor);
		this.editor = editor;
		view = (WorkspaceDrawingView) editor.view();

		lastPoint = new Point();

		tooDecisionTable = new ArrayList<ModelerToolDecisionTableEntry>();
		inPlaceEditorTool = new ProcessElementInPlaceEditorTool(this);
	}

	/**
	 * Adds an entry to the tool decision table.
	 *
	 * @param tool Tool
	 * @param figureClass Class of the figure this tool is suitable for or null for any
	 * @param requiredInputState Expected keyboard and mouse button input state or 0 for any
	 */
	public void addToolDecisionTableEntry(ModelerTool tool, Class figureClass, int requiredInputState)
	{
		tooDecisionTable.add(new ModelerToolDecisionTableEntry(tool, figureClass, requiredInputState));
	}

	public void deactivate()
	{
		super.deactivate();
		if (currentTool != null)
		{
			// Pass to subordinate tool
			currentTool.deactivate();
			currentTool = null;
		}
	}

	/**
	 * Activates the given tool.
	 *
	 * @param tool Tool to activate
	 * @param objectUnderCursor Object that will be affected by the tool
	 */
	public void activateTool(ModelerTool tool, Object objectUnderCursor)
	{
		currentTool = tool;
		currentTool.setAffectedObject(objectUnderCursor);
		currentTool.activate();
	}

	/**
	 * Clears the current tool.
	 */
	protected void clearTool()
	{
		currentTool = null;
		updateToolState();
	}

	/**
	 * Returns the last known mouse coordinates.
	 * Used for break out box display.
	 * @nowarn
	 */
	public Point getLastPoint()
	{
		return new Point(lastPoint);
	}

	/**
	 * Gets the last figure that has been hovered with the mouse.
	 * @nowarn
	 */
	public VisualElement getLastFigure()
	{
		return lastFigure;
	}

	/**
	 * Gets the editor that owns the tool.
	 * @nowarn
	 */
	public DrawingEditorPlugin getEditor()
	{
		return editor;
	}

	/**
	 * Gets the view we refer to.
	 * @nowarn
	 */
	public WorkspaceDrawingView getView()
	{
		return view;
	}

	//////////////////////////////////////////////////
	// @@ Mouse event handlers
	//////////////////////////////////////////////////

	public void mouseMove(MouseEvent e, int x, int y)
	{
		// Convert to document coordinate according to view scale factor
		x = view.applyScale(x, true);
		y = view.applyScale(y, true);

		if (currentTool != null)
		{
			// Forward to current tool
			currentTool.mouseMove(e, x, y);

			lastPoint.x = e.getX();
			lastPoint.y = e.getY();
			return;
		}

		Object objectUnderCursor = null;

		lastHandle = view.findHandle(x, y);
		if (lastHandle != null)
		{
			objectUnderCursor = lastHandle;
		}
		else
		{
			// We look for the figure under the cursor
			VisualElement figure = ((ProcessDrawing) drawing()).findVisualElementInside(x, y);
			if (figure != null)
			{
				objectUnderCursor = figure;
			}

			if (figure != lastFigure)
			{
				// Mouse moved to another figure, update figure hover effect if not entering a child element
				if (lastFigure != null && (figure == null || figure.getParentElement() != lastFigure))
				{
					// Tell the figure it is not hovered by the cursor any more
					lastFigure.invalidate();
					lastFigure.handleEvent(new VisualElementEvent(VisualElementEvent.CURSOR_LEFT, editor, e, x, y));
				}

				if (figure != null)
				{
					// Tell the figure it is hovered by the cursor
					figure.handleEvent(new VisualElementEvent(VisualElementEvent.CURSOR_ENTERED, editor, e, x, y));
				}

				lastFigure = figure;
				view.setFigureUnderCursor(figure);

				if (figure != null)
				{
					figure.invalidate();
				}
				view.repairDamage();
			}
		}

		String hintMsg = null;
		Cursor cursor = ModelerGraphics.standardCursor;

		ModelerToolDecisionTableEntry entry = determineTool(objectUnderCursor);
		if (entry != null)
		{
			ModelerTool tool = entry.getTool();
			hintMsg = tool.getToolHintMsg();
			cursor = tool.getToolCursor();
			if ((entry.getRequiredInputState() & InputState.HOVER) != 0)
			{
				activateTool(tool, objectUnderCursor);
				currentTool.mouseMove(e, x, y);
			}
		}

		// Display or remove the hint message and set the view cursor
		editor.fireEvent(new StatusBarTextEvent(editor, hintMsg));
		view.setCursor(cursor);

		lastPoint.x = e.getX();
		lastPoint.y = e.getY();
	}

	public void mouseDown(MouseEvent e, int x, int y)
	{
		// Convert to document coordinate according to view scale factor
		x = view.applyScale(x, true);
		y = view.applyScale(y, true);

		// Update input state
		InputState.updateStateOnMouseDownEvent(e);

		if (currentTool != null)
		{
			// Forward to current tool
			currentTool.mouseDown(e, x, y);
			return;
		}

		// Left mouse down activates the current tool

		if (InputState.isLeftButtonDown())
		{
			Object objectUnderCursor = lastHandle;
			if (objectUnderCursor == null)
				objectUnderCursor = lastFigure;
			ModelerToolDecisionTableEntry entry = determineTool(objectUnderCursor);
			if (entry != null)
			{
				if ((entry.getRequiredInputState() & InputState.HOVER) == 0)
				{
					ModelerTool tool = entry.getTool();
					activateTool(tool, objectUnderCursor);
					currentTool.mouseDown(e, x, y);
				}
			}
		}
	}

	public void mouseUp(MouseEvent e, int x, int y)
	{
		// Convert to document coordinate according to view scale factor
		x = view.applyScale(x, true);
		y = view.applyScale(y, true);

		if (currentTool != null)
		{
			// Reset the child tool
			currentTool.mouseUp(e, x, y);
			if (e.getClickCount() == 1)
			{
				return;
			}
		}

		boolean rightButton = InputState.isRightButtonDown();
		if (rightButton)
		{
			Figure f = null;

			ProcessElementContainer pec = ((ProcessDrawing) drawing()).findProcessElementContainerInside(x, y);
			if (pec != null && pec != drawing())
			{
				f = pec;
			}
			else
			{
				f = ((ProcessDrawing) drawing()).findVisualElementInside(x, y);
			}

			if (f != null && f != drawing())
			{
				view.displayPopupMenu(f, e);

				lastFigure = (ProcessDrawing) drawing();
			}
		}
		else if (e.getClickCount() == 2)
		{
			// Double click caught, cancel pending in place editor timer
			if (inPlaceEditorDelayTimer != null)
			{
				inPlaceEditorDelayTimer.cancel();
				inPlaceEditorDelayTimer = null;
			}

			Figure figure = drawing().findFigure(x, y);

			if (figure instanceof VisualElement)
			{
				// Let the figure itself handle the doubleclick
				// TODONOW
				if (((VisualElement) figure).handleEvent(new VisualElementEvent(VisualElementEvent.DOUBLE_CLICK, editor, e, x, y)))
				{
					// Handled by the element itself
					return;
				}
			}

			if (figure instanceof ProcessElementContainer)
			{
				// Let's see if the process element behind the figure can handle this...
				ProcessElementContainer pec = ((ProcessElementContainer) figure).findProcessElementContainerInside(x, y);

				// Double click means opening the item, pass to association plugin
				JaspiraEventMgr.fireGlobalEvent("plugin.association.open", new BasicTransferable(pec.getReferredProcessElement()));
			}
		}

		// Update input state
		InputState.updateStateOnMouseUpEvent(e);

		updateToolState();
	}

	public void mouseDrag(MouseEvent e, int x, int y)
	{
		lastPoint.x = x;
		lastPoint.y = y;

		// Convert to document coordinate according to view scale factor
		x = view.applyScale(x, true);
		y = view.applyScale(y, true);

		if (!InputState.isRightButtonDown())
		{
			if (currentTool instanceof RubberBandTool)
			{
				// Make the old rubber band disappear
				((RubberBandTool) currentTool).eraseRubberBand();
			}

			view.scrollRectToVisible(new Rectangle(x, y, TRACK_SIZE, TRACK_SIZE));

			if (currentTool != null)
			{
				// Pass to subordinate tool
				currentTool.mouseDrag(e, x, y);
			}
		}
	}

	public void keyDown(KeyEvent e, int key)
	{
		updateToolState(e);

		if (currentTool != null)
		{
			// Reset the child tool
			currentTool.keyDown(e, key);
			return;
		}
	}

	public void keyUp(KeyEvent e, int key)
	{
		if (currentTool != null)
		{
			// Reset the child tool
			currentTool.keyUp(e, key);
		}

		else if (key == KeyEvent.VK_ENTER && InputState.isAltDown())
		{
			Figure selectedFigure = null;
			Iterator it = getView().selection().iterator();
			if (it.hasNext())
			{
				selectedFigure = (Figure) it.next();
			}
			if (selectedFigure instanceof ProcessElementContainer && ! (selectedFigure instanceof ProcessDrawing))
			{
				// we directly set the tool inside the editor
				toggleInPlaceEditor((ProcessElementContainer) selectedFigure, false);
				currentTool.keyDown(e, key);
				return;
			}
		}

		updateToolState(e);
	}

	/**
	 * Shows the in place editor for the given figure.
	 *
	 * @param figure Process element container figure to edit
	 */
	public void displayInPlaceEditor(ProcessElementContainer figure)
	{
		activateTool(inPlaceEditorTool, figure);
	}

	/**
	 * Shows or hides the in place editor for the given figure.
	 *
	 * @param figure Process element container figure to edit
	 */
	public void toggleInPlaceEditor(final ProcessElementContainer figure, boolean delay)
	{
		if (currentTool == inPlaceEditorTool && inPlaceEditorTool.getAffectedFigure() == figure)
		{
			clearTool();
		}
		else
		{
			if (delay)
			{
				inPlaceEditorDelayTimer = new Timer();
				inPlaceEditorDelayTimer.schedule(new TimerTask()
				{
					public void run()
					{
						activateTool(inPlaceEditorTool, figure);
						inPlaceEditorDelayTimer.cancel();
						inPlaceEditorDelayTimer = null;
					}
				}, 400);
			}
			else
			{
				activateTool(inPlaceEditorTool, figure);
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	public void updateToolState()
	{
		updateToolState(null);
	}

	/**
	 * Updates the cursor in case of a key press.
	 * @param e Key event
	 */
	protected void updateToolState(KeyEvent e)
	{
		if (e != null)
		{
			InputState.updateStateOnInputEvent(e);
		}
		else
		{
			InputState.setState(0);
		}

		// The state of the keyboard modifier keys has changed
		// Simulate a mouse movement to the current position
		int x = lastPoint.x;
		int y = lastPoint.y;

		Component source;
		long when;
		int modifiers;
		if (e != null)
		{
			source = (Component) e.getSource();
			when = e.getWhen();
			modifiers = e.getModifiers();
		}
		else
		{
			source = view;
			when = 0L;
			modifiers = 0;
		}
		MouseEvent me = new MouseEvent(source, 0, when, modifiers, x, y, 0, false);

		if (InputState.isLeftButtonDown() || InputState.isMiddleButtonDown() || InputState.isRightButtonDown())
		{
			mouseDrag(me, x, y);
		}
		else
		{
			mouseMove(me, x, y);
		}
	}

	/**
	 * Gets the tool that would be applied to the given object according to the current settings and input states.
	 *
	 * @param object Object under cursor
	 * @return The tool entry or null
	 */
	protected ModelerToolDecisionTableEntry determineTool(Object object)
	{
		int currentState = InputState.getState() & (InputState.ALT | InputState.CTRL);

		for (ModelerToolDecisionTableEntry entry : tooDecisionTable)
		{
			int requiredInputState = entry.getRequiredInputState() & (InputState.ALT | InputState.CTRL);
			if (currentState != requiredInputState)
				continue;

			Class objectClass = entry.getObjectClass();

			boolean hasObject = object != null;
			boolean wantsObject = objectClass != null;
			if (hasObject != wantsObject)
				continue;

			if (objectClass != null && ! objectClass.isInstance(object))
				continue;

			ModelerTool tool = entry.getTool();
			if (tool.appliesTo(object))
			{
				return entry;
			}
		}
		return null;
	}
}
