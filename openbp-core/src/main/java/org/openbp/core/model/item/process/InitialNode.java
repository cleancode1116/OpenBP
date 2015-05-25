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
 * An initial node constitutes an entry, i\.e\. a starting point of a process.
 * An initial node has a number of entry parameters (these would be the parameters
 * of the called process, i. e. the request parameters) that map to the exit parameters
 * of the node.<br>
 * An initial node has exactly one node exit socket, but no node entry socket.
 *
 * @author Heiko Erhardt
 */
public interface InitialNode
	extends SingleSocketNode
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Public scope */
	public static final int SCOPE_PUBLIC = 1;

	/** Protected scope */
	public static final int SCOPE_PROTECTED = 2;

	/** Private scope */
	public static final int SCOPE_PRIVATE = 3;

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the scope of the initial node.
	 * @return {@link InitialNode#SCOPE_PUBLIC}/{@link InitialNode#SCOPE_PROTECTED}/{@link InitialNode#SCOPE_PRIVATE}
	 */
	public int getEntryScope();

	/**
	 * Sets the scope of the initial node.
	 * @param entryScope {@link InitialNode#SCOPE_PUBLIC}/{@link InitialNode#SCOPE_PROTECTED}/{@link InitialNode#SCOPE_PRIVATE}
	 */
	public void setEntryScope(int entryScope);

	/**
	 * Gets the default entry flag.
	 * Default entries will be executed if there is not initial node specified for the execution of a process.
	 * @nowarn
	 */
	public boolean isDefaultEntry();

	/**
	 * Sets the default entry flag.
	 * Default entries will be executed if there is not initial node specified for the execution of a process.
	 * @nowarn
	 */
	public void setDefaultEntry(boolean defaultEntry);

	/**
	 * Gets the role or list of roles (comma-separated) that have the permission for this socket.
	 * @nowarn
	 */
	public String getRole();

	/**
	 * Sets the role or list of roles (comma-separated) that have the permission for this socket.
	 * @nowarn
	 */
	public void setRole(String role);

	/**
	 * Get the web service public entry flag.
	 * @nowarn
	 */
	public boolean isWSPublicEntry();

	/**
	 * Sets the web service public entry flag.
	 * @nowarn
	 */
	public void setWSPublicEntry(boolean publicEntry);

	/**
	 * Gets the corresponding web service final node.
	 * @nowarn
	 */
	public FinalNode getWSCorrespondingFinalNode();

	/**
	 * Sets the corresponding web service final node.
	 * @nowarn
	 */
	public void setWSCorrespondingFinalNode(FinalNode exit);

	/**
	 * Gets the corresponding web service final node name.
	 * @nowarn
	 */
	public String getWSCorrespondingFinalNodeName();

	/**
	 * Sets the corresponding web service final node name.
	 * @nowarn
	 */
	public void setWSCorrespondingFinalNodeName(String exitName);
}
