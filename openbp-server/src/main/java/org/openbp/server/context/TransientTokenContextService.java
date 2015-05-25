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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.openbp.common.CollectionUtil;
import org.openbp.common.CommonUtil;
import org.openbp.common.property.PropertyAccessUtil;
import org.openbp.common.property.PropertyException;
import org.openbp.core.engine.EngineException;

/**
 * This token context service will keept its contexts entirely in memory.
 * The {@link #addContext} method will add the contexts to execute to the internal context list.
 * The {@link #getExecutableContexts} method will check this internal list for pending contexts.
 * The contexts are not persisted by the service.
 *
 * @author Author: Heiko Erhardt
 */
public class TransientTokenContextService extends TokenContextServiceBase
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	// Note that we use maps instead of lists in order to prevent the situation that a context is being schedulded for execution multiple times.
	// This might lead to unexpected results otherwise.

	/** Map of managed contexts */
	private final HashMap contexts;

	/** Map of managed workflows */
	private final HashMap workflowTasks;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public TransientTokenContextService()
	{
		contexts = new LinkedHashMap();
		workflowTasks = new HashMap();
	}

	//////////////////////////////////////////////////
	// @@ General
	//////////////////////////////////////////////////

	/**
	 * Begins the transaction.
	 */
	public void begin()
	{
		// No begin
	}

	/**
	 * Flushes the recent changes (if supported).
	 * Does nothing for this implementation.
	 */
	public void flush()
	{
		// No flushing
	}

	/**
	 * Commits the recent changes (if supported).
	 * Does nothing for this implementation.
	 */
	public void commit()
	{
		// No commit/rollback support
	}

	/**
	 * Rolls back the recent changes (if supported).
	 * Does nothing for this implementation.
	 */
	public void rollback()
	{
		// No commit/rollback support
	}

	/**
	 * Clears any this token context service might have.
	 */
	public void clearCache()
	{
		// No cache
	}

	//////////////////////////////////////////////////
	// @@ Context management
	//////////////////////////////////////////////////

	/**
	 * Adds a context to the service.
	 *
	 * @param context Context to add
	 * @return The saved object
	 */
	public TokenContext addContext(final TokenContext context)
	{
		synchronized (contexts)
		{
			contexts.put(context, context);
		}
		return context;
	}

	/**
	 * Save the changes to a context.
	 *
	 * @param context Context to save
	 * @return The saved object
	 */
	public TokenContext saveContext(final TokenContext context)
	{
		return context;
	}

	/**
	 * Remvoes a context from the service.
	 *
	 * @param context Context to remove
	 */
	public void deleteContext(final TokenContext context)
	{
		synchronized (contexts)
		{
			contexts.remove(context);
		}
	}

	/**
	 * Evicts the context from the cache of the underlying persistence layer, if any.
	 *
	 * @param context Context to evict
	 */
	public void evictContext(final TokenContext context)
	{
		// Do nothing
	}

	/**
	 * Returns an iterator of token contexts that match the given selection criteria.
	 *
	 * @param criteria Criteria to match
	 * @param maxResults Maximum number of result records or 0 for all (ignored)
	 * @return An iterator of {@link TokenContext} objects
	 */
	public Iterator getContexts(final TokenContextCriteria criteria, final int maxResults)
	{
		ArrayList ret = new ArrayList();

		synchronized (contexts)
		{
			// Copy due to ConcurrentModificationException 
			for (Iterator it = contexts.keySet().iterator(); it.hasNext();)
			{
				TokenContext context = (TokenContext) it.next();

				if (match(context, criteria))
					ret.add(context);
			}
		}

		return ret.iterator();
	}

	/**
	 * Gets the token context objects that are ready to execute.
	 *
	 * @param maxResults Maximum number of result records or 0 for all (ignored)
	 * @return An iterator of {@link TokenContext} objects
	 */
	public Iterator getExecutableContexts(final int maxResults)
	{
		ArrayList ret = new ArrayList();

		synchronized (contexts)
		{
			for (Iterator it = contexts.keySet().iterator(); it.hasNext();)
			{
				TokenContext context = (TokenContext) it.next();

				if (context.getLifecycleRequest() == LifecycleRequest.RESUME)
					ret.add(context);
			}
		}

		// Sort by priority
		Collections.sort(ret, new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				int p1 = ((TokenContext) o1).getPriority();
				int p2 = ((TokenContext) o2).getPriority();
				return p1 - p2;
			}
		});

		return ret.iterator();
	}

	private boolean match(final TokenContext context, final TokenContextCriteria criteria)
	{
		if (criteria != null)
		{
			if (criteria.getId() != null)
			{
				if (! criteria.getId().equals(context.getId()))
					return false;
			}

			if (criteria.getLifecycleState() != null)
			{
				if (! criteria.getLifecycleState().equals(Integer.valueOf(context.getLifecycleState())))
					return false;
			}

			if (criteria.getModel() != null)
			{
				if (! criteria.getModel().equals(context.getExecutingModel()))
					return false;
			}

			if (! matchCriterionBase(context, criteria))
				return false;
		}

		return true;
	}

	/**
	 * Gets the child contexts of the specified context.
	 *
	 * @param context Context
	 * @return An iterator of child contexts
	 */
	public Iterator getChildContexts(TokenContext context)
	{
		ArrayList children = new ArrayList();
		CollectionUtil.addAll(children, context.getChildContexts());
		return children.iterator();
	}

	/**
	 * Changes the state of all matching token context objects in the context store.
	 * This method can be used to fix the state and request of selected or running contexts after a system crash.
	 *
	 * @param fromLifecycleState Lifecycle state to search for
	 * @param toLifecycleState New lifecycle state for matching context objects
	 * @param toLifecycleRequest New lifecycle request for matching context objects
	 * @param nodeId System name of the system these contexts have been assigned to or null for all context objects
	 * @return The number of tokens that have been updated
	 */
	public int changeContextState(int fromLifecycleState, int toLifecycleState, int toLifecycleRequest, String nodeId)
	{
		int ret = 0;
		synchronized (contexts)
		{
			// Copy due to ConcurrentModificationException 
			for (Iterator it = contexts.keySet().iterator(); it.hasNext();)
			{
				TokenContext context = (TokenContext) it.next();

				if (context.getLifecycleState() != fromLifecycleState)
				{
					continue;
				}

				if (nodeId != null)
				{
					if (! nodeId.equals(context.getNodeId()))
					{
						continue;
					}
				}

				context.setLifecycleState(toLifecycleState);
				context.setLifecycleRequest(toLifecycleRequest);
				++ret;
			}
		}
		return ret;
	}

	//////////////////////////////////////////////////
	// @@ Workflow task management
	//////////////////////////////////////////////////

	/**
	 * Adds a workflow task to the service.
	 *
	 * @param workflowTask workflow task to add
	 * @return The saved object
	 */
	public WorkflowTask addWorkflowTask(final WorkflowTask workflowTask)
	{
		synchronized (workflowTasks)
		{
			workflowTasks.put(workflowTask, workflowTask);
		}
		return workflowTask;
	}

	/**
	 * Save the changes to a workflow task.
	 *
	 * @param workflowTask workflow task to save
	 * @return The saved object
	 */
	public WorkflowTask saveWorkflowTask(final WorkflowTask workflowTask)
	{
		return workflowTask;
	}

	/**
	 * Removes a workflow task from the service.
	 *
	 * @param workflowTask workflow task to remove
	 */
	public void deleteWorkflowTask(final WorkflowTask workflowTask)
	{
		synchronized (workflowTasks)
		{
			workflowTasks.remove(workflowTask);
		}
	}

	/**
	 * Returns an iterator of workflow tasks that match the given selection criteria.
	 *
	 * @param criteria Criteria to match
	 * @return An iterator of {@link WorkflowTask} objects
	 */
	public Iterator getworkflowTasks(final WorkflowTaskCriteria criteria)
	{
		ArrayList ret = new ArrayList();

		synchronized (workflowTasks)
		{
			// Copy due to ConcurrentModificationException 
			for (Iterator it = workflowTasks.keySet().iterator(); it.hasNext();)
			{
				WorkflowTask workflowTask = (WorkflowTask) it.next();

				if (match(workflowTask, criteria))
					ret.add(workflowTask);
			}
		}

		return ret.iterator();
	}

	private boolean match(final WorkflowTask task, final WorkflowTaskCriteria criteria)
	{
		if (criteria.getId() != null)
		{
			if (! criteria.getId().equals(task.getId()))
				return false;
		}

		if (criteria.getStatus() != null)
		{
			if (! criteria.getStatus().equals(Integer.valueOf(task.getStatus())))
				return false;
		}

		if (criteria.getModel() != null)
		{
			if (! criteria.getModel().equals(task.getTokenContext().getExecutingModel()))
				return false;
		}

		if (criteria.getName() != null)
		{
			if (! criteria.getName().equals(task.getName()))
				return false;
		}

		if (criteria.getStepName() != null)
		{
			if (! criteria.getStepName().equals(task.getStepName()))
				return false;
		}

		if (criteria.getRoleId() != null)
		{
			if (! criteria.getRoleId().equals(task.getRoleId()))
				return false;
		}

		if (criteria.getUserId() != null)
		{
			if (! criteria.getUserId().equals(task.getUserId()))
				return false;
		}

		if (! matchCriterionBase(task, criteria))
			return false;

		return true;
	}

	private boolean matchCriterionBase(final Object obj, final CriteriaBase criteria)
	{
		for (Iterator it = criteria.getCustomCriteriaKeys(); it.hasNext();)
		{
			String key = (String) it.next();
			Object value = criteria.getCustomCriteriaValue(key);
			if (value != null)
			{
				try
				{
					Object actualValue = PropertyAccessUtil.getProperty(obj, key);
					if (! CommonUtil.equalsNull(value, actualValue))
						return false;
				}
				catch (PropertyException e)
				{
					throw new EngineException("Expression.CustomCriteriumPropertyAccessFailed", e);
				}
			}
		}
		return true;
	}

}
