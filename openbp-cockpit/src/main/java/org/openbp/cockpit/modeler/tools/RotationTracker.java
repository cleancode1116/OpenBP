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
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import org.openbp.cockpit.modeler.figures.generic.CircleConstants;
import org.openbp.cockpit.modeler.figures.tag.AbstractTagFigure;
import org.openbp.common.CommonUtil;

/**
 * The rotation tracker is used to rotate a socket tag around the node figure.
 * By pressing SHIFT, the rotation can be limited to a 45 degree raster, which eases socket alignments.
 *
 * @author Stephan Pauxberger
 */
public class RotationTracker extends ModelerTool
{
	/** Figure to rotate */
	private AbstractTagFigure tagFigure;

	/** X coordinate of the first click */
	private int anchorX;

	/** Y coordinate of the first click */
	private int anchorY;

	/** Origin around which the figure rotates */
	private Point origin;

	/** Flag if movement should be performed */
	private boolean moved;

	/** Number of angles for constrainted angle rotation */
	private static final int ANGLE_CONSTRAINT = 8;

	public RotationTracker(ModelerToolSupport toolSupport)
	{
		super(toolSupport);
	}

	public void setAffectedObject(Object affectedObject)
	{
		super.setAffectedObject(affectedObject);

		this.tagFigure = (AbstractTagFigure) affectedObject;
	}

	public void activate()
	{
		super.activate();
		getEditor().startUndo("Rotate Socket");
	}

	public void mouseDown(MouseEvent e, int x, int y)
	{
		// Update selection in view
		getView().singleSelect(tagFigure);

		anchorX = x;
		anchorY = y;
		moved = false;

		origin = tagFigure.getOrigin();
	}

	public void mouseDrag(MouseEvent e, int x, int y)
	{
		// Give a 4 pixel tolerance before beginning the move.
		// This prevents that a simple selection click might modify the drawing.
		moved = (Math.abs(x - anchorX) > 4) || (Math.abs(y - anchorY) > 4);
		if (moved)
		{
			// When rotating around rectangles, rather consider the position of the cursor relative
			// to the bounds of the rectangle instead of the mere angle of the cursor.
			// This will make the tag move more precise along the figure bounds as the cursor is being moved.

			// Take into account the offset to the figure's center when calculating the angle
			double angle = Math.atan2(y - origin.y, x - origin.x);
			angle = CircleConstants.normalizeAngle(angle);

			Rectangle box = tagFigure.getCenterFigureBox();

			// Determines in which section of the rectangle we are
			int dir = CircleConstants.determineEighth(angle, box);

			switch (dir)
			{
			case CircleConstants.EIGHTH_S:
			case CircleConstants.EIGHTH_SW:
				// Bottom
				y = box.y + box.height;
				break;

			case CircleConstants.EIGHTH_W:
			case CircleConstants.EIGHTH_NW:
				// Left
				x = box.x;
				break;

			case CircleConstants.EIGHTH_N:
			case CircleConstants.EIGHTH_NE:
				// Top
				y = box.y;
				break;

			case CircleConstants.EIGHTH_E:
			case CircleConstants.EIGHTH_SE:
				// Right
				x = box.x + box.width;
				break;
			}

			// Always add 1 px to prevent the tag layouter (see {@link MultiplexLayouter}) from switching the orientation
			if (x < box.x + 1)
				x = box.x + 1;
			if (x > box.x + box.width - 1)
				x = box.x + box.width - 1;
			if (y < box.y)
				y = box.y;
			if (y > box.y + box.height)
				y = box.y + box.height;

			angle = Math.atan2(y - origin.y, x - origin.x);
			angle = CircleConstants.normalizeAngle(angle);

			if (e.isShiftDown())
			{
				// Constrain angle
				angle /= (2d * Math.PI);
				angle = ((double) CommonUtil.rnd(angle * ANGLE_CONSTRAINT) / ANGLE_CONSTRAINT) * 2d * Math.PI;
			}

			tagFigure.setAngle(angle);
		}
	}

	public void mouseUp(MouseEvent e, int x, int y)
	{
		if (moved)
		{
			moved = false;

			getEditor().endUndo();
		}

		super.mouseUp(e, x, y);
	}
}
