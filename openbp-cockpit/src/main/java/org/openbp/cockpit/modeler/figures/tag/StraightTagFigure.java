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
package org.openbp.cockpit.modeler.figures.tag;

import java.awt.Point;
import java.awt.Rectangle;

import org.openbp.cockpit.modeler.figures.VisualElement;
import org.openbp.cockpit.modeler.figures.generic.CircleConstants;
import org.openbp.cockpit.modeler.figures.generic.Orientation;
import org.openbp.cockpit.modeler.figures.layouter.HorizontalLayouter;
import org.openbp.cockpit.modeler.figures.layouter.MultiplexLayouter;

/**
 * Non-rotating tag figure that contains simple content that does not change its order when the tag is moved.
 *
 * @author Stephan Moritz
 */
public class StraightTagFigure extends AbstractTagFigure
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Direction the tag is facing */
	protected Orientation tagOrientation;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param parent Parent figure
	 * @param origin Origin of the tag
	 * @param modelObject Model object this tag represents or null
	 */
	public StraightTagFigure(VisualElement parent, Object modelObject, Point origin)
	{
		super(parent, modelObject);

		this.origin = origin;
		tagOrientation = Orientation.UNDETERMINED;
	}

	/**
	 * Creates the layouter for this tag.
	 * By default, this is a {@link HorizontalLayouter}
	 */
	protected void initTagLayouter()
	{
		layouter = MultiplexLayouter.getStraightInstance(this);
	}

	//////////////////////////////////////////////////
	// @@ AbstractFigure overrides
	//////////////////////////////////////////////////

	/**
	 * Positions the display box of the tag relative to its parent figure.
	 * The method will place the display box adjacent to the parent at the given orientation.
	 *
	 * @param rect Rectangle object that contains the size of the figure
	 * @return The new display box rectangle of the presentation figure
	 * @see org.openbp.cockpit.modeler.figures.tag.AbstractTagFigure#positionDisplayBox(Rectangle rect)
	 */
	protected Rectangle positionDisplayBox(Rectangle rect)
	{
		rect.translate((int) origin.getX(), (int) origin.getY());

		Rectangle cb = compactDisplayBox();

		Orientation orientation = getTagOrientation();
		if (orientation != null)
		{
			switch (orientation)
			{
			case BOTTOM:
				rect.translate(-cb.width / 2, -cb.height / 2);
				break;
	
			case TOP:
				rect.translate(-cb.width / 2, -rect.height + cb.height / 2);
				break;
	
			case RIGHT:
				rect.translate(-cb.width / 2, -cb.height / 2);
				break;
	
			case LEFT:
				rect.translate(-rect.width + cb.width / 2, -cb.height / 2);
				break;
			}
		}

		return rect;
	}

	/**
	 * Gets the direction the tag is facing.
	 * @nowarn
	 */
	public Orientation getTagOrientation()
	{
		return tagOrientation;
	}

	public void basicSetAngle(double angle)
	{
		super.basicSetAngle(angle);

		Rectangle db = getCenterFigureBox();
		tagOrientation = CircleConstants.determineOrientation(angle, db);
	}
}
