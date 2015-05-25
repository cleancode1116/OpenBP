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

import org.openbp.cockpit.modeler.figures.VisualElement;
import org.openbp.cockpit.modeler.figures.generic.UpdatableFigure;
import org.openbp.core.model.item.process.ProcessObject;

import CH.ifa.draw.framework.Figure;

/**
 * This interface is implemented by all figures which represent an OpenBP process element (nodes, sockets etc\.).
 *
 * @author Stephan Moritz
 */
public interface ProcessElementContainer
	extends VisualElement, UpdatableFigure
{
	/**
	 * Returns the contained process element.
	 * @nowarn
	 */
	public ProcessObject getProcessElement();

	/**
	 * Returns the process element this figure refers to.
	 * The returned element will be used for property browser and tool tip display.
	 * This is usually the same as returned by {@link #getProcessElement}, however this
	 * might be different also for example for sockets of initial/final nodes sockets, which will return
	 * their parent node.
	 * @nowarn
	 */
	public ProcessObject getReferredProcessElement();

	/**
	 * Returns the figure that should be selected after this figure has been deleted.
	 * @return Next figure to be selected or null
	 */
	public Figure selectionOnDelete();

	/**
	 * Gets the direct child element at the given coordinates.
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @return The child element or null if no such child exists
	 */
	public ProcessElementContainer findProcessElementContainer(int x, int y);

	/**
	 * Gets the innermost child at the given coordinates.
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @return The child element or this if the point is in this element itself or null otherwise
	 */
	public ProcessElementContainer findProcessElementContainerInside(int x, int y);
}
