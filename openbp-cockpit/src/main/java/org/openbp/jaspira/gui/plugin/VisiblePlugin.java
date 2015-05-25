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
import java.awt.datatransfer.Transferable;
import java.awt.event.FocusListener;
import java.util.List;

import javax.swing.JComponent;

import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.action.JaspiraToolbar;
import org.openbp.jaspira.event.InteractionEvent;
import org.openbp.jaspira.gui.interaction.InteractionClient;
import org.openbp.jaspira.plugin.Plugin;

/**
 * A visible plugin is a plugin that can be displayed in the user interface of an application.
 *
 * @author Stephan Moritz
 */
public interface VisiblePlugin
	extends Plugin
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Constants
	/////////////////////////////////////////////////////////////////////////

	/** Plugin toolbar type: The plugin doesn't have a toolbar */
	public static final int TOOLBAR_NONE = 0;

	/** Plugin toolbar type: The plugin toolbar contains entries for all its events accepting a {@link JaspiraActionEvent} */
	public static final int TOOLBAR_EVENTS = 1;

	/** Plugin toolbar type: The plugin toolbar will be build dynamically by broadcasting an {@link InteractionEvent} */
	public static final int TOOLBAR_DYNAMIC = 2;

	/** Plugin sizing behavior: Fixed size */
	public static final int SIZE_VARIABLE_NONE = 0;

	/** Plugin sizing behavior: Variable width */
	public static final int SIZE_VARIABLE_WIDTH = (1 << 0);

	/** Plugin sizing behavior: Variable height */
	public static final int SIZE_VARIABLE_HEIGHT = (1 << 1);

	/** Plugin sizing behavior: Variable width and height */
	public static final int SIZE_VARIABLE_BOTH = SIZE_VARIABLE_WIDTH | SIZE_VARIABLE_HEIGHT;

	/** Event name for global enviroment updates (i\. e\. toolbar and menu rebuilds) */
	public static final String GEU = "global.enviroment.update";

	/** Event name for global enviroment updates (i\. e\. toolbar and menu rebuilds) */
	public static final String GER = "global.enviroment.rebuild";

	//////////////////////////////////////////////////
	// @@ Installation
	//////////////////////////////////////////////////

	/**
	 * Called after the plugin has been displayed.
	 */
	public void pluginShown();

	/**
	 * Called after the plugin has been hidden.
	 */
	public void pluginHidden();

	//////////////////////////////////////////////////
	// @@ Plugin content
	//////////////////////////////////////////////////

	/**
	 * Gets the panel that holds the title bar of the plugin and the content panel.
	 * @nowarn
	 */
	public PluginPanel getPluginPanel();

	/**
	 * Gets the component this plugin contains.
	 *
	 * @return The content pane
	 */
	public JComponent getPluginComponent();

	/**
	 * Returns the focus component of this plugin, i\.e\. the component
	 * that is to initially receive the focus.
	 * @return The return value defaults to the first component below the content pane of the plugin.
	 * If this component is a scroll pane, the method returns the view component of the pane.
	 */
	public Component getPluginFocusComponent();

	/**
	 * Sets the holder of this plugin.
	 * @param holder The plugin holder
	 */
	public void setPluginHolder(PluginHolder holder);

	/**
	 * Returns the holder of this plugin.
	 * @return The plugin holder
	 */
	public PluginHolder getPluginHolder();

	/**
	 * Returns the page level parent plugin of this plugin.
	 * @return The page that holds this plugin or null
	 */
	public JaspiraPage getPage();

	/**
	 * Gets the type of the plugin toolbar.
	 * This will determine how the toolbar of the plugin will be constructed.
	 *
	 * @return {@link #TOOLBAR_NONE}/{@link #TOOLBAR_EVENTS}/{@link #TOOLBAR_DYNAMIC}<br>
	 * The default is TOOLBAR_EVENTS
	 */
	public int getToolbarType();

	/**
	 * Returns true if the plugin should have a close button in its title bar.
	 * Override this method to customize the plugin.
	 *
	 * @nowarn
	 */
	public boolean hasCloseButton();

	/**
	 * Returns the behavior of the plugin size.
	 * Override this method if you want to constrain the sizing of the plugin.
	 *
	 * @return {@link #SIZE_VARIABLE_NONE}/{@link #SIZE_VARIABLE_WIDTH}/{@link #SIZE_VARIABLE_HEIGHT}/{@link #SIZE_VARIABLE_BOTH}
	 */
	public int getSizeBehavior();

	/**
	 * Posts an update request of the plugin's container.
	 *
	 * @param fullRebuild
	 *		true	Causes a full container update including menu and toolbar rebuild<br>
	 *		false	Updates the container title only
	 */
	public void postPluginContainerUpdate(boolean fullRebuild);

	/**
	 * Forces the container of this plugin to rebuild its environment.
	 *
	 * @param fullRebuild
	 *		true	Causes a full container environment including menu and toolbar rebuild<br>
	 *		false	Updates the container title only
	 */
	public void updatePluginContainer(boolean fullRebuild);

	/**
	 * Creates the toolbar of this plugin.
	 * The toolbar will be constructed according to the return value of the {@link #getToolbarType} method.
	 * If you want to construct your own custom toolbar, override this method.
	 *
	 * @return The toolbar or null if the plugin does not have a toolbar
	 */
	public JaspiraToolbar createToolbar();

	//////////////////////////////////////////////////
	// @@ Visibility and focus
	//////////////////////////////////////////////////

	/**
	 * Brings the plugin to the front, but does not request the focus.
	 * @param changePage
	 *		true	Shows the {@link JaspiraPage} this container belongs to if it not the active page.<br>
	 *		false	Do not flip pages.
	 */
	public void showPlugin(boolean changePage);

	/**
	 * Checks if the plugin is currently visible.
	 * @nowarn
	 */
	public boolean isPluginVisible();

	/**
	 * Requests the focus for this plugin from the plugin manager and brings it to the front.
	 * The focus is being set to the focus component ({@link #getPluginFocusComponent}) of the plugin or
	 * to the plugin's component.
	 */
	public void focusPlugin();

	/**
	 * Checks if the plugin is currently focused.
	 * @nowarn
	 */
	public boolean isPluginFocused();

	/**
	 * Adds a focus listener to the plugin.
	 *
	 * @param listener Listener to add.<br>
	 * The listener will not be added if already present.
	 */
	public void addPluginFocusListener(FocusListener listener);

	/**
	 * Removes the focus listener from the plugin.
	 *
	 * @param listener Listener to remove
	 */
	public void removePluginFocusListener(FocusListener listener);

	/**
	 * Fires a 'focus gained event' to the focus listeners of the plugin.
	 */
	public void firePluginFocusGained();

	/**
	 * Fires a 'focus gained event' to the focus listeners of the plugin.
	 */
	public void firePluginFocusLost();

	//////////////////////////////////////////////////
	// @@ Dnd/clipboard support
	//////////////////////////////////////////////////

	/**
	 * Gets the sub clients of this drop client.
	 *
	 * @return A list of {@link InteractionClient} objects or null if this plugin doesn't have sub drop clients
	 */
	public List getSubClients();

	/**
	 * Checks if the plugin can be dragged.
	 * @nowarn
	 */
	public boolean canDrag();

	/**
	 * Checks if the plugin can copy the selected data to the clipboard.
	 *
	 * @return true	if the plugin can provide clipboard data.
	 */
	public boolean canCopy();

	/**
	 * Checks if the plugin can cut the selected data to the clipboard.
	 *
	 * @return true	if the plugin can provide clipboard data that can be deleted.
	 */
	public boolean canCut();

	/**
	 * Checks if the plugin can delete the selected data.
	 *
	 * @return If the data can be deleted.
	 */
	public boolean canDelete();

	/**
	 * Checks if the plugin can paste the data of the given transferable object.
	 *
	 * @return true	if the plugin accepts the given transferable.
	 */
	public boolean canPaste(Transferable transferable);

	/**
	 * Copies the selected data from the plugin to the clipboard.
	 *
	 * @return The copied data
	 */
	public Transferable copy();

	/**
	 * Cuts the selected data from the plugin to the clipboard.
	 *
	 * @return The copied data
	 */
	public Transferable cut();

	/**
	 * Pastes the given data into the plugin.
	 *
	 * @param transferable Content to paste
	 */
	public void paste(Transferable transferable);

	/**
	 * Deletes the selected data, if any.
	 */
	public void delete();
}
