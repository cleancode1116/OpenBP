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
 * A node provider is an item that can be converted into a process node.
 * Typical examples are activities, which provide activity nodes.
 *
 * @author Stephan Moritz
 */
public interface NodeProvider
{
	/**
	 * Generates a node from this provider.
	 *
	 * @param process The process that the node-to-be-constructed shall belong to
	 * @param syncFlags Synchronization flags (see the constants of the {@link ItemSynchronization} class)
	 * @return The new node
	 */
	public Node toNode(ProcessItem process, int syncFlags);
}
