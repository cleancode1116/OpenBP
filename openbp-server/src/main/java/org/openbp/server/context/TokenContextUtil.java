package org.openbp.server.context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Map;

import org.openbp.common.CommonRegistry;
import org.openbp.common.classloader.ClassLoaderObjectInputStream;
import org.openbp.common.logger.LogUtil;
import org.openbp.core.CoreConstants;
import org.openbp.core.engine.EngineException;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.Param;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessVariable;
import org.openbp.server.context.serializer.ContextObjectSerializerRegistry;
import org.openbp.server.engine.EngineTraceException;
import org.openbp.server.engine.EngineUtil;

/**
 * Token context util.
 *
 * @author Heiko Erhardt
 */
public final class TokenContextUtil
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private TokenContextUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	/**
	 * Checks if the specified parameter exists.
	 *
	 * @param token Token context
	 * @param param Parameter
	 * @nowarn
	 */
	public static boolean hasParamValue(TokenContext token, Param param)
	{
		if (param instanceof ProcessVariable)
		{
			return token.hasProcessVariableValue(param.getName());
		}
		return token.hasParamValue(param.getContextName());
	}

	/**
	 * Checks if the specified node parameter exists.
	 *
	 * @param token Token context
	 * @param socket Socket
	 * @param paramName Unqualified parameter name ("paramName")
	 * @nowarn
	 */
	public static boolean hasParamValue(TokenContext token, NodeSocket socket, String paramName)
	{
		Param param = socket.getParamByName(paramName);
		if (param != null)
		{
			return token.hasParamValue(param.getContextName());
		}
		return false;
	}

	/**
	 * Retrieves the value of the specified parameter.
	 *
	 * @param token Token context
	 * @param param Parameter
	 * @return The parameter value or null if no such parameter exists
	 */
	public static Object getParamValue(TokenContext token, Param param)
	{
		if (param instanceof ProcessVariable)
		{
			return token.getProcessVariableValue(param.getName());
		}
		return token.getParamValue(param.getContextName());
	}

	/**
	 * Retrieves the value of the specified node parameter.
	 *
	 * @param token Token context
	 * @param socket Socket
	 * @param paramName Unqualified parameter name ("paramName")
	 * @return The parameter value or null if no such parameter exists
	 */
	public static Object getParamValue(TokenContext token, NodeSocket socket, String paramName)
	{
		Param param = socket.getParamByName(paramName);
		if (param != null)
		{
			return token.getParamValue(param.getContextName());
		}
		return null;
	}

	/**
	 * Sets the value of the specified parameter.
	 *
	 * @param token Token context
	 * @param param Parameter
	 * @param value Param value
	 */
	public static void setParamValue(TokenContext token, Param param, Object value)
	{
		if (param instanceof ProcessVariable)
		{
			TokenContext context = token;
			ProcessVariable pv = (ProcessVariable) param;
			if (pv.isRootContextVariable())
			{
				context = TokenContextUtil.getRootContext(token);
			}
			context.setProcessVariableValue(param.getName(), value);
		}
		else
		{
			token.setParamValue(param.getContextName(), value);
		}
	}

	/**
	 * Sets the value of the specified node parameter.
	 *
	 * @param token Token context
	 * @param socket Socket
	 * @param paramName Unqualified parameter name ("paramName")
	 * @param value Param value
	 */
	public static void setParamValue(TokenContext token, NodeSocket socket, String paramName, Object value)
	{
		Param param = socket.getParamByName(paramName);
		if (param != null)
		{
			setParamValue(token, param, value);
		}
	}

	/**
	 * Removes the specified parameter value.
	 *
	 * @param token Token context
	 * @param param Parameter
	 */
	public static void removeParamValue(TokenContext token, Param param)
	{
		if (param instanceof ProcessVariable)
		{
			token.removeProcessVariableValue(param.getName());
		}
		token.removeParamValue(param.getContextName());
	}

	/**
	 * Removes the specified node parameter value.
	 *
	 * @param token Token context
	 * @param socket Socket
	 * @param paramName Unqualified parameter name ("paramName")
	 */
	public static void removeParamValue(TokenContext token, NodeSocket socket, String paramName)
	{
		Param param = socket.getParamByName(paramName);
		if (param != null)
		{
			token.removeParamValue(param.getContextName());
		}
	}

	/**
	 * Gets the root context of the context hierarchy this context is in.
	 *
	 * @param context Token context
	 * @return The root context (may be the given context itselft)
	 */
	public static TokenContext getRootContext(TokenContext context)
	{
		TokenContext rc = context;
		while (rc.getParentContext() != null)
		{
			rc = rc.getParentContext();
		}
		return rc;
	}

	/**
	 * This method checks, whether the given process is currently executing.
	 * 
	 * @param context Token context
	 * @param processToSearch
	 *            Process to look up in the call stack
	 * @return true The given process or one of its sub processes is currently
	 *         executing.\n false Otherwise
	 */
	public static boolean isProcessExecuting(TokenContext context, ProcessItem processToSearch)
	{
		if (context.getCurrentSocket() != null)
		{
			if (context.getCurrentSocket().getProcess() == processToSearch)
				// This process is the current process
				return true;
		}

		// Not the current process, but let's check the call stack...
		return context.getCallStack().isProcessExecuting(processToSearch);
	}

	/**
	 * Checks if the given identifier denotes a process variable.
	 *
	 * @param name Name to check
	 * @return true for a process variable, false for a parameter value
	 */
	public static boolean isProcessVariableIdentifier(String name)
	{
		return name != null && name.length() > 0 && name.startsWith(CoreConstants.PROCESS_VARIABLE_INDICATOR);
	}

	//////////////////////////////////////////////////
	// @@ Debugging support
	//////////////////////////////////////////////////

	/** Internal runtime attribute */
	public static final String TERMINATION_REQUESTED = "_TerminationRequested";

	/**
	 * Checks if there is a termination request flag set for the given token context and throws an EngineException if so.
	 * @param context Token context
	 */
	public static void checkTerminationRequest(TokenContext context)
		throws EngineTraceException
	{
		if (context.getRuntimeAttribute(TERMINATION_REQUESTED) != null)
		{
			// The thread being interrupted means that probably a debugger has
			// killed the process
			// Generation an engine trace exception
			resetTerminationRequest(context);
			throw new EngineTraceException("Thread killed by debugger");
		}
	}

	/**
	 * Resets the termination request flag set for the given token context.
	 * @param context Token context
	 */
	public static void resetTerminationRequest(TokenContext context)
	{
		context.removeRuntimeAttribute(TERMINATION_REQUESTED);
	}

	/**
	 * Sets the termination request flag set for the given token context and all of its child contexts.
	 * @param context Token context
	 */
	public static void requestTermination(TokenContext context)
	{
		context.setRuntimeAttribute(TERMINATION_REQUESTED, Boolean.TRUE);
		for (Iterator it = context.getChildContexts(); it.hasNext();)
		{
			TokenContext cc = (TokenContext) it.next();
			requestTermination(cc);
		}
	}

	//////////////////////////////////////////////////
	// @@ Serialization support and cloning
	//////////////////////////////////////////////////

	public static final long SERIAL_BASE = 423490238923492892L;
	public static final long SERIAL_LATEST = 0L;

	/**
	 * This method is implemented here to support serialization of the node
	 * socket. See {@link java.io.Serializable} java.io.Serializable for more information on this method.
	 * @param context Token context
	 * @param out The current object output stream
	 * @throws IOException if an I/O problem occured.
	 */
	private static void writeObject(TokenContext context, ObjectOutputStream out)
		throws IOException
	{
		ContextObjectSerializerRegistry serializerRegistry = (ContextObjectSerializerRegistry) CommonRegistry
			.lookup(ContextObjectSerializerRegistry.class);

		// We need to control the members that are serialized ourself instead
		// of calling out.defaultWriteObject ();

		// First, write out a serial number
		out.writeObject(new Long(SERIAL_BASE + SERIAL_LATEST));

		// Write the call stack
		out.writeObject(context.getCallStack());

		// TODO Fix 2: Complex objects that occur more than once in the context parameters will be serialized as different instances

		// Parameter values
		Map pv = context.getParamValues();
		for (Iterator keys = pv.keySet().iterator(); keys.hasNext();)
		{
			String key = (String) keys.next();

			TokenContextValue tcv = (TokenContextValue) pv.get(key);
			if (! tcv.isPersistentVariable())
				continue;

			Object value = null;

			try
			{
				out.writeObject(key);

				value = tcv.getValue();
				serializerRegistry.writeObjectToOutputStream(value, out, context, key);
			}
			catch (Throwable t)
			{
				// Obviously, this is one of the famous serializable objects that are not serializable.
				String className = value != null ? value.getClass().getName() : "<unknown>";
				String msg = LogUtil
					.error(
						context.getClass(),
						"Error serializing token context: Serialization of object of type $0 (key $1) failed though object implements the Serializable interface.",
						className, key, t);
				throw new EngineException("ContextSerialization", msg, t);
			}
		}

		// Write a stop signal (telling the reader that the key/value pair
		// stream ends here.
		out.writeObject(null);

		// Write some more objects (reserved for future use)
		out.writeObject(null);
		out.writeObject(null);
		out.writeObject(null);
		out.writeObject(null);
		out.writeObject(null);
		out.writeObject(null);
		out.writeObject(null);
		out.writeObject(null);
		out.writeObject(null);
		out.writeObject(null);
	}

	/**
	 * This method is implemented here to support serialization of the object.
	 * See {@link java.io.Serializable} java.io.Serializable for more information on this method.
	 * @param context Token context
	 * @param in The current object input stream\n Note that this should be an
	 * {@link ClassLoaderObjectInputStream} in order to be able to access classes
	 * using the executing model's class loader.
	 * @throws IOException if an I/O problem occured
	 * @throws ClassNotFoundException if a class could not be found
	 */
	private static void readObject(TokenContext context, ObjectInputStream in)
		throws IOException, ClassNotFoundException
	{
		// We need to control the members that are serialized ourself instead
		// of calling in.defaultReadObject ();

		if (in instanceof ClassLoaderObjectInputStream && context.getExecutingModel() != null)
		{
			// Use the executing model's class loader for instantiating deserialized objects
			((ClassLoaderObjectInputStream) in).setClassLoader(context.getExecutingModel().getClassLoader());
		}

		// First, write out a serial number
		Object initialObject = in.readObject();
		if (initialObject instanceof Long)
		{
			long serialValue = ((Long) initialObject).longValue();
			long serialVersion = serialValue - SERIAL_BASE;
			if (serialVersion >= 0 && serialVersion <= SERIAL_LATEST)
			{
				// Perform the actual read according to the serial number
				if (serialVersion == 0L)
				{
					readObject0(context, in);
				}
				else
				{
					String msg = LogUtil.error(context.getClass(), "Trying to deserialize a token with an unknown serial version number (level: $0). [{1}]", new Long(serialVersion), context);
					throw new EngineException("ContextDeserialization", msg);
				}
			}
		}
		else
		{
			// Perform the 'legacy' read
			readObjectLegacy(context, in, initialObject);
		}
	}

	private static void readObject0(TokenContext context, ObjectInputStream in)
		throws IOException, ClassNotFoundException
	{
		ContextObjectSerializerRegistry serializerRegistry = (ContextObjectSerializerRegistry) CommonRegistry
			.lookup(ContextObjectSerializerRegistry.class);

		CallStack callStack = (CallStack) in.readObject();
		if (callStack != null)
		{
			callStack.setTokenContext(context);
			((CallStackImpl) callStack).initializeAfterDeserialization(context);
		}
		context.setCallStack(callStack);

		for (;;)
		{
			String key = (String) in.readObject();
			if (key == null)
			{
				// End of parameters
				break;
			}

			if (isProcessVariableIdentifier(key))
			{
				context.createProcessVariable(key.substring(1), true);
			}

			Object value = serializerRegistry.readObjectFromInputStream(in, context, key);
			context.setParamValue(key, value);
		}
	}

	private static void readObjectLegacy(TokenContext context, ObjectInputStream in, Object initialObject)
		throws IOException, ClassNotFoundException
	{
		ContextObjectSerializerRegistry serializerRegistry = (ContextObjectSerializerRegistry) CommonRegistry
			.lookup(ContextObjectSerializerRegistry.class);

		if (initialObject != null && ! (initialObject instanceof CallStack))
		{
			String msg = LogUtil.error(context.getClass(), "Trying to deserialize a token and found an invalid initial object $0. [{1}]", initialObject, context);
			throw new EngineException("ContextDeserialization", msg);
		}
		CallStack callStack = (CallStack) initialObject;
		if (callStack != null)
		{
			callStack.setTokenContext(context);
			((CallStackImpl) callStack).initializeAfterDeserialization(context);
		}
		context.setCallStack(callStack);

		for (;;)
		{
			String key = (String) in.readObject();
			if (key == null)
			{
				// End of parameters
				break;
			}

			if (isProcessVariableIdentifier(key))
			{
				context.createProcessVariable(key.substring(1), true);
			}

			Object value = serializerRegistry.readObjectFromInputStream(in, context, key);
			context.setParamValue(key, value);
		}
	}

	/**
	 * This method converts this context into a byte array.
	 * 
	 * @param context Token context
	 * @return The hibernated context
	 */
	public static byte[] toByteArray(TokenContext context)
	{
		LogUtil.debug(context.getClass(), "Serializing context $0.", context);

		try
		{
			// Prepare in-memory stream
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);

			// Perform the actual serialization.
			writeObject(context, oos);

			oos.flush();
			oos.close();

			// Return the result bytes
			byte[] ret = bos.toByteArray();
			return ret;
		}
		catch (IOException ioe)
		{
			// We've done in-memory I/O, so we hardly except it. Anyway: log it.
			LogUtil.error(context.getClass(), "TokenContextImpl.toByteArray: Encountered I/O problem. [{0}]", context, ioe);

			// Nothing meaningful can be returned here.
			return null;
		}
	}

	/**
	 * Deserializes the token context from the given byte array.
	 * 
	 * @param context Token context
	 * @param bytes The hibernated context
	 */
	public static void fromByteArray(TokenContext context, byte[] bytes)
	{
		context.clearParamValues();
		if (context.getCurrentSocket() != null)
		{
			EngineUtil.createProcessVariables(context.getCurrentSocket().getProcess(), context);
		}

		try
		{
			// Setup an object input stream.
			// TODO FIXME 6 At this point in time, we do not have a reference to the executing unit,
			// so we cannot provide a ClassLoaderObjectInputStream that references the executing unit's class loader :(
			// ObjectInputStream ois = new ClassLoaderObjectInputStream(new ByteArrayInputStream(bytes));
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));

			// Create a new token context and deserialize it from the stream
			readObject(context, ois);

			LogUtil.debug(context.getClass(), "Deserialized context $0.", context);
		}
		catch (ClassNotFoundException e)
		{
			// This is unlikely
			String msg = LogUtil.error(context.getClass(), "Could not find class when deserializing token context value. [{0}]", context, e);
			throw new EngineException("ContextDeserialization", msg, e);
		}
		catch (IOException e)
		{
			// We've done in-memory I/O, so we hardly except it. Anyway: log it.
			String msg = LogUtil.error(context.getClass(), "Encountered I/O problem when deserializing token context value. [{0}]", context, e);
			throw new EngineException("ContextDeserialization", msg, e);
		}
	}
}
