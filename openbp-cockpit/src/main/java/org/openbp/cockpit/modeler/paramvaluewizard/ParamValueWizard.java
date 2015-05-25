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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.openbp.cockpit.CockpitConstants;
import org.openbp.cockpit.modeler.Modeler;
import org.openbp.cockpit.modeler.figures.process.NodeFigure;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.rc.ResourceCollectionMgr;
import org.openbp.core.model.item.ConfigurationBean;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.process.ActivityNode;
import org.openbp.core.model.item.process.MultiSocketNode;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.SubprocessNode;
import org.openbp.core.model.item.process.WorkflowNode;
import org.openbp.jaspira.plugin.ApplicationUtil;
import org.openbp.swing.components.wizard.WizardEvent;
import org.openbp.swing.components.wizard.WizardImpl;
import org.openbp.swing.components.wizard.WizardPage;

/**
 * Parameter value wizard.
 * This wizard displays all entry parameters of a newly created node
 * that the user can preset with constant values.<br>
 * These are parameters that have their {@link NodeParam#setParamValueWizard} property set.
 * This property determines the type of wizard page that is used to enter the value.
 * For each parameter, there will be one wizard page.
 *
 * @author Heiko Erhardt
 */
public class ParamValueWizard extends WizardImpl
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Default size of the dialog */
	public static final Dimension DEFAULT_SIZE = new Dimension(600, 600);

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Dialog that displays the wizard */
	private final JDialog dialog;

	/** List of {@link NodeParam} objects to display in the dialog */
	private final List valueParams;

	/** The node we refer to */
	private final Node node;

	/** Configuration bean of the node */
	private final ConfigurationBean configurationBean;

	/** Finished */
	private boolean finished;

	//////////////////////////////////////////////////
	// @@ Static methods
	//////////////////////////////////////////////////

	/**
	 * Checks if the parameter value wizard can be applied to the given node.
	 *
	 * @param modeler Modeler in charge
	 * @param nodeFigure The new node
	 * @param socketName Name of the socket to edit or null for the default entry socket
	 * @return
	 * true: There are parameters or setting values that can be edited by the wizard.<br>
	 * false: No wizard hints have been defined for the parameters and there is no setting object to edit.
	 */
	public static boolean isParameterValueWizardApplyable(Modeler modeler, NodeFigure nodeFigure, String socketName)
	{
		Node node = nodeFigure.getNode();

		NodeSocket socket = null;
		if (socketName != null)
		{
			socket = node.getSocketByName(socketName);
		}
		else
		{
			socket = node.getDefaultEntrySocket();
		}

		if (socket == null)
			return false;

		// Check if there are parameters we might assign a value
		for (Iterator it = socket.getParams(); it.hasNext();)
		{
			NodeParam param = (NodeParam) it.next();

			if (param.getParamValueWizard() != null)
				return true;
		}

		// Check if there is a configuration bean associated with this type of node
		if (node instanceof MultiSocketNode)
		{
			// Determine the item the figure element references to
			Item referencedItem = null;
			if (node instanceof SubprocessNode)
			{
				referencedItem = ((SubprocessNode) node).getSubprocess();
			}
			else if (node instanceof ActivityNode)
			{
				// TODO Feature 4 Configuration bean not supported currently due to removal of ActivityNode->ActivityItem reference
				// referencedItem = ((ActivityNode) node).getActivity();
			}

			if (referencedItem != null && referencedItem.getConfigurationClassName() != null)
				return true;
		}

		return false;
	}

	/**
	 * Display the parameter value wizard.
	 * Called after a node has been inserted in the workspace or if the user clicked the 'Display paramete value' menu item.
	 * Checks if the parameters of the default entry socket contain parameter wizard hints or if there
	 * is a setting object associated with the node that can be edited.
	 * If so, it displays the parameter wizard.
	 *
	 * @param modeler Modeler in charge
	 * @param nodeFigure The new node
	 * @param socketName Name of the socket to edit or null for the default entry socket
	 * @param paramName Name of the parameter to display by default or null for the first parameter
	 */
	public static void displayParameterValueWizard(Modeler modeler, NodeFigure nodeFigure, String socketName, String paramName)
	{
		Node node = nodeFigure.getNode();

		NodeSocket socket = null;
		if (socketName != null)
		{
			socket = node.getSocketByName(socketName);
		}
		else
		{
			socket = node.getDefaultEntrySocket();
		}

		// Check if there are parameters we might assign a value
		List valueParams = null;

		if (socket != null)
		{
			for (Iterator it = socket.getParams(); it.hasNext();)
			{
				NodeParam param = (NodeParam) it.next();

				if (param.getParamValueWizard() != null)
				{
					if (valueParams == null)
						valueParams = new ArrayList();
					valueParams.add(param);
				}
			}
		}

		ConfigurationBean configurationBean = null;
		if (node instanceof MultiSocketNode)
		{
			MultiSocketNode msNode = (MultiSocketNode) node;

			// Determine the item the figure element references to
			Item referencedItem = null;
			if (node instanceof SubprocessNode)
			{
				referencedItem = ((SubprocessNode) node).getSubprocess();
			}
			else if (node instanceof ActivityNode)
			{
				// TODO Feature 4 Configuration bean not supported currently due to removal of ActivityNode->ActivityItem reference
				// referencedItem = ((ActivityNode) node).getActivity();
			}

			// Check if we shall display the settings in the wizard or not
			// (applies only for new nodes, i. e. if refering to the default entry socket)
			if (socketName == null && referencedItem != null && ! referencedItem.isHideSettingsInWizard())
			{
				// Create a configuration bean if not already there
				// in order to be able to edit the settings in the property browser.
				// The bean will be removed if it contains the default values only when saving the activity node again
				// (see plugin_propertybrowser_executesave () in the ConfigurationBeanPage)
				configurationBean = msNode.getConfigurationBean();
				if (configurationBean == null)
				{
					// We create a new configuration bean (if defined by the underlying activity)
					if (referencedItem != null)
					{
						configurationBean = referencedItem.createConfigurationBean();
					}
				}
			}
		}

		if (node instanceof WorkflowNode)
		{
			// Fill the workflow task settings of a new workflow node
			configurationBean = ((WorkflowNode) node).getWorkflowTaskDescriptor();
		}

		if (valueParams != null || configurationBean != null)
		{
			// Yes, display the wizard encapsulated in an undo transaction
			modeler.startUndo("Assign Parameter Values");

			// Create the wizard and show it
			ParamValueWizard wizard = new ParamValueWizard(node, valueParams, configurationBean, paramName);
			if (wizard.showWizardDialog())
			{
				// The dialog was ended using the 'Finish' button.
				// The dialog was cancelled.
				modeler.endUndo();

				nodeFigure.changed();
			}
			else
			{
				// The dialog was cancelled.
				modeler.cancelUndo();
			}

			// Set the focus back to the drawing view
			((Component) modeler.view()).requestFocus();
		}
	}

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param node The node we refer to
	 * @param valueParams List of {@link NodeParam} objects to display in the dialog
	 * @param configurationBean Configuration bean of the node
	 * @param paramName Name of the parameter to display by default or null for the first parameter
	 */
	public ParamValueWizard(Node node, List valueParams, ConfigurationBean configurationBean, String paramName)
	{
		super(null);

		// Use the wizard resource from the 'Cockpit' resource component
		setWizardResource(ResourceCollectionMgr.getDefaultInstance().getResource(CockpitConstants.RESOURCE_COCKPIT, getClass()));

		// Remember the arguments
		this.node = node;
		this.valueParams = valueParams;
		this.configurationBean = configurationBean;

		// Initialize the wizard pages
		setupPages();
		if (paramName != null)
		{
			if (getPage(paramName) != null)
			{
				displayPage(paramName);
			}
			else
			{
				displayFirst();
			}
		}
		else
		{
			displayFirst();
		}

		// Create a modal dialog that displays the wizard
		JFrame activeFrame = ApplicationUtil.getActiveWindow();
		dialog = new JDialog(activeFrame, true);

		dialog.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				fireWizardEvent(WizardEvent.CANCEL);
			}
		});

		dialog.setSize(DEFAULT_SIZE);
		dialog.setLocationRelativeTo(activeFrame);

		dialog.getContentPane().add(this);
	}

	/**
	 * Shows the wizard dialog.
	 * @return
	 * true: The dialog was ended using the 'Finish' button.<br>
	 * false: The dialog was cancelled.
	 */
	public boolean showWizardDialog()
	{
		// Show the dialog
		dialog.setVisible(true);

		return finished;
	}

	//////////////////////////////////////////////////
	// @@ GeneratorWizard overrides
	//////////////////////////////////////////////////

	/**
	 * Sets up the wizard pages.
	 */
	protected void setupPages()
	{
		clearPages();

		if (valueParams != null)
		{
			// Traverse backwards to display the param pages in the right order
			int n = valueParams.size();
			for (int i = n - 1; i >= 0; --i)
			{
				NodeParam param = (NodeParam) valueParams.get(i);

				WizardPage page = null;
				String type = param.getParamValueWizard();

				// Create the page according to the wizard type specified by the parameter
				if (type.equals("string"))
				{
					page = new ParamValueStringPage(this, param);
				}
				else if (type.equals("integer"))
				{
					page = new ParamValueIntegerPage(this, param);
				}
				else if (type.equals("boolean"))
				{
					page = new ParamValueBooleanPage(this, param);
				}
				else if (type.equals("datatype"))
				{
					page = new ParamValueDataTypePage(this, param);
				}
				else
				{
					LogUtil.error(getClass(), "Unknown parameter value wizard type $0 for parameter $1", type, param.getQualifier());
				}

				if (page != null)
				{
					// Add the new page to the wizard
					addAndLinkPage(param.getName(), page);
				}
			}
		}

		if (configurationBean != null)
		{
			ConfigurationBeanPage page = new ConfigurationBeanPage(this, (MultiSocketNode) node, configurationBean);

			// Add the new page to the wizard
			addAndLinkPage("node-settings", page);
		}
	}

	/**
	 * Fires a wizard event.
	 *
	 * @param event Event to fire
	 * @return
	 * true: Processing can be continued.<br>
	 * false: The cancel flag of the event has been set by an event listener.
	 */
	protected boolean fireWizardEvent(WizardEvent event)
	{
		// The super method will link and show the result page if set
		if (! super.fireWizardEvent(event))
			return false;

		if (event.eventType == WizardEvent.FINISH)
		{
			// Iterate the pages and apply the expression value entered/selected by the user to the node parameter.
			for (Iterator it = getPages(); it.hasNext();)
			{
				ParamValueWizardPart page = (ParamValueWizardPart) it.next();

				page.apply();
			}

			finished = true;
		}

		return true;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Creates a wizard page for parameters of type 'string'.
	 *
	 * @param param Parameter to edit
	 * @return The new page or null on error
	 */
	protected WizardPage createStringPage(NodeParam param)
	{
		return null;
	}

	/**
	 * Creates a wizard page for parameters of type 'integer'.
	 *
	 * @param param Parameter to edit
	 * @return The new page or null on error
	 */
	protected WizardPage createIntegerPage(NodeParam param)
	{
		return null;
	}

	/**
	 * Creates a wizard page for parameters of type 'boolean'.
	 *
	 * @param param Parameter to edit
	 * @return The new page or null on error
	 */
	protected WizardPage createBooleanPage(NodeParam param)
	{
		return null;
	}

	/**
	 * Creates a wizard page for parameters of type 'data type'.
	 *
	 * @param param Parameter to edit
	 * @return The new page or null on error
	 */
	protected WizardPage createDataTypePage(NodeParam param)
	{
		return null;
	}

	/*
	 // Get the description of the page from the page descriptor
	 String pageDescription = pd.getDescription ();
	 page.setDescription (pageDescription);

	 // Get the title of the page from the page descriptor
	 String pageTitle = pd.getDisplayName ();
	 if (pageTitle == null)
	 pageTitle = generator.getDisplayName ();
	 page.setTitle (pageTitle);

	 if (pd.isFinish ())
	 {
	 page.canFinish = true;
	 }
	 */
}
