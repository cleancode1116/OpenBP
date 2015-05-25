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
package org.openbp.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.classloader.ClassLoaderObjectInputStream;
import org.openbp.common.generic.Copyable;

/**
 * Various static utility methods for copying objects.
 * All based on usage of the Clonable interface.
 *
 * @author Heiko Erhardt
 */
public final class CopyUtil
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/**
	 * {@link #copyCollection}/{@link #copyMap} copy mode: No copy, reference only.
	 * The new map will refer to the same objects as the original.
	 */
	public static final int CLONE_NONE = 0;

	/**
	 * {@link #copyMap} copy mode: Copy keys.
	 * The new map will contain deep clones of the original.
	 */
	public static final int CLONE_KEYS = (1 << 0);

	/**
	 * {@link #copyCollection}/{@link #copyMap} copy mode: Copy values.
	 * The new map will contain deep clones of the original.
	 */
	public static final int CLONE_VALUES = (1 << 1);

	/**
	 * {@link #copyCollection}/{@link #copyMap} copy mode: Copy keys and values.
	 * The new map will contain deep clones of the original.
	 */
	public static final int CLONE_ALL = (CLONE_KEYS | CLONE_VALUES);

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private CopyUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Copy methods
	//////////////////////////////////////////////////

	/**
	 * Returns a new of the same type as the given object.
	 *
	 * @param object Object to create an empty clone for or null
	 * @return The new object or null if object is null
	 * @throws CloneNotSupportedException On error
	 */
	public static Object createNew(Object object)
		throws CloneNotSupportedException
	{
		if (object != null)
		{
			Class cls = object.getClass();
			try
			{
				return cls.newInstance();
			}
			catch (IllegalAccessException e)
			{
				throw new CloneNotSupportedException("Error creating new object of type '" + cls.getName() + "' (access): " + e.getMessage());
			}
			catch (InstantiationException e)
			{
				throw new CloneNotSupportedException("Error creating new object of type '" + cls.getName() + "' (instantiation): " + e.getMessage());
			}
		}

		return null;
	}

	/**
	 * Creates a copy of an arbitrary object if it supports the clone interface.
	 * This is to work around the fact the Object.clone method is a protected method.
	 * The protected access mode will be usually overridden as a public one by objects
	 * that implement the method.
	 *
	 * @param object The object to copy or null
	 *
	 * @param copyMode Determines if a deep copy, a first level copy or a shallow copy is to be
	 * performed ({@link Copyable#COPY_SHALLOW}/{@link Copyable#COPY_FIRST_LEVEL}/{@link Copyable#COPY_DEEP}).<br>
	 * Note that this can be accounted for only if the object implements the {@link Copyable} interface.
	 * If COPY_DEEP is specified, we will use the object's clone method (is is defined to use
	 * deep copy mode). Otherwise, we try to construct a new object using the default constructor
	 * and apply the {@link Copyable#copyFrom} method on it.<br>
	 * If the object does not implement Copyable, the method will try to use the object's
	 * clone method. The clone method will be called using the Java reflection API.
	 * @param classLoader Class loader if the serialize/deserialize approach (last fallback) will be used
	 *
	 * @return The copied object or null if the object itself is null
	 * @throws CloneNotSupportedException If the object cannot be cloned
	 */
	public static Object copyObject(Object object, int copyMode, ClassLoader classLoader)
		throws CloneNotSupportedException
	{
		if (object == null)
			return null;

		Object newObject = null;
		if (object instanceof Copyable)
		{
			if (copyMode == Copyable.COPY_DEEP)
			{
				// Copyable.clone is defined to use deep copy mode
				newObject = ((Copyable) object).clone();
			}
			else
			{
				try
				{
					newObject = object.getClass().newInstance();
				}
				catch (InstantiationException e)
				{
					ExceptionUtil.printTrace(e);
					throw new CloneNotSupportedException("Cannot clone object of type '" + object.getClass().getName() + "'. No default constructor available.");
				}
				catch (IllegalAccessException e)
				{
					ExceptionUtil.printTrace(e);
					throw new CloneNotSupportedException("Cannot clone object of type '" + object.getClass().getName() + "'. Cannot access default constructor.");
				}
				((Copyable) newObject).copyFrom(object, copyMode);
			}
		}
		else if (object instanceof Cloneable)
		{
			// clone() is a protected method by default and will be overridden
			// as a public one by objects that implement the method.
			try
			{
				Method method = object.getClass().getMethod("clone", (Class []) null);
				newObject = method.invoke(object, (Object[]) null);
			}
			catch (Exception e)
			{
				ExceptionUtil.printTrace(e);
				throw new CloneNotSupportedException("Cannot clone object of type '" + object.getClass().getName() + "'");
			}
		}
		else if (object instanceof Serializable)
		{
			try
			{
				// Prepare in-memory stream
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);

				// Perform the actual serialization.
				oos.writeObject(object);

				oos.flush();
				oos.close();

				// Return the result bytes
				byte [] bytes = bos.toByteArray();

				// Setup an object input stream.
				ClassLoaderObjectInputStream ois = new ClassLoaderObjectInputStream(new ByteArrayInputStream(bytes));
				ois.setClassLoader(classLoader);

				// Create a new token context and deserialize it from the stream
				newObject = ois.readObject();
			}
			catch (IOException e)
			{
				ExceptionUtil.printTrace(e);
				throw new CloneNotSupportedException("Cannot clone object of type '" + object.getClass().getName() + "'");
			}
			catch (ClassNotFoundException e)
			{
				ExceptionUtil.printTrace(e);
				throw new CloneNotSupportedException("Cannot clone object of type '" + object.getClass().getName() + "'");
			}
		}
		else
		{
			throw new CloneNotSupportedException("Cannot clone object of type '" + object.getClass().getName() + "'");
		}

		return newObject;
	}

	/**
	 * Creates a copy of a collection object.
	 *
	 * @param source The collection to copy or null
	 * @param copyMode Determines if a first level copy or a deep copy is to be
	 * performed ({@link #CLONE_NONE}/{@link #CLONE_VALUES}/{@link #CLONE_ALL})
	 * @return The copied map or null
	 * @throws CloneNotSupportedException If either the collection object or one of its value cannot be cloned
	 */
	public static Collection copyCollection(Collection source, int copyMode)
		throws CloneNotSupportedException
	{
		if (source == null)
			return null;

		Collection copy;
		try
		{
			copy = source.getClass().newInstance();
		}
		catch (Exception e)
		{
			throw new CloneNotSupportedException("Cannot clone object of type '" + source.getClass().getName() + "'");
		}

		Iterator itElements = source.iterator();
		while (itElements.hasNext())
		{
			Object element = itElements.next();

			if ((copyMode & CLONE_VALUES) != 0)
				element = copyObject(element, Copyable.COPY_DEEP, null);

			copy.add(element);
		}

		return copy;
	}

	/**
	 * Creates a copy of a map object.
	 *
	 * @param source The map to copy or null
	 *
	 * @param copyMode Determines if a first level copy or a deep copy is to be
	 * performed ({@link #CLONE_NONE}/{@link #CLONE_KEYS}/{@link #CLONE_VALUES}/{@link #CLONE_ALL})
	 *
	 * @return The copied map or null
	 * @throws CloneNotSupportedException If either the map object or one of its keys or value cannot be cloned
	 */
	public static Map copyMap(Map source, int copyMode)
		throws CloneNotSupportedException
	{
		if (source == null)
			return null;

		Map copy;
		try
		{
			copy = source.getClass().newInstance();
		}
		catch (Exception e)
		{
			throw new CloneNotSupportedException("Cannot clone object of type '" + source.getClass().getName() + "'");
		}

		Iterator itValues = source.values().iterator();
		Iterator itKeys = source.keySet().iterator();
		while (itValues.hasNext())
		{
			Object key = itKeys.next();
			Object value = itValues.next();

			if ((copyMode & CLONE_KEYS) != 0)
				key = copyObject(key, Copyable.COPY_DEEP, null);
			if ((copyMode & CLONE_VALUES) != 0)
				value = copyObject(value, Copyable.COPY_DEEP, null);

			copy.put(key, value);
		}

		return copy;
	}
}
