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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.StringTokenizer;
import java.util.Vector;

import org.openbp.common.CollectionUtil;
import org.openbp.common.CommonUtil;
import org.openbp.swing.SwingUtil;

import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.Handle;

/**
 * The text element figure represents a simple text rectangle.
 * The size of the figure is computed automatically based on the text and font settings.
 *
 * @author Heiko Erhardt
 */
public class XTextFigure extends BasicFigure
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Constants
	/////////////////////////////////////////////////////////////////////////

	/** Distance between display box and font size handle */
	public static final int HANDLE_DISTANCE = 5;

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Text to display */
	private String text;

	/** Text font */
	private Font font;

	/** X origin */
	protected int originX;

	/** Y origin */
	protected int originY;

	/** Text alignment */
	private int alignment;

	/** Auto-size flag */
	private boolean autoSize;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Display box cache */
	protected Rectangle displayBoxCache;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public XTextFigure()
	{
		super();

		font = new Font("Helvetica", Font.PLAIN, 12);
	}

	//////////////////////////////////////////////////
	// @@ Figure overrides
	//////////////////////////////////////////////////

	protected void basicMoveBy(int x, int y)
	{
		originX += x;
		originY += y;

		// Update display box cache
		if (autoSize)
		{
			displayBoxCache = null;
		}
		else
		{
			if (displayBoxCache != null)
			{
				displayBoxCache.x += x;
				displayBoxCache.y += y;
			}
		}
	}

	/**
	 * We ignore the second argument because the figure will compute its size based on the text and font.
	 * @see CH.ifa.draw.figures.AttributeFigure#basicDisplayBox(Point newOrigin, Point newCorner)
	 */
	public void basicDisplayBox(Point newOrigin, Point newCorner)
	{
		originX = newOrigin.x;
		originY = newOrigin.y;

		// Update display box cache
		if (autoSize)
		{
			displayBoxCache = null;
		}
		else
		{
			displayBoxCache = new Rectangle(newOrigin.x, newOrigin.y, newCorner.x - newOrigin.x, newCorner.y - newOrigin.y);
		}
	}

	/**
	 * @see CH.ifa.draw.figures.AttributeFigure#displayBox()
	 */
	public Rectangle displayBox()
	{
		if (displayBoxCache == null)
		{
			displayBoxCache = new Rectangle(originX, originY, 0, 0);

			if (autoSize)
			{
				// Compute bounding box dimensions
				FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
				SwingUtil.computeMultilineStringBounds(fm, text, alignment, displayBoxCache);
			}
		}
		return new Rectangle(displayBoxCache);
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.generic.BasicFigure#drawFigure(Graphics g)
	 */
	protected void drawFigure(Graphics g)
	{
		g.setFont(font);

		Rectangle db = displayBox();
		SwingUtil.drawMultilineString(g, text, alignment, db, true);
	}

	/**
	 * Adds the offset for the handle distance to the invalidation area.
	 * @see CH.ifa.draw.standard.AbstractFigure#invalidate()
	 */
	public void invalidate()
	{
		if (listener() != null)
		{
			Rectangle r = displayBox();
			r.grow(Handle.HANDLESIZE + HANDLE_DISTANCE, Handle.HANDLESIZE + HANDLE_DISTANCE);
			listener().figureInvalidated(new FigureChangeEvent(this, r));
		}
	}

	/**
	 * We display a single font size handle in the top left corner only.
	 * @see CH.ifa.draw.standard.AbstractFigure#handles()
	 */
	public Vector handles()
	{
		return CollectionUtil.EMPTY_VECTOR;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the text to display.
	 * @nowarn
	 */
	public String getText()
	{
		return text;
	}

	/**
	 * Sets the text to display.
	 * @nowarn
	 */
	public void setText(String text)
	{
		if (!CommonUtil.equalsNull(text, this.text))
		{
			willChange();

			this.text = text;

			if (autoSize)
			{
				// Clear display box cache
				displayBoxCache = null;
			}

			changed();
		}
	}

	/**
	 * Gets the font size.
	 * @nowarn
	 */
	public int getFontSize()
	{
		Font font = getFont();
		return font.getSize();
	}

	/**
	 * Sets the font size.
	 * @nowarn
	 */
	public void setFontSize(int fontSize)
	{
		Font font = getFont();
		setFont(new Font(font.getName(), font.getStyle(), fontSize));
	}

	/**
	 * Gets the text font.
	 * @nowarn
	 */
	public Font getFont()
	{
		return font;
	}

	/**
	 * Sets the text font.
	 * @nowarn
	 */
	public void setFont(Font font)
	{
		willChange();

		this.font = font;

		if (autoSize)
		{
			// Clear display box cache
			displayBoxCache = null;
		}

		changed();
	}

	/**
	 * Gets the text alignment.
	 * @return Text alignment
	 */
	public int getAlignment()
	{
		return alignment;
	}

	/**
	 * Sets the text alignment.
	 * @param alignment Text alignment
	 */
	public void setAlignment(int alignment)
	{
		this.alignment = alignment;
	}

	/**
	 * Gets the auto-size flag.
	 * Auto-sizes the display box of the figure according to the text extent.
	 * @nowarn
	 */
	public boolean isAutoSize()
	{
		return autoSize;
	}

	/**
	 * Sets the auto-size flag.
	 * Auto-sizes the display box of the figure according to the text extent.
	 * @nowarn
	 */
	public void setAutoSize(boolean autoSize)
	{
		this.autoSize = autoSize;
	}

	//////////////////////////////////////////////////
	// @@ Geometry serialization support
	//////////////////////////////////////////////////

	/**
	 * Decodes the given geometry information by
	 * breaking it into single parameters and handing them to {@link #parseParameter}.
	 *
	 * @param geometry '|'-separated geometry information or null
	 */
	public void parseGeometry(String geometry)
	{
		if (geometry == null)
		{
			return;
		}

		StringTokenizer tok = new StringTokenizer(geometry, "|");
		while (tok.hasMoreTokens())
		{
			parseParameter(tok.nextToken());
		}
	}

	/**
	 * Decodes a single geometry parameter.
	 *
	 * @param parameter Parameter to decode
	 */
	protected void parseParameter(String parameter)
	{
		StringTokenizer inner = new StringTokenizer(parameter, ":");

		try
		{
			String paramName = inner.nextToken();

			if (paramName.equals("rect"))
			{
				int x = Integer.parseInt(inner.nextToken());
				int y = Integer.parseInt(inner.nextToken());
				int w = Integer.parseInt(inner.nextToken());
				int h = Integer.parseInt(inner.nextToken());

				Rectangle db = new Rectangle(x, y, w, h);
				displayBox(db);
			}
			else if (paramName.equals("fillcolor"))
			{
				int r = Integer.parseInt(inner.nextToken());
				int g = Integer.parseInt(inner.nextToken());
				int b = Integer.parseInt(inner.nextToken());

				setFillColor(new Color(r, g, b));
			}
			else if (paramName.equals("fontsize"))
			{
				int s = Integer.parseInt(inner.nextToken());
				setFontSize(s);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Encodes the geometry information of this object into a string.
	 *
	 * @return geometry '|'-sparamted geometry information or null
	 */
	public String createGeometry()
	{
		Rectangle db = displayBox();
		String geometry = "rect:" + db.x + ":" + db.y + ":" + db.width + ":" + db.height;

		Color c = getFillColor();
		if (c != null && !c.equals(getDefaultFillColor()))
		{
			geometry += "|fillcolor:" + c.getRed() + ":" + c.getGreen() + ":" + c.getBlue();
		}

		int fontSize = getFontSize();
		if (fontSize != 12)
		{
			geometry += "|fontsize:" + fontSize;
		}

		return geometry;
	}
}
