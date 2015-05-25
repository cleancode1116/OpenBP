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
import java.awt.Graphics;

import org.openbp.jaspira.decoration.DecorationMgr;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.standard.AbstractFigure;

/**
 * Extended figure.
 *
 * @author Heiko Erhardt
 */
public abstract class BasicFigure extends AbstractFigure
	implements ChildFigure, Colorizable
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Decoration key for the fill color (used with Color objects) */
	public static final String DECO_FILLCOLOR = "Figure.FillColor";

	/** Decoration key for overlay figures (used with Figure objects) */
	public static final String DECO_OVERLAY = "Figure.Overlay";

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Fill color */
	private Color fillColor;

	/////////////////////////////////////////////////////////////////////////
	// @@ Data members
	/////////////////////////////////////////////////////////////////////////

	/** Parent figure */
	private transient Figure parent;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public BasicFigure()
	{
	}

	/**
	 * Creates a clone of this object.
	 * @nowarn
	 */
	public Object clone()
	{
		BasicFigure c = (BasicFigure) super.clone();

		c.fillColor = fillColor;
		c.parent = parent;

		c.displayBox(displayBox());

		return c;
	}

	//////////////////////////////////////////////////
	// @@ Decoration
	//////////////////////////////////////////////////

	/**
	 * Determines the decoration for this figure using the given key.
	 * The method will use the {@link #getDecorationOwner} method to determine the owner of the figure.
	 *
	 * @param key Decoration key
	 * @param value Value used for the decoration
	 * @return The decorated value
	 */
	protected Object decorateValue(String key, Object value)
	{
		return DecorationMgr.decorate(getDecorationOwner(), key, value);
	}

	/**
	 * Returns the decoration owner.
	 *
	 * @return The parent of this figure (can be null)
	 */
	public Object getDecorationOwner()
	{
		return getParent();
	}

	//////////////////////////////////////////////////
	// @@ Drawing
	//////////////////////////////////////////////////

	/**
	 * Draws the figure in the given graphics.
	 * Template method calling {@link #drawFigure}.
	 *
	 * @param g Graphics to draw to
	 * @see CH.ifa.draw.standard.AbstractFigure#draw(Graphics g)
	 */
	public void draw(Graphics g)
	{
		// Determine the fill color; filter it through the decoration manager
		Color c = getFillColor();
		if (c == null)
			c = getDefaultFillColor();
		c = (Color) decorateValue(DECO_FILLCOLOR, c);
		if (c != null)
		{
			Color oldColor = g.getColor();
			g.setColor(c);

			drawFigure(g);

			g.setColor(oldColor);
		}
	}

	/**
	 * Draws the figure.
	 *
	 * @param g Graphics to draw to
	 */
	protected void drawFigure(Graphics g)
	{
	}

	//////////////////////////////////////////////////
	// @@ ChildFigure implementation
	//////////////////////////////////////////////////

	/**
	 * @see ChildFigure#getParent()
	 */
	public Figure getParent()
	{
		return parent;
	}

	/**
	 * @see ChildFigure#setParent(Figure)
	 */
	public void setParent(Figure parent)
	{
		this.parent = parent;
	}

	//////////////////////////////////////////////////
	// @@ Colorizable implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the fill color.
	 * @return Fill color or null for no fill color
	 */
	public Color getFillColor()
	{
		return fillColor;
	}

	/**
	 * Sets the fill color.
	 * @param fillColor Fill color or null for no fill color
	 */
	public void setFillColor(Color fillColor)
	{
		this.fillColor = fillColor;
	}

	/**
	 * Gets the default fill color.
	 * @return Default fill color or null for no fill color
	 */
	public Color getDefaultFillColor()
	{
		return Color.BLACK;
	}
}
