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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

import org.openbp.common.logger.LogUtil;
import org.openbp.core.OpenBPException;
import org.openbp.core.engine.EngineException;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessVariable;
import org.openbp.core.model.modelmgr.ModelMgr;
import org.openbp.server.engine.EngineUtil;

/**
 * This is an implementation of the {@link CallStack} interface that is used
 * by the Engine via the {@link TokenContextImpl} to track invocations of
 * sub processes and their returns.
 *
 * @author Falk Hartmann
 */
public class CallStackImpl
	implements CallStack, Serializable, Cloneable
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Maximum call stack size */
	public static final int MAX_CALL_STACK_SIZE = 50;

	private static final long serialVersionUID = 605837511221074718L;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Token context this call stack belongs to */
	private transient TokenContext tokenContext;

	/**
	 * Sub process call stack. We use an vector here, a push appends objects on the right side,
	 * while a pop removes them from there (right side == high index). The vector contains
	 * {@link StackItem}
	 */
	private Vector stackItems;

	//////////////////////////////////////////////////
	// @@ Construction.
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param tokenContext Token context this call stack belongs to
	 */
	public CallStackImpl(TokenContext tokenContext)
	{
		this();

		this.tokenContext = tokenContext;
	}

	/**
	 * Serialization constructor.
	 */
	public CallStackImpl()
	{
		// Initialize call stack data structures.
		stackItems = new Vector();
	}

	/**
	 * Creates a clone of this object.
	 * @return The base method return value
	 */
	public Object clone()
		throws CloneNotSupportedException
	{
		CallStackImpl c = (CallStackImpl) super.clone();
		c.stackItems = (Vector) stackItems.clone();
		return c;
	}

	public void initializeAfterDeserialization(TokenContext context)
	{
		ModelMgr modelMgr = context.getModelMgr();

		for (Iterator it = iterator(); it.hasNext();)
		{
			StackItem stackItem = (StackItem) it.next();
			stackItem.resolveNodeSocket(modelMgr);

			ProcessItem process = stackItem.getNodeSocket().getProcess();
			EngineUtil.createProcessVariables(process, context);
		}
	}

	/**
	 * Sets the token context this call stack belongs to.
	 * For internal use only.
	 * @nowarn
	 */
	public void setTokenContext(TokenContext tokenContext)
	{
		this.tokenContext = tokenContext;
	}

	//////////////////////////////////////////////////
	// @@ Basic stack handling
	//////////////////////////////////////////////////

	/**
	 * @copy CallStack.clear()
	 */
	public void clear()
	{
		setStackSize(0);
	}

	/**
	 * Gets the process call stack depth.
	 * The call stack depth increases each time a sub process is called and decreases
	 * when the sub process is being left.
	 * @return If the call stack depth is 0, the process is a top-level process
	 */
	public int getCallDepth()
	{
		return stackItems.size();
	}

	/**
	 * @copy CallStack.pushSubprocess(NodeSocket)
	 */
	public CallStackItem pushSubprocess(NodeSocket entrySocket)
	{
		CallStackItem stackItem = new StackItem(CallStackItem.TYPE_SEARCH, entrySocket);
		push(stackItem);
		return stackItem;
	}

	/**
	 * Pushes a stack item onto the stack.
	 *
	 * @param stackItem Item to push
	 */
	private void push(CallStackItem stackItem)
	{
		if (stackItems.size() >= MAX_CALL_STACK_SIZE)
			throw new EngineException("MaximumStackSizeExceeded", "Maximum call stack size "
				+ Integer.valueOf(MAX_CALL_STACK_SIZE)
				+ " has been exceeded. An infinite sub process recursion is likely to be the cause.");

		if (LogUtil.isDebugEnabled(getClass()))
		{
			LogUtil.debug(getClass(), "Call stack push (size now {0}): {1}", Integer.valueOf(stackItems.size() + 1),
				stackItem);
		}

		stackItems.add(stackItem);
	}

	/**
	 * Pops the current position from the call stack.
	 *
	 * @return The popped call stack item containing the node socket and the former current position
	 * @throws OpenBPException If the call stack is empty
	 */
	public CallStackItem pop()
	{
		// In case of an empty stack, we need to throw an exception...
		int size = stackItems.size();
		if (size == 0)
			throw new EngineException("EmptyCallStack", "The call stack is empty");

		// Get the last stack item.
		StackItem stackItem = (StackItem) stackItems.get(size - 1);

		// Pop the stack
		setStackSize(size - 1);

		// Return the result.
		return stackItem;
	}

	/**
	 * Returns the current position from the call stack without popping it off the stack.
	 *
	 * @return The popped call stack item containing the node socket and the former current position
	 * or null if the stack is empty
	 */
	public CallStackItem peek()
	{
		if (stackItems.size() > 0)
			return (StackItem) stackItems.get(stackItems.size() - 1);

		return null;
	}

	/**
	 * Gets an iteration of {@link CallStackItem} objects.
	 * @nowarn
	 */
	public Iterator iterator()
	{
		return stackItems.iterator();
	}

	//////////////////////////////////////////////////
	// @@ Advanced operations.
	//////////////////////////////////////////////////

	/**
	 * This method checks, whether the call stack contains a invocation of the given process.
	 *
	 * @param processToSearch Process to look up in the call stack
	 * @return
	 *		true	If the process has been found in the call stack.<br>
	 *		false	Otherwise
	 */
	public boolean isProcessExecuting(ProcessItem processToSearch)
	{
		for (int i = stackItems.size() - 1; i >= 0; --i)
		{
			StackItem stackItem = (StackItem) stackItems.get(i);

			ProcessItem process = stackItem.getNodeSocket().getProcess();
			if (process == processToSearch)
				return true;
		}

		return false;
	}

	/**
	 * Gets a process variable by its name.
	 *
	 * @param name Name of the process variable
	 * @return The process variable or null if no such process variable exists
	 */
	public ProcessVariable getProcessVariableByName(String name)
	{
		for (int i = stackItems.size() - 1; i >= 0; --i)
		{
			StackItem stackItem = (StackItem) stackItems.get(i);

			ProcessItem process = stackItem.getNodeSocket().getProcess();
			ProcessVariable var = process.getProcessVariableByName(name);
			if (var != null)
				return var;
		}

		return null;
	}

	/**
	 * This method checks, whether the call stack contains the given socket.
	 * Note that only call stack items of type {@link CallStackItem#TYPE_CONTINUE} are considered.
	 *
	 * @param socketToSearch Socket to look up in the call stack
	 * @return
	 *		true	If some call stack item refers to this socket.<br>
	 *		false	Otherwise
	 */
	public boolean containsSocketReference(NodeSocket socketToSearch)
	{
		// For all items on the stack...
		for (int i = stackItems.size() - 1; i >= 0; --i)
		{
			// Get the stack item.
			StackItem stackItem = (StackItem) stackItems.get(i);

			if (stackItem.getType() != CallStackItem.TYPE_CONTINUE)
			{
				// No continue stack item, ignore
				continue;
			}

			// Get the process of the corresponding node socket
			if (stackItem.getNodeSocket() == socketToSearch)
				// Found
				return true;
		}

		// Socket not found in call stack
		return false;
	}

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
	public boolean performProcessUpdate(ProcessItem process)
	{
		boolean result = true;

		for (int i = 0; i < stackItems.size(); i++)
		{
			StackItem stackItem = (StackItem) stackItems.get(i);
			NodeSocket nodeSocket = stackItem.getNodeSocket();

			if (nodeSocket != null && nodeSocket.getProcess() == process)
			{
				NodeSocket updatedNodeSocket = EngineUtil.updateSocketReference(nodeSocket, process);
				if (updatedNodeSocket != null)
				{
					stackItem.setNodeSocket(updatedNodeSocket);
				}
				else
				{
					result = false;
				}
			}
		}

		return result;
	}

	//////////////////////////////////////////////////
	// @@ Helper methods
	//////////////////////////////////////////////////

	/**
	 * This method sets a new size of the call stack.
	 * If the new size is larger or equals the current size, the method does just nothing.
	 * In the case of a smaller size, the stack item vector is decreased in size and all
	 * local process variables of the dropped stack frames are removed.
	 *
	 * @param newSize The size the call stack should have after the message has been called
	 */
	private void setStackSize(int newSize)
	{
		// Make sure this is really a decrease in size.
		int size = stackItems.size();
		if (newSize >= size)
			// Nothing to do here...
			return;

		// First, clear the process variables of the current process
		if (tokenContext.getCurrentSocket() != null)
		{
			ProcessItem process = tokenContext.getCurrentSocket().getProcess();
			clearProcessVariables(process, size - 1);
		}

		// Clear the process variables of all processes of the stack items that are purged.
		// Do not include the last one, this holds our current position.
		for (int i = size - 1; i > newSize + 1; --i)
		{
			// Remove all process variables local to this process
			StackItem stackItem = (StackItem) stackItems.get(i);
			ProcessItem process = stackItem.getNodeSocket().getProcess();
			clearProcessVariables(process, i - 1);
		}

		// Basically we just set the size of the stack item vector here.
		if (newSize < 0)
			newSize = 0;
		stackItems.setSize(newSize);

		// Log size of stack.
		if (LogUtil.isDebugEnabled(getClass()))
		{
			LogUtil.debug(getClass(), "Call stack size is now {0}.", Integer.valueOf(stackItems.size() + 1));
		}
	}

	/**
	 * Clears all process variables local to the process denoted by the given call level
	 * as long as this process is not referenced within the current calll stack.
	 *
	 * @param processToClear Process to clear
	 * @param maxLevel Level to clear up to
	 */
	private void clearProcessVariables(ProcessItem processToClear, int maxLevel)
	{
		for (int i = maxLevel; i >= 0; --i)
		{
			// Remove all process variables local to this process
			StackItem stackItem = (StackItem) stackItems.get(i);
			ProcessItem process = stackItem.getNodeSocket().getProcess();

			if (process == processToClear)
				// This process is still referenced in the call stack, so do nothing
				return;
		}

		EngineUtil.clearProcessVariables(processToClear, ProcessVariable.SCOPE_PROCESS, tokenContext);
	}

	//////////////////////////////////////////////////
	// @@ Inner classes
	//////////////////////////////////////////////////

	/**
	 * The base class for items to be hold in the {@link CallStackImpl#stackItems} vector.
	 * Please note that the NodeSocket held by the class maybe an exit or an entry socket,
	 * depending on whether a subprocess or something different is described
	 * by the stack item.
	 */
	private static class StackItem
		implements CallStackItem, Serializable, Cloneable
	{
		private static final long serialVersionUID = 7738785401795416565L;

		/** Type of the stack item */
		private int type;

		// TODO Cleanup 4 Cause should be removed, however take care that deserialization still works... 
		/** Cause of the creation of the item */
		@SuppressWarnings("unused")
		private int cause;

		/** Temporary node socket qualifier (during deserialization only) */
		private transient String tempNodeSocketQualifier;

		/** The node socket payload of the stack item */
		private transient NodeSocket nodeSocket;

		/**
		 * The constructor.
		 *
		 * @param type {@link CallStackItem#TYPE_CONTINUE}/{@link CallStackItem#TYPE_SEARCH}
		 * @param nodeSocket The NodeSocket to be held in the item
		 */
		public StackItem(int type, NodeSocket nodeSocket)
		{
			this.type = type;
			this.nodeSocket = nodeSocket;
		}

		/**
		 * Creates a clone of this object.
		 * @return The base method return value
		 */
		public Object clone()
			throws CloneNotSupportedException
		{
			return super.clone();
		}

		/**
		 * Gets the type of the stack item.
		 * @return {@link CallStackItem#TYPE_CONTINUE}/{@link CallStackItem#TYPE_SEARCH}
		 */
		public int getType()
		{
			return type;
		}

		/**
		 * This method returns the node socket held by this item.
		 *
		 * @return The NodeSocket held by thisstack item
		 */
		public NodeSocket getNodeSocket()
		{
			return nodeSocket;
		}

		/**
		 * This method sets the NodeSocket held by this item.
		 * This is needed during hot code replace.
		 *
		 * @param nodeSocket The new NodeSocket to be held by this
		 */
		public void setNodeSocket(NodeSocket nodeSocket)
		{
			this.nodeSocket = nodeSocket;
		}

		/**
		 * This method is implemented here to support serialization
		 * of the node socket.
		 * See {@link java.io.Serializable} for more information on this method.
		 *
		 * @param in The current object input stream
		 * @throws IOException if an I/O problem occured.
		 * @throws ClassNotFoundException if a class could not be found
		 */
		private void readObject(ObjectInputStream in)
			throws IOException, ClassNotFoundException
		{
			// Do the default read.
			in.defaultReadObject();

			// Recreate the node socket.
			tempNodeSocketQualifier = (String) in.readObject();
		}

		public void resolveNodeSocket(ModelMgr modelMgr)
		{
			if (tempNodeSocketQualifier != null)
			{
				ModelQualifier qualifier = new ModelQualifier(tempNodeSocketQualifier);
				qualifier.setItemType(ItemTypes.PROCESS);
				nodeSocket = EngineUtil.determineNodeSocketFromQualifier(qualifier, modelMgr);
				tempNodeSocketQualifier = null;
			}
		}

		/**
		 * This method is implemented here to support serialization
		 * of the node socket.
		 * See {@link java.io.Serializable} for more information on this method.
		 *
		 * @param out The current object output stream
		 * @throws IOException if an I/O problem occured.
		 */
		private void writeObject(ObjectOutputStream out)
			throws IOException
		{
			// Do the default write.
			out.defaultWriteObject();

			// Write out the node socket's information.
			ModelQualifier qualifier = nodeSocket.getQualifier();
			out.writeObject(qualifier.toString());
		}

		/**
		 * Returns a string representation of this object.
		 * Can be used for logger msgs.
		 * @nowarn
		 */
		public String toString()
		{
			StringBuilder sb = new StringBuilder(32);

			sb.append("type: ");
			switch (type)
			{
			case TYPE_CONTINUE:
				sb.append("continue");
				break;

			case TYPE_SEARCH:
				sb.append("search");
				break;
			}

			sb.append(", socket: ");
			sb.append(nodeSocket.getQualifier());

			return sb.toString();
		}
	}
}
