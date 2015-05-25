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
package org.openbp.guiclient.util;

import javax.swing.tree.TreeNode;

import org.openbp.common.generic.description.DisplayObject;
import org.openbp.guiclient.plugins.displayobject.DisplayObjectPlugin;
import org.openbp.swing.components.tree.DefaultTreeNode;

/**
 * General-purpose tree node that holds a reference to a display object.
 * The display text of the object will be used as tree node text.
 *
 * @author Heiko Erhardt
 */
public class DisplayObjectTreeNode extends DefaultTreeNode
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Display object represented by this node */
	protected DisplayObject object;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public DisplayObjectTreeNode()
	{
	}

	/**
	 * Default constructor.
	 *
	 * @param object Display object represented by this node
	 */
	public DisplayObjectTreeNode(DisplayObject object)
	{
		this.object = object;
	}

	/**
	 * Default constructor.
	 *
	 * @param parent Parent tree node
	 */
	public DisplayObjectTreeNode(TreeNode parent)
	{
		this.parent = parent;
	}

	/**
	 * Default constructor.
	 *
	 * @param object Display object represented by this node
	 * @param parent Parent tree node
	 */
	public DisplayObjectTreeNode(DisplayObject object, TreeNode parent)
	{
		this.object = object;
		this.parent = parent;
	}

	/**
	 * Returns the string representation of this node.
	 * According to the role manager, either the display text or the name of the item.
	 * @nowarn
	 */
	public String toString()
	{
		if (object != null)
		{
			return DisplayObjectPlugin.getInstance().isTitleModeText() ? object.getDisplayText() : object.getName();
		}
		return "";
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the display object represented by this node.
	 * @nowarn
	 */
	public DisplayObject getObject()
	{
		return object;
	}

	/**
	 * Sets the display object represented by this node.
	 * @nowarn
	 */
	public void setObject(DisplayObject object)
	{
		this.object = object;
	}
}
