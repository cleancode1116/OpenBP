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
 * The visual node is an activity node that refers to a visual item.
 * A visual executes a user interface template and sends the output to the
 * output stream of the context the process executes in.
 *
 * @author Heiko Erhardt
 */
public interface VisualNode
	extends ActivityNode
{
	/**
	 * Gets the Id of the visual item.
	 * @nowarn
	 */
	public String getVisualId();

	/**
	 * Gets the flag that denotes if the process state should be persisted.
	 * @nowarn
	 */
	public boolean isWaitStateNode();

	/**
	 * Sets the flag that denotes if the process state should be persisted.
	 * @nowarn
	 */
	public void setWaitStateNode(boolean waitStateNode);
}
