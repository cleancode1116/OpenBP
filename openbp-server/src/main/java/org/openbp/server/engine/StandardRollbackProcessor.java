/*
 *   Copyright 2009 skynamics AG
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openbp.common.generic.Copyable;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.util.CopyUtil;
import org.openbp.core.engine.EngineException;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.RollbackDataBehavior;
import org.openbp.core.model.item.process.RollbackPositionBehavior;
import org.openbp.server.context.CallStack;
import org.openbp.server.context.ProgressInfo;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextService;
import org.openbp.server.context.TokenContextUtil;
import org.openbp.server.context.TokenContextValue;
import org.openbp.server.context.serializer.PersistenceContextObjectSerializer;
import org.openbp.server.persistence.PersistenceContextProvider;

/**
 * Configurable standard implementation of the rollback behavior.
 *
 * @author Heiko Erhardt
 */
public class StandardRollbackProcessor
{
	/** Rollback data behavior */
	private int rollbackDataBehavior = RollbackDataBehavior.UPDATE_VARIABLES;

	/** Rollback position behavior */
	private int rollbackPositionBehavior = RollbackPositionBehavior.MAINTAIN_POSITION;

	/** Commit after update */
	private boolean commitTokenContextChangesEnabled = true;

	/**
	 * Default constructor.
	 */
	public StandardRollbackProcessor()
	{
	}

	/**
	 * Performs a rollback of the token context that is held by the given engine context.
	 * Use this method to perform rollback handling within an activity handler.
	 *
	 * @param ec The engine context to run on
	 */
	public void performRollback(EngineContext ec)
	{
		Engine engine = ec.getEngine();
		TokenContext memContext = ec.getTokenContext();
		Object contextId = memContext.getId();

		// Save variable values before we perform the rollback
		NodeSocket memCurrentSocket = memContext.getCurrentSocket();
		int memPriority = memContext.getPriority();
		String memQueueType = memContext.getQueueType();
		CallStack memCallStack = null;
		ProgressInfo memProgressInfo = null;
		Map memProcessVariables = null;
		try
		{
			if (rollbackPositionBehavior == RollbackPositionBehavior.MAINTAIN_POSITION)
			{
				memCallStack = (CallStack) CopyUtil.copyObject(memContext.getCallStack(), Copyable.COPY_DEEP, null);
				memProgressInfo = (ProgressInfo) CopyUtil.copyObject(memContext.getProgressInfo(), Copyable.COPY_DEEP, null);
			}
		}
		catch (CloneNotSupportedException e)
		{
			String msg = LogUtil.error(getClass(), "Error cloning token. [{0}]", memContext, e);
			throw new EngineException("NoDefaultExitSocket", msg);
		}
		if (rollbackDataBehavior == RollbackDataBehavior.UPDATE_VARIABLES || rollbackDataBehavior == RollbackDataBehavior.ADD_VARIABLES)
		{
			memProcessVariables = copyProcessVariables(memContext, engine.getPersistenceContextProvider());
		}

		// Perform transaction rollback and get rid of the rollback-invalid persistence context
		TokenContextService contextService = engine.getTokenContextService();
		contextService.evictContext(memContext);
		contextService.rollback();

		// Retrieve the current version of the context
		contextService.begin();
		TokenContext dbContext = contextService.getContextById(contextId);
		boolean updateContext = false;

		if (rollbackPositionBehavior == RollbackPositionBehavior.MAINTAIN_POSITION)
		{
			// Maintain the current position, so update the DB context from the memory context.
			dbContext.setCurrentSocket (memCurrentSocket);
			dbContext.setCallStack (memCallStack);
			dbContext.setPriority (memPriority);
			dbContext.setQueueType (memQueueType);
			dbContext.setProgressInfo (memProgressInfo);
			updateContext = true;
		}

		if (memProcessVariables != null)
		{
			for (Iterator it = memProcessVariables.entrySet().iterator(); it.hasNext();)
			{
				Map.Entry entry = (Map.Entry) it.next();
				String varName = (String) entry.getKey();
				Object value = entry.getValue();
				value = PersistenceContextObjectSerializer.resolveSerializableObjectReference(value, dbContext, varName, engine.getPersistenceContextProvider());

				if (rollbackDataBehavior == RollbackDataBehavior.UPDATE_VARIABLES)
				{
					// Update the DB context with the variable value of the memory context
					dbContext.setProcessVariableValue(varName, value);
					updateContext = true;
				}
				else
				{
					// Add new variables of the memory context to the DB context
					if (dbContext.getProcessVariableValue(varName) == null)
					{
						if (value != null)
						{
							dbContext.setProcessVariableValue(varName, value);
							updateContext = true;
						}
					}
				}
			}
		}

		if (updateContext)
		{
			contextService.saveContext(dbContext);
			if (isCommitTokenContextChangesEnabled())
			{
				contextService.commit();
			}
		}

		// Make the rolled-back context the current one
		ec.setTokenContext(dbContext);
	}

	private Map copyProcessVariables(TokenContext context, PersistenceContextProvider pcp)
	{
		HashMap map = new HashMap();

		for (Iterator it = context.getParamValues().entrySet().iterator(); it.hasNext();)
		{
			Map.Entry entry = (Map.Entry) it.next();
			String varName = (String) entry.getKey();
			if (! TokenContextUtil.isProcessVariableIdentifier(varName))
			{
				// Not a process variable
				continue;
			}
			varName = varName.substring(1);
			TokenContextValue tcv = (TokenContextValue) entry.getValue();
			Object value = tcv.getValue();

			if (value != null)
			{
				if (PersistenceContextObjectSerializer.isSerializableObject(value, pcp))
				{
					value = PersistenceContextObjectSerializer.createSerializableObjectReference(value, context, varName, pcp);
				}
				else
				{
					try
					{
						value = CopyUtil.copyObject(value, Copyable.COPY_DEEP, null);
					}
					catch (CloneNotSupportedException e)
					{
						String msg = LogUtil.error(getClass(), "Error cloning process variable $0 (value: $1). [{2}]", varName, value, context, e);
						throw new EngineException("NoDefaultExitSocket", msg);
					}
				}
			}

			map.put(varName, value);
		}

		return map;
	}

	/**
	 * Gets the rollback data behavior.
	 * @return {@link RollbackDataBehavior#UPDATE_VARIABLES}/{@link RollbackDataBehavior#ADD_VARIABLES}/{@link RollbackDataBehavior#RESTORE_VARIABLES}
	 */
	public int getRollbackDataBehavior()
	{
		return rollbackDataBehavior;
	}

	/**
	 * Sets the rollback data behavior.
	 * @param rollbackDataBehavior {@link RollbackDataBehavior#UPDATE_VARIABLES}/{@link RollbackDataBehavior#ADD_VARIABLES}/{@link RollbackDataBehavior#RESTORE_VARIABLES}
	 */
	public void setRollbackDataBehavior(int rollbackDataBehavior)
	{
		this.rollbackDataBehavior = rollbackDataBehavior;
	}

	/**
	 * Gets the rollback position behavior.
	 * @return {@link RollbackPositionBehavior#MAINTAIN_POSITION}/{@link RollbackPositionBehavior#RESTORE_POSITION}
	 */
	public int getRollbackPositionBehavior()
	{
		return rollbackPositionBehavior;
	}

	/**
	 * Sets the rollback position behavior.
	 * @param rollbackPositionBehavior {@link RollbackPositionBehavior#MAINTAIN_POSITION}/{@link RollbackPositionBehavior#RESTORE_POSITION}
	 */
	public void setRollbackPositionBehavior(int rollbackPositionBehavior)
	{
		this.rollbackPositionBehavior = rollbackPositionBehavior;
	}

	/**
	 * Gets the flag if the processor should issue a commit after changes to the token context.
	 * @nowarn
	 */
	public boolean isCommitTokenContextChangesEnabled()
	{
		return commitTokenContextChangesEnabled;
	}

	/**
	 * Sets the flag if the processor should issue a commit after changes to the token context.
	 * @nowarn
	 */
	public void setCommitTokenContextChangesEnabled(boolean commitTokenContextChangesEnabled)
	{
		this.commitTokenContextChangesEnabled = commitTokenContextChangesEnabled;
	}
}
