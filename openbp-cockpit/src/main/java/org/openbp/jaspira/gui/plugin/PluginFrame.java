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
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;

import org.openbp.jaspira.action.JaspiraToolbar;
import org.openbp.jaspira.plugin.Plugin;
import org.openbp.jaspira.plugin.PluginMgr;

/**
 */
public class PluginFrame extends JFrame
	implements PluginHolder
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Plugin of this frame */
	private VisiblePlugin plugin;

	/** Id of our return page plugin */
	private String returnPageId;

	/** Main toolbar */
	private JaspiraToolbar mainToolbar;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param plugin Contained plugin
	 * @param returnPageId Id of the {@link JaspiraPage} to add the plugin to after window closing.
	 * If this id is null, the plugin will be uninstalled if the frame is closed.
	 * @param gc Graphics configuration for frame display
	 */
	public PluginFrame(VisiblePlugin plugin, String returnPageId, GraphicsConfiguration gc)
	{
		super(plugin.getTitle(), gc);

		init(plugin, returnPageId);
	}

	/**
	 * Constructor, creating a frame without owner using the default graphics configuration.
	 *
	 * @param plugin Contained plugin
	 * @param returnPageId Id of the {@link JaspiraPage} to add the plugin to after window closing.
	 * If this id is null, the plugin will be uninstalled if the frame is closed.
	 */
	public PluginFrame(VisiblePlugin plugin, String returnPageId)
	{
		this(plugin, returnPageId, null);
	}

	/**
	 * Initializes the frame and its content.
	 *
	 * @param plugin Contained plugin
	 * @param returnPageId Id of the {@link JaspiraPage} to add the plugin to after window closing.
	 * If this id is null, the plugin will be uninstalled if the frame is closed.
	 */
	protected void init(VisiblePlugin plugin, String returnPageId)
	{
		this.plugin = plugin;
		this.returnPageId = returnPageId;

		plugin.setPluginHolder(this);

		initIcon();

		JComponent comp = plugin.getPluginComponent();
		getContentPane().add(comp);

		buildToolBar();

		pack();

		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent we)
			{
				close();
			}
		});
	}

	/**
	 * Initializes the icon of the frame.
	 */
	private void initIcon()
	{
		// Set frame icon
		Icon icon = plugin.getIcon();
		BufferedImage buf = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = buf.getGraphics();
		icon.paintIcon(this, g, 0, 0);
		setIconImage(buf);
	}

	/**
	 * Builds the frame's tool bar.
	 */
	protected void buildToolBar()
	{
		if (mainToolbar != null)
		{
			getContentPane().remove(mainToolbar);
		}

		mainToolbar = new JaspiraToolbar();
		mainToolbar.setFloatable(false);

		JaspiraToolbar toolbar = plugin.createToolbar();
		if (toolbar != null)
		{
			mainToolbar.add(toolbar, BorderLayout.WEST);
		}

		getContentPane().add(mainToolbar, BorderLayout.NORTH);
	}

	/**
	 * Closes the frame and handles the plugin.
	 * If the return page id is null or the page doesn't exist, the plugin will be uninstalled.
	 */
	public void close()
	{
		dispose();

		if (returnPageId != null)
		{
			Plugin page = PluginMgr.getInstance().getPlugin(returnPageId);

			if (page != null)
			{
				// Use the panel of the plugin as holder again
				plugin.setPluginHolder(plugin.getPluginPanel());

				// Re-initialize the plugin panel
				plugin.getPluginPanel().initTitleBar();

				// Re-add the plugin to the back page
				((JaspiraPage) page).addPlugin(plugin);
				return;
			}
		}

		// No further use for this plugin, uninstall it
		PluginMgr.getInstance().removeInstance(plugin);
	}

	public void setVisible(boolean visible)
	{
		if (visible)
		{
			setLocationRelativeTo(getOwner());
		}
		super.setVisible(visible);
		invalidate();
		repaint();
	}

	//////////////////////////////////////////////////
	// @@ PluginHolder implementation
	//////////////////////////////////////////////////

	/**
	 * Brings the plugin frame to the front.
	 * @param changePage Ignored
	 */
	public void showHolder(boolean changePage)
	{
		toFront();
	}

	public void updateHolder(boolean fullRebuild)
	{
		if (fullRebuild || mainToolbar == null)
		{
			buildToolBar();
		}
		setTitle(plugin.getTitle());
	}

	public void unlinkHolder()
	{
		dispose();
	}
}
