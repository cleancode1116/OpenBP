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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.openbp.core.model.item.process.NodeParam;
import org.openbp.guiclient.plugins.displayobject.DisplayObjectPlugin;
import org.openbp.jaspira.gui.wizard.JaspiraWizardPage;
import org.openbp.swing.plaf.sky.SkyTheme;

/**
 * Wizard page for the parameter value wizard.
 *
 * @author Heiko Erhardt
 */
public class ParamValuePage extends JaspiraWizardPage
	implements ParamValueWizardPart
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Parameter to edit */
	private NodeParam param;

	/** Expression value of the parameter */
	private String expression;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Panel that holds the parameter value widgets */
	private JPanel valuePanel;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 * @param wizard Wizard that owns the page
	 * @param param Parameter to edit
	 */
	public ParamValuePage(ParamValueWizard wizard, NodeParam param)
	{
		super(wizard);

		// Save the parameter
		this.param = param;

		// Determine the current expression value
		expression = param.getExpression();
		if (expression != null && expression.startsWith("\"") && expression.endsWith("\""))
		{
			// Cut "
			expression = expression.substring(1);
			expression = expression.substring(0, expression.length() - 1);
		}

		// Construct the UI
		JPanel cp = getContentPanel();
		cp.setBackground(SkyTheme.COLOR_BACKGROUND_LIGHT);

		// The value panel holds the parameter value widget
		valuePanel = new JPanel(new BorderLayout());
		valuePanel.setBackground(cp.getBackground());
		valuePanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		// The text panel holds the parameter name and description
		JPanel textPanel = new JPanel(new BorderLayout());
		textPanel.setBackground(cp.getBackground());
		JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		namePanel.setBackground(cp.getBackground());

		JLabel label;
		Font font;

		// Param: Name (required)
		label = new JLabel(wizard.getResource().getRequiredString("wizard.page.name") + " ");
		font = label.getFont();
		font = font.deriveFont(14f);
		label.setFont(font);
		namePanel.add(label);

		String name;
		if (DisplayObjectPlugin.getInstance().isTitleModeText())
			name = param.getDisplayText();
		else
			name = param.getName();
		label = new JLabel(name);
		font = label.getFont();
		font = font.deriveFont(Font.BOLD, 14f);
		label.setFont(font);
		namePanel.add(label);

		if (!param.isOptional())
		{
			label = new JLabel(" " + wizard.getResource().getRequiredString("wizard.page.required"));
			font = label.getFont();
			font = font.deriveFont(14f);
			label.setFont(font);
			namePanel.add(label);
		}

		textPanel.add(namePanel, BorderLayout.NORTH);

		// Description1
		// Description2
		// ... (max 200 pixels high)
		String description = param.getDescription();
		if (description != null)
		{
			JTextArea descriptionText = new JTextArea(description);
			descriptionText.setEditable(false);
			descriptionText.setBackground(cp.getBackground());

			JScrollPane sp = new JScrollPane(descriptionText);
			sp.setBackground(cp.getBackground());
			sp.setBorder(new EmptyBorder(0, 5, 5, 5));
			sp.setMaximumSize(new Dimension(10000, 200));

			textPanel.add(sp, BorderLayout.CENTER);
		}

		// Add text panel at the top, value panel to fill below
		cp.add(textPanel, BorderLayout.NORTH);
		cp.add(valuePanel, BorderLayout.CENTER);

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
		param.setExpression(expression);
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the panel that holds the parameter value widgets.
	 * @nowarn
	 */
	public JPanel getValuePanel()
	{
		return valuePanel;
	}

	/**
	 * Gets the parameter to edit.
	 * @nowarn
	 */
	public NodeParam getParam()
	{
		return param;
	}

	/**
	 * Gets the expression value of the parameter.
	 * @nowarn
	 */
	public String getExpression()
	{
		return expression;
	}

	/**
	 * Sets the expression value of the parameter.
	 * @nowarn
	 */
	public void setExpression(String expression)
	{
		this.expression = expression;
	}
}
