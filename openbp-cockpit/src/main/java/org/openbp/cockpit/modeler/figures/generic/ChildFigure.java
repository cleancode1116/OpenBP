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

import CH.ifa.draw.framework.Figure;

/**
 * A child figure is a dependent figure of its parent.
 *
 * @author Stephan Moritz
 */
public interface ChildFigure
	extends Figure
{
	/**
	 * Gets the parent figure of this figure.
	 *
	 * @return The parent or null if the figure does not have a parent
	 */
	public Figure getParent();

	/**
	 * Sets the parent figure of this figure.
	 *
	 * @param parent The parent or null if the figure does not have a parent
	 */
	public void setParent(Figure parent);
}
