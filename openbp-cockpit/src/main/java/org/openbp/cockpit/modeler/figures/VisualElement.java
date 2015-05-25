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
package org.openbp.cockpit.modeler.figures;

import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.jaspira.gui.interaction.InteractionClient;

import CH.ifa.draw.framework.Figure;

/**
 * A visual element represents a figure of a process drawing.
 * It supports visual status changes, element hierarchies, drag and drop and custom cursors/tools.
 *
 * @author Heiko Erhardt
 */
public interface VisualElement
	extends Figure, InteractionClient
{
	//////////////////////////////////////////////////
	// @@ Visiblity status constants
	//////////////////////////////////////////////////

	/** Visual element is currently visible */
	public static final int VISUAL_VISIBLE = (1 << 0);

	/** Visual element is currently a drop target */
	public static final int VISUAL_DND_PARTICIPANT = (1 << 1);

	/** Display the entire content of the visual element */
	public static final int VISUAL_DISPLAY_ALL = (1 << 2);

	//////////////////////////////////////////////////
	// @@ Associated objects
	//////////////////////////////////////////////////

	/**
	 * Sets the drawing the element is a part of.
	 * @nowarn
	 */
	public void setDrawing(ProcessDrawing processDrawing);

	/**
	 * Gets the drawing the element is a part of.
	 * @nowarn
	 */
	public ProcessDrawing getDrawing();

	/**
	 * Gets parent element of this element, if any.
	 * @nowarn
	 */
	public VisualElement getParentElement();

	/**
	 * Gets the presentation figure of this element.
	 *
	 * @return The presentation figure<br>
	 * Note that this can be the visual element itself if it does not have a separate presentation figure
	 */
	public Figure getPresentationFigure();

	/**
	 * Updates (reinitializes) the presentation figure.
	 */
	public void updatePresentationFigure();

	//////////////////////////////////////////////////
	// @@ Visibility status
	//////////////////////////////////////////////////

	/**
	 * Gets the flag if the element is currently visible.
	 * @nowarn
	 */
	public boolean isVisible();

	/**
	 * Sets the flag if the element is currently visible.
	 * @nowarn
	 */
	public void setVisible(boolean visible);

	//////////////////////////////////////////////////
	// @@ Event handling
	//////////////////////////////////////////////////

	/**
	 * Handler method that is called for handling events.
	 * Events can be cursor events, selection events etc.
	 *
	 * @param event Event that desribes the event
	 * @return
	 * true: The event was handled by the element.<br>
	 * false: No special handling, perform the default handling.
	 */
	public boolean handleEvent(VisualElementEvent event);

	//////////////////////////////////////////////////
	// @@ Child element support
	//////////////////////////////////////////////////

	/**
	 * Gets the visual element that is a child of this element and matches the given coordinates.
	 *
	 * @param x World coordinates of the point to search
	 * @param y World coordinates of the point to search
	 * @return The element or null if no child element is located at the given position
	 */
	public VisualElement findVisualElement(int x, int y);

	/**
	 * Gets the visual element that is a child of this element and matches the given coordinates (recursively).
	 *
	 * @param x World coordinates of the point to search
	 * @param y World coordinates of the point to search
	 * @return The element or null if no child element is located at the given position
	 */
	public VisualElement findVisualElementInside(int x, int y);
}
