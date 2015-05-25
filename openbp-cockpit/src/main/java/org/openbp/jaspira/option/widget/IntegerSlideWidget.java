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

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openbp.jaspira.option.Option;
import org.openbp.jaspira.option.OptionWidget;
import org.openbp.swing.plaf.sky.ReverseShadowBorder;
import org.openbp.swing.plaf.sky.SimpleBorder;

/**
 * A Menu Widget for selecting a number from a ranges.
 *
 * @author Jens Ferchland
 */
public class IntegerSlideWidget extends OptionWidget
	implements ChangeListener
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Component that will be returned */
	private Box panel;

	/** Slider */
	private JSlider slider;

	/** Slider value label */
	private JLabel label;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param option Option the widget refers to
	 * @param min Minimum value
	 * @param max Maximum value
	 */
	public IntegerSlideWidget(Option option, int min, int max)
	{
		this(option, min, max, SwingConstants.HORIZONTAL);
	}

	/**
	 * Constructor.
	 *
	 * @param option Option the widget refers to
	 * @param min Minimum value
	 * @param max Maximum value
	 * @param orientation Orientation of the slider
	 */
	public IntegerSlideWidget(Option option, int min, int max, int orientation)
	{
		this(option, min, max, 10, orientation);
	}

	/**
	 * Constructor.
	 *
	 * @param option Option the widget refers to
	 * @param min Minimum value
	 * @param max Maximum value
	 * @param steps Slider step setting
	 * @param orientation Orientation of the slider
	 */
	public IntegerSlideWidget(Option option, int min, int max, int steps, int orientation)
	{
		super(option);

		slider = new JSlider(orientation, min, max, min);
		slider.setSnapToTicks(true);

		slider.addChangeListener(this);

		JPanel labelpan = new JPanel(new BorderLayout());

		label = new JLabel(Integer.toString(slider.getValue()), SwingUtilities.CENTER);
		label.setOpaque(false);
		labelpan.setBorder(new ReverseShadowBorder());
		labelpan.add(label);

		panel = Box.createVerticalBox();
		panel.setBorder(SimpleBorder.getStandardBorder());

		panel.add(slider);
		panel.add(labelpan);
	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		// Update the text under the slider.
		label.setText(Integer.toString(slider.getValue()));

		notifyOptionMgrOfOptionChange();
	}

	//////////////////////////////////////////////////
	// @@ OptionWidget implementation
	//////////////////////////////////////////////////

	public Object getValue()
	{
		return Integer.valueOf(slider.getValue());
	}

	public void setValue(Object o)
	{
		slider.setValue(((Integer) o).intValue());
	}

	public JComponent getWidgetComponent()
	{
		return panel;
	}
}
