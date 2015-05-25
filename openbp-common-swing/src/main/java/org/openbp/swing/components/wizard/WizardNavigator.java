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
package org.openbp.swing.components.wizard;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.rc.ResourceCollectionUtil;

/**
 * Wizard navigator.
 *
 * @author Heiko Erhardt
 */
public class WizardNavigator extends JPanel
	implements ActionListener
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Wizard component */
	private Wizard wizard;

	/** Wizard resource file */
	private ResourceCollection wizardResourceCollection;

	/** Back button */
	JButton back;

	/** Next button */
	JButton next;

	/** Finish button */
	JButton finish;

	/** Cancel button */
	JButton cancel;

	/** Close button */
	JButton close;

	/** Help button */
	JButton help;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param wizard Wizard component that own the navigator
	 * @param wizardResourceCollection Wizard resource file
	 */
	public WizardNavigator(Wizard wizard, ResourceCollection wizardResourceCollection)
	{
		this.wizard = wizard;
		this.wizardResourceCollection = wizardResourceCollection;

		setOpaque(true);
		setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		setBorder(new EdgeBorder(SwingConstants.NORTH));

		back = createButton("Back");
		back.setMargin(new Insets(1, 5, 1, 10));
		back.addActionListener(this);
		add(back);

		next = createButton("Next");
		next.setHorizontalTextPosition(SwingConstants.LEFT);
		next.setMargin(new Insets(1, 1, 1, 1));
		next.addActionListener(this);
		add(next);

		finish = createButton("Finish");
		finish.addActionListener(this);
		add(finish);

		cancel = createButton("Cancel");
		cancel.addActionListener(this);
		add(cancel);

		close = createButton("Close");
		close.addActionListener(this);
		add(close);

		help = new JButton("Help");
		help.setMnemonic('H');
		add(help);
	}

	/**
	 * Show or hides the help button.
	 * @nowarn
	 */
	public void setShowHelp(boolean showHelp)
	{
		help.setVisible(showHelp);
	}

	/**
	 * Creates a button.
	 *
	 * @param name Name of the button. Used for resource lookup.
	 * @return The new button
	 */
	private JButton createButton(String name)
	{
		String lowName = name.toLowerCase();

		String title = null;
		ImageIcon icon = null;
		char mnemonic = '\0';
		int x = 75;
		int y = 26;

		if (wizardResourceCollection != null)
		{
			title = wizardResourceCollection.getOptionalString(lowName + ".name", name);

			Object o = wizardResourceCollection.getOptionalObject(lowName + ".icon");
			if (o != null)
			{
				if (o instanceof ImageIcon)
					icon = (ImageIcon) o;
			}

			// TODO Feature 6: Get the menemonic from the button text from the ActionMgr
			mnemonic = ResourceCollectionUtil.getOptionalChar(wizardResourceCollection, lowName + ".mnemonic", title.charAt(0));

			x = ResourceCollectionUtil.getOptionalInt(wizardResourceCollection, lowName + ".xsize", x);
			y = ResourceCollectionUtil.getOptionalInt(wizardResourceCollection, lowName + ".ysize", y);
		}

		if (title == null)
		{
			title = name;
		}
		if (icon == null)
		{
			URL url = getClass().getClassLoader().getResource("com/openbp/swing/components/wizard/" + name + ".gif");
			if (url != null)
			{
				icon = new ImageIcon(url);
			}
		}
		if (mnemonic == '\0')
		{
			mnemonic = title.charAt(0);
		}

		JButton button = new JButton(title, icon);
		button.setMnemonic(mnemonic);
		button.setPreferredSize(new Dimension(x, y));

		return button;
	}

	/**
	 * Action listener implementation for navigator buttons.
	 *
	 * @param event Event
	 */
	public void actionPerformed(ActionEvent event)
	{
		Object source = event.getSource();
		if (source == back)
		{
			wizard.displayBack();
		}
		else if (source == next)
		{
			wizard.displayNext();
		}
		else if (source == finish)
		{
			wizard.finish();
		}
		else if (source == cancel)
		{
			wizard.cancel();
		}
		else if (source == close)
		{
			wizard.close();
		}
	}

	/**
	 * Updates the status of the navigator buttons.
	 */
	public void updateNavigator()
	{
		boolean canBack = wizard.hasBackward() && wizard.canMoveBackward();
		back.setEnabled(canBack);

		boolean canForward = wizard.hasForward() && wizard.canMoveForward();
		next.setEnabled(canForward);

		String resultPageName = wizard.getResultPageName();
		if (resultPageName != null && resultPageName.equals(wizard.getManager().getCurrent()))
		{
			// This is the result page; show the close button instead of the cancel button
			finish.setEnabled(false);

			cancel.setVisible(false);
			close.setVisible(true);
		}
		else
		{
			// Regular page
			boolean canFinish = wizard.canFinish();
			finish.setEnabled(canFinish);

			cancel.setVisible(true);
			close.setVisible(false);
		}

		boolean canCancel = wizard.canCancel();
		cancel.setEnabled(canCancel);
		close.setEnabled(canCancel);

		paintAll(getGraphics());
	}
}
