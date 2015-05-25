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
package org.openbp.jaspira.option.widget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.openbp.jaspira.plugin.ApplicationUtil;
import org.openbp.jaspira.plugins.colorchooser.DefaultPreviewPanel;
import org.openbp.swing.components.JStandardDialog;
import org.openbp.swing.layout.UnitLayout;
import org.openbp.swing.plaf.sky.ShadowBorder;

/**
 * Color selection dialog for the color option widget.
 *
 * @author Baumgartner Michael
 */
public class ColorDialog extends JStandardDialog
{
	//////////////////////////////////////////////////
	// @@ Member
	//////////////////////////////////////////////////

	/** The selected color. */
	private Color selectedColor;

	/** Color chooser. */
	private JColorChooser colorChooser;

	/** Preview panel*/
	private DefaultPreviewPanel preview;

	/** Dimension of the color palette in pixels */
	private static final int PALETTE_DIMENSION = 200;

	/** Size of the preview panel */
	private static final Dimension PREVIEW_SIZE = new Dimension(50, PALETTE_DIMENSION + 3);

	//////////////////////////////////////////////////
	// @@ Constructor
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public ColorDialog()
	{
		super(ApplicationUtil.getActiveWindow(), true);
		setTitle("Select Color");

		// Set up the color chooser component
		colorChooser = new JColorChooser();

		// The chooser doesn't center itself correctly, so move it down a little by force
		colorChooser.setBorder(new EmptyBorder(7, 0, 0, 0));

		// Set up the preview component
		preview = new DefaultPreviewPanel();
		preview.setMinimumSize(PREVIEW_SIZE);
		preview.setMaximumSize(PREVIEW_SIZE);
		preview.setPreferredSize(PREVIEW_SIZE);
		preview.setSize(PREVIEW_SIZE);
		preview.setBorder(new ShadowBorder());
		preview.setAlignmentX(BOTTOM_ALIGNMENT);

		// Connect the preview panel to the chooser; it's important to do this
		// before adding the preview to the plugin
		colorChooser.setPreviewPanel(preview);

		// The preview component will be displayed centered in order to match the color chooser's palette position
		JPanel previewPanel = new JPanel(new UnitLayout(UnitLayout.CENTER, UnitLayout.TOP));
		previewPanel.setBorder(new EmptyBorder(40, 5, 5, 10));
		previewPanel.add(preview);

		// Add preview to the left, chooser to fill
		JPanel chooserPanel = new JPanel(new BorderLayout());
		chooserPanel.add(BorderLayout.CENTER, colorChooser);
		chooserPanel.add(BorderLayout.WEST, previewPanel);

		//setComponentBackground (chooserPanel, SkyTheme.COLOR_BACKGROUND_LIGHT);
		getMainPane().add(chooserPanel);

		pack();
	}

	/**
	 * @see org.openbp.swing.components.JStandardDialog#handleOk()
	 */
	protected void handleOk()
	{
		super.handleOk();
		selectedColor = colorChooser.getColor();
	}

	/**
	 * Set the selected color.
	 *
	 * @param color The color
	 */
	public void setColor(Color color)
	{
		colorChooser.setColor(color);
	}

	/**
	 * Get the selected color.
	 * @return the selected color
	 */
	public Color getSelectedColor()
	{
		return selectedColor;
	}
}
