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
package org.openbp.core.model.item.activity;

import org.openbp.core.handler.HandlerDefinition;

/**
 * Activity object that executes a Java handler.
 *
 * This activity wraps an activity handler that is being called when the activity is executed.
 *
 * @author Heiko Erhardt
 */
public interface JavaActivityItem
	extends ActivityItem
{
	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the handler definition.
	 * @nowarn
	 */
	public HandlerDefinition getHandlerDefinition();

	/**
	 * Sets the handler definition.
	 * @nowarn
	 */
	public void setHandlerDefinition(HandlerDefinition handlerDefinition);
}
