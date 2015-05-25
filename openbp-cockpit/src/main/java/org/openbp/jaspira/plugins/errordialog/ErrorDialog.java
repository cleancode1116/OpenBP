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
package org.openbp.jaspira.plugins.errordialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.markup.HTMLEscapeHelper;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.swing.SwingUtil;
import org.openbp.swing.components.JStandardDialog;
import org.openbp.swing.plaf.sky.SimpleBorder;

/**
 * The dialog shows the exception and the error message.
 *
 * @author Andreas Putz
 */
public class ErrorDialog extends JStandardDialog
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	public static String KEY_BUTTON_CLOSE = "errordialog.close";

	public static String KEY_TITLE = "errordialog.title";

	public static String KEY_BUTTON_DETAILS_TEXT = "errordialog.button.detail.text";

	public static String KEY_NO_MESSAGE = "errordialog.nomessage";

	static String DEFAULT_BUTTON_CLOSE = "Close";

	static String DEFAULT_TITLE = "Error dialog";

	static String DEFAULT_NO_MESSAGE = "No error message available";

	static String DEFAULT_BUTTON_DETAILS = "Details";

	/** Minimum width */
	static final int DEFAULT_WIDTH = 600;

	/** Height of the dialog in regular mode */
	static final int REGULAR_HEIGHT = 200;

	/** Height of the dialog in detail mode */
	static final int DETAIL_HEIGHT = 400;

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** The error message */
	private String message;

	/** The throwable like an exception */
	private Throwable throwable;

	/** The stacktrace as string */
	private String stackTraceString;

	//////////////////////////////////////////////////
	// @@ Panel members
	//////////////////////////////////////////////////

	/** The detail panel */
	private JPanel detailPanel;

	/** Detail initialize flag */
	private boolean detailsInitialized;

	/** Detail button */
	private JToggleButton detailButton;

	/** Resource */
	private static ResourceCollection resourceCollection;

	//////////////////////////////////////////////////
	// @@ Static methods
	//////////////////////////////////////////////////

	/**
	 * Shows the exception dialog.
	 *
	 * @param frame The parent frame
	 * @param modal
	 *	true	The dialog is modal<br>
	 *  false	The dialog is non modal
	 * @param message The error message
	 * @param t The throwable (exception)
	 *
	 * @return The dialog
	 */
	public static JDialog showDialog(JFrame frame, boolean modal, String message, Throwable t)
	{
		return showDialog(frame, modal, message, t, null);
	}

	/**
	 * Shows the exception dialog.
	 *
	 * @param frame The parent frame
	 * @param modal
	 *	true	The dialog is modal<br>
	 *  false	The dialog is non modal
	 * @param message The error message
	 * @param exceptionString The full exception info as string
	 *
	 * @return The dialog
	 */
	public static JDialog showDialog(JFrame frame, boolean modal, String message, String exceptionString)
	{
		return showDialog(frame, modal, message, null, exceptionString);
	}

	/**
	 * Shows the exception dialog.
	 *
	 * @param frame The parent frame
	 * @param modal
	 *	true	The dialog is modal<br>
	 *  false	The dialog is non modal
	 * @param message The error message
	 * @param t The throwable (exception)
	 * @param exceptionString The full exception info as string
	 *
	 * @return The dialog
	 */
	static JDialog showDialog(JFrame frame, boolean modal, String message, Throwable t, String exceptionString)
	{
		ErrorDialog ed = new ErrorDialog(frame, modal);
		ed.setMessage(message);

		if (t != null)
			ed.setThrowable(t);

		// Sets the exception string only if the exception string is not null,
		// because the {@link #setThrowable} () set the stacktrace string too.
		if (exceptionString != null)
			ed.setStackTraceString(exceptionString);

		ed.initialize();

		ed.setLocationRelativeTo(frame);

		SwingUtil.show(ed);
		return ed;
	}

	/**
	 * Sets the resource.
	 * @nowarn
	 */
	public static void setResourceCollection(ResourceCollection resourceArg)
	{
		resourceCollection = resourceArg;
	}

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param owner Owner frame
	 * @param modal Modal flag
	 *
	 * @throws HeadlessException see JDialog
	 */
	public ErrorDialog(Frame owner, boolean modal)
		throws HeadlessException
	{
		super(owner, modal);
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Set the the error message.
	 *
	 * @param message The error message
	 */
	void setMessage(String message)
	{
		this.message = message;
	}

	/**
	 * Set the throwable.
	 *
	 * @param throwable Throwable
	 */
	void setThrowable(Throwable throwable)
	{
		this.throwable = throwable;
		this.stackTraceString = ExceptionUtil.getNestedTrace(throwable);
	}

	/**
	 * Sets the stack trace as string.
	 * It is needed by exception messages
	 * whitout any throwable objects.
	 *
	 * @param stacktrace The string of a stacktrace
	 */
	void setStackTraceString(String stacktrace)
	{
		this.stackTraceString = stacktrace;
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Initialize the dialog.
	 */
	public void initialize()
	{
		setTitle(resourceCollection.getOptionalString(KEY_TITLE, DEFAULT_TITLE));

		// Sets the button panel
		getOkBtn().setText(resourceCollection.getOptionalString(KEY_BUTTON_CLOSE, DEFAULT_BUTTON_CLOSE));
		getCancelBtn().setVisible(false);

		JPanel buttonPanel = getBtnPane();

		detailButton = new JToggleButton();
		String text = resourceCollection.getOptionalString(KEY_BUTTON_DETAILS_TEXT, DEFAULT_BUTTON_DETAILS);
		detailButton.setText(text);
		detailButton.addActionListener(new DetailActionListener());
		buttonPanel.add(detailButton, 1);

		// Sets the center panel
		createCenterPanel();

		pack();
	}

	//////////////////////////////////////////////////
	// @@ Private methods
	//////////////////////////////////////////////////

	/**
	 * Creates the panel which is positioned to the
	 * center of the dialog.
	 * The panel contains the message information and
	 * the stacktrace of the throwable.
	 */
	private void createCenterPanel()
	{
		JPanel pane = new JPanel();
		getMainPane().add(pane, BorderLayout.CENTER);
		pane.setBorder(new EmptyBorder(5, 5, 5, 5));
		pane.setBackground(getMainPane().getBackground());

		BoxLayout layout = new BoxLayout(pane, BoxLayout.Y_AXIS);
		pane.setLayout(layout);

		Dimension size = new Dimension(DEFAULT_WIDTH, REGULAR_HEIGHT);
		pane.setMinimumSize(size);
		pane.setPreferredSize(size);

		// Creates the message text
		StringBuffer buf = new StringBuffer(200);
		if (message != null)
		{
			buf.append(message);
			buf.append("\n\n");
		}
		if (throwable != null && throwable.getMessage() != null)
		{
			buf.append(throwable.getMessage());
		}
		else if (stackTraceString != null)
		{
			String s = stackTraceString;
			if (s.startsWith("java.lang."))
			{
				int pos = stackTraceString.indexOf('\n');
				if (pos > 0)
				{
					s = stackTraceString.substring(0, pos);
				}
			}
			else
			{
				int pos1 = stackTraceString.indexOf(':') + 2;
				int pos2 = stackTraceString.indexOf("\tat", pos1);
				if (pos1 >= 10 && pos2 > 0 && pos1 < pos2)
				{
					s = stackTraceString.substring(pos1, pos2);
				}
			}
			buf.append(s);
		}

		if (buf.length() == 0)
		{
			buf.append(resourceCollection.getOptionalString(KEY_NO_MESSAGE, DEFAULT_NO_MESSAGE));
		}

		int lines = 0;
		for (int i = buf.length() - 1; i >= 0; --i)
		{
			if (buf.charAt(i) == '\n')
				++lines;
		}

		Box messageBox = Box.createHorizontalBox();

		Icon icon = (Icon) UIManager.get("OptionPane.warningIcon");

		/*
		 if (buf.length () > 400)
		 {
		 buf.setLength (403);
		 buf.setCharAt (400, '.');
		 buf.setCharAt (401, '.');
		 buf.setCharAt (402, '.');
		 }
		 */
		String s = buf.toString();
		s = HTMLEscapeHelper.htmlescape(s);

		JLabel label = new JLabel("<html>" + s + "<br></html>");
		label.setBorder(new EmptyBorder(10, 10, 10, 10));
		label.setIcon(icon);
		label.setIconTextGap(15);
		label.setVerticalAlignment(SwingConstants.TOP);
		label.setVerticalTextPosition(SwingConstants.TOP);

		if (lines > 5)
		{
			messageBox.add(new JScrollPane(label));
		}
		else
		{
			messageBox.add(label);
		}
		messageBox.add(Box.createHorizontalGlue());

		pane.add(messageBox);

		detailPanel = new JPanel();
		detailPanel.setVisible(false);
		pane.add(detailPanel);
	}

	//////////////////////////////////////////////////
	// @@ Inner classes
	//////////////////////////////////////////////////

	/**
	 * Action listener implementation for the detail button.
	 */
	private class DetailActionListener
		implements ActionListener
	{
		//////////////////////////////////////////////////
		// @@ Listener implementation
		//////////////////////////////////////////////////

		/**
		 * see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 * @nowarn
		 */
		public void actionPerformed(ActionEvent e)
		{
			boolean showDetails = !((JToggleButton) e.getSource()).isSelected();
			if (!showDetails && !detailsInitialized)
			{
				// initialize the detail panel
				detailPanel.setLayout(new BorderLayout());
				detailPanel.setBorder(SimpleBorder.getStandardBorder());

				JTextArea stackTrace = new JTextArea();
				JScrollPane scrollPane = new JScrollPane(stackTrace);
				scrollPane.getViewport().setViewPosition(new Point(0, 0));
				detailPanel.add(scrollPane, BorderLayout.CENTER);

				stackTrace.setEditable(false);

				// Sets the stack trace information
				stackTrace.setText(stackTraceString);

				detailsInitialized = true;
			}

			showDetails = !showDetails;

			detailPanel.setVisible(showDetails);

			JPanel panel = (JPanel) getContentPane();

			Dimension size = panel.getSize();
			if (size.width < WIDTH)
				size.width = WIDTH;
			size.height = showDetails ? DETAIL_HEIGHT : REGULAR_HEIGHT;
			panel.setPreferredSize(size);

			pack();
		}
	}
}
