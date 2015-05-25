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

import org.openbp.core.model.ModelObjectSymbolNames;

/**
 * Standard implementation of a decision node.
 * Depending on the evaluation of its expression Value, forwards control to the 'Yes' or 'No' socket.
 *
 * @author Heiko Erhardt
 */
public class DecisionNodeImpl extends MultiSocketNodeImpl
	implements DecisionNode
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Expression */
	private String expression;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public DecisionNodeImpl()
	{
	}

	/**
	 * Copies the values of the source object to this object.
	 *
	 * @param source The source object. Must be of the same type as this object.
	 * @param copyMode Determines if a deep copy, a first level copy or a shallow copy is to be
	 * performed. See the constants of the org.openbp.common.generic.description.Copyable class.
	 * @throws CloneNotSupportedException If the cloning of one of the contained objects failed
	 */
	public void copyFrom(Object source, int copyMode)
		throws CloneNotSupportedException
	{
		if (source == this)
			return;
		super.copyFrom(source, copyMode);

		DecisionNodeImpl src = (DecisionNodeImpl) source;

		// Copy member data
		expression = src.expression;
	}

	/**
	 * Gets the name of the standard icon of this object.
	 * The icon name can be used by the client-side IconModel to retrieve an icon for the object.
	 *
	 * @return The icon name or null if the object does not have a particular icon
	 */
	public String getModelObjectSymbolName()
	{
		return ModelObjectSymbolNames.DECISION_NODE;
	}

	//////////////////////////////////////////////////
	// @@ ModelObject overrides
	//////////////////////////////////////////////////

	/**
	 * Gets text that can be used to display this object.
	 *
	 * @nowarn
	 */
	public String getDisplayText()
	{
		String text = getDisplayName();
		if (text != null)
			return text;

		if (expression != null)
			return expression + "?";

		return "?";
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the expression.
	 * The expression determines if the decsion node forwards control to the 'Yes' or 'No' exit socket.
	 * It is a regular Java expression that is executed by the Bean Shell Java interpreter.
	 * If the expression evaluates to null or false, the 'No' exit socket will be used.
	 * Within the expression, you may access parameters of the entry socket of the node or process variables by their parameter name just as a Java variable.
	 * @nowarn
	 */
	public String getExpression()
	{
		return expression;
	}

	/**
	 * Sets the expression.
	 * The expression determines if the decsion node forwards control to the 'Yes' or 'No' exit socket.
	 * It is a regular Java expression that is executed by the Bean Shell Java interpreter.
	 * If the expression evaluates to null or false, the 'No' exit socket will be used.
	 * Within the expression, you may access parameters of the entry socket of the node or process variables by their parameter name just as a Java variable.
	 * @nowarn
	 */
	public void setExpression(String expression)
	{
		this.expression = expression;
	}
}
