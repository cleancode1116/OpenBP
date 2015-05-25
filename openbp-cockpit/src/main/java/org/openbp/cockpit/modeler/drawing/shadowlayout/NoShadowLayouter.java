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
package org.openbp.cockpit.modeler.drawing.shadowlayout;

import java.awt.Graphics;
import java.awt.Rectangle;

import CH.ifa.draw.framework.FigureEnumeration;

/**
 * @author Stephan Moritz
 */
public class NoShadowLayouter
	implements ShadowLayouter
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public NoShadowLayouter()
	{
	}

	// implementation of org.openbp.cockpit.modeler.ShadowLayouter interface

	public void drawShadows(FigureEnumeration param1, Graphics param2)
	{
		// Doing a lot of things *G*
	}

	public Rectangle transformRectangle(Rectangle r)
	{
		return r;
	}

	public void releaseShadowLayouter()
	{
	}
}
