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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.openbp.common.listener.AWTListenerSupport;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraToolbar;
import org.openbp.jaspira.action.keys.KeySequence;
import org.openbp.jaspira.event.InteractionEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.event.VetoableEvent;
import org.openbp.jaspira.gui.interaction.InteractionClient;
import org.openbp.jaspira.plugin.AbstractPlugin;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.jaspira.plugin.Plugin;
import org.openbp.jaspira.plugin.PluginMgr;

/**
 * Basic implementation of a visible plugin.
 *
 * @author Stephan Moritz
 */
public abstract class AbstractVisiblePlugin extends AbstractPlugin
	implements VisiblePlugin, HierarchyListener
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** Panel that holds the title bar of the plugin and the content panel */
	private final PluginPanel pluginPanel;

	/** Content pane to be used by sub classes to place the gui in */
	private final ContentPanel contentPanel;

	/**
	 * Holder of this plugin.
	 * This is usually the plugin panel, but can also be a {@link PluginDialog} or a {@link PluginFrame}
	 */
	private PluginHolder holder;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public AbstractVisiblePlugin()
	{
		super();

		contentPanel = new ContentPanel(this);

		// Create a plugin panel for this plugin that will serve as plugin holder by default
		pluginPanel = new PluginPanel(this);
		setPluginHolder(pluginPanel);
	}

	/**
	 * Loads the plugin resources.
	 */
	protected void initializeResources()
	{
		super.initializeResources();

		// Add an action for the key sequences that activate the plugin
		String keys = getPluginResourceCollection().getOptionalString(PROPERTY_SEQUENCE);
		if (keys != null)
		{
			// Key sequences used to activate the plugin
			StringTokenizer sto = new StringTokenizer(keys, JaspiraAction.KEY_SEQUENCE_DELIM);
			KeySequence[] sequences = new KeySequence[sto.countTokens()];

			for (int i = 0; sto.hasMoreTokens(); i++)
			{
				sequences[i] = new KeySequence(sto.nextToken());
			}

			// Simply perform a requestFocus operation when activated
			JaspiraAction action = new JaspiraAction(getName(), getTitle(), getDescription(), null, sequences, 0, JaspiraAction.TYPE_ACTION)
			{
				public void actionPerformed(ActionEvent ae)
				{
					focusPlugin();
				}
			};

			for (int j = 0; j < sequences.length; j++)
			{
				addActionKeySequence(sequences[j], action);
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Installation
	//////////////////////////////////////////////////

	/**
	 * Installs the plugin and all its modules.
	 * Should only be called by the plugin manager.
	 */
	public void installPlugin()
	{
		super.installPlugin();

		// Initialize the title of the plugin panel
		pluginPanel.initTitleBar();

		// Initialize various listeners of the plugin panel
		pluginPanel.addListeners();

		// Rebuild the holder ui
		postPluginContainerUpdate(true);

		if (ApplicationBase.isInitialized())
		{
			applicationReady();
		}
	}

	/**
	 * Installs the contents of the plugin.
	 */
	protected void installPluginContent()
	{
		super.installPluginContent();
		initializeComponents();
	}

	/**
	 * Called after the application has been initialized AND the plugin initialization is finished.
	 */
	public void applicationReady()
	{
		JComponent comp = getPluginComponent();
		if (comp != null)
		{
			comp.addHierarchyListener(this);
		}
	}

	/**
	 * Uninstalls the plugin and all of its modules.
	 * Never call this method directly, use {@link PluginMgr#removeInstance(Plugin)} instead.
	 */
	public final void uninstallPlugin()
	{
		// Reset the listeners of the plugin panel
		pluginPanel.removeListeners();

		super.uninstallPlugin();

		JComponent comp = getPluginComponent();
		if (comp != null)
		{
			comp.removeHierarchyListener(this);
		}
	}

	/**
	 * This template method is called after internal Components (i\.e\. title bar etc\.)
	 * have been initialized.
	 * It shoud be used to initialize plugin specific components.
	 */
	protected abstract void initializeComponents();

	//////////////////////////////////////////////////
	// @@ Event handling
	//////////////////////////////////////////////////

	/**
	 * Ensures that there are not multiple GEUs or GERs stacked.
	 *
	 * @param je The event to stack
	 */
	public void stackEvent(JaspiraEvent je)
	{
		String eventName = je.getEventName();

		if (eventName.equals(VisiblePlugin.GER))
		{
			// When stacking a GEU, remove all GERs
			removeStackedEvent(VisiblePlugin.GEU);
		}
		else if (eventName.equals(VisiblePlugin.GEU))
		{
			// Ignore any GEUs, if there is already a GER stacked
			if (containsStackedEvent(VisiblePlugin.GER))
				return;
		}

		// Stack the event
		super.stackEvent(je);
	}

	//////////////////////////////////////////////////
	// @@ Template methods
	//////////////////////////////////////////////////

	/**
	 * Called after the plugin has been displayed.
	 */
	public void pluginShown()
	{
		// Update the holder ui
		postPluginContainerUpdate(false);
	}

	/**
	 * Called after the plugin has been hidden.
	 */
	public void pluginHidden()
	{
	}

	/**
	 * This method is called before a page change takes place. It allows the
	 * plugin to veto the change by returning false. Override this to check for
	 * veto conditions.
	 *
	 * @param oldPage Current page
	 * @param newPage Page that should be made the new current page
	 * @return
	 *		true	If the plugin accepts the page change<br>
	 *		false	Otherwise
	 */
	protected boolean canPageChange(JaspiraPage oldPage, JaspiraPage newPage)
	{
		return true;
	}

	//////////////////////////////////////////////////
	// @@ Plugin content
	//////////////////////////////////////////////////

	/**
	 * Gets the panel that holds the title bar of the plugin and the content panel.
	 * @nowarn
	 */
	public PluginPanel getPluginPanel()
	{
		return pluginPanel;
	}

	/**
	 * Returns the plugin content pane.
	 * @return A panel that uses the border layout
	 */
	public JPanel getContentPane()
	{
		return contentPanel;
	}

	/**
	 * Gets the component this plugin contains.
	 *
	 * @return The content pane
	 */
	public JComponent getPluginComponent()
	{
		return contentPanel;
	}

	/**
	 * Returns the focus component of this plugin, i\.e\. the component
	 * that is to initially receive the focus.
	 * @return The return value defaults to the first component below the content pane of the plugin.
	 * If this component is a scroll pane, the method returns the view component of the pane.
	 * If there is no focusable component, the method returns null.
	 */
	public Component getPluginFocusComponent()
	{
		if (contentPanel.getComponentCount() == 0)
			return contentPanel;

		Component comp = contentPanel.getComponent(0);
		if (comp instanceof JScrollPane)
			return ((JScrollPane) comp).getViewport().getView();

		if (comp.isFocusable())
			return comp;

		return null;
	}

	/**
	 * Sets the holder of this plugin.
	 * @param holder The plugin holder
	 */
	public void setPluginHolder(PluginHolder holder)
	{
		this.holder = holder;
	}

	/**
	 * Returns the holder of this plugin.
	 * @return The plugin holder
	 */
	public PluginHolder getPluginHolder()
	{
		return holder;
	}

	/**
	 * Returns the page level parent plugin of this plugin.
	 * @return The page that holds this plugin or null
	 */
	public JaspiraPage getPage()
	{
		for (Plugin p = this; p != null; p = p.getParentPlugin())
		{
			if (p instanceof JaspiraPage)
				return (JaspiraPage) p;
		}
		return null;
	}

	/**
	 * Gets the type of the plugin toolbar.
	 * This will determine how the toolbar of the plugin will be constructed.
	 *
	 * @return {@link VisiblePlugin#TOOLBAR_NONE}/{@link VisiblePlugin#TOOLBAR_EVENTS}/{@link VisiblePlugin#TOOLBAR_DYNAMIC}<br>
	 * Default: TOOLBAR_EVENTS
	 */
	public int getToolbarType()
	{
		return TOOLBAR_EVENTS;
	}

	/**
	 * Returns true if the plugin should have a close button in its title bar.
	 * Override this method to customize the plugin.
	 *
	 * @nowarn
	 */
	public boolean hasCloseButton()
	{
		return false;
	}

	/**
	 * Returns the behavior of the plugin size.
	 * Override this method if you want to constrain the sizing of the plugin.
	 *
	 * @return {@link VisiblePlugin#SIZE_VARIABLE_NONE}/{@link VisiblePlugin#SIZE_VARIABLE_WIDTH}/{@link VisiblePlugin#SIZE_VARIABLE_HEIGHT}/{@link VisiblePlugin#SIZE_VARIABLE_BOTH}
	 */
	public int getSizeBehavior()
	{
		return SIZE_VARIABLE_BOTH;
	}

	/**
	 * Posts an update request of the plugin's holder.
	 *
	 * @param fullRebuild
	 *		true	Causes a full holder update including menu and toolbar rebuild<br>
	 *		false	Updates the holder title only
	 */
	public void postPluginContainerUpdate(boolean fullRebuild)
	{
		String eventName;

		if (fullRebuild)
		{
			eventName = VisiblePlugin.GER;
		}
		else
		{
			// Ignore any GEUs, if there is already a rebuild request
			if (containsStackedEvent(VisiblePlugin.GER))
				return;

			eventName = VisiblePlugin.GEU;
		}

		// Ignore if there is already an update request
		if (containsStackedEvent(eventName))
			return;

		JaspiraPage page = getPage();
		if (page == null)
			// No update until the plugin has been added to a Jaspira page
			return;

		Window frame = page.getWindow();
		if (frame == null)
			// No update until the Jaspira page has been added to a frame
			return;

		if (fullRebuild)
		{
			// When stacking a GER, remove all GEUs
			removeStackedEvent(VisiblePlugin.GEU);
		}

		// Stack the event;
		// We can safely use the super method here because we already made the checks that are also implemented in this.stackEvent
		super.stackEvent(new JaspiraEvent(this, eventName, null, JaspiraEvent.TYPE_DIRECT, Plugin.LEVEL_PLUGIN, JaspiraEvent.STACKABLE));
	}

	/**
	 * Forces the holder of this plugin to update its title bar.
	 *
	 * @param fullRebuild
	 *		true	Causes a full holder update including menu and toolbar rebuild<br>
	 *		false	Updates the holder title only
	 */
	public void updatePluginContainer(boolean fullRebuild)
	{
		if (holder != null)
		{
			holder.updateHolder(fullRebuild);
		}
	}

	/**
	 * Creates the toolbar of this plugin.
	 * The toolbar will be constructed according to the return value of the {@link #getToolbarType() method}.
	 * If you want to construct your own custom toolbar, override this method.
	 *
	 * @return The toolbar or null if the plugin does not have a toolbar
	 */
	public JaspiraToolbar createToolbar()
	{
		JaspiraToolbar toolbar = null;

		switch (getToolbarType())
		{
		case TOOLBAR_EVENTS:
			// Standard toolbar, add all event actions
			toolbar = new JaspiraToolbar();

			List actionNames = getEventActionNames();
			if (actionNames != null)
			{
				int n = actionNames.size();
				for (int i = 0; i < n; ++i)
				{
					String actionName = (String) actionNames.get(i);
					toolbar.add(getAction(actionName));
				}
			}
			break;

		case TOOLBAR_DYNAMIC:
			// Dynamic toolbar, broadcast interaction event.
			// The global.interaction.toolbar listeners of the receiving plugins may add entries to the toolbar.
			InteractionEvent iae = new InteractionEvent(this, InteractionEvent.TOOLBAR, null);
			fireEvent(iae);
			toolbar = iae.createToolbar();
		}

		return toolbar;
	}

	//////////////////////////////////////////////////
	// @@ HierarchyListener implementation
	//////////////////////////////////////////////////

	/**
	 * Called when a hierarchyChanged event is received, i\.e\. something in the hierarchy has changed.
	 * Calls the {@link #pluginShown} or {@link #pluginHidden} methods.
	 * @nowarn
	 */
	public void hierarchyChanged(HierarchyEvent e)
	{
		JComponent comp = getPluginComponent();
		if (comp != null)
		{
			if (comp.isShowing())
			{
				pluginShown();
			}
			else
			{
				pluginHidden();
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Focus support
	//////////////////////////////////////////////////

	/**
	 * Brings the plugin to the front, but does not request the focus.
	 * @param changePage
	 *		true	Shows the {@link JaspiraPage} this holder belongs to if it not the active page.<br>
	 *		false	Do not flip pages.
	 */
	public void showPlugin(boolean changePage)
	{
		if (holder != null && ! isPluginVisible())
		{
			// We show the plugin by showing its holder
			holder.showHolder(changePage);
		}
	}

	/**
	 * Checks if the plugin is currently visible.
	 * @nowarn
	 */
	public boolean isPluginVisible()
	{
		JComponent comp = getPluginComponent();
		if (comp == null)
			return false;
		return comp.isShowing();
	}

	/**
	 * Requests the focus for this plugin from the plugin manager and brings it to the front.
	 * The focus is being set to the focus component ({@link #getPluginFocusComponent}) of the plugin or
	 * to the plugin's component.
	 */
	public void focusPlugin()
	{
		// Bring the plugin to the front first
		showPlugin(true);

		PluginFocusMgr.getInstance().changeFocus(this);

		final Component comp = getPluginFocusComponent();
		if (comp != null)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					comp.requestFocus();
				}
			});
		}
	}

	/**
	 * Checks if the plugin is currently focused.
	 * @nowarn
	 */
	public boolean isPluginFocused()
	{
		return PluginFocusMgr.getInstance().getFocusedPlugin() == this;
	}

	/** Listener support object holding the listeners */
	private AWTListenerSupport listenerSupport;

	/**
	 * Fires a 'focus gained' message to all registered focus listeners.
	 */
	public void firePluginFocusGained()
	{
		if (listenerSupport != null && listenerSupport.containsListeners(FocusListener.class))
		{
			listenerSupport.fireFocusGained(new FocusEvent(getPluginComponent(), FocusEvent.FOCUS_GAINED));
		}
	}

	/**
	 * Fires a 'focus lost' message to all registered focus listeners.
	 */
	public void firePluginFocusLost()
	{
		if (listenerSupport != null && listenerSupport.containsListeners(FocusListener.class))
		{
			listenerSupport.fireFocusLost(new FocusEvent(getPluginComponent(), FocusEvent.FOCUS_LOST));
		}
	}

	/**
	 * Adds a focus listener to the listener list.
	 * The listener is registered for all properties as a WEAK listener, i. e. it may
	 * be garbage-collected if not referenced otherwise.<br>
	 * ATTENTION: Never add an automatic class (i. e new FocusListener () { ... }) or an inner
	 * class that is not referenced otherwise as a weak listener to the list. These objects
	 * will be cleared by the garbage collector during the next gc run!
	 *
	 * @param listener The listener to be added
	 */
	public synchronized void addPluginFocusListener(FocusListener listener)
	{
		if (listenerSupport == null)
		{
			listenerSupport = new AWTListenerSupport();
		}
		listenerSupport.addWeakListener(FocusListener.class, listener);
	}

	/**
	 * Removes a focus listener from the listener list.
	 *
	 * @param listener The listener to be removed
	 */
	public synchronized void removePluginFocusListener(FocusListener listener)
	{
		if (listenerSupport != null)
		{
			listenerSupport.removeListener(FocusListener.class, listener);
		}
	}

	//////////////////////////////////////////////////
	// @@ Dnd/clipboard support
	//////////////////////////////////////////////////

	public List getSubClients()
	{
		if (this instanceof InteractionClient)
			return Collections.singletonList(this);
		return null;
	}

	public boolean canDrag()
	{
		return true;
	}

	public boolean canCopy()
	{
		return false;
	}

	public boolean canCut()
	{
		return false;
	}

	public boolean canPaste(Transferable transferable)
	{
		return false;
	}

	public boolean canDelete()
	{
		return false;
	}

	public Transferable copy()
	{
		return null;
	}

	public Transferable cut()
	{
		return null;
	}

	public void paste(Transferable transferable)
	{
	}

	public void delete()
	{
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Standard event module
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Event module.
	 */
	public class StandardVisiblePluginEvents extends EventModule
	{
		public String getName()
		{
			return "global.enviroment";
		}

		/**
		 * Event handler: Called when a global environment update is being performed.
		 * This causes the plugin to rebuild its toolbar, if dynamic.
		 *
		 * @event global.enviroment.update
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode update(JaspiraEvent je)
		{
			JComponent comp = getPluginComponent();
			if (comp != null && comp.isShowing())
			{
				// Perform a partial holder ui including toolbar rebuild and title update of necessary<br>
				updatePluginContainer(false);

				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
		}

		/**
		 * Event handler: Called when a global environment rebuild is being performed.
		 * This causes the plugin to rebuild its toolbar, if dynamic.
		 *
		 * @event global.enviroment.rebuild
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode rebuild(JaspiraEvent je)
		{
			JComponent comp = getPluginComponent();
			if (comp != null && comp.isShowing())
			{
				// Perform a full holder ui including menu and toolbar rebuild<br>
				updatePluginContainer(true);

				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
		}

		/**
		 * Event handler: Called when a page is about to change.
		 * Invokes the {@link AbstractVisiblePlugin#canPageChange} method and vetoes the event if unsuccessful.
		 *
		 * @event global.page.askchange
		 * @param ve Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode global_page_askchange(VetoableEvent ve)
		{
			if (! canPageChange((JaspiraPage) ve.getOldObject(), (JaspiraPage) ve.getNewObject()))
			{
				ve.veto();

				return EVENT_CONSUMED;
			}

			return EVENT_HANDLED;
		}

		/**
		 * Event handler: Called when the application initialization is complete.
		 *
		 * @event global.init.completed
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode global_init_completed(JaspiraEvent je)
		{
			applicationReady();

			return EVENT_HANDLED;
		}

		/**
		 * Event handler: Adds another plugin to the container of this plugin.
		 * Consumes the event if successful.
		 *
		 * @event global.plugin.addtocontainer
		 * @eventparam The {@link AbstractVisiblePlugin} to add to the container that
		 * contains this plugin
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode global_plugin_addtocontainer(JaspiraEvent je)
		{
			if (holder == pluginPanel)
			{
				VisiblePlugin otherPlugin = (VisiblePlugin) je.getObject();

				// This plugin is held by a tabbed container, get it
				TabbedPluginContainer tpc = pluginPanel.getTabbedContainer();

				// Add the argument plugin to the save container, but don't bring it to the top
				tpc.addPlugin(otherPlugin, false);

				return EVENT_CONSUMED;
			}

			return EVENT_IGNORED;
		}
	}

	//////////////////////////////////////////////////
	// @@ Content panel class
	//////////////////////////////////////////////////

	/**
	 * Panel holding the actual content of the plugin.
	 */
	private static class ContentPanel extends JPanel
	{
		/** Plugin that owns the panel */
		private final VisiblePlugin plugin;

		/**
		 * Constructor
		 * @param plugin Plugin that owns the panel
		 */
		public ContentPanel(VisiblePlugin plugin)
		{
			super(new BorderLayout());

			this.plugin = plugin;
		}

		/**
		 * Returns the plugin.
		 * @nowarn
		 */
		public VisiblePlugin getPlugin()
		{
			return plugin;
		}

		/**
		 * @see java.awt.Component#isFocusable()
		 */
		public boolean isFocusable()
		{
			// The content panel is not directly focusable
			return false;
		}
	}

	/**
	 * Searches backwards in the component hierarchy of the given component
	 * and returns the plugin that is associated with a parent of the component.
	 * Note that this works only if the parent is an {@link AbstractVisiblePlugin}
	 *
	 * @param c We will start the search with the parent component of this component
	 * @return The plugin or null if no ancestor of the component is a plugin's content panel
	 */
	public static VisiblePlugin getPluginFromComponentHierarchy(Component c)
	{
		Container con = SwingUtilities.getAncestorOfClass(AbstractVisiblePlugin.ContentPanel.class, c);
		if (con != null)
		{
			AbstractVisiblePlugin.ContentPanel panel = (AbstractVisiblePlugin.ContentPanel) con;
			if (panel != null)
				return panel.getPlugin();
		}
		return null;
	}
}
