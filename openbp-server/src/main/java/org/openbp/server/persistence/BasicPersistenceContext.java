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
package org.openbp.server.persistence;

import org.openbp.common.ReflectException;
import org.openbp.common.ReflectUtil;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.registry.ClassMappingRegistry;
import org.openbp.common.util.ToStringHelper;

/**
 * Basic implementation of a persistence context.
 *
 * This object should be rather lightweight than heavyweight.
 *
 * @author Heiko Erhardt
 */
public abstract class BasicPersistenceContext
	implements PersistenceContext
{
	/** Persistence context provider that created this context */
	private BasicPersistenceContextProvider persistenceContextProvider;

	/**
	 * Constructor.
	 *
	 * @param persistenceContextProvider Persistence context provider
	 */
	public BasicPersistenceContext(BasicPersistenceContextProvider persistenceContextProvider)
	{
		this.persistenceContextProvider = persistenceContextProvider;
	}

	/**
	 * Releases the context.
	 * The underlying database session will be closed.
	 */
	public void release()
	{
		persistenceContextProvider.unbindThreadContext(this);
	}

	/**
	 * Returns a string representation of this object.
	 * @nowarn
	 */
	public String toString()
	{
		return ToStringHelper.toString(this);
	}

	/**
	 * Gets the persistence context provider that created this context.
	 * @nowarn
	 */
	public PersistenceContextProvider getPersistenceContextProvider()
	{
		return persistenceContextProvider;
	}

	/**
	 * Creates a query descriptor object for the given object class.
	 *
	 * @param cls Cls
	 * @return The new query descriptor object
	 */
	public PersistenceQuery createQuery(Class cls)
	{
		return new PersistenceQuery(this, cls);
	}

	/**
	 * Creates a new object of the given type.
	 *
	 * @param cls Type of object to create (usually a class implementing PeristentObject)
	 * @return The new object
	 * @throws PersistenceException On error
	 */
	public Object createObject(Class cls)
		throws PersistenceException
	{
		cls = getMappedObjectClass(cls, true);
		Object ret = null;
		try
		{
			ret = ReflectUtil.instantiate(cls, null, "persistent object");
		}
		catch (ReflectException e)
		{
			throw new PersistenceException("Error creating persistent object.", e.getCause());
		}

		getPersistenceContextProvider().fireOnCreate(ret, this);

		return ret;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Creates a persistence exception and logs it.
	 *
	 * @param cause Underlying database exception
	 * @return The exception
	 */
	protected PersistenceException createLoggedException(Throwable cause)
	{
		String msg = LogUtil.error(getClass(), "Persistence error.", cause);
		return new PersistenceException(msg, cause);
	}

	/**
	 * Gets the mapped class or the original class.
	 *
	 * @param cls Class to look up
	 * @param checkInterface true if the method should check that the resulting class is not an interface
	 * @return The mapped class if the class mapping registry of the persistence context provider
	 * defines a substitution for it, otherwise the original class argument itself.
	 */
	protected Class getMappedObjectClass(Class cls, boolean checkInterface)
	{
		Class newCls = null;

		ClassMappingRegistry classMapper = persistenceContextProvider.getClassMappingRegistry();
		if (classMapper != null)
		{
			newCls = classMapper.getMappedClass(cls);
		}

		if (newCls != null)
		{
			if (newCls.isInterface())
			{
				String msg = LogUtil.error(getClass(), "Class $0 mapped for $1 is an interface and cannot be instantiated.", newCls.getName(), cls.getName());
				throw new PersistenceException(msg);
			}
			cls = newCls;
		}
		else
		{
			if (cls.isInterface())
			{
				String msg = LogUtil.error(getClass(), "No class mapping configured for interface $0.", cls.getName());
				throw new PersistenceException(msg);
			}
		}

		return cls;
	}
}
