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
package org.openbp.swing.components.splash;

import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JWindow;

/**
 * This is a abstract class for a splash screen.
 *
 * @author Jens Ferchland
 */
public abstract class Splash extends JWindow
{
	//////////////////////////////////////////////////
	// @@ construction
	//////////////////////////////////////////////////

	/**
	 * Inits the window.
	 * Splash is abstract and can't be instantiated.
	 */
	public Splash()
	{
	}

	/**
	 * Shows the splash screen.
	 */
	public void setVisible(boolean visible)
	{
		if (visible)
		{
			layoutWindow();
		}
		super.setVisible(visible);
	}

	//////////////////////////////////////////////////
	// @@ abstract
	//////////////////////////////////////////////////

	/**
	 * Returns the main component of the splash screen.
	 * This is normally a Image but it can also be a movie,...
	 * @nowarn
	 */
	protected abstract Progressable getMainComponent();

	/**
	 * Returns a array of Progressable objects that
	 * represents the sections or parts of starting
	 * your own application.
	 *
	 * To set the value of the progress you can use
	 * {@link #setProgressTo(int, double)};
	 * @return The section components
	 */
	protected abstract Progressable [] getSectionComponents();

	//////////////////////////////////////////////////
	// @@ progress component action
	//////////////////////////////////////////////////

	/**
	 * This method sets the value of progress of a section.
	 * The progress of the main component is the
	 * avarage of all sections. Sections will be counted from 1.
	 * @param section Section index
	 * @param value Progress value (0 <= value <= 1f)
	 */
	public void setProgressTo(int section, double value)
	{
		Progressable [] comps = getSectionComponents();
		if (comps != null && comps.length > 0)
		{
			if (comps.length < section || value < 0d || value > 1)
			{
				return;
			}

			comps [section - 1].setProgress(value);

			updateMainProgress();
		}
	}

	/**
	 * Updates the progress of the main componentn.
	 */
	private void updateMainProgress()
	{
		Progressable [] comps = getSectionComponents();
		if (comps != null && comps.length > 0)
		{
			// the wrong progress values that will be ignored.
			int fail = 0;

			// the count of data
			int count = comps.length;

			// the value of the main component.
			double value = 0d;

			for (int i = 0; i < count; i++)
			{
				double d = comps [i].getProgress();

				if (d < 0 || d > 1)
				{
					fail++;
					continue;
				}

				value += d;
			}

			value = value / (count - fail);

			getMainComponent().setProgress(value);
		}
	}

	//////////////////////////////////////////////////
	// @@ layout
	//////////////////////////////////////////////////

	private void layoutWindow()
	{
		// Rectangle of our main component;
		Rectangle maincomponent = new Rectangle();

		// first get all sizes

		JComponent maincomp = getMainComponent().getProgressableComponent();
		maincomponent.width += maincomp.getWidth();
		maincomponent.height += maincomp.getHeight();

		getContentPane().add(maincomp);

		// pack the window
		pack();

		// display the window in the center
		setLocationRelativeTo(null);
	}
}
