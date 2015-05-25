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
import java.awt.Dialog;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.openbp.common.rc.ResourceCollection;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraToolbarButton;
import org.openbp.jaspira.option.Option;
import org.openbp.jaspira.option.OptionWidget;
import org.openbp.swing.SwingUtil;
import org.openbp.swing.components.PasswordDialog;

/**
 * This is a simple OptionWidget whitch shows a text field and
 * manages the input events.
 *
 * @author Andreas Putz
 */
public class PasswordWidget extends OptionWidget
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Component that will be returned */
	private JPanel panel;

	/** The password */
	private String password;

	/** Password field */
	private JLabel passwordField;

	/** Resource */
	private ResourceCollection resourceCollection;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param option Option the widget refers to
	 * @param resourceCollection Resource to use for the titles and buttons
	 */
	public PasswordWidget(Option option, ResourceCollection resourceCollection)
	{
		super(option);

		this.resourceCollection = resourceCollection;
		panel = new JPanel(new BorderLayout());

		JComponent heading = createHeading();
		if (heading != null)
		{
			panel.add(heading, BorderLayout.WEST);
		}

		passwordField = new JLabel();
		passwordField.setHorizontalTextPosition(SwingConstants.LEFT);
		passwordField.setHorizontalAlignment(SwingConstants.LEFT);

		JPanel pwfPane = new JPanel(new BorderLayout());
		pwfPane.add(passwordField, BorderLayout.WEST);
		pwfPane.setOpaque(false);
		panel.add(pwfPane, BorderLayout.CENTER);

		Box btnPane = Box.createHorizontalBox();
		JaspiraToolbarButton setPWBtn = new JaspiraToolbarButton(new PWAction(resourceCollection, "passwordwidget.newbtn"));
		setPWBtn.setIconSize(12);
		btnPane.add(setPWBtn);
		JaspiraToolbarButton removePWBtn = new JaspiraToolbarButton(new JaspiraAction(resourceCollection, "passwordwidget.removebtn")
		{
			public void actionPerformed(ActionEvent ae)
			{
				setValue(null);
				notifyOptionMgrOfOptionChange();
			}
		});
		removePWBtn.setIconSize(12);
		btnPane.add(removePWBtn);
		panel.add(btnPane, BorderLayout.EAST);
	}

	//////////////////////////////////////////////////
	// @@ OptionWidget implementation
	//////////////////////////////////////////////////

	public Object getValue()
	{
		return password;
	}

	public void setValue(Object o)
	{
		this.password = o == null ? null : o.toString();
		if (password == null || password.trim().length() == 0)
			passwordField.setText(resourceCollection.getRequiredString("passwordwidget.inactive"));
		else
			passwordField.setText(resourceCollection.getRequiredString("passwordwidget.active"));
	}

	public JComponent getWidgetComponent()
	{
		return panel;
	}

	//////////////////////////////////////////////////
	// @@ Password action
	//////////////////////////////////////////////////

	/**
	 * Password dialog action
	 */
	private class PWAction extends JaspiraAction
	{
		//////////////////////////////////////////////////
		// @@ Members
		//////////////////////////////////////////////////

		/** Resource */
		private ResourceCollection resourceCollection;

		//////////////////////////////////////////////////
		// @@ Construction
		//////////////////////////////////////////////////

		/**
		 * Constructor.
		 *
		 * @param resourceCollection The resource
		 * @param name The action name
		 */
		private PWAction(ResourceCollection resourceCollection, String name)
		{
			super(resourceCollection, name);
			this.resourceCollection = resourceCollection;
		}

		//////////////////////////////////////////////////
		// @@ Action performed method
		//////////////////////////////////////////////////

		/**
		 * Called if the action has been activated.
		 *
		 * @param ae Event
		 */
		public void actionPerformed(ActionEvent ae)
		{
			PasswordDialog dialog = new PasswordDialog((Dialog) PasswordWidget.this.panel.getTopLevelAncestor(), true, resourceCollection);
			SwingUtil.show(dialog);

			if (dialog.isCancelled())
				return;

			PasswordWidget.this.setValue(dialog.getPassword());
			notifyOptionMgrOfOptionChange();
		}
	}
}
