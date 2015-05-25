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
package org.openbp.cockpit.plugins.debugger;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.openbp.common.CommonUtil;
import org.openbp.common.MsgFormat;
import org.openbp.common.icon.FlexibleSize;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.string.TextUtil;
import org.openbp.core.engine.debugger.DebuggerService;
import org.openbp.core.engine.debugger.ObjectMemberInfo;
import org.openbp.guiclient.model.item.ItemIconMgr;
import org.openbp.guiclient.remote.ServerConnection;
import org.openbp.jaspira.event.AskEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.swing.components.treetable.DefaultTreeCellRenderer;
import org.openbp.swing.components.treetable.DefaultTreeTableModel;
import org.openbp.swing.components.treetable.JTreeTable;

/**
 * This plugin implements a token context inspector.
 * It is meant to be used together with the SimpleDebugger plugin.
 *
 * @author Heiko Erhardt
 */
public class InspectorPlugin extends AbstractVisiblePlugin
	implements TreeExpansionListener, TreeSelectionListener
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** The debugger client id under which we are connected to the debugger service */
	private String debuggerId;

	/** Tree table model */
	private DefaultTreeTableModel treeModel;

	/** Used to represent the model */
	private JTreeTable treeTable;

	/** Table header */
	private static String [] tableHeader;

	/** Title status */
	private static String titleStatus;

	/** Default icon */
	private Icon defaultIcon;

	/** Is the debugger listening to the debugger service? */
	private boolean debuggerListening;

	/** List containing {@link InspectorPlugin.NodeSpec} objects specifying the currently expanded nodes */
	private List expandedNodeSpecList;

	/** Node specification specifying the currently selected node */
	private NodeSpec selectedNodeSpec;

	/** Flag if the state of the tree has been saved */
	private boolean stateSaved;

	public String getResourceCollectionContainerName()
	{
		return "plugin.debugger";
	}

	//////////////////////////////////////////////////
	// @@ Init/Activate
	//////////////////////////////////////////////////

	/**
	 * @copy org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin.initializeComponents
	 */
	protected void initializeComponents()
	{
		expandedNodeSpecList = new ArrayList();

		defaultIcon = getIcon();

		treeModel = new DefaultTreeTableModel(null);

		ResourceCollection res = getPluginResourceCollection();
		if (tableHeader == null)
		{
			tableHeader = new String [] { res.getRequiredString("header.column1"), res.getRequiredString("header.column2"), res.getRequiredString("header.column3") };

			titleStatus = res.getRequiredString("titlestatus");
		}
		treeModel.setColumnHeader(tableHeader);

		treeTable = new JTreeTable(treeModel);
		treeTable.setDefaultRowHeight(24);

		treeTable.getTree().setCellRenderer(new InspectorTreeCellRenderer(treeTable));
		treeTable.getTree().addTreeExpansionListener(this);
		treeTable.getTree().addTreeSelectionListener(this);

		treeTable.setRootVisible(false);

		getContentPane().add(new JScrollPane(treeTable), BorderLayout.CENTER);
	}

	/**
	 * Refreshes the inspector view.
	 */
	protected void refresh()
	{
		DebuggerService debuggerService = (DebuggerService) ServerConnection.getInstance().lookupOptionalService(DebuggerService.class);
		if (debuggerService == null)
		{
			return;
		}

		// Save the expanded node state
		saveState();

		determineDebuggerId();

		// Initialize the tree model with the current server values
		treeModel.setRoot(new InspectorNode(debuggerId, debuggerService));

		// treeTable.sizeRowsToFit ();
		// treeTable.sizeColumnsToFit ();

		// Restore the expanded node state
		restoreState();
	}

	/**
	 * Determines the debugger id from the debugger if not know yet.
	 */
	protected void determineDebuggerId()
	{
		if (debuggerId == null)
		{
			// Get the debugger client id we need to use from the debugger plugin
			AskEvent ae = new AskEvent(this, "debugger.client.getdebuggerid");
			fireEvent(ae);
			debuggerId = (String) ae.getAnswer();
			if (debuggerId == null)
			{
				System.err.println("Debugger id could not be requested from debugger plugin");
				return;
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ AbstractPlugin overrides
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.plugin.AbstractPlugin#getTitle()
	 */
	public String getTitle()
	{
		if (!debuggerListening)
		{
			return super.getTitle();
		}

		// Display the debugger client id in the title bar if the debugger is running
		determineDebuggerId();
		return super.getTitle() + " " + MsgFormat.format(titleStatus, debuggerId);
	}

	/**
	 * @see org.openbp.jaspira.plugin.AbstractPlugin#getSubTitle()
	 */
	public String getSubTitle()
	{
		return super.getTitle();
	}

	//////////////////////////////////////////////////
	// @@ Tree state management
	//////////////////////////////////////////////////

	/**
	 * Saves the state of all expanded nodes.
	 */
	void saveState()
	{
		if (stateSaved)
		{
			// Already saved
			return;
		}
		stateSaved = true;

		JTree tree = treeTable.getTree();

		// Save all expanded nodes
		expandedNodeSpecList.clear();

		int nRows = tree.getRowCount();
		for (int iRow = 0; iRow < nRows; iRow++)
		{
			TreePath path = tree.getPathForRow(iRow);
			if (tree.isExpanded(path))
			{
				Object o = path.getLastPathComponent();
				if (!(o instanceof InspectorNode))
				{
					// Necessary to prevent a CCE from the default contents of the DefaultMutableTreeModel
					continue;
				}

				InspectorNode node = (InspectorNode) o;
				expandedNodeSpecList.add(new NodeSpec(node));
			}
		}

		// Determine the currently selected node
		selectedNodeSpec = null;
		TreePath [] paths = treeTable.getSelection();
		if (paths != null && paths.length > 0)
		{
			if (paths [0] != null)
			{
				Object o = paths [0].getLastPathComponent();
				if (o instanceof InspectorNode)
				{
					// Necessary to prevent a CCE from the default contents of the DefaultMutableTreeModel
					InspectorNode node = (InspectorNode) o;
					selectedNodeSpec = new NodeSpec(node);
				}
			}
		}
	}

	/**
	 * Restores the state of the saved expanded nodes.
	 */
	private void restoreState()
	{
		JTree tree = treeTable.getTree();

		// Restore the expansion state
		int n = expandedNodeSpecList.size();
		for (int i = 0; i < n; ++i)
		{
			NodeSpec nodeSpec = (NodeSpec) expandedNodeSpecList.get(i);

			InspectorNode node = findNode(nodeSpec);
			if (node != null)
			{
				TreeNode [] nodes = ((DefaultTreeTableModel) tree.getModel()).getPathToRoot(node);
				TreePath path = new TreePath(nodes);
				treeTable.expandPath(path);
			}
		}

		// Restore the selection state
		if (selectedNodeSpec != null)
		{
			InspectorNode node = findNode(selectedNodeSpec);
			if (node != null)
			{
				treeTable.selectNode(node);
			}
		}

		// Reset state info
		stateSaved = false;
		expandedNodeSpecList.clear();
		selectedNodeSpec = null;
	}

	/**
	 * Finds a node given the node info.
	 *
	 * @param nodeSpec Node specification object
	 * @return The node or null if no node matches the specification
	 */
	private InspectorNode findNode(NodeSpec nodeSpec)
	{
		JTree tree = treeTable.getTree();

		int nRows = tree.getRowCount();
		for (int iRow = 0; iRow < nRows; iRow++)
		{
			TreePath path = tree.getPathForRow(iRow);

			Object o = path.getLastPathComponent();
			if (nodeSpec.matches(o))
				return (InspectorNode) o;
		}

		return null;
	}

	//////////////////////////////////////////////////
	// @@ Debugger server communication module
	//////////////////////////////////////////////////

	/**
	 * Debugger server event module.
	 * Attention:<br>
	 * The names of the follow event methods need to correspond to the event names defined in
	 * the EngineTraceEvent class.
	 * These server events will be mapped automatically to the corresponding client events
	 * by the {@link DebuggerServerEvent} class.
	 */
	public class ServerEvents extends EventModule
	{
		public String getName()
		{
			return "debugger.server";
		}

		//////////////////////////////////////////////////
		// @@ EventHandling
		//////////////////////////////////////////////////

		/**
		 * Event handler: Nodeentrys.
		 *
		 * @event debugger.server.nodeentry
		 * @param dse Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode nodeentry(DebuggerServerEvent dse)
		{
			refresh();
			return EVENT_HANDLED;
		}

		/**
		 * Event handler: Nodeexits.
		 *
		 * @event debugger.server.nodeexit
		 * @param dse Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode nodeexit(DebuggerServerEvent dse)
		{
			refresh();
			return EVENT_HANDLED;
		}

		/**
		 * Event handler: Dataflows.
		 *
		 * @event debugger.server.dataflow
		 * @param dse Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode dataflow(DebuggerServerEvent dse)
		{
			refresh();
			return EVENT_HANDLED;
		}

		/**
		 * Event handler: Clears the context inspector window.
		 *
		 * @event debugger.client.clearstatuswindows
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode debugger_client_clearstatuswindows(JaspiraEvent je)
		{
			// Save the expanded node state
			saveState();

			treeModel.setRoot(null);
			return EVENT_HANDLED;
		}

		/**
		 * Event handler: Change the title bar of the plugin to indicate that the debugger is
		 * listening or has stopped.
		 *
		 * @event debugger.client.statuschange
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode debugger_client_statuschange(JaspiraEvent je)
		{
			// Remember if the debugger is currently active
			String clientId = (String) je.getObject();
			debuggerListening = clientId != null;

			// Update the title bar
			postPluginContainerUpdate(false);

			// Save the expanded node state
			saveState();

			// Clear the tree
			treeModel.setRoot(null);

			return EVENT_HANDLED;
		}
	}

	//////////////////////////////////////////////////
	// @@ TreeExpansionListener and TreeSelectionListener implementation
	//////////////////////////////////////////////////

	/** 
	 * Called whenever the value of the selection changes.
	 * @param event the event that characterizes the change.
	 */
	public void valueChanged(TreeSelectionEvent event)
	{
		TreePath path = event.getNewLeadSelectionPath();

		if (path != null)
		{
			InspectorNode node = (InspectorNode) treeTable.getNodeByPath(path);
			if (node != null)
			{
				node.loadValueDetails();

				ObjectMemberInfo info = node.getInfo();
				String value = info.getToStringValue();
				if (value != null)
				{
					String title = info.getKey() + " (" + info.getType() + ")";
					String text = TextUtil.convertToHTML(new String [] { title, value }, true, -1, -1);
					fireEvent(new JaspiraEvent(this, "plugin.infopanel.setinfotext", text));
				}
				else
				{
					fireEvent(new JaspiraEvent(this, "plugin.infopanel.clearinfotext"));
				}
			}
		}
	}

	/**
	 * @see javax.swing.event.TreeExpansionListener#treeExpanded(javax.swing.event.TreeExpansionEvent)
	 */
	public void treeExpanded(TreeExpansionEvent event)
	{
		TreePath path = event.getPath();

		if (path != null)
		{
			InspectorNode node = (InspectorNode) treeTable.getNodeByPath(path);
			if (node != null)
			{
				node.loadChildren();

				treeModel.fireNodeStructureChanged(node);
			}
		}
	}

	/**
	 * @see javax.swing.event.TreeExpansionListener#treeCollapsed(javax.swing.event.TreeExpansionEvent)
	 */
	public void treeCollapsed(TreeExpansionEvent event)
	{
	}

	//////////////////////////////////////////////////
	// @@ Default tree cell renderer overrides
	//////////////////////////////////////////////////

	/**
	 * Overridden default tree cell renderer to define the icons.
	 */
	private class InspectorTreeCellRenderer extends DefaultTreeCellRenderer
	{
		//////////////////////////////////////////////////
		// @@ Construction
		//////////////////////////////////////////////////

		/**
		 * Constructor.
		 *
		 * @param treeTable The tree table
		 */
		private InspectorTreeCellRenderer(JTreeTable treeTable)
		{
			super(treeTable);
		}

		//////////////////////////////////////////////////
		// @@ Default tree cell renderer overridden methods
		//////////////////////////////////////////////////

		/**
		 * Overridden method to define the icons of a tree node.
		 * @nowarn
		 */
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			if (c instanceof JLabel)
			{
				// We display the icon according to the type of the object represented by the node.
				Icon icon = null;

				// Get the node by the row number
				TreePath path = tree.getPathForRow(row);
				if (path != null)
				{
					InspectorNode node = (InspectorNode) path.getLastPathComponent();

					// Determine the type
					ObjectMemberInfo info = node.getInfo();
					if (info != null)
					{
						String iconName;

						String type = info.getType();
						if (type != null)
						{
							// Hack: Complex types look like "/Bugscape/UserInfo (compiled)"
							// We search for the space to determine if it is one
							int index = type.indexOf(' ');
							if (index > 0)
							{
								iconName = "TypeBean";
							}
							else
							{
								if (type.equals("Byte") || type.equals("Double") || type.equals("Float") || type.equals("Integer") || type.equals("Long") || type.equals("Short"))
								{
									type = "Numeric";
								}

								iconName = "Type" + type;
							}
						}
						else
						{
							iconName = "TypeObject";
						}

						icon = ItemIconMgr.getInstance().getIcon(iconName, FlexibleSize.SMALL);
					}
				}

				if (icon == null)
				{
					icon = defaultIcon;
				}

				((JLabel) c).setIcon(icon);
			}

			return c;
		}
	}

	//////////////////////////////////////////////////
	// @@ Helper class for saving/restoring the tree state
	//////////////////////////////////////////////////

	/**
	 * Helper class for saving/restoring the tree state.
	 */
	private static class NodeSpec
	{
		/** Context path */
		private String contextPath;

		/** Expression */
		private String expression;

		/**
		 * Constructor.
		 *
		 * @param node Node the state should be saved from
		 */
		public NodeSpec(InspectorNode node)
		{
			contextPath = node.getContextPath();
			expression = node.getExpression();
		}

		/**
		 * Checks if the given (node) object matches the specification.
		 *
		 * @param o Event
		 * @return
		 * true: The match is achieved if both the context path and the expression match.<br>
		 * false: No match
		 */
		public boolean matches(Object o)
		{
			if (o instanceof InspectorNode)
			{
				InspectorNode node = (InspectorNode) o;

				if (CommonUtil.equalsNull(contextPath, node.getContextPath()) && CommonUtil.equalsNull(expression, node.getExpression()))
				{
					return true;
				}
			}

			return false;
		}
	}
}
