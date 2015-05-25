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
import java.awt.geom.Point2D;
import java.util.List;

import org.openbp.cockpit.modeler.ViewModeMgr;
import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.figures.VisualElementEvent;
import org.openbp.cockpit.modeler.figures.generic.XFigure;
import org.openbp.cockpit.modeler.figures.generic.XFigureDescriptor;
import org.openbp.cockpit.modeler.figures.spline.PolySplineConnection;
import org.openbp.cockpit.modeler.figures.tag.AbstractTagFigure;
import org.openbp.cockpit.modeler.figures.tag.TagConnector;
import org.openbp.cockpit.modeler.skins.LinkDescriptor;
import org.openbp.common.CommonUtil;
import org.openbp.common.util.ToStringHelper;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.process.ControlLink;
import org.openbp.core.model.item.process.ControlLinkImpl;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessObject;

import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Figure;

/**
 * Spline figure representing a control link.
 *
 * @author Stephan Moritz
 */
public class FlowConnection extends PolySplineConnection
	implements ProcessElementContainer
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** The controlLink that this Figure represents */
	private ControlLink controlLink;

	/** The process this link belongs to */
	private ProcessItem process;

	/** Figure for control links with begin transaction control */
	private XFigure beginFigure;

	/** Figure for control links with commit transaction control */
	private XFigure commitFigure;

	/** Figure for control links with commit and begin transaction control */
	private XFigure commitBeginFigure;

	/** Figure for control links with rollback transaction control */
	private XFigure rollbackFigure;

	/** Figure for control links with rollback and begin transaction control */
	private XFigure rollbackBeginFigure;

	/** Color for control links with commit transaction control */
	private Color commitColor = Color.BLACK;

	/** Color for control links with rollback transaction control */
	private Color rollbackColor = Color.BLACK;

	/** Color for control links with default transaction control */
	private Color defaultColor = Color.BLACK;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param controlLink Control represented by this figure
	 * @param drawing Process drawing that owns the figure
	 */
	public FlowConnection(ControlLink controlLink, ProcessDrawing drawing)
	{
		super(drawing);

		this.controlLink = controlLink;
		controlLink.setRepresentation(this);

		process = controlLink.getProcess();

		// The label serves as name holder of the control link name
		getLabel().setClient(controlLink);

		decodeGeometry();

		initializeFigureAttributes();
	}

	/**
	 * Constructor for a virgin figure.
	 *
	 * @param drawing Process drawing that owns the figure
	 */
	public FlowConnection(ProcessDrawing drawing)
	{
		super(drawing);

		this.process = drawing.getProcess();

		// We retrieve a new control link from the process
		controlLink = process.createControlLink();
		controlLink.setRepresentation(this);
		getLabel().setClient(controlLink);

		initializeFigureAttributes();
	}

	/**
	 * Initializes the figure's drawing attributes.
	 */
	protected void initializeFigureAttributes()
	{
		LinkDescriptor linkDesc = getDrawing().getProcessSkin().getLinkDescriptor(FigureTypes.LINKTYPE_CONTROL);
		if (linkDesc != null)
		{
			setStroke(linkDesc.getStroke());
			setFrameColor(linkDesc.getColor());
			commitColor = linkDesc.getColor2();
			rollbackColor = linkDesc.getColor3();
			defaultColor = linkDesc.getColor4();
		}

		// TODO Refactor 6 This should not be created for each figure, should do to create it once and synchronize access to it.
		XFigureDescriptor figureDesc;

		figureDesc = getDrawing().getProcessSkin().getSymbolDescriptor(FigureTypes.SYMBOLTYPE_BEGIN);
		if (figureDesc != null)
		{
			beginFigure = figureDesc.createFigure();
		}

		figureDesc = getDrawing().getProcessSkin().getSymbolDescriptor(FigureTypes.SYMBOLTYPE_COMMIT);
		if (figureDesc != null)
		{
			commitFigure = figureDesc.createFigure();
		}

		figureDesc = getDrawing().getProcessSkin().getSymbolDescriptor(FigureTypes.SYMBOLTYPE_COMMIT_BEGIN);
		if (figureDesc != null)
		{
			commitBeginFigure = figureDesc.createFigure();
		}

		figureDesc = getDrawing().getProcessSkin().getSymbolDescriptor(FigureTypes.SYMBOLTYPE_ROLLBACK);
		if (figureDesc != null)
		{
			rollbackFigure = figureDesc.createFigure();
		}

		figureDesc = getDrawing().getProcessSkin().getSymbolDescriptor(FigureTypes.SYMBOLTYPE_ROLLBACK_BEGIN);
		if (figureDesc != null)
		{
			rollbackBeginFigure = figureDesc.createFigure();
		}
	}

	/**
	 * Returns the control link associated with this flow connection.
	 * @nowarn
	 */
	public ControlLink getControlLink()
	{
		return controlLink;
	}

	/**
	 * Returns a string representation of this object.
	 * @nowarn
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "controlLink");
	}

	//////////////////////////////////////////////////
	// @@ PolySplineFigure overrides
	//////////////////////////////////////////////////

	public Rectangle displayBox()
	{
		Rectangle ret = super.displayBox();

		XFigure transactionFigure = null;
		switch (controlLink.getTransactionControl())
		{
			case ControlLink.TA_BEGIN:
				transactionFigure = beginFigure;
				break;

			case ControlLink.TA_COMMIT:
				transactionFigure = commitFigure;
				break;

			case ControlLink.TA_COMMIT_BEGIN:
				transactionFigure = commitBeginFigure;
				break;

			case ControlLink.TA_ROLLBACK:
				transactionFigure = rollbackFigure;
				break;

			case ControlLink.TA_ROLLBACK_BEGIN:
				transactionFigure = rollbackBeginFigure;
				break;
		}
		if (transactionFigure != null)
		{
			Rectangle taDb = transactionFigure.displayBox();
			ret.add(taDb.width, taDb.height);
		}

		return ret.union(label.displayBox());
	}
		
	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#draw(Graphics g)
	 */
	public void draw(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(getStroke());

		Color oldColor = g2.getColor();

		Color c = null;
		XFigure transactionFigure = null;
		switch (controlLink.getTransactionControl())
		{
			case ControlLink.TA_BEGIN:
				transactionFigure = beginFigure;
				break;

			case ControlLink.TA_COMMIT:
				c = commitColor;
				transactionFigure = commitFigure;
				break;

			case ControlLink.TA_COMMIT_BEGIN:
				c = commitColor;
				transactionFigure = commitBeginFigure;
				break;

			case ControlLink.TA_ROLLBACK:
				c = rollbackColor;
				transactionFigure = rollbackFigure;
				break;

			case ControlLink.TA_ROLLBACK_BEGIN:
				c = rollbackColor;
				transactionFigure = rollbackBeginFigure;
				break;
		}
		if (c == null)
		{
			c = defaultColor;
		}
		g2.setColor(c);

		drawSpline(g2);

		if (transactionFigure != null)
		{
			setTransactionFigurePosition(transactionFigure);
			g2.setColor(c);
			transactionFigure.draw(g2);
		}

		if (drawDecorations)
		{
			g2.setColor(c);
			drawDecorations(g);
		}

		g2.setColor(oldColor);
		g2.setStroke(oldStroke);
	}

	private void setTransactionFigurePosition(XFigure transactionFigure)
	{
		Rectangle tafDb = transactionFigure.displayBox();

		Point2D p = getPointOnCurve(0.5d);
		Point p1 = new Point(CommonUtil.rnd(p.getX() - tafDb.width / 2), CommonUtil.rnd(p.getY() - tafDb.height / 2));
		Point p2 = new Point(CommonUtil.rnd(p.getX() + tafDb.width - tafDb.width / 2), CommonUtil.rnd(p.getY() + tafDb.height - tafDb.height / 2));

		transactionFigure.changed();
		transactionFigure.basicDisplayBox(p1, p2);
		transactionFigure.changed();
	}

	//////////////////////////////////////////////////
	// @@ PolySplineConnection overrides
	//////////////////////////////////////////////////

	/**
	 * Delegates the check to the underlying control link.
	 * (true if one is an entry, the other one an exit socket)
	 * @see org.openbp.cockpit.modeler.figures.spline.PolySplineConnection#canConnectFigures(Figure start, Figure end, int flags)
	 */
	public boolean canConnectFigures(Figure start, Figure end, int flags)
	{
		// Get the socket figures
		while (start instanceof ParamFigure)
		{
			start = ((ParamFigure) start).getParent();
		}

		while (end instanceof ParamFigure)
		{
			end = ((ParamFigure) end).getParent();
		}

		if (!((start instanceof SocketFigure) && (end instanceof SocketFigure)))
			return false;

		return ControlLinkImpl.canLink(((SocketFigure) start).getNodeSocket(), ((SocketFigure) end).getNodeSocket()) != ControlLink.CANNOT_LINK;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.spline.PolySplineConnection#shouldReverse(Figure start, Figure end)
	 */
	protected boolean shouldReverse(Figure start, Figure end)
	{
		SocketFigure socketFigure = (SocketFigure) startFigure();
		return socketFigure.isEntrySocket();
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.spline.PolySplineConnection#handleConnect(Figure start, Figure end)
	 */
	protected void handleConnect(Figure start, Figure end)
	{
		SocketFigure startFigure = (SocketFigure) start;
		SocketFigure endFigure = (SocketFigure) end;

		controlLink.link(startFigure.getNodeSocket(), endFigure.getNodeSocket());

		startFigure.addFlowConnection(this);
		endFigure.addFlowConnection(this);

		// Add the link to the process if it has not already been added
		List links = process.getControlLinkList();
		if (links != null)
		{
			int n = links.size();
			for (int i = 0; i < n; ++i)
			{
				Object link = links.get(i);
				if (link == controlLink)
				{
					// Already present
					return;
				}
			}
		}

		// Add it to the process
		process.addControlLink(controlLink);
		controlLink.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.SYNC_LOCAL_REFNAMES);
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.spline.PolySplineConnection#handleDisconnect(Figure start, Figure end)
	 */
	protected void handleDisconnect(Figure start, Figure end)
	{
		process.removeControlLink(controlLink);

		SocketFigure startFigure = (SocketFigure) start;
		SocketFigure endFigure = (SocketFigure) end;

		if (startFigure != null)
			startFigure.removeFlowConnection(this);
		if (endFigure != null)
			endFigure.removeFlowConnection(this);
	}

	/**
	 * If the given connector belongs to a param figure, we search for the corresponding socket figure.
	 * @see org.openbp.cockpit.modeler.figures.spline.PolySplineConnection#connectStart(Connector newStartConnector)
	 */
	public void connectStart(Connector newStartConnector)
	{
		while (newStartConnector instanceof ParamConnector)
		{
			newStartConnector = ((AbstractTagFigure) newStartConnector.owner()).getParent().connectorAt(0, 0);
		}

		super.connectStart(newStartConnector);
	}

	/**
	 * If the given connector belongs to a param figure, we search for the corresponding socket figure.
	 * @see org.openbp.cockpit.modeler.figures.spline.PolySplineConnection#connectEnd(Connector newEndConnector)
	 */
	public void connectEnd(Connector newEndConnector)
	{
		while (newEndConnector instanceof ParamConnector)
		{
			newEndConnector = ((AbstractTagFigure) newEndConnector.owner()).getParent().connectorAt(0, 0);
		}
		super.connectEnd(newEndConnector);
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.spline.PolySplineConnection#connectsSame(ConnectionFigure other)
	 */
	public boolean connectsSame(ConnectionFigure other)
	{
		if (!(other instanceof FlowConnection))
		{
			return false;
		}

		ControlLink otherLink = ((FlowConnection) other).getControlLink();

		return controlLink.getSourceSocketName().equals(otherLink.getSourceSocketName()) && controlLink.getTargetSocketName().equals(otherLink.getTargetSocketName());
	}

	//////////////////////////////////////////////////
	// @@ Change listeners
	//////////////////////////////////////////////////

	/**
	 * Adds ourself as figure change listener to the start socket we are connected to.
	 * @see org.openbp.cockpit.modeler.figures.spline.PolySplineConnection#addStartConnectorChangeListener()
	 */
	protected void addStartConnectorChangeListener()
	{
		if (getStartConnector() != null)
		{
			((TagConnector) getStartConnector()).getSocketFigure().addFigureChangeListener(this);
		}
	}

	/**
	 * Removes ourself as figure change listener from the start socket we are connected to.
	 * @see org.openbp.cockpit.modeler.figures.spline.PolySplineConnection#removeStartConnectorChangeListener()
	 */
	protected void removeStartConnectorChangeListener()
	{
		if (getStartConnector() != null)
		{
			((TagConnector) getStartConnector()).getSocketFigure().removeFigureChangeListener(this);
		}
	}

	/**
	 * Adds ourself as figure change listener to the end socket we are connected to.
	 * @see org.openbp.cockpit.modeler.figures.spline.PolySplineConnection#addEndConnectorChangeListener()
	 */
	protected void addEndConnectorChangeListener()
	{
		if (getEndConnector() != null)
		{
			((TagConnector) getEndConnector()).getSocketFigure().addFigureChangeListener(this);
		}
	}

	/**
	 * Removes ourself as figure change listener from the end socket we are connected to.
	 * @see org.openbp.cockpit.modeler.figures.spline.PolySplineConnection#removeEndConnectorChangeListener()
	 */
	protected void removeEndConnectorChangeListener()
	{
		if (getEndConnector() != null)
		{
			((TagConnector) getEndConnector()).getSocketFigure().removeFigureChangeListener(this);
		}
	}

	/**
	 * Checks if the connection is minimized.
	 * @nowarn
	 */
	public boolean isMinimized()
	{
		return !ViewModeMgr.getInstance().isControlLinkVisible(this);
	}

	//////////////////////////////////////////////////
	// @@ Figure overrides
	//////////////////////////////////////////////////

	/**
	 * @see CH.ifa.draw.framework.Figure#release()
	 */
	public void release()
	{
		super.release();

		process.removeControlLink(controlLink);
	}

	//////////////////////////////////////////////////
	// @@ Geometry serialization support
	//////////////////////////////////////////////////

	public void decodeGeometry()
	{
		decode(controlLink.getGeometry());
	}

	public void encodeGeometry()
	{
		controlLink.setGeometry(encode());
	}

	//////////////////////////////////////////////////
	// @@ VisualElement overrides
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#handleEvent(VisualElementEvent event)
	 */
	public boolean handleEvent(VisualElementEvent event)
	{
		boolean ret = false;

		if (event.type == VisualElementEvent.UPDATE_STATE)
		{
			// Element was selected or deselected.
			// If control anchors are to be hidden, we need to rebuild the content of the connected sockets.
			if (!ViewModeMgr.getInstance().isControlAnchorVisible(this))
			{
				if (getStartConnector() != null)
				{
					((TagConnector) getStartConnector()).getSocketFigure().handleEvent(event);
				}
				if (getEndConnector() != null)
				{
					((TagConnector) getEndConnector()).getSocketFigure().handleEvent(event);
				}
			}
			ret = true;
		}

		if (super.handleEvent(event))
			ret = true;

		return ret;
	}

	//////////////////////////////////////////////////
	// @@ ProcessElementContainer implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#getProcessElement()
	 */
	public ProcessObject getProcessElement()
	{
		return controlLink;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#getReferredProcessElement()
	 */
	public ProcessObject getReferredProcessElement()
	{
		return getProcessElement();
	}

	/**
	 * Start socket should be selected on deletion.
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#selectionOnDelete()
	 */
	public Figure selectionOnDelete()
	{
		return (SocketFigure) startFigure();
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
		return containsPoint(x, y) ? this : null;
	}
}
