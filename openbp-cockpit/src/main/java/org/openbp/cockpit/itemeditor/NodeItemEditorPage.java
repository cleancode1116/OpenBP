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
package org.openbp.cockpit.itemeditor;

import org.openbp.cockpit.plugins.itembrowser.NodeEditorItemBrowserPlugin;
import org.openbp.cockpit.plugins.toolbox.SocketToolBoxPlugin;
import org.openbp.core.model.item.process.Node;
import org.openbp.jaspira.action.keys.KeyMgr;
import org.openbp.jaspira.gui.plugin.ApplicationBase;
import org.openbp.jaspira.gui.plugin.JaspiraPage;
import org.openbp.jaspira.gui.plugin.PluginDivider;
import org.openbp.jaspira.gui.plugin.TabbedPluginContainer;
import org.openbp.jaspira.plugin.PluginMgr;
import org.openbp.jaspira.plugins.colorchooser.ColorChooserPlugin;
import org.openbp.jaspira.plugins.propertybrowser.PropertyBrowserPlugin;

/**
 * Jaspira page for displaying the node editor plugin.
 * This page is usually not used as a real page, it is rather placed inside an item editor Wizard.
 *
 * @author Stephan Moritz
 */
public class NodeItemEditorPage extends JaspiraPage
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** The plugin containing the property browser */
	private PropertyBrowserPlugin propertyBrowser;

	/** Item browser plugin */
	private NodeEditorItemBrowserPlugin itemBrowser;

	/** The node editor itself */
	private NodeItemEditorPlugin nodeEditor;

	/** Toolbox providing sockets */
	private SocketToolBoxPlugin socketToolBox;

	/** Color chooser */
	private ColorChooserPlugin colorChooser;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public NodeItemEditorPage()
	{
	}

	/**
	 * Gets the the node editor itself.
	 * @nowarn
	 */
	public NodeItemEditorPlugin getNodeEditor()
	{
		return nodeEditor;
	}

	public String getResourceCollectionContainerName()
	{
		return "plugin.cockpit";
	}

	/**
	 * @see org.openbp.jaspira.gui.plugin.JaspiraPage#layoutDefaultContent()
	 */
	public void layoutDefaultContent()
	{
		// We don't register the page as plugin, so use the application as parent plugin (the key manager requires a parent)
		this.setParentPlugin(ApplicationBase.getInstance());

		PluginMgr pm = PluginMgr.getInstance();
		propertyBrowser = (PropertyBrowserPlugin) pm.createInstance(PropertyBrowserPlugin.class, this);
		itemBrowser = (NodeEditorItemBrowserPlugin) pm.createInstance(NodeEditorItemBrowserPlugin.class, this);
		nodeEditor = (NodeItemEditorPlugin) pm.createInstance(NodeItemEditorPlugin.class, this);
		socketToolBox = (SocketToolBoxPlugin) pm.createInstance(SocketToolBoxPlugin.class, this);
		colorChooser = (ColorChooserPlugin) pm.createInstance(ColorChooserPlugin.class, this);
		colorChooser.setHelpText(getPluginResourceCollection().getOptionalString("chooserhelptext"));

		PluginDivider main = new PluginDivider(PluginDivider.HORIZONTAL_SPLIT);
		PluginDivider left = new PluginDivider(PluginDivider.VERTICAL_SPLIT);
		PluginDivider right = new PluginDivider(PluginDivider.VERTICAL_SPLIT);

		left.addPlugin(socketToolBox);
		left.addPlugin(itemBrowser);

		right.addPlugin(nodeEditor);

		TabbedPluginContainer bottomContainer = new TabbedPluginContainer();
		bottomContainer.addPlugin(propertyBrowser);
		bottomContainer.addPlugin(colorChooser);
		right.addClient(bottomContainer);

		main.addClient(left);
		main.addClient(right);

		left.setClientProportions(new double [] { 0.3d, 0.7d });

		right.setClientProportions(new double [] { 0.5d, 0.5d });

		main.setClientProportions(new double [] { 0.25d, 0.75d });

		setPluginDivider(main);

		// Register the page with the key manager in order to be able to handle key events
		KeyMgr.getInstance().install(getContentPane());
	}

	/**
	 * @see org.openbp.jaspira.plugin.AbstractPlugin#pluginUninstalled()
	 */
	protected void pluginUninstalled()
	{
		super.pluginUninstalled();

		PluginMgr pm = PluginMgr.getInstance();
		pm.removeInstance(propertyBrowser);
		pm.removeInstance(itemBrowser);
		pm.removeInstance(nodeEditor);
		pm.removeInstance(socketToolBox);
		pm.removeInstance(colorChooser);

		// Unregister the page from the key manager
		KeyMgr.getInstance().uninstall(getContentPane());
	}

	/**
	 * Saves the edited node.
	 */
	public void saveNode()
	{
		nodeEditor.saveNode();
	}

	/**
	 * Sets the node to edit.
	 *
	 * @param node The node
	 */
	public void setNode(Node node)
	{
		nodeEditor.setNode(node);
	}
}
