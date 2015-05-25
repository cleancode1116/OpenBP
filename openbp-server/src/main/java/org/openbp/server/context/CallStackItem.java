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
package org.openbp.server.context;

import org.openbp.core.model.item.process.NodeSocket;

/**
 * Item that has been pushed onto the call stack.
 * Please note that the NodeSocket held by the class maybe an exit or an entry socket,
 * depending on whether a subprocess or something different is described
 * by the StackItem.<br>
 * The item also holds the current position before the call.
 *
 * @author Heiko Erhardt
 */
public interface CallStackItem
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/**
	 * Indicates that execution should advance with the socket returned by {@link #getNodeSocket}.
	 */
	public static final int TYPE_CONTINUE = 0;

	/**
	 * Indicates that the socket returned by {@link #getNodeSocket} should be used as a starting point
	 * for a socket search based on the name of the final node that popped the call stack.
	 * This is the case for regular sub process calls.
	 */
	public static final int TYPE_SEARCH = 1;

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the type of the stack item.
	 * @return {@link CallStackItem#TYPE_CONTINUE}/{@link CallStackItem#TYPE_SEARCH}
	 */
	public int getType();

	/**
	 * This method returns the node socket held by this item.
	 *
	 * @return The NodeSocket hold by this StackItem
	 */
	public NodeSocket getNodeSocket();
}
