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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openbp.jaspira.option.Option;
import org.openbp.jaspira.option.OptionWidget;
import org.openbp.swing.plaf.sky.SimpleBorder;
import org.openbp.swing.plaf.sky.SkyTheme;

/**
 * Option widget that supports the selection of a color.
 *
 * @author Baumgartner Michael
 */
public class ColorWidget extends OptionWidget
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Component that will be returned */
	private JPanel panel;

	/** Text field for the path */
	private JTextField colorField;

	/** File chooser dialog */
	private ColorDialog colorChooser;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param option Option the widget refers to
	 */
	public ColorWidget(Option option)
	{
		super(option);

		// Prepare text field
		colorField = new JTextField();
		colorField.setEditable(false);
		colorField.setBorder(SimpleBorder.getStandardBorder());
		colorField.setBackground(SkyTheme.COLOR_BACKGROUND_LIGHT);

		Dimension size = colorField.getPreferredSize();
		size.width = 100;
		size.height = 18;
		colorField.setPreferredSize(size);
		colorField.setMinimumSize(size);
		colorField.setMaximumSize(size);

		colorField.addMouseListener(new MouseAdapter()
		{
			public void mouseReleased(MouseEvent me)
			{
				colorField.postActionEvent();
			}
		});

		colorField.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (colorChooser == null)
				{
					// Prepare color chooser
					colorChooser = new ColorDialog();
				}

				// Show the file chooser
				colorChooser.setColor(colorField.getBackground());
				colorChooser.setVisible(true);

				// If a file has been choosen, set the new value.
				Color color = colorChooser.getSelectedColor();
				if (color != null)
				{
					colorField.setBackground(color);
					notifyOptionMgrOfOptionChange();
				}
			}
		});

		// Prepare reset button
		JButton resetButton = new JButton();
		resetButton.setBorder(SimpleBorder.getStandardBorder());
		resetButton.setText("Reset");

		size = resetButton.getPreferredSize();
		size.height = 18;
		resetButton.setPreferredSize(size);
		resetButton.setMinimumSize(size);
		resetButton.setMaximumSize(size);

		resetButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Color color = (Color) getOption().getDefaultValue();
				if (color != null)
				{
					colorField.setBackground(color);
					notifyOptionMgrOfOptionChange();
				}
			}
		});

		// Prepare component
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JComponent heading = createHeading();
		if (heading != null)
		{
			panel.add(heading);
		}
		panel.add(colorField);
		panel.add(resetButton);
	}

	//////////////////////////////////////////////////
	// @@ OptionWidget implementation
	//////////////////////////////////////////////////

	/**
	 * Get the selected color.
	 * @return A {@link java.awt.Color} object
	 */
	public Object getValue()
	{
		return colorField.getBackground();
	}

	/**
	 * Get the panel for the color widget. It contains
	 * a text with the color and a button to change it.
	 * @return the panel
	 */
	public JComponent getWidgetComponent()
	{
		return panel;
	}

	/**
	 * Set a new color.
	 * @param o The {@link java.awt.Color} object to set
	 */
	public void setValue(Object o)
	{
		if (o != null && o instanceof Color)
			colorField.setBackground((Color) o);
	}
}
