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
package org.openbp.core.model.item.process;

/**
 * A placeholder node is used in a process if at present the type of the underlying item cannot be determined yet.
 * It will be substituted for an activity or process node in a later stage of the development process.
 * As opposed to other types of multi socket nodes, a placeholder is not associated with an item
 * and thus has no special properties.
 *
 * @author Heiko Erhardt
 */
public interface PlaceholderNode
	extends MultiSocketNode
{
	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the reference path to some process element.
	 * @nowarn
	 */
	public String getReferencePath();

	/**
	 * Sets the reference path to some process element.
	 * @nowarn
	 */
	public void setReferencePath(String referencePath);
}
