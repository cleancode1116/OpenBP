package org.openbp.server;

import java.util.Iterator;
import java.util.Map;

import org.openbp.common.logger.LogUtil;
import org.openbp.core.OpenBPException;
import org.openbp.core.engine.EngineException;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.item.process.InitialNode;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.type.DataTypeItem;
import org.openbp.core.model.item.type.ValidationException;
import org.openbp.server.context.LifecycleRequest;
import org.openbp.server.context.LifecycleState;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextCriteria;
import org.openbp.server.context.TokenContextService;
import org.openbp.server.context.TokenContextUtil;
import org.openbp.server.context.WorkflowTask;
import org.openbp.server.context.WorkflowTaskCriteria;
import org.openbp.server.engine.Engine;
import org.openbp.server.engine.EngineEvent;
import org.openbp.server.engine.EngineRunner;

/**
 * The process facade provides the standard functionality for running processes.
 * It is a convenient shortcut to a variety of services provided by the OpenBP framework components.
 *
 * @author Heiko Erhardt
 */
public class ProcessFacadeImpl
	implements ProcessFacade
{
	/** Engine */
	private Engine engine;

	/** Engine runner */
	private EngineRunner engineRunner;

	/**
	 * Default constructor.
	 */
	public ProcessFacadeImpl()
	{
	}

	//////////////////////////////////////////////////
	// @@ Token and workflow lifecycle and control
	//////////////////////////////////////////////////

	/**
	 * Creates a new token.
	 * This method just creates the context, it does commit it to the database.
	 * Use the {@link #startToken(TokenContext)} method to start the process and to persist it to the database.
	 *
	 * @return The new token
	 */
	public TokenContext createToken()
	{
		return engine.getTokenContextService().createContext();
	}

	/**
	 * Starts the given token at the given start reference using the optional parameter map.
	 * The token is marked for further execution by the process engine.
	 *
	 * @param context Token context
	 * @param startRef Reference to the start node/socket ("/Process/Node").
	 * For a description of the supported formats, see the {@link Engine#resolveSocketRef} method.
	 * @param inputParamValues A map of parameter name/parameter value pairs that will be assigned to the
	 * parameters of the target socket or null
	 */
	public void startToken(TokenContext context, String startRef, Map inputParamValues)
	{
		setInitialPosition(context, startRef, inputParamValues);
		startToken(context);
	}

	/**
	 * Starts the given token at its current position.
	 * The token is marked for further execution by the process engine.
	 *
	 * @param context Token context
	 */
	public void startToken(TokenContext context)
	{
		engine.startToken(context);
	}

	/**
	 * Simply saves the given token as suspended token.
	 * Can be used to save token that shall be executed by the scheduler to the database without starting them.
	 * The method commits the changes to the database.
	 *
	 * @param context Token context
	 */
	public void prepareTokenForScheduler(TokenContext context)
	{
		engine.prepareTokenForScheduler(context);
	}

	/**
	 * Resumes the given suspended token at the given position using the optional parameter map.
	 * The token is marked for further execution by the process engine.
	 *
	 * If the token was suspended using a workflow node, you should use the {@link #resumeToken(TokenContext)} method.
	 *
	 * @param context Token context
	 * @param resumptionRef Reference to the point of resumption or null for resumption at the current exit socket.
	 * For a description of the supported formats, see the {@link Engine#resolveSocketRef} method.
	 * @param inputParamValues A map of parameter name/parameter value pairs that will be assigned to the
	 * parameters of the target socket or null
	 */
	public void resumeToken(TokenContext context, String resumptionRef, Map inputParamValues)
	{
		setResumptionPosition(context, resumptionRef, inputParamValues);
		resumeToken(context);
	}

	/**
	 * Resumes the given suspended token at its current position.
	 * The token is marked for further execution by the process engine.
	 *
	 * If the token was suspended using a workflow node, you should use the {@link #resumeToken(TokenContext)} method.
	 *
	 * @param context Token context
	 */
	public void resumeToken(TokenContext context)
	{
		engine.resumeToken(context);
	}

	/**
	 * Ends the token.
	 * If the token is currently in execution, this method simply calls {@link TokenContext#setLifecycleRequest}
	 * method with the LifecycleRequest.STOP argument, instructing the OpenBP engine to end the execution gracefully.
	 *
	 * Otherwise, the token and all of its child tokens and associated workflow tasks will be deleted from
	 * the database. In this case, the method also sets the state of the workflow task that is associated
	 * with this context (if any) to {@link WorkflowTask#STATUS_COMPLETED} or deletes the workflows.
	 * Also raises the engine events {@link EngineEvent#BEFORE_END_TOKEN} and {@link EngineEvent#AFTER_END_TOKEN}.
	 *
	 * @param context Context that holds the token's state information
	 */
	public void endToken(TokenContext context)
	{
		// TODO endToken needs to check state of the context and act accordingly: setLifecycleRequest(LifecycleRequest.STOP)
		engine.endToken(context);
	}

	/**
	 * Resumes a suspended workflow.
	 * - Assigns the workflow to the current user if desired.<br>
	 * - Sets the status of the workflow to {@link WorkflowTask#STATUS_RESUMED}.<br>
	 * - Set the AcceptingUserId and TimeAccepted properties of the workflow task.<br>
	 * - Determines the socket of the workflow node to resume at.<br>
	 * - Begins a transaction, saves the workflow task and commit the transaction.<br>
	 * The lifecycle request of the token will be in state {@link LifecycleRequest#RESUME}.
	 *
	 * Use this method instead of {@link #resumeToken(TokenContext)} if the token was suspended using a workflow node.
	 *
	 * @param workflowTask Workflow task this workflow refers to
	 * @param resumptionRef Reference to the point of resumption or null for resumption at the current exit socket
	 * (usually the name of an exit socket of the current node, but may also specifiy an entry node name of the current process).
	 * @param currentUserId Id of the user that accepts this workflow (may be null);
	 * will be assigned to the 'AcceptingUser' property of the workflow and to the 'UserId' of the workflow
	 * if the 'AssignToCurrentUser' property of the workflow has been set.
	 * @throws OpenBPException Any exception that may occur during start of the preparation of the workflow task
	 */
	public void resumeWorkflow(WorkflowTask workflowTask, String resumptionRef, String currentUserId)
	{
		engine.resumeWorkflow(workflowTask, resumptionRef, currentUserId);
	}

	/**
	 * Sets the initial position of a process using the given start reference.
	 *
	 * @param context Token context
	 * @param startRef Reference to the start node/socket. For a description of the supported formats, see the {@link Engine#resolveSocketRef} method.
	 * @param inputParamValues A map of parameter name/parameter value pairs that will be assigned to the
	 * parameters of the target socket or null
	 */
	public void setInitialPosition(TokenContext context, String startRef, Map inputParamValues)
	{
		NodeSocket startSocket = getEngine().resolveSocketRef(startRef, context.getCurrentSocket(), context, true);

		InitialNode initialNode = (InitialNode) startSocket.getNode();
		if (initialNode.getEntryScope() != InitialNode.SCOPE_PUBLIC)
		{
			String msg = LogUtil.error(getClass(), "Node $0 is not a public initial node (start node reference: $1). [{2}]", initialNode.getQualifier(), startRef, context);
			throw new ModelException("NoPublicInitialNode", msg);
		}
		context.setCurrentSocket(startSocket);

		if (context.getExecutingModel() == null)
		{
			context.setExecutingModel(startSocket.getProcess().getModel());
		}

		bindInputParameters(context, startSocket, inputParamValues);
	}

	/**
	 * Sets the resumption position of a suspended process using the given socket name.
	 *
	 * @param context Token context
	 * @param resumptionRef Reference to the point of resumption or null for resumption at the current exit socket.
	 * For a description of the supported formats, see the {@link Engine#resolveSocketRef} method.
	 * @param inputParamValues A map of parameter name/parameter value pairs that will be assigned to the
	 * parameters of the target socket or null
	 */
	public void setResumptionPosition(TokenContext context, String resumptionRef, Map inputParamValues)
	{
		NodeSocket startSocket = getEngine().resolveSocketRef(resumptionRef, context.getCurrentSocket(), context, true);
		context.setCurrentSocket(startSocket);

		bindInputParameters(context, startSocket, inputParamValues);
	}

	/**
	 * Binds the given input parameters to the parameters ofthe entry socket.
	 *
	 * @param context Token context
	 * @param socket Entry socket
	 * @param inputParamValues A map of parameter name/parameter value pairs that will be assigned to the
	 * parameters of the target socket or null
	 */
	private void bindInputParameters(TokenContext context, NodeSocket socket, Map inputParamValues)
	{
		if (inputParamValues != null)
		{
			LogUtil.debug(getClass(), "Binding request parameters to socket $0.", socket.getQualifier());

			Iterator it = socket.getParams();
			if (it.hasNext())
			{
				// Iterate all parameters of the exit socket
				while (it.hasNext())
				{
					NodeParam nodeParam = (NodeParam) it.next();

					bindInputParameter(context, nodeParam, inputParamValues);
				}
			}
		}
	}

	/**
	 * Handle the parameter binding debending of the dynamic visual.
	 * 
	 * If there is used a dynamic visual, the parameter of the visual is
	 * important in regard to the data type for the parameter binding.
	 * 
	 * Normally the datatype of the node parameter is used.
	 * 
	 * @param context Token context
	 * @param param Node parameter
	 * @param inputParamValues A map of parameter name/parameter value pairs or null
	 * @throws OpenBPException On any exception
	 */
	private void bindInputParameter(TokenContext context, NodeParam param, Map inputParamValues)
	{
		String name = param.getName();

		DataTypeItem type = param.getDataType();

		if (type == null)
			// No type specified, skip this parameter
			return;

		// Get the current value of the parameter as default we can operate on
		// if it exists
		Object value = inputParamValues != null ? inputParamValues.get(name) : null;

		if (type.isSimpleType())
		{
			// Get the parameter value

			try
			{
				if (value != null)
				{
					if (value instanceof String)
					{
						// Try to convert the string representation to the type
						// we expect for this parameter
						value = type.convertFromString((String) value, null, null);
					}
					else
					{
						// Check if the value type matches the parameter type;
						// otherwise the application programmer obviously supplied the wrong type of data in the input parameters.
						if (! type.getJavaClass().isAssignableFrom(value.getClass()))
							throw new EngineException("IncorrectParameterType", "Cannot bind value of type "
								+ value.getClass() + " to parameter '" + param.getQualifier() + "' (type "
								+ type.getJavaClass() + ").");
					}
				}
			}
			catch (ValidationException e)
			{
				// Conversion failed, log the error
				throw new EngineException("ParamterValidation", "Error binding value of type " + value.getClass()
					+ " to parameter '" + param.getQualifier() + "' (type " + type.getJavaClass() + ").", e);
			}
		}
		else
		{
			// Complex type; instantiate it if not present yet
			if (value != null)
			{
				if (! type.getJavaClass().isAssignableFrom(value.getClass()))
					throw new EngineException("IncorrectParameterType", "Cannot bind value of type " + value.getClass()
						+ " to parameter '" + param.getQualifier() + "' (type " + type.getJavaClass() + ").");
			}
		}

		// Assign the result to the parameter
		TokenContextUtil.setParamValue(context, param, value);
	}
	/**
	 * Commits the transaction on the token context store.
	 */
	public void commitTokenContextTransaction()
	{
		engine.commitTokenContextTransaction();
	}

	/**
	 * Rolls back the transaction on the token context store.
	 */
	public void rollbackTokenContextTransaction()
	{
		engine.rollbackTokenContextTransaction();
	}

	/**
	 * Retrieves the output variables of the given context.
	 * Collects the values of all parameters of the current socket
	 * (i. e. the exit socket the process ended with) and stores them in the supplied map.
	 * @param context Token context
	 * @param outputParamValues Map to fill
	 */
	public void retrieveOutputParameters(TokenContext context, Map outputParamValues)
	{
		// Copy parameters of exit socket
		NodeSocket socket = context.getCurrentSocket();
		if (socket == null)
			return;

		for (Iterator it = socket.getParams(); it.hasNext();)
		{
			NodeParam param = (NodeParam) it.next();

			Object value = TokenContextUtil.getParamValue(context, param);
			outputParamValues.put(param.getName(), value);
		}
	}

	/**
	 * Retrieves a token context by its id.
	 *
	 * @param id Context id
	 * @return The context or null if no such context exists
	 */
	public TokenContext getTokenById(Object id)
	{
		return engine.getTokenContextService().getContextById(id);
	}

	/**
	 * Returns an iterator of token contexts that match the given selection criteria.
	 *
	 * @param criteria Criteria to match
	 * @param maxResults Maximum number of result records or 0 for all
	 * @return An iterator of {@link TokenContext} objects.
	 * The objects will be sorted by their priority (ascending).
	 */
	public Iterator getTokens(TokenContextCriteria criteria, int maxResults)
	{
		return engine.getTokenContextService().getContexts(criteria, maxResults);
	}

	/**
	 * Returns an iterator of workflow tasks that match the given selection criteria.
	 *
	 * @param criteria Criteria to match
	 * @return An iterator of {@link WorkflowTask} objects
	 */
	public Iterator getworkflowTasks(WorkflowTaskCriteria criteria)
	{
		return engine.getTokenContextService().getworkflowTasks(criteria);
	}

	/**
	 * Resets the state of tokens that were currently executing after a system crash.
	 * Each token of the specified node in the state {@link LifecycleState#SELECTED} or {@link LifecycleState#RUNNING}
	 * will be set to LifecycleState.SUSPENDED and LifecycleRequest.RESUME.
	 * This will cause the engine to resume the process.
	 * Call this method during application startup to prevent tokens from being lost due to a system crash.
	 * However, NEVER call this method during normal system operation, which might set currently running
	 * tokens to an invalid state.
	 *
	 * @param nodeId Node id or null for the current node
	 * @return The number of tokens that have been updated
	 */
	public int resetExecutingTokenState(String nodeId)
	{
		if (nodeId == null)
		{
			nodeId = engineRunner.getSystemNameProvider().getSystemName();
		}

		int ret = engine.getTokenContextService().changeContextState(LifecycleState.SELECTED, LifecycleState.SUSPENDED, LifecycleRequest.RESUME, nodeId);
		ret += engine.getTokenContextService().changeContextState(LifecycleState.RUNNING, LifecycleState.SUSPENDED, LifecycleRequest.RESUME, nodeId);
		return ret;
	}

	//////////////////////////////////////////////////
	// @@ Process execution
	//////////////////////////////////////////////////

	/**
	 * Main execution loop.
	 * Use this method in the thread that reads pending token contexts and distributes them for execution.
	 * The method never returns!
	 * @param sleepTime When there are no context available for execution, the method will sleep for the supplied sleep time (in milli seconds).
	 */
	public void mainExecutionLoop(int sleepTime)
	{
		engineRunner.mainExecutionLoop(sleepTime);
	}

	/**
	 * Requests the end of the main execution loop and wait until all currently
	 * executing contexts have come to an halt.
	 * The method will return if either all contexts have finished executing
	 * (i. e. reached a wait state or have ended) or if the specified timeout has elapsed.
	 *
	 * @param timeoutMS Timeout in milliseconds.
	 * If this value is 0, the method will just check if everything has completed, but will not wait for any processes.
	 * If this value is -1, no timeout will apply (i. e. the method will definately wait
	 * until all context executions have finished).
	 * @return true If no context is currently executing, false if the timeout has elapsed
	 */
	public boolean waitForStop(long timeoutMS)
	{
		return engineRunner.waitForStop(timeoutMS);
	}

	/**
	 * Executes token contexts that are ready for execution in a different thread.
	 * The method will query the executable contexts using the token context service.
	 * Each context retrieved will be executed using the thread distribution
	 * strategy of the particular {@link EngineRunner} implementation.
	 * The method returns after it has processed (i. e. distributed to the threads)
	 * the last element of the context list returned by the
	 * {@link TokenContextService#getExecutableContexts} method.
	 * Since the execution will be asynchronously, this does not mean that the contexts
	 * have finished their execution at this point in time.
	 * @return The number of contexts that have been retrieved for execution and passed to the thread pool
	 */
	public int executePendingContextsInDifferentThread()
	{
		return engineRunner.executePendingContextsInDifferentThread();
	}

	/**
	 * Executes token contexts that are ready fro execution in this thread.
	 * Since this will block the thread, this method is intended for test cases primarily.
	 * The method will query the executable contexts using the token context service.
	 * Each context retrieved will be executed directly in this thread.
	 * The method returns after it has processed the last element of the context list
	 * returned by the {@link TokenContextService#getExecutableContexts} method.
	 * @return true if the method has found contexts for execution, false otherwise
	 */
	public boolean executePendingContextsInThisThread()
	{
		return engineRunner.executePendingContextsInThisThread();
	}

	/**
	 * Executes the given context immediately in this thread.
	 * Since this will block the thread, this method is intended for test cases and for situations where
	 * the completion of a process execution is needed in order to continue the program.
	 *
	 * @param context Context to execute
	 */
	public void executeContextInThisThread(TokenContext context)
	{
		engineRunner.executeContextInThisThread(context);
	}

	//////////////////////////////////////////////////
	// @@ Component setter/getter
	//////////////////////////////////////////////////

	/**
	 * Gets the engine.
	 * @nowarn
	 */
	public Engine getEngine()
	{
		return engine;
	}

	/**
	 * Sets the engine.
	 * @nowarn
	 */
	public void setEngine(Engine engine)
	{
		this.engine = engine;
	}

	/**
	 * Gets the engine runner.
	 * @nowarn
	 */
	public EngineRunner getEngineRunner()
	{
		return engineRunner;
	}

	/**
	 * Sets the engine runner.
	 * @nowarn
	 */
	public void setEngineRunner(EngineRunner engineRunner)
	{
		this.engineRunner = engineRunner;
	}

}
