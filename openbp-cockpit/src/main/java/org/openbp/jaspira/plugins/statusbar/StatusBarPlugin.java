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
package org.openbp.jaspira.plugins.statusbar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;

import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.swing.SwingUtil;

/**
 * Statusbar of a Jaspira page.
 *
 * @author Jens Ferchland
 */
public class StatusBarPlugin extends AbstractVisiblePlugin
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Status bar minimum size - the height is nessesary */
	private static final Dimension MIN_SIZE = new Dimension(20, 22);

	/** Minimum status bar text size */
	private static final Dimension MIN_TEXT_SIZE = new Dimension(200, 20);

	/** Minimum status bar text size */
	private static final Dimension MAX_TEXT_SIZE = new Dimension(20000, 20);

	/** Status bar progress size */
	private static final Dimension MIN_PROGRESS_SIZE = new Dimension(200, 20);

	/** Time after the status bar text wil be cleared in milliseconds */
	private static final int TEXT_TIME = 5000;

	/** Time after the busy state of the status bar text wil be cleared in milliseconds */
	private static final int BUSY_TIME = 20000;

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Text component */
	private JLabel text;

	/** Thread that clears the text of the statusbar */
	private Timer textTimer;

	/** Thread that clears the busy state of the statusbar */
	private Timer busyTimer;

	/** Window of the plugin */
	private Window window;

	/** Used to display progress */
	private JProgressBar progressBar;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	public String getResourceCollectionContainerName()
	{
		return "plugin.standard";
	}

	protected void initializeComponents()
	{
		JPanel cp = getContentPane();

		cp.setLayout(new BoxLayout(cp, BoxLayout.LINE_AXIS));
		cp.setBorder(new StatusBarBorder());

		// Show an empty statusbar if there is no text
		cp.setMinimumSize(MIN_SIZE);
		cp.setMaximumSize(MIN_SIZE);
		cp.setPreferredSize(MIN_SIZE);

		// Left padding
		cp.add(Box.createHorizontalStrut(3));

		// Status text
		text = new JLabel();
		text.setMinimumSize(MIN_TEXT_SIZE);
		text.setPreferredSize(MAX_TEXT_SIZE);
		cp.add(text);

		// Center padding
		cp.add(Box.createHorizontalStrut(10));

		// contentPane.add (Box.createHorizontalGlue ());

		// Progress bar
		progressBar = new JProgressBar(0, 100);
		progressBar.setBorder(new EmptyBorder(1, 0, 0, 0));
		progressBar.setMinimumSize(MIN_PROGRESS_SIZE);
		progressBar.setMaximumSize(MIN_PROGRESS_SIZE);
		progressBar.setPreferredSize(MIN_PROGRESS_SIZE);
		cp.add(progressBar);

		// Right padding
		cp.add(Box.createHorizontalStrut(1));
	}

	/**
	 * Gets the window that owns the status bar.
	 *
	 * @return The window (usually a java.awt.Frame)
	 */
	public Window getWindow()
	{
		if (window == null)
		{
			window = SwingUtilities.getWindowAncestor(getPluginComponent());
		}

		return window;
	}

	/**
	 * Forces an immediate update of the status bar.
	 */
	protected void updateStatusBar()
	{
		// getContentPane ().repaint ();
		/*
		 getContentPane ().validate ();
		 if (SwingUtilities.isEventDispatchThread ())
		 {
		 text.repaint ();
		 }
		 else
		 {
		 try
		 {
		 SwingUtilities.invokeAndWait (new Runnable ()
		 {
		 public void run ()
		 {
		 text.repaint ();
		 }
		 });
		 }
		 catch (Exception e)
		 {
		 }
		 }
		 */
		// SwingUtil.processPendingEvents ();
	}

	/**
	 * Starts the clear timer.
	 * Clears the status bar text and the busy state after 5 seconds.
	 */
	protected void startTextTimer()
	{
		if (textTimer == null)
		{
			textTimer = new Timer(TEXT_TIME, new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (text != null)
					{
						text.setText(null);
					}
				}
			});
			textTimer.start();
		}
		else
		{
			if (textTimer.isRunning())
			{
				textTimer.restart();
			}
			else
			{
				textTimer.start();
			}
		}
	}

	/**
	 * Starts the clear timer.
	 * Resets the progress bar and the wait cursor after 20 seconds.
	 */
	protected void startBusyTimer()
	{
		if (busyTimer == null)
		{
			busyTimer = new Timer(BUSY_TIME, new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// Deactivate the progress bar
					progressBar.setIndeterminate(false);
					progressBar.setValue(0);

					// It is likely that a wait cursor has been displayed, so try to reset it
					SwingUtil.waitCursorOff(getPluginComponent());
				}
			});
			busyTimer.start();
		}
		else
		{
			if (busyTimer.isRunning())
			{
				busyTimer.restart();
			}
			else
			{
				busyTimer.start();
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Event module
	//////////////////////////////////////////////////

	/**
	 * Event module.
	 */
	public class Events extends EventModule
	{
		public String getName()
		{
			return "statusbar";
		}

		/**
		 * The module type determines the way the events of this module are handled.
		 * @return {@link EventModule#MODULE_PRE_GLOBAL}<br>
		 * Events of this module should always be handled on a global basis right away
		 */
		public int getModuleType()
		{
			return MODULE_PRE_GLOBAL;
		}

		/**
		 * Event handler: Updates the message text of the statusbar.
		 *
		 * @event statusbar.updatetext
		 * @param event Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode updatetext(StatusBarTextEvent event)
		{
			if (text != null && event != null)
			{
				text.setText(event.getText());
				updateStatusBar();
			}

			startTextTimer();

			return EVENT_HANDLED;
		}

		/**
		 * Event handler: Starts the status bar's busy animation.
		 *
		 * @event statusbar.busy
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode busy(JaspiraEvent je)
		{
			progressBar.setIndeterminate(true);
			updateStatusBar();

			startBusyTimer();

			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Stops the status bar's busy animation.
		 *
		 * @event statusbar.unbusy
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode unbusy(JaspiraEvent je)
		{
			progressBar.setIndeterminate(false);
			progressBar.setValue(0);
			updateStatusBar();

			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Shows the given progress in the progress indicator of the status bar.
		 *
		 * @event statusbar.step
		 * @eventobject Float Progress status (0 <= status <= 1f)
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode step(JaspiraEvent je)
		{
			progressBar.setIndeterminate(false);
			progressBar.setValue((int) (((Float) je.getObject()).floatValue() * 100f));
			updateStatusBar();

			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Manage status bar components.
		 * Adds, removes or updates status bar components
		 *
		 * @event statusbar.managecomponent
		 * @param event Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode managecomponent(StatusBarComponentEvent event)
		{
			JPanel cp = getContentPane();
			switch (event.getOperation())
			{
			case StatusBarComponentEvent.ADD:
				cp.add(event.getNewComponent());
				return EVENT_CONSUMED;

			case StatusBarComponentEvent.REMOVE:
				cp.remove(event.getOldComponent());
				return EVENT_CONSUMED;

			default:
				cp.remove(event.getOldComponent());
				cp.add(event.getNewComponent());
				return EVENT_CONSUMED;
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Status bar border class
	//////////////////////////////////////////////////

	public static class StatusBarBorder extends AbstractBorder
	{
		private static final Insets insets = new Insets(2, 2, 2, 2);

		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
		{
			g.setColor(Color.gray);
			g.drawRect(x + 1, y + 1, width - 2, height - 2);
		}

		public Insets getBorderInsets(Component c)
		{
			return insets;
		}
	}
}
