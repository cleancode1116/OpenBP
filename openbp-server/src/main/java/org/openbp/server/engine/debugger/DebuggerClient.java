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
package org.openbp.server.engine.debugger;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.openbp.core.OpenBPException;
import org.openbp.core.engine.debugger.Breakpoint;
import org.openbp.core.model.ModelQualifier;
import org.openbp.server.context.TokenContext;
import org.openbp.server.engine.EngineTraceEvent;

/**
 * This object holds all information regarding the status of a debugger client.
 * The debugger client object is also used for synchronization purposes.
 *
 * @author Heiko Erhardt
 */
public class DebuggerClient
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Id of the client */
	private String clientId;

	/** Timeout for automatically unregistering the client, 0 to prevent unregistering */
	private int timeout;

	/** Timestamp of last node execution in seconds */
	private long timestamp;

	/** Operation mode of the debugger */
	private int debuggerMode;

	/**
	 * Table of breakpoints.
	 * Maps positions (fully qualified node, socket or parameter names (Strings)) to breakpoints ({@link Breakpoint} objects)
	 */
	private final Hashtable breakpoints;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Command that has been put into the queue for this client */
	private int nextCommand;

	/** Current depth of process call stack at the time the command has been issued */
	private int callDepth = 0;

	/** Qualified name of the process object we should step to */
	private String stepUntilPosition;

	/** Information regarding the currently halted process */
	private HaltInfo haltInfo;

	/** Stack of halt info objects to process (contains {@link HaltInfo} objects) */
	private final List stackedHaltInfos;

	/** Token context inspector */
	private ContextInspector inspector;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public DebuggerClient()
	{
		debuggerMode = Debugger.MODE_BREAK_ON_EXCEPTION;

		breakpoints = new Hashtable();
		stackedHaltInfos = new ArrayList();
	}

	/**
	 * Value constructor.
	 *
	 * @param clientId Id of the client
	 */
	public DebuggerClient(String clientId)
	{
		this();
		this.clientId = clientId;
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Saves the current depth of process call stack.
	 */
	public void saveCallDepth()
	{
		callDepth = haltInfo != null ? haltInfo.getContext().getCallStack().getCallDepth() : 0;
	}

	/**
	 * Resets the current depth of process call stack.
	 * @nowarn
	 */
	public void resetCallDepth()
	{
		callDepth = 0;
	}

	/**
	 * Gets the current depth of process call stack at the time the last command has been issued.
	 * @nowarn
	 */
	public int getCallDepth()
	{
		return callDepth;
	}

	/**
	 * Exposes the Java notification mechanism to the outside world, so the debugger can use the
	 * client object for synchronization purposes to continue the process execution.
	 */
	public synchronized void callNotify()
	{
		notify();
	}

	/**
	 * Exposes the Java notification mechanism to the outside world, so the debugger can use the
	 * client object for synchronization purposes to continue the process execution.
	 * @throws InterruptedException If the process can be continued
	 */
	public synchronized void callWait()
		throws InterruptedException
	{
		wait();
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the id of the client.
	 * @nowarn
	 */
	public String getClientId()
	{
		return clientId;
	}

	/**
	 * Sets the id of the client.
	 * @nowarn
	 */
	public void setClientId(String clientId)
	{
		this.clientId = clientId;
	}

	/**
	 * Gets the timeout for automatically unregistering the client, 0 to prevent unregistering.
	 * @nowarn
	 */
	public int getTimeout()
	{
		return timeout;
	}

	/**
	 * Sets the timeout for automatically unregistering the client, 0 to prevent unregistering.
	 * @nowarn
	 */
	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	/**
	 * Gets the timestamp of last node execution in seconds.
	 * @nowarn
	 */
	public long getTimestamp()
	{
		return timestamp;
	}

	/**
	 * Sets the timestamp of last node execution in seconds.
	 * @nowarn
	 */
	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}

	/**
	 * Sets the operation mode of the debugger.
	 *
	 * @param debuggerMode {@link Debugger#MODE_SKIP_SYSTEM_MODEL}|{@link Debugger#MODE_BREAK_ON_EXCEPTION}|{@link Debugger#MODE_BREAK_ON_TOP_LEVEL}|{@link Debugger#MODE_BREAK_ON_WORKFLOW}
	 * @throws OpenBPException If the client id is invalid
	 */
	public void setDebuggerMode(int debuggerMode)
	{
		this.debuggerMode = debuggerMode;
	}

	/**
	 * Gets the operation mode of the debugger.
	 *
	 * @return {@link Debugger#MODE_SKIP_SYSTEM_MODEL}|{@link Debugger#MODE_BREAK_ON_EXCEPTION}|{@link Debugger#MODE_BREAK_ON_TOP_LEVEL}|{@link Debugger#MODE_BREAK_ON_WORKFLOW}
	 * @throws OpenBPException If the client id is invalid
	 */
	public int getDebuggerMode()
	{
		return debuggerMode;
	}

	/**
	 * Checks if the client has an active breakpoint on the specified position.
	 * @param qualifier Reference to a node or a node socket
	 * @return The breakpoint or null if no such breakpoint exists or the breakpoint is disabled
	 */
	public Breakpoint getActiveBreakpoint(ModelQualifier qualifier)
	{
		Breakpoint bp = (Breakpoint) breakpoints.get(qualifier);
		if (bp != null)
		{
			if (bp.isEnabled())
				return bp;
		}
		return null;
	}

	/**
	 * Checks if the client has an active breakpoint on the specified position.
	 * @param qualifier Reference to a node or a node socket
	 * @return The breakpoint or null if no such breakpoint exists or the breakpoint is disabled
	 */
	public Breakpoint getActiveGlobalBreakpoint(ModelQualifier qualifier)
	{
		Breakpoint bp = (Breakpoint) breakpoints.get(qualifier);
		if (bp != null)
		{
			int state = bp.getState();
			if ((state & Breakpoint.STATE_DISABLED) != 0 && (state & Breakpoint.STATE_GLOBAL) != 0)
				return bp;
		}
		return null;
	}

	/**
	 * Adds or updates a breakpoint.
	 * @param qualifier Reference to a node or a node socket
	 * @param state Any combination of {@link Breakpoint#STATE_DISABLED} | {@link Breakpoint#STATE_GLOBAL} or 0
	 * @throws OpenBPException If the client id is invalid
	 * or if the process has been halted by another client
	 */
	public void setBreakpoint(ModelQualifier qualifier, int state)
	{
		Breakpoint bp = (Breakpoint) breakpoints.get(qualifier);
		if (bp != null)
		{
			// Existing breakpoints can't be temporary
			state &= ~ Breakpoint.STATE_TEMPORARY;

			bp.setState(state);
		}
		else
		{
			bp = new Breakpoint(qualifier, state);
			breakpoints.put(qualifier, bp);
		}
	}

	/**
	 * Clears a breakpoint.
	 * @param qualifier Reference to a node or a node socket
	 */
	public void clearBreakpoint(ModelQualifier qualifier)
	{
		breakpoints.remove(qualifier);
	}

	/**
	 * Gets a list of all breakpoints of a client.
	 * @return A list of {@link Breakpoint} objects or null if there are not breakpoints
	 * defined for this client
	 * @throws OpenBPException If the client id is invalid
	 * or if the process has been halted by another client
	 */
	public List getBreakpoints()
	{
		if (breakpoints.isEmpty())
			return null;

		List list = new ArrayList();

		for (Iterator it = breakpoints.values().iterator(); it.hasNext();)
		{
			Breakpoint bp = (Breakpoint) it.next();
			list.add(bp);
		}

		return list;
	}

	/**
	 * Changes the state of all breakpoints of the specified process.
	 * @param processPath Reference to the process
	 * or null to enable or disable all breakpoints of the client.
	 * @param state Any combination of {@link Breakpoint#STATE_DISABLED} | {@link Breakpoint#STATE_GLOBAL}
	 * @param set
	 *		true	Sets the specified state flags.<br>
	 *		false	Clears the specified state flags.
	 */
	public void updateBreakpoints(String processPath, int state, boolean set)
	{
		// Iterate all breakpoints and remove the breakpoints of the specified process
		for (Iterator it = breakpoints.values().iterator(); it.hasNext();)
		{
			Breakpoint bp = (Breakpoint) it.next();

			if (processPath != null)
			{
				String processName = bp.getQualifier().getItem();
				if (! processPath.equals(processName))
					continue;
			}

			bp.updateState(state, set);
		}
	}

	/**
	 * Clears all breakpoints of the specified process.
	 * @param processPath Reference to the process
	 * or null to clear all breakpoints
	 */
	public void clearBreakpoints(String processPath)
	{
		// Iterate all breakpoints and remove the breakpoints of the specified process
		for (Iterator it = breakpoints.values().iterator(); it.hasNext();)
		{
			Breakpoint bp = (Breakpoint) it.next();

			if (processPath != null)
			{
				String processName = bp.getQualifier().getItem();
				if (! processPath.equals(processName))
					continue;
			}

			it.remove();
		}
	}

	//////////////////////////////////////////////////
	// @@ Data member access
	//////////////////////////////////////////////////

	/**
	 * Gets the information regarding the currently halted process.
	 * @nowarn
	 */
	public HaltInfo getHaltInfo()
	{
		return haltInfo;
	}

	/**
	 * Adds information regarding a halted process to this client.
	 *
	 * @param position Current position in the halted process
	 * @param event Event that caused the process to halt
	 * @param context Token context currently assigned to this client
	 */
	public HaltInfo addHaltInfo(ModelQualifier position, EngineTraceEvent event, TokenContext context)
	{
		HaltInfo hi = new HaltInfo(position, event, context);

		if (haltInfo == null)
		{
			// We have no halted process currently, so this is our currently halted process
			haltInfo = hi;
		}
		else
		{
			synchronized (stackedHaltInfos)
			{
				// We already have a halted process. Save the halt info for later use.
				stackedHaltInfos.add(hi);
			}
		}

		return hi;
	}

	/**
	 * Resets the information on the currently halted process.
	 */
	public void resetHaltInfo()
	{
		haltInfo = null;

		synchronized (stackedHaltInfos)
		{
			if (stackedHaltInfos.size() > 0)
			{
				// Advance to the next halt info object in our list
				haltInfo = (HaltInfo) stackedHaltInfos.remove(0);
			}
		}
	}

	/**
	 * Gets the command that has been put into the queue for this client.
	 * @nowarn
	 */
	public int getNextCommand()
	{
		return nextCommand;
	}

	/**
	 * Sets the command that has been put into the queue for this client.
	 * @nowarn
	 */
	public void setNextCommand(int nextCommand)
	{
		this.nextCommand = nextCommand;
	}

	/**
	 * Gets the qualified name of the process object we should step to.
	 * @nowarn
	 */
	public String getStepUntilPosition()
	{
		return stepUntilPosition;
	}

	/**
	 * Sets the qualified name of the process object we should step to.
	 * @nowarn
	 */
	public void setStepUntilPosition(String stepUntilPosition)
	{
		this.stepUntilPosition = stepUntilPosition;
	}

	/**
	 * Gets the token context inspector.
	 * @nowarn
	 */
	public ContextInspector getInspector()
	{
		if (inspector == null)
		{
			// Instantiate a new inspector when needed
			inspector = new ContextInspector();
		}

		// Make sure the inspector is always connected to the current context
		inspector.setContext(haltInfo != null ? haltInfo.getContext() : null);

		return inspector;
	}

	//////////////////////////////////////////////////
	// @@ Helper classes
	//////////////////////////////////////////////////

	/**
	 * Container object that holds the information regarding a halted process.
	 */
	public class HaltInfo
	{
		/** Current position in the halted process */
		private ModelQualifier position;

		/** Event that caused the process to halt */
		private EngineTraceEvent event;

		/** Token context currently assigned to this client */
		private TokenContext context;

		/**
		 * Constructor.
		 *
		 * @param position Current position in the halted process
		 * @param event Event that caused the process to halt
		 * @param context Token context currently assigned to this client
		 */
		public HaltInfo(ModelQualifier position, EngineTraceEvent event, TokenContext context)
		{
			this.position = position;
			this.event = event;
			this.context = context;
		}

		/**
		 * Gets the current position in the halted process.
		 * @nowarn
		 */
		public ModelQualifier getPosition()
		{
			return position;
		}

		/**
		 * Sets the current position in the halted process.
		 * @nowarn
		 */
		public void setPosition(ModelQualifier position)
		{
			this.position = position;
		}

		/**
		 * Gets the event that caused the process to halt.
		 * @nowarn
		 */
		public EngineTraceEvent getEvent()
		{
			return event;
		}

		/**
		 * Sets the event that caused the process to halt.
		 * @nowarn
		 */
		public void setEvent(EngineTraceEvent event)
		{
			this.event = event;
		}

		/**
		 * Gets the token context currently assigned to this client.
		 * @nowarn
		 */
		public TokenContext getContext()
		{
			return context;
		}

		/**
		 * Sets the token context currently assigned to this client.
		 * @nowarn
		 */
		public void setContext(TokenContext context)
		{
			this.context = context;
		}
	}
}
