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
 * A single socket node is a node that has one socket only.
 * Examples for this node type are entry and final nodes.
 *
 * @author Heiko Erhardt
 */
public interface SingleSocketNode
	extends Node
{
	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the the only node exit socket.
	 * @nowarn
	 */
	public NodeSocket getSocket();

	/**
	 * Sets the the only node exit socket.
	 * @nowarn
	 */
	public void setSocket(NodeSocket socket);

	/**
	 * Gets the geometry information of the sub process socket that calls this node (required by the Modeler).
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public String getSocketGeometry();

	/**
	 * Sets the geometry information of the sub process socket that calls this node (required by the Modeler).
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public void setSocketGeometry(String socketGeometry);
}
