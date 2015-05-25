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
package org.openbp.awt;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Utility methods that convert a color to its string representation and vice versa.
 * The string representation consists of a colon-separated list of RGB and alpha (transparency) values.
 *
 * @author Heiko Erhardt
 */
public final class Color2StringConverter
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Color value separator */
	public static final String SEPARATOR = ":";

	/** Prefined color list (contains {@link PredefinedColor} objects) */
	private static List predefinedColorList = new ArrayList();

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private Color2StringConverter()
	{
	}

	static
	{
		addPredefinedColor("white", Color.white);
		addPredefinedColor("lightGray", Color.lightGray);
		addPredefinedColor("gray", Color.gray);
		addPredefinedColor("darkGray", Color.darkGray);
		addPredefinedColor("black", Color.black);
		addPredefinedColor("red", Color.red);
		addPredefinedColor("pink", Color.pink);
		addPredefinedColor("orange", Color.orange);
		addPredefinedColor("yellow", Color.yellow);
		addPredefinedColor("green", Color.green);
		addPredefinedColor("magenta", Color.magenta);
		addPredefinedColor("cyan", Color.cyan);
		addPredefinedColor("blue", Color.blue);
	}

	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	/**
	 * Adds a predefined color.
	 *
	 * @param name Name of the color
	 * @param color Color value
	 */
	public static void addPredefinedColor(String name, Color color)
	{
		predefinedColorList.add(new PredefinedColor(name, color));
	}

	/**
	 * Converts a color to its string representation.
	 *
	 * @param color Color or null
	 * @return The color string or null if color is null
	 */
	public static String color2Str(Color color)
	{
		if (color == null)
			return null;

		// First check the table of predefined colors
		int n = predefinedColorList.size();
		for (int i = 0; i < n; ++i)
		{
			PredefinedColor pdc = (PredefinedColor) predefinedColorList.get(i);
			if (pdc.color.equals(color))
				return pdc.name;
		}

		// Special color, encode RGBA values
		StringBuffer sb = new StringBuffer();
		sb.append(color.getRed());
		sb.append(SEPARATOR);
		sb.append(color.getGreen());
		sb.append(SEPARATOR);
		sb.append(color.getBlue());
		if (color.getAlpha() != 0xff)
		{
			sb.append(SEPARATOR);
			sb.append(color.getAlpha());
		}
		return sb.toString();
	}

	/**
	 * Converts a string representation to a color.
	 *
	 * @param s String representation or null
	 * @return The color or null if s is null
	 */
	public static Color str2Color(String s)
	{
		if (s == null)
			return null;

		// First check the table of predefined colors
		int n = predefinedColorList.size();
		for (int i = 0; i < n; ++i)
		{
			PredefinedColor pdc = (PredefinedColor) predefinedColorList.get(i);
			if (pdc.name.equals(s))
				return pdc.color;
		}

		// Special color, decode RGBA values
		StringTokenizer st = new StringTokenizer(s, SEPARATOR);
		int red = getColorInt(st, 0);
		int green = getColorInt(st, 0);
		int blue = getColorInt(st, 0);
		int alpha = getColorInt(st, 0xff);
		return new Color(red, green, blue, alpha);
	}

	private static int getColorInt(StringTokenizer st, int dflt)
	{
		if (st.hasMoreTokens())
		{
			try
			{
				return Integer.valueOf(st.nextToken()).intValue();
			}
			catch (NumberFormatException e)
			{
			}
		}

		return dflt;
	}

	/**
	 * Object desribing a pre-defined color
	 */
	private static class PredefinedColor
	{
		/** Name */
		public String name;

		/** Color */
		public Color color;

		/**
		 * Constructor.
		 *
		 * @param name Name
		 * @param color Color
		 */
		public PredefinedColor(String name, Color color)
		{
			this.name = name;
			this.color = color;
		}
	}
}
