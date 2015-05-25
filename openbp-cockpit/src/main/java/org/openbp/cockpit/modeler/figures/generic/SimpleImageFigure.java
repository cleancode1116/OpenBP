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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openbp.common.icon.MultiIcon;

import CH.ifa.draw.standard.AbstractFigure;
import CH.ifa.draw.standard.BoxHandleKit;

/**
 * A simple image figure.
 *
 * @author Heiko Erhardt
 */
public class SimpleImageFigure extends AbstractFigure
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Image */
	private transient Image image;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Display box */
	protected Rectangle box;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public SimpleImageFigure()
	{
		box = new Rectangle();
	}

	/**
	 * Default constructor.
	 *
	 * @param image Image
	 */
	public SimpleImageFigure(Image image)
	{
		this();
		setImage(image);
	}

	/**
	 * Default constructor.
	 *
	 * @param icon Icon
	 */
	public SimpleImageFigure(Icon icon)
	{
		this();
		setIcon(icon);
	}

	//////////////////////////////////////////////////
	// @@ AbstractFigure overrides
	//////////////////////////////////////////////////

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#basicDisplayBox(Point origin, Point corner)
	 */
	public void basicDisplayBox(Point origin, Point corner)
	{
		box = new Rectangle(origin);
		box.add(corner);
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#handles()
	 */
	public Vector handles()
	{
		Vector handles = new Vector();
		BoxHandleKit.addHandles(this, handles);
		return handles;
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#displayBox()
	 */
	public Rectangle displayBox()
	{
		return new Rectangle(box);
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#basicMoveBy(int x, int y)
	 */
	protected void basicMoveBy(int x, int y)
	{
		box.translate(x, y);
	}

	//////////////////////////////////////////////////
	// @@ Drawing
	//////////////////////////////////////////////////

	/**
	 * Draws the image in the given graphics.
	 *
	 * @param g Graphics to draw to
	 * @see CH.ifa.draw.standard.AbstractFigure#draw(Graphics g)
	 */
	public void draw(Graphics g)
	{
		if (image == null)
			return;

		g.drawImage(image, box.x, box.y, box.width, box.height, null);
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the image.
	 * @nowarn
	 */
	public Image getImage()
	{
		return image;
	}

	/**
	 * Sets the image.
	 * @nowarn
	 */
	public void setImage(Image image)
	{
		this.image = image;
	}

	/**
	 * Sets the icon.
	 * @nowarn
	 */
	public void setIcon(Icon icon)
	{
		if (icon instanceof ImageIcon)
		{
			image = ((ImageIcon) icon).getImage();
		}
		else if (icon instanceof MultiIcon)
		{
			image = ((MultiIcon) icon).getImage();
		}

		box.width = icon.getIconWidth();
		box.height = icon.getIconHeight();
	}
}
