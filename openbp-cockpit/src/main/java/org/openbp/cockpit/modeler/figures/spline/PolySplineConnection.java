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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.openbp.cockpit.modeler.Modeler;
import org.openbp.cockpit.modeler.ModelerColors;
import org.openbp.cockpit.modeler.ModelerGraphics;
import org.openbp.cockpit.modeler.drawing.DrawingEditorPlugin;
import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.figures.VisualElement;
import org.openbp.cockpit.modeler.figures.VisualElementEvent;
import org.openbp.cockpit.modeler.figures.generic.MoveableTitleFigure;
import org.openbp.cockpit.modeler.figures.generic.Orientation;
import org.openbp.cockpit.modeler.figures.generic.UpdatableFigure;
import org.openbp.cockpit.modeler.figures.generic.XArrowTip;
import org.openbp.cockpit.modeler.figures.tag.TagConnector;
import org.openbp.cockpit.modeler.undo.ModelerUndoable;
import org.openbp.cockpit.modeler.util.FigureUtil;
import org.openbp.common.CommonUtil;
import org.openbp.jaspira.decoration.DecorationMgr;

import CH.ifa.draw.figures.LineDecoration;
import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.Locator;
import CH.ifa.draw.standard.AbstractHandle;
import CH.ifa.draw.standard.AbstractLocator;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

/**
 * A spline connection is a standard implementation of the connection figure interface.
 *
 * @author Stephan Moritz
 */
public abstract class PolySplineConnection extends PolySplineFigure
	implements ConnectionFigure, VisualElement, UpdatableFigure
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	public static final Double NAN = new Double(Double.NaN);

	/** Decoration key for the spline animation (used with Double objects) */
	public static final String DECO_ANIMATION = "Line.Animation";

	/** Decoration for the end of the connection (arrow) */
	private static final LineDecoration endDecoration = new XArrowTip(0.4, 15, 10);

	/** Decoration for the end of the animation (arrow) */
	private static final LineDecoration animationDecoration = new XArrowTip(0.4, 10, -1);

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Start connector */
	private Connector startConnector;

	/** End connector */
	private Connector endConnector;

	/** Position of the label on the spline */
	private SplineLocator textLocator;

	/** Label of this connection */
	protected MoveableTitleFigure label;

	/** Process drawing we belong to */
	private ProcessDrawing drawing;

	/** Factor used for the postion of the controlpoint of the startpoint */
	private double startFactor = 0.3d;

	/** Factor used for the postion of the controlpoint of the endpoint */
	private double endFactor = 0.3d;

	/** Visual status as defined by VisualElement */
	private int visualStatus = VISUAL_VISIBLE;

	//////////////////////////////////////////////////
	// @@ Constructor
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param drawing Process drawing that owns the figure
	 */
	public PolySplineConnection(ProcessDrawing drawing)
	{
		this();
		setDrawing(drawing);
	}

	/**
	 * Constructor.
	 */
	public PolySplineConnection()
	{
		super();

		// Set the decorators
		// (start: none, end: arrow, animation: point)
		setEndDecoration(endDecoration);
		setAnimationDecoration(animationDecoration);

		textLocator = new SplineLocator();

		label = new MoveableTitleFigure();
		label.connect(this);
	}

	//////////////////////////////////////////////////
	// @@ ConnectionFigure implementation
	//////////////////////////////////////////////////

	/**
	 * Tests whether two figures can be connected.
	 *
	 * @param startFigure Proposed start figure
	 * @param endFigure Proposed end figure
	 * @param flags App-specific flags that may define constraints for the connection check
	 */
	public abstract boolean canConnectFigures(Figure startFigure, Figure endFigure, int flags);

	/**
	 * Tests whether two figures can be connected.
	 *
	 * @param startFigure Proposed start figure
	 * @param endFigure Proposed end figure
	 */
	public boolean canConnect(Figure startFigure, Figure endFigure)
	{
		return canConnectFigures(startFigure, endFigure, 0);
	}

	/**
	 * Calls {@link #handleDisconnect} if a connection can be established.
	 * @see CH.ifa.draw.framework.ConnectionFigure#connectStart(Connector startConnector)
	 */
	public void connectStart(Connector startConnector)
	{
		this.startConnector = startConnector;

		// Add ourself as figure change listener to the figure we are connected to
		addStartConnectorChangeListener();

		// check if this results in a finished connection
		if (endConnector != null)
		{
			handleConnect(startFigure(), endFigure());
		}
	}

	/**
	 * Calls {@link #handleConnect}.
	 * @see CH.ifa.draw.framework.ConnectionFigure#connectEnd(Connector endConnector)
	 */
	public void connectEnd(Connector endConnector)
	{
		this.endConnector = endConnector;

		// Add ourself as figure change listener to the figure we are connected to
		addEndConnectorChangeListener();

		// If necessary, reverse the connection
		if (shouldReverse(startFigure(), endFigure()))
		{
			Connector swap = startConnector;
			startConnector = this.endConnector;
			this.endConnector = swap;
		}
		layoutConnection();

		handleConnect(startFigure(), endFigure());
	}

	/**
	 * Disconnects the start figure.
	 * Calls {@link #handleDisconnect}.
	 * @see CH.ifa.draw.framework.ConnectionFigure#disconnectStart()
	 */
	public void disconnectStart()
	{
		handleDisconnect(startFigure(), endFigure());

		// Remove ourself as figure change listener from the figure we are connected to
		removeStartConnectorChangeListener();

		startConnector = null;
	}

	/**
	 * Disconnects the end figure.
	 * Calls {@link #handleDisconnect}.
	 * @see CH.ifa.draw.framework.ConnectionFigure#disconnectEnd()
	 */
	public void disconnectEnd()
	{
		handleDisconnect(startFigure(), endFigure());

		// Remove ourself as figure change listener from the socket we are connected to
		removeEndConnectorChangeListener();

		endConnector = null;
	}

	/**
	 * Tests whether a connection connects the same figures as another connection figure.
	 * @param other Connection to check on equality with this figure
	 * @return
	 * true: If the start and end figures of this connection and the other connection match.<br>
	 * false: If the start or end figure is different.
	 * @see CH.ifa.draw.framework.ConnectionFigure#connectsSame(ConnectionFigure other)
	 */
	public abstract boolean connectsSame(ConnectionFigure other);

	/**
	 * @see CH.ifa.draw.framework.ConnectionFigure#updateConnection()
	 */
	public void updateConnection()
	{
		layoutConnection();
	}

	/**
	 * Sets the start connector.
	 *
	 * @param startConnector Start connector
	 */
	protected void setStartConnector(Connector startConnector)
	{
		this.startConnector = startConnector;
	}

	/**
	 * Gets the start connector.
	 *
	 * @return The connector or null if not connected
	 * @see CH.ifa.draw.framework.ConnectionFigure#getStartConnector()
	 */
	public Connector getStartConnector()
	{
		return startConnector;
	}

	/**
	 * Sets the end connector.
	 *
	 * @param endConnector End connector
	 */
	protected void setEndConnector(Connector endConnector)
	{
		this.endConnector = endConnector;
	}

	/**
	 * Gets end connector.
	 *
	 * @return The connector or null if not connected
	 * @see CH.ifa.draw.framework.ConnectionFigure#getEndConnector()
	 */
	public Connector getEndConnector()
	{
		return endConnector;
	}

	/**
	 * Sets the start point of the connection.
	 * @nowarn
	 * @see CH.ifa.draw.framework.ConnectionFigure#startPoint(int x, int y)
	 */
	public void startPoint(int x, int y)
	{
		if (segments.size() == 0)
		{
			willChange();

			segments.add(new CubicCurve2D.Double(x, y, x, y, x, y, x, y));
			changed();
		}
		else
		{
			setPointAt(0, new Point(x, y));
		}
	}

	/**
	 * Gets the start point of the connection.
	 * @nowarn
	 * @see CH.ifa.draw.framework.ConnectionFigure#startPoint()
	 */
	public Point startPoint()
	{
		Point2D p = getPointAt(0);

		return new Point(CommonUtil.rnd(p.getX()), CommonUtil.rnd(p.getY()));
	}

	/**
	 * Sets the end point of the connection.
	 * @nowarn
	 * @see CH.ifa.draw.framework.ConnectionFigure#endPoint(int x, int y)
	 */
	public void endPoint(int x, int y)
	{
		setPointAt(segments.size(), new Point(x, y));
	}

	/**
	 * Gets the end point of the connection.
	 * @nowarn
	 * @see CH.ifa.draw.framework.ConnectionFigure#endPoint()
	 */
	public Point endPoint()
	{
		Point2D p = getPointAt(segments.size());

		return new Point(CommonUtil.rnd(p.getX()), CommonUtil.rnd(p.getY()));
	}

	/**
	 * Gets the start figure of the connection.
	 * @return The start figure or null if not connected
	 * @see CH.ifa.draw.framework.ConnectionFigure#startFigure()
	 */
	public Figure startFigure()
	{
		if (startConnector != null)
		{
			return startConnector.owner();
		}
		return null;
	}

	/**
	 * Gets the end figure of the connection.
	 * @return The end figure or null if not connected
	 * @see CH.ifa.draw.framework.ConnectionFigure#endFigure()
	 */
	public Figure endFigure()
	{
		if (endConnector != null)
		{
			return endConnector.owner();
		}
		return null;
	}

	//////////////////////////////////////////////////
	// @@ PolySplineFigure overrides
	//////////////////////////////////////////////////

	protected Point2D constrainCtrlPoint(int index, int side, Point2D ctrl)
	{
		if ((index == 0 && side == RIGHT_CONTROLPOINT && startFigure() != null) || (index == segments.size() && side == LEFT_CONTROLPOINT && endFigure() != null))
		{
			// we are constraining the start-controlPoint

			Point2D start = getPointAt(index);
			Point2D end = getPointAt(index == 0 ? 1 : segments.size() - 1);

			double xdiff = start.getX() - end.getX();
			xdiff *= xdiff;

			double ydiff = start.getY() - end.getY();
			ydiff *= ydiff;

			double distance = Math.sqrt(xdiff + ydiff);

			Orientation orientation;
			if (index == 0)
			{
				distance *= getStartFactor();
				orientation = ((TagConnector) getStartConnector()).getOrientation();
			}
			else
			{
				distance *= getEndFactor();
				orientation = ((TagConnector) getEndConnector()).getOrientation();
			}

			switch (orientation)
			{
			case TOP:
				distance = -distance;

			// Fall through

			case BOTTOM:
				ctrl.setLocation(start.getX(), start.getY() + distance);
				break;

			case LEFT:
				distance = -distance;

			// Fall through

			case RIGHT:
				ctrl.setLocation(start.getX() + distance, start.getY());
				break;
			}
		}

		return ctrl;
	}

	/**
	 * Ensures that a connection is updated if the connection was moved.
	 * @see org.openbp.cockpit.modeler.figures.spline.PolySplineFigure#basicMoveBy(int dx, int dy)
	 */
	protected void basicMoveBy(int dx, int dy)
	{
		super.basicMoveBy(dx, dy);

		// Make sure that we are still connected
		layoutConnection();
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#displayBox()
	 */
	public Rectangle displayBox()
	{
		return super.displayBox().union(label.displayBox());
	}

	/**
	 * Draws the label in addition to the spline.
	 * Performs drawing only if the connection is not minimized or under the cursor.
	 * @see org.openbp.cockpit.modeler.figures.spline.PolySplineFigure#drawSpline(Graphics2D g2)
	 */
	protected void drawSpline(Graphics2D g2)
	{
		if (isVisible() && !isMinimized())
		{
			super.drawSpline(g2);

			label.draw(g2);
		}
	}

	/**
	 * Draws the start, end and animation decorations of the spline.
	 * @see org.openbp.cockpit.modeler.figures.spline.PolySplineFigure#drawDecorations(Graphics g)
	 */
	protected void drawDecorations(Graphics g)
	{
		if (isVisible() && !isMinimized())
		{
			// Draw the arrows
			super.drawDecorations(g);

			if (getAnimationDecoration() != null)
			{
				// Draw the small circle running down the spline
				double pos = ((Double) DecorationMgr.decorate(this, DECO_ANIMATION, NAN)).doubleValue();

				if (pos >= 0d && pos <= 1d)
				{
					Point2D p = getPointOnCurve(pos);

					g.fillOval(CommonUtil.rnd(p.getX()) - 4, CommonUtil.rnd(p.getY()) - 4, 8, 8);
				}
			}
		}
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#containsPoint(int x, int y)
	 */
	public boolean containsPoint(int x, int y)
	{
		if (!isVisible() || isMinimized())
		{
			// We won't react on user interaction if we are not visible or minimized.
			return false;
		}

		return label.containsPoint(x, y) || super.containsPoint(x, y);
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#handles()
	 */
	public Vector handles()
	{
		// We have a handle for each waypoint, one each for start and end
		// control points and two each for every inner waypoint's control point
		Vector handles = new Vector(segments.size() * 3 + 1);

		int max = segments.size();

		// Handle of first ControlPoint

		handles.add(new ConstrainedControlPointHandle(this, 0, 1));

		for (int i = 1; i < max; i++)
		{
			handles.add(new ControlPointHandle(this, i, 0));
			handles.add(new ControlPointHandle(this, i, 1));
		}

		handles.add(new ConstrainedControlPointHandle(this, max, max - 1));

		handles.addElement(new ChangeConnectionStartHandle(this));
		handles.addElement(new ChangeConnectionEndHandle(this));

		for (int i = 1; i < max; i++)
		{
			handles.add(new WayPointHandle(this, i));
		}

		handles.add(new MoveLableHandle());

		return handles;
	}

	public void setPointAt(Point p, int i)
	{
		super.setPointAt(i, p);

		layoutConnection();
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#connectedTextLocator(Figure)
	 */
	public Locator connectedTextLocator(Figure figure)
	{
		return textLocator;
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#release()
	 */
	public void release()
	{
		super.release();

		handleDisconnect(startFigure(), endFigure());

		// Remove ourself as figure change listener from the figure we are connected to
		removeStartConnectorChangeListener();
		removeEndConnectorChangeListener();
	}

	//////////////////////////////////////////////////
	// @@ Class methods
	//////////////////////////////////////////////////

	/**
	 * Checks if connection should be reversed.
	 * @param startFigure Proposed start figure
	 * @param endFigure Proposed end figure
	 * @return The default implementation always returns false
	 */
	protected boolean shouldReverse(Figure startFigure, Figure endFigure)
	{
		return false;
	}

	/**
	 * Handles the connection of a connection.
	 * Does nothing by default.
	 * @param startFigure Figure to connnect to
	 * @param endFigure Figure to connnect to
	 */
	protected void handleConnect(Figure startFigure, Figure endFigure)
	{
	}

	/**
	 * Handles the disconnection of a connection.
	 * Does nothing by default.
	 * @param startFigure Figure to disconnect from
	 * @param endFigure Figure to disconnect from
	 */
	protected void handleDisconnect(Figure startFigure, Figure endFigure)
	{
	}

	/**
	 * Adds ourself as figure change listener to the start figure we are connected to.
	 * Does nothing by default.
	 */
	protected void addStartConnectorChangeListener()
	{
	}

	/**
	 * Removes ourself as figure change listener from the start figure we are connected to.
	 * Does nothing by default.
	 */
	protected void removeStartConnectorChangeListener()
	{
	}

	/**
	 * Adds ourself as figure change listener to the end figure we are connected to.
	 * Does nothing by default.
	 */
	protected void addEndConnectorChangeListener()
	{
	}

	/**
	 * Removes ourself as figure change listener from the end figure we are connected to.
	 * Does nothing by default.
	 */
	protected void removeEndConnectorChangeListener()
	{
	}

	/**
	 * Gets the factor used to calculate the actual postion of the control point of the spline's start point.
	 * @nowarn
	 */
	public double getStartFactor()
	{
		return startFactor;
	}

	/**
	 * Sets the factor used to calculate the actual postion of the control point of the spline's start point.
	 * @nowarn
	 */
	public void setStartFactor(double startFactor)
	{
		this.startFactor = startFactor;

		layoutConnection();
	}

	/**
	 * Gets the factor used to calculate the actual postion of the control point of the spline's end point.
	 * @nowarn
	 */
	public double getEndFactor()
	{
		return endFactor;
	}

	/**
	 * Sets the factor used to calculate the actual postion of the control point of the spline's end point.
	 * @nowarn
	 */
	public void setEndFactor(double endFactor)
	{
		this.endFactor = endFactor;

		layoutConnection();
	}

	/**
	 * Gets the label of this connection.
	 * @nowarn
	 */
	public MoveableTitleFigure getLabel()
	{
		return label;
	}

	/**
	 * Checks if the connection is minimized.
	 * @nowarn
	 */
	public boolean isMinimized()
	{
		return false;
	}

	/**
	 * Performs a layout of the connection.
	 * This is called when the connection itself changes.
	 * By default the start and end points of the connection are recalculated.
	 */
	public void layoutConnection()
	{
		if (startConnector != null)
		{
			Point start = startConnector.findStart(this);
			if (start != null)
			{
				startPoint(start.x, start.y);
			}
		}

		if (endConnector != null)
		{
			Point end = endConnector.findEnd(this);
			if (end != null)
			{
				endPoint(end.x, end.y);
			}
		}

		clearShapeCache();
	}

	/**
	 * Performs a layout of the connection, adjusting connection start/end point directions.
	 * The method tries to ensure that the connection does not cross its start/end point
	 * figures by adjusting the direction the connection will take from its start or end point.
	 *
	 * By default, this method just calls the {@link #layoutConnection} method.
	 */
	public void layoutAndAdjustConnection()
	{
		layoutConnection();
	}

	//////////////////////////////////////////////////
	// @@ FigureChangeListener overrides
	//////////////////////////////////////////////////

	public void figureChanged(FigureChangeEvent e)
	{
		layoutConnection();
	}

	public void figureRemoved(FigureChangeEvent e)
	{
	}

	public void figureRequestRemove(FigureChangeEvent e)
	{
	}

	public void figureInvalidated(FigureChangeEvent e)
	{
	}

	public void figureRequestUpdate(FigureChangeEvent e)
	{
	}

	//////////////////////////////////////////////////
	// @@ Geometry serialization support
	//////////////////////////////////////////////////

	public void decode(String decoder)
	{
		if (decoder == null)
		{
			return;
		}

		StringTokenizer tok = new StringTokenizer(decoder, "|");

		while (tok.hasMoreTokens())
		{
			decodeParameter(tok.nextToken());
		}

		rebuildShapeCache();
	}

	protected void decodeParameter(String parameter)
	{
		StringTokenizer inner = new StringTokenizer(parameter, ":");

		try
		{
			String name = inner.nextToken();

			if (name.equals("points"))
			{
				int x = Integer.parseInt(inner.nextToken());

				// We provide empty segments
				segments.clear();

				for (int i = 0; i < x; i++)
				{
					double x1 = Double.parseDouble(inner.nextToken());
					double y1 = Double.parseDouble(inner.nextToken());
					double x2 = Double.parseDouble(inner.nextToken());
					double y2 = Double.parseDouble(inner.nextToken());
					double x3 = Double.parseDouble(inner.nextToken());
					double y3 = Double.parseDouble(inner.nextToken());
					double x4 = Double.parseDouble(inner.nextToken());
					double y4 = Double.parseDouble(inner.nextToken());

					segments.add(new CubicCurve2D.Double(x1, y1, x2, y2, x3, y3, x4, y4));
				}
			}
			else if (name.equals("label"))
			{
				// Label position
				int x = Integer.parseInt(inner.nextToken());
				int y = Integer.parseInt(inner.nextToken());

				label.moveBy(x, y);
			}
			else if (name.equals("factors"))
			{
				startFactor = Double.parseDouble(inner.nextToken());
				endFactor = Double.parseDouble(inner.nextToken());
			}
		}
		catch (Exception e)
		{
		}
	}

	protected String encode()
	{
		String result = "points:" + segments.size();

		for (Iterator it = segments.iterator(); it.hasNext();)
		{
			CubicCurve2D next = (CubicCurve2D) it.next();

			result += (":" + FigureUtil.printInt(next.getX1()) + ":" + FigureUtil.printInt(next.getY1()) + ":" + FigureUtil.printInt(next.getCtrlX1()) + ":" + FigureUtil.printInt(next.getCtrlY1()) + ":" + FigureUtil.printInt(next.getCtrlX2()) + ":" + FigureUtil.printInt(next.getCtrlY2()) + ":" + FigureUtil.printInt(next.getX2()) + ":" + FigureUtil.printInt(next.getY2()));
		}

		result += "|label:" + (label.center().x - textLocator.locate(this).x) + ":" + (label.center().y - textLocator.locate(this).y);

		result += "|factors:" + startFactor + ":" + endFactor;

		return result;
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
		if (event.type == VisualElementEvent.DOUBLE_CLICK)
		{
			// TODONOW
			DrawingEditorPlugin editor = getDrawing().getEditor();

			editor.startUndo("Remove Link Control Point");

			if (!joinSegments(event.x, event.y))
			{
				splitSegment(event.x, event.y);

				ModelerUndoable undoable = (ModelerUndoable) ((Modeler) editor).getCurrentUndoable();
				undoable.setDisplayName("Add Link Control Point");
			}

			editor.view().toggleSelection(this);
			editor.view().toggleSelection(this);

			editor.endUndo();

			layoutConnection();

			return true;
		}
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
		return containsPoint(x, y) ? this : null;
	}

	//////////////////////////////////////////////////
	// @@ UpdatableFigure implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.generic.UpdatableFigure#updateFigure()
	 */
	public void updateFigure()
	{
		label.updateFigure();
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ InteractionClient
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
		return getImportersAt(p);
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

	//////////////////////////////////////////////////
	// @@ Storable implementation
	//////////////////////////////////////////////////

	public void write(StorableOutput dw)
	{
		super.write(dw);

		dw.writeStorable(startConnector);
		dw.writeStorable(endConnector);
	}

	public void read(StorableInput dr)
		throws IOException
	{
		super.read(dr);
		Connector start = (Connector) dr.readStorable();
		if (start != null)
		{
			connectStart(start);
		}
		Connector end = (Connector) dr.readStorable();
		if (end != null)
		{
			connectEnd(end);
		}
		if (start != null && end != null)
		{
			layoutConnection();
		}
	}

	private void readObject(ObjectInputStream s)
		throws ClassNotFoundException, IOException
	{
		s.defaultReadObject();
		if (startConnector != null)
		{
			connectStart(startConnector);
		}
		if (endConnector != null)
		{
			connectEnd(endConnector);
		}
	}

	//////////////////////////////////////////////////
	// @@ Spline locator class
	//////////////////////////////////////////////////

	/**
	 * Locator that returns an arbitary position on the spline. This position defaults
	 * to center.
	 */
	class SplineLocator extends AbstractLocator
	{
		/** The postion ranging from 0d (start) to 1d (end) */
		private double position;

		/**
		 * Create a new SplineLocator that defaults to 0\.5d.
		 */
		public SplineLocator()
		{
			super();

			this.position = 0.5d;
		}

		/**
		 * Returns position on the Spline.
		 *
		 * @param figure Is ignored
		 */
		public Point locate(Figure figure)
		{
			Point2D p = getPointOnCurve(position);

			return new Point(CommonUtil.rnd(p.getX()), CommonUtil.rnd(p.getY()));
		}

		/**
		 * Returns the position.
		 * @return double
		 */
		public double getPosition()
		{
			return position;
		}

		/**
		 * Sets the position.
		 * @param position The position to set
		 */
		public void setPosition(double position)
		{
			this.position = position;
		}
	}

	//////////////////////////////////////////////////
	// @@ Handle class
	//////////////////////////////////////////////////

	/**
	 * Handle that is used to move the position of the TextLable,
	 * relative to its anchorpoint on the spline.
	 */
	class MoveLableHandle extends AbstractHandle
	{
		int lastX;

		int lastY;

		/**
		 * Constructor.
		 */
		public MoveLableHandle()
		{
			super(PolySplineConnection.this);
		}

		/**
		 * @see CH.ifa.draw.standard.AbstractHandle#invokeStart(int x, int y, Drawing drawing)
		 */
		public void invokeStart(int x, int y, Drawing drawing)
		{
			lastX = x;
			lastY = y;
		}

		/**
		 * @see CH.ifa.draw.standard.AbstractHandle#invokeStep(int x, int y, int ax, int ay, DrawingView view)
		 */
		public void invokeStep(int x, int y, int ax, int ay, DrawingView view)
		{
			willChange();

			label.moveBy(x - lastX, y - lastY);

			lastX = x;
			lastY = y;

			changed();
		}

		/**
		 * @see CH.ifa.draw.standard.AbstractHandle#locate()
		 */
		public Point locate()
		{
			return label.displayBox().getLocation();
		}

		/**
		 * @see CH.ifa.draw.standard.AbstractHandle#draw(Graphics g)
		 */
		public void draw(Graphics g)
		{
			super.draw(g);

			Graphics2D g2 = (Graphics2D) g;

			Point p1 = textLocator.locate(PolySplineConnection.this);
			Point p2 = locate();

			Stroke old = g2.getStroke();

			g2.setStroke(ModelerGraphics.labelHandleStroke);

			Color olc = g.getColor();

			g.setColor(ModelerColors.LABEL_HANDLE_LINE);

			g.drawLine(p1.x, p1.y, p2.x, p2.y);

			g2.setStroke(old);
			g.setColor(olc);
		}
	}
}
