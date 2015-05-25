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
 * Vertical layouter.
 *
 * @author Stephan Moritz
 */
public class VerticalLayouter extends AbstractTagLayouter
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Horizontal alignment ({@link Orientation#LEFT}/{@link Orientation#RIGHT}/{@link Orientation#CENTER}) */
	protected Orientation hAlign = Orientation.CENTER;

	/** Vertical alignment ({@link Orientation#TOP}/{@link Orientation#BOTTOM}) */
	protected Orientation vAlign = Orientation.CENTER;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param tagFigure Tag to lay out
	 */
	public VerticalLayouter(AbstractTagFigure tagFigure)
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
		if (direction < CircleConstants.EIGHTH_W)
			vAlign = Orientation.BOTTOM;
		else
			vAlign = Orientation.TOP;

		hAlign = Orientation.CENTER;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.layouter.AbstractTagLayouter#isVerticalLayouter()
	 * @return true; The vertical layouter is always vertically oriented
	 */
	public boolean isVerticalLayouter()
	{
		return true;
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

			width = Math.max(r.width, width);

			// Add sub figure height and vertical insets
			height += r.height + insets.top + insets.bottom;
		}

		// Add left & right horizontal edges
		width += insets.left + insets.right;

		return new Rectangle(0, 0, width, height);
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.layouter.TagLayouter#performLayout(Rectangle box)
	 */
	public void performLayout(Rectangle box)
	{
		// Determine the base line
		int xBaseLine;
		double xFactor;

		switch (hAlign)
		{
		case LEFT:
			xBaseLine = box.x + insets.left;
			xFactor = 0;
			break;

		case RIGHT:
			xBaseLine = box.x + box.width - insets.right;
			xFactor = -1d;
			break;

		default:
			// Center
			xBaseLine = box.x + box.width / 2;
			xFactor = -0.5d;
			break;
		}

		// Add sub top vertical inset
		int yPos = insets.top + box.y;

		for (FigureEnumeration en = (vAlign == Orientation.BOTTOM ? tagFigure.figures() : tagFigure.figuresReverse()); en.hasMoreElements();)
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
			int x = xBaseLine + CommonUtil.rnd(xFactor * r.width);
			r.setLocation(x, yPos);
			subFigure.displayBox(r);

			// Add sub figure height and vertical insets
			yPos += insets.top + insets.bottom + r.height;
		}
	}
}
