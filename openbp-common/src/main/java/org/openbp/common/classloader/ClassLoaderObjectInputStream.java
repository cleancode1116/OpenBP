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
package org.openbp.common.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;
import java.lang.reflect.Proxy;

/**
 * Object input stream for deserialization of objects that require a custom class loader.
 * Subclasses the regular object input stream, but uses a partilcar class loader for object construction.
 *
 * @author Heiko Erhardt
 */
public class ClassLoaderObjectInputStream extends ObjectInputStream
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Class loader for this stream */
	private ClassLoader classLoader;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Creates an object input stream that reads from the specified input stream.
	 *
	 * @param in  The underlying input stream from which to read
	 * @exception StreamCorruptedException The version or magic number are incorrect.
	 * @exception IOException An exception occurred in the underlying stream.
	 */
	public ClassLoaderObjectInputStream(InputStream in)
		throws IOException
	{
		super(in);
	}

	/**
	 * Gets the class loader for this stream.
	 * @nowarn
	 */
	public ClassLoader getClassLoader()
	{
		return classLoader;
	}

	/**
	 * Sets the class loader for this stream.
	 * @nowarn
	 */
	public void setClassLoader(ClassLoader classLoader)
	{
		this.classLoader = classLoader;
	}

	//////////////////////////////////////////////////
	// @@ Object input stream overrides
	//////////////////////////////////////////////////

	/**
	 * Load the local class equivalent of the specified stream class description.
	 * Uses the class loader for this stream.
	 *
	 * @param v  an instance of class ObjectStreamClass
	 * @return a Class object corresponding to <code>v</code>
	 * @exception IOException Any of the usual Input/Output exceptions.
	 * @exception ClassNotFoundException If class of
	 * a serialized object cannot be found.
	 */
	protected Class resolveClass(ObjectStreamClass v)
		throws IOException, ClassNotFoundException
	{
		ClassLoader loader = getClassLoader();
		return Class.forName(v.getName(), false, loader);
	}

	/**
	 * Returns a proxy class that implements the interfaces named in a
	 * proxy class descriptor; subclasses may implement this method to
	 * read custom data from the stream along with the descriptors for
	 * dynamic proxy classes, allowing them to use an alternate loading
	 * mechanism for the interfaces and the proxy class.
	 *
	 * @param	interfaces the list of interface names that were
	 *		deserialized in the proxy class descriptor
	 * @return  a proxy class for the specified interfaces
	 * @throws	IOException any exception thrown by the underlying
	 *		<code>InputStream</code>
	 * @throws	ClassNotFoundException if the proxy class or any of the
	 *		named interfaces could not be found
	 */
	protected Class resolveProxyClass(String [] interfaces)
		throws IOException, ClassNotFoundException
	{
		ClassLoader loader = getClassLoader();

		Class[] classObjs = new Class [interfaces.length];
		for (int i = 0; i < interfaces.length; i++)
		{
			classObjs [i] = Class.forName(interfaces [i], false, loader);
		}

		try
		{
			return Proxy.getProxyClass(loader, classObjs);
		}
		catch (IllegalArgumentException e)
		{
			throw new ClassNotFoundException(null, e);
		}
	}
}
