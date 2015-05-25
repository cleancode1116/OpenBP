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

import org.openbp.common.ReflectUtil;
import org.openbp.common.logger.LogUtil;
import org.openbp.core.engine.EngineException;
import org.openbp.server.context.TokenContext;
import org.openbp.server.persistence.PersistenceContext;
import org.openbp.server.persistence.PersistenceContextProvider;

/**
 * Context object serializer class that supports the regular Java serialization.
 * This should be used as last serializer in the serializer chain.
 *
 * @author Heiko Erhardt
 */
public class PersistenceContextObjectSerializer extends JavaSerializationContextObjectSerializer
	implements ContextObjectSerializer
{
	/** Persistence context provider */
	private PersistenceContextProvider persistenceContextProvider;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public PersistenceContextObjectSerializer()
	{
	}

	/**
	 * Gets the persistence context provider.
	 * @nowarn
	 */
	public PersistenceContextProvider getPersistenceContextProvider()
	{
		return persistenceContextProvider;
	}

	/**
	 * Sets the persistence context provider.
	 * @nowarn
	 */
	public void setPersistenceContextProvider(PersistenceContextProvider persistenceContextProvider)
	{
		this.persistenceContextProvider = persistenceContextProvider;
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
		return isSerializableObject(object, getPersistenceContextProvider());
	}

	/**
	 * @see org.openbp.server.context.serializer.ContextObjectSerializer#writeContextObject(Object object, ObjectOutputStream out, TokenContext context, String key)
	 */
	public void writeContextObject(Object object, ObjectOutputStream out, TokenContext context, String key)
		throws IOException
	{
		Object objectToSerialize = object;

		objectToSerialize = createSerializableObjectReference(objectToSerialize, context, key, getPersistenceContextProvider());

		super.writeContextObject(objectToSerialize, out, context, key);
	}

	/**
	 * @see org.openbp.server.context.serializer.ContextObjectSerializer#readContextObject(ObjectInputStream in, TokenContext context, String key)
	 */
	public Object readContextObject(ObjectInputStream in, TokenContext context, String key)
		throws IOException, ClassNotFoundException
	{
		Object value = super.readContextObject(in, context, key);

		value = resolveSerializableObjectReference(value, context, key, getPersistenceContextProvider());

		return value;
	}

	public static boolean isSerializableObject(Object object, PersistenceContextProvider pcp)
	{
		if (object != null)
		{
			PersistenceContext pc = pcp.obtainPersistenceContext();

			if (pc != null)
			{
				if (pc.isPersistentClass(object.getClass()))
					return true;
			}
		}
		return false;
	}

	public static Object createSerializableObjectReference(Object object, TokenContext context, String key, PersistenceContextProvider pcp)
	{
		Object ret = object;

		if (ret != null)
		{
			PersistenceContext pc = pcp.obtainPersistenceContext();

			if (pc != null)
			{
				if (pc.isPersistentClass(ret.getClass()))
				{
					// Make sure the object is associated with the session
					Object newObject = pc.merge(ret);
					if (newObject != ret)
					{
						context.setParamValue(key, newObject);
						ret = newObject;
					}

					// Get the id of the object
					Object ident = pc.getObjectId(ret);
					if (ident == null)
					{
						// Object is new, let's do a flush
						LogUtil.debug(PersistenceContextObjectSerializer.class, "Performing persistence context flush in order to serialize id of object $0 (variable $1).", object, key);
						pc.flush();

						ident = pc.getObjectId(ret);
					}

					if (ident != null)
					{
						// Save the reference instead of the object
						ret = new PersistentObjectReference(object.getClass(), ident);
					}
					else
					{
						// Otherwise we consider the object to be transient and just save it by regular serialization
						LogUtil.warn(PersistenceContextObjectSerializer.class, "Serializing object of type $0 (key $1) as transient object due to missing object id. Maybe PersistenceContext.saveObject was not called on the object.", object.getClass().getName(), key);
					}
				}
			}
		}

		return ret;
	}

	public static Object resolveSerializableObjectReference(Object object, TokenContext context, String key, PersistenceContextProvider pcp)
	{
		Object ret = object;

		if (ret != null && ret instanceof PersistentObjectReference)
		{
			PersistentObjectReference por = (PersistentObjectReference) ret;
			Object id = por.getObjectId();
			Class cls = ReflectUtil.loadClass(por.getClassName());

			PersistenceContext pc = pcp.obtainPersistenceContext();
			if (pc == null)
			{
				String msg = LogUtil.error(PersistenceContextObjectSerializer.class, "Error obtaining persistence context for deserialization of a persistent object of type $0 (id: $1, variable: $2). [{3}]", cls.getName(), id, key, context);
				throw new EngineException("ContextDeserialization", msg);
			}

			Object loaded = pc.findById(id, cls);
			if (loaded == null)
			{
				String msg = LogUtil.error(PersistenceContextObjectSerializer.class, "Persistent object of type $0 not found when deserializing token (id: $1, variable: $2). [{3}]", cls.getName(), id, key, context);
				throw new EngineException("ContextDeserialization", msg);
			}

			ret = loaded;
		}

		return ret;
	}

	//////////////////////////////////////////////////
	// @@ Private class
	//////////////////////////////////////////////////

	/**
	 * This container class denotes a reference to a persistent object.
	 *
	 * @author Heiko Erhardt
	 */
	private static class PersistentObjectReference
		implements Serializable
	{
		static final long serialVersionUID = 5005715612921697992L;
		
		/** Class name of the peristent object */
		private String className;

		/** Primary key */
		private Object objectId;

		/**
		 * Default constructor.
		 *
		 * @param cls Class of the object to reference
		 * @param id Primary key of the object
		 */
		public PersistentObjectReference(Class cls, Object id)
		{
			className = cls.getName();
			this.objectId = id;
		}

		/**
		 * Gets the class name of the peristent object.
		 * @nowarn
		 */
		public String getClassName()
		{
			return className;
		}

		/**
		 * Gets the primary key.
		 * @nowarn
		 */
		public Object getObjectId()
		{
			return objectId;
		}
	}
}
