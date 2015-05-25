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
package org.openbp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.application.Application;
import org.openbp.core.model.WorkflowTaskDescriptor;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.WaitStateNode;
import org.openbp.core.model.item.process.WorkflowNode;
import org.openbp.server.context.TokenContext;

/**
 * Generic sample that runs a process in batch mode.
 * For each wait state or task node, the process stops, prints out the names
 * of the possible exit sockets and asks the user for the socket to choose using the standard input/output.
 *
 * @author Heiko Erhardt
 */
public class CommandLineSample
{
	//////////////////////////////////////////////////
	// @@ Main method
	//////////////////////////////////////////////////

	/**
	 * Main entry point to the OpenBP server.
	 * @param args Command line arguments
	 */
	public static void main(String [] args)
	{
		try
		{
			CommandLineSample sample = new CommandLineSample(args);
			while (sample.perform())
				;
		}
		catch (Exception e)
		{
			ExceptionUtil.printTrace(e);
			System.exit(1);
		}

		System.exit(0);
	}

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** User input reader */
	private BufferedReader reader;

	/** Process server */
	private ProcessServer processServer;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Value constructor.
	 *
	 * Parses the command line for server, core or application options and initalizes
	 * the OpenBP framwork.<br>
	 * If an option parsing or initialization error occurs, the application is exited.
	 * An error message will be written to standard error.
	 *
	 * @param arguments Arguments to the main method or null
	 */
	public CommandLineSample(String [] arguments)
	{
		long time = System.currentTimeMillis();

		try
		{
			// Initialize the server
			Application.setArguments(arguments);

			// Start up the server
			processServer = new ProcessServerFactory().createProcessServer();

			// Compute startup time and print startup message
			time = System.currentTimeMillis() - time;
			String startedUpMessage = "OpenBP batch sample startup complete [" + time + " ms].";
			System.out.println(startedUpMessage);
		}
		catch (Exception e)
		{
			ExceptionUtil.printTrace("Error initializing OpenBP engine", e);
			System.exit(1);
		}
	}

	/**
	 * Runs the sample.
	 * @return false in the program should end
	 */
	public boolean perform()
		throws Exception
	{
		// Get an instance of the process facade
		ProcessFacade processFacade = processServer.getProcessFacade();

		reader = new BufferedReader(new InputStreamReader(System.in));

		String pos = readLine("Enter start node reference in the form \"/Model/Process.Node\" or \"q\" to quit:", "/PDVDemo/PDVTest.In");
		if (pos == null)
			return false;

		// Instantiate and fill the map to provide parameters
		Map params = null;

		// Create a new token that we can execute
		TokenContext token = processFacade.createToken();
		token.setDebuggerId("Deb1");

		// Start the token using the provided parameters
		processFacade.startToken(token, pos, params);
		pos = null;

		for (;;)
		{
			// Invoke the process
			if (pos != null)
			{
				// We have a position to continue the process with, so apply it
				processFacade.resumeToken(token, pos, null);
			}

			// Make the process engine execute all pending contexts and wait until there is all contexts have been executed.s have been executed.s have been executed.
			processFacade.executePendingContextsInThisThread();

			// See where we are
			if (token.getCurrentSocket() == null)
			{
				System.out.println("Process ended normally.");
				return false;
			}
			Node node = token.getCurrentSocket().getNode();

			if (node instanceof WaitStateNode)
			{
				System.out.println("Wait state node " + node.getName() + " encountered.");
			}
			else if (node instanceof WorkflowNode)
			{
				WorkflowTaskDescriptor task = ((WorkflowNode) node).getWorkflowTaskDescriptor();
				System.out.println("Task " + task.getStepDisplayName() + " encountered.");
			}
			else
			{
				System.out.println("Process end at node " + node.getName() + ".");
				/*
				 if (node instanceof FinalNode)
				 {
				 return false;
				 }
				 */
				break;
			}

			// Get the decision where to continue from the user
			pos = determineNextStartRef(node);
			if (pos == null)
				return false;
		}

		return true;
	}

	/**
	 * Prints out the possible resume options and asks the user what to do.
	 *
	 * @param node Current process node
	 * @return A string denoting the exit socket to choose (".SocketName") or null to stop
	 */
	private String determineNextStartRef(Node node)
	{
		System.out.println("Possible options (* denotes the default):");

		HashMap options = new HashMap();
		int dflt = 0;

		int i = 1;
		for (Iterator it = node.getSockets(); it.hasNext();)
		{
			NodeSocket socket = (NodeSocket) it.next();

			if (socket.isExitSocket())
			{
				String flag = " ";
				if (socket.isDefaultSocket())
				{
					dflt = i;
					flag = "*";
				}

				options.put(Integer.valueOf(i), "." + socket.getName());
				System.out.println("" + i + flag + ": " + socket.getDisplayText());

				++i;
			}
		}

		// Get the user's answer
		int answer = readInt("Enter option or empty for default:", dflt);
		if (answer <= 0)
			return null;

		String ret = (String) options.get(Integer.valueOf(answer));
		return ret;
	}

	private String readLine(String msg, String dflt)
	{
		System.out.println(msg);
		String s = null;

		try
		{
			s = reader.readLine();
			if (s != null)
			{
				if (s.length() == 0)
					s = dflt;

				if (s.equals("quit") || s.equals("q"))
					s = null;
			}
		}
		catch (IOException e)
		{
			ExceptionUtil.printTrace("I/O error", e);
		}

		return s;
	}

	private int readInt(String msg, int dflt)
	{
		for (;;)
		{
			String line = readLine(msg, "" + dflt);

			if (line == null)
				return -1;

			try
			{
				int ret = Integer.valueOf(line).intValue();
				return ret;
			}
			catch (NumberFormatException e)
			{
				System.out.println("Choose a valid number:");
			}
		}
	}
}
