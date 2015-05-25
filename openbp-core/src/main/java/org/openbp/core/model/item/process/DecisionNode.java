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
 * A decision node is the equivalent of an 'if' statement in a programming language.
 * Depending on the evaluation of its expression Value, forwards control to the 'Yes' or 'No' socket.
 *
 * @author Heiko Erhardt
 */
public interface DecisionNode
	extends MultiSocketNode
{
	/**
	 * Gets the expression.
	 * The expression determines if the decsion node forwards control to the 'Yes' or 'No' exit socket.
	 * It is a regular Java expression that is executed by the Bean Shell Java interpreter.
	 * If the expression evaluates to null or false, the 'No' exit socket will be used.
	 * Within the expression, you may access parameters of the entry socket of the node or process variables by their parameter name just as a Java variable.
	 * @nowarn
	 */
	public String getExpression();

	/**
	 * Sets the expression.
	 * The expression determines if the decsion node forwards control to the 'Yes' or 'No' exit socket.
	 * It is a regular Java expression that is executed by the Bean Shell Java interpreter.
	 * If the expression evaluates to null or false, the 'No' exit socket will be used.
	 * Within the expression, you may access parameters of the entry socket of the node or process variables by their parameter name just as a Java variable.
	 * @nowarn
	 */
	public void setExpression(String expression);
}
