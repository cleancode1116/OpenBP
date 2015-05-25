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

import org.openbp.cockpit.modeler.util.FigureUtil;
import org.openbp.common.generic.description.DisplayObject;
import org.openbp.swing.SwingUtil;

/**
 * Fixed width/height text figure that is connected to a given display object (the client) and displays
 * either the name or the display text of the object.
 *
 * @author Heiko Erhardt
 */
public class FixedTitleFigure extends XTextFigure
	implements TitleFigure
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** The display object we are connected with */
	private DisplayObject client;

	/** Title format */
	private String titleFormat;

	/** Flag if the display text or the object name should be displayed if there is no display name of the client object */
	private boolean verboseDisplay;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public FixedTitleFigure()
	{
		// Text figure with constant size
		setAutoSize(false);
	}

	/**
	 * Constructor.
	 *
	 * @param client The display object we are connected with
	 */
	public FixedTitleFigure(DisplayObject client)
	{
		this();

		setClient(client);
		setAlignment(SwingUtil.CENTER | SwingUtil.MIDDLE);
	}

	//////////////////////////////////////////////////
	// @@ UpdatableFigure implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.generic.UpdatableFigure#updateFigure()
	 */
	public void updateFigure()
	{
		FigureUtil.synchronizeText(this);
	}

	//////////////////////////////////////////////////
	// @@ TitleFigure implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.generic.TitleFigure#getClient()
	 */
	public DisplayObject getClient()
	{
		return client;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.generic.TitleFigure#setClient(DisplayObject client)
	 */
	public void setClient(DisplayObject client)
	{
		this.client = client;

		updateFigure();
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.generic.TitleFigure#getTitleFormat()
	 */
	public String getTitleFormat()
	{
		return titleFormat;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.generic.TitleFigure#setTitleFormat(String titleFormat)
	 */
	public void setTitleFormat(String titleFormat)
	{
		this.titleFormat = titleFormat;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.generic.TitleFigure#isVerboseDisplay()
	 */
	public boolean isVerboseDisplay()
	{
		return verboseDisplay;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.generic.TitleFigure#setVerboseDisplay(boolean verboseDisplay)
	 */
	public void setVerboseDisplay(boolean verboseDisplay)
	{
		if (this.verboseDisplay != verboseDisplay)
		{
			this.verboseDisplay = verboseDisplay;
			updateFigure();
		}
	}
}
