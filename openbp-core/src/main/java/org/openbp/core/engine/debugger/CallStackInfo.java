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
package org.openbp.core.engine.debugger;

import java.io.Serializable;

import org.openbp.core.model.ModelQualifier;

/**
 * Contains information about a call stack element.
 *
 * @author Heiko Erhardt
 */
public class CallStackInfo
	implements Serializable
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Stack element type */
	private int type;

	/** Qualified name of the node socket payload of this stack item */
	private ModelQualifier savedPosition;

	/** Qualified name of the current socket at the time of the stack element creation */
	private ModelQualifier currentPosition;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public CallStackInfo()
	{
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the stack element type.
	 * @seem CallStackItem.getType
	 * @nowarn
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * Sets the stack element type.
	 * @nowarn
	 */
	public void setType(int type)
	{
		this.type = type;
	}

	/**
	 * Gets the qualified name of the node socket payload of this stack item.
	 * @nowarn
	 */
	public ModelQualifier getSavedPosition()
	{
		return savedPosition;
	}

	/**
	 * Sets the qualified name of the node socket payload of this stack item.
	 * @nowarn
	 */
	public void setSavedPosition(ModelQualifier savedPosition)
	{
		this.savedPosition = savedPosition;
	}

	/**
	 * Gets the qualified name of the current socket at the time of the stack element creation.
	 * @nowarn
	 */
	public ModelQualifier getCurrentPosition()
	{
		return currentPosition;
	}

	/**
	 * Sets the qualified name of the current socket at the time of the stack element creation.
	 * @nowarn
	 */
	public void setCurrentPosition(ModelQualifier currentPosition)
	{
		this.currentPosition = currentPosition;
	}
}
