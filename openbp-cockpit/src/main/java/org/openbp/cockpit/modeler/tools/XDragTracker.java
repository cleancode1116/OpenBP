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

import java.awt.event.MouseEvent;

import org.openbp.cockpit.modeler.figures.VisualElement;
import org.openbp.cockpit.modeler.figures.process.ProcessElementContainer;

import CH.ifa.draw.framework.FigureEnumeration;

/**
 * Performs dragging of the clicked figure.
 *
 * @author Stephan Pauxberger
 */
public class XDragTracker extends ModelerTool
{
	/** X coordinate of the first click */
	private int anchorX;

	/** Y coordinate of the first click */
	private int anchorY;

	/** X coordinate of the last mouse position determine in mouse movement handler */
	private int lastX;

	/** X coordinate of the last mouse position determine in mouse movement handler */
	private int lastY;

	/** Flag if scaling should be performed */
	private boolean moved;

	/** Flag if the object was already selected */
	private boolean wasSelected;

	public XDragTracker(ModelerToolSupport toolSupport)
	{
		super(toolSupport);
	}

	public void mouseDown(MouseEvent e, int x, int y)
	{
		anchorX = x;
		anchorY = y;
		moved = false;

		lastX = x;
		lastY = y;

		wasSelected = getView().isFigureSelected(getAffectedFigure());

		if (e.isShiftDown())
		{
			getView().toggleSelection(getAffectedFigure());
		}
		else if (! wasSelected)
		{
			getView().singleSelect(getAffectedFigure());
		}
	}

	public void mouseDrag(MouseEvent e, int x, int y)
	{
		// Give a 4 pixel tolerance before beginning the move.
		// This prevents that a simple selection click might modify the drawing.
		moved = (Math.abs(x - anchorX) > 4) || (Math.abs(y - anchorY) > 4);
		if (moved)
		{
			if (!getEditor().isUndoRecording())
			{
				getEditor().startUndo("Move Element");
			}

			FigureEnumeration figures = getView().selectionElements();
			while (figures.hasMoreElements())
			{
				figures.nextFigure().moveBy(x - lastX, y - lastY);
			}
		}

		lastX = x;
		lastY = y;
	}

	public void mouseUp(MouseEvent e, int x, int y)
	{
		if (moved)
		{
			getEditor().endUndo();
		}

		super.mouseUp(e, x, y);

		if (! moved && wasSelected && e.getClickCount() == 1)
		{
			VisualElement lastFigure = getToolSupport().getLastFigure();
			if (lastFigure instanceof ProcessElementContainer)
			{
				getToolSupport().toggleInPlaceEditor((ProcessElementContainer) lastFigure, true);
			}
		}

		moved = false;
	}
}
