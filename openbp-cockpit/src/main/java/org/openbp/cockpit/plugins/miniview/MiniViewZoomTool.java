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
package org.openbp.cockpit.plugins.miniview;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import org.openbp.cockpit.modeler.Modeler;
import org.openbp.cockpit.modeler.drawing.DrawingEditorPlugin;

import CH.ifa.draw.standard.AbstractTool;

/**
 * Tool for the DravingOverView for selecting and moving a viewport
 *
 * @author Jens Ferchland
 */
public class MiniViewZoomTool extends AbstractTool
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Mini view we are working on */
	private MiniView miniView;

	/** Last mouse position */
	private int lastX, lastY;

	/** Tracker status flag */
	private boolean tracking;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 * @param miniView Miniview the tool is associated with
	 */
	public MiniViewZoomTool(MiniView miniView)
	{
		super(miniView.editor());
		this.miniView = miniView;
	}

	//////////////////////////////////////////////////
	// @@ Mouse event handlers
	//////////////////////////////////////////////////

	/**
	 * A mouse button was pressed.
	 *
	 * @param e Mouse event which should be interpreted
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @see CH.ifa.draw.framework.Tool#mouseDown(MouseEvent, int, int)
	 */
	public void mouseDown(MouseEvent e, int x, int y)
	{
		lastX = x;
		lastY = y;
	}

	/**
	 * A mouse button was released.
	 *
	 * @param e Mouse event which should be interpreted
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @see CH.ifa.draw.framework.Tool#mouseUp(MouseEvent, int, int)
	 */
	public void mouseUp(MouseEvent e, int x, int y)
	{
		Modeler modeler = miniView.getModeler();
		if (modeler != null)
		{
			if (e.getClickCount() == 2)
			{
				modeler.setScaleFactor(1.0);
				modeler.centerTrackerAt(calculateDocumentCoordinates(e.getPoint()));
			}
			else if (tracking)
			{
				miniView.setTrackRect(null);
				int rx = x < lastX ? x : lastX;
				int ry = y < lastY ? y : lastY;
				int w = x < lastX ? lastX - x : x - lastX;
				int h = y < lastX ? lastY - y : y - lastY;
				Rectangle r = new Rectangle(rx, ry, w, h);
				modeler.setVisibleArea(calculateDocumentCoordinates(r));
			}
			else
			{
				modeler.centerTrackerAt(calculateDocumentCoordinates(e.getPoint()));
			}
		}

		tracking = false;

		// Focus the editor workspace
		DrawingEditorPlugin editor = (DrawingEditorPlugin) miniView.editor();
		editor.focusPlugin();
	}

	/**
	 * The mouse was dragged.
	 *
	 * @param e Mouse event which should be interpreted
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @see CH.ifa.draw.framework.Tool#mouseDrag(MouseEvent, int, int)
	 */
	public void mouseDrag(MouseEvent e, int x, int y)
	{
		Rectangle r = new Rectangle(lastX, lastY, x - lastX, y - lastY);

		if (r.width >= 4 || r.width <= -4 || r.height >= 4 || r.height <= -4)
		{
			miniView.setTrackRect(calculateDocumentCoordinates(r));

			tracking = true;
		}
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Applies the mini view scale to a point.
	 * @param p Point in mini view coordinates
	 * @return The point in document coordinates
	 */
	private Point calculateDocumentCoordinates(Point p)
	{
		double scale = miniView.getScale();
		p.x /= scale;
		p.y /= scale;
		return p;
	}

	/**
	 * Applies the mini view scale to a rectangle.
	 * @param r Rectangle in mini view coordinates
	 * @return The rectangle in workspace coordinates
	 */
	private Rectangle calculateDocumentCoordinates(Rectangle r)
	{
		// if the width is negative the mouse is moved to the left of our startpoint
		// so we have to translate the start point
		if (r.width < 0)
		{
			r.x += r.width;
			r.width *= -1;
		}

		// if the height is negative the mouse is moved above of our startpoint
		// so we have to translate the start point
		if (r.height < 0)
		{
			r.y += r.height;
			r.height *= -1;
		}

		double scale = miniView.getScale();
		r.x /= scale;
		r.y /= scale;
		r.width /= scale;
		r.height /= scale;
		return r;
	}
}
