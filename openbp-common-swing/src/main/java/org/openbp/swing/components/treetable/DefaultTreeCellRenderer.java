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
package org.openbp.swing.components.treetable;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingConstants;

/**
 * Default tree cell renderer used by the {@link JTreeTable} to renderer the labels for the
 * nodes of the tree.
 *
 * @author Erich Lauterbach
 */
public class DefaultTreeCellRenderer extends javax.swing.tree.DefaultTreeCellRenderer
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Tree Table that uses this tree cell rendered for it's tree. */
	public JTreeTable treeTable;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default Constructor.
	 *
	 * @param treeTable The {@link JTreeTable} using this TreeCellRenderer
	 */
	public DefaultTreeCellRenderer(JTreeTable treeTable)
	{
		this.treeTable = treeTable;
	}

	/**
	 * Overriden method. See javax.swing.tree.DefaultTreeCellRenderer.getTreeCellRendererComponent.
	 * The Label returned to the tree will have the height adjusted according to the
	 * largest component contained in the {@link TreeTableNode}
	 *
	 * @nowarn
	 */
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		Component component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

		if (component instanceof JLabel)
		{
			JLabel label = (JLabel) component;
			if (value instanceof TreeTableNode)
			{
				TreeTableNode node = (TreeTableNode) value;

				String text = node.getNodeText();
				label.setText(text);
				label.setToolTipText(text);

				Dimension labelMinSize = label.getMinimumSize();
				Dimension labelPrefSize = label.getPreferredSize();

				int defaultRowHeight = treeTable.getDefaultRowHeight();
				int nodeHeight = node.getPreferredSize().height;

				Icon icon = node.isLeaf() ? leafIcon : expanded ? openIcon : closedIcon;
				int iconHeight = icon != null ? icon.getIconHeight() : 0;

				// The actual height of the cell is the maximum of the height of the label's min size,
				// the preferred node height, the default row height and the icon size
				int height = labelMinSize.height;
				height = Math.max(height, nodeHeight);
				height = Math.max(height, defaultRowHeight);
				height = Math.max(height, iconHeight);

				int width = Math.max(labelMinSize.width, labelPrefSize.width);

				// Work around for bug in JTree
				width -= 3;

				label.setPreferredSize(new Dimension(width, height));

				// Save the computed size as last node size, so the table cell renderer can access it
				// int JTreeTable.sizeCellsToFit () to prevent tree and table getting out of sync
				node.setLastHeight(height);

				// Make the text appear at the top of large cells
				label.setVerticalAlignment(SwingConstants.NORTH);
			}
			return label;
		}

		return component;
	}
}
