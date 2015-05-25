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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openbp.common.CollectionUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.util.CopyUtil;
import org.openbp.common.util.observer.EventObserverMgr;
import org.openbp.core.engine.EngineException;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.Param;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessVariable;
import org.openbp.core.model.item.process.RollbackDataBehavior;
import org.openbp.core.model.item.process.RollbackPositionBehavior;
import org.openbp.core.model.modelmgr.ModelMgr;
import org.openbp.server.context.CallStack;
import org.openbp.server.context.ProgressInfo;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextService;
import org.openbp.server.context.TokenContextUtil;
import org.openbp.server.context.TokenContextValue;
import org.openbp.server.context.serializer.PersistenceContextObjectSerializer;
import org.openbp.server.engine.script.ExpressionParser;
import org.openbp.server.engine.script.TokenContextToExpressionContextAdapter;
import org.openbp.server.persistence.PersistenceContextProvider;
import org.openbp.server.scheduler.SchedulerEngineEvent;

/**
 * Process util.
 *
 * @author Heiko Erhardt
 */
public class EngineUtil
{
	/**
	 * Private constructor prevents instantiation.
	 */
	private EngineUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Socket/param data transfer
	//////////////////////////////////////////////////

	/**
	 * Copies the data of a socket to another socket.
	 *
	 * @param srcSocket Source socket
	 * @param srcContext Source token context
	 * @param destSocket Destination socket
	 * @param destContext Destination token context
	 */
	public static void copySocketData(NodeSocket srcSocket, TokenContext srcContext, NodeSocket destSocket,
		TokenContext destContext)
	{
		// Iterate all parameters of the exit socket
		for (Iterator it = srcSocket.getParams(); it.hasNext();)
		{
			NodeParam srcParam = (NodeParam) it.next();

			// Copy the parameter value
			copyParamData(srcParam, srcContext, destSocket, destContext);
		}
	}

	/**
	 * Copies the data of a socket parameter to the matching parameter
	 * of another socket, if present.
	 *
	 * @param srcParam Source parameter
	 * @param srcContext Source token context
	 * @param destSocket Destination socket
	 * @param destContext Destination token context
	 */
	private static void copyParamData(NodeParam srcParam, TokenContext srcContext, NodeSocket destSocket,
		TokenContext destContext)
	{
		NodeParam destParam = destSocket.getParamByName(srcParam.getName());
		if (destParam != null)
		{
			Object value = TokenContextUtil.getParamValue(srcContext, srcParam);
			TokenContextUtil.setParamValue(destContext, destParam, value);
		}
	}

	/**
	 * Copies the parameters of the given entry socket to parameters
	 * of the same name to one or more other sockets of the node.
	 * This is useful for 'dragging along' parameters and is also required for
	 * form binding that 'extends' the values of a given entry bean.
	 *
	 * @param sourceSocket Source socket
	 * @param targetSocket Target socket or null (in this case, the parameters will be copied
	 * to all exit sockets of the node the target socket belongs to)
	 * @param context Token context
	 */
	static void copySocketParameters(NodeSocket sourceSocket, NodeSocket targetSocket, TokenContext context)
	{
		Node node = sourceSocket.getNode();

		// Iterate all parameters of the exit socket
		for (Iterator itParam = sourceSocket.getParams(); itParam.hasNext();)
		{
			Param sourceParam = (Param) itParam.next();
			String paramName = sourceParam.getName();

			if (targetSocket != null)
			{
				Param targetParam = targetSocket.getParamByName(paramName);
				if (targetParam == null)
					continue;

				Object value = TokenContextUtil.getParamValue(context, sourceParam);
				TokenContextUtil.setParamValue(context, targetParam, value);
			}
			else
			{
				boolean haveValue = false;
				Object value = null;

				for (Iterator itSocket = node.getSockets(); itSocket.hasNext();)
				{
					NodeSocket socket = (NodeSocket) itSocket.next();

					if (! socket.isExitSocket())
						continue;

					Param targetParam = socket.getParamByName(paramName);
					if (targetParam == null)
						continue;

					// We found a parameter of the same name
					if (! haveValue)
					{
						value = TokenContextUtil.getParamValue(context, sourceParam);
						haveValue = true;
					}

					// Add the value directly as target parameter value to the context
					TokenContextUtil.setParamValue(context, targetParam, value);
				}
			}
		}
	}

	/**
	 * Removes the parameter data of all sockets of the given node.
	 *
	 * @param node Node
	 * @param context Token context
	 */
	static void removeNodeData(Node node, TokenContext context)
	{
		for (Iterator it = node.getSockets(); it.hasNext();)
		{
			NodeSocket socket = (NodeSocket) it.next();

			EngineUtil.removeSocketData(socket, context);
		}
	}

	/**
	 * Iterates all parameters of a socket and removes the parameter values from the context
	 * as long as the given socket has not been pushed onto the call stack.
	 *
	 * @param socket Socket to operate on or null
	 * @param context Token context
	 */
	static void removeSocketData(NodeSocket socket, TokenContext context)
	{
		if (socket == null)
			return;

		CallStack callStack = context.getCallStack();
		if (callStack.containsSocketReference(socket))
			// This socket is still in the call stack, so we shouldn't remove its data
			return;

		// Iterate all parameters of the entry socket
		for (Iterator it = socket.getParams(); it.hasNext();)
		{
			Param param = (Param) it.next();

			TokenContextUtil.removeParamValue(context, param);
		}
	}

	/**
	 * Updates a reference to a socket if the socket is owned by a process that
	 * has been updated.
	 *
	 * @param socket Socket
	 * @param process Updated process
	 * @return The updated socket or null if the socket does not exist any more in the
	 * updated process
	 */
	public static NodeSocket updateSocketReference(NodeSocket socket, ProcessItem process)
	{
		String nodeName = socket.getNode().getName();
		String socketName = socket.getName();

		Node newNode = process.getNodeByName(nodeName);
		if (newNode != null)
		{
			NodeSocket newSocket = newNode.getSocketByName(socketName);
			if (newSocket != null)
				return newSocket;
		}

		return null;
	}

	/**
	 * Creates all process variables of the given process.
	 *
	 * @param process Process that defines the process variables to create
	 * @param context Token context
	 */
	public static void createProcessVariables(ProcessItem process, TokenContext context)
	{
		// Iterate all process variables of the process
		List variables = process.getProcessVariableList();
		if (variables != null)
		{
			int n = variables.size();
			for (int i = 0; i < n; ++i)
			{
				ProcessVariable param = (ProcessVariable) variables.get(i);

				boolean isPersistent = param.isPersistentVariable();
				context.createProcessVariable(param.getName(), isPersistent);
			}
		}
	}

	/**
	 * Clears all process variables defined by the given scope from the context.
	 *
	 * @param process Process that defines the process variables to clear
	 * @param scope {@link ProcessVariable#SCOPE_PROCESS} Clears all variables having the scope 'process'
	 * @param context Token context
	 */
	public static void clearProcessVariables(ProcessItem process, int scope, TokenContext context)
	{
		// Iterate all process variables of the process
		List variables = process.getProcessVariableList();
		if (variables != null)
		{
			int n = variables.size();
			for (int i = 0; i < n; ++i)
			{
				ProcessVariable param = (ProcessVariable) variables.get(i);

				int paramScope = param.getScope();
				if (scope == ProcessVariable.SCOPE_PROCESS && paramScope == ProcessVariable.SCOPE_PROCESS)
				{
					// We found a process-local parameter, remove it from the token context
					context.removeProcessVariableValue(param.getName());
				}
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Expressions
	//////////////////////////////////////////////////

	/**
	 * Creates a new expression parser instance.
	 *
	 * @param context Context to refer to
	 * @param engine Engine
	 * @return The new instance
	 */
	public static ExpressionParser createExpressionParser(TokenContext context, Engine engine)
	{
		ExpressionParser ep = new ExpressionParser(new TokenContextToExpressionContextAdapter(context));
		if (engine != null)
		{
			ep.setPersistenceContextProvider(engine.getPersistenceContextProvider());
		}
		return ep;
	}

	/**
	 * This method identifies a NodeSocket from the passed socket qualifier.
	 *
	 * @param qualifier The path of the socket. Must contain an absolute model specification.
	 * @param modelMgr Model manager that can be used to resolve process references
	 * @return The node socket found or null
	 */
	public static NodeSocket determineNodeSocketFromQualifier(ModelQualifier qualifier, ModelMgr modelMgr)
	{
		if (qualifier != null)
		{
			qualifier.setItemType(ItemTypes.PROCESS);

			try
			{
				ProcessItem process = (ProcessItem) modelMgr.getItemByQualifier(qualifier, true);

				String objectPath = qualifier.getObjectPath();
				if (objectPath != null)
				{
					String nodeName = null;
					String socketName = null;

					int i = objectPath.indexOf(ModelQualifier.OBJECT_DELIMITER_CHAR);
					if (i >= 0)
					{
						nodeName = objectPath.substring(0, i);
						socketName = objectPath.substring(i + 1);

						Node node = process.getNodeByName(nodeName);

						return node != null ? node.getSocketByName(socketName) : null;
					}
				}
			}
			catch (ModelException e)
			{
				// Fall thru (same handling as no socket found).
			}
		}
		return null;
	}

	public static TokenContext rollbackAndContinue(TokenContext contextArg, int rollbackDataBehavior, int rollbackPositionBehavior, Engine engine)
	{
		TokenContext memContext = contextArg;
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
			String msg = LogUtil.error(EngineUtil.class, "Error cloning token. [{0}]", memContext, e);
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
			contextService.commit();
		}

		// Make the rolled-back context the current one
		return dbContext;
	}

	private static Map copyProcessVariables(TokenContext context, PersistenceContextProvider pcp)
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
						String msg = LogUtil.error(EngineUtil.class, "Error cloning process variable $0 (value: $1). [{2}]", varName, value, context, e);
						throw new EngineException("NoDefaultExitSocket", msg);
					}
				}
			}

			map.put(varName, value);
		}

		return map;
	}

	/**
	 * Internal helper method that adds the event types supported by the engine to the observer manager.
	 *
	 * @param observerMgr Observer mgr
	 */
	public static void prepareEngineObserverMgr(EventObserverMgr observerMgr)
	{
		ArrayList eventTypes = new ArrayList();
		CollectionUtil.addAll(eventTypes, CollectionUtil.iterator(EngineEvent.getSupportedEventTypes()));
		CollectionUtil.addAll(eventTypes, CollectionUtil.iterator(EngineTraceEvent.getSupportedEventTypes()));
		CollectionUtil.addAll(eventTypes, CollectionUtil.iterator(CancelableEngineEvent.getSupportedEventTypes()));
		CollectionUtil.addAll(eventTypes, CollectionUtil.iterator(EngineExceptionHandlerEvent.getSupportedEventTypes()));
		CollectionUtil.addAll(eventTypes, CollectionUtil.iterator(SchedulerEngineEvent.getSupportedEventTypes()));
		observerMgr.setSupportedEventTypes(CollectionUtil.toStringArray(eventTypes));
	}
}
