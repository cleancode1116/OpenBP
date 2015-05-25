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

import java.util.ArrayList;
import java.util.List;

/**
 * A content block consists of zero or more lines of text that is part of
 * the input or the output file.
 * This class is an abstract class. Its implementation are either regular text content
 * ({@link TextContentBlock}) or sections ({@link SectionContentBlock}).
 *
 * @author Heiko Erhardt
 */
public abstract class ContentBlock
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Lines (contains String objects) */
	private List lines;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ContentBlock()
	{
	}

	/**
	 * Checks if this content block is a section block.
	 * @nowarn
	 */
	public boolean isSection()
	{
		return false;
	}

	/**
	 * Copyies the lines of this block to the result list.
	 *
	 * @param resultLines List of result lines
	 */
	public void copyToResult(List resultLines)
	{
		if (lines != null)
		{
			int n = lines.size();
			for (int i = 0; i < n; ++i)
			{
				resultLines.add(lines.get(i));
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Adds a line.
	 * @param line The line to add
	 */
	public void addLine(String line)
	{
		if (lines == null)
			lines = new ArrayList();
		lines.add(line);
	}

	/**
	 * Clears the lines.
	 */
	public void clearLines()
	{
		lines = null;
	}

	/**
	 * Gets the lines.
	 * @return A list of String objects
	 */
	public List getLines()
	{
		return lines;
	}

	/**
	 * Sets the lines.
	 * @param lines A list of String objects
	 */
	public void setLines(List lines)
	{
		this.lines = lines;
	}
}
