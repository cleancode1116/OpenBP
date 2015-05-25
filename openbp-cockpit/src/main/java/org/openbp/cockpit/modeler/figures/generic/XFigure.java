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
package org.openbp.cockpit.modeler.figures.generic;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;

import javax.swing.ImageIcon;

import org.openbp.cockpit.modeler.util.FigureResources;
import org.openbp.common.CommonUtil;
import org.openbp.swing.draw.RadialGradientPaint;

/**
 * Extended figure.
 *
 * @author Heiko Erhardt
 */
public abstract class XFigure extends BasicFigure
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Decoration key for the frame stroke (used with Stroke objects) */
	public static final String DECO_FRAMESTROKE = "Figure.FrameStroke";

	/** Decoration key for the frame color (used with Color objects) */
	public static final String DECO_FRAMECOLOR = "Figure.FrameColor";

	/** Decoration key for the second fill color (used with Color objects) */
	public static final String DECO_FILLCOLOR2 = "Figure.FillColor2";

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Orientation of the figure */
	private Orientation orientation;

	/** Frame stroke */
	private transient Stroke frameStroke;

	/** Frame color */
	private transient Color frameColor;

	/** Second (optional) fill color for gradients */
	private transient Color fillColor2;

	/** Optional image */
	private transient ImageIcon imageIcon;

	/////////////////////////////////////////////////////////////////////////
	// @@ Data members
	/////////////////////////////////////////////////////////////////////////

	/** Figure descriptor */
	protected transient XFigureDescriptor descriptor;

	/** Paint used to fill the figure */
	protected transient Paint paint;

	/** Flag if the paint should be updated */
	protected transient boolean needUpdatePaint;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public XFigure()
	{
		// Default: Top-down orientation
		orientation = Orientation.BOTTOM;

		// 1 px stroke
		frameStroke = FigureResources.standardStroke1;

		requestPaintUpdate();
	}

	/**
	 * Creates a clone of this object.
	 * @nowarn
	 */
	public Object clone()
	{
		XFigure c = (XFigure) super.clone();

		c.orientation = orientation;
		c.frameStroke = frameStroke;
		c.frameColor = frameColor;
		c.fillColor2 = fillColor2;
		c.imageIcon = imageIcon;
		c.descriptor = descriptor;

		c.requestPaintUpdate();

		return c;
	}

	/**
	 * Gets the figure descriptor.
	 * @nowarn
	 */
	public XFigureDescriptor getDescriptor()
	{
		return descriptor;
	}

	/**
	 * Sets the figure descriptor.
	 * @nowarn
	 */
	public void setDescriptor(XFigureDescriptor descriptor)
	{
		this.descriptor = descriptor;
	}

	/**
	 * Initializes the figure after all information from the figure descriptor has been set (template method).
	 * Can be used to parse tagged values of the figure descriptor.
	 * The default implementation does nothing.
	 */
	public void initialize()
	{
	}

	//////////////////////////////////////////////////
	// @@ Drawing
	//////////////////////////////////////////////////

	/**
	 * Draws the figure in the given graphics.
	 * Template method calling {@link BasicFigure#drawFigure} followed by {@link #drawFrame} and {@link #drawImage}.
	 *
	 * @param g Graphics to draw to
	 * @see org.openbp.cockpit.modeler.figures.generic.BasicFigure#draw(Graphics g)
	 */
	public void draw(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;

		// Determine the background paint; filter it through the decoration manager
		if (needUpdatePaint)
		{
			updatePaint();
		}
		if (paint != null)
		{
			Paint oldPaint = g2.getPaint();
			g2.setPaint(paint);

			drawFigure(g2);

			g2.setPaint(oldPaint);
		}

		Color c = getFrameColor();
		if (c == null)
			c = getDefaultFrameColor();
		c = (Color) decorateValue(DECO_FRAMECOLOR, c);

		if (c != null)
		{
			Stroke stroke = (Stroke) decorateValue(DECO_FRAMESTROKE, getFrameStroke());
			if (stroke != null)
			{
				Color oldColor = g2.getColor();
				g2.setColor(c);

				Stroke oldStroke = g2.getStroke();
				g2.setStroke(stroke);

				drawFrame(g2);

				drawAppliances(g2);

				g2.setStroke(oldStroke);
				g2.setColor(oldColor);
			}
		}

		drawImage(g2);
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#changed ()
	 */
	public void changed()
	{
		super.changed();

		// Update the paint in case of a gradient when changing sizes
		requestPaintUpdate();
	}

	/**
	 * Draws the frame of the figure.
	 *
	 * @param g Graphics to draw to
	 */
	protected void drawFrame(Graphics g)
	{
	}

	/**
	 * Draws additional frame parts.
	 *
	 * @param g Graphics to draw to
	 */
	protected void drawAppliances(Graphics g)
	{
	}

	/**
	 * Draws the image of the figure.
	 * By default, the image will be drawn according to the orientation of the figure.
	 *
	 * @param g Graphics to draw to
	 */
	protected void drawImage(Graphics g)
	{
		if (imageIcon == null)
			return;

		// Mirror the image according to its orientation
		Graphics2D g2 = (Graphics2D) g;

		Rectangle db = displayBox();

		int x = db.x;
		int y = db.y;
		int xScale = 1;
		int yScale = 1;

		switch (orientation)
		{
		case LEFT:
			// Flip vertically
			x = -x - db.width;
			xScale = -1;
			break;

		case TOP:
			// Flip horizontally
			y = -y - db.height;
			yScale = -1;
			break;
		}

		if (xScale != 1 || yScale != 1)
		{
			g2.scale(xScale, yScale);
		}

		g.drawImage(imageIcon.getImage(), x, y, db.width, db.height, imageIcon.getImageObserver());

		if (xScale != 1 || yScale != 1)
		{
			// Reset scale
			g2.scale(xScale, yScale);
		}
	}

	//////////////////////////////////////////////////
	// @@ Colors and gradients
	//////////////////////////////////////////////////

	/**
	 * Requests an update of the figure's paint.
	 */
	public void requestPaintUpdate()
	{
		needUpdatePaint = true;
	}

	/**
	 * Updates the paint object used for filling the figure.
	 * Should be called when the fill colors change (done by the {@link #setFillColor} or {@link #setFillColor2} methods)
	 * or when the size or orientation of the figure changes.
	 */
	public void updatePaint()
	{
		Color c1 = getFillColor();
		if (c1 == null)
			c1 = getDefaultFillColor();
		c1 = (Color) decorateValue(DECO_FILLCOLOR, c1);

		if (c1 != null)
		{
			Color c2 = getFillColor2();
			if (c2 == null)
				c2 = getDefaultFillColor2();
			c2 = (Color) decorateValue(DECO_FILLCOLOR2, c2);

			if (c2 != null && !c1.equals(c2))
			{
				// Determine the gradient position according to the orientation of the figure
				Rectangle r = displayBox();
				Orientation orientation = getOrientation();

				// Try to get the gradient settings from the descriptor;
				// if no descriptor is available, assume a cylcic gradient originating from the center of the figure
				int gradientPos1 = descriptor != null ? descriptor.getGradientPos1() : XFigureDescriptor.GRADIENTPOS_CENTER;
				int gradientPos2 = descriptor != null ? descriptor.getGradientPos2() : XFigureDescriptor.GRADIENTPOS_S;
				boolean cyclic = descriptor != null ? descriptor.isCyclicGradient() : true;

				Point2D p1 = determineGradientPoint(gradientPos1, orientation, r);
				Point2D p2 = determineGradientPoint(gradientPos2, orientation, r);

				if (cyclic)
				{
					// Create a radial gradient
					Point2D radius = new Point2D.Double(p2.getX() - p1.getX(), p2.getY() - p1.getY());
					paint = new RadialGradientPaint(p1, c1, radius, c2);
				}
				else
				{
					// Create a linear gradient
					paint = new GradientPaint(p1, c1, p2, c2, true);
				}
			}
			else
			{
				paint = c1;
			}
		}
		else
		{
			paint = null;
		}

		needUpdatePaint = false;
	}

	/**
	 * Determines a gradient start/end point according to the given gradient position
	 * and the bounding rectangle of the figure.
	 *
	 * @param gradientPos Gradient position
	 * @param orientation Figure orientation
	 * @param r Display box of the figure
	 * @return The point or null
	 */
	private Point2D determineGradientPoint(int gradientPos, Orientation orientation, Rectangle r)
	{
		// Adjust the gradient position according to the orientation of the figure
		int offset = 0;
		switch (orientation)
		{
		case BOTTOM:
			offset = 0;
			break;

		case LEFT:
			offset = 2;
			break;

		case TOP:
			offset = 4;
			break;

		case RIGHT:
			offset = 6;
			break;
		}
		gradientPos = (gradientPos + offset) % 8;

		// Determine the point according to the gradient position
		double x;
		double y;

		switch (gradientPos)
		{
		case XFigureDescriptor.GRADIENTPOS_E:
			x = r.getMinX();
			y = r.getCenterY();
			break;

		case XFigureDescriptor.GRADIENTPOS_SE:
			x = r.getMinX();
			y = r.getMaxY();
			break;

		case XFigureDescriptor.GRADIENTPOS_S:
			x = r.getCenterX();
			y = r.getMaxY();
			break;

		case XFigureDescriptor.GRADIENTPOS_SW:
			x = r.getMaxX();
			y = r.getMaxY();
			break;

		case XFigureDescriptor.GRADIENTPOS_W:
			x = r.getMaxX();
			y = r.getCenterY();
			break;

		case XFigureDescriptor.GRADIENTPOS_NW:
			x = r.getMaxX();
			y = r.getMinY();
			break;

		case XFigureDescriptor.GRADIENTPOS_N:
			x = r.getCenterX();
			y = r.getMinY();
			break;

		case XFigureDescriptor.GRADIENTPOS_NE:
			x = r.getMinX();
			y = r.getMinY();
			break;

		default:
			x = r.getCenterX();
			y = r.getCenterY();
			break;
		}

		return new Point2D.Float((float) x, (float) y);
	}

	//////////////////////////////////////////////////
	// @@ Geometry
	//////////////////////////////////////////////////

	/**
	 * Places the given rectangle so that its center is in the given direction
	 * and it exactly touches the figure, without crossing any lines.
	 * @param rect Rectangle to adjust
	 * @param angle Direction in which the rectangle should be placed
	 * @return The translated rectangle
	 */
	public abstract Rectangle placeAdjacent(Rectangle rect, double angle);

	/**
	 * Creates a shape object that defines the outline of the figure.
	 * The shape returned by this method is supposed to be more acurate than the one returned by {@link #createRectangularShape}.
	 * However, for most figure types it will be the same.
	 *
	 * @return The shape or null if the figure does not support shape creation
	 */
	public Shape createShape()
	{
		return null;
	}

	/**
	 * Creates a rectangular shape object that defines the outline of the figure.
	 * The shape returned by this method might be more inacurate than the one returned by {@link #createShape}, but it is supposed to
	 * be faster and it returns an easily translatable and scalable object.
	 *
	 * @return The shape or null if the figure does not support shape creation
	 */
	public RectangularShape createRectangularShape()
	{
		return null;
	}

	//////////////////////////////////////////////////
	// @@ XColorizable implementation
	//////////////////////////////////////////////////

	/**
	 * Sets the fill color.
	 * @param fillColor Fill color or null for no fill color
	 */
	public void setFillColor(Color fillColor)
	{
		if (!CommonUtil.equalsNull(getFillColor(), fillColor))
		{
			super.setFillColor(fillColor);
			requestPaintUpdate();
		}
	}

	/**
	 * Gets the default fill color.
	 * @return Default fill color or null for no fill color
	 */
	public Color getDefaultFillColor()
	{
		return descriptor != null ? descriptor.getFillColor() : null;
	}

	/**
	 * Gets the second (optional) fill color for gradients.
	 * @return Gradient color or null for solid fill
	 */
	public Color getFillColor2()
	{
		return fillColor2;
	}

	/**
	 * Sets the second (optional) fill color for gradients.
	 * @param fillColor2 Gradient color or null for solid fill
	 */
	public void setFillColor2(Color fillColor2)
	{
		if (!CommonUtil.equalsNull(this.fillColor2, fillColor2))
		{
			this.fillColor2 = fillColor2;
			requestPaintUpdate();
		}
	}

	/**
	 * Gets the default second (optional) fill color for gradients.
	 * @return Default gradient color or null for solid fill
	 */
	public Color getDefaultFillColor2()
	{
		return descriptor != null ? descriptor.getFillColor2() : null;
	}

	/**
	 * Gets the frame color.
	 * @return The frame color or null for no frame
	 */
	public Color getFrameColor()
	{
		return frameColor;
	}

	/**
	 * Sets the frame color.
	 * @param frameColor The frame color or null for no frame
	 */
	public void setFrameColor(Color frameColor)
	{
		this.frameColor = frameColor;
	}

	/**
	 * Gets the default frame color.
	 * @return Default frame color or null for no frame color
	 */
	public Color getDefaultFrameColor()
	{
		return descriptor != null ? descriptor.getFrameColor() : null;
	}

	/**
	 * Gets the optional image.
	 * @nowarn
	 */
	public ImageIcon getImageIcon()
	{
		return imageIcon;
	}

	/**
	 * Sets the optional image.
	 * @nowarn
	 */
	public void setImageIcon(ImageIcon imageIcon)
	{
		this.imageIcon = imageIcon;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the orientation of the figure.
	 * @return {@link Orientation#RIGHT}/{@link Orientation#BOTTOM}/{@link Orientation#LEFT}/{@link Orientation#TOP}
	 */
	public Orientation getOrientation()
	{
		return orientation;
	}

	/**
	 * Sets the orientation of the figure.
	 * @param orientation {@link Orientation#RIGHT}/{@link Orientation#BOTTOM}/{@link Orientation#LEFT}/{@link Orientation#TOP}
	 */
	public void setOrientation(Orientation orientation)
	{
		if (this.orientation != orientation)
		{
			this.orientation = orientation;
			requestPaintUpdate();
		}
	}

	/**
	 * Gets the frame stroke.
	 * @nowarn
	 */
	public Stroke getFrameStroke()
	{
		return frameStroke;
	}

	/**
	 * Sets the frame stroke.
	 * @nowarn
	 */
	public void setFrameStroke(Stroke frameStroke)
	{
		this.frameStroke = frameStroke;
	}
}
