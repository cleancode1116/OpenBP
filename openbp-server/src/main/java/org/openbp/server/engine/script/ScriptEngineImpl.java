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

import java.io.PrintStream;
import java.io.StringReader;
import java.util.Iterator;

import org.openbp.common.logger.LogLevel;
import org.openbp.common.logger.LogUtil;
import org.openbp.core.OpenBPException;
import org.openbp.core.engine.EngineException;
import org.openbp.core.model.Model;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.Param;
import org.openbp.server.context.TokenContext;
import org.openbp.server.handler.HandlerContext;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.NameSpace;
import bsh.Primitive;
import bsh.TargetError;

/**
 * Implementation of the script engine for bean shell scripts.
 *
 * For a description of the bean shell, see <a href="http://www.beanshell.org">www.beanshell.org</a>.
 *
 * @author Heiko Erhardt
 */
public class ScriptEngineImpl
	implements ScriptEngine
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Name of the entry socket for node executions */
	public static final String ENTRY = "entry";

	/** Name of the exit socket for node executions */
	public static final String EXIT = "exit";

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Expression context */
	private ExpressionContext context;

	/** Bean shell interpreter */
	private Interpreter interpreter;

	/** Name space used for expression context access from within scripts */
	private ScriptNameSpace scriptNameSpace;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ScriptEngineImpl()
	{
		Interpreter.DEBUG = LogUtil.isDebugEnabled(getClass());
		Interpreter.TRACE = LogUtil.isTraceEnabled(getClass());

		PrintStream infoStream = new PrintStream(new ScriptEngineOutputStream(LogLevel.TRACE));
		PrintStream debugStream = new PrintStream(new ScriptEngineOutputStream(LogLevel.DEBUG));
		interpreter = new Interpreter(null, infoStream, debugStream, true);

		NameSpace globalNameSpace = interpreter.getNameSpace();
		scriptNameSpace = new ScriptNameSpace(this, globalNameSpace);
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the expression context.
	 * @return The context or null if the expression does not operate on a context
	 */
	public ExpressionContext getContext()
	{
		return context;
	}

	/**
	 * Sets the expression context.
	 * @param context Context or null if the expression does not operate on a context
	 */
	public void setContext(ExpressionContext context)
	{
		this.context = context;

		// We should use the model's class loader also for classes used in bean shell scripts
		if (context instanceof TokenContext)
		{
			Model executingModel = ((TokenContext) context).getExecutingModel();
			if (executingModel != null)
			{
				ClassLoader classLoader = executingModel.getClassLoader();
				if (classLoader != null)
				{
					interpreter.setClassLoader(classLoader);
				}
			}
		}
		else
		{
			interpreter.setClassLoader(null);
		}
	}

	//////////////////////////////////////////////////
	// @@ Script execution
	//////////////////////////////////////////////////

	/**
	 * Prepares the script engine for execution of a node parameter-related script.
	 * Copies the parameter data of the socket that owns the parameter from the expression context to the script namespace.
	 *
	 * @param param Node parameter in which's context the script shall be executed
	 */
	public void prepareNodeParamExecution(NodeParam param)
	{
		if (Interpreter.TRACE)
		{
			LogUtil.trace(getClass(), "Executing script of parameter $0.", param.getQualifier());
		}

		prepareNodeSocketExecution(param.getSocket());
	}

	/**
	 * Cleans up the script engine after execution of a node parameter-related script.
	 * The method will retrieve all parameters of the socket from the script namespace
	 * and transfer them back to the expression context.
	 *
	 * @param param Node parameter in which's context the script shall be executed
	 */
	public void finishNodeParamExecution(NodeParam param)
	{
		finishNodeSocketExecution(param.getSocket());
	}

	/**
	 * Prepares the script engine for execution of a handler script.
	 * Copies the parameter data of the current entry socket from the expression context to the script namespace.
	 *
	 * @param hc Handler context
	 */
	public void prepareHandlerExecution(HandlerContext hc)
	{
		NodeSocket socket = hc.getCurrentSocket();
		if (Interpreter.TRACE)
		{
			LogUtil.trace(getClass(), "Executing script of node $0.", socket.getNode().getQualifier());
		}

		prepareNodeSocketExecution(socket);

		// Provide the entry name
		setScriptVariable(ENTRY, socket.getName());

		// Default the exit name to null; need this to have it in global scope
		setScriptVariable(EXIT, null);
	}

	/**
	 * Cleans up the script engine after execution of a handler script.
	 * The method will retrieve all parameters of the current socket from the script namespace
	 * and transfer them back to the expression context.
	 *
	 * @param hc Handler context
	 */
	public void finishHandlerExecution(HandlerContext hc)
	{
		// Get the exit name
		Object ret = getScriptVariable(EXIT);
		if (ret instanceof String)
		{
			String nextSocketName = (String) ret;
			hc.setNextSocket(nextSocketName);
		}

		finishNodeSocketExecution(hc.getNextSocket());

		removeScriptVariable(ENTRY);
		removeScriptVariable(EXIT);
	}

	/**
	 * Prepares the script engine for execution of a node socket-related script.
	 * An example for this is the expression of a decision node.
	 * The method adds all parameters of the socket to the script namespace.
	 * If the parameter is a bean, the members of the bean may be accessed using the dot notation, e. g.<br>
	 * @code 3
	 *		Company.CompanyId = "123";
	 * @code
	 *
	 * @param socket Node parameter in which's context the script shall be executed
	 */
	public void prepareNodeSocketExecution(NodeSocket socket)
	{
		// Add all parameter values to the script namespace
		for (Iterator itParam = socket.getParams(); itParam.hasNext();)
		{
			Param param = (Param) itParam.next();

			String name = param.getName();
			String contextName = param.getContextName();
			Object value = context.getObject(contextName);

			setScriptVariable(name, value);
		}
	}

	/**
	 * Cleans up the script engine after execution of a node socket-related script.
	 * An example for this is the expression of a decision node.
	 * The method will retrieve all parameters of the socket from the script namespace
	 * and transfer them back to the expression context.
	 *
	 * @param socket Node parameter in which's context the script shall be executed
	 */
	public void finishNodeSocketExecution(NodeSocket socket)
	{
		for (Iterator itParam = socket.getParams(); itParam.hasNext();)
		{
			Param param = (Param) itParam.next();

			String name = param.getName();
			String contextName = param.getContextName();
			Object value = getScriptVariable(name);
			context.setObject(contextName, value);

			// Remove the parameter from the script namespace
			removeScriptVariable(name);
		}
	}

	/**
	 * Executes a script.
	 * Uses the Bean Shell interpreter to perform the evaluation.
	 *
	 * @param script Script to execute
	 * @param fileType Textual description of the type of file that contains the script (see 'fileName' parameter)
	 * or null for regular file-based scripts
	 * @param fileName Name of the object containing the script (e. g. a file name or a qualified model object name)
	 * @return The value of the last expression in the script or null if the expression value itself
	 * is null or the script did not return a valid value
	 * @throws OpenBPException On syntactical or semantical errors
	 */
	public Object executeScript(String script, String fileType, String fileName)
	{
		script = script.trim();
		if (script.length() > 0)
		{
			char c = script.charAt(script.length() - 1);
			if (c != ';' && c != '}')
			{
				script += ";";
			}
		}

		Throwable error = null;
		String errMsg;

		try
		{
			StringReader reader = new StringReader(script);
			Object value = interpreter.eval(reader, scriptNameSpace, "<?>");
			return value;
		}
		catch (TargetError e)
		{
			errMsg = e.getMessage();
			error = e.getTarget();
		}
		catch (EvalError e)
		{
			errMsg = e.getMessage();
		}

		if (errMsg != null)
		{
			int i = errMsg.indexOf("<?>");
			if (i >= 0)
			{
				int n = errMsg.length();
				for (i += 3; i < n; ++i)
				{
					char c = errMsg.charAt(i);
					if (c != ':' && !Character.isWhitespace(c))
						break;
				}
				errMsg = errMsg.substring(i);
			}
		}
		String msg = LogUtil.error(getClass(), "Error evaluating {0} $1 (context: $2):\n{3}.", fileType, fileName, context, errMsg);

		// Throw an exception
		if (error != null)
		{
			throw new ScriptTargetException(msg, error);
		}

		throw new EngineException("ScriptExecutionFailed", msg, error);
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Sets a variable in the global namespace of the script interpreter.
	 *
	 * @param name Name of the variable
	 * @param value Value of the variable
	 */
	public void setScriptVariable(String name, Object value)
	{
		try
		{
			// map null to Primtive.NULL coming in...
			if (value == null)
			{
				value = Primitive.NULL;
			}
			scriptNameSpace.setVariable(name, value, false);
		}
		catch (Exception e)
		{
			// Shoudln't happen
		}
	}

	/**
	 * Gets the value of a variable in the global namespace of the script interpreter.
	 *
	 * @param name Name of the variable
	 * @return Value of the variable or null
	 */
	public Object getScriptVariable(String name)
	{
		try
		{
			Object value = scriptNameSpace.getVariable(name);
			if (value != null)
			{
				value = Primitive.unwrap(value);
			}
			return value;
		}
		catch (Exception e)
		{
			// Shoudln't happen
		}
		return null;
	}

	/**
	 * Removes a variable from the global namespace of the script interpreter.
	 *
	 * @param name Name of the variable
	 */
	public void removeScriptVariable(String name)
	{
		try
		{
			// This will remove the variable from the name space
			scriptNameSpace.removeVariable(name);
		}
		catch (Exception e)
		{
			// Shoudln't happen
		}
	}
}
