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
package org.openbp.cockpit.modeler;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

import org.openbp.cockpit.modeler.undo.ModelerUndoPlugin;
import org.openbp.cockpit.plugins.debugger.DebuggerPlugin;
import org.openbp.cockpit.plugins.debugger.InspectorPlugin;
import org.openbp.cockpit.plugins.debugger.StackTracePlugin;
import org.openbp.cockpit.plugins.infopanel.InfoPanelPlugin;
import org.openbp.cockpit.plugins.itembrowser.ItemBrowserPlugin;
import org.openbp.cockpit.plugins.miniview.MiniViewPlugin;
import org.openbp.cockpit.plugins.toolbox.StandardToolBoxPlugin;
import org.openbp.cockpit.plugins.variables.VariablesPlugin;
import org.openbp.common.CommonUtil;
import org.openbp.core.MimeTypes;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.guiclient.event.OpenEvent;
import org.openbp.guiclient.event.OpenEventInfo;
import org.openbp.guiclient.event.QualifierEvent;
import org.openbp.guiclient.model.ModelConnector;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.action.JaspiraToolbarCombo;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.event.JaspiraEventMgr;
import org.openbp.jaspira.event.RequestEvent;
import org.openbp.jaspira.gui.plugin.JaspiraPage;
import org.openbp.jaspira.gui.plugin.JaspiraPageContainer;
import org.openbp.jaspira.gui.plugin.PluginDivider;
import org.openbp.jaspira.gui.plugin.TabbedPluginContainer;
import org.openbp.jaspira.option.OptionMgr;
import org.openbp.jaspira.plugin.ApplicationUtil;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.jaspira.plugin.PluginMgr;
import org.openbp.jaspira.plugins.colorchooser.ColorChooserPlugin;
import org.openbp.jaspira.plugins.propertybrowser.PropertyBrowserPlugin;
import org.openbp.swing.components.JMsgBox;

/**
 * Jaspira page that hosts the Modeler.
 *
 * @author Jens Ferchland
 */
public class ModelerPage extends JaspiraPage
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Maximum history size */
	public static final int MAX_HISTORY_SIZE = 20;

	/** Tab component containing the active modeler views ({@link Modeler} objects) */
	private TabbedPluginContainer modelerContainer;

	/** Browser */
	// private InternalBrowserPlugin webBrowser;

	/** Browse history list contains URLs (strings) */
	private List historyList;

	/** Current position in history list */
	private int historyIndex;

	/** History debug flag */
	private boolean historyDebug;

	/** Browser forward action */
	private JaspiraAction forwardAction;

	/** Browser backward action */
	private JaspiraAction backwardAction;

	/** Description text of browser forward action */
	private String forwardActionText;

	/** Description text of browser backward action */
	private String backwardActionText;

	/** Performing history action */
	private boolean skipNextHistoryEvent;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new view using a given modeler.
	 */
	public ModelerPage()
	{
		super();
	}

	public String getResourceCollectionContainerName()
	{
		return "plugin.modeler";
	}

	//////////////////////////////////////////////////
	// @@ Layout
	//////////////////////////////////////////////////

	/**
	 * Performs the layout of the initial default plugins (toolboxes, property browser, browsers etc.)
	 * and their containers within this view.
	 */
	public void layoutDefaultContent()
	{
		historyList = new ArrayList();
		historyIndex = -1;

		PluginMgr pm = PluginMgr.getInstance();

		// Create the non-visible plugins
		pm.createInstance(DebuggerPlugin.class, this);
		pm.createInstance(ModelerUndoPlugin.class, this);

		JComponent leftArea = createLeftArea();
		JComponent rightArea = createRightArea();
		JComponent bottomArea = createBottomArea();

		PluginDivider verticalDivider = new PluginDivider(JSplitPane.HORIZONTAL_SPLIT);
		PluginDivider horizontalDivider = new PluginDivider(JSplitPane.VERTICAL_SPLIT);

		horizontalDivider.addClient(rightArea);
		horizontalDivider.addClient(bottomArea);
		horizontalDivider.setClientProportions(new double [] { 0.75d, 0.25d });

		verticalDivider.addClient(leftArea);
		verticalDivider.addClient(horizontalDivider);
		verticalDivider.setClientProportions(new double [] { 0.2d, 0.8d });

		setPluginDivider(verticalDivider);

		// This will also bring the browser to the front
		displayWelcomePage();

		// Load custom plugins of the model page
		PluginMgr.getInstance().loadCustomPlugins("openbp.cockpit.plugins.modelerpage");
	}

	private JComponent createLeftArea()
	{
		PluginMgr pm = PluginMgr.getInstance();

		// Create the toolbox container
		TabbedPluginContainer toolContainer = new TabbedPluginContainer();
		toolContainer.addPlugin(pm.createVisibleInstance(StandardToolBoxPlugin.class, this), true);

		// toolContainer.addPlugin (pm.createVisibleInstance (UserToolBoxPlugin.class, this), false);

		toolContainer.addPlugin(pm.createVisibleInstance(MiniViewPlugin.class, this), false);

		// Create the item browser container
		TabbedPluginContainer itemContainer = new TabbedPluginContainer();
		itemContainer.addPlugin(pm.createVisibleInstance(ItemBrowserPlugin.class, this));

		TabbedPluginContainer infoContainer = new TabbedPluginContainer();
		infoContainer.addPlugin(pm.createVisibleInstance(InfoPanelPlugin.class, this));

		// Hor. divider for toolbox and item browser
		PluginDivider divider = new PluginDivider(JSplitPane.VERTICAL_SPLIT);
		divider.addContainer(toolContainer);
		divider.addContainer(itemContainer);
		divider.addContainer(infoContainer);

		divider.setClientProportions(new double [] { 0.25d, 0.50d, 0.25d });

		return divider;
	}

	private JComponent createRightArea()
	{
		// Container for modeler views; Does always show tabs and doesn't remove itself if empty
		modelerContainer = new TabbedPluginContainer();
		modelerContainer.setAlwaysTabs(true);
		modelerContainer.setSolid(true);

		// Add the browser plugin to the modeler container
		// Currently, we do not support browser actions, so this has been commented out
		// modelerContainer.addPlugin(webBrowser = (InternalBrowserPlugin) PluginMgr.getInstance().createVisibleInstance(JDICBrowserPlugin.class, this), false);

		return modelerContainer;
	}

	private JComponent createBottomArea()
	{
		PluginMgr pm = PluginMgr.getInstance();

		// The content container consists of...

		// ...the property browser and the browser (left)
		TabbedPluginContainer leftContainer = new TabbedPluginContainer();
		leftContainer.addPlugin(pm.createVisibleInstance(PropertyBrowserPlugin.class, this), true);

		// ...and the process variables, color chooser, context inspector and the console (right)
		ColorChooserPlugin colorChooser = (ColorChooserPlugin) pm.createInstance(ColorChooserPlugin.class, this);
		colorChooser.setHelpText(getPluginResourceCollection().getOptionalString("chooserhelptext"));

		TabbedPluginContainer rightContainer = new TabbedPluginContainer();
		rightContainer.addPlugin(pm.createVisibleInstance(VariablesPlugin.class, this), true);
		rightContainer.addPlugin(colorChooser, false);
		rightContainer.addPlugin(pm.createVisibleInstance(InspectorPlugin.class, this), false);
		rightContainer.addPlugin(pm.createVisibleInstance(StackTracePlugin.class, this), false);

		// Divider for content area (modeler, toolbox, item browser)
		PluginDivider divider = new PluginDivider();
		divider.addContainer(leftContainer);
		divider.addContainer(rightContainer);

		divider.setClientProportions(new double [] { 0.5d, 0.5d });

		return divider;
	}

	/**
	 * Displays a welcome page in the internal browser.
	 */
	public void displayWelcomePage()
	{
	}

	//////////////////////////////////////////////////
	// @@ Other plugin overrides
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.plugin.AbstractPlugin#pluginInstalled()
	 */
	protected void pluginInstalled()
	{
		super.pluginInstalled();

		// Update button status
		JaspiraAction action;
		action = getAction("modelerpage.view.controlanchor");
		if (action != null)
		{
			action.setSelected(!ViewModeMgr.getInstance().isControlAnchorVisible());
		}
		action = getAction("modelerpage.view.controltoggle");
		if (action != null)
		{
			action.setSelected(!ViewModeMgr.getInstance().isControlLinkVisible());
		}
		action = getAction("modelerpage.view.datatoggle");
		if (action != null)
		{
			action.setSelected(!ViewModeMgr.getInstance().isDataLinkVisible());
		}

		backwardAction = getAction("modelerpage.view.processback");
		forwardAction = getAction("modelerpage.view.processforward");

		if (backwardAction != null)
		{
			backwardActionText = backwardAction.getDescription();
		}
		if (forwardAction != null)
		{
			forwardActionText = forwardAction.getDescription();
		}

		updateNavigationButtons();
	}

	/**
	 * @see org.openbp.jaspira.gui.plugin.JaspiraPage#pluginUninstalled()
	 */
	protected void pluginUninstalled()
	{
		super.pluginUninstalled();

		// Clear references for better garbage collection in case of memory leaks
		modelerContainer = null;
	}

	/**
	 * Creates a new {@link Modeler} and adds it to the modeler page.
	 *
	 * @param process Process to display in the new view
	 * @param readOnly
	 * true: Open the process in read-only mode<br>
	 * false: Open the process in edit mode
	 * @return The new modeler
	 */
	protected Modeler addModelerView(ProcessItem process, boolean readOnly)
	{
		// Create a new modeler
		Modeler modeler = (Modeler) PluginMgr.getInstance().createInstance(Modeler.class, this);

		// Make the modeler operate on the given process
		modeler.setProcess(process, readOnly);

		int nModelersBefore = modelerContainer.getNumberOfPlugins();

		// Add the new view to the visible plugin area of the page
		modelerContainer.addPlugin(modeler);

		int nModelersAfter = modelerContainer.getNumberOfPlugins();

		// Check if we had/have no modeler plugin (comparison to 1 due to web browser plugin, which resides also in this container)
		if (nModelersBefore == 1 || nModelersAfter == 1)
		{
			// Rebuild the menu and toolbar in order to consider the modeler-specific menu items
			JaspiraPageContainer pageContainer = (JaspiraPageContainer) getParentContainer();
			pageContainer.buildMenu();
		}

		modeler.focusPlugin();

		return modeler;
	}

	//////////////////////////////////////////////////
	// @@ History support
	//////////////////////////////////////////////////

	/**
	 * Updates the back and forward navigation buttons.
	 */
	protected void updateNavigationButtons()
	{
		updateNavigationButton(backwardAction, backwardActionText, -1);
		updateNavigationButton(forwardAction, forwardActionText, 1);
	}

	/**
	 * Updates a navigation button.
	 *
	 * @param action Action representing the button
	 * @param text Original text of the button
	 * @param offset Offset to current history index
	 */
	private void updateNavigationButton(JaspiraAction action, String text, int offset)
	{
		if (action != null)
		{
			ModelQualifier qualifier = null;
			if (getHistoryIndex() >= 0)
			{
				qualifier = getHistoryEntryAt(getHistoryIndex() + offset);
			}

			action.setEnabled(qualifier != null);
			if (qualifier != null)
			{
				action.setDescription(text + " (" + qualifier.toUntypedString() + ")");
			}
			else
			{
				action.setDescription(text);
			}
		}
	}

	/**
	 * Adds the given process path to the process history.
	 *
	 * @param qualifier Model qualifier to add
	 */
	protected void addQualifierToHistory(ModelQualifier qualifier)
	{
		if (skipNextHistoryEvent)
		{
			skipNextHistoryEvent = false;
		}
		else
		{
			// Remove all entries after the current one
			for (int i = historyList.size() - 1; i > historyIndex; --i)
			{
				historyList.remove(i);
			}

			// Trim to maximum size
			while (historyList.size() >= MAX_HISTORY_SIZE)
			{
				historyList.remove(0);
			}

			// Add URL. The current index points to the added element.
			historyList.add(qualifier);
			historyIndex = historyList.size() - 1;

			debugHistory("after add");
		}

		updateNavigationButtons();
	}

	/**
	 * Gets the history entry at the specified index.
	 *
	 * @param index History index
	 * @return The model qualifier or null if the index is out of bounds
	 */
	protected ModelQualifier getHistoryEntryAt(int index)
	{
		if (index >= 0 && index < historyList.size())
		{
			return (ModelQualifier) historyList.get(index);
		}
		return null;
	}

	/**
	 * Gets the current position in history list.
	 * @nowarn
	 */
	public int getHistoryIndex()
	{
		return historyIndex;
	}

	/**
	 * Checks if we can execute the {@link #goBackward} function.
	 * @nowarn
	 */
	protected boolean canGoBackward()
	{
		return historyIndex > 0;
	}

	/**
	 * Checks if we can execute the {@link #goForward} function.
	 * @nowarn
	 */
	protected boolean canGoForward()
	{
		return historyIndex >= 0 && historyIndex < historyList.size() - 1;
	}

	/**
	 * Goes to previous page in history.
	 */
	protected void goBackward()
	{
		if (historyIndex > 0)
		{
			--historyIndex;

			ModelQualifier qualifier = (ModelQualifier) historyList.get(historyIndex);
			displayProcess(qualifier);

			debugHistory("after back");
		}
	}

	/**
	 * Goes to next page in history.
	 */
	protected void goForward()
	{
		if (historyIndex >= 0 && historyIndex < historyList.size() - 1)
		{
			++historyIndex;

			ModelQualifier qualifier = (ModelQualifier) historyList.get(historyIndex);
			displayProcess(qualifier);

			debugHistory("after forward");
		}
	}

	/**
	 * Open the given process.
	 *
	 * @param qualifier Process qualifier
	 */
	protected void displayProcess(ModelQualifier qualifier)
	{
		// This will generate a history event by itself, take care to ignore it
		skipNextHistoryEvent = true;

		JaspiraEventMgr.fireGlobalEvent(new OpenEvent(this, "open.modeler", qualifier));
	}

	/**
	 * Prints a debug output of the history.
	 *
	 * @param title Debug title
	 */
	private void debugHistory(String title)
	{
		if (historyDebug)
		{
			System.out.println("***** Browse history (" + title + ") *****");
			for (int i = 0; i < historyList.size(); ++i)
			{
				ModelQualifier qualifier = (ModelQualifier) historyList.get(i);
				System.out.println((i == historyIndex ? "*" : " ") + "history[" + i + "] = " + (qualifier != null ? qualifier.toUntypedString() : "null"));
			}
			System.out.println();
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Event module
	/////////////////////////////////////////////////////////////////////////

	/**
	 * EventModule responsible for opening new modelers.
	 */
	public class Events extends EventModule
	{
		public String getName()
		{
			return "modelerpage.view";
		}

		/**
		 * Gets the module priority.
		 * We are high priority.
		 *
		 * @return The priority. 0 is lowest, 100 is highest.
		 */
		public int getPriority()
		{
			return 1;
		}

		/**
		 * Event handler: Check supported mime types for the open event.
		 * Adds the event names for open events for process and action items
		 * to the result of the poll event.
		 *
		 * @event plugin.association.supports
		 * @param event Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode plugin_association_supports(RequestEvent event)
		{
			String mimeType = (String) event.getObject();

			if (MimeTypes.PROCESS_ITEM.equals(mimeType) || MimeTypes.PROCESS_NODE.equals(mimeType))
			{
				event.addResult(new OpenEventInfo("open.modeler", mimeType, getPluginResourceCollection().getRequiredString("title.association.modeler")));
				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
		}

		/**
		 * Event handler: Open an object.
		 * Checks if the object to open is a process and handles it.
		 *
		 * @event open.modeler
		 * @param oe Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode open_modeler(OpenEvent oe)
		{
			Object o = oe.getObject();

			// Display the wait cursor
			ApplicationUtil.waitCursorOn();

			try
			{
				ModelQualifier qualifier = null;

				if (o instanceof String)
				{
					qualifier = new ModelQualifier((String) o);
					qualifier.setItemType(ItemTypes.PROCESS);
				}

				if (o instanceof ModelQualifier)
				{
					// Get the item specified by themodel qualifier 
					qualifier = (ModelQualifier) o;
				}

				if (qualifier != null)
				{
					String itemType = qualifier.getItemType();
					if (itemType != null && !itemType.equals(ItemTypes.PROCESS))
					{
						// Not a processmodel qualifier 
						return EVENT_IGNORED;
					}
					qualifier.setItemType(ItemTypes.PROCESS);

					try
					{
						o = ModelConnector.getInstance().getItemByQualifier(qualifier, true);
					}
					catch (ModelException e)
					{
						String msg = "The process '" + qualifier + "' does not exist.";
						JMsgBox.show(null, msg, JMsgBox.ICON_ERROR);
						return EVENT_CONSUMED;
					}
				}

				if (o instanceof ProcessItem)
				{
					// Open the process
					ProcessItem process = (ProcessItem) o;
					addModelerView(process, oe.isReadonly());

					if (qualifier != null && qualifier.getObjectPath() != null)
					{
						ModelerPage.this.fireEvent(new QualifierEvent(ModelerPage.this, "modeler.view.select", qualifier));
					}

					return EVENT_CONSUMED;
				}
			}
			finally
			{
				// Reset the wait cursor
				ApplicationUtil.waitCursorOff();
			}

			return EVENT_IGNORED;
		}

		/**
		 * Event handler: A modeler view has become active.
		 *
		 * @event modeler.view.activated
		 * @eventobject Editor that owns the view ({@link Modeler})
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode modeler_view_activated(JaspiraEvent je)
		{
			Object o = je.getObject();

			if (o instanceof Modeler)
			{
				ModelQualifier qualifier = ((Modeler) o).getProcess().getQualifier();
				addQualifierToHistory(qualifier);

				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
		}

		/**
		 * Event handler: Add the given model qualifier to the history.
		 *
		 * @event modeler.view.addtohistory
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode modeler_view_addtohistory(QualifierEvent je)
		{
			addQualifierToHistory(je.getQualifier());
			return EVENT_CONSUMED;
		}

		/**
		 * Toggles control anchor display.
		 * @event modelerpage.view.controlanchor
		 * @param jae Action event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode controlanchor(JaspiraActionEvent jae)
		{
			// Toggle the visibility status
			boolean visible = ViewModeMgr.getInstance().isControlAnchorVisible();
			visible = !visible;
			ViewModeMgr.getInstance().setControlAnchorVisible(visible);

			return EVENT_HANDLED;
		}

		/**
		 * Toggles control link display.
		 * @event modelerpage.view.controltoggle
		 * @param jae Action event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode controltoggle(JaspiraActionEvent jae)
		{
			// Toggle the visibility status
			boolean visible = ViewModeMgr.getInstance().isControlLinkVisible();
			visible = !visible;
			ViewModeMgr.getInstance().setControlLinkVisible(visible);

			return EVENT_HANDLED;
		}

		/**
		 * Toggles data link display.
		 * @event modelerpage.view.datatoggle
		 * @param jae Action event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode datatoggle(JaspiraActionEvent jae)
		{
			// Toggle the visibility status
			boolean visible = ViewModeMgr.getInstance().isDataLinkVisible();
			visible = !visible;
			ViewModeMgr.getInstance().setDataLinkVisible(visible);

			return EVENT_HANDLED;
		}

		/**
		 * Toggles grid display.
		 * @event modelerpage.view.gridtoggle
		 * @param jae Action event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode gridtoggle(JaspiraActionEvent jae)
		{
			boolean showGrid = OptionMgr.getInstance().getBooleanOption("editor.grid.display", false);
			showGrid = !showGrid;
			OptionMgr.getInstance().setOption("editor.grid.display", new Boolean(showGrid));
			OptionMgr.getInstance().saveOptions();

			// Since we change a global option, prevent that the event will be processed by
			// several Modeler instances, which may cause the toggle to be reverted.
			return EVENT_CONSUMED;
		}

		/**
		 * Moves backward in the process history.
		 * @event modelerpage.view.processback
		 * @param jae Action event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode processback(JaspiraActionEvent jae)
		{
			ModelerPage.this.goBackward();

			return EVENT_CONSUMED;
		}

		/**
		 * Moves forward in the process history.
		 * @event modelerpage.view.processforward
		 * @param jae Action event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode processforward(JaspiraActionEvent jae)
		{
			ModelerPage.this.goForward();

			return EVENT_CONSUMED;
		}

		//////////////////////////////////////////////////
		// @@ Zoom support
		//////////////////////////////////////////////////

		/**
		 * Adjusts the zoom factor.
		 * This event will be produced by the zoom combo box in the main toolbar.
		 * @event modelerpage.view.zoomfactor
		 * @param jae Action event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode zoomfactor(JaspiraActionEvent jae)
		{
			String text = null;

			ActionEvent ae = jae.getActionEvent();
			if (ae.getSource() instanceof JaspiraToolbarCombo)
			{
				JaspiraToolbarCombo combo = (JaspiraToolbarCombo) ae.getSource();
				text = combo.getText();
				if (text != null)
				{
					int i = text.indexOf(' ');
					if (i < 0)
						i = text.indexOf('%');
					if (i > 0)
					{
						text = text.substring(0, i);
					}
				}
			}

			double factor = 1d;
			if (text != null)
			{
				try
				{
					factor = Double.parseDouble(text) / 100;
				}
				catch (NumberFormatException e)
				{
				}
			}

			ModelerPage.this.fireEvent("modeler.view.setzoomfactor", new Double(factor));

			return EVENT_CONSUMED;
		}

		/**
		 * Shows the given zoom factor in the zoom combo box.
		 * @event modelerpage.view.showzoomfactor
		 * @eventparam Double The current zoom factor
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode showzoomfactor(JaspiraEvent je)
		{
			JaspiraAction action = getAction("modelerpage.view.zoomfactor");
			if (action != null)
			{
				Double factor = (Double) je.getObject();
				if (factor != null)
				{
					action.putValue(JaspiraToolbarCombo.PROPERTY_TEXT, "" + CommonUtil.rnd(factor.doubleValue() * 100) + " %");
					action.setEnabled(true);
				}
				else
				{
					action.putValue(JaspiraToolbarCombo.PROPERTY_TEXT, null);
					action.setEnabled(false);
				}
			}

			return EVENT_CONSUMED;
		}
	}
}
