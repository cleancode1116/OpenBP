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

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.openbp.cockpit.modeler.drawing.Trackable;
import org.openbp.cockpit.modeler.drawing.WorkspaceDrawingView;
import org.openbp.cockpit.modeler.util.InputState;
import org.openbp.common.CommonUtil;

/**
 * Scrolls the workspace.
 *
 * @author Stephan Pauxberger
 */
public class ScrollTool extends ModelerTool
{
	public ScrollTool(ModelerToolSupport toolSupport)
	{
		super(toolSupport);
	}

	public void mouseMove(MouseEvent e, int x, int y)
	{
		WorkspaceDrawingView view = (WorkspaceDrawingView) getView();
		Point lastPoint = getLastPoint();

		// ALT key pressed. Move the drawing accordingly using heuristic values.
		int xDiff = CommonUtil.rnd((e.getX() - lastPoint.x) / 1.5);
		int yDiff = CommonUtil.rnd((e.getY() - lastPoint.y) / 1.5);

		// Convert to document coordinates
		xDiff = view.applyScale(xDiff, true);
		yDiff = view.applyScale(yDiff, true);

		if (xDiff != 0 || yDiff != 0)
		{
			((Trackable) getEditor()).moveTrackerBy(xDiff, yDiff);
		}
	}

	public void deactivate()
	{
		getView().revalidate();

		super.deactivate();
	}

	public void keyDown(KeyEvent e, int key)
	{
		if (InputState.isCtrlDown())
		{
			deactivate();
			getToolSupport().updateToolState(e);
		}
	}

	public void keyUp(KeyEvent e, int key)
	{
		deactivate();
	}
}
