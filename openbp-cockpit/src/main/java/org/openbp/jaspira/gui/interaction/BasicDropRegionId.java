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
package org.openbp.jaspira.gui.interaction;

/**
 * Mini identifier used for identifying standard drop regions.
 * Contains a name and a matching constraint number.
 * Primarily used to identify the four edges and the center or a window.
 *
 * @author Stephan Moritz
 */
public class BasicDropRegionId
{
	/** Name of the region */
	private String name;

	/** Region constraint */
	private String constraint;

	/**
	 * Constructor.
	 *
	 * @param name Name of the region
	 * @param constraint Region constraint
	 */
	public BasicDropRegionId(String name, String constraint)
	{
		this.name = name;
		this.constraint = constraint;
	}

	/**
	 * Check for equality.
	 * @nowarn
	 */
	public boolean equals(BasicDropRegionId obj)
	{
		// Same if both BasicIdentifiers and both naems are equal.
		return name.equals(obj.name);
	}

	/**
	 * Gets the name of the region.
	 * @nowarn
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Gets the region constraint.
	 * @nowarn
	 */
	public String getConstraint()
	{
		return constraint;
	}
}
