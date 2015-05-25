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

import org.openbp.common.generic.LifecycleSupport;

/**
 * The token context service provides token context and workflow task management services to the execution engine and the application.
 * There are currently two implementations of the service:<br>
 * - a persistent form of the service ({@link PersistentTokenContextService})<br>
 * - a transient form of the service ({@link TransientTokenContextService})
 *
 * @author Author: Heiko Erhardt
 */
public interface TokenContextService
	extends LifecycleSupport
{
	//////////////////////////////////////////////////
	// @@ General
	//////////////////////////////////////////////////

	/**
	 * Begins the transaction.
	 */
	public void begin();

	/**
	 * Flushes the recent changes (if supported).
	 */
	public void flush();

	/**
	 * Commits the recent changes (if supported).
	 */
	public void commit();

	/**
	 * Rolls back the recent changes (if supported).
	 */
	public void rollback();

	/**
	 * Clears any this token context service might have.
	 */
	public void clearCache();

	//////////////////////////////////////////////////
	// @@ Context management
	//////////////////////////////////////////////////

	/**
	 * Creates a context.
	 *
	 * @return The new context
	 */
	public TokenContext createContext();

	/**
	 * Creates a new child context.
	 * 
	 * @param parentContext Parent token context
	 * @return The context
	 */
	public TokenContext createChildContext(TokenContext parentContext);

	/**
	 * Adds a context to the service.
	 *
	 * @param context Context to add
	 * @return The saved object
	 */
	public TokenContext addContext(TokenContext context);

	/**
	 * Save the changes to a context.
	 *
	 * @param context Context to save
	 * @return The saved object
	 */
	public TokenContext saveContext(TokenContext context);

	/**
	 * Removes a context from the service.
	 *
	 * @param context Context to remove
	 */
	public void deleteContext(TokenContext context);

	/**
	 * Evicts the context from the cache of the underlying persistence layer, if any.
	 *
	 * @param context Context to evict
	 */
	public void evictContext(final TokenContext context);

	/**
	 * Retrieves a token context by its id.
	 *
	 * @param id Context id
	 * @return The context or null if no such context exists
	 */
	public TokenContext getContextById(Object id);

	/**
	 * Returns an iterator of token contexts that match the given selection criteria.
	 *
	 * @param criteria Criteria to match
	 * @param maxResults Maximum number of result records or 0 for all
	 * @return An iterator of {@link TokenContext} objects.
	 * The objects will be sorted by their priority (ascending).
	 */
	public Iterator getContexts(TokenContextCriteria criteria, int maxResults);

	/**
	 * Gets the token context objects that are ready to execute.
	 *
	 * @param maxResults Maximum number of result records or 0 for all
	 * @return An iterator of {@link TokenContext} objects.
	 * The objects will be sorted by their priority (ascending).
	 */
	public Iterator getExecutableContexts(int maxResults);

	/**
	 * Gets the child contexts of the specified context.
	 *
	 * @param context Context
	 * @return An iterator of child contexts
	 */
	public Iterator getChildContexts(TokenContext context);

	/**
	 * Changes the state of all matching token context objects in the context store.
	 * This method can be used to fix the state of selected or running contexts after a system crash.
	 *
	 * @param fromLifecycleState Lifecycle state to search for
	 * @param toLifecycleState New lifecycle state for matching context objects
	 * @param toLifecycleRequest New lifecycle request for matching context objects
	 * @param nodeId System name of the system these contexts have been assigned to or null for all context objects
	 * @return The number of context objects that have been updated
	 */
	public int changeContextState(int fromLifecycleState, int toLifecycleState, int toLifecycleRequest, String nodeId);

	//////////////////////////////////////////////////
	// @@ Workflow task management
	//////////////////////////////////////////////////

	/**
	 * Creates a workflow task.
	 * However, the workflow task is not added to the workflow task list.
	 * 
	 * @param context Token context to associate with the workflow task
	 * @return The new workflow task
	 */
	public WorkflowTask createWorkflowTask(TokenContext context);

	/**
	 * Adds a workflow task to the service.
	 *
	 * @param workflowTask workflow task to add
	 * @return The saved object
	 */
	public WorkflowTask addWorkflowTask(WorkflowTask workflowTask);

	/**
	 * Save the changes to a workflow task.
	 *
	 * @param workflowTask workflow task to save
	 * @return The saved object
	 */
	public WorkflowTask saveWorkflowTask(WorkflowTask workflowTask);

	/**
	 * Removes a workflow task from the service.
	 *
	 * @param workflowTask workflow task to remove
	 */
	public void deleteWorkflowTask(WorkflowTask workflowTask);

	/**
	 * Returns an iterator of workflow tasks that match the given selection criteria.
	 *
	 * @param criteria Criteria to match
	 * @return An iterator of {@link WorkflowTask} objects
	 */
	public Iterator getworkflowTasks(WorkflowTaskCriteria criteria);
}
