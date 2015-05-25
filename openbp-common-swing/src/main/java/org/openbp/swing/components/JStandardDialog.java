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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.openbp.swing.plaf.sky.ShadowBorder;
import org.openbp.swing.plaf.sky.SkyTheme;

/**
 * The standard dialog consists of a scroll pane and a button pane containing
 * an Ok and (optional) Cancel button.
 *
 * The scroll pane is the content pane of the dialog. Add any dialog component to the
 * scroll pane, which can be retrieved by calling the getContentPane method.
 *
 * If the user clicks the 'Ok' button or presses ENTER, an Ok event will result.
 * If the user clicks the 'Cancel' button, the close button of the title bar or presses ESCAPE,
 * a Cancel event will be generated.<br>
 * The Ok and Cancel events of the dialog can be processed by overriding the
 * {@link #handleOk} and {@link #handleCancel} methods.
 *
 * If you want to hide or enable/disable a particular button or change the button text,
 * use the {@link #getOkBtn} or {@link #getCancelBtn} methods to access the buttons.
 *
 * Show the dialog using the {@link #setVisible} method.
 * The dialog will be displayed centered to the own window of the dialog (or the screen
 * if there is none).
 *
 * @author Heiko Erhardt
 */
public class JStandardDialog extends JDialog
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Main content panel */
	private JPanel mainPane;

	/** Button pane */
	private JPanel btnPane;

	/** Ok button */
	private JButton okBtn;

	/** Cancel button */
	private JButton cancelBtn;

	/** Status flag: Dialog was cancelled */
	private boolean cancelled;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor for a modal dialog.
	 */
	public JStandardDialog()
	{
		this((Frame) null, true);
	}

	/**
	 * Constructs a modal dialog.
	 * @param owner Owning frame. The dialog will be centered over the owner.
	 */
	public JStandardDialog(Frame owner)
	{
		this(owner, true);
	}

	/**
	 * Constructor. Creates a dialog with event handling and buttons.
	 * @param modal
	 *		true	Display as modal dialog.<br>
	 *		false	Display as modeless dialog.
	 */
	public JStandardDialog(boolean modal)
	{
		this((Frame) null, modal);
	}

	/**
	 * Constructor.
	 * @param owner Owning frame. The dialog will be centered over the owner.
	 * @param modal
	 *		true	Display as modal dialog.<br>
	 *		false	Display as modeless dialog.
	 */
	public JStandardDialog(Frame owner, boolean modal)
	{
		super(owner, modal);
		initialize(true);
	}

	/**
	 * Constructor. Creates a dialog with event handling and buttons.
	 * @param owner Owning dialog. The dialog will be centered over the owner.
	 * @param modal
	 *		true	Display as modal dialog.<br>
	 *		false	Display as modeless dialog.
	 */
	public JStandardDialog(Dialog owner, boolean modal)
	{
		super(owner, modal);
		initialize(true);
	}

	/**
	 * Constructor.
	 *
	 * @param owner Owning frame or null
	 * @param title Title of the dialog or null
	 * @param modal
	 *		true	Creates a modal dialog.<br>
	 *		false	Creates a modeless dialog.
	 * @param gc Graphics configuration or null - i.e. to use alternate desktop
	 * @param showbuttons
	 *		true	Shows the Ok and Cancel buttons.<br>
	 *		false	Does not add the button pane.
	 */
	public JStandardDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc, boolean showbuttons)
	{
		super(owner, title, modal, gc);
		initialize(showbuttons);
	}

	/**
	 * Constructor.
	 *
	 * @param owner Owning Dialog or null
	 * @param title Title of the dialog or null
	 * @param modal
	 *		true	Creates a modal dialog.<br>
	 *		false	Creates a modeless dialog.
	 * @param gc Graphics configuration or null - i.e. to use alternate desktop
	 * @param showbuttons
	 *		true	Shows the Ok and Cancel buttons.<br>
	 *		false	Does not add the button pane.
	 */
	public JStandardDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc, boolean showbuttons)
	{
		super(owner, title, modal, gc);
		initialize(showbuttons);
	}

	/**
	 * Initializes the dialog.
	 * @param showbuttons
	 *		true	Shows the Ok and Cancel buttons.<br>
	 *		false	Does not add the button pane.
	 */
	private void initialize(boolean showbuttons)
	{
		// Add content pane
		mainPane = new JPanel(new BorderLayout());
		mainPane.setBackground(SkyTheme.COLOR_BACKGROUND_LIGHT);
		mainPane.setBorder(new ShadowBorder());
		getContentPane().add(mainPane, BorderLayout.CENTER);

		ActionListener cancelListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				handleCancel();
			}
		};

		ActionListener okListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				handleOk();
			}
		};

		// Set up button pane
		// TOLOCALIZE
		if (showbuttons)
		{
			cancelBtn = new JButton("Cancel");
			cancelBtn.addActionListener(cancelListener);

			okBtn = new JButton("Ok");
			okBtn.addActionListener(okListener);

			btnPane = new JPanel();
			btnPane.setBackground(SkyTheme.COLOR_BACKGROUND_LIGHT);
			btnPane.setBorder(new ShadowBorder());

			btnPane.add(okBtn);
			btnPane.add(cancelBtn);

			getContentPane().add(btnPane, BorderLayout.SOUTH);

			btnPane.registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			btnPane.registerKeyboardAction(okListener, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		}

		// Catch ESC and ENTER keys
		mainPane.registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		mainPane.registerKeyboardAction(okListener, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	//////////////////////////////////////////////////
	// @@ JDialog overrides
	//////////////////////////////////////////////////

	/**
	 * Shows or hides the dialog
	 *
	 * @param visible
	 *		true	Shows the dialog
	 *		false	Hides the dialog
	 */
	public void setVisible(boolean visible)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension size = getSize();
		if (size.height > screenSize.height)
			size.height = screenSize.height;
		if (size.width > screenSize.width)
			size.width = screenSize.width;
		setSize(size);

		if (getOwner() != null)
		{
			setLocationRelativeTo(getOwner());
		}
		else
		{
			// Center the window
			setLocation((screenSize.width - size.width) / 2, (screenSize.height - size.height) / 2);
		}

		super.setVisible(visible);
	}

	//////////////////////////////////////////////////
	// @@ Overridables
	//////////////////////////////////////////////////

	/**
	 * Handles the Ok event.
	 * Called if the user clicks the 'Ok' button or presses ENTER.
	 * The default implementation hides the dialog and disposes it.
	 */
	protected void handleOk()
	{
		setVisible(false);
		dispose();
	}

	/**
	 * Handles the Ok event.
	 * Called if the user clicks the 'Cancel' button, the close button of the title bar or presses ESCAPE.
	 * The default implementation hides the dialog and disposes it.
	 */
	protected void handleCancel()
	{
		cancelled = true;
		setVisible(false);
		dispose();
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the main content panel, which contains the actual contents of the dialog.
	 * @nowarn
	 */
	public JPanel getMainPane()
	{
		return mainPane;
	}

	/**
	 * Gets the button pane.
	 * @nowarn
	 */
	public JPanel getBtnPane()
	{
		return btnPane;
	}

	/**
	 * Gets the ok button.
	 * @nowarn
	 */
	public JButton getOkBtn()
	{
		return okBtn;
	}

	/**
	 * Gets the cancel button.
	 * @nowarn
	 */
	public JButton getCancelBtn()
	{
		return cancelBtn;
	}

	/**
	 * Gets the status flag: Dialog was cancelled.
	 * @nowarn
	 */
	public boolean isCancelled()
	{
		return cancelled;
	}

	/**
	 * Sets the status flag: Dialog was cancelled.
	 * @nowarn
	 */
	public void setCancelled(boolean cancelled)
	{
		this.cancelled = cancelled;
	}
}
