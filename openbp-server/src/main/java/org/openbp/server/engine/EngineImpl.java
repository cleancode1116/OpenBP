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

import java.sql.Timestamp;
import java.util.Iterator;

import org.openbp.common.logger.LogUtil;
import org.openbp.common.util.observer.EventObserver;
import org.openbp.common.util.observer.EventObserverMgr;
import org.openbp.core.OpenBPException;
import org.openbp.core.engine.EngineException;
import org.openbp.core.handler.HandlerDefinition;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.InitialNode;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.WorkflowNode;
import org.openbp.core.model.modelmgr.ModelMgr;
import org.openbp.server.context.LifecycleRequest;
import org.openbp.server.context.LifecycleState;
import org.openbp.server.context.SessionRegistry;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextService;
import org.openbp.server.context.WorkflowTask;
import org.openbp.server.context.WorkflowTaskCriteria;
import org.openbp.server.engine.script.ScriptEngine;
import org.openbp.server.engine.script.ScriptEngineFactory;
import org.openbp.server.handler.Handler;
import org.openbp.server.handler.HandlerContext;
import org.openbp.server.persistence.PersistenceContextProvider;

/**
 * Default implementation of the OpenBP process engine.
 * This implementation does not perform any commit after an operation.
 * Call the {@link #commitTokenContextTransaction} after calling the {@link #startToken}, {@link #resumeToken} or
 * {@link #resumeWorkflow} methods.
 *
 * @author Heiko Erhardt
 */
public class EngineImpl
	implements Engine
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Session mode */
	private int sessionMode = SessionMode.MANUAL;

	/** Flag that determines if an automatic rollback should be performed on unhandled errors */
	private boolean rollbackOnError = true;

	/** Model manager */
	private ModelMgr modelMgr;

	/** Token context management service */
	private TokenContextService tokenContextService;

	/** Persistence context provider */
	private PersistenceContextProvider persistenceContextProvider;

	/** Session registry */
	private SessionRegistry sessionRegistry;

	/** Script engine factory */
	private ScriptEngineFactory scriptEngineFactory;

	/** Engine observer manager */
	private EventObserverMgr observerMgr;

	/** Model object executor manager */
	private ModelObjectExecutorMgr modelObjectExecutorMgr;

	//////////////////////////////////////////////////
	// @@ Construction and data members
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public EngineImpl()
	{
		modelObjectExecutorMgr = new ModelObjectExecutorMgr(this);
	}

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
	public void startToken(final TokenContext context)
	{
		LogUtil.trace(getClass(), "Requesting start of token. [{0}]", context);

		int lifecycleState = context.getLifecycleState();
		if (lifecycleState != LifecycleState.CREATED)
		{
			String msg = LogUtil.error(getClass(), "Token has invalid lifecycle state for operation startToken. [{0}]", context);
			throw new OpenBPException("InvalidLifecycleState", msg);
		}

		beginToken(context);
		context.setLifecycleRequest(LifecycleRequest.RESUME);
		tokenContextService.addContext(context);
		flushTokenContextChanges();
	}

	/**
	 * Resumes the given token at its current position.
	 * The token is marked for further execution by the process engine.
	 * The method commits the changes to the database.
	 *
	 * @param context Token context
	 */
	public void resumeToken(final TokenContext context)
	{
		LogUtil.trace(getClass(), "Requesting resumption of token. [{0}]", context);

		int lifecycleState = context.getLifecycleState();
		if (lifecycleState != LifecycleState.SUSPENDED && lifecycleState != LifecycleState.IDLING)
		{
			String msg = LogUtil.error(getClass(), "Token has invalid lifecycle state for operation resumeToken. [{0}]", context);
			throw new OpenBPException("InvalidLifecycleState", msg);
		}

		context.setLifecycleRequest(LifecycleRequest.RESUME);

		if (lifecycleState != LifecycleState.IDLING)
		{
			tokenContextService.saveContext(context);
			flushTokenContextChanges();
		}
	}

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
	 */
	public void resumeWorkflow(final WorkflowTask workflowTask, final String socketName, final String currentUserId)
	{
		TokenContext context = workflowTask.getTokenContext();

		context = workflowTask.getTokenContext();

		NodeSocket workflowEntrySocket = context.getCurrentSocket();
		WorkflowNode workflowNode = (WorkflowNode) workflowEntrySocket.getNode();

		NodeSocket nextSocket = resolveSocketRef(socketName, workflowEntrySocket, context, true);
		context.setCurrentSocket(nextSocket);
		context.setLifecycleRequest(LifecycleRequest.RESUME);
		tokenContextService.saveContext(context);

		if (workflowNode != null)
		{
			if (workflowNode.isAssignToCurrentUser())
			{
				workflowTask.setRoleId(null);
				workflowTask.setUserId(currentUserId);
			}
		}

		// Update the workflow task status
		workflowTask.setStatus(WorkflowTask.STATUS_RESUMED);

		// Also save the id of the current user and the time
		workflowTask.setAcceptingUserId(currentUserId);
		workflowTask.setTimeAccepted(new Timestamp(System.currentTimeMillis()));

		// Update the workflow task in the persistent storage
		tokenContextService.saveWorkflowTask(workflowTask);
		flushTokenContextChanges();
	}

	/**
	 * Simply saves the given token as suspended token.
	 * Can be used to save token that shall be executed by the scheduler to the database without starting them.
	 * The method commits the changes to the database.
	 *
	 * @param context Token context
	 */
	public void prepareTokenForScheduler(final TokenContext context)
	{
		LogUtil.trace(getClass(), "Preparing scheduler. [{0}]", context);

		// TODO Fix 2 Check for the CREATED state after it has been introduced
		int lifecycleState = context.getLifecycleState();
		if (lifecycleState != LifecycleState.CREATED)
		{
			String msg = LogUtil.error(getClass(), "Token has invalid lifecycle state for operation prepareTokenForScheduler. [{0}]", context);
			throw new OpenBPException("InvalidLifecycleState", msg);
		}

		context.setLifecycleRequest(LifecycleRequest.NONE);
		tokenContextService.addContext(context);
		flushTokenContextChanges();
	}

	/**
	 * Flushes the changes to the token context store.
	 */
	public void flushTokenContextChanges()
	{
		tokenContextService.flush();
	}

	/**
	 * Commits the transaction on the token context store.
	 */
	public void commitTokenContextTransaction()
	{
		tokenContextService.commit();
	}

	/**
	 * Rolls back the transaction on the token context store.
	 */
	public void rollbackTokenContextTransaction()
	{
		tokenContextService.rollback();
	}

	//////////////////////////////////////////////////
	// @@ Internal: Token lifecycle
	//////////////////////////////////////////////////

	/**
	 * Begins the token (for internal use only).
	 * Raises the engine event {@link EngineEvent#BEGIN_TOKEN}.
	 *
	 * @param context Context that holds the token's state information
	 */
	public void beginToken(final TokenContext context)
	{
		// Create the process variables of the subprocess
		if (context.getCurrentSocket() != null)
		{
			EngineUtil.createProcessVariables(context.getCurrentSocket().getProcess(), context);
		}

		if (hasActiveObservers(EngineEvent.BEGIN_TOKEN, context))
		{
			fireEngineEvent(new EngineEvent(EngineEvent.BEGIN_TOKEN, context, this));
		}
	}

	/**
	 * Ends the token (for internal use only).
	 * In order to end a token, call the {@link TokenContext#setLifecycleRequest} method with the LifecycleRequest.STOP argument.
	 * Ends all it's child contexts and detaches it from it's parent context.
	 * Also sets the state of the workflow task that is associated with this context (if any) to {@link WorkflowTask#STATUS_COMPLETED}.
	 * Also raises the engine events {@link EngineEvent#BEFORE_END_TOKEN} and {@link EngineEvent#AFTER_END_TOKEN}.
	 *
	 * @param context Context that holds the token's state information
	 */
	public void endToken(final TokenContext context)
	{
		LogUtil.trace(getClass(), "Ending token. [{0}]", context);
		if (hasActiveObservers(EngineEvent.BEFORE_END_TOKEN, context))
		{
			fireEngineEvent(new EngineEvent(EngineEvent.BEFORE_END_TOKEN, context, this));
		}

		WorkflowTaskCriteria criteria = new WorkflowTaskCriteria();
		criteria.setTokenContext(context);
		criteria.setStatus(WorkflowTask.STATUS_RESUMED);

		// We may delete the context only if we may delete all WF tasks
		boolean deleteContext = true;
		for (Iterator itTask = tokenContextService.getworkflowTasks(criteria); itTask.hasNext();)
		{
			WorkflowTask workflowTask = (WorkflowTask) itTask.next();
			if (workflowTask.getId() != null)
			{
				if (! workflowTask.isDeleteAfterCompletion())
				{
					deleteContext = false;
				}
			}
		}

		// End all child contexts.
		// Copy the list in order to prevent a concurrent modification exception
		for (Iterator it = tokenContextService.getChildContexts(context); it.hasNext();)
		{
			TokenContext childContext = (TokenContext) it.next();
			endToken(childContext);
		}

		TokenContext parentContext = context.getParentContext();
		if (parentContext != null)
		{
			parentContext.removeChildContext(context);
		}
		context.setParentContext(null);

		for (Iterator itTask = tokenContextService.getworkflowTasks(criteria); itTask.hasNext();)
		{
			WorkflowTask workflowTask = (WorkflowTask) itTask.next();
			if (workflowTask.getId() != null)
			{
				if (workflowTask.isDeleteAfterCompletion())
				{
					tokenContextService.deleteWorkflowTask(workflowTask);
				}
				else
				{
					if (workflowTask != null)
					{
						workflowTask.setStatus(WorkflowTask.STATUS_COMPLETED);
						tokenContextService.saveWorkflowTask(workflowTask);
					}
				}
			}
		}

		if (deleteContext && context.getId() != null)
		{
			tokenContextService.deleteContext(context);

			// After deleting the context, nevertheless set the context state to 'complete' to prevent further processing.
			changeTokenState(context, LifecycleState.COMPLETED, LifecycleRequest.NONE);
		}
		else
		{
			changeTokenState(context, LifecycleState.COMPLETED, LifecycleRequest.NONE);
			tokenContextService.saveContext(context);
		}

		if (hasActiveObservers(EngineEvent.AFTER_END_TOKEN, context))
		{
			fireEngineEvent(new EngineEvent(EngineEvent.AFTER_END_TOKEN, context, this));
		}
	}

	/**
	 * Changes the lifecycle state and lifecycle request of the given token.
	 * Also raises the engine event {@link EngineEvent#TOKEN_STATE_CHANGE}.
	 *
	 * @param context Context that holds the token's state information
	 * @param lifecycleState Lifecycle state
	 * @param lifecycleRequest Lifecycle request
	 */
	public void changeTokenState(final TokenContext context, int lifecycleState, int lifecycleRequest)
	{
		int oldLifecycleState = context.getLifecycleState();
		int oldLifecycleRequest = context.getLifecycleRequest();
		if (oldLifecycleState != lifecycleState || oldLifecycleRequest != lifecycleRequest)
		{
			context.setLifecycleState(lifecycleState);
			context.setLifecycleRequest(lifecycleRequest);

			if (hasActiveObservers(EngineEvent.TOKEN_STATE_CHANGE, context))
			{
				fireEngineEvent(new EngineEvent(EngineEvent.TOKEN_STATE_CHANGE, context, this));
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Internal: Token execution
	//////////////////////////////////////////////////

	/**
	 * Executes the given context in this thread.
	 * The execution will end at the next transaction boundary 
	 *
	 * @param context Context
	 */
	public void executeContext(final TokenContext context)
	{
		LogUtil.trace(getClass(), "Executing token. [{0}]", context);
		EngineExecutor ee = new EngineExecutor(context, this);
		ee.executeTransaction();
	}

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
	public HandlerContext executeHandler(final HandlerDefinition handlerDef, final String eventType,
		final TokenContext context, NodeSocket currentSocket, NodeSocket nextSocket)
	{
		if (handlerDef != null && handlerDef.canHandle(eventType))
		{
			HandlerContext hc = new HandlerContext(this, eventType, context);
			if (currentSocket == null)
				currentSocket = context.getCurrentSocket();
			if (currentSocket == null)
			{
				String msg = LogUtil.error(getClass(), "Cannot call handler w/o a current socket. [{0}]", context);
				throw new OpenBPException("HandlerExecutionFailed", msg);
			}
			hc.setCurrentSocket(currentSocket);
			if (nextSocket == null && currentSocket != null)
				nextSocket = currentSocket.getNode().getDefaultExitSocket();
			hc.setNextSocket(nextSocket);

			// Execute the handler
			if (handlerDef.getHandlerClassName() != null)
			{
				Object handlerInstanceObject = handlerDef.obtainHandlerInstance();
				if (! (handlerInstanceObject instanceof Handler))
				{
					String msg = LogUtil.error(getClass(),
						"Handler object class $0 does not implement the handler interface $1. [{2}]", handlerInstanceObject
							.getClass().getName(), Handler.class.getName(), context);
					throw new OpenBPException("HandlerExecutionFailed", msg);
				}

				Handler handlerInstance = (Handler) handlerInstanceObject;
				LogUtil.trace(getClass(), "Executing handler on node $0 (class $1) as {2} handler. [{3}]",
					currentSocket.getNode().getQualifier().toString(), handlerInstance.getClass().getName(), eventType, context);

				// Execute the Java implementation of this handler
				try
				{
					handlerInstance.execute(hc);
				}
				catch (OpenBPException ee)
				{
					// Engine exceptions can be rethrown.
					throw ee;
				}
				catch (Throwable t)
				{
					// Catch any exceptions, so the server won't be confused by errors in handlers
					String msg = LogUtil.error(getClass(), "Error executing handler of $0. [{1}]", currentSocket.getNode()
						.getQualifier().toString(), t);
					throw new OpenBPException("HandlerExecutionFailed", msg, t);
				}
			}
			else if (handlerDef.getScript() != null)
			{
				LogUtil.trace(getClass(), "Executing script on node $0 as {1} handler. [{2}]",
					currentSocket.getNode().getQualifier().toString(), eventType, context);

				// Execute the script associated with this handler
				ScriptEngine scriptEngine = getScriptEngineFactory().obtainScriptEngine(hc.getTokenContext());
				try
				{
					scriptEngine.prepareHandlerExecution(hc);
					String script = handlerDef.getScript();
					scriptEngine.executeScript(script, "handler script", currentSocket.getNode().getQualifier().toString());
					scriptEngine.finishHandlerExecution(hc);
				}
				finally
				{
					getScriptEngineFactory().releaseScriptEngine(scriptEngine);
				}
			}

			return hc;
		}
		return null;
	}
	

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
	public NodeSocket resolveSocketRef(String ref, NodeSocket currentSocket, TokenContext context, boolean mustExist)
	{
		NodeSocket ret;
		if (ref != null && ref.startsWith(ModelQualifier.PATH_DELIMITER))
		{
			// Absolute reference
			ret = determineInitialPosition(context, ref, this);
		}
		else
		{
			ret = determineResumptionPosition(context, currentSocket, ref, this, mustExist);
		}
		return ret;
	}

	/**
	 * Determines the initial position of a process using the given start reference.
	 *
	 * @param context Token context
	 * @param ref Reference to the start node/socket ("/Process/Node" or ".Socket")
	 * @param engine Engine that shall be used to execute the context
	 * @return The socket
	 */
	public NodeSocket determineInitialPosition(TokenContext context, String ref, Engine engine)
	{
		ModelQualifier processQD = new ModelQualifier(ref);
		String processModelName = processQD.getModel();
		String processName = processQD.getItem();
		String entryName = processQD.getObjectPath();

		// Model that contains the process to execute
		Model processModel = null;
		if (processModelName != null)
		{
			if (processModelName.charAt(0) != ModelQualifier.PATH_DELIMITER_CHAR)
			{
				processModelName = ModelQualifier.PATH_DELIMITER_CHAR + processModelName;
			}

			processModel = engine.getModelMgr().getModelByQualifier(
				ModelQualifier.constructModelQualifier(processModelName));
		}
		if (processModel == null)
		{
			processModel = context.getExecutingModel();
		}
		if (processModel == null)
		{
			String msg = LogUtil.error(getClass(), "Position reference $0 requires a model specification or the executing model to be set. [{1}]", ref, context);
			throw new ModelException("NoDefaultProcess", msg);
		}

		if (processName == null)
		{
			String msg = LogUtil.error(getClass(), "Cannot determine process name from position reference $0. [{1}]", ref, context);
			throw new ModelException("NoDefaultProcess", msg);
		}

		// Absolute reference: The process specification was given in the start reference
		ProcessItem process;
		if (processName != null)
		{
			// Resolve the process reference
			process = (ProcessItem) processModel.resolveItemRef(processName, ItemTypes.PROCESS);
		}
		else
		{
			// Use the model's default process
			process = processModel.getDefaultProcess();

			if (process == null)
			{
				String msg = LogUtil.error(getClass(), "Model $0 does not have a default process (position reference: $1). [{2}]", processModel.getQualifier(), ref, context);
				throw new ModelException("NoDefaultProcess", msg);
			}
		}

		// Get the entry socket
		Node node;
		if (entryName != null)
		{
			node = process.getNodeByName(entryName);
			if (node == null)
			{
				String msg = LogUtil.error(getClass(), "Initial node $0 not found in process $1 (position reference: $2). [{3}]", entryName, process.getQualifier(), ref, context);
				throw new ModelException("NodeNotFound", msg);
			}
		}
		else
		{
			node = process.getDefaultInitialNode();
			if (node == null)
			{
				String msg = LogUtil.error(getClass(), "Process $0 does not have a default initial node (position reference: $1). [{2}]", process.getQualifier(), ref, context);
				throw new ModelException("NoDefaultNode", msg);
			}
		}

		if (! (node instanceof InitialNode))
		{
			String msg = LogUtil.error(getClass(), "Node $0 is not an initial node (position reference: $1). [{2}]", node.getQualifier(), ref, context);
			throw new ModelException("NoInitialNode", msg);
		}

		NodeSocket initialSocket = ((InitialNode) node).getSocket();

		if (! initialSocket.hasControlLinks())
		{
			String msg = LogUtil.error(getClass(), "There are no control links attached to socket $0. [{1}]", initialSocket.getQualifier().toUntypedString(), context);
			throw new EngineException("NoControlLink", msg);
		}

		return initialSocket;
	}

	/**
	 * Determines the resumption position of a suspended process using the given socket reference.
	 *
	 * @param context Token context
	 * @param ref Reference to the point of resumption or null for resumption at the current exit socket
	 * (usually the name of an exit socket of the current node, but may also specifiy an entry node name of the current process).
	 * @param engine Engine that shall be used to execute the context
	 * @param mustExist If true the method will throw an exception if the specified socket does not exist.
	 * @return The socket
	 */
	public NodeSocket determineResumptionPosition(TokenContext context, String ref, Engine engine, boolean mustExist)
	{
		return determineResumptionPosition(context, context.getCurrentSocket(), ref, engine, mustExist);
	}

	/**
	 * Determines the resumption position of a suspended process using the given socket reference.
	 *
	 * @param context Token context
	 * @param ref Reference to the point of resumption or null for resumption at the current exit socket
	 * (usually the name of an exit socket of the current node, but may also specifiy an entry node name of the current process).
	 * @param engine Engine that shall be used to execute the context
	 * @param mustExist If true the method will throw an exception if the specified socket does not exist.
	 * @return The socket
	 */
	private NodeSocket determineResumptionPosition(TokenContext context, NodeSocket currentSocket, String ref, Engine engine, boolean mustExist)
	{
		NodeSocket startSocket = null;

		// Relative execution request, the current process of the context should be continued with the specified entry name.
		if (currentSocket == null)
		{
			// No current position we can search the relative entry from.
			// The user either tries to access the application using a relative request
			// or the session context has timed out.

			// Log and create an exception
			String msg = LogUtil.error(getClass(), "Session does not have a current position or session has expired for socket reference $0. [{1}]", ref, context);
			throw new EngineException("NoCurrentPosition", msg);
		}

		// Try to determine the socket to start with using the usual socket search strategy
		if (ref != null)
		{
			Node currentNode = currentSocket.getNode();
			String socketName;

			int index = ref.indexOf(ModelQualifier.OBJECT_DELIMITER_CHAR);
			if (index >= 0)
			{
				// "Node.Socket"
				String nodeName = ref.substring (0, index);
				socketName = ref.substring(index + 1);

				currentNode = currentSocket.getProcess().getNodeByName(nodeName);
				if (currentNode == null)
				{
					String msg = LogUtil.error(getClass(), "Initial node $0 not found (position reference: $1). [{2}]", nodeName, ref, context);
					throw new ModelException("NodeNotFound", msg);
				}
				if (! (currentNode instanceof InitialNode))
				{
					String msg = LogUtil.error(getClass(), "Node $0 is not an initial node (position reference: $1). [{2}]", currentNode.getQualifier(), ref, context);
					throw new ModelException("NoInitialNode", msg);
				}
			}
			else if (index == 0)
			{
				// ".Socket"
				socketName = ref.substring(1);
			}
			else
			{
				// "Socket"
				socketName = ref;
			}

			startSocket = currentNode.getSocketByName(socketName);
			if (startSocket == null)
			{
				if (mustExist)
				{
					String msg = LogUtil.error(getClass(), "Exit socket $0 not found. [{1}]", socketName, context);
					throw new EngineException("NoExitSocket", msg);
				}
			}
		}
		else
		{
			// Resuming at an entry socket of a wait state node would lead to an endless loop.
			if (currentSocket.isExitSocket())
			{
				startSocket = currentSocket;
			}
			else
			{
				startSocket = currentSocket.getNode().getDefaultExitSocket();
			}
		}

		if (startSocket != null && ! mustExist)
		{
			if (! startSocket.hasControlLinks())
			{
				// Accept connected sockets only when looking for an optional socket
				startSocket = null;
			}
		}

		return startSocket;
	}

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
	public void registerObserver(final EventObserver observer, final String[] eventTypes)
	{
		getObserverMgr().registerObserver(observer, eventTypes);
	}

	/**
	 * Unregisters an observer.
	 *
	 * @param observer The observer
	 */
	public void unregisterObserver(final EventObserver observer)
	{
		getObserverMgr().unregisterObserver(observer);
	}

	/**
	 * Suspends broadcasting of engine events.
	 *
	 * @return The previous suspend status
	 */
	public boolean suspendEngineEvents()
	{
		return getObserverMgr().suspendObserverEvents();
	}

	/**
	 * Resumes broadcasting of engine events.
	 */
	public void resumeEngineEvents()
	{
		getObserverMgr().resumeObserverEvents();
	}

	/**
	 * Checks if there are active engine event observers registered (for internal use only).
	 *
	 * @param eventType Type of event in question
	 * @param context Token context of current operation; may be configured to supply a token-local observer
	 * @return true if there is at least one observer
	 */
	public boolean hasActiveObservers(final String eventType, TokenContext context)
	{
		return getObserverMgr().hasActiveObservers(eventType);
	}

	/**
	 * Notifies all registered observers about a engine event (for internal use only).
	 *
	 * @param event Engine event to dispatch
	 */
	public void fireEngineEvent(final EngineEvent event)
	{
		TokenContext context = event.getContext();
		context.fireEngineEvent(event);
		if (! event.shallSkipSubsequentObservers())
		{
			getObserverMgr().fireEvent(event);
		}
	}

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
	public boolean isFatalException(Throwable t)
	{
		return false;
	}

	//////////////////////////////////////////////////
	// @@ Component setters/getters
	//////////////////////////////////////////////////

	/**
	 * Gets the session mode.
	 * @return {@link SessionMode#MANUAL}/{@link SessionMode#AUTO}
	 */
	public int getSessionMode()
	{
		return sessionMode;
	}

	/**
	 * Sets the session mode.
	 * @param sessionMode {@link SessionMode#MANUAL}/{@link SessionMode#AUTO}
	 */
	public void setSessionMode(final int sessionMode)
	{
		this.sessionMode = sessionMode;
	}

	/**
	 * Gets the flag that determines if an automatic rollback should be performed on unhandled errors.
	 * @nowarn
	 */
	public boolean isRollbackOnError()
	{
		return rollbackOnError;
	}

	/**
	 * Sets the flag that determines if an automatic rollback should be performed on unhandled errors.
	 * @nowarn
	 */
	public void setRollbackOnError(boolean rollbackOnError)
	{
		this.rollbackOnError = rollbackOnError;
	}

	/**
	 * Gets the model manager.
	 * @nowarn
	 */
	public ModelMgr getModelMgr()
	{
		return modelMgr;
	}

	/**
	 * Sets the model manager.
	 * @nowarn
	 */
	public void setModelMgr(ModelMgr modelMgr)
	{
		this.modelMgr = modelMgr;
	}

	/**
	 * Gets the token context management service.
	 * @nowarn
	 */
	public TokenContextService getTokenContextService()
	{
		return tokenContextService;
	}

	/**
	 * Sets the token context management service.
	 * @nowarn
	 */
	public void setTokenContextService(final TokenContextService tokenContextService)
	{
		this.tokenContextService = tokenContextService;
	}

	/**
	 * Gets the persistence context provider.
	 * @nowarn
	 */
	public PersistenceContextProvider getPersistenceContextProvider()
	{
		return persistenceContextProvider;
	}

	/**
	 * Sets the persistence context provider.
	 * @nowarn
	 */
	public void setPersistenceContextProvider(final PersistenceContextProvider persistenceContextProvider)
	{
		this.persistenceContextProvider = persistenceContextProvider;
	}

	/**
	 * Gets the session registry.
	 * @nowarn
	 */
	public SessionRegistry getSessionRegistry()
	{
		return sessionRegistry;
	}

	/**
	 * Sets the session registry.
	 * @nowarn
	 */
	public void setSessionRegistry(final SessionRegistry sessionRegistry)
	{
		this.sessionRegistry = sessionRegistry;
	}

	/**
	 * Gets the script engine factory.
	 * @nowarn
	 */
	public ScriptEngineFactory getScriptEngineFactory()
	{
		return scriptEngineFactory;
	}

	/**
	 * Sets the script engine factory.
	 * @nowarn
	 */
	public void setScriptEngineFactory(final ScriptEngineFactory scriptEngineFactory)
	{
		this.scriptEngineFactory = scriptEngineFactory;
	}

	private EventObserverMgr getObserverMgr()
	{
		if (observerMgr == null)
		{
			setObserverMgr(new EngineEventObserverMgr());
		}
		return observerMgr;
	}

	// For Spring framework support
	public void setObserverMgr(final EventObserverMgr observerMgr)
	{
		this.observerMgr = observerMgr;

		if (observerMgr != null)
		{
			EngineUtil.prepareEngineObserverMgr(observerMgr);
		}
	}

	/**
	 * Gets the model object executor manager.
	 * @nowarn
	 */
	public ModelObjectExecutorMgr getModelObjectExecutorMgr()
	{
		return modelObjectExecutorMgr;
	}

	/**
	 * Sets the model object executor manager.
	 * @nowarn
	 */
	public void setModelObjectExecutorMgr(final ModelObjectExecutorMgr modelObjectExecutorMgr)
	{
		this.modelObjectExecutorMgr = modelObjectExecutorMgr;
	}
}
