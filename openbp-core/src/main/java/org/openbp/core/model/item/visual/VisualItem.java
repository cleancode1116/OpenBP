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
package org.openbp.core.model.item.visual;

import org.openbp.core.model.item.activity.ActivityItem;

/**
 * OpenBP activity object that executes a user interface template
 * (the actual implementation of the visual) and writes the
 * result of the execution to the request output stream.
 *
 * The type of the visual implementation depends on the user interface engine.<br>
 * A visual implementation might be a Java Server Page (JSP) or a Velocity template for example.
 * The UI node will store a compiled version of the visual implementation (if the user interface
 * engine supports this) to increase performance.
 *
 * @author Heiko Erhardt
 */
public interface VisualItem
	extends ActivityItem
{
	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the Id of the visual item.
	 * @nowarn
	 */
	public String getVisualId();

	/**
	 * Set the Id for the visual item.
	 * @nowarn
	 */
	public void setVisualId(String visualId);
}
