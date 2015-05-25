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
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.ToolTipManager;

import org.openbp.cockpit.modeler.AutoConnector;
import org.openbp.cockpit.modeler.Modeler;
import org.openbp.cockpit.modeler.ViewModeMgr;
import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.figures.VisualElement;
import org.openbp.cockpit.modeler.figures.VisualElementEvent;
import org.openbp.cockpit.modeler.figures.generic.BasicFigure;
import org.openbp.cockpit.modeler.figures.process.FlowConnection;
import org.openbp.cockpit.modeler.figures.process.NodeFigure;
import org.openbp.cockpit.modeler.figures.process.ParamConnection;
import org.openbp.cockpit.modeler.figures.process.ProcessElementContainer;
import org.openbp.cockpit.modeler.figures.process.SocketFigure;
import org.openbp.cockpit.modeler.figures.spline.PolySplineConnection;
import org.openbp.cockpit.modeler.figures.tag.TagConnector;
import org.openbp.cockpit.modeler.util.FigureResources;
import org.openbp.cockpit.modeler.util.InputState;
import org.openbp.core.model.item.process.DataLink;
import org.openbp.core.model.item.process.DataLinkImpl;
import org.openbp.jaspira.decoration.DecorationMgr;
import org.openbp.jaspira.decoration.Decorator;
import org.openbp.jaspira.decoration.FilteredDecorator;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

/**
 * The connection tool can be used to connect figures, to split
 * connections, and to join two segments of a connection.
 * The connection tool turns the visibility of the connectors on when it enters a figure.
 * The connection object to be created is specified by a prototype.
 *
 * @author Stephan Pauxberger
 */
public abstract class XConnectionTool extends ModelerTool
{
	/** The currently created connection */
	private PolySplineConnection connectionFigure;

	/** Anchor point of the interaction */
	private Connector startConnector;

	/** Current target connector */
	private Connector targetConnector;

	/** Figure the target connector belongs to */
	private Figure targetFigure;

	/**
	 * The figure that was actually added as a result of the interaction.
	 * Note, this can be a different figure from the one which has been created.
	 */
	private Figure addedFigure;

	/** Decorator that paints figures we can connect to in green */
	private Decorator canConnectDecorator = new CanConnectDecorator();

	/** X coordinate of the first click */
	private int anchorX;

	/** Y coordinate of the first click */
	private int anchorY;

	/** Flag if scaling should be performed */
	private boolean moved;

	/** Flag if the object was already selected */
	private boolean wasSelected;

	public XConnectionTool(ModelerToolSupport toolSupport)
	{
		super(toolSupport);
	}

	/**
	 * Checks if the tool can be applied to the given figure.
	 *
	 * @param affectedObject Object the cursor is over
	 * @return true if the tool is suitable for the figure
	 */
	public boolean appliesTo(Object affectedObject)
	{
		/*
		if (affectedObject instanceof Figure)
		{
			if (((Figure) affectedObject).canConnect())
				return true;
		}
		return false;
		 */
		return true;
	}

	public void activate()
	{
		super.activate();
	}

	public void deactivate()
	{
		if (targetFigure != null)
		{
			targetFigure.connectorVisibility(false);
		}

		// Display all figures in the regular way again
		toggleDecorator(false);

		if (connectionFigure != null)
		{
			getView().remove(connectionFigure);
		}
		connectionFigure = null;
		startConnector = null;
		targetConnector = null;
		targetFigure = null;
		addedFigure = null;

		super.deactivate();
	}

	public void mouseDown(MouseEvent e, int x, int y)
	{
		anchorX = x;
		anchorY = y;
		moved = false;

		wasSelected = getView().isFigureSelected(getAffectedFigure());

		if (! wasSelected)
		{
			getView().singleSelect(getAffectedFigure());
		}

		targetFigure = findConnectionStart(x, y, getDrawing());

		if (targetFigure != null)
		{
			startConnector = findConnector(x, y, targetFigure);
			if (startConnector != null)
			{
				getEditor().startUndo("Create Link");

				connectionFigure = createConnection();
				connectionFigure.startPoint(x, y);
				connectionFigure.endPoint(x, y);

				addedFigure = getView().add(connectionFigure);

				// Display all figures we can connect to in green
				toggleDecorator(true);
			}
		}
	}

	public void mouseDrag(MouseEvent e, int x, int y)
	{
		// Give a 4 pixel tolerance before beginning the move.
		// This prevents that a simple selection click might modify the drawing.
		moved = (Math.abs(x - anchorX) > 4) || (Math.abs(y - anchorY) > 4);
		if (moved)
		{
			// This should cause the ToolTipManager to display tooltips while dragging.
			ToolTipManager.sharedInstance().mouseMoved(e);

			Point p = new Point(x, y);
			if (connectionFigure != null)
			{
				trackConnectors(e, x, y);

				if (targetConnector != null)
				{
					p = targetConnector.findEnd(connectionFigure);
				}
				connectionFigure.endPoint(p.x, p.y);
			}
		}
	}

	public void mouseUp(MouseEvent e, int x, int y)
	{
		Connector endConnector = null;
		if (moved)
		{
			Figure figure = null;
			if (startConnector != null)
			{
				figure = findTargetFigure(x, y, getDrawing());
				if (figure != null)
				{
					endConnector = findConnector(x, y, figure);
				}
			}

			// Display all figures in the regular way again (do this here in order to have access to the current connectors)
			toggleDecorator(false);

			if (figure != null)
			{
				if (endConnector != null)
				{
					connectionFigure.connectStart(startConnector);
					connectionFigure.connectEnd(endConnector);

					// Apply auto-conversions for data links if necessary
					if (connectionFigure instanceof ParamConnection)
					{
						ParamConnection paramConnection = (ParamConnection) connectionFigure;
						DataLink dataLink = paramConnection.getDataLink();

						String newSourceMemberPath = DataLinkImpl.checkAutoConversion(dataLink.getSourceParam(), dataLink.getSourceMemberPath(), dataLink.getTargetParam(), dataLink.getTargetMemberPath());
						dataLink.setSourceMemberPath(newSourceMemberPath);
					}

					connectionFigure.layoutAndAdjustConnection();

					connectionFigure.setDrawDecorations(true);

					getView().singleSelect(addedFigure);

					getEditor().endUndo();

					// Pressing the CTRL key while connecting two sockets will invoke the data link autoconnector.
					if (connectionFigure instanceof FlowConnection && !InputState.isCtrlDown())
					{
						FlowConnection flowConnectionFigure = (FlowConnection) connectionFigure;
						SocketFigure sourceSocketFigure = ((TagConnector) flowConnectionFigure.getStartConnector()).getSocketFigure();
						SocketFigure targetSocketFigure = ((TagConnector) flowConnectionFigure.getEndConnector()).getSocketFigure();
						AutoConnector autoConnector = new AutoConnector((Modeler) getEditor(), sourceSocketFigure, targetSocketFigure);
						autoConnector.autoConnectDataLinks();
					}
				}
			}
		}

		if (endConnector == null)
		{
			if (connectionFigure != null)
			{
				getView().remove(connectionFigure);
			}
		}

		connectionFigure = null;
		startConnector = null;
		addedFigure = null;

		super.mouseUp(e, x, y);

		if (! moved)
		{
			if (wasSelected)
			{
				VisualElement lastFigure = getToolSupport().getLastFigure();
				if (lastFigure instanceof ProcessElementContainer)
				{
					getToolSupport().toggleInPlaceEditor((ProcessElementContainer) lastFigure, true);
				}
			}
		}
	}

	public void mouseMove(MouseEvent e, int x, int y)
	{
		trackConnectors(e, x, y);
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Creates the connection figure.
	 * @return The prototype
	 */
	protected abstract PolySplineConnection createConnection();

	/**
	 * Tracks connectors.
	 *
	 * @param e Mouse event which should be interpreted
	 * @param x Document coordinate
	 * @param y Document coordinate
	 */
	protected void trackConnectors(MouseEvent e, int x, int y)
	{
		Figure figure = null;

		if (startConnector == null)
		{
			figure = findSourceFigure(x, y, getDrawing());
		}
		else
		{
			figure = findTargetFigure(x, y, getDrawing());
		}

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

		Connector connector = null;
		if (figure != null)
		{
			connector = findConnector(x, y, figure);
		}
		if (connector != targetConnector)
		{
			targetConnector = connector;
		}

		getView().checkDamage();
	}

	/**
	 * Finds a connectable figure as source at the given position.
	 *
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @param drawing Drawing we operate on
	 * @return The figure or null
	 */
	protected Figure findSourceFigure(int x, int y, Drawing drawing)
	{
		return findConnectableFigure(x, y, drawing);
	}

	/**
	 * Finds a connectable figure as target at the given position.
	 *
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @param drawing Drawing we operate on
	 * @return The figure or null
	 */
	protected Figure findTargetFigure(int x, int y, Drawing drawing)
	{
		Figure target = findConnectableFigure(x, y, drawing);
		Figure start = startConnector.owner();

		if (target != null && connectionFigure != null && target.canConnect() && !target.includes(start) && canLinkFigures(start, target))
		{
			return target;
		}
		return null;
	}

	/**
	 * Finds a connection start figure at the given position.
	 *
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @param drawing Drawing we operate on
	 * @return The figure or null
	 */
	protected Figure findConnectionStart(int x, int y, Drawing drawing)
	{
		Figure target = findConnectableFigure(x, y, drawing);
		if ((target != null) && target.canConnect())
		{
			return target;
		}
		return null;
	}

	/**
	 * Finds a figure we can connect to at the given position.
	 *
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @param drawing Drawing we operate on
	 * @return The figure or null
	 */
	protected Figure findConnectableFigure(int x, int y, Drawing drawing)
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
	 * Finds a connector of the given figure at the given position.
	 *
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @param figure Figure to search
	 * @return The connector or null
	 */
	protected Connector findConnector(int x, int y, Figure figure)
	{
		return figure.connectorAt(x, y);
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

	private void toggleDecorator(boolean on)
	{
		if (startConnector != null && connectionFigure instanceof FlowConnection)
		{
			Figure startSocketFigure = startConnector.owner();

			// If control connectors are hidden, make sure we display the connectors we can connect to
			if (!ViewModeMgr.getInstance().isControlAnchorVisible())
			{
				for (Iterator it = ((ProcessDrawing) getDrawing()).getNodeFigures(); it.hasNext();)
				{
					NodeFigure nodeFigure = (NodeFigure) it.next();

					for (FigureEnumeration fe = nodeFigure.figures(); fe.hasMoreElements();)
					{
						Figure f = fe.nextFigure();
						if (f instanceof SocketFigure)
						{
							SocketFigure socketFigure = (SocketFigure) f;

							boolean affected = false;

							if (socketFigure == startConnector.owner())
							{
								// This is the start socket of the new connection; always visible
								affected = true;
							}

							if (socketFigure == connectionFigure.startFigure() || socketFigure == connectionFigure.endFigure())
							{
								// This is the end socket of the new connection; always visible
								affected = true;
							}

							if (canLinkFigures(startSocketFigure, socketFigure))
							{
								// We can link to this socket, so it's affected.
								affected = true;
							}

							if (affected)
							{
								// Notify possibly affected sockets of the DnD operation
								String eventType = on ? VisualElementEvent.SET_DND_PARTICIPANT : VisualElementEvent.UNSET_DND_PARTICIPANT;
								socketFigure.handleEvent(new VisualElementEvent(eventType, getEditor()));
							}
						}
					}
				}
			}
		}

		if (on)
		{
			// Display all figures we can connect to in green
			DecorationMgr.addDecorator(getEditor(), BasicFigure.DECO_OVERLAY, canConnectDecorator);
		}
		else
		{
			// Display all figures we can connect to the regular way again
			DecorationMgr.removeDecorator(getEditor(), BasicFigure.DECO_OVERLAY, canConnectDecorator);
		}

		getView().redraw();
	}

	//////////////////////////////////////////////////
	// @@ Decorator
	//////////////////////////////////////////////////

	/**
	 * Decorator that paints figures we can connect to in green.
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

			if (connectionFigure instanceof FlowConnection && !(owner instanceof SocketFigure))
			{
				return false;
			}

			return canLinkFigures(startConnector.owner(), (Figure) owner);
		}
	}
}
