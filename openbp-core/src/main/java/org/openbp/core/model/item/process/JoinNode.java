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
 * A join node is the equivalent of an 'if' statement in a programming language.
 * Depending on the evaluation of its expression Value, forwards control to the 'Yes' or 'No' socket.
 *
 * @author Heiko Erhardt
 */
public interface JoinNode
	extends MultiSocketNode
{
	/** Join type: Suceeds if all incoming tokens are present */
	public static final String JOIN_AND = "and";

	/** Join type: Suceeds if at least one incoming token is present, purging any other tokens that may be active in the context of this workflow */
	public static final String JOIN_OR = "or";

	/**
	 * Gets the join type.
	 * @return See the constants of this classs
	 */
	public String getJoinType();

	/**
	 * Sets the join type.
	 * @param joinType See the constants of this classs
	 */
	public void setJoinType(String joinType);
}
