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

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.openbp.core.model.item.process.NodeParam;

/**
 * Param value string page.
 *
 * @author Heiko Erhardt
 */
public class ParamValueBooleanPage extends ParamValuePage
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Check box */
	private JCheckBox checkBox;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 * @param wizard Wizard that owns the page
	 * @param param Parameter to edit
	 */
	public ParamValueBooleanPage(ParamValueWizard wizard, NodeParam param)
	{
		// This will set up the UI
		super(wizard, param);

		JPanel widgetPanel = new JPanel(new BorderLayout());
		widgetPanel.setBackground(getContentPanel().getBackground());

		// TODO Fix 5: We need a three-state check box here!
		checkBox = new JCheckBox(getParam().getDisplayText());
		checkBox.setBackground(getContentPanel().getBackground());
		if ("true".equals(getExpression()))
		{
			checkBox.setSelected(true);
		}

		widgetPanel.add(checkBox, BorderLayout.CENTER);

		JPanel valuePanel = getValuePanel();
		valuePanel.add(widgetPanel, BorderLayout.NORTH);
	}

	/**
	 * Applys the parameter value entered by the user to the parameter.
	 */
	public void apply()
	{
		if (checkBox.isSelected())
		{
			getParam().setExpression("true");
		}
	}
}
