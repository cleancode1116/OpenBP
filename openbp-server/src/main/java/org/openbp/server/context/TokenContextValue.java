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
package org.openbp.server.context;

import java.io.Serializable;

import org.openbp.common.util.ToStringHelper;

/**
 * Token context value.
 *
 * @author Heiko Erhardt
 */
public class TokenContextValue
	implements Serializable
{
	private static final long serialVersionUID = 605837511221074718L;

	/** Value */
	private Object value;

	/** Persistent variable property */
	private boolean persistentVariable;

	/**
	 * Default constructor.
	 */
	public TokenContextValue()
	{
	}

	/**
	 * Returns a string represenation of this object.
	 * 
	 * @return Debug string containing the most important properties of this object
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "value", "persistentVariable");
	}

	/**
	 * Gets the value.
	 * @nowarn
	 */
	public Object getValue()
	{
		return value;
	}

	/**
	 * Sets the value.
	 * @nowarn
	 */
	public void setValue(Object value)
	{
		this.value = value;
	}

	/**
	 * Gets the persistent variable property.
	 * @nowarn
	 */
	public boolean isPersistentVariable()
	{
		return persistentVariable;
	}

	/**
	 * Sets the persistent variable property.
	 * @nowarn
	 */
	public void setPersistentVariable(boolean persistentVariable)
	{
		this.persistentVariable = persistentVariable;
	}
}
