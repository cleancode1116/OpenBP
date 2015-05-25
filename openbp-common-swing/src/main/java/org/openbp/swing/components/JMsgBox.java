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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.openbp.common.MsgFormat;
import org.openbp.common.icon.FlexibleSize;
import org.openbp.common.icon.MultiIcon;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.rc.ResourceCollectionMgr;
import org.openbp.common.string.TextUtil;
import org.openbp.swing.SwingUtil;
import org.openbp.swing.layout.VerticalFlowLayout;

/**
 * General purpose modal message box component.
 * To be used similar to the JOptionPane class.
 * This class subclasses the JDialog class in order to provide functionality
 * commonly needed in message boxes.
 *
 * The message box provides some additional features in addition to the features
 * known by JOptionPane:<br>
 * - Resource based images and button texts<br>
 * - 'Yes for all'/'No for all' button<br>
 * - Extended accelerators<br>
 * - Help support using help strategy (planned)<br>
 * - Stack trace display support using an JTextField (planned)
 *
 * A special feature is the {@link #TYPE_OKLATER} flag for the message type.
 * If set, the message box will be displayed at a later time using
 * {@link javax.swing.SwingUtilities#invokeLater(Runnable)} method.
 * This is very useful if e. g. a field validation is performed from withing a
 * focus lost handler. Displaying the message box at this point in time would lead
 * to a hangup of the Swing event thread. Using TYPE_OKLATER will immediately return,
 * displaying the dialog after the processing of the current event has taken place.
 *
 * @author Stephan Schmid
 */
public class JMsgBox extends JDialog
	implements ActionListener
{
	//////////////////////////////////////////////////
	// @@ Button constants
	//////////////////////////////////////////////////

	/** Message type: Ok button */
	public static final int TYPE_OK = (1 << 0);

	/** Message type: Cancel button */
	public static final int TYPE_CANCEL = (1 << 1);

	/** Message type: Ok and cancel button */
	public static final int TYPE_OKCANCEL = (TYPE_OK | TYPE_CANCEL);

	/** Message type: Yes button */
	public static final int TYPE_YES = (1 << 2);

	/** Message type: No button */
	public static final int TYPE_NO = (1 << 3);

	/** Message type: Yes and no button */
	public static final int TYPE_YESNO = (TYPE_YES | TYPE_NO);

	/** Message type: Yes, no and cancel button */
	public static final int TYPE_YESNOCANCEL = (TYPE_YES | TYPE_NO | TYPE_CANCEL);

	/** Message type: Yes for all button */
	public static final int TYPE_YES_FOR_ALL = (1 << 4);

	/** Message type: No for all button */
	public static final int TYPE_NO_FOR_ALL = (1 << 5);

	/** Message type: Yes/No for all button */
	public static final int TYPE_FOR_ALL = (TYPE_YES_FOR_ALL | TYPE_NO_FOR_ALL);

	/** Message type: Abort button */
	public static final int TYPE_ABORT = (1 << 6);

	/** Message type: Retry button */
	public static final int TYPE_RETRY = (1 << 7);

	/** Message type: Ignore button */
	public static final int TYPE_IGNORE = (1 << 8);

	/** Additional message type: Display 'Do not show this message again' checkbox */
	public static final int TYPE_DO_NOT_SHOW_AGAIN = (1 << 9);

	//////////////////////////////////////////////////
	// @@ Icon constants
	//////////////////////////////////////////////////

	/** Message type: Info icon */
	public static final int ICON_INFO = (1 << 16);

	/** Message type: Warning icon */
	public static final int ICON_WARNING = (1 << 17);

	/** Message type: Error icon */
	public static final int ICON_ERROR = (1 << 18);

	/** Message type: Fatal icon */
	public static final int ICON_FATAL = (1 << 19);

	/** Message type: Question icon */
	public static final int ICON_QUESTION = (1 << 20);

	//////////////////////////////////////////////////
	// @@ Defaultbutton constants
	//////////////////////////////////////////////////

	/** Message type: Default button ok */
	public static final int DEFAULT_OK = (1 << 21);

	/** Message type: Default button cancel */
	public static final int DEFAULT_CANCEL = (1 << 22);

	/** Message type: Default button yes */
	public static final int DEFAULT_YES = (1 << 23);

	/** Message type: Default button no */
	public static final int DEFAULT_NO = (1 << 24);

	/** Message type: Default button abort */
	public static final int DEFAULT_ABORT = (1 << 25);

	/** Message type: Default button retry */
	public static final int DEFAULT_RETRY = (1 << 26);

	/** Message type: Default button ignore */
	public static final int DEFAULT_IGNORE = (1 << 27);

	//////////////////////////////////////////////////
	// @@ Invocation constants
	//////////////////////////////////////////////////

	/** Message flag: Show the dialog box using SwingUtilities.invokeLater */
	private static final int INVOKE_LATER = (1 << 30);

	/** Message type: Ok button */
	public static final int TYPE_OKLATER = (TYPE_OK | INVOKE_LATER);

	//////////////////////////////////////////////////
	// @@ Private constants
	//////////////////////////////////////////////////

	/** Border padding */
	private static final int PAD = 10;

	/** Outer border for message box */
	private static final Border OUTER_BORDER = BorderFactory.createEmptyBorder(PAD + 10, PAD + 10, PAD, PAD + 10);

	/** Etched border for message box */
	private static final Border ETCHED_BORDER = BorderFactory.createEtchedBorder();

	/** Inner border for message box */
	private static final Border INNER_BORDER = BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD);

	/** Button border for message box */
	private static final Border BUTTON_BORDER = BorderFactory.createEmptyBorder(PAD, PAD, 0, PAD);

	/** Border for the 'Do not show this messsage again' check box */
	private static final Border CHECKBOX_BORDER = BorderFactory.createEmptyBorder(PAD, PAD, 0, PAD);

	//////////////////////////////////////////////////
	// @@ Static data
	//////////////////////////////////////////////////

	/** Message box default resource */
	private static ResourceCollection defaultResourceCollection;

	/** Default title */
	private static String defaultTitle;

	/** Default message box owner provider */
	public static DefaultOwnerProvider defaultOwnerProvider;

	/** The icon to display */
	private static Icon icon;

	//////////////////////////////////////////////////
	// @@ Instance data
	//////////////////////////////////////////////////

	/** Message */
	private String msg;

	/** Message type */
	private int msgType;

	/** The return value of the message box */
	private int userChoice;

	/** The focussed button when a message box is openend */
	private JButton defaultButton;

	/** All JButtons to display */
	private ArrayList buttons;

	/**
	 * Keeps the JButtons as keys for the corresponding constants as values.
	 * (used to determine the return value)
	 */
	private Map typeByButton;

	/**
	 * Keeps the constants as keys for the corresponding JButtons as values.
	 * (used to determine the default button)
	 */
	private Map buttonByType;

	/** 'Do not show this message again' check box */
	private JCheckBox doNotShowAgainCheckBox;

	/** Actual resource to use for this message box */
	private ResourceCollection resourceCollection;

	/** Actual resource to use for this message box */
	private String resourcePrefix;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Array of all button constants */
	private static final int [] allButtonConstants = new int [] { TYPE_OK, TYPE_YES, TYPE_NO, TYPE_YES_FOR_ALL, TYPE_NO_FOR_ALL, TYPE_CANCEL, TYPE_ABORT, TYPE_RETRY, TYPE_IGNORE };

	/** Array of all icon constants */
	private static final int [] allIconConstants = new int [] { ICON_INFO, ICON_WARNING, ICON_ERROR, ICON_FATAL, ICON_QUESTION };

	/** Array of all defaultbutton constants */
	private static final int [] allDefaultButtonConstants = new int [] { DEFAULT_OK, DEFAULT_CANCEL, DEFAULT_YES, DEFAULT_NO, DEFAULT_ABORT, DEFAULT_RETRY, DEFAULT_IGNORE };

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	static
	{
		defaultResourceCollection = ResourceCollectionMgr.getDefaultInstance().getResource(SwingUtil.RESOURCE_COMMON, JMsgBox.class);
	}

	/**
	 * Constructor.
	 *
	 * @param owner Parent window that opened the message box
	 * @param title Title of the message box
	 * @param msg Message to display in the message box
	 * @param msgType Msg type (see the constants of this class)
	 */
	public JMsgBox(Component owner, String title, String msg, int msgType)
	{
		super(getParentFrame(owner), title, true);

		this.msgType = msgType;
		this.msg = msg;
	}

	/**
	 * Gets the the return value of the message box.
	 * @nowarn
	 */
	public int getUserChoice()
	{
		return userChoice;
	}

	/**
	 * Gets the actual resource to use for this message box.
	 * @nowarn
	 */
	public ResourceCollection getResource()
	{
		return resourceCollection;
	}

	/**
	 * Sets the actual resource to use for this message box.
	 * @nowarn
	 */
	public void setResource(ResourceCollection resourceCollection)
	{
		this.resourceCollection = resourceCollection;
	}

	/**
	 * Gets the actual resource to use for this message box.
	 * @nowarn
	 */
	public String getResourcePrefix()
	{
		return resourcePrefix;
	}

	/**
	 * Sets the actual resource to use for this message box.
	 * @nowarn
	 */
	public void setResourcePrefix(String resourcePrefix)
	{
		this.resourcePrefix = resourcePrefix;
	}

	//////////////////////////////////////////////////
	// @@ Initialization
	//////////////////////////////////////////////////

	/**
	 * Initializes the dialog.
	 */
	public void initDialog()
	{
		buttons = new ArrayList();
		typeByButton = new HashMap();
		buttonByType = new HashMap();

		parseMsgType();

		createUI(msg);
		setLocationRelativeTo(getOwner());
	}

	/**
	 * Creates all necessary components for the UI.
	 * The UI consists of two JPanels - one containing the icon in the upper
	 * left corner and the message aside, the other holding the various buttons
	 * at the bottom of the window.
	 * To have an uncongested appearance, the borders are set fairly generous.
	 *
	 * @param msg Message to display
	 */
	private void createUI(String msg)
	{
		// We use an extra JPanel as contentpane
		JPanel content = new JPanel();
		content.setLayout(new VerticalFlowLayout());
		content.setBorder(OUTER_BORDER);

		// The standard panel contains the icon and the message
		JPanel standardPanel = new JPanel(new BorderLayout());
		standardPanel.setBorder(ETCHED_BORDER);

		if (icon != null)
		{
			JLabel iconPanel = new JLabel(icon);
			iconPanel.setBorder(INNER_BORDER);
			iconPanel.setVerticalAlignment(SwingConstants.TOP);
			standardPanel.add(BorderLayout.WEST, iconPanel);
		}

		// Split the message into separate lines
		List lines = TextUtil.breakIntoLines(msg, true, 100);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < lines.size(); i++)
		{
			if (i > 0)
				sb.append("\n");
			sb.append(lines.get(i));
		}
		msg = sb.toString();

		// We use a JTextArea component to display the message
		JTextArea messagePanel = new JTextArea(msg);
		messagePanel.setBorder(INNER_BORDER);
		messagePanel.setBackground(getContentPane().getBackground());

		Font font = messagePanel.getFont();
		int size = font.getSize() + 2;
		font = font.deriveFont((float) size);
		messagePanel.setFont(font);

		standardPanel.add(BorderLayout.CENTER, messagePanel);

		// Only the buttons should be able to gain focus, so ...
		messagePanel.setFocusable(false);

		// The button panel contains the buttons (obviously)
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BUTTON_BORDER);
		buttonPanel.setLayout(new FlowLayout());

		for (int i = 0; i < buttons.size(); i++)
		{
			// We place the buttons that were created earlier
			buttonPanel.add((JButton) buttons.get(i));
		}

		// Place the standard- and the button panel on the contentpane(l)
		content.add(standardPanel);
		content.add(buttonPanel);

		if ((msgType & TYPE_DO_NOT_SHOW_AGAIN) != 0)
		{
			// Add the 'Do not show this messsage again' check box
			String againMsg = defaultResourceCollection.getOptionalString("DoNotShowAgain");
			if (againMsg != null)
			{
				doNotShowAgainCheckBox = new JCheckBox(againMsg);
				doNotShowAgainCheckBox.setBorder(CHECKBOX_BORDER);
				content.add(doNotShowAgainCheckBox);
			}
		}

		// Set the content pane
		getContentPane().add(content);

		pack();
		setResizable(false);

		if (defaultButton != null)
		{
			defaultButton.requestFocus();
		}
	}

	//////////////////////////////////////////////////
	// @@ Basic static methods
	//////////////////////////////////////////////////

	/**
	 * Shows a modal message box using the default title.
	 *
	 * @param owner Owner window of this message box
	 * @param msg Message to display
	 * @param msgType Msg type (see the constants of this class)
	 * @return The user's response or 0 on error
	 */
	public static int show(Component owner, String msg, int msgType)
	{
		return show(owner, null, msg, msgType);
	}

	/**
	 * Shows a modal message box.
	 *
	 * @param owner Owner window of this message box
	 * @param title Message box title
	 * @param msg Message to display
	 * @param msgType Msg type (see the constants of this class)
	 * @return The user's response or 0 on error
	 */
	public static int show(Component owner, String title, String msg, int msgType)
	{
		if (title == null)
		{
			title = defaultTitle;
			if (title == null)
			{
				title = "Message";
			}
		}

		final JMsgBox msgBox = new JMsgBox(owner, title, msg, msgType);
		msgBox.initDialog();

		if ((msgType & INVOKE_LATER) != 0)
		{
			// We shall not display the dialog now, put it into the Swing event queue.
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					SwingUtil.show(msgBox);
				}
			});

			// Return immediately with the 'Ok' response
			return TYPE_OK;
		}

		SwingUtil.show(msgBox);
		return msgBox.userChoice;
	}

	/**
	 * Shows a modal message box using the default title.
	 *
	 * @param owner Owner window of this message box
	 * @param msg Message to display
	 * @param msgArgs Message arguments (for details see the {@link MsgFormat} class)
	 * @param msgType Msg type (see the constants of this class)
	 * @return The user's response or 0 on error
	 */
	public static int show(Component owner, String msg, Object [] msgArgs, int msgType)
	{
		return show(owner, null, msg, msgArgs, msgType);
	}

	/**
	 * Shows a modal message box.
	 *
	 * @param owner Owner window of this message box
	 * @param title Message box title
	 * @param msg Message to display
	 * @param msgArgs Message arguments (for details see the {@link MsgFormat} class)
	 * @param msgType Msg type (see the constants of this class)
	 * @return The user's response or 0 on error
	 */
	public static int show(Component owner, String title, String msg, Object [] msgArgs, int msgType)
	{
		msg = MsgFormat.format(msg, msgArgs);
		return show(owner, title, msg, msgType);
	}

	//////////////////////////////////////////////////
	// @@ Convenience methods for title-less message boxes using message format
	//////////////////////////////////////////////////

	/**
	 * Shows a modal message box using the default title.
	 *
	 * @param owner Owner window of this message box
	 * @param msg Message to display
	 * @param msgArg1 First message argument (for details see the {@link MsgFormat} class)
	 * @param msgType Msg type (see the constants of this class)
	 * @return The user's response or 0 on error
	 */
	public static int showFormat(Component owner, String msg, Object msgArg1, int msgType)
	{
		return show(owner, null, msg, new Object [] { msgArg1 }, msgType);
	}

	/**
	 * Shows a modal message box using the default title.
	 *
	 * @param owner Owner window of this message box
	 * @param msg Message to display
	 * @param msgArg1 First message argument (for details see the {@link MsgFormat} class)
	 * @param msgArg2 First message argument (for details see the {@link MsgFormat} class)
	 * @param msgType Msg type (see the constants of this class)
	 * @return The user's response or 0 on error
	 */
	public static int showFormat(Component owner, String msg, Object msgArg1, Object msgArg2, int msgType)
	{
		return show(owner, null, msg, new Object [] { msgArg1, msgArg2 }, msgType);
	}

	/**
	 * Shows a modal message box using the default title.
	 *
	 * @param owner Owner window of this message box
	 * @param msg Message to display
	 * @param msgArg1 First message argument (for details see the {@link MsgFormat} class)
	 * @param msgArg2 First message argument (for details see the {@link MsgFormat} class)
	 * @param msgArg3 First message argument (for details see the {@link MsgFormat} class)
	 * @param msgType Msg type (see the constants of this class)
	 * @return The user's response or 0 on error
	 */
	public static int showFormat(Component owner, String msg, Object msgArg1, Object msgArg2, Object msgArg3, int msgType)
	{
		return show(owner, null, msg, new Object [] { msgArg1, msgArg2, msgArg3 }, msgType);
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Sets the message box default resource.
	 * @nowarn
	 */
	public static void setDefaultResourceCollection(ResourceCollection defaultResourceCollectionArg)
	{
		defaultResourceCollection = defaultResourceCollectionArg;
	}

	/**
	 * Sets the default title.
	 * @nowarn
	 */
	public static void setDefaultTitle(String defaultTitleArg)
	{
		defaultTitle = defaultTitleArg;
	}

	/**
	 * Sets the default message box owner provider.
	 * @nowarn
	 */
	public static void setDefaultOwnerProvider(DefaultOwnerProvider defaultOwnerProviderArg)
	{
		defaultOwnerProvider = defaultOwnerProviderArg;
	}

	//////////////////////////////////////////////////
	// @@ Little helpers
	//////////////////////////////////////////////////

	/**
	 * Parses the msgType parameter and creates/initializes the various
	 * components (i. e. the buttons to display, the icon for the specific
	 * message box and the default button).
	 * If the msgType parameter doesn't make use of its 'overwrite capability'
	 * this method creates/initializes default components/values as follows:
	 *
	 * ICON
	 * If no icon is specified, the displayed icon depends on the given button
	 * constants:
	 * - Combination YES and NO always provides the question icon
	 * - Combination ABORT, RETRY and IGNORE always provides the warning icon
	 * - Any other button combination (or single button) provides the info icon
	 *
	 * DEFAULT BUTTON
	 * If no default button is specified, the leftmost button is the default
	 * button. The button order derives from the 'allButtonConstants'-array
	 * which already implements the correct 'default button hierarchy'.
	 * If only one button is assigned to the message box, this one naturally is
	 * the default button.
	 *
	 * BUTTONS
	 * If not even a button is specified (msgType = 0!), just an OK-Button is
	 * displayed under the terms described above.
	 *
	 * SPECIAL
	 * If ONLY an ICON constant and NO BUTTON constant is assigned to the
	 * message box, certain standard buttons will be generated:
	 * - Info icon provides the OK-Button
	 * - Warning icon provides button combination ABORT, RETRY and IGNORE
	 * - Error icon provides the OK-Button
	 * - Fatal icon provides the OK-Button
	 * - Question icon provides the button combination YES and NO
	 */
	private void parseMsgType()
	{
		ChoiceActionListener cancelListener = null;

		// Check if a button type has been specified, if not, choose a default
		if (!hasButtonType())
		{
			if ((msgType & ICON_QUESTION) != 0)
			{
				// Question icon -> Yes/no message box
				msgType |= TYPE_YESNO;
			}
			else
			{
				// Ok message box for any other situation
				msgType |= TYPE_OK;
			}
		}

		// Check for all buttons to display
		for (int i = 0; i < allButtonConstants.length; i++)
		{
			// We store all mnemonics not to have the same char twice!
			HashMap mnemonicChars = new HashMap();

			if ((msgType & allButtonConstants [i]) != 0)
			{
				// We have a button
				int buttonValue = allButtonConstants [i];
				String buttonType;

				// Check for the specific button type
				switch (buttonValue)
				{
				case TYPE_OK:
					buttonType = "ok";
					break;
				case TYPE_CANCEL:
					buttonType = "cancel";
					break;
				case TYPE_YES:
					buttonType = "yes";
					break;
				case TYPE_NO:
					buttonType = "no";
					break;
				case TYPE_YES_FOR_ALL:
					buttonType = "yesforall";
					break;
				case TYPE_NO_FOR_ALL:
					buttonType = "noforall";
					break;
				case TYPE_ABORT:
					buttonType = "abort";
					break;
				case TYPE_RETRY:
					buttonType = "retry";
					break;
				case TYPE_IGNORE:
					buttonType = "ignore";
					break;
				default:
					buttonType = "ok";
				}

				// Access the resource file
				String resourceGroup = "buttons." + buttonType;
				String buttonCaption = null;
				if (resourceCollection != null)
				{
					buttonCaption = resourceCollection.getOptionalString((resourcePrefix != null ? resourcePrefix : "") + resourceGroup + ".caption");
				}
				if (buttonCaption == null)
				{
					buttonCaption = defaultResourceCollection.getRequiredString(resourceGroup + ".caption");
				}

				char mnemonic = '~';

				int pos = buttonCaption.indexOf('_');

				if (pos != -1 && (pos + 1) < buttonCaption.length())
				{
					// A mnemonic was set
					mnemonic = buttonCaption.charAt(pos + 1);

					// Remember the mnemonic
					mnemonicChars.put(new Character(mnemonic), null);

					// Cut the delimiter from the display name
					buttonCaption = buttonCaption.substring(0, pos) + buttonCaption.substring(pos + 1);
				}
				else
				{
					// We use another char of the caption as mnemonic
					for (int counter = 0; counter < buttonCaption.length(); counter++)
					{
						Character bufferChar = new Character(buttonCaption.charAt(counter));

						if (!mnemonicChars.containsKey(bufferChar))
						{
							mnemonic = bufferChar.charValue();
							mnemonicChars.put(bufferChar, null);

							// We have a mnemonic
							break;
						}
					}
				}

				// Create the button
				JButton button = new JButton(buttonCaption);

				if (mnemonic != '~')
				{
					// A mnemonic was specified
					button.setMnemonic(mnemonic);

					// We will also register the mnemonic character as keyboard action.
					// This we eliminate the need to press the ALT button when choosing the answer.
					// Create an action listener for this particular button
					ChoiceActionListener listener = new ChoiceActionListener(buttonValue);

					// If it is a listener for some cancel action, save it as ESCAPE keyboard binding (see below)
					if (buttonValue == TYPE_OK || buttonValue == TYPE_CANCEL || buttonValue == TYPE_ABORT)
					{
						if (cancelListener == null || cancelListener.getChoice() == TYPE_OK)
						{
							cancelListener = listener;
						}
					}

					// Register it as keyboard action using the mnemonic
					char activationChar = mnemonic;
					if (Character.isLowerCase(activationChar))
						activationChar = Character.toUpperCase(activationChar);
					((JComponent) getContentPane()).registerKeyboardAction(listener, KeyStroke.getKeyStroke(activationChar, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
				}

				button.addActionListener(this);

				// We store the button in the various lists accessed later
				buttons.add(button);
				typeByButton.put(button, Integer.valueOf(buttonValue));
				buttonByType.put(Integer.valueOf(buttonValue), button);
			}
		}

		if (cancelListener != null)
		{
			((JComponent) getContentPane()).registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		}

		// Determine default icon type if no icon type given
		if (!hasIconType())
		{
			if (((msgType & TYPE_YES) != 0) && ((msgType & TYPE_NO) != 0))
			{
				// Yes and no leads to question icon
				msgType |= ICON_QUESTION;
			}
			else if (((msgType & TYPE_ABORT) != 0) && ((msgType & TYPE_RETRY) != 0) && ((msgType & TYPE_IGNORE) != 0))
			{
				// Abort, retry and ignore leads to warning icon
				msgType |= ICON_WARNING;
			}
			else
			{
				// In all other cases we use the information icon
				msgType |= ICON_INFO;
			}
		}

		// Check for a given icon
		for (int i = 0; i < allIconConstants.length; i++)
		{
			if ((msgType & allIconConstants [i]) != 0)
			{
				// We have an icon
				int iconValue = allIconConstants [i];
				String iconName;

				switch (iconValue)
				{
				case ICON_INFO:
					iconName = "information";
					break;
				case ICON_WARNING:
					iconName = "warning";
					break;
				case ICON_ERROR:
					iconName = "error";
					break;
				case ICON_FATAL:
					iconName = "fatal";
					break;
				case ICON_QUESTION:
					iconName = "question";
					break;
				default:
					iconName = "information";
				}

				// Access the resource file
				String resourceName = "icons." + iconName;

				MultiIcon bufferIcon = null;
				if (resourceCollection != null)
				{
					bufferIcon = (MultiIcon) resourceCollection.getOptionalObject((resourcePrefix != null ? resourcePrefix : "") + resourceName);
				}
				if (bufferIcon == null)
				{
					bufferIcon = (MultiIcon) defaultResourceCollection.getOptionalObject(resourceName);
				}

				if (bufferIcon == null)
				{
					// We use the standard icon
					String name = iconName;
					if (name.equals("fatal"))
					{
						name = "error";
					}

					icon = UIManager.getIcon("OptionPane." + name + "Icon");
				}
				else
				{
					icon = bufferIcon.getIcon(FlexibleSize.LARGE);
				}

				// Since we have an icon we stop looking for another one
				break;
			}
		}

		// Check for a default button
		for (int i = 0; i < allDefaultButtonConstants.length; i++)
		{
			if ((msgType & allDefaultButtonConstants [i]) != 0)
			{
				// A defaultbutton was set intentionally
				int defaultButtonValue = allDefaultButtonConstants [i];

				switch (defaultButtonValue)
				{
				case DEFAULT_OK:
					defaultButtonValue = TYPE_OK;
					break;
				case DEFAULT_CANCEL:
					defaultButtonValue = TYPE_CANCEL;
					break;
				case DEFAULT_YES:
					defaultButtonValue = TYPE_YES;
					break;
				case DEFAULT_NO:
					defaultButtonValue = TYPE_NO;
					break;
				case DEFAULT_ABORT:
					defaultButtonValue = TYPE_ABORT;
					break;
				case DEFAULT_RETRY:
					defaultButtonValue = TYPE_RETRY;
					break;
				case DEFAULT_IGNORE:
					defaultButtonValue = TYPE_IGNORE;
					break;
				}

				defaultButton = (JButton) buttonByType.get(Integer.valueOf(defaultButtonValue));

				// Since we have a default button we stop looking for another one
				break;
			}
		}
	}

	/**
	 * ActionListener implementation - determines the return value of the message box.
	 * @nowarn
	 */
	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();

		Integer returnValue = (Integer) typeByButton.get(source);
		userChoice = returnValue.intValue();

		if (doNotShowAgainCheckBox != null && doNotShowAgainCheckBox.isSelected())
		{
			userChoice |= TYPE_DO_NOT_SHOW_AGAIN;
		}

		dispose();
	}

	private class ChoiceActionListener
		implements ActionListener
	{
		/** The user's choice to be set */
		private int choice;

		/**
		 * Constructor.
		 * @param choice The user's choice to be set
		 */
		ChoiceActionListener(int choice)
		{
			this.choice = choice;
		}

		/**
		 * The ActionListener implementation - determines the return value of
		 * the message box
		 */
		public void actionPerformed(ActionEvent e)
		{
			userChoice = choice;

			if (doNotShowAgainCheckBox != null && doNotShowAgainCheckBox.isSelected())
			{
				userChoice |= TYPE_DO_NOT_SHOW_AGAIN;
			}

			dispose();
		}

		/**
		 * Get the user's choice to be set.
		 * @nowarn
		 */
		public int getChoice()
		{
			return choice;
		}
	}

	/**
	 * Checks if the message type defines a button type.
	 * @nowarn
	 */
	private boolean hasButtonType()
	{
		for (int i = 0; i < allButtonConstants.length; ++i)
		{
			if ((msgType & allButtonConstants [i]) != 0)
				return true;
		}
		return false;
	}

	/**
	 * Checks if the message type defines an icon type.
	 * @nowarn
	 */
	private boolean hasIconType()
	{
		for (int i = 0; i < allIconConstants.length; ++i)
		{
			if ((msgType & allIconConstants [i]) != 0)
				return true;
		}
		return false;
	}

	/**
	 * Determines the parent frame of a component.
	 *
	 * @param c Component
	 * @return The parent frame or null if the component is not a direct or indirect child of a JFrame
	 */
	public static JFrame getParentFrame(Component c)
	{
		// Check the given owner component for a parent frame
		for (; c != null; c = c.getParent())
		{
			if (c instanceof JFrame)
			{
				return (JFrame) c;
			}
		}

		// Not found, try the default owner provider if given
		if (defaultOwnerProvider != null)
		{
			c = defaultOwnerProvider.getDefaultOwner();

			// Make sure we get a frame
			for (; c != null; c = c.getParent())
			{
				if (c instanceof JFrame)
				{
					return (JFrame) c;
				}
			}
		}

		return null;
	}

	//////////////////////////////////////////////////
	// @@ Interface definitions
	//////////////////////////////////////////////////

	public interface DefaultOwnerProvider
	{
		/**
		 * Gets a default owner component for a message box.
		 *
		 * @return The owner component or null<br>
		 * The component will be used to determine a parent JFrame
		 * (which should be a direct or indirect parent of the component)
		 */
		public Component getDefaultOwner();
	}
}
