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
 * An final node constitutes an exit, i\.e\. an ending point of a process.
 * The process execution will stop after processing the node.<br>
 * The entry parameters of the node will be the return parameters of the
 * process.<br>
 * An final node has exactly one node entry socket, but no node exit socket.
 *
 * @author Heiko Erhardt
 */
public interface FinalNode
	extends SingleSocketNode
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Public scope */
	public static final int SCOPE_PUBLIC = 1;

	/** Private scope */
	public static final int SCOPE_PRIVATE = 3;

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the scope of the final node.
	 * @return {@link FinalNode#SCOPE_PUBLIC}/{@link FinalNode#SCOPE_PRIVATE}
	 */
	public int getExitScope();

	/**
	 * Sets the scope of the final node.
	 * @param exitScope {@link FinalNode#SCOPE_PUBLIC}/{@link FinalNode#SCOPE_PRIVATE}
	 */
	public void setExitScope(int exitScope);

	/**
	 * Gets the name of an initial node to continue with.
	 * @nowarn
	 */
	public String getJumpTarget();

	/**
	 * Sets the name of an initial node to continue with.
	 * @nowarn
	 */
	public void setJumpTarget(String jumpTarget);
}
