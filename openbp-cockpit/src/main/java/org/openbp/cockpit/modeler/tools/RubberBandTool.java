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

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import org.openbp.cockpit.modeler.ModelerColors;
import org.openbp.cockpit.modeler.figures.process.NodeFigure;
import org.openbp.cockpit.modeler.figures.process.TextElementFigure;
import org.openbp.cockpit.modeler.figures.spline.PolySplineConnection;
import org.openbp.cockpit.modeler.util.InputState;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

/**
 * Performs a rubberband selection of an area.
 *
 * @author Stephan Pauxberger
 */
public class RubberBandTool extends ModelerTool
{
	/** Current selection rectangle */
	private Rectangle selectRect;

	/** X coordinate of the first click */
	private int anchorX;

	/** Y coordinate of the first click */
	private int anchorY;

	public RubberBandTool(ModelerToolSupport toolSupport)
	{
		super(toolSupport);
	}

	public void activate()
	{
		if (!InputState.isShiftDown())
		{
			getView().clearSelection();
		}
	}

	public void mouseDown(MouseEvent e, int x, int y)
	{
		getView().clearSelection();

		anchorX = x;
		anchorY = y;

		rubberBand(anchorX, anchorY, x, y);
	}

	public void mouseDrag(MouseEvent e, int x, int y)
	{
		rubberBand(anchorX, anchorY, x, y);
	}

	public void mouseUp(MouseEvent e, int x, int y)
	{
		eraseRubberBand();
		selectRectangle(e.isShiftDown());

		super.mouseUp(e, x, y);
	}

	/**
	 * Define the selection rectangle and draws the rubber band.
	 *
	 * @param x1 Rectangle coordinate
	 * @param y1 Rectangle coordinate
	 * @param x2 Rectangle coordinate
	 * @param y2 Rectangle coordinate
	 */
	private void rubberBand(int x1, int y1, int x2, int y2)
	{
		selectRect = new Rectangle(new Point(x1, y1));
		selectRect.add(new Point(x2, y2));

		drawXORRect(selectRect);
	}

	/**
	 * Erases the rubber band.
	 */
	public void eraseRubberBand()
	{
		drawXORRect(selectRect);
	}

	/**
	 * Draws a rectangle using an XOR operation.
	 * Fast way to turn a rectangly on or of
	 *
	 * @param r Rectangle to draw
	 */
	private void drawXORRect(Rectangle r)
	{
		r = getView().applyScale(r, false);

		Graphics g = getView().getGraphics();
		if (g != null)
		{
			try
			{
				g.setXORMode(getView().getBackground());
				g.setColor(ModelerColors.RUBBERBAND);
				g.drawRect(r.x, r.y, r.width, r.height);
			}
			finally
			{
				g.dispose();
			}
		}
	}

	/**
	 * Selects all figures within the given rectangle.
	 *
	 * @param toggle
	 * true: Toggles the selection state<br>
	 * false: Simply selects the figures
	 */
	protected void selectRectangle(boolean toggle)
	{
		for (FigureEnumeration fe = getDrawing().figures(); fe.hasMoreElements();)
		{
			Figure figure = fe.nextFigure();
			Figure selected = null;

			if (figure instanceof NodeFigure)
			{
				// The body of the node figure (excluding the tags) must be within the rectangle
				Rectangle r = ((NodeFigure) figure).compactDisplayBox();

				if (selectRect.contains(r.x, r.y) && selectRect.contains(r.x + r.width, r.y + r.height))
				{
					selected = figure;
				}
			}
			else if (figure instanceof PolySplineConnection)
			{
				// Start and end point of the spline have to be within the rectangle
				PolySplineConnection con = (PolySplineConnection) figure;

				if (selectRect.contains(con.startPoint()) && selectRect.contains(con.endPoint()))
				{
					selected = figure;
				}
			}
			else if (figure instanceof TextElementFigure)
			{
				// The body of the node figure (excluding the tags) must be within the rectangle
				Rectangle r = ((TextElementFigure) figure).displayBox();

				if (selectRect.contains(r.x, r.y) && selectRect.contains(r.x + r.width, r.y + r.height))
				{
					selected = figure;
				}
			}

			if (selected != null)
			{
				if (toggle)
				{
					getView().toggleSelection(selected);
				}
				else
				{
					getView().addToSelection(selected);
				}
			}
		}

		getView().redraw();
	}
}
