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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.openbp.common.string.StringUtil;
import org.openbp.core.model.item.process.NodeParam;

/**
 * Param value string page.
 *
 * @author Heiko Erhardt
 */
public class ParamValueStringPage extends ParamValuePage
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Text field */
	private JTextField textField;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 * @param wizard Wizard that owns the page
	 * @param param Parameter to edit
	 */
	public ParamValueStringPage(ParamValueWizard wizard, NodeParam param)
	{
		// This will set up the UI
		super(wizard, param);

		JPanel widgetPanel = new JPanel(new BorderLayout());
		widgetPanel.setBackground(getContentPanel().getBackground());

		JLabel heading = new JLabel(wizard.getResource().getRequiredString("wizard.page.value.string"));
		heading.setBackground(getContentPanel().getBackground());
		heading.setBorder(new EmptyBorder(0, 0, 0, 5));

		textField = new JTextField();
		textField.setText(getExpression());

		widgetPanel.add(heading, BorderLayout.WEST);
		widgetPanel.add(textField, BorderLayout.CENTER);

		JPanel valuePanel = getValuePanel();
		valuePanel.add(widgetPanel, BorderLayout.NORTH);
	}

	/**
	 * Applys the parameter value entered by the user to the parameter.
	 */
	public void apply()
	{
		String text = StringUtil.trimNull(textField.getText());
		if (text != null)
		{
			getParam().setExpression("\"" + text + "\"");
		}
		else
		{
			getParam().setExpression(null);
		}
	}
}
