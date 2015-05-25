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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.openbp.common.CommonUtil;
import org.openbp.common.icon.MultiIcon;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.rc.ResourceCollectionMgr;
import org.openbp.common.util.ToStringHelper;
import org.openbp.jaspira.JaspiraConstants;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraToolbar;
import org.openbp.jaspira.action.JaspiraToolbarButton;
import org.openbp.jaspira.gui.interaction.BasicDropRegionId;
import org.openbp.jaspira.gui.interaction.CircleDropRegion;
import org.openbp.jaspira.gui.interaction.DragInitiator;
import org.openbp.jaspira.gui.interaction.DragOrigin;
import org.openbp.jaspira.gui.interaction.DropClientUtil;
import org.openbp.jaspira.gui.interaction.InteractionClient;
import org.openbp.jaspira.gui.interaction.RectangleSegment;
import org.openbp.jaspira.util.StandardFlavors;
import org.openbp.swing.plaf.sky.ReverseShadowBorder;

/**
 * A plugin panel displays a plugin.
 * It contains the plugin component as 'content pane' and a gradient title bar
 * showing the name of the plugin and a toolbar.<br>
 * If the toolbar doesn't fit into the title bar, a small icon is displayed that pops up
 * the toolbar.<br>
 * A plugin panel is always contained in a {@link TabbedPluginContainer} or a {@link PluginDivider}
 *
 * @author Stephan Moritz
 */
public class PluginPanel extends JPanel
	implements PluginHolder, InteractionClient, DragOrigin, FocusListener, ComponentListener, MouseListener
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Constants
	/////////////////////////////////////////////////////////////////////////

	/** Color of left edge of title of an active plugin window */
	private static final Color ACTIVE_GRADIENT_1 = new Color(133, 157, 199, 255);

	/** Color of left edge of title of an active plugin window */
	private static final Color ACTIVE_GRADIENT_2 = new Color(133, 157, 199, 0);

	/** Left title bar gradient color */
	private static final Color INACTIVE_GRADIENT_1 = new Color(192, 192, 192, 255);

	/** Right title bar gradient color */
	private static final Color INACTIVE_GRADIENT_2 = new Color(192, 192, 192, 0);

	/** Minimum dimension for toolbars */
	private static final Dimension NULL_DIMENSION = new Dimension(0, 0);

	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** Our content is a {@link VisiblePlugin} */
	private VisiblePlugin plugin;

	/** Title bar containing the name bar and the title tool bar */
	private JaspiraToolbar titleBar;

	/** Pane containing the name of the plugin */
	private GradientPane nameBar;

	/** Label containing the plugin title */
	private JLabel titleLabel;

	/** Plugin close button */
	private JaspiraToolbarButton closeButton;

	/** Toolbar of the plugin */
	private JToolBar cachedToolBar;

	/** Flag if the toolbar has been cached - necessary because cachedToolBar might be null if the plugin does not provide a toolbar */
	private boolean hasCachedToolBar;

	/** Popup menu of the toolbar */
	private JPopupMenu toolbarPopup;

	/** Last width of the panel */
	private int lastWidth;

	/** Resource that contains the close action and the toolbar popup action */
	static private ResourceCollection commonResourceCollection = ResourceCollectionMgr.getDefaultInstance().getResource(JaspiraConstants.RESOURCE_JASPIRA, PluginPanel.class);

	//////////////////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param plugin Plugin that should be held by this panel
	 */
	public PluginPanel(VisiblePlugin plugin)
	{
		super(new BorderLayout());

		// Connect to the plugin
		this.plugin = plugin;
	}

	/**
	 * Initializes the title bar of the panel.
	 */
	protected final void initTitleBar()
	{
		removeAll();

		titleLabel = new JLabel(null, null, SwingConstants.CENTER);
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setMinimumSize(NULL_DIMENSION);
		titleLabel.addMouseListener(this);

		nameBar = new GradientPane();
		nameBar.setToolTipText(plugin.getDescription());
		nameBar.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
		nameBar.addMouseListener(this);
		nameBar.add(titleLabel);

		titleBar = new JaspiraToolbar();
		titleBar.setFloatable(false);
		titleBar.setToolTipText(plugin.getDescription());
		titleBar.addMouseListener(this);

		add(titleBar, BorderLayout.NORTH);

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(new ReverseShadowBorder());
		contentPane.add(plugin.getPluginComponent());

		add(contentPane, BorderLayout.CENTER);

		if (plugin.hasCloseButton())
		{
			JaspiraAction closeAction = new JaspiraAction(commonResourceCollection, "plugin.close")
			{
				public void actionPerformed(ActionEvent e)
				{
					plugin.requestClose();
				}
			};

			contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.CTRL_MASK, true), "closePlugin");
			contentPane.getActionMap().put("closePlugin", closeAction);

			closeButton = new JaspiraToolbarButton(closeAction);
		}

		DragInitiator.makeDraggable(titleLabel, this);
		DragInitiator.makeDraggable(titleBar, this);
		DragInitiator.makeDraggable(nameBar, this);
	}

	/**
	 * Initializes various listeners.
	 */
	protected final void addListeners()
	{
		// Used to paint the title bar when the plugin receives the focus
		plugin.addPluginFocusListener(this);

		// Used to recognize clicks on the panel or title bar, which will focus the plugin
		// (also for the title bar components in initPanel below)
		addMouseListener(this);

		// Used to update the title bar if the plugin was resized
		addComponentListener(this);
	}

	/**
	 * Removes all listeners.
	 */
	protected final void removeListeners()
	{
		plugin.removePluginFocusListener(this);
		removeMouseListener(this);
		removeComponentListener(this);
	}

	/**
	 * Returns a string representation of this object.
	 * @nowarn
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "plugin");
	}

	/**
	 * Returns the current plugin of this plugin holder.
	 *
	 * @return The plugin or null if the plugin panel is empty
	 */
	public VisiblePlugin getPlugin()
	{
		return plugin;
	}

	/**
	 * Returns the tabbed container this holder belongs to.
	 * @return The container or null if the holder is not contained in a {@link TabbedPluginContainer}
	 */
	public TabbedPluginContainer getTabbedContainer()
	{
		for (Container c = getParent(); c != null; c = c.getParent())
		{
			if (c instanceof TabbedPluginContainer)
				return (TabbedPluginContainer) c;
		}
		return null;
	}

	/**
	 * Returns the container this holder belongs to.
	 * @return The container or null if the holder is not contained in a {@link PluginContainer}
	 */
	public PluginContainer getContainer()
	{
		for (Container c = getParent(); c != null; c = c.getParent())
		{
			if (c instanceof PluginContainer)
				return (PluginContainer) c;
		}
		return null;
	}

	//////////////////////////////////////////////////
	// @@ Toolbar
	//////////////////////////////////////////////////

	/**
	 * Rebuilds the title bar.
	 * The title bar consists of:<br>
	 * - Name and Icon<br>
	 * - User toolbar<br>
	 * - close button
	 *
	 * @param fullRebuild
	 *		true	Causes a full container environment including toolbar rebuild<br>
	 *		false	Updates the container title only
	 *				Also check if this would cause any change in the toolbar before rebuilding.
	 *				When rebuilding, the plugin will use the cached toolbar.
	 */
	protected void buildToolBar(boolean fullRebuild)
	{
		// Check if the plugin has received a global environment update event
		if (plugin.containsStackedEvent(VisiblePlugin.GEU) || plugin.containsStackedEvent(VisiblePlugin.GER))
		{
			// Yes, a GEU or GER has been stacked by the plugin for later execution.
			// This will cause a full toolbar rebuild, so we don't need to do anything now.
			// DEBUG System.out.println (plugin.getName () + " stacked geu detected");
			return;
		}

		// Update title
		String title = plugin.getTitle();
		String oldTitle = titleLabel.getText();
		if (!CommonUtil.equalsNull(title, oldTitle))
		{
			titleLabel.setText(plugin.getTitle());
		}

		if (titleBar.getComponentCount() == 0)
		{
			// The title bar hasn't been initialized so far, perform a full rebuild
			fullRebuild = true;
		}

		if (fullRebuild)
		{
			// Update the plugin icon
			titleLabel.setIcon(plugin.getIcon().getIcon(MultiIcon.SMALL));
		}

		// Create the toolbar
		if (fullRebuild || !hasCachedToolBar)
		{
			// refresh cache - reload toolbar
			// DEBUG System.out.println (plugin.getName () + " buildToolbar create");
			JToolBar newToolBar = plugin.createToolbar();
			if (newToolBar != null)
			{
				newToolBar.setFloatable(false);
			}

			if (cachedToolBar != newToolBar)
			{
				cachedToolBar = newToolBar;
				fullRebuild = true;
			}

			hasCachedToolBar = true;
		}

		if (!fullRebuild && !requiresToolbarRebuildOnSizeChange())
		{
			// The toolbar/toolbar popup button is already up to date, just update the title
			return;
		}

		titleBar.removeAll();
		titleBar.add(nameBar);

		if (cachedToolBar != null)
		{
			if (shouldDisplayToolbarPopup())
			{
				// DEBUG System.out.println (plugin.getName () + " buildToolbar popup");
				// If the toolbar and the title do not fit within the title bar,
				// display a small button that pops up the toolbar
				if (toolbarPopup == null)
				{
					toolbarPopup = new JPopupMenu();
				}
				else
				{
					toolbarPopup.removeAll();
				}
				toolbarPopup.add(cachedToolBar);

				JaspiraAction toolbarPopupTrigger = new JaspiraAction(commonResourceCollection, "plugin.toolbarmenu")
				{
					public void actionPerformed(ActionEvent e)
					{
						Dimension d = getToolkit().getScreenSize();
						Component c = (Component) e.getSource();

						if (cachedToolBar == null)
							return;
						Dimension prefSize = cachedToolBar.getPreferredSize();

						int x = 2;
						int y = c.getHeight() + 2;

						if (c.getLocationOnScreen().x + prefSize.width > d.width)
						{
							x -= 2 + prefSize.width;
						}

						toolbarPopup.show(c, x, y);
					}
				};
				titleBar.add(toolbarPopupTrigger);
			}
			else
			{
				// DEBUG System.out.println (plugin.getName () + " buildToolbar visible");
				toolbarPopup = null;
				titleBar.add(cachedToolBar);
			}
		}

		if (closeButton != null)
		{
			titleBar.add(closeButton);
		}
	}

	/**
	 * Checks if a toolbar rebuild due to a size change of the panel is required.
	 * @nowarn
	 */
	protected boolean requiresToolbarRebuildOnSizeChange()
	{
		boolean required = false;

		if (cachedToolBar != null)
		{
			boolean showPopup = shouldDisplayToolbarPopup();
			boolean popupVisible = toolbarPopup != null;

			if (showPopup != popupVisible)
				required = true;
		}

		return required;
	}

	/**
	 * Checks if a toolbar popup button instead of the entire toolbar should be displayed.
	 * @return
	 *		true	The panel is too small, display just the single button to pop it open.<br>
	 *		false	The panel is large enough to display the entire toolbar.
	 */
	protected boolean shouldDisplayToolbarPopup()
	{
		if (cachedToolBar != null)
		{
			int width = getWidth();
			if (width >= 0)
			{
				Dimension toolbarSize = cachedToolBar.getPreferredSize();
				if (toolbarSize != null)
				{
					// Compute the size remaining for the toolbar based on the current sizes of the panel
					int remainingSize = width - nameBar.getPreferredSize().width;
					if (closeButton != null)
					{
						remainingSize -= closeButton.getPreferredSize().width;
					}

					if (remainingSize < toolbarSize.width)
					{
						// The toolbar won't fit, we we display a toolbar popup
						return true;
					}
				}
			}
		}

		return false;
	}

	//////////////////////////////////////////////////
	// @@ PluginHolder implementation
	//////////////////////////////////////////////////

	/**
	 */
	public void updateHolder(boolean fullRebuild)
	{
		buildToolBar(fullRebuild);

		// Propagate to the tabbed container, if any
		TabbedPluginContainer tpc = getTabbedContainer();
		if (tpc != null)
		{
			tpc.updateContainer(fullRebuild);
		}
	}

	/**
	 * Removes the plugin panel from its container.
	 */
	public void unlinkHolder()
	{
		// Propagate to the tabbed container, if any
		TabbedPluginContainer tpc = getTabbedContainer();
		if (tpc != null)
		{
			tpc.removePlugin(plugin);
		}
	}

	/**
	 * Requests the plugin holder to become visible.
	 *
	 * @param changePage
	 *		true	Switches the page if the container is not a part of the current page.<br>
	 *		false	Makes the container the active container on its page, but does not switch to the new page.
	 */
	public void showHolder(boolean changePage)
	{
		// Propagate to the tabbed container, if any
		TabbedPluginContainer tpc = getTabbedContainer();
		if (tpc != null)
		{
			tpc.setSelectedPlugin(plugin);
		}

		if (changePage)
		{
			// Propagate to the page
			JaspiraPage page = plugin.getPage();
			if (page != null)
			{
				page.showPlugin(true);
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ JComponent overrides
	//////////////////////////////////////////////////

	/**
	 * The component requests the focus.
	 * A plugin container can't have the focus - forward the request to the plugin.
	 */
	public void requestFocus()
	{
		plugin.focusPlugin();
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Plugin proxy properties
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the title of the plugin.
	 * @nowarn
	 */
	public String getTitle()
	{
		return plugin.getTitle();
	}

	/**
	 * Returns the sub title of this plugin of the plugin.
	 * @nowarn
	 */
	public String getSubTitle()
	{
		return plugin.getSubTitle();
	}

	/**
	 * Gets the description of the plugin.
	 * @nowarn
	 */
	public String getDescription()
	{
		return plugin.getDescription();
	}

	/**
	 * Gets the icon of the plugin.
	 * @nowarn
	 */
	public MultiIcon getIcon()
	{
		return plugin.getIcon();
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ InteractionClient implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * We do not react to any drag events per default.
	 *
	 * @param transferable Transferable to be dragged
	 */
	public void dragStarted(Transferable transferable)
	{
		DropClientUtil.dragStarted(this, transferable);
	}

	/**
	 * We do not react to any drag events per default.
	 *
	 * @param transferable Transferable that has been dragged
	 */
	public void dragEnded(Transferable transferable)
	{
		DropClientUtil.dragEnded(this, transferable);
	}

	/**
	 * We do not react to any action events per default.
	 */
	public void dragActionTriggered(Object regionId, Point p)
	{
	}

	/**
	 * Standard region for a plugin panel is the center of the panel.
	 *
	 * In order to register your own zones, do not override this, override getUserRegions () instead.
	 *
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getDropRegions(List, Transferable, MouseEvent)
	 */
	public List getDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		// Our standardRegions only react to pluginStatesFlavor, so
		// this is what we check for.
		if (flavors.contains(StandardFlavors.PLUGIN) || flavors.contains(StandardFlavors.PLUGIN_STATE))
		{
			// Create and add our standard regions
			// Identifier for NORTH, this is DropCLient, shape, framecolor black, no Stroke,
			// Fill Red, default cursor, no overlay, copy and move actions, this component.
			CircleDropRegion region = new CircleDropRegion(PluginContainer.REGION_CENTER, this, new RectangleSegment(this, 10, PluginContainer.CENTER), 50, this);
			region.setPaint(Color.GREEN);
			return Collections.singletonList(region);
		}

		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getAllDropRegions(List, Transferable, MouseEvent)
	 */
	public final List getAllDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		return DropClientUtil.getAllDropRegions(this, flavors, data, mouseEvent);
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
	 * Imports the data from the given DragAwareRegion.
	 * Handles imports from the plugin's standard regions.
	 */
	public final boolean importData(Object regionId, Transferable data, Point p)
	{
		// Is it one of our default regions?
		if (regionId instanceof BasicDropRegionId)
		{
			// Forward to the container of the plugin panel
			return getTabbedContainer().importData(regionId, data, p);
		}

		return false;
	}

	/**
	 * Returns all sub clients.
	 *
	 * @return A list of {@link InteractionClient} objects or null if this drop client doesn't have sub drop clients.<br>
	 * Returns the sub clients of the plugin contained within this panel.
	 */
	public List getSubClients()
	{
		return plugin != null ? plugin.getSubClients() : null;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ DragOrigin implementation
	/////////////////////////////////////////////////////////////////////////

	public boolean canDrag()
	{
		return true;
	}

	public void dropAccepted(Transferable t)
	{
	}

	public void dropCanceled(Transferable t)
	{
	}

	public void dropPerformed(Transferable t)
	{
	}

	public Transferable getTranferableAt(Point p)
	{
		if (plugin != null && plugin.canDrag())
		{
			return new PluginTransferable(plugin);
		}
		return null;
	}

	public MultiIcon getDragImage()
	{
		return plugin.getIcon();
	}

	//////////////////////////////////////////////////
	// @@ FocusListener, ComponentListener and MouseListener implementation
	//////////////////////////////////////////////////

	public void focusGained(FocusEvent e)
	{
		nameBar.setColors(ACTIVE_GRADIENT_1, ACTIVE_GRADIENT_2);
	}

	public void focusLost(FocusEvent e)
	{
		nameBar.setColors(INACTIVE_GRADIENT_1, INACTIVE_GRADIENT_2);
	}

	/**
	 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
	 */
	public void componentHidden(ComponentEvent e)
	{
	}

	/**
	 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
	 */
	public void componentMoved(ComponentEvent e)
	{
	}

	/**
	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
	 */
	public void componentResized(ComponentEvent e)
	{
		// If the width of the panel has changed, we should update the container
		// - the toolbar might not fit any more into the title bar. In this case, we will display a small toolbar popup icon.
		if (plugin != null && isVisible() && getWidth() != lastWidth)
		{
			lastWidth = getWidth();

			// Update the container environment
			plugin.postPluginContainerUpdate(false);
		}
	}

	/**
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentShown(ComponentEvent e)
	{
	}

	/**
	 * Used to recognize clicks on the panel or title bar and get the focus.
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e)
	{
		plugin.focusPlugin();
	}

	/**
	 * Used to recognize clicks on the panel or title bar and get the focus.
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e)
	{
		plugin.focusPlugin();
	}

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e)
	{
	}

	/**
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e)
	{
	}

	/**
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e)
	{
	}

	//////////////////////////////////////////////////
	// @@ Inner classes
	//////////////////////////////////////////////////

	/**
	 * A JPanel with a gradient paint as background.
	 */
	private class GradientPane extends JPanel
	{
		/** Gradient colors*/
		private Color gradientColor1;

		/** Gradient colors*/
		private Color gradientColor2;

		/** the gradient for the title */
		private GradientPaint gradientPaint;

		/** Current width */
		private int currentWidth;

		/** Current height */
		private int currentHeight;

		//////////////////////////////////////////////////
		// @@ constructor
		//////////////////////////////////////////////////

		/**
		 * Constructor.
		 */
		public GradientPane()
		{
			setOpaque(false);

			gradientColor1 = INACTIVE_GRADIENT_1;
			gradientColor2 = INACTIVE_GRADIENT_2;
		}

		//////////////////////////////////////////////////
		// @@ overrides
		//////////////////////////////////////////////////

		/**
		 * @see javax.swing.JComponent#paint(Graphics)
		 */
		public void paint(Graphics g)
		{
			int width = getWidth();
			int height = getHeight();
			if (width == 0 || height == 0)
				return;

			if (gradientPaint == null || width != currentWidth || height != currentHeight)
			{
				// Rebuild gradient if necessary
				gradientPaint = new GradientPaint(0f, 0f, gradientColor1, width, height, gradientColor2);
				currentWidth = width;
				currentHeight = height;
			}

			Graphics2D g2 = (Graphics2D) g;

			g2.setPaint(gradientPaint);
			g2.fillRect(0, 0, getWidth(), getHeight());

			super.paint(g);
		}

		//////////////////////////////////////////////////
		// @@ member access
		//////////////////////////////////////////////////

		/**
		 * Sets the left and right colors of the gradient.
		 * @nowarn
		 */
		public void setColors(Color color1, Color color2)
		{
			if (!gradientColor1.equals(color1) || !gradientColor2.equals(color2))
			{
				gradientColor1 = color1;
				gradientColor2 = color2;
				gradientPaint = null;

				repaint();
			}
		}
	}
}
