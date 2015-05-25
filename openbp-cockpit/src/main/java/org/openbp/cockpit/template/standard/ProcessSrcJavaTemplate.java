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
package org.openbp.cockpit.template.standard;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openbp.common.generic.PrintNameProvider;
import org.openbp.common.generic.description.DescriptionObject;
import org.openbp.common.string.StringUtil;
import org.openbp.common.template.writer.JavaTemplateWriter;
import org.openbp.common.template.writer.Placeholder;
import org.openbp.core.CoreConstants;
import org.openbp.core.model.ModelLocationUtil;
import org.openbp.core.model.item.process.ActivityNode;
import org.openbp.core.model.item.process.ControlLink;
import org.openbp.core.model.item.process.DataLink;
import org.openbp.core.model.item.process.DecisionNode;
import org.openbp.core.model.item.process.FinalNode;
import org.openbp.core.model.item.process.InitialNode;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.Param;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessVariable;
import org.openbp.core.model.item.process.SubprocessNode;
import org.openbp.core.model.item.process.VisualNode;
import org.openbp.core.model.item.process.WebServiceNode;
import org.openbp.core.model.item.type.DataTypeItem;

/**
 * Process source code templates.
 * Input: Process items.
 *
 * @author Heiko Erhardt
 */
public class ProcessSrcJavaTemplate extends JavaSrcTemplate
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Process */
	protected ProcessItem process;

	/** Source file writer */
	protected JavaTemplateWriter w;

	/** Initial node that corresponds to the method */
	protected InitialNode currentInitialNode;

	/**
	 * List of nodes that have been encountered in the current execution path, i. e. in the current block or its parent blocks.
	 * (maps {@link Node} objects to themselves).
	 */
	protected Map executionPathNodes = new HashMap();

	/**
	 * List of all nodes that have been encountered yet.
	 * Used to detect jumps between independant paths of execution (which is not allowed).
	 * (maps {@link Node} objects to themselves).
	 */
	protected Map allNodes = new HashMap();

	//////////////////////////////////////////////////
	// @@ Standard methods
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public ProcessSrcJavaTemplate()
	{
	}

	/**
	 * This method is called when the template executes.
	 *
	 * @param templateArgs Template arguments provided by the caller (null in this case)
	 * @throws Exception On error
	 */
	public void generate(Object[] templateArgs)
		throws Exception
	{
		process = (ProcessItem) getProperty("item");

		String className = determineClassName();

		// Create the source file in the 'src' directory
		w = new JavaTemplateWriter(ModelLocationUtil.expandModelLocation(process.getModel(), ModelLocationUtil.DIR_SRC), className);
		printFile();
		w.close();

		// Communicate the information about the generated files to the generator that invoked us.
		addResultFileInfo(w.getFileName(), "text/x-java");
	}

	/**
	 * Returns the fully qualified class name to use for the process class.
	 */
	protected String determineClassName()
	{
		// TODO Feature 6: Determine class name for process
		return process.getName();
	}

	//////////////////////////////////////////////////
	// @@ Custom methods
	//////////////////////////////////////////////////

	/**
	 * Performs the generation.
	 *
	 * @throws Exception On error
	 */
	protected void printFile()
		throws Exception
	{
		// File header generation
		printFileHeader(w);

		// Class comment
		String comment = createComment(process);
		w.printComment(comment, JavaTemplateWriter.COMMENT_MULTI);

		// Print the class itself
		printClassCode();
	}

	//////////////////////////////////////////////////
	// @@ Print methods
	//////////////////////////////////////////////////

	/**
	 * Prints the class comment.
	 *
	 * @throws Exception On error
	 */
	protected void printClassCode()
		throws Exception
	{
		// Class name
		w.println("public class " + w.getClassName() + "");

		// Base classes
		w.addIndent(1);
		w.println("// {{*Custom extends*");
		w.println("// }}*Custom extends*");

		// Implemented interfaces
		w.println("// {{*Custom interfaces*");
		w.println("// }}*Custom interfaces*");
		w.addIndent(- 1);

		// Begin of class
		w.println("{");

		// Print process variables as static or member variables
		printProcessVariables();

		// Custom members
		w.println("// {{*Custom members*");
		w.println("// }}*Custom members*");
		w.println();

		// Default constructor
		w.println("/**");
		w.println("* Default constructor.");
		w.println("*/");
		w.println("public " + w.getClassName() + " ()");
		w.println("{");
		w.println("// {{*Custom default constructor code*");
		w.println("// }}*Custom default constructor code*");
		w.println("}");
		w.println();

		// Custom constructors
		w.println("// {{*Custom constructor*");
		w.println("// }}*Custom constructor*");
		w.println();

		// Print a method for each initial node
		printMethods();

		// Custom methods
		w.println("// {{*Custom methods*");
		w.println("// }}*Custom methods*");

		// End of class
		w.println("}");
	}

	//////////////////////////////////////////////////
	// @@ Process variables
	//////////////////////////////////////////////////

	/**
	 * Prints declarations of process variables.
	 *
	 * @throws Exception On error
	 */
	protected void printProcessVariables()
		throws Exception
	{
		for (Iterator it = process.getProcessVariables(); it.hasNext();)
		{
			ProcessVariable param = (ProcessVariable) it.next();
			printGlobal(param);
		}
	}

	/**
	 * Prints the declaration of a process variable.
	 *
	 * @param param Process variable to declare
	 * @throws Exception On error
	 */
	protected void printGlobal(ProcessVariable param)
		throws Exception
	{
		// Print the member comment
		String comment = createComment(param);
		w.printComment(comment, JavaTemplateWriter.COMMENT_SINGLE);

		// Determine data type
		DataTypeItem dataType = param.getDataType();
		String type = determineJavaType(w, dataType);

		String modifier = "private";
		switch (param.getScope())
		{
		case ProcessVariable.SCOPE_PROCESS:
			// Protected static scope
			modifier = "protected";
			break;

		case ProcessVariable.SCOPE_CONTEXT:
			// Public static scope
			modifier = "public";
			break;
		}

		w.println(modifier + " " + type + " " + makeVariableName(param) + ";");
		w.println();
	}

	//////////////////////////////////////////////////
	// @@ Local variables
	//////////////////////////////////////////////////

	/**
	 * Defines a method-local variable representing a data link.
	 *
	 * @param link Data link to define
	 */
	protected void addDataLinkVariable(DataLink link)
	{
		String name = makeVariableName(link);

		// Determine data type of the local variable
		DataTypeItem sourceType = link.getSourceParam().getDataType();
		DataTypeItem targetType = link.getTargetParam().getDataType();

		// Use the more specific type
		DataTypeItem dataType;
		if (sourceType.isBaseTypeOf(targetType))
		{
			dataType = targetType;
		}
		else
		{
			dataType = sourceType;
		}

		String stmt = constructDataLinkVariableDeclaration(name, dataType);

		// Add the declaration to the local variables placeholder for this method if not already present
		Placeholder placeholder = w.getPlaceholder(currentInitialNode.getName() + "Locals");
		if (! placeholder.contains(name))
		{
			placeholder.add(name, stmt);
		}
	}

	/**
	 * Constructs the declaration for a data link variable.
	 *
	 * @param name Name of the variable
	 * @param dataType Data type of the variable
	 * @return The statement
	 */
	protected String constructDataLinkVariableDeclaration(String name, DataTypeItem dataType)
	{
		String type = determineJavaType(w, dataType);

		// Construct the delcaration statement
		return type + " " + name + ";";
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Prints a method for each initial node.
	 *
	 * @throws Exception On error
	 */
	protected void printMethods()
		throws Exception
	{
		for (Iterator itNodes = process.getNodes(); itNodes.hasNext();)
		{
			Node node = (Node) itNodes.next();

			if (! (node instanceof InitialNode))
				continue;

			printMethod((InitialNode) node);
		}
	}

	/**
	 * Prints a method for each initial node.
	 *
	 * @param initialNode Initial node of the method
	 * @throws Exception On error
	 */
	protected void printMethod(InitialNode initialNode)
		throws Exception
	{
		currentInitialNode = initialNode;

		// Determine corresponding exit socket and print return type by its param type or void if none
		FinalNode finalNode = initialNode.getWSCorrespondingFinalNode();

		// Print the method comment
		String comment = createComment(initialNode);
		w.printComment(comment, JavaTemplateWriter.COMMENT_MULTI);

		// Print the method header including the parameter list
		printMethodHeader(initialNode, finalNode);

		// Print the method header including the parameter list
		try
		{
			printMethodBody(initialNode, finalNode);
		}
		catch (CodeGeneratorException e)
		{
			// We catch all errors caused by an invalid execution flow of the process here.
			// This is usually something like unconnected sockets, cross-method control or data links etc.
			// The error has been printed to stderr already, so we don't need to do anything here.
		}

		// Print the method header including the parameter list
		printMethodFooter(initialNode, finalNode);

		w.println();
	}

	/**
	 * Prints the method header including the parameter list.
	 *
	 * @param initialNode Initial node of the method
	 * @param finalNode Final node of the method or null
	 * @throws Exception On error
	 */
	protected void printMethodHeader(InitialNode initialNode, FinalNode finalNode)
		throws Exception
	{
		String modifier = "public";
		switch (initialNode.getEntryScope())
		{
		case InitialNode.SCOPE_PUBLIC:
			// Private scope
			break;

		case InitialNode.SCOPE_PROTECTED:
			// Protected scope
			modifier = "protected";
			break;

		case InitialNode.SCOPE_PRIVATE:
			// Private static scope
			modifier = "private";
			break;
		}

		// Determine the method return type from the (only!) final node parameter
		// By default, we assume a method returning no value.
		String returnType = "void";
		if (finalNode != null)
		{
			boolean firstParam = true;
			for (Iterator it = finalNode.getSocket().getParams(); it.hasNext();)
			{
				if (! firstParam)
				{
					errMsg("Warning: Final node '" + finalNode.getQualifier() + "' contains more than one return parameter.");
					break;
				}

				NodeParam param = (NodeParam) it.next();

				// Determine the return data type
				DataTypeItem dataType = param.getDataType();
				returnType = determineJavaType(w, dataType);

				firstParam = false;
			}
		}

		// Print method name and opening parenthesis
		w.print(modifier + " " + returnType + " " + makeVariableName(initialNode) + "(");

		boolean firstParam = true;
		for (Iterator it = initialNode.getSocket().getParams(); it.hasNext();)
		{
			NodeParam param = (NodeParam) it.next();

			// Determine data type
			DataTypeItem dataType = param.getDataType();
			String type = determineJavaType(w, dataType);

			if (firstParam)
				firstParam = false;
			else
				w.print(", ");

			// Print parameter type and name
			w.print(type + " " + makeVariableName(param));
		}

		// Print closing parenthesis
		w.println(")");

		w.println("{");
	}

	/**
	 * Prints the method body.
	 *
	 * @param initialNode Initial node of the method
	 * @param finalNode Final node of the method or null
	 * @throws Exception On error
	 * @throws CodeGeneratorException If there is a severe error that prevents the generation of meaningful source code
	 */
	protected void printMethodBody(InitialNode initialNode, FinalNode finalNode)
		throws Exception
	{
		executionPathNodes.clear();

		// Define and print the placeholder
		String placeholderName = initialNode.getName() + "Locals";
		w.definePlaceholder(placeholderName, Placeholder.HASH_TYPE);
		w.println("@[" + placeholderName + "]@");
		w.println();

		// Print the execution path starting with the initial node until an final node is encountered
		printExecution(initialNode.getSocket(), null);
	}

	/**
	 * Prints the method footer.
	 *
	 * @param initialNode Initial node of the method
	 * @param finalNode Final node of the method or null
	 * @throws Exception On error
	 */
	protected void printMethodFooter(InitialNode initialNode, FinalNode finalNode)
		throws Exception
	{
		w.println("}");
	}

	//////////////////////////////////////////////////
	// @@ Execution handling
	//////////////////////////////////////////////////

	/**
	 * Prints an execution path starting with the supplied start socket.
	 *
	 * @param startSocket Socket to start with (can be an entry or exit socket)
	 * @param stopNode Node that ends the execution path or null.
	 * The code of the stop node is not printed.
	 * A return statement always finishes the execution path generation normally.
	 *
	 * @throws Exception On error
	 * @throws CodeGeneratorException If there is a severe error that prevents the generation of meaningful source code
	 */
	protected void printExecution(NodeSocket startSocket, Node stopNode)
		throws Exception
	{
		boolean start = true;

		for (NodeSocket socket = startSocket; socket != null;)
		{
			Node node = socket.getNode();

			if (node == stopNode)
			{
				// We reached the stop condition
				break;
			}

			if (! start)
			{
				// Print a newline to separate the node from the last one
				w.println();
			}
			start = false;

			NodeSocket entrySocket = null;
			NodeSocket exitSocket = null;

			if (socket.isEntrySocket())
			{
				// Entry socket specified, determine the default exit socket
				entrySocket = socket;
				exitSocket = node.getDefaultExitSocket();
			}
			else
			{
				// Exit socket specified, no entry socket (e. g. for initial node)
				exitSocket = socket;
			}

			String comment = node.getDisplayText();
			if (comment != null)
			{
				w.printComment(comment, JavaTemplateWriter.COMMENT_SHORT);
			}

			if (node instanceof DecisionNode)
			{
				// Decision node: if-then-else statement
				DecisionNode decisionNode = (DecisionNode) node;

				// Determine the outgoing sockets of the decision node
				// and advance to the entry socket of the next nodes.
				NodeSocket yesSocket = getNamedSocket(node, CoreConstants.SOCKET_YES);
				yesSocket = ((ControlLink) yesSocket.getControlLinks().next()).getTargetSocket();
				NodeSocket noSocket = getNamedSocket(node, CoreConstants.SOCKET_NO);
				noSocket = ((ControlLink) noSocket.getControlLinks().next()).getTargetSocket();

				Node joinNode = skipDecisionNodeBranches(decisionNode);

				printDecisionNodeCondition(decisionNode, entrySocket);
				printDecisionNodeThen(decisionNode, yesSocket, joinNode);
				printDecisionNodeElse(decisionNode, noSocket, joinNode);

				// Continue with the join node
				if (joinNode != null)
				{
					socket = joinNode.getDefaultEntrySocket();
					continue;
				}

				// No common join point, end here
				break;
			}
			else if (node instanceof VisualNode)
			{
				errMsg("Error: Visual node '" + node.getQualifier() + "' cannot be mapped to a code-based representation.");
				throw new CodeGeneratorException();
			}
			else if (node instanceof WebServiceNode)
			{
				errMsg("Error: Web service node '" + node.getQualifier() + "' cannot be mapped to a code-based representation.");
				throw new CodeGeneratorException();
			}
			else if (node instanceof SubprocessNode)
			{
				// Sub process node: Method call
				// TODO Feature 6: Implement sub process call
			}
			else if (node instanceof ActivityNode)
			{
				// Activity node: Regular statement or method call
				printActivityNode((ActivityNode) node, entrySocket, exitSocket);
			}
			else if (node instanceof InitialNode)
			{
				// Initial node: Entry parameter assignments
				printInitialNode((InitialNode) node, exitSocket);
			}
			else if (node instanceof FinalNode)
			{
				// Final node: Return statement
				printFinalNode((FinalNode) node, entrySocket);
				break;
			}
			else
			{
				errMsg("Error: Unkown node '" + node.getQualifier() + "' cannot be mapped to a code-based representation.");
				throw new CodeGeneratorException();
			}

			if (exitSocket == null || ! exitSocket.hasControlLinks())
			{
				errMsg("Error: Node '" + node.getQualifier() + "' has no default exit socket or no control links attached to the socket.");
				throw new CodeGeneratorException();
			}

			ControlLink cl = (ControlLink) exitSocket.getControlLinks().next();
			socket = cl.getTargetSocket();
		}
	}

	//////////////////////////////////////////////////
	// @@ Entry/final nodes
	//////////////////////////////////////////////////

	/**
	 * Prints the initial node code.
	 *
	 * @param node Initial node
	 * @param exitSocket Exit socket
	 * @throws Exception On error
	 * @throws CodeGeneratorException If there is a severe error that prevents the generation of meaningful source code
	 */
	protected void printInitialNode(InitialNode node, NodeSocket exitSocket)
		throws Exception
	{
		// Initial node: Entry parameter assignments
		for (Iterator itParam = exitSocket.getParams(); itParam.hasNext();)
		{
			NodeParam param = (NodeParam) itParam.next();

			for (Iterator itLink = param.getDataLinks(); itLink.hasNext();)
			{
				DataLink link = (DataLink) itLink.next();

				String sourceName = makeVariableName(param);
				String targetName;

				Param targetParam = link.getTargetParam();
				if (targetParam instanceof ProcessVariable)
				{
					// Use the name of the process variable as parameter substitution value
					targetName = makeVariableName(targetParam);
				}
				else
				{
					// Use the name of the data link as parameter substitution value (is used as temporary variable)
					addDataLinkVariable(link);
					targetName = makeVariableName(link);
				}

				printAsssignment(targetName, sourceName);
			}
		}
	}

	/**
	 * Prints the final node code.
	 *
	 * @param node Final node
	 * @param entrySocket Entry socket
	 * @throws Exception On error
	 * @throws CodeGeneratorException If there is a severe error that prevents the generation of meaningful source code
	 */
	protected void printFinalNode(FinalNode node, NodeSocket entrySocket)
		throws Exception
	{
		boolean first = true;

		// Final node: Exit parameter assignments
		for (Iterator itParam = entrySocket.getParams(); itParam.hasNext();)
		{
			NodeParam param = (NodeParam) itParam.next();

			String expr = obtainExpression(param.getExpression(), "Java", param);
			if (expr == null)
				expr = param.getName();

			if (! first)
			{
				errMsg("Warning: Final node '" + entrySocket.getNode().getQualifier() + "' may not have more than one return parameter.");
				break;
			}
			first = false;

			String stmt = "return " + expr + ";";

			stmt = expandParametrizedExpression(stmt, entrySocket);

			w.println(stmt);
		}
	}

	/**
	 * Prints a variable asssignment.
	 *
	 * @param targetName Target variable name
	 * @param sourceName Source variable name
	 * @throws Exception On error
	 */
	protected void printAsssignment(String targetName, String sourceName)
		throws Exception
	{
		w.println(targetName + " = " + sourceName + ";");
	}

	//////////////////////////////////////////////////
	// @@ Activity node
	//////////////////////////////////////////////////

	/**
	 * Prints the activity node code.
	 *
	 * @param node Activity node
	 * @param entrySocket Entry socket
	 * @param exitSocket Exit socket
	 * @throws Exception On error
	 * @throws CodeGeneratorException If there is a severe error that prevents the generation of meaningful source code
	 */
	protected void printActivityNode(ActivityNode node, NodeSocket entrySocket, NodeSocket exitSocket)
		throws Exception
	{
		String script = obtainExpression(node.getActivityHandlerDefinition().getScript(), "Java", node);
		if (script != null)
		{
			script = expandParametrizedExpression(script, entrySocket, exitSocket);
			w.println(script + ";");
		}
		else
		{
			// TODO Feature 6: Implement activity node that is not script-based
			errMsg("Error: No script associated with activity node '" + node.getQualifier() + "'.");
		}
	}

	//////////////////////////////////////////////////
	// @@ Decision node
	//////////////////////////////////////////////////

	/**
	 * Prints the decision node condition.
	 *
	 * @param node Decision node
	 * @param entrySocket Entry socket
	 * @throws Exception On error
	 * @throws CodeGeneratorException If there is a severe error that prevents the generation of meaningful source code
	 */
	protected void printDecisionNodeCondition(DecisionNode node, NodeSocket entrySocket)
		throws Exception
	{
		// Get the if expression
		String expression = obtainExpression(node.getExpression(), "Java", node);

		if (expression == null)
		{
			errMsg("Error: No Java expression defined for decision node '" + node.getQualifier() + "'.");
			throw new CodeGeneratorException();
		}

		// Expand parameter values in the expression
		expression = expandParametrizedExpression(expression, entrySocket, null);

		w.println("if (" + expression + ")");
	}

	/**
	 * Prints the decision node then.
	 *
	 * @param node Decision node
	 * @param yesSocket Socket that refers to the 'then' path of the statement
	 * @param joinNode Node that joins the 'then' and the 'else' path
	 * @throws Exception On error
	 * @throws CodeGeneratorException If there is a severe error that prevents the generation of meaningful source code
	 */
	protected void printDecisionNodeThen(DecisionNode node, NodeSocket yesSocket, Node joinNode)
		throws Exception
	{
		w.println("{");

		printExecution(yesSocket, joinNode);

		w.println("}");
	}

	/**
	 * Prints the decision node else.
	 *
	 * @param node Decision node
	 * @param noSocket Socket that refers to the 'else' path of the statement
	 * @param joinNode Node that joins the 'then' and the 'else' path
	 * @throws Exception On error
	 * @throws CodeGeneratorException If there is a severe error that prevents the generation of meaningful source code
	 */
	protected void printDecisionNodeElse(DecisionNode node, NodeSocket noSocket, Node joinNode)
		throws Exception
	{
		// Only print the else part if it contains statements at all
		if (noSocket.getNode() != joinNode)
		{
			w.println("else");
			w.println("{");

			printExecution(noSocket, joinNode);

			w.println("}");
		}
	}

	/**
	 * Skips decision node branches.
	 *
	 * @param decisionNode Decision node
	 * @throws CodeGeneratorException If there is a severe error that prevents the generation of meaningful source code
	 */
	protected Node skipDecisionNodeBranches(DecisionNode decisionNode)
		throws CodeGeneratorException
	{
		// Determine the outgoing sockets of the decision node
		// and advance to the entry socket of the next nodes.

		NodeSocket yesSocket = getNamedSocket(decisionNode, CoreConstants.SOCKET_YES);
		yesSocket = ((ControlLink) yesSocket.getControlLinks().next()).getTargetSocket();

		NodeSocket noSocket = getNamedSocket(decisionNode, CoreConstants.SOCKET_NO);
		noSocket = ((ControlLink) noSocket.getControlLinks().next()).getTargetSocket();

		return findJoiningNode(decisionNode, yesSocket, noSocket);
	}

	/**
	 * Finds the node that joins the 'then' and the 'else' part of a decison node.
	 *
	 * @param decisionNode Decision node 
	 * @param yesSocket Yes socket
	 * @param noSocket No socket
	 * @return The first common node or null if no common node has been encountered
	 * @throws CodeGeneratorException If there is a severe error that prevents the generation of meaningful source code
	 */
	protected Node findJoiningNode(DecisionNode decisionNode, NodeSocket yesSocket, NodeSocket noSocket)
		throws CodeGeneratorException
	{
		// Follow the 'yes' path of the decision node
		for (NodeSocket socket = yesSocket; socket != null;)
		{
			Node node = socket.getNode();

			// Check if this node is also present in the 'no' path.
			if (containsPathNode(node, noSocket))
				// Yes, the node is common to both paths.
				// This is our join node.
				return node;

			if (node instanceof DecisionNode)
			{
				Node joinNode = skipDecisionNodeBranches((DecisionNode) node);
				if (joinNode == null)
					// Both decision node branches end with final nodes
					return null;
				socket = joinNode.getDefaultEntrySocket();
				continue;
			}
			else if (node instanceof FinalNode)
				// Yes branch ends with final node
				return null;

			socket = node.getDefaultExitSocket();
			if (socket == null || ! socket.hasControlLinks())
			{
				errMsg("Error: Node '" + node.getQualifier() + "' has no default exit socket or no control links attached to the socket.");
				throw new CodeGeneratorException();
			}

			ControlLink cl = (ControlLink) socket.getControlLinks().next();
			socket = cl.getTargetSocket();
		}

		return null;
	}

	protected boolean containsPathNode(Node nodeToSearch, NodeSocket socket)
		throws CodeGeneratorException
	{
		while (socket != null)
		{
			Node node = socket.getNode();

			if (node == nodeToSearch)
				// Found the node
				return true;

			if (node instanceof DecisionNode)
			{
				Node joinNode = skipDecisionNodeBranches((DecisionNode) node);
				if (joinNode == null)
				{
					// Both decision node branches end with final nodes
					break;
				}
				socket = joinNode.getDefaultEntrySocket();
				continue;
			}
			else if (node instanceof FinalNode)
			{
				// Yes branch ends with final node
				break;
			}

			socket = node.getDefaultExitSocket();
			if (socket == null || ! socket.hasControlLinks())
			{
				errMsg("Error: Node '" + node.getQualifier() + "' has no default exit socket or no control links attached to the socket.");
				throw new CodeGeneratorException();
			}

			ControlLink cl = (ControlLink) socket.getControlLinks().next();
			socket = cl.getTargetSocket();
		}

		return false;
	}

	//////////////////////////////////////////////////
	// @@ Parameter processing
	//////////////////////////////////////////////////

	/**
	 * Expands parameters in an expression, substituting parameter names for their actual values.
	 *
	 * @param expression Expression to evaluate
	 * @param entrySocket Entry socket
	 * @param exitSocket Exit socket
	 * @return The expanded expression
	 * @throws CodeGeneratorException On error
	 */
	protected String expandParametrizedExpression(String expression, NodeSocket entrySocket, NodeSocket exitSocket)
		throws CodeGeneratorException
	{
		expression = expandParametrizedExpression(expression, entrySocket);
		expression = expandParametrizedExpression(expression, exitSocket);
		return expression;
	}

	/**
	 * Expands parameters in an expression, substituting parameter names for their actual values.
	 *
	 * @param expression Expression to evaluate
	 * @param socket socket that hosts the parameters
	 * @return The expanded expression
	 * @throws CodeGeneratorException On error
	 */
	protected String expandParametrizedExpression(String expression, NodeSocket socket)
		throws CodeGeneratorException
	{
		if (socket != null)
		{
			for (Iterator it = socket.getParams(); it.hasNext();)
			{
				NodeParam param = (NodeParam) it.next();
				String name = param.getName();

				// TODO Feature 6: Hmmm, this should be checked against something like end-of-word or so
				if (expression.indexOf(name) >= 0)
				{
					String value = getParamValue(socket, name);
					expression = StringUtil.substitute(expression, name, value);
				}
			}
		}

		return expression;
	}

	/**
	 * Obtains the value for a named parameter.
	 *
	 * @param socket Socket that hosts the parameter
	 * @param paramName Parameter name
	 * @return The parameter value or null if no such parameter exists
	 * @throws CodeGeneratorException If the value could not be resolved
	 */
	protected String getParamValue(NodeSocket socket, String paramName)
		throws CodeGeneratorException
	{
		NodeParam param = socket.getParamByName(paramName);
		if (param == null)
		{
			errMsg("Error: Parameter '" + paramName + "' does not exist in socket '" + socket.getQualifier() + "'.");
			throw new CodeGeneratorException();
		}

		Iterator it = param.getDataLinks();
		if (! it.hasNext())
		{
			// No parameters, try the expression
			if (param.getExpression() != null)
				return param.getExpression();

			errMsg("Error: Parameter '" + param.getQualifier() + "' not connected to a data link and not associated with an expression.");
			throw new CodeGeneratorException();
		}

		DataLink link = (DataLink) it.next();

		if (it.hasNext())
		{
			errMsg("Warning: Only a single data link allowed to parameter '" + param.getQualifier() + "'.");
		}

		Param sourceParam = link.getSourceParam();
		if (sourceParam instanceof ProcessVariable)
			// Use the name of the process variable as parameter substitution value
			return makeVariableName(sourceParam);

		// Use the name of the data link as parameter substitution value (is used as temporary variable)
		addDataLinkVariable(link);
		return makeVariableName(link);
	}

	//////////////////////////////////////////////////
	// @@ Utilities
	//////////////////////////////////////////////////

	/**
	 * Gets a socket by its name.
	 *
	 * @param name Name
	 * @return The socket or null 
	 * @throws CodeGeneratorException If there is a severe error that prevents the generation of meaningful source code
	 */
	protected NodeSocket getNamedSocket(Node node, String name)
		throws CodeGeneratorException
	{
		for (Iterator itSockets = node.getSockets(); itSockets.hasNext();)
		{
			NodeSocket socket = (NodeSocket) itSockets.next();

			if (socket.getName().equals(name))
			{
				if (! socket.hasControlLinks())
				{
					errMsg("Error: Socket '" + name + "' of node '" + node.getQualifier() + "' not connected.");
					throw new CodeGeneratorException();
				}
				return socket;
			}
		}

		errMsg("Error: Socket '" + name + "' of node '" + node.getQualifier() + "' not present.");
		throw new CodeGeneratorException();
	}

	/**
	 * Obtains an expression for this source language from the given string.
	 * The string is expected to either contain a number of expressions - each separated by $$<i>language</i>$$ prefices -
	 * or a single Java expression.
	 *
	 * @param expression Expresssion
	 * @param desiredLanguage The language we are looking for (e. g. "Java")
	 * @return The language-specific expression or null if no appropriate expression was found.
	 * @throws CodeGeneratorException If there is no definition for the specified language
	 */
	protected String obtainExpression(String expression, String desiredLanguage, PrintNameProvider owner)
		throws CodeGeneratorException
	{
		if (expression == null || ! expression.startsWith("$$"))
			// Generic expression
			return expression;

		/*
		 * Scan something like:
		 * $$Java$$
		 * System.out.println (Value)
		 * $$SSL$$
		 * say Value
		 */

		while (expression != null && expression.startsWith("$$"))
		{
			int endIndex = expression.indexOf("$$", 2);
			if (endIndex < 0)
			{
				errMsg("$$ separator missing in expression or script of element '" + owner.getPrintName() + "'.");
				throw new CodeGeneratorException();
			}

			String language = expression.substring(2, endIndex);
			language = language.trim();

			String value;
			int endIndex2 = expression.indexOf("$$", endIndex + 2);
			if (endIndex2 < 0)
			{
				value = expression.substring(endIndex + 2);
				expression = null;
			}
			else
			{
				value = expression.substring(endIndex + 2, endIndex2);
				expression = expression.substring(endIndex2);
			}
			value = value.trim();

			if (language.equals(desiredLanguage))
				return value;
		}

		errMsg("Language '" + desiredLanguage + "' not found in expression or script of element '" + owner.getPrintName() + "'.");
		throw new CodeGeneratorException();
	}

	/**
	 * Produces a syntactically correct variable name from an arbitrary object.
	 *
	 * @param o Object to create a name for.<br>
	 * The original object name (o.getName ()) should be pretty fine, since allowed for Java identifiers only.<br>
	 * However, we have to make sure that it doesn't conflict with the name of a reserved word by prepending an underscore if we have to.
	 * @return The name (first character decapitalized)
	 */
	protected String makeVariableName(DescriptionObject o)
	{
		return StringUtil.decapitalize(o.getName());
	}
}
