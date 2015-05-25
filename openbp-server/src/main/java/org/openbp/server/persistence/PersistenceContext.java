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

import java.util.Collection;

/**
 * Context object that contains information regarding access to a persistence store.
 * The implementation of this object depends on the type of persistence store.
 * For example, it can encapsulate a JDBC connection or an O/R mapper session.
 *
 * This object should be rather lightweight than heavyweight.
 *
 * @author Heiko Erhardt
 */
public interface PersistenceContext
{
	//////////////////////////////////////////////////
	// @@ General
	//////////////////////////////////////////////////

	/**
	 * Gets the persistence context provider that created this context.
	 * @nowarn
	 */
	public PersistenceContextProvider getPersistenceContextProvider();

	/**
	 * Releases the context.
	 * The underlying database session will be closed.
	 */
	public void release();

	/**
	 * Determines if the given class is managed by this persistence context.
	 *
	 * @param cls Class to check (usually a class implementing PeristentObject)
	 * @return The persistence status of the class
	 */
	public boolean isPersistentClass(Class cls);

	//////////////////////////////////////////////////
	// @@ Object creation
	//////////////////////////////////////////////////

	/**
	 * Creates a new object of the given type.
	 *
	 * @param cls Type of object to create (usually a class implementing PeristentObject)
	 * @return The new object
	 * @throws PersistenceException On error
	 */
	public Object createObject(Class cls)
		throws PersistenceException;

	//////////////////////////////////////////////////
	// @@ Object access
	//////////////////////////////////////////////////

	/**
	 * Gets the primary key of the given object.
	 *
	 * @param obj The object
	 * @return The primary key or null if the object is not persistent or if it has not been inserted into the database yet
	 * @throws PersistenceException On error
	 */
	public Object getObjectId(Object obj)
		throws PersistenceException;

	/**
	 * Merges the (transient) given object with the current session and returns the persistent object.
	 * Reloads the object values from the database.
	 *
	 * @param obj Object to refresh
	 * @return The merged object
	 * @throws PersistenceException On error
	 */
	public Object merge(Object obj)
		throws PersistenceException;

	/**
	 * Refreshes the given persistent object.
	 * Reloads the object values from the database.
	 *
	 * @param obj Object to refresh
	 * @throws PersistenceException On error
	 */
	public void refreshObject(Object obj)
		throws PersistenceException;

	/**
	 * Removes the given persistent object from the session cache.
	 *
	 * @param obj Object to evict
	 * @throws PersistenceException On error
	 */
	public void evict(Object obj)
		throws PersistenceException;

	/**
	 * Finds an object by its primary key.
	 *
	 * @param id Primary key
	 * @param cls Type of object to lookup (usually a class implementing PeristentObject)
	 * @return The object or null if no such object can be found
	 * @throws PersistenceException On error
	 */
	public Object findById(Object id, Class cls)
		throws PersistenceException;

	/**
	 * Creates a query descriptor object for the given object class.
	 *
	 * @param cls Cls
	 * @return The new query descriptor object
	 */
	public PersistenceQuery createQuery(Class cls);

	/**
	 * Returns a list of all objects matching the specified query.
	 *
	 * @param query Query descriptor
	 * @return The list or null
	 * @throws PersistenceException On error
	 */
	public Collection runQuery(PersistenceQuery query)
		throws PersistenceException;

	//////////////////////////////////////////////////
	// @@ Object modification
	//////////////////////////////////////////////////

	/**
	 * Saves the given object to the persistent storage.
	 * According to the persistence state of the object, performs an update or insert.
	 *
	 * @param o Object to insert or update
	 * @return The updated object; usually this will be identical to the argument object,
	 * except if the object already exists in persistent storage and a merge operation needs to be performed.
	 * @throws PersistenceException On error
	 */
	public Object saveObject(Object o)
		throws PersistenceException;

	/**
	 * Deletes an object from persistent storage.
	 *
	 * @param o Object to delete
	 * @throws PersistenceException On error
	 */
	public void deleteObject(Object o)
		throws PersistenceException;

	//////////////////////////////////////////////////
	// @@ SQL support
	//////////////////////////////////////////////////

	/**
	 * Runs the given SQL update or delete statement.
	 *
	 * @param sql An SQL update statement
	 * @return The number of rows affected.
	 * @throws PersistenceException On error
	 */
	public int executeUpdateOrDelete(String sql)
		throws PersistenceException;

	/**
	 * Runs the given SQL select statement.
	 *
	 * @param sql SQL query to run
	 * @param maxResults Maximum number of result rows or 0 for unlimited
	 * @return A list of result elements (contains Object or Object[] elements, depending if this was a single column or multi-column query)
	 * @throws PersistenceException On error
	 */
	public Collection executeSelect(String sql, int maxResults)
		throws PersistenceException;

	//////////////////////////////////////////////////
	// @@ Transaction control
	//////////////////////////////////////////////////

	/**
	 * Checks if a transaction is currently in progress.
	 *
	 * @return true if transaction has been started and not yet committed or rolled back
	 */
	public boolean isTransactionActive()
		throws PersistenceException;

	/**
	 * Begins a new transaction.
	 * Does nothing if a transaction is already running.
	 */
	public void beginTransaction()
		throws PersistenceException;

	/**
	 * Reverts any changes that have occurred to objects within the transaction.
	 * Does nothing if no transaction is currently running.
	 */
	public void rollbackTransaction()
		throws PersistenceException;

	/**
	 * Commits any changes that have occurred to objects within the transaction.
	 * @throws PersistenceException On error performing the operations on the persistent storage
	 */
	public void commitTransaction()
		throws PersistenceException;

	/**
	 * Forces the underlying persistence session to flush.
	 * A flush will synchronize the underlying persistent storage with the object state in memory.
	 * However, a flush operation will not change the transaction state.
	 *
	 * @throws PersistenceException On error performing the operations on the persistent storage
	 */
	public void flush()
		throws PersistenceException;
}
