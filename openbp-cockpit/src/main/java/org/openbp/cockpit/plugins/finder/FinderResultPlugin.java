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
package org.openbp.cockpit.plugins.finder;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.openbp.cockpit.plugins.finder.treemodel.GenericModel;
import org.openbp.cockpit.plugins.finder.treemodel.LeafNode;
import org.openbp.common.rc.ResourceCollectionUtil;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.activity.ActivityItem;
import org.openbp.core.model.item.activity.ActivityParam;
import org.openbp.core.model.item.activity.ActivitySocket;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.type.DataMember;
import org.openbp.core.model.item.type.DataTypeItem;
import org.openbp.guiclient.event.OpenEvent;
import org.openbp.guiclient.event.QualifierEvent;
import org.openbp.guiclient.model.item.itemfinder.FinderEngine;
import org.openbp.guiclient.model.item.itemfinder.FinderEngineImpl;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.action.JaspiraPopupMenu;
import org.openbp.jaspira.event.InteractionEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.gui.interaction.BasicTransferable;
import org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin;
import org.openbp.jaspira.plugin.ApplicationUtil;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.jaspira.plugin.InteractionModule;
import org.openbp.swing.components.tree.TreeExpander;
import org.openbp.swing.plaf.sky.SkyTheme;

/**
 * This plugin show the results of the finder in the modeler. If the
 * modeler is not open, it will be opened.
 *
 * @author Baumgartner Michael
 */
public class FinderResultPlugin extends AbstractVisiblePlugin
{
	//////////////////////////////////////////////////
	// @@ Member
	//////////////////////////////////////////////////

	/** The strategy used for the model. */
	private RefStrategy strategy;

	/** Label that displays the number of hits */
	private JLabel label;

	/** The intelligent expander of the tree. */
	private TreeExpander treeExpander;

	/** The treetable itself. */
	protected JTree tree;

	/** The model for the treetable. */
	protected GenericModel model;

	/** The model object that was used for searching. */
	protected ModelObject referenceObject;

	/** The list with the found references. */
	protected List referenceList;

	/** Top panel. */
	protected JPanel topPanel;

	/** Action to clear the list. */
	protected JaspiraAction clearListAction;

	/** Action to refresh the list. */
	protected JaspiraAction refreshAction;

	/** Finder result icon */
	static ImageIcon finderResultIcon;

	//////////////////////////////////////////////////
	// @@ Constructor
	//////////////////////////////////////////////////

	/**
	 * Constructor to load the icon for the results.
	 */
	public FinderResultPlugin()
	{
		super();

		finderResultIcon = (ImageIcon) getPluginResourceCollection().getOptionalObject(("icon.finder.result"));
	}

	/**
	 * @copy org.openbp.jaspira.plugin.AbstractPlugin.getResourceCollectionContainerName()
	 */
	public String getResourceCollectionContainerName()
	{
		return "plugin.finder";
	}

	/**
	 * @copy org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin.initializeComponents()
	 */
	protected void initializeComponents()
	{
		// Load the actions
		clearListAction = getAction("plugin.finderresult.delete");
		if (clearListAction != null)
		{
			clearListAction.setEnabled(false);
		}
		refreshAction = getAction("plugin.finderresult.refresh");
		if (refreshAction != null)
		{
			refreshAction.setEnabled(false);
		}

		// Initialize the tree
		strategy = new RefStrategy();
		model = new GenericModel(strategy, true);
		tree = new JTree();
		treeExpander = new TreeExpander(tree);
		tree.setToggleClickCount(2);
		tree.setRootVisible(false);
		tree.setModel(model);
		tree.setCellRenderer(new FinderResultRenderer());
		tree.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON3)
				{
					showPopup(e.getPoint());
				}
				else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
					focus(e.getPoint());
			}
		});

		// Create the label
		label = new JLabel();

		// Create the top panel with the label and the reference object
		topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.setVisible(false);
		topPanel.add(label);
		topPanel.setBackground(SkyTheme.COLOR_BACKGROUND_LIGHT);
		topPanel.setBorder(new EmptyBorder(1, 3, 4, 4));

		// Add the components to the plugin
		getContentPane().add(topPanel, BorderLayout.NORTH);
		getContentPane().add(new JScrollPane(tree), BorderLayout.CENTER);
	}

	/**
	 * Shows the popup menu for the given item.
	 *
	 * @param pos Position of the upper left corner of the popup
	 */
	protected void showPopup(final Point pos)
	{
		int selectedRow = tree.getClosestRowForLocation(pos.x, pos.y);
		TreePath path = tree.getPathForRow(selectedRow);
		if (path == null)
			return;

		TreeNode node = (TreeNode) path.getLastPathComponent();
		if (!node.isLeaf())
			return;

		// Turn on the wait cursor
		ApplicationUtil.waitCursorOn();

		int row = tree.getRowForPath(path);
		tree.setSelectionRow(row);

		InteractionEvent iae = null;
		try
		{
			// Broadcast an interaction event to collect the popup menu entries to display
			iae = new InteractionEvent(this, InteractionEvent.POPUP, new BasicTransferable(((LeafNode) node).getLeafData()));
			fireEvent(iae);
		}
		finally
		{
			// Reset the wait cursor
			ApplicationUtil.waitCursorOff();
		}

		// Create and display the menu
		JaspiraPopupMenu menu = iae.createPopupMenu();

		// When showing the menu directly from here, the portion of the menu that overlaps the current
		// tree row gets painted, but the remainder of the menu will be painted after the tree view
		// was refreshed. In order to prevent this, we dealy the menu display until the update events
		// have been processed.
		if (menu != null)
		{
			menu.show(tree, pos.x, pos.y);
		}
	}

	/**
	 * @copy org.openbp.jaspira.gui.plugin.VisiblePlugin.getToolbarType()
	 */
	public int getToolbarType()
	{
		return TOOLBAR_DYNAMIC;
	}

	/**
	 * Update the model to display the new found references.
	 */
	void updateModel()
	{
		// Update the top panel
		topPanel.setVisible(true);

		Integer n = Integer.valueOf(referenceList != null ? referenceList.size() : 0);
		String text = ResourceCollectionUtil.formatMsg(getPluginResourceCollection(), "search.hits", n, referenceObject.getName());
		String toolTipText = ResourceCollectionUtil.formatMsg(getPluginResourceCollection(), "search.hits", n, referenceObject.getQualifier());
		label.setText(text);
		label.setToolTipText(toolTipText);

		// Update the model
		if (referenceList != null)
		{
			if (referenceObject instanceof Item)
				strategy.setReferenceItemType(((Item) referenceObject).getItemType());
			else
				strategy.setReferenceItemType(referenceObject.getQualifier().getItemType());

			model.reload(referenceList.iterator());
			treeExpander.simpleExpand(2);
		}
		else
		{
			model.clearModel();
		}
	}

	/**
	 * Focus the modeler to the component, that was double clicked in the
	 * tree. Either the modeler shows the node/socket/parameter or the
	 * component editor opens the component or the model managers focuses
	 * on the datatype
	 * @param pos The position of the mouse click
	 */
	void focus(Point pos)
	{
		int selectedRow = tree.getClosestRowForLocation(pos.x, pos.y);
		if (selectedRow == -1)
			return;

		TreePath path = tree.getPathForRow(selectedRow);
		TreeNode node = (TreeNode) path.getLastPathComponent();
		if (node instanceof LeafNode)
		{
			ModelObject mo = (ModelObject) ((LeafNode) node).getLeafData();
			ModelQualifier qualifier = mo.getQualifier();

			if (qualifier.getItemType().equals(ItemTypes.PROCESS))
			{
				// Open the process of select the node/socket/parameter
				ProcessItem process = getProcess(mo);
				OpenEvent oEvent = new OpenEvent(this, "open.modeler", process);
				fireEvent(oEvent);

				// The event must be fired twice, because the first only deselect the current selection.
				// The second select the component.
				QualifierEvent jEvent = new QualifierEvent(this, "modeler.view.select", qualifier);
				fireEvent(jEvent);
				fireEvent(jEvent);
			}
			else if (qualifier.getItemType().equals(ItemTypes.ACTIVITY) || qualifier.getItemType().equals(ItemTypes.WEBSERVICE) || qualifier.getItemType().equals(ItemTypes.VISUAL) || qualifier.getItemType().equals(ItemTypes.ACTOR))
			{
				// Open the activity in the component editor
				ActivityItem activity = getActivity(mo);
				OpenEvent event = new OpenEvent(this, "global.edit.edit", activity);
				fireEvent(event);
			}
			else if (qualifier.getItemType().equals(ItemTypes.TYPE))
			{
				// Open the datatype in the model manager
				DataTypeItem data = getDataType(mo);
				OpenEvent event = new OpenEvent(this, "plugin.itembrowser.open", data);
				fireEvent(event);
			}
			else if (qualifier.getItemType().equals(ItemTypes.MODEL))
			{
				// Open the datatype in the model manager
				OpenEvent event = new OpenEvent(this, "plugin.itembrowser.open", mo);
				fireEvent(event);
			}
		}
	}

	/**
	 * Get the datatype of the model object. If the model object
	 * is a member then the parent datatype is returned.
	 * @param mo The found reference
	 * @return the Datatype to focus on
	 */
	private DataTypeItem getDataType(ModelObject mo)
	{
		if (mo instanceof DataMember)
			return ((DataMember) mo).getParentDataType();
		else if (mo instanceof DataTypeItem)
			return (DataTypeItem) mo;
		return null;
	}

	/**
	 * Get the process of the model object.
	 * @param mo The found reference
	 * @return the process containing the model object
	 */
	private ProcessItem getProcess(ModelObject mo)
	{
		if (mo instanceof NodeParam)
			return ((NodeParam) mo).getSocket().getNode().getProcess();
		else if (mo instanceof NodeSocket)
			return ((NodeSocket) mo).getNode().getProcess();
		else if (mo instanceof Node)
			return ((Node) mo).getProcess();
		return null;
	}

	/**
	 * Get the activity of the model object.
	 * @param mo The found reference
	 * @return The activity containing the model object
	 */
	private ActivityItem getActivity(ModelObject mo)
	{
		if (mo instanceof ActivityParam)
			return ((ActivityParam) mo).getSocket().getActivity();
		else if (mo instanceof ActivitySocket)
			return ((ActivitySocket) mo).getActivity();
		else if (mo instanceof ActivityItem)
			return (ActivityItem) mo;
		return null;
	}

	//////////////////////////////////////////////////
	// @@ Interaction module
	//////////////////////////////////////////////////

	/**
	 * Interaction module.
	 */
	public class Interaction extends InteractionModule
	{
		/**
		 * Get the module priority.
		 * We are super-low priority.
		 *
		 * @return The priority. 0 is lowest, 100 is highest.
		 */
		public int getPriority()
		{
			return 0;
		}

		/**
		 * @copy org.openbp.jaspira.plugin.InteractionModule.popup
		 */
		public JaspiraEventHandlerCode popup(InteractionEvent ie)
		{
			if (!(ie.getSourcePlugin() instanceof FinderResultPlugin))
				return EVENT_IGNORED;

			// Create the group to manage the results
			JaspiraAction group = new JaspiraAction("popup.manage", null, null, null, null, 10, JaspiraAction.TYPE_GROUP);
			if (refreshAction != null)
			{
				group.addMenuChild(refreshAction);
			}
			if (clearListAction != null)
			{
				group.addMenuChild(clearListAction);
			}
			ie.add(group);
			return EVENT_CONSUMED;
		}

		/**
		 * @copy org.openbp.jaspira.plugin.InteractionModule.toolbar
		 */
		public JaspiraEventHandlerCode toolbar(InteractionEvent ie)
		{
			if (!(ie.getSourcePlugin() instanceof FinderResultPlugin))
				return EVENT_IGNORED;

			// Create the group to manage the results
			JaspiraAction group = new JaspiraAction("popup.manage.finder", null, null, null, null, 10, JaspiraAction.TYPE_GROUP);
			if (refreshAction != null)
			{
				group.addToolbarChild(refreshAction);
			}
			if (clearListAction != null)
			{
				group.addToolbarChild(clearListAction);
			}
			ie.add(group);
			return EVENT_CONSUMED;
		}
	}

	//////////////////////////////////////////////////
	// @@ Event module
	//////////////////////////////////////////////////

	/**
	 * Event module for the auditor result plugin.
	 */
	public class Event extends EventModule
	{
		/**
		 * @copy org.openbp.jaspira.plugin.EventModule.getName()
		 */
		public String getName()
		{
			return "plugin.finderresult";
		}

		/**
		 * Event handler: Event to start a new reference search.
		 *
		 * @event plugin.finderresult.present
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode present(JaspiraEvent je)
		{
			// Find the references and display them in the tree
			referenceObject = (ModelObject) je.getObject();
			FinderEngine finderEngine = new FinderEngineImpl();
			referenceList = finderEngine.createReferenceList(referenceObject, null);
			updateModel();

			// Update the refresh button
			refreshAction.setEnabled(true);
			clearListAction.setEnabled(referenceList != null);
			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Removes all entries from the list.
		 *
		 * @event plugin.finderresult.delete
		 * @param jae Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode delete(JaspiraActionEvent jae)
		{
			model.clearModel();
			topPanel.setVisible(false);
			clearListAction.setEnabled(false);
			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Refresh the current reference list
		 *
		 * @event plugin.finderresult.refresh
		 * @param jae Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode refresh(JaspiraActionEvent jae)
		{
			FinderEngine finderEngine = new FinderEngineImpl();
			referenceList = finderEngine.createReferenceList(referenceObject, null);
			updateModel();

			clearListAction.setEnabled(referenceList != null);
			return EVENT_CONSUMED;
		}

		public JaspiraEventHandlerCode displayobject_changed_titlemode(JaspiraEvent je)
		{
			tree.repaint();
			return EVENT_HANDLED;
		}
	}
}
