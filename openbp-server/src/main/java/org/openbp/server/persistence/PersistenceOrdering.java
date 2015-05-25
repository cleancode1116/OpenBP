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
 * Ordering criteria descriptor.
 *
 * @author Heiko Erhardt
 */
public class PersistenceOrdering
{
	/** Property name */
	private String propertyName;

	/** Ascending order flag */
	private boolean ascending;

	/**
	 * Default constructor.
	 *
	 * @param propertyName Property name
	 * @param ascending Ascending order flag
	 */
	public PersistenceOrdering(final String propertyName, final boolean ascending)
	{
		this.propertyName = propertyName;
		this.ascending = ascending;
	}

	/**
	 * Value constructor (ascending order).
	 *
	 *
	 * @param propertyName Property name
	 */
	public PersistenceOrdering(final String propertyName)
	{
		this.propertyName = propertyName;
		this.ascending = true;
	}

	/**
	 * Value constructor.
	 */
	public PersistenceOrdering()
	{
	}

	/**
	 * Gets the property name.
	 * @nowarn
	 */
	public String getPropertyName()
	{
		return propertyName;
	}

	/**
	 * Sets the property name.
	 * @nowarn
	 */
	public void setPropertyName(final String propertyName)
	{
		this.propertyName = propertyName;
	}

	/**
	 * Gets the ascending order flag.
	 * @nowarn
	 */
	public boolean isAscending()
	{
		return ascending;
	}

	/**
	 * Sets the ascending order flag.
	 * @nowarn
	 */
	public void setAscending(final boolean ascending)
	{
		this.ascending = ascending;
	}
}
