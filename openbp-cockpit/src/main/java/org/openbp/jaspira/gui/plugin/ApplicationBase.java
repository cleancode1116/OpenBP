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
package org.openbp.jaspira.gui.plugin;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.FocusManager;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.icon.FlexibleSize;
import org.openbp.common.icon.MultiIcon;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.event.InteractionEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.gui.clipboard.ClipboardPlugin;
import org.openbp.jaspira.option.OptionMgr;
import org.openbp.jaspira.plugin.AbstractPlugin;
import org.openbp.jaspira.plugin.ApplicationUtil;
import org.openbp.jaspira.plugin.ConfigMgr;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.jaspira.plugin.InteractionModule;
import org.openbp.jaspira.plugin.Plugin;
import org.openbp.jaspira.plugin.PluginMgr;
import org.openbp.jaspira.plugins.AboutBoxPlugin;
import org.openbp.jaspira.plugins.PluginMgrPlugin;
import org.openbp.jaspira.plugins.statusbar.StatusBarTextEvent;
import org.openbp.jaspira.util.StandardFlavors;
import org.openbp.swing.SwingUtil;
import org.openbp.swing.components.JMsgBox;

/**
 * The application base is the base class for any Jaspira application main class.
 * Derive any main application class from this one.
 * The constructor of this class will register the application base as a regular plugin.
 * In order to perform initialization routines and loader plugins, override the
 * {@link #preInstallApplication} and {@link #postInstallApplication} template methods.
 *
 * The application base also keeps track of the frames ({@link JaspiraPageContainer}) of the application.
 *
 * @author Heiko Erhardt
 */
public abstract class ApplicationBase extends AbstractPlugin
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** List of frames of the application (contains {@link JaspiraPageContainer} objects) */
	private List frames;

	/** Singleton instance */
	private static ApplicationBase singletonInstance;

	/** Is false until the initialization process has ended. Is used to delay GEUs. */
	private static boolean isInitialized;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 * Initializes the application.
	 * This will register the ApplicationBase as a regular plugin.
	 * This in turn will invoke the {@link #initializePlugin()} method, which may perform further
	 * plugin loading etc.
	 */
	public ApplicationBase()
	{
		singletonInstance = this;

		// Plugins and actions will make use of the configuration manager for condition evaluation,
		// so initialize it first
		ConfigMgr.getInstance().initialize();

		initializePlugin();

		PluginMgr.getInstance().registerPlugin(this);
	}

	/**
	 * Returns the singleton instance of the application base.
	 * Note that this method does not construct the application base.
	 * This is usually done by instantiating the application class derived from ApplicationBase
	 * and registering it with the {@link PluginMgr}
	 * The constructor, which will be invoked by the application loader,
	 * will set the singleton instance. If the construction has not been
	 * performed, this method will throw a RuntimeException.
	 * @nowarn
	 */
	public static ApplicationBase getInstance()
	{
		if (singletonInstance == null)
		{
			throw new RuntimeException("ApplicationBase.getInstance (): The application base has not been instantiated yet!");
		}
		return singletonInstance;
	}

	/**
	 * Checks if an instance of the application base has been set.
	 * This method serves the purpose to prevent {@link #getInstance} throwing an exception
	 * in cases where no application base object exists. It is e. g. used by the {@link OptionMgr}
	 * which may be used in command line applications as well.
	 *
	 * @return
	 *		true	{@link #getInstance} can be called safely.
	 *		false	No application object has been instantiated yet.
	 */
	public static boolean hasInstance()
	{
		return singletonInstance != null;
	}

	/**
	 * Checks the initialization status of the application.
	 * @return
	 *		true	If the application initialization is finished.<br>
	 *		false	If the application initialization is still in progress.
	 */
	public static final boolean isInitialized()
	{
		return isInitialized;
	}

	//////////////////////////////////////////////////
	// @@ Plugin initialization
	//////////////////////////////////////////////////

	/**
	 * Installs the plugin and all its modules.
	 * Should only be called by the plugin manager.
	 */
	public void installPlugin()
	{
		frames = new ArrayList();

		// Init the look and feel of this application
		installLookAndFeel();

		// Provide a default owner frame for message boxes
		JMsgBox.setDefaultOwnerProvider(new ActiveFrameProvider());

		// Let our focus manager do the focus issues
		FocusManager.setCurrentManager(PluginFocusMgr.getInstance());

		// Call the template method
		preInstallApplication();

		// Install the application as plugin
		super.installPlugin();

		// Create standard plugins
		PluginMgr.getInstance().createInstance(ClipboardPlugin.class, this);

		// Call the template method
		postInstallApplication();

		// Notifies components that init is complete
		fireEvent("global.init.completed");

		fireEvent(new StatusBarTextEvent(this, getPluginResourceCollection().getRequiredString("application.loaded")));
		isInitialized = true;

		// Sometimes, the title bar of the plugin windows are missing. Force a global rebuild to prevent this.
		fireEvent(new JaspiraEvent(this, VisiblePlugin.GER, null, JaspiraEvent.TYPE_FLOOD, Plugin.LEVEL_PAGE, JaspiraEvent.STACKABLE));
	}

	/**
	 * Shuts down the application.
	 * Disposes the frames and exits the program exits with exit status 0.
	 * Override this method in order to implement e. g. application exit save handling
	 * and call super.showdown at the end of the method.
	 */
	void shutdown()
	{
		// Close all frames
		int n = frames.size();
		for (int i = 0; i < n; ++i)
		{
			((JaspiraPageContainer) frames.get(i)).dispose();
		}

		// Shut down application without error
		System.exit(0);
	}

	//////////////////////////////////////////////////
	// @@ Template methods
	//////////////////////////////////////////////////

	/**
	 * Template method that is called before the actual installation of the application base plugin is performed.
	 */
	protected void preInstallApplication()
	{
	}

	/**
	 * Template method that is called after the actual installation of the application base plugin is performed.
	 */
	protected void postInstallApplication()
	{
	}

	/**
	 * Installs the Look and feel of this application.
	 * If you want to use a different LookAndFeel or another behavior, override this method.
	 * The default LookAndFeel is the skynamcis PLAF.
	 */
	protected void installLookAndFeel()
	{
		try
		{
			UIManager.setLookAndFeel("org.openbp.swing.plaf.sky.SkyLookAndFeel");
		}
		catch (ClassNotFoundException e)
		{
			ExceptionUtil.printTrace(e);
		}
		catch (InstantiationException e)
		{
			ExceptionUtil.printTrace(e);
		}
		catch (IllegalAccessException e)
		{
			ExceptionUtil.printTrace(e);
		}
		catch (UnsupportedLookAndFeelException e)
		{
			ExceptionUtil.printTrace(e);
		}
	}

	/**
	 * Returns the class of the about box plugin.
	 * Override this to return your own AboutBoxPlugin plugin.
	 * @nowarn
	 */
	protected Class getAboutBoxClass()
	{
		return AboutBoxPlugin.class;
	}

	//////////////////////////////////////////////////
	// @@ Frame and page management
	//////////////////////////////////////////////////

	/**
	 * Adds the given page as new frame to the application.
	 * Creates a new frame and adds the page to it.
	 *
	 * @param page Page to add
	 * @param d Dimension of the new frame
	 * @param state Initial window state (regular, maximized etc.)
	 * @param gc Graphics configuration to use for the new frame
	 * @return The new frame
	 */
	public JaspiraPageContainer addFrame(JaspiraPage page, Dimension d, int state, GraphicsConfiguration gc)
	{
		// Create a new frame
		final JaspiraPageContainer frame = new JaspiraPageContainer(getTitle(), gc);

		// Trigger global.frame events when activating, deactivating or closing the frame
		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				fireEvent(new JaspiraEvent((JaspiraPageContainer) e.getWindow(), "global.frame.close"));
			}

			public void windowActivated(WindowEvent e)
			{
				fireEvent(new JaspiraEvent((JaspiraPageContainer) e.getWindow(), "global.frame.activated"));
			}

			public void windowDeactivated(WindowEvent e)
			{
				fireEvent(new JaspiraEvent((JaspiraPageContainer) e.getWindow(), "global.frame.deactivated"));
			}
		});

		// Copy the icon to the frame
		MultiIcon multiIcon = getIcon();
		Icon icon = multiIcon.getIcon(FlexibleSize.LARGE);
		BufferedImage buf = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = buf.getGraphics();
		icon.paintIcon(frame, g, 0, 0);
		frame.setIconImage(buf);

		// Add the frame
		frames.add(frame);

		// Add the page to the frame
		frame.addPage(page);

		// Show frame in given window state and size
		frame.pack();
		frame.setSize(d);
		frame.setExtendedState(state);
		SwingUtil.show(frame);

		return frame;
	}

	/**
	 * Adds the given page as new frame to the application using the current graphics configuration.
	 * Convenience method.
	 *
	 * @param page Page to add
	 * @param d Dimension of the new frame
	 * @param state Initial window state (regular, maximized etc.)
	 * @return The new frame
	 */
	public JaspiraPageContainer addFrame(JaspiraPage page, Dimension d, int state)
	{
		return addFrame(page, d, state, null);
	}

	/**
	 * Adds the given page as new maximized frame to the application using the current graphics configuration.
	 * Convenience method.
	 *
	 * @param page Page to add
	 * @return The new frame
	 */
	public JaspiraPageContainer addFrame(JaspiraPage page)
	{
		// To prevent a redraw set the frame size to the screen size and use the frame normal.
		return addFrame(page, Toolkit.getDefaultToolkit().getScreenSize(), JFrame.NORMAL);
	}

	/**
	 * Returns the numbers of frames of this application.
	 * @nowarn
	 */
	public int getFrameCount()
	{
		return frames.size();
	}

	/**
	 * Adds a new page to the active frame.
	 * If there is no active frame, a new frame will be created.
	 *
	 * @param page Page to add
	 * @return The argument
	 */
	public JaspiraPage addPage(JaspiraPage page)
	{
		if (frames.isEmpty())
		{
			addFrame(page);
		}
		else
		{
			getActiveFrame().addPage(page);
		}

		return page;
	}

	/**
	 * Shows the given page.
	 * The method will activate the page and bring the frame it contains to the front.
	 *
	 * @param page Page to show
	 */
	public void showPage(JaspiraPage page)
	{
		int n = frames.size();
		for (int i = 0; i < n; ++i)
		{
			JaspiraPageContainer frame = (JaspiraPageContainer) frames.get(i);

			if (frame.containsPage(page))
			{
				frame.setPageActive(page);
				frame.toFront();
			}
		}
	}

	/**
	 * Checks if there is already a frame for the given graphics configuration.
	 * @nowarn
	 */
	public boolean hasFrameForGraphicsConfiguration(GraphicsConfiguration gc)
	{
		int n = frames.size();
		for (int i = 0; i < n; ++i)
		{
			JaspiraPageContainer frame = (JaspiraPageContainer) frames.get(i);

			if (gc == frame.getGraphicsConfiguration())
				return true;
		}
		return false;
	}

	/**
	 * Returns the active frame of the application.
	 * @return The active frame or the first frame if no frame is currently active or null
	 * if there are no frames yet
	 */
	public JaspiraPageContainer getActiveFrame()
	{
		Window win = FocusManager.getCurrentManager().getActiveWindow();
		if (win instanceof JaspiraPageContainer)
		{
			return (JaspiraPageContainer) win;
		}

		if (frames.size() > 0)
			return (JaspiraPageContainer) frames.get(0);

		return null;
	}

	/**
	 * Gets the active plugin or null.
	 * @return The active plugin of the currently focused frame or null
	 * if no frame is focused.
	 */
	public VisiblePlugin getActivePlugin()
	{
		int n = frames.size();
		for (int i = 0; i < n; ++i)
		{
			JaspiraPageContainer frame = (JaspiraPageContainer) frames.get(i);

			if (frame.isFocused())
			{
				return frame.getActivePlugin();
			}
		}
		return null;
	}

	/**
	 * Updates the menus of the frames of the application.
	 */
	public void buildMenus()
	{
		int n = frames.size();
		for (int i = 0; i < n; ++i)
		{
			((JaspiraPageContainer) frames.get(i)).buildMenu();
		}
	}

	//////////////////////////////////////////////////
	// @@ Status message support
	//////////////////////////////////////////////////

	/**
	 * Shows a message in the status bar.
	 * Fires a {@link StatusBarTextEvent} that can be caught by the status bar plugin.
	 *
	 * @param text Text to display
	 */
	public void showStatusText(String text)
	{
		fireEvent(new StatusBarTextEvent(this, text));
	}

	//////////////////////////////////////////////////
	// @@ Plugin overrides
	//////////////////////////////////////////////////

	/**
	 * Returns the page level parent plugin of this plugin.
	 * @return Always null; a page container does not have a page parent
	 */
	public JaspiraPage getPage()
	{
		return null;
	}

	//////////////////////////////////////////////////
	// @@ close event module
	//////////////////////////////////////////////////

	/**
	 * Event module.
	 * Handles application and frame close events.
	 */
	public class CloseEvents extends EventModule
	{
		public String getName()
		{
			return "global";
		}

		/**
		 * Event handler: Exits the application.
		 *
		 * @event global.application.exit
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode global_application_exit(JaspiraEvent je)
		{
			if (requestClose())
			{
				shutdown();
			}

			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: A Jaspira page container has been closed.
		 *
		 * @event global.frame.close
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode global_frame_close(JaspiraEvent je)
		{
			if (frames.size() == 1)
			{
				// Don't fire an event, take the short cut
				global_application_exit(new JaspiraEvent(ApplicationBase.this, "global.application.exit"));
			}
			else
			{
				// Get the frame to close
				JaspiraPageContainer source = (JaspiraPageContainer) je.getSource();

				// Remove it from the frame table
				frames.remove(source);

				// Get a free frame
				JaspiraPageContainer target = (JaspiraPageContainer) frames.get(0);

				// Put all pages of the old frame in the remaining frame
				for (Iterator it = source.getPages().iterator(); it.hasNext();)
				{
					target.addPage((JaspiraPage) it.next());
				}

				// Close the old frame
				source.dispose();
			}

			return EVENT_CONSUMED;
		}
	}

	//////////////////////////////////////////////////
	// @@ Global menu interaction module
	//////////////////////////////////////////////////

	/**
	 * Interaction module that handles activations of the application-related menu items.
	 */
	public class MenuInteractionEvents extends InteractionModule
	{
		/**
		 * Gets the module priority.
		 * We are low priority.
		 *
		 * @return The priority. 0 is lowest, 100 is highest.
		 */
		public int getPriority()
		{
			return 99;
		}

		/**
		 * Event handler: Exits the application.
		 *
		 * @event global.interaction.exit
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode exit(JaspiraActionEvent je)
		{
			fireEvent("global.application.exit");

			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Opens the plugin manager.
		 *
		 * @event global.interaction.pluginmanageropen
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode pluginmanageropen(JaspiraActionEvent je)
		{
			// Create the plugin manager plugin
			PluginMgrPlugin plugin = new PluginMgrPlugin();
			plugin.initializePlugin();
			plugin.installPlugin();

			// The dialog will uninstall the plugin again when closed
			PluginDialog dialog = new PluginDialog(ApplicationUtil.getActiveWindow(), plugin, null, true);
			SwingUtil.show(dialog);

			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Opens the about box.
		 *
		 * @event global.interaction.openaboutbox
		 * @param ie Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode openaboutbox(JaspiraActionEvent ie)
		{
			VisiblePlugin plugin = PluginMgr.getInstance().createVisibleInstance(getAboutBoxClass(), ApplicationBase.this);

			// The dialog will uninstall the plugin again when closed
			PluginDialog dialog = new PluginDialog(ApplicationUtil.getActiveWindow(), plugin, null, true);
			SwingUtil.show(dialog);

			return EVENT_CONSUMED;
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Page interaction module
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Interaction module that creates the popup menu for the Jaspira page buttons on the left of the main window.
	 */
	public class PageInteractionEvents extends InteractionModule
	{
		/**
		 * Standard event handler that is called when a popup menu is to be shown.
		 * Adds the popup menu entries for Jaspira page buttons.
		 *
		 * @event global.interaction.popup
		 * @param ie Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode popup(final InteractionEvent ie)
		{
			if (!ie.isDataFlavorSupported(StandardFlavors.JASPIRA_PAGE))
			{
				return EVENT_IGNORED;
			}

			JaspiraAction group = new JaspiraAction("popup.page", null, null, null, null, 1, JaspiraAction.TYPE_GROUP);

			// 'Open page in new frame' popup menu
			group.addMenuChild(new JaspiraAction(ApplicationBase.this, "frame.openinframe")
			{
				public void actionPerformed(ActionEvent e)
				{
					JaspiraPage page = (JaspiraPage) ie.getSafeTransferData(StandardFlavors.JASPIRA_PAGE);
					if (page != null)
					{
						JaspiraPageContainer frame = (JaspiraPageContainer) page.getWindow();
						frame.openInNewFrame(page);
					}
				}
			});

			// TODO Feature 6: Commented out, right now we simply disallow closing of ALL pages.
			//			// 'Close page' popup menu
			//			group.addMenuChild (new JaspiraAction (ApplicationBase.this, "frame.closepage")
			//				{
			//					public void actionPerformed (ActionEvent e)
			//					{
			//						JaspiraPage page = (JaspiraPage) ie.getSafeTransferData (StandardFlavors.JASPIRA_PAGE);
			//						if (page != null)
			//						{
			//							if (page.requestClose ())
			//							{
			//								JaspiraPageContainer frame = (JaspiraPageContainer) page.getWindow ();
			//								frame.removePage (page);
			//							}
			//						}
			//					}
			//				});

			ie.add(group);

			return EVENT_HANDLED;
		}
	}

	//////////////////////////////////////////////////
	// @@ Default message box owner provider
	//////////////////////////////////////////////////

	private static class ActiveFrameProvider
		implements JMsgBox.DefaultOwnerProvider
	{
		/**
		 * Gets a default owner component for a message box.
		 *
		 * @return The owner component or null<br>
		 * The component will be used to determine a parent JFrame
		 * (which should be a direct or indirect parent of the component)
		 */
		public Component getDefaultOwner()
		{
			return ApplicationBase.getInstance().getActiveFrame();
		}
	}
}
