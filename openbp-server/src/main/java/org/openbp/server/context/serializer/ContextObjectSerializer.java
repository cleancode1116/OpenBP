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

import org.openbp.server.context.TokenContext;


/**
 * Serializer class for objects that are stored in the token context.
 * Serializers are kept in the {@link ContextObjectSerializerRegistry} and are used when serializing or deserializing parameter values in the token context.
 *
 * @author Heiko Erhardt
 */
public interface ContextObjectSerializer
{
	/**
	 * Determines if this serialize can serialize the given object.
	 *
	 * @param object Object to write or null
	 * @param context Context to be serialized/deserialized
	 * @return
	 *		true	The serialize is able to write this object.
	 *		false	The object should be processed by another serializer.
	 */
	public boolean acceptsContextObject(Object object, TokenContext context);

	/**
	 * Writes an object to an output stream.
	 *
	 * @param object Object to write
	 * @param out Output stream to write to
	 * @param context Context to be serialized/deserialized
	 * @param key Name of the context variable that is about to be deserialized
	 * @throws IOException On write error
	 */
	public void writeContextObject(Object object, ObjectOutputStream out, TokenContext context, String key)
		throws IOException;

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
	public Object readContextObject(ObjectInputStream in, TokenContext context, String key)
		throws IOException, ClassNotFoundException;
}
