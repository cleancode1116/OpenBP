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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.openbp.common.logger.LogUtil;

/**
 * This class contains various static utility methods concerning reflections
 * and class processing.
 *
 * @author Heiko Erhardt
 */
public final class ReflectUtil
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Print error message to standard error */
	public static final int CM_PRINT = (1 << 0);

	/** Re-throw any exceptions */
	public static final int CM_THROW = (1 << 1);

	//////////////////////////////////////////////////
	// @@ Private static data
	//////////////////////////////////////////////////

	/**
	 * Do not instantiate this class!
	 */
	private ReflectUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Class instantiation
	//////////////////////////////////////////////////

	/**
	 * Searches for a particular class in a list of packages.
	 *
	 * @param className Unqualified name of the class
	 * @param packageList List of package to search the class in (contains strings)
	 *
	 * @return The class or null if no such class could be found
	 */
	public static Class findClassInPackageList(String className, List packageList)
	{
		return findClassInPackageList(className, packageList, null);
	}

	/**
	 * Searches for a particular class in a list of packages.
	 *
	 * @param className Unqualified name of the class
	 * @param packageList List of package to search the class in (contains strings)
	 * @param cl The class loader which shall load the class or null for the default loader
	 *
	 * @return The class or null if no such class could be found
	 */
	public static Class findClassInPackageList(String className, List packageList, ClassLoader cl)
	{
		if (packageList != null)
		{
			int n = packageList.size();
			for (int i = 0; i < n; ++i)
			{
				String pkg = (String) packageList.get(i);

				Class c = loadClass(pkg + "." + className, cl);
				if (c != null)
					return c;
			}
		}

		return null;
	}

	/**
	 * Loads a class by its fully qualified class name.
	 *
	 * @param className Qualified class name
	 *
	 * @return The class or null if no such class exists
	 */
	public static Class loadClass(String className)
	{
		return loadClass(className, null);
	}

	/**
	 * Loads a class by its fully qualified class name.
	 *
	 * @param className Qualified class name
	 * @param cl The class loader which shall load the class or null for the default loader
	 *
	 * @return The class or null if no such class exists
	 */
	public static Class loadClass(String className, ClassLoader cl)
	{
		try
		{
			if (cl == null)
			{
				// Use the thread loader if no explicit loader defined
				cl = Thread.currentThread().getContextClassLoader();
			}

			if (cl != null)
			{
				return cl.loadClass(className);
			}

			// Final fallback to the standard loader
			return Class.forName(className);
		}
		catch (ClassNotFoundException e)
		{
			return null;
		}
		catch (NoClassDefFoundError e)
		{
			return null;
		}
	}

	/**
	 * Creates an object instance by its fully qualified class name using the default constructor.
	 *
	 * @param className Qualified class name
	 * @param cl The class loader which shall load the class or null for the default loader
	 * @param type If not null, the method will check if the instantiated class matches the given type
	 * @param displayName Display name of the type of object to instantiate for error messages
	 *
	 * @return The new object
	 * @throws ReflectException On any error. The exception message will describe the cause of the error.
	 * The exception will encapsulate the exception that led to the error.
	 */
	public static Object instantiate(String className, ClassLoader cl, Class type, String displayName)
	{
		Class cls = loadClass(className, cl);
		if (cls == null)
		{
			throw new ReflectException(MsgFormat.format("The {0} class $1 could not be found in the class path.", displayName, className));
		}

		return instantiate(cls, type, displayName);
	}

	/**
	 * Creates an object instance given its class using the default constructor.
	 *
	 * @param cls Object class to use for the instantiation
	 * @param type If not null, the method will check if the instantiated class matches the given type
	 * @param displayName Display name of the type of object to instantiate for error messages
	 *
	 * @return The new object
	 * @throws ReflectException On any error. The exception message will describe the cause of the error.
	 * The exception will encapsulate the exception that led to the error.
	 */
	public static Object instantiate(Class cls, Class type, String displayName)
	{
		return instantiate(cls, null, type, displayName);
	}

	/**
	 * Creates an object instance given its class using the default constructor.
	 *
	 * @param cls Object class to use for the instantiation
	 * @param cl The class loader which shall load the class or null for the default loader
	 * @param type If not null, the method will check if the instantiated class matches the given type
	 * @param displayName Display name of the type of object to instantiate for error messages
	 *
	 * @return The new object
	 * @throws ReflectException On any error. The exception message will describe the cause of the error.
	 * The exception will encapsulate the exception that led to the error.
	 */
	public static Object instantiate(Class cls, ClassLoader cl, Class type, String displayName)
	{
		try
		{
			Object o = cls.newInstance();

			if (type != null && !type.isInstance(o))
			{
					throw new ReflectException(MsgFormat.format("The {0} class $1 is not an instance of the interface or class $2.", displayName, getPrintableClassName(cls), type.getName()));
			}

			return o;
		}
		catch (InstantiationException e)
		{
			throw new ReflectException(MsgFormat.format("The {0} class $1 could not be instantiated.", displayName, getPrintableClassName(cls)), e);
		}
		catch (IllegalAccessException e)
		{
			throw new ReflectException(MsgFormat.format("The {0} class $1 could not be instantiated.", displayName, getPrintableClassName(cls)), e);
		}
		catch (Throwable t)
		{
			// Catch any exceptions, so the server won't be confused by errors in actions
			throw new ReflectException(MsgFormat.format("The {0} class $1 could not be initialized.", displayName, getPrintableClassName(cls)), t);
		}
	}

	/**
	 * Creates a class by its fully qualified class name using the default constructor (convenience method).
	 *
	 * @param className Qualified class name
	 * @param type If not null, the method will check if the instantiated class matches the given type
	 * @param displayName Display name of the type of object to instantiate for error messages
	 *
	 * @return The new object
	 * @throws ReflectException On any error. The exception message will describe the cause of the error.
	 * The exception will encapsulate the exception that led to the error.
	 */
	public static Object instantiate(String className, Class type, String displayName)
	{
		return instantiate(className, null, type, displayName);
	}

	/**
	 * Creates an object instance given its class using the constructor with the given argument types.
	 *
	 * @param cls Object class to use for the instantiation
	 * @param argTypes Object classes of the constructor arguments
	 * @param args constructor arguments
	 * @param type If not null, the method will check if the instantiated class matches the given type
	 * @param displayName Display name of the type of object to instantiate for error messages
	 * @param allowDefaultConstructor If true, the default constructor will be used, if no constructor 
	 * with the given argument types could be found. This might result in an error, if the Class has no 
	 * accessible default constructor.
	 *
	 * @return The new object
	 * @throws ReflectException On any error. The exception message will describe the cause of the error.
	 * The exception will encapsulate the exception that led to the error.
	 */
	public static Object instantiateByConstructor(Class cls, Class [] argTypes, Object [] args, Class type,
			String displayName, boolean allowDefaultConstructor)
	{
		Object instance = null;

		try
		{
			Constructor constructor = cls.getConstructor(argTypes);

			/* invoke constructor */
			if (constructor != null)
			{
				/* If a constructor with the given arguments was found */
				instance = constructor.newInstance(args);
			}
			else
			{
				if (!allowDefaultConstructor)
				{
					StringBuffer sb = new StringBuffer();
					for (int indexAT = 0, sizeAT = argTypes.length; indexAT < sizeAT; indexAT++)
					{
						sb.append(ReflectUtil.getPrintableClassName(argTypes [indexAT]));
						if (indexAT < sizeAT - 1)
						{
							sb.append(", ");
						}
					}
					String msg = LogUtil.error(ReflectUtil.class, "Instantiation of {0}({1})failed! ", new String []
					{ ReflectUtil.getPrintableClassName(cls), sb.toString() });
					throw new ReflectException(msg);
				}

				instance = cls.newInstance();
			}
		}
		catch (SecurityException e)
		{
			String msg = LogUtil.error(ReflectUtil.class, e.getMessage(), e);
			throw new ReflectException(msg);
		}
		catch (IllegalArgumentException e)
		{
			String msg = LogUtil.error(ReflectUtil.class, e.getMessage(), e);
			throw new ReflectException(msg);
		}
		catch (NoSuchMethodException e)
		{
			String msg = LogUtil.error(ReflectUtil.class, e.getMessage(), e);
			throw new ReflectException(msg);
		}
		catch (InstantiationException e)
		{
			String msg = LogUtil.error(ReflectUtil.class, e.getMessage(), e);
			throw new ReflectException(msg);
		}
		catch (IllegalAccessException e)
		{
			String msg = LogUtil.error(ReflectUtil.class, e.getMessage(), e);
			throw new ReflectException(msg);
		}
		catch (InvocationTargetException e)
		{
			String msg = LogUtil.error(ReflectUtil.class, e.getMessage(), e);
			throw new ReflectException(msg);
		}

		// Check for correct type
		if (type != null && !type.isInstance(instance))
		{
			throw new ReflectException(MsgFormat.format(
					"The {0} class $1 is not an instance of the interface or class $2.", displayName,
					getPrintableClassName(cls), type.getName()));
		}

		return instance;
	}


	//////////////////////////////////////////////////
	// @@ Method invocation
	//////////////////////////////////////////////////

	/**
	 * Calls a method using Java reflection.
	 *
	 * @param o Object to invoke the method upon
	 * @param methodName Name of the method
	 * @param argClasses Argument classes or null
	 * @param argValues Argument values or null
	 * @param errorHandlingMode Mode that describes how errors are to be handled.<br>
	 * (any combination of {@link #CM_PRINT} | {@link #CM_THROW})
	 * @return The return value of the method call
	 * @throws Exception If an error occurred and errorHandlingMode was set to CM_THROW
	 */
	public static Object callMethod(Object o, String methodName, Class [] argClasses, Object [] argValues, int errorHandlingMode)
		throws Exception
	{
		if (o == null)
		{
			if ((errorHandlingMode & CM_PRINT) != 0)
			{
				System.err.println("Cannot call method '" + methodName + "' on null object");
			}
			if ((errorHandlingMode & CM_THROW) != 0)
			{
				throw new IllegalArgumentException("Cannot call method '" + methodName + "' on null object");
			}
			return null;
		}

		Class cls = o.getClass();

		Method method = null;
		try
		{
			method = cls.getMethod(methodName, argClasses);
		}
		catch (NoSuchMethodException e)
		{
			if ((errorHandlingMode & CM_PRINT) != 0)
			{
				System.err.println("Cannot find method '" + methodName + "' in class '" + cls.getName() + "'");
			}
			if ((errorHandlingMode & CM_THROW) != 0)
				throw e;
			return null;
		}

		return callMethod(o, method, argValues, errorHandlingMode);
	}

	/**
	 * Calls a method using Java reflection.
	 *
	 * @param o Object to invoke the method upon
	 * @param method Method to invoke
	 * @param argValues Argument values or null
	 * @param errorHandlingMode Mode that describes how errors are to be handled.<br>
	 * (any combination of {@link #CM_PRINT} | {@link #CM_THROW})
	 * @return The return value of the method call
	 * @throws Exception If an error occurred and errorHandlingMode was set to CM_THROW
	 */
	public static Object callMethod(Object o, Method method, Object [] argValues, int errorHandlingMode)
		throws Exception
	{
		Class cls = o.getClass();

		Object result;
		try
		{
			result = method.invoke(o, argValues);
		}
		catch (IllegalAccessException e)
		{
			if ((errorHandlingMode & CM_PRINT) != 0)
			{
				System.err.println("Cannot access method '" + method.getName() + "' in class '" + cls.getName() + "'");
			}
			if ((errorHandlingMode & CM_THROW) != 0)
				throw e;
			return null;
		}
		catch (IllegalArgumentException e)
		{
			if ((errorHandlingMode & CM_PRINT) != 0)
			{
				System.err.println("Invalid arguments to method '" + method.getName() + "' in class '" + cls.getName() + "'");
			}
			if ((errorHandlingMode & CM_THROW) != 0)
				throw e;
			return null;
		}
		catch (InvocationTargetException e)
		{
			if ((errorHandlingMode & CM_PRINT) != 0)
			{
				System.err.println("Exception in method '" + method.getName() + "' in class '" + cls.getName() + "': " + ExceptionUtil.getNestedTrace(e));
			}
			if ((errorHandlingMode & CM_THROW) != 0)
				throw e;
			return null;
		}

		return result;
	}

	/**
	 * Creates a string that specifies the method signature containing a fullly qualified method name.
	 *
	 * @param method Method
	 * @return String in the form class.method(argclass1,argclass2,...), e. g.<br>
	 * "org.openbp.common.ReflectUtil.callMethod(java.lang.Object,java.lang.reflect.Method,java.lang.Object[],int)"
	 */
	public static String getQualifiedMethodSignature(Method method)
	{
		return method.getDeclaringClass().getName() + '.' + getUnqualifiedMethodSignature(method);
	}

	/**
	 * Creates a string that specifies the method signature containing an unqualified method name.
	 *
	 * @param method Method
	 * @return String in the form class.method(argclass1,argclass2,...), e. g.<br>
	 * "org.openbp.common.ReflectUtil.callMethod(java.lang.Object,java.lang.reflect.Method,java.lang.Object[],int)"
	 */
	public static String getUnqualifiedMethodSignature(Method method)
	{
		StringBuffer sb = new StringBuffer();

		sb.append(method.getName());
		sb.append('(');

		Class [] params = method.getParameterTypes();
		for (int i = 0; i < params.length; ++i)
		{
			if (i > 0)
				sb.append(',');
			sb.append(getPrintableClassName(params [i]));
		}

		sb.append(')');

		return sb.toString();
	}

	/**
	 * Finds a method according to its signature.
	 *
	 * @param signature Signature as generated by {@link #getQualifiedMethodSignature}
	 * @return The method
	 * @throws NoSuchMethodException If no matching method was found
	 * @throws ClassNotFoundException If an argument class could not be loaded by the thread or standard class loader
	 */
	public static Method findByQualifiedMethodSignature(String signature)
		throws NoSuchMethodException, ClassNotFoundException
	{
		int iOpar = signature.indexOf('(');
		int iCpar = signature.indexOf(')');
		if (iOpar < 0 || iCpar < 0)
			throw new NoSuchMethodException("Invalid method signature: " + signature);
		String classAndMethodName = signature.substring(0, iOpar);
		String argList = signature.substring(iOpar, iCpar + 1);

		int i = classAndMethodName.lastIndexOf('.');
		String objectClassName = classAndMethodName.substring(0, i);
		Class objectClass = loadClass(objectClassName);

		String unqualifiedSignature = classAndMethodName.substring(i + 1) + argList;

		return findByUnqualifiedMethodSignature(objectClass, unqualifiedSignature);
	}

	/**
	 * Finds a method according to its signature.
	 *
	 * @param objectClass Class to search for the method
	 * @param signature Signature as generated by {@link #getQualifiedMethodSignature}
	 * @return The method
	 * @throws NoSuchMethodException If no matching method was found
	 * @throws ClassNotFoundException If an argument class could not be loaded by the thread or standard class loader
	 */
	public static Method findByUnqualifiedMethodSignature(Class objectClass, String signature)
		throws NoSuchMethodException, ClassNotFoundException
	{
		int iOpar = signature.indexOf('(');
		int iCpar = signature.indexOf(')');
		if (iOpar < 0 || iCpar < 0)
			throw new NoSuchMethodException("Invalid method signature: " + signature);
		String methodName = signature.substring(0, iOpar);
		String argClassNames = signature.substring(iOpar + 1, iCpar);

		ArrayList args = new ArrayList();

		StringTokenizer st = new StringTokenizer(argClassNames, ",");
		while (st.hasMoreTokens())
		{
			String argClassName = st.nextToken();

			Class argClass = determineClass(argClassName);
			if (argClass == null)
			{
				throw new ClassNotFoundException("Argument class " + argClassName + " not found.");
			}

			args.add(argClass);
		}

		Class [] argClasses = (Class []) CollectionUtil.toArray(args, Class.class);

		Method m = objectClass.getMethod(methodName, argClasses);
		return m;
	}

	/**
	 * Gets a printable form of a class name.
	 *
	 * By default, array class names are printed like "Lclass;".
	 * This looks awful. Therefore, we format them a little prettier,
	 * like "class[]".
	 *
	 * @param cls The class to get the name for
	 * @return Formatted class name
	 */
	public static String getPrintableClassName(Class cls)
	{
		if (cls == null)
			return "null";

		if (cls.isArray())
		{
			Class componentClass = cls.getComponentType();
			return getPrintableClassName(componentClass) + "[]";
		}

		return cls.getName();
	}

	/**
	 * Determines the class object by type string.
	 *
	 * @param stringRepresentation Example: java.lang.String or java.lang.String[] or int
	 *
	 * @return The class
	 */
	public static Class determineClass(String stringRepresentation)
	{
		String array = "[]";
		boolean isArray = stringRepresentation.endsWith(array);
		if (isArray)
		{
			stringRepresentation = stringRepresentation.substring(0, stringRepresentation.indexOf(array)).trim();
		}

		Class c = null;

		if ("int".equals(stringRepresentation))
			c = Integer.TYPE;
		else if ("long".equals(stringRepresentation))
			c = Long.TYPE;
		else if ("short".equals(stringRepresentation))
			c = Short.TYPE;
		else if ("byte".equals(stringRepresentation))
			c = Byte.TYPE;
		else if ("boolean".equals(stringRepresentation))
			c = Boolean.TYPE;
		else if ("char".equals(stringRepresentation))
			c = Character.TYPE;
		else if ("float".equals(stringRepresentation))
			c = Float.TYPE;
		else if ("double".equals(stringRepresentation))
			c = Double.TYPE;
		else
		{
			c = loadClass(stringRepresentation);
			if (c == null)
				return null;
		}

		if (isArray)
		{
			c = Array.newInstance(c, 0).getClass();
		}

		return c;
	}

	//////////////////////////////////////////////////
	// @@ Convenience wrappers around some reflection methods
	//////////////////////////////////////////////////

	/**
	 * Get a method using reflections. Does not throw an exception if it's not there.
	 *
	 * @param cls Class
	 * @param name Name of the method to locate
	 * @param args Arguments of the method signature looking for
	 * @return The method or null if the specified method does not exist
	 */
	public static Method getMethodOrNull(Class cls, String name, Class [] args)
	{
		try
		{
			return cls.getMethod(name, args);
		}
		catch (NoSuchMethodException nsme)
		{
			return null;
		}
	}

	/**
	 * Get a field using reflections. Does not throw an exception if it's not there.
	 *
	 * @param cls Class
	 * @param name Name of the field to locate
	 * @return The field or null if the specified field does not exist
	 */
	public static Field getFieldOrNull(Class cls, String name)
	{
		try
		{
			return cls.getField(name);
		}
		catch (NoSuchFieldException nsme)
		{
			return null;
		}
	}
}
