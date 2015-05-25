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
package org.openbp.core.engine;

/**
 * Script-related utility methods.
 *
 * @author Heiko Erhardt
 */
public final class ExpressionConstants
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/**
	 * Delimiter character between object members.
	 * Example: "Order.Buyer.Name"
	 */
	public static final String MEMBER_OPERATOR = ".";

	/**
	 * Delimiter character between object members.
	 * Example: "Order.Buyer.Name"
	 */
	public static final char MEMBER_OPERATOR_CHAR = '.';

	/**
	 * Operator for object reference by primary key.
	 * Example: "OrderId>>Order"
	 */
	public static final String REFERENCE_KEY_OPERATOR = ">>";

	/**
	 * Operator for object reference by primary key.
	 * Example: "OrderId>>Order"
	 */
	public static final char REFERENCE_KEY_OPERATOR_CHAR = '>';

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private ExpressionConstants()
	{
	}
}
