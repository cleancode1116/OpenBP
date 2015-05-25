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
package org.openbp.cockpit.generator.wizard;

import org.openbp.cockpit.generator.Generator;
import org.openbp.swing.components.treetable.DefaultTreeTableNode;

/**
 * The tree table node of the context inspector.
 *
 * @author Heiko Erhardt
 */
public class GeneratorNode extends DefaultTreeTableNode
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Generator (for leaf nodes) */
	private Generator generator;

	/** Functional group (for group nodes) */
	private String group;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Root node constructor.
	 */
	protected GeneratorNode()
	{
	}

	/**
	 * Group node constructor.
	 *
	 * @param group Functional group (for group nodes)
	 */
	protected GeneratorNode(String group)
	{
		this.group = group;
	}

	/**
	 * Leaf node constructor.
	 *
	 * @param generator Context path
	 */
	protected GeneratorNode(Generator generator)
	{
		this.generator = generator;
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Gets the generator (for leaf nodes).
	 * @nowarn
	 */
	public Generator getGenerator()
	{
		return generator;
	}

	/**
	 * Sets the generator (for leaf nodes).
	 * @nowarn
	 */
	public void setGenerator(Generator generator)
	{
		this.generator = generator;
	}

	/**
	 * Gets the functional group (for group nodes).
	 * @nowarn
	 */
	public String getGroup()
	{
		return group;
	}

	/**
	 * Sets the functional group (for group nodes).
	 * @nowarn
	 */
	public void setGroup(String group)
	{
		this.group = group;
	}

	//////////////////////////////////////////////////
	// @@ Tree table node implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.swing.components.treetable.TreeTableNode#getNodeText()
	 */
	public String getNodeText()
	{
		if (generator != null)
		{
			// Leaf node
			return generator.getDisplayText();
		}

		if (group != null)
		{
			// Group node
			return group;
		}

		// No text for the root node
		return null;
	}

	/**
	 * @see org.openbp.swing.components.treetable.TreeTableNode#getColumnValue(int)
	 */
	public Object getColumnValue(int columnIndex)
	{
		if (generator != null)
		{
			if (columnIndex == 1)
				return generator.getTemplateName();
		}

		return super.getColumnValue(columnIndex);
	}
}
