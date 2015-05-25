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
package org.openbp.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.openbp.common.string.StringUtil;

/**
 * Static exception utiltity methods.
 * The methods of this class can be used to obtain exception messages and stack traces.
 * The account especially for nested exceptions, i. e. exceptions that are held not only
 * as exception cause (see {@link java.lang.Throwable#getCause}), but may also be returned
 * by one of the following methods, which will be accessed using reflections:<br>
 * getException<br>
 * getTargetException<br>
 * getNestedException
 *
 * @author Heiko Erhardt
 */
public final class ExceptionUtil
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private ExceptionUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Static methods
	//////////////////////////////////////////////////

	/**
	 * Gets the exception message of the throwable plus any additional message
	 * of exceptions nested by this throwable.
	 * The method will consider not only the exception cause when printing nested
	 * exceptions, but also exceptions encapsulated by the methods mentioned in
	 * the comment of this class.<br>
	 * The method ensures that the returned string does not contain duplicate messages.
	 *
	 * @param t Throwable object
	 * @return The message or null if there is none
	 */
	public static String getNestedMessage(Throwable t)
	{
		ArrayList msgs = new ArrayList();

		// Collect the exception messages
		while (t != null)
		{
			String msg = StringUtil.trimNull(t.getMessage());
			if (msg != null)
			{
				int i = msg.indexOf("; nested exception is");
				if (i >= 0)
				{
					msg = msg.substring(0, i);
				}

				if (t instanceof ClassNotFoundException)
				{
					msg = "Class not found: " + msg;
				}

				// Add message if not already present
				if (!msgs.contains(msg))
					msgs.add(msg);
			}

			t = tryNestedException(t);
		}

		int n = msgs.size();
		if (n == 0)
			return null;
		if (n == 1)
			return (String) msgs.get(0);

		// Concatenate the exception text
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < n; ++i)
		{
			String msg = (String) msgs.get(i);
			if (i > 0)
				sb.append("\n\n");
			sb.append(msg);
		}

		return sb.toString();
	}

	/**
	 * Gets the stack trace of the throwable plus the stack traces
	 * of exceptions nested by this throwable.
	 * This looks exactly like the output of the toString method of a Throwable,
	 * however, it does not only consider the exception cause when printing nested
	 * exceptions, but also exceptions encapsulated by the methods mentioned in
	 * the comment of this class.
	 *
	 * @param throwable Throwable object
	 * @return The message or null if there is none
	 */
	public static String getNestedTrace(Throwable throwable)
	{
		StringBuffer sb = new StringBuffer();

		// Collect the stack traces messages
		Throwable cause = null;
		for (Throwable t = throwable; t != null; t = tryNestedException(t))
		{
			if (t != throwable)
			{
				sb.append("\n\tcaused by: ");
			}

			// Class name and exception message
			sb.append(t.getClass().getName());
			String msg = StringUtil.trimNull(t.getMessage());
			if (msg != null)
			{
				sb.append(": ");
				sb.append(msg);
			}

			cause = t;
		}

		if (cause != null)
		{
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			cause.printStackTrace(pw);
			String trace = sw.getBuffer().toString();

			int i = trace.indexOf('\t');
			if (i > 0)
			{
				// Cut message before stack trace (we already collected the messages above)
				trace = trace.substring(i);
			}
			sb.append("\n");
			sb.append(trace);
		}

		return sb.toString();
	}

	/**
	 * Gets a list containing the given throwable and all its nested exceptions.
	 *
	 * @param throwable Throwable object
	 * @return A list of {@link java.lang.Throwable} objects.<br>
	 * The list will contain each exception in the exception chain only one.
	 */
	public static List getNestedExceptions(Throwable throwable)
	{
		ArrayList list = new ArrayList();

		// Collect the stack traces messages
		for (Throwable t = throwable; t != null; t = tryNestedException(t))
		{
			if (list.contains(t))
				break;

			list.add(t);
		}

		return list;
	}

	/**
	 * Gets the target excpeption of a Throwable object or the object itself.
	 *
	 * @param t Throwable object
	 * @return The last exception of the chain of target exceptions if the Throwable
	 * contains a nested exception (i. e. a method named getException(), getTargetException ()
	 * or getNestedException() or the Throwable object itself otherwise
	 */
	public static Throwable getNestedException(Throwable t)
	{
		for (;;)
		{
			Throwable nestedException = tryNestedException(t);
			if (nestedException == null)
				break;

			t = nestedException;
		}
		return t;
	}

	/**
	 * Recurses the exception chain down until the specified exception.
	 * This method can be used to determine if a particular exception is part of
	 * the exception chain.
	 *
	 * @param t Throwable object
	 * @param className Fully qualified class name of the exception to search
	 * @return The specified exception or null if the exception chain does not contain
	 * such an exception.
	 */
	public static Throwable findNestedException(Throwable t, String className)
	{
		while (t != null)
		{
			if (t.getClass().getName().equals(className))
				return t;

			t = tryNestedException(t);
		}
		return t;
	}

	/**
	 * Prints a nested stack trace of the given throwable to stderr.
	 *
	 * @param t Exception to print
	 */
	public static void printTrace(Throwable t)
	{
		printTrace(null, t);
	}

	/**
	 * Prints a nested stack trace of the given throwable to stderr.
	 *
	 * @param msg Additional explaining message that should preceede the stack trace or null
	 * @param t Exception to print
	 */
	public static void printTrace(String msg, Throwable t)
	{
		if (msg != null)
		{
			System.err.print(msg);
			System.err.println();
		}

		if (t != null)
		{
			String trace = getNestedTrace(t);
			System.err.println(trace);
		}
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Tries to retrieve a nested exception from a Throwable object by trying various
	 * access methods using reflection on the Throwable.
	 *
	 * @param t Throwable object
	 * @return The nested exception or null if the throwable does not contain a nested exception
	 */
	private static Throwable tryNestedException(Throwable t)
	{
		Throwable nestedException = tryNestedException(t, "getException");

		if (nestedException == null)
			nestedException = tryNestedException(t, "getTargetException");

		if (nestedException == null)
			nestedException = tryNestedException(t, "getNestedException");

		if (nestedException == null)
			nestedException = tryNestedException(t, "getRootCause");

		if (nestedException == null)
			nestedException = tryNestedException(t, "getCause");

		return nestedException;
	}

	/**
	 * Tries to retrieve a nested exception from a Throwable object using by trying
	 * the supplied method using reflection on the Throwable.
	 *
	 * @param t Throwable object
	 * @param methodName Name of the method to call
	 * @return The nested exception or null if no such method exists in the Throwable
	 * or the return value is not a Throwable itself.
	 */
	private static Throwable tryNestedException(Throwable t, String methodName)
	{
		try
		{
			// Regular attribute, look for set<Name>(<type>)
			Method method = t.getClass().getMethod(methodName, (Class[]) null);
			Object ret = method.invoke(t, new Object [] {});
			if (ret instanceof Throwable && ret != t)
				return (Throwable) ret;
		}
		catch (Exception e)
		{
			// Ignored
		}
		return null;
	}

	/**
	 * Extracts a runtime exception from the given exception.
	 * If the exception or it's direct cause is not a runtime exception, it will be wrapped into a new RuntimeException.
	 *
	 * @param e Exception to inspect
	 * @return The runtime exception
	 */
	public static RuntimeException extractRuntimeException(Throwable e)
	{
		RuntimeException re = null;
		if (e instanceof RuntimeException)
		{
			re = (RuntimeException) e;
		}
		else
		{
			if ((e instanceof RemoteException) && e.getCause() instanceof RuntimeException)
				re = (RuntimeException) e.getCause();
		}
		if (re == null)
		{
			if (e instanceof RuntimeException && e.getCause() != null)
			{
				re = new RuntimeException(e.getCause());
			}
			else
			{
				re = new RuntimeException(e);
			}
		}
		return re;
	}
}
