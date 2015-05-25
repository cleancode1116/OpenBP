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
package org.openbp.server.context;

import java.util.Iterator;
import java.util.Map;

import org.openbp.common.property.RuntimeAttributeContainer;
import org.openbp.common.util.observer.EventObserver;
import org.openbp.common.util.observer.EventObserverMgr;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.Model;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.modelmgr.ModelMgr;
import org.openbp.server.engine.EngineEvent;
import org.openbp.server.engine.EngineTraceEvent;
import org.openbp.server.persistence.PersistentObject;

/**
 * The context for process execution.
 * The token context contains all information that reflects
 * the current state of the process.
 *
 * This includes process execution data, the current global and
 * socket parameter values of the process, i/o etc.
 *
 * The TokenContext interface now extends the RuntimeAttribute interface.
 * This provides the ability to store arbitrary objects in a map.
 * However, the map won't be serialized to persistent storage.
 * When a context is executed by the engine, the thread executing the context is saved as runtime attribute
 * under the key {@link TokenContext#RUNTIME_ATTRIBUTE_THREAD}.
 * This feature may be used to terminate hanging threads by force in emergency situations.
 *
 * @author Heiko Erhardt
 */
public interface TokenContext
	extends PersistentObject, RuntimeAttributeContainer
{
	//////////////////////////////////////////////////
	// @@ constants
	//////////////////////////////////////////////////

	/**
	 * Runtime attribute that holds the thread that is currently executing this context.
	 * Contains a java.lang.Thread object.
	 */
	public static final String RUNTIME_ATTRIBUTE_THREAD = "Thread";

	//////////////////////////////////////////////////
	// @@ Context hierarchy
	//////////////////////////////////////////////////

	/**
	 * Gets the parent context.
	 * @nowarn
	 */
	public TokenContext getParentContext();

	/**
	 * Sets the parent context.
	 * @nowarn
	 */
	public void setParentContext(TokenContext parentContext);

	/**
	 * Adds a child context to this context.
	 *
	 * @param childContext Child context
	 */
	public void addChildContext(TokenContext childContext);

	/**
	 * Removes a child context.
	 *
	 * @param childContext Child context to remove
	 */
	public void removeChildContext(TokenContext childContext);

	/**
	 * Checks if this token has any child tokens.
	 * @nowarn
	 */
	public boolean hasChildContext();

	/**
	 * Gets the list of child contexts.
	 * @return An iterator of {@link TokenContext} objects
	 */
	public Iterator getChildContexts();

	//////////////////////////////////////////////////
	// @@ Execution parameters
	//////////////////////////////////////////////////

	/**
	 * Gets the base model under which the processes using this context executes.
	 * The executing model will be used for item lookups.
	 *
	 * @return The executing model
	 */
	public Model getExecutingModel();

	/**
	 * Sets the base model under which the processes using this context executes.
	 * For internal use only!
	 * @nowarn
	 */
	public void setExecutingModel(Model executingModel);

	//////////////////////////////////////////////////
	// @@ Execution control
	//////////////////////////////////////////////////

	/**
	 * Gets the current socket of the node that was executed recently.
	 * This value is valid during node execution only.
	 * Before node execution this will point to the entry, after node execution
	 * to the exit socket.
	 * @return The entry socket or null if there is no current entry
	 */
	public NodeSocket getCurrentSocket();

	/**
	 * Sets the current entry socket of the node that was executed recently.
	 * This value is valid only during node execution only.
	 * Before node execution this will point to the entry, after node execution
	 * to the exit socket.
	 * @param currentSocket The entry socket or null if there is no current entry
	 */
	public void setCurrentSocket(NodeSocket currentSocket);

	/**
	 * Gets the sub process call stack.
	 * The call stack contains the position in the calling process
	 * (the input socket of the calling sub process node, {@link NodeSocket} objects).
	 *
	 * @nowarn
	 */
	public CallStack getCallStack();

	/**
	 * Sets the sub process call stack.
	 * The call stack contains the position in the calling process
	 * (the input socket of the calling sub process node, {@link NodeSocket} objects).
	 *
	 * @nowarn
	 */
	public void setCallStack(CallStack callStack);

	/**
	 * Gets the lifecycle state.
	 * @return See the constants of the {@link LifecycleState} class
	 */
	public int getLifecycleState();

	/**
	 * Sets the lifecycle state.
	 * This method is to be used internally only by the OpenBP engine.
	 * In order to trigger a change of the lifecycle state, use {@link #setLifecycleRequest}.
	 * @param lifecycleState See the constants of the {@link LifecycleState} class
	 */
	public void setLifecycleState(int lifecycleState);

	/**
	 * Gets the lifecycle request.
	 *
	 * @return See the constants of the {@link LifecycleRequest} class
	 */
	public int getLifecycleRequest();

	/**
	 * Sets the lifecycle request.
	 *
	 * @param lifecycleRequest See the constants of the {@link LifecycleRequest} class
	 */
	public void setLifecycleRequest(int lifecycleRequest);

	/**
	 * Waits until the specified lifecycle request has been set (blocking).
	 *
	 * @param lifecycleRequest Lifecycle request to wait for
	 */
	public void waitLifecycleRequest(int lifecycleRequest);

	/**
	 * Gets the priority (0 = highest).
	 * @nowarn
	 */
	public int getPriority();

	/**
	 * Sets the priority (0 = highest).
	 * @nowarn
	 */
	public void setPriority(int priority);

	/**
	 * Gets the type of queue for the current node.
	 * The queue type may be used to process different node or activity types by different engine instances (e. g. different servers).
	 * @nowarn
	 */
	public String getQueueType();

	/**
	 * Sets the type of queue for the current node.
	 * The queue type may be used to process different node or activity types by different engine instances (e. g. different servers).
	 * @nowarn
	 */
	public void setQueueType(String queueType);

	/**
	 * Gets the name of the cluster node that currently processes this context.
	 * Valid only for token contexts that have the lifecycle state SELECTED or RUNNING.
	 * @nowarn
	 */
	public String getNodeId();

	/**
	 * Sets the name of the cluster node that currently processes this context.
	 * @nowarn
	 */
	public void setNodeId(String nodeId);

	/**
	 * Gets the user who owns this token context (optional).
	 * @nowarn
	 */
	public String getUserId();

	/**
	 * Sets the user who owns this token context (optional).
	 * @nowarn
	 */
	public void setUserId(String userId);

	/**
	 * Gets the progress info.
	 * @nowarn
	 */
	public ProgressInfo getProgressInfo();

	/**
	 * Sets the progress info.
	 * @nowarn
	 */
	public void setProgressInfo(ProgressInfo progressInfo);

	/**
	 * Gets the serialized context data.
	 * For internal use only.
	 * @nowarn
	 */
	public byte[] getContextData();

	/**
	 * Sets the serialized context data.
	 * For internal use only.
	 * @nowarn
	 */
	public void setContextData(final byte[] serializedContextData);

	//////////////////////////////////////////////////
	// @@ Debugging support
	//////////////////////////////////////////////////

	/**
	 * Gets the id of the debugger that controls this process' execution.
	 * This id must be an id returned by the Debugger.registerClient method.
	 *
	 * @return The debugger id or null if no debugger is associated with the process
	 */
	public String getDebuggerId();

	/**
	 * Sets the id of the debugger that controls this process' execution.
	 * This id must be an id returned by the Debugger.registerClient method.
	 *
	 * @param debuggerId The debugger id or null if no debugger is to be associated with the process
	 */
	public void setDebuggerId(String debuggerId);

	//////////////////////////////////////////////////
	// @@ Parameter access
	//////////////////////////////////////////////////

	/**
	 * Checks if the specified parameter exists.
	 *
	 * @param qualParamName Qualified parameter name ("node.socket.paramName")
	 * @nowarn
	 */
	public boolean hasParamValue(String qualParamName);

	/**
	 * Retrieves the value of the specified parameter.
	 *
	 * @param qualParamName Qualified parameter name ("node.socket.paramName")
	 * @return The parameter value or null if no such parameter exists
	 *
	 */
	public Object getParamValue(String qualParamName);

	/**
	 * Sets the value of the specified parameter.
	 *
	 * @param qualParamName Qualified parameter name ("node.socket.paramName")
	 * @param value Param value
	 */
	public void setParamValue(String qualParamName, Object value);

	/**
	 * Removes the specified parameter value.
	 *
	 * @param qualParamName Qualified parameter name ("node.socket.paramName")
	 */
	public void removeParamValue(String qualParamName);

	/**
	 * Removes all parameters values from the context.
	 */
	public void clearParamValues();

	/**
	 * Gets the node parameters (maps node-qualified parameter names (Strings)
	 * to parameter values (Objects).
	 * 
	 * @nowarn
	 */
	public Map getParamValues();

	//////////////////////////////////////////////////
	// @@ Process variables
	//////////////////////////////////////////////////

	/**
	 * Creates a new persistent process variable.
	 * Does nothing if the process variable already exists in this token or one of its parent tokens.
	 * Persistent process variables always have 'context' scope.
	 *
	 * @param variableName Name of the new process variable
	 * @param isPersistent true to create a persistent process variable, false for a transient one
	 */
	public void createProcessVariable(final String variableName, final boolean isPersistent);

	/**
	 * Checks if the process variable exists.
	 * This method will search for the process variable in the token context hierarchy.
	 *
	 * @param variableName Name of the process variable
	 * @nowarn
	 */
	public boolean hasProcessVariableValue(String variableName);

	/**
	 * Gets the value of a process variable.
	 * This method will search for the process variable in the token context hierarchy.
	 *
	 * @param variableName Name of the process variable
	 * @return The value or null if no such global exists
	 * @throws OpenBPException if the process variable is undefined and the
	 * openbp.processVariableHandling.strict system property has been set
	 */
	public Object getProcessVariableValue(String variableName);

	/**
	 * Sets the value of a process variable.
	 * Note that this applies to process variables of scope 'token context' and 'process' only.
	 *
	 * @param variableName Name of the process variable
	 * @param value Parameter value
	 * @throws OpenBPException if the process variable is undefined
	 */
	public void setProcessVariableValue(String variableName, Object value);

	/**
	 * Removes a process variable.
	 * This method will remove the process variable from the token context.
	 *
	 * @param variableName Name of the process variable
	 */
	public void removeProcessVariableValue(String variableName);

	/**
	 * Gets the names of all process variables that are not null.
	 *
	 * @return An iterator of strings
	 */
	public Iterator getProcessVariableNames();

	//////////////////////////////////////////////////
	// @@ RuntimeAttributeContainer implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the runtime attribute table.
	 * @nowarn
	 */
	public Map getRuntimeAttributes();

	/**
	 * Gets the model manager.
	 * @nowarn
	 */
	public ModelMgr getModelMgr();

	//////////////////////////////////////////////////
	// @@ Engine observation
	//////////////////////////////////////////////////

	/**
	 * Registers an observer.
	 *
	 * @param observer The observer; The observer's observeEvent method will receive events of the type
	 * {@link EngineEvent} or {@link EngineTraceEvent}.
	 * @param eventTypes Lit of event types the observer wants to be notified of
	 * or null for all event types
	 */
	public void registerObserver(final EventObserver observer, final String[] eventTypes);

	/**
	 * Unregisters an observer.
	 *
	 * @param observer The observer
	 */
	public void unregisterObserver(final EventObserver observer);

	/**
	 * Suspends broadcasting of engine events.
	 *
	 * @return The previous suspend status
	 */
	public boolean suspendEngineEvents();

	/**
	 * Resumes broadcasting of engine events.
	 */
	public void resumeEngineEvents();

	/**
	 * Checks if there are active engine event observers registered.
	 *
	 * @param eventType Type of event in question
	 * @return true if there is at least one observer
	 */
	public boolean hasActiveObservers(final String eventType);

	/**
	 * Notifies all registered observers about a engine event (for internal use only).
	 *
	 * @param event Engine event to dispatch
	 */
	public void fireEngineEvent(final EngineEvent event);

	/**
	 * Gets the engine observer manager that is local to this token.
	 * @nowarn
	 */
	public EventObserverMgr getObserverMgr();

	/**
	 * Sets the engine observer manager that is local to this token.
	 * @nowarn
	 */
	public void setObserverMgr(EventObserverMgr observerMgr);
}
