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
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.ref.WeakReference;

import javax.swing.JComponent;
import javax.swing.JDialog;

import org.openbp.jaspira.action.JaspiraToolbar;
import org.openbp.jaspira.plugin.Plugin;
import org.openbp.jaspira.plugin.PluginMgr;

/**
 * Shows a {@link VisiblePlugin} in a dialog.
 * Optionally, the id of a return page (a {@link JaspiraPage}) can be provided to this class on construction.
 * If the dialog is closed, its contained plugin will be added to the page.
 *
 * @author Jens Ferchland
 */
public class PluginDialog extends JDialog
	implements PluginHolder
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Plugin of this dialog */
	private VisiblePlugin plugin;

	/** Id of our return page plugin */
	private String returnPageId;

	/** Main toolbar */
	private JaspiraToolbar mainToolbar;

	/**
	 * Used to store the plugin that had the focus before calling the dialog as modal dialog.
	 * This is a temporary variable only, so we use weak references here in order to prevent memory leaks.
	 */
	protected WeakReference focusedPlugin;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor, using a frame as owner.
	 *
	 * @param owner Dialog owner
	 * @param plugin Contained plugin
	 * @param returnPageId Id of the {@link JaspiraPage} to add the plugin to after window closing.
	 * If this id is null, the plugin will be uninstalled if the dialog is closed.
	 * @param modal
	 *		true	Display as modal dialog<br>
	 *		false	Display as modeless dialog
	 * @param gc Graphics configuration for dialog display
	 */
	public PluginDialog(Frame owner, VisiblePlugin plugin, String returnPageId, boolean modal, GraphicsConfiguration gc)
	{
		super(owner, plugin.getTitle(), modal, gc);

		init(plugin, returnPageId);

		if (modal)
		{
			// Save the currently focused plugin for focus restoration after closing the dialog
			VisiblePlugin p = PluginFocusMgr.getInstance().getFocusedPlugin();
			if (p != null)
			{
				focusedPlugin = new WeakReference(p);
			}
		}
	}

	/**
	 * Constructor, using the graphics configuration of the owner.
	 *
	 * @param owner Dialog owner
	 * @param plugin Contained plugin
	 * @param returnPageId Id of the {@link JaspiraPage} to add the plugin to after window closing.
	 * If this id is null, the plugin will be uninstalled if the dialog is closed.
	 * @param modal
	 *		true	Display as modal dialog<br>
	 *		false	Display as modeless dialog
	 */
	public PluginDialog(Frame owner, VisiblePlugin plugin, String returnPageId, boolean modal)
	{
		this(owner, plugin, returnPageId, modal, owner != null ? owner.getGraphicsConfiguration() : null);
	}

	/**
	 * Constructor, creating a modeless dialog using the graphics configuration of the owner.
	 *
	 * @param owner Dialog owner
	 * @param plugin Contained plugin
	 * @param returnPageId Id of the {@link JaspiraPage} to add the plugin to after window closing.
	 * If this id is null, the plugin will be uninstalled if the dialog is closed.
	 */
	public PluginDialog(Frame owner, VisiblePlugin plugin, String returnPageId)
	{
		this(owner, plugin, returnPageId, false);
	}

	/**
	 * Constructor, using a dialog as owner.
	 *
	 * @param owner Dialog owner
	 * @param plugin Contained plugin
	 * @param returnPageId Id of the {@link JaspiraPage} to add the plugin to after window closing.
	 * If this id is null, the plugin will be uninstalled if the dialog is closed.
	 * @param modal
	 *		true	Display as modal dialog<br>
	 *		false	Display as modeless dialog
	 * @param gc Graphics configuration for dialog display
	 */
	public PluginDialog(Dialog owner, VisiblePlugin plugin, String returnPageId, boolean modal, GraphicsConfiguration gc)
	{
		super(owner, plugin.getTitle(), modal, gc);

		init(plugin, returnPageId);
	}

	/**
	 * Constructor, using the graphics configuration of the owner.
	 *
	 * @param owner Dialog owner
	 * @param plugin Contained plugin
	 * @param returnPageId Id of the {@link JaspiraPage} to add the plugin to after window closing.
	 * If this id is null, the plugin will be uninstalled if the dialog is closed.
	 * @param modal
	 *		true	Display as modal dialog<br>
	 *		false	Display as modeless dialog
	 */
	public PluginDialog(Dialog owner, VisiblePlugin plugin, String returnPageId, boolean modal)
	{
		this(owner, plugin, returnPageId, modal, owner.getGraphicsConfiguration());
	}

	/**
	 * Constructor, creating a modeless dialog using the graphics configuration of the owner.
	 *
	 * @param owner Dialog owner
	 * @param plugin Contained plugin
	 * @param returnPageId Id of the {@link JaspiraPage} to add the plugin to after window closing.
	 * If this id is null, the plugin will be uninstalled if the dialog is closed.
	 */
	public PluginDialog(Dialog owner, VisiblePlugin plugin, String returnPageId)
	{
		this(owner, plugin, returnPageId, false);
	}

	/**
	 * Constructor, creating a modeless dialog without owner using the default graphics configuration.
	 *
	 * @param plugin Contained plugin
	 * @param returnPageId Id of the {@link JaspiraPage} to add the plugin to after window closing.
	 * If this id is null, the plugin will be uninstalled if the dialog is closed.
	 */
	public PluginDialog(VisiblePlugin plugin, String returnPageId)
	{
		this((Frame) null, plugin, returnPageId, false, GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
	}

	/**
	 * Constructor, creating a modeless dialog without owner using the default graphics configuration.
	 *
	 * @param plugin Contained plugin
	 */
	public PluginDialog(VisiblePlugin plugin)
	{
		this((Frame) null, plugin, null, false, GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
	}

	/**
	 * Initializes the dialog.
	 *
	 * @param plugin Contained plugin
	 * @param returnPageId Id of the {@link JaspiraPage} to add the plugin to after window closing.
	 * If this id is null, the plugin will be uninstalled if the dialog is closed.
	 */
	protected void init(VisiblePlugin plugin, String returnPageId)
	{
		this.plugin = plugin;
		this.returnPageId = returnPageId;

		plugin.setPluginHolder(this);

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
	 * Closes the dialog and handles the plugin.
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

		if (focusedPlugin != null)
		{
			// Try to restore the focus to the stored plugin if it still exists
			VisiblePlugin p = (VisiblePlugin) focusedPlugin.get();

			if (p != null && p.getPluginHolder() != null)
			{
				// Force a re-focus of the plugin
				PluginFocusMgr.getInstance().resetFocusCache();
				p.focusPlugin();
			}

			// Kill the reference
			focusedPlugin = null;
		}
	}

	public void setVisible(boolean visible)
	{
		if (visible)
		{
			setLocationRelativeTo(getOwner());
		}
		super.setVisible(visible);
	}

	//////////////////////////////////////////////////
	// @@ PluginHolder implementation
	//////////////////////////////////////////////////

	/**
	 * Brings the plugin dialog to the front.
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
