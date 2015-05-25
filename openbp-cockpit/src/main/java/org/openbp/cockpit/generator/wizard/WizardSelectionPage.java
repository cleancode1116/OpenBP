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

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.openbp.cockpit.generator.Generator;
import org.openbp.cockpit.generator.GeneratorContext;
import org.openbp.cockpit.generator.GeneratorSettings;
import org.openbp.common.icon.MultiIcon;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.jaspira.gui.StdIcons;
import org.openbp.jaspira.gui.wizard.JaspiraWizardPage;
import org.openbp.swing.components.treetable.DefaultTreeCellRenderer;
import org.openbp.swing.components.treetable.DefaultTreeTableModel;
import org.openbp.swing.components.treetable.JTreeTable;
import org.openbp.swing.components.wizard.SequenceManager;
import org.openbp.swing.components.wizard.WizardEvent;

/**
 * Wizard selection page.
 *
 * @author Heiko Erhardt
 */
public class WizardSelectionPage extends JaspiraWizardPage
	implements TreeSelectionListener
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Tree table model */
	private DefaultTreeTableModel treeModel;

	/** Used to represent the model */
	private JTreeTable treeTable;

	/** Table header */
	private static String [] tableHeader;

	/** Default description */
	private String defaultDescription;

	/** Root node of the generator tree */
	private GeneratorNode rootNode = new GeneratorNode();

	/**
	 * Group node table that maps functional groups (Strings) to group nodes
	 * ({@link GeneratorNode} objects)
	 */
	private Map groupNodes = new HashMap();

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param wizard Wizard that owns the page
	 */
	public WizardSelectionPage(GeneratorWizard wizard)
	{
		super(wizard);

		// Set up the tree table displaying the generation options
		rootNode = new GeneratorNode();
		treeModel = new DefaultTreeTableModel(rootNode);

		if (tableHeader == null)
		{
			ResourceCollection resourceCollection = wizard.getResource();
			tableHeader = new String [] { resourceCollection.getRequiredString("wizard.selection.header1"), resourceCollection.getRequiredString("wizard.selection.header2"), };
		}
		treeModel.setColumnHeader(tableHeader);

		treeTable = new JTreeTable(treeModel);
		treeTable.setRootVisible(false);

		treeTable.getTree().setCellRenderer(new GeneratorTreeCellRenderer(treeTable));
		treeTable.getTree().addTreeSelectionListener(this);

		// Construct the UI
		JPanel cp = getContentPanel();

		// Add the tree table to the page
		JScrollPane sp = new JScrollPane(treeTable);
		cp.add(sp, BorderLayout.CENTER);
	}

	/**
	 * Handles a wizard event caused by this wizard page.
	 *
	 * @param event Event to handle
	 */
	public void handleWizardEvent(WizardEvent event)
	{
		if (event.eventType == WizardEvent.SHOW)
		{
			if (defaultDescription == null)
			{
				defaultDescription = getDescription();
			}

			// Select the tree node corresponding to the currently selected generator
			GeneratorNode currentNode = null;
			GeneratorContext context = getContext();
			Generator generator = context.getSelectedGenerator();
			if (generator != null)
			{
				currentNode = findNode(rootNode, generator);
			}

			if (currentNode != null)
			{
				treeTable.selectNode(currentNode);
			}
			else
			{
				treeTable.selectDefaultCell();
			}

			updatePageStatus();
		}
	}

	/**
	 * Finds the tree node corresponding to the given generator, starting from the given node.
	 *
	 * @param node Node to start the search from
	 * @param generator Generator to search
	 * @return The node or null if not found
	 */
	private GeneratorNode findNode(GeneratorNode node, Generator generator)
	{
		if (node.getGenerator() == generator)
			return node;

		int n = node.getChildCount();
		for (int i = 0; i < n; ++i)
		{
			GeneratorNode childNode = (GeneratorNode) node.getChildAt(i);
			childNode = findNode(childNode, generator);
			if (childNode != null)
				return childNode;
		}

		return null;
	}

	/**
	 * Adds a generator.
	 * @param generator The generator to add
	 */
	public void addGenerator(Generator generator)
	{
		String group = generator.getFunctionalGroup();

		GeneratorNode groupNode = (GeneratorNode) groupNodes.get(group);
		if (groupNode == null)
		{
			groupNode = new GeneratorNode(group);
			rootNode.addChild(groupNode);
			groupNodes.put(group, groupNode);
		}

		groupNode.addChild(new GeneratorNode(generator));
	}

	/**
	 * Expands the tree of generators.
	 */
	public void expandTree()
	{
		treeTable.expandAll(true);
	}

	//////////////////////////////////////////////////
	// @@ Tree selection listener
	//////////////////////////////////////////////////

	/**
	 * Tree selection has changed.
	 * @nowarn
	 */
	public void valueChanged(TreeSelectionEvent e)
	{
		GeneratorContext context = getContext();

		Generator generator = null;

		// Display either the description of the selected element or the default description
		TreePath path = e.getNewLeadSelectionPath();
		if (path != null)
		{
			GeneratorNode node = (GeneratorNode) path.getLastPathComponent();

			generator = node.getGenerator();
			if (generator == context.getSelectedGenerator())
			{
				// No change
				return;
			}
		}

		context.setSelectedGenerator(generator);

		// Remember that a (re-)generation should be performed when finishing the wizard after changing the generator
		context.setNeedGeneration(true);

		((GeneratorWizard) getWizard()).updateGeneratorPageSequence();

		updatePageStatus();
	}

	/**
	 * Updates the status (description, next/finish buttons) of the page.
	 */
	private void updatePageStatus()
	{
		GeneratorContext context = getContext();
		Generator generator = context.getSelectedGenerator();

		String description = generator != null ? generator.getDescription() : defaultDescription;
		setDescription(description);

		canFinish = false;
		canMoveForward = false;
		if (generator != null)
		{
			if (!context.isInvalidGenerator())
			{
				SequenceManager manager = getWizard().getManager();

				if (manager.getNext() != null)
				{
					// We can move to the next page
					canMoveForward = true;
				}
				else
				{
					// No next page, we can finish here
					canFinish = true;
				}
			}
		}

		updateNavigator();
	}

	//////////////////////////////////////////////////
	// @@ Default tree cell renderer overrides
	//////////////////////////////////////////////////

	/**
	 * Overridden default tree cell renderer to define the icons.
	 */
	private class GeneratorTreeCellRenderer extends DefaultTreeCellRenderer
	{
		//////////////////////////////////////////////////
		// @@ Construction
		//////////////////////////////////////////////////

		/**
		 * Constructor.
		 *
		 * @param treeTable The tree table
		 */
		private GeneratorTreeCellRenderer(JTreeTable treeTable)
		{
			super(treeTable);
		}

		//////////////////////////////////////////////////
		// @@ Default tree cell renderer overridden methods
		//////////////////////////////////////////////////

		/**
		 * Overridden method to define the icons of a tree node.
		 * @nowarn
		 */
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			if (c instanceof JLabel)
			{
				MultiIcon icon = null;

				if (row == 0)
				{
					icon = expanded ? StdIcons.openFolderIcon : StdIcons.closedFolderIcon;
				}
				else
				{
					// Get the node by the row number
					TreePath path = tree.getPathForRow(row);
					if (path != null)
					{
						GeneratorNode node = (GeneratorNode) path.getLastPathComponent();
						Generator generator = node.getGenerator();

						if (generator == null)
						{
							// Use folder icons for non-leaf nodes
							icon = expanded ? StdIcons.openFolderIcon : StdIcons.closedFolderIcon;
						}
					}
				}

				((JLabel) c).setIcon(icon);
			}

			return c;
		}
	}

	//////////////////////////////////////////////////
	// @@ Convenience methods
	//////////////////////////////////////////////////

	/**
	 * Gets the generator context.
	 * @nowarn
	 */
	public GeneratorContext getContext()
	{
		return ((GeneratorWizard) getWizard()).getContext();
	}

	/**
	 * Gets the currently selected generator.
	 *
	 * @return The generator or null if no generator has been selected yet
	 */
	public Generator getGenerator()
	{
		return ((GeneratorWizard) getWizard()).getContext().getSelectedGenerator();
	}

	/**
	 * Gets the generator settings.
	 * @nowarn
	 */
	public GeneratorSettings getGeneratorSettings()
	{
		return ((GeneratorWizard) getWizard()).getContext().getGeneratorSettings();
	}
}
