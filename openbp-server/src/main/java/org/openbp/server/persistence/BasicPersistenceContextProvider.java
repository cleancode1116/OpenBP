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

import java.util.ArrayList;
import java.util.List;

import org.openbp.common.logger.LogUtil;
import org.openbp.common.registry.ClassMappingRegistry;
import org.openbp.common.util.ToStringHelper;

/**
 * Abstract implementation of a persistence context provider.
 * Uses a ThreadLocal to bind contexts to the current thread.
 * The final implementation needs to supply the {@link #createPersistenceContext} method.
 * Also provides the {@link #unbindThreadContext} method that contexts can call from their
 * {@link PersistenceContext#release} method.
 *
 * @author Heiko Erhardt
 */
public abstract class BasicPersistenceContextProvider
	implements PersistenceContextProvider
{
	/** Class mapper */
	private ClassMappingRegistry classMappingRegistry;

	/** Lifecycle listeners */
	private List <EntityLifecycleListener> listenerList = new ArrayList<EntityLifecycleListener> ();

	/** Thread local that holds the persistence context bound to this thread */
	protected ThreadLocal threadContext = new ThreadLocal();

	/**
	 * @seem LifecycleSupport.initialize()
	 */
	public void initialize()
	{
	}

	/**
	 * @seem LifecycleSupport.shutdown()
	 */
	public void shutdown()
	{
	}

	/**
	 * Returns a string representation of this object.
	 * @nowarn
	 */
	public String toString()
	{
		return ToStringHelper.toString(this);
	}

	//////////////////////////////////////////////////
	// @@ Persistence context management
	//////////////////////////////////////////////////

	/**
	 * Obtains a persistence context that can be used to persist process engine objects related to the given token context.
	 * Creates a new context if none exists for this thread.
	 *
	 * @return The persistence context
	 * @throws PersistenceException On error (e. g. if there is no database defined or a database session cannot be established)
	 */
	public PersistenceContext obtainPersistenceContext()
		throws PersistenceException
	{
		PersistenceContext context = (PersistenceContext) threadContext.get();
		if (context == null)
		{
			context = createPersistenceContext();
			threadContext.set(context);
			LogUtil.debug(getClass(), "Created thread persistence context $0.", context);
		}
		return context;
	}

	/**
	 * Obtains an existing persistence context that can be used to persist process engine objects related to the given token context.
	 *
	 * @return The persistence context or null if none exists
	 */
	public PersistenceContext obtainExistingPersistenceContext()
	{
		return (PersistenceContext) threadContext.get();
	}

	/**
	 * Sets the context to use for the current thread.
	 *
	 * @param context Context to release
	 */
	public void bindThreadContext(PersistenceContext context)
	{
		LogUtil.debug(getClass(), "Bound persistence context $0 to current thread..", context);
		threadContext.set(context);
	}

	/**
	 * Creates a new persistence context.
	 *
	 * @return The new context
	 * @throws PersistenceException On error (e. g. if there is no database defined or a database session cannot be established)
	 */
	protected abstract PersistenceContext createPersistenceContext()
		throws PersistenceException;

	/**
	 * Releases a persistence context obtained by the obtainPersistenceContext method.
	 * After calling this method, the context must not be used any more.
	 *
	 * @param context Context to release
	 */
	protected void unbindThreadContext(PersistenceContext context)
	{
		threadContext.set(null);
		LogUtil.debug(getClass(), "Unbound persistence context $0 from current thread.", context);
	}

	//////////////////////////////////////////////////
	// @@ Lifecycle support
	//////////////////////////////////////////////////

	/**
	 * Adds a lifecycle listener.
	 *
	 * @param listener Listener
	 */
	public void addEntityLifecycleListener(EntityLifecycleListener listener)
	{
		if (! listenerList.contains(listener))
		{
			listenerList.add(listener);
		}
	}

	/**
	 * Removes a lifecycle listener.
	 *
	 * @param listener Listener
	 */
	public void removeEntityLifecycleListener(EntityLifecycleListener listener)
	{
		listenerList.remove(listener);
	}

	public void fireOnCreate(Object entity, PersistenceContext pc)
	{
		if (entity instanceof PersistentObject)
		{
			((PersistentObject) entity).onCreate();
		}

		EntityLifecycleEvent event = new EntityLifecycleEvent(entity, pc);
		for (EntityLifecycleListener listener : listenerList)
		{
			listener.onCreate(event);
		}
	}

	public void fireOnLoad(Object entity, PersistenceContext pc)
	{
		if (entity instanceof PersistentObject)
		{
			((PersistentObject) entity).onLoad();
		}

		EntityLifecycleEvent event = new EntityLifecycleEvent(entity, pc);
		for (EntityLifecycleListener listener : listenerList)
		{
			listener.onLoad(event);
		}
	}

	public void fireBeforeSave(Object entity, PersistenceContext pc)
	{
		if (entity instanceof PersistentObject)
		{
			((PersistentObject) entity).beforeSave();
		}

		EntityLifecycleEvent event = new EntityLifecycleEvent(entity, pc);
		for (EntityLifecycleListener listener : listenerList)
		{
			listener.beforeSave(event);
		}
	}

	public void fireAfterSave(Object entity, PersistenceContext pc)
	{
		if (entity instanceof PersistentObject)
		{
			((PersistentObject) entity).afterSave();
		}

		EntityLifecycleEvent event = new EntityLifecycleEvent(entity, pc);
		for (EntityLifecycleListener listener : listenerList)
		{
			listener.afterSave(event);
		}
	}

	public void fireBeforeDelete(Object entity, PersistenceContext pc)
	{
		if (entity instanceof PersistentObject)
		{
			((PersistentObject) entity).beforeDelete();
		}

		EntityLifecycleEvent event = new EntityLifecycleEvent(entity, pc);
		for (EntityLifecycleListener listener : listenerList)
		{
			listener.beforeDelete(event);
		}
	}

	public void fireAfterDelete(Object entity, PersistenceContext pc)
	{
		if (entity instanceof PersistentObject)
		{
			((PersistentObject) entity).afterDelete();
		}

		EntityLifecycleEvent event = new EntityLifecycleEvent(entity, pc);
		for (EntityLifecycleListener listener : listenerList)
		{
			listener.afterDelete(event);
		}
	}

	//////////////////////////////////////////////////
	// @@ Class mapping registry
	//////////////////////////////////////////////////

	/**
	 * Gets the class mapper.
	 * Can be used to twist business object implementation class names (e. g. for custom implementations of the OpenBP standard entities).
	 * @nowarn
	 */
	public ClassMappingRegistry getClassMappingRegistry()
	{
		return classMappingRegistry;
	}

	/**
	 * Sets the class mapper.
	 * Can be used to twist business object implementation class names (e. g. for custom implementations of the OpenBP standard entities).
	 * @nowarn
	 */
	public void setClassMappingRegistry(ClassMappingRegistry classMappingRegistry)
	{
		this.classMappingRegistry = classMappingRegistry;
	}
}
