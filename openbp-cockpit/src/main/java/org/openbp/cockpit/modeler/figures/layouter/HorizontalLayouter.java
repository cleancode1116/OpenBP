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

import java.awt.Rectangle;

import org.openbp.cockpit.modeler.figures.generic.CircleConstants;
import org.openbp.cockpit.modeler.figures.generic.Expandable;
import org.openbp.cockpit.modeler.figures.generic.Orientation;
import org.openbp.cockpit.modeler.figures.tag.AbstractTagFigure;
import org.openbp.common.CommonUtil;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

/**
 * Horizontal layouter.
 *
 * @author Stephan Moritz
 */
public class HorizontalLayouter extends AbstractTagLayouter
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Vertical alignment ({@link Orientation#TOP}/{@link Orientation#BOTTOM}/{@link Orientation#CENTER}) */
	protected Orientation vAlign = Orientation.CENTER;

	/** Horizontal alignment ({@link Orientation#LEFT}/{@link Orientation#RIGHT}) */
	protected Orientation hAlign = Orientation.CENTER;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param tagFigure Tag to lay out
	 */
	public HorizontalLayouter(AbstractTagFigure tagFigure)
	{
		super(tagFigure);
	}

	//////////////////////////////////////////////////
	// @@ AbstractLayouter overrides
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.layouter.AbstractTagLayouter#applyDirection()
	 */
	public void applyDirection()
	{
		if (direction < CircleConstants.EIGHTH_S || direction >= CircleConstants.EIGHTH_N)
			hAlign = Orientation.RIGHT;
		else
			hAlign = Orientation.LEFT;

		vAlign = Orientation.CENTER;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.layouter.AbstractTagLayouter#isVerticalLayouter()
	 * @return false; The horizontal layouter is always horizontally oriented
	 */
	public boolean isVerticalLayouter()
	{
		return false;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.layouter.TagLayouter#calculateSize()
	 */
	public Rectangle calculateSize()
	{
		int height = 0;
		int width = 0;

		for (FigureEnumeration en = tagFigure.figures(); en.hasMoreElements();)
		{
			Figure subFigure = en.nextFigure();
			Rectangle r;

			if (subFigure instanceof Expandable)
			{
				r = ((Expandable) subFigure).compactDisplayBox();
			}
			else if (subFigure instanceof LayoutableTag)
			{
				r = ((LayoutableTag) subFigure).getLayouter().calculateSize();
			}
			else
			{
				r = subFigure.displayBox();
			}

			height = Math.max(r.height, height);

			// Add sub figure width and horizontal insets
			width += r.width + insets.left + insets.right;
		}

		// Add top & bottom horizontal edges
		height += insets.top + insets.bottom;

		return new Rectangle(0, 0, width, height);
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.layouter.TagLayouter#performLayout(Rectangle box)
	 */
	public void performLayout(Rectangle box)
	{
		// Determine the base line
		int yBaseLine;
		double yFactor;

		switch (vAlign)
		{
		case TOP:
			yBaseLine = box.y + insets.top;
			yFactor = 0;
			break;

		case BOTTOM:
			yBaseLine = box.y + box.height - insets.bottom;
			yFactor = -1d;
			break;

		default:
			// Center
			yBaseLine = box.y + box.height / 2;
			yFactor = -0.5d;
			break;
		}

		// Add sub left horizontal inset
		int xPos = insets.left + box.x;

		for (FigureEnumeration en = (hAlign == Orientation.RIGHT ? tagFigure.figures() : tagFigure.figuresReverse()); en.hasMoreElements();)
		{
			Figure subFigure = en.nextFigure();
			Rectangle r;

			// Calculate the sub figure size
			if (subFigure instanceof Expandable)
			{
				r = ((Expandable) subFigure).compactDisplayBox();
			}
			else if (subFigure instanceof LayoutableTag)
			{
				r = ((LayoutableTag) subFigure).getLayouter().calculateSize();
			}
			else
			{
				r = subFigure.displayBox();
			}

			// Set the sub figure position and size
			int y = yBaseLine + CommonUtil.rnd(yFactor * r.height);
			r.setLocation(xPos, y);
			subFigure.displayBox(r);

			// Add sub figure width and horizontal insets
			xPos += insets.left + insets.right + r.width;
		}
	}
}
