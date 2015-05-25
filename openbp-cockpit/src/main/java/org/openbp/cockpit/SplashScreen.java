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

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.openbp.swing.components.splash.Progressable;
import org.openbp.swing.components.splash.Splash;

/**
 * This is the splash screen of OpenBP.
 *
 * @author Jens Ferchland
 */
public class SplashScreen extends Splash
	implements Progressable
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Image to display */
	private JLabel imageLabel;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param image Image to display
	 */
	public SplashScreen(ImageIcon image)
	{
		super();
		imageLabel = new JLabel(image);
	}

	//////////////////////////////////////////////////
	// @@ Splash implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.swing.components.splash.Splash#getMainComponent()
	 */
	protected Progressable getMainComponent()
	{
		return this;
	}

	/**
	 * @see org.openbp.swing.components.splash.Splash#getSectionComponents()
	 */
	protected Progressable [] getSectionComponents()
	{
		return null;
	}

	//////////////////////////////////////////////////
	// @@ Progressable implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.swing.components.splash.Progressable#getProgressableComponent()
	 */
	public JComponent getProgressableComponent()
	{
		return imageLabel;
	}

	/**
	 * @see org.openbp.swing.components.splash.Progressable#setProgress(double d)
	 */
	public void setProgress(double d)
	{
	}

	/**
	 * @see org.openbp.swing.components.splash.Progressable#getProgress()
	 */
	public double getProgress()
	{
		return 0;
	}
}
