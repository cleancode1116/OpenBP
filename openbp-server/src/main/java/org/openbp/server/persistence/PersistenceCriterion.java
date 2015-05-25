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
package org.openbp.server.persistence;

/**
 * Expression that can be used for finding persistent objects.
 *
 * @author Heiko Erhardt
 */
public class PersistenceCriterion
{
	/** Operator constant for the 'equals' expression */
	public static final String OPERATOR_EQ = "eq";

	/** Operator constant for the 'equals or null' expression */
	public static final String OPERATOR_EQ_OR_NULL = "eq-or-null";

	/** Operator constant for the 'not equals' expression */
	public static final String OPERATOR_NEQ = "neq";

	/** Operator constant for the 'greater than' expression */
	public static final String OPERATOR_GT = "gt";

	/** Operator constant for the 'greater than or equal' expression */
	public static final String OPERATOR_GTE = "gte";

	/** Operator constant for the 'lower than' expression */
	public static final String OPERATOR_LT = "lt";

	/** Operator constant for the 'lower than or equal' expression */
	public static final String OPERATOR_LTE = "lte";

	/** Operator constant for the 'like' expression */
	public static final String OPERATOR_LIKE = "like";

	/** Operator constant for the 'null' expression */
	public static final String OPERATOR_NULL = "null";

	/** Operator constant for the 'alias' expression */
	public static final String OPERATOR_ALIAS = "alias";

	/** Property */
	private String property;

	/** Operator */
	private String operator;

	/** Operand */
	private Object operand;

	/**
	 * Constructor.
	 */
	public PersistenceCriterion()
	{
	}

	/**
	 * Value constructor.
	 *
	 * @param property Property
	 * @param operator Operator
	 * @param operand Operand
	 */
	public PersistenceCriterion(String property, String operator, Object operand)
	{
		this.property = property;
		this.operator = operator;
		this.operand = operand;
	}

	/**
	 * Gets the property.
	 * @nowarn
	 */
	public String getProperty()
	{
		return property;
	}

	/**
	 * Sets the property.
	 * @nowarn
	 */
	public void setProperty(String property)
	{
		this.property = property;
	}

	/**
	 * Gets the operator.
	 * @nowarn
	 */
	public String getOperator()
	{
		return operator;
	}

	/**
	 * Sets the operator.
	 * @nowarn
	 */
	public void setOperator(String operator)
	{
		this.operator = operator;
	}

	/**
	 * Gets the operand.
	 * @nowarn
	 */
	public Object getOperand()
	{
		return operand;
	}

	/**
	 * Sets the operand.
	 * @nowarn
	 */
	public void setOperand(Object operand)
	{
		this.operand = operand;
	}
}
