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
package org.openbp.jaspira.gui.wizard;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.openbp.swing.components.wizard.Wizard;
import org.openbp.swing.components.wizard.WizardPage;
import org.openbp.swing.plaf.sky.ReverseShadowBorder;
import org.openbp.swing.plaf.sky.SkyTheme;

/**
 * Base class of all wizard pages of the generator.
 *
 * @author Heiko Erhardt
 */
public abstract class JaspiraWizardPage extends WizardPage
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Frame panel that holds the content panel */
	private JPanel framePanel;

	/** Content panel */
	private JPanel contentPanel;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param wizard Wizard that owns the page
	 */
	public JaspiraWizardPage(Wizard wizard)
	{
		super(wizard);

		// The content panel has a white background
		contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBorder(new ReverseShadowBorder());
		contentPanel.setBackground(SkyTheme.COLOR_BACKGROUND_LIGHT);

		// Create a frame panel that has the same background properties as the wizard page (or else the border would appear white)
		framePanel = new JPanel(new BorderLayout());
		framePanel.setBorder(new EmptyBorder(15, 13, 15, 13));
		framePanel.add(contentPanel, BorderLayout.CENTER);

		add(framePanel, BorderLayout.CENTER);
	}

	/**
	 * Gets the content panel of the page.
	 * The contents of the page (e. g. a scroll pane containing an property browser or a tree) should go into this panel.
	 * @nowarn
	 */
	public JPanel getContentPanel()
	{
		return contentPanel;
	}

	/**
	 * Gets the frame panel that holds the content panel.
	 * Use this panel to add controls that should appear above or below the actual content panel.
	 * @nowarn
	 */
	public JPanel getFramePanel()
	{
		return framePanel;
	}

	//////////////////////////////////////////////////
	// @@ WizardPage overrides
	//////////////////////////////////////////////////

	/**
	 * Returns the focus component of this plugin, i\.e\. the component
	 * that is to initially receive the focus.
	 * @return The return value defaults to the first component below the content pane of the plugin.
	 * If this component is a scroll pane, the method returns the view component of the pane.
	 */
	public Component getFocusComponent()
	{
		if (contentPanel.getComponentCount() == 0)
		{
			return contentPanel;
		}

		Component comp = contentPanel.getComponent(0);
		if (comp instanceof JScrollPane)
		{
			return ((JScrollPane) comp).getViewport().getView();
		}

		return comp;
	}
}
