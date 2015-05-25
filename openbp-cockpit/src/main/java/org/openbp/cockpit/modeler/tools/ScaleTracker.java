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

import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import org.openbp.cockpit.modeler.figures.VisualElement;
import org.openbp.cockpit.modeler.figures.generic.XCircleFigure;
import org.openbp.cockpit.modeler.figures.generic.XFigure;
import org.openbp.cockpit.modeler.figures.generic.XFigureDescriptor;
import org.openbp.cockpit.modeler.figures.generic.XTriangleFigure;
import org.openbp.cockpit.modeler.figures.process.NodeFigure;

import CH.ifa.draw.framework.Figure;

/**
 * The scale tracker is used to scale a figure.
 *
 * @author Stephan Pauxberger
 */
public class ScaleTracker extends ModelerTool
{
	/** X coordinate of the first click */
	private int anchorX;

	/** Y coordinate of the first click */
	private int anchorY;

	/** Original size of the figure */
	private int origX;

	/** Original size of the figure */
	private int origY;

	/** Flag if scaling should be performed */
	private boolean moved;

	/** Maintain aspect ratio */
	private boolean maintainRatio;

	/** Resize height only */
	private boolean heightOnly;

	public ScaleTracker(ModelerToolSupport toolSupport)
	{
		super(toolSupport);
	}

	public void setAffectedObject(Object affectedObject)
	{
		super.setAffectedObject(affectedObject);

		if (affectedObject != null)
		{
			// Make sure we maintain the aspect ration when scaling circles and triangles
			Figure presentationFigure = getAffectedFigure();
			if (affectedObject instanceof VisualElement)
			{
				presentationFigure = ((VisualElement) affectedObject).getPresentationFigure();
			}

			maintainRatio = presentationFigure instanceof XTriangleFigure;
			heightOnly = presentationFigure instanceof XCircleFigure;

			Rectangle db = presentationFigure.displayBox();
			origX = db.width;
			origY = db.height;
		}
	}

	public void activate()
	{
		super.activate();
		getEditor().startUndo("Resize Element");
	}

	public void mouseDown(MouseEvent e, int x, int y)
	{
		anchorX = x;
		anchorY = y;
		moved = false;
	}

	public void mouseDrag(MouseEvent e, int x, int y)
	{
		// Give a 4 pixel tolerance before beginning the move.
		// This prevents that a simple selection click might modify the drawing.
		moved = (Math.abs(x - anchorX) > 4) || (Math.abs(y - anchorY) > 4);
		if (moved)
		{
			int minX = 20;
			int minY = 100;
			int maxX = 20;
			int maxY = 500;

			NodeFigure nf = null;
			if (getAffectedObject() instanceof NodeFigure)
			{
				nf = (NodeFigure) getAffectedObject();
				XFigure presentationFigure = (XFigure) nf.getPresentationFigure();
				XFigureDescriptor fd = presentationFigure.getDescriptor();
				minX = fd.getMinSizeX();
				minY = fd.getMinSizeY();
				maxX = fd.getMaxSizeX();
				maxY = fd.getMaxSizeY();
			}

			int newX = origX * (100 + x - anchorX) / 100;
			int newY = origY * (100 + y - anchorY) / 100;
			newX = Math.min(newX, maxX);
			newY = Math.min(newY, maxY);
			newX = Math.max(newX, minX);
			newY = Math.max(newY, minY);

			if (nf != null)
			{
				if (maintainRatio)
				{
					// Maintain aspect ratio for triangle figures
					newY = newX;
				}
				else if (heightOnly)
				{
					// When resizing circle figures, only change the figure height
					// since it determines the radius of the circle
					newY = origY * (100 + x - anchorX) / 100;
					newY = Math.min(newY, maxY);
					newY = Math.max(newY, minY);

					newX = origX;
				}

				nf.setSize(newX, newY);
			}
			else
			{
				Rectangle db = getAffectedFigure().displayBox();
				db.grow((newX - db.width) / 2, (newY - db.height) / 2);
				getAffectedFigure().displayBox(db);
			}
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
