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

import org.openbp.common.generic.description.DisplayObject;

import CH.ifa.draw.framework.Figure;

/**
 * A title figure displays the textual title of another figure.
 * It is the display text of the object represented by the figure.
 *
 * @author Heiko Erhardt
 */
public interface TitleFigure
	extends Figure, UpdatableFigure
{
	/**
	 * Gets the title text of the figure.
	 * @nowarn
	 */
	public String getText();

	/**
	 * Sets the title text of the figure.
	 * @nowarn
	 */
	public void setText(String text);

	/**
	 * Gets the the display object we are connected with.
	 * @nowarn
	 */
	public DisplayObject getClient();

	/**
	 * Sets the the display object we are connected with.
	 * @nowarn
	 */
	public void setClient(DisplayObject client);

	/**
	 * Gets the title format.
	 * The title format determines how the title is to be formatted.
	 * The following placehodlers apply:<br>
	 * $name - Name of the underlying {@link DisplayObject}<br>
	 * $text - Display text (i. e. display name or name) of the underlying {@link DisplayObject}
	 * @nowarn
	 */
	public String getTitleFormat();

	/**
	 * Sets the title format.
	 * The title format determines how the title is to be formatted.
	 * The following placehodlers apply:<br>
	 * $name - Name of the underlying {@link DisplayObject}<br>
	 * $text - Display text (i. e. display name or name) of the underlying {@link DisplayObject}
	 * @nowarn
	 */
	public void setTitleFormat(String titleFormat);

	/**
	 * Gets the flag if the display text or the object name should be displayed if there is no display name of the client object.
	 * @nowarn
	 */
	public boolean isVerboseDisplay();

	/**
	 * Sets the flag if the display text or the object name should be displayed if there is no display name of the client object.
	 * @nowarn
	 */
	public void setVerboseDisplay(boolean verboseDisplay);
}
