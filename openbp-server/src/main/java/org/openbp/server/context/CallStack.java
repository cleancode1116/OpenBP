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

import java.util.Iterator;

import org.openbp.core.OpenBPException;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessVariable;

/**
 * This interface defines the methods classes has to implement to be
 * used by an TokenContext implementation as a stack of NodeSocket's
 * as it is needed by the engine to perform subprocess-calls and -returns.
 *
 * @author Falk Hartmann
 */
public interface CallStack extends Cloneable
{
	//////////////////////////////////////////////////
	// @@ Basic stack handling
	//////////////////////////////////////////////////

	/**
	 * This method removes everything that has been pushed onto this call stack.
	 */
	public void clear();

	/**
	 * Gets the process call stack depth.
	 * The call stack depth increases each time a sub process is called and decreases
	 * when the sub process is being left.
	 * @return If the call stack depth is 0, the process is a top-level process
	 */
	public int getCallDepth();

	/**
	 * Push the current position before a sub process call onto the call stack.
	 *
	 * @param entrySocket Entry socket of the sub process node
	 * @throws OpenBPException If the maximum call stack depth has been reached.
	 * In this case, an unfinite recursion is likely to be the cause.
	 * @return The new call stack item that is now on top of the call stack
	 */
	public CallStackItem pushSubprocess(NodeSocket entrySocket);

	/**
	 * Pops the current position from the call stack.
	 *
	 * @return The popped call stack item containing the node socket and the former current position
	 * @throws OpenBPException If the call stack is empty
	 */
	public CallStackItem pop();

	/**
	 * Returns the current position from the call stack without popping it off the stack.
	 *
	 * @return The popped call stack item containing the node socket and the former current position
	 * or null if the stack is empty
	 */
	public CallStackItem peek();

	/**
	 * Gets an iteration of {@link CallStackItem} objects.
	 * @nowarn
	 */
	public Iterator iterator();

	//////////////////////////////////////////////////
	// @@ Advanced operations.
	//////////////////////////////////////////////////

	/**
	 * This method checks, whether the call stack contains an invocation of the given process.
	 *
	 * @param processToSearch Process to look up in the call stack
	 * @return
	 *		true	If the process has been found in the call stack.<br>
	 *		false	Otherwise
	 */
	public boolean isProcessExecuting(ProcessItem processToSearch);

	/**
	 * Gets a process variable by its name.
	 *
	 * @param name Name of the process variable
	 * @return The process variable or null if no such process variable exists
	 */
	public ProcessVariable getProcessVariableByName(String name);

	/**
	 * This method checks, whether the call stack contains the given socket.
	 *
	 * @param socketToSearch Socket to look up in the call stack
	 * @return
	 *		true	If some call stack item refers to this socket.<br>
	 *		false	Otherwise
	 */
	public boolean containsSocketReference(NodeSocket socketToSearch);

	/**
	 * Checks if this call stack references any sockets of the supplied process and
	 * refreshes the socket reference if appropriate.
	 *
	 * @param process The process that has been updated
	 * @return
	 *		true	All updates have been performed successfully.<br>
	 *		false	The call stack references one or more sockets that do not exist
	 *				any more in the updated process.
	 */
	public boolean performProcessUpdate(ProcessItem process);

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Sets the token context this call stack belongs to.
	 * For internal use only.
	 * @nowarn
	 */
	public void setTokenContext(TokenContext tokenContext);
}
