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

/**
 * Extended colorizable.
 *
 * @author Heiko Erhardt
 */
public interface XColorizable
	extends Colorizable
{
	/**
	 * Gets the second (optional) fill color for gradients.
	 * @return Gradient color or null for solid fill
	 */
	public Color getFillColor2();

	/**
	 * Sets the second (optional) fill color for gradients.
	 * @param fillColor2 Gradient color or null for solid fill
	 */
	public void setFillColor2(Color fillColor2);

	/**
	 * Gets the default second (optional) fill color for gradients.
	 * @return Default gradient color or null for solid fill
	 */
	public Color getDefaultFillColor2();
}
