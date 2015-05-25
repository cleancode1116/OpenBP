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
package org.openbp.swing.components.wizard.test;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.openbp.swing.components.wizard.WizardImpl;
import org.openbp.swing.components.wizard.WizardPage;
import org.openbp.swing.layout.UnitLayout;

/**
 * Information page.
 *
 * @author Heiko Erhardt
 */
public class TestInformationPanel extends WizardPage
	implements DocumentListener
{
	protected JTextField name, email, phone;

	public TestInformationPanel(WizardImpl wizard)
	{
		super(wizard, "Personal Information", "Please enter your name, email address and phone number.  " + "The wizard uses a WizardDataModel and needs something " + "in these fields before allowing forward movement.  " + "The WizardValidator interface is called when changes " + "are made to the model");

		JLabel label;
		JPanel prompts = new JPanel();
		prompts.setLayout(new GridLayout(3, 1, 7, 7));
		prompts.add(label = new JLabel("Name: ", SwingConstants.RIGHT));
		prompts.add(new JLabel("E-Mail Address: ", SwingConstants.RIGHT));
		prompts.add(new JLabel("Phone Number: ", SwingConstants.RIGHT));
		label.setPreferredSize(new Dimension(20, 20));

		JPanel fields = new JPanel();
		fields.setLayout(new GridLayout(3, 1, 7, 7));
		fields.add(name = new JTextField(15));
		fields.add(email = new JTextField(15));
		fields.add(phone = new JTextField(15));
		name.setPreferredSize(new Dimension(20, 20));

		JPanel panel = new JPanel();
		panel.add("West", prompts);
		panel.add("Center", fields);

		JPanel center = new JPanel();
		center.setLayout(new UnitLayout());
		center.add(panel);
		add("Center", center);

		name.getDocument().addDocumentListener(this);
		email.getDocument().addDocumentListener(this);
		phone.getDocument().addDocumentListener(this);
	}

	public void changedUpdate(DocumentEvent event)
	{
		Document document = event.getDocument();
		if (document == name.getDocument())
		{
			getDataModel().put("info.name", name.getText());
		}
		if (document == email.getDocument())
		{
			getDataModel().put("info.email", name.getText());
		}
		if (document == phone.getDocument())
		{
			getDataModel().put("info.phone", name.getText());
		}
		if (getDataModel().containsKey("info.name") && getDataModel().containsKey("info.email") && getDataModel().containsKey("info.phone"))
		{
			canMoveForward = true;
		}
	}

	public void insertUpdate(DocumentEvent event)
	{
		changedUpdate(event);
	}

	public void removeUpdate(DocumentEvent event)
	{
		changedUpdate(event);
	}
}
