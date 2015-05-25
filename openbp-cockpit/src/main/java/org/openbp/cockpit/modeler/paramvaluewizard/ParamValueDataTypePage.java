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
package org.openbp.cockpit.modeler.paramvaluewizard;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openbp.core.model.Model;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.type.ComplexTypeItem;
import org.openbp.core.model.item.type.DataTypeItem;
import org.openbp.guiclient.model.item.itemtree.ItemTree;
import org.openbp.guiclient.model.item.itemtree.ItemTreeState;
import org.openbp.swing.plaf.sky.ShadowBorder;

/**
 * Param value string page.
 *
 * @author Heiko Erhardt
 */
public class ParamValueDataTypePage extends ParamValuePage
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
	 * @param wizard Wizard that owns the page
	 * @param param Parameter to edit
	 */
	public ParamValueDataTypePage(ParamValueWizard wizard, NodeParam param)
	{
		// This will set up the UI
		super(wizard, param);

		itemTree = new ItemTree();

		// We may select multiple objects
		itemTree.setSelectionMode(ItemTree.SELECTION_SINGLE);
		itemTree.setShowGroups(false);

		// Display models and data types
		itemTree.setSupportedItemTypes(new String [] { ItemTypes.MODEL, ItemTypes.TYPE, });
		itemTree.setSelectableItemTypes(new String [] { ItemTypes.TYPE, });

		// Set the initial state of the item tree
		ItemTreeState state = new ItemTreeState();

		// Open the tree at the model of the target item
		Model currentModel = getParam().getProcess().getOwningModel();
		state.addExpandedQualifier(currentModel.getQualifier());

		// Select the current data type if present
		if (getExpression() != null)
		{
			// Try to determine the new data type and perform a default configuration
			// of the member according to its new type
			DataTypeItem type = (DataTypeItem) currentModel.resolveItemRef(getExpression(), ItemTypes.TYPE);
			if (type != null)
			{
				state.addSelectedQualifier(type.getQualifier());
			}
			else
			{
				state.addSelectedQualifier(currentModel.getQualifier());
			}
		}

		itemTree.rebuildTree();
		itemTree.expand(1);
		itemTree.restoreState(state);

		JPanel valuePanel = getValuePanel();
		JScrollPane sp = new JScrollPane(itemTree);
		sp.setBorder(new ShadowBorder());
		valuePanel.add(sp, BorderLayout.CENTER);
	}

	/**
	 * Applys the parameter value entered by the user to the parameter.
	 */
	public void apply()
	{
		List selection = itemTree.getSelectedObjects();
		if (selection != null)
		{
			Object o = selection.get(0);
			if (o instanceof ComplexTypeItem)
			{
				ComplexTypeItem type = (ComplexTypeItem) o;

				String typeName = getParam().getProcess().determineItemRef(type);
				getParam().setExpression("\"" + typeName + "\"");
			}
		}
	}
}
