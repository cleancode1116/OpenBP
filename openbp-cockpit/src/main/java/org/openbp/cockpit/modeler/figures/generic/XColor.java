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
import java.awt.color.ColorSpace;

import org.openbp.awt.Color2StringConverter;

/**
 * X color.
 *
 * @author Heiko Erhardt
 */
public class XColor extends Color
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	// Inherited constructors

    public XColor(int r, int g, int b)
	{
		super(r, g, b);
	}

    public XColor(int r, int g, int b, int a)
	{
		super(r, g, b, a);
	}

    public XColor(int rgb)
	{
		super(rgb);
	}

    public XColor(int rgba, boolean hasalpha)
	{
		super(rgba, hasalpha);
	}

    public XColor(float r, float g, float b)
	{
		super(r, g, b);
	}

    public XColor(float r, float g, float b, float a)
	{
		super(r, g, b, a);
	}

    public XColor(ColorSpace cspace, float components[], float alpha)
	{
		super(cspace, components, alpha);
	}

	/**
	 * Default constructor.
	 *
	 * @param s String reprensentatin of the color
	 */
	public XColor(String s)
	{
		super(getRGB(s));
	}

	private static int getRGB(String s)
	{
		Color c = Color2StringConverter.str2Color(s);
		if (c != null)
		{
			return c.getRGB();
		}
		return 0;
	}
}
