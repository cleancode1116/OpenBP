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
package org.openbp.core.model;

import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.NodeSocket;

/**
 * Utility methods for building token context parameter names.
 *
 * @author Heiko Erhardt
 */
public final class ContextNameUtil
{
	/**
	 * Private constructor prevents instantiation.
	 */
	private ContextNameUtil()
	{
	}

	/**
	 * Gets the context name of the specified socket parameter.
	 *
	 * @param socket Socket
	 * @param paramName Param name
	 * @return The context name
	 */
	public static String getContextName(NodeSocket socket, String paramName)
	{
		return socket.getContextName() + ModelQualifier.OBJECT_DELIMITER_CHAR + paramName;
	}

	/**
	 * Gets the context name of the specified socket parameter.
	 *
	 * @param node Node
	 * @param socketName Socket name
	 * @param paramName Param name
	 * @return The context name
	 */
	public static String getContextName(Node node, String socketName, String paramName)
	{
		return node.getContextName() + ModelQualifier.OBJECT_DELIMITER_CHAR + socketName + ModelQualifier.OBJECT_DELIMITER_CHAR + paramName;
	}

	/**
	 * Constructs the name used to access the given node in the parameter table of a token context.
	 *
	 * @param node Node to access
	 * @return The parameter's context name ("node")
	 */
	public static String constructContextName(Node node)
	{
		return constructContextName(node, null, null);
	}

	/**
	 * Builds the qualified name of a node socket.
	 * Constructs the name used to access the given socket in the parameter table of a token context.
	 *
	 * @param socket Socket to access
	 * @return The socket's context name ("node.socket")
	 */
	public static String constructContextName(NodeSocket socket)
	{
		return constructContextName(socket.getNode(), socket.getName(), null);
	}

	/**
	 * Constructs the name used to access the given parameter in the parameter table of a token context.
	 *
	 * @param param Parameter to access
	 * @return The parameter's context name ("node.socket.param")
	 */
	public static String constructContextName(NodeParam param)
	{
		NodeSocket socket = param.getSocket();
		return constructContextName(socket.getNode(), socket.getName(), param.getName());
	}

	/**
	 * Constructs the name used to access the given parameter in the parameter table of a token context.
	 *
	 * @param node Node that owns parameter
	 * @param socketName Name of the socket that holds the parameter
	 * @param paramName Name of the parameter
	 * @return The parameter's context name ("node.socket.param")
	 */
	public static String constructContextName(Node node, String socketName, String paramName)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(node.getName());
		if (socketName != null)
		{
			sb.append(ModelQualifier.OBJECT_DELIMITER_CHAR);
			sb.append(socketName);
			if (paramName != null)
			{
				sb.append(ModelQualifier.OBJECT_DELIMITER_CHAR);
				sb.append(paramName);
			}
		}
		return sb.toString();
	}
}
