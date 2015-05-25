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

import java.util.List;

import org.openbp.common.generic.LifecycleSupport;
import org.openbp.core.OpenBPException;
import org.openbp.core.engine.debugger.Breakpoint;
import org.openbp.core.engine.debugger.CallStackInfo;
import org.openbp.core.engine.debugger.DebuggerEvent;
import org.openbp.core.engine.debugger.DebuggerService;
import org.openbp.core.engine.debugger.ObjectMemberInfo;
import org.openbp.core.model.ModelQualifier;

/**
 * Debugger service.
 * The debugger service controls the process debugger.
 * The service contains methods to issue debugger command and query the execution and debugging
 * status of a process.
 *
 * @author Erich Lauterbach
 */
public class DebuggerServiceImpl
	implements DebuggerService, LifecycleSupport
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** The debugger object */
	private Debugger debugger;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 * @throws OpenBPException Never
	 */
	public DebuggerServiceImpl()
	{
	}

	// For Spring framework support
	public void setDebugger(Debugger debugger)
	{
		this.debugger = debugger;
	}

	/**
	 * Implementation of {@link LifecycleSupport#initialize()}.
	 */
	public void initialize()
	{
		if (debugger instanceof LifecycleSupport)
		{
			((LifecycleSupport) debugger).initialize();
		}
	}

	/**
	 * Implementation of {@link LifecycleSupport#shutdown()}.
	 */
	public void shutdown()
	{
		if (debugger instanceof LifecycleSupport)
		{
			((LifecycleSupport) debugger).shutdown();
		}
	}

	//////////////////////////////////////////////////
	// @@ Debugger client registration
	//////////////////////////////////////////////////

	/**
	 * Registers a debugger client.
	 * The debugger client will be automatically unregistered when the timeout expires.
	 *
	 * @param clientId The client id or null in order to generate one
	 * @param timeout Timeout in seconds or 0 for no automatic unregistering
	 *
	 * @return The client id (must be passed in subsequent calls to the debugger)
	 * @throws OpenBPException If a client with this name has already registered
	 */
	public String registerClient(String clientId, int timeout)
	{
		return debugger.registerClient(clientId, timeout);
	}

	/**
	 * Checks if a debugger client has been registered.
	 *
	 * @param clientId The client id
	 *
	 * @return true of this client has been registered
	 */
	public boolean isClientRegistered(String clientId)
	{
		return debugger.isClientRegistered(clientId);
	}

	/**
	 * Removes the client from the table of registered clients.
	 * Also removes (cleanup) all breakpoints set by this client.
	 *
	 * @param clientId Id of the client returned by {@link #registerClient}
	 * @throws OpenBPException If the client id is invalid or unregistering failed
	 */
	public void unregisterClient(String clientId)
	{
		debugger.unregisterClient(clientId);
	}

	/**
	 * Removes all clients from the table of registered clients.
	 * Also removes all breakpoints.
	 * @throws OpenBPException If unregistering failed
	 */
	public void unregisterAllClients()
	{
		debugger.unregisterAllClients();
	}

	//////////////////////////////////////////////////
	// @@ Process engine trace
	//////////////////////////////////////////////////

	/**
	 * Kills all processes that run under control of this client.
	 *
	 * @param clientId Id of the client
	 * @throws OpenBPException If the client id is invalid
	 */
	public void killProcesses(String clientId)
	{
		debugger.killProcesses(clientId);
	}

	/**
	 * Kills a halted processes - if any - of a client.
	 *
	 * @param clientId Id of the client
	 * @throws OpenBPException If the client id is invalid
	 */
	public void killHaltedProcess(String clientId)
	{
		debugger.killHaltedProcess(clientId);
	}

	/**
	 * Aborts the execution of a process.
	 *
	 * @param clientId Id of the client
	 * @return The event
	 * @throws OpenBPException If the client id is invalid
	 */
	public DebuggerEvent getEvent(String clientId)
	{
		return debugger.getEvent(clientId);
	}

	/**
	 * Aborts the execution of a process.
	 *
	 * @param clientId Id of the client
	 * @throws OpenBPException If the client id is invalid
	 */
	public void stop(String clientId)
	{
		debugger.stop(clientId);
	}

	/**
	 * Steps until the next event.
	 * Stops before the next socket.
	 *
	 * @param clientId Id of the client
	 * @throws OpenBPException If the client id is invalid
	 */
	public void stepNext(String clientId)
	{
		debugger.stepNext(clientId);
	}

	/**
	 * Steps into call or jump nodes or steps just one socket (stepNext)
	 *
	 * @param clientId Id of the client
	 * @throws OpenBPException If the client id is invalid
	 */
	public void stepInto(String clientId)
	{
		debugger.stepInto(clientId);
	}

	/**
	 * Steps over call or jump nodes or step just one socket (stepNext)
	 *
	 * @param clientId Id of the client
	 * @throws OpenBPException If the client id is invalid
	 */
	public void stepOver(String clientId)
	{
		debugger.stepOver(clientId);
	}

	/**
	 * Continues execution of a process until it's end (step out).
	 *
	 * @param clientId Id of the client
	 * @throws OpenBPException If the client id is invalid
	 */
	public void stepOut(String clientId)
	{
		debugger.stepOut(clientId);
	}

	/**
	 * Steps until a particular position.
	 * The client actually should already have reserved the process to
	 * debug because of {@link #setBreakpoint}
	 *
	 * @param clientId Id of the client
	 * @param position Reference to a process object
	 * @throws OpenBPException If the client id or the position is invalid
	 */
	public void stepUntil(String clientId, String position)
	{
		debugger.stepUntil(clientId, position);
	}

	/**
	 * Continues a halted process.
	 *
	 * @param clientId Id of the client
	 * @throws OpenBPException If the client id is invalid
	 */
	public void run(String clientId)
	{
		debugger.run(clientId);
	}

	//////////////////////////////////////////////////
	// @@ Breakpoint control
	//////////////////////////////////////////////////

	/**
	 * Sets the operation mode of the debugger.
	 *
	 * @param clientId Id of the client
	 * @param debuggerMode {@link DebuggerService#MODE_SKIP_SYSTEM_MODEL}|
	 * {@link DebuggerService#MODE_BREAK_ON_EXCEPTION}|{@link DebuggerService#MODE_BREAK_ON_TOP_LEVEL}|{@link Debugger#MODE_BREAK_ON_WORKFLOW}
	 * @throws OpenBPException If the client id is invalid
	 */
	public void setDebuggerMode(String clientId, int debuggerMode)
	{
		debugger.setDebuggerMode(clientId, debuggerMode);
	}

	/**
	 * Gets the operation mode of the debuggerService.
	 *
	 * @param clientId Id of the client
	 * @return {@link DebuggerService#MODE_SKIP_SYSTEM_MODEL}|
	 * {@link DebuggerService#MODE_BREAK_ON_EXCEPTION}|{@link DebuggerService#MODE_BREAK_ON_TOP_LEVEL}|{@link Debugger#MODE_BREAK_ON_WORKFLOW}
	 * @throws OpenBPException If the client id is invalid
	 */
	public int getDebuggerMode(String clientId)
	{
		return debugger.getDebuggerMode(clientId);
	}

	/**
	 * Adds or updates a breakpoint.
	 * @param clientId Id of the client
	 * @param qualifier Reference to a node or a node socket
	 * @param state Any combination of {@link Breakpoint#STATE_DISABLED} | {@link Breakpoint#STATE_GLOBAL} or 0
	 * @throws OpenBPException If the client id is invalid
	 * or if the process has been halted by another client
	 */
	public void setBreakpoint(String clientId, ModelQualifier qualifier, int state)
	{
		debugger.setBreakpoint(clientId, qualifier, state);
	}

	/**
	 * Clears a breakpoint.
	 * @param clientId Id of the client
	 * @param qualifier Reference to a node or a node socket
	 * @throws OpenBPException If the client id is invalid
	 */
	public void clearBreakpoint(String clientId, ModelQualifier qualifier)
	{
		debugger.clearBreakpoint(clientId, qualifier);
	}

	/**
	 * Gets a list of all breakpoints of a client.
	 * @param clientId Id of the client
	 * @return A list of {@link Breakpoint} objects or null if there are not breakpoints
	 * defined for this client
	 * @throws OpenBPException If the client id is invalid
	 * or if the process has been halted by another client
	 */
	public List getBreakpoints(String clientId)
	{
		return debugger.getBreakpoints(clientId);
	}

	/**
	 * Changes the state of all breakpoints of the specified process.
	 * @param clientId Id of the client
	 * @param qualifiedProcessName Reference to the process
	 * or null to clear all breakpoints of the client.
	 * @param state Any combination of {@link Breakpoint#STATE_DISABLED} | {@link Breakpoint#STATE_GLOBAL}
	 * @param set
	 *		true	Sets the specified state flags.<br>
	 *		false	Clears the specified state flags.
	 * @throws OpenBPException If the client id is invalid
	 * or if the process has been halted by another client
	 */
	public void updateBreakpoints(String clientId, String qualifiedProcessName, int state, boolean set)
	{
		debugger.updateBreakpoints(clientId, qualifiedProcessName, state, set);
	}

	/**
	 * Clears all breakpoints of the specified process.
	 * @param clientId Id of the client
	 * @param qualifiedProcessName Reference to the process
	 * or null to clear all breakpoints of the client.
	 * @throws OpenBPException If the client id is invalid
	 */
	public void clearBreakpoints(String clientId, String qualifiedProcessName)
	{
		debugger.clearBreakpoints(clientId, qualifiedProcessName);
	}

	//////////////////////////////////////////////////
	// @@ Context inspection
	//////////////////////////////////////////////////

	/**
	 * Retrieves information about a parameter of the token context or a member
	 * of a particular parameter object within the object hierarchy of the parameter.
	 *
	 * @param clientId Id of the client
	 *
	 * @param contextPath Path of the context object we are refering to.<br>
	 * The path must specify an existing context parameter.
	 *
	 * @param expression This expression may refer to a member of the parameter
	 * (e. g. contextPath = "CreateClient.Out.Client", expression = "User.Profile"
	 * will return the 'Profile' member of the 'User' object of the created client.
	 *
	 * @return The object member information or null if the request could not be resolved
	 * @throws OpenBPException If the client id or the expression are invalid
	 */
	public ObjectMemberInfo getObjectValue(String clientId, String contextPath, String expression)
	{
		return debugger.getObjectValue(clientId, contextPath, expression);
	}

	/**
	 * Retrieves information about parameters of the token context or the members
	 * of a particular parameter object within the object hierarchy of the parameter.
	 *
	 * @param clientId Id of the client
	 *
	 * @param contextPath Path of the context object we are refering to:<br>
	 * If the path is null, all parameters of the context will be returned.
	 * If the path specifies the full path of a context object (e. g. "node.socket.param"),
	 * the method returns all members of the object.<br>
	 * Otherwise, all parameters beginning with the specified path will be returned.
	 *
	 * @param expression if the 'contextPath' referes to a particular parameter, this expression
	 * may refer to a member of this parameter (e. g. contextPath = "CreateClient.Out.Client",
	 * expression = "User.Profile" will return all members of the 'Profile' member of the
	 * 'User' object of the created client.
	 *
	 * @return A list of {@link ObjectMemberInfo} objects or null if the request could not be resolved
	 * @throws OpenBPException If the client id or the expression are invalid
	 */
	public List getObjectMembers(String clientId, String contextPath, String expression)
	{
		return debugger.getObjectMembers(clientId, contextPath, expression);
	}

	/**
	 * Retrieves information about the current state of the call stack.
	 *
	 * @param clientId Id of the client
	 * @return A list of {@link CallStackInfo} objects or null if the call stack is empty
	 * @throws OpenBPException If the client id or the expression are invalid
	 */
	public List getCallStackElements(String clientId)
	{
		return debugger.getCallStackElements(clientId);
	}
}
