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
package org.openbp.cockpit.modeler.figures.process;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

import org.openbp.cockpit.itemeditor.ItemCreationUtil;
import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.figures.VisualElementEvent;
import org.openbp.cockpit.modeler.util.FigureUtil;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.PlaceholderNode;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.guiclient.model.item.itemtree.ItemSelectionDialog;
import org.openbp.guiclient.model.item.itemtree.ItemTree;
import org.openbp.guiclient.model.item.itemtree.ItemTreeState;
import org.openbp.guiclient.util.ClientFlavors;
import org.openbp.jaspira.gui.interaction.BasicTransferable;
import org.openbp.jaspira.plugin.ApplicationUtil;
import org.openbp.jaspira.util.StandardFlavors;
import org.openbp.swing.SwingUtil;
import org.openbp.swing.components.JMsgBox;

/**
 * Placeholder figure.
 *
 * @author Heiko Erhardt
 */
public class PlaceholderNodeFigure extends MultiSocketNodeFigure
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Flag if this is a placeholder node that is referencing another node */
	private boolean isReferencingPlaceholder;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public PlaceholderNodeFigure()
	{
	}

	//////////////////////////////////////////////////
	// @@ VisualElement overrides
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#handleEvent(VisualElementEvent event)
	 */
	public boolean handleEvent(VisualElementEvent event)
	{
		// TODONOW
		if (event.type == VisualElementEvent.DOUBLE_CLICK)
		{
			PlaceholderNode placeholderNode = (PlaceholderNode) node;

			if (placeholderNode.getReferencePath() == null)
			{
				// Double-clicking a placeholder w/o reference path means creating a reference to a process or process element.
				// We may either create a new one or choose an existing one.
				// Ask the user what he wants:

				ResourceCollection res = getDrawing().getEditor().getPluginResourceCollection();
				String title = res.getRequiredString("placeholder.referencedialog.title");
				String text = res.getRequiredString("placeholder.referencedialog.text");

				JMsgBox msgBox = new JMsgBox(null, title, text, JMsgBox.TYPE_YESNOCANCEL);
				msgBox.setResource(res);
				msgBox.setResourcePrefix("placeholder.referencedialog.");

				msgBox.initDialog();
				SwingUtil.show(msgBox);

				int choice = msgBox.getUserChoice();

				Model model = node.getOwningModel();

				if (choice == JMsgBox.TYPE_YES)
				{
					// Yes means create a new process
					final Item item = ItemCreationUtil.createItem(model, placeholderNode.getName(), placeholderNode.getDisplayName(), ItemTypes.PROCESS, null);
					if (item != null)
					{
						assignPlaceholderReference(item);

						// Open the new process
						SwingUtilities.invokeLater(new Runnable()
						{
							public void run()
							{
								getDrawing().getEditor().fireEvent("plugin.association.open", new BasicTransferable(item));
							}
						});
					}
				}
				else if (choice == JMsgBox.TYPE_NO)
				{
					// Yes means reference an existing process
					ItemSelectionDialog dlg = new ItemSelectionDialog(ApplicationUtil.getActiveWindow(), true);

					String dlgTitle = res.getRequiredString("placeholder.referencedialog.select");
					dlg.setTitle(dlgTitle);

					// We may select a single object only
					dlg.setSelectionMode(ItemTree.SELECTION_SINGLE);
					dlg.setShowGroups(false);

					dlg.setSupportedItemTypes(new String [] { ItemTypes.MODEL, ItemTypes.PROCESS });
					dlg.setSelectableItemTypes(new String [] { ItemTypes.PROCESS });
					dlg.setSupportedObjectClasses(new Class [] { Node.class });
					dlg.setSelectableObjectClasses(new Class [] { Node.class });

					ItemTreeState state = new ItemTreeState();
					state.addExpandedQualifier(model.getQualifier());

					// Build the tree, expanding the first level and the currently selected item
					dlg.rebuildTree();
					dlg.expand(1);
					dlg.restoreState(state);

					// Show the dialog
					dlg.setVisible(true);

					List selection = dlg.getSelectedObjects();
					if (selection != null)
					{
						ModelObject selectedObject = (ModelObject) selection.get(0);
						assignPlaceholderReference(selectedObject);
					}
				}

				return true;
			}
		}

		return super.handleEvent(event);
	}

	//////////////////////////////////////////////////
	// @@ UpdatableFigure overrides
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.NodeFigure#updateFigure()
	 */
	public void updateFigure()
	{
		super.updateFigure();

		boolean hasReference = ((PlaceholderNode) node).getReferencePath() != null;
		if (hasReference != isReferencingPlaceholder)
		{
			// Update the status
			isReferencingPlaceholder = hasReference;

			// Referencing placeholder nodes usually have another color than non-referencing nodes.
			// Reinitialize the presentation figure and update the child figures.
			FigureUtil.updateSkin(this);
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ InteractionClient helper overrides
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Determines if none of the flavors in the list is accepted by DnD actions.
	 *
	 * @param flavors Flavors to check
	 * @param data Transferred object
	 * @param mouseEvent Mouse event of the DnD drop action
	 * @return The region id that should be used for the accepted flavor or null if the flavor is not accepted
	 */
	protected String getDropRegionIdForFlavor(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		String regionId = super.getDropRegionIdForFlavor(flavors, data, mouseEvent);

		if (regionId == null)
		{
			if (flavors.contains(ClientFlavors.PROCESS_ITEM) || flavors.contains(ClientFlavors.NODE) || flavors.contains(ClientFlavors.MODEL_QUALIFIER))
			{
				regionId = node.getQualifier().toString();
			}
		}

		return regionId;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#importData(Object, Transferable, Point)
	 */
	public boolean importData(Object regionId, Transferable data, Point p)
	{
		if (super.importData(regionId, data, p))
			return true;

		p = SwingUtil.convertFromGlassCoords(p, getDrawing().getView());

		try
		{
			if (data.isDataFlavorSupported(ClientFlavors.PROCESS_ITEM)
				|| data.isDataFlavorSupported(ClientFlavors.NODE))
			{
				// This is a placeholder node;
				// dragging a process or process node to the node means creating a reference to it.
				ModelObject mo = (ModelObject) data.getTransferData(StandardFlavors.OBJECT);
				assignPlaceholderReference(mo);
				getDrawing().getEditor().focusPlugin();
				return true;
			}
			else if (data.isDataFlavorSupported(ClientFlavors.MODEL_QUALIFIER))
			{
				ModelQualifier qualifier = (ModelQualifier) data.getTransferData(ClientFlavors.MODEL_QUALIFIER);
				assignPlaceholderReference(qualifier.toString());
				getDrawing().getEditor().focusPlugin();
				return true;
			}
		}
		catch (UnsupportedFlavorException e)
		{
		}
		catch (IOException e)
		{
		}

		return false;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Assigns a reference to the given model object to this placeholder node figure.
	 *
	 * @param mo Model object to reference
	 */
	protected void assignPlaceholderReference(ModelObject mo)
	{
		String referencePath = null;

		Model model = getDrawing().getProcess().getModel();
		if (model != null)
		{
			referencePath = model.determineObjectRef(mo);
		}

		if (referencePath == null)
		{
			referencePath = mo.getQualifier().toString();
		}

		assignPlaceholderReference(referencePath);
	}

	/**
	 * Assigns a reference to the given model object to this placeholder node figure.
	 *
	 * @param referencePath Reference path to assign
	 */
	public void assignPlaceholderReference(String referencePath)
	{
		ProcessDrawing drawing = getDrawing();

		drawing.getEditor().startUndo("Create placeholder reference");

		// Assign the object reference to the placeholder
		PlaceholderNode placeholderNode = (PlaceholderNode) node;
		placeholderNode.setReferencePath(referencePath);

		// Make the skin reflect the change
		updateFigure();

		// Redisplay the placeholder properties
		drawing.getView().updateSelection();

		drawing.getEditor().endUndo();

		drawing.getView().redraw();
	}

	// TODO Feature 5 Method currently not used, should be private then, is protected to prevent compiler warning
	protected void substitutePlaceholderNode(Node newNode, boolean newNodeFlag)
	{
		// TODO Feature 4: Placeholder substitution: Implement
		ProcessDrawing drawing = getDrawing();
		ProcessItem process = drawing.getProcess();

		// Save any geometry changes
		drawing.encodeGeometry();

		drawing.getEditor().startUndo("Replace placeholder");

		// Add the new node to the process
		process.addNode(node);
		node.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.SYNC_LOCAL_REFNAMES);

		// Try to connect the control and data links from/to the placeholder to the new node

		// If the orientation of the new node figure and the drawing don't match,
		// rotate the figure
		PlaceholderNode plNode = (PlaceholderNode) getNode();

		// Try to connect

		// Iterate all sockets of the placeholder
		for (Iterator itSockets = plNode.getSockets(); itSockets.hasNext();)
		{
			// TODO Feature 6 NodeSocket plSocket = (NodeSocket) itSockets.next();
		}

		// Reinitialize the drawing
		drawing.setProcess(process);

		drawing.getEditor().endUndo();

		drawing.getView().redraw();
	}
}
