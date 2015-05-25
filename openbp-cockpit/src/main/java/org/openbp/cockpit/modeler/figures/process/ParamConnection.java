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
import java.awt.Stroke;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.openbp.cockpit.modeler.ViewModeMgr;
import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.figures.generic.CircleConstants;
import org.openbp.cockpit.modeler.figures.generic.GeometryUtil;
import org.openbp.cockpit.modeler.figures.generic.Orientation;
import org.openbp.cockpit.modeler.figures.generic.XArrowTip;
import org.openbp.cockpit.modeler.figures.spline.PolySplineConnection;
import org.openbp.cockpit.modeler.figures.tag.TagConnector;
import org.openbp.cockpit.modeler.skins.LinkDescriptor;
import org.openbp.common.CollectionUtil;
import org.openbp.common.util.ToStringHelper;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.process.ControlLink;
import org.openbp.core.model.item.process.DataLink;
import org.openbp.core.model.item.process.DataLinkImpl;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.Param;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessObject;

import CH.ifa.draw.figures.LineDecoration;
import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Figure;

/**
 * Spline figure representing a data link.
 *
 * @author Stephan Moritz
 */
public class ParamConnection extends PolySplineConnection
	implements ProcessElementContainer
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** The data link this connection represents */
	private DataLink dataLink;

	/** The process this link belongs to */
	private ProcessItem process;

	/** Color for data links with path specification */
	private Color memberPathColor = Color.BLACK;

	/** Decoration for the end of the connection (arrow) */
	private static final LineDecoration endDecoration = new XArrowTip(0.4, 12, 9);

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param dataLink Link represented by this figure
	 * @param startConnector Start connector the connection should attach to
	 * @param endConnector End connector the connection should attach to
	 * @param drawing Process drawing that owns the figure
	 */
	public ParamConnection(DataLink dataLink, Connector startConnector, Connector endConnector, ProcessDrawing drawing)
	{
		super(drawing);

		// Connection between the data link and the spline
		this.dataLink = dataLink;
		dataLink.setRepresentation(this);

		process = dataLink.getProcess();

		// The label serves as name holder of the data link name
		getLabel().setClient(dataLink);

		// The decodeGeometry method may need the connectors in order to set the connector orientation, so provide it up front
		setStartConnector(startConnector);
		setEndConnector(endConnector);

		decodeGeometry();

		initializeFigureAttributes();

		// Now do the actual connection after the geometry data has been decoded
		connectStart(startConnector);
		connectEnd(endConnector);
	}

	/**
	 * Constructor for a virgin figure.
	 *
	 * @param drawing Process drawing that owns the figure
	 */
	public ParamConnection(ProcessDrawing drawing)
	{
		super(drawing);

		this.process = drawing.getProcess();

		// We retrieve a new data link from the process
		dataLink = process.createDataLink();
		dataLink.setRepresentation(this);
		getLabel().setClient(dataLink);

		drawDecorations = false;

		initializeFigureAttributes();
	}

	/**
	 * Initializes the figure's drawing attributes.
	 */
	protected void initializeFigureAttributes()
	{
		setEndDecoration(endDecoration);

		LinkDescriptor desc = getDrawing().getProcessSkin().getLinkDescriptor(FigureTypes.LINKTYPE_DATA);
		if (desc != null)
		{
			setStroke(desc.getStroke());
			setFrameColor(desc.getColor());
			memberPathColor = desc.getColor2();
		}
	}

	/**
	 * Returns the data link associated with this parameter connection.
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
		return ToStringHelper.toString(this, "dataLink");
	}

	//////////////////////////////////////////////////
	// @@ PolySplineFigure overrides
	//////////////////////////////////////////////////

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#draw(Graphics g)
	 */
	public void draw(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(getStroke());

		Color oldColor = g2.getColor();

		Color c;
		if (dataLink.getSourceMemberPath() != null || dataLink.getTargetMemberPath() != null)
		{
			c = memberPathColor;
		}
		else
		{
			c = getFrameColor();
		}
		g2.setColor(c);

		drawSpline(g2);

		if (drawDecorations)
		{
			g2.setColor(c);
			drawDecorations(g);
		}

		g2.setColor(oldColor);
		g2.setStroke(oldStroke);
	}

	//////////////////////////////////////////////////
	// @@ PolySplineConnection overrides
	//////////////////////////////////////////////////

	/**
	 * Delegates the check to the underlying control link.
	 * (true if data types match and one is an entry, the other one an exit parameter)
	 * @see org.openbp.cockpit.modeler.figures.spline.PolySplineConnection#canConnectFigures(Figure start, Figure end, int flags)
	 */
	public boolean canConnectFigures(Figure startFigure, Figure endFigure, int flags)
	{
		if (!((startFigure instanceof ParamFigure) && (endFigure instanceof ParamFigure)))
			return false;

		SocketFigure startSocketFigure = (SocketFigure) ((ParamFigure) startFigure).getParent();
		SocketFigure endSocketFigure = (SocketFigure) ((ParamFigure) endFigure).getParent();

		if (startSocketFigure.isEntrySocket() == endSocketFigure.isEntrySocket())
		{
			// Never can connect an input param to another input param or output params accordingly
			return false;
		}

		// Check if there is also a control link going from the source socket to the target socket
		boolean haveControlLink = false;
		NodeSocket startSocket = startSocketFigure.getNodeSocket();
		NodeSocket endSocket = endSocketFigure.getNodeSocket();
		for (Iterator it = startSocket.getControlLinks(); it.hasNext();)
		{
			ControlLink link = (ControlLink) it.next();
			if (link.getTargetSocket() == endSocket)
			{
				haveControlLink = true;
				break;
			}
		}
		for (Iterator it = endSocket.getControlLinks(); it.hasNext();)
		{
			ControlLink link = (ControlLink) it.next();
			if (link.getTargetSocket() == startSocket)
			{
				haveControlLink = true;
				break;
			}
		}
		if (!haveControlLink)
			return false;

		return (DataLinkImpl.canLink(((ParamFigure) startFigure).getNodeParam(), dataLink.getSourceMemberPath(), (((ParamFigure) endFigure).getNodeParam()), dataLink.getTargetMemberPath(), flags) != DataLink.CANNOT_LINK);
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.spline.PolySplineConnection#shouldReverse(Figure startFigure, Figure endFigure)
	 */
	protected boolean shouldReverse(Figure startFigure, Figure endFigure)
	{
		SocketFigure socketFigure = (SocketFigure) ((ParamFigure) startFigure()).getParent();
		return socketFigure.isEntrySocket();
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.spline.PolySplineConnection#handleConnect(Figure startFigure, Figure endFigure)
	 */
	protected void handleConnect(Figure startFigure, Figure endFigure)
	{
		ParamFigure startParamFigure = (ParamFigure) startFigure;
		ParamFigure endParamFigure = (ParamFigure) endFigure;

		Param startParam = startParamFigure.getNodeParam();
		Param endParam = endParamFigure.getNodeParam();

		// Link the parameters
		dataLink.link(startParam, endParam);

		startParamFigure.addParamConnection(this);
		endParamFigure.addParamConnection(this);

		// Add the link to the process if it has not already been added
		if (CollectionUtil.containsReference(process.getDataLinkList(), dataLink))
		{
			// Already present
			return;
		}

		// Add it to the process
		process.addDataLink(dataLink);
		dataLink.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.SYNC_LOCAL_REFNAMES);
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.spline.PolySplineConnection#handleDisconnect(Figure startFigure, Figure endFigure)
	 */
	protected void handleDisconnect(Figure startFigure, Figure endFigure)
	{
		process.removeDataLink(dataLink);

		ParamFigure startParamFigure = (ParamFigure) startFigure;
		ParamFigure endParamFigure = (ParamFigure) endFigure;

		if (startParamFigure != null)
			startParamFigure.removeParamConnection(this);
		if (endParamFigure != null)
			endParamFigure.removeParamConnection(this);
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.spline.PolySplineConnection#connectsSame(ConnectionFigure other)
	 */
	public boolean connectsSame(ConnectionFigure other)
	{
		if (!(other instanceof ParamConnection))
		{
			return false;
		}

		DataLink otherLink = ((ParamConnection) other).getDataLink();

		return dataLink.getSourceParamName().equals(otherLink.getSourceParamName()) && dataLink.getTargetParamName().equals(otherLink.getTargetParamName());
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Change listeners
	/////////////////////////////////////////////////////////////////////////

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
		return !ViewModeMgr.getInstance().isDataLinkVisible(this);
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

		process.removeDataLink(dataLink);
	}

	//////////////////////////////////////////////////
	// @@ Connection orientation
	//////////////////////////////////////////////////

	/**
	 * Checks if the direction is locked.
	 * @nowarn
	 */
	public boolean isOrientationLocked()
	{
		TagConnector startConnector = (TagConnector) getStartConnector();
		TagConnector endConnector = (TagConnector) getEndConnector();

		return startConnector.isOrientationLocked() || endConnector.isOrientationLocked();
	}

	/**
	 * Toggles the orientation lock.
	 */
	public void toggleOrientationLock()
	{
		TagConnector startConnector = (TagConnector) getStartConnector();
		TagConnector endConnector = (TagConnector) getEndConnector();

		boolean startLocked = startConnector.isOrientationLocked();
		boolean endLocked = endConnector.isOrientationLocked();

		if (startLocked && endLocked)
		{
			// Unlock the orientation of the associated connectors
			if (startLocked)
			{
				startConnector.setLockedOrientation(Orientation.UNDETERMINED);
			}
			if (endLocked)
			{
				endConnector.setLockedOrientation(Orientation.UNDETERMINED);
			}
		}
		else
		{
			// Lock the orientation of the associated connectors
			if (!startLocked)
			{
				startConnector.toggleOrientationLock();
			}
			if (!endLocked)
			{
				endConnector.toggleOrientationLock();
			}
		}
	}

	/**
	 * Flips (toggles) the locked orientation of the associated parameter figures.
	 */
	public void flipOrientation()
	{
		TagConnector startConnector = (TagConnector) getStartConnector();
		TagConnector endConnector = (TagConnector) getEndConnector();

		// First, lock the orientation of the associated figures
		if (!startConnector.isOrientationLocked())
		{
			startConnector.toggleOrientationLock();
		}
		if (!endConnector.isOrientationLocked())
		{
			endConnector.toggleOrientationLock();
		}

		Orientation startOrientation = startConnector.getLockedOrientation();
		Orientation endOrientation = endConnector.getLockedOrientation();

		// Order of orientations to toggle:
		// ^ ^
		// v ^
		// ^ v
		// v v

		if ((startOrientation == Orientation.TOP || startOrientation == Orientation.LEFT) && (endOrientation == Orientation.TOP || endOrientation == Orientation.LEFT))
		{
			startConnector.flipOrientation();
		}
		else if ((startOrientation == Orientation.BOTTOM || startOrientation == Orientation.RIGHT) && (endOrientation == Orientation.TOP || endOrientation == Orientation.LEFT))
		{
			startConnector.flipOrientation();
			endConnector.flipOrientation();
		}
		else if ((startOrientation == Orientation.TOP || startOrientation == Orientation.LEFT) && (endOrientation == Orientation.BOTTOM || endOrientation == Orientation.RIGHT))
		{
			startConnector.flipOrientation();
		}
		else if ((startOrientation == Orientation.BOTTOM || startOrientation == Orientation.RIGHT) && (endOrientation == Orientation.BOTTOM || endOrientation == Orientation.RIGHT))
		{
			startConnector.flipOrientation();
			endConnector.flipOrientation();
		}
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.spline.PolySplineConnection#layoutAndAdjustConnection()
	 */
	public void layoutAndAdjustConnection()
	{
		TagConnector startConnector = (TagConnector) getStartConnector();
		TagConnector endConnector = (TagConnector) getEndConnector();

		Orientation startOrientation = startConnector.getOrientation();
		Orientation endOrientation = endConnector.getOrientation();

		SocketFigure startSocketFigure = (SocketFigure) ((ParamFigure) startFigure()).getParent();
		SocketFigure endSocketFigure = (SocketFigure) ((ParamFigure) endFigure()).getParent();
		Figure startNodeFigure = startSocketFigure.getParent();
		Figure endNodeFigure = endSocketFigure.getParent();

		Point startCenter = startSocketFigure.center();
		Point endCenter = endSocketFigure.center();
		Orientation startSocketOrientation = CircleConstants.determineOrientation(startSocketFigure.getAngle(), startNodeFigure.displayBox());
		Orientation endSocketOrientation = CircleConstants.determineOrientation(endSocketFigure.getAngle(), endNodeFigure.displayBox());
		boolean startSocketIsVertical = startSocketOrientation == Orientation.TOP || startSocketOrientation == Orientation.BOTTOM;
		boolean endSocketIsVertical = endSocketOrientation == Orientation.TOP || endSocketOrientation == Orientation.BOTTOM;
		boolean socketsInSync = startSocketIsVertical == endSocketIsVertical;

		// TODO Fix 4: Param autoconnector orientation doesn't work optimal, test...
		if (socketsInSync)
		{
			endOrientation = startOrientation;
		}
		else
		{
			if (startSocketOrientation == Orientation.LEFT || startSocketOrientation == Orientation.RIGHT)
			{
				if (startCenter.y <= endCenter.y)
					startOrientation = Orientation.BOTTOM;
				else
					startOrientation = Orientation.TOP;
			}

			if (startSocketOrientation == Orientation.TOP || startSocketOrientation == Orientation.BOTTOM)
			{
				if (startCenter.x <= endCenter.x)
					startOrientation = Orientation.RIGHT;
				else
					startOrientation = Orientation.LEFT;
			}

			if (endSocketOrientation == Orientation.LEFT || endSocketOrientation == Orientation.RIGHT)
			{
				if (startCenter.y <= endCenter.y)
					endOrientation = Orientation.TOP;
				else
					endOrientation = Orientation.BOTTOM;
			}

			if (endSocketOrientation == Orientation.TOP || endSocketOrientation == Orientation.BOTTOM)
			{
				if (startCenter.x <= endCenter.x)
					endOrientation = Orientation.LEFT;
				else
					endOrientation = Orientation.RIGHT;
			}

			/*
			if (verticalRelationship)
			{
				// Vertical line, connectors can be LEFT and RIGHT for param connections
				if (startCenter.x < endCenter.x)
				{
					startOrientation = Orientation.RIGHT;
					endOrientation = Orientation.LEFT;
				}
				else
				{
					startOrientation = Orientation.LEFT;
					endOrientation = Orientation.RIGHT;
				}
			}
			else
			{
				// Horizontal line, connectors can be TOP and BOTTOM for param connections
				if (startCenter.y < endCenter.y)
				{
					startOrientation = Orientation.BOTTOM;
					endOrientation = Orientation.TOP;
				}
				else
				{
					startOrientation = Orientation.TOP;
					endOrientation = Orientation.BOTTOM;
				}
				startConnector.setLockedOrientation(startOrientation);
				endConnector.setLockedOrientation(endOrientation);
			}
			 */
		}

		startConnector.setLockedOrientation(startOrientation);
		endConnector.setLockedOrientation(endOrientation);

		// Perform a simple connection layout
		layoutConnection();
	}

	//////////////////////////////////////////////////
	// @@ Geometry serialization support
	//////////////////////////////////////////////////

	public void decodeGeometry()
	{
		decode(dataLink.getGeometry());
	}

	public void encodeGeometry()
	{
		dataLink.setGeometry(encode());
	}

	protected void decodeParameter(String parameter)
	{
		super.decodeParameter(parameter);

		String errIdent = dataLink.getQualifier().toUntypedString();
		StringTokenizer st = new StringTokenizer(parameter, ":");

		try
		{
			String ident = st.nextToken();

			if (ident.equals("orientation"))
			{
				Orientation startOrientation = Orientation.fromInt(GeometryUtil.parseInt(st, ident, errIdent));
				Orientation endOrientation = Orientation.fromInt(GeometryUtil.parseInt(st, ident, errIdent));
				((TagConnector) getStartConnector()).setLockedOrientation(startOrientation);
				((TagConnector) getEndConnector()).setLockedOrientation(endOrientation);
			}
		}
		catch (Exception e)
		{
		}
	}

	protected String encode()
	{
		String result = super.encode();

		TagConnector startConnector = (TagConnector) getStartConnector();
		TagConnector endConnector = (TagConnector) getEndConnector();
		if ((startConnector != null && startConnector.getLockedOrientation() != Orientation.UNDETERMINED) && (endConnector != null && endConnector.getLockedOrientation() != Orientation.UNDETERMINED))
		{
			result = result + "|orientation:" + startConnector.getLockedOrientation() + ":" + endConnector.getLockedOrientation();
		}

		return result;
	}

	//////////////////////////////////////////////////
	// @@ ProcessElementContainer implementation
	//////////////////////////////////////////////////

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
	 * Start socket should be selected on deletion.
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#selectionOnDelete()
	 */
	public Figure selectionOnDelete()
	{
		return startFigure();
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
