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
package org.openbp.cockpit.modeler.figures.process;

import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.skins.LinkDescriptor;

/**
 * Vertical line figure.
 *
 * @author Heiko Erhardt
 */
public class HLineFigure extends LineFigure
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param drawing Process drawing we belong to
	 */
	public HLineFigure(ProcessDrawing drawing)
	{
		super(drawing);

		setVerticalLine(false);

		if (drawing != null)
		{
			// Get the stroke and color from the 'HLine' link descriptor of the skin
			LinkDescriptor desc = drawing.getProcessSkin().getLinkDescriptor(FigureTypes.LINKTYPE_HLINE);
			if (desc != null)
			{
				setStroke(desc.getStroke());
				setColor(desc.getColor());
			}
		}
	}
}
