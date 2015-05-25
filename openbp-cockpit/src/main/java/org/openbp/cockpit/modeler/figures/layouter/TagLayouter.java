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
package org.openbp.cockpit.modeler.figures.layouter;

import java.awt.Insets;
import java.awt.Rectangle;

/**
 * A direction-sensitive layouter.
 *
 * @author Stephan Moritz
 */
public interface TagLayouter
{
	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Calculates the dimensions of the figure by simulating the layout process.
	 *
	 * @return The new dimensions of the figure
	 */
	public Rectangle calculateSize();

	/**
	 * Performs the layout of the figure.
	 *
	 * @param box Desired display box of the figure
	 */
	public void performLayout(Rectangle box);

	/**
	 * Set the insets for spacing between the figure and its subfigures
	 *
	 * @param newInsets New spacing dimensions
	 */
	public void setInsets(Insets newInsets);

	/**
	 * Get the insets for spacing between the figure and its subfigures
	 *
	 * @return spacing dimensions
	 */
	public Insets getInsets();

	/**
	 * Sets the direction to which the tag currently faces and updates the layouter
	 * accordingly.
	 *
	 * @param direction One of the direction constants of this class
	 */
	public void setDirection(int direction);

	/**
	 * Determines the direction of the tag by the angle of the associated figure and
	 * updates the layouter accordingly.
	 */
	public void determineDirection();

	/**
	 * Checks if the orientation of the tag is vertical or horizontal.
	 * @nowarn
	 */
	public boolean isVerticalLayouter();
}
