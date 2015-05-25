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

import org.openbp.common.generic.Modifiable;

import CH.ifa.draw.framework.Handle;

/**
 * Handle tracker that notifies the underlaying process of modification.
 *
 * @author Stephan Pauxberger
 */
public class XHandleTracker extends ModelerTool
{
	/** Handle to be tracked */
	private Handle handle;

	/** X coordinate of the first click */
	private int anchorX;

	/** Y coordinate of the first click */
	private int anchorY;

	public XHandleTracker(ModelerToolSupport toolSupport)
	{
		super(toolSupport);
	}

	public void setAffectedObject(Object affectedObject)
	{
		super.setAffectedObject(affectedObject);

		this.handle = (Handle) affectedObject;
	}

	public void deactivate()
	{
		handle = null;

		super.deactivate();
	}

	public void mouseDown(MouseEvent e, int x, int y)
	{
		super.mouseDown(e, x, y);

		getEditor().startUndo("Move Handle");

		anchorX = x;
		anchorY = y;

		handle.invokeStart(x, y, getView());
	}

	public void mouseDrag(MouseEvent e, int x, int y)
	{
		super.mouseDrag(e, x, y);
		handle.invokeStep(x, y, anchorX, anchorY, getView());
	}

	public void mouseUp(MouseEvent e, int x, int y)
	{
		// Give a 4 pixel tolerance before beginning the move.
		// This prevents that a simple selection click might modify the drawing.
		boolean moved = (Math.abs(x - anchorX) > 4) || (Math.abs(y - anchorY) > 4);
		if (moved)
		{
			((Modifiable) getDrawing()).setModified();

			if (getEditor().isUndoRecording())
			{
				getEditor().endUndo();
			}
		}

		handle.invokeEnd(x, y, anchorX, anchorY, getView());

		super.mouseUp(e, x, y);
	}
}
