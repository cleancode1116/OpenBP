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
package org.openbp.cockpit;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.openbp.common.application.ProductProfile;
import org.openbp.guiclient.GUIClientModule;
import org.openbp.jaspira.plugins.AboutBoxPlugin;

/**
 * This is the AboutBoxPlugin of the OpenBP Cockpit.
 * This is a plugin which will be loaded by the AboutBoxSpawner.
 *
 * @author Jens Ferchland
 */
public class CockpitAboutBox extends AboutBoxPlugin
{
	public String getResourceCollectionContainerName()
	{
		return "plugin.cockpit";
	}

	/**
	 * @see org.openbp.jaspira.plugins.AboutBoxPlugin#getText()
	 */
	protected String getText()
	{
		ProductProfile p = GUIClientModule.getInstance().getProductProfile();
		return p.getFullProductName() + " V " + p.getVersion() + " build " + p.getBuildNumber();
	}

	/**
	 * @see org.openbp.jaspira.plugins.AboutBoxPlugin#getCopyrightComponent()
	 */
	protected Component getCopyrightComponent()
	{
		JLabel label = new JLabel("(c) 2007 skynamics AG   All rights reserved");
		label.setBorder(new EmptyBorder(5, 0, 0, 0));
		return label;
	}

	/**
	 * Returns a component containing the license information or null.
	 * @nowarn
	 */
	protected Component getLicenseComponent()
	{
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

		// TODO Feature 5 Add product info
		String licenseInfo = "OpenBP";
		JTextArea text = new JTextArea(licenseInfo);
		text.setBackground(mainPanel.getBackground());
		text.setEditable(false);

		mainPanel.add(text, BorderLayout.CENTER);

		return mainPanel;
	}
}
