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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.openbp.cockpit.modeler.util.FigureResources;
import org.openbp.common.CommonUtil;

import CH.ifa.draw.figures.LineDecoration;
import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.standard.AbstractFigure;
import CH.ifa.draw.util.Geom;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

/**
 * A poly spline figure is a line figure consisting of an arbitary number of
 * cubic Bezier curves.
 * It has handles for all waypoints as well as for each control point.
 * A waypoint can have three different states:<br>
 * A corner means that both control points are completely independent of each other.<br>
 * A curve means that both control points are on opposite angles but may have a different distance.<br>
 * An even curve means that one control point is the direct mirror of its counterpart.
 *
 * @author Stephan Moritz
 */
public class PolySplineFigure extends AbstractFigure
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	public static final int LEFT_CONTROLPOINT = 0;

	public static final int RIGHT_CONTROLPOINT = 1;

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Contains the actual curve segements (contains CubicCurve2D objects) */
	protected List segments;

	/** Decoration for the start point of the spline */
	private LineDecoration startDecoration;

	/** Decoration for the end point of the spline */
	private LineDecoration endDecoration;

	/** Decoration for the spline animation */
	private LineDecoration animationDecoration;

	/** Only draw decorations if this is true */
	protected boolean drawDecorations = true;

	/** Color of the spline */
	private Color frameColor = Color.BLACK;

	/** Stroke */
	private Stroke stroke = FigureResources.standardStroke1;

	/** Used to cache the outlines of the splines for hit-detection */
	private Shape [] shapeCache;

	/** Stroke used to generate the outline shape for hit-detection (15 pixels wide) */
	private static Stroke outlineStroke = new BasicStroke(15);

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public PolySplineFigure()
	{
		segments = new LinkedList();
	}

	//////////////////////////////////////////////////
	// @@ AbstractFigure overrides
	//////////////////////////////////////////////////

	/**
	 * The display box equals the bounds of the spline enlarged by 30 pixels vertically and horizontally.
	 * @see CH.ifa.draw.standard.AbstractFigure#displayBox()
	 */
	public Rectangle displayBox()
	{
		Rectangle r = getSplineBounds();
		r.grow(30, 30);
		return r;
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#basicDisplayBox(Point origin, Point corner)
	 */
	public void basicDisplayBox(Point origin, Point corner)
	{
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#isEmpty()
	 */
	public boolean isEmpty()
	{
		return (size().width < 3) && (size().height < 3);
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#handles()
	 */
	public Vector handles()
	{
		// We have a handle for each waypoint, one each for start and end
		// control points and two each for every inner waypoint's control point
		Vector handles = new Vector(segments.size() * 3 + 1);

		int max = segments.size();
		for (int i = 0; i <= max; i++)
		{
			handles.add(new WayPointHandle(this, i));
		}

		return handles;
	}

	/**
	 * Returns true if the given point is near to the spline.
	 * @see CH.ifa.draw.standard.AbstractFigure#containsPoint(int x, int y)
	 */
	public boolean containsPoint(int x, int y)
	{
		return findSegment(x, y) != -1;
	}

	/**
	 * Spline figures cannot be connected and return false.
	 * @see CH.ifa.draw.standard.AbstractFigure#canConnect()
	 */
	public boolean canConnect()
	{
		return false;
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#connectorAt(int x, int y)
	 */
	public Connector connectorAt(int x, int y)
	{
		// NO Connectors at the moment
		return null;
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#basicMoveBy(int dx, int dy)
	 */
	protected void basicMoveBy(int dx, int dy)
	{
		// Don't move the start and end point since they are connected
		for (Iterator it = segments.iterator(); it.hasNext();)
		{
			CubicCurve2D next = (CubicCurve2D) it.next();

			next.setCurve(next.getX1() + dx, next.getY1() + dy, next.getCtrlX1() + dx, next.getCtrlY1() + dy, next.getCtrlX2() + dx, next.getCtrlY2() + dy, next.getX2() + dx, next.getY2() + dy);
		}
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
		g2.setColor(getFrameColor());

		drawSpline(g2);

		if (drawDecorations)
		{
			g.setColor(getFrameColor());
			drawDecorations(g);
		}

		g2.setColor(oldColor);
		g2.setStroke(oldStroke);
	}

	//////////////////////////////////////////////////
	// @@ Various methods
	//////////////////////////////////////////////////

	/**
	 * Draws the spline itself.
	 *
	 * @param g2 Graphics to draw to
	 */
	protected void drawSpline(Graphics2D g2)
	{
		for (Iterator it = segments.iterator(); it.hasNext();)
		{
			g2.draw((CubicCurve2D) it.next());
		}
	}

	/**
	 * Gets the display box of the spline's segments.
	 * @nowarn
	 */
	public Rectangle getSplineBounds()
	{
		// We sum up all boundingboxes of our segments
		if (segments.size() == 0)
		{
			return new Rectangle();
		}

		Rectangle r = new Rectangle(pointAt(0));

		for (Iterator it = segments.iterator(); it.hasNext();)
		{
			r = r.union(((CubicCurve2D) it.next()).getBounds());
		}

		return r;
	}

	protected void adjustOpposite(int index, int side)
	{
		if (index == 0 || index >= segments.size())
		{
			// Corner or no valid point ?
			return;
		}

		// At this Point, the Point exists and must be modified.

		// we get the center (i.e. waypoint)
		Point2D p = getCtrlPointAt(index, side);

		Point2D center = getPointAt(index);

		Point2D target = new Point2D.Double(2f * center.getX() - p.getX(), 2f * center.getY() - p.getY());

		if (side == 0)
		{
			// We modify CtrlP1
			CubicCurve2D next = (CubicCurve2D) segments.get(index);

			next.setCurve(next.getP1(), target, next.getCtrlP2(), next.getP2());
		}
		else
		{
			// We modify CtrlP2
			CubicCurve2D prev = (CubicCurve2D) segments.get(index - 1);

			prev.setCurve(prev.getP1(), prev.getCtrlP1(), target, prev.getP2());
		}
	}

	//////////////////////////////////////////////////
	// @@ Attributes
	//////////////////////////////////////////////////

	/**
	 * Gets the attribute with the given name.
	 * Maps the "ArrowMode" attribute to a line decoration.
	 * @nowarn
	 */
	public Object getAttribute(String name)
	{
		if (name.equals("FrameColor"))
		{
			return getFrameColor();
		}
		return super.getAttribute(name);
	}

	/**
	 * Sets the attribute with the given name.
	 * Interprets the "ArrowMode" attribute to set the line decoration.
	 * @nowarn
	 */
	public void setAttribute(String name, Object value)
	{
		if (name.equals("FrameColor"))
		{
			setFrameColor((Color) value);
			changed();
		}
		else
		{
			super.setAttribute(name, value);
		}
	}

	//////////////////////////////////////////////////
	// @@ Spline points
	//////////////////////////////////////////////////

	public int pointCount()
	{
		return segments.size() + 1;
	}

	public Point pointAt(int i)
	{
		Point2D p = getPointAt(i);
		return new Point(CommonUtil.rnd(p.getX()), CommonUtil.rnd(p.getY()));
	}

	public Point2D getPointAt(int i)
	{
		return (i != 0 ? ((CubicCurve2D) segments.get(i - 1)).getP2() : ((CubicCurve2D) segments.get(i)).getP1());
	}

	public Point2D getCtrlPointAt(int i, int side)
	{
		return (side != LEFT_CONTROLPOINT ? ((CubicCurve2D) segments.get(i)).getCtrlP1() : ((CubicCurve2D) segments.get(i - 1)).getCtrlP2());
	}

	public void setPointAt(int index, Point2D target)
	{
		willChange();

		// We determine the translation
		int dx, dy;

		Point2D p = getPointAt(index);

		dx = CommonUtil.rnd(target.getX() - p.getX());
		dy = CommonUtil.rnd(target.getY() - p.getY());

		if (index > 0)
		{
			// if index is not the first segment, we adjust the endpoint of the previous segment
			CubicCurve2D prev = (CubicCurve2D) segments.get(index - 1);

			p = prev.getCtrlP2();

			p.setLocation(p.getX() + dx, p.getY() + dy);

			prev.setCurve(prev.getP1(), prev.getCtrlP1(), p, target);

			basicSetCtrlPoint(index, LEFT_CONTROLPOINT, p);
		}
		if (index < segments.size())
		{
			// likewise, if we are not the last segment, we adjust the position of the one after us

			CubicCurve2D next = (CubicCurve2D) segments.get(index);

			p = next.getCtrlP1();

			p.setLocation(p.getX() + dx, p.getY() + dy);

			next.setCurve(target, p, next.getCtrlP2(), next.getP2());

			basicSetCtrlPoint(index, RIGHT_CONTROLPOINT, p);
		}

		changed();
		clearShapeCache();
	}

	public void setCtrlPointAt(int index, int side, Point2D target)
	{
		willChange();

		basicSetCtrlPoint(index, side, target);
		adjustOpposite(index, side);

		changed();
		clearShapeCache();
	}

	protected void basicSetCtrlPoint(int index, int side, Point2D target)
	{
		target = constrainCtrlPoint(index, side, target);

		if (side != LEFT_CONTROLPOINT)
		{
			// We modify CtrlP1
			CubicCurve2D next = (CubicCurve2D) segments.get(index);
			next.setCurve(next.getP1(), target, next.getCtrlP2(), next.getP2());
		}
		else
		{
			// We modify CtrlP2
			CubicCurve2D prev = (CubicCurve2D) segments.get(index - 1);
			prev.setCurve(prev.getP1(), prev.getCtrlP1(), target, prev.getP2());
		}
	}

	public void removePoint(int i)
	{
		willChange();

		if (i == 0)
		{
			// we remove the first point and thus the first segement
			segments.remove(0);
		}
		else if (i == segments.size())
		{
			// we remove the last point and thus the last segment
			segments.remove(segments.size() - 1);
		}
		else
		{
			// - we remove the segment AFTER the given point
			// - we set the end point of the segment before the given point
			//   to the start point of the segment after the deleted segment

			CubicCurve2D removed = (CubicCurve2D) segments.remove(i);
			CubicCurve2D prev = (CubicCurve2D) segments.get(i - 1);

			prev.setCurve(prev.getP1(), prev.getCtrlP1(), removed.getCtrlP2(), removed.getP2());
		}

		changed();
		clearShapeCache();
	}

	protected Point2D constrainCtrlPoint(int index, int side, Point2D target)
	{
		// By default, no constraint
		return target;
	}

	/**
	 * Determines a point on the spline, specified by a ratio.
	 *
	 * @param position Ratio that determines the postion on the spline, ranging from 0 (startpoint) to 1 (endpoint)
	 * @return The (modified) 'over' argument or a new point if null
	 */
	protected Point2D getPointOnCurve(double position)
	{
		Point2D ret = new Point2D.Double();

		if (segments.size() == 0)
			return ret;

		if (position < 0)
		{
			position = 0;
		}
		if (position > 1)
		{
			position = 1;
		}

		// we normalize the position to the number of segments
		position = position * segments.size();

		// We get the appropriate segment
		CubicCurve2D curve = (CubicCurve2D) segments.get(Math.min(segments.size() - 1, (int) Math.floor(position)));

		position %= 1;

		double k1, k2, k3, k4;

		k1 = (1 - position) * (1 - position) * (1 - position);
		k2 = (1 - position) * (1 - position) * position * 3;
		k3 = (1 - position) * position * position * 3;
		k4 = position * position * position;

		ret.setLocation(k1 * curve.getX1() + k2 * curve.getCtrlX1() + k3 * curve.getCtrlX2() + k4 * curve.getX2(), k1 * curve.getY1() + k2 * curve.getCtrlY1() + k3 * curve.getCtrlY2() + k4 * curve.getY2());

		return ret;
	}

	//////////////////////////////////////////////////
	// @@ Spline segments
	//////////////////////////////////////////////////

	/**
	 * Gets the segment at the given index.
	 *
	 * @param i Index
	 * @return The segment
	 */
	public CubicCurve2D segmentAt(int i)
	{
		return (CubicCurve2D) segments.get(i);
	}

	/**
	 * Gets the segment of the polyline that is hit by
	 * the given point.
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @return the index of the segment or -1 if no segment was hit
	 */
	public int findSegment(int x, int y)
	{
		// quick'n'dirty check...
		Rectangle bounds = displayBox();
		bounds.grow(4, 4);
		if (!bounds.contains(x, y))
		{
			return -1;
		}

		if (shapeCache == null)
		{
			rebuildShapeCache();
		}

		// new testing via shapes
		for (int i = 0; i < shapeCache.length; i++)
		{
			if (shapeCache [i].contains(x, y))
			{
				return i;
			}
		}

		return -1;
	}

	/**
	 * Rebuilds the shape cache used for hit detection.
	 */
	protected void rebuildShapeCache()
	{
		if (shapeCache != null)
		{
			return;
		}

		// Cache is empty, we need to generate it
		shapeCache = new Shape [segments.size()];

		for (int i = 0; i < shapeCache.length; i++)
		{
			shapeCache [i] = outlineStroke.createStrokedShape(segmentAt(i));
		}
	}

	/**
	 * Clears the shape cache used for hit detection.
	 */
	protected void clearShapeCache()
	{
		shapeCache = null;
	}

	/**
	 * Splits the segment at the given point if a segment was hit.
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @return Index of the segment or -1 if no segment was hit
	 */
	public int splitSegment(int x, int y)
	{
		willChange();

		// find the segment to this coordinates
		int i = findSegment(x, y);

		if (i != -1)
		{
			CubicCurve2D insert = new CubicCurve2D.Double();

			CubicCurve2D prev = ((CubicCurve2D) segments.get(i));

			prev.subdivide(prev, insert);

			segments.add(i + 1, insert);

			setPointAt(i + 1, new Point(x, y));

			adjustOpposite(i, LEFT_CONTROLPOINT);
			adjustOpposite(i + 2, RIGHT_CONTROLPOINT);
		}

		changed();

		clearShapeCache();

		return i + 1;
	}

	/**
	 * Joins to segments into one if the given point hits a node of the polyline.
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @return
	 * true: If the two segments were joined<br>
	 * false: Otherwise
	 */
	public boolean joinSegments(int x, int y)
	{
		willChange();

		Iterator it = segments.iterator();
		if (it.hasNext())
		{
			// Skip first segment
			it.next();

			for (int i = 1; it.hasNext(); i++)
			{
				Point2D p = ((CubicCurve2D) it.next()).getP1();
				if (Geom.length(x, y, (int) p.getX(), (int) p.getY()) < 5)
				{
					removePoint(i);
					return true;
				}
			}
		}

		changed();
		clearShapeCache();
		return false;
	}

	//////////////////////////////////////////////////
	// @@ Decorations
	//////////////////////////////////////////////////

	/**
	 * Draws the start and end decorations of the spline.
	 *
	 * @param g Graphics to draw to
	 */
	protected void drawDecorations(Graphics g)
	{
		// Draw the start symbol (usually none)
		if (getStartDecoration() != null)
		{
			Point2D p1 = getPointAt(0);
			Point2D p2 = getCtrlPointAt(0, RIGHT_CONTROLPOINT);
			getStartDecoration().draw(g, CommonUtil.rnd(p1.getX()), CommonUtil.rnd(p1.getY()), CommonUtil.rnd(p2.getX()), CommonUtil.rnd(p2.getY()));
		}

		// Draw the end symbol (usually an arrow)
		if (getEndDecoration() != null)
		{
			Point2D p1 = getPointAt(segments.size());
			Point2D p2 = getCtrlPointAt(segments.size(), LEFT_CONTROLPOINT);
			getEndDecoration().draw(g, CommonUtil.rnd(p1.getX()), CommonUtil.rnd(p1.getY()), CommonUtil.rnd(p2.getX()), CommonUtil.rnd(p2.getY()));
		}
	}

	/**
	 * Gets the decoration for the start point of the spline.
	 * @nowarn
	 */
	public LineDecoration getStartDecoration()
	{
		return startDecoration;
	}

	/**
	 * Sets the decoration for the start point of the spline.
	 * @nowarn
	 */
	public void setStartDecoration(LineDecoration startDecoration)
	{
		this.startDecoration = startDecoration;
	}

	/**
	 * Gets the decoration for the end point of the spline.
	 * @nowarn
	 */
	public LineDecoration getEndDecoration()
	{
		return endDecoration;
	}

	/**
	 * Sets the decoration for the end point of the spline.
	 * @nowarn
	 */
	public void setEndDecoration(LineDecoration endDecoration)
	{
		this.endDecoration = endDecoration;
	}

	/**
	 * Gets the decoration for the spline animation.
	 * @nowarn
	 */
	public LineDecoration getAnimationDecoration()
	{
		return animationDecoration;
	}

	/**
	 * Sets the decoration for the spline animation.
	 * @nowarn
	 */
	public void setAnimationDecoration(LineDecoration animationDecoration)
	{
		this.animationDecoration = animationDecoration;
	}

	/**
	 * Sets the flag if decorations should be drawn.
	 * @nowarn
	 */
	public void setDrawDecorations(boolean drawDecorations)
	{
		this.drawDecorations = drawDecorations;
	}

	/**
	 * Gets the color of the spline.
	 * @nowarn
	 */
	public Color getFrameColor()
	{
		return frameColor;
	}

	/**
	 * Sets the color of the spline.
	 * @nowarn
	 */
	public void setFrameColor(Color frameColor)
	{
		this.frameColor = frameColor;
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

	//////////////////////////////////////////////////
	// @@ Storable implementation
	//////////////////////////////////////////////////

	public void write(StorableOutput dw)
	{
		super.write(dw);

		/*
		 dw.writeInt (fPoints.size ());
		 Enumeration k = fPoints.elements ();
		 while (k.hasMoreElements ())
		 {
		 Point p = (Point) k.nextElement ();
		 dw.writeInt (p.x);
		 dw.writeInt (p.y);
		 }
		 dw.writeStorable (startDecoration);
		 dw.writeStorable (endDecoration);
		 dw.writeColor (frameColor);
		 */
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#read(StorableInput dr)
	 */
	public void read(StorableInput dr)
		throws IOException
	{
		super.read(dr);

		/*
		 int size = dr.readInt ();
		 fPoints = new Vector (size);
		 for (int i = 0; i<size; i++)
		 {
		 int x = dr.readInt ();
		 int y = dr.readInt ();
		 fPoints.addElement (new Point (x, y));
		 }
		 setStartDecoration ((LineDecoration) dr.readStorable ());
		 setEndDecoration ((LineDecoration) dr.readStorable ());
		 frameColor = dr.readColor ();
		 */
	}
}
