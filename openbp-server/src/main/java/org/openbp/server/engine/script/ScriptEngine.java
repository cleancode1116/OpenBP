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
package org.openbp.server.engine.script;

import org.openbp.core.OpenBPException;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.server.handler.HandlerContext;

/**
 * A script engine executes a script that may access an expression context.
 *
 * Currently, there is one implementation of the script engine that uses the bean shell
 * in order execute scripted Java code.
 *
 * @author Heiko Erhardt
 */
public interface ScriptEngine
{
	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the expression context.
	 * @return The context or null if the expression does not operate on a context
	 */
	public ExpressionContext getContext();

	/**
	 * Sets the expression context.
	 * @param context Context or null if the expression does not operate on a context
	 */
	public void setContext(ExpressionContext context);

	//////////////////////////////////////////////////
	// @@ Script execution
	//////////////////////////////////////////////////

	/**
	 * Prepares the script engine for execution of a handler script.
	 * Sets the scope of the script's namespace will be set according to the context of the handler.
	 *
	 * @param hc Handler context
	 */
	public void prepareHandlerExecution(HandlerContext hc);

	/**
	 * Cleans up the script engine after execution of a handler script.
	 * The method will retrieve all parameters of the sockets of a node from the script namespace
	 * and transfer them back to the expression context.
	 * The method also resets the context prefix.
	 *
	 * @param hc Handler context
	 */
	public void finishHandlerExecution(HandlerContext hc);

	/**
	 * Prepares the script engine for execution of a node socket-related script.
	 * An example for this is the expression of a decision node.
	 * The method adds all parameters of the socket to the script namespace.
	 * The method also sets the context prefix to "Node.Socket", so a node parameter can be
	 * accessed using the "." Notation. E. g., the parameter "Company" of the socket
	 * may be simply accessed by
	 * @code 3
	 *		Company
	 * @code
	 * notation.<br>
	 * If the parameter is a bean, the members of the bean may be accessed
	 * in a similar way, e. g.<br>
	 * @code 3
	 *		Company.CompanyId = "123";
	 * @code
	 *
	 * @param socket Node parameter in which's context the script shall be executed
	 */
	public void prepareNodeSocketExecution(NodeSocket socket);

	/**
	 * Cleans up the script engine after execution of a node socket-related script.
	 * An example for this is the expression of a decision node.
	 * The method will retrieve all parameters of the socket from the script namespace
	 * and transfer them back to the expression context.
	 * The method also resets the context prefix.
	 *
	 * @param socket Node parameter in which's context the script shall be executed
	 */
	public void finishNodeSocketExecution(NodeSocket socket);

	/**
	 * Prepares the script engine for execution of a node parameter-related script.
	 * The method adds all parameters of the socket to the script namespace.
	 * The method also sets the context prefix to "Node.Socket", so a node parameter can be
	 * accessed using the "." Notation. E. g., the parameter "Company" of the socket
	 * may be simply accessed by
	 * @code 3
	 *		Company
	 * @code
	 * notation.<br>
	 * If the parameter is a bean, the members of the bean may be accessed
	 * in a similar way, e. g.<br>
	 * @code 3
	 *		Company.CompanyId = "123";
	 * @code
	 *
	 * @param param Node parameter in which's context the script shall be executed
	 */
	public void prepareNodeParamExecution(NodeParam param);

	/**
	 * Cleans up the script engine after execution of a node parameter-related script.
	 * The method will retrieve all parameters of the socket from the script namespace
	 * and transfer them back to the expression context.
	 * The method also resets the context prefix.
	 *
	 * @param param Node parameter in which's context the script shall be executed
	 */
	public void finishNodeParamExecution(NodeParam param);

	/**
	 * Exeuctes a script.
	 * Uses the Bean Shell interpreter to perform the evaluation.
	 *
	 * @param script Script to execute
	 * @param fileType Textual description of the type of file that contains the script (see 'fileName' parameter)
	 * @param fileName Name of the object containing the script (e. g. a file name or a qualified model object name)
	 * @return The value of the last expression in the script or null if the expression value itself
	 * is null or the script did not return a valid value
	 * @throws OpenBPException On syntactical or semantical errors
	 */
	public Object executeScript(String script, String fileType, String fileName);
}
