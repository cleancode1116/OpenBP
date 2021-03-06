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

import CH.ifa.draw.framework.Figure;

/**
 * A colorizable object can be assigned an arbitary fill color.
 *
 * @author Stephan Moritz
 */
public interface Colorizable
	extends Figure
{
	/**
	 * Gets the fill color.
	 * @return Fill color or null for no fill color
	 */
	public Color getFillColor();

	/**
	 * Sets the fill color.
	 * @param fillColor Fill color or null for no fill color
	 */
	public void setFillColor(Color fillColor);

	/**
	 * Gets the default fill color.
	 * @return Default fill color or null for no fill color
	 */
	public Color getDefaultFillColor();
}
