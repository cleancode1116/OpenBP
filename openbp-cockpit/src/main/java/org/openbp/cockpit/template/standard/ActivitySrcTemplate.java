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
import java.util.List;

import org.openbp.common.string.NameUtil;
import org.openbp.common.template.writer.JavaTemplateWriter;
import org.openbp.common.template.writer.TemplateWriter;
import org.openbp.core.model.ModelLocationUtil;
import org.openbp.core.model.item.activity.ActivityParam;
import org.openbp.core.model.item.activity.ActivitySocket;
import org.openbp.core.model.item.activity.JavaActivityItem;
import org.openbp.core.model.item.activity.StandardSocketNames;
import org.openbp.core.model.item.type.DataTypeItem;

/**
 * Activity implementation source code template.
 *
 * @author Heiko Erhardt
 */
public class ActivitySrcTemplate extends JavaSrcTemplate
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Activity item */
	protected JavaActivityItem activity;

	/** Method comment that describes the input sockets/parameters */
	protected StringBuffer inputComment;

	/** Method comment that describes the output sockets/parameters */
	protected StringBuffer outputComment;

	/** Code for input parameter access */
	protected List inputParamAccess;

	/** Code for output parameter access */
	protected List outputParamAccess;

	/** Name of the standard exit socket, if any */
	protected String standardExitSocket;

	//////////////////////////////////////////////////
	// @@ Standard methods
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public ActivitySrcTemplate()
	{
	}

	/**
	 * This method is called when the template executes.
	 *
	 * @param templateArgs Template arguments provided by the caller (null in this case)
	 * @throws Exception On error
	 */
	public void generate(Object [] templateArgs)
		throws Exception
	{
		activity = (JavaActivityItem) getProperty("item");

		String className = activity.getHandlerDefinition().getHandlerClassName();

		// Create the source file in the 'src' directory
		JavaTemplateWriter writer = new JavaTemplateWriter(ModelLocationUtil.expandModelLocation(activity.getModel(), ModelLocationUtil.DIR_SRC), className);
		printFile(writer);
		writer.close();

		// Communicate the information about the generated files to the generator that invoked us.
		addResultFileInfo(writer.getFileName(), "text/x-java");
	}

	//////////////////////////////////////////////////
	// @@ Custom methods
	//////////////////////////////////////////////////

	/**
	 * Performs the generation.
	 *
	 * @param w Source file writer
	 * @throws Exception On error
	 */
	protected void printFile(JavaTemplateWriter w)
		throws Exception
	{
		// Member initialization
		inputComment = new StringBuffer();
		outputComment = new StringBuffer();

		// File header generation
		printFileHeader(w);

		w.definePlaceholder("socketConstants", TemplateWriter.LIST_PLACEHOLDER);
		w.definePlaceholder("paramConstants", TemplateWriter.LIST_PLACEHOLDER);
		w.definePlaceholder("inputParamAccess", TemplateWriter.LIST_PLACEHOLDER);
		w.definePlaceholder("outputParamAccess", TemplateWriter.LIST_PLACEHOLDER);

		// Add standard imports
		w.addImport("org.openbp.server.handler.Handler");
		w.addImport("org.openbp.server.handler.HandlerContext");

		// Prepare the socket and parameter constants, the class comment and the parameter access statements
		List socketList = activity.getSocketList();
		prepareSocketInfo(w, socketList);

		if (!w.isPlaceholderEmpty("inputParamAccess"))
		{
			w.addToPlaceholder("inputParamAccess", "*/");
			w.addToPlaceholder("inputParamAccess", "");
		}

		if (!w.isPlaceholderEmpty("outputParamAccess"))
		{
			w.addToPlaceholder("outputParamAccess", "*/");
			w.addToPlaceholder("outputParamAccess", "");
		}

		// Print the class comment
		printClassComment(w);

		// Print the class itself
		printClassCode(w);
	}

	//////////////////////////////////////////////////
	// @@ Print methods
	//////////////////////////////////////////////////

	/**
	 * Prints the class comment.
	 *
	 * @param w Source file writer
	 * @throws Exception On error
	 */
	protected void printClassComment(JavaTemplateWriter w)
		throws Exception
	{
		String comment = createComment(activity, "Implementation of the " + activity.getName() + " activity handler.");

		if (inputComment.length() != 0)
		{
			comment += "\nInput sockets/parameter:\n";
			comment += inputComment.toString();
		}

		if (outputComment.length() != 0)
		{
			comment += "\nOutput sockets/parameter:\n";
			comment += outputComment.toString();
		}

		// Output the class comment
		w.printComment(comment, JavaTemplateWriter.COMMENT_MULTI);
	}

	/**
	 * Prints the class code.
	 *
	 * @param w Source file writer
	 * @throws Exception On error
	 */
	protected void printClassCode(JavaTemplateWriter w)
		throws Exception
	{
		// Class name
		w.println("public class " + w.getClassName());

		// Base classes
		w.addIndent(1);
		w.println("// {{*Custom extends*");
		w.println("// }}*Custom extends*");

		// Implemented interfaces
		w.println("// {{*Custom interfaces*");
		w.println("implements Handler");
		w.println("// }}*Custom interfaces*");
		w.addIndent(-1);

		// Begin of class
		w.println("{");

		// Constants for sockets and parameters
		w.println("@[socketConstants]@");
		w.println("@[paramConstants]@");

		// Custom constants
		w.println("// {{*Custom constants*");
		w.println("// }}*Custom constants*");
		w.println();

		// Members
		w.println("// {{*Custom members*");
		w.println("// Note: If you define member variables, consider the fact that the same handler instance may be executed");
		w.println("// by multiple threads in parallel, so you have to make sure that your implementation is thread safe.");
		w.println("// In general, member variables should be defined for global-like data only.");
		w.println("// }}*Custom members*");
		w.println();

		// Execute method
		w.println("/**");
		w.println("* Executes the handler.");
		w.println("*");
		w.println("* @param hc Handler context that contains execution parameters");
		w.println("* @return true if the handler handled the event, false to apply the default handling to the event");
		w.println("* @throws Exception Any exception that may occur during execution of the handler will be");
		w.println("* propagated to an exception handler if defined or abort the process execution otherwise.");
		w.println("*/");
		w.println("public boolean execute(HandlerContext hc)");

		w.addIndent(1);
		w.println("throws Exception");
		w.addIndent(-1);

		// Execute method - begin of body
		w.println("{");

		// Comments containing parameter access statements
		w.println("@[inputParamAccess]@");
		w.println("@[outputParamAccess]@");

		printImplemenationCodeOutsideTag(w);

		w.println("// {{*Handler implementation*");

		printImplemenationCodeInsideTag(w);

		w.println("// }}*Handler implementation*");

		// Execute method - end of body
		w.println("}");
		w.println();

		// Custom methods
		w.println("// {{*Custom methods*");
		w.println("// }}*Custom methods*");

		// End of class
		w.println("}");
	}

	/**
	 * Prints the class implementation code outside the {{*Handler implementation tag.
	 *
	 * @param w Source file writer
	 * @throws Exception On error
	 */
	protected void printImplemenationCodeOutsideTag(JavaTemplateWriter w)
		throws Exception
	{
	}

	/**
	 * Prints the class implementation code inside the {{*Handler implementation tag.
	 *
	 * @param w Source file writer
	 * @throws Exception On error
	 */
	protected void printImplemenationCodeInsideTag(JavaTemplateWriter w)
		throws Exception
	{
		w.println("return true;");
	}

	//////////////////////////////////////////////////
	// @@ Socket and parameter preparation
	//////////////////////////////////////////////////

	/**
	 * Collects data to build the list of symbolic constants ('socketConstants' and 'paramConstants' placeholders)
	 * and input and output socket/parameter explanations (goes into the class comment).
	 *
	 * @param w Source file writer
	 * @param socketList Socket list
	 */
	protected void prepareSocketInfo(JavaTemplateWriter w, List socketList)
	{
		if (socketList != null)
		{
			// Map containing an entry for each activity parameter we have generated
			// a symbolic constant for (keys: parameter names, values: Boolean.TRUE
			HashMap definedParams = new HashMap();

			int n = socketList.size();
			for (int i = 0; i < n; ++i)
			{
				ActivitySocket socket = (ActivitySocket) socketList.get(i);

				StringBuffer cm = socket.isEntrySocket() ? inputComment : outputComment;

				String socketName = socket.getName();
				String socketIdent = StandardSocketNames.getStandardSocketNameIdentifier(socketName);

				if (socketIdent == null)
				{
					// Add a string constant definition for this socket name to the 'constants' placeholder
					socketIdent = makeSocketName(socketName);
					String type = socket.isEntrySocket() ? "Input" : "Output";
					w.addToPlaceholder("socketConstants", "/** " + type + " socket " + socketName + " */");
					w.addToPlaceholder("socketConstants", "private static final String " + socketIdent + " = \"" + socketName + "\";");
					w.addToPlaceholder("socketConstants", "");
				}

				if (!socket.isEntrySocket() && socket.isDefaultSocket())
				{
					standardExitSocket = socketIdent;
				}

				cm.append("  Socket '");
				cm.append(socketName);
				cm.append("'");

				String dn = socket.getDisplayName();
				if (dn != null)
				{
					cm.append(": ");
					cm.append(dn);
				}
				cm.append("\n");

				List paramList = socket.getParamList();
				if (paramList != null)
				{
					int np = paramList.size();
					for (int ip = 0; ip < np; ++ip)
					{
						ActivityParam param = (ActivityParam) paramList.get(ip);

						String paramName = param.getName();
						String paramIdent = makeParamName(paramName);

						if (!definedParams.containsKey(paramName))
						{
							definedParams.put(paramName, Boolean.TRUE);
							w.addToPlaceholder("paramConstants", "/** Parameter " + paramName + " */");
							w.addToPlaceholder("paramConstants", "private static final String " + paramIdent + " = \"" + paramName + "\";");
							w.addToPlaceholder("paramConstants", "");
						}

						cm.append("    Parameter '");
						cm.append(paramName);
						cm.append("'");
						dn = param.getDisplayName();
						if (dn != null)
						{
							cm.append(": ");
							cm.append(dn);
						}
						cm.append("\n");

						DataTypeItem type = param.getDataType();
						if (type != null)
						{
							String className = type.getClassName();
							if (className == null)
								className = "Object";
							String typeName = extractUnqualifiedName(w, className);

							String paramVariableName = NameUtil.makeMemberName(paramName);

							if (socket.isEntrySocket())
							{
								if (w.isPlaceholderEmpty("inputParamAccess"))
								{
									w.addToPlaceholder("inputParamAccess", "/* Input parameter access code");
								}

								// 'type' 'name' = 'type' hc.getParam ('paramconstant');
								w.addToPlaceholder("inputParamAccess", typeName + " " + paramVariableName + " = " + (typeName.equals("Object") ? "" : "(" + typeName + ") ") + "hc.getParam(" + paramIdent + ");");
							}
							else
							{
								if (w.isPlaceholderEmpty("outputParamAccess"))
								{
									w.addToPlaceholder("outputParamAccess", "/* Output parameter access code");
								}

								// 'paramtype' 'name';

								w.addToPlaceholder("outputParamAccess", "" + typeName + " " + paramVariableName + ";");

								// hc.setResult('paramconstant', 'name');

								w.addToPlaceholder("outputParamAccess", "hc.setResult(" + paramIdent + ", " + paramVariableName + ");");
							}
						}
					}
				}
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	/**
	 * Returns the name of the symbolic constant for the specified socket.
	 * Either the standard socket name constant from the CoreConstants class
	 * or SOCKET_'upperName'.
	 *
	 * @param name Name
	 * @return The socket name 
	 */
	protected String makeSocketName(String name)
	{
		String stdName = StandardSocketNames.getStandardSocketNameIdentifier(name);
		if (stdName != null)
			return stdName;
		return "SOCKET_" + name.toUpperCase();
	}

	/**
	 * Returns the name of the symbolic constant for the specified parameter.
	 *
	 * @param name Name
	 * @return The param name 
	 */
	protected String makeParamName(String name)
	{
		return "PARAM_" + name.toUpperCase();
	}
}
