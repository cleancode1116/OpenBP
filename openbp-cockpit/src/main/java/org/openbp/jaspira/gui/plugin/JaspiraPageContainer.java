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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.openbp.common.icon.FlexibleSize;
import org.openbp.common.icon.MultiIcon;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.rc.ResourceCollectionMgr;
import org.openbp.common.util.ToStringHelper;
import org.openbp.jaspira.JaspiraConstants;
import org.openbp.jaspira.action.ActionMgr;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraToolbar;
import org.openbp.jaspira.action.keys.KeyMgr;
import org.openbp.jaspira.event.InteractionEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.VetoableEvent;
import org.openbp.jaspira.gui.interaction.BasicDragReactor;
import org.openbp.jaspira.gui.interaction.BasicDropRegion;
import org.openbp.jaspira.gui.interaction.DragAwareRegion;
import org.openbp.jaspira.gui.interaction.DragDropPane;
import org.openbp.jaspira.gui.interaction.DropClientUtil;
import org.openbp.jaspira.gui.interaction.DropPaneContainer;
import org.openbp.jaspira.gui.interaction.InteractionClient;
import org.openbp.jaspira.plugin.AbstractPlugin;
import org.openbp.jaspira.plugin.Plugin;
import org.openbp.jaspira.plugin.PluginMgr;
import org.openbp.jaspira.plugin.PluginState;
import org.openbp.jaspira.plugins.statusbar.StatusBarPlugin;
import org.openbp.jaspira.util.StandardFlavors;
import org.openbp.swing.SwingUtil;
import org.openbp.swing.plaf.sky.SimpleBorder;

/**
 * The page container is a frame that hold a number of {@link JaspiraPage} pages.
 * You can't add components to this frame directly, only pages.
 * Jaspira pages are areas that contain plugins. Only one page can be visible at a time.
 *
 * @author Jens Ferchland
 */
public class JaspiraPageContainer extends JFrame
	implements PluginContainer, InteractionClient, DropPaneContainer, Plugin, WindowFocusListener
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Small font for the button */
	private static final Font BUTTON_FONT = new Font("arial", Font.PLAIN, 10);

	/** Color for the hotkey (tansparent blue) */
	private static final Color HOTKEY_COLOR = new Color(0f, 0f, 1f, 0.4f);

	/** An application has a minimum size, also if the application is empty */
	public static final Dimension MIN_FRAME_SIZE = new Dimension(800, 600);

	/** Id for 'open in new frame' drop region */
	private static final String OPENINFRAME_REGION = "openinframe";

	/** Resource for our actions etc. */
	private static ResourceCollection resourceCollection = ResourceCollectionMgr.getDefaultInstance().getResource(JaspiraConstants.RESOURCE_JASPIRA, JaspiraPageContainer.class);

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/**
	 * Plugin to delegate plugin methods to.
	 * Needed because JaspiraPageContainer is a Frame and no AbstractPlugin
	 * and we can only have one superclass!
	 */
	private AbstractPlugin delegatePlugin;

	/** Panel holding the Jaspira pages */
	private JPanel pagePanel;

	/** Page button panel */
	private JPanel pageButtonPanel;

	/** Layout manager for the page panel - only one page is visible at a time */
	private CardLayout cardLayout;

	/** Main toolbar */
	private JaspiraToolbar toolbar;

	/** Toolbar to the left that displays the page buttons */
	private JaspiraToolbar pageButtonBar;

	/** Drag area below the page button bar that opens as separate dialog when the plugin is dragged onto it */
	private JLabel openInDialogLabel;

	/** Table mapping the {@link JaspiraPage} to its corresponding PageButton */
	private Map pageButtonsByPage;

	/** All page buttons are in this group - only one is selected */
	private ButtonGroup buttonGroup;

	/** The currently visible page */
	private JaspiraPage activePage;

	/** Input map of the page button bar for page hotkeys */
	private InputMap inputmap;

	/** Action map of the page button bar for page hotkeys */
	private ActionMap actionmap;

	/** Button size for page and 'open in new dialog' buttons */
	private int buttonSize;

	/** Dimension of a page button */
	private Dimension buttonDim;

	/** Font for hotkey display */
	private Font hotkeyFont;

	/** Display titles for page buttons? */
	private static boolean displayPageButtonTitles;

	//////////////////////////////////////////////////
	// @@ Constructor
	//////////////////////////////////////////////////

	/**
	 * Creates a new frame with the given title.
	 *
	 * @param title Window title of the frame
	 * @param gc Graphics context to use
	 */
	public JaspiraPageContainer(String title, GraphicsConfiguration gc)
	{
		super(title, gc);

		// Provide an empty menu bar page, menu will be rebuilt later
		setJMenuBar(new JMenuBar());

		pageButtonsByPage = new HashMap();
		buttonGroup = new ButtonGroup();

		// Create a dummy plugin for event dispatching.
		delegatePlugin = new AbstractPlugin()
		{
			public ResourceCollection getPluginResourceCollection()
			{
				return resourceCollection;
			}

			public String getResourceCollectionContainerName()
			{
				return null;
			}
		};
		delegatePlugin.setParentPlugin(ApplicationBase.getInstance());
		delegatePlugin.initializePlugin();
		delegatePlugin.installPlugin();

		// Set basic sizes and fonts according to chosen style
		if (displayPageButtonTitles)
		{
			// Large button bar with titles
			buttonSize = FlexibleSize.MEDIUM;
			buttonDim = new Dimension(52, 40);
			hotkeyFont = new Font("arial", Font.BOLD, 36);
		}
		else
		{
			// Small button bar (icons only)
			buttonSize = FlexibleSize.SMALL;
			buttonDim = new Dimension(20, 20);
			hotkeyFont = new Font("arial", Font.BOLD, 20);
		}

		// Create the button bar
		pageButtonBar = new JaspiraToolbar(buttonSize, JaspiraToolbar.VERTICAL);
		pageButtonBar.setFloatable(false);

		// Sets the alt action to show the hotkeys of the page buttons in the bar
		inputmap = pageButtonBar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		actionmap = pageButtonBar.getActionMap();
		inputmap.put(KeyStroke.getKeyStroke("alt pressed ALT"), "mark_on");
		inputmap.put(KeyStroke.getKeyStroke("released ALT"), "mark_off");
		inputmap.put(KeyStroke.getKeyStroke("ctrl released ALT"), "mark_off");
		actionmap.put("mark_on", new DisplayHotkeyAction(true));
		actionmap.put("mark_off", new DisplayHotkeyAction(false));

		// Create the main toolbar of the page container
		toolbar = new JaspiraToolbar();
		toolbar.setFloatable(false);

		// The 'open in new dialog' label can be used to drag a plugin onto it in order to move it to its own dialog frame
		openInDialogLabel = new JLabel(((MultiIcon) getPluginResourceCollection().getRequiredObject("frame.plugininframe.icon")).getIcon(buttonSize));
		openInDialogLabel.setToolTipText(getPluginResourceCollection().getRequiredString("frame.plugininframe.description"));
		openInDialogLabel.setBackground(Color.WHITE);
		openInDialogLabel.setBorder(SimpleBorder.getStandardBorder());
		openInDialogLabel.setMaximumSize(buttonDim);
		openInDialogLabel.setMinimumSize(buttonDim);
		openInDialogLabel.setPreferredSize(buttonDim);

		// Create the page button panel that holds the page button bar at the top and the 'open in new dialog' area at the bottom
		pageButtonPanel = new JPanel(new BorderLayout());
		pageButtonPanel.setBorder(SimpleBorder.getStandardBorder());
		pageButtonPanel.add(pageButtonBar, BorderLayout.NORTH);
		pageButtonPanel.add(new JSeparator());
		pageButtonPanel.add(openInDialogLabel, BorderLayout.SOUTH);

		StatusBarPlugin statusBar = (StatusBarPlugin) PluginMgr.getInstance().createInstance(StatusBarPlugin.class, this);

		// Create the area for the pages to display
		cardLayout = new CardLayout();
		pagePanel = new JPanel(cardLayout);

		// The content panel holds the main toolbar at the top, the page area centered,
		// the status bar at the bottom and the page button panel to the left
		JPanel contentPanel = (JPanel) getContentPane();
		contentPanel.setLayout(new BorderLayout());
		contentPanel.add(toolbar, BorderLayout.NORTH);
		contentPanel.add(pagePanel);
		contentPanel.add(pageButtonPanel, BorderLayout.WEST);
		contentPanel.add(statusBar.getPluginComponent(), BorderLayout.SOUTH);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// For clearing the hotkey display
		addWindowFocusListener(this);

		// Install the keyboard manager for top-level frames
		KeyMgr.getInstance().install(contentPanel);
	}

	/**
	 * Returns a string representation of this object.
	 * @nowarn
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "pages");
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ SwingComponents
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Rebuild the frame's menu by polling for entries.
	 */
	public final void buildMenu()
	{
		String pageName = activePage != null ? activePage.getName() : null;

		// Create and add the menu bar
		JMenuBar mb = new JMenuBar();

		JaspiraAction root = ActionMgr.getInstance().getAction(JaspiraAction.MENU_ROOT);
		if (root.getMenuchildren() != null)
		{
			for (Iterator it = root.getMenuchildren().iterator(); it.hasNext();)
			{
				JaspiraAction action = (JaspiraAction) it.next();

				if (action.matchesPageName(pageName))
				{
					JMenuItem menuItem = action.toMenuItem(pageName);
					if (menuItem != null)
					{
						mb.add(menuItem);
					}
				}
			}
		}

		setJMenuBar(mb);

		// Clear and populate the toolbar
		toolbar.removeAll();

		root = ActionMgr.getInstance().getAction(JaspiraAction.TOOLBAR_ROOT);
		if (root.getToolbarchildren() != null)
		{
			for (Iterator it = root.getToolbarchildren().iterator(); it.hasNext();)
			{
				JaspiraAction action = (JaspiraAction) it.next();

				if (action.matchesPageName(pageName))
				{
					JComponent toolbarComponent = action.toToolBarComponent(pageName);
					if (toolbarComponent != null)
					{
						toolbar.add(toolbarComponent);
						toolbar.addSeparator();
					}
				}
			}
		}

		toolbar.repaint();
	}

	/**
	 * Shows or hides the page hotkeys that are painted over the page buttons.
	 *
	 * @param show
	 *		true	Shows the hotkeys.
	 *		false	Hides the hotkeys.
	 */
	void showPageHotkeys(boolean show)
	{
		// Remove the hotkey indicator when loosing the focus
		for (Iterator it = pageButtonsByPage.values().iterator(); it.hasNext();)
		{
			((PageButton) it.next()).showHotkey(show);
		}
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Adds a page to the frame.
	 *
	 * @param page Page to add
	 */
	public void addPage(JaspiraPage page)
	{
		int pageNr = pageButtonsByPage.size() + 1;
		String id = page.getUniqueId();

		// Add the page to the page panel
		pagePanel.add(page.getPluginComponent(), id);

		// Create the page button which is displayed in a fixed toolbar at the left side
		PageButton pageButton = new PageButton(page, pageNr, displayPageButtonTitles);

		// Add support for page hotkeys
		KeyStroke show = KeyStroke.getKeyStroke("alt " + pageNr);
		KeyStroke open = KeyStroke.getKeyStroke("alt shift " + pageNr);
		inputmap.put(show, "show" + id);
		inputmap.put(open, "open" + id);
		actionmap.put("show" + id, new ShowAction(page));
		actionmap.put("open" + id, new OpenAction(page));

		// Add the button to button bar and groups
		pageButtonBar.add(pageButton);
		buttonGroup.add(pageButton);
		pageButtonsByPage.put(page, pageButton);

		// The first page in a frame is always the active frame.
		if (activePage == null)
		{
			setPageActive(page);
		}

		// Display the page button panel only if there is more than one page
		int numberOfPages = pageButtonsByPage.size();
		pageButtonPanel.setVisible(numberOfPages > 1);

		// We will serve as parent plugin for the added page
		DragDropPane.installDragDropPane(this);
		page.setParentPlugin(this);

		// Rebuild the menu and toolbar
		buildMenu();

		fireEvent("global.page.added", page);

		// Force a global container rebuild of all plugins of this page
		page.fireEvent(new JaspiraEvent(page, VisiblePlugin.GER, null, JaspiraEvent.TYPE_FLOOD, Plugin.LEVEL_PAGE, JaspiraEvent.STACKABLE));
	}

	/**
	 * Removes a page from the frame.
	 *
	 * @param page Page to remove
	 */
	public void removePage(JaspiraPage page)
	{
		// remove the page from the content
		cardLayout.removeLayoutComponent(page.getPluginComponent());

		// Remove the page button
		PageButton pageButton = (PageButton) pageButtonsByPage.remove(page);
		pageButtonBar.remove(pageButton);
		buttonGroup.remove(pageButton);

		// Remove the frame if it isn't the last and it contains nothing.
		if (pageButtonsByPage.size() == 0 && !(ApplicationBase.getInstance().getFrameCount() == 1))
		{
			dispose();
		}
		else
		{
			JaspiraPage newActivePage = null;

			// Reassign the hotkeys for the remaining components
			// TODO Fix 6: Remove keys first
			Component [] comp = pageButtonBar.getComponents();
			for (int i = 0; i < comp.length; i++)
			{
				pageButton = (PageButton) comp [i];

				if (newActivePage == null)
					newActivePage = pageButton.getPage();

				int pageNr = i + 1;
				pageButton.setPageNr(pageNr);

				JaspiraPage p = pageButton.getPage();
				String id = p.getUniqueId();

				KeyStroke show = KeyStroke.getKeyStroke("alt " + pageNr);
				KeyStroke open = KeyStroke.getKeyStroke("alt shift " + pageNr);
				inputmap.put(show, "show" + id);
				inputmap.put(open, "open" + id);
				actionmap.put("show" + id, new ShowAction(p));
				actionmap.put("open" + id, new OpenAction(p));
			}
			pageButtonBar.revalidate();

			if (page == activePage && newActivePage != null)
			{
				// Select a new active page after removing the active one
				setPageActive(newActivePage);
			}
		}

		fireEvent("global.page.removed", page);
	}

	/**
	 * Opens the page in a new frame.
	 *
	 * @param page Page to move to a new frame
	 */
	public void openInNewFrame(JaspiraPage page)
	{
		// if only one page in the frame don't open it in a new one.
		if (pageButtonsByPage.size() == 1)
		{
			return;
		}

		removePage(page);

		// if we have multiscreen open the frame on a new Monitor
		// not desktop! - it makes no sence to open frames in virtual desktops.
		// so we just look for the devices and not the configurations on the devices!
		GraphicsDevice [] devs = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

		GraphicsDevice current = getGraphicsConfiguration().getDevice();

		if (devs.length == 1)
		{
			ApplicationBase.getInstance().addFrame(page, MIN_FRAME_SIZE, MAXIMIZED_BOTH);
		}
		else
		{
			// we have a configuration - we are a graphic component!
			GraphicsDevice gd = devs [0];
			for (int i = 0; i < devs.length && gd == current; i++)
			{
				gd = devs [i];
			}
			GraphicsConfiguration gc = gd.getDefaultConfiguration();

			ApplicationBase.getInstance().addFrame(page, MIN_FRAME_SIZE, ApplicationBase.getInstance().hasFrameForGraphicsConfiguration(gc) ? 0 : MAXIMIZED_BOTH, gc);
		}

		// TODO Fix 4: Bring new frame to front
	}

	/**
	 * Gets a list of all pages of this container.
	 *
	 * @return A list of {@link JaspiraPage} objects
	 */
	public List getPages()
	{
		return new ArrayList(pageButtonsByPage.keySet());
	}

	/**
	 * Gets the active page of this frame.
	 *
	 * @return The active page or null if this container does not have any pages
	 */
	public JaspiraPage getActivePage()
	{
		return activePage;
	}

	/**
	 * Activates the given page.
	 * The method will fire a global.page.askchange veto event and then - if successful -
	 * a global.page.changed event. The focus of the new page will be restored to the
	 * plugin that had the focus when the page was deactivated.
	 *
	 * @param page Page to activate
	 */
	public void setPageActive(JaspiraPage page)
	{
		if (!containsPage(page))
		{
			return;
		}

		// Ask for change from active page to page.
		VetoableEvent ask = new VetoableEvent(this, "global.page.askchange", activePage, page);
		fireEvent(ask);
		if (ask.isVetoed())
		{
			// We have a veto, no change!
			return;
		}

		cardLayout.show(pagePanel, page.getUniqueId());
		activePage = page;
		((PageButton) pageButtonsByPage.get(page)).setSelected(true);

		// Always rebuild the page container's menu when switching pages, so menu items that should appear
		// for particular pages only (see the 'pagenames' entries in the plugin resource files) are added
		// and removed automatically.
		buildMenu();

		fireEvent("global.page.changed", page);

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				activePage.restoreFocus();
			}
		});
	}

	/**
	 * Checks if the given page is hosted by this container.
	 * @nowarn
	 */
	public boolean containsPage(JaspiraPage page)
	{
		return pageButtonsByPage.containsKey(page);
	}

	/**
	 * Gets the main toolbar.
	 * @nowarn
	 */
	public JaspiraToolbar getToolbar()
	{
		return toolbar;
	}

	//////////////////////////////////////////////////
	// @@ PluginContainer implementation
	//////////////////////////////////////////////////

	public JaspiraPage getPage()
	{
		return null;
	}

	public void addPlugin(VisiblePlugin p)
	{
		if (activePage != null)
		{
			activePage.addPlugin(p);
		}
	}

	/**
	 * Removes the plugin in the frame.
	 */
	public void removePlugin(VisiblePlugin p)
	{
		for (Iterator it = getPages().iterator(); it.hasNext();)
		{
			((JaspiraPage) it.next()).removePlugin(p);
		}
	}

	/**
	 * Returns the active plugin of this frame (in the visible page).
	 */
	public VisiblePlugin getActivePlugin()
	{
		return activePage != null ? activePage.getActivePlugin() : null;
	}

	/**
	 * Returns null.
	 */
	public PluginContainer getParentContainer()
	{
		return null;
	}

	/**
	 * Returns the list of plugins held by this frame.
	 * @return A list of {@link VisiblePlugin} objects
	 */
	public List getPlugins()
	{
		List list = new ArrayList();

		for (Iterator it = getPages().iterator(); it.hasNext();)
		{
			list.addAll(((JaspiraPage) it.next()).getPlugins());
		}

		return list;
	}

	/**
	 * Returns all plugins of the active page that are currently visible, i\.e\. shown in their tabbed containers.
	 * @return A list of {@link VisiblePlugin} objects or null
	 */
	public List getVisiblePlugins()
	{
		return activePage != null ? activePage.getVisiblePlugins() : null;
	}

	/**
	 * Does nothing.
	 */
	public void sliceContainer(PluginContainer toInsert, PluginContainer currentContainer, String constraint)
	{
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ InteractionClient implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Is called, when a region triggers an action. In our case, when a dragging operation
	 * hovers above a page-button for some time.
	 */
	public void dragActionTriggered(Object regionId, Point p)
	{
		if (regionId instanceof PageButton)
		{
			PageButton pageButton = (PageButton) regionId;
			setPageActive(pageButton.getPage());

			getDragDropPane().regionsInvalidated();
		}
	}

	/**
	 * Is called when either the dragging process ended inside our frame or left the frame.
	 *
	 * @param transferable Transferable to be dragged
	 */
	public void dragStarted(Transferable transferable)
	{
		DropClientUtil.dragStarted(this, transferable);
	}

	/**
	 * Is called when a dragging action has been initialized (to be precise:
	 * when a dragging event has entered our frame.
	 *
	 * @param transferable Transferable that has been dragged
	 */
	public void dragEnded(Transferable transferable)
	{
		DropClientUtil.dragEnded(this, transferable);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getAllDropRegions(List, Transferable, MouseEvent)
	 */
	public final List getAllDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		return DropClientUtil.getAllDropRegions(this, flavors, data, mouseEvent);
	}

	/**
	 * The regions of a page container consist of the page switch zones and the 'open in new frame' zone.
	 *
	 * In order to register your own zones, do not override this, override getUserRegions () instead.
	 *
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getDropRegions(List, Transferable, MouseEvent)
	 */
	public final List getDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		List result = new ArrayList();

		List userRegions = getUserRegions(flavors);
		if (userRegions != null)
		{
			result.addAll(userRegions);
		}

		// add page switch regions on our buttons
		// Do not add the regions when a plugin is beeing dragged

		if (flavors.contains(StandardFlavors.PLUGIN) || flavors.contains(StandardFlavors.PLUGIN_STATE))
		{
			BasicDropRegion region = new BasicDropRegion(OPENINFRAME_REGION, this, openInDialogLabel);
			region.setFrameColor(Color.BLUE);
			result.add(region);
		}
		else
		{
			for (Iterator it = pageButtonsByPage.values().iterator(); it.hasNext();)
			{
				PageButton next = (PageButton) it.next();

				DragAwareRegion region = new BasicDragReactor(next, this, next, 1000);
				result.add(region);
			}
		}

		return result;
	}

	public List getImportersAt(Point p)
	{
		return null;
	}

	public List getAllImportersAt(Point p)
	{
		return DropClientUtil.getAllImportersAt(this, p);
	}

	/**
	 * Override to implemnt own regions.
	 *
	 * @param flavors Flavors the regions should be suitable for
	 * @return A list of user regions or null
	 */
	protected List getUserRegions(List flavors)
	{
		return null;
	}

	/**
	 * Returns all sub clients.
	 *
	 * @return A list of {@link InteractionClient} objects or null if this drop client doesn't have sub drop clients.<br>
	 * These are any visible Plugins.
	 */
	public List getSubClients()
	{
		if (activePage != null)
		{
			return Collections.singletonList(activePage.getPluginDivider());
		}
		return null;
	}

	/**
	 * Imports data into the frame. Standard import is a page into the page list.
	 */
	public boolean importData(Object regionId, Transferable data, Point p)
	{
		if (regionId.equals(OPENINFRAME_REGION) && activePage != null)
		{
			try
			{
				VisiblePlugin plugin = null;

				if (data.isDataFlavorSupported(StandardFlavors.PLUGIN))
				{
					// We have been passed a plugin directly, unlink its holder.
					// This will cause the plugin to be removed from its current container.
					plugin = (VisiblePlugin) data.getTransferData(StandardFlavors.PLUGIN);
					if (plugin.getPluginHolder() != null)
					{
						plugin.getPluginHolder().unlinkHolder();
					}
				}
				else if (data.isDataFlavorSupported(StandardFlavors.PLUGIN_STATE))
				{
					// Instantiate the plugin from the plugin state
					PluginState state = ((PluginState) data.getTransferData(StandardFlavors.PLUGIN_STATE));
					plugin = PluginMgr.getInstance().createVisibleInstance(state, null);
				}

				if (plugin != null)
				{
					PluginFrame frame = new PluginFrame(plugin, activePage.getUniqueId());

					// Try to retain the plugin size in the new frame
					Dimension size = plugin.getPluginComponent().getSize();
					frame.setSize(size);

					// TODO Fix 5: This doesn't work, the frame appears below the application frame
					SwingUtil.show(frame);
					frame.toFront();

					// Force a re-focus of the plugin
					PluginFocusMgr.getInstance().resetFocusCache();
					plugin.focusPlugin();

					return true;
				}
			}
			catch (UnsupportedFlavorException e)
			{
			}
			catch (IOException e)
			{
			}
		}
		return false;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ DropPaneContainer implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.gui.interaction.DropPaneContainer#addDropClient(InteractionClient)
	 */
	public void addDropClient(InteractionClient client)
	{
		getDragDropPane().addDropClient(client);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.DropPaneContainer#removeDropClient(InteractionClient)
	 */
	public void removeDropClient(InteractionClient client)
	{
		getDragDropPane().removeDropClient(client);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.DropPaneContainer#getDragDropPane()
	 */
	public DragDropPane getDragDropPane()
	{
		Component glassPane = getGlassPane();

		if (!(glassPane instanceof DragDropPane))
		{
			return DragDropPane.installDragDropPane(this);
		}

		return (DragDropPane) glassPane;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.DropPaneContainer#setDragDropPane(DragDropPane)
	 */
	public void setDragDropPane(DragDropPane pane)
	{
		setGlassPane(pane);

		pane.addDropClient(this);
	}

	//////////////////////////////////////////////////
	// @@ Plugin implementation/delegation to our dummy plugin
	//////////////////////////////////////////////////

	public String getClassName()
	{
		return getClass().getName();
	}

	public ResourceCollection getPluginResourceCollection()
	{
		return resourceCollection;
	}

	public int getLevel()
	{
		return LEVEL_FRAME;
	}

	public Plugin getParentPlugin()
	{
		return ApplicationBase.getInstance();
	}

	public void initializePlugin()
	{
		delegatePlugin.initializePlugin();
	}

	public MultiIcon getIcon()
	{
		return delegatePlugin.getIcon();
	}

	public String getSubTitle()
	{
		return delegatePlugin.getSubTitle();
	}

	public Object getPeerGroup(String key)
	{
		return delegatePlugin.getPeerGroup(key);
	}

	public boolean canClose()
	{
		return delegatePlugin.canClose();
	}

	public boolean fireEvent(JaspiraEvent je)
	{
		return delegatePlugin.fireEvent(je);
	}

	public boolean fireEvent(String eventname)
	{
		return delegatePlugin.fireEvent(eventname);
	}

	public boolean fireEvent(String eventname, Object data)
	{
		return delegatePlugin.fireEvent(eventname, data);
	}

	public List getChildPlugins()
	{
		return delegatePlugin.getChildPlugins();
	}

	public List getDescendantPlugins(List plugins)
	{
		return delegatePlugin.getDescendantPlugins(plugins);
	}

	public boolean handleEvent(JaspiraEvent je)
	{
		return delegatePlugin.handleEvent(je);
	}

	public boolean inheritEvent(JaspiraEvent je)
	{
		return delegatePlugin.inheritEvent(je);
	}

	public boolean receiveEvent(JaspiraEvent je)
	{
		return delegatePlugin.receiveEvent(je);
	}

	public void setParentPlugin(Plugin plugin)
	{
		delegatePlugin.setParentPlugin(plugin);
	}

	public void addPlugin(Plugin child)
	{
		delegatePlugin.addPlugin(child);
	}

	public void removePlugin(Plugin child)
	{
		delegatePlugin.removePlugin(child);
	}

	public void stackEvent(JaspiraEvent je)
	{
		delegatePlugin.stackEvent(je);
	}

	public boolean containsStackedEvent(String eventName)
	{
		return delegatePlugin.containsStackedEvent(eventName);
	}

	public boolean requestClose()
	{
		return delegatePlugin.requestClose();
	}

	public void addToPeerGroup(String key, Object group)
	{
		delegatePlugin.addToPeerGroup(key, group);
	}

	public String getDescription()
	{
		return delegatePlugin.getDescription();
	}

	public Set getPeerGroups()
	{
		return delegatePlugin.getPeerGroups();
	}

	public Set getPeerGroupNames()
	{
		return delegatePlugin.getPeerGroupNames();
	}

	public String getUniqueId()
	{
		return delegatePlugin.getUniqueId();
	}

	public String getVendor()
	{
		return delegatePlugin.getVendor();
	}

	public String getVersion()
	{
		return delegatePlugin.getVersion();
	}

	public String getCondition()
	{
		return delegatePlugin.getCondition();
	}

	public void installPlugin()
	{
		delegatePlugin.installPlugin();
	}

	public void installFirstPlugin()
	{
		delegatePlugin.installFirstPlugin();
	}

	public boolean matchesPeerGroup(String key, Object group, boolean strict)
	{
		return delegatePlugin.matchesPeerGroup(key, group, strict);
	}

	public boolean matchesPeerGroups(Plugin plugin, boolean strict)
	{
		return delegatePlugin.matchesPeerGroups(plugin, strict);
	}

	public void removeFromPeerGroup(String key)
	{
		delegatePlugin.removeFromPeerGroup(key);
	}

	public PluginState getPluginState()
	{
		return delegatePlugin.getPluginState();
	}

	public void setPluginState(PluginState state)
	{
		delegatePlugin.setPluginState(state);
	}

	public void uninstallPlugin()
	{
		delegatePlugin.uninstallPlugin();
	}

	public void uninstallLastPlugin()
	{
		delegatePlugin.uninstallLastPlugin();
	}

	public JaspiraAction getAction(String name)
	{
		return delegatePlugin.getAction(name);
	}

	public List getEventActionNames()
	{
		return delegatePlugin.getEventActionNames();
	}

	//////////////////////////////////////////////////
	// @@ WindowFocusListener implementation
	//////////////////////////////////////////////////

	/**
	 * Called when this frame gains the focus.
	 *
	 * @see java.awt.event.WindowFocusListener#windowGainedFocus(WindowEvent)
	 * @nowarn
	 */
	public void windowGainedFocus(WindowEvent e)
	{
	}

	/**
	 * Called when this frame lost the focus.
	 *
	 * @see java.awt.event.WindowFocusListener#windowLostFocus(WindowEvent)
	 * @nowarn
	 */
	public void windowLostFocus(WindowEvent e)
	{
		// Remove the hotkey indicator when loosing the focus
		showPageHotkeys(false);
	}

	//////////////////////////////////////////////////
	// @@ Local action classes
	//////////////////////////////////////////////////

	/**
	 * This class is used in the ActionMap to activate a page.
	 */
	private class ShowAction extends AbstractAction
	{
		/** Page this action refers to */
		private JaspiraPage page;

		/**
		 * Constructor.
		 *
		 * @param page Page this action refers to
		 */
		public ShowAction(JaspiraPage page)
		{
			this.page = page;
		}

		public void actionPerformed(ActionEvent e)
		{
			// Remove the hotkey indicator
			showPageHotkeys(false);

			setPageActive(page);
		}
	}

	/**
	 * This class is used in the ActionMap to activate a page.
	 */
	private class OpenAction extends AbstractAction
	{
		/** Page this action refers to */
		private JaspiraPage page;

		/**
		 * Constructor.
		 *
		 * @param page Page this action refers to
		 */
		public OpenAction(JaspiraPage page)
		{
			this.page = page;
		}

		public void actionPerformed(ActionEvent e)
		{
			// Remove the hotkey indicator
			showPageHotkeys(false);

			openInNewFrame(page);
		}
	}

	/**
	 * Shows on all PageButtons the hotkey.
	 */
	private class DisplayHotkeyAction extends AbstractAction
	{
		/** Flag if the hot key is to be shown or to be hidden */
		private boolean show;

		/**
		 * Constructor.
		 * @param show Flag if the hot key is to be shown or to be hidden
		 */
		public DisplayHotkeyAction(boolean show)
		{
			this.show = show;
		}

		public void actionPerformed(ActionEvent e)
		{
			// Remove the hotkey indicator
			showPageHotkeys(show);
		}
	}

	//////////////////////////////////////////////////
	// @@ PageButton class
	//////////////////////////////////////////////////

	/**
	 * The page button is displayed in the page button bar to the left of the frame
	 * and allows the activation of a page of the frame.
	 */
	private class PageButton extends JToggleButton
		implements ActionListener
	{
		/** Page referred by this button */
		private JaspiraPage page;

		/** Hotkey display text */
		private String hotKeyString;

		/** Flag if the hotkey is shown */
		private boolean hotkeyShown;

		/**
		 * Creates a new page button.
		 * @param page Page referred by this button
		 * @param nr The page number is used as hotkey for the page selection
		 * @param displayTitle
		 *		true	Also display the page title in the page button<br>
		 *		false	Display the page icon only
		 */
		public PageButton(JaspiraPage page, int nr, boolean displayTitle)
		{
			this.page = page;
			setPageNr(nr);

			setActionCommand(page.getUniqueId());

			MultiIcon icon = page.getIcon();
			JLabel iconLabel = new JLabel(icon.getIcon(buttonSize));
			iconLabel.setVerticalAlignment(SwingConstants.CENTER);
			iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
			iconLabel.setForeground(Color.BLUE);
			iconLabel.setOpaque(false);

			if (displayTitle)
			{
				// Add icon and title label
				JPanel panel = new JPanel(new BorderLayout());
				panel.setOpaque(false);

				panel.add(iconLabel);

				JLabel titleLabel = new JLabel(page.getTitle());
				titleLabel.setFont(BUTTON_FONT);
				titleLabel.setOpaque(false);
				titleLabel.setHorizontalAlignment(CENTER);
				panel.add(titleLabel, BorderLayout.SOUTH);

				add(panel);
			}
			else
			{
				// Add icon label only
				// add (iconLabel);
				JPanel panel = new JPanel(new BorderLayout());
				panel.setOpaque(false);

				panel.add(iconLabel);

				add(panel);
			}

			setMinimumSize(buttonDim);
			setMaximumSize(buttonDim);
			setPreferredSize(buttonDim);

			String s = page.getDescription();
			if (s == null)
				s = page.getTitle();
			setToolTipText(s);

			// Add an Actionlistener to the button that activates the page
			addActionListener(this);

			// Add a mouse listener for the popup menu
			addMouseListener(new MouseAdapter()
			{
				public void mouseReleased(MouseEvent me)
				{
					if (me.isPopupTrigger())
					{
						InteractionEvent iae = new InteractionEvent(JaspiraPageContainer.this, InteractionEvent.POPUP, new JaspiraPageTransferable(PageButton.this.page));
						fireEvent(iae);

						iae.createPopupMenu().show(me.getComponent(), me.getX(), me.getY());
					}
				}
			});
		}

		/**
		 * Gets the page referred by this button.
		 * @nowarn
		 */
		public JaspiraPage getPage()
		{
			return page;
		}

		/**
		 * Sets the number of this page.
		 * The page number is used as hotkey for the page selection.
		 */
		public void setPageNr(int nr)
		{
			hotKeyString = Integer.toString(nr, 10);
		}

		/**
		 * Shows or hides the page hotkey that is painted over the page button.
		 *
		 * @param show
		 *		true	Shows the hotkey.
		 *		false	Hides the hotkey.
		 */
		public void showHotkey(boolean show)
		{
			if (show != hotkeyShown)
			{
				this.hotkeyShown = show;
				repaint();
			}
		}

		public void paint(Graphics g)
		{
			super.paint(g);

			if (hotkeyShown)
			{
				g.setColor(HOTKEY_COLOR);
				g.setFont(hotkeyFont);

				FontMetrics fm = g.getFontMetrics();
				int textWidth = SwingUtilities.computeStringWidth(fm, hotKeyString);

				int w = getWidth();
				int h = getHeight();

				int x = (w - textWidth) / 2;
				int y = h / 2 + fm.getAscent() / 2 - fm.getLeading();

				g.drawString(hotKeyString, x, y);
			}
		}

		public void actionPerformed(ActionEvent ae)
		{
			setPageActive(page);
		}
	}

	//////////////////////////////////////////////////
	// @@ Serialization support
	//////////////////////////////////////////////////

	/**
	 * Returns a description of the current state of this frame.
	 */
	/*	public FrameDescriptor getFrameDescriptor ()
	 {
	 FrameDescriptor fdis = new FrameDescriptor ();

	 // set the frame state
	 fdis.setFrameState (this.getExtendedState ());

	 // the relativ position of the frame
	 Point l = this.getLocation ();
	 fdis.setPosX (l.getX () / screenDimension.getWidth ());
	 fdis.setPosY (l.getY () / screenDimension.getHeight ());

	 // the relativ size of the frame
	 Dimension dim = this.getSize ();
	 fdis.setWidth (dim.getWidth () / screenDimension.getWidth ());
	 fdis.setHeight (dim.getHeight () / screenDimension.getHeight ());

	 List pagesdis = new ArrayList ();
	 int count = pagetabpane.getTabCount ();

	 for (int i = 0; i < count; i++)
	 {
	 JaspiraPage page = (JaspiraPage) pagetabpane.getComponentAt (i);
	 pagesdis.add (page.getPageDescription ());
	 }

	 // set the display name
	 fdis.setTitle (getTitle ());

	 // set the List of pages
	 fdis.setPages (pagesdis);
	 return fdis;
	 }
	 */
	/**
	 * Creates a Frame frome a StateObject in the applicationbase context.
	 */
	/*	public JaspiraPageContainer (FrameDescriptor fdis, ApplicationBase appbase)
	 {
	 this (fdis.getTitle (), appbase, null);

	 this.setDefaultCloseOperation (JFrame.DO_NOTHING_ON_CLOSE);

	 // let the frame layout its content.
	 this.pack ();

	 // set the size and posiotion
	 if (fdis.getWidth () < 0 || fdis.getHeight () < 0)
	 {
	 // the frame has no saved place. Set default maximized frame!

	 // set the minimum size of the frame
	 this.setSize (MIN_FRAME_SIZE);

	 // The application is maximized by default.
	 this.setExtendedState (JFrame.MAXIMIZED_BOTH);
	 }
	 else
	 {
	 this.setSize ((int) (fdis.getWidth () * screenDimension.width),
	 (int) (fdis.getHeight () * screenDimension.height));

	 this.setLocation ((int) (fdis.getPosX () * screenDimension.width),
	 (int) (fdis.getPosY () * screenDimension.height));

	 // set the State of the Frame (extended, ...)
	 this.setExtendedState (fdis.getFrameState ());
	 }

	 // load the pages
	 for (Iterator it = fdis.getPages ().iterator (); it.hasNext ();)
	 {
	 PageDescriptor vdis = (PageDescriptor) it.next ();

	 JaspiraPage page;

	 if (vdis.getName ().equals (JaspiraPage.class.getName ()))
	 {
	 // the page is a standdard client page
	 page = new JaspiraPage (vdis);
	 addPage (page);
	 }
	 else
	 {
	 // try to load the subclass
	 try
	 {
	 Class subclass;
	 subclass = this.getClass ().getClassLoader ().loadClass (vdis.getName ());
	 page = (JaspiraPage) subclass.newInstance ();

	 // set the old state
	 page.setPluginDivider (new PluginDivider (vdis.getDivider ()));

	 // add to our content
	 addPage (page);
	 }
	 catch (ClassNotFoundException e)
	 {
	 ExceptionUtil.printTrace (e);
	 }
	 catch (InstantiationException e)
	 {
	 ExceptionUtil.printTrace (e);
	 }
	 catch (IllegalAccessException e)
	 {
	 ExceptionUtil.printTrace (e);
	 }
	 }
	 }

	 addWindowFocusListener (this);
	 }
	 */
}
