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
package org.openbp.cockpit.modeler.figures.process;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.figures.VisualElement;
import org.openbp.cockpit.modeler.figures.VisualElementEvent;
import org.openbp.cockpit.modeler.figures.generic.GeometryUtil;
import org.openbp.common.CollectionUtil;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.FigureChangeListener;
import CH.ifa.draw.standard.AbstractFigure;

/**
 * Abstract line figure.
 *
 * @author Heiko Erhardt
 */
public abstract class LineFigure extends AbstractFigure
	implements VisualElement
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Vertical line */
	private boolean verticalLine;

	/** Stroke */
	private Stroke stroke;

	/** Color */
	private Color color;

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Position of the line (x coordinate) */
	protected int xPos;

	/** Position of the line (y coordinate) */
	protected int yPos;

	/** The visual status as defined in org.openbp.cockpit.modeler.figures.VisualElement */
	private int visualStatus = VISUAL_VISIBLE;

	/** Process drawing we belong to */
	private ProcessDrawing drawing;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param drawing Process drawing we belong to
	 */
	public LineFigure(ProcessDrawing drawing)
	{
		this.drawing = drawing;
	}

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/**
	 * Gets the vertical line.
	 * @nowarn
	 */
	public boolean isVerticalLine()
	{
		return verticalLine;
	}

	/**
	 * Sets the vertical line.
	 * @nowarn
	 */
	public void setVerticalLine(boolean verticalLine)
	{
		this.verticalLine = verticalLine;
	}

	/**
	 * Gets the stroke.
	 * @nowarn
	 */
	public Stroke getStroke()
	{
		return stroke;
	}

	/**
	 * Sets the stroke.
	 * @nowarn
	 */
	public void setStroke(Stroke stroke)
	{
		this.stroke = stroke;
	}

	/**
	 * Gets the color.
	 * @nowarn
	 */
	public Color getColor()
	{
		return color;
	}

	/**
	 * Sets the color.
	 * @nowarn
	 */
	public void setColor(Color color)
	{
		this.color = color;
	}

	//////////////////////////////////////////////////
	// @@ AbstractFigure overrides
	//////////////////////////////////////////////////

	/**
	 * We display a single font size handle in the top left corner only.
	 * @see CH.ifa.draw.standard.AbstractFigure#handles()
	 */
	public Vector handles()
	{
		return CollectionUtil.EMPTY_VECTOR;
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#basicDisplayBox(Point origin, Point corner)
	 */
	public void basicDisplayBox(Point origin, Point corner)
	{
		xPos = origin.x;
		if (xPos < 0)
			xPos = 0;
		yPos = origin.y;
		if (yPos < 0)
			yPos = 0;
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#displayBox()
	 */
	public Rectangle displayBox()
	{
		// We keep the display box small in order not to affect the entire drawing size
		// Instead, we override invalidate() in derived classes
		return new Rectangle(xPos - 1, yPos - 1, 3, 3);
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#invalidate()
	 */
	public void invalidate()
	{
		FigureChangeListener l = listener();
		if (l != null)
		{
			Rectangle r = infiniteDisplayBox();
			l.figureInvalidated(new FigureChangeEvent(this, r));
		}
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#basicMoveBy(int x, int y)
	 */
	protected void basicMoveBy(int x, int y)
	{
		xPos += x;
		if (xPos < 0)
			xPos = 0;
		yPos += y;
		if (yPos < 0)
			yPos = 0;
	}

	/**
	 * Returns the display box of the figure that extends until infinity (or at least up to a very high value).
	 * @return The display box
	 */
	public Rectangle infiniteDisplayBox()
	{
		Rectangle r;
		if (verticalLine)
			r = new Rectangle(xPos - 1, 0, 3, 10000);
		else
			r = new Rectangle(0, yPos - 1, 10000, 3);
		return r;
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#containsPoint(int, int)
	 */
	public boolean containsPoint(int x, int y)
	{
		if (verticalLine)
			return x >= xPos - 4 && x <= xPos + 4;
		else
			return y >= yPos - 4 && y <= yPos + 4;
	}

	/**
	 * Draws the figure in the given graphics.
	 *
	 * @param g Graphics to draw to
	 * @see CH.ifa.draw.standard.AbstractFigure#draw(Graphics g)
	 */
	public void draw(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;

		Color oldColor = g2.getColor();
		g2.setColor(getColor());

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(getStroke());

		if (verticalLine)
			g.drawLine(xPos, 0, xPos, 10000);
		else
			g.drawLine(0, yPos, 10000, yPos);

		g2.setColor(oldColor);
		g2.setStroke(oldStroke);
	}

	//////////////////////////////////////////////////
	// @@ Geometry serialization support
	//////////////////////////////////////////////////

	/**
	 * Decodes enclosed geometry information.
	 *
	 * @param geometry Geometry string to decode or null
	 */
	public void decodeGeometry(String geometry)
	{
		if (geometry == null)
			return;

		StringTokenizer st = new StringTokenizer(geometry, ":");
		while (st.hasMoreTokens())
		{
			String ident = st.nextToken();
			if (ident.equalsIgnoreCase("x"))
			{
				xPos = GeometryUtil.parseInt(st, ident, "line figure");
			}
			else if (ident.equalsIgnoreCase("y"))
			{
				yPos = GeometryUtil.parseInt(st, ident, "line figure");
			}
		}
	}

	/**
	 * Encodes the figure geometry in a string.
	 *
	 * @return Geometry string containing "|" and ":" separated tokens
	 */
	public String encodeGeometry()
	{
		return "x:" + xPos + ":y:" + yPos;
	}

	//////////////////////////////////////////////////
	// @@ VisualElement implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#setDrawing(ProcessDrawing)
	 */
	public void setDrawing(ProcessDrawing drawing)
	{
		this.drawing = drawing;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#getDrawing()
	 */
	public ProcessDrawing getDrawing()
	{
		return drawing;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#getParentElement()
	 */
	public VisualElement getParentElement()
	{
		return getDrawing();
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#getPresentationFigure()
	 */
	public Figure getPresentationFigure()
	{
		return null;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#updatePresentationFigure()
	 */
	public void updatePresentationFigure()
	{
		// No dynamic presentation figure, so do nothing
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#isVisible()
	 */
	public boolean isVisible()
	{
		return (visualStatus & VisualElement.VISUAL_VISIBLE) != 0;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#setVisible(boolean)
	 */
	public void setVisible(boolean visible)
	{
		willChange();

		if (visible)
		{
			visualStatus |= VisualElement.VISUAL_VISIBLE;
		}
		else
		{
			visualStatus &= ~VisualElement.VISUAL_VISIBLE;
		}

		changed();
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#handleEvent(VisualElementEvent event)
	 */
	public boolean handleEvent(VisualElementEvent event)
	{
		return false;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#findVisualElement(int, int)
	 */
	public VisualElement findVisualElement(int x, int y)
	{
		return this;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#findVisualElementInside(int, int)
	 */
	public VisualElement findVisualElementInside(int x, int y)
	{
		return this;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ InteractionClient implementation
	// Dropping not allowed, nevertheless we have to implement it.
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragActionTriggered(Object, Point)
	 */
	public void dragActionTriggered(Object regionId, Point p)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragEnded(Transferable)
	 */
	public void dragEnded(Transferable transferable)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragStarted(Transferable)
	 */
	public void dragStarted(Transferable transferable)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getAllDropRegions(List, Transferable, MouseEvent)
	 */
	public List getAllDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		return getDropRegions(flavors, data, mouseEvent);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getDropRegions(List, Transferable, MouseEvent)
	 */
	public List getDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getImportersAt(Point)
	 */
	public List getImportersAt(Point p)
	{
		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getAllImportersAt(Point)
	 */
	public List getAllImportersAt(Point p)
	{
		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getSubClients()
	 */
	public List getSubClients()
	{
		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#importData(Object, Transferable, Point)
	 */
	public boolean importData(Object regionId, Transferable data, Point p)
	{
		return false;
	}
}
