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
package org.openbp.jaspira.gui.interaction;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;

import org.openbp.common.icon.MultiIcon;

/**
 * An entry of a break out menu.
 *
 * @author Jens Ferchland
 */
public interface BreakoutBoxEntry
{
	/**
	 * Gets the transferable object of this entry.
	 * @nowarn
	 */
	public Transferable getTransferable();

	/**
	 * Gets the title of this entry.
	 * @nowarn
	 */
	public String getTitle();

	/**
	 * Gets the icon of this entry.
	 * @nowarn
	 */
	public MultiIcon getIcon();

	/**
	 * Gets the description of this entry.
	 * @nowarn
	 */
	public String getDescription();

	/**
	 * Gets the tooltip for this entry.
	 * @nowarn
	 */
	public String getToolTipText();

	/**
	 * Gets the importer of the entry.
	 * @nowarn
	 */
	public Importer getImporter();

	/**
	 * Draws the entry.
	 * @param g Graphics context
	 */
	public void draw(Graphics g);

	/**
	 * Checks if the given point is inside the region of the entry.
	 * @param x Position in screen coordinates
	 * @param y Position in screen coordinates
	 * @nowarn
	 */
	public boolean reactsOn(int x, int y);

	/**
	 * Imports the data at the drop point.
	 * @param p Import position in glass coordinates
	 * @return
	 *		true	The data was successfully imported.<br>
	 *		false	An error occured while importing the data.
	 */
	public boolean importData(Point p);

	/**
	 * Sets the location and dimension of the entry.
	 * @param r Bounds in screen coordinates
	 */
	public void setLocationOnGlassPanel(Rectangle r);

	/**
	 * Gets the location and dimension of the entry.
	 * @return Bounds in screen coordinates
	 */
	public Rectangle getLocationOnGlassPanel();
}
