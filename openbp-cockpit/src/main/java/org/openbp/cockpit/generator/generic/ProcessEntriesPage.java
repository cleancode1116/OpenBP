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
package org.openbp.cockpit.generator.generic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JScrollPane;

import org.openbp.cockpit.generator.wizard.GeneratorWizard;
import org.openbp.cockpit.generator.wizard.WizardCustomPage;
import org.openbp.core.model.Model;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.InitialNode;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.guiclient.model.item.itemtree.ItemTree;
import org.openbp.guiclient.model.item.itemtree.ItemTreeEvent;
import org.openbp.guiclient.model.item.itemtree.ItemTreeListener;
import org.openbp.guiclient.model.item.itemtree.ItemTreeState;
import org.openbp.swing.components.wizard.WizardEvent;
import org.openbp.swing.plaf.sky.ShadowBorder;

/**
 * Wizard page that displays a tree of process initial nodes.
 * This page usually serves as base class for process entry selection
 * e. g. for navigation bar visuals, web service entries etc.
 *
 * @author Heiko Erhardt
 */
public class ProcessEntriesPage extends WizardCustomPage
	implements ItemTreeListener
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Initial node browser */
	private ItemTree itemTree;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param wizard Wizard that owns the page
	 */
	public ProcessEntriesPage(GeneratorWizard wizard)
	{
		super(wizard);

		// Construct the item browser
		itemTree = new ItemTree();
		itemTree.addItemTreeListener(this);

		// We may select multiple objects
		itemTree.setSelectionMode(ItemTree.SELECTION_MULTI);
		itemTree.setShowGroups(false);

		// Display models, processes and initial nodes
		itemTree.setSupportedItemTypes(new String [] { ItemTypes.MODEL, ItemTypes.PROCESS, });
		itemTree.setSupportedObjectClasses(new Class [] { Model.class, ProcessItem.class, InitialNode.class, });

		// Select initial nodes only
		itemTree.setSelectableObjectClasses(new Class [] { InitialNode.class, });

		ItemTreeState state = new ItemTreeState();

		Item item = wizard.getContext().getItem();
		Model currentModel = item.getOwningModel();

		if (item instanceof ProcessItem)
		{
			// Open the tree at the target item
			state.addExpandedQualifier(item.getQualifier());
		}
		else
		{
			// Open the tree at the model of the target item
			state.addExpandedQualifier(currentModel.getQualifier());
		}

		// Select the previously selected initial nodes if present
		ProcessEntrySettings settings = (ProcessEntrySettings) getGeneratorSettings();
		List entries = settings.getEntryList();
		if (entries != null)
		{
			int n = entries.size();
			for (int i = 0; i < n; ++i)
			{
				ProcessEntry entry = (ProcessEntry) entries.get(i);

				InitialNode initialNode = entry.getInitialNode();
				if (initialNode != null)
				{
					state.addSelectedQualifier(initialNode.getQualifier());
				}
			}
		}
		else
		{
			state.addSelectedQualifier(currentModel.getQualifier());
		}

		itemTree.rebuildTree();
		itemTree.expand(1);
		itemTree.restoreState(state);

		JScrollPane sp = new JScrollPane(itemTree);
		sp.setBorder(new ShadowBorder());

		getContentPanel().add(sp);
	}

	/**
	 * Gets the initial node browser.
	 * @nowarn
	 */
	public ItemTree getItemTree()
	{
		return itemTree;
	}

	/**
	 * Handles a wizard event caused by this wizard page.
	 *
	 * @param event Event to handle
	 */
	public void handleWizardEvent(WizardEvent event)
	{
		if (event.eventType == WizardEvent.BACK || event.eventType == WizardEvent.NEXT || event.eventType == WizardEvent.FINISH)
		{
			// Get the selected objects
			List initialNodes = itemTree.getSelectedObjects();

			// Add them as entry list to the property object
			ProcessEntrySettings settings = (ProcessEntrySettings) getGeneratorSettings();

			// Note: We do not clear and rebuild the entry list in order to prevent loosing
			// entry customizations the user may have done for an existing object
			// Instead, we update an existing entry list

			// First, remove all entries from the existing entry list that haven't been selected
			List entryList = settings.getEntryList();
			if (entryList != null)
			{
				if (initialNodes == null)
				{
					entryList = null;
				}
				else
				{
					for (Iterator it = entryList.iterator(); it.hasNext();)
					{
						ProcessEntry entry = (ProcessEntry) it.next();
						InitialNode node = entry.getInitialNode();
						if (!initialNodes.contains(node))
						{
							it.remove();
						}
					}

					if (entryList.size() == 0)
					{
						entryList = null;
					}
				}
			}
			else
			{
				if (initialNodes != null)
				{
					entryList = new ArrayList();
				}
			}

			// Now add all new initial nodes
			if (initialNodes != null && entryList != null)
			{
				int n = initialNodes.size();
				for (int i = 0; i < n; ++i)
				{
					InitialNode node = (InitialNode) initialNodes.get(i);

					if (!containsInitialNode(entryList, node))
					{
						ProcessEntry entry = new ProcessEntry();
						entry.setModel(settings.getModel());
						entry.setInitialNode(node);
						entry.setName(node.getName());
						entry.setDisplayName(node.getDisplayName());
						entryList.add(entry);
					}
				}
			}

			settings.setEntryList(entryList);
		}
	}

	/**
	 * Checks if the entry list contains an entry referencing the given initial node.
	 *
	 * @param entryList List of {@link ProcessEntry} objects to search
	 * @param node Initial node to search
	 * @return
	 * true: The list contains such an entry.<br>
	 * false: The initial node is not present in the list
	 */
	public static boolean containsInitialNode(List entryList, InitialNode node)
	{
		int n = entryList.size();
		for (int i = 0; i < n; ++i)
		{
			ProcessEntry entry = (ProcessEntry) entryList.get(i);
			if (entry.getInitialNode() == node)
				return true;
		}
		return false;
	}

	//////////////////////////////////////////////////
	// @@ ItemTreeListener implementation
	//////////////////////////////////////////////////

	/**
	 * Called when an item tree event has happened.
	 *
	 * @param e Item tree event holding the event information
	 */
	public void handleItemTreeEvent(ItemTreeEvent e)
	{
		if (e.eventType == ItemTreeEvent.SELECTION_CHANGED)
		{
			// Update Ok button status according to selection
			List selection = itemTree.getSelectedObjects();
			if (selection != null)
			{
				canMoveForward = true;
				if (!getContext().isNewItem())
				{
					canFinish = true;
				}
			}
			else
			{
				canMoveForward = false;
			}
			updateNavigator();

			// Remember that a (re-)generation should be performed when finishing the wizard after changing settings
			getContext().setNeedGeneration(true);
		}
	}
}
