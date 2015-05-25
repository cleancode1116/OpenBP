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

import org.openbp.core.handler.HandlerDefinition;

/**
 * The activity node is the all-purpose node of a process.
 * An activity node can encapsulate a Java method, a visual or another process.<br>
 * The activity node has an arbitrary number of entries and exits, which
 * have an arbitrary number of parameters each.
 *
 * An activity node can execute in several execution modes:<br>
 * In immediate execution mode, the activity will be performed at once
 * and is expected to return in a moment. The process will continue normally
 * after the node execution.<br>
 * Asynchronous execution means that after the node has been executed, the
 * process will be suspended and later be resumed at one of the node exits.
 * This is e. g. the case for visual nodes, which send output back to the browser.<br>
 * Persisted execution works similar to Asynchronous exection, but in addition
 * the process state will be written to persistent storage. When the process is
 * about to be resumed, the process state will be deserialized from persistent
 * storage. Activities that trigger a long-lasting processing (e. g. a workflow
 * activity, which requires human interaction) would use this mode.
 *
 * @author Heiko Erhardt
 */
public interface ActivityNode
	extends MultiSocketNode
{
	//////////////////////////////////////////////////
	// @@ Property access: Miscelleanous
	//////////////////////////////////////////////////

	/**
	 * Gets the type of the activity item referred to by this node.
	 * @nowarn
	 */
	public String getActivityItemType();

	/**
	 * Gets the activity handler definition.
	 * @nowarn
	 */
	public HandlerDefinition getActivityHandlerDefinition();

	/**
	 * Sets the activity handler definition.
	 * @nowarn
	 */
	public void setActivityHandlerDefinition(HandlerDefinition activityHandlerDefinition);
}
