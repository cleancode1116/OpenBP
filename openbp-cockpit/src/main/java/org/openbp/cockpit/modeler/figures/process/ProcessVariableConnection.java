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
import java.util.Vector;

import org.openbp.cockpit.modeler.ViewModeMgr;
import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.figures.VisualElement;
import org.openbp.cockpit.modeler.figures.VisualElementEvent;
import org.openbp.cockpit.modeler.figures.generic.Orientation;
import org.openbp.cockpit.modeler.figures.generic.XArrowTip;
import org.openbp.cockpit.modeler.figures.generic.XFigure;
import org.openbp.cockpit.modeler.skins.LinkDescriptor;
import org.openbp.cockpit.modeler.util.FigureResources;
import org.openbp.common.util.ToStringHelper;
import org.openbp.core.model.item.process.DataLink;
import org.openbp.core.model.item.process.ProcessObject;
import org.openbp.jaspira.decoration.DecorationMgr;

import CH.ifa.draw.figures.LineDecoration;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.standard.AbstractFigure;

/**
 * Graphical represention of a datalink between a node parameter and a process variable.
 * Note that this figure is not really a connection but a symbol that is to be displayed
 * as part a {@link ParamFigure}
 *
 * @author Stephan Moritz
 */
public class ProcessVariableConnection extends AbstractFigure
	implements ProcessElementContainer
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Constants
	/////////////////////////////////////////////////////////////////////////

	/** Decoration for the end of the connection (arrow) */
	private static final LineDecoration endDecoration = new XArrowTip(0.4, 12, 9);

	public static final int DEFAULT_WIDTH = 20;

	public static final int DEFAULT_HEIGHT = 5;

	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** The data link that this figure represents */
	private DataLink dataLink;

	/** The parent figure */
	private ParamFigure paramFigure;

	/** Position */
	private Point start;

	/** Stroke */
	private Stroke stroke = FigureResources.standardStroke1;

	/** Color */
	private Color color = Color.BLACK;

	/** Color2 */
	private Color color2 = Color.BLACK;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param link Link represented by this figure
	 * @param paramFigure Source parameter figure of the global link
	 */
	public ProcessVariableConnection(DataLink link, ParamFigure paramFigure)
	{
		this.paramFigure = paramFigure;

		this.dataLink = link;
		dataLink.setRepresentation(this);

		start = new Point();

		LinkDescriptor desc = getDrawing().getProcessSkin().getLinkDescriptor(FigureTypes.LINKTYPE_DATA);
		if (desc != null)
		{
			stroke = desc.getStroke();
			color = desc.getColor();
			color2 = desc.getColor2();
		}
	}

	/**
	 * Gets the the data link that this figure represents.
	 * @nowarn
	 */
	public DataLink getDataLink()
	{
		return dataLink;
	}

	/**
	 * Returns a string representation of this object.
	 * @nowarn
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "paramFigure", "DataLink");
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Figure overrides
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#basicMoveBy(int dx, int dy)
	 */
	protected void basicMoveBy(int dx, int dy)
	{
		start.x += dx;
		start.y += dy;
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#basicDisplayBox(Point start, Point end)
	 */
	public void basicDisplayBox(Point start, Point end)
	{
		this.start = start;
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#displayBox()
	 */
	public Rectangle displayBox()
	{
		int w, h;
		if (paramFigure.isVerticalOrientation())
		{
			w = DEFAULT_HEIGHT;
			h = DEFAULT_WIDTH;
		}
		else
		{
			w = DEFAULT_WIDTH;
			h = DEFAULT_HEIGHT;
		}

		return new Rectangle(start.x, start.y, w, h);
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#handles()
	 */
	public Vector handles()
	{
		return paramFigure.handles();
	}

	/**
	 * Draws the connection. If the decoration manager reports stroke and frame
	 * decorations for this figure, overlays the figure with a rectangle.
	 * @see CH.ifa.draw.standard.AbstractFigure#draw(Graphics g)
	 */
	public void draw(Graphics g)
	{
		if (!ViewModeMgr.getInstance().isDataLinkVisible(this))
		{
			// Don't paint if data link display is turned off
			return;
		}

		Color c;
		if (dataLink.getSourceMemberPath() != null || dataLink.getTargetMemberPath() != null)
			c = color2;
		else
			c = color;
		g.setColor(c);
		((Graphics2D) g).setStroke(stroke);

		Point p1 = new Point();
		Point p2 = new Point();

		Rectangle r = displayBox();
		getPoints(r, p1, p2);

		g.drawLine(p1.x, p1.y, p2.x, p2.y);

		endDecoration.draw(g, p2.x, p2.y, p1.x, p1.y);

		Stroke stroke = (Stroke) DecorationMgr.decorate(this, XFigure.DECO_FRAMESTROKE, null);
		Color frame = (Color) DecorationMgr.decorate(this, XFigure.DECO_FRAMECOLOR, null);

		if (frame != null && stroke != null)
		{
			Graphics2D g2 = (Graphics2D) g;

			g2.setStroke(stroke);
			g.setColor(frame);

			if (paramFigure.isVerticalOrientation())
			{
				g.drawRoundRect(r.x - 9, r.y - 1, r.width + 17, r.height + 1, 8, 8);
			}
			else
			{
				g.drawRoundRect(r.x - 1, r.y - 9, r.width + 1, r.height + 17, 8, 8);
			}
		}
	}

	private void getPoints(Rectangle r, Point p1, Point p2)
	{
		SocketFigure socketFigure = (SocketFigure) paramFigure.getParent();
		boolean isExit = !socketFigure.isEntrySocket();

		if (paramFigure.isVerticalOrientation())
		{
			// Determine the top/bottom orientation;
			// XOR both sides (exactly one of both must be true, not both)
			Orientation socketOrientation = socketFigure.determine2WayOrientation(true);
			boolean topToBottom = socketOrientation == Orientation.TOP ^ isExit;

			int x = r.x + r.width / 2;

			if (topToBottom)
			{
				// Top -> Bottom
				p1.setLocation(x, r.y);
				p2.setLocation(x, r.y + r.height);
			}
			else
			{
				// Bottom -> Top
				p2.setLocation(x, r.y);
				p1.setLocation(x, r.y + r.height);
			}
		}
		else
		{
			// Determine the left/right orientation;
			// XOR both sides (exactly one of both must be true, not both)
			Orientation socketOrientation = socketFigure.determine2WayOrientation(false);
			boolean leftToRight = socketOrientation == Orientation.LEFT ^ isExit;

			int y = r.y + r.height / 2;

			if (leftToRight)
			{
				// Left -> Right
				p1.setLocation(r.x, y);
				p2.setLocation(r.x + r.width, y);
			}
			else
			{
				// Right -> Left
				p2.setLocation(r.x, y);
				p1.setLocation(r.x + r.width, y);
			}
		}
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#canConnect()
	 */
	public boolean canConnect()
	{
		return false;
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#release()
	 */
	public void release()
	{
		super.release();

		paramFigure.removeProcessVariableConnection();
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ ProcessElementContainer implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#getProcessElement()
	 */
	public ProcessObject getProcessElement()
	{
		return dataLink;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#getReferredProcessElement()
	 */
	public ProcessObject getReferredProcessElement()
	{
		return getProcessElement();
	}

	/**
	 * No object should be selected on deletion.
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#selectionOnDelete()
	 */
	public Figure selectionOnDelete()
	{
		return null;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#findProcessElementContainer(int, int)
	 */
	public ProcessElementContainer findProcessElementContainer(int x, int y)
	{
		return null;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#findProcessElementContainerInside(int, int)
	 */
	public ProcessElementContainer findProcessElementContainerInside(int x, int y)
	{
		return this.containsPoint(x, y) ? this : null;
	}

	//////////////////////////////////////////////////
	// @@ VisualElement implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#setDrawing(ProcessDrawing)
	 */
	public void setDrawing(ProcessDrawing drawing)
	{
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#getDrawing()
	 */
	public ProcessDrawing getDrawing()
	{
		return paramFigure.getDrawing();
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#getParentElement()
	 */
	public VisualElement getParentElement()
	{
		return paramFigure;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#getPresentationFigure()
	 */
	public Figure getPresentationFigure()
	{
		return this;
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
		return false;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#setVisible(boolean)
	 */
	public void setVisible(boolean visible)
	{
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
		return null;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#findVisualElementInside(int, int)
	 */
	public VisualElement findVisualElementInside(int x, int y)
	{
		return this.containsPoint(x, y) ? this : null;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ InteractionClient implementation
	/////////////////////////////////////////////////////////////////////////

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
		return getImportersAt(p);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getAllDropRegions(List, Transferable, MouseEvent)
	 */
	public List getAllDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
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

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragStarted(Transferable)
	 */
	public void dragStarted(Transferable transferable)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragEnded(Transferable)
	 */
	public void dragEnded(Transferable transferable)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragActionTriggered(Object, Point)
	 */
	public void dragActionTriggered(Object regionId, Point p)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getSubClients()
	 */
	public List getSubClients()
	{
		return null;
	}

	//////////////////////////////////////////////////
	// @@ UpdatableFigure implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.generic.UpdatableFigure#updateFigure()
	 */
	public void updateFigure()
	{
		// Nothing changed
	}
}
