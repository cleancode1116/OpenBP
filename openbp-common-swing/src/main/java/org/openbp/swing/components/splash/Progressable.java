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

import javax.swing.JComponent;

/**
 * This Interface declares objects that can be displayed in a
 * splash screen.
 *
 * @author Jens Ferchland
 */
public interface Progressable
{
	/**
	 * Returns the component that can show the progress.
	 * @return This may be e. g. a JProgressBar
	 */
	public JComponent getProgressableComponent();

	/**
	 * Sets the progress of the object.
	 * @param d Progress (0 <= d <= 1f)<br>
	 * 0 means nothing done and 1 means action finished.
	 */
	public void setProgress(double d);

	/**
	 * Returns the progress of this Progressable object.
	 * @return Progress (0 <= d <= 1f)<br>
	 * 0 means nothing done and 1 means action finished.
	 */
	public double getProgress();
}
