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
package org.openbp.jaspira.gui.interaction;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.datatransfer.Transferable;
import java.awt.geom.RectangularShape;

import javax.swing.SwingUtilities;

import org.openbp.swing.SwingUtil;

/**
 * Basic implementation of DragAwareRegion. This implementation receives its parameters
 * via the constructor. It can not be used for component elements that change
 * dynamically during Dragging.
 *
 * @author Stephan Moritz
 */
public class BasicDropRegion
	implements DragAwareRegion
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** Name to identify this drop region to the drop client */
	protected Object id;

	/** Drop client that this region belongs to */
	protected InteractionClient parent;

	/** Visible shape of the region. Relative to drop client origin. */
	private RectangularShape shape;

	/** Color of the frame of the region. May be null. */
	private Color frameColor;

	/** Stroke for the frame. May be null. */
	private Stroke stroke;

	/** Fill paint for the region */
	private Paint paint;

	/** Cursor prototype that should be used for constructing a cursor if a drag over occurs */
	private CursorPrototype cursor;

	/** Overlay shape for this region */
	private Shape overlay;

	/** Origin to which the coordinates are relative */
	protected Component origin;

	/** Text of the Tooltip that should be displayed for this region */
	protected String toolTipText;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor for a rectangular region.
	 *
	 * @param id Name to identify this drop region to the drop client
	 * @param parent Drop client that this region belongs to
	 * @param shape Shape to represent this region. Relative to drop client origin.
	 * @param origin Component to which th shape is relative to
	 */
	public BasicDropRegion(Object id, InteractionClient parent, RectangularShape shape, Component origin)
	{
		this.id = id;
		this.parent = parent;
		this.shape = shape;
		this.origin = origin;

		// We change the coordiantes to world coordinates.
		this.shape.setFrame(SwingUtil.convertRectToGlassCoords(shape.getBounds(), origin));
	}

	/**
	 * Component constructor.
	 *
	 * @param id Name to identify this drop region to the drop client
	 * @param parent Drop client that this region belongs to
	 * @param source Component that shall be covered by this region.
	 * The bounds region will equals the component's bounds.
	 */
	public BasicDropRegion(Object id, InteractionClient parent, Component source)
	{
		this(id, parent, SwingUtilities.getLocalBounds(source), source);
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Drag&Drop support
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Imports the dragged data into our parent.
	 * @param data Data to import
	 * @param p Import position in glass coordinates
	 * @return
	 *		true	If the data could be imported.<br>
	 *		false	Otherwise
	 */
	public boolean importData(Transferable data, Point p)
	{
		return parent.importData(id, data, p);
	}

	/**
	 * BasicDropRegion objects can always import.
	 * @nowarn
	 */
	public boolean canImport()
	{
		return true;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Visual Support
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Draws the region with the given attributes.
	 * @param g Graphics context
	 */
	public void draw(Graphics2D g)
	{
		// Save graphics attributes
		Paint oldPaint = g.getPaint();

		if (paint != null)
		{
			// Draw the shape in the given color
			g.setPaint(paint);
			g.fill(shape);
		}

		if (frameColor != null)
		{
			// Draw the outlines
			if (stroke == null)
				stroke = new BasicStroke();

			Stroke oldStroke = g.getStroke();
			g.setStroke(stroke);
			g.setColor(frameColor);

			g.draw(shape);

			g.setStroke(oldStroke);
		}

		// Reset the attributes - paint and Color change the same attribute.
		g.setPaint(oldPaint);
	}

	/**
	 * Returns the bounding box of the region.
	 * @return The bounding box
	 */
	public Rectangle getBounds()
	{
		return shape.getBounds();
	}

	/**
	 * Checks if the given coordinates are within the shapes region.
	 * @param x Position in screen coordinates
	 * @param y Position in screen coordinates
	 */
	public boolean reactsOn(int x, int y)
	{
		return shape.contains(x, y);
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ DragDrop Events
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Do nothing, return true.
	 */
	public boolean dragEnter()
	{
		return false;
	}

	/**
	 * Do nothing, return true.
	 */
	public boolean dragExit()
	{
		return false;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Property access
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the name to identify this drop region to the drop client.
	 * @nowarn
	 */
	public Object getId()
	{
		return id;
	}

	/**
	 * Sets the name to identify this drop region to the drop client.
	 * @nowarn
	 */
	public void setId(Object id)
	{
		this.id = id;
	}

	/**
	 * Gets the drop client that this region belongs to.
	 * @nowarn
	 */
	public InteractionClient getParent()
	{
		return parent;
	}

	/**
	 * Sets the drop client that this region belongs to.
	 * @nowarn
	 */
	public void setParent(InteractionClient parent)
	{
		this.parent = parent;
	}

	/**
	 * Gets the visible shape of the region. Relative to drop client origin.
	 * @nowarn
	 */
	public RectangularShape getShape()
	{
		return shape;
	}

	/**
	 * Sets the visible shape of the region. Relative to drop client origin.
	 * @nowarn
	 */
	public void setShape(RectangularShape shape)
	{
		this.shape = shape;
	}

	/**
	 * Gets the color of the frame of the region. May be null.
	 * @nowarn
	 */
	public Color getFrameColor()
	{
		return frameColor;
	}

	/**
	 * Sets the color of the frame of the region. May be null.
	 * @nowarn
	 */
	public void setFrameColor(Color frameColor)
	{
		this.frameColor = frameColor;
	}

	/**
	 * Gets the stroke for the frame. May be null.
	 * @nowarn
	 */
	public Stroke getStroke()
	{
		return stroke;
	}

	/**
	 * Sets the stroke for the frame. May be null.
	 * @nowarn
	 */
	public void setStroke(Stroke stroke)
	{
		this.stroke = stroke;
	}

	/**
	 * Gets the fill paint for the region.
	 * @nowarn
	 */
	public Paint getPaint()
	{
		return paint;
	}

	/**
	 * Sets the fill paint for the region.
	 * @nowarn
	 */
	public void setPaint(Paint paint)
	{
		this.paint = paint;
	}

	/**
	 * Gets the cursor prototype that should be used for constructing a cursor if a drag over occurs.
	 * @nowarn
	 */
	public CursorPrototype getCursor()
	{
		return cursor;
	}

	/**
	 * Sets the cursor prototype that should be used for constructing a cursor if a drag over occurs.
	 * @nowarn
	 */
	public void setCursor(CursorPrototype cursor)
	{
		this.cursor = cursor;
	}

	/**
	 * Gets the overlay shape for this region.
	 * @nowarn
	 */
	public Shape getOverlay()
	{
		return overlay;
	}

	/**
	 * Sets the overlay shape for this region.
	 * @nowarn
	 */
	public void setOverlay(Shape overlay)
	{
		this.overlay = overlay;
	}

	/**
	 * Gets the origin to which the coordinates are relative.
	 * @nowarn
	 */
	public Component getOrigin()
	{
		return origin;
	}

	/**
	 * Sets the origin to which the coordinates are relative.
	 * @nowarn
	 */
	public void setOrigin(Component origin)
	{
		this.origin = origin;
	}

	/**
	 * Gets the text of the Tooltip that should be displayed for this region.
	 * @nowarn
	 */
	public String getToolTipText()
	{
		return toolTipText;
	}

	/**
	 * Sets the text of the Tooltip that should be displayed for this region.
	 * @nowarn
	 */
	public void setToolTipText(String toolTipText)
	{
		this.toolTipText = toolTipText;
	}
}
