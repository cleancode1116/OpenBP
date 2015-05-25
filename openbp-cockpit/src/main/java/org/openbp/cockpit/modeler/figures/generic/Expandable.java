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

import java.awt.Rectangle;

/**
 * An expandable figure has sub figures that are attached to the figure.
 * This interface defines a single method that returns the 'basic' display box of the figure.
 *
 * @author Stephan Moritz
 */
public interface Expandable
{
	/**
	 * Returns the display box of the figure, excluding any appendices.
	 * @nowarn
	 */
	public Rectangle compactDisplayBox();
}
