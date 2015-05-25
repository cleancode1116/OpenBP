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

import org.openbp.common.generic.LifecycleSupport;


/**
 * The task of the persistence context provider is to provider a persistence context that can be used
 * to access/save process engine objects from/to the persistent storage.
 * Implementations will create persistence contexts if necessary.
 * The lifetime of a persistence context is subject of the provider.
 * A common pattern found in provider implementations is to used ThreadLocal objects to bind contexts to the current session.
 * The {@link PersistenceContext#release} method should notify the context provider in order to unbind the context again.
 *
 * @author Heiko Erhardt
 */
public interface PersistenceContextProvider
	extends LifecycleSupport
{
	/**
	 * Obtains a persistence context that can be used to persist process engine objects related to the given token context.
	 * Creates a new context if none exists for this thread.
	 *
	 * @return The persistence context
	 * @throws PersistenceException On error (e. g. if there is no database defined or a database session cannot be established)
	 */
	public PersistenceContext obtainPersistenceContext()
		throws PersistenceException;

	/**
	 * Obtains an existing persistence context that can be used to persist process engine objects related to the given token context.
	 *
	 * @return The persistence context or null if none exists
	 */
	public PersistenceContext obtainExistingPersistenceContext();

	/**
	 * Adds a lifecycle listener.
	 *
	 * @param listener Listener
	 */
	public void addEntityLifecycleListener(EntityLifecycleListener listener);

	/**
	 * Removes a lifecycle listener.
	 *
	 * @param listener Listener
	 */
	public void removeEntityLifecycleListener(EntityLifecycleListener listener);

	public void fireOnCreate(Object entity, PersistenceContext pc);
	public void fireOnLoad(Object entity, PersistenceContext pc);
	public void fireBeforeSave(Object entity, PersistenceContext pc);
	public void fireAfterSave(Object entity, PersistenceContext pc);
	public void fireBeforeDelete(Object entity, PersistenceContext pc);
	public void fireAfterDelete(Object entity, PersistenceContext pc);
}
