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

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;

import org.openbp.common.ExceptionUtil;
import org.openbp.swing.components.wizard.WizardImpl;

/**
 * Wizard test.
 *
 * @author Heiko Erhardt
 */
public class TestWizard extends JFrame
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor.
	 */
	private TestWizard()
	{
		super("Wizard Test");
		setBounds(100, 100, 479, 357);

		ImageIcon image = null;
		URL url = getClass().getClassLoader().getResource("org/openbp/swing/components/wizard/test/Wizard.png");
		if (url != null)
		{
			image = new ImageIcon(url);
		}

		ImageIcon backgroundImage = null;
		url = getClass().getClassLoader().getResource("org/openbp/swing/components/wizard/test/Background.png");
		if (url != null)
		{
			backgroundImage = new ImageIcon(url);
		}

		WizardImpl wizard = new WizardImpl(null, image, backgroundImage);
		wizard.addAndLinkPage("info", new TestInformationPanel(wizard));
		wizard.addAndLinkPage("favorites", new TestFavoritesPanel(wizard));
		wizard.addAndLinkPage("java", new TestResultPanel(wizard, "Java", "100% Pure Genius!"));
		wizard.addAndLinkPage("cpp", new TestResultPanel(wizard, "C/C++", "You'll see..."));
		wizard.addAndLinkPage("pascal", new TestResultPanel(wizard, "Pascal", "Oh well..."));
		wizard.addAndLinkPage("smalltalk", new TestResultPanel(wizard, "SmallTalk", "Object Oriented"));
		wizard.addAndLinkPage("cobol", new TestResultPanel(wizard, "Cobol", "Year 2k problems?"));
		wizard.addAndLinkPage("fortran", new TestResultPanel(wizard, "Fortran", "Try Mathematica!"));
		wizard.displayFirst();

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add("Center", wizard);
	}

	public static void main(String [] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			// UIManager.setLookAndFeel (UIManager.getCrossPlatformLookAndFeelClassName ());
			// UIManager.setLookAndFeel ("org.openbp.swing.plaf.sky.SkyLookAndFeel");
		}
		catch (Exception e)
		{
			ExceptionUtil.printTrace(e);
		}

		TestWizard wiz = new TestWizard();
		wiz.setVisible(true);
	}
}
