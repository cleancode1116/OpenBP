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
package org.openbp.cockpit.modeler.figures.spline;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import org.openbp.cockpit.modeler.AutoConnector;
import org.openbp.cockpit.modeler.ModelerColors;
import org.openbp.cockpit.modeler.drawing.WorkspaceDrawingView;
import org.openbp.cockpit.modeler.figures.generic.BasicFigure;
import org.openbp.cockpit.modeler.figures.process.ParamConnection;
import org.openbp.cockpit.modeler.util.FigureResources;
import org.openbp.cockpit.modeler.util.InputState;
import org.openbp.core.model.item.process.DataLink;
import org.openbp.core.model.item.process.DataLinkImpl;
import org.openbp.jaspira.decoration.DecorationMgr;
import org.openbp.jaspira.decoration.Decorator;
import org.openbp.jaspira.decoration.FilteredDecorator;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.standard.AbstractHandle;
import CH.ifa.draw.util.Geom;

/**
 * ChangeConnectionHandle factors the common code for handles
 * that can be used to reconnect connections.
 *
 * @see ChangeConnectionEndHandle
 * @see ChangeConnectionStartHandle
 *
 * @author Stephan Moritz
 */
public abstract class ChangeConnectionHandle extends AbstractHandle
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Connection the handle belongs to */
	protected PolySplineConnection connectionFigure;

	/** Target figure */
	private Figure targetFigure;

	/** Original target figure */
	private Connector originalTarget;

	/** Impossible decorator */
	private Decorator canConnectDecorator = new CanConnectDecorator();

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Initializes the change connection handle.
	 *
	 * @param owner Owner figure of the handle
	 */
	protected ChangeConnectionHandle(Figure owner)
	{
		super(owner);
		connectionFigure = (PolySplineConnection) owner();
	}

	/**
	 * Disconnects the connection.
	 */
	protected abstract void disconnect();

	/**
	 * Connect the spline with the given figure.
	 * @param c Connector of the figure to connect to
	 */
	protected abstract void connect(Connector c);

	/**
	 * Sets the location of the target point.
	 *
	 * @param x Document coordinate
	 * @param y Document coordinate
	 */
	protected abstract void setPoint(int x, int y);

	/**
	 * Returns the target connector of the change.
	 * @nowarn
	 */
	protected abstract Connector target();

	/**
	 * Gets the side of the connection that is unaffected by the change.
	 * @nowarn
	 */
	protected Connector source()
	{
		if (target() == connectionFigure.getStartConnector())
		{
			return connectionFigure.getEndConnector();
		}
		return connectionFigure.getStartConnector();
	}

	//////////////////////////////////////////////////
	// @@ AbstractHandle overrides
	//////////////////////////////////////////////////

	/**
	 * Disconnects the connection.
	 *
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @param view View to draw on
	 * @see CH.ifa.draw.standard.AbstractHandle#invokeStart(int x, int y, DrawingView view)
	 */
	public void invokeStart(int x, int y, DrawingView view)
	{
		originalTarget = target();

		disconnect();

		DecorationMgr.addDecorator(null, BasicFigure.DECO_OVERLAY, canConnectDecorator);

		((WorkspaceDrawingView) view).redraw();
	}

	/**
	 * Finds a new target of the connection.
	 *
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @param anchorX Document coordinate of the anchor point of the handle
	 * @param anchorY Document coordinate of the anchor point of the handle
	 * @param view View to draw on
	 * @see CH.ifa.draw.standard.AbstractHandle#invokeStep(int x, int y, int anchorX, int anchorY, DrawingView view)
	 */
	public void invokeStep(int x, int y, int anchorX, int anchorY, DrawingView view)
	{
		Point p = new Point(x, y);
		Figure figure = findConnectableFigure(x, y, view.drawing());

		// track the figure containing the mouse
		if (figure != targetFigure)
		{
			if (targetFigure != null)
			{
				targetFigure.connectorVisibility(false);
			}
			targetFigure = figure;
			if (targetFigure != null)
			{
				targetFigure.connectorVisibility(true);
			}
		}

		Connector target = findConnectionTarget(p.x, p.y, view.drawing());
		if (target != null)
		{
			p = Geom.center(target.displayBox());
		}
		setPoint(p.x, p.y);
	}

	/**
	 * Connects the figure to the new target.
	 * If there is no new target the connection reverts to its original one.
	 *
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @param anchorX Document coordinate of the anchor point of the handle
	 * @param anchorY Document coordinate of the anchor point of the handle
	 * @param view View to draw on
	 * @see CH.ifa.draw.standard.AbstractHandle#invokeEnd(int x, int y, int anchorX, int anchorY, DrawingView view)
	 */
	public void invokeEnd(int x, int y, int anchorX, int anchorY, DrawingView view)
	{
		DecorationMgr.removeDecorator(null, BasicFigure.DECO_OVERLAY, canConnectDecorator);
		((WorkspaceDrawingView) view).redraw();

		Connector target = findConnectionTarget(x, y, view.drawing());
		if (target == null)
		{
			target = originalTarget;
		}

		setPoint(x, y);
		connect(target);

		if (connectionFigure instanceof ParamConnection)
		{
			ParamConnection paramConnection = (ParamConnection) connectionFigure;
			DataLink dataLink = paramConnection.getDataLink();

			String newSourceMemberPath = DataLinkImpl.checkAutoConversion(dataLink.getSourceParam(), dataLink.getSourceMemberPath(), dataLink.getTargetParam(), dataLink.getTargetMemberPath());
			if (newSourceMemberPath != null)
			{
				dataLink.setSourceMemberPath(newSourceMemberPath);
			}
		}

		// connectionFigure.updateConnection ();
		connectionFigure.layoutAndAdjustConnection();

		((WorkspaceDrawingView) view).singleSelect(connectionFigure);

		if (targetFigure != null)
		{
			targetFigure.connectorVisibility(false);
			targetFigure = null;
		}
	}

	/**
	 * Finds a connection target the given position.
	 *
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @param drawing Process drawing
	 * @return The connector of the target or null if there is none at this position
	 */
	private Connector findConnectionTarget(int x, int y, Drawing drawing)
	{
		Figure target = findConnectableFigure(x, y, drawing);

		if (target != null && connectionFigure != null && target.canConnect() && !target.includes(source().owner()) && canLinkFigures(source().owner(), target))
		{
			return target.connectorAt(x, y);
		}
		return null;
	}

	/**
	 * Finds a connection target of the given figure at the given position.
	 *
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @param figure Figure to search
	 * @return The connector of the target or null if there is none at this position
	 */
	protected Connector findConnector(int x, int y, Figure figure)
	{
		return figure.connectorAt(x, y);
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractHandle#draw(Graphics g)
	 */
	public void draw(Graphics g)
	{
		Rectangle r = displayBox();

		g.setColor(ModelerColors.HANDLE_ANGLE_FILL);
		g.fillRect(r.x, r.y, r.width, r.height);

		g.setColor(ModelerColors.HANDLE_ANGLE_BORDER);
		g.drawRect(r.x, r.y, r.width, r.height);
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Finds a figure we can connect to.
	 *
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @param drawing Process drawing
	 * @return The figure or null if there is no figure or the figure is not connectable
	 */
	private Figure findConnectableFigure(int x, int y, Drawing drawing)
	{
		for (FigureEnumeration k = drawing.figures(); k.hasMoreElements();)
		{
			Figure figure = k.nextFigure();
			figure = figure.findFigureInside(x, y);

			if (figure != null && !figure.includes(connectionFigure) && figure.canConnect())
			{
				return figure;
			}
		}
		return null;
	}

	/**
	 * Checks if two figures can be connected using a control link.
	 * In case of parameter figures, the method will check if the data types expected/returned by the parameters are compatible.
	 * If the CTRL key is pressed, this check will be omitted. This is useful if the data link properties (source/target member path)
	 * need to be edited in order to make the figure data types compatible.
	 *
	 * @param start Start figure
	 * @param target Target figure
	 * @return
	 * true: The figures can be connected.<br>
	 * false: The figures can be connected.
	 */
	public boolean canLinkFigures(Figure start, Figure target)
	{
		int flags = 0;
		if (InputState.isShiftDown())
		{
			flags = DataLink.LINK_OMIT_TYPE_CHECK;
		}
		else
		{
			if (AutoConnector.getDataLinkAutoConnectorMode() >= AutoConnector.DLA_CONVERTIBLE_TYPES)
			{
				flags = DataLink.LINK_AUTOCONVERSION;
			}
		}
		return connectionFigure.canConnectFigures(start, target, flags);
	}

	//////////////////////////////////////////////////
	// @@ Decorators
	//////////////////////////////////////////////////

	/**
	 * Decorator that decorates a figure if the source of the connection can connect to it
	 * with a transparent green overlay rectangle.
	 */
	public class CanConnectDecorator extends FilteredDecorator
	{
		/**
		 * @see FilteredDecorator#doDecorate(Object, String, Object)
		 */
		public Object doDecorate(Object owner, String key, Object value)
		{
			return FigureResources.getAcceptOverlay(owner);
		}

		/**
		 * @see FilteredDecorator#qualifies(Object)
		 */
		public boolean qualifies(Object owner)
		{
			if (connectionFigure == null)
			{
				return false;
			}
			return canLinkFigures(source().owner(), (Figure) owner);
		}
	}
}
