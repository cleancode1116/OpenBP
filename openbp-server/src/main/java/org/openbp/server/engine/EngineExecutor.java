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

import java.util.Iterator;
import java.util.List;

import org.openbp.common.generic.Copyable;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.setting.SettingUtil;
import org.openbp.common.util.CopyUtil;
import org.openbp.core.CoreConstants;
import org.openbp.core.OpenBPException;
import org.openbp.core.engine.EngineException;
import org.openbp.core.engine.ExpressionConstants;
import org.openbp.core.handler.HandlerTypes;
import org.openbp.core.model.item.process.ControlLink;
import org.openbp.core.model.item.process.DataLink;
import org.openbp.core.model.item.process.InitialNode;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.Param;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessVariable;
import org.openbp.server.ServerConstants;
import org.openbp.server.context.LifecycleRequest;
import org.openbp.server.context.LifecycleState;
import org.openbp.server.context.SessionRegistry;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextService;
import org.openbp.server.context.TokenContextUtil;
import org.openbp.server.engine.script.ExpressionParser;
import org.openbp.server.engine.script.ScriptEngine;
import org.openbp.server.engine.script.ScriptUtil;
import org.openbp.server.handler.HandlerContext;

/**
 * Helper class that executes a portion of work.
 * This portion is defined by the transaction boundaries of the process.
 *
 * @author Heiko Erhardt
 */
public class EngineExecutor
	implements EngineContext
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Token context */
	private TokenContext context;

	/** Engine */
	private final EngineImpl engine;

	/** Session timeout */
	private long sessionTimeout = -1L;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param context Context
	 * @param engine Engine
	 */
	public EngineExecutor(TokenContext context, EngineImpl engine)
	{
		this.context = context;
		this.engine = engine;
	}

	/**
	 * Gets the token context.
	 * @nowarn
	 */
	public TokenContext getTokenContext()
	{
		return context;
	}

	/**
	 * Sets the token context.
	 * @nowarn
	 */
	public void setTokenContext(TokenContext context)
	{
		this.context = context;
	}

	/**
	 * Gets the engine.
	 * @nowarn
	 */
	public EngineImpl getEngine()
	{
		return engine;
	}

	//////////////////////////////////////////////////
	// @@ Execution
	//////////////////////////////////////////////////

	public void executeTransaction()
	{
		try
		{
			LogUtil.debug(getClass(), "Starting execution of context $0.", context);

			engine.changeTokenState(context, LifecycleState.RUNNING, LifecycleRequest.NONE);

			Thread thisThread = Thread.currentThread();
			context.setRuntimeAttribute(TokenContext.RUNTIME_ATTRIBUTE_THREAD, thisThread);

			if (engine.hasActiveObservers(EngineTraceEvent.BEGIN_EXECUTION, context))
			{
				engine.fireEngineEvent(new EngineEvent(EngineEvent.BEGIN_EXECUTION, context, engine));
			}

			// Register the context's session
			if (engine.getSessionMode() == SessionMode.AUTO)
			{
				SessionRegistry sessionRegistry = engine.getSessionRegistry();
				if (sessionRegistry != null)
				{
					if (sessionTimeout == -1L)
					{
						sessionTimeout = SettingUtil.getIntSetting(ServerConstants.SYSPROP_SERVERSESSION_TIMEOUT, 0);
					}
					sessionRegistry.registerSession(context.getId(), context, sessionTimeout);
				}
			}

			TokenContextService contextService = engine.getTokenContextService();
			contextService.saveContext(context);
			contextService.commit();

			do
			{
				executeNextStep();
			}
			while (context.getLifecycleState() == LifecycleState.RUNNING);

			// The application should not set the lifecycle state by itself, however in order to prevent
			// leaving this loop w/o committing the transaction, we do a commit here
			// (the commit will check if we have a transaction running at all).
			contextService.commit();

			LogUtil.debug(getClass(), "Finished execution of context $0.", context);
		}
		catch (Throwable t)
		{
			LogUtil.error(getClass(), "Error occured executing a process. [{0}]", context, t);

			if (engine.isRollbackOnError())
			{
				// We shall try to perform a rollback...
				if (! engine.isFatalException(t))
				{
					// ...and the exception is not fatal.
					rollbackAndExit(LifecycleState.ERROR);
				}
			}
			throw OpenBPException.wrapUnrecoverable(t);
		}
		finally
		{
			// Unregister the context's session
			if (engine.getSessionMode() == SessionMode.AUTO)
			{
				unregisterSession();
			}

			if (engine.hasActiveObservers(EngineTraceEvent.END_EXECUTION, context))
			{
				engine.fireEngineEvent(new EngineEvent(EngineEvent.END_EXECUTION, context, engine));
			}
			context.removeRuntimeAttribute(TokenContext.RUNTIME_ATTRIBUTE_THREAD);
		}
	}

	//////////////////////////////////////////////////
	// @@ Process execution
	//////////////////////////////////////////////////

	/**
	 * Executes the next process step of the given context.
	 * At least one node will be executed.
	 * The method will continue to execute proces nodes using this context until it is safe to interrupt the process execution,
	 * i. e. until there is no transaction running any more.
	 *
	 * Internal method, do not call from the application program.
	 *
	 * @throws OpenBPException On errors that are not handled by process error mechanisms
	 */
	protected void executeNextStep()
	{
		// Save the current thread class loader
		ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();

		// Use the model's class loader for access to any classes we require (e. g. mapped beans)
		if (context.getExecutingModel().getClassLoader() != null)
		{
			Thread.currentThread().setContextClassLoader(context.getExecutingModel().getClassLoader());
		}

		try
		{
			// Initialize the next executable socket to the initial socket.
			NodeSocket currentSocket = context.getCurrentSocket();

			if (currentSocket == null)
				// There is no executable socket; the process has stopped
				return;

			Node currentNode = currentSocket.getNode();
			NodeSocket entrySocket = currentSocket;
			boolean isEntrySocket = currentSocket.isEntrySocket();

			if (isEntrySocket)
			{
				// We have an entry socket; Regular node execution

				// Evaluate any input parameter expressions or scripts and
				// ensure that any required parameters are present.
				prepareSocket(entrySocket);

				// By default, we copy the parameters of the entry socket to parameters
				// of the same name at the exit sockets
				EngineUtil.copySocketParameters(entrySocket, null, context);

				if (engine.hasActiveObservers(EngineTraceEvent.NODE_ENTRY, context))
				{
					engine.fireEngineEvent(new EngineTraceEvent(EngineTraceEvent.NODE_ENTRY, context, entrySocket, engine));
				}
				// Reassign the entry socket if the process has been updated while waiting for a debugger command ("hot code replace")
				entrySocket = context.getCurrentSocket();
				currentNode = entrySocket.getNode();

				/////////////////////////////////////////////////
				// Execute the node connected to the socket
				/////////////////////////////////////////////////

				if (LogUtil.isTraceEnabled(getClass()))
				{
					String nodeType = currentNode.getModelObjectSymbolName();
					LogUtil.trace(getClass(), "Executing {0}Node $1. [{2}]", nodeType, currentNode.getQualifier(), context);
				}

				// Execute the NodeEntry handler, if any
				HandlerContext hc = engine.executeHandler(currentNode.getEventHandlerDefinition(), HandlerTypes.NODE_ENTRY, context, entrySocket,
					currentNode.getDefaultExitSocket());
				if (hc != null && hc.hasNextSocketChanged())
				{
					// Handler changed the flow of control...
					if (hc.getNextSocket() == null)
						throw new EngineException("MissingNextSocket", "Handler of node  '" + currentNode.getQualifier()
							+ "' has set a null next socket.\nThe process cannot be continued.");
					context.setCurrentSocket(hc.getNextSocket());
					return;
				}

				// *** Execute the node ***
				ModelObjectExecutor executor = engine.getModelObjectExecutorMgr().getExecutor(currentNode);
				executor.executeModelObject(currentNode, this);
			}
			else
			{
				// We have an exit socket; Node has been executed already, process is being continued now.
			}

			// *** Handle any lifecycle state requests and perform the transaction handling ***
			handleLifecycleRequest();

			if (context.getLifecycleState() == LifecycleState.RUNNING)
			{
				NodeSocket nextSocket = context.getCurrentSocket();
				if (nextSocket != null)
				{
					// Execute the node exit handler, if any
					HandlerContext hc2 = engine.executeHandler(currentNode.getEventHandlerDefinition(), HandlerTypes.NODE_EXIT, context, entrySocket, nextSocket);
					if (hc2 != null && hc2.hasNextSocketChanged())
					{
						// Handler changed the flow of control...
						nextSocket = hc2.getNextSocket();
						context.setCurrentSocket(nextSocket);
					}

					if (nextSocket != null)
					{
						// Evaluate any output parameter expressions or scripts
						postProcessSocket(nextSocket);
					}

					if (isEntrySocket)
					{
						// Clear the entry socket data from the context
						EngineUtil.removeSocketData(entrySocket, context);
					}
				}

				if (nextSocket != null)
				{
					if (engine.hasActiveObservers(EngineTraceEvent.NODE_EXIT, context))
					{
						engine.fireEngineEvent(new EngineTraceEvent(EngineTraceEvent.NODE_EXIT, context, nextSocket, engine));
					}
					// Reassign the entry socket if the process has been updated while waiting for a debugger command ("hot code replace")
					nextSocket = context.getCurrentSocket();

					// If we ended up with an entry socket for the next socket, the flow of control was diverted by the node handler directly.
					// Otherwise, we need to perform the regular control link processing.
					if (nextSocket.isExitSocket())
					{
						// *** Follow the control links to determine the new node. ***
						// Note that the control link may perform transaction handling here.
						advanceToNextSocket(nextSocket);
						nextSocket = context.getCurrentSocket();
					}
				}

				if (nextSocket == null || nextSocket.getNode() != currentNode)
				{
					// Remove local node data if we changed nodes
					EngineUtil.removeNodeData(currentNode, context);
				}
			}
		}
		catch (EngineTraceException e)
		{
			// The engine trace generated an exception (this usually means to abort the process)
			// Pass this exception as it is

			unregisterSession();

			// Rethrow the exception.
			throw e;
		}
		catch (Throwable t)
		{
			// Might handle or rethrow the exception
			handleException(t);

			// Continue
		}
		finally
		{
			// Restore the thread class loader
			Thread.currentThread().setContextClassLoader(oldClassLoader);
		}
	}

	/**
	 * Handles any lifecycle state requests and perform the transaction handling.
	 */
	private void handleLifecycleRequest()
	{
		TokenContextService contextService = engine.getTokenContextService();

		int lifecycleRequest = context.getLifecycleRequest();

		if (lifecycleRequest == LifecycleRequest.SUSPEND_MEMORY)
		{
			LogUtil.trace(getClass(), "Suspending token (memory suspend). [{0}]", context);

			// This is a mere memory-suspend, so don't commit this change.
			engine.changeTokenState(context, LifecycleState.IDLING, LifecycleRequest.NONE);

			// Wait for some notification, using the context as synchronisation object
			synchronized (context)
			{
				try
				{
					context.wait();
				}
				catch (InterruptedException e)
				{
				}
			}

			// Resume again
			LogUtil.trace(getClass(), "Resuming token (memory suspend). [{0}]", context);
			engine.changeTokenState(context, LifecycleState.RUNNING, LifecycleRequest.NONE);
		}

		if (lifecycleRequest == LifecycleRequest.SUSPEND_IMMEDIATE)
		{
			// This node type requires persistence of the process state
			LogUtil.trace(getClass(), "Suspending token (immediate suspend). [{0}]", context);

			engine.changeTokenState(context, LifecycleState.SUSPENDED, LifecycleRequest.NONE);

			contextService.saveContext(context);
			contextService.commit();
		}
		else if (lifecycleRequest == LifecycleRequest.STOP)
		{
			// Normally terminate processing
			LogUtil.trace(getClass(), "Completing token. [{0}]", context);
			engine.endToken(context);
			contextService.commit();
		}
		else if (lifecycleRequest == LifecycleRequest.ABORT)
		{
			// Abort processing
			LogUtil.trace(getClass(), "Aborting token. [{0}]", context);

			rollbackAndExit(LifecycleState.ABORTED);
		}
	}

	protected void advanceToNextSocket(NodeSocket exitSocket)
	{
		// Iterate all exit parameters and forward them to other nodes
		// they are connected with by data links.
		transferExitSocketData(exitSocket);

		// Now forward control to the sockets connected to this exit socket, if any
		NodeSocket oldCurrentSocket = context.getCurrentSocket();
		for (Iterator it = exitSocket.getControlLinks(); it.hasNext();)
		{
			ControlLink link = (ControlLink) it.next();

			if (engine.hasActiveObservers(EngineTraceEvent.CONTROL_FLOW, context))
			{
				engine.fireEngineEvent(new EngineTraceEvent(EngineTraceEvent.CONTROL_FLOW, context, link, engine));
			}
			executeControlLink(link);
		}

		if (context.getCurrentSocket() == oldCurrentSocket)
		{
			// No control link present, try to continue at an initial node
			// with the same name as the exit socket
			String nextName = exitSocket.getName();

			NodeSocket nextSocket = null;
			Node nextNode = context.getCurrentSocket().getProcess().getNodeByName(nextName);
			if (nextNode != null && nextNode instanceof InitialNode)
			{
				nextSocket = nextNode.getDefaultExitSocket();
			}

			if (nextSocket == null)
			{
				ProcessItem process = exitSocket.getNode().getProcess();
				String msg = LogUtil.error(getClass(),
					"Unconnected socket encountered and no $0 initial node present in process $1 (current position: $2). [{3}]", new Object[]
					{
						nextName, process.getQualifier(), exitSocket.getQualifier(), context
					});
				throw new EngineException("UnconnectedSocket", msg);
			}

			// Copy the data of the current exit socket to the newly determined exit socket
			EngineUtil.copySocketData(exitSocket, context, nextSocket, context);
			context.setCurrentSocket(nextSocket);
		}
	}

	/**
	 * Executes a process element.
	 *
	 * @param link Contro link
	 * @throws OpenBPException On error
	 */
	public void executeControlLink(ControlLink link)
	{
		NodeSocket oldCurrentSocket = context.getCurrentSocket();

		int transactionControl = link.getTransactionControl();
		if (transactionControl != ControlLink.TA_NONE)
		{
			TokenContextService contextService = engine.getTokenContextService();

			boolean begin = false;
			switch (transactionControl)
			{
			  case ControlLink.TA_BEGIN:
				begin = true;
				break;

			  case ControlLink.TA_COMMIT_BEGIN:
				begin = true;
				// Fallthrough

			  case ControlLink.TA_COMMIT:
				contextService.saveContext(context);
				contextService.commit();
				break;

			  case ControlLink.TA_ROLLBACK_BEGIN:
				begin = true;
				// Fallthrough

			  case ControlLink.TA_ROLLBACK:
				TokenContext newContext = EngineUtil.rollbackAndContinue(context, link.getRollbackDataBehavior(), link.getRollbackPositionBehavior(), engine);
				if (newContext != null)
					context = newContext;
				break;
			}

			if (begin)
			{
				contextService.begin();
			}

			if (context.getLifecycleRequest() == LifecycleRequest.SUSPEND_TRANSACTION)
			{
				// There is a suspension request for the next transaction control, serve it.
				LogUtil.trace(getClass(), "Suspending token (transaction suspend). [{0}]", context);

				engine.changeTokenState(context, LifecycleState.SUSPENDED, LifecycleRequest.NONE);
				contextService.saveContext(context);
				contextService.commit();
			}
		}

		if (context.getCurrentSocket() == oldCurrentSocket)
		{
			context.setCurrentSocket(link.getTargetSocket());
		}
	}

	private void rollbackAndExit(int lifecycleState)
	{
		TokenContextService contextService = engine.getTokenContextService();

		Object contextId = context.getId();
		contextService.rollback();

		context = contextService.getContextById(contextId);
		engine.changeTokenState(context, lifecycleState, LifecycleRequest.NONE);
		contextService.saveContext(context);
		contextService.commit();

		// TODO Fix 3 What to do with child/root context? Maybe call endToken with a parameter?
	}

	/**
	 * This method handles an {@link OpenBPException} that occured while executing a process.
	 *
	 * If an error handler has been defined (using the {@link Engine#registerObserver} method with the 
	 * {@link EngineExceptionHandlerEvent#HANDLE_EXCEPTION} event type), the handler may determine the way
	 * that the exception is being handled. For details, see the description of the {@link EngineExceptionHandlerEvent} class.
	 *
	 * If there is no handler (or the handler opts for this choice) and the exception is not an unrecoverable
	 * OpenBPException (i. e. the {@link OpenBPException#setUnrecoverable(boolean)} attribute is set),
	 * the method will try to find an error socket of the current node.
	 * In this case, the given exception will be bound to the 'Exception' parameter
	 * of the socket.<br>
	 * If no error socket can be found or if the handler opted to propagate the exception,
	 * the method will throw an {@link OpenBPException}
	 *
	 * @param t The exception that should be handled
	 */
	protected void handleException(Throwable t)
	{
		int handlingOption = EngineExceptionHandlerEvent.HANDLING_OPTION_ERROR_SOCKET;

		// Check if we have an exception handler
		if (engine.hasActiveObservers(EngineExceptionHandlerEvent.HANDLE_EXCEPTION, context))
		{
			EngineExceptionHandlerEvent event = new EngineExceptionHandlerEvent(EngineExceptionHandlerEvent.HANDLE_EXCEPTION, context, t, engine); 
			event.setEngine(engine);
			engine.fireEngineEvent(event);

			// Save any changes the event handler did (either by performing a rollback or by changing the handling option)
			context = event.getContext();
			handlingOption = event.getHandlingOption();
		}

		if (handlingOption == EngineExceptionHandlerEvent.HANDLING_OPTION_CONTINUE)
		{
			// Continue execution
			LogUtil.trace(getClass(), "Continuing execution after handling of exception. [{0}]", context, t);
			return;
		}

		// If this is an unrecoverable exception, pass it through
		if (t instanceof OpenBPException)
		{
			OpenBPException oe = (OpenBPException) t;
			if (oe.isUnrecoverable())
			{
				handlingOption = EngineExceptionHandlerEvent.HANDLING_OPTION_RETHROW;
			}
		}

		if (handlingOption == EngineExceptionHandlerEvent.HANDLING_OPTION_ERROR_SOCKET)
		{
			// Let's see if there is an error socket to continue with.
			NodeSocket socket = determineErrorSocket(context);
			if (socket != null)
			{
				LogUtil.trace(getClass(), "Continuing execution at error socket $0 after handling of exception. [{0}]", socket.getQualifier(), context, t);

				context.setCurrentSocket(socket);

				// Try to find an exception parameter at the socket we're going to start with.
				NodeParam exceptionParam = socket.getParamByName(CoreConstants.EXCEPTION_PARAM_NAME);
				if (exceptionParam != null)
				{
					// ...and set the exception to be handled.
					TokenContextUtil.setParamValue(context, exceptionParam, t);
				}
				return;
			}
		}

		// Report exception to the engine trace
		if (engine.hasActiveObservers(EngineTraceEvent.PROCESS_EXCEPTION, context))
		{
			engine.fireEngineEvent(new EngineTraceEvent(EngineTraceEvent.PROCESS_EXCEPTION, context, t, engine));
		}

		// Pass through our exception
		LogUtil.trace(getClass(), "Rethrowing exception. [{0}]", context, t);
		throw OpenBPException.wrapUnrecoverable(t);
	}

	/**
	 * Implements the search strategy for an error socket.
	 * This implementation searches for a socket named 'Error' of the current node first,
	 * then tries to find an initial node named 'Error'.
	 *
	 * @param context Context
	 * @return The error socket or null if no such socket could be found.
	 */
	protected NodeSocket determineErrorSocket(TokenContext context)
	{
		NodeSocket socket = null;
		try
		{
			socket = engine.resolveSocketRef(CoreConstants.ERROR_SOCKET_NAME, context.getCurrentSocket(), context, false);
		}
		catch (OpenBPException e2)
		{
			// Likely not to happen here, but...
			LogUtil.error(getClass(), "The following error occured while trying to handle an exception. [{0}]", context, e2);
		}
		if (socket == null)
		{
			Node node = context.getCurrentSocket().getProcess().getNodeByName(CoreConstants.ERROR_SOCKET_NAME);
			if (node != null)
			{
				if (! (node instanceof InitialNode))
				{
					String msg = LogUtil.error(getClass(), "Error node $0 must be an initial node. [{0}]", node.getQualifier(), context);
					throw new EngineException("InvalidErrorNode", msg);
				}
				socket = ((InitialNode) node).getDefaultExitSocket();
			}
		}
		return socket;
	}

	//////////////////////////////////////////////////
	// @@ Socket execution
	//////////////////////////////////////////////////

	/**
	 * Prepares the socket for execution.
	 * The method will evaluate any input parameter expressions or scripts and will ensure
	 * that any required parameters are present.
	 *
	 * @param socket Socket to execute
	 * @throws OpenBPException If required parameters are missing or there was an error
	 * in the expression/script evaluation.
	 */
	private void prepareSocket(NodeSocket socket)
	{
		List paramList = socket.getParamList();
		if (paramList != null)
		{
			int nParams = paramList.size();

			// First, provide any data from process variables the parameters might be connected to
			// We do this before evaluating any parameter expressions because they might depend on the global values.
			for (int i = 0; i < nParams; ++i)
			{
				NodeParam param = (NodeParam) paramList.get(i);

				checkGlobalLinks(param);
			}

			// Now evaluate parameter expressions and check for required parameters
			for (int i = 0; i < nParams; ++i)
			{
				NodeParam param = (NodeParam) paramList.get(i);

				Object value = null;
				boolean required = ! param.isOptional();

				String expression = param.getExpression();
				if (expression != null)
				{
					// Evaluate the expression before checking the parameters
					if (ScriptUtil.isConstantExpression(expression))
					{
						// Evaluate constant expressions only if we do not yet have a parameter value
						value = TokenContextUtil.getParamValue(context, param);

						if (value == null)
						{
							value = ScriptUtil.getConstantExpressionValue(expression);

							if (value != null)
							{
								// Finally, we have a value. Assign it to to the parameter
								TokenContextUtil.setParamValue(context, param, value);
							}
						}
					}
					else
					{
						// Evaluate a script expression
						ScriptEngine scriptEngine = engine.getScriptEngineFactory().obtainScriptEngine(context);
						try
						{
							// Evaluate the expression
							scriptEngine.prepareNodeParamExecution(param);
							value = scriptEngine.executeScript(expression, "entry parameter script", param.getQualifier().toString());
							scriptEngine.finishNodeParamExecution(param);

							// Assign the result to the parameter
							TokenContextUtil.setParamValue(context, param, value);
						}
						finally
						{
							engine.getScriptEngineFactory().releaseScriptEngine(scriptEngine);
						}
					}
				}
				else
				{
					if (required)
					{
						value = TokenContextUtil.getParamValue(context, param);
					}
				}

				if (required && value == null)
					// This is an error in this case
					throw new EngineException("RequiredParameterMissing", "Required parameter '" + param.getQualifier() + "' not present");
			}
		}
	}

	/**
	 * Post-processes the socket after the node execution.
	 * The method will evaluate any output parameter expressions or scripts.
	 *
	 * @param socket Socket to post-process
	 * @throws OpenBPException If there was an error in the expression/script evaluation.
	 */
	private void postProcessSocket(NodeSocket socket)
	{
		List paramList = socket.getParamList();
		if (paramList != null)
		{
			int nParams = paramList.size();
			for (int i = 0; i < nParams; ++i)
			{
				NodeParam param = (NodeParam) paramList.get(i);

				String expression = param.getExpression();
				if (expression != null)
				{
					// Evaluate the expression before checking the parameters
					if (ScriptUtil.isConstantExpression(expression))
					{
						// Evaluate constant expressions only if we do not yet have a parameter value
						Object value = TokenContextUtil.getParamValue(context, param);

						if (value == null)
						{
							value = ScriptUtil.getConstantExpressionValue(expression);

							if (value != null)
							{
								// Finally, we have a value. Assign it to to the parameter
								TokenContextUtil.setParamValue(context, param, value);
							}
						}
					}
					else
					{
						// Evaluate a script expression
						ScriptEngine scriptEngine = engine.getScriptEngineFactory().obtainScriptEngine(context);
						try
						{
							// Evaluate the expression
							scriptEngine.prepareNodeParamExecution(param);
							Object value = scriptEngine.executeScript(expression, "exit parameter script", param.getQualifier().toString());
							scriptEngine.finishNodeParamExecution(param);

							// Assign the result to the parameter
							TokenContextUtil.setParamValue(context, param, value);
						}
						finally
						{
							engine.getScriptEngineFactory().releaseScriptEngine(scriptEngine);
						}
					}
				}
			}
		}
	}

	/**
	 * Provide any data from process variables the parameter might be connected to.
	 *
	 * @param param Node parameter
	 * @throws OpenBPException On error, e. g. if the evaluation of a destination parameter expression fails
	 */
	private void checkGlobalLinks(NodeParam param)
	{
		boolean foundLink = false;

		ProcessItem process = param.getProcess();

		for (Iterator itVar = process.getProcessVariables(); itVar.hasNext();)
		{
			ProcessVariable global = (ProcessVariable) itVar.next();

			for (Iterator itLink = global.getDataLinks(); itLink.hasNext();)
			{
				DataLink link = (DataLink) itLink.next();

				Param targetParam = link.getTargetParam();
				if (targetParam == param)
				{
					// We found a global that is connected to this parameter, execute the link
					executeDataLink(link);
					foundLink = true;
				}
			}
		}

		if (! foundLink)
		{
			// Try process variable auto-assignment
			ProcessVariable var = process.getProcessVariableByName(param.getName());
			if (var != null)
			{
				if (var.isAutoAssign())
				{
					Object value = context.getProcessVariableValue(param.getName());
					TokenContextUtil.setParamValue(context, param, value);
				}
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Data link execution
	//////////////////////////////////////////////////

	/**
	 * Iterate all parameters of an exit socket and forward the parameter data to input parameters
	 * the output parameter is connected to by data links.
	 *
	 * @param exitSocket Exit socket to operate on
	 * @throws OpenBPException On error
	 */
	private void transferExitSocketData(NodeSocket exitSocket)
	{
		// Iterate all parameters of the exit socket
		for (Iterator it = exitSocket.getParams(); it.hasNext();)
		{
			Param sourceParam = (Param) it.next();

			// Transfer the parameter value
			transferParamData(sourceParam);

			// After we have distributed the exit parameter, we can remove it from the context
			TokenContextUtil.removeParamValue(context, sourceParam);
		}
	}

	/**
	 * Iterates all data links of an output parameter and forwards the parameter data to the input parameters
	 * the output parameter is connect to by data links.
	 *
	 * @param sourceParam Source parameter
	 * @throws OpenBPException On error, e. g. if the evaluation of a destination parameter expression fails
	 */
	private void transferParamData(Param sourceParam)
	{
		// Iterate all data links of the exit parameter
		boolean foundDataLink = false;
		for (Iterator it = sourceParam.getDataLinks(); it.hasNext();)
		{
			DataLink link = (DataLink) it.next();

			executeDataLink(link);
			foundDataLink = true;
		}

		if (! foundDataLink)
		{
			// Try process variable auto-assignment
			ProcessVariable var = sourceParam.getProcess().getProcessVariableByName(sourceParam.getName());
			if (var != null)
			{
				if (var.isAutoAssign())
				{
					Object value = TokenContextUtil.getParamValue(context, sourceParam);
					context.setProcessVariableValue(sourceParam.getName(), value);
				}
			}
		}
	}

	/**
	 * Executes a data link by transferring the data from the source parameter to the target parameter.
	 *
	 * @param link Data link
	 * @throws OpenBPException On error, e. g. if the evaluation of a destination parameter expression fails
	 */
	private void executeDataLink(DataLink link)
	{
		Param sourceParam = link.getSourceParam();
		String sourceMember = link.getSourceMemberPath();
		Param targetParam = link.getTargetParam();
		String targetMember = link.getTargetMemberPath();

		//
		// Retrieve the source value
		//
		Object value;
		if (sourceMember != null)
		{
			// We have a member path specification for the source.
			// We need an expression parser to evaluate it.
			ExpressionParser parser = EngineUtil.createExpressionParser(context, engine);

			String contextName = sourceParam.getContextName();
			String paramName = sourceParam.getName();
			int pos = contextName.lastIndexOf(paramName);
			String contextPrefix = contextName.substring(0, pos);

			parser.setContextPrefix(contextPrefix);

			// Any members that are missing in the path will cause a null value to be returned
			String expr;
			if (sourceMember.startsWith(ExpressionConstants.MEMBER_OPERATOR))
				expr = paramName + sourceMember;
			else if (sourceMember.startsWith(ExpressionConstants.REFERENCE_KEY_OPERATOR))
				expr = paramName + sourceMember;
			else
				expr = paramName + ExpressionConstants.MEMBER_OPERATOR + sourceMember;
			value = parser.getContextPathValue(expr, null, 0);
		}
		else
		{
			// Direct parameter value access, cache the parameter value for remaining data links
			value = TokenContextUtil.getParamValue(context, sourceParam);
		}

		if (value != null && link.isCloningSource())
		{
			// This link want to have the source value cloned
			try
			{
				value = CopyUtil.copyObject(value, Copyable.COPY_DEEP, context.getExecutingModel().getClassLoader());
			}
			catch (Exception e)
			{
				throw new EngineException("Clone", "Cloning of data link value failed.", e);
			}
		}

		//
		// Set the target value
		//
		if (targetMember != null)
		{
			// We have a member path specification for the target.
			// We need an expression parser to evaluate it.
			ExpressionParser parser = EngineUtil.createExpressionParser(context, engine);

			String contextName = targetParam.getContextName();
			String paramName = targetParam.getName();
			int pos = contextName.lastIndexOf(paramName);
			String contextPrefix = contextName.substring(0, pos);

			parser.setContextPrefix(contextPrefix);

			// Provide the target parameter type and the 'create all objects' flag to the parser,
			// so any members that are missing in the path will be created on the fly.
			parser.setContextPathValue(targetMember, value, targetParam.getDataType(), ExpressionParser.CREATE_ALL_OBJECTS);
		}
		else
		{
			// Add the value directly as target parameter value to the context
			TokenContextUtil.setParamValue(context, targetParam, value);
		}

		if (engine.hasActiveObservers(EngineTraceEvent.DATA_FLOW, context))
		{
			engine.fireEngineEvent(new EngineTraceEvent(EngineTraceEvent.DATA_FLOW, context, link, value, engine));
		}
	}

	private void unregisterSession()
	{
		SessionRegistry sessionRegistry = engine.getSessionRegistry();
		if (sessionRegistry != null)
		{
			sessionRegistry.unregisterSession(context.getId());
		}
	}
}
