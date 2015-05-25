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

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.openbp.swing.components.wizard.WizardEvent;
import org.openbp.swing.components.wizard.WizardImpl;
import org.openbp.swing.components.wizard.WizardPage;

/**
 * Result page.
 *
 * @author Heiko Erhardt
 */
public class TestResultPanel extends WizardPage
{
	public TestResultPanel(WizardImpl wizard, String title, String message)
	{
		super(wizard, title, null);
		canMoveForward = true;

		JLabel msg = new JLabel(message, SwingConstants.CENTER);
		msg.setFont(new Font("Helvetica", Font.PLAIN, 18));
		add("Center", msg);
	}

	/**
	 * Handles a wizard event.
	 *
	 * @param event Event to handle
	 */
	public void handleWizardEvent(WizardEvent event)
	{
		if (event.eventType == WizardEvent.FINISH)
		{
			System.out.println("Result: Finish");
		}

		super.handleWizardEvent(event);

		if (event.eventType == WizardEvent.FINISH || event.eventType == WizardEvent.CANCEL)
		{
			System.exit(0);
		}
	}
}
