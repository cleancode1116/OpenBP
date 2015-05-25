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
package org.openbp.swing.components;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.openbp.common.rc.ResourceCollection;
import org.openbp.swing.SwingUtil;

/**
 * Dialog to input a password.
 *
 * @author Andreas Putz
 */
public class PasswordDialog extends JStandardDialog
{
	//////////////////////////////////////////////////
	// @@ GUI Members
	//////////////////////////////////////////////////

	/** Resource */
	private ResourceCollection res;

	/** Password field for input */
	private JPasswordField pwd1;

	/** Repeated password input field */
	private JPasswordField pwd2;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Password */
	private String password;

	/** Flag wheter the dialog is displayed to set the password or to request the password */
	private boolean definePassword;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param owner The parent frame
	 * @param definePassword
	 *		true	Adds password and password confirmation field<br>
	 *		false	Omits password fields
	 * @param res The resource
	 */
	public PasswordDialog(Frame owner, boolean definePassword, ResourceCollection res)
	{
		super(owner, true);
		this.definePassword = definePassword;
		this.res = res;
		initialize();
		pack();
	}

	/**
	 * Constructor.
	 *
	 * @param owner The parent dialog
	 * @param definePassword
	 *		true	Adds password and password confirmation field<br>
	 *		false	Omits password fields
	 * @param res The resource
	 */
	public PasswordDialog(Dialog owner, boolean definePassword, ResourceCollection res)
	{
		super(owner, true);
		this.definePassword = definePassword;
		this.res = res;
		initialize();
	}

	/**
	 * Initializes this dialog.
	 */
	private void initialize()
	{
		setTitle(res.getRequiredString("password-dialog.title"));
		setCancelled(true);

		Box pane = Box.createVerticalBox();
		pane.setBorder(new EmptyBorder(5, 5, 5, 5));
		getMainPane().setLayout(new BorderLayout());
		getMainPane().add(pane, BorderLayout.CENTER);

		JPanel labelPanel = new JPanel(new BorderLayout());
		labelPanel.setBackground(getMainPane().getBackground());
		JLabel label = new JLabel(res.getRequiredString("password-dialog.message"));
		label.setHorizontalAlignment(SwingConstants.LEFT);
		label.setVerticalAlignment(SwingConstants.TOP);
		labelPanel.add(label, BorderLayout.CENTER);

		pane.add(labelPanel);
		pane.add(Box.createVerticalStrut(6));

		Box b1 = Box.createHorizontalBox();
		pane.add(b1);
		pane.add(Box.createVerticalStrut(3));
		b1.add(new JLabel(res.getRequiredString("password-dialog.password1")));
		b1.add(Box.createHorizontalStrut(10));
		pwd1 = new JPasswordField(10);
		b1.add(pwd1);

		if (definePassword)
		{
			Box b2 = Box.createHorizontalBox();
			pane.add(b2);
			pane.add(Box.createVerticalStrut(3));

			b2.add(new JLabel(res.getRequiredString("password-dialog.password2")));
			b2.add(Box.createHorizontalStrut(10));
			pwd2 = new JPasswordField(10);
			b2.add(pwd2);
		}

		pack();
		if (getOwner() != null)
		{
			setLocationRelativeTo(getOwner());
		}
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Gets the password.
	 * @nowarn
	 */
	public String getPassword()
	{
		return password;
	}

	//////////////////////////////////////////////////
	// @@ Event handler overrides
	//////////////////////////////////////////////////

	/**
	 * @copy JStandardDialog.handleOk
	 */
	protected void handleOk()
	{
		char [] tmp1 = pwd1.getPassword();
		boolean tmp2IsNull = definePassword;

		if (definePassword)
		{
			char [] tmp2 = pwd2.getPassword();

			boolean equals = Arrays.equals(tmp1, tmp2);

			if (!equals)
			{
				JOptionPane.showMessageDialog(this, res.getRequiredString("password-dialog.errormessage.notequal"));
				return;
			}

			tmp2IsNull = (tmp2 == null || tmp2.length == 0);
		}

		if (tmp1 == null || tmp1.length == 0 || tmp2IsNull)
		{
			JOptionPane.showMessageDialog(this, res.getRequiredString("password-dialog.errormessage.isnull"));
			return;
		}

		if (tmp1 != null)
		{
			password = pwd1.getPassword().toString();
		}
		else
		{
			password = null;
		}

		setCancelled(false);
		super.handleOk();
	}

	//////////////////////////////////////////////////
	// @@ Main method
	//////////////////////////////////////////////////

	/**
	 * Sample.
	 * @nowarn
	 */
	public static void main(String [] args)
	{
		PasswordDialog dialog = new PasswordDialog((Frame) null, true, null);
		dialog.pack();
		SwingUtil.show(dialog);
		if (dialog.isCancelled())
			System.out.println("cancelled");
		else
			System.out.println(dialog.getPassword());
	}
}
