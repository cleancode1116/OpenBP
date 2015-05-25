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
package org.openbp.server.engine;

import org.openbp.common.util.observer.EventObserver;
import org.openbp.core.OpenBPException;
import org.openbp.core.handler.HandlerDefinition;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.modelmgr.ModelMgr;
import org.openbp.server.context.LifecycleRequest;
import org.openbp.server.context.SessionRegistry;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextService;
import org.openbp.server.context.WorkflowTask;
import org.openbp.server.engine.script.ScriptEngineFactory;
import org.openbp.server.handler.HandlerContext;
import org.openbp.server.persistence.PersistenceContextProvider;

/**
 * The OpenBP process engine.
 * Call the {@link #commitTokenContextTransaction} after calling the {@link #startToken}, {@link #resumeToken} or
 * {@link #resumeWorkflow} methods.
 *
 * @author Heiko Erhardt
 */
public interface Engine
{
	//////////////////////////////////////////////////
	// @@ API: Token and workflow invocation
	//////////////////////////////////////////////////

	/**
	 * Starts the given token at its current position.
	 * The token is marked for further execution by the process engine.
	 * Note that the current position within the token must have been set before.
	 * The method commits the changes to the database.
	 *
	 * @param context Token context
	 */
	public void startToken(TokenContext context);

	/**
	 * Resumes the given token at its current position.
	 * The token is marked for further execution by the process engine.
	 * The method commits the changes to the database.
	 *
	 * @param context Token context
	 */
	public void resumeToken(TokenContext context);

	/**
	 * Resumes a suspended workflow.
	 * - Assigns the workflow to the current user if desired.<br>
	 * - Sets the status of the workflow to {@link WorkflowTask#STATUS_RESUMED}.<br>
	 * - Set the AcceptingUserId and TimeAccepted properties of the workflow task.<br>
	 * - Determines the socket of the workflow node to resume at.<br>
	 * The lifecycle request of the token will be in state {@link LifecycleRequest#RESUME}.
	 * The method commits the changes to the database.
	 *
	 * @param workflowTask Workflow task this workflow refers to
	 * @param socketName Name of the socket of the workflow node to continue with or null for the default socket
	 * @param currentUserId Id of the user that accepts this workflow (may be null);
	 * will be assigned to the 'AcceptingUser' property of the workflow and to the 'UserId' of the workflow
	 * if the 'AssignToCurrentUser' property of the workflow has been set.
	 * @throws OpenBPException Any exception that may occur during start of the preparation of the workflow task
	 */
	public void resumeWorkflow(WorkflowTask workflowTask, String socketName, String currentUserId);

	/**
	 * Simply saves the given token as suspended token.
	 * Can be used to save token that shall be executed by the scheduler to the database without starting them.
	 * The method commits the changes to the database.
	 *
	 * @param context Token context
	 */
	public void prepareTokenForScheduler(TokenContext context);

	/**
	 * Commits the transaction on the token context store.
	 */
	public void commitTokenContextTransaction();

	/**
	 * Rolls back the transaction on the token context store.
	 */
	public void rollbackTokenContextTransaction();

	//////////////////////////////////////////////////
	// @@ Internal: Token lifecycle
	//////////////////////////////////////////////////

	/**
	 * Begins the token (for internal use only).
	 * For internal use only!
	 * Raises the engine event {@link EngineEvent#BEGIN_TOKEN}.
	 *
	 * @param context Context that holds the token's state information
	 */
	public void beginToken(TokenContext context);

	/**
	 * Ends the token (for internal use only).
	 * For internal use only! In order to end a token, call the {@link TokenContext#setLifecycleRequest} method with the LifecycleRequest.STOP argument.
	 * Ends all it's child contexts and detaches it from it's parent context.
	 * Also sets the state of the workflow task that is associated with this context (if any) to {@link WorkflowTask#STATUS_COMPLETED}.
	 * Also raises the engine events {@link EngineEvent#BEFORE_END_TOKEN} and {@link EngineEvent#AFTER_END_TOKEN}.
	 *
	 * @param context Context that holds the token's state information
	 */
	public void endToken(TokenContext context);

	/**
	 * Changes the lifecycle state and lifecycle request of the given token.
	 * Also raises the engine event {@link EngineEvent#TOKEN_STATE_CHANGE}.
	 *
	 * @param context Context that holds the token's state information
	 * @param lifecycleState Lifecycle state
	 * @param lifecycleRequest Lifecycle request
	 */
	public void changeTokenState(TokenContext context, int lifecycleState, int lifecycleRequest);

	//////////////////////////////////////////////////
	// @@ Internal: Token execution
	//////////////////////////////////////////////////

	/**
	 * Executes the given context in this thread.
	 * The execution will end at the next transaction boundary 
	 *
	 * @param context Context
	 */
	public void executeContext(TokenContext context);

	/**
	 * Executes a handler, if any (for internal use only).
	 *
	 * Internal method, do not call from the application program.
	 *
	 * @param handlerDef Handler definition or null
	 * @param eventType Event type
	 * @param context Token context
	 * @param currentSocket Current socket or null in order to obtain from the token context
	 * @param nextSocket Next socket or null in order to use the default exit socket of the current node
	 * @return The handler context or null if no handler has been specified
	 * @throws OpenBPException In case of handler failure
	 */
	public HandlerContext executeHandler(HandlerDefinition handlerDef, String eventType, TokenContext context,
		NodeSocket currentSocket, NodeSocket nextSocket);

	/**
	 * Notifies all registered observers about a engine event.
	 *
	 * Internal method, do not call from the application program.
	 *
	 * @param event Engine event to dispatch
	 */
	public void fireEngineEvent(final EngineEvent event);

	/**
	 * Checks if there are active engine event observers registered.
	 *
	 * Internal method, do not call from the application program.
	 *
	 * @param eventType Type of event in question
	 * @param context Token context of current operation; may be configured to supply a token-local observer
	 * @return true if there is at least one observer
	 */
	public boolean hasActiveObservers(final String eventType, TokenContext context);

	//////////////////////////////////////////////////
	// @@ Socket search
	//////////////////////////////////////////////////

	/**
	 * Resolves an explicit or implicit reference to an exit socket.
	 * This method is used wherever an exit socket is referenced by its name.
	 * The reference can be absolute (starting with "/") or relative.
	 * The following formats are supported:
	 *
	 * <ul>
	 * <li>"[/Model]/Process": Default exit socket of the default initial node of the referenced process</li>
	 * <li>"[/Model]/Process.Node": Default exit socket of the referenced initial node</li>
	 * <li>"[/Model]/Process.Node.Socket": Fully qualified reference</li>
	 * <li>"Node.Socket": Referenced node/socket of the current process</li>
	 * <li>".Socket": Referenced socket of the current node</li>
	 * <li>"Socket": Referenced socket of the current node</li>
	 * <li>null: Default exit socket of the current node</li>
	 * </ul>
	 *
	 * @param ref Reference of the socket to retrieve
	 * @param currentSocket Current socket
	 * @param context Token context
	 * @param mustExist If true the method will throw an exception if the specified socket does not exist.
	 * @return The socket or null if no such socket could be found
	 * @throws OpenBPException If<br>
	 * - A fully qualified reference was supplied and the specified process/node/socket does not exist.<br>
	 * - The mustExist flag was specified and no such socket or no default socket could be found.
	 */
	public NodeSocket resolveSocketRef(String ref, NodeSocket currentSocket, TokenContext context, boolean mustExist);

	//////////////////////////////////////////////////
	// @@ Engine observation
	//////////////////////////////////////////////////

	/**
	 * Registers an observer.
	 *
	 * @param observer The observer; The observer's observeEvent method will receive events of the type
	 * {@link EngineEvent} or {@link EngineTraceEvent}
	 * @param eventTypes Lit of event types the observer wants to be notified of
	 * or null for all event types
	 */
	public void registerObserver(EventObserver observer, String[] eventTypes);

	/**
	 * Unregisters an observer.
	 *
	 * @param observer The observer
	 */
	public void unregisterObserver(EventObserver observer);

	/**
	 * Gets the session mode.
	 * @return {@link SessionMode#MANUAL}/{@link SessionMode#AUTO}
	 */
	public int getSessionMode();

	/**
	 * Gets the flag that determines if an automatic rollback should be performed on unhandled errors.
	 * @nowarn
	 */
	public boolean isRollbackOnError();

	/**
	 * Sets the flag that determines if an automatic rollback should be performed on unhandled errors.
	 * @nowarn
	 */
	public void setRollbackOnError(boolean rollbackOnError);

	//////////////////////////////////////////////////
	// @@ Template methods
	//////////////////////////////////////////////////

	/**
	 * Template method that determines if an exception is a fatal exception.
	 * If a fatal exception occurs when executing a process, the engine will not try to perform
	 * a rollback or to persist the process state when terminating the process execution.
	 * A typical example would be an out of memory error or a lost connection to the database.
	 * The default implementation of this method will always return false.
	 *
	 * @param t Exception to check
	 * @return true The exception is a fatal exception, false otherwise
	 */
	public boolean isFatalException(Throwable t);

	//////////////////////////////////////////////////
	// @@ Sub components
	//////////////////////////////////////////////////

	/**
	 * Gets the model manager.
	 * @nowarn
	 */
	public ModelMgr getModelMgr();

	/**
	 * Gets the token context management service.
	 * @nowarn
	 */
	public TokenContextService getTokenContextService();

	/**
	 * Gets the persistence context provider.
	 * @nowarn
	 */
	public PersistenceContextProvider getPersistenceContextProvider();

	/**
	 * Gets the session registry.
	 * @nowarn
	 */
	public SessionRegistry getSessionRegistry();

	/**
	 * Gets the script engine factory.
	 * @nowarn
	 */
	public ScriptEngineFactory getScriptEngineFactory();

	/**
	 * Gets the model object executor manager.
	 * @nowarn
	 */
	public ModelObjectExecutorMgr getModelObjectExecutorMgr();
}
