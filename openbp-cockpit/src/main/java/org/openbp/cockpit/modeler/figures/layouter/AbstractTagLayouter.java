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

import org.openbp.cockpit.modeler.figures.generic.CircleConstants;
import org.openbp.cockpit.modeler.figures.tag.AbstractTagFigure;

/**
 * A tag layouter is responsible for laying out the sub figures of a tag.
 *
 * If a center figure is given, the direction of the tag will be determined by checking if the tag angle crosses
 * the left, right, top or bottom lines of the center figure's display box.
 * Otherwise, the direction will be determined according to the circle section that is determined by the angle.
 * For square- or circle shaped figures, you don't need to provide a center figure.
 * For rectangular or elliptic figures, however, it is recommended.
 *
 * @author Stephan Moritz
 */
public abstract class AbstractTagLayouter
	implements TagLayouter
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/**
	 * Insets for each sub component.
	 * The insets define the distance between the sub components and the distance to the edge of the container.
	 */
	protected Insets insets;

	/** Direction to which the tag currently faces (one of the EIGHT_* direction constants of the {@link CircleConstants} class) */
	protected int direction = CircleConstants.EIGHTH_S;

	/** Tag figure to layout */
	protected AbstractTagFigure tagFigure;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param tagFigure Tag to lay out
	 */
	public AbstractTagLayouter(AbstractTagFigure tagFigure)
	{
		this.tagFigure = tagFigure;

		// Standard insets: 3 pixel vertical, 3 pixel horizontal
		insets = new Insets(3, 3, 3, 3);

		// Initialize sub layouters, if any
		setupLayouter();
	}

	/**
	 * Sets up the layouter.
	 * Sets up surrogate layouters and such.
	 * Does nothing by default.
	 */
	protected void setupLayouter()
	{
	}

	/**
	 * Initializes the layouter according to the current direction.
	 */
	public abstract void applyDirection();

	/**
	 * @return true; The vertical layouter is always vertically oriented
	 */
	public abstract boolean isVerticalLayouter();

	//////////////////////////////////////////////////
	// @@ Layouter implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the insets for each sub component.
	 * The insets define the distance between the sub components and the distance to the edge of the container.
	 * @nowarn
	 */
	public Insets getInsets()
	{
		return insets;
	}

	/**
	 * Sets the insets for each sub component.
	 * The insets define the distance between the sub components and the distance to the edge of the container.
	 * @nowarn
	 */
	public void setInsets(Insets insets)
	{
		this.insets = insets;
	}

	/**
	 * Sets the direction to which the tag currently faces and updates the layouter accordingly.
	 * @param direction One of the eighth direction constants of the {@link CircleConstants} class (EIGHTH_*)
	 */
	public void setDirection(int direction)
	{
		if (this.direction != direction)
		{
			this.direction = direction;
			applyDirection();
		}
	}

	/**
	 * Determines the direction of the tag by the angle of the associated figure and
	 * updates the layouter accordingly.
	 */
	public void determineDirection()
	{
		double angle = tagFigure.getAngle();
		Rectangle centerBox = tagFigure.getCenterFigureBox();

		int dir = CircleConstants.determineEighth(angle, centerBox);
		setDirection(dir);
	}

	static String dirstr(int dir)
	{
		switch (dir)
		{
		case CircleConstants.EIGHTH_E:
			return "E ";

		case CircleConstants.EIGHTH_SE:
			return "SE";

		case CircleConstants.EIGHTH_S:
			return "S ";

		case CircleConstants.EIGHTH_SW:
			return "SW";

		case CircleConstants.EIGHTH_W:
			return "W ";

		case CircleConstants.EIGHTH_NW:
			return "NW";

		case CircleConstants.EIGHTH_N:
			return "N ";

		case CircleConstants.EIGHTH_NE:
			return "NE";
		}
		return "? ";
	}
}
