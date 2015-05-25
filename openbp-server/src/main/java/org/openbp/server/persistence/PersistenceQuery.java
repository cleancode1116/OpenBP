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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.common.util.iterator.EmptyIterator;

/**
 * Query descriptor that can be used to describe and execute a query.
 *
 * @author Heiko Erhardt
 */
public class PersistenceQuery
{
	/** Context */
	private final PersistenceContext context;

	/** Object class */
	private final Class objectClass;

	/** Maximum number of result records */
	private int maxResults;

	/** List of {@link PersistenceOrdering} objects */
	private List orderingList;

	/** List of {@link PersistenceCriterion} objects */
	private List criterionList;

	/**
	 * Default constructor.
	 *
	 * @param context Context
	 * @param objectClass Object class
	 */
	public PersistenceQuery(final PersistenceContext context, final Class objectClass)
	{
		this.context = context;
		this.objectClass = objectClass;
	}

	/**
	 * Gets the context.
	 * @nowarn
	 */
	public PersistenceContext getContext()
	{
		return context;
	}

	/**
	 * Gets the object class.
	 * @nowarn
	 */
	public Class getObjectClass()
	{
		return objectClass;
	}

	/**
	 * Gets the maximum number of result records.
	 * @nowarn
	 */
	public int getMaxResults()
	{
		return maxResults;
	}

	/**
	 * Sets the maximum number of result records.
	 * @nowarn
	 */
	public void setMaxResults(final int maxResults)
	{
		this.maxResults = maxResults;
	}

	//////////////////////////////////////////////////
	// @@ Criteria
	//////////////////////////////////////////////////

	/**
	 * Adds an 'equals' expression.
	 *
	 * @param property Attribute name or relation path
	 * @param value Value to compare to
	 */
	public void eq(final String property, final Object value)
	{
		addCriterion(property, PersistenceCriterion.OPERATOR_EQ, value);
	}

	/**
	 * Adds an 'equals or null' expression.
	 *
	 * @param property Attribute name or relation path
	 * @param value Value to compare to
	 */
	public void eqOrNull(final String property, final Object value)
	{
		addCriterion(property, PersistenceCriterion.OPERATOR_EQ_OR_NULL, value);
	}

	/**
	 * Adds a 'not equals' expression.
	 *
	 * @param property Attribute name or relation path
	 * @param value Value to compare to
	 */
	public void neq(final String property, final Object value)
	{
		addCriterion(property, PersistenceCriterion.OPERATOR_NEQ, value);
	}

	/**
	 * Adds a 'greater than' expression.
	 *
	 * @param property Attribute name or relation path
	 * @param value Value to compare to
	 */
	public void gt(final String property, final Object value)
	{
		addCriterion(property, PersistenceCriterion.OPERATOR_GT, value);
	}

	/**
	 * Adds a 'greater than or equal' expression.
	 *
	 * @param property Attribute name or relation path
	 * @param value Value to compare to
	 */
	public void gte(final String property, final Object value)
	{
		addCriterion(property, PersistenceCriterion.OPERATOR_GTE, value);
	}

	/**
	 * Adds a 'lower than' expression.
	 *
	 * @param property Attribute name or relation path
	 * @param value Value to compare to
	 */
	public void lt(final String property, final Object value)
	{
		addCriterion(property, PersistenceCriterion.OPERATOR_LT, value);
	}

	/**
	 * Adds a 'lower than or equal' expression.
	 *
	 * @param property Attribute name or relation path
	 * @param value Value to compare to
	 */
	public void lte(final String property, final Object value)
	{
		addCriterion(property, PersistenceCriterion.OPERATOR_LTE, value);
	}

	/**
	 * Adds a 'like' expression.
	 *
	 * @param property Attribute name or relation path
	 * @param value Value to compare to
	 */
	public void like(final String property, final Object value)
	{
		addCriterion(property, PersistenceCriterion.OPERATOR_LIKE, value);
	}

	/**
	 * Adds an 'is null' expression.
	 *
	 * @param property Attribute name or relation path
	 */
	public void isNull(final String property)
	{
		addCriterion(property, PersistenceCriterion.OPERATOR_NULL, null);
	}

	/**
	 * Adds an alias to a related entity.
	 *
	 * @param property Attribute name or relation path
	 * @param alias Alias name or null
	 */
	public void alias(final String property, final String alias)
	{
		addCriterion(property, PersistenceCriterion.OPERATOR_ALIAS, alias);
	}

	//////////////////////////////////////////////////
	// @@ Orderings
	//////////////////////////////////////////////////

	/**
	 * Gets the ordering list.
	 * @return An iterator of {@link PersistenceOrdering} objects
	 */
	public Iterator getOrderings()
	{
		if (orderingList == null)
			return EmptyIterator.getInstance();
		return orderingList.iterator();
	}

	/**
	 * Adds an ordering.
	 * @param memberName Member name
	 * @param ascending Ascending order flag
	 */
	public void addOrdering(final String memberName, final boolean ascending)
	{
		PersistenceOrdering ordering = new PersistenceOrdering(memberName, ascending);
		addOrdering(ordering);
	}

	/**
	 * Adds an ascending ordering.
	 * @param memberName Member name
	 */
	public void addOrdering(final String memberName)
	{
		PersistenceOrdering ordering = new PersistenceOrdering(memberName);
		addOrdering(ordering);
	}

	/**
	 * Adds an ordering.
	 * @param ordering The ordering to add
	 */
	public void addOrdering(final PersistenceOrdering ordering)
	{
		if (orderingList == null)
			orderingList = new ArrayList();
		orderingList.add(ordering);
	}

	//////////////////////////////////////////////////
	// @@ Criteria
	//////////////////////////////////////////////////

	/**
	 * Gets the list of {@link PersistenceCriterion} objects.
	 * @return An iterator of {@link PersistenceCriterion} objects
	 */
	public Iterator getCriterions()
	{
		if (criterionList == null)
			return EmptyIterator.getInstance();
		return criterionList.iterator();
	}

	/**
	 * Adds a criterion.
	 * @param property Property
	 * @param operator Operator
	 * @param operand Operand
	 */
	protected void addCriterion(final String property, final String operator, final Object operand)
	{
		if (criterionList == null)
			criterionList = new ArrayList();
		PersistenceCriterion criterion = new PersistenceCriterion(property, operator, operand);
		criterionList.add(criterion);
	}
}
