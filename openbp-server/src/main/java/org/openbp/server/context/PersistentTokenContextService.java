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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.openbp.common.CollectionUtil;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.util.iterator.WrappingIterator;
import org.openbp.server.persistence.PersistenceContext;
import org.openbp.server.persistence.PersistenceException;
import org.openbp.server.persistence.PersistenceQuery;
import org.openbp.server.persistence.PersistentObjectNotFoundException;
import org.openbp.server.persistence.TransactionGuard;

/**
 * Token context service providing access to the persistent storage for context and workflow task management.
 *
 * @author Author: Heiko Erhardt
 */
public class PersistentTokenContextService extends TokenContextServiceBase
{
	/**
	 * Isolation level none for the {@link #getExecutableContexts} method: No isolation
	 * The context iterator returned by the method will contain all context that are currently in the state to be executed.
	 * The iterator will not be synchronized with the database as you iterate through the context set.
	 */
	public static final int ISOLATION_LEVEL_NONE = 0;

	/**
	 * Isolation level none for the {@link #getExecutableContexts} method: No isolation
	 * The context iterator returned by the method will contain all context that are currently in the state to be executed.
	 * However, the iterator will synchronize the contexts with the database ('merge') as you iterate through the context set.
	 * Contexts that appear not to be in LifecycleRequest.RESUME state will be skipped to the iterator.
	 */
	public static final int ISOLATION_LEVEL_MERGE = 1;

	/**
	 * Isolation level none for the {@link #getExecutableContexts} method: No isolation
	 * The context iterator returned by the method will contain the first context that is currently in the state to be executed only.
	 */
	public static final int ISOLATION_LEVEL_SINGLE = 2;

	/** Context execution isolation level */
	private int isolationLevel = ISOLATION_LEVEL_MERGE;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public PersistentTokenContextService()
	{
	}

	/**
	 * LifecycleSupport implementation.
	 */
	public void initialize()
	{
	}

	/**
	 * Shuts down the object.
	 */
	public void shutdown()
	{
		PersistenceContext pc = getPersistenceContextProvider().obtainExistingPersistenceContext();
		if (pc != null)
		{
			pc.release();
		}
	}

	//////////////////////////////////////////////////
	// @@ General
	//////////////////////////////////////////////////

	/**
	 * Gets the context execution isolation level.
	 * @return ISOLATION_LEVEL_NONE/ISOLATION_LEVEL_MERGE/ISOLATION_LEVEL_SINGLE
	 */
	public int getIsolationLevel()
	{
		return isolationLevel;
	}

	/**
	 * Sets the context execution isolation level.
	 * @param isolationLevel ISOLATION_LEVEL_NONE/ISOLATION_LEVEL_MERGE/ISOLATION_LEVEL_SINGLE
	 */
	public void setIsolationLevel(final int isolationLevel)
	{
		this.isolationLevel = isolationLevel;
	}

	/**
	 * Begins the transaction.
	 */
	public void begin()
	{
		LogUtil.debug(getClass(), "Beginning transaction.");
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		pc.beginTransaction();
	}

	/**
	 * Flushes the recent changes (if supported).
	 */
	public void flush()
	{
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		pc.flush();
	}

	/**
	 * Commits the recent changes (if supported).
	 */
	public void commit()
	{
		LogUtil.debug(getClass(), "Committing transaction.");
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		pc.commitTransaction();
	}

	/**
	 * Rolls back the recent changes (if supported).
	 */
	public void rollback()
	{
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		pc.rollbackTransaction();
		pc.release();
	}

	/**
	 * Clears any this token context service might have.
	 */
	public void clearCache()
	{
		PersistenceContext pc = getPersistenceContextProvider().obtainExistingPersistenceContext();
		if (pc != null)
		{
			pc.release();
		}
	}

	//////////////////////////////////////////////////
	// @@ Context management
	//////////////////////////////////////////////////

	/**
	 * Adds a context to the service.
	 *
	 * @param context Context to add @return The saved object
	 */
	public TokenContext addContext(final TokenContext context)
	{
		LogUtil.debug(getClass(), "Creating token. [{0}]", context);
		return saveContext(context);
	}

	/**
	 * Save the changes to a context.
	 *
	 * @param context Context to save @return The saved object
	 */
	public TokenContext saveContext(final TokenContext context)
	{
		LogUtil.debug(getClass(), "Updating token. [{0}]", context);
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		return (TokenContext) pc.saveObject(context);
	}

	/**
	 * Removes a context from the service.
	 *
	 * @param context Context to remove
	 */
	public void deleteContext(final TokenContext context)
	{
		LogUtil.debug(getClass(), "Deleting token. [{0}]", context);
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		pc.deleteObject(context);
	}

	/**
	 * Evicts the context from the cache of the underlying persistence layer, if any.
	 *
	 * @param context Context to evict
	 */
	public void evictContext(final TokenContext context)
	{
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		pc.evict(context);
	}

	/**
	 * Retrieves a token context by its id.
	 *
	 * @param id Context id @return The context or null if no such context
	 * exists
	 */
	public TokenContext getContextById(final Object id)
	{
		LogUtil.debug(getClass(), "Loading token having id $0.", id);
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();

		TransactionGuard tg = new TransactionGuard(pc);
		try
		{
			TokenContext context = (TokenContext) pc.findById(id, TokenContext.class);

			if (context != null)
			{
				LogUtil.debug(getClass(), "Refreshing loaded token. [{0}]", context);
				pc.refreshObject(context);
			}

			LogUtil.debug(getClass(), "Loaded token. [{0}]", context);
			return context;
		}
		catch (PersistenceException e)
		{
			tg.doCatch();
			throw e;
		}
		finally
		{
			tg.doFinally();
		}
	}

	/**
	 * Returns an iterator of token contexts that match the given selection criteria.
	 *
	 * @param criteria Criteria to match @return An iterator of {@link TokenContext} objects
	 * @param maxResults Maximum number of result records or 0 for all
	 * @return An iterator of {@link TokenContext} objects.
	 * The objects will be sorted by their priority (ascending).
	 */
	public Iterator getContexts(final TokenContextCriteria criteria, int maxResults)
	{
		if (criteria != null)
		{
			LogUtil.debug(getClass(), "Performing token query (criteria $0).", criteria);
		}
		else
		{
			LogUtil.debug(getClass(), "Performing token query (all tokens).");
		}

		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		PersistenceQuery query = pc.createQuery(TokenContext.class);

		if (criteria != null)
		{
			configureCriterion(query, criteria);
		}

		Collection result = pc.runQuery(query);
		return wrapRegularContextIterator(result.iterator());
	}

	private void configureCriterion(final PersistenceQuery query, final TokenContextCriteria criteria)
	{
		if (criteria.getId() != null)
		{
			query.eq("id", criteria.getId());
		}

		if (criteria.getLifecycleState() != null)
		{
			query.eq("lifecycleState", criteria.getLifecycleState());
		}

		if (criteria.getModel() != null)
		{
			query.eq("executingModelQualifier", criteria.getModel().getQualifier().toString());
		}

		configureCriterionBase(query, criteria);
	}

	/**
	 * Wraps the given iterator with an iterator class that will refresh any context
	 * that is about to be executed in order to reflect the latest changes to the database.
	 * @param it Iterator to wrapped
	 * @return Wrapping iterator
	 */
	protected Iterator wrapRegularContextIterator(Iterator it)
	{
		return new RegularContextIterator(it);
	}

	private class RegularContextIterator extends WrappingIterator
	{
		/**
		 * Constructor.
		 *
		 * @param basis Contains the iterator we are based on
		 */
		public RegularContextIterator(Iterator basis)
		{
			super(basis);
		}

		/**
		 * Retrieves current object by querying the underlying iterator.
		 * @param basis The underlying iterator
		 * @return The current object or null if the end of the underlying iterator has been reached.
		 */
		protected Object retrieveCurrentObject(Iterator basis)
		{
			while (basis.hasNext())
			{
				TokenContext context = (TokenContext) basis.next();

				PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();

				try
				{
					LogUtil.debug(getClass(), "Refreshing fetched token. [{0}]", context);
					pc.refreshObject(context);
				}
				catch (PersistentObjectNotFoundException e)
				{
					// Object has been deleted meanwhile - continue with next one
					continue;
				}

				return context;
			}

			return null;
		}
	}

	/**
	 * Gets the token context objects that are ready to execute.
	 *
	 * @param maxResults Maximum number of result records or 0 for all
	 * @return An iterator of {@link TokenContext} objects.
	 * The objects will be sorted by their priority (ascending).
	 */
	public Iterator getExecutableContexts(int maxResults)
	{
		LogUtil.debug(getClass(), "Performing query for executable tokens.");
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();

		// Construct search search criterion for executable token contexts;
		// don't cache this, depends on current session.
		PersistenceQuery query = pc.createQuery(TokenContext.class);
		query.eq("lifecycleRequest", Integer.valueOf(LifecycleRequest.RESUME));
		query.neq("lifecycleState", Integer.valueOf(LifecycleState.SELECTED));
		query.addOrdering("priority");

		int max = maxResults;
		if (getIsolationLevel() == ISOLATION_LEVEL_SINGLE)
		{
			max = 1;
		}
		if (max > 0)
		{
			query.setMaxResults(max);
		}

		// TODO Fix 2 A 'select for update' might be advisable
		Collection result = pc.runQuery(query);
		return wrapExecutableContextIterator(result.iterator());
	}

	/**
	 * Wraps the given iterator with an iterator class that will aggressively refresh any context
	 * that is about to be executed in order to reflect the latest changes to the database.
	 * @param it Iterator to wrapped
	 * @return Wrapping iterator
	 */
	protected Iterator wrapExecutableContextIterator(Iterator it)
	{
		return new ExecutableContextIterator(it);
	}

	private class ExecutableContextIterator extends WrappingIterator
	{
		/**
		 * Constructor.
		 *
		 * @param basis Contains the iterator we are based on
		 */
		public ExecutableContextIterator(final Iterator basis)
		{
			super(basis);
		}

		/**
		 * Retrieves current object by querying the underlying iterator.
		 * @param basis The underlying iterator
		 * @return The current object or null if the end of the underlying iterator has been reached.
		 */
		protected Object retrieveCurrentObject(final Iterator basis)
		{
			while (basis.hasNext())
			{
				TokenContext context = (TokenContext) basis.next();

				PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();

				try
				{
					LogUtil.debug(getClass(), "Refreshing fetched token. [{0}]", context);
					pc.refreshObject(context);
				}
				catch (PersistentObjectNotFoundException e)
				{
					// Object has been deleted meanwhile - continue with next one
					continue;
				}

				if (getIsolationLevel() == ISOLATION_LEVEL_MERGE)
				{
					// This will execute a 'merge', so we can be quite sure that
					// the lifecycle request comparison is up-to-date.
					context = synchronizeContextWithPersistenceStore(context);
				}

				if (context.getLifecycleRequest() == LifecycleRequest.RESUME)
					return context;
			}
			return null;
		}
	}

	/**
	 * Gets the child contexts of the specified context.
	 *
	 * @param context Context
	 * @return An iterator of child contexts
	 */
	public Iterator getChildContexts(TokenContext context)
	{
		LogUtil.debug(getClass(), "Performing query for child tokens.");
		ArrayList children = new ArrayList();
		CollectionUtil.addAll(children, context.getChildContexts());
		return children.iterator();
	}

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
	public int changeContextState(int fromLifecycleState, int toLifecycleState, int toLifecycleRequest, String nodeId)
	{
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();

		String sql = "update OPENBPTOKENCONTEXT set TC_LIFECYCLE_STATE = " + toLifecycleState + ", TC_LIFECYCLE_REQUEST = " + toLifecycleRequest +
                " where TC_LIFECYCLE_STATE = " + fromLifecycleState;
		if (nodeId != null)
		{
			sql += " and TC_NODE_ID = '" + nodeId + "'";
		}

		return pc.executeUpdateOrDelete(sql);
	}

	//////////////////////////////////////////////////
	// @@ Workflow task management
	//////////////////////////////////////////////////

	/**
	 * Adds a workflow task to the service.
	 *
	 * @param workflowTask workflow task to add @return The saved object
	 */
	public WorkflowTask addWorkflowTask(final WorkflowTask workflowTask)
	{
		LogUtil.debug(getClass(), "Creating workflow task $0.", workflowTask);
		return saveWorkflowTask(workflowTask);
	}

	/**
	 * Save the changes to a workflow task.
	 *
	 * @param workflowTask workflow task to save @return The saved object
	 */
	public WorkflowTask saveWorkflowTask(final WorkflowTask workflowTask)
	{
		LogUtil.debug(getClass(), "Updating workflow task $0.", workflowTask);
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		return (WorkflowTask) pc.saveObject(workflowTask);
	}

	/**
	 * Removes a workflow task from the service.
	 *
	 * @param workflowTask workflow task to remove
	 */
	public void deleteWorkflowTask(final WorkflowTask workflowTask)
	{
		LogUtil.debug(getClass(), "Deleting workflow task $0.", workflowTask);
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		pc.deleteObject(workflowTask);
	}

	/**
	 * Returns an iterator of workflow tasks that match the given selection criteria.
	 *
	 * @param criteria Criteria to match @return An iterator of {@link WorkflowTask} objects
	 */
	public Iterator getworkflowTasks(final WorkflowTaskCriteria criteria)
	{
		LogUtil.debug(getClass(), "Performing workflow task query $0.", criteria);
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		PersistenceQuery query = pc.createQuery(WorkflowTask.class);

		if (criteria != null)
		{
			configureCriterion(query, criteria);
		}

		Collection result = pc.runQuery(query);

		return result.iterator();
	}

	private void configureCriterion(final PersistenceQuery query, final WorkflowTaskCriteria criteria)
	{
		if (criteria.getId() != null)
		{
			query.eq("id", criteria.getId());
		}

		if (criteria.getStatus() != null)
		{
			query.eq("status", criteria.getStatus());
		}

		if (criteria.getName() != null)
		{
			query.eq("name", criteria.getName());
		}

		if (criteria.getStepName() != null)
		{
			query.eq("stepName", criteria.getStepName());
		}

		if (criteria.getRoleId() != null)
		{
			query.eq("roleId", criteria.getRoleId());
		}

		if (criteria.getUserId() != null)
		{
			query.eq("userId", criteria.getUserId());
		}

		if (criteria.getTokenContext() != null)
		{
			query.eq("tokenContext", criteria.getTokenContext());
		}

		if (criteria.getModel() != null)
		{
			query.alias("tokenContext", "tc");
			query.eq("tc.executingModelQualifier", criteria.getModel().getQualifier().toString());
		}

		configureCriterionBase(query, criteria);
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	private void configureCriterionBase(final PersistenceQuery query, final CriteriaBase criteria)
	{
		for (Iterator it = criteria.getCustomCriteriaKeys(); it.hasNext();)
		{
			String key = (String) it.next();
			Object value = criteria.getCustomCriteriaValue(key);
			if (value != null)
			{
				query.eq(key, value);
			}
		}
	}

	protected TokenContext synchronizeContextWithPersistenceStore(TokenContext context)
	{
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();

		TokenContext newContext = (TokenContext) pc.merge(context);
		if (context != newContext)
		{
			// The persistence store returned a new context that was read from the database.
			// We need to copy the transient attributes to the database-bound context in order not to loose
			// application-/caller-related processing information.
			Map ra = context.getRuntimeAttributes();
			if (ra != null)
			{
				for (Iterator it = ra.entrySet().iterator(); it.hasNext();)
				{
					Map.Entry entry = (Map.Entry) it.next();
					newContext.setRuntimeAttribute((String) entry.getKey(), entry.getValue());
				}
			}

			context = newContext;
		}
		return context;
	}
}
