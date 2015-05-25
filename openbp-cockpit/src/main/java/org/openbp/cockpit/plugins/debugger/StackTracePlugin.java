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
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.openbp.common.MsgFormat;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.core.OpenBPException;
import org.openbp.core.engine.debugger.CallStackInfo;
import org.openbp.core.engine.debugger.DebuggerService;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.guiclient.event.OpenEvent;
import org.openbp.guiclient.event.QualifierEvent;
import org.openbp.guiclient.remote.ServerConnection;
import org.openbp.jaspira.event.AskEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin;
import org.openbp.jaspira.plugin.EventModule;

/**
 * This plugin implements a token context inspector.
 * It is meant to be used together with the SimpleDebugger plugin.
 *
 * @author Heiko Erhardt
 */
public class StackTracePlugin extends AbstractVisiblePlugin
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** The debugger client id under which we are connected to the debugger service */
	private String debuggerId;

	/** Current position */
	private ModelQualifier currentPos;

	/** Tree table model */
	private StackTraceTableModel tableModel;

	/** Used to represent the model */
	private JTable table;

	/** Table header */
	private static Vector tableHeaders;

	/** Title status */
	private static String titleStatus;

	/** Is the debugger listening to the debugger service? */
	private boolean debuggerListening;

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
		if (tableHeaders == null)
		{
			// Initialize static resources
			ResourceCollection res = getPluginResourceCollection();

			tableHeaders = new Vector();
			tableHeaders.add(res.getRequiredString("header.column1"));
			tableHeaders.add(res.getRequiredString("header.column2"));

			titleStatus = res.getRequiredString("titlestatus");
		}

		// Create the table
		tableModel = new StackTraceTableModel();

		table = new JTable(tableModel);
		table.setRowHeight(20);

		table.addMouseListener(new MouseAdapter()
		{
			// Double-clicking a row of the table means displaying the position of this stack element
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					Point p = e.getPoint();
					int row = table.rowAtPoint(p);

					String pos = (String) tableModel.getValueAt(row, 1);
					if (pos == null)
						return;

					ModelQualifier qualifier = new ModelQualifier(pos);
					qualifier.setItemType(ItemTypes.PROCESS);

					// First, make sure the process the new current position refers to is loaded
					ModelQualifier processQualifier = new ModelQualifier(qualifier);
					processQualifier.setObjectPath(null);
					StackTracePlugin.this.fireEvent(new OpenEvent(StackTracePlugin.this, "open.modeler", processQualifier));

					// Now select the current position
					QualifierEvent event = new QualifierEvent(StackTracePlugin.this, "modeler.view.select", qualifier);
					StackTracePlugin.this.fireEvent(event);
					StackTracePlugin.this.fireEvent(event);
				}
			}
		});

		getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
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

		determineDebuggerId();

		List callStackElements = null;
		try
		{
			callStackElements = debuggerService.getCallStackElements(debuggerId);
		}
		catch (OpenBPException e)
		{
			// Ignore
		}
		if (callStackElements == null)
			callStackElements = new ArrayList();

		// Initialize the tree model with the current server values
		tableModel.setCallStackElements(callStackElements);
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
			currentPos = dse.getHaltedPosition();
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
			currentPos = dse.getHaltedPosition();
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
			tableModel.setCallStackElements(null);
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

			// Clear the tree
			tableModel.setCallStackElements(null);

			return EVENT_HANDLED;
		}
	}

	public class StackTraceTableModel extends DefaultTableModel
	{
		public StackTraceTableModel()
		{
			super();
		}

		/**
		 * Sets call stack elements.
		 *
		 * @param callStackElements A list of {@link CallStackInfo} objects
		 */
		public void setCallStackElements(List callStackElements)
		{
			Vector rows = new Vector();

			if (callStackElements != null)
			{
				Vector row;

				// Insert call stack elements in reverse order
				for (Iterator it = callStackElements.iterator(); it.hasNext();)
				{
					CallStackInfo callStackInfo = (CallStackInfo) it.next();
					row = new Vector();

					row.add(callStackInfo.getSavedPosition());

					rows.add(0, row);
				}

				// Insert current position at start of vector
				row = new Vector();
				row.add(currentPos.toString());
				rows.add(0, row);
			}

			setDataVector(rows, tableHeaders);

			TableColumnModel columnModel = table.getColumnModel();
			try
			{
				TableColumn column = columnModel.getColumn(0);
				if (column != null)
				{
					column.setPreferredWidth(100);
					column.setMaxWidth(100);
				}
			}
			catch (ArrayIndexOutOfBoundsException e)
			{
				// Ignore, happens sometimes due to threading issues
			}
		}

		/**
		 * @see javax.swing.table.TableModel#isCellEditable(int, int)
		 */
		public boolean isCellEditable(int arg0, int arg1)
		{
			return false;
		}
	}
}
