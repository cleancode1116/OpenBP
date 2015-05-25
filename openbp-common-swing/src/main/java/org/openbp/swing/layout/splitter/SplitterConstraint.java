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
package org.openbp.swing.layout.splitter;

/**
 * A SplitterConstraint is a set of component layout constraints used by the {@link SplitterLayout}
 * layout manager.
 */
public class SplitterConstraint
{
	/** Component is a filler component, e. g. fills up any remaining space of the target */
	private boolean filler;

	/** Component is a makeshift filler component,
	 * e. g. fills up any remaining space of the target as long as there is no true filler */
	private boolean makeshiftFiller;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public SplitterConstraint()
	{
	}

	/**
	 * Checks if the component is a regular or makeshift filler component.
	 * @return
	 *		true	The component fills up any remaining space of the target.<br>
	 *		false	The component will try to keep its size in the layout.
	 */
	public boolean isAnyFiller()
	{
		return filler || makeshiftFiller;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Checks if the component is a filler component.
	 * @return
	 *		true	The component fills up any remaining space of the target.<br>
	 *		false	The component will try to keep its size in the layout.
	 */
	public boolean isFiller()
	{
		return filler;
	}

	/**
	 * Sets the filler component property.
	 * @param filler
	 *		true	The component fills up any remaining space of the target.<br>
	 *		false	The component will try to keep its size in the layout.
	 */
	public void setFiller(boolean filler)
	{
		this.filler = filler;
	}

	/**
	 * Checks if the component is a makeshift filler component.
	 * @return
	 *		true	The component fills up any remaining space of the target
	 *				as long as there is no true filler.<br>
	 *		false	The component will try to keep its size in the layout.
	 */
	public boolean isMakeshiftFiller()
	{
		return makeshiftFiller;
	}

	/**
	 * Sets the makeshift filler component property.
	 * @param makeshiftFiller
	 *		true	The component fills up any remaining space of the target
	 *				as long as there is no true filler.<br>
	 *		false	The component will try to keep its size in the layout.
	 */
	public void setMakeshiftFiller(boolean makeshiftFiller)
	{
		this.makeshiftFiller = makeshiftFiller;
	}
}
