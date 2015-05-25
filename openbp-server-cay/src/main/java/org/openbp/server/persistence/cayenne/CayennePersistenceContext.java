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
package org.openbp.server.persistence.cayenne;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.DataObjectUtils;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.access.DataContext;
import org.apache.cayenne.access.DataDomain;
import org.apache.cayenne.access.ObjectStore;
import org.apache.cayenne.conf.Configuration;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.SQLTemplate;
import org.apache.cayenne.query.SelectQuery;
import org.openbp.common.logger.LogUtil;
import org.openbp.server.persistence.BasicPersistenceContext;
import org.openbp.server.persistence.BasicPersistenceContextProvider;
import org.openbp.server.persistence.DeferedOnLoadCollection;
import org.openbp.server.persistence.PersistenceCriterion;
import org.openbp.server.persistence.PersistenceException;
import org.openbp.server.persistence.PersistenceOrdering;
import org.openbp.server.persistence.PersistenceQuery;
import org.openbp.server.persistence.PersistentObjectNotFoundException;
import org.openbp.server.persistence.TransactionGuard;

/**
 * The Cayenne persistence context represents a Cayenne data context.
 *
 * @author Heiko Erhardt
 */
public class CayennePersistenceContext extends BasicPersistenceContext
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Cayenne configuration */
	private final Configuration cayenneConfiguration;

	/** Underlying Cayenne data context */
	private DataContext dataContext;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param cayenneConfiguration Cayenne configuration
	 * @param provider Persistence context provider
	 */
	public CayennePersistenceContext(final Configuration cayenneConfiguration, final BasicPersistenceContextProvider provider)
	{
		super(provider);

		this.cayenneConfiguration = cayenneConfiguration;
	}

	//////////////////////////////////////////////////
	// @@ General
	//////////////////////////////////////////////////

	/**
	 * Releases the context.
	 * The underlying database data context will be closed.
	 */
	public void release()
	{
		if (dataContext != null)
		{
			LogUtil.debug(getClass(), "Releasing Cayenne data context $0.", dataContext);

			// Nothing else to do...
			dataContext = null;
		}
		super.release();
	}

	/**
	 * Determines if the given class is managed by this persistence context.
	 *
	 * @param cls Class to check (usually a class implementing PeristentObject)
	 * @nowarn
	 */
	public boolean isPersistentClass(Class cls)
	{
		cls = getMappedObjectClass(cls, false);
		String className = cls.getName();
		for (Iterator itMaps = getDataContext().getEntityResolver().getDataMaps().iterator(); itMaps.hasNext();)
		{
			DataMap map = (DataMap) itMaps.next();
			for (Iterator itEntities = map.getObjEntities().iterator(); itEntities.hasNext();)
			{
				ObjEntity objEntity = (ObjEntity) itEntities.next();
				if (objEntity.getClassName().equals(className))
					return true;
			}
		}
		return false;
	}

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
		throws PersistenceException
	{
		cls = getMappedObjectClass(cls, true);
		Object ret = null;

		try
		{
			ret = getDataContext().newObject(cls);
		}
		catch (Exception e)
		{
			throw new PersistenceException("Error creating persistent object.", e.getCause());
		}

		checkObject(ret);

		getPersistenceContextProvider().fireOnCreate(ret, this);

		return ret;
	}

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
	public Object getObjectId(final Object obj)
		throws PersistenceException
	{
		checkObject(obj);
		Object id = ((DataObject) obj).getObjectId();
		return id;
	}

	/**
	 * Merges the (transient) given object with the current data context and returns the persistent object.
	 * Reloads the object values from the database.
	 *
	 * @param obj Object to refresh
	 * @throws PersistenceException On error
	 * @return The merged object
	 */
	public Object merge(final Object obj)
		throws PersistenceException
	{
		/* TODO Fix 3 We don't do a merge (Cayenne has no out-of-the-box functionality for this), so we simply do nothing currently.
		checkObject(obj);
		TransactionGuard tg = new TransactionGuard(this);
		try
		{
			Class cls = obj.getClass();
			Object id = getObjectId(obj);
			DataObject updatedObject = getDataContext().refetchObject(obj);
			return updatedObject;
		}
		catch (CayenneRuntimeException e)
		{
			tg.doCatch();
			String msg = LogUtil.error(getClass(), "Persistence error.", e);
			throw new PersistentObjectNotFoundException(msg, e);
		}
		finally
		{
			tg.doFinally();
		}
		 */
		return obj;
	}

	/**
	 * Refreshes the given persistent object.
	 * Reloads the object values from the database.
	 *
	 * @param obj Object to refresh
	 * @throws PersistenceException On error
	 */
	public void refreshObject(final Object obj)
		throws PersistenceException
	{
		if (obj != null)
		{
			checkObject(obj);
			TransactionGuard tg = new TransactionGuard(this);
			try
			{
				getDataContext().invalidateObjects(Collections.singletonList(obj));
			}
			catch (CayenneRuntimeException e)
			{
				tg.doCatch();

				// We assume that the object doesn't exist any more.
				String msg = LogUtil.error(getClass(), "Persistence error.", e);
				throw new PersistentObjectNotFoundException(msg, e);
			}
			finally
			{
				tg.doFinally();
			}
		}
	}

	/**
	 * Removes the given persistent object from the data context cache.
	 *
	 * @param obj Object to evict
	 * @throws PersistenceException On error
	 */
	public void evict(final Object obj)
		throws PersistenceException
	{
		if (obj != null)
		{
			checkObject(obj);
			getDataContext().invalidateObjects(Collections.singletonList(obj));
		}
	}

	/**
	 * Finds an object by its primary key.
	 *
	 * @param id Primary key
	 * @param cls Type of object to lookup (usually a class implementing PeristentObject)
	 * @return The object or null if no such object can be found
	 * @throws PersistenceException On error
	 */
	public Object findById(final Object id, Class cls)
		throws PersistenceException
	{
		cls = getMappedObjectClass(cls, false);
		Object ret = null;

		if (id instanceof ObjectId)
		{
			ret = DataObjectUtils.objectForPK(getDataContext(), (ObjectId) id);
		}
		else
		{
			ret = DataObjectUtils.objectForPK(getDataContext(), cls, id);
		}

		getPersistenceContextProvider().fireOnLoad(ret, this);

		return ret;
	}

	/**
	 * Returns a list of the objects of a particular type that match the given criterion.
	 *
	 * @param query Query to run
	 * @return The list or null
	 * @throws PersistenceException On error
	 */
	public Collection runQuery(final PersistenceQuery query)
		throws PersistenceException
	{
		TransactionGuard tg = new TransactionGuard(this);
		try
		{
			Expression exp = null;

			for (Iterator it = query.getCriterions(); it.hasNext();)
			{
				PersistenceCriterion criterion = (PersistenceCriterion) it.next();

				String property = criterion.getProperty();
				String operator = criterion.getOperator();
				Object value = criterion.getOperand();
				if (PersistenceCriterion.OPERATOR_EQ.equals(operator))
				{
					Expression newExp1 = ExpressionFactory.matchExp(property, value);
					Expression newExp2 = ExpressionFactory.matchExp(property, null);
					Expression newExp = newExp1.orExp(newExp2);

					exp = conjugate(newExp, exp);
				}
				else if (PersistenceCriterion.OPERATOR_EQ_OR_NULL.equals(operator))
				{
					Expression newExp = ExpressionFactory.matchExp(property, value);
					exp = conjugate(newExp, exp);
				}
				else if (PersistenceCriterion.OPERATOR_NEQ.equals(operator))
				{
					Expression newExp = ExpressionFactory.noMatchExp(property, value);
					exp = conjugate(newExp, exp);
				}
				else if (PersistenceCriterion.OPERATOR_GT.equals(operator))
				{
					Expression newExp = ExpressionFactory.noMatchExp(property, value);
					exp = conjugate(newExp, exp);
				}
				else if (PersistenceCriterion.OPERATOR_GTE.equals(operator))
				{
					Expression newExp = ExpressionFactory.noMatchExp(property, value);
					exp = conjugate(newExp, exp);
				}
				else if (PersistenceCriterion.OPERATOR_LT.equals(operator))
				{
					Expression newExp = ExpressionFactory.noMatchExp(property, value);
					exp = conjugate(newExp, exp);
				}
				else if (PersistenceCriterion.OPERATOR_LTE.equals(operator))
				{
					Expression newExp = ExpressionFactory.noMatchExp(property, value);
					exp = conjugate(newExp, exp);
				}
				else if (PersistenceCriterion.OPERATOR_LIKE.equals(operator))
				{
					Expression newExp = ExpressionFactory.likeExp(property, value);
					exp = conjugate(newExp, exp);
				}
				else if (PersistenceCriterion.OPERATOR_NULL.equals(operator))
				{
					Expression newExp = ExpressionFactory.matchExp(property, null);
					exp = conjugate(newExp, exp);
				}
				else if (PersistenceCriterion.OPERATOR_ALIAS.equals(operator))
					throw new PersistenceException("'alias' operation not supproted by Cayenne persistence criterion.");
			}

			Class cls = getMappedObjectClass(query.getObjectClass(), false);
			SelectQuery cq = new SelectQuery(cls, exp);

			if (query.getMaxResults() > 0)
			{
				cq.setFetchLimit(query.getMaxResults());
			}

			for (Iterator it = query.getOrderings(); it.hasNext();)
			{
				PersistenceOrdering ordering = (PersistenceOrdering) it.next();

				cq.addOrdering(ordering.getPropertyName(), ordering.isAscending());
			}

			// Run query and wrap result list into list that calls onLoad for each element that is being accessed.
			// Run query and wrap result list into a collection that calls onLoad for each element that is being accessed.
			List root = getDataContext().performQuery(cq);
			return new DeferedOnLoadCollection(root, this);
		}
		catch (Exception e)
		{
			tg.doCatch();
			throw createLoggedException(e);
		}
		finally
		{
			tg.doFinally();
		}
	}

	/**
	 * Creates a conjunction between 'exp1' and 'exp2'.
	 * If any is null, return the other.  
	 * @param exp1 Expression 1
	 * @param exp2 Expression 2
	 * @return The result expression
	 */
	private static Expression conjugate(final Expression exp1, final Expression exp2)
	{
		if (exp1 != null)
		{
			if (exp2 != null)
				return exp1.andExp(exp2);
			else
				return exp1;
		}
		else
			return exp2;
	}

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
	public Object saveObject(final Object o)
		throws PersistenceException
	{
		checkObject(o);
		TransactionGuard tg = new TransactionGuard(this);
		try
		{
			getPersistenceContextProvider().fireBeforeSave(o, this);

			// For Cayenne, we don't have to do anything here...

			getPersistenceContextProvider().fireAfterSave(o, this);

			return o;
		}
		catch (Exception e)
		{
			tg.doCatch();
			throw createLoggedException(e);
		}
		finally
		{
			tg.doFinally();
		}
	}

	/**
	 * Deletes an object from persistent storage.
	 *
	 * @param o Object to delete
	 * @throws PersistenceException On error
	 */
	public void deleteObject(final Object o)
		throws PersistenceException
	{
		checkObject(o);
		TransactionGuard tg = new TransactionGuard(this);
		try
		{
			getPersistenceContextProvider().fireBeforeDelete(o, this);

			getDataContext().deleteObject(o);

			getPersistenceContextProvider().fireAfterDelete(o, this);
		}
		catch (Exception e)
		{
			tg.doCatch();
			throw createLoggedException(e);
		}
		finally
		{
			tg.doFinally();
		}
	}

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
		throws PersistenceException
	{
		TransactionGuard tg = new TransactionGuard(this);
		try
		{
			SQLTemplate temp = new SQLTemplate();
			temp.setDefaultTemplate(sql);

			DataMap dataMap = getDataContext().getEntityResolver().getDataMaps().iterator().next();
			temp.setRoot(dataMap);

			int count = 0;
			int [] counts = getDataContext().performNonSelectingQuery(temp);
			if (counts.length > 0)
				count = counts[0];
			return count;
		}
		catch (Exception e)
		{
			tg.doCatch();
			throw createLoggedException(e);
		}
		finally
		{
			tg.doFinally();
		}
	}

	/**
	 * Runs the given SQL select statement.
	 *
	 * @param sql SQL query to run
	 * @param maxResults Maximum number of result rows or 0 for unlimited
	 * @return A list of result elements (contains Object or Object[] elements, depending if this was a single column or multi-column query)
	 * @throws PersistenceException On error
	 */
	public Collection executeSelect(String sql, int maxResults)
		throws PersistenceException
	{
		TransactionGuard tg = new TransactionGuard(this);
		try
		{
			SQLTemplate temp = new SQLTemplate();
			temp.setDefaultTemplate(sql);
			if (maxResults > 0)
			{
				temp.setFetchLimit(maxResults);
			}

			DataMap dataMap = getDataContext().getEntityResolver().getDataMaps().iterator().next();
			temp.setRoot(dataMap);

			Collection root = getDataContext().performQuery(temp);
			return new DeferedOnLoadCollection(root, this);
		}
		catch (Exception e)
		{
			tg.doCatch();
			throw createLoggedException(e);
		}
		finally
		{
			tg.doFinally();
		}
	}

	//////////////////////////////////////////////////
	// @@ Transaction control
	//////////////////////////////////////////////////

	/**
	 * Checks if a transaction is currently in progress.
	 *
	 * @return true if transaction has been started and not yet committed or rolled back
	 */
	public boolean isTransactionActive()
		throws PersistenceException
	{
		return getDataContext().hasChanges();
	}

	/**
	 * Begins a new transaction.
	 * Does nothing if a transaction is already running.
	 */
	public void beginTransaction()
		throws PersistenceException
	{
	}

	/**
	 * Reverts any changes that have occurred to objects within the transaction.
	 * Does nothing if no transaction is currently running.
	 */
	public void rollbackTransaction()
		throws PersistenceException
	{
		if (getDataContext().hasChanges())
		{
			LogUtil.trace(getClass(), "Rolling back transaction.");
			getDataContext().rollbackChanges();
		}
	}

	/**
	 * Commits any changes that have occurred to objects within the transaction.
	 * Does nothing if no transaction is currently running.
	 *
	 * @throws PersistenceException On error performing the operations on the persistent storage
	 */
	public void commitTransaction()
		throws PersistenceException
	{
		if (getDataContext().hasChanges())
		{
			LogUtil.trace(getClass(), "Committing transaction.");

			try
			{
				getDataContext().commitChanges();
				LogUtil.trace(getClass(), "Committed transaction.");
			}
			catch (Exception e)
			{
				throw createLoggedException(e);
			}
		}
	}

	/**
	 * Forces the underlying persistence data context to flush.
	 * A flush will synchronize the underlying persistent storage with the object state in memory.
	 * However, a flush operation will not change the transaction state.
	 *
	 * @throws PersistenceException On error performing the operations on the persistent storage
	 */
	public void flush()
		throws PersistenceException
	{
		try
		{
			getDataContext().commitChanges();
		}
		catch (Exception e)
		{
			throw createLoggedException(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ Cayenne-specific
	//////////////////////////////////////////////////

	/**
	 * Gets the underlying Cayenne data context object.
	 * @nowarn
	 */
	public DataContext getDataContext()
	{
		if (dataContext == null)
		{
			DataDomain domain = cayenneConfiguration.getDomain();
			dataContext = new DataContext(domain, new ObjectStore(domain.getSharedSnapshotCache()));

			dataContext.setValidatingObjectsOnCommit(true);

			LogUtil.debug(getClass(), "Opened Cayenne session $0.", dataContext);
		}

		return dataContext;
	}

	private void checkObject(final Object o)
	{
		if (! (o instanceof DataObject))
			throw new PersistenceException("Class '" + o.getClass().getName() + "' must implement the org.apache.cayenne.DataObject interface.");
	}
}
