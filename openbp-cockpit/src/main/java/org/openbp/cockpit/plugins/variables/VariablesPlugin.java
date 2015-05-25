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
package org.openbp.cockpit.plugins.variables;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.openbp.cockpit.modeler.Modeler;
import org.openbp.cockpit.modeler.ModelerColors;
import org.openbp.cockpit.modeler.figures.process.ProcessVariableConnection;
import org.openbp.common.CommonUtil;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.generic.description.DescriptionObject;
import org.openbp.common.icon.FlexibleSize;
import org.openbp.common.icon.MultiIcon;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.util.CopyUtil;
import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.process.DataLink;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessVariable;
import org.openbp.core.model.item.process.ProcessVariableImpl;
import org.openbp.core.model.item.type.DataTypeItem;
import org.openbp.guiclient.model.item.ItemIconMgr;
import org.openbp.guiclient.util.ClientFlavors;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.gui.interaction.BasicDropRegion;
import org.openbp.jaspira.gui.interaction.BasicTransferable;
import org.openbp.jaspira.gui.interaction.DragInitiator;
import org.openbp.jaspira.gui.interaction.DragOrigin;
import org.openbp.jaspira.gui.interaction.InteractionClient;
import org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.jaspira.propertybrowser.PropertyBrowser;
import org.openbp.jaspira.propertybrowser.PropertyBrowserEvent;
import org.openbp.jaspira.propertybrowser.PropertyBrowserImpl;
import org.openbp.jaspira.propertybrowser.PropertyBrowserListener;
import org.openbp.jaspira.propertybrowser.SaveStrategy;
import org.openbp.jaspira.propertybrowser.nodes.AbstractNode;
import org.openbp.jaspira.propertybrowser.nodes.ObjectNode;

/**
 * This plugin displays the variables used by a process in a tree table.
 * It supports drag and drop between the tree table and the workspace (i. e. node socket parameters).
 *
 * @author Heiko Erhardt
 */
public class VariablesPlugin extends AbstractVisiblePlugin
	implements SaveStrategy, PropertyBrowserListener, InteractionClient, DragOrigin, FocusListener
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Current modeler */
	private Modeler currentModeler;

	/** Current process */
	private ProcessItem currentProcess;

	/** Container object for the list of variables */
	private VariablesContainer variablesContainer;

	/** Property browser */
	private PropertyBrowserImpl propertyBrowser;

	/** Region id for dnd */
	private static final String MAINREGION = "main";

	/** Used to store the image of a drag operation */
	private MultiIcon dragImage;

	//////////////////////////////////////////////////
	// @@ Init/Activate
	//////////////////////////////////////////////////

	public String getResourceCollectionContainerName()
	{
		return "plugin.modeler";
	}

	/**
	 * @copy org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin.initializeComponents
	 */
	protected void initializeComponents()
	{
		propertyBrowser = new PropertyBrowserImpl(this, null);
		propertyBrowser.setRootVisible(false);

		// TODO Feature 5: Turn on again save immediately and restore current position after update
		// propertyBrowser.setSaveImmediately (true);

		getContentPane().add(new JScrollPane(propertyBrowser), BorderLayout.CENTER);

		propertyBrowser.addPropertyBrowserListener(this);

		addPluginFocusListener(this);
		DragInitiator.makeDraggable(propertyBrowser, this);
	}

	/**
	 * Refreshes the inspector view.
	 */
	protected void refresh()
	{
	}

	//////////////////////////////////////////////////
	// @@ PropertyBrowserListener implementation
	//////////////////////////////////////////////////

	public void handlePropertyBrowserEvent(PropertyBrowserEvent e)
	{
		switch (e.eventType)
		{
		case PropertyBrowserEvent.FOCUS_GAINED:
		case PropertyBrowserEvent.SELECTION_CHANGED:

			// Communicate the HTML tooltip text to the info panel
			if (e.node != null)
			{
				Object eventArg = null;
				if (e.node instanceof ObjectNode)
					eventArg = ((ObjectNode) e.node).getObject();
				else
					eventArg = e.node.getColumnValue(0);

				if (eventArg instanceof DescriptionObject)
				{
					fireEvent(new JaspiraEvent(this, "plugin.infopanel.setinfotext", eventArg));
				}
			}

			break;
		}
	}

	//////////////////////////////////////////////////
	// @@ FocusListener implementation
	//////////////////////////////////////////////////

	/**
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	public void focusGained(FocusEvent e)
	{
	}

	/**
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	public void focusLost(FocusEvent e)
	{
		propertyBrowser.saveObject();

		fireEvent(new JaspiraEvent(this, "plugin.infopanel.clearinfotext", null));
	}

	//////////////////////////////////////////////////
	// @@ SaveStrategy implementation
	//////////////////////////////////////////////////

	/**
	 * This method is called by the property browser when the list of variables needs to be saved.
	 *
	 * @param editor Property browser
	 * @return
	 * true: Save successful. The wizard will continue to the next page.<br>
	 * false: Save failed. The page will remain active.
	 */
	public boolean executeSave(PropertyBrowser editor)
	{
		currentModeler.startUndo("Edit Process Variables");

		variablesContainer = (VariablesContainer) editor.getModifiedObject();

		// Retrive the parameter list from the variables container
		List newVariables = variablesContainer.getProcessVariableList();
		updateProcessVariables(newVariables);

		// This will remove any links to variables that have been removed
		fireEvent("modeler.view.updatedrawing");
		fireEvent("modeler.process.modified");
		currentModeler.endUndo();

		// Re-initialize
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				// Redisplay
				showParams();
			}
		});

		return true;
	}

	/**
	 * Updates the process variables of the process according to the current state of the process variable list.
	 *
	 * @param newVars New vars
	 */
	private void updateProcessVariables(List newVars)
	{
		// Unlink the current process variables first
		List oldVars = currentProcess.getProcessVariableList();

		// DEBUG System.out.println ();
		// DEBUG new Dumper ().dump ("Old", oldVars);
		// DEBUG new Dumper ().dump ("New", newVars);

		if (oldVars != null)
		{
			ArrayList toDelete = null;

			// Update existing ones and remove deleted ones
			for (Iterator itVar = oldVars.iterator(); itVar.hasNext();)
			{
				ProcessVariable oldVar = (ProcessVariable) itVar.next();

				ProcessVariable newVar = findVariable(newVars, oldVar);
				if (newVar != null)
				{
					// DEBUG System.out.println ("Updating " + oldVar.getName () + " to " + newVar.getName ());
					try
					{
						// Existing one
						oldVar.copyFrom(newVar, Copyable.COPY_FIRST_LEVEL);
					}
					catch (CloneNotSupportedException e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					// This variable has been deleted

					// DEBUG System.out.println ("Deleting " + oldVar.getName ());

					List dataLinks = null;
					try
					{
						// Make a copy of the data link list, it might get modified during the iteration
						dataLinks = (List) CopyUtil.copyCollection(currentProcess.getDataLinkList(), CopyUtil.CLONE_NONE);

						// Delete each data link from/to it by releasing the associated figure
						for (Iterator itLinks = dataLinks.iterator(); itLinks.hasNext();)
						{
							DataLink link = (DataLink) itLinks.next();

							if (link.getSourceParam() == oldVar || link.getTargetParam() == oldVar)
							{
								// DEBUG System.out.println ("Removing data link " + link.getName () + " from " + link.getSourceParam ().getPath () + " to " + link.getTargetParam ().getPath ());
								ProcessVariableConnection con = (ProcessVariableConnection) link.getRepresentation();
								con.release();

								// Remove the link itself
								currentProcess.removeDataLink(link);
							}
						}
					}
					catch (CloneNotSupportedException e)
					{
						// Doesn't happen
						e.printStackTrace();
					}

					// Save the variable to delete in order to prevent ConcurrentModificationException
					if (toDelete == null)
						toDelete = new ArrayList();
					toDelete.add(oldVar);
				}
			}

			if (toDelete != null)
			{
				for (Iterator it = toDelete.iterator(); it.hasNext();)
				{
					ProcessVariable oldVar = (ProcessVariable) it.next();

					// Remove the process variable itself
					currentProcess.removeProcessVariable(oldVar);
				}
			}
		}

		// Add new ones
		if (newVars != null)
		{
			for (Iterator itVar = newVars.iterator(); itVar.hasNext();)
			{
				ProcessVariable newVar = (ProcessVariable) itVar.next();

				ProcessVariable oldVar = findVariable(oldVars, newVar);
				if (oldVar == null)
				{
					// DEBUG System.out.println ("Adding " + newVar.getName ());

					// Add new one
					try
					{
						ProcessVariable toAdd = (ProcessVariable) newVar.clone();

						// Create a temporary reference id in order to prevent match by CommonUtil.equalsNull, see below
						toAdd.setTmpReference("!");

						currentProcess.addProcessVariable(toAdd);
					}
					catch (CloneNotSupportedException e)
					{
					}
				}
			}
		}

		// Clear references to the original object
		List vars = currentProcess.getProcessVariableList();
		adjustTemporaryReferences(vars, false);

		currentProcess.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.SYNC_LOCAL_REFNAMES);
	}

	ProcessVariable findVariable(List list, ProcessVariable reference)
	{
		if (list != null)
		{
			for (Iterator it = list.iterator(); it.hasNext();)
			{
				ProcessVariable var = (ProcessVariable) it.next();

				if (CommonUtil.equalsNull(var.getTmpReference(), reference.getTmpReference()))
					return var;
			}
		}
		return null;
	}

	void adjustTemporaryReferences(List vars, boolean set)
	{
		if (vars != null)
		{
			for (Iterator it = vars.iterator(); it.hasNext();)
			{
				ProcessVariable var = (ProcessVariable) it.next();

				if (set)
					var.setTmpReference(var.getName());
				else
					var.setTmpReference(null);
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Operations
	//////////////////////////////////////////////////

	/**
	 * Shows the variables of the currently selected process.
	 */
	public void showParams()
	{
		// Create a container for property browser display
		if (currentProcess != null)
		{
			currentProcess.maintainReferences(ModelObject.RESOLVE_GLOBAL_REFS | ModelObject.RESOLVE_LOCAL_REFS);

			try
			{
				// Establish references to the original object
				List vars = currentProcess.getProcessVariableList();
				adjustTemporaryReferences(vars, true);

				// Establish references to the original object
				List processVariableList = (List) CopyUtil.copyCollection(vars, CopyUtil.CLONE_VALUES);
				adjustTemporaryReferences(processVariableList, true);

				variablesContainer = new VariablesContainer(currentProcess, processVariableList);
			}
			catch (CloneNotSupportedException e)
			{
				// Doesn't happen
				e.printStackTrace();
			}
		}
		else
		{
			variablesContainer = null;
		}

		// Give it to the property browser
		try
		{
			propertyBrowser.setObject(variablesContainer, false);
			propertyBrowser.expandAll(true);
		}
		catch (CloneNotSupportedException e)
		{
			ExceptionUtil.printTrace(e);
		}
		catch (XMLDriverException e)
		{
			ExceptionUtil.printTrace(e);
		}
	}

	/**
	 * Adds a parameter to the variable list of the currently selected process.
	 *
	 * @param name Parameter name or null to create one
	 * @param type Data type of the parameter
	 */
	public void addParam(String name, DataTypeItem type)
	{
		if (currentProcess == null)
			return;

		currentModeler.startUndo("Add Process Variable");

		// Create a new parameter
		ProcessVariable param = new ProcessVariableImpl();

		// Provide the data type and ensure the type name gets updated
		param.setDataType(type);

		// Pick a unique name based on the type name
		List paramList = currentProcess.getProcessVariableList();
		if (name == null)
			name = type.getName();
		name = NamedObjectCollectionUtil.createUniqueId(paramList, name);
		param.setName(name);

		// Add the new parameter to the process
		currentProcess.addProcessVariable(param);
		param.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.SYNC_LOCAL_REFNAMES);

		// Redisplay the param list
		showParams();

		// We modified the current object by adding a parameter
		propertyBrowser.setObjectModified(true);

		fireEvent("modeler.process.modified");
		currentModeler.endUndo();
	}

	//////////////////////////////////////////////////
	// @@ InteractionClient implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragActionTriggered(Object, Point)
	 */
	public void dragActionTriggered(Object regionId, Point p)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragEnded(Transferable)
	 */
	public void dragEnded(Transferable transferable)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragStarted(Transferable)
	 */
	public void dragStarted(Transferable transferable)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getAllDropRegions(List, Transferable, MouseEvent)
	 */
	public List getAllDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		return getDropRegions(flavors, data, mouseEvent);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getDropRegions(List, Transferable, MouseEvent)
	 */
	public List getDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		if (flavors.contains(ClientFlavors.TYPE_ITEM))
		{
			BasicDropRegion region = new BasicDropRegion(MAINREGION, this, getPluginComponent());
			region.setPaint(ModelerColors.DROP_REGION);
			return Collections.singletonList(region);
		}

		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getImportersAt(Point)
	 */
	public List getImportersAt(Point p)
	{
		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getAllImportersAt(Point)
	 */
	public List getAllImportersAt(Point p)
	{
		return getImportersAt(p);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#importData(Object, Transferable, Point)
	 */
	public boolean importData(Object regionId, Transferable data, Point p)
	{
		if (MAINREGION.equals(regionId))
		{
			DataFlavor [] flavors = data.getTransferDataFlavors();

			for (int i = 0; i < flavors.length; i++)
			{
				if (flavors [i].equals(ClientFlavors.TYPE_ITEM))
				{
					try
					{
						DataTypeItem type = (DataTypeItem) data.getTransferData(ClientFlavors.TYPE_ITEM);

						if (!propertyBrowser.saveObject())
							return false;

						addParam(null, type);
						return true;
					}
					catch (UnsupportedFlavorException e)
					{
					}
					catch (IOException e)
					{
					}
				}
			}

			focusPlugin();
		}

		return false;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ DragOrigin implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Is called when the drop has been accepted. Does nothing.
	 * @nowarn
	 */
	public void dropAccepted(Transferable t)
	{
	}

	/**
	 * Is called when the drop has been Canceled. Does nothing.
	 * @nowarn
	 */
	public void dropCanceled(Transferable t)
	{
	}

	/**
	 * Is called when the drop has been performed. Does nothing.
	 * @nowarn
	 */
	public void dropPerformed(Transferable t)
	{
	}

	/**
	 * Returns the
	 */
	public Transferable getTranferableAt(Point p)
	{
		TreePath path = propertyBrowser.getPathByPoint(p);

		// No node at this postion or root node selected.
		if (path == null || path.getPathCount() < 2)
			return null;

		AbstractNode treenode = (AbstractNode) path.getLastPathComponent();

		if (treenode != null)
		{
			ObjectNode odn = treenode.getObjectNode();

			if (odn != null && odn.getObject() instanceof ProcessVariable)
			{
				if (!propertyBrowser.saveObject())
					return null;

				ProcessVariable param = (ProcessVariable) odn.getObject();

				dragImage = ItemIconMgr.getMultiIcon(ItemIconMgr.getInstance().getTypeIcon(param.getDataType(), FlexibleSize.MEDIUM));

				return new BasicTransferable(param);
			}
		}

		return null;
	}

	/**
	 * Returns the icon that is udes to display the transferable.
	 */
	public MultiIcon getDragImage()
	{
		return dragImage;
	}

	//////////////////////////////////////////////////
	// @@ Event handler module
	//////////////////////////////////////////////////

	/**
	 * Event handler module.
	 */
	public class Events extends EventModule
	{
		public String getName()
		{
			return "variables";
		}

		/**
		 * Returns the priority of the module.
		 * We are low-level priority.
		 * @nowarn
		 */
		public int getPriority()
		{
			return 101;
		}

		//////////////////////////////////////////////////
		// @@ Event handlers
		//////////////////////////////////////////////////

		/**
		 * Event handler: Refreshes the list of variables.
		 *
		 * @event variables.refresh
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode refresh(JaspiraEvent je)
		{
			showParams();
			return EVENT_HANDLED;
		}

		/**
		 * Event handler: A modeler view has become active.
		 *
		 * @event modeler.view.activated
		 * @eventobject Editor that owns the view ({@link Modeler})
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode modeler_view_activated(JaspiraEvent je)
		{
			Object o = je.getObject();

			if (o instanceof Modeler)
			{
				if (!propertyBrowser.saveObject())
				{
					return EVENT_IGNORED;
				}

				currentModeler = ((Modeler) o);
				currentProcess = currentModeler.getProcess();
				showParams();

				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
		}

		/**
		 * Event handler: A modeler view has become inactive.
		 *
		 * @event modeler.view.closed
		 * @eventobject Editor that owns the view ({@link Modeler})
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode modeler_view_closed(JaspiraEvent je)
		{
			if (!propertyBrowser.saveObject())
			{
				return EVENT_IGNORED;
			}

			currentModeler = null;
			currentProcess = null;
			showParams();

			return EVENT_HANDLED;
		}

		/**
		 * Event method: Saves the currently edited object.
		 * This event will be sent by any plugin that wants the property browser to save its object.
		 *
		 * @event plugin.propertybrowser.saveobject
		 * @param event The event
		 * @return The event status code
		 */
		public synchronized JaspiraEventHandlerCode saveobject(JaspiraEvent event)
		{
			propertyBrowser.saveObject();

			return EVENT_HANDLED;
		}
	}

	/**
	 * Event module that manages the save operation.
	 */
	public class SaveEvents extends EventModule
	{
		public String getName()
		{
			return "standard.file";
		}

		/**
		 * Gets the module priority.
		 * In cases where a higher-level editor (as the Modeler for example) contains an object
		 * editor plugin to edit a part of a structure, the oe's save command should be executed
		 * before the editor's save command, so assign it a priority higher than the standard priority 50.
		 *
		 * @return The priority. 0 is lowest, 100 is highest.
		 */
		public int getPriority()
		{
			return 25;
		}

		/////////////////////////////////////////////////////////////////////////
		// @@ EventHandling
		/////////////////////////////////////////////////////////////////////////

		/**
		 * Event handler: Save contents of the property browser (triggered by the standard toolbar).
		 *
		 * @event standard.file.save
		 * @param jae Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode save(JaspiraActionEvent jae)
		{
			if (!getPluginComponent().isShowing())
			{
				// Ignore if not visible
				return EVENT_IGNORED;
			}

			propertyBrowser.saveObject();
			return EVENT_HANDLED;
		}
	}
}
