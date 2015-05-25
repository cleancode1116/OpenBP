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
package org.openbp.swing.plaf.sky;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Some usefull methods for shadow and 3D effects.
 *
 * @author Jens Ferchland
 */
public class SkyUtil
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** The default radius in the Sky LookAndFeel package */
	public static final int RADIUS = 4;

	/** Max. shadow depth in the Sky  LookAndFeel package */
	public static final int MAXSHADOWDEPTH = 4;

	/** The default shadow depth in the Sky  LookAndFeel package */
	public static final int DEFAULTSHADOWDEPTH = 4;

	/** Shadow color */
	private static final Color SHADOWCOLOR = new Color(0, 0, 0, 20);

	/** Maximum shadow alpha channel value */
	private static final double MAXSHADOWALPHA = 100d;

	/** Maximum light alpha channel value */
	private static final double MAXLIGHTALPHA = 100d;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Helper class for shadow smoothing */
	private static Color [] shadowColorArray;

	/** Helper class for light smoothing */
	private static Color [] lightColorArray;

	//////////////////////////////////////////////////
	// @@ Rectangle drawing
	//////////////////////////////////////////////////

	/**
	 * Draw a shadow shaped as a RoundRect with smooth edges and a depth of 5.
	 *
	 * @param g a <code>Graphics</code> value
	 * @param radius an <code>int</code> value
	 * @nowarn
	 */
	public static void paintRoundRectShadow(Graphics g, int x, int y, int width, int height, int radius)
	{
		paintRoundRectShadow(g, x, y, width, height, radius, 5);
	}

	/**
	 * Draw a shadow shaped as a RoundRect with smooth edges.
	 *
	 * @param g a <code>Graphics</code> value
	 * @param radius an <code>int</code> value
	 * @param shadowsteps an <code>int</code> value
	 * @nowarn
	 */
	public static void paintRoundRectShadow(Graphics g, int x, int y, int width, int height, int radius, int shadowsteps)
	{
		// This method draw a shadow by painting several RoundRectangles.
		// The shadow color has got a alpha channel to multiply the
		// areas of the ROundRectangle
		// Clipping is used to increase the processing time

		Shape clip = g.getClip();

		int smooth = (int) Math.min((double) width, (double) height) / 2;
		if (smooth > shadowsteps)
			smooth = shadowsteps;

		// calculate clip for shadow
		Area area = new Area(new RoundRectangle2D.Double(x, y, width, height, RADIUS, RADIUS));

		area.subtract(new Area(new RoundRectangle2D.Double(x, y, width - smooth, height - smooth, radius, radius)));

		// Be sure we don't paint in a foreign area.
		area.intersect(new Area(clip.getBounds()));

		g.setClip(area);

		g.setColor(SHADOWCOLOR);

		// TODO Optimize: This is pretty slow... fillRoundRect is called 5 times each. Is there a better solution?
		// Paint parts of shadow
		for (int i = 0; i < smooth; i++)
		{
			g.fillRoundRect(x + i, y + i, width - (2 * i), height - (2 * i), (radius - i < 0) ? 0 : radius - i, (radius - i < 0) ? 0 : radius - i);
		}

		// set old clip
		g.setClip(clip);
	}

	/**
	 * Draw a shadow shaped as a Rectangle with smooth edges with a depth of 5 if possible.
	 *
	 * @param g a <code>Graphics</code> value
	 * @nowarn
	 */
	public static void paintRectShadow(Graphics g, int x, int y, int width, int height)
	{
		paintRectShadow(g, x, y, width, height, 5);
	}

	/**
	 * Draw a shadow shaped as a Rectangle with smooth edges and a depth of shadowsteps if possible.
	 *
	 * @param g a <code>Graphics</code> value
	 * @param shadowsteps an <code>int</code> value
	 * @nowarn
	 */
	public static void paintRectShadow(Graphics g, int x, int y, int width, int height, int shadowsteps)
	{
		Shape clip = g.getClip();

		int smooth = (int) Math.min((double) width, (double) height) / 2;
		if (smooth > shadowsteps)
			smooth = shadowsteps;

		// calculate clip for shadow
		Area area = new Area(new RoundRectangle2D.Double(x, y, width, height, RADIUS, RADIUS));

		area.subtract(new Area(new Rectangle(x, y, width - smooth, height - smooth)));

		// Be sure we don't paint in a foreign area.
		area.intersect(new Area(clip.getBounds()));

		g.setClip(area);

		g.setColor(SHADOWCOLOR);

		for (int i = 0; i < smooth; i++)
		{
			g.fillRoundRect(x + i, y + i, (width - (2 * i)), (height - (2 * i)), (RADIUS - i < 0) ? 0 : RADIUS - i, (RADIUS - i < 0) ? 0 : RADIUS - i);
		}

		// set old clip
		g.setClip(clip);
	}

	/**
	 * Paint a 3D effect to a RoundRect.
	 *
	 * @param radius an <code>int</code> value
	 * @nowarn
	 */
	public static void paint3DRoundRectEffect(Graphics g, int x, int y, int width, int height, int radius)
	{
		paint3DRoundRectEffect(g, x, y, width, height, radius, 5);
	}

	/**
	 * Paint a 3D effect to a RoundRect.
	 *
	 * @param radius an <code>int</code> value
	 * @param depth an <code>int</code> value
	 * @nowarn
	 */
	public static void paint3DRoundRectEffect(Graphics g, int x, int y, int width, int height, int radius, int depth)
	{
		if (shadowColorArray == null)
			createArrays();

		if (!(g instanceof Graphics2D))
			return;

		int smooth = Math.min(width, height) / 2;
		if (smooth > depth)
			smooth = depth;

		for (int i = 0; i < smooth; i++)
		{
			g.setColor(shadowColorArray [(int) (MAXSHADOWALPHA / smooth * (smooth - i)) - 1]);

			//south of the RoundRect
			g.drawLine(x + radius + 1, y + height - i, x + width - radius - 1, y + height - i);

			// area in th east of the RoundRect
			g.drawLine(x + width - i, y + radius + 1, x + width - i, y + height - radius - 1);

			// dark part of the left corner on the bottom
			g.drawArc(x + i, y + height - 2 * radius + i, 2 * (radius - i), 2 * (radius - i), -90, -45);

			// draw arc area in the right corner of the bottom
			g.drawArc(x + width - 2 * radius + i, y + height - 2 * radius + i, 2 * (radius - i), 2 * (radius - i), 0, -90);

			// dark part of the right corner on the top
			g.drawArc(x + width - 2 * radius + i, y + i, 2 * (radius - i), 2 * (radius - i), 45, -45);

			g.setColor(lightColorArray [(int) (MAXLIGHTALPHA / smooth * i)]);

			// area in the north of the RoundRect
			g.drawLine(x + radius + 1, y + i, x + width - radius - 1, y + i);

			//area in the west of the RoundRect
			g.drawLine(x + i, y + radius + 1, x + i, y + height - radius - 1);

			// light part of the right corner on the top
			g.drawArc(x + width - 2 * radius + i, y + i, 2 * (radius - i), 2 * (radius - i), 90, -45);

			// left corner on the top
			g.drawArc(x + i, y + i, 2 * (radius - i), 2 * (radius - i), 90, 90);

			// light part of the left corner on the bottom
			g.drawArc(x + i, y + height - 2 * radius + i, 2 * (radius - i), 2 * (radius - i), 180, 45);
		}
	}

	/**
	 * Paint a 3D effect to a Rectangle
	 *
	 * @param graphics a <code>Graphics</code> value
	 * @nowarn
	 */
	public static void paint3DRectEffect(Graphics graphics, int x, int y, int width, int height)
	{
		paint3DRectEffect(graphics, x, y, width, height, 5);
	}

	/**
	 * Paint a 3D effect to a Rectangle.
	 *
	 * @param graphics a <code>Graphics</code> value
	 * @param depth an <code>int</code> value
	 * @nowarn
	 */
	public static void paint3DRectEffect(Graphics graphics, int x, int y, int width, int height, int depth)
	{
		if (shadowColorArray == null)
			createArrays();

		if (!(graphics instanceof Graphics2D))
			return;

		Graphics2D g = (Graphics2D) graphics;

		int smooth = (int) Math.min((double) width, (double) height) / 2;
		if (smooth > depth)
			smooth = depth;

		for (int i = 0; i < smooth; i++)
		{
			g.setColor(shadowColorArray [(int) (MAXSHADOWALPHA / smooth * (smooth - i)) - 1]);

			//south of the Rectangle
			g.drawLine(x + i + 1, y + height - i, x + width - i - 1, y + height - i);

			// area in th east of the Rectangle
			g.drawLine(x + width - i, y + i, x + width - i, y + height - i);

			g.setColor(lightColorArray [(int) (MAXLIGHTALPHA / smooth * i)]);

			// area in the north of the Rectangle
			g.drawLine(x + i + 1, y + i, x + width - i - 1, y + i);

			//area in the west of the Rectangle
			g.drawLine(x + i, y + i, x + i, y + height - i);
		}
	}

	private static void createArrays()
	{
		shadowColorArray = new Color [100];
		lightColorArray = new Color [100];

		for (int i = 0; i < 100; i++)
		{
			shadowColorArray [i] = new Color(0, 0, 0, i);
			lightColorArray [i] = new Color(255, 255, 255, i);
		}
	}

	//////////////////////////////////////////////////
	// @@ Icons
	//////////////////////////////////////////////////

	/**
	 * Creates a 'disabled' version of an icon.
	 *
	 * @param icon Icon to convert
	 * @return The 'grayed' version of the icon
	 */
	public static Icon createGrayIcon(Icon icon)
	{
		Icon disabledIcon = null;

		if (icon instanceof ImageIcon)
			disabledIcon = new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) icon).getImage()));

		return (disabledIcon != null) ? disabledIcon : icon;
	}
}
