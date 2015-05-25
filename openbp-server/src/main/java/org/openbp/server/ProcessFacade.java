package org.openbp.server;

import java.util.Iterator;
import java.util.Map;

import org.openbp.core.OpenBPException;
import org.openbp.server.context.LifecycleRequest;
import org.openbp.server.context.LifecycleState;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextCriteria;
import org.openbp.server.context.TokenContextService;
import org.openbp.server.context.WorkflowTask;
import org.openbp.server.context.WorkflowTaskCriteria;
import org.openbp.server.engine.Engine;
import org.openbp.server.engine.EngineRunner;

/**
 * The process facade provides the standard functionality for running processes.
 * It is a convenient shortcut to a variety of services provided by the OpenBP framework components.<br>
 * Call the {@link #commitTokenContextTransaction} after calling the {@link #startToken(TokenContext)}, {@link #resumeToken(TokenContext)} or
 * {@link #resumeWorkflow} methods.
 *
 * @author Heiko Erhardt
 */
public interface ProcessFacade
{
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
	public TokenContext createToken();

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
	public void startToken(TokenContext context, String startRef, Map inputParamValues);

	/**
	 * Starts the given token at its current position.
	 * The token is marked for further execution by the process engine.
	 *
	 * @param context Token context
	 */
	public void startToken(TokenContext context);

	/**
	 * Simply saves the given token as suspended token.
	 * Can be used to save token that shall be executed by the scheduler to the database without starting them.
	 * The method commits the changes to the database.
	 *
	 * @param context Token context
	 */
	public void prepareTokenForScheduler(TokenContext context);

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
	public void resumeToken(TokenContext context, String resumptionRef, Map inputParamValues);

	/**
	 * Resumes the given token at its current position.
	 * The token is marked for further execution by the process engine.
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
	 * - Begins a transaction, saves the workflow task and commit the transaction.<br>
	 * The lifecycle request of the token will be in state {@link LifecycleRequest#RESUME}.
	 *
	 * @param workflowTask Workflow task this workflow refers to
	 * @param resumptionRef Reference to the point of resumption or null for resumption at the current exit socket
	 * (usually the name of an exit socket of the current node, but may also specifiy an entry node name of the current process).
	 * @param currentUserId Id of the user that accepts this workflow (may be null);
	 * will be assigned to the 'AcceptingUser' property of the workflow and to the 'UserId' of the workflow
	 * if the 'AssignToCurrentUser' property of the workflow has been set.
	 * @throws OpenBPException Any exception that may occur during start of the preparation of the workflow task
	 */
	public void resumeWorkflow(WorkflowTask workflowTask, String resumptionRef, String currentUserId);

	/**
	 * Sets the initial position of a process using the given start reference.
	 *
	 * @param context Token context
	 * @param startRef Reference to the start node/socket. For a description of the supported formats, see the {@link Engine#resolveSocketRef} method.
	 * @param inputParamValues A map of parameter name/parameter value pairs that will be assigned to the
	 * parameters of the target socket or null
	 */
	public void setInitialPosition(TokenContext context, String startRef, Map inputParamValues);

	/**
	 * Sets the resumption position of a suspended process using the given socket name.
	 *
	 * @param context Token context
	 * @param resumptionRef Reference to the point of resumption or null for resumption at the current exit socket.
	 * For a description of the supported formats, see the {@link Engine#resolveSocketRef} method.
	 * @param inputParamValues A map of parameter name/parameter value pairs that will be assigned to the
	 * parameters of the target socket or null
	 */
	public void setResumptionPosition(TokenContext context, String resumptionRef, Map inputParamValues);

	/**
	 * Commits the transaction on the token context store.
	 */
	public void commitTokenContextTransaction();

	/**
	 * Rolls back the transaction on the token context store.
	 */
	public void rollbackTokenContextTransaction();

	/**
	 * Retrieves the output variables of the given context.
	 * Collects the values of all parameters of the current socket
	 * (i. e. the exit socket the process ended with) and stores them in the supplied map.
	 * @param context Token context
	 * @param outputParamValues Map to fill
	 */
	public void retrieveOutputParameters(TokenContext context, Map outputParamValues);

	/**
	 * Retrieves a token context by its id.
	 *
	 * @param id Token context id
	 * @return The context or null if no such context exists
	 */
	public TokenContext getTokenById(Object id);

	/**
	 * Returns an iterator of token contexts that match the given selection criteria.
	 *
	 * @param criteria Criteria to match
	 * @param maxResults Maximum number of result records or 0 for all
	 * @return An iterator of {@link TokenContext} objects.
	 * The objects will be sorted by their priority (ascending).
	 */
	public Iterator getTokens(TokenContextCriteria criteria, int maxResults);

	/**
	 * Returns an iterator of workflow tasks that match the given selection criteria.
	 *
	 * @param criteria Criteria to match
	 * @return An iterator of {@link WorkflowTask} objects
	 */
	public Iterator getworkflowTasks(WorkflowTaskCriteria criteria);

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
	public int resetExecutingTokenState(String nodeId);

	//////////////////////////////////////////////////
	// @@ Process execution
	//////////////////////////////////////////////////

	/**
	 * Main execution loop.
	 * Use this method in the thread that reads pending token contexts and distributes them for execution.
	 * The method never returns!
	 * @param sleepTime When there are no context available for execution, the method will sleep for the supplied sleep time (in milli seconds).
	 */
	public void mainExecutionLoop(int sleepTime);

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
	public boolean waitForStop(long timeoutMS);

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
	public int executePendingContextsInDifferentThread();

	/**
	 * Executes token contexts that are ready for execution immediately in this thread.
	 * The method will query the executable contexts using the token context service.
	 * Each context retrieved will be executed directly.
	 * The method returns after it has processed (i. e. executed)
	 * the last element of the context list returned by the
	 * {@link TokenContextService#getExecutableContexts} method.
	 * Since this will block the thread, this method is intended for test cases primarily.
	 * @return true if the method has found contexts for execution, false otherwise
	 */
	public boolean executePendingContextsInThisThread();

	/**
	 * Executes the given context immediately in this thread.
	 * Since this will block the thread, this method is intended for test cases and for situations where
	 * the completion of a process execution is needed in order to continue the program.
	 *
	 * @param context Context to execute
	 */
	public void executeContextInThisThread(TokenContext context);
}
