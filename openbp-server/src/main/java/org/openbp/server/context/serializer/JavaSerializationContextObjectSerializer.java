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
import java.io.Serializable;

import org.openbp.common.logger.LogUtil;
import org.openbp.server.context.TokenContext;

/**
 * Context object serializer class that supports the regular Java serialization.
 * This should be used as last serializer in the serializer chain.
 *
 * @author Heiko Erhardt
 */
public class JavaSerializationContextObjectSerializer
	implements ContextObjectSerializer
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public JavaSerializationContextObjectSerializer()
	{
	}

	//////////////////////////////////////////////////
	// @@ ContextObjectSerializer implementation
	//////////////////////////////////////////////////

	/**
	 * @return true for java.io.Serializable objects
	 * @see org.openbp.server.context.serializer.ContextObjectSerializer#acceptsContextObject(Object object, TokenContext context)
	 */
	public boolean acceptsContextObject(Object object, TokenContext context)
	{
		if (object instanceof Serializable)
			return true;
		return false;
	}

	/**
	 * @see org.openbp.server.context.serializer.ContextObjectSerializer#writeContextObject(Object object, ObjectOutputStream out, TokenContext context, String key)
	 */
	public void writeContextObject(Object object, ObjectOutputStream out, TokenContext context, String key)
		throws IOException
	{
		if (!(object instanceof Serializable))
		{
			String msg = LogUtil.error(getClass(), "Process variable value $0 cannot be persisted because it does not implement the java.io.Serializable interface (value class: $1).", key, object.getClass().getName());
			throw new IOException(msg);
		}

		out.writeObject(object);
	}

	/**
	 * @see org.openbp.server.context.serializer.ContextObjectSerializer#readContextObject(ObjectInputStream in, TokenContext context, String key)
	 */
	public Object readContextObject(ObjectInputStream in, TokenContext context, String key)
		throws IOException, ClassNotFoundException
	{
		return in.readObject();
	}
}
