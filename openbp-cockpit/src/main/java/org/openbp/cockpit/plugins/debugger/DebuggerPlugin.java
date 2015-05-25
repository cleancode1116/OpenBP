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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

import org.openbp.cockpit.Cockpit;
import org.openbp.cockpit.itemeditor.NodeItemEditorPlugin;
import org.openbp.cockpit.modeler.Modeler;
import org.openbp.cockpit.modeler.ModelerColors;
import org.openbp.cockpit.modeler.ModelerGraphics;
import org.openbp.cockpit.modeler.ViewModeMgr;
import org.openbp.cockpit.modeler.figures.generic.BasicFigure;
import org.openbp.cockpit.modeler.figures.generic.XFigure;
import org.openbp.cockpit.modeler.figures.process.FlowConnection;
import org.openbp.cockpit.modeler.figures.process.NodeFigure;
import org.openbp.cockpit.modeler.figures.process.ProcessElementContainer;
import org.openbp.cockpit.modeler.figures.process.SocketFigure;
import org.openbp.cockpit.modeler.figures.spline.PolySplineConnection;
import org.openbp.cockpit.modeler.figures.tag.AbstractTagFigure;
import org.openbp.cockpit.modeler.util.FigureResources;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.setting.SettingUtil;
import org.openbp.core.OpenBPException;
import org.openbp.core.engine.debugger.DebuggerEvent;
import org.openbp.core.engine.debugger.DebuggerService;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.ControlLink;
import org.openbp.core.model.item.process.InitialNode;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.SingleSocketNode;
import org.openbp.guiclient.event.OpenEvent;
import org.openbp.guiclient.event.QualifierEvent;
import org.openbp.guiclient.remote.ServerConnection;
import org.openbp.guiclient.util.ClientFlavors;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.decoration.DecorationMgr;
import org.openbp.jaspira.decoration.Decorator;
import org.openbp.jaspira.decoration.FilteredDecorator;
import org.openbp.jaspira.decoration.ListDecorator;
import org.openbp.jaspira.event.AskEvent;
import org.openbp.jaspira.event.InteractionEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.option.Option;
import org.openbp.jaspira.option.OptionMgr;
import org.openbp.jaspira.plugin.AbstractPlugin;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.jaspira.plugin.InteractionModule;
import org.openbp.jaspira.plugins.errordialog.ErrorEvent;
import org.openbp.swing.components.JMsgBox;

import CH.ifa.draw.framework.Figure;

/**
 * Simple Plugin that handles debug communication with the OpenBP server.
 *
 * @author Stephan Moritz
 */
public class DebuggerPlugin extends AbstractPlugin
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Link trace flag: Skip link */
	public static final Integer LINKTRACE_SKIP = Integer.valueOf(0);

	/** Link trace flag: Show link target only */
	public static final Integer LINKTRACE_TARGET = Integer.valueOf(1);

	/** Link trace flag: Show link animation and stop at target */
	public static final Integer LINKTRACE_ANIMATION_STOP = Integer.valueOf(2);

	/** Link trace flag: Show link animation and continue execution */
	public static final Integer LINKTRACE_ANIMATION_GO = Integer.valueOf(3);

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** The clientId under which we are connected to the debugger service */
	private String clientId;

	/** The DebuggerService used to connect to the server */
	private static DebuggerService debuggerService;

	/** Thread that polls for debugger events */
	private PollingThread poller;

	/** True while the poller is running */
	private boolean pollerActive;

	/** Current position of a halted process */
	private ModelQualifier haltedPosition;

	/** Decorator for current position of halted process (frame stroke and color) */
	private StepDecorator stepDecorator;

	/** Decorator for breakpoints */
	private BreakPointDecorator breakpointDecorator;

	/** The thread responsible for handling animation of connections */
	private AnimationThread animator;

	/** Current process */
	private ProcessItem currentProcess;

	/** Control link trace mode (see the LINK_* constants) */
	private Integer controlLinkTraceMode;

	/** Data link trace mode (see the LINK_* constants) */
	private Integer dataLinkTraceMode;

	/** Toggle breakpoints action */
	private JaspiraAction toggleBreakpointsAction;

	/** Clear breakpoints action */
	private JaspiraAction clearBreakpointsAction;

	/** Break on workflow action */
	private JaspiraAction setBreakOnTopLevelAction;

	/** Break on top level action */
	private JaspiraAction setBreakOnWorkflowAction;

	/** Break on exception action */
	private JaspiraAction setBreakOnExceptionAction;

	/** Step into action */
	private JaspiraAction stepIntoAction;

	/** Step over action */
	private JaspiraAction stepOverAction;

	/** Step out action */
	private JaspiraAction stepOutAction;

	/** Step next action */
	private JaspiraAction stepNextAction;

	/** Resume action */
	private JaspiraAction resumeAction;

	/** Stop action */
	private JaspiraAction stopAction;

	/** Maximum number of debugger animation steps */
	private int maxStepCount;

	/** Maximum debugger animation step time in ms */
	private int maxStepTime;

	//////////////////////////////////////////////////
	// @@ Init/Activate
	//////////////////////////////////////////////////

	public String getResourceCollectionContainerName()
	{
		return "plugin.debugger";
	}

	// TODO The variables used in this method are not used currently.
	// This method exists in order to prevent compiler warnings about unused private variables.
	protected void keepCompilerHappy()
	{
		if (controlLinkTraceMode == 0 && dataLinkTraceMode == 0 && currentProcess == null)
			currentProcess = null;
	}

	/**
	 * @see org.openbp.jaspira.plugin.AbstractPlugin#getExternalOptionModuleClasses()
	 */
	protected Class[] getExternalOptionModuleClasses()
	{
		return new Class[]
		{
			DebuggerOptionModule.class
		};
	}

	/**
	 * Called when the first instance of this plugin is being created.
	 * Registers module with DebuggerService.
	 */
	public void installFirstPlugin()
	{
		super.installFirstPlugin();

		// Read Cockpit.properties animation settings
		maxStepCount = SettingUtil.getIntSetting("openbp.cockpit.debugger.maxstepcount", - 1);
		maxStepTime = SettingUtil.getIntSetting("openbp.cockpit.debugger.maxsteptime", - 1);

		registerDebugger(false);
	}

	/**
	 * Gets debugger service.
	 *
	 * @return The service
	 * @throws OpenBPException If there is no server to connect to
	 */
	DebuggerService getDebugger()
	{
		if (debuggerService == null)
		{
			if (! registerDebugger(false))
			{
				String msg = getPluginResourceCollection().getRequiredString("messages.nodebuggerservice");
				throw new OpenBPException("NoDebugger", msg);
			}
		}
		return debuggerService;
	}

	/**
	 * Registers the debugger.
	 * @param silentMode 
	 * true: Does not log an error message on failure.<br>
	 * false: Logs any error message
	 * @return
	 * true: Registration successful.<br>
	 * false: Error
	 */
	boolean registerDebugger(boolean silentMode)
	{
		debuggerService = (DebuggerService) ServerConnection.getInstance().lookupOptionalService(DebuggerService.class);
		if (debuggerService == null)
			return false;

		try
		{
			// Register with the service, 1 h timeout
			String givenId = OptionMgr.getInstance().getStringOption("debugger.debuggerid", null);
			clientId = debuggerService.registerClient(givenId, 60 * 60);

			// Adjust the debugger mode
			int mode = getDebugger().getDebuggerMode(clientId);
			int oldMode = mode;

			boolean skipSystemModel = OptionMgr.getInstance().getBooleanOption("debugger.skipsystemmodel", true);
			if (skipSystemModel)
				mode |= DebuggerService.MODE_SKIP_SYSTEM_MODEL;
			else
				mode &= ~ DebuggerService.MODE_SKIP_SYSTEM_MODEL;

			if (mode != oldMode)
			{
				getDebugger().setDebuggerMode(clientId, mode);
			}
		}
		catch (Exception e)
		{
			if (! silentMode)
			{
				LogUtil.error(getClass(), "Error connecting to the debugger service.", e);
			}
			debuggerService = null;
			return false;
		}

		startPoller();

		return true;
	}

	void unregisterDebugger()
	{
		if (clientId != null)
		{
			// Cause the debugger polling thread to finish
			stopPoller();

			if (debuggerService != null)
			{
				try
				{
					getDebugger().unregisterClient(clientId);
				}
				catch (Exception e)
				{
					ExceptionUtil.printTrace(e);
					clientId = null;
				}
				debuggerService = null;
			}
		}
	}

	/**
	 * Displays a message box and tries to re-register the debugger
	 * if a connection is available.
	 * @nowarn
	 */
	private void handleError(Exception e)
	{
		String msg = ExceptionUtil.getNestedMessage(e);
		JMsgBox.show(null, msg, JMsgBox.ICON_INFO);
	}

	/**
	 * Called after the install method of this plugin has been called.
	 */
	protected void pluginInstalled()
	{
		stepDecorator = new StepDecorator();

		toggleBreakpointsAction = getAction("debugger.client.togglebreakpoints");
		clearBreakpointsAction = getAction("debugger.client.clearbreakpoints");
		setBreakOnTopLevelAction = getAction("debugger.client.setbreakontoplevel");
		setBreakOnWorkflowAction = getAction("debugger.client.setbreakonworkflow");
		setBreakOnExceptionAction = getAction("debugger.client.setbreakonexception");

		stepIntoAction = getAction("debugger.client.stepinto");
		stepOverAction = getAction("debugger.client.stepover");
		stepOutAction = getAction("debugger.client.stepout");
		stepNextAction = getAction("debugger.client.stepnext");
		resumeAction = getAction("debugger.client.resume");
		stopAction = getAction("debugger.client.stop");

		try
		{
			int mode = getDebugger().getDebuggerMode(clientId);

			if (setBreakOnTopLevelAction != null)
			{
				setBreakOnTopLevelAction.setSelected((mode & DebuggerService.MODE_BREAK_ON_TOP_LEVEL) != 0);
			}
			if (setBreakOnWorkflowAction != null)
			{
				setBreakOnWorkflowAction.setSelected((mode & DebuggerService.MODE_BREAK_ON_WORKFLOW) != 0);
			}
			if (setBreakOnExceptionAction != null)
			{
				setBreakOnExceptionAction.setSelected((mode & DebuggerService.MODE_BREAK_ON_EXCEPTION) != 0);
			}
		}
		catch (Exception e)
		{
		}

		updateActions();
	}

	/**
	 * Performs clean up (unregistering of decorators).
	 */
	protected void pluginUninstalled()
	{
		super.pluginUninstalled();

		deactivateStepDecorator();
		deactivateBreakpointDecorator();
	}

	/**
	 * Called after uninstall for the last instance has been called.
	 */
	public void uninstallLastPlugin()
	{
		super.uninstallLastPlugin();

		unregisterDebugger();
	}

	//////////////////////////////////////////////////
	// @@ Current position and view update
	//////////////////////////////////////////////////

	/**
	 * Sets the current position of a halted process.
	 * This will also update the current position marker and clear the context inspector.
	 *
	 * @param newPos ModelQualifier of the new position or null if there is no halted process.<br>
	 * If the process that owns the element specified by the new position is not loaded yet,
	 * it will be loaded automatically. The new position will also be scrolled into view.
	 */
	void setHaltedPosition(ModelQualifier newPos)
	{
		if (haltedPosition != null)
		{
			// Clear the current position, so the update will display the current object in its usual state.
			ModelQualifier oldPosition = haltedPosition;
			haltedPosition = null;

			// Invalidate the current position (will clear the current position marker)
			// This will also enforce a rebuild of the content of a tag.
			fireEvent(new QualifierEvent(DebuggerPlugin.this, "modeler.view.invalidate", oldPosition));
		}

		// Update the position
		haltedPosition = newPos;

		// Activate or deactivator for the decorator for the current position
		if (haltedPosition != null)
		{
			activateStepDecorator();
		}
		else
		{
			deactivateStepDecorator();
		}

		if (newPos != null)
		{
			// First, make sure the process the new current position refers to is loaded
			ModelQualifier processQualifier = new ModelQualifier(newPos);
			processQualifier.setItemType(ItemTypes.PROCESS);
			processQualifier.setObjectPath(null);
			fireEvent(new OpenEvent(DebuggerPlugin.this, "open.modeler", processQualifier));

			// Invalidate the new current position (will display the current position marker)
			// This will also enforce a rebuild of the content of a tag.
			fireEvent(new QualifierEvent(DebuggerPlugin.this, "modeler.view.invalidate", newPos));

			// Move the object into view; add 100 px offset to prevent the object from hanging in the corner
			scrollIntoView(newPos, true);
		}

		if (animator != null)
		{
			// This will finish the spline animation if not finished yet
			animator.abort();
		}

		// Cause a screen refresh of the invalidated figures
		fireEvent(new JaspiraEvent(DebuggerPlugin.this, "modeler.view.refresh"));

		// Update the action button status
		updateActions();
	}

	/**
	 * Shows the process element specified by the given position.
	 * Also loads the process - if not loaded yet - that contains the element.
	 *
	 * @param position Current position
	 * @param addEnlargement
	 * true: Adds an offset of 100 px to the display box of the element.<br>
	 * false: Does not add an enlargement offset
	 */
	void scrollIntoView(final ModelQualifier position, boolean addEnlargement)
	{
		// This method is not being called from inside the event queue.
		// This leads to refresh problems when scrolling the workspace, so we need to place
		// the following code into a runnable that will be executed by the event dispatch thread.
		final String eventName = addEnlargement ? "modeler.view.show" : "modeler.view.showexact";
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				// Invalidate and update the object denoted by the current position
				// fireEvent (new QualifierEvent (DebuggerPlugin.this, "modeler.view.invalidate", position));
				// fireEvent (new JaspiraEvent (DebuggerPlugin.this, "modeler.view.refresh"));

				// Move the object into view
				fireEvent(new QualifierEvent(DebuggerPlugin.this, eventName, position));
			}
		});
	}

	/**
	 * Performs a deferred screen refresh.
	 * Will cause a refresh of the workspace from inside the event queue.
	 */
	void performDeferredRefresh()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				// Move the object into view
				fireEvent("modeler.view.refresh");
			}
		});
	}

	/**
	 * Updates the enable status of the run/step actions according to the current position.
	 */
	void updateActions()
	{
		boolean haveBreakpoints = breakpointDecorator != null;
		boolean haveHaltedProcess = haltedPosition != null;

		// Enable the breakpoint toggle action if any objects are selected.
		AskEvent ae = new AskEvent(this, "modeler.view.getselectioncount");
		fireEvent(ae);
		Integer selectionCount = (Integer) ae.getAnswer();
		boolean hasSelection = selectionCount != null && selectionCount.intValue() > 0;

		if (toggleBreakpointsAction != null)
		{
			toggleBreakpointsAction.setEnabled(hasSelection);
		}

		if (clearBreakpointsAction != null)
		{
			clearBreakpointsAction.setEnabled(haveBreakpoints);
		}

		if (stepIntoAction != null)
		{
			stepIntoAction.setEnabled(haveHaltedProcess);
		}
		if (stepOverAction != null)
		{
			stepOverAction.setEnabled(haveHaltedProcess);
		}
		if (stepOutAction != null)
		{
			stepOutAction.setEnabled(haveHaltedProcess);
		}
		if (stepNextAction != null)
		{
			stepNextAction.setEnabled(haveHaltedProcess);
		}

		// The resume and stop action are always enabled.
	}

	/**
	 * Runs a process using the given initial node.
	 *
	 * @param initialNode Initial node to start from
	 * @param step Run flag
	 */
	void runEntry(final InitialNode initialNode, final boolean step)
	{
		if (step)
		{
			// If we are debugging the process or we are using the internal browser,
			// we have to open the process in the modeler first
			ModelQualifier processQualifier = initialNode.getProcess().getQualifier();
			fireEvent(new OpenEvent(this, "open.modeler", processQualifier));
		}

		// The open.modeler event will be processed asynchonously,
		// so we will also put the request to the web browser to display the URL in the event queue,
		// or otherwise the internal web browser, which resides in the Modeler, will not be instantiated yet
		// and thus miss the event

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					// Set a breakpoint at the only socket of the initial node if a initialNodeQualifier was given
					startPoller();
				}
				catch (Exception e)
				{
					handleError(e);
				}
			}
		});
	}

	/**
	 * Automatically save any modifications to processes if the autosave option has been set.
	 * @return Always true
	 */
	boolean autoSave()
	{
		// Auto-save any pending changes before continuing
		if (OptionMgr.getInstance().getBooleanOption("debugger.autosave", true))
		{
			fireEvent(new JaspiraActionEvent(DebuggerPlugin.this, "standard.file.saveall", LEVEL_APPLICATION));
		}
		return true;
	}

	//////////////////////////////////////////////////
	// @@ Debugger client command module
	//////////////////////////////////////////////////

	/**
	 * Event module for client-side debugger events.
	 * These are actually toolbar or menu actions.
	 */
	public class ClientEvents extends EventModule
	{
		public String getName()
		{
			return "debugger.client";
		}

		/**
		 * Returns the priority of the module.
		 * We are low-level priority.
		 * @nowarn
		 */
		public int getPriority()
		{
			return 100;
		}

		//////////////////////////////////////////////////
		// @@ Action events
		//////////////////////////////////////////////////

		/**
		 * Event handler that toggles the breakpoints associated with the selected object(s).
		 *
		 * @event debugger.client.togglebreakpoints
		 * @param jae Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode togglebreakpoints(JaspiraActionEvent jae)
		{
			// Get the current selection of the active modeler
			AskEvent ae = new AskEvent(DebuggerPlugin.this, "modeler.view.getselection");
			fireEvent(ae);
			List selection = (List) ae.getAnswer();
			if (selection == null || selection.size() == 0)
				return EVENT_IGNORED;

			// Qualifiers of the selected sockets
			List qualifiers = new ArrayList();

			// Iterate the selection and remember the Qualifiers of the selected sockets
			int nSelected = selection.size();
			for (int iSelected = 0; iSelected < nSelected; ++iSelected)
			{
				Object o = selection.get(iSelected);
				if (o instanceof SocketFigure)
				{
					// Socket selected; add itsmodel qualifier 
					NodeSocket socket = ((SocketFigure) o).getNodeSocket();
					qualifiers.add(socket.getQualifier());
				}
				else if (o instanceof NodeFigure)
				{
					// Node selected; add the Qualifiers of all of its sockets
					Node node = ((NodeFigure) o).getNode();
					for (Iterator itSockets = node.getSockets(); itSockets.hasNext();)
					{
						NodeSocket socket = (NodeSocket) itSockets.next();
						qualifiers.add(socket.getQualifier());
					}
				}
				else if (o instanceof FlowConnection)
				{
					// Control link selected; add the Qualifiers of its start end end sockets
					ControlLink controlLink = ((FlowConnection) o).getControlLink();
					qualifiers.add(controlLink.getSourceSocket().getQualifier());
					qualifiers.add(controlLink.getTargetSocket().getQualifier());
				}
			}

			int nQualifiers = qualifiers.size();
			if (nQualifiers == 0)
				// There is nothing selected that we can use
				return EVENT_IGNORED;

			// We should breakpoint all Qualifiers if at least one of them is not yet breakpointed.
			boolean setBp = false;
			for (int iQualifiers = 0; iQualifiers < nQualifiers; ++iQualifiers)
			{
				ModelQualifier qualifier = (ModelQualifier) qualifiers.get(iQualifiers);
				if (! hasBreakpoint(qualifier))
				{
					setBp = true;
					break;
				}
			}

			// Set or remove the breakpoints
			try
			{
				for (int iQualifiers = 0; iQualifiers < nQualifiers; ++iQualifiers)
				{
					ModelQualifier qualifier = (ModelQualifier) qualifiers.get(iQualifiers);
					boolean hasBp = hasBreakpoint(qualifier);

					if (setBp)
					{
						if (! hasBp)
						{
							// Breakpoint doesn't exist, set
							getDebugger().setBreakpoint(clientId, qualifier, 0);
							addBreakpoint(qualifier);
						}
					}
					else
					{
						if (hasBp)
						{
							// Breakpoint exists, clear
							getDebugger().clearBreakpoint(clientId, qualifier);
							removeBreakpoint(qualifier);
						}
					}
				}

				// Perform a screen refresh
				fireEvent(new JaspiraEvent(DebuggerPlugin.this, "modeler.view.refresh"));
				updateActions();
			}
			catch (Exception e)
			{
				handleError(e);
			}

			return EVENT_CONSUMED;
		}

		/**
		 * Event handler that clears all breakpoints.
		 *
		 * @event debugger.client.clearbreakpoints
		 * @param jae Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode clearbreakpoints(JaspiraActionEvent jae)
		{
			try
			{
				getDebugger().clearBreakpoints(clientId, null);

				deactivateBreakpointDecorator();

				// Several breakpoints may be concerned, so perform a screen redraw
				fireEvent(new JaspiraEvent(DebuggerPlugin.this, "modeler.view.redraw"));
				updateActions();
			}
			catch (Exception e)
			{
				handleError(e);
			}

			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Toggles break on top level mode.
		 *
		 * @event debugger.client.setbreakontoplevel
		 * @param jae Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode setBreakOnTopLevel(JaspiraActionEvent jae)
		{
			try
			{
				int mode = getDebugger().getDebuggerMode(clientId);
				mode ^= DebuggerService.MODE_BREAK_ON_TOP_LEVEL;
				getDebugger().setDebuggerMode(clientId, mode);

				if (setBreakOnTopLevelAction != null)
				{
					setBreakOnTopLevelAction.setSelected((mode & DebuggerService.MODE_BREAK_ON_TOP_LEVEL) != 0);
				}
			}
			catch (Exception e)
			{
				handleError(e);
			}

			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Toggles break on top level mode.
		 *
		 * @event debugger.client.setbreakonworkflow
		 * @param jae Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode setBreakOnWorkflow(JaspiraActionEvent jae)
		{
			try
			{
				int mode = getDebugger().getDebuggerMode(clientId);
				mode ^= DebuggerService.MODE_BREAK_ON_WORKFLOW;
				getDebugger().setDebuggerMode(clientId, mode);

				if (setBreakOnWorkflowAction != null)
				{
					setBreakOnWorkflowAction.setSelected((mode & DebuggerService.MODE_BREAK_ON_WORKFLOW) != 0);
				}
			}
			catch (Exception e)
			{
				handleError(e);
			}

			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Sets break on exception mode.
		 *
		 * @event debugger.client.setbreakonexception
		 * @param jae Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode setBreakOnException(JaspiraActionEvent jae)
		{
			try
			{
				int mode = getDebugger().getDebuggerMode(clientId);
				mode ^= DebuggerService.MODE_BREAK_ON_EXCEPTION;
				getDebugger().setDebuggerMode(clientId, mode);

				if (setBreakOnExceptionAction != null)
				{
					setBreakOnExceptionAction.setSelected((mode & DebuggerService.MODE_BREAK_ON_EXCEPTION) != 0);
				}
			}
			catch (Exception e)
			{
				handleError(e);
			}

			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Executes the step into command.
		 *
		 * @event debugger.client.stepinto
		 * @param jae Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode stepInto(JaspiraActionEvent jae)
		{
			if (! autoSave())
				return EVENT_CONSUMED;

			setHaltedPosition(null);

			try
			{
				debuggerService.stepInto(clientId);
				startPoller();
			}
			catch (Exception e)
			{
				handleError(e);
			}

			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Executes the step out command.
		 *
		 * @event debugger.client.stepout
		 * @param jae Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode stepOut(JaspiraActionEvent jae)
		{
			if (! autoSave())
				return EVENT_CONSUMED;

			setHaltedPosition(null);

			try
			{
				getDebugger().stepOut(clientId);
				startPoller();
			}
			catch (Exception e)
			{
				handleError(e);
			}

			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Executes the step over command.
		 *
		 * @event debugger.client.stepover
		 * @param jae Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode stepOver(JaspiraActionEvent jae)
		{
			if (! autoSave())
				return EVENT_CONSUMED;

			setHaltedPosition(null);

			try
			{
				getDebugger().stepOver(clientId);
				startPoller();
			}
			catch (Exception e)
			{
				handleError(e);
			}

			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Executes the step out command.
		 *
		 * @event debugger.client.stepnext
		 * @param jae Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode stepNext(JaspiraActionEvent jae)
		{
			if (! autoSave())
				return EVENT_CONSUMED;

			setHaltedPosition(null);

			try
			{
				getDebugger().stepNext(clientId);
				startPoller();
			}
			catch (Exception e)
			{
				handleError(e);
			}

			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Executes the stop process command.
		 *
		 * @event debugger.client.stop
		 * @param jae Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode stop(JaspiraActionEvent jae)
		{
			if (! autoSave())
				return EVENT_CONSUMED;

			setHaltedPosition(null);

			try
			{
				getDebugger().stop(clientId);
				startPoller();
			}
			catch (Exception e)
			{
				handleError(e);
			}

			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Executes the resume process command.
		 *
		 * @event debugger.client.resume
		 * @param jae Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode resume(JaspiraActionEvent jae)
		{
			if (! autoSave())
				return EVENT_CONSUMED;

			if (haltedPosition == null)
			{
				// We have no halted process, so we just listen and wait.
				// This is necessary for processes that are not started from
				// the Modeler, but from external sources (e. g. a WAP browser).
				startPoller();
			}
			else
			{
				setHaltedPosition(null);

				try
				{
					getDebugger().run(clientId);
					startPoller();
				}
				catch (Exception e)
				{
					handleError(e);
				}
			}

			return EVENT_CONSUMED;
		}

		//////////////////////////////////////////////////
		// @@ Events called from the outside
		//////////////////////////////////////////////////

		/**
		 * Event handler: Returns the id if the debugger client.
		 *
		 * @event debugger.client.getdebuggerid
		 * @param ae Event<br>
		 * The id will be stored into the answer of the event.
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode getdebuggerid(AskEvent ae)
		{
			// Make sure we are connected to the debugger
			try
			{
				getDebugger();
			}
			catch (Exception e)
			{
				handleError(e);
			}

			ae.setAnswer(clientId);
			return EVENT_CONSUMED;
		}

		//////////////////////////////////////////////////
		// @@ Internal events
		//////////////////////////////////////////////////

		/**
		 * Event handler: Toggles a breakpoint.
		 *
		 * @event debugger.client.togglebreakpoint
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode toggleBreakpoint(QualifierEvent je)
		{
			ModelQualifier target = je.getQualifier();

			try
			{
				if (hasBreakpoint(target))
				{
					// Breakpoint exists, clear
					getDebugger().clearBreakpoint(clientId, target);
					removeBreakpoint(target);
				}
				else
				{
					// Breakpoint doesn't exist, set
					getDebugger().setBreakpoint(clientId, target, 0);
					addBreakpoint(target);
				}

				// Perform a screen refresh
				fireEvent(new JaspiraEvent(DebuggerPlugin.this, "modeler.view.refresh"));
			}
			catch (Exception e)
			{
				handleError(e);
			}

			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Executes the step until command.
		 *
		 * @event debugger.client.stepuntil
		 * @param je Event<br>
		 * Model qualifier: ModelQualifier of the step target
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode stepUntil(QualifierEvent je)
		{
			if (! autoSave())
				return EVENT_CONSUMED;

			setHaltedPosition(null);

			ModelQualifier target = je.getQualifier();

			try
			{
				getDebugger().stepUntil(clientId, target.toString());
				startPoller();
			}
			catch (Exception e)
			{
				handleError(e);
			}

			return EVENT_CONSUMED;
		}

		//////////////////////////////////////////////////
		// @@ Modeler events
		//////////////////////////////////////////////////

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
				currentProcess = ((Modeler) o).getProcess();

				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
		}

		/**
		 * Event handler: A modeler view has become inactive.
		 *
		 * @event modeler.view.closed
		 * @eventobject Editor that owns the view ({@link Modeler})
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode modeler_view_closed(JaspiraEvent je)
		{
			Object o = je.getObject();

			if (o instanceof Modeler)
			{
				currentProcess = null;

				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
		}

		/**
		 * Event handler: The selection of a modeler view has changed.
		 *
		 * @event modeler.view.selectionchanged
		 * @eventobject Editor that owns the view ({@link Modeler})
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode modeler_view_selectionchanged(JaspiraEvent je)
		{
			Object source = je.getSource();

			if (source instanceof Modeler)
			{
				Modeler modeler = (Modeler) source;
				if (modeler.getPluginComponent().isShowing())
				{
					// The selection of the active modeler changed.
					// Update the toggle breakpoint action status.
					updateActions();
				}

				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
		}

		/**
		 * Event handler: The cockpit has reconnected to the server.
		 *
		 * @event plugin.serverconnection.reconnect
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode plugin_serverconnection_reconnect(JaspiraEvent je)
		{
			clearbreakpoints(null);
			return EVENT_HANDLED;
		}
	}

	/**
	 * Event module for client-side debugger events.
	 * These are actually toolbar or menu actions.
	 */
	public class OptionEvents extends EventModule
	{
		public String getName()
		{
			return "debugger";
		}

		/**
		 * Returns the priority of the module.
		 * We are low-level priority.
		 * @nowarn
		 */
		public int getPriority()
		{
			return 100;
		}

		/**
		 * Event handler: The skip visual process option has changed.
		 *
		 * @event debugger.skipsystemmodel
		 * @eventobject The changed option
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode skipsystemmodel(JaspiraEvent je)
		{
			boolean skip = ((Boolean) ((Option) je.getObject()).getValue()).booleanValue();

			try
			{
				int mode = getDebugger().getDebuggerMode(clientId);
				int oldMode = mode;

				if (skip)
				{
					mode |= DebuggerService.MODE_SKIP_SYSTEM_MODEL;
				}
				else
				{
					mode &= ~ DebuggerService.MODE_SKIP_SYSTEM_MODEL;
				}

				if (mode != oldMode)
				{
					getDebugger().setDebuggerMode(clientId, mode);
				}
			}
			catch (Exception e)
			{
				handleError(e);
			}

			return EVENT_HANDLED;
		}

		/**
		 * Event handler: The control link trace mode option has changed.
		 *
		 * @event debugger.controllinktracemode
		 * @eventobject The changed option
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode controllinktracemode(JaspiraEvent je)
		{
			controlLinkTraceMode = (Integer) ((Option) je.getObject()).getValue();
			return EVENT_HANDLED;
		}

		/**
		 * Event handler: The data link trace mode option has changed.
		 *
		 * @event debugger.datalinktracemode
		 * @eventobject The changed option
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode datalinktracemode(JaspiraEvent je)
		{
			dataLinkTraceMode = (Integer) ((Option) je.getObject()).getValue();
			return EVENT_HANDLED;
		}

		/**
		 * Event handler: The debugger id option has changed.
		 *
		 * @event debugger.debuggerid
		 * @eventobject The changed option
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode debuggerid(JaspiraEvent je)
		{
			unregisterDebugger();
			registerDebugger(false);
			return EVENT_HANDLED;
		}
	}

	//////////////////////////////////////////////////
	// @@ Debugger interaction module
	//////////////////////////////////////////////////

	/**
	 * Interaction module.
	 */
	public class InteractionEvents extends InteractionModule
	{
		/**
		 * Gets the module priority.
		 * We are high priority.
		 *
		 * @return The priority. 0 is lowest, 100 is highest.
		 */
		public int getPriority()
		{
			return 2;
		}

		/**
		 * Standard event handler that is called when a popup menu is to be shown.
		 * Adds the debugger popup menu entries for entry and final nodes and node sockets.
		 *
		 * @event global.interaction.popup
		 * @param ie Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode popup(InteractionEvent ie)
		{
			if (ie.getSource() instanceof NodeItemEditorPlugin)
				return EVENT_IGNORED;

			// InitialNode only -> Run/Step form here
			if (ie.isDataFlavorSupported(ClientFlavors.INITIAL_NODE) || ie.isDataFlavorSupported(ClientFlavors.FINAL_NODE))
			{
				SingleSocketNode node;
				if (ie.isDataFlavorSupported(ClientFlavors.INITIAL_NODE))
					node = (SingleSocketNode) ie.getSafeTransferData(ClientFlavors.INITIAL_NODE);
				else
					node = (SingleSocketNode) ie.getSafeTransferData(ClientFlavors.FINAL_NODE);

				final NodeSocket socket = node.getSocket();

				setupSocketContextMenu(ie, socket);
			}

			// Sockets: -> set/clear Breakpoints, run to here
			if (ie.isDataFlavorSupported(ClientFlavors.NODE_SOCKET))
			{
				final NodeSocket socket = (NodeSocket) ie.getSafeTransferData(ClientFlavors.NODE_SOCKET);

				setupSocketContextMenu(ie, socket);
			}

			return EVENT_HANDLED;
		}

		/**
		 * Standard event handler that is called when a toolbar is (re-)generated.
		 * Adds the toolbar entries to the modeler view plugin and the web browser plugin.
		 *
		 * @event global.interaction.toolbar
		 * @param ie Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode toolbar(InteractionEvent ie)
		{
			if (ie.getSourcePlugin() instanceof Modeler)
			{
				JaspiraAction group;

				group = new JaspiraAction("breakpoint", null, null, null, null, 2, JaspiraAction.TYPE_GROUP);
				if (toggleBreakpointsAction != null)
				{
					group.addToolbarChild(toggleBreakpointsAction);
				}
				if (clearBreakpointsAction != null)
				{
					group.addToolbarChild(clearBreakpointsAction);
				}
				ie.add(group);

				group = new JaspiraAction("step", null, null, null, null, 3, JaspiraAction.TYPE_GROUP);
				if (stepIntoAction != null)
				{
					group.addToolbarChild(stepIntoAction);
				}
				if (stepOverAction != null)
				{
					group.addToolbarChild(stepOverAction);
				}
				if (stepOutAction != null)
				{
					group.addToolbarChild(stepOutAction);
				}
				if (stepNextAction != null)
				{
					group.addToolbarChild(stepNextAction);
				}
				if (resumeAction != null)
				{
					group.addToolbarChild(resumeAction);
				}
				if (stopAction != null)
				{
					group.addToolbarChild(stopAction);
				}
				ie.add(group);

				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
		}

		/**
		 * Sets up the context menu for a node socket.
		 *
		 * @param ie Interaction event to add the menus to
		 * @param socket Socket the context menu should refer to
		 */
		private void setupSocketContextMenu(InteractionEvent ie, final NodeSocket socket)
		{
			JaspiraAction group = new JaspiraAction("popupbreakpoint", null, null, null, null, 1, JaspiraAction.TYPE_GROUP);

			// Menu item: Toggle breakpoint
			group.addMenuChild(new JaspiraAction(DebuggerPlugin.this, "debugger.client.togglebreakpoint")
			{
				public void actionPerformed(ActionEvent e)
				{
					fireEvent(new QualifierEvent(DebuggerPlugin.this, "debugger.client.togglebreakpoint", socket.getQualifier()));
				}
			});

			ie.add(group);

			if (haltedPosition != null)
			{
				// Menu item: Step until; add this only if we have a halted process
				group = new JaspiraAction("popupstep", null, null, null, null, 3, JaspiraAction.TYPE_GROUP);

				group.addMenuChild(new JaspiraAction(DebuggerPlugin.this, "debugger.client.stepuntil")
				{
					public void actionPerformed(ActionEvent e)
					{
						fireEvent(new QualifierEvent(DebuggerPlugin.this, "debugger.client.stepuntil", socket.getQualifier()));
					}
				});

				ie.add(group);
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Debugger server communication module
	//////////////////////////////////////////////////

	/**
	 * Debugger server interface module.
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
			// Highlight node and socket
			ModelQualifier pos = new ModelQualifier(dse.getHaltedPosition());
			setHaltedPosition(pos);

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
			// Turn off highlighting
			ModelQualifier pos = new ModelQualifier(dse.getHaltedPosition());
			setHaltedPosition(pos);

			return EVENT_HANDLED;
		}

		/**
		 * Event handler: Controlflows.
		 *
		 * @event debugger.server.controlflow
		 * @param dse Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode controlflow(DebuggerServerEvent dse)
		{
			if (maxStepCount != 0)
			{
				// Run control flow animation and
				// execute a step next command after the animation has finished
				DebuggerEvent de = dse.getDebuggerEvent();

				// Scroll the flow connection into view; however don't add enlargment offset
				scrollIntoView(de.getControlLinkQualifier(), false);

				new AnimationThread(de.getControlLinkQualifier(), true);
			}
			else
			{
				// Execute a step next command right away
				fireEvent(new JaspiraActionEvent(DebuggerPlugin.this, "debugger.client.stepnext", LEVEL_APPLICATION));
			}

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
			DebuggerEvent de = dse.getDebuggerEvent();
			ModelQualifier pos = dse.getHaltedPosition();

			if (ViewModeMgr.getInstance().isDataLinkVisible())
			{
				setHaltedPosition(new ModelQualifier(pos));
			}

			// Run data flow animation if we should (i. e. if allowed by cockpit settings
			// and if the data flow lines are visible at all).
			// After the animation finishes, a step next command will be issued.
			if (maxStepCount != 0 && ViewModeMgr.getInstance().isDataLinkVisible())
			{
				// Scroll the flow connection into view; however don't add enlargment offset
				scrollIntoView(de.getDataLinkQualifier(), false);

				new AnimationThread(de.getDataLinkQualifier(), false);
			}
			else
			{
				// No data flow animation, proceed directly by sending a step next event
				fireEvent(new JaspiraActionEvent(DebuggerPlugin.this, "debugger.client.stepnext", LEVEL_APPLICATION));
			}

			return EVENT_HANDLED;
		}

		/**
		 * Event handler: Processexceptions.
		 *
		 * @event debugger.server.processexception
		 * @param dse Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode processexception(DebuggerServerEvent dse)
		{
			ModelQualifier haltedPosition = dse.getHaltedPosition();
			setHaltedPosition(new ModelQualifier(haltedPosition));

			ModelQualifier msgPosition = new ModelQualifier(haltedPosition);
			msgPosition.setObjectPath(null);
			String message = "Exception in process '" + msgPosition + "':";

			if (dse.getException() != null)
			{
				fireEvent(new ErrorEvent(DebuggerPlugin.this, message, dse.getException()));
			}
			else
			{
				fireEvent(new ErrorEvent(DebuggerPlugin.this, message, dse.getExceptionString()));
			}

			// Automatically respond to the server with a 'resume' command.
			// This will continue the current process (if it can be continued) with the error handling.
			// If it cannot be resumed, the process must be restarted again by the user...
			/* TODO Fix 4 Debugger not resumed any more after exception - we should notify the user!
			fireEvent(new JaspiraActionEvent(DebuggerPlugin.this, "debugger.client.resume", LEVEL_APPLICATION));
			 */

			return EVENT_HANDLED;
		}

		/**
		 * Handles any unaccounted event.
		 *
		 * @param ev Event
		 * @return The event status code
		 */
		protected JaspiraEventHandlerCode handleUnaccountedEvent(JaspiraEvent ev)
		{
			// Shouldn't happen
			LogUtil.error(getClass(), "Unknown server event $0.", ev);

			return super.handleUnaccountedEvent(ev);
		}
	}

	//////////////////////////////////////////////////
	// @@ Polling Thread
	//////////////////////////////////////////////////

	/**
	 * Starts the polling thread if not already active.
	 */
	void startPoller()
	{
		if (! Cockpit.disableTimersForDebug && debuggerService != null)
		{
			pollerActive = true;

			if (poller == null)
			{
				poller = new PollingThread();
				poller.start();
			}

			fireEvent(new JaspiraEvent(DebuggerPlugin.this, "debugger.client.statuschange", clientId));
		}
	}

	/**
	 * Stops the polling thread if currently running.
	 */
	void stopPoller()
	{
		pollerActive = false;

		fireEvent(new JaspiraEvent(DebuggerPlugin.this, "debugger.client.statuschange", null));
	}

	/**
	 * Thread that checks for new server events every 500 ms.
	 */
	class PollingThread extends Thread
	{
		/**
		 * The constructor.
		 */
		public PollingThread()
		{
			super("Debugger event poller");

			// This thread must not prevent VM shutdown.
			setDaemon(true);
		}

		/**
		 * Run method.
		 */
		public void run()
		{
			while (pollerActive)
			{
				try
				{
					DebuggerEvent event = getDebugger().getEvent(clientId);

					if (event != null)
					{
						// We have an event, we wrap it into a JaspiraEvent.
						stopPoller();
						fireEvent(new DebuggerServerEvent(DebuggerPlugin.this, event));
					}
				}
				catch (Exception e)
				{
					if (! registerDebugger(true))
						break;
				}

				// Delay for a while
				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException e)
				{
				}
			}

			// Kill the reference to the thread, so it will be reinstantiated by {@link #startPoller}
			poller = null;
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Step decorator
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Creates and activates the step decorator.
	 */
	private void activateStepDecorator()
	{
		DecorationMgr.addDecorator(this, XFigure.DECO_FRAMESTROKE, stepDecorator);
		DecorationMgr.addDecorator(this, XFigure.DECO_FRAMECOLOR, stepDecorator);
		DecorationMgr.addDecorator(this, AbstractTagFigure.DECO_TAGCONTENTTYPE, stepDecorator);
	}

	/**
	 * Removes activates the step decorator.
	 */
	private void deactivateStepDecorator()
	{
		DecorationMgr.removeDecorator(this, XFigure.DECO_FRAMESTROKE, stepDecorator);
		DecorationMgr.removeDecorator(this, XFigure.DECO_FRAMECOLOR, stepDecorator);
		DecorationMgr.removeDecorator(this, AbstractTagFigure.DECO_TAGCONTENTTYPE, stepDecorator);
	}

	/**
	 * Decorator for current position of halted process.
	 */
	public class StepDecorator extends FilteredDecorator
	{
		/**
		 * @see org.openbp.jaspira.decoration.FilteredDecorator#doDecorate(Object, String, Object)
		 */
		public Object doDecorate(Object owner, String key, Object value)
		{
			if (key == XFigure.DECO_FRAMESTROKE)
				return ModelerGraphics.debuggerStroke;

			if (key == XFigure.DECO_FRAMECOLOR)
				return ModelerColors.DEBUGGER_BORDER;

			if (key == AbstractTagFigure.DECO_TAGCONTENTTYPE)
				return Integer.valueOf(((Integer) value).intValue() | AbstractTagFigure.CONTENT_FLOW | AbstractTagFigure.CONTENT_TEXT);

			return value;
		}

		/**
		 * @see org.openbp.jaspira.decoration.FilteredDecorator#qualifies(Object)
		 */
		public boolean qualifies(Object owner)
		{
			ModelQualifier qualifier;

			if (owner instanceof ModelQualifier)
			{
				qualifier = (ModelQualifier) owner;
			}
			else
			{
				if (! (owner instanceof ProcessElementContainer))
					return false;

				qualifier = ((ProcessElementContainer) owner).getProcessElement().getQualifier();
			}

			return qualifier.matches(haltedPosition);
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Breakpoint decorator
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Adds a breakpoint and installs the breakpoint decorator.
	 *
	 * @param qualifier Model qualifier of the breakpoint to add
	 */
	protected void addBreakpoint(ModelQualifier qualifier)
	{
		activateBreakpointDecorator();

		breakpointDecorator.add(qualifier);
		fireEvent(new QualifierEvent(DebuggerPlugin.this, "modeler.view.invalidate", qualifier));
	}

	/**
	 * Removes a breakpoint and deinstalls the breakpoint decorator if empty.
	 *
	 * @param qualifier Model qualifier of the breakpoint to remove
	 */
	void removeBreakpoint(ModelQualifier qualifier)
	{
		if (breakpointDecorator != null)
		{
			breakpointDecorator.remove(qualifier);
			fireEvent(new QualifierEvent(DebuggerPlugin.this, "modeler.view.invalidate", qualifier));

			if (breakpointDecorator.isEmpty())
			{
				deactivateBreakpointDecorator();
			}
		}
	}

	/**
	 * Checks if a breakpoint is set.
	 *
	 * @param qualifier Model qualifier of the breakpoint to check
	 */
	boolean hasBreakpoint(ModelQualifier qualifier)
	{
		if (breakpointDecorator != null)
			return breakpointDecorator.qualifies(qualifier);
		return false;
	}

	/**
	 * Creates and activates the breakpoint decorator.
	 */
	private void activateBreakpointDecorator()
	{
		if (breakpointDecorator == null)
		{
			breakpointDecorator = new BreakPointDecorator();
			DecorationMgr.addDecorator(this, BasicFigure.DECO_OVERLAY, breakpointDecorator);
			DecorationMgr.addDecorator(this, AbstractTagFigure.DECO_TAGCONTENTTYPE, breakpointDecorator);
		}
	}

	/**
	 * Removes activates the breakpoint decorator.
	 */
	void deactivateBreakpointDecorator()
	{
		if (breakpointDecorator != null)
		{
			DecorationMgr.removeDecorator(this, BasicFigure.DECO_OVERLAY, breakpointDecorator);
			DecorationMgr.removeDecorator(this, AbstractTagFigure.DECO_TAGCONTENTTYPE, breakpointDecorator);
			breakpointDecorator = null;
		}
	}

	/**
	 * Decorator for breakpoints.
	 */
	public class BreakPointDecorator extends ListDecorator
	{
		/**
		 * @see org.openbp.jaspira.decoration.ListDecorator#qualifies(Object)
		 */
		public boolean qualifies(Object owner)
		{
			ModelQualifier qualifier = null;

			if (owner instanceof ModelQualifier)
			{
				qualifier = (ModelQualifier) owner;
			}
			else
			{
				if (owner instanceof SocketFigure)
				{
					SocketFigure socketFigure = (SocketFigure) owner;
					qualifier = socketFigure.getNodeSocket().getQualifier();
				}
			}

			return super.qualifies(qualifier);
		}

		/**
		 * @see org.openbp.jaspira.decoration.FilteredDecorator#doDecorate(Object, String, Object)
		 */
		public Object doDecorate(Object owner, String key, Object value)
		{
			if (key == XFigure.DECO_OVERLAY && (owner instanceof SocketFigure))
			{
				// Make the breakpoint decoriation 2 pixels larger than the flow access figure
				Figure overlay = FigureResources.getBreakpointOverlay(owner);

				return overlay;
			}

			if (key == AbstractTagFigure.DECO_TAGCONTENTTYPE)
				return Integer.valueOf(((Integer) value).intValue() | AbstractTagFigure.CONTENT_FLOW | AbstractTagFigure.CONTENT_TEXT);

			return value;
		}
	}

	//////////////////////////////////////////////////
	// @@ Animation Thread
	//////////////////////////////////////////////////

	/** Step counter for stopped animation */
	private static final Double noStep = new Double(Double.NaN);

	/**
	 * Thread responsible for the animation of Connections
	 */
	class AnimationThread extends Thread
		implements Decorator
	{
		/**
		 * The current step decoration value of the animation, or NaN if stopped.
		 * Indicates the position of the point on the spline (0.0 &lt;  n &lt;  1.0).
		 */
		private Double stepDecorationValue = noStep;

		/** Contains the connection that we are decorating */
		private transient PolySplineConnection connection;

		/** Step width for the animation (0 &lt; step &lt; 1) */
		private double stepWidth;

		/** Time to elapse between each step */
		private int stepTime;

		/** Animation has been aborted by user command */
		private boolean aborted;

		/** Automatically issue a step next command when the animation has finished */
		private boolean autoStep;

		/////////////////////////////////////////////////////////////////////////
		// @@ Construction
		/////////////////////////////////////////////////////////////////////////

		/**
		 * Constructor.
		 *
		 * @param qualifier Model qualifier of the connection to animate
		 * @param autoStep
		 * true: Automatically issue a step next command when the animation has finished.<br>
		 * false: Do nothing on finish
		 */
		public AnimationThread(ModelQualifier qualifier, boolean autoStep)
		{
			super("Debugger animation");

			// This thread must not prevent VM shutdown.
			setDaemon(true);

			// First, retrieve the connection figure we want to animate
			AskEvent ae = new AskEvent(DebuggerPlugin.this, "modeler.view.getbyqualifier", qualifier);
			fireEvent(ae);

			if (ae.getAnswer() instanceof PolySplineConnection)
			{
				connection = (PolySplineConnection) ae.getAnswer();

				// We found it
				animator = this;
				this.autoStep = autoStep;

				// Determine the approximate length of the spline.
				// We do this by measuring the length of the diagonal of the spline's display box.
				Rectangle box = connection.getSplineBounds();
				int l = (int) Math.sqrt(box.width * box.width + box.height * box.height);

				// Each step should move the point by approx. 5 pixel
				int steps = l / 5;

				if (steps < 10)
				{
					// We have less than 10 steps, try 3 pixel
					steps = l / 3;
					if (steps < 10)
						steps = Math.max(l, 10);
				}
				else if (steps > 100)
				{
					steps = 100;
				}

				if (maxStepCount > 0 && steps > maxStepCount)
				{
					steps = maxStepCount;
				}

				stepWidth = 1d / steps;
				stepTime = 1000 / steps;

				if (maxStepTime >= 0 && stepTime > maxStepTime)
				{
					stepTime = maxStepTime;
				}

				// Add the animator as spline animation decorator as long as the thread is running
				DecorationMgr.addDecorator(DebuggerPlugin.this, PolySplineConnection.DECO_ANIMATION, this);

				// Start the animation
				start();
			}
			else
			{
				// Otherwise, don't start the thread because we have nothing to animate.
				// However, issue the step next command if desired.
				if (autoStep)
				{
					// Send step next event
					fireEvent(new JaspiraActionEvent(DebuggerPlugin.this, "debugger.client.stepnext", LEVEL_APPLICATION));
				}
			}
		}

		/////////////////////////////////////////////////////////////////////////
		// @@ Thread
		/////////////////////////////////////////////////////////////////////////

		/**
		 * Run method.
		 */
		public void run()
		{
			for (double currentStep = 0d; currentStep < 1d; currentStep += stepWidth)
			{
				if (aborted)
				{
					break;
				}

				// Advance the point on the spline
				stepDecorationValue = new Double(currentStep);

				if (connection != null)
				{
					// Invalidate the connection and force a screen update
					connection.invalidate();
					performDeferredRefresh();
				}

				// Delay for a while

				if (stepTime > 0)
				{
					try
					{
						// Sleep for a while
						Thread.sleep(stepTime);
					}
					catch (InterruptedException e)
					{
						// break;
					}
				}
			}

			if (autoStep && ! aborted)
			{
				// Send step next event
				fireEvent(new JaspiraActionEvent(DebuggerPlugin.this, "debugger.client.stepnext", LEVEL_APPLICATION));
			}

			aborted = false;

			animator = null;
			DecorationMgr.removeDecorator(DebuggerPlugin.this, PolySplineConnection.DECO_ANIMATION, this);

			if (connection != null)
			{
				// Invalidate the connection and force a screen update
				connection.invalidate();
				performDeferredRefresh();
			}
		}

		/////////////////////////////////////////////////////////////////////////
		// @@ Decoration
		/////////////////////////////////////////////////////////////////////////

		/**
		 * Returns the actual step
		 *
		 * @param owner Object to be decorated
		 * @param key Key under which the decorator is accessed
		 * @param value Original decoration value (usually null)
		 */
		public Object decorate(Object owner, String key, Object value)
		{
			if (connection == owner)
				// We are decorating the connection in progress, return the decoration
				return stepDecorationValue;

			// Return the original decoration value (usually null)
			return value;
		}

		/////////////////////////////////////////////////////////////////////////
		// @@ Control
		/////////////////////////////////////////////////////////////////////////

		public void abort()
		{
			aborted = true;
		}
	}
}
