/*
 *   Copyright 2010 skynamics AG
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
import java.text.DecimalFormat;
import java.util.StringTokenizer;

/**
 * Utility methods for geometry data encoding/decoding.
 *
 * @author Stephan Pauxberger
 */
public final class GeometryUtil
{
	// TODO Cleanup 3 Exception handling for geometry data parser

	/**
	 * Private constructor prevents instantiation.
	 */
	private GeometryUtil()
	{
	}


	/**
	 * Parses an integer value.
	 *
	 * @param st Tokenizer providing the geometry parameter stream
	 * @param paramName Name of the current parameter
	 * @param objectName Name of the process object that owns the parameter
	 * @return The integer
	 */
	public static int parseInt(StringTokenizer st, String paramName, String objectName)
	{
		if (! st.hasMoreTokens())
			return 0;
		String s = st.nextToken();
		return Integer.parseInt(s);
	}

	/**
	 * Parses a RGB color value.
	 *
	 * @param st Tokenizer providing the geometry parameter stream
	 * @param paramName Name of the current parameter
	 * @param objectName Name of the process object that owns the parameter
	 * @return The colro
	 */
	public static Color parseColor(StringTokenizer st, String paramName, String objectName)
	{
		int r = parseInt(st, paramName, objectName);
		int g = parseInt(st, paramName, objectName);
		int b = parseInt(st, paramName, objectName);
		return new Color(r, g, b);
	}

	/**
	 * Parses an angle value.
	 *
	 * @param st Tokenizer providing the geometry parameter stream
	 * @param paramName Name of the current parameter
	 * @param objectName Name of the process object that owns the parameter
	 * @return The color
	 */
	public static double parseAngle(StringTokenizer st, String paramName, String objectName)
	{
		if (! st.hasMoreTokens())
			return 0d;
		String s = st.nextToken();
		if (s.length() == 0)
			return 0d;

		double angle;
		if (s.charAt(0) == 'd')
		{
			// Degree
			double degree = Double.parseDouble(s.substring(1));
			angle = Math.toRadians(degree);
		}
		else
		{
			angle = Double.parseDouble(s);
		}
		return angle;
	}

	private static DecimalFormat angleFormat = new DecimalFormat("#.##");

	/**
	 * Parses an angle value.
	 *
	 * @param angle Angle in radians
	 * @return The angle as degree specification
	 */
	public static String printAngle(double angle)
	{
		double degree = Math.toDegrees(angle);
		return "d" + angleFormat.format(degree);
	}
}
