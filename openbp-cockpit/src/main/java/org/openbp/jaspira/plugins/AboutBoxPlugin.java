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
package org.openbp.jaspira.plugins;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin;
import org.openbp.swing.layout.VerticalFlowLayout;

/**
 * This Plugin is a simple AboutBoxPlugin.
 *
 * @author Jens Ferchland
 */
public abstract class AboutBoxPlugin extends AbstractVisiblePlugin
{
	//////////////////////////////////////////////////
	// @@ init
	//////////////////////////////////////////////////

	protected void initializeComponents()
	{
		JPanel panel = new JPanel(new VerticalFlowLayout());
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));

		String text = getText();
		if (text != null)
		{
			panel.add(new JLabel(text));
		}

		Icon icon = getAboutIcon();
		if (icon != null)
		{
			JLabel label = new JLabel(icon);
			label.setBorder(new EmptyBorder(5, 0, 0, 0));
			panel.add(label);
		}

		Component comp = getUserComponent();
		if (comp != null)
		{
			panel.add(comp);
		}

		comp = getLicenseComponent();
		if (comp != null)
		{
			panel.add(comp);
		}

		comp = getCopyrightComponent();
		if (comp != null)
		{
			panel.add(comp);
		}

		super.getContentPane().add(panel);
	}

	//////////////////////////////////////////////////
	// @@ user content
	//////////////////////////////////////////////////

	/**
	 * To add your own content override this method.
	 * If you don't override this the aboutbox will be
	 * generated using getText() and getIcon ().
	 *
	 * @return The user component or null of the default content
	 */
	protected Component getUserComponent()
	{
		return null;
	}

	/**
	 * Returns the text that has to be displayed in the about box.
	 * @return The default implementation returns the text taken from the resource file of the plugin
	 * using the resource key "about.text"
	 */
	protected String getText()
	{
		return getPluginResourceCollection().getOptionalString("about.text");
	}

	/**
	 * Returns the icon that is displayed in the about box.
	 * @return The default implementation returns the text taken from the resource file of the plugin
	 * using the resource key "about.icon"
	 */
	protected Icon getAboutIcon()
	{
		return (Icon) getPluginResourceCollection().getRequiredObject("about.icon");
	}

	/**
	 * Returns a component containing the copyright information or null.
	 * @nowarn
	 */
	protected Component getCopyrightComponent()
	{
		return null;
	}

	/**
	 * Returns a component containing the license information or null.
	 * @nowarn
	 */
	protected Component getLicenseComponent()
	{
		return null;
	}

	/**
	 * This method isn't allowed for adding somthing to the aboutbox.
	 * Use getUserComponent and return your content!
	 * It returns null.
	 */
	public JPanel getContentPane()
	{
		return null;
	}
}
