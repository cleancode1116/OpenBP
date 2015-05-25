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
package org.openbp.common.template.writer.merger;

/**
 * A section content block is a set of lines enclosed in content indicators.
 * ("{{*name* ... }}*name*")
 * The text content does not include the content indicators.
 *
 * @author Heiko Erhardt
 */
public class SectionContentBlock extends ContentBlock
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Name */
	private String name;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public SectionContentBlock()
	{
	}

	/**
	 * Default constructor.
	 *
	 * @param name Name
	 */
	public SectionContentBlock(String name)
	{
		this.name = name;
	}

	/**
	 * Checks if this content block is a section block.
	 * @nowarn
	 */
	public boolean isSection()
	{
		return true;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the name.
	 * @nowarn
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name.
	 * @nowarn
	 */
	public void setName(String name)
	{
		this.name = name;
	}
}
