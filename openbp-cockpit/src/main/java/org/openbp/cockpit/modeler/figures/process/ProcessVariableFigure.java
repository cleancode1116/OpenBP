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
import org.openbp.cockpit.modeler.figures.generic.SimpleImageFigure;
import org.openbp.cockpit.modeler.figures.generic.XFigure;
import org.openbp.common.icon.FlexibleSize;
import org.openbp.common.util.ToStringHelper;
import org.openbp.core.model.item.process.ProcessObject;
import org.openbp.core.model.item.process.ProcessVariable;
import org.openbp.guiclient.model.item.ItemIconMgr;
import org.openbp.jaspira.decoration.DecorationMgr;
import org.openbp.jaspira.gui.interaction.DropClientUtil;

import CH.ifa.draw.framework.Figure;

/**
 * This is the figurative representation of a process variable.
 *
 * @author Stephan Moritz
 */
public class ProcessVariableFigure extends SimpleImageFigure
	implements ProcessElementContainer
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** The param that this figure represents */
	private ProcessVariable param;

	/** Parent figure (the param tag figure we are associated with) */
	private ParamFigure parent;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param param Param the figure belongs to
	 * @param parent Parent figure
	 */
	public ProcessVariableFigure(ProcessVariable param, ParamFigure parent)
	{
		super(ItemIconMgr.getInstance().getTypeIcon(parent.getDrawing().getProcessSkin().getName(), param.getDataType(), FlexibleSize.SMALL));

		this.param = param;
		param.setRepresentation(this);

		this.parent = parent;
	}

	/**
	 * Gets the the param that this figure represents.
	 * @nowarn
	 */
	public ProcessVariable getProcessVariable()
	{
		return param;
	}

	/**
	 * Returns a string representation of this object.
	 * @nowarn
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "paramFigure", "ProcessVariable");
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Figure overrides
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see CH.ifa.draw.framework.Figure#canConnect()
	 */
	public boolean canConnect()
	{
		return false;
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#handles()
	 */
	public Vector handles()
	{
		return parent.handles();
	}

	/**
	 * Draws the icon. Checks via the decoration manager for stroke and framecolor decorations,
	 * if so, draws a frame around the image using the given parameters.
	 *
	 * @param g The graphics object to draw upon
	 * @see CH.ifa.draw.framework.Figure#draw(Graphics g)
	 */
	public void draw(Graphics g)
	{
		if (!ViewModeMgr.getInstance().isDataLinkVisible(this))
		{
			// Don't paint if data link display is turned off
			return;
		}

		super.draw(g);

		Stroke stroke = (Stroke) DecorationMgr.decorate(this, XFigure.DECO_FRAMESTROKE, null);
		Color frame = (Color) DecorationMgr.decorate(this, XFigure.DECO_FRAMECOLOR, null);

		if (frame != null && stroke != null)
		{
			Graphics2D g2 = (Graphics2D) g;

			g2.setStroke(stroke);

			g.setColor(frame);
			Rectangle r = displayBox();
			g.drawRoundRect(r.x, r.y, r.width - 1, r.height - 1, 8, 8);
		}
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#containsPoint(int x, int y)
	 */
	public boolean containsPoint(int x, int y)
	{
		if (!ViewModeMgr.getInstance().isDataLinkVisible(this))
		{
			// We won't react on user interaction if we are not displayed
			return false;
		}

		return super.containsPoint(x, y);
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#release()
	 */
	public void release()
	{
		super.release();

		parent.removeProcessVariableConnection();
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ ProcessElementContainer implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#getProcessElement()
	 */
	public ProcessObject getProcessElement()
	{
		return param;
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
		return parent.getDrawing();
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#getParentElement()
	 */
	public VisualElement getParentElement()
	{
		return parent;
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
		return findProcessElementContainerInside(x, y);
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
		return DropClientUtil.getAllImportersAt(this, p);
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
		DropClientUtil.dragStarted(this, transferable);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragEnded(Transferable)
	 */
	public void dragEnded(Transferable transferable)
	{
		DropClientUtil.dragEnded(this, transferable);
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
