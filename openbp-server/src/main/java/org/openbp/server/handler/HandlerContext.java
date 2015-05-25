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
package org.openbp.server.handler;

import java.util.List;

import org.openbp.core.OpenBPException;
import org.openbp.core.handler.HandlerTypes;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.Param;
import org.openbp.server.context.LifecycleRequest;
import org.openbp.server.context.LifecycleState;
import org.openbp.server.context.ProgressInfo;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextUtil;
import org.openbp.server.engine.Engine;
import org.openbp.server.engine.EngineContext;

/**
 * Execution context of a handler.
 *
 * Contains all variables that a handler might need for execution.
 *
 * @author Heiko Erhardt
 */
public class HandlerContext
	implements EngineContext
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Type of the handler execution */
	private final String handlerType;

	/** Engine */
	private Engine engine;

	/** Token context */
	private TokenContext tokenContext;

	/** Current node socket */
	private NodeSocket currentSocket;

	/** Next node socket */
	private NodeSocket nextSocket;

	/** Old value of next node socket */
	private NodeSocket oldNextSocket;

	/** Flag that indicates cancellation of the default action */
	private boolean canceled;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param engine Engine
	 * @param handlerType Type of the handler execution (see the constants of the {@link HandlerTypes} class)
	 */
	public HandlerContext(Engine engine, String handlerType)
	{
		this.engine = engine;
		this.handlerType = handlerType;
	}

	/**
	 * Default constructor.
	 *
	 * @param engine Engine
	 * @param handlerType Type of the handler execution (see the constants of the {@link HandlerTypes} class)
	 * @param tokenContext Token context
	 */
	public HandlerContext(Engine engine, String handlerType, TokenContext tokenContext)
	{
		this(engine, handlerType);

		setTokenContext(tokenContext);

		currentSocket = tokenContext.getCurrentSocket();
		if (currentSocket != null)
		{
			nextSocket = currentSocket.getNode().getDefaultExitSocket();
			oldNextSocket = nextSocket;
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the type of the handler execution (see the constants of the {@link HandlerTypes} class).
	 * @nowarn
	 */
	public String getHandlerType()
	{
		return handlerType;
	}

	/**
	 * Gets the token context.
	 * May be null for global event handlers.
	 * @nowarn
	 */
	public TokenContext getTokenContext()
	{
		return tokenContext;
	}

	/**
	 * Sets the token context.
	 * For internal use only.
	 * @nowarn
	 */
	public void setTokenContext(TokenContext tokenContext)
	{
		this.tokenContext = tokenContext;
	}

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
	 * Gets the progress info object of the token context.
	 * @nowarn
	 */
	public ProgressInfo getProgressInfo()
	{
		return getTokenContext() != null ? getTokenContext().getProgressInfo() : null;
	}

	/**
	 * Gets the current node socket.
	 * May be null for some events.
	 * @nowarn
	 */
	public NodeSocket getCurrentSocket()
	{
		return currentSocket;
	}

	/**
	 * Sets the current node socket.
	 * @nowarn
	 */
	public void setCurrentSocket(NodeSocket currentSocket)
	{
		this.currentSocket = currentSocket;
	}

	/**
	 * Gets the next node socket.
	 * In case of activity handlers, this will be set to the default exit socket of the activity node.
	 * @nowarn
	 */
	public NodeSocket getNextSocket()
	{
		return nextSocket;
	}

	/**
	 * Sets the next node socket.
	 * In case of activity handlers, this will be set to the default exit socket of the activity node.
	 * In order to choose a different output socket, use this method.
	 * @nowarn
	 */
	public void setNextSocket(NodeSocket nextSocket)
	{
		if (getCurrentSocket() == null)
			throw new IllegalArgumentException("setNextSocket cannt be used in this handler context.");
		this.nextSocket = nextSocket;
	}

	/**
	 * Sets the next node socket.
	 * In case of activity handlers, this will be set to the default exit socket of the activity node.
	 * In order to choose a different output socket, use this method.
	 * @nowarn
	 * @throws OpenBPException On error
	 */
	public void setNextSocket(String socketName)
	{
		if (tokenContext == null)
			throw new IllegalArgumentException("setNextSocket cannot be used in this handler context.");

		// Return value of activity execution is the name of the exit socket
		// Determine the corresponding exit socket using the usual search strategy
		this.nextSocket = getEngine().resolveSocketRef(socketName, getCurrentSocket(), tokenContext, true);
	}

	/**
	 * Gets the lifecycle state.
	 * @return See the constants of the {@link LifecycleState} class
	 */
	public int getLifecycleState()
	{
		return tokenContext.getLifecycleState();
	}

	/**
	 * Gets the lifecycle request.
	 *
	 * @return See the constants of the {@link LifecycleRequest} class
	 */
	public int getLifecycleRequest()
	{
		return tokenContext.getLifecycleRequest();
	}

	/**
	 * Sets the lifecycle request.
	 *
	 * @param lifecycleRequest See the constants of the {@link LifecycleRequest} class
	 */
	public void setLifecycleRequest(int lifecycleRequest)
	{
		tokenContext.setLifecycleRequest(lifecycleRequest);
	}

	/**
	 * Gets the flag that indicates cancellation of the default action.
	 * @nowarn
	 */
	public boolean isCanceled()
	{
		return canceled;
	}

	/**
	 * Sets the flag that indicates cancellation of the default action.
	 * @nowarn
	 */
	public void setCanceled(boolean canceled)
	{
		this.canceled = canceled;
	}

	//////////////////////////////////////////////////
	// @@ Parameter access
	//////////////////////////////////////////////////

	/**
	 * Retrieves the specified handler input parameter.
	 *
	 * @param paramName Name of the input parameter (must be a parameter of the entry socket of the activity for activity handlers)
	 * @return The parameter value or null if no such parameter exists
	 */
	public Object getParam(String paramName)
	{
		// TODO Feature 3 Add parameter support for non-activity handlers
		return TokenContextUtil.getParamValue(tokenContext, currentSocket, paramName);
	}

	/**
	 * Sets the specified handler output parameter.
	 *
	 * @param paramName Name of the output parameter (must be a parameter of the default exit socket of the activity
	 * or the socket specified by {@link #setNextSocket(String)} for activity handlers). Make sure to call {@link #setNextSocket(String)} or
	 * {@link #chooseExitSocket} before calling this method.
	 * @param value Param value
	 */
	public void setResult(String paramName, Object value)
	{
		Param param = nextSocket.getParamByName(paramName); 
		if (param != null)
		{
			// If the exit socket contains a 'WorkflowTask' parameter, set it
			TokenContextUtil.setParamValue(tokenContext, param, value);
		}
	}

	/**
	 * Chooses the given exit socket to proceed.
	 *
	 * @param socketName Socket name
	 */
	public void chooseExitSocket(String socketName)
	{
		// Return value of activity execution is the name of the exit socket
		// Determine the corresponding exit socket using the usual search strategy
		nextSocket = getEngine().resolveSocketRef(socketName, currentSocket, tokenContext, true);
	}

	/**
	 * Used to determine if a handler has changed the next socket.
	 * @nowarn
	 */
	public boolean hasNextSocketChanged()
	{
		return nextSocket != oldNextSocket;
	}

	/**
	 * Checks if the specified parameter is defined at the current socket.
	 * @param name Parameter name
	 * @nowarn
	 */
	public boolean isParamPresent(String name)
	{
		if (currentSocket != null && currentSocket.getParamByName(name) != null)
			return true;
		return false;
	}

	/**
	 * Gets the names of the defined parameters of the current socket.
	 *
	 * @return The parameters or null if there is no current socket
	 */
	public String[] getParamNames()
	{
		String[] ret = null;
		if (currentSocket != null)
		{
			List list = currentSocket.getParamList();
			if (list != null)
			{
				int n = list.size();
				ret = new String[n];

				for (int i = 0; i < n; ++i)
				{
					NodeParam param = (NodeParam) list.get(i);
					ret[i] = param.getName();
				}
			}
		}
		return ret;
	}

	/**
	 * Gets the path of the current node.
	 * Useful for logging purposes.
	 * @nowarn
	 */
	public String getNodePath()
	{
		return currentSocket.getNode().getQualifier().toString();
	}

	/**
	 * Gets the name of the current node.
	 * Useful for logging purposes.
	 * @nowarn
	 */
	public String getNodeName()
	{
		return currentSocket.getNode().getName();
	}

	/**
	 * Gets the display name of the current node.
	 * Useful for logging purposes.
	 * @nowarn
	 */
	public String getNodeDisplayName()
	{
		return currentSocket.getNode().getDisplayName();
	}
}
