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
package org.openbp.server.context.serializer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.openbp.server.context.TokenContext;


/**
 * Context object serializer registry.
 *
 * @author Heiko Erhardt
 */
public final class ContextObjectSerializerRegistry
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** List of serializers (contains {@link ContextObjectSerializer} objects) */
	private List serializers;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public ContextObjectSerializerRegistry()
	{
		serializers = new ArrayList();
	}

	/**
	 * Writes an object to an output stream.
	 *
	 * @param object Object to write
	 * @param out Output stream to write to
	 * @param context Context to be serialized/deserialized
	 * @param key Name of the context variable that is about to be deserialized
	 * @throws IOException On write error
	 */
	public void writeObjectToOutputStream(Object object, ObjectOutputStream out, TokenContext context, String key)
		throws IOException
	{
		if (object == null)
		{
			out.writeObject(null);
			return;
		}

		int n = serializers.size();
		for (int i = 0; i < n; ++i)
		{
			ContextObjectSerializer ser = (ContextObjectSerializer) serializers.get(i);

			if (ser.acceptsContextObject(object, context))
			{
				// Write serialzier class name first
				out.writeObject(ser.getClass().getName());

				// Let the serialzier do its job
				ser.writeContextObject(object, out, context, key);

				return;
			}
		}

		throw new IOException("No context object serializer found for object of type " + object.getClass().getName() + ".");
	}

	/**
	 * Reads an object from an output stream.
	 * The output stream is guaranteed to be written by the writeObjectToOutputStream method of this class.
	 * However, it might be 
	 *
	 * @param in Input stream to read from
	 * @param context Context to be serialized/deserialized
	 * @param key Name of the context variable that is about to be deserialized
	 * @return The reconstituted object
	 * @throws IOException On read error
	 * @throws ClassNotFoundException If an object class could not be found
	 */
	public Object readObjectFromInputStream(ObjectInputStream in, TokenContext context, String key)
		throws IOException, ClassNotFoundException
	{
		String serializerClassName = (String) in.readObject();
		if (serializerClassName == null)
		{
			// Null value
			return null;
		}

		int n = serializers.size();
		for (int i = 0; i < n; ++i)
		{
			ContextObjectSerializer ser = (ContextObjectSerializer) serializers.get(i);

			if (ser.getClass().getName().equals(serializerClassName))
			{
				// Let the serialzier do its job
				return ser.readContextObject(in, context, key);
			}
		}

		throw new IOException("No context object serializer of type " + serializerClassName + " found.");
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Adds a serializer to the registry.
	 * The serializer will be appended to the serializer list, i. e. having the lowest priority.
	 *
	 * @param serializer Serializer to add
	 */
	public void addSerializer(ContextObjectSerializer serializer)
	{
		serializers.add(serializer);
	}

	/**
	 * Inserts a serializer into the registry.
	 *
	 * @param index Index at which the specified serializer is to be inserted
	 * @param serializer Serializer to add
	 */
	public void addSerializer(int index, ContextObjectSerializer serializer)
	{
		serializers.add(index, serializer);
	}

	/**
	 * Gets the list of serializers.
	 * For Spring framework support.
	 * @nowarn
	 */
	public List getSerializers()
	{
		return serializers;
	}

	/**
	 * Sets the list of serializers.
	 * For Spring framework support.
	 * @nowarn
	 */
	public void setSerializers(List serializers)
	{
		this.serializers = serializers;
	}
}
