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
package org.openbp.jaspira.gui.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.openbp.common.ExceptionUtil;
import org.openbp.swing.SwingUtil;
import org.openbp.swing.SwingWorker;
import org.openbp.swing.components.JConsole;
import org.openbp.swing.components.JMsgBox;
import org.openbp.swing.components.wizard.Wizard;
import org.openbp.swing.components.wizard.WizardEvent;
import org.openbp.swing.plaf.sky.SimpleBorder;

/**
 * The wizard result page is used to give a feedback to the user while performing a lengthy operation
 * as result of a wizard dialog.
 * This page should be added to the wizard by calling Wizard.addPage (String name, JaspiraWizardResultPage page)
 * and Wizard.setResultPageName (String name).
 * It will be invoked then after the user presses the 'Finish' button on the last wizard page.
 *
 * By default, the page provides a progress bar, a text area (that displays a progress explanation) and
 * a console window that may display processing output. If you don't ant one of these components to appear
 * in the ui, simply make it invisible in the {@link #start} method.
 *
 * You must override the {@link #preProcess}, {@link #process} and {@link #postProcess} methods to add your functionality to the page.
 *
 * @author Heiko Erhardt
 */
public abstract class JaspiraWizardResultPage extends JaspiraWizardPage
	implements Runnable
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Panel for progress bar in the north region of the content pane */
	protected JPanel progressPanel;

	/** Panel for progress text output in the south region of the content pane */
	protected JPanel textPanel;

	/** Progress bar */
	private JProgressBar progressBar;

	/** Message output window */
	private JTextArea textArea;

	/** Message output window */
	private JConsole console;

	/** Action to perform on the event queue thread */
	private int threadAction = ACTION_NONE;

	/** Progress count */
	private int progressCount;

	/** Progress text */
	private String progressText;

	/** Height of the text area */
	private int textHeight;

	/** Old value of the canMoveForward flag */
	private boolean oldMoveForward;

	/** Old value of the canMoveBackward flag */
	private boolean oldMoveBackward;

	/** Old value of the canFinish flag */
	private boolean oldFinish;

	/** Old value of the canCancel flag */
	private boolean oldCancel;

	/** No action */
	private static final int ACTION_NONE = 0;

	/** User interface update action */
	private static final int ACTION_UPDATE_UI = 1;

	/** End process action */
	private static final int ACTION_END = 2;

	/** End process action */
	private static final int ACTION_ERROR = 3;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param wizard Wizard that owns the page
	 */
	public JaspiraWizardResultPage(Wizard wizard)
	{
		super(wizard);

		// Construct the UI
		JPanel cp = getContentPanel();

		// Create a self-running progress bar
		progressBar = new JProgressBar();
		progressBar.setPreferredSize(new Dimension(10000, 30));

		progressPanel = new JPanel(new BorderLayout());
		progressPanel.setBorder(new EmptyBorder(5, 2, 5, 5));
		progressPanel.add(progressBar, BorderLayout.NORTH);
		cp.add(progressPanel, BorderLayout.NORTH);

		// Create a text area for progress text output and add it to the page
		// Make the text area stay as small as possible
		textArea = new JTextArea();
		textArea.setFont(getFont());
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setOpaque(false);

		textPanel = new JPanel(new BorderLayout());
		textPanel.setBorder(new EmptyBorder(5, 2, 5, 5));
		textPanel.add(textArea, BorderLayout.CENTER);
		cp.add(textPanel, BorderLayout.SOUTH);

		// Create a text area for processing output and add it to the page
		console = new JConsole();
		console.setBorder(new CompoundBorder(new EmptyBorder(5, 2, 5, 5), SimpleBorder.getStandardBorder()));
		cp.add(console, BorderLayout.CENTER);

		canFinish = false;
	}

	/**
	 * Starts the processing.
	 * The methods first saves the status of the wizard controls and disables them all.
	 * After calling the {@link #preProcess} method, it will start a worker thread that calls
	 * the {@link #process} method. After the thread has terminated, the {@link #postProcess} method
	 * will be invoked.
	 */
	public void start()
	{
		// Lock all navigation buttons for now
		oldMoveForward = canMoveForward;
		oldMoveBackward = canMoveBackward;
		oldFinish = canFinish;
		oldCancel = canCancel;
		canMoveForward = false;
		canMoveBackward = false;
		canFinish = false;
		canCancel = false;
		updateNavigator();

		console.flush();

		try
		{
			preProcess();

			// Display the wait cursor
			SwingUtil.waitCursorOn((JComponent) getWizard());

			// Start the worker thread
			Worker worker = new Worker();
			worker.start();
		}
		catch (Exception e)
		{
			processException(e);
			try
			{
				postProcess(false);
			}
			catch (Exception e2)
			{
				processException(e2);
			}
		}
	}

	/**
	 * Handles a wizard event caused by this wizard page.
	 * If the page is being shown, the {@link #start} method will be invoked automatically.
	 *
	 * @param event Event to handle
	 */
	public void handleWizardEvent(WizardEvent event)
	{
		if (event.eventType == WizardEvent.SHOW)
		{
			// On page display, start the generation process
			start();
		}
	}

	//////////////////////////////////////////////////
	// @@ Overridables
	//////////////////////////////////////////////////

	/**
	 * Prepares for processing.
	 * This method is called before the {@link #process} method is invoked.
	 * You may initialize the result page user interface here.
	 */
	protected abstract void preProcess()
		throws Exception;

	/**
	 * Performs the processing.
	 * This method is called from inside the Swing worker thread to perform the processing.
	 * In order to update the user interface, use the {@link #updatePage} method.
	 */
	protected abstract void process()
		throws Exception;

	/**
	 * Finishes the processing.
	 * This method is called after the {@link #process} method has terminated.
	 * You may reset the result page user interface here.
	 * @param success Parameter indicating success or failure of the process
	 */
	protected abstract void postProcess(boolean success)
		throws Exception;

	/**
	 * Processes an exception that was issued during pre processing, processing or post processing.
	 * Default behaviour is to show a popup dialog that displays the exception message.
	 *
	 * @param t Exception to handle
	 */
	protected void processException(Throwable t)
	{
		String msg = ExceptionUtil.getNestedMessage(t);
		if (msg != null)
		{
			System.err.println(msg);
			JMsgBox.show(null, msg, JMsgBox.TYPE_OKLATER | JMsgBox.ICON_ERROR);
		}
	}

	//////////////////////////////////////////////////
	// @@ Runnable implementation
	//////////////////////////////////////////////////

	/**
	 * Will be invoked after the generation process has finished.
	 */
	public void run()
	{
		try
		{
			if (threadAction == ACTION_UPDATE_UI)
			{
				internalUpdatePage();
			}
			else if (threadAction == ACTION_END)
			{
				threadAction = ACTION_NONE;

				// Reset the wait cursor
				SwingUtil.waitCursorOff((JComponent) getWizard());

				// Unlock the navigation buttons
				canMoveForward = oldMoveForward;
				canMoveBackward = oldMoveBackward;
				canFinish = oldFinish;
				canCancel = oldCancel;
				updateNavigator();

				postProcess(true);
			}
			else if (threadAction == ACTION_ERROR)
			{
				threadAction = ACTION_NONE;

				// Reset the wait cursor
				SwingUtil.waitCursorOff((JComponent) getWizard());

				// Unlock the navigation buttons
				canMoveForward = oldMoveForward;
				canMoveBackward = oldMoveBackward;
				canFinish = oldFinish;
				canCancel = oldCancel;
				updateNavigator();

				postProcess(false);
			}
		}
		catch (Exception e)
		{
			processException(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ Methods to be used from inside the process method
	//////////////////////////////////////////////////

	/**
	 * Gets the progress count.
	 * @return The current progress count (usually a value between 0 and 100)
	 * or -1 to prevent progress display (e. g. for indeterminate progress bars).
	 */
	public int getProgressCount()
	{
		return progressCount;
	}

	/**
	 * Sets the progress count.
	 * Make sure to call {@link #updatePage} to display the change.
	 * @param progressCount The current progress count (usually a value between 0 and 100)
	 * or -1 to prevent progress display (e. g. for indeterminate progress bars).
	 */
	public void setProgressCount(int progressCount)
	{
		this.progressCount = progressCount;
	}

	/**
	 * Gets the progress text.
	 * @nowarn
	 */
	public String getProgressText()
	{
		return progressText;
	}

	/**
	 * Sets the progress text.
	 * Make sure to call {@link #updatePage} to display the change.
	 * @nowarn
	 */
	public void setProgressText(String progressText)
	{
		this.progressText = progressText;
	}

	/**
	 * Updates the page.
	 * Call this method from inside the {@link #process} method to update the ui of the page.
	 *
	 * @param wait
	 *		true	Returns after the ui has been updated
	 *		false	Returns immediately
	 */
	protected void updatePage(boolean wait)
	{
		if (SwingUtilities.isEventDispatchThread())
		{
			// Directly update the page
			internalUpdatePage();
		}
		else
		{
			// Forward to event dispatch thread
			threadAction = ACTION_UPDATE_UI;

			// Call the run method
			if (wait)
			{
				try
				{
					SwingUtilities.invokeAndWait(JaspiraWizardResultPage.this);
				}
				catch (Exception e)
				{
					ExceptionUtil.printTrace(e);
				}
			}
			else
			{
				SwingUtilities.invokeLater(JaspiraWizardResultPage.this);
			}
		}
	}

	/**
	 * Updates the page.
	 * To be called from the event dispatch thread only - for internal use.
	 * Use the {@link #updatePage} method instead.
	 */
	protected void internalUpdatePage()
	{
		if (progressCount >= 0)
		{
			progressBar.setValue(progressCount);
		}

		if (progressText != null)
		{
			textArea.setText(progressText);
			textArea.setVisible(true);
			getContentPanel().validate();
		}
		else
		{
			if (textHeight == 0)
			{
				// Hide the text area if it does not have a constant size assigned
				textArea.setVisible(false);
				getContentPanel().validate();
			}
			textArea.setText(progressText);
		}

		updateNavigator();
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the panel for progress bar in the north region of the content pane.
	 * @nowarn
	 */
	public JPanel getProgressPanel()
	{
		return progressPanel;
	}

	/**
	 * Gets the panel for progress text output in the south region of the content pane.
	 * @nowarn
	 */
	public JPanel getTextPanel()
	{
		return textPanel;
	}

	/**
	 * Gets the progress bar.
	 * @nowarn
	 */
	public JProgressBar getProgressBar()
	{
		return progressBar;
	}

	/**
	 * Gets the message output window.
	 * @nowarn
	 */
	public JTextArea getTextArea()
	{
		return textArea;
	}

	/**
	 * Gets the message output window.
	 * @nowarn
	 */
	public JConsole getConsole()
	{
		return console;
	}

	/**
	 * Sets the height of the text area.
	 * @nowarn
	 */
	public void setTextHeight(int textHeight)
	{
		this.textHeight = textHeight;
		Dimension dim = null;
		if (textHeight != 0)
		{
			dim = new Dimension(10000, textHeight);
		}
		textArea.setMinimumSize(dim);
		textArea.setMaximumSize(dim);
		textArea.setPreferredSize(dim);
	}

	//////////////////////////////////////////////////
	// @@ Swing worker class
	//////////////////////////////////////////////////

	/**
	 * Worker class that performs the generation process.
	 *
	 * We need to put this into a worker thread to make the progress bar work and view the output
	 * messages in the output window.
	 */
	private class Worker extends SwingWorker
	{
		/**
		 * Constructor.
		 */
		Worker()
		{
		}

		/**
		 * Performs the generation.
		 */
		public Object construct()
		{
			// Perform the processing
			try
			{
				process();
				threadAction = ACTION_END;
			}
			catch (Exception e)
			{
				ExceptionUtil.printTrace(e);
				threadAction = ACTION_ERROR;
			}
			finally
			{
				// Call the run method to invoke the finalization
				SwingUtilities.invokeLater(JaspiraWizardResultPage.this);
			}

			// Don't need a return value, return just something
			return Boolean.TRUE;
		}
	}
}
