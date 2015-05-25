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

import java.awt.Rectangle;

import org.openbp.cockpit.modeler.figures.layouter.MultiplexLayouter;
import org.openbp.cockpit.modeler.figures.process.NodeFigure;

/**
 * Horizontally oriented tag figure that may rotate around its parent.
 * Uses a left-right {@link MultiplexLayouter} to layout the tag contents
 * (i. e. the tag contents will be flipped vertically if the tag is moved to the other side of its parent figure.
 *
 * @author Stephan Moritz
 */
public class HorizontalRotatingTagFigure extends AbstractTagFigure
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param parent Parent figure
	 * @param modelObject Model object this tag represents or null
	 */
	public HorizontalRotatingTagFigure(NodeFigure parent, Object modelObject)
	{
		super(parent, modelObject);
	}

	//////////////////////////////////////////////////
	// @@ AbstractFigure overrides
	//////////////////////////////////////////////////

	/**
	 * Creates the layouter for this tag.
	 * By default, this is a left-right {@link MultiplexLayouter}
	 * @see org.openbp.cockpit.modeler.figures.tag.AbstractTagFigure#initTagLayouter()
	 */
	protected void initTagLayouter()
	{
		if (getDrawing().getProcessSkin().isRadialTags())
		{
			layouter = MultiplexLayouter.getRadialInstance(this);
		}
		else
		{
			layouter = MultiplexLayouter.getLeftRightInstance(this);
		}
	}

	/**
	 * Positions the display box of the tag relative to its parent figure.
	 * The method will place the display box adjacent to the parent at the given rotation angle.
	 *
	 * @param rect Rectangle object that contains the size of the figure
	 * @return The new display box rectangle of the presentation figure
	 * @see org.openbp.cockpit.modeler.figures.tag.AbstractTagFigure#positionDisplayBox(Rectangle rect)
	 */
	protected Rectangle positionDisplayBox(Rectangle rect)
	{
		if (parent != null)
		{
			rect = ((NodeFigure) parent).placeAdjacent(rect, angle);
		}
		return rect;
	}
}
