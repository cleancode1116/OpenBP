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

import org.openbp.common.ExceptionUtil;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.core.model.WorkflowTaskDescriptor;
import org.openbp.core.model.item.ConfigurationBean;
import org.openbp.core.model.item.process.MultiSocketNode;
import org.openbp.core.model.item.process.WorkflowNode;
import org.openbp.jaspira.gui.wizard.JaspiraWizardObjectPage;
import org.openbp.jaspira.propertybrowser.PropertyBrowser;
import org.openbp.swing.components.wizard.WizardEvent;

/**
 * Wizard page for the configuration bean.
 *
 * @author Heiko Erhardt
 */
public class ConfigurationBeanPage extends JaspiraWizardObjectPage
	implements ParamValueWizardPart
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** The node we refer to */
	private MultiSocketNode node;

	/** Configuration bean of the node */
	private ConfigurationBean configurationBean;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 * @param wizard Wizard that owns the page
	 * @param node The node we refer to
	 * @param configurationBean Configuration bean of the node
	 */
	public ConfigurationBeanPage(ParamValueWizard wizard, MultiSocketNode node, ConfigurationBean configurationBean)
	{
		super(wizard);

		this.node = node;
		this.configurationBean = configurationBean;

		// We can always move to the next page or press the finish button in this wizard
		canFinish = canMoveForward = true;
	}

	//////////////////////////////////////////////////
	// @@ ParamValueWizardPart implementation
	//////////////////////////////////////////////////

	/**
	 * Applys the data of this page to the edited object.
	 */
	public void apply()
	{
		if (!configurationBean.hasDefaultValues())
		{
			node.setConfigurationBean(configurationBean);
		}

		if (node instanceof WorkflowNode)
		{
			// Fill the workflow task settings of a new workflow node
			((WorkflowNode) node).setWorkflowTaskDescriptor((WorkflowTaskDescriptor) configurationBean);
			node.setConfigurationBean(null);
		}
	}

	//////////////////////////////////////////////////
	// @@ JaspiraWizardObjectPage overrides
	//////////////////////////////////////////////////

	/**
	 * Handles a wizard event caused by this wizard page.
	 *
	 * @param event Event to handle
	 */
	public void handleWizardEvent(WizardEvent event)
	{
		if (event.eventType == WizardEvent.SHOW)
		{
			try
			{
				getPropertyBrowser().setObject(configurationBean, true);
			}
			catch (XMLDriverException e)
			{
				ExceptionUtil.printTrace(e);
			}
			catch (CloneNotSupportedException e)
			{
				ExceptionUtil.printTrace(e);
			}
		}
		else if (event.eventType == WizardEvent.CANCEL)
		{
			// Reset the property browser in order to prevent validation error messages
			getPropertyBrowser().reset();
		}

		// This will save the edited object
		super.handleWizardEvent(event);
	}

	//////////////////////////////////////////////////
	// @@ Save strategy and object change listener
	//////////////////////////////////////////////////

	/**
	 * Called when the object needs to be saved.
	 *
	 * @param pb Property browser
	 * @return
	 * true: Save successful.<br>
	 * false: Save failed.
	 */
	public boolean executeSave(PropertyBrowser pb)
	{
		configurationBean = (ConfigurationBean) pb.getModifiedObject();

		return true;
	}
}
