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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.openbp.swing.components.wizard.WizardEvent;
import org.openbp.swing.components.wizard.WizardImpl;
import org.openbp.swing.components.wizard.WizardPage;
import org.openbp.swing.layout.UnitLayout;

/**
 * Favorites page.
 *
 * @author Heiko Erhardt
 */
public class TestFavoritesPanel extends WizardPage
	implements ActionListener
{
	protected JRadioButton java, cpp, pascal, smalltalk, cobol, fortran;

	public TestFavoritesPanel(WizardImpl wizard)
	{
		super(wizard, "Favorite Language", "Which of these is your favorite computer language?  " + "Try a few alternatives to see dynamic changes to the " + "SequenceManager in action.");

		JPanel pick = new JPanel();
		pick.setLayout(new GridLayout(3, 2, 7, 7));
		pick.add(java = new JRadioButton("Java"));
		pick.add(cpp = new JRadioButton("C/C++"));
		pick.add(pascal = new JRadioButton("Pascal"));
		pick.add(smalltalk = new JRadioButton("SmallTalk"));
		pick.add(cobol = new JRadioButton("Cobol"));
		pick.add(fortran = new JRadioButton("Fortran"));

		ButtonGroup group = new ButtonGroup();
		group.add(java);
		group.add(cpp);
		group.add(pascal);
		group.add(smalltalk);
		group.add(cobol);
		group.add(fortran);

		JPanel center = new JPanel();
		center.setLayout(new UnitLayout());
		center.add(pick);
		add("Center", center);

		java.addActionListener(this);
		cpp.addActionListener(this);
		pascal.addActionListener(this);
		smalltalk.addActionListener(this);
		cobol.addActionListener(this);
		fortran.addActionListener(this);
	}

	public void actionPerformed(ActionEvent event)
	{
		canFinish = true;
		canMoveForward = true;
		Object source = event.getSource();
		if (source == java)
		{
			getDataModel().put("favorite.language", "Java");
			getSequenceManager().chain("favorites", "java");
			getSequenceManager().chain("java", null);
		}
		if (source == cpp)
		{
			getDataModel().put("favorite.language", "C/C++");
			getSequenceManager().chain("favorites", "cpp");
			getSequenceManager().chain("cpp", null);
		}
		if (source == pascal)
		{
			getDataModel().put("favorite.language", "Pascal");
			getSequenceManager().chain("favorites", "pascal");
			getSequenceManager().chain("pascal", null);
		}
		if (source == smalltalk)
		{
			getDataModel().put("favorite.language", "SmallTalk");
			getSequenceManager().chain("favorites", "smalltalk");
			getSequenceManager().chain("smalltalk", null);
		}
		if (source == cobol)
		{
			getDataModel().put("favorite.language", "Cobol");
			getSequenceManager().chain("favorites", "cobol");
			getSequenceManager().chain("cobol", null);
		}
		if (source == fortran)
		{
			getDataModel().put("favorite.language", "Fortran");
			getSequenceManager().chain("favorites", "fortran");
			getSequenceManager().chain("fortran", null);
		}
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
			System.out.println("Favorites: Finish");
		}

		super.handleWizardEvent(event);

		if (event.eventType == WizardEvent.FINISH || event.eventType == WizardEvent.CANCEL)
		{
			System.exit(0);
		}
	}
}
