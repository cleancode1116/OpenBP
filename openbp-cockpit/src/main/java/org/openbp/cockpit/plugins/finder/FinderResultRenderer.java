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
package org.openbp.cockpit.plugins.finder;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.openbp.cockpit.plugins.finder.treemodel.GenericNode;
import org.openbp.cockpit.plugins.finder.treemodel.LeafNode;
import org.openbp.common.icon.FlexibleSize;
import org.openbp.guiclient.model.item.ItemIconMgr;
import org.openbp.jaspira.gui.StdIcons;

/**
 * This is the renderer for the tree table. It modifies the icons of group and results
 * and the color of the results depending on the severity. The tree cell renderer is responsible
 * for the first column, the following columns are rendered by the table cell renderer.
 *
 * @author Baumgartner Michael
 */
public class FinderResultRenderer extends DefaultTreeCellRenderer
{
	//////////////////////////////////////////////////
	// @@ Render methods
	//////////////////////////////////////////////////

	/**
	 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

		if (c instanceof JLabel)
		{
			// Set the right icon
			JLabel label = (JLabel) c;
			Icon icon = null;
			String itemType = (String) ((GenericNode) value).getProperty(RefStrategy.ITEMTYPE_KEY);
			if (value instanceof LeafNode)
			{
				// Load the icon for the results, if there is no icon use
				// the icon of the item found by the finder
				icon = FinderResultPlugin.finderResultIcon;
				if (icon == null)
				{
					if (itemType == null)
						itemType = (String) ((GenericNode) value).getProperty(RefStrategy.REFERENCE_ITEMTYPE_KEY);
					icon = ItemIconMgr.getInstance().getIcon(itemType, FlexibleSize.SMALL);
				}
			}
			else
			{
				if (itemType != null)
					icon = ItemIconMgr.getInstance().getIcon(itemType, FlexibleSize.SMALL);
				else
					icon = expanded ? StdIcons.openFolderIcon : StdIcons.closedFolderIcon;
			}
			label.setIcon(icon);
		}
		return c;
	}
}
