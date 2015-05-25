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

import org.openbp.cockpit.modeler.figures.generic.XFigure;
import org.openbp.cockpit.modeler.figures.generic.XRectangleFigure;
import org.openbp.cockpit.modeler.figures.process.NodeFigure;

/**
 * Simple tag figure that displays a line of text.
 *
 * @author Stephan Moritz
 */
public class SimpleTextTagFigure extends HorizontalRotatingTagFigure
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
	public SimpleTextTagFigure(NodeFigure parent, Object modelObject)
	{
		super(parent, modelObject);
	}

	/**
	 * Creates the presentation figure of this node.
	 * This is a rectangle figure.
	 * @return The presentation figure
	 */
	protected XFigure createPresentationFigure()
	{
		// We use a simple transparent rectangle figure
		XRectangleFigure figure = new XRectangleFigure();

		figure.setFrameColor(null);
		figure.setFillColor(null);

		return figure;
	}

	/**
	 * Creates the shadow figure of the tag's presentation figure.
	 */
	protected void initShadow()
	{
		// Text tags do not have a shadow, so don't create one
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#updatePresentationFigure()
	 */
	public void updatePresentationFigure()
	{
		// Re-set the origin
		if (parent != null)
		{
			origin = new Point(parent.center());
		}

		super.updatePresentationFigure();
	}
}
